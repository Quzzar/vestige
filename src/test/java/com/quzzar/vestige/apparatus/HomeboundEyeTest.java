package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.world.ManaPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HomeboundEyeTest {
    @Test void bindingRoundTripPreservesSeparateOriginAndPaymentAndRejectsMalformedData() {
        for (var payment : HomeboundEyeItem.Payment.values()) {
            var pos = new BlockPos(-200, 64, 800);
            var item = HomeboundEyeItem.bound("a".repeat(64), Level.NETHER, pos, payment);
            assertEquals(new HomeboundEyeItem.Binding("a".repeat(64), Level.NETHER, pos, payment), HomeboundEyeItem.binding(item).orElseThrow());
            assertEquals(30, item.getMaxDamage());
            CustomData.update(DataComponents.CUSTOM_DATA, item, tag -> tag.putIntArray("origin", new int[]{1, 2}));
            assertTrue(HomeboundEyeItem.binding(item).isEmpty());
        }
        assertTrue(HomeboundEyeItem.binding(new ItemStack(ScrollItems.HOMEBOUND_EYE.get())).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> HomeboundEyeItem.bound("short", Level.OVERWORLD, BlockPos.ZERO, HomeboundEyeItem.Payment.MANA));
    }
    @Test void publicRecipeUsesFourSeparateOfferingsAndPaymentHasOneLocalSelector() {
        var display = com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays.homeboundEye();
        assertEquals(4, display.capacity()); assertEquals(4, display.offerings().size());
        assertTrue(display.output().is(ScrollItems.HOMEBOUND_EYE.get()));
        assertEquals(HomeboundEyeItem.Payment.DURABILITY, HomeboundEyeRecipe.payment(ItemStack.EMPTY).orElseThrow());
        assertEquals(HomeboundEyeItem.Payment.MANA, HomeboundEyeRecipe.payment(new ItemStack(Items.AMETHYST_BLOCK)).orElseThrow());
        assertTrue(HomeboundEyeRecipe.payment(new ItemStack(Items.DIAMOND_BLOCK)).isEmpty());
    }
    @Test void manaSnapshotsRejectInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> new ManaPayload(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> new ManaPayload(101));
        assertEquals(0, new ManaPayload(0).amount());
        assertEquals(100, new ManaPayload(100).amount());
    }
}
