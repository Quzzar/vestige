package com.quzzar.vestige.travel;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** World-owned directory, including endpoints whose chunks are currently unloaded. */
public final class StoneDirectory extends SavedData {
    private static final Factory<StoneDirectory> FACTORY = new Factory<>(StoneDirectory::new, StoneDirectory::load);
    private final StoneNetwork network = new StoneNetwork();
    public static StoneDirectory get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, "vestige_standing_stones");
    }
    public StoneNetwork network() { return network; }
    public void put(StoneNetwork.Node node) { if (network.put(node)) setDirty(); }
    public void remove(UUID id) { if (network.remove(id)) setDirty(); }
    public static StoneDirectory load(CompoundTag tag, HolderLookup.Provider registries) {
        StoneDirectory result = new StoneDirectory();
        for (Tag value : tag.getList("stones", Tag.TAG_COMPOUND)) {
            CompoundTag node = (CompoundTag) value;
            ResourceLocation dimension = ResourceLocation.tryParse(node.getString("dimension"));
            try {
                if (dimension != null && node.hasUUID("id")) result.network.put(new StoneNetwork.Node(node.getUUID("id"),
                        node.getString("key"), dimension, BlockPos.of(node.getLong("position")), node.getString("name")));
            } catch (IllegalArgumentException invalid) { /* A malformed entry is not a valid destination. */ }
        }
        return result;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (StoneNetwork.Node node : network.all()) {
            CompoundTag entry = new CompoundTag(); entry.putUUID("id", node.id()); entry.putString("key", node.key());
            entry.putString("dimension", node.dimension().toString()); entry.putLong("position", node.position().asLong()); entry.putString("name", node.name()); list.add(entry);
        }
        tag.putInt("version", 1); tag.put("stones", list); return tag;
    }
}
