package com.quzzar.vestige.magic.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** One scoped first-person draw-back for all native held casting sources. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PreparationItemAnimation implements IClientItemExtensions {
    private static final float IDLE_X = .56f, IDLE_Y = -.52f, IDLE_Z = -.72f;
    private static final float DRAW_BACK = .08f;
    private PreparationItemAnimation() { }
    @SubscribeEvent public static void register(RegisterClientExtensionsEvent event) {
        event.registerItem(new PreparationItemAnimation(), ScrollItems.SCROLL.get(), ScrollItems.WAND.get(), ScrollItems.STAFF.get());
    }
    /** Return false outside preparation so vanilla equip, attack and item transforms remain in use. */
    @Override public boolean applyForgeHandTransform(PoseStack pose, LocalPlayer player, HumanoidArm arm,
                                                     ItemStack stack, float partialTick, float equip, float swing) {
        if (!player.isAlive() || player.isSpectator() || Minecraft.getInstance().screen != null) return false;
        var source = PreparationHud.heldSource().orElse(null);
        var hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (source == null || source.hand() != hand || PreparationHud.preparation().isEmpty()
                || !source.matches(stack)
                || !ItemStack.isSameItemSameComponents(stack, player.getItemInHand(hand))) return false;
        float progress = PreparationHud.fraction(partialTick);
        // Smoothly leave the ordinary rest pose, then continue drawing throughout the actual time cost.
        float draw = progress * progress * (3f - 2f * progress);
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        pose.translate(side * IDLE_X, IDLE_Y - .6f * equip, IDLE_Z + DRAW_BACK * draw);
        return true;
    }
}
