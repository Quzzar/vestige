package com.quzzar.vestige.storage;

import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.apparatus.AttunementShardItem;
import com.quzzar.vestige.apparatus.ScrollItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.*;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.Level;
import java.util.*;

/** Vanilla bundle presentation, with server-owned transfers rather than transferable item snapshots. */
public final class CraneBagItem extends BundleItem {
    public CraneBagItem(Properties properties) { super(properties.component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)); }
    static boolean validKey(String key) { return key != null && key.matches("[0-9a-f]{64}"); }
    public static Optional<String> key(ItemStack bag) {
        if (!bag.is(ScrollItems.CRANE_BAG.get()) || bag.getCount() != 1) return Optional.empty();
        var tag = bag.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompound("vestige_crane_bag");
        return tag.getInt("version") == 1 && validKey(tag.getString("key")) ? Optional.of(tag.getString("key")) : Optional.empty();
    }
    public static ItemStack bound(String key) {
        if (!validKey(key)) throw new IllegalArgumentException("Invalid attunement key");
        var bag = new ItemStack(ScrollItems.CRANE_BAG.get());
        CustomData.update(DataComponents.CUSTOM_DATA, bag, tag -> {
            var binding = new net.minecraft.nbt.CompoundTag(); binding.putInt("version", 1); binding.putString("key", key);
            tag.put("vestige_crane_bag", binding);
        });
        return bag;
    }
    public static Optional<ItemStack> fromShard(ItemStack shard) {
        return AttunementShardItem.signature(shard).map(signature -> bound(signature.key()));
    }
    @Override public boolean canFitInsideContainerItems() { return false; }
    @Override public void onDestroyed(ItemEntity entity) { /* Destroying an access point never spills shared contents. */ }
    @Override public void appendHoverText(ItemStack bag, TooltipContext context, List<Component> text, TooltipFlag flag) {
        key(bag).ifPresent(value -> text.add(AttunementMark.fromKey(value).component()));
        super.appendHoverText(bag, context, text, flag);
    }
    private static boolean realSlot(Player player, Slot slot) {
        return !player.isSpectator() && player.containerMenu.slots.contains(slot) && slot.isActive() && !slot.isFake() && player.containerMenu.stillValid(player);
    }
    private static void sound(Player player, boolean insert) {
        player.playSound(insert ? SoundEvents.BUNDLE_INSERT : SoundEvents.BUNDLE_REMOVE_ONE, .8f, .8f + player.getRandom().nextFloat() * .4f);
    }
    @Override public boolean overrideOtherStackedOnMe(ItemStack bag, ItemStack cursor, Slot slot, ClickAction action, Player player, SlotAccess cursorAccess) {
        if (action != ClickAction.SECONDARY || key(bag).isEmpty()) return false;
        if (player.level().isClientSide()) return true; // Never predict ownership-changing transfers from a preview.
        if (!(player instanceof ServerPlayer serverPlayer) || !realSlot(player, slot) || slot.getItem() != bag
                || !slot.allowModification(player) || player.containerMenu.getCarried() != cursor || cursorAccess.get() != cursor) return true;
        var storage = CraneBagStorage.get(serverPlayer.getServer()); String key = key(bag).orElseThrow();
        boolean inserting = !cursor.isEmpty();
        boolean changed = storage.edit(key, player.registryAccess(), before -> {
            var mutable = new BundleContents.Mutable(before);
            if (cursor.isEmpty()) {
                var removed = mutable.removeOne();
                if (removed == null || !cursorAccess.set(removed)) return before;
                return mutable.toImmutable();
            }
            if (!CraneBagStorage.accepts(cursor)) return before;
            var copy = cursor.copy(); int inserted = mutable.tryInsert(copy);
            if (inserted == 0 || !CraneBagStorage.valid(mutable.toImmutable(), player.registryAccess())) return before;
            cursor.shrink(inserted); return mutable.toImmutable();
        });
        if (changed) { slot.setChanged(); sound(player, inserting); }
        CraneBagAccess.refresh(serverPlayer, bag);
        return true;
    }
    @Override public boolean overrideStackedOnOther(ItemStack bag, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || key(bag).isEmpty()) return false;
        if (player.level().isClientSide()) return true;
        if (!(player instanceof ServerPlayer serverPlayer) || !realSlot(player, slot) || player.containerMenu.getCarried() != bag) return true;
        var storage = CraneBagStorage.get(serverPlayer.getServer()); String key = key(bag).orElseThrow();
        boolean inserting = slot.hasItem();
        boolean changed = storage.edit(key, player.registryAccess(), before -> {
            var mutable = new BundleContents.Mutable(before);
            if (!slot.hasItem()) {
                var removed = mutable.removeOne();
                if (removed == null) return before;
                int original = removed.getCount();
                var remainder = slot.safeInsert(removed);
                if (remainder.getCount() == original) return before;
                mutable.tryInsert(remainder); return mutable.toImmutable();
            }
            if (!slot.allowModification(player) || !CraneBagStorage.accepts(slot.getItem())) return before;
            var preview = slot.getItem().copy(); int inserted = mutable.tryInsert(preview);
            if (inserted == 0 || !CraneBagStorage.valid(mutable.toImmutable(), player.registryAccess())) return before;
            var taken = slot.safeTake(inserted, inserted, player);
            if (taken.isEmpty()) return before;
            // Rebuild with exactly what the slot actually released (result slots may reject partial takes).
            var actual = new BundleContents.Mutable(before); int accepted = actual.tryInsert(taken);
            if (!taken.isEmpty()) throw new IllegalStateException("Slot changed during Crane Bag insertion");
            return accepted == 0 ? before : actual.toImmutable();
        });
        if (changed) sound(player, inserting);
        CraneBagAccess.refresh(serverPlayer, bag);
        return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var bag = player.getItemInHand(hand); var key = key(bag);
        if (key.isEmpty()) return InteractionResultHolder.fail(bag);
        if (level.isClientSide()) return InteractionResultHolder.success(bag);
        if (!(player instanceof ServerPlayer serverPlayer) || player.isSpectator()) return InteractionResultHolder.fail(bag);
        boolean changed = CraneBagStorage.get(serverPlayer.getServer()).edit(key.get(), player.registryAccess(), before -> {
            var remaining = new ArrayList<ItemStack>();
            for (var item : before.itemsCopy()) {
                var drop = new ItemEntity(level, player.getX(), player.getEyeY() - .3, player.getZ(), item.copy());
                drop.setPickUpDelay(40); drop.setThrower(player);
                drop.setDeltaMovement(player.getLookAngle().scale(.3).add(0, .1, 0));
                if (!level.addFreshEntity(drop)) remaining.add(item);
            }
            return new BundleContents(remaining);
        });
        CraneBagAccess.refresh(serverPlayer, bag);
        if (!changed) return InteractionResultHolder.fail(bag);
        player.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, .8f, .8f + player.getRandom().nextFloat() * .4f);
        player.awardStat(Stats.ITEM_USED.get(this)); return InteractionResultHolder.success(bag);
    }
}
