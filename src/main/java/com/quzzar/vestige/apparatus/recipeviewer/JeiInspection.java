package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.apparatus.recipeviewer.jei.VestigeJeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Loaded only by the opt-in JEI inspection, keeping absent optional APIs out of client event subscribers. */
final class JeiInspection {
    static long wands(){if(VestigeJeiPlugin.runtime()==null)return 0;return VestigeJeiPlugin.runtime().getRecipeManager().createRecipeLookup(VestigeJeiPlugin.WAND_TYPE).get().count();}
    static long wandMatches(ItemStack stack){var r=VestigeJeiPlugin.runtime();var focus=r.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,stack);return r.getRecipeManager().createRecipeLookup(VestigeJeiPlugin.WAND_TYPE).limitFocus(List.of(focus)).get().count();}
    static long count(){var runtime=VestigeJeiPlugin.runtime();return runtime==null?0:runtime.getRecipeManager().createRecipeLookup(VestigeJeiPlugin.TYPE).get().count();}
    static void show(ItemStack stack){var runtime=VestigeJeiPlugin.runtime();runtime.getRecipesGui().show(runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,stack));}
    static long matches(ItemStack stack,boolean uses){var runtime=VestigeJeiPlugin.runtime();var focus=runtime.getJeiHelpers().getFocusFactory().createFocus(uses?RecipeIngredientRole.INPUT:RecipeIngredientRole.OUTPUT,VanillaTypes.ITEM_STACK,stack);return runtime.getRecipeManager().createRecipeLookup(VestigeJeiPlugin.TYPE).limitFocus(List.of(focus)).get().count();}
    static long catalysts(ItemStack stack){var runtime=VestigeJeiPlugin.runtime();var focus=runtime.getJeiHelpers().getFocusFactory().createFocus(RecipeIngredientRole.CATALYST,VanillaTypes.ITEM_STACK,stack);return runtime.getRecipeManager().createRecipeLookup(VestigeJeiPlugin.TYPE).limitFocus(List.of(focus)).get().count();}
    static ItemStack hovered(RitualDisplays.Entry entry,int x,int y) {
        var runtime=VestigeJeiPlugin.runtime();var manager=runtime.getRecipeManager();
        var layout=manager.createRecipeLayoutDrawable(manager.getRecipeCategory(VestigeJeiPlugin.TYPE),entry,
                runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElseThrow();
        layout.setPosition(0,0);return layout.getIngredientUnderMouse(x,y,VanillaTypes.ITEM_STACK).orElse(ItemStack.EMPTY);
    }
    static void drawHover(net.minecraft.client.gui.GuiGraphics graphics,RitualDisplays.Entry entry,int originX,int originY,int mouseX,int mouseY) {
        var runtime=VestigeJeiPlugin.runtime();var manager=runtime.getRecipeManager();
        var layout=manager.createRecipeLayoutDrawable(manager.getRecipeCategory(VestigeJeiPlugin.TYPE),entry,
                runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElseThrow();
        layout.setPosition(originX,originY);layout.drawOverlays(graphics,mouseX,mouseY);
    }
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
