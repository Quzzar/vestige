package com.quzzar.vestige.apparatus.recipeviewer.emi;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Standalone EMI support uses its public API, with no dependency on JEI or its bridge. */
@EmiEntrypoint
public final class VestigeEmiPlugin implements EmiPlugin {
    public static EmiRecipeCategory category;
    @Override public void register(EmiRegistry registry) {
        category=new EmiRecipeCategory(VestigeMainMod.location("spellstone_ritual"),EmiStack.of(ApparatusBlocks.SPELLSTONE.get()));
        registry.addCategory(category);
        ApparatusBlocks.all().forEach(block -> registry.addWorkstation(category,EmiStack.of(block)));
        registry.setDefaultComparison(ScrollItems.SCROLL.get(),Comparison.compareData(s -> RitualDisplays.subtype(s.getItemStack())));
        registry.setDefaultComparison(ScrollItems.FRAGMENT.get(),Comparison.compareData(s -> RitualDisplays.subtype(s.getItemStack())));
        registry.setDefaultComparison(ScrollItems.ATTUNEMENT_SHARD.get(),Comparison.DEFAULT_COMPARISON);
        for(var display:RitualViewerClient.displays()){registry.addRecipe(new Recipe(display));registry.addEmiStack(EmiStack.of(display.output()));}
    }
    public static final class Recipe implements EmiRecipe {
        private final RitualDisplays.Entry display;
        private final List<EmiIngredient> inputs;
        public RitualDisplays.Entry display(){return display;}
        public Recipe(RitualDisplays.Entry display){this.display=display;inputs=display.offerings().stream().map(o ->
                EmiIngredient.of(RitualDisplays.alternatives(o.ingredient()).stream().map(stack -> {
                    var ingredient=EmiStack.of(stack);var remainder=stack.getCraftingRemainingItem();
                    if(!remainder.isEmpty())ingredient.setRemainder(EmiStack.of(remainder));return ingredient;
                }).toList())).toList();}
        @Override public EmiRecipeCategory getCategory(){return category;}
        // Rituals use Vestige's catalog, not vanilla RecipeManager entries. EMI reserves '/' for synthetic recipes.
        @Override public ResourceLocation getId(){return ResourceLocation.fromNamespaceAndPath(display.id().getNamespace(),"/"+display.id().getPath());}
        @Override public List<EmiIngredient> getInputs(){return inputs;}
        @Override public List<EmiStack> getOutputs(){return List.of(EmiStack.of(display.output()));}
        @Override public List<EmiIngredient> getCatalysts(){return List.of(
                EmiIngredient.of(ApparatusBlocks.SPELLSTONES.values().stream().map(b -> EmiStack.of(b.get())).toList()),
                EmiIngredient.of(ApparatusBlocks.PLINTHS.values().stream().map(b -> EmiStack.of(b.get())).toList()));}
        @Override public int getDisplayWidth(){return RitualDiagram.WIDTH;}
        @Override public int getDisplayHeight(){return RitualDiagram.HEIGHT;}
        @Override public boolean supportsRecipeTree(){return !display.concealed() && !display.shapeless();}
        @Override public void addWidgets(WidgetHolder widgets) {
            // EMI clamps the holder height on small screens. Scale art, icons and hover bounds together.
            float scale=Math.min(1f,widgets.getHeight()/(float)RitualDiagram.HEIGHT);
            float offset=(RitualDiagram.WIDTH-RitualDiagram.WIDTH*scale)/2f;
            widgets.addDrawable(0,0,RitualDiagram.WIDTH,widgets.getHeight(),(graphics,mx,my,delta) -> {
                graphics.pose().pushPose();graphics.pose().translate(offset,0,0);graphics.pose().scale(scale,scale,1);
                RitualDiagram.draw(graphics,display);graphics.pose().popPose();
            });
            for(int i=0;i<inputs.size();i++) {
                var offering=display.offerings().get(i);
                widgets.add(new ScaledSlot(inputs.get(i),offset+(RitualDiagram.x(display,offering.seat())+8)*scale,
                        (RitualDiagram.y(display,offering.seat())+8)*scale,scale)).drawBack(false);
            }
            widgets.add(new ScaledSlot(getOutputs().getFirst(),offset+(RitualDiagram.OUTPUT_X+8)*scale,
                    (RitualDiagram.OUTPUT_Y+8)*scale,scale)).drawBack(false).recipeContext(this);
        }
    }
    /** Retains native EMI tooltips and interactions when the holder shrinks the drawing. */
    private static final class ScaledSlot extends SlotWidget {
        private final float centerX,centerY,scale;
        private ScaledSlot(EmiIngredient ingredient,float x,float y,float scale){super(ingredient,0,0);centerX=x;centerY=y;this.scale=scale;}
        @Override public Bounds getBounds(){return new Bounds((int)Math.floor(centerX-9*scale),(int)Math.floor(centerY-9*scale),(int)Math.ceil(18*scale),(int)Math.ceil(18*scale));}
        @Override public void drawStack(GuiGraphics graphics,int mouseX,int mouseY,float delta) {
            graphics.pose().pushPose();graphics.pose().translate(centerX-8*scale,centerY-8*scale,0);graphics.pose().scale(scale,scale,1);
            getStack().render(graphics,0,0,delta);graphics.pose().popPose();
        }
    }
}
