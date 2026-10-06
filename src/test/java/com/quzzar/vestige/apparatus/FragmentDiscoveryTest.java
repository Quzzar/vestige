package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FragmentDiscoveryTest {
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("vestige",name); }
    private static SpellDefinition spell(String name,double fire,double evocation) {
        return new SpellDefinition(id(name),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("fire"),fire,id("evocation"),evocation)),List.of(),
                List.of(new SpellTrigger(id("cast"),SpellTriggerTypes.INTERACT,List.of())),List.of(new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Constant(1)),Map.of())));
    }
    @Test void mixedFragmentsRequireEveryTraitAndDuplicatesEmphasizeTheirRatings() {
        var onlyFire=spell("pure",9,0);var mixed=spell("mixed",3,1);var other=spell("other",1,5);
        var all=List.of(onlyFire,mixed,other);
        var pool=FragmentDiscovery.pool(all,List.of(id("fire"),id("fire"),id("fire"),id("evocation")));
        assertEquals(List.of(id("mixed"),id("other")),pool.stream().map(c -> c.spell().id()).toList());
        assertEquals(2.5,pool.getFirst().weight()); assertEquals(2,pool.getLast().weight());
        assertEquals(3,FragmentDiscovery.pool(all,List.of(id("fire"),id("fire"),id("fire"),id("fire"))).size());
        assertSame(mixed,FragmentDiscovery.pick(pool,.55).orElseThrow()); assertSame(other,FragmentDiscovery.pick(pool,.56).orElseThrow());
    }
    @Test void allFirePreservesFiveToOneOddsAndRejectsInvalidDrawsOrFragmentCounts() {
        var one=spell("one",1,0);var five=spell("five",5,0);var fragments=List.of(id("fire"),id("fire"),id("fire"),id("fire"));
        var pool=FragmentDiscovery.pool(List.of(one,five),fragments);
        assertEquals(5,pool.getFirst().weight()/pool.getLast().weight());
        assertSame(five,FragmentDiscovery.pick(pool,Math.nextDown(5.0/6)).orElseThrow()); assertSame(one,FragmentDiscovery.pick(pool,Math.nextUp(5.0/6)).orElseThrow());
        assertTrue(FragmentDiscovery.pool(List.of(one),fragments.subList(0,3)).isEmpty()); assertTrue(FragmentDiscovery.pick(List.of(),.5).isEmpty());
        assertThrows(IllegalArgumentException.class,()->FragmentDiscovery.pick(pool,1));assertThrows(IllegalArgumentException.class,()->FragmentDiscovery.pick(pool,Double.NaN));
    }
}
