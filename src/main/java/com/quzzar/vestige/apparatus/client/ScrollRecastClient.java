package com.quzzar.vestige.apparatus.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollRecastPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Vanilla empty-air use supplies the second target for multi-stage single-use scrolls. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class ScrollRecastClient {
    private ScrollRecastClient() { }
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getEntity().getMainHandItem().isEmpty() && event.getEntity().getOffhandItem().isEmpty()) PacketDistributor.sendToServer(new ScrollRecastPayload());
    }
}
