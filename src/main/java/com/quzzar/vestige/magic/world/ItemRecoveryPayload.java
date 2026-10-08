package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ManaItemCosts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.*;

/** Private timing of visible abilities; exact actor recovery survives swapping to another variant or copy. */
public record ItemRecoveryPayload(Map<ResourceLocation, Progress> recoveries) implements CustomPacketPayload {
    public static final int MAX_ENTRIES = 256;
    public static final Type<ItemRecoveryPayload> TYPE = new Type<>(VestigeMainMod.location("item_recovery"));
    public record Progress(int remaining, int total) {
        public Progress { if (remaining <= 0 || total < remaining) throw new IllegalArgumentException("Invalid recovery timing"); }
    }
    public ItemRecoveryPayload {
        recoveries = Map.copyOf(recoveries);
        if (recoveries.size() > MAX_ENTRIES) throw new IllegalArgumentException("Too many recovery entries");
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemRecoveryPayload> CODEC = new StreamCodec<>() {
        public ItemRecoveryPayload decode(RegistryFriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            if (count < 0 || count > MAX_ENTRIES) throw new IllegalArgumentException("Too many recovery entries");
            var entries = new HashMap<ResourceLocation, Progress>();
            for (int i = 0; i < count; i++) if (entries.put(buffer.readResourceLocation(), new Progress(buffer.readVarInt(), buffer.readVarInt())) != null)
                throw new IllegalArgumentException("Duplicate recovery entry");
            return new ItemRecoveryPayload(entries);
        }
        public void encode(RegistryFriendlyByteBuf buffer, ItemRecoveryPayload payload) {
            buffer.writeVarInt(payload.recoveries.size());
            payload.recoveries.forEach((key, value) -> { buffer.writeResourceLocation(key); buffer.writeVarInt(value.remaining()); buffer.writeVarInt(value.total()); });
        }
    };
    @Override public Type<ItemRecoveryPayload> type() { return TYPE; }
    public static Map<ResourceLocation, Progress> snapshot(Player player) {
        var abilities = new HashSet<ResourceLocation>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) ManaItemCosts.recoveryAbility(player.getInventory().getItem(i)).ifPresent(abilities::add);
        for (var slot : player.containerMenu.slots) ManaItemCosts.recoveryAbility(slot.getItem()).ifPresent(abilities::add);
        ManaItemCosts.recoveryAbility(player.containerMenu.getCarried()).ifPresent(abilities::add);
        var result = new HashMap<ResourceLocation, Progress>();
        var runtime = NativeMagic.session(player.getServer()).runtime();
        abilities.stream().sorted().limit(MAX_ENTRIES).forEach(id -> runtime.recovery(player.getUUID(), id).ifPresent(value ->
                result.put(id, new Progress(Math.toIntExact(value.remainingTicks()), Math.toIntExact(value.totalTicks())))));
        return Map.copyOf(result);
    }
    @EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
    public static final class Sync {
        @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
            if (event.getEntity() instanceof ServerPlayer player && player.level().getGameTime() % 5 == 0
                    && player.connection != null && NetworkRegistry.hasChannel(player.connection, TYPE.id()))
                PacketDistributor.sendToPlayer(player, new ItemRecoveryPayload(snapshot(player)));
        }
    }
    @EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToClient(TYPE, CODEC, (payload, context) ->
                    com.quzzar.vestige.magic.world.client.ItemManaOverlay.acceptRecovery(payload.recoveries()));
        }
    }
}
