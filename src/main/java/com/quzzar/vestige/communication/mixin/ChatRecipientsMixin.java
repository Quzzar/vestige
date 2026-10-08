package com.quzzar.vestige.communication.mixin;

import com.quzzar.vestige.communication.WhisperingShellChat;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.function.Predicate;

/** The pinned vanilla broadcast still owns logging, outgoing signed messages and filter notices. */
@Mixin(value = PlayerList.class, remap = false)
abstract class ChatRecipientsMixin {
    private static final String BROADCAST = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V";
    @Redirect(method = BROADCAST, at = @At(value = "INVOKE", target = "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z"), remap = false)
    private boolean vestige$filter(Predicate<ServerPlayer> predicate, Object recipient, PlayerChatMessage message,
                                    Predicate<ServerPlayer> original, ServerPlayer sender, ChatType.Bound type) {
        return WhisperingShellChat.eligible(message, sender, type, (ServerPlayer) recipient) && predicate.test((ServerPlayer) recipient);
    }
    @Redirect(method = BROADCAST, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendChatMessage(Lnet/minecraft/network/chat/OutgoingChatMessage;ZLnet/minecraft/network/chat/ChatType$Bound;)V"), remap = false)
    private void vestige$recipient(ServerPlayer recipient, OutgoingChatMessage outgoing, boolean filtered, ChatType.Bound recipientType,
                                   PlayerChatMessage message, Predicate<ServerPlayer> original, ServerPlayer sender, ChatType.Bound type) {
        WhisperingShellChat.deliver(recipient, outgoing, filtered, recipientType, message, sender);
    }
}
