package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** One shared component recipe; only the String offering's own socket selects its output. */
public final class MagicalThreadRecipe {
    public enum Type {
        ENSORCELLED("ensorcelled_thread", "diamond_block"),
        CALLOUS("callous_thread", "iron_block"),
        SMOLDERING("smoldering_thread", "gold_block"),
        LACED("laced_thread", "emerald_block"),
        CONSECRATED("consecrated_thread", "glowstone");

        private final ResourceLocation id;
        private final ResourceLocation material;
        Type(String path, String material) {
            this.id = VestigeMainMod.location(path);
            this.material = ResourceLocation.withDefaultNamespace(material);
        }
        public ResourceLocation id() { return id; }
        public ResourceLocation material() { return material; }
        public Item item() {
            return switch (this) {
                case ENSORCELLED -> ScrollItems.ENSORCELLED_THREAD.get();
                case CALLOUS -> ScrollItems.CALLOUS_THREAD.get();
                case SMOLDERING -> ScrollItems.SMOLDERING_THREAD.get();
                case LACED -> ScrollItems.LACED_THREAD.get();
                case CONSECRATED -> ScrollItems.CONSECRATED_THREAD.get();
            };
        }
    }
    private MagicalThreadRecipe() { }
    public static List<Type> types() { return List.of(Type.values()); }
    public static List<Item> ingredients() { return List.of(Items.STRING, Items.AMETHYST_SHARD, Items.HONEYCOMB); }
    public static Optional<Type> selector(ItemStack material) {
        return types().stream().filter(type -> material.is(BuiltInRegistries.ITEM.get(type.material()))).findFirst();
    }
    public static boolean matches(List<ItemStack> seats) {
        if (seats.size() != 8) return false;
        var remaining = new ArrayList<>(ingredients());
        for (int i = 0; i < 8; i++) {
            var offering = seats.get(i);
            if (offering.isEmpty()) continue;
            if ((i & 1) != 0 || offering.getCount() != 1 || !remaining.remove(offering.getItem())) return false;
        }
        return remaining.isEmpty();
    }
    public static Optional<ItemStack> result(RitualInputs inputs) {
        if (inputs.geometry().slots() != 4 || inputs.nodes().size() != 4
                || inputs.nodes().stream().anyMatch(n -> n.seat() < 0 || n.seat() >= 8 || (n.seat() & 1) != 0)
                || inputs.nodes().stream().map(RitualInputs.Node::seat).distinct().count() != 4) return Optional.empty();
        var seats = new ArrayList<>(java.util.Collections.nCopies(8, ItemStack.EMPTY));
        inputs.nodes().forEach(n -> seats.set(n.seat(), n.offering()));
        if (!matches(seats)) return Optional.empty();
        var string = inputs.nodes().stream().filter(n -> n.offering().is(Items.STRING)).findFirst().orElseThrow();
        return selector(string.material()).map(type -> new ItemStack(type.item()));
    }
}
