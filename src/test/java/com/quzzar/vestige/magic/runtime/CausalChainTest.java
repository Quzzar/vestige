package com.quzzar.vestige.magic.runtime;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CausalChainTest {
    private static final CausalChain.ActivationKey A = activation("spell_a");
    private static final CausalChain.ActivationKey B = activation("spell_b");
    private static final CausalChain.ActivationKey C = activation("spell_c");

    @Test
    void allowsThreeCycleTraversalsAndRejectsTheFourth() {
        CausalChain chain = CausalChain.start(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        for (int traversal = 0; traversal < 3; traversal++) {
            chain = chain.enter(A).orElseThrow();
            chain = chain.enter(B).orElseThrow();
            chain = chain.enter(C).orElseThrow();
        }

        assertTrue(chain.enter(A).isEmpty());
        assertEquals(9, chain.activations().size());
    }

    @Test
    void siblingBranchesTrackTheirCyclesIndependently() {
        CausalChain parent = CausalChain.start(UUID.fromString("00000000-0000-0000-0000-000000000002"));

        CausalChain left = parent.enter(A).orElseThrow();
        CausalChain right = parent.enter(A).orElseThrow();

        assertEquals(parent.rootId(), left.rootId());
        assertEquals(parent.rootId(), right.rootId());
        assertEquals(1, left.activations().size());
        assertEquals(1, right.activations().size());
        assertTrue(parent.activations().isEmpty());
    }

    private static CausalChain.ActivationKey activation(String spellPath) {
        return new CausalChain.ActivationKey(
                ResourceLocation.fromNamespaceAndPath("vestige", spellPath),
                ResourceLocation.fromNamespaceAndPath("vestige", "primary")
        );
    }
}
