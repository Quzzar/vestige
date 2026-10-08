package com.quzzar.vestige.apparatus;

import net.minecraft.world.item.*;
import java.util.List;
import java.util.Optional;

/** Three clockwise inner-layer offerings; the fourth active surface stays empty. */
public final class WhisperingShellRecipe {
    private WhisperingShellRecipe() { }
    public static List<Item> ingredients() { return List.of(ScrollItems.ATTUNEMENT_SHARD.get(), Items.SCULK_SENSOR, Items.NAUTILUS_SHELL); }
    public static Optional<ItemStack> result(List<ItemStack> seats) {
        if (!FourSlotPattern.matches(seats, ingredients())) return Optional.empty();
        var shard = java.util.stream.IntStream.range(0, 4).mapToObj(i -> seats.get(i * 2))
                .filter(i -> i.is(ScrollItems.ATTUNEMENT_SHARD.get())).findFirst().orElseThrow();
        return AttunementShardItem.signature(shard).map(ignored -> WhisperingShellItem.bound(shard));
    }
}
