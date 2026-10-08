package com.quzzar.vestige.apparatus;

import com.google.gson.JsonParser;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.recipeviewer.HourglassDisplays;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.travel.PositionTrail;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class HourglassTest {
    static ItemAbilityDefinition ability() {
        try(var input=new InputStreamReader(Objects.requireNonNull(HourglassTest.class.getResourceAsStream("/data/vestige/item_abilities/kairotic_hourglass.json")))) {
            return SpellJson.readAbility(HourglassData.FAMILY,JsonParser.parseReader(input).getAsJsonObject());
        } catch(Exception failure) { throw new AssertionError(failure); }
    }
    static RitualInputs inputs(List<ItemStack> offerings,List<ItemStack> materials,LeylineShaping.Geometry geometry) {
        return new RitualInputs(geometry,java.util.stream.IntStream.range(0,4).mapToObj(i -> new RitualInputs.Node(i*2,geometry.offset(i*2),offerings.get(i),materials.get(i))).toList());
    }
    @Test void allThirtySixViewerRecipesPreserveOfferingSocketPairsUnderEveryRotation() {
        var displays=HourglassDisplays.entries();assertEquals(36,displays.size());
        for(var display:displays) {
            var offerings=display.offerings().stream().map(o -> new ItemStack(BuiltInRegistries.ITEM.get(o.ingredient().items().getFirst()))).toList();
            var materials=new ArrayList<ItemStack>(Collections.nCopies(4,ItemStack.EMPTY));display.imbuements().forEach(m -> materials.set(m.seat()/2,m.stack()));
            for(int rotation=0;rotation<4;rotation++) {
                var o=new ArrayList<>(offerings);var m=new ArrayList<>(materials);Collections.rotate(o,rotation);Collections.rotate(m,rotation);
                assertTrue(ItemStack.matches(display.output(),HourglassRecipe.result(inputs(o,m,HourglassData.NEUTRAL)).orElseThrow()));
            }
            var mirrored=new ArrayList<>(offerings);Collections.reverse(mirrored);
            assertTrue(HourglassRecipe.result(inputs(mirrored,materials,HourglassData.NEUTRAL)).isEmpty());
            var variant=HourglassData.read(display.output()).orElseThrow();
            assertEquals(variant.durability(),display.output().getMaxDamage());
            assertTrue(MagicAdjectives.words(HourglassData.FAMILY,variant.selections()).stream().noneMatch(w -> w.equals("Interwoven")));
        }
    }
    @Test void intervalCostAndDurabilityComposeOnceWithSharedTraitsAndResourceValues() {
        var base=ability();
        for(var v:HourglassData.variants()) {
            double temporal=v.choices().contains(HourglassData.Choice.FLEETING) ? 2d/3 : v.choices().contains(HourglassData.Choice.ENDURING) ? 1.2 : 1;
            double mana=60*(v.choices().contains(HourglassData.Choice.FLEETING) ? .75 : v.choices().contains(HourglassData.Choice.ENDURING) ? 1.12 : 1)
                    *(v.choices().contains(HourglassData.Choice.FRUGAL) ? .75 : v.choices().contains(HourglassData.Choice.REINFORCED) ? 1.25 : 1);
            assertEquals(300*temporal,MagicResolution.resolve(base,v.modifiers(base)).variable(VestigeMainMod.location("return_ticks")),1e-9);
            double exchanged=v.choices().stream().anyMatch(c -> c.group==2) ? Math.min(30,.75*mana) : 0;
            assertEquals(Math.floor(mana-exchanged+.5),v.costs(base).stream().filter(SpellCost.Mana.class::isInstance).map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum());
            if(v.choices().contains(HourglassData.Choice.ERUDITE))assertTrue(v.costs(base).contains(new SpellCost.Experience(ResourceValuation.experienceForMana(exchanged))));
            var modifiers=new ArrayList<>(v.modifiers(base));modifiers.add(new TraitModifier(VestigeMainMod.location("time"),TraitModifier.Operation.MULTIPLY,4));
            assertEquals(1200*temporal,MagicResolution.resolve(base,modifiers).variable(VestigeMainMod.location("return_ticks")),1e-9);
        }
    }
    @Test void savedGeometryChangesTraitsAndIsAppliedBeforeTheCappedManaExchange() {
        var v=new HourglassData.Variant(Set.of(HourglassData.Choice.ERUDITE),new LeylineShaping.Geometry(4,LeylineShaping.Shape.DIAGONAL,3,2,LeylineShaping.Shape.CROSS,0,0));
        var base=ability();double m=60*v.layout(base).cost();double e=Math.min(30,.75*m);
        assertEquals(List.of(new SpellCost.Mana(Math.floor(m-e+.5)),new SpellCost.Experience(ResourceValuation.experienceForMana(e))),v.costs(base));
        assertNotEquals(300,MagicResolution.resolve(base,v.modifiers(base)).variable(VestigeMainMod.location("return_ticks")));
        assertEquals(v,HourglassData.read(HourglassData.create(v)).orElseThrow());
    }
    @Test void knownIncompatiblePaidEffectsRejectButNeutralPairsRemainNeutral() {
        var offerings=HourglassRecipe.ingredients().stream().map(ItemStack::new).toList();
        for(var pair:List.of(Map.entry(0,Items.STONE),Map.entry(0,Items.REDSTONE_BLOCK),Map.entry(1,Items.DIAMOND_BLOCK),Map.entry(3,Items.LODESTONE))) {
            var materials=new ArrayList<ItemStack>(Collections.nCopies(4,ItemStack.EMPTY));materials.set(pair.getKey(),new ItemStack(pair.getValue()));
            assertTrue(HourglassRecipe.result(inputs(offerings,materials,HourglassData.NEUTRAL)).isEmpty());
        }
        var neutral=new ArrayList<ItemStack>(Collections.nCopies(4,new ItemStack(Items.WHITE_WOOL)));
        assertTrue(HourglassRecipe.result(inputs(offerings,neutral,HourglassData.NEUTRAL)).isPresent());
    }
    @Test void malformedAndConflictingSelectionsOrDurabilityCannotGrantAnAbility() {
        var stack=HourglassData.create(new HourglassData.Variant(Set.of(HourglassData.Choice.ERUDITE),HourglassData.NEUTRAL));
        var tag=stack.get(DataComponents.CUSTOM_DATA).copyTag();tag.getCompound("vestige_hourglass_geometry").putInt("height",500);
        stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));assertTrue(HourglassData.read(stack).isEmpty());
        assertThrows(IllegalArgumentException.class,() -> new HourglassData.Variant(Set.of(HourglassData.Choice.FLEETING,HourglassData.Choice.ENDURING),HourglassData.NEUTRAL));
        stack=HourglassData.create(new HourglassData.Variant(Set.of(),HourglassData.NEUTRAL));stack.set(DataComponents.MAX_DAMAGE,100);assertTrue(HourglassData.read(stack).isEmpty());
    }
    @Test void historySelectsAtOrBeforeTheRequestedTickAndFallsBackToOldestAvailable() {
        var trail=new PositionTrail();var dimension=ResourceLocation.parse("minecraft:overworld");
        for(int tick=0;tick<=400;tick++)trail.record(tick,dimension,new Vec3(tick,1,0),300);
        assertEquals(100,trail.destination(400,300).orElseThrow().tick());assertEquals(301,trail.size());
        trail=new PositionTrail();trail.record(100,dimension,new Vec3(1,2,3),300);trail.record(200,dimension,new Vec3(4,5,6),300);
        assertEquals(100,trail.destination(200,300).orElseThrow().tick());
        trail.record(200,dimension,new Vec3(7,8,9),300);assertEquals(2,trail.size());
        trail.record(201,ResourceLocation.parse("minecraft:the_nether"),Vec3.ZERO,300);assertEquals(1,trail.size());
    }
    @Test void largeTraitWindowsRetainBoundedMemoryWithoutClampingRequestedTime() {
        var trail=new PositionTrail();var dimension=ResourceLocation.parse("minecraft:overworld");
        for(int tick=0;tick<20000;tick++)trail.record(tick,dimension,new Vec3(tick,0,0),20000);
        assertTrue(trail.size()<=PositionTrail.MAX_SAMPLES);assertEquals(0,trail.destination(19999,20000).orElseThrow().tick());
        assertEquals(19989,trail.destination(19999,10).orElseThrow().tick());
    }
    @Test void typedExperienceParsesAndUsesTheSameCostShapingGrammar() {
        assertEquals(List.of(new SpellCost.Experience(30)),SpellJson.readCosts(JsonParser.parseString("[{\"type\":\"experience\",\"amount\":30}]").getAsJsonArray()));
        var shaping=new CastShaping(1,false,new CastShaping.CostAdjustment(Map.of("experience",1.5),List.of(),0,0));
        assertEquals(List.of(new SpellCost.Experience(45)),shaping.costs(List.of(new SpellCost.Experience(30))));
        assertThrows(IllegalArgumentException.class,() -> new SpellCost.Experience(0));
    }
}
