package com.quzzar.vestige.magic.presentation;

import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Deterministic procedural paths, shared by rendering and geometry verification. */
public final class VisualGeometry {
    private VisualGeometry() { }
    /** One outward reach followed by a quicker return, with no residual radius at expiry. */
    public static double pulseRadius(float progress) {
        double p=Math.clamp(progress,0,1);
        double reach=p<.65 ? p/.65 : (1-p)/.35;
        return reach*reach*(3-2*reach);
    }
    public static List<Vec3> arc(Vec3 start, Vec3 end, long seed) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length < .001) return List.of(start, end);
        int segments = Math.max(2, Math.min(32, (int) Math.ceil(length * 2)));
        Vec3 side = delta.cross(Math.abs(delta.normalize().y) > .95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize();
        Vec3 up = side.cross(delta).normalize();
        Random random = new Random(seed);
        List<Vec3> points = new ArrayList<>(segments + 1); points.add(start);
        double bend = Math.min(.7, .08 + length * .035);
        for (int i = 1; i < segments; i++) {
            double t = i / (double) segments;
            double envelope = Math.sin(t * Math.PI);
            points.add(start.lerp(end, t).add(side.scale((random.nextDouble() * 2 - 1) * bend * envelope))
                    .add(up.scale((random.nextDouble() * 2 - 1) * bend * envelope)));
        }
        points.add(end); return List.copyOf(points);
    }
    public static float opacity(float age, int duration) {
        if (age < 0 || age >= duration) return 0;
        return Math.max(0, Math.min(1, Math.min((age + 1) / 3f, (duration - age) / Math.min(5f, duration / 2f))));
    }
    /** Stable cosmetic spread that occupies the authored footprint, including large fields. */
    public static Vec3 emissionOffset(long seed,float age,double radius,int index) {
        Random random=new Random(seed+index*103L);
        double angle=random.nextDouble()*Math.PI*2+age*.025;
        double spread=Math.min(8,Math.max(.01,radius))*(.25+.75*random.nextDouble());
        double rise=(.1+random.nextDouble()*.5)*Math.min(2,spread);
        return new Vec3(Math.cos(angle)*spread,rise,Math.sin(angle)*spread);
    }
    /** Ordered path sampling keeps beam sparks between actual caster and target anchors. */
    public static Vec3 pathPoint(List<Vec3> points,double unit) {
        if(points.size()==1) return points.getFirst();
        double location=Math.max(0,Math.min(1,unit))*(points.size()-1);
        int segment=Math.min(points.size()-2,(int)location);
        return points.get(segment).lerp(points.get(segment+1),location-segment);
    }
    /** A continuous coiling path with exact endpoints, stable even for vertical or zero-length casts. */
    public static List<Vec3> helix(Vec3 from, Vec3 to, double radius, double rotation, int turns) {
        Vec3 delta=to.subtract(from);
        if(delta.lengthSqr()<1e-8) return List.of(from,to);
        Vec3 side=delta.cross(Math.abs(delta.normalize().y)>.95?new Vec3(1,0,0):new Vec3(0,1,0)).normalize();
        Vec3 up=side.cross(delta).normalize();
        List<Vec3> result=new ArrayList<>();
        int steps=Math.min(64,Math.max(16,turns*12));
        for(int i=0;i<=steps;i++) {
            double t=i/(double)steps,angle=rotation+t*Math.PI*2*turns;
            double spread=radius*Math.sin(t*Math.PI);
            result.add(from.lerp(to,t).add(side.scale(Math.cos(angle)*spread)).add(up.scale(Math.sin(angle)*spread)));
        }
        return List.copyOf(result);
    }
    /** Sweeping organic curves stay inside the authored field, with independent rooted stems. */
    public static List<Vec3> tendril(Vec3 center, double radius, double motion, int index, int count) {
        List<Vec3> result=new ArrayList<>();double angle=index*Math.PI*2/count;
        double length=Math.min(4,Math.max(1.8,radius*.8));
        for(int i=0;i<=12;i++) {
            double t=i/12.0,twist=angle+Math.sin(motion+index*.7+t*2)*t*.65;
            double spread=radius*(.2+.5*t);
            result.add(center.add(Math.cos(twist)*spread,t*length-.25,Math.sin(twist)*spread));
        }
        return List.copyOf(result);
    }
}
