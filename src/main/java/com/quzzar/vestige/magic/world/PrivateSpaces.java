package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Persistent private-space ownership and return points, independent of player progression. */
final class PrivateSpaces extends SavedData {
    static final ResourceKey<Level> DIMENSION = ResourceKey.create(Registries.DIMENSION, VestigeMainMod.location("pocket"));
    private final Map<UUID, Integer> rooms = new LinkedHashMap<>();
    private int next;
    private static final Factory<PrivateSpaces> FACTORY = new Factory<>(PrivateSpaces::new, PrivateSpaces::load);
    private PrivateSpaces() { }
    private static PrivateSpaces load(CompoundTag tag, HolderLookup.Provider registries) {
        PrivateSpaces data = new PrivateSpaces(); data.next = tag.getInt("next");
        for (Tag entry : tag.getList("rooms", Tag.TAG_COMPOUND)) {
            CompoundTag room = (CompoundTag) entry; data.rooms.put(room.getUUID("owner"), room.getInt("index"));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        rooms.forEach((owner, index) -> { CompoundTag room = new CompoundTag(); room.putUUID("owner", owner); room.putInt("index", index); list.add(room); });
        tag.put("rooms", list); tag.putInt("next", next); return tag;
    }
    static boolean visit(ServerPlayer player) {
        return visit(player, DIMENSION);
    }
    // Parameterized backing world also permits deterministic tests in GameTestServer,
    // whose flat preset intentionally omits data-pack dimensions.
    static boolean visit(ServerPlayer player, ResourceKey<Level> dimension) {
        ServerLevel pocket = player.getServer().getLevel(dimension); if (pocket == null) return false;
        CompoundTag persisted = player.getPersistentData();
        if (player.level().dimension().equals(dimension)) {
            ResourceLocation id = ResourceLocation.tryParse(persisted.getString("vestige:return_dimension"));
            ServerLevel back = id == null ? player.getServer().overworld() : player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
            if (back == null) back = player.getServer().overworld();
            double x = persisted.contains("vestige:return_x") ? persisted.getDouble("vestige:return_x") : back.getSharedSpawnPos().getX() + 0.5;
            double y = persisted.contains("vestige:return_y") ? persisted.getDouble("vestige:return_y") : back.getSharedSpawnPos().getY() + 1;
            double z = persisted.contains("vestige:return_z") ? persisted.getDouble("vestige:return_z") : back.getSharedSpawnPos().getZ() + 0.5;
            player.teleportTo(back, x, y, z, Set.of(), player.getYRot(), player.getXRot()); return true;
        }
        PrivateSpaces data = player.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, "vestige_private_spaces");
        boolean fresh = !data.rooms.containsKey(player.getUUID());
        int room = data.rooms.computeIfAbsent(player.getUUID(), ignored -> data.next++); data.setDirty();
        BlockPos center = center(room);
        if (fresh) {
            for (int x = -16; x <= 16; x++) for (int z = -16; z <= 16; z++) {
                pocket.setBlock(center.offset(x, -1, z), Blocks.SMOOTH_QUARTZ.defaultBlockState(), 3);
                if (Math.abs(x) == 16 || Math.abs(z) == 16) for (int y = 0; y < 4; y++) pocket.setBlock(center.offset(x, y, z), Blocks.BARRIER.defaultBlockState(), 3);
            }
        }
        persisted.putString("vestige:return_dimension", player.level().dimension().location().toString());
        persisted.putDouble("vestige:return_x", player.getX()); persisted.putDouble("vestige:return_y", player.getY()); persisted.putDouble("vestige:return_z", player.getZ());
        player.stopRiding(); player.teleportTo(pocket, center.getX() + 0.5, center.getY(), center.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot()); return true;
    }
    static void enforce(ServerPlayer player) {
        enforce(player, DIMENSION);
    }
    static void enforce(ServerPlayer player, ResourceKey<Level> dimension) {
        if (!player.level().dimension().equals(dimension)) return;
        PrivateSpaces data = player.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, "vestige_private_spaces");
        Integer room = data.rooms.get(player.getUUID());
        if (room == null) { visit(player, dimension); return; }
        BlockPos center = center(room);
        if (Math.abs(player.getX() - center.getX()) > 16 || Math.abs(player.getZ() - center.getZ()) > 16 || player.getY() < 1)
            player.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
    }
    private static BlockPos center(int index) { return new BlockPos((index % 10000) * 96, 8, (index / 10000) * 96); }
}
