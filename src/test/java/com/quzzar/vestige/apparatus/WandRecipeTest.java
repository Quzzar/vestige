package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WandRecipeTest {
    private static ItemStack source(int degree,double cost) {
        return ScrollItems.shapedScroll(VestigeMainMod.location("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,cost),
                List.of(new Spellshaping.Selection(VestigeMainMod.location("reaching"),degree)));
    }
    private static List<ItemStack> seats(WandComponents.Base base,MagicalThreadRecipe.Type thread,ItemStack scroll,int rotation) {
        var seats=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));
        seats.set(rotation,new ItemStack(base.ingredient()));seats.set((rotation+1)%8,new ItemStack(thread.item()));
        for (int offset:List.of(2,4,6)) seats.set((rotation+offset)%8,scroll.copyWithCount(1));
        return seats;
    }
    @Test void everyBaseAndThreadMatchAllFourRotationsButNotReflections() {
        for (var base:WandComponents.Base.values()) for (var core:MagicalThreadRecipe.types()) for (int r=0;r<8;r+=2) {
            var inputs=seats(base,core,source(1,1),r);var matched=WandRecipe.match(inputs).orElseThrow();
            assertEquals(base,matched.base());assertEquals(core,matched.thread());assertEquals(5,matched.occupied().size());
            var reversed=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));
            for (int i=0;i<8;i++) reversed.set((8-i)%8,inputs.get(i));
            assertTrue(WandRecipe.match(reversed).isEmpty());
        }
    }
    @Test void namesCannotSubstituteForExactSpellShapingOrIngredientCounts() {
        var inputs=seats(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,source(1,1),0);
        for (var different:List.of(source(2,1),source(1,1.2),ScrollItems.scroll(VestigeMainMod.location("pf2_shield")),new ItemStack(Items.PAPER))) {
            inputs.set(4,different);assertTrue(WandRecipe.match(inputs).isEmpty());
        }
        inputs.set(4,source(1,1));inputs.set(6,ItemStack.EMPTY);inputs.set(2,source(1,1).copyWithCount(3));
        assertTrue(WandRecipe.match(inputs).isEmpty());
        inputs.set(2,source(1,1));inputs.set(6,source(1,1));inputs.set(5,new ItemStack(Items.PAPER));
        assertTrue(WandRecipe.match(inputs).isEmpty());
        inputs.set(5,ItemStack.EMPTY);inputs.set(3,new ItemStack(Items.DIRT));
        assertTrue(WandRecipe.match(inputs).isEmpty(),"Unsupported items cannot fill the optional tip seat");
    }
    @Test void theWandCarriesOneSourceVariantAndItsVerifiedCapacity() {
        var scroll=source(2,1.2);var original=ScrollItems.scroll(scroll).orElseThrow();
        for (var base:WandComponents.Base.values()) for (var thread:MagicalThreadRecipe.types()) {
            var wand=WandData.create(base,thread,scroll);var bound=WandData.binding(wand).orElseThrow();
            assertEquals(original,bound.scroll());assertEquals(base,bound.base());assertEquals(thread,bound.thread());
            assertEquals(WandComponents.durability(base,thread),wand.getMaxDamage());assertEquals(1,wand.getMaxStackSize());
            wand.setDamageValue(wand.getMaxDamage()-1);assertTrue(WandData.binding(wand).isPresent());
            wand.set(DataComponents.MAX_DAMAGE,999);assertTrue(WandData.binding(wand).isEmpty());
        }
    }
    @Test void malformedVersionComponentIdsAndScrollPayloadReject() {
        var clean=WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,source(1,1));
        var version=clean.copy();CustomData.update(DataComponents.CUSTOM_DATA,version,t -> t.putInt("vestige_wand_version",2));
        var base=clean.copy();CustomData.update(DataComponents.CUSTOM_DATA,base,t -> t.putString("base","unknown"));
        var thread=clean.copy();CustomData.update(DataComponents.CUSTOM_DATA,thread,t -> t.putString("thread","minecraft:string"));
        var magic=clean.copy();CustomData.update(DataComponents.CUSTOM_DATA,magic,t -> t.getCompound("scroll").putString("vestige_spell",""));
        var injected=clean.copy();CustomData.update(DataComponents.CUSTOM_DATA,injected,t -> t.getCompound("scroll").putString("effects","damage"));
        for (var invalid:List.of(version,base,thread,magic,injected,clean.copyWithCount(2))) assertTrue(WandData.binding(invalid).isEmpty());
    }
}
