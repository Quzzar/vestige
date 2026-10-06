package com.quzzar.vestige.apparatus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.quzzar.vestige.apparatus.ApparatusBlock;
import com.quzzar.vestige.apparatus.ApparatusBlocks;
import com.quzzar.vestige.apparatus.ApparatusShapes;
import com.quzzar.vestige.apparatus.OfferingBlockEntity;
import com.quzzar.vestige.apparatus.RitualRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Wrong placements shake sideways; successful rituals lift ingredients while stone stays grounded. */
public final class OfferingRenderer implements BlockEntityRenderer<OfferingBlockEntity> {
    private final ItemRenderer items;
    public OfferingRenderer(BlockEntityRendererProvider.Context context) { items = context.getItemRenderer(); }

    @Override public void render(OfferingBlockEntity offering, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!(offering.getBlockState().getBlock() instanceof ApparatusBlock block)) return;
        var feedback=offering.feedback(); float age=offering.feedbackAge(partialTick);
        boolean glowing=feedback==RitualRecipe.Feedback.CORRECT || feedback==RitualRecipe.Feedback.SUCCESS;
        boolean pedestal=ApparatusBlocks.isPlinth(offering.getBlockState());
        pose.pushPose();
        if (pedestal && (feedback==RitualRecipe.Feedback.SHAKE || feedback==RitualRecipe.Feedback.WRONG)) {
            pose.translate(Math.sin(age*1.6)*.04,0,Math.cos(age*1.4)*.025);
        }
        var minecraft=Minecraft.getInstance();
        var dispatcher=minecraft.getBlockRenderer();
        int tint=minecraft.getBlockColors().getColor(offering.getBlockState(),offering.getLevel(),offering.getBlockPos(),0);
        dispatcher.getModelRenderer().renderModel(pose.last(),buffers.getBuffer(RenderType.cutout()),offering.getBlockState(),
                dispatcher.getBlockModel(offering.getBlockState()),((tint>>16)&255)/255f,((tint>>8)&255)/255f,(tint&255)/255f,
                glowing ? LightTexture.FULL_BRIGHT : light,overlay);
        if (!pedestal && !SpellstoneRuneOptions.render(offering,partialTick,pose,buffers)) {
            double seconds=(offering.getLevel().getGameTime()+partialTick)/20.0;
            AstralSealRenderer.render(pose,buffers,seconds,AstralSealRenderer.HOVER);
        }
        boolean exposed=offering.hasOfferingSpace();
        if (glowing && exposed) glow(pose,buffers,block.offeringHeight(),offering.feedbackColor(),(float)block.offeringWidth()/2);
        if (pedestal && !offering.materialItem().isEmpty()) {
            var sprite=items.getModel(offering.materialItem(),offering.getLevel(),null,0).getParticleIcon();
            var vertices=buffers.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
            float half=(float)ApparatusShapes.PLINTH_SOCKET_SIZE/2;
            float z=(float)ApparatusShapes.PLINTH_SOCKET_FACE_Z-.5f;
            float back=(float)ApparatusShapes.PLINTH_SOCKET_BACK_Z-.5f;
            float u0=sprite.getU0(),u1=sprite.getU1(),v0=sprite.getV0(),v1=sprite.getV1();
            float fraction=(back-z)/(2*half);
            float edgeU=u0+(u1-u0)*fraction,edgeV=v0+(v1-v0)*fraction;
            for (int side=0;side<4;side++) {
                pose.pushPose();pose.translate(.5,ApparatusShapes.PLINTH_SOCKET_CENTER_Y,.5);pose.mulPose(Axis.YP.rotationDegrees(side*90));
                socketVertex(vertices,pose,-half,-half,z,sprite.getU0(),sprite.getV1(),light,overlay);
                socketVertex(vertices,pose,-half,half,z,sprite.getU0(),sprite.getV0(),light,overlay);
                socketVertex(vertices,pose,half,half,z,sprite.getU1(),sprite.getV0(),light,overlay);
                socketVertex(vertices,pose,half,-half,z,sprite.getU1(),sprite.getV1(),light,overlay);
                socketFace(vertices,pose,new float[]{-half,half,z},new float[]{-half,half,back},
                        new float[]{half,half,back},new float[]{half,half,z},u0,v0,u1,edgeV,0,1,0,light,overlay);
                socketFace(vertices,pose,new float[]{-half,-half,back},new float[]{-half,-half,z},
                        new float[]{half,-half,z},new float[]{half,-half,back},u0,v0,u1,edgeV,0,-1,0,light,overlay);
                socketFace(vertices,pose,new float[]{-half,-half,back},new float[]{-half,half,back},
                        new float[]{-half,half,z},new float[]{-half,-half,z},u0,v0,edgeU,v1,-1,0,0,light,overlay);
                socketFace(vertices,pose,new float[]{half,-half,z},new float[]{half,half,z},
                        new float[]{half,half,back},new float[]{half,-half,back},u0,v0,edgeU,v1,1,0,0,light,overlay);
                pose.popPose();
            }
        }
        ItemStack stack=offering.displayedItem();
        if (exposed && !stack.isEmpty()) {
            pose.pushPose();
            if (pedestal && feedback==RitualRecipe.Feedback.SUCCESS) {
                pose.translate(0,.35*Math.sin(Math.PI*Math.clamp(age,0,60)/60),0);
            }
            resting(stack,offering,block,pose,buffers,glowing ? LightTexture.FULL_BRIGHT : light,overlay);
            pose.popPose();
        }
        else if (exposed && feedback==RitualRecipe.Feedback.HINT && !offering.hintItem().isEmpty()) {
            MultiBufferSource silhouette=type -> new SilhouetteConsumer(buffers.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS)));
            resting(offering.hintItem(),offering,block,pose,silhouette,LightTexture.FULL_BRIGHT,overlay);
        }
        if (!offering.resultItem().isEmpty()) {
            pose.pushPose();
            // A small paper-thickness stack offset keeps the reference visible beneath the result.
            if (!stack.isEmpty()) pose.translate(.05,.035,.05);
            resting(offering.resultItem(),offering,block,pose,buffers,light,overlay);
            pose.popPose();
        }
        pose.popPose();
    }
    /** A thin raised material plate per side, centered identically on all column parts. */
    private static void socketVertex(VertexConsumer vertices,PoseStack pose,float x,float y,float z,float u,float v,int light,int overlay) {
        vertices.addVertex(pose.last().pose(),x,y,z).setColor(255,255,255,255).setUv(u,v)
                .setOverlay(overlay).setLight(light).setNormal(pose.last(),0,0,-1);
    }
    private static void socketFace(VertexConsumer vertices,PoseStack pose,float[] a,float[] b,float[] c,float[] d,
                                   float u0,float v0,float u1,float v1,float nx,float ny,float nz,int light,int overlay) {
        socketEdgeVertex(vertices,pose,a,u0,v1,nx,ny,nz,light,overlay);
        socketEdgeVertex(vertices,pose,b,u0,v0,nx,ny,nz,light,overlay);
        socketEdgeVertex(vertices,pose,c,u1,v0,nx,ny,nz,light,overlay);
        socketEdgeVertex(vertices,pose,d,u1,v1,nx,ny,nz,light,overlay);
    }
    private static void socketEdgeVertex(VertexConsumer vertices,PoseStack pose,float[] point,float u,float v,
                                         float nx,float ny,float nz,int light,int overlay) {
        vertices.addVertex(pose.last().pose(),point[0],point[1],point[2]).setColor(255,255,255,255).setUv(u,v)
                .setOverlay(overlay).setLight(light).setNormal(pose.last(),nx,ny,nz);
    }
    private void resting(ItemStack stack,OfferingBlockEntity offering,ApparatusBlock block,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        var model=items.getModel(stack,offering.getLevel(),null,0);
        boolean solid=model.isGui3d(); var ground=model.getTransforms().getTransform(ItemDisplayContext.GROUND);
        pose.pushPose(); pose.translate(.5,block.offeringHeight()+(solid ? ground.scale.y()/2+.01 : .02),.5);
        if (!solid) pose.mulPose(Axis.XP.rotationDegrees(90));
        // Remove ground's positioning offset, retaining its exact native model rotation and scale.
        // Flat items lie on the stone; dropped-item bobbing, spinning and count copies are not offerings.
        pose.translate(-ground.translation.x(),-ground.translation.y(),-ground.translation.z());
        items.renderStatic(stack,ItemDisplayContext.GROUND,light,overlay,pose,buffers,offering.getLevel(),0); pose.popPose();
    }
    private static void glow(PoseStack pose,MultiBufferSource buffers,double y,int color,float radius) {
        var vertices=buffers.getBuffer(RenderType.debugQuads()); float min=.5f-radius,max=.5f+radius,w=.018f;
        rect(vertices,pose,min,(float)y+.007f,min,max,min+w,color); rect(vertices,pose,min,(float)y+.007f,max-w,max,max,color);
        rect(vertices,pose,min,(float)y+.007f,min,min+w,max,color); rect(vertices,pose,max-w,(float)y+.007f,min,max,max,color);
    }
    private static void rect(VertexConsumer v,PoseStack p,float x,float y,float z,float ex,float ez,int c) {
        int r=c>>16&255,g=c>>8&255,b=c&255;
        v.addVertex(p.last().pose(),x,y,z).setColor(r,g,b,210); v.addVertex(p.last().pose(),x,y,ez).setColor(r,g,b,210);
        v.addVertex(p.last().pose(),ex,y,ez).setColor(r,g,b,210); v.addVertex(p.last().pose(),ex,y,z).setColor(r,g,b,210);
    }
    /** Keep the baked item's shape and texture cutout, with completely opaque black faces. */
    private static final class SilhouetteConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        SilhouetteConsumer(VertexConsumer delegate) { this.delegate=delegate; }
        @Override public VertexConsumer addVertex(float x,float y,float z) { delegate.addVertex(x,y,z); return this; }
        @Override public VertexConsumer setColor(int r,int g,int b,int a) {
            delegate.setColor(0,0,0,255); return this;
        }
        @Override public VertexConsumer setUv(float u,float v) { delegate.setUv(u,v); return this; }
        @Override public VertexConsumer setUv1(int u,int v) { delegate.setUv1(u,v); return this; }
        @Override public VertexConsumer setUv2(int u,int v) { delegate.setUv2(u,v); return this; }
        @Override public VertexConsumer setNormal(float x,float y,float z) { delegate.setNormal(x,y,z); return this; }
    }
}
