package com.quzzar.vestige.storage;

import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CraneBagRecipeTest {
    private static ItemStack shard() {
        var g=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,1,LeylineShaping.Shape.DIAGONAL,3,0);
        var ingredients=AttunementShardItem.ingredients();var nodes=new ArrayList<RitualInputs.Node>();
        for(int i=0;i<8;i++)nodes.add(new RitualInputs.Node(i,g.offset(i),i<ingredients.size()?new ItemStack(BuiltInRegistries.ITEM.get(ingredients.get(i))):ItemStack.EMPTY,
                i==0?new ItemStack(Items.IRON_BLOCK):ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(g,nodes));
    }
    private static List<ItemStack> seats(ItemStack shard,int rotation) {
        var seats=new ArrayList<ItemStack>(Collections.nCopies(8,ItemStack.EMPTY));
        for(int i=0;i<4;i++)seats.set((i*2+rotation)%8,i==0?shard.copy():new ItemStack(CraneBagRecipe.ingredients().get(i)));return seats;
    }
    @Test void wholeRotationsKeepExactInheritedKeyAndOnlyKeyMetadata() {
        var shard=shard();var key=AttunementShardItem.signature(shard).orElseThrow().key();shard.set(DataComponents.CUSTOM_NAME,Component.literal("Private source name"));
        for(int rotation=0;rotation<8;rotation+=2) {
            var inputs=seats(shard,rotation);var result=CraneBagRecipe.result(inputs).orElseThrow();
            assertEquals(key,CraneBagItem.key(result).orElseThrow());assertEquals(1,result.getCount());
            assertFalse(result.has(DataComponents.CUSTOM_NAME));assertTrue(inputs.stream().filter(s->!s.isEmpty()).allMatch(s->s.getCount()==1));
        }
    }
    @Test void reflectionsCountsWrongLayersAndForgedShardsReject() {
        var input=seats(shard(),0);Collections.swap(input,2,6);assertTrue(CraneBagRecipe.result(input).isEmpty());
        input=seats(shard(),0);input.get(2).setCount(2);assertTrue(CraneBagRecipe.result(input).isEmpty());
        input=seats(shard(),0);Collections.swap(input,0,1);assertTrue(CraneBagRecipe.result(input).isEmpty());
        input=seats(new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()),0);assertTrue(CraneBagRecipe.result(input).isEmpty());
        var forged=shard();var data=forged.get(DataComponents.CUSTOM_DATA).copyTag();data.putString("key","0".repeat(64));forged.set(DataComponents.CUSTOM_DATA,CustomData.of(data));
        assertTrue(CraneBagRecipe.result(seats(forged,0)).isEmpty());
    }
    @Test void publicViewerEntryUsesTheActualOrderedPatternAndGenericBagOutput() {
        var display=RitualDisplays.craneBag();assertEquals(4,display.capacity());assertFalse(display.shapeless());assertTrue(display.output().is(ScrollItems.CRANE_BAG.get()));
        assertTrue(CraneBagItem.key(display.output()).isEmpty());
        var offerings=new ArrayList<ItemStack>(Collections.nCopies(8,ItemStack.EMPTY));
        display.offerings().forEach(o->offerings.set(o.seat(),RitualDisplays.alternatives(o.ingredient()).getFirst()));
        offerings.set(0,shard());assertTrue(CraneBagRecipe.result(offerings).isPresent());
    }
}
