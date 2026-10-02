package com.quzzar.vestige.magic.runtime;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The immutable activation history for one branch of a causal chain.
 * A branch may visit the same spell trigger three times; a fourth visit is rejected.
 */
public final class CausalChain {
    public static final int MAX_VISITS_PER_ACTIVATION = 3;

    private final UUID rootId;
    private final List<ActivationKey> activations;

    private CausalChain(UUID rootId, List<ActivationKey> activations) {
        this.rootId = Objects.requireNonNull(rootId, "rootId");
        this.activations = List.copyOf(activations);
    }

    /**
     * Starts an empty causal chain with a generated root identifier.
     */
    public static CausalChain start() {
        return start(UUID.randomUUID());
    }

    /**
     * Starts an empty causal chain with a known root identifier.
     */
    public static CausalChain start(UUID rootId) {
        return new CausalChain(rootId, List.of());
    }

    /**
     * Returns a new branch containing the activation, or an empty result when it would be the fourth visit.
     * Calling this method independently on the same parent creates independent sibling branches.
     */
    public Optional<CausalChain> enter(ActivationKey activation) {
        Objects.requireNonNull(activation, "activation");
        long visits = activations.stream().filter(activation::equals).count();
        if (visits >= MAX_VISITS_PER_ACTIVATION) {
            return Optional.empty();
        }

        List<ActivationKey> nextActivations = new ArrayList<>(activations);
        nextActivations.add(activation);
        return Optional.of(new CausalChain(rootId, nextActivations));
    }

    /**
     * Returns the root event identifier shared by every branch in this chain.
     */
    public UUID rootId() {
        return rootId;
    }

    /**
     * Returns this branch's immutable activation trace.
     */
    public List<ActivationKey> activations() {
        return activations;
    }

    /**
     * Identifies one spell trigger within a causal chain.
     *
     * @param spell the spell identifier
     * @param trigger the trigger identifier within that spell
     */
    public record ActivationKey(ResourceLocation spell, ResourceLocation trigger) {
        public ActivationKey {
            Objects.requireNonNull(spell, "spell");
            Objects.requireNonNull(trigger, "trigger");
        }
    }
}
