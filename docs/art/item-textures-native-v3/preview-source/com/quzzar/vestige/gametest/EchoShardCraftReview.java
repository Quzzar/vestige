package com.quzzar.vestige.gametest;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;

/** Isolated review fixture only; ordinary production builds exclude this source. */
@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EchoShardCraftReview {
    @GameTest(template="empty_9x3x9", batch="echo_shard_craft_review")
    public static void vanillaMenusConsumeOneOfEachAndUnlockRecipe(GameTestHelper helper) {
        var level = helper.getLevel();
        var id = VestigeMainMod.location("echo_shard");
        var recipe = level.getRecipeManager().byKey(id).orElseThrow();
        var ingredients = List.of(Items.AMETHYST_SHARD, Items.SOUL_SAND, Items.GHAST_TEAR);
        for (int a = 0; a < 3; a++) for (int b = 0; b < 3; b++) {
            if (a == b) continue;
            var input = CraftingInput.of(2, 2, List.of(new ItemStack(ingredients.get(a)), ItemStack.EMPTY,
                    new ItemStack(ingredients.get(b)), new ItemStack(ingredients.get(3 - a - b))));
            var match = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
            helper.assertTrue(match.id().equals(id), "Unordered 2x2 grid did not match Echo Shard");
            var output = match.value().assemble(input, level.registryAccess());
            helper.assertTrue(output.is(Items.ECHO_SHARD) && output.getCount() == 1, "Incorrect Echo Shard output");
        }
        var extra = CraftingInput.of(2, 2, List.of(new ItemStack(Items.AMETHYST_SHARD), new ItemStack(Items.SOUL_SAND),
                new ItemStack(Items.GHAST_TEAR), new ItemStack(Items.AMETHYST_SHARD)));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, extra, level).isEmpty(), "Extra ingredient matched");
        var pearl = CraftingInput.of(2, 2, List.of(new ItemStack(Items.AMETHYST_SHARD), new ItemStack(Items.SOUL_SAND),
                new ItemStack(Items.ENDER_PEARL), ItemStack.EMPTY));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, pearl, level).isEmpty(), "Superseded Pearl recipe matched");
        try (var player = SurvivalTestPlayer.create(helper)) {
            helper.assertTrue(!player.getRecipeBook().contains(recipe), "Fresh player already knows the recipe");
            player.getInventory().setItem(0, new ItemStack(Items.GHAST_TEAR));
            CriteriaTriggers.INVENTORY_CHANGED.trigger(player, player.getInventory(), player.getInventory().getItem(0));
            helper.assertTrue(player.getRecipeBook().contains(recipe), "Ghast Tear did not unlock the recipe book entry");
            var advancement = level.getServer().getAdvancements().get(VestigeMainMod.location("recipes/echo_shard"));
            helper.assertTrue(advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone(), "Unlock advancement did not complete");
            player.getInventory().clearContent();
            var pos = helper.absolutePos(new BlockPos(2, 1, 2));
            level.setBlockAndUpdate(pos, Blocks.CRAFTING_TABLE.defaultBlockState());
            var table = new CraftingMenu(1, player.getInventory(), ContainerLevelAccess.create(level, pos));
            craft(helper, player, table, new int[]{1, 5, 9});
            var inventory = new InventoryMenu(player.getInventory(), false, player);
            craft(helper, player, inventory, new int[]{1, 3, 4});
        }
        helper.succeed();
    }

    private static void craft(GameTestHelper helper, net.minecraft.server.level.ServerPlayer player,
                              AbstractContainerMenu menu, int[] slots) {
        var ingredients = List.of(Items.AMETHYST_SHARD, Items.SOUL_SAND, Items.GHAST_TEAR);
        for (int i = 0; i < slots.length; i++) menu.getSlot(slots[i]).set(new ItemStack(ingredients.get(i), 2));
        var result = menu.getSlot(0);
        helper.assertTrue(result.getItem().is(Items.ECHO_SHARD) && result.getItem().getCount() == 1, "Vanilla menu output missing");
        var collected = result.remove(1);
        result.onTake(player, collected);
        for (int i = 0; i < slots.length; i++) {
            var remaining = menu.getSlot(slots[i]).getItem();
            helper.assertTrue(remaining.is(ingredients.get(i)) && remaining.getCount() == 1, "Craft did not consume exactly one ingredient");
        }
    }
}
