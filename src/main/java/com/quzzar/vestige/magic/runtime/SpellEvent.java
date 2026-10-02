package com.quzzar.vestige.magic.runtime;

import net.minecraft.resources.ResourceLocation;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** A server-side event; mutable outcomes exist only before their commit. */
public record SpellEvent(ResourceLocation type, UUID actor, Optional<SpellSubject> target,
                         Optional<PendingOutcome> pending, CausalChain cause, java.util.Map<ResourceLocation, com.quzzar.vestige.magic.condition.ConditionValue> facts) {
    public SpellEvent(ResourceLocation type, UUID actor, Optional<SpellSubject> target, Optional<PendingOutcome> pending, CausalChain cause) {
        this(type, actor, target, pending, cause, java.util.Map.of());
    }
    public SpellEvent {
        Objects.requireNonNull(type); Objects.requireNonNull(actor); Objects.requireNonNull(target);
        Objects.requireNonNull(pending); Objects.requireNonNull(cause);
        facts = java.util.Map.copyOf(facts);
    }

    public static SpellEvent of(ResourceLocation type, UUID actor, SpellSubject target) {
        return new SpellEvent(type, actor, Optional.ofNullable(target), Optional.empty(), CausalChain.start());
    }

    /** A numerical calculating-phase outcome that becomes immutable on commit. */
    public static final class PendingOutcome {
        private double amount;
        private boolean committed;
        public PendingOutcome(double amount) {
            if (!Double.isFinite(amount) || amount < 0) throw new IllegalArgumentException("Invalid pending amount");
            this.amount = amount;
        }
        public double amount() { return amount; }
        public void reduce(double reduction) {
            if (committed) throw new IllegalStateException("Outcome is already committed");
            if (!Double.isFinite(reduction) || reduction < 0) throw new IllegalArgumentException("Invalid reduction");
            amount = Math.max(0, amount - reduction);
        }
        public double commit() { committed = true; return amount; }
    }
}
