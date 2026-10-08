package com.quzzar.vestige.equipment.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.MagicEquipment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RobeModels {
    private static RobeModel wide;
    private static RobeModel slim;

    private RobeModels() { }

    @SubscribeEvent
    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RobeModel.WIDE, () -> RobeModel.layer(false));
        event.registerLayerDefinition(RobeModel.SLIM, () -> RobeModel.layer(true));
    }

    @SubscribeEvent
    public static void bake(EntityRenderersEvent.AddLayers event) {
        // Re-bake on resource reload too; never retain parts from an old model set.
        var models = Minecraft.getInstance().getEntityModels();
        wide = new RobeModel(models.bakeLayer(RobeModel.WIDE));
        slim = new RobeModel(models.bakeLayer(RobeModel.SLIM));
    }

    @SubscribeEvent
    public static void extensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public Model getGenericArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (slot != EquipmentSlot.CHEST) return original;
                boolean narrow = wearer instanceof AbstractClientPlayer player && player.getSkin().model() == PlayerSkin.Model.SLIM;
                var model = narrow ? slim : wide;
                if (model == null) return original;
                ClientHooks.copyModelProperties(original, model);
                model.prepare();
                return model;
            }
        }, MagicEquipment.WARDWEAVE.get(), MagicEquipment.CINDERWEAVE.get());
    }
}
