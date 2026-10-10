package com.quzzar.vestige.travel;

import com.quzzar.vestige.apparatus.AttunementShardItem;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.apparatus.FourSlotPattern;
import com.quzzar.vestige.apparatus.RitualInputs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import java.util.*;

/** Four-offering device ritual; imbuements never rehash the already-created attunement. */
public final class StandingStoneRecipe {
    private StandingStoneRecipe() { }
    public static List<Item> ingredients() { return List.of(ScrollItems.ATTUNEMENT_SHARD.get(), Items.STONE_BRICKS, Items.ENDER_PEARL, Items.STONE_BRICKS); }
    public record Variant(ApparatusMaterials material, StandingStonePayment payment, Item body) {
        public String id() { return material.id() + "/" + payment.id() + (body == Items.CHISELED_STONE_BRICKS ? "/chiseled" : ""); }
        public List<Item> ingredients() { return List.of(ScrollItems.ATTUNEMENT_SHARD.get(), body, Items.ENDER_PEARL, body); }
        public ItemStack preview() { return StandingStones.preview(material, payment); }
    }
    public static List<Variant> variants() {
        var variants = new ArrayList<Variant>();
        for (var material : ApparatusMaterials.values()) for (var payment : StandingStonePayment.values())
            variants.add(new Variant(material, payment, BuiltInRegistries.ITEM.get(material.body())));
        for (var payment : StandingStonePayment.values())
            variants.add(new Variant(ApparatusMaterials.STONE_BRICKS, payment, Items.CHISELED_STONE_BRICKS));
        return List.copyOf(variants);
    }
    public static Optional<ItemStack> result(List<ItemStack> items) {
        if (items.size() != 8) return Optional.empty();
        var inner = java.util.stream.IntStream.range(0, 4).mapToObj(i -> items.get(i * 2)).toList();
        var shard = inner.stream().filter(i -> i.is(ScrollItems.ATTUNEMENT_SHARD.get())).findFirst().orElse(ItemStack.EMPTY);
        if (AttunementShardItem.signature(shard).isEmpty()) return Optional.empty();
        Item body = inner.stream().filter(i -> !i.isEmpty() && !i.is(ScrollItems.ATTUNEMENT_SHARD.get()) && !i.is(Items.ENDER_PEARL))
                .map(ItemStack::getItem).findFirst().orElse(Items.AIR);
        if (!FourSlotPattern.matches(items, List.of(ScrollItems.ATTUNEMENT_SHARD.get(), body, Items.ENDER_PEARL, body))) return Optional.empty();
        // The accepted original chiseled-brick composition is an alternate for the Stone Bricks finish.
        Optional<ApparatusMaterials> material = body == Items.CHISELED_STONE_BRICKS ? Optional.of(ApparatusMaterials.STONE_BRICKS)
                : Arrays.stream(ApparatusMaterials.values()).filter(value -> BuiltInRegistries.ITEM.get(value.body()) == body).findFirst();
        ItemStack attuned = shard;
        return material.map(value -> StandingStones.fromShard(attuned, value));
    }
    public static Optional<ItemStack> result(RitualInputs inputs) {
        if (inputs.geometry().slots() != 4) return Optional.empty();
        var seats = new ArrayList<ItemStack>(Collections.nCopies(8, ItemStack.EMPTY));
        for (var node : inputs.nodes()) seats.set(node.seat(), node.offering());
        var ordinary = result(seats);
        if (ordinary.isEmpty()) return Optional.empty();
        var pearl = inputs.nodes().stream().filter(n -> n.offering().is(Items.ENDER_PEARL)).findFirst().orElseThrow();
        return StandingStonePayment.fromMaterial(pearl.material()).map(route -> StandingStones.bound(
                StandingStones.key(ordinary.get()).orElseThrow(), StandingStones.material(ordinary.get()).orElseThrow(), route));
    }
}
