package com.quzzar.vestige.travel.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.travel.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StandingStoneClient {
    private StandingStoneClient() { }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(StandingStones.ENTITY.get(), StandingStoneRenderer::new);
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> StandingStones.STONE_ITEMS.values().forEach(item ->
                ItemProperties.register(item.get(), VestigeMainMod.location("stone_shape"),
                        (stack, level, entity, seed) -> StandingStoneShape.fromKey(StandingStones.key(stack).orElse("")).ordinal())));
    }
}
