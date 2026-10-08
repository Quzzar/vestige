package com.quzzar.vestige.magic.world;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ItemManaPayloadTest {
    @Test void costsAndClearingRoundTripWithBoundedUniqueFiniteEntries() {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            for (var costs : List.of(Map.of(UUID.randomUUID(), 14d, UUID.randomUUID(), 28d, UUID.randomUUID(), 0d), Map.<UUID, Double>of())) {
                var payload = new ItemManaPayload(costs); ItemManaPayload.CODEC.encode(buffer, payload);
                assertEquals(payload, ItemManaPayload.CODEC.decode(buffer)); assertEquals(0, buffer.readableBytes());
            }
            buffer.writeVarInt(ItemManaPayload.MAX_ITEMS + 1);
            assertThrows(IllegalArgumentException.class, () -> ItemManaPayload.CODEC.decode(buffer)); buffer.clear();
            var key = UUID.randomUUID(); buffer.writeVarInt(2);
            for (int i = 0; i < 2; i++) { buffer.writeUUID(key); buffer.writeDouble(1); }
            assertThrows(IllegalArgumentException.class, () -> ItemManaPayload.CODEC.decode(buffer));
            assertThrows(IllegalArgumentException.class, () -> new ItemManaPayload(Map.of(key, Double.NaN)));
            assertThrows(IllegalArgumentException.class, () -> new ItemManaPayload(Map.of(key, -1d)));
        } finally { buffer.release(); }
    }
    @Test void pooledManaRetainsTheServersExactAffordabilityBoundary() {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            var payload = new ManaPayload(Math.nextDown(30d)); ManaPayload.CODEC.encode(buffer, payload);
            assertEquals(payload, ManaPayload.CODEC.decode(buffer));
            assertThrows(IllegalArgumentException.class, () -> new ManaPayload(Double.POSITIVE_INFINITY));
            assertThrows(IllegalArgumentException.class, () -> new ManaPayload(101));
            var expanded = new ManaPayload(125,125); ManaPayload.CODEC.encode(buffer,expanded);
            assertEquals(expanded,ManaPayload.CODEC.decode(buffer));
            assertThrows(IllegalArgumentException.class, () -> new ManaPayload(126,125));
        } finally { buffer.release(); }
    }
}
