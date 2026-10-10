package com.quzzar.vestige.equipment.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.*;
import com.quzzar.vestige.magic.world.*;
import com.quzzar.vestige.magic.world.client.ItemManaOverlay;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
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

/** Opt-in production item, naming, wear and private recovery evidence in an isolated native client. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class WardweaveCapture {
    private static final boolean ENABLED="wardweave".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>();
    private static int state,hover=-1;private static long boot,next,shown;private static CompletableFuture<Void> pending;
    private WardweaveCapture() { }
    private static Path out() { return Path.of(System.getProperty("vestige.capture.output")); }
    private static void capture(Minecraft mc,String name) throws Exception {
        Files.createDirectories(out());try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())){frame.writeToFile(out().resolve(name+".png"));}
    }
    private static void prepare(Minecraft mc) {
        var server=mc.getSingleplayerServer();var level=server.overworld();var p=server.getPlayerList().getPlayers().getFirst();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)level.setBlockAndUpdate(new BlockPos(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState());
        p.connection.teleport(.5,1,.5,160,12);p.setNoGravity(true);p.getInventory().clearContent();
        for(int mask=0;mask<16;mask++)p.getInventory().setItem(9+mask,WardweaveDisplays.create(DyeColor.WHITE,mask));
        var neighbors=List.of(Items.LEATHER_CHESTPLATE,Items.CHAINMAIL_CHESTPLATE,Items.IRON_CHESTPLATE,Items.DIAMOND_CHESTPLATE,Items.WHITE_WOOL,Items.PUFFERFISH,Items.IRON_INGOT,Items.STRING);
        for(int i=0;i<neighbors.size();i++)p.getInventory().setItem(27+i,new ItemStack(neighbors.get(i)));
        var robe=WardweaveDisplays.create(DyeColor.WHITE,15);p.setItemSlot(EquipmentSlot.CHEST,robe);p.getInventory().setItem(0,robe.copy());NativeMana.set(p,0);
        p.inventoryMenu.broadcastChanges();NativeMana.sync(p,true);
    }
    private static void arm(Minecraft mc) {
        var server=mc.getSingleplayerServer();var p=server.getPlayerList().getPlayers().getFirst();var robe=p.getItemBySlot(EquipmentSlot.CHEST);
        NativeMana.set(p,0);
        p.hurt(p.damageSources().generic(),2);p.invulnerableTime=0;p.hurt(p.damageSources().generic(),1);
        var recovery=NativeMagic.session(server).runtime().recovery(p.getUUID(),WardweaveImbuements.ABILITY).orElseThrow();
        if(robe.getDamageValue()!=1 || recovery.totalTicks()!=230 || NativeMana.amount(p)!=0)throw new IllegalStateException("Native Wardweave activation/wear/recovery failed");
        CHECKS.add(Map.of("activation","integrated-server health damage and ward absorption","wear",1,"recoveryTicks",230,"manaPayment",0));
        p.inventoryMenu.broadcastChanges();NativeMana.sync(p,true);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==99)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(boot==0)boot=now;
        try {
            if(now-boot>240_000_000_000L)throw new IllegalStateException("Wardweave capture timed out: "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
            if(state==1 && mc.getSingleplayerServer()!=null && mc.getOverlay()!=null)while(mc.pollTask()){ }
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                mc.options.pauseOnLostFocus=false;mc.options.guiScale().set(2);mc.options.renderDistance().set(3);mc.options.simulationDistance().set(5);
                mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-wardweave-"+UUID.randomUUID(),new LevelSettings("Wardweave inspection",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(81344,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.player!=null && mc.screen==null) {
                pending=mc.getSingleplayerServer().submit(()->prepare(mc));state=2;next=now+4_000_000_000L;
            } else if(state==2 && pending.isDone() && now>=next) {
                pending.join();pending=mc.getSingleplayerServer().submit(()->arm(mc));state=8;next=now+700_000_000L;
            } else if(state==8 && pending.isDone() && now>=next) {
                pending.join();mc.setScreen(new InventoryScreen(mc.player){
                    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
                        int left=(width-176)/2,top=(height-166)/2;
                        super.render(g,hover<0?20:left+16+(hover%9)*18,hover<0?20:top+92+(hover/9)*18,partial);
                    }
                });mc.mouseHandler.releaseMouse();state=3;next=now+700_000_000L;shown=now;
            } else if(state==3 && now>=next) {
                capture(mc,"inventory-beside-vanilla");float recovery=ItemManaOverlay.recovery(mc.player.getItemBySlot(EquipmentSlot.CHEST));
                if(recovery<=0 || ItemManaOverlay.shortage(mc.player.getItemBySlot(EquipmentSlot.CHEST))!=0)throw new IllegalStateException("Private recovery packet or free ability readiness failed");
                CHECKS.add(Map.of("privatePacketRecovery",recovery,"zeroManaShortage",0));
                for(int mask=0;mask<16;mask++) {
                    var stack=mc.player.getInventory().getItem(9+mask);var segments=new ArrayList<Map<String,Object>>();
                    stack.getHoverName().visit((style,text)->{segments.add(Map.of("text",text,"italic",style.isItalic()));return Optional.empty();},Style.EMPTY);
                    CHECKS.add(Map.of("mask",mask,"name",stack.getHoverName().getString(),"segments",segments,"durability",stack.getMaxDamage()));
                }
                hover=15;state=4;next=now+500_000_000L;
            } else if(state==4 && now>=next) {
                capture(mc,"hover-unshaken");hover=0;state=5;next=now+500_000_000L;
            } else if(state==5 && now>=next) {
                capture(mc,"hover-plain");hover=-1;state=6;
            } else if(state==6 && now-shown>14_000_000_000L) {
                capture(mc,"recovery-finished");float remaining=ItemManaOverlay.recovery(mc.player.getItemBySlot(EquipmentSlot.CHEST));
                if(remaining!=0)throw new IllegalStateException("Native recovery decoration did not expire");CHECKS.add(Map.of("recoveryFinished",remaining));
                var hashes=new LinkedHashMap<String,String>();
                for(String asset:List.of("textures/item/wardweave_robes.png","textures/models/armor/wardweave_layer_1.png","models/item/wardweave_robes.json"))try(var input=mc.getResourceManager().open(VestigeMainMod.location(asset))) {
                    hashes.put(asset,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes())));
                }
                Files.writeString(out().resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited framebuffer / production registered items and private packets","checks",CHECKS,"loadedAssetSha256",hashes))+"\n");state=99;mc.stop();
            }
        }catch(Exception failure){state=99;LogUtils.getLogger().error("Wardweave native inspection failed",failure);try{Files.createDirectories(out());Files.writeString(out().resolve("error.txt"),failure.toString());}catch(Exception ignored){}mc.stop();}
    }
}
