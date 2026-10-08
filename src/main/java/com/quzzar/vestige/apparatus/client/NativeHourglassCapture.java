package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in native framebuffer review; ordinary players never enter this development scene. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeHourglassCapture {
    private static final boolean ENABLED="kairotic_hourglass".equals(System.getProperty("vestige.capture.kind"));
    private static int state;private static long started,next;private static CompletableFuture<Void> pending;
    private static final List<String> checks=new ArrayList<>();
    private NativeHourglassCapture() { }
    private static Path out() {return Path.of(System.getProperty("vestige.capture.output"));}
    private static void require(boolean value,String check) {if(!value)throw new IllegalStateException(check);checks.add(check);}
    private static void capture(Minecraft mc,String name) throws Exception {
        Files.createDirectories(out());try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {frame.writeToFile(out().resolve(name+".png"));}
    }
    private static void prepare(Minecraft mc) {
        var server=mc.getSingleplayerServer();var level=server.overworld();var player=server.getPlayerList().getPlayers().getFirst();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);player.setGameMode(GameType.SURVIVAL);
        for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)level.setBlockAndUpdate(new BlockPos(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState());
        player.connection.teleport(0.5,1,0.5,160,12);player.setNoGravity(true);NativeMana.set(player,100);
        for(int i=0;i<player.getInventory().getContainerSize();i++)player.getInventory().setItem(i,ItemStack.EMPTY);
        var row=List.of(new ItemStack(Items.CLOCK),new ItemStack(ScrollItems.KAIROTIC_HOURGLASS.get()),new ItemStack(Items.ECHO_SHARD),new ItemStack(Items.ENDER_PEARL),new ItemStack(Items.GLASS),new ItemStack(Items.COMPASS));
        for(int i=0;i<row.size();i++)player.getInventory().setItem(9+i,row.get(i));
        for(int i=0;i<7;i++)player.getInventory().setItem(18+i,HourglassData.create(new HourglassData.Variant(Set.of(HourglassData.Choice.values()[i]),HourglassData.NEUTRAL)));
        player.getInventory().setItem(0,new ItemStack(ScrollItems.KAIROTIC_HOURGLASS.get()));player.inventoryMenu.broadcastChanges();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==99)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(started==0)started=now;
        try {
            if(now-started>240_000_000_000L)throw new IllegalStateException("Hourglass capture timed out at "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
            // Nested world loading renders frames with scheduled client work disabled. Complete its pending resource reload.
            if(state==1 && mc.getSingleplayerServer()!=null && mc.getOverlay()!=null)while(mc.pollTask()) { }
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-hourglass-review-"+UUID.randomUUID(),new LevelSettings("Kairotic Hourglass review",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(72584,false,false),access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.player!=null && mc.getSingleplayerServer()!=null && mc.screen==null) {
                pending=mc.getSingleplayerServer().submit(()->prepare(mc));state=2;next=now+2_000_000_000L;
            } else if(state==2 && pending.isDone() && now>=next) {
                pending.join();mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(3);mc.resizeDisplay();state=3;next=now+1_000_000_000L;
            } else if(state==3 && now>=next) {
                mc.setScreen(new InventoryScreen(mc.player));state=4;next=now+800_000_000L;
            } else if(state==4 && now>=next) {
                require(mc.player.getInventory().getItem(10).getHoverName().getString().equals("Kairotic Hourglass"),"Translated item name");capture(mc,"inventory-beside-vanilla");mc.screen.onClose();state=5;next=now+800_000_000L;
            } else if(state==5 && now>=next) {
                capture(mc,"held-hourglass");pending=mc.getSingleplayerServer().submit(() -> {
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();player.connection.teleport(4.5,1,0.5,160,12);
                    require(HourglassMagic.returnToPast(player,player.getMainHandItem()),"Native partial-history return succeeded");
                    require(player.position().distanceToSqr(new net.minecraft.world.phys.Vec3(.5,1,.5))<1e-8,"Return selected exact initial position");
                    require(NativeMana.amount(player)==40 && player.getMainHandItem().getDamageValue()==1,"Return paid sixty mana and one wear");player.inventoryMenu.broadcastChanges();
                });state=6;next=now+300_000_000L;
            } else if(state==6 && pending.isDone() && now>=next) {
                pending.join();capture(mc,"after-return");mc.setScreen(new InventoryScreen(mc.player));state=7;next=now+600_000_000L;
            } else if(state==7 && now>=next) {
                capture(mc,"durability-after-return");var hashes=new LinkedHashMap<String,String>();
                for(var path:List.of("models/item/kairotic_hourglass.json","textures/item/kairotic_hourglass.png"))try(var stream=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()){hashes.put(path,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}
                Files.writeString(out().resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited framebuffer","checks",checks,"assetSha256",hashes))+"\n");state=99;mc.stop();
            }
        } catch(Exception error) {
            state=99;LogUtils.getLogger().error("Hourglass capture failed",error);try{Files.createDirectories(out());Files.writeString(out().resolve("error.txt"),error.toString());}catch(Exception ignored){}mc.stop();
        }
    }
}
