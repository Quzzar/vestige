package com.quzzar.vestige.magic.world.client;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Isolated review candidate: a native, gently smoothed zoom for the exact preparing source. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class PreparationZoom {
    private static final float MAX_ZOOM=.08f;
    private static double renderedFov=70;
    private PreparationZoom() { }
    static float modifier(Player player) {
        var mc=Minecraft.getInstance();
        if (player==null || player!=mc.player || !player.isAlive() || player.isSpectator() || mc.screen!=null
                || !mc.options.getCameraType().isFirstPerson() || PreparationHud.preparation().isEmpty()) return 1;
        var source=PreparationHud.heldSource().orElse(null);
        if (source==null || !source.matches(player.getItemInHand(source.hand()))) return 1;
        float progress=PreparationHud.fraction(0);
        float eased=progress*progress*(3-2*progress);
        return 1-MAX_ZOOM*eased*mc.options.fovEffectScale().get().floatValue();
    }
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void zoom(ComputeFovModifierEvent event) {
        event.setNewFovModifier(event.getNewFovModifier()*modifier(event.getPlayer()));
    }
    /** Inspection only: record the actual world projection after Minecraft's usual FOV interpolation. */
    @SubscribeEvent(priority=EventPriority.LOW)
    public static void inspect(ViewportEvent.ComputeFov event) {
        if (event.usedConfiguredFov()) renderedFov=event.getFOV();
    }
    static double renderedFov() { return renderedFov; }
}
