package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.apparatus.recipeviewer.*;
import com.quzzar.vestige.magic.definition.SpecialTraits;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.magic.runtime.ForfeitPolicy;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FluxedFlintImbuementsTest {
    private static final LeylineShaping.Geometry GEOMETRY = new LeylineShaping.Geometry(4, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.CROSS, 0, 0);
    private static RitualInputs inputs(List<ItemStack> offerings, List<ItemStack> materials) {
        return new RitualInputs(GEOMETRY, java.util.stream.IntStream.range(0, 4).mapToObj(i ->
                new RitualInputs.Node(i * 2, GEOMETRY.offset(i * 2), offerings.get(i), materials.get(i))).toList());
    }
    @Test void allFourPublicPatternsMatchEveryRotationIncludingEitherReinforcedIngot() {
        var displays = new ArrayList<>(List.of(RitualDisplays.fluxedFlint())); displays.addAll(FluxedFlintDisplays.entries());
        assertEquals(4, displays.size());
        for (var display : displays) for (boolean otherIngot : List.of(false, true)) {
            var offerings = display.offerings().stream().map(o -> new ItemStack(BuiltInRegistries.ITEM.get(o.ingredient().items().getFirst()))).toList();
            var materials = new ArrayList<ItemStack>(Collections.nCopies(4, ItemStack.EMPTY));
            display.imbuements().forEach(m -> materials.set(otherIngot && m.seat() == 2 ? 3 : m.seat() / 2, m.stack()));
            for (int rotation = 0; rotation < 4; rotation++) {
                var offered = new ArrayList<>(offerings); var socketed = new ArrayList<>(materials);
                Collections.rotate(offered, rotation); Collections.rotate(socketed, rotation);
                assertTrue(ItemStack.matches(display.output(), FluxedFlintRecipe.result(inputs(offered, socketed)).orElseThrow()));
            }
        }
    }
    @Test void exactTradeoffsAndCompleteNamesComposeOnce() {
        int[] budgets = {128, 96, 192, 144}; double[] chances = {.1, .05, .15, .075};
        String[] adjectives = {"", "Stabilized", "Reinforced", "Braced"};
        for (int i = 0; i < 4; i++) {
            var v = FluxedFlintImbuements.variants().get(i); var stack = FluxedFlintImbuements.create(v);
            assertEquals(budgets[i], stack.getMaxDamage()); assertEquals(v, FluxedFlintImbuements.read(stack).orElseThrow());
            assertEquals(chances[i], ForfeitPolicy.DEFAULT.chance(RitualVolatility.traits(stack), true), 1e-12);
            assertEquals(adjectives[i].isEmpty() ? List.of() : List.of(adjectives[i]), MagicAdjectives.words(FluxedFlintImbuements.FAMILY, v.selections()));
            assertEquals((adjectives[i].isEmpty() ? "" : adjectives[i] + " ") + new ItemStack(ScrollItems.FLUXED_FLINT.get()).getHoverName().getString(), stack.getHoverName().getString());
            assertTrue(stack.hasFoil());
        }
        assertEquals(2, FluxedFlintItem.TRAITS.rating(SpecialTraits.VOLATILE));
    }
    @Test void duplicatesAndRecognizedIncompatiblePairsRejectWithoutRewritingInputs() {
        var offerings = FluxedFlintRecipe.ingredients().stream().map(ItemStack::new).toList();
        var materials = new ArrayList<ItemStack>(Collections.nCopies(4, ItemStack.EMPTY));
        materials.set(1, new ItemStack(Items.IRON_BLOCK)); materials.set(3, new ItemStack(Items.IRON_BLOCK));
        assertTrue(FluxedFlintRecipe.result(inputs(offerings, materials)).isEmpty());
        // Flint / Gold is an existing spell-only Excavating route.
        materials = new ArrayList<>(Collections.nCopies(4, ItemStack.EMPTY)); materials.set(0, new ItemStack(Items.GOLD_BLOCK));
        assertTrue(FluxedFlintRecipe.result(inputs(offerings, materials)).isEmpty());
        materials = new ArrayList<>(Collections.nCopies(4, new ItemStack(Items.WHITE_WOOL)));
        assertTrue(FluxedFlintRecipe.result(inputs(offerings, materials)).isPresent());
    }
    @Test void forgedDurabilityUnknownSelectionsAndDegreesCannotGrantRepairOrRiskReduction() {
        for (var variant : FluxedFlintImbuements.variants()) {
            var stack = FluxedFlintImbuements.create(variant); stack.set(DataComponents.MAX_DAMAGE, 1000);
            assertTrue(FluxedFlintImbuements.read(stack).isEmpty());
            assertEquals(2, RitualVolatility.traits(stack).rating(SpecialTraits.VOLATILE));
        }
        for (String field : List.of("id", "degree")) {
            var stack = FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(1));
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                var entry = tag.getCompound("vestige_item_imbuements").getList("adjustments", 10).getCompound(0);
                if (field.equals("id")) entry.putString("id", "vestige:unknown"); else entry.putInt("degree", 2);
            });
            assertTrue(FluxedFlintImbuements.read(stack).isEmpty());
            assertEquals(.1, ForfeitPolicy.DEFAULT.chance(RitualVolatility.traits(stack), true), 1e-12);
        }
    }
    @Test void ordinaryAndWornFlintsRetainTheirBudgetWithoutConversionOrRefill() {
        var plain = new ItemStack(ScrollItems.FLUXED_FLINT.get()); plain.setDamageValue(31);
        assertEquals(Set.of(), FluxedFlintImbuements.read(plain).orElseThrow().choices());
        assertEquals(31, plain.getDamageValue()); assertFalse(plain.has(DataComponents.CUSTOM_DATA));
        for (var v : FluxedFlintImbuements.variants()) {
            var stack = FluxedFlintImbuements.create(v); stack.setDamageValue(v.durability() - 1);
            assertEquals(v, FluxedFlintImbuements.read(stack).orElseThrow());
            stack.setDamageValue(v.durability()); assertTrue(FluxedFlintImbuements.read(stack).isEmpty());
        }
    }
}
