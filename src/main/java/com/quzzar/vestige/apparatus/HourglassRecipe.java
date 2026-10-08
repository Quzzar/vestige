package com.quzzar.vestige.apparatus;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import java.util.*;

/** Ordered Clock → Glass → Echo Shard → Ender Pearl. Whole rotations preserve each offering/socket pair. */
public final class HourglassRecipe {
    private HourglassRecipe() { }
    public static List<Item> ingredients() { return List.of(Items.CLOCK,Items.GLASS,Items.ECHO_SHARD,Items.ENDER_PEARL); }
    public static boolean matches(List<ItemStack> items) { return FourSlotPattern.matches(items,ingredients()); }
    public static Optional<ItemStack> result(RitualInputs inputs) {
        var items=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));inputs.nodes().forEach(n -> items.set(n.seat(),n.offering()));
        if(inputs.geometry().slots()!=4 || !matches(items))return Optional.empty();
        var choices=EnumSet.noneOf(HourglassData.Choice.class);
        for(var node:inputs.nodes()) {
            int offering=ingredients().indexOf(node.offering().getItem());
            int group=offering==0 ? 0 : offering==1 ? 1 : offering==2 ? 2 : -1;
            var material=node.material();if(material.isEmpty())continue;
            var id=BuiltInRegistries.ITEM.getKey(material.getItem());
            var choice=Arrays.stream(HourglassData.Choice.values()).filter(c -> c.group==group && c.material.equals(id)).findFirst();
            if(choice.isPresent())choices.add(choice.get());
            else if(Spellshaping.rules().values().stream().anyMatch(r -> r.pairs().contains(new Spellshaping.Pair(BuiltInRegistries.ITEM.getKey(node.offering().getItem()),id))))return Optional.empty();
        }
        return Optional.of(HourglassData.create(new HourglassData.Variant(choices,inputs.geometry())));
    }
}
