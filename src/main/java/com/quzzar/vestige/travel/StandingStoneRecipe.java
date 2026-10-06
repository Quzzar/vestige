package com.quzzar.vestige.travel;

import com.quzzar.vestige.apparatus.AttunementShardItem;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import com.quzzar.vestige.apparatus.ScrollItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import java.util.*;

/** Four-offering device ritual; imbuements never rehash the already-created attunement. */
public final class StandingStoneRecipe {
    private StandingStoneRecipe() { }
    public static List<Item> ingredients() { return List.of(ScrollItems.ATTUNEMENT_SHARD.get(), Items.ENDER_PEARL, Items.STONE_BRICKS, Items.STONE_BRICKS); }
    public static Optional<ItemStack> result(List<ItemStack> items) {
        ItemStack shard = ItemStack.EMPTY;
        int pearls = 0;
        List<Item> masonry = new ArrayList<>();
        for (ItemStack item : items) {
            if (item.isEmpty()) continue;
            if (item.is(ScrollItems.ATTUNEMENT_SHARD.get())) {
                if (!shard.isEmpty() || AttunementShardItem.signature(item).isEmpty()) return Optional.empty();
                shard = item;
            } else if (item.is(Items.ENDER_PEARL)) {
                if (++pearls > 1) return Optional.empty();
            } else {
                masonry.add(item.getItem());
            }
        }
        if (shard.isEmpty() || pearls != 1 || masonry.size() != 2 || masonry.get(0) != masonry.get(1)) return Optional.empty();
        Item body = masonry.getFirst();
        // The accepted original chiseled-brick composition is an alternate for the Stone Bricks finish.
        Optional<ApparatusMaterials> material = body == Items.CHISELED_STONE_BRICKS ? Optional.of(ApparatusMaterials.STONE_BRICKS)
                : Arrays.stream(ApparatusMaterials.values()).filter(value -> BuiltInRegistries.ITEM.get(value.body()) == body).findFirst();
        ItemStack attuned = shard;
        return material.map(value -> StandingStones.fromShard(attuned, value));
    }
}
