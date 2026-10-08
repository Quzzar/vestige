package com.quzzar.vestige.equipment.client;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.MagicEquipment;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.*;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in native proof: actual registered items, dyes and vanilla player armor renderer. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class MagicEquipmentCapture {
    private static final boolean ENABLED="magic_equipment".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>();
    private static int state,index;private static long next,worldDeadline;private static Review screen;
    private MagicEquipmentCapture() { }
    private static ItemStack robe(int item,DyeColor color) {
        var stack=new ItemStack(item==0?MagicEquipment.WARDWEAVE.get():MagicEquipment.CINDERWEAVE.get());
        stack.set(DataComponents.DYED_COLOR,new DyedItemColor(color.getTextureDiffuseColor(),false));return stack;
    }
    @SubscribeEvent public static void camera(ClientTickEvent.Post event) {
        if(!ENABLED || (state!=3 && state!=4))return;
        var mc=Minecraft.getInstance();if(mc.player==null)return;
        mc.mouseHandler.releaseMouse();
        for(var key:List.of(mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,mc.options.keyJump,mc.options.keyShift,mc.options.keySprint))key.setDown(false);
        mc.player.setYRot(worldAngle());mc.player.yRotO=worldAngle();
        mc.player.yBodyRot=mc.player.yBodyRotO=15;
        mc.player.yHeadRot=mc.player.yHeadRotO=15;
        mc.player.setXRot(0);mc.player.xRotO=0;
        mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==9)return;
        var mc=Minecraft.getInstance();long now=System.nanoTime();
        try {
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;mc.options.renderDistance().set(2);mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-robes-"+UUID.randomUUID(),
                        new LevelSettings("Robe inspection",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.level!=null && mc.player!=null && mc.screen==null) {
                screen=new Review();mc.setScreen(screen);state=2;next=now+1_000_000_000L;
            } else if(state==2 && now>=next) {
                boolean outfitted=index>=64;int item=outfitted?index-64:index/32,color=outfitted?0:index%16,scale=outfitted?2:index%32<16?2:3;var dye=DyeColor.byId(color);
                if(screen.item!=item || screen.color!=dye || screen.outfitted!=outfitted || mc.options.guiScale().get()!=scale) {
                    screen.item=item;screen.color=dye;screen.outfitted=outfitted;mc.options.guiScale().set(scale);mc.resizeDisplay();screen.equip();next=now+180_000_000L;return;
                }
                var stack=robe(item,dye);int tint=mc.getItemColors().getColor(stack,0)&0xffffff;
                if(tint!=(dye.getTextureDiffuseColor()&0xffffff) || (mc.getItemColors().getColor(stack,1)&0xffffff)!=0xffffff)throw new IllegalStateException("Fabric/trim tint mismatch: " + tint + " / " + dye.getTextureDiffuseColor());
                String name=(item==0?"wardweave":"cinderweave")+"-"+dye.getName()+(outfitted?"-armor-layers":"-gui-"+scale);
                var out=Path.of(System.getProperty("vestige.capture.output"));Files.createDirectories(out);
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name+".png"));}
                CHECKS.add(Map.of("view",name,"fabricColor",tint,"trimUntinted",true,"models",List.of(screen.wide.getSkin().model().name(),screen.slim.getSkin().model().name()),"poses",List.of("front","side","back","walking","crouching"),"durability",stack.getMaxDamage()));
                if(++index==66) {
                    if(Boolean.getBoolean("vestige.capture.gui_only")) {finish(mc,out);return;}
                    index=0;state=3;mc.setScreen(null);mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);mc.options.hideGui=true;
                }
                next=now+180_000_000L;
            } else if(state==3 && now>=next) {
                int item=index/9;
                var color=List.of(DyeColor.WHITE,DyeColor.BLUE,DyeColor.BLACK).get(index/3%3);
                var stack=robe(item,color);
                var id=mc.player.getUUID();
                mc.getSingleplayerServer().execute(() -> {
                    var wearer=mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                    if(wearer!=null){wearer.setItemSlot(EquipmentSlot.CHEST,stack);wearer.setNoGravity(true);wearer.getAbilities().flying=true;wearer.onUpdateAbilities();}
                    mc.getSingleplayerServer().overworld().setDayTime(6000);
                });
                mc.player.setYRot(worldAngle());mc.player.yRotO=worldAngle();
                mc.player.setXRot(0);mc.player.xRotO=0;
                state=4;worldDeadline=now+60_000_000_000L;next=now+1_200_000_000L;
            } else if(state==4 && now>=next) {
                int item=index/9;
                var dye=List.of(DyeColor.WHITE,DyeColor.BLUE,DyeColor.BLACK).get(index/3%3);
                if(now>worldDeadline)throw new IllegalStateException("World view did not become ready for "+dye.getName());
                if(mc.screen!=null || mc.getOverlay()!=null || mc.levelRenderer.countRenderedSections()==0 || !ItemStack.isSameItemSameComponents(mc.player.getItemBySlot(EquipmentSlot.CHEST),robe(item,dye))) {
                    next=now+500_000_000L;return;
                }
                String name=(item==0?"wardweave":"cinderweave")+"-"+dye.getName()+"-world-"+List.of("front","side","back").get(index%3);
                var out=Path.of(System.getProperty("vestige.capture.output"));
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(name+".png"));}
                CHECKS.add(Map.of("view",name,"source","integrated-server equipped local player / third-person "+List.of("front","side","back").get(index%3),"fabricColor",dye.getTextureDiffuseColor()&0xffffff,"cameraPitch",mc.player.getXRot(),"cameraYaw",worldAngle(),"bodyYaw",mc.player.yBodyRot,"renderedSections",mc.levelRenderer.countRenderedSections()));
                if(++index==18) {
                    finish(mc,out);
                } else {state=3;next=now+200_000_000L;}
            }
        } catch(Exception failure) {state=9;LogUtils.getLogger().error("Robe inspection failed",failure);mc.stop();}
    }
    private static float worldAngle() { return 15 + index%3*90; }
    private static void finish(Minecraft mc,Path out) throws java.io.IOException {
        Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Minecraft main render target","checks",CHECKS))+"\n");
        state=9;mc.stop();
    }
    private static final class Review extends Screen {
        int item=-1;DyeColor color;boolean outfitted;final RemotePlayer wide,slim;final RemotePlayer[][] previews=new RemotePlayer[2][5];
        Review() {super(Component.literal("Magic equipment"));wide=model(PlayerSkin.Model.WIDE);slim=model(PlayerSkin.Model.SLIM);for(int row=0;row<2;row++)for(int view=0;view<5;view++){var p=model(row==0?PlayerSkin.Model.WIDE:PlayerSkin.Model.SLIM);if(view==3)for(int step=0;step<11;step++)p.walkAnimation.update(.8f,1);if(view==4)p.setPose(Pose.CROUCHING);previews[row][view]=p;}}
        private static RemotePlayer model(PlayerSkin.Model model) {
            UUID uuid=new UUID(0,0);for(int i=0;i<100;i++){uuid=new UUID(0,i);if(DefaultPlayerSkin.get(uuid).model()==model)break;}
            return new RemotePlayer(Minecraft.getInstance().level,new GameProfile(uuid,""));
        }
        void equip(){wide.setItemSlot(EquipmentSlot.CHEST,robe(item,color));slim.setItemSlot(EquipmentSlot.CHEST,robe(item,color));for(var row:previews)for(var p:row){p.setItemSlot(EquipmentSlot.CHEST,robe(item,color));p.setItemSlot(EquipmentSlot.LEGS,outfitted?new ItemStack(Items.IRON_LEGGINGS):ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.FEET,outfitted?new ItemStack(Items.IRON_BOOTS):ItemStack.EMPTY);}}
        @Override public boolean isPauseScreen(){return false;}
        @Override public void render(GuiGraphics g,int mx,int my,float partial) {
            if(item<0)return;
            g.fill(0,0,width,height,0xff343434);int x=4,y=(height-166)/2;
            g.blit(ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png"),x,y,0,0,176,166);
            g.drawString(font,item==0?"Wardweave Robes":"Cinderweave Robes",x+8,y+6,0x404040,false);
            for(int i=0;i<16;i++){var dye=DyeColor.byId(i);g.renderItem(robe(item,dye),x+8+i%9*18,y+18+i/9*18);}
            var neighbors=List.of(Items.LEATHER_CHESTPLATE,Items.IRON_CHESTPLATE,Items.CHAINMAIL_CHESTPLATE,Items.WHITE_WOOL,Items.BLUE_WOOL,Items.STRING,Items.HONEYCOMB,Items.AMETHYST_SHARD,Items.BLAZE_POWDER);
            for(int i=0;i<neighbors.size();i++)g.renderItem(new ItemStack(neighbors.get(i)),x+8+i*18,y+54);
            int start=184,cellWidth=(width-start-4)/5,rowHeight=(height-32)/2;
            g.drawString(font,color.getName().replace('_',' '),start,6,0xffffff,false);
            for(int row=0;row<2;row++) {
                for(int view=0;view<5;view++) {
                    int left=start+view*cellWidth,top=22+row*rowHeight;
                    g.drawString(font,(row==0?"Wide / ":"Slim / ")+List.of("front","side","back","walk","crouch").get(view),left+4,top,0xffffff,false);
                    var player=previews[row][view];
                    player.yBodyRot=player.yBodyRotO=180;
                    player.yHeadRot=player.yHeadRotO=180;
                    player.setYRot(180);player.setXRot(0);
                    float angle=switch(view){case 1 -> 90;case 2 -> 180;case 3 -> 25;default -> 0;};
                    var pose=new Quaternionf().rotateZ((float)Math.PI).rotateY(angle*(float)Math.PI/180);
                    g.enableScissor(left,top+12,left+cellWidth,top+rowHeight-3);
                    InventoryScreen.renderEntityInInventory(g,left+cellWidth/2f,top+rowHeight/2f+10,
                            Math.min(cellWidth*.65f,(rowHeight-30)/1.9f),new Vector3f(0,.95f,0),pose,
                            new Quaternionf(),player);
                    g.disableScissor();
                }
            }
        }
    }
}
