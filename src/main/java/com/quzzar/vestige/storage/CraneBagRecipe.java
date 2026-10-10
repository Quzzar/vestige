package com.quzzar.vestige.storage;

import com.quzzar.vestige.apparatus.*;
import net.minecraft.world.item.*;
import java.util.*;

/** Ordered inner-layer construction; the source shard's full key is inherited without re-attuning it. */
public final class CraneBagRecipe {
    private CraneBagRecipe() { }
    public static List<Item> ingredients() { return List.of(ScrollItems.ATTUNEMENT_SHARD.get(),Items.ENDER_EYE,Items.FEATHER,Items.LEATHER); }
    public static Optional<ItemStack> result(List<ItemStack> seats) {
        if(!FourSlotPattern.matches(seats,ingredients()))return Optional.empty();
        var shard=java.util.stream.IntStream.range(0,4).mapToObj(i->seats.get(i*2))
                .filter(i->i.is(ScrollItems.ATTUNEMENT_SHARD.get())).findFirst().orElseThrow();
        return CraneBagItem.fromShard(shard);
    }
}
