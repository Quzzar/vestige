package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.presentation.*;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** Shared native leases: collision, boundaries, protection, movement and private perception. */
final class NativeSpellFeatures {
    private final MinecraftSpellWorld world;
    private final Set<Lease> leases = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<TemporaryBlockFormation> collapsingWalls=Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean transferringDamage;
    NativeSpellFeatures(MinecraftSpellWorld world) { this.world=world; }
    void close() { List.copyOf(leases).forEach(l -> l.close(SpellRuntime.EndReason.SERVER_STOP)); collapsingWalls.forEach(w->w.finish(null));collapsingWalls.clear(); }
    void tickAnimations() { for(var wall:List.copyOf(collapsingWalls)) { wall.tick();if(wall.finished()) collapsingWalls.remove(wall); } }
    void unloading(net.minecraft.world.level.chunk.ChunkAccess chunk) {
        List.copyOf(leases).forEach(l -> l.unloading(chunk));
        for(var wall:List.copyOf(collapsingWalls)) if(wall.touches(chunk)) { wall.finish(chunk);collapsingWalls.remove(wall); }
    }
    boolean ownsTemporaryBlock(ServerLevel level,BlockPos pos) {
        return leases.stream().anyMatch(l->l.level==level && l.blocks!=null && l.blocks.owns(pos))
                || collapsingWalls.stream().anyMatch(w->w.level()==level && w.owns(pos));
    }
    void blockChanged(ServerLevel level,BlockPos pos) {
        for(Lease lease:List.copyOf(leases)) if(lease.level==level && lease.blocks!=null) lease.blocks.changed(pos);
        for(var wall:collapsingWalls) if(wall.level()==level) wall.changed(pos);
    }
    boolean consent(LivingEntity caster, LivingEntity recipient) {
        return caster == recipient || recipient instanceof OwnableEntity own && caster.getUUID().equals(own.getOwnerUUID())
                || world.isOwnedBy(recipient,caster.getUUID())
                || world.ally(caster,recipient) && (!(recipient instanceof ServerPlayer) || recipient.getPersistentData().getBoolean("vestige:accept_magic"));
    }
    boolean obscured(Vec3 from, Vec3 to, ServerLevel level) {
        return leases.stream().anyMatch(l -> l.alive() && l.level==level && (l instanceof Construct && l.body.canBeCollidedWith()
                && l.body.getBoundingBox().clip(from.add(0,1,0),to.add(0,1,0)).isPresent()
                || l instanceof Zone z && (z.behavior.equals("privacy") || z.behavior.equals("rain")) && z.inside(from)!=z.inside(to)));
    }
    boolean silent(Vec3 point, ServerLevel level) {
        return leases.stream().anyMatch(l -> l.alive() && l.level==level && l instanceof Zone z && z.behavior.equals("silence") && z.inside(point));
    }
    boolean absent(UUID subject) { return leases.stream().anyMatch(l -> l.alive() && l instanceof Mobility m && m.behavior.equals("absence") && m.recipient.getUUID().equals(subject)); }
    boolean remote(UUID actor) { return leases.stream().anyMatch(l -> l.alive() && l instanceof Sensor s && s.behavior.equals("camera") && s.caster.getUUID().equals(actor)); }
    Optional<SpellWorld.ManifestationHandle> create(SpellEffects.Manifestation definition, Map<String,Double> values, SpellRuntime.Context context) {
        LivingEntity caster=world.actor(context); if (caster==null || leases.size()>=128) return Optional.empty();
        ServerLevel level=world.level(context.target(),caster); if (level==null) return Optional.empty();
        Vec3 point=world.position(context.target(),caster);
        Lease lease = switch (definition.kind().getPath()) {
            case "construct" -> construct(definition,values,context,caster,level,point);
            case "block_wall" -> blockWall(definition,values,context,caster,level,point);
            case "zone" -> zone(definition,values,context,caster,level,point);
            case "guard" -> world.target(context) instanceof LivingEntity target && consent(caster,target)
                    ? new Guard(definition,values,context,caster,level,target) : null;
            case "mobility" -> world.target(context) instanceof LivingEntity target && consent(caster,target)
                    ? mobility(definition,values,context,caster,level,target) : null;
            case "sensor" -> obscured(caster.position(),point,level) ? null : new Sensor(definition,values,context,caster,level,point);
            case "passage" -> passage(definition,values,context,caster,level,point);
            case "pet_cache" -> PetCache.create(world,definition,values,context);
            default -> null;
        };
        if (lease==null || lease.body==null) return Optional.empty();
        leases.add(lease); return Optional.of(lease);
    }
    abstract class Lease implements SpellWorld.ManifestationHandle {
        final UUID id=UUID.randomUUID();
        final SpellEffects.Manifestation definition;
        final Map<String,Double> values;
        final SpellRuntime.Context context;
        final LivingEntity caster;
        final ServerLevel level;
        Entity body;
        TemporaryBlockFormation blocks;
        boolean closed;
        int age;
        Lease(SpellEffects.Manifestation definition, Map<String,Double> values, SpellRuntime.Context context,
              LivingEntity caster, ServerLevel level, Entity body) {
            this.definition=definition; this.values=values; this.context=context; this.caster=caster; this.level=level; this.body=body;
        }
        public SpellSubject subject() { return new SpellSubject.Entity(body.getUUID()); }
        public boolean alive() { return !closed && body!=null && body.isAlive() && !body.isRemoved() && caster.isAlive() && caster.level()==level; }
        public void tick() { age++;if(blocks!=null) blocks.tick(); }
        public void close(SpellRuntime.EndReason reason) {
            if (closed) return; closed=true; leases.remove(this); release();
            if(blocks!=null) {
                if(reason==SpellRuntime.EndReason.EXPIRED) { blocks.collapse();collapsingWalls.add(blocks); }
                else blocks.finish(null);
            }
            if (body!=null && body!=caster && !(this instanceof Mobility) && !(this instanceof Guard)) {
                world.owners.remove(body.getUUID()); world.causedEntities.remove(body.getUUID()); body.discard();
            }
        }
        void release() { }
        void unloading(net.minecraft.world.level.chunk.ChunkAccess chunk) { if(blocks!=null && blocks.touches(chunk)) { blocks.finish(chunk);close(SpellRuntime.EndReason.BACKING_REMOVED); } }
        double n(String key,double fallback,double max) { return bounded(values,key,fallback,0,max); }
        String identifier(String key,String fallback) { return definition.identifiers().getOrDefault(key,VestigeMainMod.location(fallback)).getPath(); }
        void state(String kind, Entity subject, double radius, boolean stop) {
            var packet=new SpellSensePayload(id,level.dimension().location(),kind,subject.getId(),subject.getUUID(),body.position(),radius,
                    Math.max(1,Math.min(2400,definition.durationTicks()-age)),stop);
            if (kind.equals("camera") || kind.equals("facade")) { if (caster instanceof ServerPlayer p) PacketDistributor.sendToPlayer(p,packet); }
            else if (this instanceof Mobility && subject instanceof ServerPlayer p) PacketDistributor.sendToPlayer(p,packet);
            else for (ServerPlayer p : level.players()) if (stop || p.distanceToSqr(body)<160*160) PacketDistributor.sendToPlayer(p,packet);
        }
    }
    private Entity marker(ServerLevel level,Vec3 point,LivingEntity caster,SpellRuntime.Context context) {
        SpellAnchor marker=SpellEntities.ANCHOR.get().create(level); if (marker==null) return null;
        marker.setPos(point); marker.setNoGravity(true); marker.setInvisible(true);
        if (!level.addFreshEntity(marker)) return null;
        world.owners.put(marker.getUUID(),caster.getUUID()); world.causedEntities.put(marker.getUUID(),context.cause()); return marker;
    }
    private BlockWall blockWall(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,Vec3 point) {
        int width=(int)bounded(v,"width",5,1,9),height=(int)bounded(v,"height",3,1,6);
        Direction facing=Direction.getNearest(caster.getLookAngle().x,0,caster.getLookAngle().z);
        BlockPos base=BlockPos.containing(point.add(0,.01,0));
        var wall=new TemporaryBlockFormation(level,SpellBlocks.TEMPORARY_ICE.get().defaultBlockState(),base,facing.getClockWise(),width,height,
                (int)bounded(v,"rise_ticks",24,6,80),(int)bounded(v,"collapse_ticks",20,6,80));
        if(!wall.canPlace(pos->permitted(level,pos,caster))) return null;
        Entity body=marker(level,base.getBottomCenter(),caster,c);if(body==null) return null;
        // The lifetime anchor sits inside real terrain; only the blocks are attackable.
        body.noPhysics=true;body.setInvulnerable(true);
        return new BlockWall(d,v,c,caster,level,body,wall);
    }
    private final class BlockWall extends Lease {
        BlockWall(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,Entity body,TemporaryBlockFormation wall) {
            super(d,v,c,caster,level,body);blocks=wall;
        }
    }
    private Construct construct(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,Vec3 point) {
        SpellConstruct body=SpellEntities.CONSTRUCT.get().create(level); if (body==null) return null;
        boolean tree=d.identifiers().getOrDefault("formation",VestigeMainMod.location("none")).getPath().equals("tree");
        body.geometry(bounded(v,"width",1,.2,12),bounded(v,"height",2,.2,6),bounded(v,"depth",1,.1,12),v.getOrDefault("solid",0d)>0 && !tree);
        body.setPos(point); body.setHealth((float)bounded(v,"health",16,1,100));
        if (body.canBeCollidedWith() && (!level.noCollision(body,body.getBoundingBox())
                || !level.getEntities(body,body.getBoundingBox(),e -> e instanceof LivingEntity).isEmpty()) || !level.getWorldBorder().isWithinBounds(body.getBoundingBox())) return null;
        if (caster instanceof Player p && !p.mayBuild()) return null;
        TemporaryBlockFormation formation=null;
        if(tree) {
            BlockPos base=BlockPos.containing(point.add(0,.01,0));
            formation=TemporaryBlockFormation.tree(level,base,36,24);
            if(!formation.canPlace(pos->permitted(level,pos,caster))) return null;
            body.setPos(base.getBottomCenter());
        }
        if (!level.addFreshEntity(body)) return null;
        world.owners.put(body.getUUID(),caster.getUUID()); world.causedEntities.put(body.getUUID(),c.cause());
        Construct result=new Construct(d,v,c,caster,level,body);result.blocks=formation;return result;
    }
    private final class Construct extends Lease {
        final String behavior;
        final List<UUID> recipients;
        double budget;
        Construct(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,SpellConstruct body) {
            super(d,v,c,caster,level,body); behavior=identifier("behavior","none"); budget=n("budget",0,32);
            recipients=level.getEntitiesOfClass(LivingEntity.class,body.getBoundingBox().inflate(n("radius",3,12)),e -> consent(caster,e) && !(e instanceof SpellAnchor))
                    .stream().sorted(Comparator.comparingDouble(e -> e.distanceToSqr(body))).limit((long)n("count",3,6)).map(Entity::getUUID).toList();
        }
        @Override public void tick() {
            super.tick();
            if(blocks!=null && body instanceof LivingEntity tree) {
                tree.setHealth((float)Math.min(tree.getHealth(),n("health",16,100)*blocks.survivingTrunks()/4));
                if(tree.getHealth()<=0) tree.discard();
            }
            if (age%20!=0) return;
            CausalChain previous=world.executingCause; world.executingCause=context.cause();
            try {
                if (behavior.equals("heal") && budget>0) for (UUID id : recipients) {
                    if (world.entity(id) instanceof LivingEntity e && e.isAlive() && e.level()==level && e.distanceToSqr(body)<=n("radius",4,12)*n("radius",4,12)) {
                        float before=e.getHealth(); e.heal((float)Math.min(budget,n("amount",1,8))); budget-=Math.max(0,e.getHealth()-before);
                    }
                }
                if (behavior.equals("debilitate") && budget>0) for (LivingEntity e : nearby(body.position(),n("radius",4,12),level)) if (!world.ally(caster,e)) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0)); budget--; if (budget<=0) break;
                }
                if (behavior.equals("extinguish")) nearby(body.position(),n("radius",4,12),level).stream().filter(e -> world.ally(caster,e)).limit(3).forEach(Entity::clearFire);
                if (behavior.equals("pressure") && budget>0) for (LivingEntity e : nearby(body.position(),n("radius",3,12),level)) if (!world.ally(caster,e)) {
                    double amount=Math.min(budget,n("amount",2,6)); e.invulnerableTime=0; e.hurt(caster.damageSources().indirectMagic(body,caster),(float)amount); budget-=amount; if (budget<=0) break;
                }
                if (behavior.equals("intercept")) for (Projectile p : level.getEntitiesOfClass(Projectile.class,body.getBoundingBox().inflate(n("radius",3,12))))
                    if (!(p instanceof SpellProjectile) && p.getOwner()!=caster) p.setDeltaMovement(p.getDeltaMovement().scale(.5));
            } finally { world.executingCause=previous; }
        }
        @Override public void close(SpellRuntime.EndReason reason) {
            boolean fracture=!closed && reason==SpellRuntime.EndReason.BACKING_REMOVED && body instanceof LivingEntity living && living.getHealth()<=0;
            Vec3 position=body.position(); super.close(reason);
            if (fracture && !definition.onHit().isEmpty()) context.execute(definition.onHit(),new SpellSubject.Position(level.dimension(),position));
        }
    }
    private Mobility mobility(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,LivingEntity target) {
        Mobility lease=new Mobility(d,v,c,caster,level,target);
        if (lease.behavior.equals("scale")) {
            double factor=bounded(v,"factor",1,.5,1.5);
            if (factor>1 && !level.noCollision(target,new AABB(target.getX()-target.getBbWidth()*factor/2,target.getY(),target.getZ()-target.getBbWidth()*factor/2,target.getX()+target.getBbWidth()*factor/2,target.getY()+target.getBbHeight()*factor,target.getZ()+target.getBbWidth()*factor/2))) return null;
            AttributeInstance attribute=target.getAttribute(Attributes.SCALE); if (attribute==null) return null;
            attribute.addTransientModifier(new AttributeModifier(lease.modifier,factor-1,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)); target.refreshDimensions();
            if (target instanceof Player) for (var reach : List.of(Attributes.BLOCK_INTERACTION_RANGE,Attributes.ENTITY_INTERACTION_RANGE)) {
                if (target.getAttribute(reach)!=null) target.getAttribute(reach).addTransientModifier(new AttributeModifier(lease.modifier,factor>1?1:-1,AttributeModifier.Operation.ADD_VALUE));
            }
        }
        lease.stateIf(false); return lease;
    }
    private final class Mobility extends Lease {
        final LivingEntity recipient;
        final String behavior;
        final ResourceLocation modifier=VestigeMainMod.location("lease/"+id);
        final Vec3 start;
        Mobility(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,LivingEntity target) {
            super(d,v,c,caster,level,target); recipient=target; behavior=identifier("behavior","climb"); start=target.position();
        }
        @Override public boolean alive() { return super.alive() && recipient.level()==level; }
        void stateIf(boolean stop) { if (Set.of("climb","water_walk","absence").contains(behavior)) state(behavior,recipient,0,stop); }
        @Override public void tick() {
            super.tick(); SpellMobility.apply(recipient,behavior);
            if (behavior.equals("absence")) { recipient.setDeltaMovement(Vec3.ZERO); recipient.setPos(start); recipient.hurtMarked=true; }
            if (age%20==0) stateIf(false);
        }
        @Override void release() {
            stateIf(true);
            if (behavior.equals("scale")) {
                for (var attribute : List.of(Attributes.SCALE,Attributes.BLOCK_INTERACTION_RANGE,Attributes.ENTITY_INTERACTION_RANGE))
                    if (recipient.getAttribute(attribute)!=null) recipient.getAttribute(attribute).removeModifier(modifier);
                recipient.refreshDimensions();
                // Restore normal clearance before choosing a non-colliding position, even after a shrink entered a small cavity.
                if (!level.noCollision(recipient,recipient.getBoundingBox())) for (int dy=1;dy<=128;dy++) {
                    Vec3 destination=recipient.position().add(0,dy,0);
                    if (level.hasChunkAt(BlockPos.containing(destination)) && level.getWorldBorder().isWithinBounds(recipient.getBoundingBox().move(0,dy,0))
                            && level.noCollision(recipient,recipient.getBoundingBox().move(0,dy,0))) { recipient.teleportTo(destination.x,destination.y,destination.z); break; }
                }
            }
        }
    }
    private final class Guard extends Lease {
        final LivingEntity recipient;
        final String behavior;
        double remaining;
        final List<SpellEcho> echoes=new ArrayList<>();
        Guard(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,LivingEntity target) {
            super(d,v,c,caster,level,target); recipient=target; behavior=identifier("behavior","heavy"); remaining=n("budget",6,32);
            if(behavior.equals("images")) for(int i=0;i<Math.min(8,(int)remaining);i++) {
                SpellEcho echo=SpellEntities.ECHO.get().create(level);if(echo==null) continue;
                echo.source(recipient);echo.setPos(copyPosition(i));
                if(level.addFreshEntity(echo)) echoes.add(echo);
            }
        }
        Vec3 copyPosition(int index) { double angle=index*Math.PI*2/3;return recipient.position().add(Math.cos(angle)*1.35,0,Math.sin(angle)*1.35); }
        void syncCopies() {
            while(echoes.size()>(int)remaining) {
                SpellEcho echo=echoes.removeLast();
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,echo.getX(),echo.getY()+1,echo.getZ(),30,.4,.7,.4,.06);echo.discard();
            }
            for(int i=0;i<echoes.size();i++) { echoes.get(i).setPos(copyPosition(i));echoes.get(i).setYRot(recipient.getYRot()); }
        }
        @Override void release() { echoes.forEach(Entity::discard);echoes.clear(); }
        @Override public boolean alive() { return super.alive() && remaining>0 && recipient.level()==level; }
        @Override public void tick() {
            super.tick();syncCopies();
            if (behavior.equals("air") && (recipient.isInWater() || recipient.isInWall())) recipient.setAirSupply(recipient.getMaxAirSupply());
            else if (behavior.equals("air") && age>1) remaining=0;
        }
    }
    void intercept(LivingEntity target, DamageSource source, SpellEvent.PendingOutcome pending) {
        for (Lease lease : List.copyOf(leases)) {
            if (!lease.alive() || target.level()!=lease.level || pending.amount()<=0) continue;
            if (lease instanceof Mobility m && m.recipient==target && m.behavior.equals("absence")) pending.reduce(pending.amount());
            if (lease instanceof Sensor s && s.caster==target && s.behavior.equals("camera")) s.close(SpellRuntime.EndReason.INTERRUPTED);
            if (lease instanceof Construct c && c.behavior.equals("protect") && c.recipients.contains(target.getUUID())
                    && target.distanceToSqr(c.body)<=c.n("radius",3,12)*c.n("radius",3,12)) {
                SpellConstruct tree=(SpellConstruct)c.body; double used=Math.min(pending.amount(),tree.getHealth());
                pending.reduce(used); tree.setHealth((float)(tree.getHealth()-used)); if (tree.getHealth()<=0) tree.discard();
            }
            if (lease instanceof Zone z && z.behavior.equals("containment") && z.inside(target.position()) && source.getEntity()!=null && !z.inside(source.getEntity().position())) {
                z.durability=Math.min(z.durability,((LivingEntity)z.body).getHealth());
                double used=Math.min(z.durability,pending.amount()); pending.reduce(used); z.durability-=used;
                ((LivingEntity)z.body).setHealth((float)z.durability);
            }
            if (!(lease instanceof Guard g) || g.recipient!=target) continue;
            switch (g.behavior) {
                case "share" -> {
                    if (transferringDamage || target==g.caster || target.distanceToSqr(g.caster)>g.n("range",16,32)*g.n("range",16,32)) break;
                    double requested=Math.min(g.remaining,Math.min(pending.amount()*.5,Math.max(0,g.caster.getHealth()-1)));
                    if (requested<=0) break;
                    CausalChain old=world.executingCause; world.executingCause=world.cause(source.getDirectEntity()); transferringDamage=true;
                    float health=g.caster.getHealth(), absorption=g.caster.getAbsorptionAmount();
                    try { g.caster.invulnerableTime=0; g.caster.hurt(g.caster.damageSources().indirectMagic(g.caster,g.caster),(float)requested); }
                    finally { transferringDamage=false; world.executingCause=old; }
                    double accepted=Math.min(requested,Math.max(0,health-g.caster.getHealth())+Math.max(0,absorption-g.caster.getAbsorptionAmount()));
                    pending.reduce(accepted); g.remaining-=accepted;
                }
                case "heavy" -> {
                    if (pending.amount()<g.n("threshold",8,32)) break;
                    double used=Math.min(pending.amount(),g.remaining); pending.reduce(used); g.remaining=0;
                    for (int sign : new int[]{1,-1}) if (SpellActions.teleport(target,g.level,target.position().add(sign*1.5,0,0),true)) break;
                }
                case "images" -> {
                    if (source.getEntity()==null || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_PROJECTILE)) break;
                    pending.reduce(Math.min(pending.amount(),g.n("per_hit",5,10))); g.remaining--;g.syncCopies();
                }
                case "air" -> { if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN) || source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)) pending.reduce(pending.amount()); }
                default -> { }
            }
        }
    }
    private Zone zone(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,Vec3 point) {
        TemporaryBlockFormation formation=null;
        if(d.identifiers().getOrDefault("formation",VestigeMainMod.location("none")).getPath().equals("water")) {
            if(level.dimensionType().ultraWarm()) return null;
            BlockPos base=BlockPos.containing(point.add(0,.01,0));
            Direction facing=Direction.getNearest(caster.getLookAngle().x,0,caster.getLookAngle().z);
            formation=new TemporaryBlockFormation(level,SpellBlocks.TEMPORARY_WATER.get().defaultBlockState(),base,facing.getClockWise(),
                    (int)bounded(v,"width",5,1,9),(int)bounded(v,"height",3,1,6),24,20,false);
            if(!formation.canPlace(pos->permitted(level,pos,caster))) return null;
            point=base.getBottomCenter();
        }
        Zone zone=new Zone(d,v,c,caster,level,point);zone.blocks=formation;
        if(formation!=null && zone.body!=null) { zone.body.noPhysics=true;zone.body.setInvulnerable(true); }
        return zone;
    }
    private final class Zone extends Lease {
        final String behavior;
        final double radius;
        final Map<UUID,Vec3> previous=new HashMap<>();
        double durability;
        boolean spent;
        Zone(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,Vec3 point) {
            super(d,v,c,caster,level,marker(level,point,caster,c)); behavior=identifier("behavior","repel"); radius=n("radius",3,12); durability=n("health",16,100);
            if (body instanceof LivingEntity living && behavior.equals("containment")) {
                living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(durability); living.setHealth((float)durability);
            }
            if (body!=null && Set.of("silence","privacy","rain").contains(behavior)) state(behavior,body,radius,false);
            if (body!=null) for (LivingEntity entity : nearby(point,radius+4,level)) previous.put(entity.getUUID(),entity.position());
        }
        boolean inside(Vec3 point) { return blocks!=null?blocks.owns(BlockPos.containing(point)):point.distanceToSqr(body.position())<radius*radius; }
        @Override public boolean alive() { return super.alive() && durability>0 && !spent; }
        @Override public void tick() {
            super.tick(); Vec3 center=body.position();
            for (LivingEntity entity : nearby(center,radius+4,level)) {
                Vec3 before=previous.getOrDefault(entity.getUUID(),entity.position().subtract(entity.getDeltaMovement()));
                boolean entered=!inside(before)&&inside(entity.position()), exited=inside(before)&&!inside(entity.position());
                if (behavior.equals("repel") && entered && !world.ally(caster,entity) || behavior.equals("containment") && (entered||exited)) {
                    entity.teleportTo(before.x,before.y,before.z); entity.setDeltaMovement(Vec3.ZERO); entity.hurtMarked=true;
                }
                if (behavior.equals("slip") && entered && !world.ally(caster,entity)) {
                    Vec3 away=caster.position().subtract(entity.position()).multiply(1,0,1).normalize().scale(n("distance",3,8));
                    if (SpellActions.teleport(caster,level,caster.position().add(away),true)) spent=true;
                }
                if (behavior.equals("water") && body.getBoundingBox().inflate(n("width",3,8),n("height",3,6),.4).intersects(entity.getBoundingBox())) entity.clearFire();
                if (behavior.equals("rain") && inside(entity.position()) && entity.onGround()) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(entity.getDeltaMovement().multiply(.25,0,.25))); entity.hurtMarked=true;
                }
                previous.put(entity.getUUID(),entity.position());
            }
            previous.keySet().removeIf(uuid -> world.entity(uuid)==null || world.entity(uuid).level()!=level || world.entity(uuid).distanceToSqr(body)>(radius+8)*(radius+8));
            if (behavior.equals("water")) for (Projectile p : level.getEntitiesOfClass(Projectile.class,body.getBoundingBox().inflate(n("width",3,8),n("height",3,6),.4))) {
                if (!(p instanceof SpellProjectile) && !previous.containsKey(p.getUUID())) { p.setDeltaMovement(p.getDeltaMovement().scale(.25)); p.clearFire(); previous.put(p.getUUID(),p.position()); }
            }
            if (age%20==0 && Set.of("silence","privacy","rain").contains(behavior)) state(behavior,body,radius,false);
        }
        @Override void release() { if (Set.of("silence","privacy","rain").contains(behavior)) state(behavior,body,radius,true); }
    }
    private final class Sensor extends Lease {
        final String behavior;
        final List<UUID> recipients;
        Sensor(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,Vec3 point) {
            super(d,v,c,caster,level,marker(level,point,caster,c)); behavior=identifier("behavior","unseen");
            recipients=nearby(point,n("radius",8,24),level).stream().filter(e -> consent(caster,e)).limit((long)n("count",3,6)).map(Entity::getUUID).toList();
            if (body!=null && behavior.equals("camera")) {
                body.setYRot(caster.getYRot()); body.setXRot(caster.getXRot());
                // Living cameras use head yaw for the actual view, independently of body yaw.
                if(body instanceof LivingEntity view) {view.setYHeadRot(caster.getYRot());view.setYBodyRot(caster.getYRot());}
                state("camera",body,0,false);
            }
            if (body!=null && behavior.equals("facade")) state("facade",caster,0,false);
        }
        @Override public void tick() {
            super.tick(); if (age%10!=0) return;
            if (behavior.equals("camera")) state("camera",body,0,false);
            if (behavior.equals("unseen") && caster instanceof ServerPlayer player) for (LivingEntity e : nearby(caster.position(),n("radius",16,24),level)) {
                if (!e.isInvisible() || obscured(caster.position(),e.position(),level)) continue;
                UUID cue=UUID.randomUUID();
                var layer=new SpellVisual.Layer(SpellVisual.Shape.BOX,0xb2dfff,.18f,.03f,1);
                PacketDistributor.sendToPlayer(player,new SpellVisualPayload(cue,level.dimension().location(),new SpellVisual.Resolved(12,.6f,List.of(layer)),
                        List.of(MinecraftSpellWorld.visualPoint(e,0)),cue.getLeastSignificantBits(),0,true,false,false));
            }
            if (behavior.equals("status") && age%40==0 && caster instanceof ServerPlayer player) {
                for (UUID recipient:recipients) if (world.entity(recipient) instanceof LivingEntity living
                        && living.level()==level && !obscured(caster.position(),living.position(),level)) {
                    float health=living.getHealth()/living.getMaxHealth();
                    int color=health<.3 ? 0xf07889 : health<.7 ? 0xedc879 : 0xa2dfb0;
                    var layers=new ArrayList<SpellVisual.Layer>();
                    layers.add(new SpellVisual.Layer(SpellVisual.Shape.BOX,color,.4f,.02f,1));
                    if (!living.getActiveEffects().isEmpty()) layers.add(new SpellVisual.Layer(SpellVisual.Shape.MOTES,
                            0xf4e5ff,.7f,.025f,1,0,0,Math.min(16,living.getActiveEffects().size())));
                    PacketDistributor.sendToPlayer(player,new SpellVisualPayload(UUID.randomUUID(),level.dimension().location(),
                            new SpellVisual.Resolved(40,.6f,layers),List.of(MinecraftSpellWorld.visualPoint(living,0)),0,0,true,false,false));
                }
            }
        }
        @Override void release() { if (body!=null && (behavior.equals("camera") || behavior.equals("facade"))) state(behavior,behavior.equals("camera")?body:caster,0,true); }
    }
    private Lease passage(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c,LivingEntity caster,ServerLevel level,Vec3 point) {
        BlockPos entry=BlockPos.containing(point); Direction direction=Direction.getNearest(caster.getLookAngle().x,0,caster.getLookAngle().z);
        LinkedHashMap<BlockPos,BlockState> states=new LinkedHashMap<>();
        int depth=(int)bounded(v,"depth",3,1,4);
        for (int i=0;i<depth;i++) for (int y=0;y<2;y++) {
            BlockPos pos=entry.relative(direction,i).above(y); BlockState state=level.getBlockState(pos);
            if (!ordinaryStone(state) || !permitted(level,pos,caster) || level.getBlockEntity(pos)!=null) return null;
            states.put(pos,state);
        }
        Vec3 exit=entry.relative(direction,depth).getBottomCenter();
        if (!safe(caster,level,exit)) return null;
        Entity body=marker(level,entry.getBottomCenter(),caster,c); if (body==null) return null;
        for (BlockPos pos : states.keySet()) if (!level.setBlock(pos,Blocks.AIR.defaultBlockState(),3)) {
            states.forEach((p,s) -> { if (level.getBlockState(p).isAir()) level.setBlock(p,s,3); }); body.discard(); return null;
        }
        return new Lease(d,v,c,caster,level,body) {
            net.minecraft.world.level.chunk.ChunkAccess closingChunk;
            @Override void unloading(net.minecraft.world.level.chunk.ChunkAccess chunk) {
                if (states.keySet().stream().anyMatch(p -> new net.minecraft.world.level.ChunkPos(p).equals(chunk.getPos()))) {
                    closingChunk=chunk; close(SpellRuntime.EndReason.BACKING_REMOVED);
                }
            }
            @Override void release() {
                AABB tunnel=new AABB(entry).expandTowards(direction.getStepX()*depth,1,direction.getStepZ()*depth);
                for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,tunnel)) {
                    if (!SpellActions.teleport(entity,level,exit,true)) SpellActions.teleport(entity,level,entry.relative(direction.getOpposite()).getBottomCenter(),false);
                }
                states.forEach((p,s) -> {
                    if (closingChunk!=null && new net.minecraft.world.level.ChunkPos(p).equals(closingChunk.getPos())) {
                        if (closingChunk.getBlockState(p).isAir()) { closingChunk.setBlockState(p,s,false); closingChunk.setUnsaved(true); }
                    } else if (level.hasChunkAt(p) && level.getBlockState(p).isAir()) level.setBlock(p,s,3);
                });
            }
        };
    }
    boolean execute(SpellEffects.Action action,SpellRuntime.Context context) {
        LivingEntity caster=world.actor(context); if (caster==null || absent(caster.getUUID()) || remote(caster.getUUID())) return false;
        ServerLevel level=world.level(context.target(),caster); if (level==null) return false;
        Vec3 point=world.position(context.target(),caster); BlockPos pos=BlockPos.containing(point);
        Map<String,Double> v=new HashMap<>(); action.values().forEach((k,value)->v.put(k,context.gameplayValue(k,value)));
        switch (action.type().getPath()) {
            case "utterance": return !silent(caster.position(),level);
            case "create_water": {
                if (!permitted(level,pos,caster)) return false;
                BlockState block=level.getBlockState(pos);
                if (block.is(Blocks.CAULDRON)) return level.setBlock(pos,Blocks.WATER_CAULDRON.defaultBlockState(),3);
                if (block.is(Blocks.WATER_CAULDRON) && block.getValue(LayeredCauldronBlock.LEVEL)<3) return level.setBlock(pos,block.cycle(LayeredCauldronBlock.LEVEL),3);
                return false;
            }
            case "shape_stone": {
                Optional<SpellSubject> stored=context.anchor(VestigeMainMod.location("first_anchor")); if (stored.isEmpty() || world.level(stored.get(),caster)!=level) return false;
                BlockPos source=BlockPos.containing(world.position(stored.get(),caster));
                if (source.distSqr(pos)>36 || source.equals(pos) || !ordinaryStone(level.getBlockState(source)) || !level.getBlockState(pos).canBeReplaced()
                        || !permitted(level,source,caster) || !permitted(level,pos,caster) || level.getBlockEntity(source)!=null || level.getBlockEntity(pos)!=null
                        || !level.getEntities((Entity)null,new AABB(pos),e->e instanceof LivingEntity).isEmpty()) return false;
                BlockState material=level.getBlockState(source), previous=level.getBlockState(pos);
                if (!level.setBlock(pos,material,3)) return false;
                if (!level.setBlock(source,Blocks.AIR.defaultBlockState(),3)) { level.setBlock(pos,previous,3); return false; }
                return true;
            }
            case "transpose": {
                var recipients=nearby(caster.position(),bounded(v,"radius",8,1,16),level).stream().filter(e -> consent(caster,e) && !(e instanceof SpellAnchor))
                        .sorted(Comparator.comparingDouble(e -> e.distanceToSqr(caster))).limit((long)bounded(v,"count",3,1,3)).toList();
                if (recipients.isEmpty()) return false;
                var destinations=new ArrayList<Vec3>();
                for (int i=0;i<recipients.size();i++) {
                    Vec3 destination=point.add((i-1)*2,0,0);
                    if (!safe(recipients.get(i),level,destination)) return false;
                    destinations.add(destination);
                }
                for (int i=0;i<recipients.size();i++) { Vec3 destination=destinations.get(i); recipients.get(i).teleportTo(destination.x,destination.y,destination.z); recipients.get(i).fallDistance=0; }
                return true;
            }
            case "gather_items": {
                int count=(int)bounded(v,"count",8,1,16); double radius=bounded(v,"radius",8,1,16);
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,caster.getBoundingBox().inflate(radius),e->metal(e.getItem().getItem()))) {
                    if (count--<=0) break; Vec3 direction=caster.getEyePosition().subtract(item.position()).normalize(); item.setDeltaMovement(direction.scale(.6)); item.hurtMarked=true;
                }
                return true;
            }
            default: return false;
        }
    }
    private static boolean metal(net.minecraft.world.item.Item item) {
        return Set.of(net.minecraft.world.item.Items.IRON_INGOT,net.minecraft.world.item.Items.IRON_NUGGET,net.minecraft.world.item.Items.GOLD_INGOT,
                net.minecraft.world.item.Items.GOLD_NUGGET,net.minecraft.world.item.Items.COPPER_INGOT,net.minecraft.world.item.Items.RAW_IRON,
                net.minecraft.world.item.Items.RAW_GOLD,net.minecraft.world.item.Items.RAW_COPPER,net.minecraft.world.item.Items.NETHERITE_INGOT).contains(item);
    }
    private static boolean ordinaryStone(BlockState state) { return state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) || state.is(Blocks.DEEPSLATE); }
    private static boolean permitted(ServerLevel level,BlockPos pos,LivingEntity caster) {
        if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos) || level.isOutsideBuildHeight(pos)) return false;
        if (caster instanceof Player player) return player.mayBuild() && level.mayInteract(player,pos)
                && !net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level,pos,level.getBlockState(pos),player)).isCanceled();
        return true;
    }
    private static boolean safe(LivingEntity entity,ServerLevel level,Vec3 destination) {
        BlockPos pos=BlockPos.containing(destination); AABB box=entity.getBoundingBox().move(destination.subtract(entity.position()));
        return level.hasChunkAt(pos) && !level.isOutsideBuildHeight(pos) && level.getWorldBorder().isWithinBounds(box)
                && level.noCollision(entity,box) && level.getFluidState(pos).isEmpty() && !level.getBlockState(pos.below()).getCollisionShape(level,pos.below()).isEmpty();
    }
    private static List<LivingEntity> nearby(Vec3 center,double radius,ServerLevel level) {
        return level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius),e -> e.isAlive() && !(e instanceof SpellAnchor) && e.position().distanceToSqr(center)<=radius*radius);
    }
    private static double bounded(Map<String,Double> values,String key,double fallback,double min,double max) {
        double value=values.getOrDefault(key,fallback); if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite lease value"); return Math.max(min,Math.min(max,value));
    }
}
