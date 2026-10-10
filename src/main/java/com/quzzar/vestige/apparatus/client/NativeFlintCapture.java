package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
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

/** Opt-in framebuffer evidence for the exact packaged sprite and a real atomic repair. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeFlintCapture {
    private static final boolean FRACTIOUS="fractious_flint".equals(System.getProperty("vestige.capture.kind"));
    private static final boolean IMBUEMENTS=FRACTIOUS || "fluxed_flint_imbuements".equals(System.getProperty("vestige.capture.kind"));
    private static final boolean ENABLED=IMBUEMENTS || "fluxed_flint".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>();
    private static int state;
    private static long started,next;
    private static CompletableFuture<Void> pending;
    private static CompletableFuture<Boolean> repairReady;
    private static int repairTick;
    private static OfferingBlockEntity center,catalyst,target;
    private static ItemStack original;
    private NativeFlintCapture() { }
    private static void require(boolean value,String detail) { if (!value) throw new IllegalStateException(detail);CHECKS.add(Map.of("check",detail,"passed",true)); }
    private static Path out() { return Path.of(System.getProperty("vestige.capture.output")); }
    private static void capture(Minecraft mc,String name) throws Exception {
        Files.createDirectories(out());try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {frame.writeToFile(out().resolve(name+".png"));}
    }
    private static void prepare(Minecraft mc) {
        var server=mc.getSingleplayerServer();var level=server.overworld();var player=server.getPlayerList().getPlayers().getFirst();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);player.setGameMode(GameType.SURVIVAL);
        for(int x=-9;x<=9;x++)for(int z=-9;z<=9;z++)level.setBlockAndUpdate(new BlockPos(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState());
        var pos=new BlockPos(0,1,0);level.setBlockAndUpdate(pos,ApparatusBlocks.SPELLSTONE.get().defaultBlockState());
        for(var offset:List.of(new BlockPos(2,0,0),new BlockPos(-2,0,0),new BlockPos(0,0,2),new BlockPos(0,0,-2),
                new BlockPos(3,0,-3),new BlockPos(3,0,3),new BlockPos(-3,0,3),new BlockPos(-3,0,-3)))level.setBlockAndUpdate(pos.offset(offset),ApparatusBlocks.PLINTH.get().defaultBlockState());
        center=(OfferingBlockEntity)level.getBlockEntity(pos);catalyst=(OfferingBlockEntity)level.getBlockEntity(pos.offset(3,0,-3));target=(OfferingBlockEntity)level.getBlockEntity(pos.offset(-3,0,3));
        catalyst.insert(IMBUEMENTS ? FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(FRACTIOUS ? 4 : 1)) : new ItemStack(ScrollItems.FLUXED_FLINT.get()));original=StaffData.create(VestigeMainMod.location("fire"));original.setDamageValue(23);target.insert(original);
        player.connection.teleport(4,4,-6,33.69f,22);player.setNoGravity(true);
        for(int i=0;i<player.getInventory().getContainerSize();i++)player.getInventory().setItem(i,ItemStack.EMPTY);
        var comparisons=List.of(new ItemStack(Items.FLINT),new ItemStack(ScrollItems.FLUXED_FLINT.get()),new ItemStack(Items.DIAMOND),new ItemStack(ScrollItems.DISSENTIENT_DIAMOND.get()),new ItemStack(Items.NETHERITE_INGOT));
        for(int i=0;i<comparisons.size();i++)player.getInventory().setItem(9+i,comparisons.get(i));
        if(IMBUEMENTS) for(int i=1;i<FluxedFlintImbuements.variants().size();i++) player.getInventory().setItem(13+i,FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(i)));
        player.getInventory().setItem(0,catalyst.displayedItem());player.getInventory().setItem(1,original.copy());player.inventoryMenu.broadcastChanges();
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state==99)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(started==0)started=now;
        try {
            if(now-started>240_000_000_000L)throw new IllegalStateException("Flint capture timed out at "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-flint-review-"+UUID.randomUUID(),new LevelSettings("Fluxed Flint review",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),new WorldOptions(72584,false,false),access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.player!=null && mc.getSingleplayerServer()!=null && mc.screen==null) {
                pending=mc.getSingleplayerServer().submit(()->prepare(mc));state=2;next=now+2_000_000_000L;
            } else if(state==2 && pending.isDone() && now>=next) {
                pending.join();require(mc.player.getInventory().getItem(10).getHoverName().getString().equals("Fluxed Flint"),"Translated native item name");
                if(IMBUEMENTS) {
                    var names=List.of("Stabilized Fluxed Flint","Reinforced Fluxed Flint","Braced Fluxed Flint","Fractious Fluxed Flint","Restive Fluxed Flint","Audacious Fluxed Flint","Impetuous Fluxed Flint");
                    for(int i=0;i<names.size();i++) {
                        var stack=mc.player.getInventory().getItem(14+i);
                        require(stack.getHoverName().getString().equals(names.get(i)) && stack.hasFoil() && stack.getRarity()==Rarity.RARE,"Native variant name, rarity and glint: "+names.get(i));
                    }
                }
                mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(3);mc.resizeDisplay();
                state=21;next=now+1_000_000_000L;
            } else if(state==21 && now>=next) {
                mc.setScreen(new InventoryScreen(mc.player));state=3;next=now+800_000_000L;
            } else if(state==3 && now>=next) {
                if(IMBUEMENTS) {
                    var graphics=new net.minecraft.client.gui.GuiGraphics(mc,mc.renderBuffers().bufferSource());
                    mc.screen.renderWithTooltip(graphics,(mc.getWindow().getGuiScaledWidth()-176)/2+107,(mc.getWindow().getGuiScaledHeight()-166)/2+92,0);
                    graphics.flush();
                }
                capture(mc,"inventory-beside-vanilla");mc.screen.onClose();state=4;next=now+600_000_000L;
            } else if(state==4 && now>=next && mc.screen==null) {
                capture(mc,"held-and-offered");pending=mc.getSingleplayerServer().submit(()->{
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();player.getRandom().setSeed(0);
                    require(RitualCrafting.layout(center).geometry().slots()==8 && RitualCrafting.layout(center).items().stream().filter(item -> !item.isEmpty()).count()==2,
                            "Eight-slot fixture has only two outer offerings");
                    require(RitualCrafting.activate(player,center)==RitualCrafting.Outcome.CRAFTING,"Native outer-only activation enters repair lifecycle");
                    repairTick=mc.getSingleplayerServer().getTickCount();
                });state=5;next=now+1_000_000_000L;
            } else if(state==5 && pending.isDone() && now>=next) {
                pending.join();capture(mc,"repair-in-progress");state=6;next=now+1_500_000_000L;
            } else if(state==6 && now>=next) {
                // Native scenes can run below 20 TPS during parallel builds. Wait for the
                // server lifecycle, rather than mistaking a wall-clock delay for commitment.
                if(repairReady==null) {repairReady=mc.getSingleplayerServer().submit(()->mc.getSingleplayerServer().getTickCount()>=repairTick+65);return;}
                if(!repairReady.isDone())return;
                boolean finished=repairReady.join();repairReady=null;
                if(!finished){next=now+250_000_000L;return;}
                capture(mc,"repaired-output");pending=mc.getSingleplayerServer().submit(()->{
                    int restored=FRACTIOUS ? 20 : 10;var expected=original.copy();expected.setDamageValue(23-restored);
                    require(ItemStack.matches(expected,RitualTestOutput.stack(center)),"Actual dropped staff differs only by "+restored+" restored durability");
                    require(catalyst.displayedItem().getDamageValue()==restored && target.displayedItem().isEmpty(),"Catalyst spends "+restored+" points and target is consumed once");
                    if(IMBUEMENTS) require(catalyst.displayedItem().getMaxDamage()==(FRACTIOUS ? 128 : 77) && FluxedFlintImbuements.read(catalyst.displayedItem()).orElseThrow().choices().equals(Set.of(FRACTIOUS ? FluxedFlintImbuements.Choice.FRACTIOUS : FluxedFlintImbuements.Choice.STABILIZED)),"Actual repaired catalyst retains its crafted identity and budget");
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();player.getInventory().setItem(0,catalyst.displayedItem());player.getInventory().setItem(1,RitualTestOutput.stack(center));player.inventoryMenu.broadcastChanges();
                });state=7;next=now+600_000_000L;
            } else if(state==7 && pending.isDone() && now>=next) {
                pending.join();mc.setScreen(new InventoryScreen(mc.player));state=8;next=now+600_000_000L;
            } else if(state==8 && now>=next) {
                capture(mc,"durability-after-repair");var hashes=new LinkedHashMap<String,String>();
                for(var path:List.of("models/item/fluxed_flint.json","textures/item/fluxed_flint.png"))try(var stream=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()){hashes.put(path,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));}
                Files.writeString(out().resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited framebuffer","checks",CHECKS,"assetSha256",hashes))+"\n");state=99;mc.stop();
            }
        } catch(Exception error) {
            state=99;LogUtils.getLogger().error("Fluxed Flint capture failed",error);try{Files.createDirectories(out());Files.writeString(out().resolve("error.txt"),error.toString());}catch(Exception ignored){}mc.stop();
        }
    }
}
