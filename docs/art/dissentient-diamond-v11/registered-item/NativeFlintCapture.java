package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ApparatusBlocks;
import com.quzzar.vestige.apparatus.OfferingBlockEntity;
import com.quzzar.vestige.apparatus.ScrollItems;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Isolated verification of the registered ingredient and native rarity tooltip. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeFlintCapture {
    private static final boolean ENABLED="fluxed_flint".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>();
    private static int state;
    private static long started,next;
    private static CompletableFuture<Void> pending;
    private NativeFlintCapture() { }
    private static Path out(){return Path.of(System.getProperty("vestige.capture.output"));}
    private static void require(boolean value,String description){
        if(!value)throw new IllegalStateException(description);
        CHECKS.add(Map.of("check",description,"passed",true));
    }
    private static ItemStack candidate(){
        return new ItemStack(ScrollItems.DISSENTIENT_DIAMOND.get());
    }
    private static void prepare(Minecraft mc){
        var server=mc.getSingleplayerServer();var level=server.overworld();
        var player=server.getPlayerList().getPlayers().getFirst();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
        for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)level.setBlockAndUpdate(new BlockPos(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState());
        var pos=new BlockPos(0,1,0);level.setBlockAndUpdate(pos,ApparatusBlocks.PLINTH.get().defaultBlockState());
        ((OfferingBlockEntity)level.getBlockEntity(pos)).insert(candidate());
        var dropped=new ItemEntity(level,1.8,1.3,.5,candidate());
        dropped.setNoGravity(true);dropped.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        dropped.setPickUpDelay(32767);level.addFreshEntity(dropped);
        player.connection.teleport(3.4,2.8,-4.4,36.75f,19.0f);player.setNoGravity(true);
        for(int i=0;i<player.getInventory().getContainerSize();i++)player.getInventory().setItem(i,ItemStack.EMPTY);
        var comparisons=List.of(new ItemStack(Items.DIAMOND),candidate(),new ItemStack(Items.PRISMARINE_SHARD),new ItemStack(Items.ECHO_SHARD),new ItemStack(Items.NETHERITE_SCRAP));
        for(int i=0;i<comparisons.size();i++)player.getInventory().setItem(9+i,comparisons.get(i));
        var ingredients=List.of(new ItemStack(Items.WITHER_SKELETON_SKULL),new ItemStack(Items.GUNPOWDER),new ItemStack(Items.GUNPOWDER),new ItemStack(Items.DIAMOND));
        for(int i=0;i<ingredients.size();i++)player.getInventory().setItem(18+i,ingredients.get(i));
        player.getInventory().setItem(0,candidate());player.getInventory().setItem(1,new ItemStack(Items.DIAMOND));
        player.inventoryMenu.broadcastChanges();
    }
    private static void capture(Minecraft mc,String name)throws Exception{
        Files.createDirectories(out());
        try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())){frame.writeToFile(out().resolve(name+".png"));}
    }
    private static void finish(Minecraft mc)throws Exception{
        var stack=mc.player.getInventory().getItem(10);
        require(stack.is(ScrollItems.DISSENTIENT_DIAMOND.get()) && stack.getHoverName().getString().equals("Dissentient Diamond"),"Candidate name and native inventory synchronization");
        require(stack.getRarity()==new ItemStack(Items.WITHER_SKELETON_SKULL).getRarity() && stack.getRarity()==Rarity.UNCOMMON,"Registered ingredient matches Wither Skeleton Skull yellow Uncommon rarity");
        require(!stack.hasFoil(),"Ordinary ingredient has no enchantment shimmer");
        require(!stack.isDamageableItem() && stack.getMaxStackSize()==64,"Ordinary stackable ingredient preview");
        var hashes=new LinkedHashMap<String,String>();
        for(var path:List.of("models/item/dissentient_diamond.json","textures/item/dissentient_diamond.png")){
            byte[] bytes;
            try(var stream=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()){bytes=stream.readAllBytes();}
            hashes.put(path,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
            if(path.endsWith(".png"))try(var image=NativeImage.read(bytes)){
                require(image.getWidth()==16 && image.getHeight()==16,"Loaded texture uses the actual 16x16 pixel grid");
            }else require(new String(bytes,java.nio.charset.StandardCharsets.UTF_8).contains("minecraft:item/generated"),"Ordinary generated item model scale");
        }
        Files.writeString(out().resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited framebuffer",
                "fixture","Registered Dissentient Diamond ingredient; native rarity tooltips; crafting recipe remains pending",
                "checks",CHECKS,"assetSha256",hashes))+"\n");
    }
    @SubscribeEvent public static void tooltip(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event){
        if(!ENABLED || state!=4 && state!=5)return;
        var mc=Minecraft.getInstance();
        if(!(mc.screen instanceof InventoryScreen))return;
        var gui=event.getGuiGraphics();
        int left=(mc.getWindow().getGuiScaledWidth()-176)/2;
        int top=(mc.getWindow().getGuiScaledHeight()-166)/2;
        gui.renderTooltip(mc.font,candidate(),left+26,top+70);
        gui.renderTooltip(mc.font,new ItemStack(Items.WITHER_SKELETON_SKULL),left+8,top+129);
    }
    @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Post event){
        if(!ENABLED || state==99)return;
        var mc=Minecraft.getInstance();long now=System.nanoTime();if(started==0)started=now;
        try{
            if(now-started>240_000_000_000L)throw new IllegalStateException("Diamond art capture timed out at "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null){
                mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);
                mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);
                pending=CompletableFuture.completedFuture(null);state=10;
            }else if(state==10 && pending.isDone() && mc.getOverlay()==null){
                pending.join();state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-diamond-art-"+UUID.randomUUID(),
                        new LevelSettings("Dissentient Diamond art review",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(72585,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            }else if(state==1 && mc.player!=null && mc.getSingleplayerServer()!=null && mc.screen==null){
                pending=mc.getSingleplayerServer().submit(()->prepare(mc));state=2;next=now+2_000_000_000L;
            }else if(state==2 && pending.isDone() && now>=next){
                pending.join();mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(3);mc.resizeDisplay();state=3;next=now+1_000_000_000L;
            }else if(state==3 && now>=next){
                mc.setScreen(new InventoryScreen(mc.player));state=4;next=now+800_000_000L;
            }else if(state==4 && now>=next){
                capture(mc,"inventory-scale-3");mc.options.guiScale().set(2);mc.resizeDisplay();state=5;next=now+800_000_000L;
            }else if(state==5 && now>=next){
                capture(mc,"inventory-scale-2");mc.screen.onClose();mc.options.guiScale().set(3);mc.resizeDisplay();state=6;next=now+800_000_000L;
            }else if(state==6 && now>=next && mc.screen==null){
                capture(mc,"held-offered-and-dropped");finish(mc);state=99;mc.stop();
            }
        }catch(Exception error){
            state=99;LogUtils.getLogger().error("Dissentient Diamond art capture failed",error);
            try{Files.createDirectories(out());Files.writeString(out().resolve("error.txt"),error.toString());}catch(Exception ignored){}
            mc.stop();
        }
    }
}
