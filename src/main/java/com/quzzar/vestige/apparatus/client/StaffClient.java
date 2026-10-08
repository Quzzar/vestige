package com.quzzar.vestige.apparatus.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.MundaneStaffs;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Chooses the anchored 3D guard model only while a mundane Staff is being held up. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class StaffClient {
    private StaffClient() { }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MundaneStaffs.all().forEach(item -> ItemProperties.register(item,
                VestigeMainMod.location("guarding"), (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem()
                                && ItemStack.isSameItemSameComponents(stack, entity.getUseItem()) ? 1.0F : 0.0F)));
    }
}
