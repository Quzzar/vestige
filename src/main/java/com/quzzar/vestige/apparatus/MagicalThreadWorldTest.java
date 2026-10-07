package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagicalThreadWorldTest {
    private static final BlockPos CENTER = new BlockPos(4, 1, 4);
    private static final LeylineShaping.Geometry GEOMETRY = new LeylineShaping.Geometry(8,
            LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
    private static RitualCrafting.Layout ritual(GameTestHelper h, MagicalThreadRecipe.Type type, int stringSeat, boolean outer) {
        h.setBlock(CENTER, ApparatusBlocks.SPELLSTONE.get());
        for (int i = 0; i < 8; i++) if ((i & 1) == 0 || outer)
            h.setBlock(CENTER.offset(GEOMETRY.offset(i)), ApparatusBlocks.PLINTH.get());
        var center = (OfferingBlockEntity) h.getBlockEntity(CENTER);
        var layout = LeylineStructure.find(center, 4).getFirst();
        for (int i = 0; i < 3; i++) layout.stands().get((stringSeat + i * 2) % 8)
                .insert(new ItemStack(MagicalThreadRecipe.ingredients().get(i)));
        layout.stands().get(stringSeat).installMaterial(new ItemStack(BuiltInRegistries.ITEM.get(type.material())));
        layout.stands().get((stringSeat + 2) % 8).installMaterial(new ItemStack(Items.COPPER_BLOCK));
        layout.stands().get((stringSeat + 4) % 8).installMaterial(new ItemStack(Items.SOUL_SAND));
        layout.stands().get((stringSeat + 6) % 8).installMaterial(new ItemStack(Items.EMERALD_BLOCK));
        return layout;
    }
    private static net.minecraft.world.entity.player.Player quietPlayer(GameTestHelper h) {
        return new net.minecraft.world.entity.player.Player(h.getLevel(), h.absolutePos(CENTER), 0,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "quiet-thread-player")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
            @Override public void displayClientMessage(net.minecraft.network.chat.Component message, boolean overlay) {
                throw new AssertionError("Thread ritual emitted gameplay text: " + message.getString());
            }
            @Override public void sendSystemMessage(net.minecraft.network.chat.Component message) {
                throw new AssertionError("Thread ritual emitted gameplay text: " + message.getString());
            }
        };
    }
    private static void craft(GameTestHelper h, MagicalThreadRecipe.Type type, int stringSeat) {
        var layout = ritual(h, type, stringSeat, false);
        var inputs = RitualInputs.capture(layout);
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Thread ritual rejected");
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.BUSY, "Duplicate activation accepted");
        h.runAfterDelay(65, () -> {
            var output = RitualTestOutput.stack(layout.center());
            h.assertTrue(output.is(type.item()) && output.getCount() == 1 && !output.has(DataComponents.CUSTOM_DATA), "Wrong count/type or incidental shaping");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && layout.center().displayedItem().isEmpty(), "Offerings/reference were not correct after commitment");
            h.assertTrue(inputs.nodes().stream().allMatch(n -> ItemStack.matches(n.material(), layout.stands().get(n.seat()).materialItem())), "Imbuement consumed or changed");
            h.assertTrue(layout.blocks().stream().noneMatch(OfferingBlockEntity::busy), "Craft retained locks");
            h.succeed();
        });
    }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads", timeoutTicks = 90)
    public static void ensorcelledCraftsWithDiamond(GameTestHelper h) { craft(h, MagicalThreadRecipe.Type.ENSORCELLED, 0); }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads", timeoutTicks = 90)
    public static void callousCraftsWithIron(GameTestHelper h) { craft(h, MagicalThreadRecipe.Type.CALLOUS, 2); }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads", timeoutTicks = 90)
    public static void smolderingCraftsWithGold(GameTestHelper h) { craft(h, MagicalThreadRecipe.Type.SMOLDERING, 4); }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads", timeoutTicks = 90)
    public static void lacedCraftsWithEmerald(GameTestHelper h) { craft(h, MagicalThreadRecipe.Type.LACED, 6); }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads", timeoutTicks = 90)
    public static void consecratedCraftsWithGlowstone(GameTestHelper h) { craft(h, MagicalThreadRecipe.Type.CONSECRATED, 0); }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads_outer", timeoutTicks = 90)
    public static void occupiedOuterPlinthsAreIgnoredEvenIfChangedDuringCraft(GameTestHelper h) {
        var layout = ritual(h, MagicalThreadRecipe.Type.CONSECRATED, 2, true);
        var advanced = RitualCrafting.layout(layout.center());
        for (int i : List.of(1, 3, 5, 7)) {
            advanced.stands().get(i).insert(new ItemStack(Items.DIAMOND));
            advanced.stands().get(i).installMaterial(new ItemStack(Items.DIAMOND_BLOCK));
        }
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Outer offerings prevented inner recipe");
        h.runAfterDelay(10, () -> { advanced.stands().get(1).remove(); advanced.stands().get(1).insert(new ItemStack(Items.PAPER)); });
        h.runAfterDelay(65, () -> {
            h.assertTrue(RitualTestOutput.stack(layout.center()).is(MagicalThreadRecipe.Type.CONSECRATED.item()), "Outer edit changed/canceled the output");
            for (int i : List.of(1, 3, 5, 7)) h.assertTrue(!advanced.stands().get(i).displayedItem().isEmpty()
                    && advanced.stands().get(i).materialItem().is(Items.DIAMOND_BLOCK) && !advanced.stands().get(i).busy(), "Outer node participated in consumption/locking");
            h.succeed();
        });
    }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads_rejection")
    public static void missingUnsupportedOrMispairedSelectorAndFourthOfferingReject(GameTestHelper h) {
        var layout = ritual(h, MagicalThreadRecipe.Type.ENSORCELLED, 0, false);
        var string = layout.stands().get(0); string.removeMaterial();
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.INVALID, "Missing selector accepted");
        string.installMaterial(new ItemStack(Items.COPPER_BLOCK));
        h.assertTrue(MagicalThreadRecipe.result(RitualInputs.capture(layout)).isEmpty(), "Unsupported selector accepted");
        string.removeMaterial(); string.installMaterial(new ItemStack(Items.DIAMOND_BLOCK));
        layout.stands().get(6).insert(new ItemStack(Items.PAPER));
        h.assertTrue(!MagicalThreadRecipe.matches(layout.items()) && RitualTestOutput.stack(layout.center()).isEmpty()
                && layout.items().stream().filter(i -> !i.isEmpty()).count() == 4, "Extra offering crafted/consumed ingredients");
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads_cancel", timeoutTicks = 80)
    public static void changingStringSelectorCancelsBeforeConsumingAnything(GameTestHelper h) {
        var layout = ritual(h, MagicalThreadRecipe.Type.ENSORCELLED, 0, false);
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Initial craft rejected");
        h.runAfterDelay(10, () -> {
            var string = layout.stands().get(0); string.unlock(); string.removeMaterial(); string.installMaterial(new ItemStack(Items.IRON_BLOCK));
        });
        h.runAfterDelay(65, () -> assertCanceled(h, layout));
    }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads_empty", timeoutTicks = 80)
    public static void fillingReservedEmptySeatCancelsWithoutPartialConsumption(GameTestHelper h) {
        var layout = ritual(h, MagicalThreadRecipe.Type.CALLOUS, 0, false);
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Initial craft rejected");
        h.runAfterDelay(10, () -> layout.stands().get(6).insert(new ItemStack(Items.PAPER)));
        h.runAfterDelay(65, () -> assertCanceled(h, layout));
    }
    private static void assertCanceled(GameTestHelper h, RitualCrafting.Layout layout) {
        h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty()
                && layout.items().stream().filter(i -> !i.isEmpty()).count() >= 3
                && layout.stands().get(0).displayedItem().is(Items.STRING)
                && !layout.stands().get(2).displayedItem().isEmpty()
                && layout.stands().get(4).displayedItem().is(Items.HONEYCOMB)
                && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy), "Canceled ritual spent offerings, spawned output or retained locks");
        h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads_spawn", timeoutTicks = 80)
    public static void canceledOutputSpawnPreservesAllIngredientsAndSockets(GameTestHelper h) {
        var layout = ritual(h, MagicalThreadRecipe.Type.LACED, 0, false);
        Consumer<EntityJoinLevelEvent> cancel = event -> {
            if (event.getLevel() == h.getLevel() && event.getEntity() instanceof ItemEntity item && item.getItem().is(MagicalThreadRecipe.Type.LACED.item())
                    && item.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(layout.center().getBlockPos())) < 4) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(cancel);
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Initial craft rejected");
        h.runAfterDelay(65, () -> { NeoForge.EVENT_BUS.unregister(cancel); assertCanceled(h, layout); });
    }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads_join", timeoutTicks = 80)
    public static void ingredientEditedDuringOutputJoinPreventsCommitment(GameTestHelper h) {
        var layout = ritual(h, MagicalThreadRecipe.Type.SMOLDERING, 0, false);
        Consumer<EntityJoinLevelEvent> edit = event -> {
            if (event.getLevel() == h.getLevel() && event.getEntity() instanceof ItemEntity item && item.getItem().is(MagicalThreadRecipe.Type.SMOLDERING.item())
                    && item.position().distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(layout.center().getBlockPos())) < 4) {
                layout.stands().get(2).remove(); layout.stands().get(2).insert(new ItemStack(Items.PAPER));
            }
        };
        NeoForge.EVENT_BUS.addListener(edit);
        h.assertTrue(RitualCrafting.activate(quietPlayer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Initial craft rejected");
        h.runAfterDelay(65, () -> { NeoForge.EVENT_BUS.unregister(edit); assertCanceled(h, layout); });
    }
    @GameTest(template = "empty_9x3x9", batch = "magical_threads_persistence")
    public static void componentIdentityAndSocketPersistenceSurviveSaving(GameTestHelper h) {
        var layout = ritual(h, MagicalThreadRecipe.Type.CONSECRATED, 0, false);
        var saved = layout.stands().get(0).saveWithoutMetadata(h.getLevel().registryAccess());
        layout.stands().get(0).remove(); layout.stands().get(0).removeMaterial();
        layout.stands().get(0).loadWithComponents(saved, h.getLevel().registryAccess());
        h.assertTrue(MagicalThreadRecipe.result(RitualInputs.capture(layout)).orElseThrow().is(MagicalThreadRecipe.Type.CONSECRATED.item()), "Socket/ingredient save lost recipe");
        for (var type : MagicalThreadRecipe.types()) {
            var stack = new ItemStack(type.item(), 3);
            var restored = ItemStack.parse(h.getLevel().registryAccess(), stack.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(ItemStack.matches(stack, restored) && stack.getMaxStackSize() == 64, "Thread did not round-trip or use provisional vanilla component stack limit");
        }
        h.succeed();
    }
}
