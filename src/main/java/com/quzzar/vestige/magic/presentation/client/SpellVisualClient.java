package com.quzzar.vestige.magic.presentation.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.presentation.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.*;

/** One bounded renderer for composable paths, surfaces, rings and particle emitters. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class SpellVisualClient {
    private static final Map<UUID, Cue> CUES = new LinkedHashMap<>();
    private static ClientLevel level;
    private static int quads;
    private static Vec3 renderCamera = Vec3.ZERO;
    private SpellVisualClient() { }
    public static void clear() { CUES.clear(); level = null; }
    private static void ensureLevel() {
        ClientLevel current = Minecraft.getInstance().level;
        if (level != current) { CUES.clear(); level = current; }
    }
    public static void receive(SpellVisualPayload payload) {
        ensureLevel();
        if (level == null || !level.dimension().location().equals(payload.dimension())) return;
        if (payload.stop()) {
            CUES.remove(payload.id());
            if (!payload.burst()) return;
            payload = new SpellVisualPayload(payload.id(), payload.dimension(), payload.visual().remaining(6),
                    payload.points(), payload.seed(), 0, false, false, true);
        }
        if (CUES.size() >= 256 && !CUES.containsKey(payload.id())) return;
        CUES.put(payload.id(), new Cue(payload));
    }
    public static boolean follows(UUID entity) {
        return CUES.values().stream().anyMatch(cue -> cue.data.follow() && cue.data.points().stream().anyMatch(point -> point.entityUuid().equals(entity)));
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        ensureLevel();
        Minecraft minecraft = Minecraft.getInstance();
        if (level == null || minecraft.isPaused()) return;
        int particles = 0;
        Iterator<Cue> iterator = CUES.values().iterator();
        while (iterator.hasNext()) {
            Cue cue = iterator.next();
            if (++cue.age >= cue.data.visual().duration()) { iterator.remove(); continue; }
            if (cue.data.follow() && cue.data.points().stream().anyMatch(point -> point.entityId() >= 0 && !matching(point))) {
                if (++cue.missing > 5) { iterator.remove(); continue; }
            } else cue.missing = 0;
            List<Vec3> anchors=points(cue,1);
            Vec3 center = anchors.getLast();
            if (center.distanceToSqr(minecraft.gameRenderer.getMainCamera().getPosition()) > 64 * 64) continue;
            for (SpellVisual.Layer layer : cue.data.visual().layers()) {
                if (layer.shape() != SpellVisual.Shape.SPARKS && layer.shape() != SpellVisual.Shape.FIRE && layer.shape() != SpellVisual.Shape.SMOKE && layer.shape() != SpellVisual.Shape.SPLASH) continue;
                int density=Math.min(16,6+(int)cue.data.visual().radius());
                int count = switch (minecraft.options.particles().get()) { case ALL -> density; case DECREASED -> Math.max(2,density/2); case MINIMAL -> 0; };
                Random random = new Random(cue.data.seed() + cue.age * 31L + layer.shape().ordinal());
                for (int i = 0; i < count && particles < 128; i++, particles++) {
                    if (random.nextFloat() > layer.alpha() * VisualGeometry.opacity(cue.age, cue.data.visual().duration())) continue;
                    Vec3 at = cue.previous == null ? center : cue.previous.lerp(center, (i + 1.0) / count);
                    if(path(cue)) at=VisualGeometry.pathPoint(anchors,(i+.5)/count);
                    Vec3 offset=VisualGeometry.emissionOffset(cue.data.seed(),cue.age,cue.data.visual().radius()*layer.scale(),i);
                    at=at.add(offset);
                    var particle = switch (layer.shape()) {
                        case FIRE -> ParticleTypes.FLAME;
                        case SMOKE -> ParticleTypes.SMOKE;
                        case SPLASH -> ParticleTypes.SPLASH;
                        default -> new DustParticleOptions(new Vector3f(((layer.color() >> 16) & 255) / 255f,
                                ((layer.color() >> 8) & 255) / 255f, (layer.color() & 255) / 255f), Math.min(2, layer.scale()));
                    };
                    level.addParticle(particle, at.x, at.y, at.z, offset.x*.04, .025, offset.z*.04);
                }
            }
            cue.previous = center;
        }
    }
    private static boolean matching(SpellVisualPayload.Point point) {
        Entity entity = level.getEntity(point.entityId());
        return entity != null && !entity.isRemoved() && entity.getUUID().equals(point.entityUuid());
    }
    private static List<Vec3> points(Cue cue, float partial) {
        return cue.data.points().stream().map(point -> {
            if (cue.data.follow() && point.entityId() >= 0 && matching(point))
                return level.getEntity(point.entityId()).getPosition(partial).add(0, point.height(), 0);
            return point.position();
        }).toList();
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        ensureLevel(); if (level == null || CUES.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = event.getCamera().getPosition();
        var buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.debugQuads());
        // LevelRenderer already applies the camera rotation to the shader's
        // model-view state. Applying getModelViewMatrix here rotates twice.
        Matrix4f matrix = new Matrix4f(event.getPoseStack().last().pose());
        renderCamera = camera;
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        quads = 0;
        for (Cue cue : CUES.values()) {
            if (quads >= 4096) break;
            List<Vec3> points = points(cue, partial);
            if (points.stream().noneMatch(point -> point.distanceToSqr(camera) < 192 * 192)) continue;
            float age = cue.age + partial;
            float fade = VisualGeometry.opacity(age, cue.data.visual().duration());
            float progress = age / cue.data.visual().duration();
            float expansion = cue.data.follow() ? 1 : .25f + .75f * Math.min(1, age / 6);
            if (cue.data.burst()) expansion = 1 + progress * .5f;
            for (SpellVisual.Layer layer : cue.data.visual().layers()) {
                if (quads >= 4096) break;
                double radius = cue.data.visual().radius() * layer.scale() * expansion;
                float alpha = layer.alpha() * fade;
                double motion=age*layer.speed()*.06+layer.phase();
                Vec3 hub=points.getLast();
                switch (layer.shape()) {
                    case SIGIL -> {
                        int count=Math.min(16,layer.count());
                        orientedRing(consumer,matrix,hub,new Vec3(1,0,0),new Vec3(0,0,1),radius,layer.width(),layer.color(),alpha*.8f,motion);
                        orientedRing(consumer,matrix,hub,new Vec3(1,0,0),new Vec3(0,0,1),radius*.74,layer.width()*.5,layer.color(),alpha*.5f,-motion);
                        for(int i=0;i<count;i++) {
                            double a=motion+i*Math.PI*2/count;
                            Vec3 tip=radial(hub,radius,a),left=radial(hub,radius*.84,a-.075),right=radial(hub,radius*.84,a+.075);
                            ribbon(consumer,matrix,camera,left,tip,layer.width(),layer.color(),alpha);
                            ribbon(consumer,matrix,camera,tip,right,layer.width(),layer.color(),alpha);
                            ribbon(consumer,matrix,camera,radial(hub,radius*.48,a),radial(hub,radius*.65,a+.16),layer.width()*.65,layer.color(),alpha);
                        }
                    }
                    case HELIX -> {
                        if(points.size()>1) for(int i=1;i<points.size();i++) {
                            var coil=VisualGeometry.helix(points.get(i-1),points.get(i),Math.max(.15,radius),motion,Math.max(1,layer.count()/3));
                            stroke(consumer,matrix,camera,coil,layer.width(),layer.color(),alpha);
                        } else for(int strand=0;strand<2;strand++) {
                            var coil=VisualGeometry.helix(hub.add(0,-.8,0),hub.add(0,1.2,0),radius,motion+strand*Math.PI,Math.max(1,layer.count()/4));
                            stroke(consumer,matrix,camera,coil,layer.width(),layer.color(),alpha);
                        }
                    }
                    case SHARDS -> {
                        for(int i=0;i<layer.count();i++) {
                            double a=i*Math.PI*2/layer.count()+layer.phase();
                            double pulse=.65+.35*Math.sin(motion*2+i*1.7);
                            Vec3 at=radial(hub,radius*(.5+.3*Math.sin(i*2.4)),a).add(0,Math.sin(motion+i)*.12,0);
                            crystal(consumer,matrix,at,Math.min(.3,.08+radius*.09),Math.min(2,.4+radius*.45)*pulse,layer.color(),alpha);
                        }
                    }
                    case TENDRILS -> {
                        for(int i=0;i<Math.min(12,layer.count());i++) {
                            var curve=VisualGeometry.tendril(hub,radius,motion,i,Math.min(12,layer.count()));
                            for(int j=1;j<curve.size();j++) {
                                double width=layer.width()*(1-j/(double)curve.size())*3;
                                ribbon(consumer,matrix,camera,curve.get(j-1),curve.get(j),width*2,layer.color(),alpha*.16f);
                                ribbon(consumer,matrix,camera,curve.get(j-1),curve.get(j),width,layer.color(),alpha);
                            }
                        }
                    }
                    case FLARE -> {
                        for(int i=0;i<layer.count();i++) {
                            double a=i*Math.PI*2/layer.count()+motion*.2;
                            double rise=.55+.45*Math.sin(motion*3+i*1.9);
                            Vec3 root=radial(hub,radius*.75,a).add(0,-.25,0);
                            Vec3 tip=root.add(Math.cos(a)*radius*.15,Math.min(3,.6+radius)*rise,Math.sin(a)*radius*.15);
                            Vec3 side=new Vec3(-Math.sin(a),0,Math.cos(a)).scale(Math.min(.5,.1+radius*.15));
                            quad(consumer,matrix,root.subtract(side),root.add(side),tip,tip,layer.color(),alpha*.8f);
                            ribbon(consumer,matrix,camera,root,root.lerp(tip,.7),layer.width()*2,0xffefd0,alpha*.75f);
                        }
                    }
                    case VORTEX -> {
                        Vec3 core=hub.add(0,Math.min(1.6,radius*.4),0);
                        for(int arm=0;arm<Math.min(8,layer.count());arm++) {
                            Vec3 previous=null;
                            for(int i=0;i<=24;i++) {
                                double t=i/24.0,a=arm*Math.PI*2/Math.min(8,layer.count())+motion*2+t*5;
                                Vec3 at=radial(core,radius*(1-t*.85),a).add(0,Math.sin(a+arm)*radius*.3,0);
                                if(previous!=null)ribbon(consumer,matrix,camera,previous,at,layer.width()*(.3+t),layer.color(),alpha*(float)(.3+t*.7));
                                previous=at;
                            }
                        }
                        sphere(consumer,matrix,core,Math.max(.15,radius*.22),0x100b22,alpha);
                    }
                    case MOTES, LEAVES -> {
                        for(int i=0;i<layer.count();i++) {
                            double a=i*Math.PI*2/layer.count()+motion;
                            Vec3 at=radial(hub,radius*(.65+.25*Math.sin(i*2.1+motion*.7)),a).add(0,Math.sin(a*2+i)*Math.min(1.2,radius*.6),0);
                            double size=Math.min(.3,.1+radius*.04)*layer.width()*12;
                            if(layer.shape()==SpellVisual.Shape.LEAVES) {
                                Vec3 side=new Vec3(Math.cos(a),0,Math.sin(a)).scale(size);
                                quad(consumer,matrix,at.add(0,size,0),at.add(side),at.add(0,-size,0),at.subtract(side),layer.color(),alpha);
                                ribbon(consumer,matrix,camera,at.add(0,-size,0),at.add(0,size,0),.025,0xe0f6bc,alpha*.6f);
                            } else {
                                crystal(consumer,matrix,at,size*.55,size,layer.color(),alpha);
                                ribbon(consumer,matrix,camera,at,at.add(Math.sin(a)*.25,-.08,-Math.cos(a)*.25),layer.width(),layer.color(),alpha*.5f);
                            }
                        }
                    }
                    case RAYS -> {
                        for(int i=0;i<layer.count();i++) {
                            double a=i*Math.PI*2/layer.count()+motion*.15;
                            Vec3 direction=new Vec3(Math.cos(a),.3+Math.sin(i*1.7)*.7,Math.sin(a)).normalize();
                            Vec3 from=hub.add(direction.scale(radius*.25)),to=hub.add(direction.scale(radius*(.7+.3*Math.sin(motion+i))));
                            ribbon(consumer,matrix,camera,from,to,layer.width()*3,layer.color(),alpha*.15f);
                            ribbon(consumer,matrix,camera,from,to,layer.width(),layer.color(),alpha);
                        }
                    }
                    case RIPPLE -> {
                        Vec3 from=points.getFirst(),delta=hub.subtract(from);
                        Vec3 normal=delta.lengthSqr()<.001?new Vec3(0,1,0):delta.normalize();
                        Vec3 side=normal.cross(Math.abs(normal.y)>.95?new Vec3(1,0,0):new Vec3(0,1,0)).normalize(),up=side.cross(normal).normalize();
                        for(int i=0;i<Math.min(8,layer.count());i++) {
                            double t=(i/(double)Math.min(8,layer.count())+motion*.35)%1;
                            if(t<0)t++;
                            Vec3 at=delta.lengthSqr()<.001?hub:from.lerp(hub,t);
                            orientedRing(consumer,matrix,at,side,up,radius*(.35+t*.65),layer.width(),layer.color(),alpha*(float)(1-t*.6),motion);
                        }
                    }
                    case SLASH -> {
                        Vec3 normal=camera.subtract(hub).normalize();
                        Vec3 side=normal.cross(new Vec3(0,1,0)).normalize(),up=side.cross(normal).normalize();
                        double size=Math.max(.5,radius);
                        for(int band=0;band<3;band++) for(int i=0;i<24;i++) {
                            double a=-1.15+i/24.0*2.7+motion*.35,b=-1.15+(i+1)/24.0*2.7+motion*.35;
                            double r=size*(1-band*.1);
                            Vec3 first=hub.add(side.scale(Math.cos(a)*r)).add(up.scale(Math.sin(a)*r));
                            Vec3 second=hub.add(side.scale(Math.cos(b)*r)).add(up.scale(Math.sin(b)*r));
                            ribbon(consumer,matrix,camera,first,second,layer.width()*(1-band*.2),layer.color(),alpha*(1-band*.25f));
                        }
                    }
                    case CHAIN -> {
                        List<Vec3> linksPath=points;
                        if(points.size()==1)linksPath=List.of(radial(hub,radius,-motion*.08),hub.add(0,1,0),radial(hub,radius,Math.PI*2/3),hub.add(0,1,0),radial(hub,radius,Math.PI*4/3));
                        for(int p=1;p<linksPath.size();p++) {
                            Vec3 from=linksPath.get(p-1),to=linksPath.get(p),delta=to.subtract(from).normalize();
                            Vec3 side=delta.cross(Math.abs(delta.y)>.95?new Vec3(1,0,0):new Vec3(0,1,0)).normalize(),up=side.cross(delta).normalize();
                            int links=Math.min(24,Math.max(3,(int)Math.ceil(from.distanceTo(to)*3)));
                            for(int i=0;i<links;i++) {
                                Vec3 at=from.lerp(to,(i+.5)/links).add(0,Math.sin(i*.6+motion)*.06,0);
                                Vec3 transverse=i%2==0?side:up;
                                orientedRing(consumer,matrix,at,delta,transverse,.12,layer.width(),layer.color(),alpha,0);
                            }
                        }
                    }
                    case WINGS -> {
                        var anchor=cue.data.points().getLast();
                        Vec3 facing=matching(anchor)?level.getEntity(anchor.entityId()).getLookAngle():camera.subtract(hub).normalize();
                        Vec3 spread=facing.cross(new Vec3(0,1,0)).normalize();
                        if(spread.lengthSqr()<.1)spread=new Vec3(1,0,0);
                        for(int wing: new int[]{-1,1})for(int i=0;i<Math.min(12,layer.count());i++) {
                            double t=i/(double)Math.min(12,layer.count());
                            Vec3 root=hub.add(spread.scale(wing*.22)).add(0,.35,0);
                            Vec3 tip=hub.add(spread.scale(wing*radius*(.7+t*.6))).add(0,.7+Math.sin(t*2.7+motion*.15)*radius*.6,0).add(facing.scale(Math.sin(motion)*.12));
                            Vec3 low=tip.add(spread.scale(-wing*.25)).add(0,-.45-t*.5,0);
                            quad(consumer,matrix,root,tip,low,root,layer.color(),alpha*.24f);
                            ribbon(consumer,matrix,camera,root,tip,layer.width(),layer.color(),alpha);
                        }
                    }
                    case FANGS -> {
                        double close=.2+.8*Math.pow(Math.sin(Math.min(1,progress)*Math.PI),2);
                        Vec3 normal=camera.subtract(hub).normalize(),side=normal.cross(new Vec3(0,1,0)).normalize();
                        double size=Math.max(.6,radius);
                        for(int jaw:new int[]{-1,1})for(int i=0;i<layer.count();i++) {
                            double x=(i/(double)Math.max(1,layer.count()-1)-.5)*size*2;
                            Vec3 root=hub.add(side.scale(x)).add(0,jaw*size*close,0);
                            Vec3 tip=root.add(0,-jaw*(.4+size*.4),0);
                            quad(consumer,matrix,root.subtract(side.scale(.13)),root.add(side.scale(.13)),tip,tip,layer.color(),alpha);
                            if(i>0)ribbon(consumer,matrix,camera,root.subtract(side.scale(size*2/Math.max(1,layer.count()-1))),root,layer.width()*2,layer.color(),alpha);
                        }
                    }
                    case CLOCK, EYE -> {
                        Vec3 normal=camera.subtract(hub).normalize(),side=normal.cross(new Vec3(0,1,0)).normalize(),up=side.cross(normal).normalize();
                        orientedRing(consumer,matrix,hub,side,up,radius,layer.width(),layer.color(),alpha,motion);
                        if(layer.shape()==SpellVisual.Shape.CLOCK) {
                            for(int i=0;i<12;i++) {
                                double a=i*Math.PI/6;
                                ribbon(consumer,matrix,camera,hub.add(side.scale(Math.cos(a)*radius*.82)).add(up.scale(Math.sin(a)*radius*.82)),hub.add(side.scale(Math.cos(a)*radius)).add(up.scale(Math.sin(a)*radius)),layer.width(),layer.color(),alpha);
                            }
                            ribbon(consumer,matrix,camera,hub,hub.add(side.scale(Math.cos(motion)*radius*.7)).add(up.scale(Math.sin(motion)*radius*.7)),layer.width()*1.5,layer.color(),alpha);
                            ribbon(consumer,matrix,camera,hub,hub.add(side.scale(Math.cos(motion*.3)*radius*.45)).add(up.scale(Math.sin(motion*.3)*radius*.45)),layer.width()*2,layer.color(),alpha);
                        } else {
                            sphere(consumer,matrix,hub,radius*.22,layer.color(),alpha);
                            for(int i=0;i<24;i++) {
                                double a=i*Math.PI/12,b=(i+1)*Math.PI/12;
                                ribbon(consumer,matrix,camera,hub.add(side.scale(Math.cos(a)*radius*1.4)).add(up.scale(Math.sin(a)*radius*.6)),hub.add(side.scale(Math.cos(b)*radius*1.4)).add(up.scale(Math.sin(b)*radius*.6)),layer.width(),layer.color(),alpha);
                            }
                        }
                    }
                    case ARC -> {
                        for (int i = 1; i < points.size(); i++) {
                            var path = VisualGeometry.arc(points.get(i - 1), points.get(i), cue.data.seed() + i * 13L + (cue.age / 2));
                            for (int j = 1; j < path.size(); j++) {
                                ribbon(consumer, matrix, camera, path.get(j - 1), path.get(j), layer.width() * 2.5, layer.color(), alpha * .25f);
                                ribbon(consumer, matrix, camera, path.get(j - 1), path.get(j), layer.width(), layer.color(), alpha);
                            }
                        }
                    }
                    case BEAM -> {
                        if (points.size() > 1) {
                            ribbon(consumer, matrix, camera, points.getFirst(), points.getLast(), layer.width() * 3, layer.color(), alpha * .25f);
                            ribbon(consumer, matrix, camera, points.getFirst(), points.getLast(), layer.width(), layer.color(), alpha);
                        }
                    }
                    case JET -> {
                        if(points.size()>1) {
                            Vec3 from=points.getFirst(),to=points.getLast(),direction=to.subtract(from).normalize();
                            Vec3 side=direction.cross(new Vec3(0,1,0)).normalize();
                            ribbon(consumer,matrix,camera,from,to,layer.width(),layer.color(),alpha*.5f);
                            for(int i=0;i<8;i++) {
                                double t=((i/8.0+age*.11)%1);
                                Vec3 bead=from.lerp(to,t).add(side.scale(Math.sin(t*12+age*.7)*.12));
                                sphere(consumer,matrix,bead,.12+Math.sin(t*Math.PI)*.12,layer.color(),alpha*.8f);
                                ribbon(consumer,matrix,camera,bead,bead.add(direction.scale(.35)),.07,0xe0f7ff,alpha);
                            }
                        }
                    }
                    case SHIELD -> {
                        Vec3 center=points.getLast();
                        for(int panel=0;panel<6;panel++) {
                            double angle=panel*Math.PI/3+age*.006;
                            Vec3 outward=new Vec3(Math.cos(angle),0,Math.sin(angle));
                            Vec3 side=new Vec3(-outward.z,0,outward.x);
                            Vec3 panelHub=center.add(outward.scale(radius));
                            for(int edge=0;edge<6;edge++) {
                                double a=edge*Math.PI/3,b=(edge+1)*Math.PI/3;
                                Vec3 first=panelHub.add(side.scale(Math.cos(a)*.5)).add(0,Math.sin(a)*.65,0);
                                Vec3 second=panelHub.add(side.scale(Math.cos(b)*.5)).add(0,Math.sin(b)*.65,0);
                                quad(consumer,matrix,panelHub,first,second,panelHub,layer.color(),alpha*.12f);
                                ribbon(consumer,matrix,camera,first,second,layer.width(),layer.color(),alpha);
                            }
                            ribbon(consumer,matrix,camera,panelHub.add(0,-.22,0),panelHub.add(0,.22,0),.04,0xffffff,alpha);
                        }
                    }
                    case RING -> ring(consumer, matrix, points.getLast(), radius, layer.width(), layer.color(), alpha);
                    case PULSE -> {
                        double reach=cue.data.visual().radius()*layer.scale()*VisualGeometry.pulseRadius(progress);
                        ring(consumer,matrix,hub,reach,layer.width(),layer.color(),alpha);
                        for(int i=0;i<layer.count();i++) {
                            double angle=layer.phase()+i*Math.PI*2/layer.count();
                            ribbon(consumer,matrix,camera,radial(hub,reach*.8,angle),radial(hub,reach,angle),
                                    layer.width(),layer.color(),alpha);
                        }
                    }
                    case SPHERE -> sphere(consumer, matrix, points.getLast(), radius, layer.color(), alpha);
                    case VEIL -> sphere(consumer, matrix, points.getLast(), radius, layer.color(), alpha*(.85f+.15f*(float)Math.sin(age*.1)));
                    case BOX -> {
                        var point=cue.data.points().getLast();
                        var box=cue.data.follow() && matching(point) ? level.getEntity(point.entityId()).getBoundingBox()
                                : new net.minecraft.world.phys.AABB(points.getLast().add(-radius,0,-radius),points.getLast().add(radius,radius*2,radius));
                        box(consumer,matrix,box,layer.color(),alpha);
                    }
                    case BODY -> {
                        Vec3 center=points.getLast();
                        for (int i=0;i<3;i++) {
                            Vec3 copy=radial(center,Math.max(.8,radius),i*Math.PI*2/3+age*.025);
                            box(consumer,matrix,new net.minecraft.world.phys.AABB(copy.add(-.25,0,-.25),copy.add(.25,1.5,.25)),layer.color(),alpha);
                            sphere(consumer,matrix,copy.add(0,1.7,0),.23,layer.color(),alpha);
                        }
                    }
                    case TREE -> {
                        Vec3 center=points.getLast();
                        box(consumer,matrix,new net.minecraft.world.phys.AABB(center.add(-.25,0,-.25),center.add(.25,2,.25)),0x72533e,alpha);
                        sphere(consumer,matrix,center.add(0,2.2,0),radius,layer.color(),alpha);
                        ring(consumer,matrix,center.add(0,.03,0),radius,.1,layer.color(),alpha*.5f);
                    }
                    case WAVE -> {
                        Vec3 center=points.getLast();
                        for (int i=0;i<12;i++) {
                            double x=(i/11.0-.5)*radius*2;
                            ribbon(consumer,matrix,camera,center.add(x,0,Math.sin(age*.13+i)*.12),center.add(x,3,Math.sin(age*.13+i+.5)*.12),radius/6,layer.color(),alpha);
                        }
                    }
                    case RAIN -> {
                        Random random=new Random(cue.data.seed()); Vec3 center=points.getLast();
                        for (int i=0;i<36;i++) {
                            double x=(random.nextDouble()-.5)*radius*2,z=(random.nextDouble()-.5)*radius*2;
                            double y=4-((age*.24+random.nextDouble()*4)%4);
                            ribbon(consumer,matrix,camera,center.add(x,y,z),center.add(x-.12,y+.5,z),layer.width(),layer.color(),alpha);
                        }
                    }
                    case SPARKS -> {
                        // Visible glints supplement dust; both share the same bounded cosmetic footprint.
                        int count=minecraft.options.particles().get()==net.minecraft.client.ParticleStatus.MINIMAL?0:18;
                        for(int i=0;i<count && quads<4096;i++) {
                            Vec3 center=path(cue)?VisualGeometry.pathPoint(points,(i+.5)/count):points.getLast();
                            Vec3 at=center.add(VisualGeometry.emissionOffset(cue.data.seed(),age,radius,i));
                            double size=.1*layer.scale();
                            ribbon(consumer,matrix,camera,at.add(0,-size,0),at.add(0,size,0),size*.45,layer.color(),alpha);
                            ribbon(consumer,matrix,camera,at.add(-size,0,0),at.add(size,0,0),size*.3,0xffffff,alpha*.8f);
                        }
                    }
                    default -> { }
                }
            }
        }
        buffers.endBatch(RenderType.debugQuads());
    }
    /** Paths and faceted surfaces share the same global quad budget as every other layer. */
    private static void stroke(VertexConsumer out,Matrix4f matrix,Vec3 camera,List<Vec3> path,double width,int color,float alpha) {
        for(int i=1;i<path.size() && quads<4096;i++)ribbon(out,matrix,camera,path.get(i-1),path.get(i),width,color,alpha);
    }
    private static void orientedRing(VertexConsumer out,Matrix4f matrix,Vec3 center,Vec3 side,Vec3 up,double radius,double width,int color,float alpha,double rotation) {
        for(int i=0;i<32 && quads<4096;i++) {
            double a=rotation+i*Math.PI/16,b=rotation+(i+1)*Math.PI/16;
            Vec3 from=center.add(side.scale(Math.cos(a)*radius)).add(up.scale(Math.sin(a)*radius));
            Vec3 to=center.add(side.scale(Math.cos(b)*radius)).add(up.scale(Math.sin(b)*radius));
            ribbon(out,matrix,renderCamera,from,to,width,color,alpha);
        }
    }
    private static void crystal(VertexConsumer out,Matrix4f matrix,Vec3 center,double width,double height,int color,float alpha) {
        Vec3 top=center.add(0,height,0),bottom=center.add(0,-height*.25,0);
        for(int i=0;i<4 && quads<4096;i++) {
            Vec3 a=radial(center,width,i*Math.PI/2),b=radial(center,width,(i+1)*Math.PI/2);
            quad(out,matrix,a,b,top,top,color,alpha*(.65f+i*.08f));
            quad(out,matrix,b,a,bottom,bottom,color,alpha*.7f);
        }
    }
    private static boolean path(Cue cue) {
        return cue.data.visual().layers().stream().anyMatch(l->l.shape()==SpellVisual.Shape.ARC || l.shape()==SpellVisual.Shape.BEAM || l.shape()==SpellVisual.Shape.JET);
    }
    private static void ribbon(VertexConsumer out, Matrix4f matrix, Vec3 camera, Vec3 from, Vec3 to, double width, int color, float alpha) {
        Vec3 direction = to.subtract(from);
        if (direction.lengthSqr() < 1e-8) return;
        Vec3 side = direction.cross(camera.subtract(from.lerp(to, .5))).normalize();
        if (side.lengthSqr() < .1) side = direction.cross(Math.abs(direction.normalize().y) > .95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize();
        side = side.scale(width / 2);
        quad(out, matrix, from.add(side), to.add(side), to.subtract(side), from.subtract(side), color, alpha);
    }
    private static void ring(VertexConsumer out, Matrix4f matrix, Vec3 center, double radius, double width, int color, float alpha) {
        for (int i = 0; i < 48; i++) {
            if (quads >= 4096) return;
            double a = i * Math.PI * 2 / 48, b = (i + 1) * Math.PI * 2 / 48;
            quad(out, matrix, radial(center, radius + width / 2, a), radial(center, radius + width / 2, b),
                    radial(center, Math.max(0, radius - width / 2), b), radial(center, Math.max(0, radius - width / 2), a), color, alpha);
        }
    }
    private static Vec3 radial(Vec3 center, double radius, double angle) { return center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius); }
    private static void sphere(VertexConsumer out, Matrix4f matrix, Vec3 center, double radius, int color, float alpha) {
        for (int latitude = 0; latitude < 8; latitude++) {
            double a = -Math.PI / 2 + latitude * Math.PI / 8, b = a + Math.PI / 8;
            for (int longitude = 0; longitude < 16; longitude++) {
                if (quads >= 4096) return;
                double u = longitude * Math.PI * 2 / 16, v = u + Math.PI * 2 / 16;
                quad(out, matrix, spherical(center, radius, a, u), spherical(center, radius, b, u),
                        spherical(center, radius, b, v), spherical(center, radius, a, v), color, alpha);
            }
        }
    }
    private static Vec3 spherical(Vec3 center, double radius, double latitude, double longitude) {
        return center.add(radius * Math.cos(latitude) * Math.cos(longitude), radius * Math.sin(latitude), radius * Math.cos(latitude) * Math.sin(longitude));
    }
    private static void box(VertexConsumer out,Matrix4f matrix,net.minecraft.world.phys.AABB b,int color,float alpha) {
        Vec3 a=new Vec3(b.minX,b.minY,b.minZ),c=new Vec3(b.maxX,b.minY,b.minZ),d=new Vec3(b.maxX,b.maxY,b.minZ),e=new Vec3(b.minX,b.maxY,b.minZ);
        Vec3 f=new Vec3(b.minX,b.minY,b.maxZ),g=new Vec3(b.maxX,b.minY,b.maxZ),h=new Vec3(b.maxX,b.maxY,b.maxZ),i=new Vec3(b.minX,b.maxY,b.maxZ);
        quad(out,matrix,a,c,d,e,color,alpha); quad(out,matrix,f,i,h,g,color,alpha);
        quad(out,matrix,a,f,g,c,color,alpha); quad(out,matrix,e,d,h,i,color,alpha);
        quad(out,matrix,a,e,i,f,color,alpha); quad(out,matrix,c,g,h,d,color,alpha);
    }
    private static void quad(VertexConsumer out, Matrix4f matrix, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color, float alpha) {
        if (quads++ >= 4096 || alpha <= 0) return;
        for (Vec3 point : List.of(a, b, c, d)) out.addVertex(matrix, (float) (point.x - renderCamera.x),
                (float) (point.y - renderCamera.y), (float) (point.z - renderCamera.z))
                .setColor(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f, alpha);
    }
    private static final class Cue {
        final SpellVisualPayload data;
        int age, missing;
        Vec3 previous;
        Cue(SpellVisualPayload data) { this.data = data; age = data.elapsed() - 1; }
    }
}
