package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellRarity;
import com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RitualRecipeShapeTest {
    @Test void authoredEightSlotShapesNeedNotFillTheInnerRing() {
        var items=List.of(Items.PAPER,Items.IRON_INGOT,Items.AMETHYST_SHARD,Items.DIAMOND);
        var parts=new ArrayList<RitualRecipe.Part>();
        for(int i=0;i<4;i++) parts.add(new RitualRecipe.Part(i*2+1,"part_"+i,new RitualRecipe.Ingredient(
                List.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(items.get(i))),List.of())));
        var id=VestigeMainMod.location("test_outer_shape");
        var recipe=new RitualRecipe(id,"Outer shape",8,0xffffff,parts,List.of());
        var seats=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));
        for(int i=0;i<4;i++) seats.set(i*2+1,new ItemStack(items.get(i)));
        assertTrue(recipe.evaluate(seats).correct());
        var display=new RitualDisplays.Entry(id,Optional.of(id),true,SpellRarity.COMMON,8,
                parts.stream().map(p -> new RitualDisplays.Offering(p.seat(),p.ingredient())).toList());
        assertFalse(display.shapeless());
        Collections.rotate(seats,2);assertTrue(recipe.evaluate(seats).correct());
        var wrong=seats.get(1);seats.set(1,seats.get(3));seats.set(3,wrong);
        assertFalse(recipe.evaluate(seats).correct());
    }
}
