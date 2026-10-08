package com.quzzar.vestige.apparatus.recipeviewer.emi;

import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.function.Function;

/** One entry per exact scroll/core/tip. Shared generated-slot seeds keep the pictured body/output paired. */
public final class WandEmiRecipe implements EmiRecipe {
    private final WandDisplays.Entry display;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;
    public WandEmiRecipe(WandDisplays.Entry display){
        this.display=display;this.inputs=buildInputs();this.outputs=display.outputs().stream().map(EmiStack::of).toList();
    }
    public WandDisplays.Entry display(){return display;}
    public EmiRecipeCategory getCategory(){return VestigeEmiPlugin.wandCategory;}
    public ResourceLocation getId(){return ResourceLocation.fromNamespaceAndPath(display.id().getNamespace(),"/"+display.id().getPath());}
    private List<EmiIngredient> buildInputs(){
        var inputs=new ArrayList<EmiIngredient>();var base=display.bases().getFirst();
        for(var offering:display.diagram().offerings())inputs.add(offering.seat()==WandRecipe.BASE
                ? EmiIngredient.of(display.bases().stream().map(b -> EmiStack.of(b.ingredient())).toList())
                : EmiStack.of(display.offering(offering.seat(),base)));
        return List.copyOf(inputs);
    }
    public List<EmiIngredient> getInputs(){return inputs;}
    public List<EmiStack> getOutputs(){return outputs;}
    public List<EmiIngredient> getCatalysts(){return List.of(EmiIngredient.of(ApparatusBlocks.SPELLSTONES.values().stream().map(b -> EmiStack.of(b.get())).toList()),EmiIngredient.of(ApparatusBlocks.PLINTHS.values().stream().map(b -> EmiStack.of(b.get())).toList()));}
    public int getDisplayWidth(){return RitualDiagram.WIDTH;}public int getDisplayHeight(){return RitualDiagram.HEIGHT;}
    public boolean supportsRecipeTree(){return true;}
    public void addWidgets(WidgetHolder holder){
        float scale=Math.min(1f,holder.getHeight()/(float)RitualDiagram.HEIGHT),offset=(RitualDiagram.WIDTH-RitualDiagram.WIDTH*scale)/2;
        holder.addDrawable(0,0,RitualDiagram.WIDTH,holder.getHeight(),(g,x,y,d) -> {g.pose().pushPose();g.pose().translate(offset,0,0);g.pose().scale(scale,scale,1);RitualDiagram.draw(g,display.diagram());g.pose().popPose();});
        var bases=display.bases();int seed=display.id().hashCode();
        for(var offering:display.diagram().offerings()) {
            Function<Random,EmiIngredient> generator=offering.seat()==WandRecipe.BASE ? random -> EmiStack.of(bases.get(random.nextInt(bases.size())).ingredient())
                    : random -> EmiStack.of(display.offering(offering.seat(),bases.getFirst()));
            holder.add(new ScaledGeneratedSlot(generator,seed,offset+(RitualDiagram.x(display.diagram(),offering.seat())+8)*scale,(RitualDiagram.y(display.diagram(),offering.seat())+8)*scale,scale)).drawBack(false);
        }
        holder.add(new ScaledGeneratedSlot(random -> EmiStack.of(display.output(bases.get(random.nextInt(bases.size())))),seed,
                offset+(RitualDiagram.OUTPUT_X+8)*scale,(RitualDiagram.OUTPUT_Y+8)*scale,scale)).drawBack(false).recipeContext(this);
    }
    private static final class ScaledGeneratedSlot extends GeneratedSlotWidget {
        private final float x,y,scale;
        ScaledGeneratedSlot(Function<Random,EmiIngredient> generator,int seed,float x,float y,float scale){super(generator,seed,0,0);this.x=x;this.y=y;this.scale=scale;}
        public Bounds getBounds(){return new Bounds((int)Math.floor(x-9*scale),(int)Math.floor(y-9*scale),(int)Math.ceil(18*scale),(int)Math.ceil(18*scale));}
        public void drawStack(GuiGraphics g,int mx,int my,float delta){g.pose().pushPose();g.pose().translate(x-8*scale,y-8*scale,0);g.pose().scale(scale,scale,1);getStack().render(g,0,0,delta);g.pose().popPose();}
    }
}
