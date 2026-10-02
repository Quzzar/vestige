package com.quzzar.vestige.magic.presentation;

import com.quzzar.vestige.VestigeMainMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class SpellVisualNetwork {
    private SpellVisualNetwork() { }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        // The handler is invoked only on the logical client; dedicated servers never load its renderer.
        event.registrar("4").playToClient(SpellVisualPayload.TYPE, SpellVisualPayload.STREAM_CODEC,
                (payload, context) -> com.quzzar.vestige.magic.presentation.client.SpellVisualClient.receive(payload));
        event.registrar("4").playToClient(SpellSensePayload.TYPE, SpellSensePayload.STREAM_CODEC,
                (payload, context) -> com.quzzar.vestige.magic.presentation.client.SpellSenseClient.receive(payload));
    }
}
