package com.quzzar.vestige.magic.presentation.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ApparatusBlocks;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Development-only tint for the opt-in framing comparison resource pack. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ApparatusFramingPreview {
    public static final int TINT_INDEX = 31;
    public static final int TINT_COLOR = 0xfff0f0f0;

    private ApparatusFramingPreview() { }

    private static boolean enabled() {
        return System.getProperty("vestige.capture.output") != null
                && System.getProperty("vestige.capture.apparatus_preview", "current").equals("plinth-framing");
    }

    @SubscribeEvent public static void blocks(RegisterColorHandlersEvent.Block event) {
        if (!enabled()) return;
        event.register((state, level, pos, tint) -> tint == TINT_INDEX ? TINT_COLOR : -1,
                ApparatusBlocks.PLINTHS.get(ApparatusMaterials.COBBLESTONE).get(),
                ApparatusBlocks.SPELLSTONES.get(ApparatusMaterials.STONE).get());
    }

    @SubscribeEvent public static void items(RegisterColorHandlersEvent.Item event) {
        if (!enabled()) return;
        event.register((stack, tint) -> tint == TINT_INDEX ? TINT_COLOR : -1,
                ApparatusBlocks.PLINTHS.get(ApparatusMaterials.COBBLESTONE).get().asItem(),
                ApparatusBlocks.SPELLSTONES.get(ApparatusMaterials.STONE).get().asItem());
    }
}
