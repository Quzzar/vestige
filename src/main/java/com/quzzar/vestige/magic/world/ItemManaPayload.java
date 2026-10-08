package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.*;

/** Private, bounded mana prices for visible item identities, including exact shaped and equipped variants. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record ItemManaPayload(Map<UUID, Double> costs) implements CustomPacketPayload {
    public static final int MAX_ITEMS = 256;
    public static final Type<ItemManaPayload> TYPE = new Type<>(VestigeMainMod.location("item_mana"));
    public ItemManaPayload {
        costs = Map.copyOf(costs);
        if (costs.size() > MAX_ITEMS || costs.values().stream().anyMatch(v -> !Double.isFinite(v) || v < 0))
            throw new IllegalArgumentException("Invalid item mana prices");
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemManaPayload> CODEC = new StreamCodec<>() {
        public ItemManaPayload decode(RegistryFriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            if (count < 0 || count > MAX_ITEMS) throw new IllegalArgumentException("Too many item mana prices");
            var entries = new HashMap<UUID, Double>();
            for (int i = 0; i < count; i++) if (entries.put(buffer.readUUID(), buffer.readDouble()) != null)
                throw new IllegalArgumentException("Duplicate item mana price");
            return new ItemManaPayload(entries);
        }
        public void encode(RegistryFriendlyByteBuf buffer, ItemManaPayload payload) {
            buffer.writeVarInt(payload.costs.size());
            payload.costs.forEach((key, cost) -> { buffer.writeUUID(key); buffer.writeDouble(cost); });
        }
    };
    @Override public Type<ItemManaPayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, (payload, context) ->
                com.quzzar.vestige.magic.world.client.ItemManaOverlay.acceptCosts(payload.costs()));
    }
}
