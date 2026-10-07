package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exercise the real conditional recipe reload with absent, individual and combined providers. */
@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PedestalCompatibilityTest {
    private static final List<String> PROVIDERS = List.of("irons_spellbooks", "supplementaries");

    private PedestalCompatibilityTest() { }

    @GameTest(template = "empty_3x3x3", batch = "pedestal_compat")
    public static void recipesAndUnlocksFollowProviderAndItemAvailability(GameTestHelper h) {
        for (String provider : PROVIDERS) {
            ResourceLocation source = ResourceLocation.fromNamespaceAndPath(provider, "pedestal");
            ResourceLocation recipe = VestigeMainMod.location("compat/" + provider + "/plinth");
            boolean available = ModList.get().isLoaded(provider) && BuiltInRegistries.ITEM.containsKey(source);
            h.assertTrue(h.getLevel().getRecipeManager().byKey(recipe).isPresent() == available,
                    "Conversion availability differs from the installed source pedestal: " + provider);
            h.assertTrue((h.getLevel().getServer().getAdvancements().get(
                    VestigeMainMod.location("recipes/compat/" + provider + "/plinth")) != null) == available,
                    "Recipe-book unlock did not follow the conversion condition: " + provider);
        }
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "pedestal_compat")
    public static void availablePedestalsConvertOneToOneInEitherCraftingGrid(GameTestHelper h) {
        for (String provider : PROVIDERS) {
            var holder = h.getLevel().getRecipeManager().byKey(VestigeMainMod.location("compat/" + provider + "/plinth"));
            if (holder.isEmpty()) continue;
            if (!(holder.get().value() instanceof CraftingRecipe recipe)) {
                h.fail("Pedestal conversion is not a crafting-grid recipe: " + provider);
                return;
            }
            var source = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(provider, "pedestal"));
            for (int width : List.of(2, 3)) {
                for (int slot = 0; slot < width * width; slot++) {
                    var stacks = new ArrayList<>(Collections.nCopies(width * width, ItemStack.EMPTY));
                    stacks.set(slot, new ItemStack(source, 3));
                    var input = CraftingInput.of(width, width, stacks);
                    h.assertTrue(recipe.matches(input, h.getLevel()), "Single pedestal failed in grid slot " + slot);
                    var selected = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel());
                    h.assertTrue(selected.isPresent() && selected.get().id().equals(holder.get().id()),
                            "Normal crafting selected a conflicting recipe: " + provider);
                    var output = recipe.assemble(input, h.getLevel().registryAccess());
                    h.assertTrue(output.is(ApparatusBlocks.PLINTH.get().asItem()) && output.getCount() == 1,
                            "Conversion changed finish or duplicated the pedestal: " + provider);
                    h.assertTrue(recipe.getRemainingItems(input).stream().allMatch(ItemStack::isEmpty),
                            "Conversion returned a second pedestal as a crafting remainder: " + provider);
                }
            }
            h.assertTrue(!recipe.matches(CraftingInput.of(2, 2, List.of(
                    new ItemStack(source), new ItemStack(source), ItemStack.EMPTY, ItemStack.EMPTY)), h.getLevel()),
                    "Two occupied pedestal slots were accepted by a one-pedestal recipe");
            for (var wrong : List.of(Items.STONE_BRICKS, ApparatusBlocks.PLINTH.get().asItem(),
                    ApparatusBlocks.PLINTHS.get(ApparatusMaterials.TUFF).get().asItem())) {
                h.assertTrue(!recipe.matches(CraftingInput.of(2, 2, List.of(
                        new ItemStack(wrong), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY)), h.getLevel()),
                        "Conversion accepted a native block or the wrong material");
            }
        }
        h.succeed();
    }
}
