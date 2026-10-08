package com.quzzar.vestige.equipment.client;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.*;
import com.quzzar.vestige.magic.runtime.SpellRuntime;
import com.quzzar.vestige.magic.world.*;
import com.quzzar.vestige.magic.world.client.ItemManaOverlay;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.*;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.security.MessageDigest;

/** Opt-in actual framebuffer evidence: registered boots, native armor renderer and private readiness packets. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class WayfarerCapture {
    private static final boolean ENABLED="wayfarer".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>();
    private static int state,frame,hoverView; private static long next,boot,started;
    private static CompletableFuture<Void> pending;
    private WayfarerCapture() { }
    private static void setup() {
        var server=Minecraft.getInstance().getSingleplayerServer();var p=server.getPlayerList().getPlayers().getFirst();
        p.getInventory().clearContent();p.setNoGravity(true);p.setPos(.5,70,.5);
        for(int i=0;i<16;i++)p.getInventory().setItem(i,WayfarerImbuements.create(WayfarerDisplays.choices(i)));
        p.setItemSlot(EquipmentSlot.FEET,WayfarerImbuements.create(Set.of()));
        p.setSprinting(true);NativeMana.set(p,100);
        var cast=WayfarerMagic.activate(p);
        if(cast==null || cast.status()!=SpellRuntime.Status.COMPLETED || NativeMana.amount(p)!=95)throw new IllegalStateException("Native boot payment failed");
        NativeMana.set(p,0);NativeMana.sync(p,true);p.containerMenu.broadcastChanges();
        CHECKS.add(Map.of("activation","real integrated-server ability/payment","paidMana",5,"remainingAfterPayment",95,"recoveryTicks",300));
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==9)return;
        var mc=Minecraft.getInstance();long now=System.nanoTime();if(boot==0)boot=now;
        try {
            if(now-boot>240_000_000_000L)throw new IllegalStateException("Wayfarer native capture timed out: "+state);
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                mc.options.pauseOnLostFocus=false;mc.options.hideGui=false;mc.options.guiScale().set(2);mc.options.renderDistance().set(2);
                mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-wayfarer-"+UUID.randomUUID(),
                        new LevelSettings("Wayfarer inspection",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.player!=null && mc.level!=null && mc.screen==null) {
                mc.setScreen(new Review());state=2;next=now+1_000_000_000L;
            } else if(state==2 && now>=next) {
                var out=Path.of(System.getProperty("vestige.capture.output"));Files.createDirectories(out);
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("art-names.png"));}
                for(int mask:List.of(0,9,15)) {
                    var stack=WayfarerImbuements.create(WayfarerDisplays.choices(mask));var name=stack.getHoverName();var styles=new ArrayList<Map<String,Object>>();
                    name.visit((style,text)->{styles.add(Map.of("text",text,"italic",style.isItalic()));return Optional.empty();},net.minecraft.network.chat.Style.EMPTY);
                    CHECKS.add(Map.of("mask",mask,"name",name.getString(),"segments",styles,"durability",stack.getMaxDamage()));
                }
                for(String asset:List.of("textures/item/wayfarer_boots.png","textures/models/armor/wayfarer_layer_1.png","models/item/wayfarer_boots.json")) {
                    try(var input=mc.getResourceManager().open(VestigeMainMod.location(asset))) {
                        CHECKS.add(Map.of("loadedResource",asset,"sha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()))));
                    }
                }
                mc.setScreen(null);pending=mc.getSingleplayerServer().submit(WayfarerCapture::setup);state=3;
            } else if(state==3 && pending.isDone()) {
                pending.join();hoverView=0;mc.setScreen(new InventoryScreen(mc.player) {
                    @Override public void render(GuiGraphics g,int mx,int my,float partial) {
                        int left=(width-176)/2,top=(height-166)/2;
                        super.render(g,hoverView<0?20:left+16+(hoverView==2?6*18:0),hoverView<0?20:top+(hoverView==0?150:92),partial);
                    }
                });mc.mouseHandler.releaseMouse();state=4;next=now+800_000_000L;started=now;
            } else if(state==4 && now>=next) {
                var stack=mc.player.getItemBySlot(EquipmentSlot.FEET);float shortage=ItemManaOverlay.shortage(stack),recovery=ItemManaOverlay.recovery(stack);
                var out=Path.of(System.getProperty("vestige.capture.output"));Files.createDirectories(out.resolve("readiness"));
                String file="readiness/frame-"+String.format(Locale.ROOT,"%03d",frame++)+".png";
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(file));}
                if(frame<=3) {String name=List.of("plain","nimble","unfaltering").get(frame-1);try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("hover-"+name+".png"));}hoverView=frame<3?frame:-1;}
                CHECKS.add(Map.of("file",file,"seconds",(now-started)/1e9,"mana",ItemManaOverlay.amount(),"shortage",shortage,"recovery",recovery));
                if(frame==1 && (shortage!=1 || recovery<=0))throw new IllegalStateException("Actual private packets did not reach boots decorations");
                next=now+200_000_000L;
                if(now-started>16_500_000_000L) {
                    Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited main render target / registered items and production readiness packets","checks",CHECKS))+"\n");state=9;mc.stop();
                }
            }
        } catch(Exception failure){state=9;LogUtils.getLogger().error("Wayfarer native inspection failed",failure);mc.stop();}
    }
    private static final class Review extends Screen {
        final RemotePlayer[][] models=new RemotePlayer[2][5];
        Review(){super(Component.literal("Wayfarer Boots"));for(int row=0;row<2;row++)for(int view=0;view<5;view++) {
            UUID uuid=new UUID(0,0);for(int i=0;i<100;i++){uuid=new UUID(0,i);if(DefaultPlayerSkin.get(uuid).model()==(row==0?PlayerSkin.Model.WIDE:PlayerSkin.Model.SLIM))break;}
            var p=new RemotePlayer(Minecraft.getInstance().level,new GameProfile(uuid,""));p.setItemSlot(EquipmentSlot.FEET,WayfarerImbuements.create(Set.of()));
            if(view==3)for(int step=0;step<11;step++)p.walkAnimation.update(.8f,1);if(view==4)p.setPose(Pose.CROUCHING);models[row][view]=p;
        }}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void render(GuiGraphics g,int mx,int my,float partial) {
            g.fill(0,0,width,height,0xff343434);int x=4,y=28;
            g.blit(ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png"),x,y,0,0,176,166);
            for(int i=0;i<16;i++)g.renderItem(WayfarerImbuements.create(WayfarerDisplays.choices(i)),x+8+i%9*18,y+18+i/9*18);
            var neighbors=List.of(Items.LEATHER_BOOTS,Items.IRON_BOOTS,Items.CHAINMAIL_BOOTS,Items.DIAMOND_BOOTS,Items.LEATHER,Items.FEATHER,Items.RABBIT_FOOT,Items.STRING,Items.HONEYCOMB);
            for(int i=0;i<9;i++)g.renderItem(new ItemStack(neighbors.get(i)),x+8+i*18,y+54);
            for(int i=0;i<3;i++){var stack=WayfarerImbuements.create(WayfarerDisplays.choices(List.of(0,9,15).get(i)));g.renderItem(stack,x+8,y+82+i*22);g.drawString(font,stack.getHoverName(),x+28,y+86+i*22,0xffffff,false);}
            int start=184,cell=(width-start)/5,rowHeight=(height-32)/2;
            for(int row=0;row<2;row++)for(int view=0;view<5;view++) {
                int left=start+view*cell,top=16+row*rowHeight;var p=models[row][view];p.yBodyRot=p.yBodyRotO=p.yHeadRot=p.yHeadRotO=180;p.setYRot(180);p.setXRot(0);
                g.drawString(font,List.of("front","side","back","walk","crouch").get(view),left,top,0xffffff,false);
                float angle=switch(view){case 1->90;case 2->180;case 3->25;default->0;};
                g.enableScissor(left,top+12,left+cell,top+rowHeight);
                InventoryScreen.renderEntityInInventory(g,left+cell/2f,top+rowHeight/2f+12,Math.min(cell*.7f,(rowHeight-20)/1.9f),new Vector3f(0,.95f,0),new Quaternionf().rotateZ((float)Math.PI).rotateY(angle*(float)Math.PI/180),new Quaternionf(),p);
                g.disableScissor();
            }
        }
    }
}
