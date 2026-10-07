package com.quzzar.vestige.apparatus.recipeviewer.jei;

import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Base and output slots have linked alternatives; focusing a wand chooses its actual body. */
public final class WandJeiCategory implements IRecipeCategory<WandDisplays.Entry> {
    private final IDrawable icon;
    public WandJeiCategory(IGuiHelper helper){icon=helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ScrollItems.WAND.get()));}
    public RecipeType<WandDisplays.Entry> getRecipeType(){return VestigeJeiPlugin.WAND_TYPE;}
    public Component getTitle(){return Component.translatable("vestige.viewer.wand_title");}
    public IDrawable getIcon(){return icon;}
    public int getWidth(){return RitualDiagram.WIDTH;}public int getHeight(){return RitualDiagram.HEIGHT;}
    public boolean needsRecipeBorder(){return false;}
    public void setRecipe(IRecipeLayoutBuilder builder,WandDisplays.Entry recipe,IFocusGroup focus){
        var diagram=recipe.diagram();var bodies=recipe.bases();
        var base=builder.addSlot(RecipeIngredientRole.INPUT,RitualDiagram.x(diagram,WandRecipe.BASE),RitualDiagram.y(diagram,WandRecipe.BASE))
                .addItemStacks(bodies.stream().map(b -> new ItemStack(b.ingredient())).toList());
        for(var part:diagram.offerings())if(part.seat()!=WandRecipe.BASE)builder.addSlot(RecipeIngredientRole.INPUT,RitualDiagram.x(diagram,part.seat()),RitualDiagram.y(diagram,part.seat()))
                .addItemStack(recipe.offering(part.seat(),bodies.getFirst()));
        var output=builder.addSlot(RecipeIngredientRole.OUTPUT,RitualDiagram.OUTPUT_X,RitualDiagram.OUTPUT_Y).addItemStacks(recipe.outputs());
        builder.createFocusLink(base,output);
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStacks(ApparatusBlocks.SPELLSTONES.values().stream().map(b -> new ItemStack(b.get())).toList());
        builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStacks(ApparatusBlocks.PLINTHS.values().stream().map(b -> new ItemStack(b.get())).toList());
    }
    public void draw(WandDisplays.Entry recipe,IRecipeSlotsView slots,GuiGraphics graphics,double x,double y){RitualDiagram.draw(graphics,recipe.diagram());}
}
