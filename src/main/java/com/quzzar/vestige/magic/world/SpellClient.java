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
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
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
    @SubscribeEvent public static void reload(net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener) manager -> {
            SpellVisualClient.clear(); com.quzzar.vestige.magic.presentation.client.SpellSenseClient.clear();
        });
    }
}
