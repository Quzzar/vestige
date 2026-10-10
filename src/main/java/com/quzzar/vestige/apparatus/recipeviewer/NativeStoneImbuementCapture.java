package com.quzzar.vestige.apparatus.recipeviewer;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import com.quzzar.vestige.magic.world.NativeMana;
import com.quzzar.vestige.travel.*;
import com.quzzar.vestige.travel.client.StoneNetworkScreen;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in fresh-world inspection of real typed menus and both optional viewers. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeStoneImbuementCapture {
    private static final boolean ENABLED="stone_imbuements".equals(System.getProperty("vestige.capture.kind"));
    private static final String KEY="7a".repeat(32);
    private static final BlockPos SOURCE=new BlockPos(0,65,0);
    private static int state,route;private static long boot,next;private static UUID source;
    private static StoneNetworkScreen refreshedScreen;private static CompletableFuture<Void> pending;private static final List<Map<String,Object>> checks=new ArrayList<>();
    private NativeStoneImbuementCapture() { }
    private static boolean emi() { return ModList.get().isLoaded("emi"); }
    private static long matches(ItemStack stack) { return emi()?EmiInspection.matches(stack,false):JeiInspection.matches(stack,false); }
    private static StandingStoneEntity place(MinecraftServer server,BlockPos pos,String name) {
        var level=server.overworld();var state=StandingStones.STONE.get().defaultBlockState();level.setBlock(pos.below(),Blocks.STONE.defaultBlockState(),3);
        level.setBlock(pos,state,3);level.setBlock(pos.above(),state.setValue(StandingStoneBlock.HALF,DoubleBlockHalf.UPPER),3);
        var stone=(StandingStoneEntity)level.getBlockEntity(pos);stone.configure(KEY,name);return stone;
    }
    private static void prepare(MinecraftServer server) {
        var level=server.overworld();level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);level.setDayTime(5500);
        for(int x=-4;x<=12;x++)for(int z=-4;z<=6;z++)level.setBlock(new BlockPos(x,64,z),Blocks.GRASS_BLOCK.defaultBlockState(),3);
        source=place(server,SOURCE,"Ancient Henge").id();place(server,new BlockPos(6,65,0),"Forest Shrine");place(server,new BlockPos(8,129,4),"Mountain Observatory");place(server,new BlockPos(256,65,0),"Sunken Temple");
        var player=server.getPlayerList().getPlayers().getFirst();player.setGameMode(GameType.SURVIVAL);player.setNoGravity(true);player.teleportTo(.5,65,-2.5);player.setYRot(0);player.setXRot(12);
        player.getInventory().clearContent();for(var payment:StandingStonePayment.values())player.getInventory().setItem(payment.ordinal(),StandingStones.bound(KEY,ApparatusMaterials.ANDESITE,payment));
        player.getInventory().setItem(9,new ItemStack(Items.ENDER_PEARL));player.getInventory().setItem(10,new ItemStack(Items.AMETHYST_SHARD));player.getInventory().setItem(11,new ItemStack(Items.STONE_BRICKS));
    }
    private static void menu(MinecraftServer server,StandingStonePayment payment) {
        var player=server.getPlayerList().getPlayers().getFirst();player.setHealth(20);player.getFoodData().setFoodLevel(20);NativeMana.set(player,100);player.setExperienceLevels(0);player.setExperiencePoints(0);player.giveExperiencePoints(60);
        var stone=(StandingStoneEntity)server.overworld().getBlockEntity(SOURCE);stone.configure(KEY,"Ancient Henge",payment);StoneTravel.open(player,stone,0);
    }
    private static Path out() throws Exception {var path=Path.of(System.getProperty("vestige.capture.output"),emi()?"emi":"jei");Files.createDirectories(path);return path;}
    private static void capture(Minecraft mc,String name) throws Exception {
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out().resolve(name+".png"));}
        checks.add(Map.of("capture",name,"screen",mc.screen.getClass().getName(),"source","Unedited native Minecraft framebuffer"));
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==99)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(boot==0)boot=now;
        try {
            if(now-boot>300_000_000_000L)throw new IllegalStateException("Standing Stone inspection timed out: "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),0,0);
            var cursor=GLFW.glfwSetCursorPosCallback(mc.getWindow().getWindow(),null);if(cursor!=null){GLFW.glfwSetCursorPosCallback(mc.getWindow().getWindow(),cursor);cursor.invoke(mc.getWindow().getWindow(),0,0);}
            if(state==1 && mc.getSingleplayerServer()!=null && mc.getOverlay()!=null)while(mc.pollTask()){ }
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                if(!emi() && !ModList.get().isLoaded("jei"))throw new IllegalStateException("Recipe viewer required");
                mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.renderDistance().set(2);mc.options.simulationDistance().set(5);mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-stone-imbuements-"+UUID.randomUUID(),new LevelSettings("Stone imbuement inspection",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(51644,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.player!=null && mc.screen==null && (emi()?EmiInspection.count():JeiInspection.count())==RitualViewerClient.displays().size()) {
                pending=mc.getSingleplayerServer().submit(()->prepare(mc.getSingleplayerServer()));state=2;
            } else if(state==2 && pending.isDone()) {
                pending.join();for(var variant:StandingStoneRecipe.variants()) {
                    long expected=variant.material()==ApparatusMaterials.STONE_BRICKS?2:1;
                    var bound=StandingStones.bound(KEY,variant.material(),variant.payment());
                    if(matches(variant.preview())!=expected || matches(bound)!=expected)throw new IllegalStateException("Wrong public/bound recipe index "+variant.id());
                }
                checks.add(Map.of("publicAndBoundVariantsVerified",185,"finishes",36,"routes",5,"rituals",RitualViewerClient.displays().size()));
                route=0;pending=mc.getSingleplayerServer().submit(()->menu(mc.getSingleplayerServer(),StandingStonePayment.values()[route]));state=3;next=now+1_500_000_000L;
            } else if(state==3 && pending.isDone() && now>=next && mc.screen instanceof StoneNetworkScreen screen) {
                pending.join();var payment=StandingStonePayment.values()[route];
                if(!screen.view().source().equals(source) || screen.view().destinations().size()!=3 || screen.view().destinations().stream().anyMatch(d->d.quote().route()!=payment || !d.affordable()))throw new IllegalStateException("Real menu route mismatch "+payment);
                capture(mc,"menu-"+payment.id());refreshedScreen=screen;
                if(payment==StandingStonePayment.ERUDITE)checks.add(Map.of("eruditeQuotes",screen.view().destinations().stream().map(d->Map.of("xp",d.quote().amount(),"mana",d.quote().manaAmount())).toList()));
                pending=mc.getSingleplayerServer().submit(()->{var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();player.setHealth(1);player.getFoodData().setFoodLevel(0);NativeMana.set(player,0);player.setExperienceLevels(0);player.setExperiencePoints(0);});state=6;next=now+1_500_000_000L;
            } else if(state==6 && pending.isDone() && now>=next) {
                pending.join();if(mc.screen!=refreshedScreen || refreshedScreen.view().destinations().stream().anyMatch(d->d.affordable()))throw new IllegalStateException("Affordability did not refresh the existing menu");
                capture(mc,"menu-"+StandingStonePayment.values()[route].id()+"-unaffordable");
                if(StandingStonePayment.values()[route]==StandingStonePayment.ERUDITE) {
                    pending=mc.getSingleplayerServer().submit(()->mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst().giveExperiencePoints(60));state=7;next=now+1_500_000_000L;
                } else if(++route<5) {pending=mc.getSingleplayerServer().submit(()->menu(mc.getSingleplayerServer(),StandingStonePayment.values()[route]));state=3;next=now+1_500_000_000L;}
                else {mc.setScreen(new InventoryScreen(mc.player));pending=mc.getSingleplayerServer().submit(()->mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst().setHealth(20));state=4;next=now+1_500_000_000L;}
            } else if((state==7 || state==8) && pending.isDone() && now>=next) {
                pending.join();if(mc.screen!=refreshedScreen || refreshedScreen.view().destinations().stream().anyMatch(d->d.affordable()))throw new IllegalStateException("Erudite ignored missing currency");
                capture(mc,state==7?"menu-erudite-no-mana":"menu-erudite-no-xp");
                if(state==7) {
                    pending=mc.getSingleplayerServer().submit(()->{var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();player.setExperienceLevels(0);player.setExperiencePoints(0);NativeMana.set(player,100);});state=8;next=now+1_500_000_000L;
                } else {++route;pending=mc.getSingleplayerServer().submit(()->menu(mc.getSingleplayerServer(),StandingStonePayment.values()[route]));state=3;next=now+1_500_000_000L;}
            } else if(state==4 && now>=next) {
                pending.join();if(!(mc.screen instanceof InventoryScreen))throw new IllegalStateException("Balance refresh reopened a closed travel menu");checks.add(Map.of("closedMenuStaysClosedOnBalanceRefresh",true));
                capture(mc,"inventory");route=0;mc.setScreen(new InventoryScreen(mc.player));var stack=StandingStones.bound(KEY,ApparatusMaterials.ANDESITE,StandingStonePayment.values()[route]);if(emi())EmiInspection.show(stack);else JeiInspection.show(stack);state=5;next=now+1_500_000_000L;
            } else if(state==5 && now>=next) {
                capture(mc,"recipe-"+StandingStonePayment.values()[route].id());
                if(++route<5){mc.setScreen(new InventoryScreen(mc.player));var stack=StandingStones.bound(KEY,ApparatusMaterials.ANDESITE,StandingStonePayment.values()[route]);if(emi())EmiInspection.show(stack);else JeiInspection.show(stack);next=now+1_500_000_000L;}
                else {Files.writeString(out().resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","checks",checks))+"\n");state=99;mc.stop();}
            }
        }catch(Exception failure){state=99;LogUtils.getLogger().error("Standing Stone inspection failed",failure);try{Files.writeString(out().resolve("error.txt"),failure.toString());}catch(Exception ignored){}mc.stop();}
    }
}
