package com.quzzar.vestige.apparatus;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AttunementTest {
    private static ResourceLocation mc(String s){return ResourceLocation.withDefaultNamespace(s);}
    private static Attunement.Signature fixture(){
        var g=new LeylineShaping.Geometry(8,LeylineShaping.Shape.DIAGONAL,2,2,LeylineShaping.Shape.CROSS,6,-1);
        var names=List.of("amethyst_shard","amethyst_shard","echo_shard","iron_ingot","diamond","lapis_lazuli","air","air");
        var nodes=new ArrayList<Attunement.Node>();for(int i=0;i<8;i++)nodes.add(new Attunement.Node(i%2,g.offset(i),mc(names.get(i)),names.get(i).equals("air") ? 0 : 1,i==1 ? Optional.empty() : Optional.of(mc(i%2==0 ? "copper_block" : "iron_block"))));
        return new Attunement.Signature(8,g.innerShape(),Optional.of(g.outerShape()),nodes);
    }
    private static Attunement.Signature changed(Attunement.Signature s,List<Attunement.Node> nodes){return new Attunement.Signature(s.slots(),s.innerShape(),s.outerShape(),nodes);}
    @Test void wholeQuarterTurnsAndIterationOrderPreserveIdentity(){
        var original=fixture();var nodes=original.nodes();assertEquals(64,original.key().length());
        for(int i=0;i<4;i++){nodes=nodes.stream().map(Attunement.Node::rotate).toList();assertEquals(original.key(),changed(original,nodes).key());}
        var reversed=new ArrayList<>(nodes);Collections.reverse(reversed);assertEquals(original.key(),changed(original,reversed).key());
    }
    @Test void localPairingsLayersIndependentHeightsAndScaleMatter(){
        var original=fixture();var nodes=new ArrayList<>(original.nodes());var a=nodes.get(0);var b=nodes.get(2);
        nodes.set(0,new Attunement.Node(a.layer(),a.offset(),b.ingredient(),1,a.material()));nodes.set(2,new Attunement.Node(b.layer(),b.offset(),a.ingredient(),1,b.material()));
        assertNotEquals(original.key(),changed(original,nodes).key());
        nodes=new ArrayList<>(original.nodes());nodes.set(0,new Attunement.Node(a.layer(),a.offset(),a.ingredient(),1,Optional.of(mc("gold_block"))));assertNotEquals(original.key(),changed(original,nodes).key());
        for(int layer=0;layer<2;layer++){int which=layer;var shifted=original.nodes().stream().map(n->n.layer()!=which ? n : new Attunement.Node(n.layer(),n.offset().above(),n.ingredient(),n.count(),n.material())).toList();assertNotEquals(original.key(),changed(original,shifted).key());}
        var outerTurn=original.nodes().stream().map(n->n.layer()==1 ? n.rotate() : n).toList();assertNotEquals(original.key(),changed(original,outerTurn).key());
        var mirror=original.nodes().stream().map(n->new Attunement.Node(n.layer(),new BlockPos(-n.offset().getX(),n.offset().getY(),n.offset().getZ()),n.ingredient(),n.count(),n.material())).toList();assertNotEquals(original.key(),changed(original,mirror).key());
    }
    @Test void emptyOfferingPositionsAndTheirMaterialSocketsStillAffectIdentity(){
        var original=fixture();var nodes=new ArrayList<>(original.nodes());var occupied=nodes.get(0);var empty=nodes.get(6);
        nodes.set(0,new Attunement.Node(occupied.layer(),occupied.offset(),empty.ingredient(),empty.count(),occupied.material()));
        nodes.set(6,new Attunement.Node(empty.layer(),empty.offset(),occupied.ingredient(),occupied.count(),empty.material()));
        assertNotEquals(original.key(),changed(original,nodes).key());
        nodes=new ArrayList<>(original.nodes());nodes.set(6,new Attunement.Node(empty.layer(),empty.offset(),empty.ingredient(),empty.count(),Optional.of(mc("gold_block"))));
        assertNotEquals(original.key(),changed(original,nodes).key());
    }
    @Test void malformedBlueprintsCannotCreateKeys(){
        var s=fixture();var nodes=new ArrayList<>(s.nodes());nodes.set(1,nodes.get(0));assertThrows(IllegalArgumentException.class,()->changed(s,nodes));
        assertThrows(IllegalArgumentException.class,()->new Attunement.Node(0,new BlockPos(17,0,0),mc("quartz"),1,Optional.empty()));
        assertThrows(IllegalArgumentException.class,()->new Attunement.Node(0,BlockPos.ZERO,mc("quartz"),0,Optional.empty()));
    }
    @Test void imbuementColorsAreCosmeticForKeysButTheirActualBlueprintMaterialsAreRetained(){
        var original=fixture();var nodes=new ArrayList<>(original.nodes());
        var occupied=nodes.get(0);var empty=nodes.get(6);
        nodes.set(0,new Attunement.Node(occupied.layer(),occupied.offset(),occupied.ingredient(),occupied.count(),Optional.of(mc("white_wool"))));
        nodes.set(6,new Attunement.Node(empty.layer(),empty.offset(),empty.ingredient(),empty.count(),Optional.of(mc("white_concrete"))));
        var white=changed(original,nodes);
        for(var color:DyeColor.values()) {
            var wool=mc(color.getName()+"_wool");var concrete=mc(color.getName()+"_concrete");
            var colored=new ArrayList<>(white.nodes());
            colored.set(0,new Attunement.Node(occupied.layer(),occupied.offset(),occupied.ingredient(),occupied.count(),Optional.of(wool)));
            colored.set(6,new Attunement.Node(empty.layer(),empty.offset(),empty.ingredient(),empty.count(),Optional.of(concrete)));
            var signature=changed(original,colored);
            assertEquals(white.key(),signature.key());assertEquals(white.blueprint(),signature.blueprint());
            assertEquals(Optional.of(wool),signature.nodes().get(0).material());
            assertEquals(Optional.of(concrete),signature.nodes().get(6).material());
            assertEquals(white.key(),changed(original,signature.nodes().stream().map(Attunement.Node::rotate).toList()).key());
        }
        var different=new ArrayList<>(white.nodes());
        different.set(0,new Attunement.Node(occupied.layer(),occupied.offset(),occupied.ingredient(),occupied.count(),Optional.of(mc("white_concrete"))));
        assertNotEquals(white.key(),changed(original,different).key());
    }
}
