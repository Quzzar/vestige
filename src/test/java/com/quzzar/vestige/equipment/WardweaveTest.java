package com.quzzar.vestige.equipment;

import com.google.gson.JsonParser;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.runtime.MagicResolution;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WardweaveTest {
    private static List<ItemStack> offerings(DyeColor color) {
        var items = new ArrayList<ItemStack>();
        for (int i=0;i<8;i++) items.add(new ItemStack(i%2==0?MagicArmorRecipe.wool(color):i==1||i==5?ScrollItems.CALLOUS_THREAD.get():i==3?Items.IRON_INGOT:Items.PUFFERFISH));
        return items;
    }
    @Test void allAuthoredLayoutsUseRealMatcherIncludingColorRotationAndThreadPositions() {
        int checked=0;
        for(int mask=0;mask<16;mask++) for(var color:DyeColor.values()) for(int rotation=0;rotation<8;rotation+=2)
            for(int wool:((mask&2)!=0?new int[]{0,2,4,6}:new int[]{0})) for(int thread:((mask&12)!=0?new int[]{1,5}:new int[]{1})) {
                var seats=offerings(color);var materials=new ArrayList<ItemStack>(Collections.nCopies(8,ItemStack.EMPTY));
                if((mask&1)!=0)materials.set(7,new ItemStack(Items.IRON_BLOCK));
                if((mask&2)!=0)materials.set(wool,new ItemStack(Items.AMETHYST_BLOCK));
                if((mask&4)!=0)materials.set(thread,new ItemStack(Items.COPPER_BLOCK));
                if((mask&8)!=0)materials.set((mask&4)!=0?6-thread:thread,new ItemStack(Items.IRON_BLOCK));
                Collections.rotate(seats,rotation);Collections.rotate(materials,rotation);
                var output=MagicArmorRecipe.result(seats,materials).orElseThrow();
                assertEquals(WardweaveDisplays.choices(mask),WardweaveImbuements.read(output).orElseThrow().choices());
                assertEquals(color.getTextureDiffuseColor(),output.get(DataComponents.DYED_COLOR).rgb());checked++;
            }
        assertEquals(4480,checked);
    }
    @Test void duplicatesUnsupportedPairsAndWrongShapeRejectWithoutChangingInputs() {
        var seats=offerings(DyeColor.BLUE);var materials=new ArrayList<ItemStack>(Collections.nCopies(8,ItemStack.EMPTY));
        materials.set(0,new ItemStack(Items.AMETHYST_BLOCK));materials.set(2,new ItemStack(Items.AMETHYST_BLOCK));
        assertTrue(MagicArmorRecipe.result(seats,materials).isEmpty());
        materials.replaceAll(m->ItemStack.EMPTY);materials.set(1,new ItemStack(Items.COPPER_BLOCK));materials.set(5,new ItemStack(Items.COPPER_BLOCK));
        assertTrue(MagicArmorRecipe.result(seats,materials).isEmpty());
        materials.replaceAll(m->ItemStack.EMPTY);materials.set(1,new ItemStack(Items.IRON_BLOCK));materials.set(5,new ItemStack(Items.IRON_BLOCK));
        assertTrue(MagicArmorRecipe.result(seats,materials).isEmpty());
        materials.replaceAll(m->ItemStack.EMPTY);materials.set(3,new ItemStack(Items.COPPER_BLOCK));
        assertTrue(MagicArmorRecipe.result(seats,materials).isEmpty());
        materials.replaceAll(m->ItemStack.EMPTY);materials.set(0,new ItemStack(Items.RED_WOOL));
        assertTrue(MagicArmorRecipe.result(seats,materials).isEmpty());
        materials.replaceAll(m->ItemStack.EMPTY);Collections.swap(seats,1,3);
        assertTrue(MagicArmorRecipe.result(seats,materials).isEmpty());
        assertTrue(seats.stream().allMatch(s->s.getCount()==1));
    }
    @Test void everyVariantUsesSharedTraitsCostsAndOneCompleteName() throws Exception {
        var ability=SpellJson.readAbility(WardweaveImbuements.ABILITY,JsonParser.parseReader(new InputStreamReader(getClass().getResourceAsStream("/data/vestige/item_abilities/wardweave.json"))).getAsJsonObject());
        var names=new HashSet<String>();
        for(int mask=0;mask<16;mask++) {
            var stack=WardweaveImbuements.create(WardweaveDisplays.choices(mask));var v=WardweaveImbuements.read(stack).orElseThrow();
            var resolved=MagicResolution.resolve(ability,v.modifiers());
            assertEquals(4*((mask&1)!=0?.8:1)*((mask&2)!=0?1.5:1),resolved.variable(VestigeMainMod.location("ward_duration")),1e-9);
            assertEquals(2*((mask&1)!=0?1.5:1)*((mask&4)!=0?.8:1)*((mask&8)!=0?.8:1),resolved.variable(VestigeMainMod.location("protection")),1e-9);
            assertEquals(25,resolved.variable(VestigeMainMod.location("mana_bonus")));
            var cost=(SpellCost.Cooldown)v.shaping().costs(ability.costs()).getFirst();
            assertEquals(Math.round(240*((mask&2)!=0?1.2:1)*((mask&4)!=0?.8:1)),cost.ticks());
            assertEquals((mask&8)!=0?120:80,stack.getMaxDamage());assertEquals(stack.getMaxDamage()/4,v.repair());
            assertTrue(names.add(stack.getHoverName().getString()));
        }
        var v=WardweaveImbuements.read(WardweaveImbuements.create(Set.of(WardweaveImbuements.Choice.WARDED,WardweaveImbuements.Choice.REINFORCED))).orElseThrow();
        var modifiers=new ArrayList<>(v.modifiers());
        for(String trait:List.of("time","force","amplify")) modifiers.add(new TraitModifier(VestigeMainMod.location(trait),TraitModifier.Operation.MULTIPLY,1.25));
        var boosted=MagicResolution.resolve(ability,modifiers);
        assertEquals(3.75,boosted.variable(VestigeMainMod.location("protection")),1e-9);assertEquals(4,boosted.variable(VestigeMainMod.location("ward_duration")),1e-9);
    }
    @Test void untrustedFamiliesDegreesAndCarrierValuesCannotBecomeAbilityVariants() {
        var stack=WardweaveImbuements.create(Set.of(WardweaveImbuements.Choice.REINFORCED));
        stack.set(DataComponents.MAX_DAMAGE,121);assertTrue(WardweaveImbuements.read(stack).isEmpty());
        stack=WardweaveImbuements.create(Set.of(WardweaveImbuements.Choice.REINFORCED));
        var forged = new net.minecraft.nbt.CompoundTag(); forged.putString("vestige_item_imbuements","forged");
        stack.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(forged));
        assertTrue(WardweaveImbuements.read(stack).isEmpty());
        for (String field:List.of("family","degree","id")) {
            stack=WardweaveImbuements.create(Set.of(WardweaveImbuements.Choice.REINFORCED));
            var data=stack.get(DataComponents.CUSTOM_DATA).copyTag();var tag=data.getCompound("vestige_item_imbuements");
            if(field.equals("family"))tag.putString("family","vestige:wayfarer_boots");
            else if(field.equals("degree"))tag.getList("adjustments",10).getCompound(0).putInt("degree",2);
            else tag.getList("adjustments",10).getCompound(0).putString("id","vestige:wardweave/unknown");
            stack.set(DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));
            assertTrue(WardweaveImbuements.read(stack).isEmpty(),field);
        }
        stack=WardweaveImbuements.create(Set.of(WardweaveImbuements.Choice.REINFORCED));stack.setDamageValue(120);assertTrue(WardweaveImbuements.read(stack).isEmpty());
    }
}
