package com.quzzar.vestige.magic.world.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMana;
import com.quzzar.vestige.magic.presentation.client.ManaDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** A quiet horizontal mana meter in the bottom-right corner, hidden at full mana. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class ManaHud {
    private static float amount = NativeMana.MAX;
    private ManaHud() { }
    public static void accept(float value) { amount = value; }
    public static float amount() { return amount; }
    public static boolean visible() { return amount < NativeMana.MAX; }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { amount = NativeMana.MAX; }
    private static void draw(GuiGraphics graphics, DeltaTracker delta) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.isSpectator() || mc.options.hideGui || !visible()) return;
        int occupied = Math.max(mc.gui.leftHeight, mc.gui.rightHeight);
        int x = graphics.guiWidth() - ManaDisplay.METER_WIDTH - 8;
        boolean besideHotbar = x >= graphics.guiWidth() / 2 + 91 + 8;
        int y = besideHotbar ? graphics.guiHeight() - ManaDisplay.METER_HEIGHT - 8 : graphics.guiHeight() - occupied;
        ManaDisplay.drawMeter(graphics, mc.font, x, y, amount);
        // Compact windows lift the right-aligned meter clear of the hotbar and held-item name.
        if (!besideHotbar) { mc.gui.leftHeight = occupied + 12; mc.gui.rightHeight = occupied + 12; }
    }
    @EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void layers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.AIR_LEVEL, VestigeMainMod.location("mana"), ManaHud::draw);
        }
    }
}
