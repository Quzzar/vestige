package com.quzzar.vestige.apparatus.recipeviewer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Builds a masked rim from the active block sprite; no vanilla textures are bundled. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class ImbuementFrameRenderer {
    private static final int RESOLUTION=256;
    private static final Map<ResourceLocation,ResourceLocation> TEXTURES=new HashMap<>();
    private static final ResourceLocation PLINTH=VestigeMainMod.location("textures/gui/ritual/plinth.png");
    private ImbuementFrameRenderer() { }
    @SubscribeEvent public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)manager -> {
            var textures=Minecraft.getInstance().getTextureManager();
            TEXTURES.values().forEach(textures::release);TEXTURES.clear();
        });
    }
    public static void draw(GuiGraphics graphics,ResourceLocation material,int centerX,int centerY) {
        var texture=TEXTURES.computeIfAbsent(material,ImbuementFrameRenderer::create);
        if(texture==null)return;
        graphics.pose().pushPose();graphics.pose().translate(centerX-17f,centerY-17f,0);
        graphics.blit(texture,0,0,34,34,0f,0f,RESOLUTION,RESOLUTION,RESOLUTION,RESOLUTION);
        graphics.pose().popPose();
    }
    private static ResourceLocation create(ResourceLocation material) {
        var minecraft=Minecraft.getInstance();
        var sprite=minecraft.getBlockRenderer().getBlockModelShaper().getBlockModel(
                BuiltInRegistries.BLOCK.get(material).defaultBlockState()).getParticleIcon().contents();
        var face=sprite.getOriginalImage();
        try(var stream=minecraft.getResourceManager().open(PLINTH);var art=NativeImage.read(stream)) {
            var image=new NativeImage(RESOLUTION,RESOLUTION,true);
            for(int y=0;y<RESOLUTION;y++)for(int x=0;x<RESOLUTION;x++) {
                double u=(x+.5)/RESOLUTION,v=(y+.5)/RESOLUTION;
                if(!ImbuementFrame.contains(u*ImbuementFrame.SOURCE_SIZE,v*ImbuementFrame.SOURCE_SIZE))continue;
                int ink=art.getPixelRGBA((int)(u*art.getWidth()),(int)(v*art.getHeight()));
                int block=face.getPixelRGBA((int)(u*sprite.width()),(int)(v*sprite.height()));
                int color=0xff000000;
                for(int shift=0;shift<24;shift+=8)color|=(int)(((block>>>shift)&255)*(.15+.85*((ink>>>shift)&255)/255.))<<shift;
                image.setPixelRGBA(x,y,color);
            }
            return minecraft.getTextureManager().register("vestige_imbuement_frame",new DynamicTexture(image));
        }catch(IOException exception){LogUtils.getLogger().warn("Unable to render ritual imbuement {}",material,exception);return null;}
    }
}
