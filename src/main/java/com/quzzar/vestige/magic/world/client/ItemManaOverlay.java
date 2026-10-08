package com.quzzar.vestige.magic.world.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ManaItemCosts;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.magic.presentation.ManaReadiness;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import java.util.*;

/** Vanilla cooldown shade represents the missing fraction of this stack's next mana payment. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class ItemManaOverlay {
    private static double amount = NativeMana.MAX;
    private static Map<UUID, Double> costs = Map.of();
    private static Map<ResourceLocation, com.quzzar.vestige.magic.world.ItemRecoveryPayload.Progress> recoveries = Map.of();
    private static long recoveryReceived;
    private ItemManaOverlay() { }
    public static void acceptMana(double value) { amount = value; }
    public static double amount() { return amount; }
    public static void acceptCosts(Map<UUID, Double> values) { costs = Map.copyOf(values); }
    public static void acceptRecovery(Map<ResourceLocation, com.quzzar.vestige.magic.world.ItemRecoveryPayload.Progress> values) {
        recoveries = Map.copyOf(values);
        recoveryReceived = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
    }
    public static float recovery(ItemStack stack) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return 0;
        return ManaItemCosts.recoveryAbility(stack).map(recoveries::get).map(value -> Mth.clamp(
                (float) (value.remaining() - (mc.level.getGameTime() - recoveryReceived) - mc.getTimer().getGameTimeDeltaPartialTick(true)) / value.total(), 0, 1)).orElse(0f);
    }
    public static float shortage(ItemStack stack) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isCreative() || player.isSpectator()) return 0;
        return ManaItemCosts.source(stack).map(source -> ManaReadiness.shortage(amount, costs.getOrDefault(source.key(), 0d))).orElse(0f);
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { amount = NativeMana.MAX; costs = Map.of(); recoveries = Map.of(); }
    private static boolean draw(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        float missing = shortage(stack);
        var mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        float vanilla = mc.player.getCooldowns().getCooldownPercent(stack.getItem(), mc.getTimer().getGameTimeDeltaPartialTick(true));
        float recovery = recovery(stack);
        // Vanilla is already drawn. Extend that recovery coverage if the server ability lasts longer.
        int top = Mth.floor(16 * (1 - recovery)), bottom = Mth.floor(16 * (1 - vanilla));
        if (recovery > vanilla && bottom > top) graphics.fill(RenderType.guiOverlay(), x, y + top, x + 16, y + bottom, Integer.MAX_VALUE);
        // The independently accepted mana layer compounds with recovery where their vanilla-white fills overlap.
        if (missing > 0) graphics.fill(RenderType.guiOverlay(), x, y + Mth.floor(16 * (1 - missing)), x + 16, y + 16, Integer.MAX_VALUE);
        return false;
    }
    @EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void decorations(RegisterItemDecorationsEvent event) {
            for (var item : List.of(ScrollItems.SCROLL, ScrollItems.WAND, ScrollItems.STAFF, ScrollItems.HOMEBOUND_EYE))
                event.register(item, ItemManaOverlay::draw);
            event.register(com.quzzar.vestige.equipment.MagicEquipment.WAYFARER, ItemManaOverlay::draw);
            event.register(com.quzzar.vestige.equipment.MagicEquipment.WARDWEAVE, ItemManaOverlay::draw);
        }
    }
}
