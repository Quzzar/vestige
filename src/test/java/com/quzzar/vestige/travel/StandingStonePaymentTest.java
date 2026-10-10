package com.quzzar.vestige.travel;

import com.quzzar.vestige.magic.definition.SpellCost;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StandingStonePaymentTest {
    @Test void approvedTravelExamplesResolveAllFiveResourceRoutesWithoutRoundingTheBudgetEarly() {
        int[][] examples = {{64,15,11,15,2,2}, {256,18,14,18,3,2}, {1024,24,18,24,4,2}, {4096,36,27,36,5,4}, {16384,60,45,60,8,4}};
        for (var example : examples) for (var route : StandingStonePayment.values())
            assertEquals(example[route.ordinal()+1], StandingStoneFare.quote(BlockPos.ZERO, new BlockPos(example[0],0,0), route).amount());
        assertEquals(1, StandingStonePayment.ERUDITE.quote(.01).amount());
        assertEquals(2, StandingStonePayment.HEALTH.quote(30).amount());
        assertEquals(4, StandingStonePayment.HEALTH.quote(30.0001).amount());
        assertEquals(1, StandingStonePayment.HUNGER.quote(7.5).amount());
        assertEquals(2, StandingStonePayment.HUNGER.quote(7.5001).amount());
        assertEquals(11, StandingStonePayment.ERUDITE.quote(14.6).amount());
    }
    @Test void routePersistenceContainsAnIdentityRatherThanAPlayerControlledPrice() {
        for (var route : StandingStonePayment.values()) {
            var tag = new CompoundTag(); tag.putString("vestige_attunement", "same-network"); route.write(tag);
            assertEquals(route, StandingStonePayment.read(tag).orElseThrow());
            assertEquals("same-network", tag.getString("vestige_attunement"));
            assertEquals(3, tag.getAllKeys().size());
            tag.putInt("cost", 0); tag.putDouble("discount", 1);
            assertEquals(route.quote(24), StandingStonePayment.read(tag).orElseThrow().quote(24));
        }
        assertEquals(StandingStonePayment.EXPERIENCE, StandingStonePayment.read(new CompoundTag()).orElseThrow());
        var invalid = new CompoundTag(); invalid.putString("vestige_stone_payment", "health");
        assertTrue(StandingStonePayment.read(invalid).isEmpty());
        invalid.putInt("vestige_stone_payment_version", 1); invalid.putString("vestige_stone_payment", "free");
        assertTrue(StandingStonePayment.read(invalid).isEmpty());
        invalid.putString("vestige_stone_payment", "mana"); invalid.putInt("vestige_stone_payment_version", 2);
        assertTrue(StandingStonePayment.read(invalid).isEmpty());
    }
    @Test void costsRemainSharedNativeTypesAndNamesRemainOneReusedAdjective() {
        var routes=StandingStonePayment.values();
        var words=new String[]{"", "Erudite", "Charged", "Fasting", "Bloodbound"};
        for (int i=0;i<routes.length;i++) assertEquals(words[i], String.join(" ",MagicAdjectives.words(StandingStonePayment.FAMILY,routes[i].adjectives())));
        assertEquals(new SpellCost.Experience(18), StandingStonePayment.ERUDITE.quote(24).cost());
        assertEquals(new SpellCost.Mana(24), StandingStonePayment.MANA.quote(24).cost());
        assertEquals(new SpellCost.Hunger(4), StandingStonePayment.HUNGER.quote(24).cost());
        assertEquals(new SpellCost.Health(2), StandingStonePayment.HEALTH.quote(24).cost());
        assertThrows(IllegalArgumentException.class,()->StandingStonePayment.HEALTH.quote(Double.NaN));
        assertThrows(IllegalArgumentException.class,()->StandingStonePayment.HUNGER.quote(-1));
        assertThrows(IllegalArgumentException.class,()->new StandingStonePayment.Quote(StandingStonePayment.MANA,0));
    }
}
