package com.quzzar.vestige.apparatus;

import com.google.gson.JsonParser;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StaffDataTest {
    private static ResourceLocation id(String path) { return VestigeMainMod.location(path); }
    private static SpellDefinition spell(String name) {
        try(var reader=new InputStreamReader(Objects.requireNonNull(StaffDataTest.class.getResourceAsStream("/data/vestige/runtime_spells/"+name+".json")))) {
            return SpellJson.read(id(name),JsonParser.parseReader(reader).getAsJsonObject());
        } catch(java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
    }
    @Test void affinitiesAreOpenButScalingTraitsAndIncompatibleBaseSpellsReject() {
        for(var name:List.of("amplify","range","area")) assertThrows(IllegalArgumentException.class,() -> StaffData.create(id(name)));
        assertTrue(StaffData.affinityAllowed(ResourceLocation.parse("addon:fire")));
        var staff=StaffData.create(id("fire"));
        assertThrows(IllegalArgumentException.class,() -> StaffData.bind(staff,ScrollItems.scroll(id("pf2_heal")),spell("pf2_heal")));
        assertThrows(IllegalArgumentException.class,() -> StaffData.bind(staff,ScrollItems.scroll(id("fireball")),spell("pf2_heal")));
        assertDoesNotThrow(() -> StaffData.bind(staff,ScrollItems.scroll(id("fireball")),spell("fireball")));
    }
    @Test void selectionReplacementAndExpansionPreserveOtherVariantsAndUsedWear() {
        var first=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,.8),List.of(new Spellshaping.Selection(id("reaching"),2)));
        var staff=StaffData.bind(StaffData.create(id("fire")),first,spell("fireball"));staff.setDamageValue(17);
        var second=ScrollItems.scroll(id("firebolt"));
        staff=StaffData.bind(StaffData.select(staff,1),second,spell("firebolt"));
        var expanded=StaffData.expand(StaffData.expand(staff));var data=StaffData.binding(expanded).orElseThrow();
        assertEquals(6,data.capacity());assertEquals(1,data.selected());assertEquals(17,expanded.getDamageValue());
        assertEquals(120,expanded.getMaxDamage());assertEquals(ScrollItems.scroll(first),data.slots().get(0));
        assertEquals(ScrollItems.scroll(second),data.slots().get(1));assertTrue(data.slots().subList(2,6).stream().allMatch(Optional::isEmpty));
        assertThrows(IllegalArgumentException.class,() -> StaffData.expand(expanded));
        var replacement=StaffData.bind(expanded,first,spell("fireball"));
        assertEquals(data.slots().get(0),StaffData.binding(replacement).orElseThrow().slots().get(0));
        assertEquals(ScrollItems.scroll(first),StaffData.binding(replacement).orElseThrow().active());
        assertEquals(17,replacement.getDamageValue());assertFalse(ItemStack.matches(first,second));
    }
    @Test void malformedSlotsSelectionCapacityAndInjectedExecutablePayloadReject() {
        var valid=StaffData.bind(StaffData.create(id("fire")),ScrollItems.scroll(id("fireball")),spell("fireball"));
        var wrongVersion=valid.copy();CustomData.update(DataComponents.CUSTOM_DATA,wrongVersion,t -> t.putInt("vestige_staff_version",2));
        var wrongSize=valid.copy();CustomData.update(DataComponents.CUSTOM_DATA,wrongSize,t -> t.getList("slots",Tag.TAG_COMPOUND).add(new CompoundTag()));
        var wrongType=valid.copy();CustomData.update(DataComponents.CUSTOM_DATA,wrongType,t -> {var list=new ListTag();list.add(StringTag.valueOf("spell"));list.add(StringTag.valueOf("spell"));t.put("slots",list);});
        var selected=valid.copy();CustomData.update(DataComponents.CUSTOM_DATA,selected,t -> t.putInt("selected",2));
        var executable=valid.copy();CustomData.update(DataComponents.CUSTOM_DATA,executable,t -> t.getList("slots",Tag.TAG_COMPOUND).getCompound(0).putString("effects","damage"));
        var durability=valid.copy();durability.set(DataComponents.MAX_DAMAGE,999);
        for(var bad:List.of(wrongVersion,wrongSize,wrongType,selected,executable,durability,valid.copyWithCount(2))) assertTrue(StaffData.binding(bad).isEmpty());
        assertThrows(IllegalArgumentException.class,() -> StaffData.select(valid,-1));
    }
    @Test void removableSourcesRoundTripShapingWithoutChangingSelectionWearOrOtherBindings() {
        var source=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,.8),List.of(new Spellshaping.Selection(id("reaching"),2)));
        var staff=StaffData.bind(StaffData.create(id("fire")),source,spell("fireball"));staff.setDamageValue(9);
        staff=StaffData.bind(StaffData.select(staff,1),ScrollItems.scroll(id("firebolt")),spell("firebolt"));
        var returned=StaffData.source(staff,0);assertTrue(ItemStack.matches(source,returned));
        var emptied=StaffData.withScrolls(staff,List.of(ItemStack.EMPTY,StaffData.source(staff,1)));
        assertTrue(StaffData.source(emptied,0).isEmpty());assertEquals(1,StaffData.binding(emptied).orElseThrow().selected());
        assertEquals(9,emptied.getDamageValue());assertTrue(ItemStack.matches(StaffData.source(staff,1),StaffData.source(emptied,1)));
        var restored=StaffData.withScrolls(emptied,List.of(returned,StaffData.source(emptied,1)));assertTrue(ItemStack.matches(staff,restored));
        var original=staff;
        assertThrows(IllegalArgumentException.class,() -> StaffData.withScrolls(original,List.of(returned.copyWithCount(2),ItemStack.EMPTY)));
        assertThrows(IllegalArgumentException.class,() -> StaffData.withScrolls(original,List.of(returned)));
    }

}
