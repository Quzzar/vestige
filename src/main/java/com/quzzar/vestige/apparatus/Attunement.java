package com.quzzar.vestige.apparatus;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Reproducible physical identity. Neither world coordinates nor shaping coefficients enter the key. */
public final class Attunement {
    public static final int VERSION=1;
    private Attunement() { }
    public record Node(int layer, BlockPos offset, ResourceLocation ingredient, int count, Optional<ResourceLocation> material) {
        public Node {
            Objects.requireNonNull(offset); Objects.requireNonNull(ingredient); Objects.requireNonNull(material);
            offset=offset.immutable();
            if (layer<0 || layer>1 || count<0 || count>1 || (count==0)!=ingredient.equals(ResourceLocation.withDefaultNamespace("air"))
                    || Math.abs(offset.getX())>16 || Math.abs(offset.getZ())>16 || Math.abs(offset.getY())>12) throw new IllegalArgumentException("Invalid attunement node");
        }
        Node rotate() { return new Node(layer,new BlockPos(-offset.getZ(),offset.getY(),offset.getX()),ingredient,count,material); }
        String encoded() { return layer+","+offset.getX()+","+offset.getY()+","+offset.getZ()+","+ingredient+","+count+","+material.map(ImbuementMaterials::canonical).map(Object::toString).orElse("-"); }
    }
    public record Signature(int slots, LeylineShaping.Shape innerShape, Optional<LeylineShaping.Shape> outerShape, List<Node> nodes) {
        public Signature {
            nodes=List.copyOf(nodes); Objects.requireNonNull(innerShape); Objects.requireNonNull(outerShape);
            if ((slots!=4 && slots!=8) || nodes.size()!=slots || outerShape.isPresent()!=(slots==8)
                    || nodes.stream().filter(n->n.layer()==0).count()!=4 || nodes.stream().map(Node::offset).distinct().count()!=slots) throw new IllegalArgumentException("Invalid attunement layers");
            for (int layer=0;layer<(slots==8 ? 2 : 1);layer++) {
                int wanted=layer; var ring=nodes.stream().filter(n->n.layer()==wanted).toList();
                if (ring.size()!=4 || ring.stream().map(n->n.offset().getY()).distinct().count()!=1) throw new IllegalArgumentException("Irregular attunement ring");
                var shape=layer==0 ? innerShape : outerShape.orElseThrow(); var first=ring.getFirst().offset();
                int step=Math.max(Math.abs(first.getX()),Math.abs(first.getZ()));
                if (step==0 || shape.radius(step)>(layer==0 ? 8 : 16)) throw new IllegalArgumentException("Attunement radius exceeds bounds");
                if (!new HashSet<>(LeylineStructure.offsets(shape,step,first.getY())).equals(new HashSet<>(ring.stream().map(Node::offset).toList()))) throw new IllegalArgumentException("Invalid attunement positions");
            }
            int innerY=nodes.stream().filter(n->n.layer()==0).findFirst().orElseThrow().offset().getY();
            if (Math.abs(innerY)>6) throw new IllegalArgumentException("Invalid inner height");
            if (slots==8) {
                var inside=nodes.stream().filter(n->n.layer()==0).findFirst().orElseThrow().offset();
                var outside=nodes.stream().filter(n->n.layer()==1).findFirst().orElseThrow().offset();
                if (Math.abs(outside.getY()-innerY)>6 || Math.hypot(outside.getX(),outside.getZ())<=Math.hypot(inside.getX(),inside.getZ())) throw new IllegalArgumentException("Invalid outer stage");
            }
        }
        public String blueprint() {
            List<Node> rotated=nodes; String best=null;
            for (int turn=0;turn<4;turn++) {
                String candidate="vestige:attunement/"+VERSION+";"+slots+";"+innerShape+";"+outerShape.map(Enum::name).orElse("-")+";"
                        +String.join(";",rotated.stream().map(Node::encoded).sorted().toList());
                if (best==null || candidate.compareTo(best)<0) best=candidate;
                rotated=rotated.stream().map(Node::rotate).toList();
            }
            return best;
        }
        public String key() {
            try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(blueprint().getBytes(StandardCharsets.UTF_8))); }
            catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
        }
    }
}
