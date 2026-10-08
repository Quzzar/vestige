package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays;
import com.quzzar.vestige.travel.StandingStoneRecipe;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderedConstructionTest {
    private static ItemStack shard() {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        var ingredients=AttunementShardItem.ingredients();var nodes=new ArrayList<RitualInputs.Node>();
        for(int i=0;i<8;i++) nodes.add(new RitualInputs.Node(i,geometry.offset(i),i<ingredients.size()
                ? new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))) : ItemStack.EMPTY,ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(geometry,nodes));
    }
    private static List<ItemStack> seats(List<Item> ingredients,int rotation) {
        var seats=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));
        for(int i=0;i<ingredients.size();i++) seats.set((i*2+rotation*2)%8,new ItemStack(ingredients.get(i)));
        for(int i=0;i<8;i+=2) if(seats.get(i).is(ScrollItems.ATTUNEMENT_SHARD.get())) seats.set(i,shard());
        return seats;
    }
    @Test void publicDisplaysDescribeTheActualOrderedPatternsAndTheShardStaysUnordered() {
        for(var entry:List.of(RitualDisplays.homeboundEye(),RitualDisplays.whisperingShell(),RitualDisplays.fluxedFlint(),RitualDisplays.dissentientDiamond())) {
            assertFalse(entry.shapeless());assertEquals(4,entry.capacity());
            var shown=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));
            entry.offerings().forEach(o->shown.set(o.seat(),RitualDisplays.alternatives(o.ingredient()).getFirst()));
            if(shown.getFirst().is(ScrollItems.ATTUNEMENT_SHARD.get())) shown.set(0,shard());
            if(entry.output().is(ScrollItems.HOMEBOUND_EYE.get())) assertTrue(HomeboundEyeRecipe.matches(shown));
            else if(entry.output().is(ScrollItems.WHISPERING_SHELL.get())) assertTrue(WhisperingShellRecipe.result(shown).isPresent());
            else if(entry.output().is(ScrollItems.DISSENTIENT_DIAMOND.get())) assertTrue(DissentientDiamondRecipe.create(shown).isPresent());
            else assertTrue(FluxedFlintRecipe.create(shown).isPresent());
        }
        assertTrue(RitualDisplays.shard().shapeless());
    }
    @Test void homeboundAndFlintAcceptFourRotationsButRejectWrongPositionsAndInvalidBindings() {
        for(int rotation=0;rotation<4;rotation++) {
            var eye=seats(HomeboundEyeRecipe.ingredients(),rotation); assertTrue(HomeboundEyeRecipe.matches(eye));
            var flint=seats(FluxedFlintRecipe.ingredients(),rotation);assertTrue(FluxedFlintRecipe.create(flint).isPresent());
            Collections.swap(eye,(rotation*2+2)%8,(rotation*2+6)%8);assertFalse(HomeboundEyeRecipe.matches(eye));
            Collections.swap(flint,(rotation*2)%8,(rotation*2+2)%8);assertTrue(FluxedFlintRecipe.create(flint).isEmpty());
        }
        var invalid=seats(HomeboundEyeRecipe.ingredients(),0);invalid.set(0,new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()));assertFalse(HomeboundEyeRecipe.matches(invalid));
    }
    @Test void diamondAndFlintRequireSeparateDuplicateOfferingsAndRetireTheOldComposition() {
        for(int rotation=0;rotation<4;rotation++) {
            var diamond=seats(DissentientDiamondRecipe.ingredients(),rotation);
            assertEquals(1,DissentientDiamondRecipe.create(diamond).orElseThrow().getCount());
            Collections.swap(diamond,rotation*2,(rotation*2+2)%8);assertTrue(DissentientDiamondRecipe.create(diamond).isEmpty());
        }
        var diamond=seats(DissentientDiamondRecipe.ingredients(),0);diamond.set(2,new ItemStack(Items.GUNPOWDER,2));diamond.set(6,ItemStack.EMPTY);
        assertTrue(DissentientDiamondRecipe.create(diamond).isEmpty());
        var flint=seats(FluxedFlintRecipe.ingredients(),0);flint.set(2,new ItemStack(Items.NETHERITE_INGOT,2));flint.set(6,ItemStack.EMPTY);
        assertTrue(FluxedFlintRecipe.create(flint).isEmpty());
        assertTrue(FluxedFlintRecipe.create(seats(List.of(Items.FLINT,Items.DIAMOND_BLOCK,Items.NETHERITE_INGOT,Items.ECHO_SHARD),0)).isEmpty());
    }
    @Test void standingStoneRequiresOppositeShardAndPearlAndTwoSeparateMatchingBodies() {
        for(int rotation=0;rotation<4;rotation++) {
            var stone=seats(StandingStoneRecipe.ingredients(),rotation);assertTrue(StandingStoneRecipe.result(stone).isPresent());
            Collections.swap(stone,(rotation*2+2)%8,(rotation*2+4)%8);assertTrue(StandingStoneRecipe.result(stone).isEmpty());
        }
        var wrong=seats(StandingStoneRecipe.ingredients(),0);wrong.set(6,new ItemStack(Items.TUFF));assertTrue(StandingStoneRecipe.result(wrong).isEmpty());
        wrong=seats(StandingStoneRecipe.ingredients(),0);wrong.set(2,new ItemStack(Items.STONE_BRICKS,2));wrong.set(6,ItemStack.EMPTY);assertTrue(StandingStoneRecipe.result(wrong).isEmpty());
    }
}
