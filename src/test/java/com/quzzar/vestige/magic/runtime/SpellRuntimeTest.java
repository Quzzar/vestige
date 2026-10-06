package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.condition.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.presentation.SpellVisual;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SpellRuntimeTest {
    @Test void presentationReceivesTheSameOrderedSelectionWithoutASecondQuery() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var first = new SpellSubject.Entity(UUID.randomUUID()); var second = new SpellSubject.Entity(UUID.randomUUID());
        world.selection = List.of(first, second);
        var visual = new SpellVisual(8, new SpellValue.Constant(.2), 1,
                List.of(new SpellVisual.Layer(SpellVisual.Shape.ARC, 0xffffff, 1, .04f, 1)), false, Optional.empty());
        var each = new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.CHAIN, new SpellValue.Constant(16)), List.of(DAMAGE), Optional.of(visual));
        assertEquals(SpellRuntime.Status.COMPLETED, runtime.cast(spell(List.of(), List.of(each)), event(), List.of(), true).status());
        assertEquals(1, world.selections);
        assertEquals(List.of(new SpellSubject.Entity(ACTOR), first, second), world.presented);
        assertEquals(List.of(5.0, 5.0), world.amounts);
    }
    @Test void consumedBindingsNotifyOnceWithoutDeletingTheirManifestation() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var ward = new SpellEffects.Manifestation(id("status"), 20, Map.of(), Map.of(), List.of(binding(20, 2, List.of(action("reduce_pending_damage", 3)))));
        runtime.cast(spell(List.of(), List.of(new SpellEffects.CreateManifestation(ward, TargetSpec.self()))), event(), List.of(), true);
        runtime.emit(new SpellEvent(HIT, ACTOR, Optional.of(new SpellSubject.Entity(ACTOR)), Optional.of(new SpellEvent.PendingOutcome(5)), CausalChain.start()));
        assertTrue(world.exhausted.isEmpty());
        runtime.emit(new SpellEvent(HIT, ACTOR, Optional.of(new SpellSubject.Entity(ACTOR)), Optional.of(new SpellEvent.PendingOutcome(5)), CausalChain.start()));
        assertEquals(List.of(true), world.exhausted); assertEquals(1, runtime.activeManifestations());
        for (int i = 0; i < 5; i++) runtime.tick();
        assertEquals(List.of(true), world.exhausted);
    }
    @Test void expiredBindingsNotifyOnceAndDistinguishNaturalExpiry() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var ward = new SpellEffects.Manifestation(id("status"), 20, Map.of(), Map.of(), List.of(binding(3, 1, List.of(DAMAGE))));
        runtime.cast(spell(List.of(), List.of(new SpellEffects.CreateManifestation(ward, TargetSpec.self()))), event(), List.of(), true);
        for (int i = 0; i < 6; i++) runtime.tick();
        assertEquals(List.of(false), world.exhausted); assertEquals(1, runtime.activeManifestations());
    }
    private static final UUID ACTOR = UUID.randomUUID();
    private static final ResourceLocation AMPLIFY = id("amplify");
    private static final ResourceLocation HIT = id("damage_calculating");
    private static final SpellEffects.Action DAMAGE = action("damage", 5);

    @Test void rarityIsQueryableAndUnaffectedByTraitBoosts() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var rarityCondition = new BuiltInCondition.Compare(ConditionPaths.SPELL_RARITY,
                BuiltInCondition.Comparison.EQUAL, new ConditionValue.Text("mythic"), Optional.empty());
        SpellDefinition base = spell(List.of(), List.of(
                new SpellEffects.SetValue(ConditionPaths.SPELL_RARITY, new ConditionValue.Text("common")),
                new SpellEffects.Branch(rarityCondition, List.of(DAMAGE), List.of(action("damage", 1)))));
        SpellDefinition mythic = new SpellDefinition(base.id(), SpellRarity.MYTHIC, base.traditions(), base.traits(), base.costs(), base.triggers(), base.effects());
        var misleadingFacts = new SpellEvent(SpellTriggerTypes.INTERACT, ACTOR, Optional.empty(), Optional.empty(), CausalChain.start(),
                Map.of(ConditionPaths.SPELL_RARITY, new ConditionValue.Text("common")));
        runtime.cast(mythic, misleadingFacts, List.of(new TraitModifier(AMPLIFY, TraitModifier.Operation.MULTIPLY, 2)), true);
        assertEquals(List.of(5.0), world.amounts);
        assertEquals(SpellRarity.MYTHIC, mythic.rarity());
        assertEquals(1, mythic.traits().rating(AMPLIFY));
        runtime.cast(base, event(), List.of(), true);
        assertEquals(List.of(5.0, 1.0), world.amounts);
        assertEquals(SpellRarity.COMMON, base.rarity());
    }

    @Test void capturedFactsRemainStableAcrossDelayedDelivery() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var source = id("actor/weapon_damage"); var saved = id("snapshot/weapon_damage");
        world.facts.put(source, new ConditionValue.Decimal(7));
        var cast = runtime.cast(spell(List.of(), List.of(new SpellEffects.CaptureValue(saved, new SpellValue.Fact(source)),
                new SpellEffects.Delay(2), new SpellEffects.Action(id("damage"), Map.of("amount", new SpellValue.Fact(saved)), Map.of()))), event(), List.of(), true);
        world.facts.put(source, new ConditionValue.Decimal(1));
        runtime.tick(); runtime.tick();
        assertEquals(List.of(7.0), world.amounts);
        assertEquals(SpellRuntime.Status.COMPLETED, cast.status());
    }

    @Test void chargeAndDelayExecuteInOrderWithAnImmutableResolvedProfile() {
        World world = new World();
        SpellRuntime runtime = new SpellRuntime(world);
        SpellDefinition spell = spell(List.of(new SpellCost.Time(2), new SpellCost.Mana(10)), List.of(
                new SpellEffects.Action(id("damage"), Map.of("amount", new SpellValue.Product(List.of(
                        new SpellValue.Constant(5), new SpellValue.Trait(AMPLIFY)))), Map.of()),
                new SpellEffects.Delay(2), DAMAGE));
        var cast = runtime.cast(spell, event(), List.of(new TraitModifier(AMPLIFY, TraitModifier.Operation.ADD, 1)), true);
        assertEquals(SpellRuntime.Status.CHARGING, cast.status());
        runtime.tick(); assertEquals(0, world.payments);
        runtime.tick(); assertEquals(List.of(10.0), world.amounts);
        assertEquals(1, world.payments);
        runtime.tick(); assertEquals(1, world.amounts.size());
        runtime.tick(); assertEquals(List.of(10.0, 5.0), world.amounts);
        assertEquals(SpellRuntime.Status.COMPLETED, cast.status());
        assertEquals(1, spell.traits().rating(AMPLIFY));
        assertEquals(0, runtime.activeCasts());
    }

    @Test void recastResumesTheSameSessionAndPaysOnlyOnce() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        ResourceLocation stage = id("session/stage");
        SpellDefinition spell = spell(List.of(), List.of(new SpellEffects.SetValue(stage, new ConditionValue.Text("endpoint")),
                new SpellEffects.AwaitRecast(5), DAMAGE));
        var cast = runtime.cast(spell, event(), List.of(), true);
        assertEquals(SpellRuntime.Status.AWAITING_RECAST, cast.status());
        assertTrue(world.amounts.isEmpty());
        assertEquals(new ConditionValue.Text("endpoint"), cast.state().get(stage));
        assertSame(cast, runtime.cast(spell, event(), List.of(), true));
        assertEquals(SpellRuntime.Status.COMPLETED, cast.status());
        assertEquals(1, world.payments); assertEquals(List.of(5.0), world.amounts);
    }

    @Test void timeoutAndInterruptionCancelContinuationsWithoutSpendingChargeCosts() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var waiting = runtime.cast(spell(List.of(), List.of(new SpellEffects.AwaitRecast(2), DAMAGE)), event(), List.of(), true);
        runtime.tick(); runtime.tick();
        assertEquals(SpellRuntime.Status.TIMED_OUT, waiting.status());
        var charging = runtime.cast(spell(List.of(new SpellCost.Time(2)), List.of(DAMAGE)), event(), List.of(), true);
        assertTrue(runtime.interrupt(charging.id()));
        runtime.tick(); runtime.tick();
        assertEquals(SpellRuntime.Status.INTERRUPTED, charging.status());
        assertTrue(world.amounts.isEmpty()); assertEquals(1, world.payments);
    }

    @Test void bindingsReactOnlyOnTheirSubjectAndRespectChargesAndExpiry() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var binding = binding(3, 2, List.of(DAMAGE));
        runtime.cast(spell(List.of(), List.of(new SpellEffects.InstallBinding(binding, TargetSpec.self()))), event(), List.of(), true);
        runtime.emit(SpellEvent.of(HIT, UUID.randomUUID(), null));
        assertTrue(world.amounts.isEmpty());
        runtime.emit(SpellEvent.of(HIT, ACTOR, null)); runtime.emit(SpellEvent.of(HIT, ACTOR, null));
        runtime.emit(SpellEvent.of(HIT, ACTOR, null));
        assertEquals(List.of(5.0, 5.0), world.amounts); assertEquals(0, runtime.activeBindings());
        runtime.cast(spell(List.of(), List.of(new SpellEffects.InstallBinding(binding, TargetSpec.self()))), event(), List.of(), true);
        runtime.tick(); runtime.tick(); runtime.tick();
        runtime.emit(SpellEvent.of(HIT, ACTOR, null)); assertEquals(2, world.amounts.size());
    }

    @Test void replacingBehaviorAndDispellingManifestationsOwnsCleanup() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var manifestation = new SpellEffects.Manifestation(id("barrier"), 10, Map.of("health", new SpellValue.Constant(5)),
                Map.of(), List.of(binding(10, 1, List.of(new SpellEffects.EndManifestation()))));
        SpellDefinition spell = spell(List.of(), List.of(new SpellEffects.CreateManifestation(manifestation, TargetSpec.self())));
        runtime.cast(spell, event(), List.of(), true);
        assertEquals(1, runtime.activeBindings());
        runtime.cast(spell, event(), List.of(), true);
        assertEquals(List.of(SpellRuntime.EndReason.REPLACED), world.ends);
        assertEquals(1, runtime.activeManifestations()); assertEquals(1, runtime.activeBindings());
        runtime.emit(SpellEvent.of(HIT, ACTOR, null));
        assertEquals(0, runtime.activeManifestations()); assertEquals(0, runtime.activeBindings());
        assertEquals(SpellRuntime.EndReason.DESTROYED, world.ends.getLast());
        runtime.cast(spell, event(), List.of(), true); runtime.close(); runtime.close();
        assertEquals(SpellRuntime.EndReason.SERVER_STOP, world.ends.getLast());
        assertThrows(IllegalStateException.class, runtime::tick);
    }

    @Test void expiredBackingAndUnavailableActorsRemovePersistentBehavior() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var manifestation = new SpellEffects.Manifestation(id("summon"), 2, Map.of(), Map.of(), List.of());
        runtime.cast(spell(List.of(), List.of(new SpellEffects.CreateManifestation(manifestation, TargetSpec.self()))), event(), List.of(), true);
        runtime.tick(); assertEquals(1, runtime.activeManifestations()); runtime.tick();
        assertEquals(List.of(SpellRuntime.EndReason.EXPIRED), world.ends);
        runtime.cast(spell(List.of(), List.of(new SpellEffects.CreateManifestation(manifestation, TargetSpec.self()))), event(), List.of(), true);
        world.active = false; runtime.tick();
        assertEquals(SpellRuntime.EndReason.OWNER_UNAVAILABLE, world.ends.getLast());
    }

    @Test void reactionsMutateOnlyUncommittedOutcomes() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var reduce = action("reduce_pending_damage", 3);
        runtime.cast(spell(List.of(), List.of(new SpellEffects.InstallBinding(binding(20, 2, List.of(reduce)), TargetSpec.self()))), event(), List.of(), true);
        var pending = new SpellEvent.PendingOutcome(10);
        runtime.emit(new SpellEvent(HIT, ACTOR, Optional.empty(), Optional.of(pending), CausalChain.start()));
        assertEquals(7, pending.commit());
        assertThrows(IllegalStateException.class, () -> pending.reduce(1));
        runtime.emit(new SpellEvent(HIT, ACTOR, Optional.empty(), Optional.of(pending), CausalChain.start()));
        assertEquals(0, runtime.activeBindings());
    }

    @Test void failedCostsForfeitsAndFailedEffectsDoNotLeaveActiveState() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        world.canPay = false;
        var cast = runtime.cast(spell(List.of(), List.of(DAMAGE)), event(), List.of(), true);
        assertEquals(SpellRuntime.Status.COST_FAILED, cast.status()); assertTrue(world.amounts.isEmpty());
        assertFalse(cast.paymentCommitted());
        world.canPay = true; world.sample = 0;
        cast = runtime.cast(spell(List.of(), List.of(DAMAGE)), event(), List.of(), false);
        assertEquals(SpellRuntime.Status.FORFEITED, cast.status()); assertEquals(1, world.forfeits);
        assertTrue(cast.paymentCommitted());
        world.sample = 0.99; world.canExecute = false;
        cast = runtime.cast(spell(List.of(), List.of(DAMAGE)), event(), List.of(), true);
        assertEquals(SpellRuntime.Status.EFFECT_FAILED, cast.status()); assertTrue(cast.failure().isPresent());
        assertTrue(cast.paymentCommitted());
        assertEquals(0, runtime.activeCasts());
    }

    @Test void repeatBranchAndTargetIterationUseReusablePlans() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var plan = new SpellEffects.ForEach(TargetSpec.self(), List.of(new SpellEffects.Branch(new BuiltInCondition.Chance(1),
                List.of(new SpellEffects.Repeat(3, 1, List.of(DAMAGE))), List.of())));
        var cast = runtime.cast(spell(List.of(), List.of(plan)), event(), List.of(), true);
        assertEquals(1, world.amounts.size()); runtime.tick(); runtime.tick();
        assertEquals(List.of(5.0, 5.0, 5.0), world.amounts);
        assertEquals(SpellRuntime.Status.COMPLETED, cast.status());
    }

    @Test void bindingExpiryAndReplacementCancelItsDelayedConsequences() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var shortBinding = binding(2, 1, List.of(new SpellEffects.Delay(3), DAMAGE));
        SpellDefinition shortSpell = spell(List.of(), List.of(new SpellEffects.InstallBinding(shortBinding, TargetSpec.self())));
        runtime.cast(shortSpell, event(), List.of(), true);
        runtime.emit(SpellEvent.of(HIT, ACTOR, null));
        runtime.tick(); runtime.tick(); runtime.tick();
        assertTrue(world.amounts.isEmpty());
        var longBinding = binding(20, 1, List.of(new SpellEffects.Delay(3), DAMAGE));
        SpellDefinition longSpell = spell(List.of(), List.of(new SpellEffects.InstallBinding(longBinding, TargetSpec.self())));
        runtime.cast(longSpell, event(), List.of(), true);
        runtime.emit(SpellEvent.of(HIT, ACTOR, null));
        runtime.cast(longSpell, event(), List.of(), true);
        runtime.tick(); runtime.tick(); runtime.tick();
        assertTrue(world.amounts.isEmpty());
    }

    @Test void modesChooseAnAlternateChargePlanWithoutChangingSpellIdentity() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        SpellDefinition base = spell(List.of(), List.of(DAMAGE));
        SpellMode mode = new SpellMode(id("charged"), List.of(new SpellCost.Time(2)), List.of(action("damage", 10)));
        SpellDefinition spell = new SpellDefinition(base.id(), base.traditions(), base.traits(), base.costs(), base.triggers(), base.effects(), Map.of(mode.id(), mode));
        var cast = runtime.cast(spell, event(), List.of(), true, Optional.of(mode.id()));
        assertEquals(SpellRuntime.Status.CHARGING, cast.status());
        runtime.tick(); assertTrue(world.amounts.isEmpty()); runtime.tick();
        assertEquals(List.of(10.0), world.amounts); assertEquals(SpellRuntime.Status.COMPLETED, cast.status());
        assertEquals(SpellRuntime.Status.CONDITIONS_FAILED, runtime.cast(spell, event(), List.of(), true, Optional.of(id("absent"))).status());
    }

    @Test void cooldownStartsAfterPaymentAndExpiresExactlyForItsActorAndSpell() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var spell = spell(List.of(new SpellCost.Time(2), new SpellCost.Cooldown(3)), List.of(DAMAGE));
        runtime.cast(spell, event(), List.of(), true);
        runtime.tick(); runtime.tick();
        assertEquals(1, world.payments);
        assertEquals(SpellRuntime.Status.COOLDOWN, runtime.cast(spell, event(), List.of(), true).status());
        var other = SpellEvent.of(SpellTriggerTypes.INTERACT, UUID.randomUUID(), null);
        assertEquals(SpellRuntime.Status.CHARGING, runtime.cast(spell, other, List.of(), true).status());
        runtime.tick(); runtime.tick();
        assertEquals(SpellRuntime.Status.COOLDOWN, runtime.cast(spell, event(), List.of(), true).status());
        runtime.tick();
        assertEquals(SpellRuntime.Status.CHARGING, runtime.cast(spell, event(), List.of(), true).status());
    }

    @Test void interruptedChargesAndFailedPaymentDoNotStartRecoveryButPaidFailuresDo() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var charged = spell(List.of(new SpellCost.Time(2), new SpellCost.Cooldown(10)), List.of(DAMAGE));
        runtime.interrupt(runtime.cast(charged, event(), List.of(), true).id());
        assertEquals(SpellRuntime.Status.CHARGING, runtime.cast(charged, event(), List.of(), true).status());
        world.canPay = false; runtime.tick(); runtime.tick();
        var instant = spell(List.of(new SpellCost.Cooldown(10)), List.of(DAMAGE));
        assertEquals(SpellRuntime.Status.COST_FAILED, runtime.cast(instant, event(), List.of(), true).status());
        world.canPay = true; world.canExecute = false;
        assertEquals(SpellRuntime.Status.EFFECT_FAILED, runtime.cast(instant, event(), List.of(), true).status());
        assertEquals(SpellRuntime.Status.COOLDOWN, runtime.cast(instant, event(), List.of(), true).status());
    }

    @Test void recastsShareOnePaymentAndRecoveryWhileDevelopmentBypassRemainsExplicit() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var spell = spell(List.of(new SpellCost.Cooldown(10)), List.of(DAMAGE, new SpellEffects.AwaitRecast(5), DAMAGE));
        var first = runtime.cast(spell, event(), List.of(), true);
        assertSame(first, runtime.cast(spell, event(), List.of(), true));
        assertEquals(1, world.payments);
        assertEquals(SpellRuntime.Status.COOLDOWN, runtime.cast(spell, event(), List.of(), true).status());
        var bypass = runtime.cast(spell, event(), List.of(), true, Optional.empty(), true);
        assertEquals(SpellRuntime.Status.AWAITING_RECAST, bypass.status());
        assertSame(bypass, runtime.cast(spell, event(), List.of(), true, Optional.empty(), true));
        assertEquals(1, world.payments);
    }

    @Test void simultaneousChargesAndChannelsAreBlockedButDormantRecastsAllowOtherSpells() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var base = spell(List.of(), List.of(DAMAGE, new SpellEffects.Delay(3), DAMAGE));
        var other = new SpellDefinition(id("other"), base.traditions(), base.traits(), List.of(), base.triggers(), List.of(DAMAGE));
        runtime.cast(base, event(), List.of(), true);
        assertEquals(SpellRuntime.Status.BUSY, runtime.cast(other, event(), List.of(), true).status());
        runtime.interruptActor(ACTOR);
        runtime.cast(spell(List.of(), List.of(new SpellEffects.AwaitRecast(5), DAMAGE)), event(), List.of(), true);
        assertEquals(SpellRuntime.Status.COMPLETED, runtime.cast(other, event(), List.of(), true).status());
    }

    @Test void fieldPulsesAreAnchoredToCreationAndIncludeTheFinalIntervalBeforeCleanup() {
        World world = new World(); SpellRuntime runtime = new SpellRuntime(world);
        var field = new SpellEffects.Manifestation(id("area"), 6, Map.of(), Map.of(), List.of(), List.of(), List.of(DAMAGE), List.of(), 2);
        runtime.cast(spell(List.of(new SpellCost.Time(3)), List.of(new SpellEffects.CreateManifestation(field, TargetSpec.self()))), event(), List.of(), true);
        for (int i=0;i<4;i++) runtime.tick();
        assertTrue(world.amounts.isEmpty());
        runtime.tick(); assertEquals(List.of(5.0), world.amounts);
        for (int i=0;i<4;i++) runtime.tick();
        assertEquals(List.of(5.0,5.0,5.0), world.amounts);
        assertEquals(List.of(SpellRuntime.EndReason.EXPIRED), world.ends);
        assertEquals(0,runtime.activeManifestations());
    }

    @Test void replacementUsesTheOriginalTargetWhenBackingCreatesADifferentSubject() {
        World world = new World(); world.distinctBacking = true; SpellRuntime runtime = new SpellRuntime(world);
        var summon = new SpellEffects.Manifestation(id("summon"), 10, Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(), 1);
        var spell = spell(List.of(), List.of(new SpellEffects.CreateManifestation(summon, TargetSpec.self())));
        runtime.cast(spell,event(),List.of(),true); runtime.cast(spell,event(),List.of(),true);
        assertEquals(1,runtime.activeManifestations());
        assertEquals(List.of(SpellRuntime.EndReason.REPLACED),world.ends);
    }

    @Test void shapingPersistsThroughChargeRecastAndManifestationCallbacksAndPaysOnce() {
        World world=new World();SpellRuntime runtime=new SpellRuntime(world);
        var fractional=action("damage",2.25*2);
        var field=new SpellEffects.Manifestation(id("area"),4,Map.of("radius",new SpellValue.Product(List.of(new SpellValue.Constant(2.5),new SpellValue.Trait(AMPLIFY)))),Map.of(),List.of(),List.of(),List.of(fractional),List.of(),2);
        var definition=spell(List.of(new SpellCost.Mana(20),new SpellCost.Time(5)),List.of(fractional,new SpellEffects.AwaitRecast(20),new SpellEffects.CreateManifestation(field,TargetSpec.self())));
        var cast=runtime.cast(definition,event(),List.of(new TraitModifier(AMPLIFY,TraitModifier.Operation.MULTIPLY,1.2)),true,Optional.empty(),false,new CastShaping(1.1,true));
        for (int i=0;i<5;i++) runtime.tick();assertFalse(cast.paymentCommitted());
        runtime.tick();assertEquals(List.of(new SpellCost.Mana(22),new SpellCost.Time(6)),world.lastCosts);assertEquals(List.of(5.0),world.amounts);
        assertEquals(cast,runtime.cast(definition,event(),List.of(),true));assertEquals(3,world.lastManifest.get("radius"));
        for (int i=0;i<4;i++) runtime.tick();assertEquals(List.of(5.0,5.0,5.0),world.amounts);assertEquals(1,world.payments);
        assertEquals(20,((SpellCost.Mana)definition.costs().getFirst()).amount());
    }

    private static SpellDefinition spell(List<SpellCost> costs, List<SpellEffect> effects) {
        return new SpellDefinition(id("sample"), Set.of(Tradition.ARCANE), new TraitProfile(Map.of(AMPLIFY, 1.0)), costs,
                List.of(new SpellTrigger(id("primary"), SpellTriggerTypes.INTERACT, List.of())), effects);
    }
    private static SpellEffects.Binding binding(int ticks, int charges, List<SpellEffect> effects) {
        return new SpellEffects.Binding(id("defense"), List.of(new SpellTrigger(id("defense_trigger"), HIT, List.of())), effects, ticks, charges);
    }
    private static SpellEffects.Action action(String type, double amount) {
        return new SpellEffects.Action(id(type), Map.of("amount", new SpellValue.Constant(amount)), Map.of());
    }
    private static SpellEvent event() { return SpellEvent.of(SpellTriggerTypes.INTERACT, ACTOR, null); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("vestige", path); }

    private static final class World implements SpellWorld {
        List<SpellSubject> selection, presented;
        int selections;
        final List<Boolean> exhausted = new ArrayList<>();
        final List<Double> amounts = new ArrayList<>();
        final List<SpellRuntime.EndReason> ends = new ArrayList<>();
        final Map<ResourceLocation, ConditionValue> facts = new HashMap<>();
        int payments, forfeits;
        List<SpellCost> lastCosts; Map<String,Double> lastManifest;
        boolean active = true, canPay = true, canExecute = true, distinctBacking;
        double sample = 0.99;
        @Override public ConditionContext conditions(SpellRuntime.Context context) {
            return new ConditionContext() {
                public Optional<ConditionValue> value(ResourceLocation path) { return Optional.ofNullable(facts.get(path)); }
                public boolean isTagged(ResourceLocation path, ResourceLocation tag) { return false; }
                public boolean matches(ResourceLocation path, ResourceLocation predicate) { return false; }
                public double random() { return sample; }
            };
        }
        @Override public List<SpellSubject> select(TargetSpec target, SpellRuntime.Context context) { selections++; return selection == null ? List.of(new SpellSubject.Entity(context.actor())) : selection; }
        @Override public void present(SpellVisual visual, List<SpellSubject> points, SpellRuntime.Context context) { presented = points; }
        @Override public boolean pay(List<SpellCost> costs, SpellRuntime.Context context) { if (!canPay) return false; payments++; lastCosts=List.copyOf(costs); return true; }
        @Override public boolean active(SpellRuntime.Context context) { return active; }
        @Override public void forfeit(SpellRuntime.Context context) { forfeits++; }
        @Override public boolean execute(SpellEffects.Action action, SpellRuntime.Context context) {
            if (canExecute) amounts.add(context.gameplayValue("amount",action.values().get("amount")));
            return canExecute;
        }
        @Override public Optional<ManifestationHandle> manifest(SpellEffects.Manifestation definition, Map<String, Double> values, SpellRuntime.Context context) {
            lastManifest=Map.copyOf(values);
            return Optional.of(new ManifestationHandle() {
                final SpellSubject subject = distinctBacking ? new SpellSubject.Entity(UUID.randomUUID()) : context.target();
                public SpellSubject subject() { return subject; }
                public boolean alive() { return true; }
                public void bindingsExhausted(boolean consumed) { exhausted.add(consumed); }
                public void close(SpellRuntime.EndReason reason) { ends.add(reason); }
            });
        }
    }
}
