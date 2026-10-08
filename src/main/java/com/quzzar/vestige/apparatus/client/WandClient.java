package com.quzzar.vestige.apparatus.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.apparatus.WandData;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class WandClient {
    private WandClient() { }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ScrollItems.WAND.get(),VestigeMainMod.location("wand_appearance"),
                (stack,level,entity,seed) -> WandData.binding(stack).map(b -> (float)(b.base().ordinal()*(com.quzzar.vestige.apparatus.WandTips.Tip.values().length+1)+b.tip().map(t -> t.ordinal()+1).orElse(0))).orElse(0f)));
    }
}
