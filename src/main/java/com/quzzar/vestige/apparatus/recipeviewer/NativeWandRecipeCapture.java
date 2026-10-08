package com.quzzar.vestige.apparatus.recipeviewer;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.*;
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

/** Opt-in real client, public viewer lookups and exact shaped-wand recipe inspection. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeWandRecipeCapture {
    private static final boolean ENABLED="wand_recipes".equals(System.getProperty("vestige.capture.kind"));
    private static int state,index,lastState=-1;private static long deadline,nextLog;private static ItemStack shaped;
    private static final List<Map<String,Object>> captures=new ArrayList<>();
    private static final String[] NAMES={"untipped","resonating","steadfast-utility","exact-shaped-source","reduced-shaped-source"};
    private NativeWandRecipeCapture(){ }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception {
        if(!ENABLED || state==5)return;var mc=Minecraft.getInstance();long now=System.nanoTime();boolean viewer=ModList.get().isLoaded("emi") || ModList.get().isLoaded("jei");
        mc.options.pauseOnLostFocus=false;mc.options.framerateLimit().set(30);mc.getWindow().setFramerateLimit(30);
        if((state==1 || state==2) && mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(state!=lastState){lastState=state;com.mojang.logging.LogUtils.getLogger().info("Wand inspection phase {}, destination {}",state,System.getProperty("vestige.capture.output"));}
        if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null){
            mc.options.pauseOnLostFocus=false;mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);mc.getWindow().setWindowed(1280,960);mc.options.guiScale().set(4);mc.resizeDisplay();state=1;deadline=now+300_000_000_000L;
            mc.createWorldOpenFlows().createFreshLevel("vestige-wand-viewer-"+UUID.randomUUID(),new LevelSettings("Wand viewer inspection",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(48103,false,false),access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
        }else if(state==1 && mc.player!=null && mc.level!=null && WandViewerClient.displays().size()>200 && (!viewer || ready() && count()==WandViewerClient.displays().size())){
            shaped=ScrollItems.shapedScroll(VestigeMainMod.location("force_arrow"),new LeylineShaping.Modifiers(1.1,1.2,.9,.8),List.of(new Spellshaping.Selection(VestigeMainMod.location("reaching"),2)));
            var server=mc.getSingleplayerServer();var uuid=mc.player.getUUID();server.execute(() -> server.getPlayerList().getPlayer(uuid).getInventory().setItem(0,shaped.copy()));
            state=2;deadline=now+300_000_000_000L;
        }else if(state==2 && exact() && (!viewer || ready() && count()==WandViewerClient.displays().size())){
            if(!viewer){save(mc,"none","no-viewer",0);finish(mc,"none");return;}
            mc.setScreen(new InventoryScreen(mc.player));show();state=3;deadline=now+3_000_000_000L;
        }else if(state==3 && now>deadline){
            String mode=ModList.get().isLoaded("emi")?(ModList.get().isLoaded("jei")?"both":"emi"):"jei";
            var stack=output();long matches=matches(stack);if(matches!=1)throw new IllegalStateException("Wand lookup expected one recipe, found "+matches);
            save(mc,mode,NAMES[index],matches);
            if(++index==NAMES.length){finish(mc,mode);return;}
            if(index==4){mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(6);mc.resizeDisplay();}
            show();deadline=now+2_000_000_000L;
        }
        if((state==1 || state==2) && mc.player!=null && now>nextLog){nextLog=now+30_000_000_000L;com.mojang.logging.LogUtils.getLogger().info("Wand inspection waiting: phase={}, source={}, viewer={}, exact={}, ready={}",state,WandViewerClient.displays().size(),count(),exact(),ready());}
        if((state==1 || state==2) && now>deadline)throw new IllegalStateException("Wand recipe inspection timed out: state="+state+", sources="+WandViewerClient.displays().size()+", viewer="+count()+", ready="+ready()+"");
    }
    private static boolean exact(){return shaped!=null && WandViewerClient.displays().stream().anyMatch(e -> ScrollItems.scroll(e.source().scroll()).equals(ScrollItems.scroll(shaped)));}
    private static ItemStack output(){
        String spell=index==2?"pf2_shield":"force_arrow";var tip=index==0?Optional.<WandTips.Tip>empty():Optional.of(index==2?WandTips.Tip.NETHERITE:index>=3?WandTips.Tip.DIAMOND:WandTips.Tip.AMETHYST);
        return WandData.create(WandComponents.Base.BREEZE_ROD,MagicalThreadRecipe.Type.CALLOUS,tip,index>=3?shaped:ScrollItems.scroll(VestigeMainMod.location(spell)));
    }
    private static boolean ready(){return !ModList.get().isLoaded("emi") || com.quzzar.vestige.apparatus.recipeviewer.emi.EmiLiveRefresh.ready();}
    private static long count(){return ModList.get().isLoaded("emi")?EmiInspection.wands():ModList.get().isLoaded("jei")?JeiInspection.wands():0;}
    private static long matches(ItemStack stack){return ModList.get().isLoaded("emi")?EmiInspection.wandMatches(stack):JeiInspection.wandMatches(stack);}
    private static void show(){if(ModList.get().isLoaded("emi"))EmiInspection.show(output());else JeiInspection.show(output());}
    private static void save(Minecraft mc,String mode,String name,long matches)throws Exception {
        var out=Path.of(System.getProperty("vestige.capture.output"),mode);Files.createDirectories(out);
        com.mojang.logging.LogUtils.getLogger().info("Wand inspection saving {} to {}",name,out);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name+".png"));}
        captures.add(Map.of("case",name,"screen",mc.screen==null?"world":mc.screen.getClass().getName(),"recipes",WandViewerClient.displays().size(),"matches",matches,"guiScale",mc.options.guiScale().get()));
    }
    private static void finish(Minecraft mc,String mode)throws Exception {
        var out=Path.of(System.getProperty("vestige.capture.output"),mode);Files.createDirectories(out);
        Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("source","Minecraft framebuffer and native recipe APIs","viewer",mode,"captures",captures,"exactSourceReceived",exact()))+"\n");state=5;mc.stop();
    }
}
