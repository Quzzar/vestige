package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.definition.SpellDefinition;
import net.minecraft.resources.ResourceLocation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Exact intersection and slot-counted average weighting, independently of rarity or combat power. */
public final class FragmentDiscovery {
    private FragmentDiscovery() { }
    public record Candidate(SpellDefinition spell, double weight) { }
    public static List<Candidate> pool(Collection<SpellDefinition> spells, List<ResourceLocation> fragments) {
        if (fragments.size() < 4 || fragments.size() > 8) return List.of();
        return spells.stream().filter(s -> fragments.stream().allMatch(t -> s.traits().rating(t) > 0))
                .sorted(java.util.Comparator.comparing(SpellDefinition::id))
                .map(s -> new Candidate(s, fragments.stream().mapToDouble(t -> s.traits().rating(t) / fragments.size()).sum())).toList();
    }
    /** One draw is supplied only after a real activation has validated its pool. */
    public static Optional<SpellDefinition> pick(List<Candidate> pool, double draw) {
        if (!Double.isFinite(draw) || draw < 0 || draw >= 1) throw new IllegalArgumentException("Draw must be in [0,1)");
        if (pool.isEmpty()) return Optional.empty();
        double largest = pool.stream().mapToDouble(Candidate::weight).max().orElseThrow();
        double total = pool.stream().mapToDouble(c -> c.weight / largest).sum();
        double remaining = draw * total;
        for (Candidate c : pool) { remaining -= c.weight / largest; if (remaining < 0) return Optional.of(c.spell); }
        return Optional.of(pool.getLast().spell);
    }
}
