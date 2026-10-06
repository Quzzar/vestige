package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.presentation.client.SpellProjectileRenderer;
import com.quzzar.vestige.magic.presentation.client.SpellVisualClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SpellClient {
    private SpellClient() { }
    @SubscribeEvent public static void itemDecorations(net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent event) {
        event.register(com.quzzar.vestige.apparatus.ScrollItems.ATTUNEMENT_SHARD.get(),(graphics,font,stack,x,y)->{
            com.quzzar.vestige.apparatus.AttunementShardItem.mark(stack).ifPresent(mark->{
                graphics.pose().pushPose();
                graphics.pose().translate(0,0,210);
                graphics.drawString(font,mark.runes().getFirst().component(),x,y,0xffffffff,true);
                graphics.pose().popPose();
            });
            return false;
        });
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.quzzar.vestige.apparatus.ApparatusBlocks.OFFERING.get(),
                com.quzzar.vestige.apparatus.client.OfferingRenderer::new);
        event.registerEntityRenderer(SpellEntities.ECHO.get(),SpellEchoRenderer::new);
        event.registerEntityRenderer(SpellEntities.PROJECTILE.get(), SpellProjectileRenderer::new);
        event.registerEntityRenderer(SpellEntities.ANCHOR.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(SpellEntities.CONSTRUCT.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(SpellEntities.BLOCK_DISPLAY.get(), SpellBlockDisplayRenderer::new);
    }
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(()->{
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(SpellBlocks.TEMPORARY_LEAVES.get(),net.minecraft.client.renderer.RenderType.cutoutMipped());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(SpellFluids.WATER.get(),net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(SpellFluids.FLOWING_WATER.get(),net.minecraft.client.renderer.RenderType.translucent());
        });
    }
    @SubscribeEvent public static void colors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Block event) {
        event.register((state,level,pos,tint)->level!=null && pos!=null?net.minecraft.client.renderer.BiomeColors.getAverageFoliageColor(level,pos):0x48a83a,SpellBlocks.TEMPORARY_LEAVES.get());
    }
    @SubscribeEvent public static void itemColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack,tint) -> {
            if (tint==0) return -1;
            var trait=com.quzzar.vestige.apparatus.ScrollItems.fragment(stack).orElse(null);
            if (trait==null) return 0xffb489df;
            return 0xff000000 | switch(trait.getPath()) {
                case "fire" -> 0xef713c; case "water" -> 0x43afe8; case "ice" -> 0x8de4ed;
                case "lightning" -> 0xffdd63; case "nature" -> 0x67bd73; case "blood" -> 0xc54764;
                case "holy" -> 0xffe4a4; case "ender" -> 0x9b78eb; default -> 0xb489df;
            };
        },com.quzzar.vestige.apparatus.ScrollItems.SCROLL.get(),com.quzzar.vestige.apparatus.ScrollItems.FRAGMENT.get());
    }
    @SubscribeEvent public static void reload(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> {
            SpellVisualClient.clear(); com.quzzar.vestige.magic.presentation.client.SpellSenseClient.clear();
        });
    }
}
