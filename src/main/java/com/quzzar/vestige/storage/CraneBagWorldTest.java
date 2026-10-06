package com.quzzar.vestige.storage;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.gametest.SurvivalTestPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
import java.util.function.Consumer;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraneBagWorldTest {
    private static String key() { return UUID.randomUUID().toString().replace("-", "").repeat(2); }
    private static ItemStack equip(ServerPlayer player, String key) {
        player.containerMenu.setCarried(ItemStack.EMPTY); var bag = CraneBagItem.bound(key);
        player.getInventory().setItem(0, bag); CraneBagAccess.refresh(player); return bag;
    }
    private static void click(ServerPlayer player, ItemStack cursor) {
        player.containerMenu.setCarried(cursor); player.containerMenu.clicked(36, 1, ClickType.PICKUP, player);
    }
    private static int count(CraneBagStorage storage, String key, Item item) {
        return storage.contents(key).itemCopyStream().filter(stack -> stack.is(item)).mapToInt(ItemStack::getCount).sum();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_corruption")
    public static void unreadableSavedPoolsCannotBeReplacedByEmptyStorage(GameTestHelper h) throws java.io.IOException {
        var missingPools = new CompoundTag(); missingPools.putInt("version", 1);
        boolean missingRejected = false;
        try { CraneBagStorage.load(missingPools, h.getLevel().registryAccess()); } catch (IllegalStateException expected) { missingRejected = true; }
        h.assertTrue(missingRejected, "A missing pool list was treated as empty storage");
        var wrongPools = new CompoundTag(); wrongPools.putInt("version", 1); var wrongList = new ListTag(); wrongList.add(StringTag.valueOf("not a pool")); wrongPools.put("pools", wrongList);
        boolean wrongRejected = false;
        try { CraneBagStorage.load(wrongPools, h.getLevel().registryAccess()); } catch (IllegalStateException expected) { wrongRejected = true; }
        h.assertTrue(wrongRejected, "A malformed pool list was silently discarded");
        var directory = java.nio.file.Files.createTempDirectory(h.getLevel().getServer()
                .getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT), "crane-bag-load-test-");
        var file = directory.resolve("vestige_crane_bags.dat");
        try {
            var invalid = new CompoundTag(); var data = new CompoundTag(); data.putInt("version", 99); invalid.put("data", data);
            invalid.putInt("DataVersion", net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            NbtIo.writeCompressed(invalid, file); byte[] before = java.nio.file.Files.readAllBytes(file);
            var storage = new net.minecraft.world.level.storage.DimensionDataStorage(directory.toFile(),
                    h.getLevel().getServer().getFixerUpper(), h.getLevel().registryAccess());
            boolean rejected = false;
            try { CraneBagStorage.open(storage, file); } catch (IllegalStateException expected) { rejected = true; }
            storage.save();
            h.assertTrue(rejected && Arrays.equals(before, java.nio.file.Files.readAllBytes(file)), "Unreadable storage was replaced/discarded");
        } finally { java.nio.file.Files.deleteIfExists(file); java.nio.file.Files.deleteIfExists(directory); }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_sharing")
    public static void twoPlayersCannotBothWithdrawTheLastDiamondAndDifferentFullKeysStaySeparate(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var first = SurvivalTestPlayer.create(h); var second = SurvivalTestPlayer.create(h)) {
            equip(first, key); equip(second, key); click(first, new ItemStack(Items.DIAMOND));
            h.assertTrue(first.containerMenu.getCarried().isEmpty(), "Insertion did not consume the real cursor item");
            click(first, ItemStack.EMPTY); click(second, ItemStack.EMPTY);
            h.assertTrue(first.containerMenu.getCarried().is(Items.DIAMOND) && second.containerMenu.getCarried().isEmpty()
                    && count(storage, key, Items.DIAMOND) == 0, "Two peers duplicated the last item");
            click(first, new ItemStack(Items.EMERALD, 12)); equip(second, key()); click(second, ItemStack.EMPTY);
            h.assertTrue(second.containerMenu.getCarried().isEmpty() && count(storage, key, Items.EMERALD) == 12, "Different signatures shared storage");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_capacity")
    public static void partialInsertionAndMixedStackSizesUseVanillaBundleWeight(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var player = SurvivalTestPlayer.create(h)) {
            equip(player, key); click(player, new ItemStack(Items.ENDER_PEARL, 8)); click(player, new ItemStack(Items.COBBLESTONE, 40));
            h.assertTrue(player.containerMenu.getCarried().getCount() == 8 && count(storage, key, Items.COBBLESTONE) == 32, "Partial weighted capacity was wrong");
            click(player, new ItemStack(Items.DIAMOND)); h.assertTrue(player.containerMenu.getCarried().getCount() == 1, "Full bag consumed an item");
            click(player, ItemStack.EMPTY); h.assertTrue(player.containerMenu.getCarried().getCount() == 32, "Most recent stack did not extract");
            click(player, ItemStack.EMPTY); h.assertTrue(player.containerMenu.getCarried().getCount() == 8, "Pearl stack did not extract");
            click(player, new ItemStack(Items.IRON_SWORD)); click(player, new ItemStack(Items.DIRT));
            h.assertTrue(player.containerMenu.getCarried().is(Items.DIRT) && storage.contents(key).size() == 1, "Unstackable did not occupy the whole bag");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_slot")
    public static void carriedBagRespectsSlotLimitsPermissionsAndFailedCursorWrites(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var player = SurvivalTestPlayer.create(h)) {
            var bag = equip(player, key); click(player, new ItemStack(Items.DIAMOND, 20));
            var slot = player.containerMenu.getSlot(36);
            player.containerMenu.setCarried(ItemStack.EMPTY);
            ScrollItems.CRANE_BAG.get().overrideOtherStackedOnMe(bag, ItemStack.EMPTY, slot, ClickAction.SECONDARY, player, SlotAccess.NULL);
            h.assertTrue(count(storage, key, Items.DIAMOND) == 20, "Rejected cursor write removed shared items");
            player.getInventory().setItem(0, ItemStack.EMPTY); player.containerMenu.setCarried(bag);
            player.containerMenu.slots.set(36, new Slot(player.getInventory(), 0, 0, 0) {
                @Override public int getMaxStackSize() { return 3; }
                @Override public int getMaxStackSize(ItemStack item) { return 3; }
            });
            player.containerMenu.clicked(36, 1, ClickType.PICKUP, player);
            h.assertTrue(player.getInventory().getItem(0).getCount() == 3 && count(storage, key, Items.DIAMOND) == 17, "Carried bag ignored target slot capacity or lost its remainder");
            player.containerMenu.clicked(36, 1, ClickType.PICKUP, player);
            h.assertTrue(player.getInventory().getItem(0).isEmpty() && count(storage, key, Items.DIAMOND) == 20, "Carried bag did not insert from slot");
            var denied = new Slot(player.getInventory(), 0, 0, 0) {
                @Override public boolean mayPlace(ItemStack item) { return false; }
                @Override public boolean mayPickup(net.minecraft.world.entity.player.Player p) { return false; }
            };
            player.containerMenu.slots.set(36, denied);
            player.containerMenu.clicked(36, 1, ClickType.PICKUP, player);
            h.assertTrue(player.getInventory().getItem(0).isEmpty() && count(storage, key, Items.DIAMOND) == 20, "Read-only slot received shared contents");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_packets")
    public static void forgedClientContentsRepeatedPacketsAndChangedBagBindingsCannotCreateItems(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var player = SurvivalTestPlayer.create(h)) {
            equip(player, key); click(player, new ItemStack(Items.DIAMOND)); player.containerMenu.setCarried(ItemStack.EMPTY);
            var fake = CraneBagItem.bound(key()); fake.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(new ItemStack(Items.NETHERITE_INGOT, 64))));
            var changes = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<ItemStack>(); changes.put(36, fake);
            var packet = new net.minecraft.network.protocol.game.ServerboundContainerClickPacket(player.containerMenu.containerId,
                    player.containerMenu.getStateId(), 36, 1, ClickType.PICKUP, new ItemStack(Items.NETHERITE_INGOT, 64), changes);
            player.connection.handleContainerClick(packet);
            h.assertTrue(player.containerMenu.getCarried().is(Items.DIAMOND) && CraneBagItem.key(player.getInventory().getItem(0)).orElseThrow().equals(key), "Client installed forged authoritative items/key");
            player.connection.handleContainerClick(packet);
            int total = count(storage, key, Items.DIAMOND) + (player.containerMenu.getCarried().is(Items.DIAMOND) ? player.containerMenu.getCarried().getCount() : 0);
            h.assertTrue(total == 1 && count(storage, key, Items.NETHERITE_INGOT) == 0, "Repeated packet created items");
            equip(player, key()); player.connection.handleContainerClick(packet);
            h.assertTrue(player.containerMenu.getCarried().isEmpty() && count(storage, key, Items.DIAMOND) == 1, "Old packet accessed a removed binding");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_components")
    public static void exactComponentsStayDistinctAndPreviewCopiesNeverBecomeWithdrawableItems(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var player = SurvivalTestPlayer.create(h)) {
            var bag = equip(player, key); var named = new ItemStack(Items.DIAMOND, 3); named.set(DataComponents.CUSTOM_NAME, Component.literal("Kept name"));
            click(player, named); click(player, new ItemStack(Items.DIAMOND, 2));
            h.assertTrue(storage.contents(key).size() == 2, "Different components merged");
            click(player, ItemStack.EMPTY); h.assertTrue(player.containerMenu.getCarried().getCount() == 2, "Ordinary stack changed");
            click(player, ItemStack.EMPTY); h.assertTrue(player.containerMenu.getCarried().getHoverName().getString().equals("Kept name"), "Stored components were lost");
            bag.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(new ItemStack(Items.NETHERITE_INGOT, 64))));
            click(player, ItemStack.EMPTY);
            h.assertTrue(player.containerMenu.getCarried().isEmpty() && storage.contents(key).isEmpty()
                    && bag.get(DataComponents.BUNDLE_CONTENTS).isEmpty(), "A forged/stale preview supplied real contents or remained visible");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_nesting")
    public static void nestedBagsAndOversizedComponentPayloadsAreRejectedWithoutConsumption(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var player = SurvivalTestPlayer.create(h)) {
            equip(player, key); click(player, CraneBagItem.bound(key()));
            h.assertTrue(CraneBagItem.key(player.containerMenu.getCarried()).isPresent() && storage.contents(key).isEmpty(), "Shared bag nested");
            click(player, new ItemStack(Items.BUNDLE)); h.assertTrue(player.containerMenu.getCarried().is(Items.BUNDLE), "Ordinary bundle nested");
            var huge = new ItemStack(Items.PAPER); CustomData.update(DataComponents.CUSTOM_DATA, huge, tag -> tag.putString("large", "x".repeat(40_000)));
            click(player, huge); h.assertTrue(player.containerMenu.getCarried() == huge && huge.getCount() == 1 && storage.contents(key).isEmpty(), "Oversized preview consumed an item");
            h.assertTrue(!ScrollItems.CRANE_BAG.get().canFitInsideContainerItems(), "Vanilla bundles can hold a shared bag");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_persistence")
    public static void contentsPersistIndependentlyOfCopiesAndSurviveDestroyedAccessPoints(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var player = SurvivalTestPlayer.create(h)) {
            var bag = equip(player, key); click(player, new ItemStack(Items.EMERALD, 17));
            var saved = storage.save(new CompoundTag(), h.getLevel().registryAccess());
            var restored = CraneBagStorage.load(saved, h.getLevel().registryAccess());
            h.assertTrue(count(restored, key, Items.EMERALD) == 17 && !restored.session.equals(storage.session), "Save/load lost contents or reused preview epoch");
            var isolatedCopy = restored.contents(key); isolatedCopy.getItemUnsafe(0).setCount(64);
            h.assertTrue(count(restored, key, Items.EMERALD) == 17, "Read-only preview mutated authority");
            var entity = new ItemEntity(h.getLevel(), 0, 0, 0, bag.copy()); bag.getItem().onDestroyed(entity);
            player.getInventory().setItem(0, ItemStack.EMPTY);
            h.assertTrue(count(storage, key, Items.EMERALD) == 17, "Destroying the last bag destroyed contents");
            equip(player, key); click(player, ItemStack.EMPTY);
            h.assertTrue(player.containerMenu.getCarried().getCount() == 17 && storage.contents(key).isEmpty(), "Replacement bag duplicated/lost retained contents");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_sync")
    public static void peerPreviewsRefreshAcrossDimensionsAndAfterAnOfflineBagReturns(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var first = SurvivalTestPlayer.create(h); var second = SurvivalTestPlayer.create(h)) {
            var firstBag = equip(first, key); var secondBag = equip(second, key);
            var nether = h.getLevel().getServer().getLevel(Level.NETHER); h.assertTrue(nether != null, "Missing Nether");
            second.teleportTo(nether, 0, 100, 0, 0, 0);
            click(first, new ItemStack(Items.REDSTONE, 10)); CraneBagAccess.refresh(second);
            h.assertTrue(secondBag.get(DataComponents.BUNDLE_CONTENTS).equals(firstBag.get(DataComponents.BUNDLE_CONTENTS)), "Cross-dimension peer preview remained stale");
            var offline = secondBag.copy(); equip(second, key()); click(first, ItemStack.EMPTY);
            second.getInventory().setItem(0, offline); CraneBagAccess.refresh(second);
            h.assertTrue(offline.get(DataComponents.BUNDLE_CONTENTS).isEmpty() && storage.contents(key).isEmpty(), "Stored bag resurrected cached items");
            click(second, ItemStack.EMPTY); h.assertTrue(second.containerMenu.getCarried().isEmpty(), "Stale cache yielded an item");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_drop")
    public static void rejectedDropsAndReentrantWithdrawalsPreserveExactlyOneAuthoritativeCopy(GameTestHelper h) {
        String key = key(); var storage = CraneBagStorage.get(h.getLevel().getServer());
        try (var first = SurvivalTestPlayer.create(h); var second = SurvivalTestPlayer.create(h)) {
            var bag = equip(first, key); equip(second, key); click(first, new ItemStack(Items.DIAMOND, 7));
            Consumer<EntityJoinLevelEvent> cancel = event -> {
                if (event.getEntity() instanceof ItemEntity item && item.getItem().is(Items.DIAMOND)) {
                    click(second, ItemStack.EMPTY); event.setCanceled(true);
                }
            };
            NeoForge.EVENT_BUS.addListener(cancel);
            try { bag.getItem().use(first.level(), first, InteractionHand.MAIN_HAND); }
            finally { NeoForge.EVENT_BUS.unregister(cancel); }
            h.assertTrue(count(storage, key, Items.DIAMOND) == 7 && second.containerMenu.getCarried().isEmpty(), "Rejected/reentrant drop duplicated or lost items");
            bag.getItem().use(first.level(), first, InteractionHand.MAIN_HAND);
            h.assertTrue(storage.contents(key).isEmpty(), "Successful drop retained transferable contents");
            int dropped = h.getLevel().getEntitiesOfClass(ItemEntity.class, first.getBoundingBox().inflate(3)).stream()
                    .filter(entity -> entity.getItem().is(Items.DIAMOND)).mapToInt(entity -> entity.getItem().getCount()).sum();
            h.assertTrue(dropped == 7, "World drop count changed");
            click(second, ItemStack.EMPTY); h.assertTrue(second.containerMenu.getCarried().isEmpty(), "Peer withdrew a second copy after drop");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "crane_bag_binding")
    public static void copiedShardKeyUsesTheSameRuneTooltipAndInvalidBindingsCannotAccessPools(GameTestHelper h) {
        var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        var ingredients = AttunementShardItem.ingredients(); var nodes = new ArrayList<RitualInputs.Node>();
        for (int i = 0; i < 8; i++) nodes.add(new RitualInputs.Node(i, geometry.offset(i), i < ingredients.size()
                ? new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))) : ItemStack.EMPTY, ItemStack.EMPTY));
        var shard = AttunementShardItem.create(new RitualInputs(geometry, nodes));
        var bag = CraneBagItem.fromShard(shard).orElseThrow(); String key = AttunementShardItem.signature(shard).orElseThrow().key();
        h.assertTrue(CraneBagItem.key(bag).orElseThrow().equals(key), "Device rehashed the source shard");
        for (var flag : List.of(TooltipFlag.NORMAL, TooltipFlag.ADVANCED)) {
            var lines = bag.getTooltipLines(Item.TooltipContext.of(h.getLevel()), h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL), flag);
            h.assertTrue(lines.get(0).getString().equals(bag.getHoverName().getString()) && lines.get(1).equals(AttunementMark.fromKey(key).component()), "Rune tooltip changed or moved");
        }
        try (var player = SurvivalTestPlayer.create(h)) {
            // This valid-shard fixture has a reproducible key, so reset its pool when rerunning the same test world.
            CraneBagStorage.get(player.getServer()).edit(key, player.registryAccess(), before -> BundleContents.EMPTY);
            equip(player, key); click(player, new ItemStack(Items.DIAMOND));
            var invalid = new ItemStack(ScrollItems.CRANE_BAG.get()); player.getInventory().setItem(0, invalid); click(player, ItemStack.EMPTY);
            h.assertTrue(count(CraneBagStorage.get(player.getServer()), key, Items.DIAMOND) == 1, "Unbound bag accessed a pool");
        }
        h.succeed();
    }
}
