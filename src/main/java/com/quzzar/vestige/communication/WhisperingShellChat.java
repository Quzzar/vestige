package com.quzzar.vestige.communication;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** Narrows vanilla player-chat recipients without replacing filtering, signatures or moderation. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class WhisperingShellChat {
    public static final ResourceKey<ChatType> SHELL_CHAT = ResourceKey.create(Registries.CHAT_TYPE, VestigeMainMod.location("whispering_shell"));
    private WhisperingShellChat() { }
    public record Transmission(boolean active, Set<String> keys) {
        public Transmission { keys = Collections.unmodifiableSet(new TreeSet<>(keys)); }
    }
    // The body survives vanilla's decorated/filtered message copies. Weak identity keys release canceled
    // submissions too; values retain neither players nor servers. UUID separates equal bodies.
    private static final Map<SignedMessageBody, Map<UUID, Transmission>> SUBMISSIONS = new com.google.common.collect.MapMaker().weakKeys().makeMap();
    public static Transmission transmitting(Inventory inventory) {
        var slots = new ArrayList<ItemStack>(inventory.items.subList(0, 9)); slots.addAll(inventory.offhand);
        return transmitting(slots);
    }
    public static Transmission transmitting(List<ItemStack> activeSlots) {
        boolean active = false; var keys = new TreeSet<String>();
        for (var stack : activeSlots) {
            if (!stack.is(ScrollItems.WHISPERING_SHELL.get())) continue;
            active = true; WhisperingShellItem.key(stack).ifPresent(keys::add);
        }
        return new Transmission(active, keys);
    }
    public static Set<String> listening(Inventory inventory) {
        var slots = new ArrayList<ItemStack>(inventory.items); slots.addAll(inventory.offhand);
        return listening(slots);
    }
    public static Set<String> listening(List<ItemStack> directSlots) {
        var keys = new TreeSet<String>();
        directSlots.forEach(stack -> WhisperingShellItem.key(stack).ifPresent(keys::add));
        return Collections.unmodifiableSet(keys);
    }
    public static List<String> shared(Transmission transmission, Set<String> listeners) {
        return transmission.keys().stream().filter(listeners::contains).toList();
    }
    /** Display ordered full keys without treating their compact symbols as identities. */
    public static MutableComponent channelMarks(List<String> keys) {
        var result = Component.empty();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) result.append(Component.literal(" & ").withStyle(ChatFormatting.GRAY));
            result.append(AttunementMark.fromKey(keys.get(i)).symbol());
        }
        return result;
    }
    public static void capture(PlayerChatMessage message, ServerPlayer sender) {
        SUBMISSIONS.computeIfAbsent(message.signedBody(), ignored -> new HashMap<>())
                .put(sender.getUUID(), transmitting(sender.getInventory()));
    }
    public static Transmission transmission(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound type) {
        if (sender == null || !type.chatType().is(ChatType.CHAT) || !message.sender().equals(sender.getUUID())) return new Transmission(false, Set.of());
        var captured = SUBMISSIONS.get(message.signedBody());
        return captured != null && captured.containsKey(sender.getUUID()) ? captured.get(sender.getUUID()) : transmitting(sender.getInventory());
    }
    private static List<String> sharedWith(Transmission transmission, ServerPlayer sender, ServerPlayer recipient) {
        return recipient == sender ? List.copyOf(transmission.keys()) : shared(transmission, listening(recipient.getInventory()));
    }
    public static boolean eligible(PlayerChatMessage message, ServerPlayer sender, ChatType.Bound type, ServerPlayer recipient) {
        var transmission = transmission(message, sender, type);
        return !transmission.active() || !sharedWith(transmission, sender, recipient).isEmpty();
    }
    public static void deliver(ServerPlayer recipient, OutgoingChatMessage outgoing, boolean filtered, ChatType.Bound type,
                               PlayerChatMessage message, ServerPlayer sender) {
        var transmission = transmission(message, sender, type);
        if (!transmission.active()) { recipient.sendChatMessage(outgoing, filtered, type); return; }
        var keys = sharedWith(transmission, sender, recipient);
        if (keys.isEmpty()) return;
        var name = channelMarks(keys).append(Component.literal(" > ").withStyle(ChatFormatting.GRAY)).append(type.name());
        var shellType = recipient.registryAccess().registryOrThrow(Registries.CHAT_TYPE).getHolderOrThrow(SHELL_CHAT);
        recipient.sendChatMessage(outgoing, filtered, new ChatType.Bound(shellType, name, type.targetName()));
        if (recipient.getChatVisibility() == ChatVisiblity.FULL && !message.filter(filtered).isFullyFiltered()
                && net.neoforged.neoforge.network.registration.NetworkRegistry.hasChannel(recipient.connection, ShellCuePayload.TYPE.id()))
            PacketDistributor.sendToPlayer(recipient, new ShellCuePayload(recipient == sender, keys));
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { SUBMISSIONS.clear(); }
}
