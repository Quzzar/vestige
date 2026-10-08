package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.definition.TraitProfile;
import com.quzzar.vestige.magic.runtime.ForfeitPolicy;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.DoubleSupplier;

/** Independent input rolls, center first then ascending active seats. First trigger survives. */
public final class RitualVolatility {
    public static final int CENTER = -1;
    private RitualVolatility() { }
    public static TraitProfile traits(ItemStack item) {
        if (item.isEmpty()) return TraitProfile.empty();
        if (item.getItem() instanceof RitualTraitSource source) return source.ritualTraits(item);
        var scroll=ScrollItems.scroll(item).orElse(null);
        if (scroll!=null) {
            var spell=NativeMagic.spells().spells().get(scroll.spell());
            if (spell!=null) return spell.traits().resolve(scroll.modifiers());
        }
        // The staff owns one fixed affinity; contained source scrolls do not leak into its traits.
        var staff=StaffData.binding(item).orElse(null);
        if (staff!=null) return new TraitProfile(java.util.Map.of(staff.affinity(),1.0));
        return TraitProfile.empty();
    }
    public static OptionalInt culprit(ItemStack reference, RitualInputs inputs, List<Integer> used, double referenceChance, DoubleSupplier random) {
        if (!reference.isEmpty() && referenceChance>0 && random.getAsDouble()<referenceChance) return OptionalInt.of(CENTER);
        for (int seat:used.stream().distinct().sorted().toList()) {
            var item=inputs.nodes().stream().filter(n -> n.seat()==seat).findFirst().orElseThrow().offering();
            double chance=ForfeitPolicy.DEFAULT.chance(traits(item),true);
            if (chance>0 && random.getAsDouble()<chance) return OptionalInt.of(seat);
        }
        return OptionalInt.empty();
    }
}
