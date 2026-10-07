package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;

/** Development-only: renders all composed item models through Minecraft's ordinary item renderer. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeWandCapture {
    private static final boolean ENABLED="wands".equals(System.getProperty("vestige.capture.kind"));
    private static boolean opened,done;private static long deadline;
    private NativeWandCapture(){ }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception {
        if(!ENABLED || done)return;var mc=Minecraft.getInstance();
        if(!opened){if(!(mc.screen instanceof TitleScreen) || mc.getOverlay()!=null)return;opened=true;mc.getWindow().setWindowed(1440,1000);mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new Palette());deadline=System.nanoTime()+3_000_000_000L;return;}
        if(System.nanoTime()<deadline)return;done=true;var out=Path.of(System.getProperty("vestige.capture.output"),"wands");Files.createDirectories(out);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("all-63-wands-and-five-threads.png"));}
        var hashes=new TreeMap<String,String>();var paths=new ArrayList<String>();
        for(var base:WandComponents.Base.values())paths.add("textures/item/wands/base_"+base.id()+".png");
        for(var tip:WandTips.Tip.values())paths.add("textures/item/wands/tip_"+tip.id()+".png");
        for(var thread:MagicalThreadRecipe.types())paths.add("textures/item/"+thread.id().getPath()+".png");
        paths.add("models/item/wand.json");
        for(String path:paths)try(var input=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()){
            byte[] bytes=input.readAllBytes();
            if(path.endsWith(".png"))try(var sprite=com.mojang.blaze3d.platform.NativeImage.read(bytes)){
                if(sprite.getWidth()!=16 || sprite.getHeight()!=16)throw new IllegalStateException("Non-16x16 item sprite: "+path);
            }
            hashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)));
        }
        Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("source","Minecraft main render target, ordinary item renderer","wand_models",63,"thread_models",5,"assetSha256",hashes))+"\n");mc.stop();
    }
    private static final class Palette extends Screen {
        Palette(){super(Component.literal("Vestige wand appearance inspection"));}
        public void render(GuiGraphics g,int x,int y,float delta){
            g.fill(0,0,width,height,0xff242527);g.drawString(font,title,18,14,0xffe7dec9,false);
            String[] headings={"Untipped","Amethyst","Diamond","Emerald","Ender","Copper","Iron","Ghast","Netherite"};
            int left=104,cell=(width-left-14)/9,row=(height-115)/7;
            for(int tip=0;tip<9;tip++)g.drawString(font,headings[tip],left+tip*cell,36,0xffc2b7a4,false);
            int base=0;
            for(var body:WandComponents.Base.values()){
                int py=54+base++*row;g.drawString(font,body.id().replace('_',' '),18,py+13,0xffd4caba,false);
                for(int tip=0;tip<9;tip++){
                    var chosen=tip==0?Optional.<WandTips.Tip>empty():Optional.of(WandTips.Tip.values()[tip-1]);
                    var item=WandData.create(body,MagicalThreadRecipe.Type.CALLOUS,chosen,ScrollItems.scroll(VestigeMainMod.location("fireball")));
                    g.pose().pushPose();g.pose().translate(left+tip*cell,py,0);g.pose().scale(2.25f,2.25f,1);g.renderItem(item,0,0);g.pose().popPose();
                }
            }
            int px=104;g.drawString(font,"Magical threads",18,height-36,0xffd4caba,false);
            for(var thread:MagicalThreadRecipe.types()){g.pose().pushPose();g.pose().translate(px,height-49,0);g.pose().scale(2,2,1);g.renderItem(new ItemStack(thread.item()),0,0);g.pose().popPose();g.drawString(font,thread.name(),px+38,height-35,0xffc2b7a4,false);px+=112;}
        }
        public boolean isPauseScreen(){return false;}
    }
}
