package com.quzzar.vestige.travel;

import com.quzzar.vestige.apparatus.AttunementShardItem;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.apparatus.FourSlotPattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import java.util.*;

/** Four-offering device ritual; imbuements never rehash the already-created attunement. */
public final class StandingStoneRecipe {
    private StandingStoneRecipe() { }
    public static List<Item> ingredients() { return List.of(ScrollItems.ATTUNEMENT_SHARD.get(), Items.STONE_BRICKS, Items.ENDER_PEARL, Items.STONE_BRICKS); }
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
}
