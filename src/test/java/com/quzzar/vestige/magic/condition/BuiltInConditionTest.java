package com.quzzar.vestige.magic.condition;

import com.quzzar.vestige.magic.definition.SpellCondition;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltInConditionTest {
    private static final ResourceLocation DIAMOND_SWORD = ResourceLocation.withDefaultNamespace("diamond_sword");
    private static final ResourceLocation ZOMBIE = ResourceLocation.withDefaultNamespace("zombie");

    @Test
    void containsTheAcceptedBuiltInConditionCatalog() {
        assertEquals(8, SpellConditionTypes.all().size());
    }

    @Test
    void matchesTheNamedDiamondSwordExample() {
        TestContext context = new TestContext()
                .withValue(ConditionPaths.SOURCE_ITEM, new ConditionValue.Identifier(DIAMOND_SWORD))
                .withValue(ConditionPaths.SOURCE_CUSTOM_NAME, new ConditionValue.Text("Bane of Zombies"));
        SpellCondition condition = new BuiltInCondition.All(java.util.List.of(
                BuiltInCondition.Compare.to(
                        ConditionPaths.SOURCE_ITEM,
                        BuiltInCondition.Comparison.EQUAL,
                        new ConditionValue.Identifier(DIAMOND_SWORD)
                ),
                BuiltInCondition.Compare.to(
                        ConditionPaths.SOURCE_CUSTOM_NAME,
                        BuiltInCondition.Comparison.EQUAL,
                        new ConditionValue.Text("Bane of Zombies")
                )
        ));

        assertTrue(condition.matches(context));
    }

    @Test
    void matchesTheSummonedZombieExample() {
        TestContext context = new TestContext()
                .withValue(ConditionPaths.CREATED_ENTITY_TYPE, new ConditionValue.Identifier(ZOMBIE));
        SpellCondition condition = BuiltInCondition.Compare.to(
                ConditionPaths.CREATED_ENTITY_TYPE,
                BuiltInCondition.Comparison.EQUAL,
                new ConditionValue.Identifier(ZOMBIE)
        );

        assertTrue(condition.matches(context));
    }

    @Test
    void matchesForeignSpellClassificationWithoutKnowingTheSpecificSpell() {
        ResourceLocation ironFireSchool = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "fire");
        ResourceLocation ironSpellDamage = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "spell_damage");
        ResourceLocation ironLongCast = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "long");
        ResourceLocation ironSpellbookSource = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "spellbook");
        ResourceLocation fireTrait = id("fire");
        TestContext context = new TestContext()
                .withValue(ConditionPaths.SPELL_PROVIDER, new ConditionValue.Text("irons_spellbooks"))
                .withValue(ConditionPaths.EVENT_PROVIDER, new ConditionValue.Text("irons_spellbooks"))
                .withValue(ConditionPaths.EVENT_ID, new ConditionValue.Identifier(ironSpellDamage))
                .withValue(ConditionPaths.SPELL_SCHOOL, new ConditionValue.Identifier(ironFireSchool))
                .withValue(ConditionPaths.SPELL_LEVEL, new ConditionValue.Decimal(3))
                .withValue(ConditionPaths.SPELL_CAST_TYPE, new ConditionValue.Identifier(ironLongCast))
                .withValue(ConditionPaths.SPELL_CAST_SOURCE, new ConditionValue.Identifier(ironSpellbookSource))
                .withValue(ConditionPaths.trait(fireTrait), new ConditionValue.Decimal(4));
        SpellCondition condition = new BuiltInCondition.All(java.util.List.of(
                BuiltInCondition.Compare.to(
                        ConditionPaths.SPELL_PROVIDER,
                        BuiltInCondition.Comparison.EQUAL,
                        new ConditionValue.Text("irons_spellbooks")
                ),
                BuiltInCondition.Compare.to(
                        ConditionPaths.EVENT_ID,
                        BuiltInCondition.Comparison.EQUAL,
                        new ConditionValue.Identifier(ironSpellDamage)
                ),
                BuiltInCondition.Compare.to(
                        ConditionPaths.SPELL_SCHOOL,
                        BuiltInCondition.Comparison.EQUAL,
                        new ConditionValue.Identifier(ironFireSchool)
                ),
                BuiltInCondition.Compare.to(
                        ConditionPaths.SPELL_LEVEL,
                        BuiltInCondition.Comparison.GREATER_THAN_OR_EQUAL,
                        new ConditionValue.Decimal(2)
                ),
                BuiltInCondition.Compare.to(
                        ConditionPaths.trait(fireTrait),
                        BuiltInCondition.Comparison.GREATER_THAN,
                        new ConditionValue.Decimal(0)
                )
        ));

        assertTrue(condition.matches(context));
    }

    @Test
    void evaluatesBooleanNumericTagPredicateAndChanceConditions() {
        ResourceLocation hostileTag = id("hostile");
        ResourceLocation armoredPredicate = id("armored");
        TestContext context = new TestContext()
                .withValue(ConditionPaths.ACTOR_HEALTH_PERCENT, new ConditionValue.Decimal(0.25))
                .withTag(ConditionPaths.TARGET_ENTITY_TYPE, hostileTag)
                .withPredicate(ConditionPaths.TARGET, armoredPredicate)
                .withRandom(0.2);

        assertTrue(BuiltInCondition.Compare.between(ConditionPaths.ACTOR_HEALTH_PERCENT, 0.2, 0.3).matches(context));
        assertTrue(new BuiltInCondition.Exists(ConditionPaths.ACTOR_HEALTH_PERCENT).matches(context));
        assertTrue(new BuiltInCondition.Tagged(ConditionPaths.TARGET_ENTITY_TYPE, hostileTag).matches(context));
        assertTrue(new BuiltInCondition.Matches(ConditionPaths.TARGET, armoredPredicate).matches(context));
        assertTrue(new BuiltInCondition.Chance(0.25).matches(context));
        assertFalse(new BuiltInCondition.Chance(0.1).matches(context));
        assertTrue(new BuiltInCondition.Not(new BuiltInCondition.Chance(0)).matches(context));
        assertTrue(new BuiltInCondition.Any(java.util.List.of(
                new BuiltInCondition.Chance(0),
                new BuiltInCondition.Chance(1)
        )).matches(context));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("vestige", path);
    }

    private static final class TestContext implements ConditionContext {
        private final Map<ResourceLocation, ConditionValue> values = new HashMap<>();
        private final Set<ContextReference> tags = new HashSet<>();
        private final Set<ContextReference> predicates = new HashSet<>();
        private double random;

        TestContext withValue(ResourceLocation path, ConditionValue value) {
            values.put(path, value);
            return this;
        }

        TestContext withTag(ResourceLocation path, ResourceLocation tag) {
            tags.add(new ContextReference(path, tag));
            return this;
        }

        TestContext withPredicate(ResourceLocation path, ResourceLocation predicate) {
            predicates.add(new ContextReference(path, predicate));
            return this;
        }

        TestContext withRandom(double random) {
            this.random = random;
            return this;
        }

        @Override
        public Optional<ConditionValue> value(ResourceLocation path) {
            return Optional.ofNullable(values.get(path));
        }

        @Override
        public boolean isTagged(ResourceLocation path, ResourceLocation tag) {
            return tags.contains(new ContextReference(path, tag));
        }

        @Override
        public boolean matches(ResourceLocation path, ResourceLocation predicate) {
            return predicates.contains(new ContextReference(path, predicate));
        }

        @Override
        public double random() {
            return random;
        }
    }

    private record ContextReference(ResourceLocation path, ResourceLocation reference) {
    }
}
