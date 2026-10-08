package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StaffRecipeTest {
    private static List<ItemStack> seats() { return new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY)); }
    @Test void constructionRequiresOneMundaneShaftAndItsFixedRelativeOrder() {
        for(var shaft:MundaneStaffs.Shaft.values()) {
            var original=seats();original.set(0,new ItemStack(MundaneStaffs.item(shaft)));
            original.set(4,ScrollItems.fragment(VestigeMainMod.location("fire")));original.set(6,new ItemStack(Items.AMETHYST_SHARD));
            original.set(1,new ItemStack(Items.IRON_INGOT));original.set(3,new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));
            for(int turn=0;turn<8;turn+=2) {
                var rotated=seats();for(int i=0;i<8;i++) rotated.set((i+turn)%8,original.get(i));
                assertEquals(StaffRecipe.Operation.CONSTRUCT,StaffRecipe.match(rotated,8).orElseThrow().operation());
                assertTrue(StaffRecipe.match(rotated,4).isEmpty());
            }
        }
        var invalid=seats();invalid.set(0,new ItemStack(Items.STICK));invalid.set(4,ScrollItems.fragment(VestigeMainMod.location("fire")));
        invalid.set(6,new ItemStack(Items.AMETHYST_SHARD));invalid.set(1,new ItemStack(Items.IRON_INGOT));invalid.set(3,new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));
        assertTrue(StaffRecipe.match(invalid,8).isEmpty());
    }
    @Test void scrollInsertionIsNotARitualAndCapacityUpgradesRemainStrict() {
        var data=StaffData.create(VestigeMainMod.location("fire"));var slots=seats();slots.set(0,data);
        slots.set(2,ScrollItems.scroll(VestigeMainMod.location("fireball")));slots.set(4,new ItemStack(Items.AMETHYST_SHARD));
        assertTrue(StaffRecipe.match(slots,4).isEmpty());
        assertTrue(StaffRecipe.match(slots,8).isEmpty());slots.set(2,new ItemStack(Items.DIAMOND));
        slots.set(6,new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));assertEquals(StaffRecipe.Operation.EXPAND,StaffRecipe.match(slots,4).orElseThrow().operation());
        slots.set(0,StaffData.expand(data));assertTrue(StaffRecipe.match(slots,4).isEmpty());
        slots.set(2,new ItemStack(Items.ECHO_SHARD));slots.set(4,new ItemStack(Items.DIAMOND));slots.set(6,new ItemStack(Items.AMETHYST_SHARD));
        slots.set(1,new ItemStack(Items.IRON_INGOT));slots.set(3,new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));
        assertEquals(StaffRecipe.Operation.EXPAND,StaffRecipe.match(slots,8).orElseThrow().operation());
        slots.set(7,new ItemStack(Items.PAPER));assertTrue(StaffRecipe.match(slots,8).isEmpty());
    }
}
