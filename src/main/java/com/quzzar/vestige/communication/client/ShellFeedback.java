package com.quzzar.vestige.communication.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.communication.ShellCuePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

/** One quiet hollow note per conversation; channel symbols stay in chat and item corners. */
public final class ShellFeedback {
    private ShellFeedback() { }
    public static void accept(ShellCuePayload cue) {
        var mc = Minecraft.getInstance(); if (mc.player == null || mc.level == null) return;
        mc.player.playSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, cue.sending() ? .22f : .13f, cue.sending() ? .65f : .8f);
    }
    @EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void decorations(RegisterItemDecorationsEvent event) {
            event.register(ScrollItems.WHISPERING_SHELL.get(), (graphics, font, stack, x, y) -> {
                WhisperingShellItem.key(stack).ifPresent(key -> {
                    graphics.pose().pushPose(); graphics.pose().translate(0, 0, 210);
                    graphics.drawString(font, AttunementMark.fromKey(key).symbol(), x, y, 0xffffffff, true);
                    graphics.pose().popPose();
                }); return false;
            });
        }
    }
}
