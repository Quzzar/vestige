package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.runtime.MagicResolution;
import com.google.gson.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MagicArmorRecipeTest {
    private static List<ItemStack> recipe(DyeColor color, boolean cinder) {
        var seats = new ArrayList<ItemStack>();
        for (int i = 0; i < 8; i++) seats.add(new ItemStack(i % 2 == 0 ? MagicArmorRecipe.wool(color)
                : i == 1 || i == 5 ? cinder ? ScrollItems.SMOLDERING_THREAD.get() : ScrollItems.CALLOUS_THREAD.get()
                : i == 3 ? cinder ? Items.BLAZE_POWDER : Items.IRON_INGOT : cinder ? Items.MAGMA_CREAM : Items.PUFFERFISH));
        return seats;
    }
    @Test void everyColorAndQuarterTurnCraftsColoredChestArmorWithRealDurability() {
        for (var color : DyeColor.values()) for (boolean cinder : List.of(false, true)) for (int rotation = 0; rotation < 8; rotation += 2) {
            var seats = recipe(color, cinder); Collections.rotate(seats, rotation);
            var result = MagicArmorRecipe.result(seats).orElseThrow();
            assertTrue(result.is(cinder ? MagicEquipment.CINDERWEAVE.get() : MagicEquipment.WARDWEAVE.get()));
            assertEquals(color.getTextureDiffuseColor(), result.get(DataComponents.DYED_COLOR).rgb());
            assertEquals(80, result.getMaxDamage()); assertEquals(0, result.getDamageValue());
            assertEquals(2, ((ArmorItem) result.getItem()).getDefense());
            assertFalse(result.has(DataComponents.CUSTOM_DATA));
        }
    }
    @Test void mixedColorsWrongOfferingsStacksAndWrongLayersRejectWithoutMutation() {
        var seats = recipe(DyeColor.BLUE, false); seats.set(4, new ItemStack(Items.WHITE_WOOL));
        assertTrue(MagicArmorRecipe.result(seats).isEmpty()); assertEquals(1, seats.get(4).getCount());
        seats = recipe(DyeColor.BLUE, false); Collections.rotate(seats, 1); assertTrue(MagicArmorRecipe.result(seats).isEmpty());
        seats = recipe(DyeColor.BLUE, false); seats.get(0).setCount(2); assertTrue(MagicArmorRecipe.result(seats).isEmpty());
        seats = recipe(DyeColor.BLUE, false); seats.set(7, new ItemStack(Items.COD)); assertTrue(MagicArmorRecipe.result(seats).isEmpty());
    }
    @Test void viewerRecipesAreConcreteMatchingColorRecipesWithTheSameActualOutputs() {
        var entries=MagicArmorDisplays.entries();assertEquals(32,entries.size());
        for(var entry:entries) {
            assertFalse(entry.shapeless());assertEquals(8,entry.capacity());
            var seats=new ArrayList<ItemStack>(Collections.nCopies(8,ItemStack.EMPTY));
            for(var offering:entry.offerings()) seats.set(offering.seat(),new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(offering.ingredient().items().getFirst())));
            assertTrue(ItemStack.matches(entry.output(),MagicArmorRecipe.result(seats).orElseThrow()));
        }
    }
    @Test void productionFormulasMatchReviewedTraitVariables() throws IOException {
        try (var reader = new InputStreamReader(getClass().getResourceAsStream("/ability-proposals.json"))) {
            var fixture = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("items");
            for (int i = 0; i < 2; i++) {
                var name = fixture.get(i).getAsJsonObject().get("id").getAsString();
                try (var production = new InputStreamReader(getClass().getResourceAsStream("/data/vestige/item_abilities/" + name + ".json"))) {
                    var definition = SpellJson.readAbility(VestigeMainMod.location(name), JsonParser.parseReader(production).getAsJsonObject());
                    var reviewed = SpellJson.readAbility(VestigeMainMod.location(name), fixture.get(i).getAsJsonObject().getAsJsonObject("definition"));
                    assertEquals(reviewed.variables(), definition.variables()); assertEquals(reviewed.traits().ratings(), definition.traits().ratings());
                    assertEquals(25, MagicResolution.resolve(definition, List.of()).variable(VestigeMainMod.location("mana_bonus")));
                }
            }
        }
    }
}
