package com.quzzar.vestige.apparatus.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ApparatusBlocks;
import com.quzzar.vestige.apparatus.ApparatusShapes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

/** The visible stone edges are straight even though Minecraft collision uses small slices. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class SpellstoneHighlight {
    private SpellstoneHighlight() { }

    @SubscribeEvent public static void render(RenderHighlightEvent.Block event) {
        var level=Minecraft.getInstance().level;
        var pos=event.getTarget().getBlockPos();
        if (level==null || !ApparatusBlocks.isSpellstone(level.getBlockState(pos))) return;
        event.setCanceled(true);
        var pose=event.getPoseStack();
        var camera=event.getCamera().getPosition();
        pose.pushPose();
        pose.translate(pos.getX()-camera.x,pos.getY()-camera.y,pos.getZ()-camera.z);
        VertexConsumer vertices=event.getMultiBufferSource().getBuffer(RenderType.lines());
        for (var corners:ApparatusShapes.SPELLSTONE_OUTLINE) {
            for (int i=0;i<corners.length;i++) {
                for (int bit:new int[]{1,2,4}) {
                    int next=i^bit;
                    if (next<=i) continue;
                    Vec3 a=corners[i],b=corners[next],direction=b.subtract(a).normalize();
                    vertices.addVertex(pose.last().pose(),(float)a.x,(float)a.y,(float)a.z).setColor(0,0,0,.4f)
                            .setNormal(pose.last(),(float)direction.x,(float)direction.y,(float)direction.z);
                    vertices.addVertex(pose.last().pose(),(float)b.x,(float)b.y,(float)b.z).setColor(0,0,0,.4f)
                            .setNormal(pose.last(),(float)direction.x,(float)direction.y,(float)direction.z);
                }
            }
        }
        pose.popPose();
    }
}
