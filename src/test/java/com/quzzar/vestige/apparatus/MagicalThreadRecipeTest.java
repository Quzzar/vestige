package com.quzzar.vestige.apparatus;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MagicalThreadRecipeTest {
    private static RitualInputs inputs(MagicalThreadRecipe.Type type, int a, int b, int c,
                                      LeylineShaping.Shape shape, int height) {
        var geometry = new LeylineShaping.Geometry(4, shape, 2, height, LeylineShaping.Shape.CROSS, 1, 0);
        var nodes = new ArrayList<RitualInputs.Node>();
        for (int i = 0; i < 4; i++) {
            var item = i == a ? Items.STRING : i == b ? Items.AMETHYST_SHARD : i == c ? Items.HONEYCOMB : Items.AIR;
            var material = i == a ? BuiltInRegistries.ITEM.get(type.material()) : Items.COPPER_BLOCK;
            nodes.add(new RitualInputs.Node(i * 2, geometry.offset(i * 2), new ItemStack(item), new ItemStack(material)));
        }
        return new RitualInputs(geometry, nodes);
    }
    @Test void allFiveTypesAcceptOnlyWholeRotationsWithoutGeometryOrOtherSocketEffects() {
        assertEquals(5, MagicalThreadRecipe.types().size());
        assertEquals(5, MagicalThreadRecipe.types().stream().map(MagicalThreadRecipe.Type::item).distinct().count());
        for (var type : MagicalThreadRecipe.types()) for (int a = 0; a < 4; a++) for (int b = 0; b < 4; b++) for (int c = 0; c < 4; c++) {
            if (a == b || a == c || b == c) continue;
            for (var shape : LeylineShaping.Shape.values()) {
                var result = MagicalThreadRecipe.result(inputs(type, a, b, c, shape, 1));
                if (b != (a + 1) % 4 || c != (a + 2) % 4) { assertTrue(result.isEmpty()); continue; }
                var output = result.orElseThrow();
                assertTrue(output.is(type.item())); assertEquals(1, output.getCount());
                assertFalse(output.has(DataComponents.CUSTOM_DATA)); assertFalse(output.isDamageableItem());
            }
        }
    }
    @Test void onlyTheStringSocketSelectsAndMissingOrUnsupportedSelectorsReject() {
        var baseline = inputs(MagicalThreadRecipe.Type.CALLOUS, 0, 1, 2, LeylineShaping.Shape.CROSS, 0);
        for (var selector : List.of(ItemStack.EMPTY, new ItemStack(Items.COPPER_BLOCK), new ItemStack(Items.DIAMOND))) {
            var nodes = new ArrayList<>(baseline.nodes()); var string = nodes.getFirst();
            nodes.set(0, new RitualInputs.Node(string.seat(), string.offset(), string.offering(), selector));
            var other = nodes.get(1);
            nodes.set(1, new RitualInputs.Node(other.seat(), other.offset(), other.offering(), new ItemStack(Items.IRON_BLOCK)));
            assertTrue(MagicalThreadRecipe.result(new RitualInputs(baseline.geometry(), nodes)).isEmpty());
        }
    }
    @Test void theRecipeRequiresThreeDistinctSingleOfferingsAndEveryOtherSeatEmpty() {
        var seats = new ArrayList<>(Collections.nCopies(8, ItemStack.EMPTY));
        seats.set(0, new ItemStack(Items.STRING)); seats.set(2, new ItemStack(Items.AMETHYST_SHARD)); seats.set(4, new ItemStack(Items.HONEYCOMB));
        assertTrue(MagicalThreadRecipe.matches(seats));
        for (var wrong : List.of(Items.GLOW_INK_SAC, Items.PAPER, Items.STRING)) {
            seats.set(4, new ItemStack(wrong)); assertFalse(MagicalThreadRecipe.matches(seats));
        }
        seats.set(4, new ItemStack(Items.HONEYCOMB)); seats.set(6, new ItemStack(Items.PAPER));
        assertFalse(MagicalThreadRecipe.matches(seats)); seats.set(6, ItemStack.EMPTY);
        seats.set(0, new ItemStack(Items.STRING, 2)); assertFalse(MagicalThreadRecipe.matches(seats));
        seats.set(0, new ItemStack(Items.STRING)); seats.set(1, new ItemStack(Items.HONEYCOMB));
        assertTrue(MagicalThreadRecipe.matches(seats)); assertFalse(MagicalThreadRecipe.matches(List.of()));
    }
    @Test void IncompleteSnapshotsAndDuplicateOrMissingNodesReject() {
        var basic = inputs(MagicalThreadRecipe.Type.ENSORCELLED, 0, 1, 2, LeylineShaping.Shape.CROSS, 0);
        var advanced = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        assertTrue(MagicalThreadRecipe.result(new RitualInputs(advanced, basic.nodes())).isEmpty());
        assertTrue(MagicalThreadRecipe.result(new RitualInputs(basic.geometry(), basic.nodes().subList(0, 3))).isEmpty());
        var duplicate = new ArrayList<>(basic.nodes()); duplicate.set(3, duplicate.getFirst());
        assertTrue(MagicalThreadRecipe.result(new RitualInputs(basic.geometry(), duplicate)).isEmpty());
    }
    @Test void anEightNodeSnapshotCannotSupplyOuterIngredientsForAFourSlotRecipe() {
        var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        var nodes = new ArrayList<RitualInputs.Node>();
        for(int i=0;i<8;i++) nodes.add(new RitualInputs.Node(i,geometry.offset(i),new ItemStack(
                i==1 ? Items.STRING : i==3 ? Items.AMETHYST_SHARD : i==5 ? Items.HONEYCOMB : Items.AIR),
                new ItemStack(Items.DIAMOND_BLOCK)));
        assertTrue(MagicalThreadRecipe.result(new RitualInputs(geometry,nodes)).isEmpty());
    }
}
