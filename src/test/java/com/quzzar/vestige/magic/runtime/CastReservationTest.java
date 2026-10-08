package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.condition.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CastReservationTest {
    private static final UUID ACTOR=UUID.randomUUID();
    private static ResourceLocation id(String value) { return ResourceLocation.fromNamespaceAndPath("vestige",value); }
    private static SpellEvent event() { return SpellEvent.of(id("interact"),ACTOR,null); }
    private static final SpellEffects.Action ACTION=new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Constant(1)),Map.of());
    private static SpellDefinition spell(List<SpellEffect> plan) {
        return new SpellDefinition(id("reservation_fixture"),Set.of(Tradition.ARCANE),TraitProfile.empty(),
                List.of(new SpellCost.Time(2),new SpellCost.Mana(6),new SpellCost.Cooldown(4)),
                List.of(new SpellTrigger(id("primary"),id("interact"),List.of())),plan);
    }
    private static SpellRuntime.Cast cast(SpellRuntime runtime,SpellDefinition definition,Reservation source,boolean identified) {
        return runtime.cast(definition,event(),List.of(),identified,Optional.empty(),false,CastShaping.NONE,source);
    }
    private static class Reservation implements CastReservation {
        boolean valid=true;int commits;final List<String> order;
        Reservation(List<String> order) { this.order=order; }
        public boolean valid() { return valid; }
        public void commit() { commits++;order.add("wear");valid=false; }
        public Optional<Recovery> recovery() { return Optional.of(new Recovery(id("wand"),1200)); }
    }
    @Test void cancellationAndFailedPaymentDoNotCommitTheSourceOrRecovery() {
        var world=new World();var runtime=new SpellRuntime(world);var source=new Reservation(world.order);
        var canceled=cast(runtime,spell(List.of(ACTION)),source,true);source.valid=false;runtime.tick();runtime.tick();
        assertEquals(SpellRuntime.Status.INTERRUPTED,canceled.status());assertEquals(0,world.payments);assertEquals(0,source.commits);
        world.canPay=false;source.valid=true;var unpaid=cast(runtime,spell(List.of(ACTION)),source,true);runtime.tick();runtime.tick();
        assertEquals(SpellRuntime.Status.COST_FAILED,unpaid.status());assertFalse(unpaid.paymentCommitted());assertEquals(0,source.commits);
        world.canPay=true;assertEquals(SpellRuntime.Status.CHARGING,cast(runtime,spell(List.of(ACTION)),source,true).status());
    }
    @Test void resourceAndSourceCommitOnceBeforeEffectsAndDelayedContinuation() {
        var world=new World();var runtime=new SpellRuntime(world);var source=new Reservation(world.order);
        var cast=cast(runtime,spell(List.of(ACTION,new SpellEffects.Delay(2),ACTION)),source,true);
        runtime.tick();assertEquals(0,source.commits);runtime.tick();
        assertTrue(cast.paymentCommitted());assertEquals(List.of("pay","wear","damage"),world.order);
        runtime.tick();runtime.tick();assertEquals(SpellRuntime.Status.COMPLETED,cast.status());
        assertEquals(1,world.payments);assertEquals(1,source.commits);assertEquals(List.of("pay","wear","damage","damage"),world.order);
    }
    @Test void paidForfeitSpendsTheSourceAndKeepsExactlySixtySecondsOfRecovery() {
        var world=new World();world.random=0;var runtime=new SpellRuntime(world);var source=new Reservation(world.order);
        var definition=spell(List.of(ACTION));var forfeited=cast(runtime,definition,source,false);runtime.tick();runtime.tick();
        assertEquals(SpellRuntime.Status.FORFEITED,forfeited.status());assertTrue(forfeited.paymentCommitted());
        assertEquals(1,world.payments);assertEquals(1,source.commits);assertEquals(List.of("pay","wear","forfeit"),world.order);
        for (int i=0;i<1199;i++) runtime.tick();
        assertEquals(SpellRuntime.Status.COOLDOWN,cast(runtime,definition,new Reservation(world.order),true).status());
        runtime.tick();assertEquals(SpellRuntime.Status.CHARGING,cast(runtime,definition,new Reservation(world.order),true).status());
    }
    @Test void sourceRecoveryDoesNotLengthenOrdinarySpellRecovery() {
        var world=new World();var runtime=new SpellRuntime(world);var source=new Reservation(world.order);var definition=spell(List.of(ACTION));
        cast(runtime,definition,source,true);runtime.tick();runtime.tick();
        for (int i=0;i<4;i++) runtime.tick();
        assertEquals(SpellRuntime.Status.COOLDOWN,cast(runtime,definition,new Reservation(world.order),true).status());
        assertEquals(SpellRuntime.Status.CHARGING,runtime.cast(definition,event(),List.of(),true).status());
    }
    @Test void aPaidRecastRetainsItsOriginalViewWithoutSecondWearOrPayment() {
        var world=new World();var runtime=new SpellRuntime(world);var source=new Reservation(world.order);
        var definition=spell(List.of(ACTION,new SpellEffects.AwaitRecast(20),ACTION));
        var paid=cast(runtime,definition,source,true);runtime.tick();runtime.tick();assertEquals(SpellRuntime.Status.AWAITING_RECAST,paid.status());
        var continued=runtime.cast(definition,event(),List.of(),true);
        assertSame(paid,continued);assertEquals(SpellRuntime.Status.COMPLETED,continued.status());
        assertEquals(1,world.payments);assertEquals(1,source.commits);assertEquals(2,Collections.frequency(world.order,"damage"));
    }
    private static class World implements SpellWorld {
        final List<String> order=new ArrayList<>();int payments;boolean canPay=true;double random=.99;
        public ConditionContext conditions(SpellRuntime.Context context) {
            return new ConditionContext() {
                public Optional<ConditionValue> value(ResourceLocation key) { return Optional.empty(); }
                public boolean isTagged(ResourceLocation key,ResourceLocation tag) { return false; }
                public boolean matches(ResourceLocation key,ResourceLocation predicate) { return false; }
                public double random() { return random; }
            };
        }
        public List<SpellSubject> select(TargetSpec target,SpellRuntime.Context context) { return List.of(context.target()); }
        public boolean pay(List<SpellCost> costs,SpellRuntime.Context context) { if (!canPay) return false;payments++;order.add("pay");return true; }
        public boolean active(SpellRuntime.Context context) { return true; }
        public void forfeit(SpellRuntime.Context context) { order.add("forfeit"); }
        public boolean execute(SpellEffects.Action action,SpellRuntime.Context context) { order.add(action.type().getPath());return true; }
        public Optional<ManifestationHandle> manifest(SpellEffects.Manifestation definition,Map<String,Double> values,SpellRuntime.Context context) { return Optional.empty(); }
    }
}
