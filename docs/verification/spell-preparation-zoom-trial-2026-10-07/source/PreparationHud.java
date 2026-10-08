package com.quzzar.vestige.magic.world.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.runtime.SpellRuntime;
import com.quzzar.vestige.magic.world.PreparationPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import java.util.Optional;

/** Private preparation state drives the held item and experimental camera zoom. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class PreparationHud {
    private static Optional<SpellRuntime.Preparation> preparation = Optional.empty();
    private static Optional<PreparationPayload.HeldSource> heldSource = Optional.empty();
    private PreparationHud() { }
    public static void accept(PreparationPayload value) { preparation = value.preparation(); heldSource = value.heldSource(); }
    public static Optional<SpellRuntime.Preparation> preparation() { return preparation; }
    public static Optional<PreparationPayload.HeldSource> heldSource() { return heldSource; }
    /** Both cues interpolate the same final composed time cost, never a hard-coded bow duration. */
    public static float fraction(float partialTick) {
        return preparation.map(p -> (float) Math.min(.999, (p.elapsedTicks() + partialTick) / p.totalTicks())).orElse(0f);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        preparation = Optional.empty(); heldSource = Optional.empty();
    }
}
