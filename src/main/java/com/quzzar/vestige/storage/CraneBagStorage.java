package com.quzzar.vestige.storage;

import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.commons.lang3.math.Fraction;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

/** One authoritative pool per full key, shared across dimensions of this server save. */
public final class CraneBagStorage extends SavedData {
    static final int MAX_PREVIEW_BYTES = 32_768;
    private static final Factory<CraneBagStorage> FACTORY = new Factory<>(CraneBagStorage::new, CraneBagStorage::load);
    private record Pool(BundleContents contents, long revision) { }
    private final Map<String, Pool> pools = new HashMap<>();
    private final Set<String> editing = new HashSet<>();
    final UUID session = UUID.randomUUID();

    public static CraneBagStorage get(MinecraftServer server) {
        if (!server.isSameThread()) throw new IllegalStateException("Crane Bag access must run on the server thread");
        return open(server.overworld().getDataStorage(), server.getWorldPath(LevelResource.ROOT).resolve("data/vestige_crane_bags.dat"));
    }
    static CraneBagStorage open(DimensionDataStorage data, Path file) {
        var existing = data.get(FACTORY, "vestige_crane_bags");
        if (existing != null) return existing;
        // Minecraft catches loader exceptions and returns null. Never replace an unreadable existing pool file.
        if (Files.exists(file)) throw new IllegalStateException("Existing Crane Bag storage could not be loaded: " + file);
        return data.computeIfAbsent(FACTORY, "vestige_crane_bags");
    }
    long revision(String key) { var pool = pools.get(key); return pool == null ? 0 : pool.revision(); }
    BundleContents contents(String key) {
        var pool = pools.get(key);
        return pool == null ? BundleContents.EMPTY : copy(pool.contents());
    }
    static BundleContents copy(BundleContents contents) { return new BundleContents(contents.itemCopyStream().toList()); }

    /** No client snapshot is read here. A lock also rejects reentrant access from slot/drop callbacks. */
    boolean edit(String key, HolderLookup.Provider registries, Function<BundleContents, BundleContents> operation) {
        if (!CraneBagItem.validKey(key) || !editing.add(key)) return false;
        try {
            BundleContents before = contents(key);
            BundleContents after = operation.apply(before);
            if (after == null || after.equals(before)) return false;
            // Operations validate candidate contents before changing external inventories/entities.
            if (!valid(after, registries)) throw new IllegalStateException("Invalid Crane Bag transaction result");
            pools.put(key, new Pool(copy(after), revision(key) + 1));
            setDirty();
            return true;
        } finally { editing.remove(key); }
    }
    static boolean accepts(ItemStack item) {
        return !item.isEmpty() && item.getItem().canFitInsideContainerItems()
                && !item.has(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS)
                && !item.has(net.minecraft.core.component.DataComponents.CONTAINER);
    }
    static boolean valid(BundleContents contents, HolderLookup.Provider registries) {
        if (contents.size() > 64 || contents.weight().compareTo(Fraction.ONE) > 0
                || contents.weight().compareTo(Fraction.ZERO) < 0) return false;
        for (var item : contents.items()) {
            if (!accepts(item) || item.getCount() > item.getMaxStackSize()) return false;
        }
        var encoded = encode(contents, registries).result();
        if (encoded.isEmpty()) return false;
        try (var bytes = new ByteArrayOutputStream(); var out = new DataOutputStream(bytes)) {
            NbtIo.write(encoded.get(), out);
            return bytes.size() <= MAX_PREVIEW_BYTES;
        } catch (IOException invalid) { return false; }
    }
    private static DataResult<CompoundTag> encode(BundleContents contents, HolderLookup.Provider registries) {
        return BundleContents.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), contents)
                .map(value -> { var tag = new CompoundTag(); tag.put("contents", value); return tag; });
    }
    public static CraneBagStorage load(CompoundTag tag, HolderLookup.Provider registries) {
        var result = new CraneBagStorage();
        if (!tag.contains("version", Tag.TAG_INT) || tag.getInt("version") != 1 || !tag.contains("pools", Tag.TAG_LIST))
            throw new IllegalStateException("Unsupported or malformed Crane Bag storage");
        var list = (ListTag) tag.get("pools");
        if (!list.isEmpty() && list.getElementType() != Tag.TAG_COMPOUND) throw new IllegalStateException("Malformed Crane Bag pool list");
        for (var value : list) {
            var entry = (CompoundTag) value; String key = entry.getString("key");
            if (!entry.contains("key", Tag.TAG_STRING) || !entry.contains("revision", Tag.TAG_LONG)
                    || entry.getLong("revision") < 0 || !entry.contains("contents", Tag.TAG_LIST))
                throw new IllegalStateException("Malformed Crane Bag pool entry");
            var contents = BundleContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), entry.get("contents"))
                    .getOrThrow(message -> new IllegalStateException("Cannot load Crane Bag contents: " + message));
            if (!CraneBagItem.validKey(key) || result.pools.containsKey(key) || !valid(contents, registries))
                throw new IllegalStateException("Invalid Crane Bag pool in world save");
            result.pools.put(key, new Pool(copy(contents), entry.getLong("revision")));
        }
        return result;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var list = new ListTag();
        pools.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(pool -> {
            var entry = encode(pool.getValue().contents(), registries)
                    .getOrThrow(message -> new IllegalStateException("Cannot save Crane Bag contents: " + message));
            entry.putString("key", pool.getKey()); entry.putLong("revision", pool.getValue().revision()); list.add(entry);
        });
        tag.putInt("version", 1); tag.put("pools", list); return tag;
    }
}
