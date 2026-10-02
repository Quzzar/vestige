package com.quzzar.vestige.magic.world;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/** Reuses the live source's vanilla/mod renderer, pose, skin, profession and equipment. */
final class SpellEchoRenderer extends EntityRenderer<SpellEcho> {
    SpellEchoRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(SpellEcho echo) { return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS; }
    @SuppressWarnings("unchecked")
    @Override public void render(SpellEcho echo,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        var source=echo.source();if(source==null) return;
        EntityRenderer<Entity> renderer=(EntityRenderer<Entity>)(EntityRenderer<?>)entityRenderDispatcher.getRenderer(source);
        pose.pushPose();
        renderer.render(source,source.getYRot(),partial,pose,buffers,light);
        pose.popPose();
    }
}
