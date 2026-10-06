package com.quzzar.vestige.apparatus;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.stream.IntStream;

/** One captured active arrangement, shared by output compilation and atomic commitment. */
public record RitualInputs(LeylineShaping.Geometry geometry, List<Node> nodes) {
    public record Node(int seat, BlockPos offset, ItemStack offering, ItemStack material) {
        public Node { offset=offset.immutable(); offering=offering.copy(); material=material.copy(); }
        @Override public ItemStack offering() { return offering.copy(); }
        @Override public ItemStack material() { return material.copy(); }
    }
    public RitualInputs { nodes=List.copyOf(nodes); }
    public static RitualInputs capture(RitualCrafting.Layout layout) {
        return new RitualInputs(layout.geometry(),IntStream.range(0,8)
                .filter(i -> layout.geometry().slots()==8 || (i&1)==0)
                .mapToObj(i -> new Node(i,layout.geometry().offset(i),layout.stands().get(i).displayedItem(),layout.stands().get(i).materialItem())).toList());
    }
    public boolean matches(RitualCrafting.Layout layout) {
        return geometry.equals(layout.geometry()) && nodes.stream().allMatch(n -> {
            var block=layout.stands().get(n.seat());
            return block!=null && block.hasOfferingSpace()
                    && ItemStack.matches(n.offering(),block.displayedItem()) && ItemStack.matches(n.material(),block.materialItem());
        });
    }
}
