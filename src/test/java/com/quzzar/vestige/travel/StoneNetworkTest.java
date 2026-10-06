package com.quzzar.vestige.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StoneNetworkTest {
    private static final ResourceLocation WORLD = ResourceLocation.withDefaultNamespace("overworld");
    private static StoneNetwork.Node node(String key, int x) { return new StoneNetwork.Node(UUID.randomUUID(), key, WORLD, new BlockPos(x, 64, 0), "Stone " + x); }
    @Test void matchingFullKeysLinkAllPeersWithoutAHierarchy() {
        StoneNetwork network = new StoneNetwork();
        var first = node("a".repeat(64), 0); var second = node("a".repeat(64), 4); var foreign = node("a".repeat(63) + "b", 8);
        network.put(first); network.put(second); network.put(foreign);
        assertEquals(Set.of(first, second), new HashSet<>(network.peers(first.key())));
        assertTrue(network.peers("unattuned").isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> network.peers(first.key()).clear());
    }
    @Test void replacingAnAddressAndRemovingAnEndpointCannotLeaveDuplicateDestinations() {
        StoneNetwork network = new StoneNetwork(); var first = node("a".repeat(64), 0); var replacement = node("b".repeat(64), 0);
        assertTrue(network.put(first)); assertFalse(network.put(first)); assertTrue(network.put(replacement));
        assertTrue(network.get(first.id()).isEmpty()); assertEquals(List.of(replacement), network.all());
        assertTrue(network.remove(replacement.id())); assertFalse(network.remove(replacement.id()));
    }
    @Test void malformedKeysAreNeverEndpoints() {
        for (String value : List.of("", "a".repeat(63), "A".repeat(64), "x".repeat(64)))
            assertThrows(IllegalArgumentException.class, () -> node(value, 0));
    }
}
