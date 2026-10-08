package com.quzzar.vestige.equipment.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.MagicEquipment;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class MagicArmorColors {
    private MagicArmorColors() { }
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item event) {
        event.register((stack,layer) -> layer==0 ? 0xff000000 | DyedItemColor.getOrDefault(stack,DyeColor.WHITE.getTextureDiffuseColor()) : -1,
                MagicEquipment.WARDWEAVE.get(),MagicEquipment.CINDERWEAVE.get());
    }
}
