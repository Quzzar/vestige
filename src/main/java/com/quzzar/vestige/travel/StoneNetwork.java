package com.quzzar.vestige.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/** Persistent endpoint directory. An attunement selects peers, never a privileged hub. */
public final class StoneNetwork {
    public record Node(UUID id, String key, ResourceLocation dimension, BlockPos position, String name) {
        public Node {
            Objects.requireNonNull(id); Objects.requireNonNull(dimension);
            position = position.immutable();
            if (!validKey(key) || name == null || name.isBlank() || name.length() > 64)
                throw new IllegalArgumentException("Invalid standing stone endpoint");
        }
    }
    private final Map<UUID, Node> nodes = new LinkedHashMap<>();
    public static boolean validKey(String key) { return key != null && key.matches("[0-9a-f]{64}"); }
    public boolean put(Node node) {
        // A block replacement at the same address must not leave an old endpoint behind.
        boolean removed = nodes.values().removeIf(old -> !old.id().equals(node.id())
                && old.dimension().equals(node.dimension()) && old.position().equals(node.position()));
        return !node.equals(nodes.put(node.id(), node)) || removed;
    }
    public boolean remove(UUID id) { return nodes.remove(id) != null; }
    public Optional<Node> get(UUID id) { return Optional.ofNullable(nodes.get(id)); }
    public List<Node> all() { return List.copyOf(nodes.values()); }
    public List<Node> peers(String key) {
        return nodes.values().stream().filter(node -> node.key().equals(key))
                .sorted(Comparator.comparing(Node::name).thenComparing(node -> node.id().toString())).toList();
    }
}
