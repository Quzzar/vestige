package com.quzzar.vestige.magic.runtime;
import com.quzzar.vestige.magic.definition.SpellCost;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class CastShapingTest {
    @Test void everyExistingPaymentScalesAndRoundsAfterComposition() {
        var iron=ResourceLocation.parse("minecraft:iron_ingot");var pick=ResourceLocation.parse("minecraft:iron_pickaxe");
        var base=List.<SpellCost>of(new SpellCost.Time(20),new SpellCost.Cooldown(100),new SpellCost.Mana(2.4),new SpellCost.Mana(2.4),
                new SpellCost.Health(3),new SpellCost.Hunger(3),new SpellCost.Material(iron,SpellCost.Material.Operation.CONSUME,3),new SpellCost.Material(pick,SpellCost.Material.Operation.DAMAGE,3));
        assertEquals(List.of(new SpellCost.Time(24),new SpellCost.Cooldown(120),new SpellCost.Mana(6),new SpellCost.Health(4),new SpellCost.Hunger(4),
                new SpellCost.Material(iron,SpellCost.Material.Operation.CONSUME,4),new SpellCost.Material(pick,SpellCost.Material.Operation.DAMAGE,4)),new CastShaping(1.2,true).costs(base));
        assertEquals(List.of(new SpellCost.Health(4)),new CastShaping(1.2,true).costs(List.of(new SpellCost.Health(4))));
        assertEquals(List.of(),new CastShaping(.65,true).costs(List.of(new SpellCost.Mana(.2))));
        assertSame(base,CastShaping.NONE.costs(base));
    }
    @Test void typedAdjustmentsAndManaExchangesComposeBeforeOneFinalRounding() {
        var shape=new CastShaping(1.2,true,new CastShaping.CostAdjustment(java.util.Map.of("mana",1.1),List.of(new SpellCost.Mana(2)),.25,.25));
        assertEquals(List.of(new SpellCost.Mana(7),new SpellCost.Health(2),new SpellCost.Hunger(1)),shape.costs(List.of(new SpellCost.Mana(8))));
        assertThrows(IllegalArgumentException.class,()->new CastShaping.CostAdjustment(java.util.Map.of(),List.of(),.8,.3));
        var duplicates=new CastShaping(1,true,new CastShaping.CostAdjustment(java.util.Map.of(),List.of(new SpellCost.Mana(.4)),0,0));
        assertEquals(List.of(new SpellCost.Mana(1)),duplicates.costs(List.of(new SpellCost.Mana(.4))));
    }
    @Test void finalAmountsRoundWhileRatiosSpeedsAndIntermediateProductsKeepPrecision() {
        var shaped=new CastShaping(1,true);
        assertEquals(5,shaped.value("amount",2.25*2));
        assertEquals(.15,shaped.value("weapon_fraction",.15));assertEquals(.6,shaped.value("speed",.6));
        assertEquals(-1,shaped.amount(-1.4));assertThrows(IllegalArgumentException.class,()->new CastShaping(Double.NaN,true));
    }
}
