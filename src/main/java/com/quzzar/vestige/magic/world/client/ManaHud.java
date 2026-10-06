package com.quzzar.vestige.magic.world.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** A quiet native HUD: one narrow violet gauge between XP and the hotbar while mana is missing. */
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
        // Vanilla XP ends at height-25 and the hotbar begins at height-22.
        int width = 100, x = graphics.guiWidth() / 2 - width / 2, y = graphics.guiHeight() - 24;
        graphics.fill(x - 1, y, x + width + 1, y + 2, 0xff21172f);
        graphics.fill(x, y, x + width, y + 2, 0xff453458);
        int filled = Math.clamp((int) Math.floor(amount), 0, width);
        graphics.fill(x, y, x + filled, y + 2, 0xffac73e8);
        graphics.fill(x, y, x + filled, y + 1, 0xffdbc0fc);
    }
    @EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void layers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.HOTBAR, VestigeMainMod.location("mana"), ManaHud::draw);
        }
    }
}
