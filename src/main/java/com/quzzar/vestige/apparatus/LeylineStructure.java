package com.quzzar.vestige.apparatus;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.*;

/** Finds regular fourfold rings without loading chunks or treating decorative blocks as nodes. */
final class LeylineStructure {
    private LeylineStructure() { }
    static List<RitualCrafting.Layout> find(OfferingBlockEntity center, int slots) {
        if (!(center.getLevel() instanceof ServerLevel level) || !ApparatusBlocks.isSpellstone(center.getBlockState())) return List.of();
        List<RitualCrafting.Layout> innerLayers=new ArrayList<>();
        for (var shape : LeylineShaping.Shape.values()) for (int inner = 1; shape.radius(inner) <= 8; inner++) for (int dy = -6; dy <= 6; dy++) {
            var nodes=ring(level,center.getBlockPos(),shape,inner,dy);
            if (nodes!=null) innerLayers.add(layout(level,center,nodes,null,new LeylineShaping.Geometry(4,shape,inner,dy,LeylineShaping.Shape.CROSS,0,0)));
        }
        double nearest=innerLayers.stream().mapToDouble(l->l.geometry().d1()).min().orElse(Double.POSITIVE_INFINITY);
        var innermost=innerLayers.stream().filter(l->Math.abs(l.geometry().d1()-nearest)<1e-8).toList();
        if (slots==4) return innermost;
        if (slots!=8) throw new IllegalArgumentException("Expected four or eight slots");
        List<RitualCrafting.Layout> found=new ArrayList<>();
        for (var innerLayer:innermost) {
            var g=innerLayer.geometry();
            var nodes=List.of(innerLayer.stands().get(0),innerLayer.stands().get(2),innerLayer.stands().get(4),innerLayer.stands().get(6));
            for (var outerShape:LeylineShaping.Shape.values()) for (int outer=1;outerShape.radius(outer)<=16;outer++) {
                if (outerShape.radius(outer)<=g.d1()) continue;
                for (int step=-6;step<=6;step++) {
                    var outerNodes=ring(level,center.getBlockPos(),outerShape,outer,g.innerHeight()+step);
                    if (outerNodes!=null) found.add(layout(level,center,nodes,outerNodes,new LeylineShaping.Geometry(8,g.innerShape(),g.inner(),g.innerHeight(),outerShape,outer,step)));
                }
            }
        }
        return List.copyOf(found);
    }
    static List<BlockPos> offsets(LeylineShaping.Shape shape,int distance,int height) {
        if (shape == LeylineShaping.Shape.CROSS) return List.of(new BlockPos(0,height,-distance),new BlockPos(distance,height,0),new BlockPos(0,height,distance),new BlockPos(-distance,height,0));
        return List.of(new BlockPos(distance,height,-distance),new BlockPos(distance,height,distance),new BlockPos(-distance,height,distance),new BlockPos(-distance,height,-distance));
    }
    private static List<OfferingBlockEntity> ring(ServerLevel level,BlockPos center,LeylineShaping.Shape shape,int distance,int height) {
        List<OfferingBlockEntity> nodes=new ArrayList<>();
        for (var offset : offsets(shape,distance,height)) {
            BlockPos pos=center.offset(offset);
            if (pos.getY()<level.getMinBuildHeight() || pos.getY()>=level.getMaxBuildHeight() || !level.hasChunkAt(pos)
                    || !ApparatusBlocks.isPlinth(level.getBlockState(pos)) || !ApparatusBlock.hasOfferingSpace(level.getBlockState(pos),level,pos)
                    || !(level.getBlockEntity(pos) instanceof OfferingBlockEntity node)) return null;
            nodes.add(node);
        }
        return nodes;
    }
    private static RitualCrafting.Layout layout(ServerLevel level,OfferingBlockEntity center,List<OfferingBlockEntity> inner,List<OfferingBlockEntity> outer,LeylineShaping.Geometry geometry) {
        List<OfferingBlockEntity> nodes=new ArrayList<>(Collections.nCopies(8,null));
        for (int i=0;i<4;i++) { nodes.set(i*2,inner.get(i)); if (outer!=null) nodes.set(i*2+1,outer.get(i)); }
        return new RitualCrafting.Layout(level,center,nodes,geometry.slots()==8,geometry);
    }
}
