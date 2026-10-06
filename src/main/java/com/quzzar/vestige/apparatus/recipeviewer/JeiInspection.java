package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.apparatus.recipeviewer.jei.VestigeJeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Loaded only by the opt-in JEI inspection, keeping absent optional APIs out of client event subscribers. */
final class JeiInspection {
    static long count(){var runtime=VestigeJeiPlugin.runtime();return runtime==null?0:runtime.getRecipeManager().createRecipeLookup(VestigeJeiPlugin.TYPE).get().count();}
    static void show(ItemStack stack){var runtime=VestigeJeiPlugin.runtime();runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,stack));}
    static long matches(ItemStack stack,boolean uses){var runtime=VestigeJeiPlugin.runtime();var focus=runtime.getJeiHelpers().getFocusFactory().createFocus(uses?RecipeIngredientRole.INPUT:RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,stack);return runtime.getRecipeManager().createRecipeLookup(VestigeJeiPlugin.TYPE).limitFocus(List.of(focus)).get().count();}
    static long magneticSearch() {
        var filter=VestigeJeiPlugin.runtime().getIngredientFilter();var previous=filter.getFilterText();
        try {
            filter.setFilterText("Magnetic Attraction");
            return filter.getFilteredItemStacks().stream().filter(stack -> com.quzzar.vestige.apparatus.ScrollItems.scroll(stack)
                    .map(scroll -> scroll.spell().getPath().equals("pf2_magnetic_attraction")).orElse(false)).count();
        } finally {filter.setFilterText(previous);}
    }
    static int fireballCapacity(){return VestigeJeiPlugin.runtime().getRecipeManager().createRecipeLookup(VestigeJeiPlugin.TYPE).get().filter(r -> r.spell().map(s -> s.getPath().equals("fireball")).orElse(false)).mapToInt(RitualDisplays.Entry::capacity).findFirst().orElse(0);}
}
