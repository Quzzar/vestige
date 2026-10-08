package com.quzzar.vestige.magic.runtime;

import com.google.gson.JsonParser;
import com.quzzar.vestige.magic.condition.ConditionContext;
import com.quzzar.vestige.magic.condition.ConditionValue;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import org.junit.jupiter.api.Test;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UnifiedMagicTest {
    private static final UUID ACTOR = UUID.randomUUID(), OTHER = UUID.randomUUID();
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("vestige", name); }
    private static SpellEvent event() { return SpellEvent.of(SpellTriggerTypes.INTERACT, ACTOR, null); }
    private static TraitModifier multiply(String trait, double value) { return new TraitModifier(id(trait), TraitModifier.Operation.MULTIPLY, value); }
    private static SpellValue variable(String name) { return new SpellValue.Variable(id(name)); }
    private static SpellValue scaled(double base, String trait) { return new SpellValue.Product(List.of(new SpellValue.Constant(base), new SpellValue.Trait(id(trait)))); }
    private static SpellEffects.Action amount(SpellValue value) { return new SpellEffects.Action(id("test_amount"), Map.of("amount", value), Map.of()); }
    private static ItemAbilityDefinition ability(String name, List<SpellEffect> effects) {
        return new ItemAbilityDefinition(id(name), new TraitProfile(Map.of(id("time"), 1d, id("amplify"), 1d)),
                Map.of(id("ward_ticks"), scaled(80, "time"), id("protection"), scaled(2, "amplify")),
                List.of(new SpellCost.Mana(5)), List.of(new SpellTrigger(id("use"), SpellTriggerTypes.INTERACT, List.of())), effects);
    }
    private static ItemAbilityDefinition boost(int duration, List<TraitModifier> modifiers) {
        return ability("test_boost", List.of(new SpellEffects.GrantTraits(id("test_group"), modifiers,
                new SpellValue.Constant(duration), TargetSpec.self())));
    }

    @Test void playerAndSourceModifiersComposeOnceForSpellsAndItemAbilities() {
        World world = new World(); world.modifiers = List.of(multiply("amplify", 1.5));
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            runtime.activate(boost(50, List.of(multiply("time", 1.25))), event(), List.of(), CastReservation.NONE);
            var item = ability("ward", List.of(amount(variable("protection"))));
            var spell = new SpellDefinition(id("spell"), SpellRarity.COMMON, Set.of(Tradition.ARCANE), item.traits(),
                    item.costs(), item.triggers(), item.effects(), Map.of(), Optional.empty(), item.variables());
            var modifiers = List.of(new TraitModifier(id("amplify"), TraitModifier.Operation.ADD, 1));
            var itemCast = runtime.activate(item, event(), modifiers, CastReservation.NONE);
            var spellCast = runtime.cast(spell, event(), modifiers, true);
            assertEquals(100, itemCast.resolution().variable(id("ward_ticks")));
            assertEquals(itemCast.resolution().traits().ratings(), spellCast.resolution().traits().ratings());
            assertEquals(itemCast.resolution().variables(), spellCast.resolution().variables());
            assertEquals(List.of(6d, 6d), world.amounts);
            assertEquals(1, item.traits().rating(id("amplify")));
            assertEquals(3, world.modifierQueries);
            assertEquals(3, world.payments);
        }
    }

    @Test void aFiveSecondWardKeepsItsSnapshotAfterTheTimeBoostExpires() {
        World world = new World();
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            runtime.activate(boost(3, List.of(multiply("time", 1.25), multiply("amplify", 1.5))), event(), List.of(), CastReservation.NONE);
            var binding = new SpellEffects.Binding(id("ward"), List.of(new SpellTrigger(id("ward_hit"), SpellTriggerTypes.DAMAGE_CALCULATING, List.of())),
                    List.of(amount(variable("protection"))), 80, 1, Optional.of(variable("ward_ticks")));
            var ward = ability("ward", List.of(new SpellEffects.InstallBinding(binding, TargetSpec.self())));
            var cast = runtime.activate(ward, event(), List.of(), CastReservation.NONE);
            assertEquals(100, cast.resolution().variable(id("ward_ticks")));
            for (int i = 0; i < 90; i++) runtime.tick();
            assertEquals(80, runtime.resolve(ward, ACTOR, List.of()).variable(id("ward_ticks")));
            var pending = new SpellEvent.PendingOutcome(10);
            runtime.emit(new SpellEvent(SpellTriggerTypes.DAMAGE_CALCULATING, ACTOR, Optional.empty(), Optional.of(pending), CausalChain.start()));
            assertEquals(7, pending.commit());
            assertEquals(0, runtime.activeBindings());
        }
    }

    @Test void resolvedBindingExpiresAtItsDeadlineAndCannotProtectLaterHits() {
        World world = new World();
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            var binding = new SpellEffects.Binding(id("ward"), List.of(new SpellTrigger(id("ward_hit"), SpellTriggerTypes.DAMAGE_CALCULATING, List.of())),
                    List.of(amount(variable("protection"))), 80, 1, Optional.of(variable("ward_ticks")));
            runtime.activate(ability("ward", List.of(new SpellEffects.InstallBinding(binding, TargetSpec.self()))), event(),
                    List.of(multiply("time", 1.25)), CastReservation.NONE);
            for (int i = 0; i < 100; i++) runtime.tick();
            var pending = new SpellEvent.PendingOutcome(10);
            runtime.emit(new SpellEvent(SpellTriggerTypes.DAMAGE_CALCULATING, ACTOR, Optional.empty(), Optional.of(pending), CausalChain.start()));
            assertEquals(10, pending.commit());
            assertEquals(0, runtime.activeBindings());
        }
    }

    @Test void boostsAreBoundedToTheirRecipientReplaceTheirOwnGroupAndDispelCleanly() {
        World world = new World();
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            var item = ability("sample", List.of(amount(variable("protection"))));
            var boost = boost(10, List.of(multiply("time", 1.25)));
            runtime.activate(boost, event(), List.of(), CastReservation.NONE);
            runtime.activate(boost, event(), List.of(), CastReservation.NONE);
            assertEquals(100, runtime.resolve(item, ACTOR, List.of()).variable(id("ward_ticks")));
            assertEquals(80, runtime.resolve(item, OTHER, List.of()).variable(id("ward_ticks")));
            assertEquals(1, runtime.dispelActor(ACTOR));
            assertEquals(80, runtime.resolve(item, ACTOR, List.of()).variable(id("ward_ticks")));
            runtime.activate(boost, event(), List.of(), CastReservation.NONE);
            world.active = false;
            assertEquals(80, runtime.resolve(item, ACTOR, List.of()).variable(id("ward_ticks")));
        }
    }

    @Test void recastsRetainTheOriginalBoostSnapshotAndPayOnce() {
        World world = new World();
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            runtime.activate(boost(1, List.of(multiply("amplify", 2))), event(), List.of(), CastReservation.NONE);
            var item = ability("recast", List.of(new SpellEffects.AwaitRecast(10), amount(variable("protection"))));
            var first = runtime.activate(item, event(), List.of(), CastReservation.NONE);
            runtime.tick();
            var continued = runtime.activate(item, event(), List.of(), CastReservation.NONE);
            assertSame(first, continued);
            assertEquals(List.of(4d), world.amounts);
            assertEquals(2, world.payments);
            assertEquals(2, world.modifierQueries);
        }
    }

    @Test void itemAbilitiesHaveNoUnknownScrollRiskButKeepExplicitVolatility() {
        World world = new World(); world.sample = 0;
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            var item = ability("item", List.of(amount(variable("protection"))));
            assertEquals(SpellRuntime.Status.COMPLETED, runtime.cast(item, event(), List.of(), false).status());
            assertEquals(0, world.forfeits);
            var volatileItem = new ItemAbilityDefinition(id("volatile_item"), new TraitProfile(Map.of(id("volatile"), 20d)),
                    Map.of(), List.of(), item.triggers(), List.of(amount(new SpellValue.Constant(2))));
            assertEquals(SpellRuntime.Status.FORFEITED, runtime.activate(volatileItem, event(), List.of(), CastReservation.NONE).status());
            assertEquals(1, world.forfeits);
        }
    }

    @Test void unaffordableAbilityCannotGrantABoostAndInvalidVariablesFailBeforePayment() {
        World world = new World(); world.affordable = false;
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            var boost = boost(10, List.of(multiply("time", 2)));
            assertEquals(SpellRuntime.Status.COST_FAILED, runtime.activate(boost, event(), List.of(), CastReservation.NONE).status());
            assertEquals(80, runtime.resolve(boost, ACTOR, List.of()).variable(id("ward_ticks")));
            world.affordable = true;
            var invalid = new ItemAbilityDefinition(id("overflow"), TraitProfile.empty(), Map.of(id("overflow"),
                    new SpellValue.Product(List.of(new SpellValue.Constant(Double.MAX_VALUE), new SpellValue.Constant(2)))),
                    List.of(new SpellCost.Mana(5)), boost.triggers(), boost.effects());
            var failed = runtime.activate(invalid, event(), List.of(), CastReservation.NONE);
            assertEquals(SpellRuntime.Status.EFFECT_FAILED, failed.status());
            assertFalse(failed.paymentCommitted());
            assertEquals(0, world.payments);
        }
    }

    @Test void grammarSupportsForwardVariablesAndRejectsCyclesMissingReferencesAndWorldFacts() {
        var json = JsonParser.parseString("""
                {"traits":{"time":1},"variables":{"seconds":{"product":[4,{"trait":"time"}]},
                "ticks":{"product":[20,{"variable":"seconds"}]}}}
                """).getAsJsonObject();
        var item = SpellJson.readAbility(id("passive"), json);
        var resolved = MagicResolution.resolve(item, List.of(multiply("time", 1.25)));
        assertEquals(5, resolved.variable(id("seconds")));
        assertEquals(100, resolved.variable(id("ticks")));
        for (String variables : List.of("{\"a\":{\"variable\":\"b\"}}", "{\"a\":{\"variable\":\"b\"},\"b\":{\"variable\":\"a\"}}", "{\"a\":{\"fact\":\"actor/health\"}}"))
            assertThrows(IllegalArgumentException.class, () -> SpellJson.readAbility(id("invalid"),
                    JsonParser.parseString("{\"variables\":" + variables + "}").getAsJsonObject()));
    }

    private static final class World implements SpellWorld {
        List<TraitModifier> modifiers = List.of();
        List<Double> amounts = new ArrayList<>();
        int modifierQueries, payments, forfeits;
        boolean active = true, affordable = true;
        double sample = .99;
        public List<TraitModifier> traitModifiers(UUID actor, MagicDefinition definition) { modifierQueries++; return modifiers; }
        public ConditionContext conditions(SpellRuntime.Context context) { return new ConditionContext() {
            public Optional<ConditionValue> value(ResourceLocation path) { return Optional.empty(); }
            public boolean isTagged(ResourceLocation path, ResourceLocation tag) { return false; }
            public boolean matches(ResourceLocation path, ResourceLocation predicate) { return false; }
            public double random() { return sample; }
        }; }
        public List<SpellSubject> select(TargetSpec target, SpellRuntime.Context context) { return List.of(context.target()); }
        public boolean pay(List<SpellCost> costs, SpellRuntime.Context context) { if (!affordable) return false; payments++; return true; }
        public boolean active(SpellRuntime.Context context) { return active; }
        public void forfeit(SpellRuntime.Context context) { forfeits++; }
        public boolean execute(SpellEffects.Action action, SpellRuntime.Context context) {
            double amount = context.number(action.values().get("amount")); amounts.add(amount);
            context.event().pending().ifPresent(pending -> pending.reduce(amount)); return true;
        }
        public Optional<ManifestationHandle> manifest(SpellEffects.Manifestation definition, Map<String, Double> values, SpellRuntime.Context context) { return Optional.empty(); }
    }

    @Test void reactiveEquipmentDoesNotInterruptOrBlockAPreparedSpell() {
        World world = new World();
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            var base = ability("prepared", List.of(amount(variable("protection"))));
            var prepared = new SpellDefinition(id("prepared"), SpellRarity.COMMON, Set.of(Tradition.ARCANE), base.traits(),
                    List.of(new SpellCost.Time(3), new SpellCost.Mana(5)), base.triggers(), base.effects(), Map.of(), Optional.empty(), base.variables());
            var spellCast = runtime.cast(prepared, event(), List.of(), true);
            var reactive = new ItemAbilityDefinition(id("reactive"), base.traits(), base.variables(), List.of(),
                    base.triggers(), base.effects(), ItemAbilityDefinition.Activation.REACTIVE);
            assertEquals(SpellRuntime.Status.COMPLETED, runtime.activate(reactive, event(), List.of(), CastReservation.NONE).status());
            assertEquals(SpellRuntime.Status.CHARGING, spellCast.status());
            assertEquals(SpellRuntime.Status.BUSY, runtime.activate(base, event(), List.of(), CastReservation.NONE).status());
            for (int i = 0; i < 3; i++) runtime.tick();
            assertEquals(SpellRuntime.Status.COMPLETED, spellCast.status());
            assertEquals(List.of(2d, 2d), world.amounts);
        }
    }

    @Test void aBoostGrantedByAConsumedBindingOwnsItsIndependentLifetime() {
        World world = new World();
        try (SpellRuntime runtime = new SpellRuntime(world)) {
            var binding = new SpellEffects.Binding(id("reaction"), List.of(new SpellTrigger(id("reaction_hit"), SpellTriggerTypes.DAMAGE_CALCULATING, List.of())),
                    List.of(new SpellEffects.GrantTraits(id("reaction_boost"), List.of(multiply("time", 1.25)),
                            new SpellValue.Constant(5), TargetSpec.self())), 10, 1);
            var item = ability("reaction", List.of(new SpellEffects.InstallBinding(binding, TargetSpec.self())));
            runtime.activate(item, event(), List.of(), CastReservation.NONE);
            runtime.emit(SpellEvent.of(SpellTriggerTypes.DAMAGE_CALCULATING, ACTOR, null));
            assertEquals(0, runtime.activeBindings());
            assertEquals(100, runtime.resolve(item, ACTOR, List.of()).variable(id("ward_ticks")));
            for (int i = 0; i < 5; i++) runtime.tick();
            assertEquals(80, runtime.resolve(item, ACTOR, List.of()).variable(id("ward_ticks")));
        }
    }
}
