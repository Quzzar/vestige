package com.quzzar.vestige.communication.mixin;

import com.quzzar.vestige.communication.WhisperingShellChat;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Freeze the intended route before the asynchronous filter: moving a Shell cannot leak queued speech. */
@Mixin(value = ServerGamePacketListenerImpl.class, remap = false)
abstract class ChatSubmissionMixin {
    @Shadow public ServerPlayer player;
    @Inject(method = "getSignedMessage", at = @At("RETURN"), remap = false)
    private void vestige$capture(ServerboundChatPacket packet, LastSeenMessages seen, CallbackInfoReturnable<PlayerChatMessage> callback) {
        WhisperingShellChat.capture(callback.getReturnValue(), player);
    }
}
