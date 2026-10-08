package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.condition.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CastObserverTest {
    private static ResourceLocation id(String name){return VestigeMainMod.location(name);}
    private static final SpellEffects.Action HIT=new SpellEffects.Action(id("damage"),Map.of(),Map.of());
    private static SpellDefinition spell(){return new SpellDefinition(id("observer_fixture"),Set.of(Tradition.ARCANE),TraitProfile.empty(),List.of(new SpellCost.Time(2)),List.of(new SpellTrigger(id("primary"),id("interact"),List.of())),List.of(HIT));}
    private static SpellRuntime.Cast cast(SpellRuntime runtime,World world,Observer observer,boolean known){return runtime.cast(spell(),SpellEvent.of(id("interact"),world.actor,null),List.of(),known,Optional.empty(),false,CastShaping.NONE,new CastReservation(){public boolean valid(){return true;}public void commit(){}public CastObserver observer(){return observer;}});}
    @Test void lifecycleUsesActualPaymentAndSecondaryRidersDoNotNotifyOrReserveCasting(){
        var world=new World();var runtime=new SpellRuntime(world);var observer=new Observer();observer.rider=true;
        var primary=cast(runtime,world,observer,true);assertEquals(List.of("prepare"),observer.events);
        runtime.tick();runtime.tick();assertEquals(SpellRuntime.Status.COMPLETED,primary.status());
        assertEquals(List.of("prepare","activate:6.0","DAMAGE","end:COMPLETED"),observer.events);
        assertEquals(1,world.primary);assertEquals(0,world.secondary);
        // A delayed equipment echo does not hold the actor's ordinary spell reservation.
        var another=cast(runtime,world,new Observer(),true);assertEquals(SpellRuntime.Status.CHARGING,another.status());
        runtime.tick();runtime.tick();runtime.tick();assertEquals(1,world.secondary);assertEquals(2,world.primary);
        assertEquals(4,observer.events.size(),"Secondary outcomes retriggered the component");
    }
    @Test void failureAndForfeitAlwaysEndPreparationWithoutActivatingTheTip(){
        var world=new World();var runtime=new SpellRuntime(world);var unpaid=new Observer();world.payment=false;
        var failed=cast(runtime,world,unpaid,true);runtime.tick();runtime.tick();assertEquals(SpellRuntime.Status.COST_FAILED,failed.status());
        assertEquals(List.of("prepare","end:COST_FAILED"),unpaid.events);
        world.payment=true;world.random=0;var forfeit=new Observer();var forfeited=cast(runtime,world,forfeit,false);runtime.tick();runtime.tick();
        assertEquals(SpellRuntime.Status.FORFEITED,forfeited.status());assertEquals(List.of("prepare","end:FORFEITED"),forfeit.events);assertEquals(0,world.primary);
    }
    @Test void closingTheRuntimeCancelsFiniteEquipmentContinuations(){
        var world=new World();var runtime=new SpellRuntime(world);var observer=new Observer();observer.rider=true;
        cast(runtime,world,observer,true);runtime.tick();runtime.tick();runtime.close();
        assertThrows(IllegalStateException.class,runtime::tick);assertEquals(0,world.secondary);assertEquals(0,runtime.activeCasts());
    }
    @Test void actualMitigationNotifiesForSecondaryIncomingDamageAndSourceRemovalRevokesFutureBindings() {
        var world=new World();var runtime=new SpellRuntime(world);var enabled=new java.util.concurrent.atomic.AtomicBoolean(true);
        var prevented=new ArrayList<Double>();
        var binding=new SpellEffects.Binding(id("worn/ward"),List.of(new SpellTrigger(id("worn/hit"),SpellTriggerTypes.ARMOR_DAMAGE_CALCULATING,List.of())),
                List.of(new SpellEffects.Action(id("reduce_pending_damage"),Map.of("amount",new com.quzzar.vestige.magic.expression.SpellValue.Constant(2)),Map.of())),20,2);
        var ability=new ItemAbilityDefinition(id("worn"),TraitProfile.empty(),Map.of(),List.of(),List.of(new SpellTrigger(id("use"),SpellTriggerTypes.INTERACT,List.of())),
                List.of(new SpellEffects.InstallBinding(binding,TargetSpec.self())),ItemAbilityDefinition.Activation.REACTIVE);
        var source=new CastReservation() {
            public boolean valid(){return true;}public void commit(){}public boolean continues(){return enabled.get();}
            public CastObserver observer(){return new CastObserver(){public void mitigated(double amount,SpellRuntime.Context c){prevented.add(amount);}};}
        };
        runtime.activate(ability,SpellEvent.of(SpellTriggerTypes.INTERACT,world.actor,null),List.of(),source);
        var first=new SpellEvent.PendingOutcome(5);
        runtime.emit(new SpellEvent(SpellTriggerTypes.ARMOR_DAMAGE_CALCULATING,world.actor,Optional.of(new SpellSubject.Entity(world.actor)),Optional.of(first),CausalChain.start().asSecondary()));
        assertEquals(3,first.amount());assertEquals(List.of(2d),prevented);
        enabled.set(false);var second=new SpellEvent.PendingOutcome(5);
        runtime.emit(new SpellEvent(SpellTriggerTypes.ARMOR_DAMAGE_CALCULATING,world.actor,Optional.of(new SpellSubject.Entity(world.actor)),Optional.of(second),CausalChain.start()));
        assertEquals(5,second.amount());assertEquals(List.of(2d),prevented);
    }
    private static final class Observer implements CastObserver {
        final List<String> events=new ArrayList<>();boolean rider;
        public void preparing(SpellRuntime.Context c){events.add("prepare");}
        public void activated(SpellRuntime.Context c){events.add("activate:"+c.paidMana());if(rider)c.emitSecondary(List.of(new SpellEffects.Delay(3),HIT),c.target());}
        public void resolved(Kind k,double amount,SpellRuntime.Context c){events.add(k.name());}
        public void ended(SpellRuntime.Status s){events.add("end:"+s);}
    }
    private static final class World implements SpellWorld {
        UUID actor=UUID.randomUUID();boolean payment=true;double random=.99;int primary,secondary;
        public ConditionContext conditions(SpellRuntime.Context c){return new ConditionContext(){public Optional<ConditionValue> value(ResourceLocation p){return Optional.empty();}public boolean isTagged(ResourceLocation p,ResourceLocation t){return false;}public boolean matches(ResourceLocation p,ResourceLocation t){return false;}public double random(){return random;}};}
        public List<SpellSubject> select(TargetSpec t,SpellRuntime.Context c){return List.of(c.target());}
        public boolean pay(List<SpellCost> costs,SpellRuntime.Context c){if(!payment)return false;c.paidMana(6);return true;}
        public boolean active(SpellRuntime.Context c){return true;}
        public void forfeit(SpellRuntime.Context c){}
        public boolean execute(SpellEffects.Action a,SpellRuntime.Context c){if(c.cause().secondary())secondary++;else primary++;c.resolved(CastObserver.Kind.DAMAGE,1);return true;}
        public Optional<ManifestationHandle> manifest(SpellEffects.Manifestation m,Map<String,Double> v,SpellRuntime.Context c){return Optional.empty();}
    }
}
