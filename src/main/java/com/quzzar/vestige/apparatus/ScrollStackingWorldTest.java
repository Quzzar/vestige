package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.gametest.SurvivalTestPlayer;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ScrollStackingWorldTest {
    private static ResourceLocation id(String name) { return VestigeMainMod.location(name); }

    private static ItemStack reaching(int degree, double cost) {
        return ScrollItems.shapedScroll(id("fireball"), new LeylineShaping.Modifiers(1, 1, 1, cost),
                List.of(new Spellshaping.Selection(id("reaching"), degree)));
    }

    @GameTest(template = "empty_9x3x9", batch = "scroll_stacking")
    public static void identicalScrollsMergeToSixteenAndOverflowToAnotherSlot(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var first = reaching(1, 1).copyWithCount(15);
        var added = reaching(1, 1).copyWithCount(3);
        h.assertTrue(first.getMaxStackSize() == 16, "Scroll stack limit is not sixteen");
        player.getInventory().setItem(0, first);
        h.assertTrue(player.getInventory().add(added) && added.isEmpty(), "Inventory rejected compatible scrolls");
        var stacks = player.getInventory().items.stream().filter(s -> !s.isEmpty()).toList();
        h.assertTrue(stacks.size() == 2 && stacks.get(0).getCount() == 16 && stacks.get(1).getCount() == 2,
                "Merge exceeded the limit or lost overflow scrolls");
        h.assertTrue(stacks.stream().allMatch(s -> ItemStack.isSameItemSameComponents(s, reaching(1, 1))),
                "Inventory merge changed the spell or shaping");
        h.succeed();
    }

    @GameTest(template = "empty_9x3x9", batch = "scroll_stacking")
    public static void differentSpellsAugmentsDegreesAndLeylineValuesStaySeparate(GameTestHelper h) {
        var samples = List.of(ScrollItems.scroll(id("fireball")), ScrollItems.scroll(id("pf2_shield")),
                reaching(1, 1), reaching(2, 1), reaching(1, 1.2),
                ScrollItems.shapedScroll(id("fireball"), new LeylineShaping.Modifiers(1, 1.2, 1, 1),
                        List.of(new Spellshaping.Selection(id("reaching"), 1))),
                ScrollItems.shapedScroll(id("fireball"), new LeylineShaping.Modifiers(1, 1, 1, 1),
                        List.of(new Spellshaping.Selection(id("bleeding"), 1))));
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (var sample : samples) {
            h.assertTrue(ScrollItems.scroll(sample).isPresent(), "Fixture contains an invalid scroll");
            h.assertTrue(player.getInventory().add(sample.copyWithCount(2)), "Inventory rejected a scroll variant");
        }
        var stacks = player.getInventory().items.stream().filter(s -> !s.isEmpty()).toList();
        h.assertTrue(stacks.size() == samples.size() && stacks.stream().allMatch(s -> s.getCount() == 2),
                "Different spell or shaping variants merged");
        for (var sample : samples) {
            h.assertTrue(stacks.stream().filter(s -> ItemStack.isSameItemSameComponents(s, sample)).count() == 1,
                    "A scroll variant was changed or duplicated");
        }
        h.succeed();
    }

    @GameTest(template = "empty_9x3x9", batch = "scroll_stacking")
    public static void splitAndSavePreserveCountAndExactShaping(GameTestHelper h) {
        var stack = reaching(2, 1.2).copyWithCount(16);
        var data = ScrollItems.scroll(stack).orElseThrow();
        var split = stack.split(3);
        h.assertTrue(stack.getCount() == 13 && split.getCount() == 3
                        && ItemStack.isSameItemSameComponents(stack, split), "Splitting changed scroll magic or count");
        for (var portion : List.of(stack, split)) {
            var restored = ItemStack.parse(h.getLevel().registryAccess(), portion.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(ItemStack.matches(portion, restored) && ScrollItems.scroll(restored).orElseThrow().equals(data),
                    "Saved scroll stack lost its count or shaping");
        }
        h.succeed();
    }

    @GameTest(template = "empty_9x3x9", batch = "scroll_stacking_cast", timeoutTicks = 100)
    public static void successfulCastConsumesOneAndCooldownPreservesTheRest(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var spell = id("pf2_shield");
        SpellKnowledge.identify(player, spell);
        player.getPersistentData().putDouble("vestige:mana", 100);
        player.setItemInHand(InteractionHand.MAIN_HAND, ScrollItems.scroll(spell).copyWithCount(16));
        var original = player.getMainHandItem().copyWithCount(1);
        h.assertTrue(ScrollCasting.cast(player, player.getMainHandItem()), "Affordable stacked scroll cast was rejected");
        h.runAfterDelay(1, () -> {
            h.assertTrue(player.getMainHandItem().getCount() == 15
                            && ItemStack.isSameItemSameComponents(original, player.getMainHandItem()),
                    "Cast consumed multiple scrolls or changed the remaining magic");
            double mana = player.getPersistentData().getDouble("vestige:mana");
            h.assertTrue(!ScrollCasting.cast(player, player.getMainHandItem())
                            && player.getMainHandItem().getCount() == 15
                            && player.getPersistentData().getDouble("vestige:mana") == mana,
                    "Cooldown rejection consumed another scroll or payment");
        });
        h.runAfterDelay(80, () -> {
            h.assertTrue(player.getMainHandItem().getCount() == 15, "Settling consumed the scroll stack twice");
            NativeMagic.session(h.getLevel().getServer()).runtime().dispelActor(player.getUUID());
            h.succeed();
        });
    }

    @GameTest(template = "empty_9x3x9", batch = "scroll_stacking_cost", timeoutTicks = 80)
    public static void failedPaymentPreservesTheEntireStack(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.getPersistentData().putDouble("vestige:mana", 0);
        var spell = id("fireball");
        var original = ScrollItems.scroll(spell).copyWithCount(16);
        player.setItemInHand(InteractionHand.MAIN_HAND, original.copy());
        ScrollCasting.cast(player, player.getMainHandItem());
        h.runAfterDelay(65, () -> {
            h.assertTrue(ItemStack.matches(original, player.getMainHandItem()) && !SpellKnowledge.identified(player, spell),
                    "Failed payment consumed, changed or identified stacked scrolls");
            h.succeed();
        });
    }

    @GameTest(template = "empty_9x3x9", batch = "scroll_stacking_dismantling")
    public static void dismantlingConsumesOneScrollAndProducesThreeFragments(GameTestHelper h) {
        try (var player = SurvivalTestPlayer.create(h)) {
            var grid = player.inventoryMenu.getCraftSlots();
            var original = reaching(1, 1).copyWithCount(16);
            grid.setItem(0, original.copy());
            var input = grid.asCraftInput();
            var recipe = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow();
            var output = recipe.value().assemble(input, h.getLevel().registryAccess());
            h.assertTrue(output.is(ScrollItems.FRAGMENT.get()) && output.getCount() == 3,
                    "Stacked-scroll dismantling multiplied the output");
            player.inventoryMenu.getSlot(0).onTake(player, output);
            h.assertTrue(grid.getItem(0).getCount() == 15 && ItemStack.isSameItemSameComponents(original, grid.getItem(0)),
                    "Taking fragments consumed more than one scroll or changed the remaining magic");
            player.getInventory().add(output);
            for (var fragment : List.copyOf(player.getInventory().items)) ScrollFragmentItem.resolve(player, fragment);
            h.assertTrue(player.getInventory().items.stream().filter(s -> s.is(ScrollItems.FRAGMENT.get()))
                            .mapToInt(ItemStack::getCount).sum() == 3,
                    "Dismantling lost or duplicated fragments");
        }
        h.succeed();
    }
}
