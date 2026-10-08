package com.quzzar.vestige.magic.world.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in framebuffer evidence of held-item preparation, both hands, release and cancellation. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativePreparationCapture {
    private static final boolean ENABLED="preparation".equals(System.getProperty("vestige.capture.kind"));
    private record View(String name,int scale,int width,int height,int kind,InteractionHand hand,HumanoidArm arm,boolean cancel,int backdrop) { }
    private static final List<View> VIEWS=List.of(
            new View("scroll-wide",4,960,540,0,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,0),
            new View("scroll-offhand",4,960,540,0,InteractionHand.OFF_HAND,HumanoidArm.RIGHT,false,0),
            new View("wand-wide",4,960,540,1,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,0),
            new View("staff-left",4,960,540,2,InteractionHand.MAIN_HAND,HumanoidArm.LEFT,false,0),
            new View("cancel-wide",4,960,540,0,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,true,0),
            new View("scroll-compact",2,640,360,0,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,0),
            new View("staff-compact",2,640,360,2,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,0),
            new View("scroll-snow",4,960,540,0,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,1),
            new View("scroll-night",4,960,540,0,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,2),
            new View("scroll-fov-off",4,960,540,0,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,0),
            new View("scroll-third-person",4,960,540,0,InteractionHand.MAIN_HAND,HumanoidArm.RIGHT,false,0));
    private static final List<Map<String,Object>> FRAMES=new ArrayList<>(), CHECKS=new ArrayList<>();
    private static int state,index,frame;
    private static long next,started;
    private static boolean canceled;
    private static double renderedFov=70;
    private static CompletableFuture<Void> pending,cancel;
    private NativePreparationCapture() { }
    private static void prepare(MinecraftServer server) {
        var level=server.overworld();var player=server.getPlayerList().getPlayers().getFirst();
        if(index==0) {
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
            level.setWeatherParameters(6000,0,false,false);level.setDayTime(5500);
            for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++) {
                level.setBlock(new BlockPos(x,64,z),Blocks.GRASS_BLOCK.defaultBlockState(),3);
                for(int y=65;y<70;y++)level.setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
            }
            var origin=new BlockPos(0,65,0);level.setBlock(origin,ApparatusBlocks.SPELLSTONE.get().defaultBlockState(),3);
            for(var pos:List.of(origin.north(2),origin.east(2),origin.south(2),origin.west(2)))
                level.setBlock(pos,ApparatusBlocks.PLINTH.get().defaultBlockState(),3);
            player.setNoGravity(true);player.teleportTo(.5,65,-5.5);player.setYRot(0);player.setXRot(8);
        }
        NativeMagic.session(server).runtime().interruptActor(player.getUUID());
        var spell=VestigeMainMod.location("greater_heal");SpellKnowledge.identify(player,spell);
        player.setHealth(4);NativeMana.set(player,100);
        player.getInventory().selected=0;player.getInventory().clearContent();
        var view=VIEWS.get(index);player.setMainArm(view.arm());
        level.setDayTime(view.backdrop()==2?18000:5500);player.setXRot(view.backdrop()==0?8:45);
        for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++) level.setBlock(new BlockPos(x,64,z),
                (view.backdrop()==1?Blocks.SNOW_BLOCK:view.backdrop()==2?Blocks.BLACK_CONCRETE:Blocks.GRASS_BLOCK).defaultBlockState(),3);
        var scroll=ScrollItems.scroll(spell);
        var source=switch(view.kind()) {
            case 0 -> scroll.copyWithCount(4);
            case 1 -> WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.ENSORCELLED,scroll);
            default -> StaffData.bind(StaffData.create(VestigeMainMod.location("life")),scroll,NativeMagic.spells().spells().get(spell));
        };
        player.setItemInHand(view.hand(),source);
    }
    private static void cast(MinecraftServer server) {
        var player=server.getPlayerList().getPlayers().getFirst();
        var view=VIEWS.get(index);
        boolean accepted=switch(view.kind()) {
            case 0 -> ScrollCasting.cast(player,player.getItemInHand(view.hand()));
            case 1 -> WandCasting.cast(player,view.hand());
            default -> StaffCasting.cast(player,view.hand());
        };
        if(!accepted)throw new IllegalStateException("Native source charge rejected");
        var actual=NativeMagic.session(server).runtime().preparation(player.getUUID()).orElseThrow();
        if(actual.totalTicks()!=(view.kind()==1?50:40))
            throw new IllegalStateException("Incorrect authored preparation");
    }
    private static void verify(MinecraftServer server) {
        var player=server.getPlayerList().getPlayers().getFirst();var view=VIEWS.get(index);
        int remaining=view.cancel?player.getInventory().getItem(1).getCount():player.getItemInHand(view.hand()).getCount();
        int expectedRemaining=view.kind()==0?(view.cancel?4:3):1;
        double expectedMana=view.cancel?100:view.kind()==1?64:58;
        double mana=NativeMana.amount(player);
        if(NativeMagic.session(server).runtime().preparation(player.getUUID()).isPresent()
                || mana!=expectedMana || remaining!=expectedRemaining)
            throw new IllegalStateException("Invalid release/cancellation: "+view+" / "+mana+" / "+remaining);
        CHECKS.add(Map.of("view",view.name,"mana",mana,"remainingScrolls",remaining,"preparationCleared",true));
    }
    private static void capture(Minecraft mc,View view,int number,double seconds) throws java.io.IOException {
        Path out=Path.of(System.getProperty("vestige.capture.output"));Files.createDirectories(out.resolve(view.name));
        String file=view.name+"/"+(number<0?"idle":String.format(Locale.ROOT,"frame-%03d",number))+".png";
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(file));}
        var progress=PreparationHud.preparation();var held=PreparationHud.heldSource();
        var data=new LinkedHashMap<String,Object>();
        data.put("file",file);data.put("seconds",seconds);data.put("guiWidth",mc.getWindow().getGuiScaledWidth());
        data.put("guiHeight",mc.getWindow().getGuiScaledHeight());data.put("visible",progress.isPresent());
        data.put("elapsedTicks",progress.map(p->p.elapsedTicks()).orElse(0L));data.put("totalTicks",progress.map(p->p.totalTicks()).orElse(0L));
        data.put("hand",view.hand());data.put("mainArm",view.arm());
        data.put("heldSourceMatches",held.map(h->h.hand()==view.hand() && h.matches(mc.player.getItemInHand(view.hand()))).orElse(false));
        data.put("crosshairStyle","native_unchanged_subtle_zoom");
        data.put("worldFov",renderedFov);data.put("zoomTarget",PreparationZoom.modifier(mc.player));
        data.put("fovEffectScale",mc.options.fovEffectScale().get());data.put("backdrop",view.backdrop());data.put("clientPitch",mc.player.getXRot());
        data.put("animationRegistered",net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(mc.player.getItemInHand(view.hand())) instanceof PreparationItemAnimation);
        FRAMES.add(data);
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void inspectFov(ViewportEvent.ComputeFov event) {
        if(ENABLED && event.usedConfiguredFov())renderedFov=event.getFOV();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==9)return;
        var mc=Minecraft.getInstance();long now=System.nanoTime();
        try {
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                mc.options.pauseOnLostFocus=false;mc.options.hideGui=false;mc.options.guiScale().set(4);
                mc.options.renderDistance().set(4);mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);
                mc.options.setCameraType(CameraType.FIRST_PERSON);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-preparation-"+UUID.randomUUID(),
                        new LevelSettings("Spell preparation inspection",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902,false,false),
                        access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.level!=null && mc.player!=null && mc.getSingleplayerServer()!=null && mc.screen==null) {
                state=2;pending=mc.getSingleplayerServer().submit(()->prepare(mc.getSingleplayerServer()));
            } else if(state==2 && pending.isDone()) {
                pending.join();var view=VIEWS.get(index);mc.getWindow().setWindowed(view.width,view.height);
                mc.options.fov().set(70);mc.options.fovEffectScale().set(view.name().equals("scroll-fov-off")?0d:1d);
                mc.options.setCameraType(view.name().equals("scroll-third-person")?CameraType.THIRD_PERSON_BACK:CameraType.FIRST_PERSON);
                mc.options.guiScale().set(view.scale);mc.options.mainHand().set(view.arm());mc.options.broadcastOptions();
                mc.player.setXRot(view.backdrop()==0?8:45);mc.player.xRotO=mc.player.getXRot();
                mc.resizeDisplay();mc.mouseHandler.releaseMouse();
                state=3;next=now+2_000_000_000L;
            } else if(state==3 && now>=next && mc.screen==null) {
                capture(mc,VIEWS.get(index),-1,0);
                state=4;pending=mc.getSingleplayerServer().submit(()->cast(mc.getSingleplayerServer()));
            } else if(state==4 && pending.isDone()) {
                pending.join();state=5;started=now;next=now;frame=0;canceled=false;cancel=null;
            } else if(state==5 && now>=next && mc.screen==null) {
                var view=VIEWS.get(index);var progress=PreparationHud.preparation();
                if(view.cancel && !canceled && progress.isPresent() && progress.get().elapsedTicks()>=20) {
                    canceled=true;cancel=mc.getSingleplayerServer().submit(()->{
                        var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
                        player.getInventory().setItem(1,player.getItemInHand(view.hand()));player.setItemInHand(view.hand(),ItemStack.EMPTY);
                    });
                }
                capture(mc,view,frame++,(now-started)/1e9);
                next=now+100_000_000L;
                if(now-started>3_100_000_000L && (cancel==null || cancel.isDone())) {
                    if(cancel!=null)cancel.join();state=6;pending=mc.getSingleplayerServer().submit(()->verify(mc.getSingleplayerServer()));
                }
            } else if(state==6 && pending.isDone()) {
                pending.join();
                if(++index==VIEWS.size()) {
                    Path out=Path.of(System.getProperty("vestige.capture.output"));
                    Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                            "engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Minecraft main render target",
                            "frames",FRAMES,"checks",CHECKS))+"\n");state=9;mc.stop();
                } else {state=2;pending=mc.getSingleplayerServer().submit(()->prepare(mc.getSingleplayerServer()));}
            }
        } catch(Exception failure){state=9;LogUtils.getLogger().error("Preparation inspection failed",failure);mc.stop();}
    }
}
