package com.quzzar.vestige.travel;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StandingStoneTest {
    private StandingStoneTest() { }
    private static String key() { return UUID.randomUUID().toString().replace("-", "").repeat(2); }
    private static StandingStoneEntity stone(ServerLevel level, BlockPos pos, String key, String name) {
        level.setBlock(pos, StandingStones.STONE.get().defaultBlockState(), 3);
        level.setBlock(pos.above(), StandingStones.STONE.get().defaultBlockState().setValue(StandingStoneBlock.HALF,
                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
        var stone = (StandingStoneEntity) level.getBlockEntity(pos); stone.configure(key, name); return stone;
    }
    private static void floor(ServerLevel level, BlockPos center, Block block) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            level.setBlock(center.offset(x, -1, z), block.defaultBlockState(), 3);
            for (int y = 0; y <= 3; y++) level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
    }
    private static ItemStack shard(int distance) {
        var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, distance, 0, LeylineShaping.Shape.DIAGONAL, 5, 0);
        var offerings = List.of(Items.AMETHYST_SHARD, Items.AMETHYST_SHARD, Items.ECHO_SHARD, Items.IRON_INGOT, Items.DIAMOND, Items.LAPIS_LAZULI, Items.AIR, Items.AIR);
        List<RitualInputs.Node> nodes = new ArrayList<>();
        for (int i = 0; i < 8; i++) nodes.add(new RitualInputs.Node(i, geometry.offset(i), new ItemStack(offerings.get(i)), ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(geometry, nodes));
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void occupiedUpperSpaceRejectsPlacementWithoutConsumingTheBoundItem(GameTestHelper h) {
        var pos = new BlockPos(4, 1, 4); h.setBlock(pos, Blocks.AIR); h.setBlock(pos.below(), Blocks.STONE); h.setBlock(pos.above(), Blocks.DIAMOND_BLOCK);
        var player = h.makeMockPlayer(GameType.SURVIVAL); var item = StandingStones.bound(key());
        player.setItemInHand(InteractionHand.MAIN_HAND, item); var ground = h.absolutePos(pos.below());
        item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false)));
        h.assertTrue(h.getBlockState(pos).isAir() && h.getBlockState(pos.above()).is(Blocks.DIAMOND_BLOCK)
                && player.getMainHandItem().getCount() == 1, "Placement changed the blocked fixture: lower=" + h.getBlockState(pos)
                        + ", upper=" + h.getBlockState(pos.above()) + ", count=" + player.getMainHandItem().getCount());
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void miningEitherHalfDropsExactlyOneBoundStoneAndRemovesItsEndpoint(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(4, 1, 4)); floor(level, pos, Blocks.STONE);
        for (int half = 0; half < 2; half++) {
            String key = key(); var stone = stone(level, pos, key, "Old Henge"); var id = stone.id();
            level.destroyBlock(pos.above(half), true);
            h.assertTrue(level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir(), "Orphaned monolith half");
            var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(pos).inflate(2));
            h.assertTrue(drops.size() == 1 && drops.getFirst().getItem().getCount() == 1
                    && StandingStones.key(drops.getFirst().getItem()).orElseThrow().equals(key)
                    && drops.getFirst().getItem().getHoverName().getString().equals("Old Henge"), "Mining duplicated or lost the bound drop");
            drops.forEach(net.minecraft.world.entity.Entity::discard);
            h.assertTrue(StoneDirectory.get(level.getServer()).network().get(id).isEmpty(), "Broken stone retained its endpoint");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void creativeUpperBreakLeavesNoLowerHalfOrBoundDrop(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(4, 1, 4)); floor(level, pos, Blocks.STONE);
        var stone = stone(level, pos, key(), "Creative prototype"); var player = h.makeMockPlayer(GameType.CREATIVE);
        var upper = level.getBlockState(pos.above()); upper.getBlock().playerWillDestroy(level, pos.above(), upper, player);
        level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
        h.assertTrue(level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(pos).inflate(2)).isEmpty(),
                "Creative upper break left a half or dropped a bound item");
        h.assertTrue(StoneDirectory.get(level.getServer()).network().get(stone.id()).isEmpty(), "Creative break retained endpoint");
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones", timeoutTicks = 300)
    public static void profileFacingCollisionAndUpperInteractionBelongToOneEndpoint(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(4, 1, 4)); floor(level, pos, Blocks.STONE);
        // Keep the destination's entire random arrival area away from the source fixture.
        var targetPos = pos.east(6); floor(level, targetPos, Blocks.STONE);
        var target = stone(level, targetPos, "0".repeat(64), "Destination");
        var player = h.makeMockServerPlayerInLevel();
        try {
            for (int i = 0; i < 16; i++) for (var facing : Direction.Plane.HORIZONTAL) {
                var state = StandingStones.STONE.get().defaultBlockState().setValue(StandingStoneBlock.FACING, facing);
                level.setBlock(pos, state, 3); level.setBlock(pos.above(), state.setValue(StandingStoneBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
                String key = "0".repeat(62) + String.format(java.util.Locale.ROOT, "%02x", i);
                var stone = (StandingStoneEntity) level.getBlockEntity(pos); stone.configure(key, "Henge");
                var lower = level.getBlockState(pos); var upper = level.getBlockState(pos.above());
                var expectedProfile = StandingStoneShape.fromKey(key);
                h.assertTrue(lower.getValue(StandingStoneBlock.PROFILE) == expectedProfile
                        && upper.getValue(StandingStoneBlock.PROFILE) == expectedProfile
                        && upper.getValue(StandingStoneBlock.FACING) == facing && level.getBlockEntity(pos.above()) == null, "Profile, facing or endpoint ownership diverged");
                var shape = lower.getCollisionShape(level, pos); var box = shape.bounds();
                h.assertTrue(!shape.isEmpty() && box.minX >= 0 && box.maxX <= 1 && box.minY >= 0 && box.maxY <= 1
                        && box.minZ >= 0 && box.maxZ <= 1 && shape.toAabbs().size() > 1, "Monolith collision became a full cube or escaped its occupied block");
                var upperBox = upper.getCollisionShape(level, pos.above()).bounds();
                h.assertTrue(upperBox.minX >= 0 && upperBox.maxX <= 1 && upperBox.minY >= 0 && upperBox.maxY <= 1
                        && upperBox.minZ >= 0 && upperBox.maxZ <= 1, "Upper collision escaped its reserved block");
                target.configure(key, "Destination"); player.setPos(Vec3.atBottomCenterOf(pos.north())); var before = player.position();
                ((StandingStoneBlock) upper.getBlock()).useWithoutItem(upper, level, pos.above(), player,
                        new BlockHitResult(Vec3.atCenterOf(pos.above()), facing, pos.above(), false));
                h.assertTrue(StoneDirectory.get(level.getServer()).network().peers(key).stream().filter(n -> n.id().equals(stone.id())).count() == 1,
                        "Upper half registered a second destination");
                StoneTravel.travel(player, stone.id(), target.id());
                h.assertTrue(player.position().distanceToSqr(before) > 1, "Upper-half interaction did not open the source session");
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        } finally { level.getServer().getPlayerList().remove(player); player.discard(); }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void matchingMasonrySelectsEveryFinishWithoutChangingTheCopiedKey(GameTestHelper h) {
        var shard = shard(2); String key = AttunementShardItem.signature(shard).orElseThrow().key();
        for (var material : ApparatusMaterials.values()) {
            Item body = BuiltInRegistries.ITEM.get(material.body());
            var offerings = List.of(new ItemStack(body, 3), ItemStack.EMPTY, shard.copy(), new ItemStack(Items.ENDER_PEARL), new ItemStack(body));
            var output = StandingStoneRecipe.result(offerings).orElseThrow();
            h.assertTrue(StandingStones.material(output).orElseThrow() == material && StandingStones.key(output).orElseThrow().equals(key),
                    "Wrong finish or changed attunement for " + material.id());
            h.assertTrue(StandingStoneShape.fromKey(StandingStones.key(output).orElseThrow()) == StandingStoneShape.fromKey(key)
                    && AttunementMark.fromKey(StandingStones.key(output).orElseThrow()).equals(AttunementMark.fromKey(key)),
                    "Finish changed the signature-selected model or runes");
            var reversed = new ArrayList<>(offerings); Collections.reverse(reversed);
            h.assertTrue(ItemStack.isSameItemSameComponents(output, StandingStoneRecipe.result(reversed).orElseThrow()), "Recipe order changed finish");
            h.assertTrue(StandingStoneRecipe.result(List.of(shard, new ItemStack(Items.ENDER_PEARL), new ItemStack(body),
                    new ItemStack(BuiltInRegistries.ITEM.get(material.slab())))).isEmpty(), "A slab replaced a full masonry offering");
        }
        h.assertTrue(StandingStoneRecipe.result(List.of(shard, new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.TUFF), new ItemStack(Items.QUARTZ_BLOCK))).isEmpty(),
                "Mixed masonry finishes accepted");
        h.assertTrue(StandingStoneRecipe.result(List.of(shard, new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.STONE_BRICKS, 2))).isEmpty(),
                "One stack replaced two offering surfaces");
        h.assertTrue(StandingStoneRecipe.result(List.of(shard, new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.TUFF), new ItemStack(Items.TUFF), new ItemStack(Items.DIRT))).isEmpty(),
                "Extra offering accepted");
        var original = StandingStoneRecipe.result(List.of(shard, new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.CHISELED_STONE_BRICKS), new ItemStack(Items.CHISELED_STONE_BRICKS))).orElseThrow();
        h.assertTrue(StandingStones.material(original).orElseThrow() == ApparatusMaterials.STONE_BRICKS, "Original chiseled-brick alternative lost");
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void everyFinishPlacesAndDropsItsOwnMaterialAndExactBinding(GameTestHelper h) {
        String key = key(); var pos = new BlockPos(4, 1, 4); h.setBlock(pos.below(), Blocks.STONE); h.setBlock(pos, Blocks.AIR);
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var ground = h.absolutePos(pos.below());
        for (var material : ApparatusMaterials.values()) {
            var item = StandingStones.bound(key, material);
            item.set(DataComponents.CUSTOM_NAME, Component.literal("Ancient Henge"));
            player.setItemInHand(InteractionHand.MAIN_HAND, item);
            item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false)));
            h.assertTrue(h.getBlockState(pos).is(StandingStones.STONES.get(material).get()) && player.getMainHandItem().isEmpty(), "Variant placement failed: " + material.id());
            var placed = (StandingStoneEntity) h.getBlockEntity(pos);
            var drops = Block.getDrops(placed.getBlockState(), h.getLevel(), placed.getBlockPos(), placed, player, new ItemStack(Items.DIAMOND_PICKAXE));
            h.assertTrue(drops.size() == 1 && StandingStones.material(drops.getFirst()).orElseThrow() == material
                    && StandingStones.key(drops.getFirst()).orElseThrow().equals(key) && drops.getFirst().getHoverName().getString().equals("Ancient Henge"),
                    "Drop lost finish, key or name: " + material.id());
            h.assertTrue(StoneDirectory.get(h.getLevel().getServer()).network().get(placed.id()).orElseThrow().key().equals(key), "Finish changed network membership");
            h.setBlock(pos, Blocks.AIR);
            h.assertTrue(StoneDirectory.get(h.getLevel().getServer()).network().get(placed.id()).isEmpty(), "Variant removal retained endpoint");
        }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void placedNamedStoneDropsItsAttunementAndDirectorySurvivesSerialization(GameTestHelper h) {
        String key = key(); var pos = new BlockPos(4, 1, 4); h.setBlock(pos.below(), Blocks.STONE); h.setBlock(pos, Blocks.AIR);
        ItemStack item = StandingStones.bound(key); item.set(DataComponents.CUSTOM_NAME, Component.literal("Ancient Henge"));
        var player = h.makeMockPlayer(GameType.SURVIVAL); player.setItemInHand(InteractionHand.MAIN_HAND, item);
        BlockPos ground = h.absolutePos(pos.below());
        var interaction = item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(ground), Direction.UP, ground, false)));
        h.assertTrue(h.getBlockState(pos).is(StandingStones.STONE.get()) && player.getMainHandItem().isEmpty(),
                "Stone did not place/consume: " + interaction + ", state=" + h.getBlockState(pos));
        var placed = (StandingStoneEntity) h.getBlockEntity(pos);
        h.assertTrue(placed.key().equals(key) && placed.name().equals("Ancient Henge"), "Placement lost key/name");
        var saved = placed.saveWithFullMetadata(h.getLevel().registryAccess());
        var restored = new StandingStoneEntity(placed.getBlockPos(), placed.getBlockState()); restored.loadWithComponents(saved, h.getLevel().registryAccess());
        h.assertTrue(restored.id().equals(placed.id()) && restored.key().equals(key), "Endpoint changed on reload");
        var directory = StoneDirectory.get(h.getLevel().getServer());
        var copy = StoneDirectory.load(directory.save(new CompoundTag(), h.getLevel().registryAccess()), h.getLevel().registryAccess());
        h.assertTrue(copy.network().get(placed.id()).orElseThrow().key().equals(key), "Unloaded endpoint lost persistent membership");
        var drops = Block.getDrops(placed.getBlockState(), h.getLevel(), placed.getBlockPos(), placed, player, new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(drops.size() == 1 && StandingStones.key(drops.getFirst()).orElseThrow().equals(key)
                && drops.getFirst().getHoverName().getString().equals("Ancient Henge"), "Mining lost binding/name");
        h.setBlock(pos, Blocks.AIR);
        h.assertTrue(directory.network().get(placed.id()).isEmpty(), "Broken stone remained a destination"); h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stone_ritual", timeoutTicks = 100)
    public static void ritualConsumesOneBoundShardCopiesKeyAndIgnoresOuterLayer(GameTestHelper h) {
        var centerPos = new BlockPos(4, 1, 4); h.setBlock(centerPos, ApparatusBlocks.SPELLSTONE.get());
        var geometry = new LeylineShaping.Geometry(4, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        for (int i : new int[]{0, 2, 4, 6}) h.setBlock(centerPos.offset(geometry.offset(i)), ApparatusBlocks.PLINTH.get());
        var center = (OfferingBlockEntity) h.getBlockEntity(centerPos); var layout = RitualCrafting.layout(center);
        ItemStack shard = shard(2); String expected = AttunementShardItem.signature(shard).orElseThrow().key();
        var inputs = List.of(shard.copyWithCount(3), new ItemStack(Items.ENDER_PEARL, 4), new ItemStack(Items.CHISELED_STONE_BRICKS, 2), new ItemStack(Items.CHISELED_STONE_BRICKS, 2));
        for (int i = 0; i < 4; i++) layout.stands().get(i * 2).insert(inputs.get(i));
        layout.stands().get(0).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        var outerPos = new BlockPos(7, 1, 7); h.setBlock(outerPos, ApparatusBlocks.PLINTH.get());
        var outer = (OfferingBlockEntity) h.getBlockEntity(outerPos); outer.insert(new ItemStack(Items.DIRT));
        h.assertTrue(StandingStoneRecipe.result(List.of(new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()), inputs.get(1), inputs.get(2), inputs.get(3))).isEmpty(), "Unattuned shard accepted");
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL), center) == RitualCrafting.Outcome.CRAFTING, "Stone ritual rejected");
        h.assertTrue(!outer.busy(), "Inactive outer offering was reserved"); h.setBlock(outerPos, Blocks.AIR);
        h.runAfterDelay(65, () -> {
            h.assertTrue(StandingStones.key(com.quzzar.vestige.apparatus.RitualTestOutput.stack(center)).orElseThrow().equals(expected), "Device ingredients or socket rehashed key");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && layout.stands().get(0).materialItem().is(Items.GOLD_BLOCK), "Offerings or sockets consumed incorrectly");
            h.succeed();
        });
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stone_cancel", timeoutTicks = 100)
    public static void changingOnlyTheShardKeyCancelsWithoutPartialConsumption(GameTestHelper h) {
        var centerPos = new BlockPos(4, 1, 4); h.setBlock(centerPos, ApparatusBlocks.SPELLSTONE.get());
        var geometry = new LeylineShaping.Geometry(4, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        for (int i : new int[]{0, 2, 4, 6}) h.setBlock(centerPos.offset(geometry.offset(i)), ApparatusBlocks.PLINTH.get());
        var center = (OfferingBlockEntity) h.getBlockEntity(centerPos); var layout = RitualCrafting.layout(center);
        var inputs = List.of(shard(2), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.CHISELED_STONE_BRICKS), new ItemStack(Items.CHISELED_STONE_BRICKS));
        for (int i = 0; i < 4; i++) layout.stands().get(i * 2).insert(inputs.get(i));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL), center) == RitualCrafting.Outcome.CRAFTING, "Stone ritual rejected");
        h.runAfterDelay(10, () -> { layout.stands().get(0).remove(); layout.stands().get(0).insert(shard(3)); });
        h.runAfterDelay(65, () -> {
            h.assertTrue(com.quzzar.vestige.apparatus.RitualTestOutput.stack(center).isEmpty() && !center.busy() && layout.items().stream().filter(item -> !item.isEmpty()).count() == 4,
                    "A changed attunement consumed offerings, produced a stone or retained locks"); h.succeed();
        });
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void travelRequiresOpenNearbySourceAndExactNetworkMembership(GameTestHelper h) {
        var level = h.getLevel(); String key = key();
        BlockPos start = h.absolutePos(new BlockPos(2, 1, 4)), end = h.absolutePos(new BlockPos(6, 1, 4));
        floor(level, start, Blocks.STONE); floor(level, end, Blocks.STONE);
        var source = stone(level, start, key, "Home"); var target = stone(level, end, key, "Henge");
        var foreign = stone(level, h.absolutePos(new BlockPos(8, 1, 8)), key(), "Other network");
        var player = h.makeMockServerPlayerInLevel(); player.setPos(Vec3.atBottomCenterOf(start.south())); Vec3 initial = player.position();
        StoneTravel.travel(player, source.id(), target.id()); h.assertTrue(player.position().equals(initial), "Unsolicited travel accepted");
        StoneTravel.open(player, source, 0); StoneTravel.travel(player, source.id(), foreign.id());
        h.assertTrue(player.position().equals(initial), "Foreign key accepted");
        player.setPos(initial.add(12, 0, 0)); Vec3 away = player.position();
        StoneTravel.travel(player, source.id(), target.id()); h.assertTrue(player.position().equals(away), "Travel accepted after leaving the source");
        player.setPos(initial);
        StoneTravel.travel(player, source.id(), target.id());
        h.assertTrue(player.position().distanceTo(Vec3.atBottomCenterOf(end)) < 3 && !player.position().equals(initial), "Matching endpoint did not teleport");
        Vec3 arrived = player.position(); StoneTravel.travel(player, source.id(), target.id()); h.assertTrue(player.position().equals(arrived), "Used session remained replayable");
        level.getServer().getPlayerList().remove(player); player.discard(); h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void crowdedDestinationsUseNearbyFallbackButRemovedDestinationsReject(GameTestHelper h) {
        var level = h.getLevel(); String key = key();
        BlockPos start = h.absolutePos(new BlockPos(2, 1, 2)), end = h.absolutePos(new BlockPos(6, 1, 6));
        floor(level, start, Blocks.STONE); floor(level, end, Blocks.MAGMA_BLOCK);
        var source = stone(level, start, key, "Home"); var target = stone(level, end, key, "Blocked");
        var player = h.makeMockServerPlayerInLevel(); player.setPos(Vec3.atBottomCenterOf(start.south())); Vec3 initial = player.position();
        StoneTravel.open(player, source, 0); StoneTravel.travel(player, source.id(), target.id());
        h.assertTrue(!player.position().equals(initial), "No opening did not use the nearby fallback");
        floor(level, end, Blocks.STONE); target = stone(level, end, key, "Blocked");
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) if (x != 0 || z != 0)
            for (int y = 0; y <= 3; y++) level.setBlock(end.offset(x, y, z), Blocks.STONE.defaultBlockState(), 3);
        player.setPos(initial); StoneTravel.open(player, source, 0);
        StoneTravel.travel(player, source.id(), target.id());
        h.assertTrue(!player.position().equals(initial) && !level.noCollision(player, player.getBoundingBox()), "Obstructed destination did not use occupied fallback");
        UUID removed = target.id(); level.setBlock(end, Blocks.AIR.defaultBlockState(), 3);
        player.setPos(initial); StoneTravel.open(player, source, 0);
        StoneTravel.travel(player, source.id(), removed); h.assertTrue(player.position().equals(initial), "Removed destination moved player");
        level.getServer().getPlayerList().remove(player); player.discard(); h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void aMatchingStoneInAnotherDimensionCannotBeSelected(GameTestHelper h) {
        var level = h.getLevel(); String key = key(); BlockPos start = h.absolutePos(new BlockPos(4, 1, 4));
        floor(level, start, Blocks.STONE); var source = stone(level, start, key, "Home");
        var destination = level.getServer().getLevel(Level.END); BlockPos end = new BlockPos(80, 70, 80);
        floor(destination, end, Blocks.STONE); var target = stone(destination, end, key, "End Sanctuary");
        var player = h.makeMockServerPlayerInLevel(); player.setPos(Vec3.atBottomCenterOf(start.south())); Vec3 initial = player.position();
        StoneTravel.open(player, source, 0); StoneTravel.travel(player, source.id(), target.id());
        h.assertTrue(player.level() == level && player.position().equals(initial), "Cross-dimension request moved the player");
        destination.setBlock(end, Blocks.AIR.defaultBlockState(), 3);
        level.getServer().getPlayerList().remove(player); player.discard(); h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void menuRenamePreservesBindingAppearanceSavedNameAndMinedItem(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(4, 1, 4)); floor(level, pos, Blocks.STONE);
        String key = key(); var source = stone(level, pos, key, "Home"); UUID id = source.id();
        var lower = level.getBlockState(pos); var upper = level.getBlockState(pos.above());
        var player = h.makeMockServerPlayerInLevel(); player.setPos(Vec3.atBottomCenterOf(pos.south()));
        try {
            StoneTravel.open(player, source, 0);
            h.assertTrue(StoneTravel.rename(player, id, "  Northwatch Sanctum  ", 0), "Nearby menu rename was rejected");
            h.assertTrue(source.name().equals("Northwatch Sanctum") && source.id().equals(id) && source.key().equals(key)
                    && level.getBlockState(pos).equals(lower) && level.getBlockState(pos.above()).equals(upper),
                    "Renaming changed the endpoint, signature, facing or model");
            var directory = StoneDirectory.get(level.getServer());
            h.assertTrue(directory.network().get(id).orElseThrow().name().equals(source.name())
                    && directory.network().peers(key).size() == 1, "Renaming lost or duplicated the directory endpoint");
            var restored = new StandingStoneEntity(pos, lower);
            restored.loadWithComponents(source.saveWithFullMetadata(level.registryAccess()), level.registryAccess());
            var restoredDirectory = StoneDirectory.load(directory.save(new CompoundTag(), level.registryAccess()), level.registryAccess());
            h.assertTrue(restored.name().equals(source.name()) && restored.id().equals(id) && restored.key().equals(key)
                    && restoredDirectory.network().get(id).orElseThrow().name().equals(source.name()), "Renamed endpoint or directory lost the label on reload");
            var drops = Block.getDrops(lower, level, pos, source, player, new ItemStack(Items.DIAMOND_PICKAXE));
            h.assertTrue(drops.size() == 1 && drops.getFirst().getHoverName().getString().equals(source.name())
                    && StandingStones.key(drops.getFirst()).orElseThrow().equals(key), "Renamed mined item lost label or binding");
            for (var invalid : List.of("", "  ", "\n\u0000", "x".repeat(65), source.name()))
                h.assertTrue(!StoneTravel.rename(player, id, invalid, 0) && source.name().equals("Northwatch Sanctum"), "Invalid or unchanged name mutated the endpoint");
        } finally { level.getServer().getPlayerList().remove(player); player.discard(); }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void menuRenameRejectsMissingRemoteForeignAndReplacedSourceSessions(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(3, 1, 3)); floor(level, pos, Blocks.STONE);
        var source = stone(level, pos, key(), "Home"); UUID id = source.id();
        var foreign = stone(level, h.absolutePos(new BlockPos(7, 1, 7)), key(), "Other network");
        var player = h.makeMockServerPlayerInLevel(); var nearby = Vec3.atBottomCenterOf(pos.south()); player.setPos(nearby);
        try {
            h.assertTrue(!StoneTravel.rename(player, id, "Unsolicited", 0), "Rename without a menu session accepted");
            StoneTravel.open(player, source, 0);
            h.assertTrue(!StoneTravel.rename(player, foreign.id(), "Foreign", 0) && foreign.name().equals("Other network"), "Menu session renamed another endpoint");
            player.setPos(nearby.add(12, 0, 0));
            h.assertTrue(!StoneTravel.rename(player, id, "Remote", 0) && source.name().equals("Home"), "Rename accepted after leaving the source");
            player.setPos(nearby); level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            var replacement = stone(level, pos, source.key(), "Replacement");
            h.assertTrue(!StoneTravel.rename(player, id, "Stale", 0) && replacement.name().equals("Replacement"), "Stale session renamed a replacement stone");
        } finally { level.getServer().getPlayerList().remove(player); player.discard(); }
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "standing_stones")
    public static void networkPagesIncludeEverySameDimensionPeerAndRetainCurrentName(GameTestHelper h) {
        var level = h.getLevel(); var pos = h.absolutePos(new BlockPos(4, 1, 4)); floor(level, pos, Blocks.STONE);
        String key = key(); var source = stone(level, pos, key, "Zulu Home");
        var directory = StoneDirectory.get(level.getServer()); var ids = new HashSet<UUID>(); ids.add(source.id());
        var extra = new ArrayList<UUID>(); var player = h.makeMockServerPlayerInLevel();
        try {
            // Persistent unloaded destinations must remain reachable without loading their chunks for the list.
            for (int i = 0; i < 20; i++) {
                UUID id = UUID.randomUUID(); ids.add(id); extra.add(id);
                directory.put(new StoneNetwork.Node(id, key, level.dimension().location(), new BlockPos(10000 + i, 70, 10000), "Peer %02d".formatted(i)));
            }
            UUID otherDimension = UUID.randomUUID(), otherKey = UUID.randomUUID(); extra.add(otherDimension); extra.add(otherKey);
            directory.put(new StoneNetwork.Node(otherDimension, key, Level.END.location(), new BlockPos(0, 70, 0), "Other dimension"));
            directory.put(new StoneNetwork.Node(otherKey, key(), level.dimension().location(), new BlockPos(20000, 70, 0), "Other key"));
            var seen = new HashSet<UUID>();
            for (int page = 0; page < 3; page++) {
                var view = StoneTravel.view(player, source, page);
                h.assertTrue(view.page() == page && view.total() == 21 && view.sourceName().equals("Zulu Home")
                        && view.destinations().size() == (page < 2 ? 8 : 5), "Incorrect page size, total or current name");
                var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), level.registryAccess());
                try {
                    StoneTravelPayloads.View.CODEC.encode(buffer, view);
                    h.assertTrue(StoneTravelPayloads.View.CODEC.decode(buffer).equals(view), "Network view codec lost current name or page");
                } finally { buffer.release(); }
                for (var destination : view.destinations()) h.assertTrue(seen.add(destination.id()), "Duplicate destination across pages");
            }
            h.assertTrue(seen.equals(ids), "Pages omitted peers or admitted foreign dimensions/keys");
            h.assertTrue(StoneTravel.view(player, source, Integer.MIN_VALUE).page() == 0
                    && StoneTravel.view(player, source, Integer.MAX_VALUE).page() == 2, "Out-of-range pages were not bounded");
            var rename = new StoneTravelPayloads.Rename(source.id(), "Northwatch", 2);
            var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), level.registryAccess());
            try {
                StoneTravelPayloads.Rename.CODEC.encode(buffer, rename);
                h.assertTrue(StoneTravelPayloads.Rename.CODEC.decode(buffer).equals(rename), "Rename codec lost source, label or page");
            } finally { buffer.release(); }
        } finally {
            extra.forEach(directory::remove); level.getServer().getPlayerList().remove(player); player.discard();
        }
        h.succeed();
    }

}
