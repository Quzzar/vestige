package com.quzzar.vestige.magic.definition;

import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TraitProfileTest {
    private static final ResourceLocation FIRE = ResourceLocation.fromNamespaceAndPath("vestige", "fire");
    private static final ResourceLocation EVOCATION = ResourceLocation.fromNamespaceAndPath("vestige", "evocation");

    @Test
    void resolvesFlatTraitModifiersWithoutMutatingTheBaseProfile() {
        TraitProfile base = new TraitProfile(Map.of(FIRE, 4.0, EVOCATION, 2.0));

        TraitProfile resolved = base.resolve(List.of(
                new TraitModifier(FIRE, TraitModifier.Operation.MULTIPLY, 2.0),
                new TraitModifier(EVOCATION, TraitModifier.Operation.ADD, 1.0)
        ));

        assertEquals(4.0, base.rating(FIRE));
        assertEquals(8.0, resolved.rating(FIRE));
        assertEquals(3.0, resolved.rating(EVOCATION));
    }

    @Test
    void resolvesEffectValuesFromTheResolvedTraitProfile() {
        TraitProfile traits = new TraitProfile(Map.of(FIRE, 8.0));
        SpellValue damage = new SpellValue.Product(List.of(
                new SpellValue.Trait(FIRE),
                new SpellValue.Constant(4.0)
        ));

        assertEquals(32.0, damage.resolve(traits));
    }
}
