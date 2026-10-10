package com.quzzar.vestige.apparatus.recipeviewer;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.storage.CraneBagItem;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in actual optional-viewer evidence for the public Crane Bag recipe, including keyed output lookup. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeCraneRecipeCapture {
    private static final boolean ENABLED="crane_recipe".equals(System.getProperty("vestige.capture.kind"));
    private static int state;private static long boot,next;private static Map<String,Object> checks;
    private NativeCraneRecipeCapture() { }
    private static boolean emi() { return ModList.get().isLoaded("emi"); }
    private static long matches(ItemStack stack,boolean uses) { return emi()?EmiInspection.matches(stack,uses):JeiInspection.matches(stack,uses); }
    private static ItemStack bound() {
        var g=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);var nodes=new ArrayList<RitualInputs.Node>();var ingredients=AttunementShardItem.ingredients();
        for(int i=0;i<8;i++)nodes.add(new RitualInputs.Node(i,g.offset(i),i<ingredients.size()?new ItemStack(BuiltInRegistries.ITEM.get(ingredients.get(i))):ItemStack.EMPTY,ItemStack.EMPTY));
        return CraneBagItem.fromShard(AttunementShardItem.create(new RitualInputs(g,nodes))).orElseThrow();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==99)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(boot==0)boot=now;
        try {
            if(now-boot>240_000_000_000L)throw new IllegalStateException("Crane recipe inspection timed out: "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
            if(state==1 && mc.getSingleplayerServer()!=null && mc.getOverlay()!=null)while(mc.pollTask()){ }
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                if(!emi() && !ModList.get().isLoaded("jei"))throw new IllegalStateException("Recipe viewer required");
                mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-crane-recipe-"+UUID.randomUUID(),new LevelSettings("Crane recipe inspection",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(51344,false,false),
                        access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.player!=null && mc.screen==null && (emi()?EmiInspection.count():JeiInspection.count())==RitualViewerClient.displays().size()) {
                var generic=RitualDisplays.craneBag().output();var actual=bound();
                long genericMatches=matches(generic,false),boundMatches=matches(actual,false),featherUses=matches(new ItemStack(Items.FEATHER),true);
                if(genericMatches!=1 || boundMatches!=1 || featherUses<1)throw new IllegalStateException("Crane recipe indexing failed");
                checks=Map.of("genericOutputMatches",genericMatches,"boundOutputMatches",boundMatches,"featherUses",featherUses,"capacity",4,"shapeless",false,"registeredRituals",RitualViewerClient.displays().size());
                mc.setScreen(new InventoryScreen(mc.player));if(emi())EmiInspection.show(actual);else JeiInspection.show(actual);state=2;next=now+3_000_000_000L;
            } else if(state==2 && now>=next) {
                var out=Path.of(System.getProperty("vestige.capture.output"),emi()?"emi":"jei");Files.createDirectories(out);
                try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())){frame.writeToFile(out.resolve("crane-bag-recipe.png"));}
                Files.writeString(out.resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited native optional-viewer framebuffer","screen",mc.screen.getClass().getName(),"checks",checks))+"\n");state=99;mc.stop();
            }
        }catch(Exception failure){state=99;LogUtils.getLogger().error("Crane recipe inspection failed",failure);try{var out=Path.of(System.getProperty("vestige.capture.output"),emi()?"emi":"jei");Files.createDirectories(out);Files.writeString(out.resolve("error.txt"),failure.toString());}catch(Exception ignored){}mc.stop();}
    }
}
