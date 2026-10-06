package com.quzzar.vestige.storage;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Existing vanilla slot synchronization carries bounded, display-only bundle previews. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class CraneBagAccess {
    private CraneBagAccess() { }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) refresh(player);
    }
    static void refresh(ServerPlayer player) {
        var storage = CraneBagStorage.get(player.getServer()); boolean changed = false;
        for (var stack : player.getInventory().items) changed |= refresh(stack, storage);
        for (var stack : player.getInventory().offhand) changed |= refresh(stack, storage);
        for (var stack : player.getInventory().armor) changed |= refresh(stack, storage);
        for (var slot : player.containerMenu.slots) changed |= refresh(slot.getItem(), storage);
        changed |= refresh(player.containerMenu.getCarried(), storage);
        if (changed) {
            player.getInventory().setChanged(); player.containerMenu.broadcastChanges();
            if (player.containerMenu != player.inventoryMenu) player.inventoryMenu.broadcastChanges();
        }
    }
    static void refresh(ServerPlayer player, ItemStack activeBag) {
        // Also correct a modified snapshot after an action whose authoritative contents did not change.
        CustomData.update(DataComponents.CUSTOM_DATA, activeBag, tag -> tag.remove("vestige_crane_preview"));
        refresh(player);
    }
    private static boolean refresh(ItemStack bag, CraneBagStorage storage) {
        var key = CraneBagItem.key(bag); if (key.isEmpty()) return false;
        var data = bag.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        var preview = data.getCompound("vestige_crane_preview"); long revision = storage.revision(key.get());
        if (preview.hasUUID("session") && storage.session.equals(preview.getUUID("session"))
                && key.get().equals(preview.getString("key")) && preview.getLong("revision") == revision) return false;
        bag.set(DataComponents.BUNDLE_CONTENTS, storage.contents(key.get()));
        CustomData.update(DataComponents.CUSTOM_DATA, bag, tag -> {
            var current = new net.minecraft.nbt.CompoundTag(); current.putUUID("session", storage.session); current.putLong("revision", revision);
            current.putString("key", key.get());
            tag.put("vestige_crane_preview", current);
        });
        return true;
    }
}
