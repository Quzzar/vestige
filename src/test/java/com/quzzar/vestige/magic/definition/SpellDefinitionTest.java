package com.quzzar.vestige.magic.definition;

import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellDefinitionTest {
    private static final ResourceLocation FIRE = id("fire");
    private static final ResourceLocation EVOCATION = id("evocation");
    private static final ResourceLocation AMPLIFY = id("amplify");

    @Test
    void representsTheFireBoltSpellBones() {
        SpellValue damage = new SpellValue.Product(List.of(
                new SpellValue.Constant(8.0),
                new SpellValue.Trait(AMPLIFY)
        ));
        SpellDefinition fireBolt = new SpellDefinition(
                id("fire_bolt"),
                Set.of(Tradition.ARCANE, Tradition.PRIMAL),
                new TraitProfile(Map.of(FIRE, 4.0, EVOCATION, 2.0, AMPLIFY, 1.0)),
                List.of(
                        new SpellCost.Mana(15),
                        new SpellCost.Material(
                                ResourceLocation.withDefaultNamespace("flint_and_steel"),
                                SpellCost.Material.Operation.DAMAGE,
                                3
                        ),
                        new SpellCost.Time(16)
                ),
                List.of(new SpellTrigger(id("primary"), id("interact"), List.of())),
                List.of(new TestEffect(damage, List.of()))
        );

        assertEquals(Set.of(Tradition.ARCANE, Tradition.PRIMAL), fireBolt.traditions());
        assertEquals(4.0, fireBolt.traits().rating(FIRE));
        assertEquals(3, fireBolt.costs().size());
        assertInstanceOf(SpellCost.Material.class, fireBolt.costs().get(1));
        assertTrue(fireBolt.triggers().getFirst().conditions().isEmpty());
        assertEquals(8.0, ((TestEffect) fireBolt.effects().getFirst()).damage().resolve(fireBolt.traits()));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("vestige", path);
    }

    private record TestEffect(SpellValue damage, List<SpellCondition> conditions) implements SpellEffect {
    }
}
