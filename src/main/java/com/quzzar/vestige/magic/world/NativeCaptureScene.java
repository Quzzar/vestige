package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.effect.SpellCapabilities;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.presentation.SpellVisualPayload;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Fixtures for the opt-in capture client. Called only on its isolated integrated server. */
public final class NativeCaptureScene {
    public record Job(String spell, int phase, String label, String kind) {
        public String file() { return spell.replace(':','-')+"-"+kind+"-"+phase; }
    }
    private LivingEntity caster, target;
    private SpellConstruct geometry;
    private SpellRuntime.Cast cast;
    private Job job;
    private final List<net.minecraft.world.entity.LivingEntity> subjects=new ArrayList<>();
    private final Map<UUID,Float> startingHealth=new HashMap<>();
    private final Map<UUID,String> roles=new HashMap<>();
    private final Map<UUID,Vec3> startingPositions=new HashMap<>();
    private final Map<UUID,ResourceLocation> startingDimensions=new HashMap<>();
    private MinecraftServer captureServer;
    private String scenario;
    private String formation;
    private BlockPos formationBase;
    private boolean summonScene;
    private int chainCount;
    private double sceneRadius;
    private final Set<BlockPos> fixtureBlocks=new HashSet<>();
    private volatile int elapsed;
    public int elapsedTicks() { return elapsed; }
    private float pitch;
    private boolean defensive;
    private boolean repeatedRecast;
    private float yaw;
    private boolean playerView, blockScene, outgoingAttacks, spatialRecast, summonCohort;
    private boolean fireDamage, waterWalking, climbing, airGuard, sharedGuard;
    private volatile boolean showHud;
    private volatile boolean eating;
    private volatile boolean thirdPerson;
    private int recasts;
    private final Set<String> actions=new HashSet<>();
    private final List<SpellEffects.Manifestation> manifestations=new ArrayList<>();
    private final Set<String> observations=new LinkedHashSet<>();
    private final Map<BlockPos,String> initialBlocks=new LinkedHashMap<>();
    public boolean showHud() { return showHud; }
    /** The client must hold the normal use input; server-only use is canceled by the client. */
    public boolean eating() { return eating; }
    public boolean thirdPerson() { return thirdPerson; }

    public static List<Job> jobs(String filter, String kind) {
        if (!kind.equals("replay") && !kind.equals("cast")) throw new IllegalArgumentException("Capture kind must be replay or cast");
        Set<String> wanted=new HashSet<>();
        for (String id:filter.split(",")) if (!id.isBlank()) wanted.add(id.contains(":")?id:"vestige:"+id);
        List<Job> jobs=new ArrayList<>();
        NativeMagic.spells().spells().values().stream().sorted(Comparator.comparing(s->s.id().toString())).forEach(spell->{
            if (!wanted.isEmpty() && !wanted.contains(spell.id().toString())) return;
            if (kind.equals("cast")) jobs.add(new Job(spell.id().toString(),0,"Primary cast",kind));
            else {
                var phases=SpellEffectGallery.phases(spell);
                for(int i=0;i<phases.size();i++) jobs.add(new Job(spell.id().toString(),i,phases.get(i).label(),kind));
            }
        });
        if (jobs.isEmpty()) throw new IllegalArgumentException("No capture jobs match "+filter);
        if (!wanted.isEmpty() && jobs.stream().map(Job::spell).distinct().count()!=wanted.size()) throw new IllegalArgumentException("Unknown capture spell in "+filter);
        return List.copyOf(jobs);
    }
    public void build(MinecraftServer server) {
        var level=server.overworld();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
        level.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false,server);
        level.getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false,server);
        level.setDayTime(6000); level.setWeatherParameters(0,0,false,false);
        for(int x=-24;x<=24;x++) for(int z=-24;z<=24;z++) {
            level.setBlock(new BlockPos(x,64,z),(x%4==0 || z%4==0?Blocks.SMOOTH_STONE:Blocks.WHITE_CONCRETE).defaultBlockState(),3);
        }
        for(ServerPlayer player:level.players()) player.setGameMode(GameType.SPECTATOR);
    }
    public void prepare(MinecraftServer server, Job next) {
        captureServer=server;
        NativeMagic.reload();
        var level=server.overworld(); var session=NativeMagic.session(server);
        for(var dimension:server.getAllLevels())for(Entity entity:List.copyOf(com.google.common.collect.Lists.newArrayList(dimension.getAllEntities()))) if (!(entity instanceof ServerPlayer)) entity.discard();
        for(var player:server.getPlayerList().getPlayers()) {
            player.closeContainer();player.removeAllEffects();player.getInventory().clearContent();player.setHealth(20);player.getFoodData().setFoodLevel(20);
            player.teleportTo(level,1.8,66.8,10,Set.of(),180,10);player.setGameMode(GameType.SPECTATOR);
        }
        for(BlockPos pos:fixtureBlocks) level.setBlock(pos,pos.getY()==64?(pos.getX()%4==0 || pos.getZ()%4==0?Blocks.SMOOTH_STONE:Blocks.WHITE_CONCRETE).defaultBlockState():Blocks.AIR.defaultBlockState(),3);
        fixtureBlocks.clear();
        roles.clear();startingPositions.clear();startingDimensions.clear();scenario="Caster aims at a villager";formation="";formationBase=null;summonScene=false;chainCount=0;sceneRadius=0;
        job=next; cast=null; geometry=null;elapsed=0;subjects.clear();startingHealth.clear();defensive=false;repeatedRecast=false;eating=false;thirdPerson=false;
        actions.clear();manifestations.clear();observations.clear();initialBlocks.clear();recasts=0;blockScene=false;outgoingAttacks=false;spatialRecast=false;summonCohort=false;playerView=false;showHud=false;yaw=-90;
        fireDamage=false;waterWalking=false;climbing=false;airGuard=false;sharedGuard=false;
        caster=villager(server,-3,65,0); target=villager(server,3,65,0);
        caster.setYRot(-90); caster.setYHeadRot(-90); caster.setXRot(0);
        session.world().registerActor(caster); session.world().registerActor(target);
        SpellDefinition spell=NativeMagic.spells().spells().get(ResourceLocation.parse(job.spell()));
        collectPlan(spell.effects(),actions,manifestations);
        playerView=actions.stream().anyMatch(Set.of("flight","recall","ender_inventory","pocket_dimension","detect_magic","inspect_item","food_mana")::contains)
                || manifestations.stream().anyMatch(m->m.kind().getPath().equals("sensor"));
        thirdPerson=actions.contains("flight");
        if(playerView && job.kind().equals("cast")) {
            caster.discard();subjects.remove(caster);caster=server.getPlayerList().getPlayers().getFirst();
            var player=(ServerPlayer)caster;player.setGameMode(GameType.SURVIVAL);player.teleportTo(level,-3,65,0,Set.of(),-90,0);
            player.setRespawnPosition(level.dimension(),new BlockPos(-7,65,0),0,true,false);
            player.getPersistentData().putDouble("vestige:mana",0);subjects.add(caster);session.world().registerActor(caster);showHud=true;
        }
        if(job.kind().equals("cast")) {
            caster.setCustomName(net.minecraft.network.chat.Component.literal("Caster"));caster.setCustomNameVisible(true);
            target.setCustomName(net.minecraft.network.chat.Component.literal("Target"));target.setCustomNameVisible(true);
            // A recording exercises the primary plan; optional modes must not select its fixtures.
            boolean heals=actions.contains("heal") || actions.contains("dwell_heal") || manifestations.stream().anyMatch(m->m.kind().getPath().equals("construct") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("heal"));
            if(heals) { caster.setHealth(8);target.setHealth(8); }
            defensive=actions.contains("shield") || actions.contains("reduce_pending_damage") || actions.contains("defer_pending_damage")
                    || manifestations.stream().anyMatch(m->m.kind().getPath().equals("guard") || m.kind().getPath().equals("construct") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("protect"))
                    || manifestations.stream().anyMatch(m->m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("absence"));
            repeatedRecast=spell.effects().stream().anyMatch(e->e instanceof SpellEffects.Repeat repeat
                    && repeat.effects().stream().anyMatch(child->child instanceof SpellEffects.AwaitRecast));
            if(defensive) {
                target.teleportTo(-3,65,2.5);
                caster.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
            }
            boolean chain=SpellEffectGallery.phases(spell).stream().anyMatch(p->p.visual().layers().stream().anyMatch(l->l.shape()==com.quzzar.vestige.magic.presentation.SpellVisual.Shape.ARC));
            var plans=new ArrayList<SpellEffects.ForEach>();collectSelections(spell.effects(),plans);
            chainCount=plans.stream().filter(e->e.target().selection()==TargetSpec.Selection.CHAIN).mapToInt(e->(int)e.target().options().getOrDefault("count",new com.quzzar.vestige.magic.expression.SpellValue.Constant(2)).resolve(spell.traits())).max().orElse(chain?2:0);
            if(chainCount>0) {
                for(int i=1;i<chainCount;i++) {
                    var nextTarget=villager(server,3+(i%2)*2.0,65,(i/2)*2.0+(i%2)*1.2);
                    label(nextTarget,"Hop "+(i+1));session.world().registerActor(nextTarget);
                }
                var beyond=villager(server,10,65,-3);label(beyond,"Beyond jump range");session.world().registerActor(beyond);
                scenario="Ordered lightning chain: "+chainCount+" targets; a distant villager stays outside jump range";
            }
            sceneRadius=plans.stream().filter(e->e.target().selection()==TargetSpec.Selection.NEAR_TARGET && e.effects().stream().anyMatch(child->child instanceof SpellEffects.Action action && Set.of("damage","typed_damage").contains(action.type().getPath())))
                    .mapToDouble(e->e.target().distance().resolve(spell.traits())).max().orElse(0);
            if(sceneRadius>0 && chainCount==0) {
                double r=Math.min(5,sceneRadius);
                label(target,"Center");
                label(villager(server,3,65,-r*.55),"Inside A");
                label(villager(server,3+r*.55,65,r*.4),"Inside B");
                label(villager(server,3,65,r+1.4),"Outside A");
                label(villager(server,3+r+1.4,65,-1.5),"Outside B");
                for(int x=-3;x<=10;x++) for(int z=-7;z<=7;z++) {
                    double d=Math.hypot(x+.5-3,z+.5);
                    if(Math.abs(d-r)<.4) { var pos=new BlockPos(x,64,z);level.setBlock(pos,Blocks.CYAN_CONCRETE.defaultBlockState(),3);fixtureBlocks.add(pos); }
                }
                scenario="Area encounter: villagers inside and outside the "+r+"-block footprint; cyan floor marks the test boundary";
            }
            formation=findFormation(spell.effects());
            summonScene=job.spell().equals("vestige:pf2_summon_animal");
            if(summonScene) {
                target.discard();subjects.remove(target);
                var enemy=EntityType.ZOMBIE.create(level);enemy.setPos(0,65,0);enemy.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET));
                enemy.setNoAi(true);level.addFreshEntity(enemy);subjects.add(enemy);label(enemy,"Hostile zombie");
                scenario="Owned wolf engages a hostile zombie, follows its caster, then is dismissed by recast";
            }
            if(defensive && formation.isEmpty()) scenario="Caster receives four real melee hits; finite ward charges are consumed";
            prepareEligibleFixtures(server,spell,plans);
            label(caster,"Caster");if(!roles.containsKey(target.getUUID()))label(target,"Target");
            for(var mob:subjects) { startingHealth.put(mob.getUUID(),mob.getHealth());startingPositions.put(mob.getUUID(),mob.position());startingDimensions.put(mob.getUUID(),mob.level().dimension().location());session.world().registerActor(mob); }
        }
        double radius=2;
        if (job.kind().equals("replay")) {
            var phase=SpellEffectGallery.phases(spell).get(job.phase()); radius=phase.visual().radius().resolve(spell.traits());
            if (phase.geometry().containsKey("width")) {
                geometry=SpellEntities.CONSTRUCT.get().create(level);
                geometry.geometry(resolve(phase,spell,"width",1),resolve(phase,spell,"height",2),resolve(phase,spell,"depth",1),false);
                geometry.setPos(0,65,0); level.addFreshEntity(geometry);
            }
        }
        double distance=Math.max(1,Math.min(3,radius/6));
        boolean pushes=SpellCapabilities.of(spell).contains(VestigeMainMod.location("knockback"));
        Vec3 camera=job.kind().equals("cast")?new Vec3(1.8,68.4,10):new Vec3(6*distance,68+3*(distance-1),8*distance);
        Vec3 focus=new Vec3(0,66.3,0);
        if(job.kind().equals("cast")) {
            if(chainCount>2 || sceneRadius>0) { camera=new Vec3(3,69.3,16);focus=new Vec3(1,66.7,0); }
            if(pushes) { camera=new Vec3(5,69.3,16);focus=new Vec3(5,66.7,0); }
            if(defensive) focus=new Vec3(-2,66.5,1);
            if(!formation.isEmpty())focus=new Vec3(-.5,67,0);
            if(summonScene) { camera=new Vec3(0,68.4,10);focus=new Vec3(-2,66.3,0); }
            if(airGuard) {camera=new Vec3(-3,71,5);focus=new Vec3(-3,65,0);}
        }
        Vec3 delta=focus.subtract(camera);
        float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));
        float pitch=(float)Math.toDegrees(Math.atan2(-delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z)));
        if(!playerView)for(ServerPlayer player:level.players()) player.teleportTo(level,camera.x,camera.y-player.getEyeHeight(),camera.z,Set.of(),yaw,pitch);
    }
    private double resolve(SpellEffectGallery.Phase phase,SpellDefinition spell,String key,double fallback) {
        return phase.geometry().containsKey(key)?phase.geometry().get(key).resolve(spell.traits()):fallback;
    }
    public void play(MinecraftServer server) {
        var level=server.overworld(); var session=NativeMagic.session(server);
        SpellDefinition spell=NativeMagic.spells().spells().get(ResourceLocation.parse(job.spell()));
        if (job.kind().equals("cast")) {
            caster.setYRot(yaw);caster.setYHeadRot(yaw);
            // Ground-aimed plans use the normal actor ray, with a grounded test target.
            boolean ground=spell.effects().stream().anyMatch(effect->effect instanceof SpellEffects.ForEach each
                    && each.target().selection()==TargetSpec.Selection.AIMED_POSITION);
            boolean healingPlant=manifestations.stream().anyMatch(m->m.kind().getPath().equals("construct") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("heal"));
            pitch=ground?(formation.equals("tree") || healingPlant?40:defensive?30:15.1f):6;caster.setXRot(pitch);
            if(blockScene)aim(new Vec3(2.5,65.5,0.5));
            if(spatialRecast && !blockScene)aim(new Vec3(1,65,-2));
            boolean blockWall=spell.effects().stream().anyMatch(effect->effect instanceof SpellEffects.ForEach each
                    && each.effects().stream().anyMatch(child->child instanceof SpellEffects.CreateManifestation manifestation
                    && manifestation.manifestation().kind().getPath().equals("block_wall")));
            if(blockWall || formation.equals("water") || formation.equals("tree")) {
                pitch=formation.equals("tree")?40:30;caster.setXRot(pitch);
                var eye=caster.getEyePosition();var end=eye.add(caster.getLookAngle().scale(18));
                var hit=level.clip(new net.minecraft.world.level.ClipContext(eye,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,caster));
                var base=BlockPos.containing(hit.getLocation().add(0,.01,0));formationBase=base;
                if(!formation.equals("tree")) target.teleportTo(base.getX()+.5,base.getY(),base.getZ()+.5);
                scenario=formation.equals("tree")?"Oak grows and absorbs incoming hits; a leaf is replaced with diamond, which survives retraction":formation.equals("water")?"Water grows around a villager, extinguishes it and drains; a diamond replacement survives":"Ice rises, lifts a villager and retracts its surviving blocks";
                if(formation.equals("water"))target.igniteForSeconds(3);
                for(var mob:subjects) startingPositions.put(mob.getUUID(),mob.position());
            }
            cast=session.runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),
                    new SpellSubject.Entity(target.getUUID())),List.of(),true,Optional.empty(),true);
        } else {
            var phase=SpellEffectGallery.phases(spell).get(job.phase());
            var start=new SpellVisualPayload.Point(caster.getEyePosition(),-1,new UUID(0,0),0);
            var end=geometry==null?new SpellVisualPayload.Point(new Vec3(0,65+phase.visual().height(),0),-1,new UUID(0,0),0)
                    :new SpellVisualPayload.Point(geometry.position().add(0,phase.visual().height(),0),geometry.getId(),geometry.getUUID(),(float)phase.visual().height());
            session.world().visuals.start(level,phase.visual(),phase.visual().radius().resolve(spell.traits()),geometry!=null,()->List.of(start,end),()->true);
        }
    }
    public String outcome() { return cast==null?"cosmetic replay":cast.status().name(); }
    public double minimumSeconds(Job next,double requested) {
        if(!next.kind().equals("cast")) return requested;
        var spell=NativeMagic.spells().spells().get(ResourceLocation.parse(next.spell()));
        boolean wall=spell.effects().stream().anyMatch(e->e instanceof SpellEffects.ForEach each
                && each.effects().stream().anyMatch(child->child instanceof SpellEffects.CreateManifestation m
                && m.manifestation().kind().getPath().equals("block_wall")));
        Set<String> verbs=new HashSet<>();var forms=new ArrayList<SpellEffects.Manifestation>();collectPlan(spell.effects(),verbs,forms);
        return wall || !findFormation(spell.effects()).isEmpty() || forms.stream().anyMatch(m->Set.of("passage","pet_cache").contains(m.kind().getPath()))?Math.max(16,requested):jobSpellSummon(next)?Math.max(14,requested):requested;
    }
    /** Capture fixtures use real attacks to demonstrate wards; they never manufacture spell outcomes. */
    public void tick(MinecraftServer server) {
        if(cast==null || caster==null) return;elapsed++;
        caster.setYRot(yaw);caster.setYHeadRot(yaw);caster.setYBodyRot(yaw);caster.setXRot(pitch);
        if(repeatedRecast && elapsed%30==0 && cast.status()==SpellRuntime.Status.AWAITING_RECAST) {
            var spell=NativeMagic.spells().spells().get(ResourceLocation.parse(job.spell()));
            NativeMagic.session(server).runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),
                    new SpellSubject.Entity(target.getUUID())),List.of(),true,Optional.empty(),true);
        }
        if(defensive && (elapsed==50 || elapsed==80 || formation.isEmpty() && (elapsed==110 || elapsed==140)) && target.isAlive() && caster.isAlive()) {
            caster.invulnerableTime=0;caster.hurt(fireDamage?caster.damageSources().onFire():caster.damageSources().mobAttack(target),4);
        }
        if(formationBase!=null && elapsed==90 && !formation.isEmpty()) {
            BlockPos edited=formation.equals("tree")?formationBase.offset(1,3,0):formationBase.above();
            server.overworld().setBlock(edited,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);fixtureBlocks.add(edited);
        }
        if(summonScene && elapsed==30) {
            for(var wolf:server.overworld().getEntitiesOfClass(net.minecraft.world.entity.animal.Wolf.class,caster.getBoundingBox().inflate(12))) {
                if(!NativeMagic.session(server).world().isOwnedBy(wolf,caster.getUUID()))continue;
                subjects.add(wolf);label(wolf,"Summoned wolf");startingHealth.put(wolf.getUUID(),wolf.getHealth());startingPositions.put(wolf.getUUID(),wolf.position());
            }
        }
        if(summonScene && elapsed==160)caster.teleportTo(-6,65,-3);
        if(summonScene && elapsed==230) {
            var spell=NativeMagic.spells().spells().get(ResourceLocation.parse(job.spell()));
            NativeMagic.session(server).runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(caster.getUUID())),List.of(),true,Optional.empty(),true);
        }
        tickFixtures(server);
    }
    private static boolean jobSpellSummon(Job job) { return job.spell().equals("vestige:pf2_summon_animal"); }
    /** Supplies eligible ordinary world inputs without changing the definition or manufacturing its outcomes. */
    private void prepareEligibleFixtures(MinecraftServer server,SpellDefinition spell,List<SpellEffects.ForEach> plans) {
        var level=server.overworld();var world=NativeMagic.session(server).world();
        fireDamage=manifestations.stream().flatMap(m->m.bindings().stream()).flatMap(b->b.triggers().stream()).flatMap(t->t.conditions().stream())
                .anyMatch(c->c instanceof com.quzzar.vestige.magic.condition.BuiltInCondition.Compare compare && compare.path().getPath().equals("event/fire") && compare.expected().equals(new com.quzzar.vestige.magic.condition.ConditionValue.Flag(true)));
        waterWalking=manifestations.stream().anyMatch(m->m.kind().getPath().equals("mobility") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("water_walk"));
        climbing=manifestations.stream().anyMatch(m->m.kind().getPath().equals("mobility") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("climb"));
        airGuard=manifestations.stream().anyMatch(m->m.kind().getPath().equals("guard") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("air"));
        sharedGuard=manifestations.stream().anyMatch(m->m.kind().getPath().equals("guard") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("share"));
        if(sharedGuard) {defensive=false;target.teleportTo(3,65,0);}
        if(plans.stream().anyMatch(p->p.target().selection()==TargetSpec.Selection.MELEE || p.target().selection()==TargetSpec.Selection.CONE)) {
            target.teleportTo(-.7,65,0);scenario="Close-range encounter with a hostile villager";
        }
        double shortRay=plans.stream().filter(p->Set.of(TargetSpec.Selection.ENTITY_RAY,TargetSpec.Selection.ANY_ENTITY_RAY).contains(p.target().selection()))
                .mapToDouble(p->p.target().distance().resolve(spell.traits())).filter(d->d>0 && d<6).min().orElse(6);
        if(shortRay<6 && !defensive)target.teleportTo(-3+shortRay*.7,65,0);
        if(plans.stream().anyMatch(p->p.target().selection()==TargetSpec.Selection.NEARBY_ENTITIES && p.target().relationship()==TargetSpec.Relationship.HOSTILE)) {
            double range=plans.stream().filter(p->p.target().selection()==TargetSpec.Selection.NEARBY_ENTITIES).mapToDouble(p->p.target().distance().resolve(spell.traits())).max().orElse(4);
            target.teleportTo(-3+Math.min(3,range*.5),65,0);label(target,"Inside radius");
            label(villager(server,-3,65,Math.min(3,range*.5)),"Inside radius B");
            label(villager(server,-3-range-2,65,0),"Outside radius");scenario="Radial cast with villagers inside and outside its reach";
        }
        if(plans.stream().anyMatch(p->p.target().relationship()==TargetSpec.Relationship.ALLY) || actions.contains("transpose") || manifestations.stream().anyMatch(m->m.kind().getPath().equals("sensor") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("status"))) {
            ally(server,target);scenario="Consenting allied villagers receive native support";
            if(actions.contains("transpose")) {
                target.teleportTo(-2,65,2);var other=villager(server,-4,65,2);label(other,"Ally B");ally(server,other);
                fixtureBlock(server,new BlockPos(3,64,0),Blocks.CYAN_CONCRETE);scenario="Three consenting allies relocate together to clear marked ground";
            }
        }
        if(actions.contains("cleanse") || actions.contains("remove_status")) {
            caster.addEffect(new MobEffectInstance(MobEffects.POISON,300));target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,300));
            observations.add("Fixture begins with removable negative conditions.");
        }
        if(actions.contains("weapon_damage") || actions.contains("inspect_item") || actions.contains("throw"))caster.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
        if(spell.id().getPath().equals("throw"))caster.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
        if(needsUndead(spell.effects())) {
            replaceTarget(server,EntityType.SKELETON);scenario="Cast at an undead skeleton, matching the spell’s eligibility";
        }
        if(actions.contains("aggro_convert")) {replaceTarget(server,EntityType.SHEEP);scenario="Native conversion of an eligible sheep";}
        if(manifestations.stream().anyMatch(m->m.kind().getPath().equals("pet_cache"))) {
            replaceTarget(server,EntityType.WOLF);var pet=(Wolf)target;pet.setTame(true,true);pet.setOwnerUUID(caster.getUUID());pet.setNoAi(true);pet.setHealth(9);
            scenario="An owned wolf enters shelter and returns with its identity and health preserved";
        }
        if(plans.stream().anyMatch(p->p.target().relationship()==TargetSpec.Relationship.OWNED)) {
            world.owners.put(target.getUUID(),caster.getUUID());label(target,"Owned creature");scenario="An owned creature supplies the spell’s required target";
        }
        blockScene=plans.stream().anyMatch(p->p.target().selection()==TargetSpec.Selection.BLOCK_RAY);
        if(blockScene) {
            target.teleportTo(3,65,3);
            if(actions.contains("create_water")) {fixtureBlock(server,new BlockPos(2,65,0),Blocks.CAULDRON);scenario="Water fills an initially empty cauldron";}
            else if(manifestations.stream().anyMatch(m->m.kind().getPath().equals("block_lock"))) {fixtureBlock(server,new BlockPos(2,65,0),Blocks.CHEST);scenario="A real chest is locked, then unlocked by a second cast";}
            else if(manifestations.stream().anyMatch(m->m.kind().getPath().equals("passage"))) {
                for(int x=2;x<=4;x++)for(int y=65;y<=66;y++)for(int z=-1;z<=1;z++)fixtureBlock(server,new BlockPos(x,y,z),Blocks.STONE);
                scenario="A passage opens through real stone, then restores it safely";
            } else if(actions.contains("shape_stone"))fixtureBlock(server,new BlockPos(2,65,0),Blocks.STONE);
            else {for(int y=65;y<=67;y++)for(int z=-1;z<=1;z++)fixtureBlock(server,new BlockPos(2,y,z),Blocks.STONE);scenario="Native terrain utility applied to an ordinary stone face";}
            if(actions.contains("shape_stone")) {fixtureBlock(server,new BlockPos(4,65,2),Blocks.AIR);spatialRecast=true;scenario="Two real cast inputs move one stone block to clear ground";}
        }
        if(manifestations.stream().anyMatch(m->m.kind().getPath().equals("construct") && m.values().getOrDefault("solid",new com.quzzar.vestige.magic.expression.SpellValue.Constant(0)).resolve(spell.traits())>0) && formation.isEmpty()) {
            target.teleportTo(-3,65,3);scenario="Solid native construct appears on clear ground";
        }
        if(actions.contains("teleport") && !actions.contains("weapon_damage") && formation.isEmpty())target.teleportTo(3,65,3);
        if(manifestations.stream().anyMatch(m->Set.of("wall","portal").contains(m.kind().getPath()))) {
            spatialRecast=true;scenario="Two aimed cast inputs join separate ground anchors";
        }
        outgoingAttacks=manifestations.stream().flatMap(m->m.bindings().stream()).flatMap(b->b.triggers().stream()).anyMatch(t->t.event().getPath().equals("damage_dealt") || t.event().getPath().equals("attack"));
        if(outgoingAttacks && !defensive)target.teleportTo(-.7,65,0);
        summonCohort=manifestations.stream().anyMatch(m->m.kind().getPath().equals("summon")) && !summonScene;
        if(summonCohort) {
            replaceTarget(server,EntityType.ZOMBIE);target.teleportTo(1,65,0);scenario="Owned native summons acquire a hostile and are dismissed by recast or dispel";
        }
        if(actions.contains("reduce_pending_heal")) {target.setHealth(8);scenario="Healing attempts against the blighted target are intercepted";}
        if(actions.contains("gather_items")) {
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level,1,65,0,new ItemStack(Items.IRON_INGOT,7)));
            level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level,1,65,3,new ItemStack(Items.DIAMOND,2)));
            scenario="Loose iron is attracted while nearby diamond remains unaffected";
        }
        if(actions.contains("dispel") || actions.contains("interrupt")) {
            var ward=NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_shield"));
            NativeMagic.session(server).runtime().cast(ward,SpellEvent.of(SpellTriggerTypes.INTERACT,target.getUUID(),new SpellSubject.Entity(target.getUUID())),List.of(),true,Optional.empty(),true);
            scenario="A target with an actual native ward receives the dispelling cast";
        }
        if(sharedGuard)scenario="A consenting ally takes real hits, sharing the spell’s bounded damage with the caster";
        if(fireDamage)scenario=defensive?"Real fire damage tests the spell’s authored fire protection":"An injured caster regenerates until real fire damage ends the spell";
        if(waterWalking) {
            for(int x=-7;x<=6;x++)for(int z=-3;z<=3;z++)fixtureBlock(server,new BlockPos(x,63,z),Blocks.STONE);
            for(int x=-5;x<=0;x++)for(int z=-1;z<=1;z++) {fixtureBlock(server,new BlockPos(x,63,z),Blocks.STONE);fixtureBlock(server,new BlockPos(x,64,z),Blocks.WATER);}
            for(int x=-6;x<=1;x++)for(int z=-2;z<=2;z++)if(x==-6||x==1||Math.abs(z)==2)fixtureBlock(server,new BlockPos(x,64,z),Blocks.STONE);
            caster.teleportTo(-6.5,65,0);scenario="Caster steps from dry ground across a real water surface";
        }
        if(climbing) {
            for(int y=65;y<=70;y++)for(int z=-1;z<=1;z++)fixtureBlock(server,new BlockPos(-2,y,z),Blocks.STONE);
            target.teleportTo(3,65,3);scenario="Caster moves into a real stone wall and climbs using native contact movement";
        }
        if(airGuard) {
            defensive=false;
            target.teleportTo(3,65,3);
            for(int x=-5;x<=-1;x++)for(int z=-2;z<=2;z++) {
                fixtureBlock(server,new BlockPos(x,63,z),Blocks.STONE);
                for(int y=64;y<=67;y++)fixtureBlock(server,new BlockPos(x,y,z),x==-5||x==-1||Math.abs(z)==2?Blocks.GLASS:y<67?Blocks.WATER:Blocks.AIR);
            }
            caster.teleportTo(-3,64,0);caster.setAirSupply(10);if(caster instanceof Mob mob)mob.setNoAi(true);
            scenario="A submerged caster receives the real air bubble and its replenished breath";
        }
        if(manifestations.stream().anyMatch(m->m.kind().getPath().equals("construct") && m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("heal"))) {
            ally(server,target);target.teleportTo(-3,65,2);scenario="An injured caster and consenting ally recover near their native healing plant";
        }
        if(playerView) {
            if(actions.contains("food_mana")) {caster.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BREAD,3));((ServerPlayer)caster).getFoodData().setFoodLevel(10);scenario="Player eats bread under Gluttony and receives native mana";}
            else if(actions.contains("ender_inventory")) {((ServerPlayer)caster).getEnderChestInventory().setItem(0,new ItemStack(Items.DIAMOND,3));scenario="Player opens their actual Ender Chest inventory";}
            else if(actions.contains("pocket_dimension"))scenario="Player enters their real private room";
            else if(actions.contains("recall"))scenario="Player returns to their configured respawn point";
            else if(actions.contains("flight"))scenario="Player receives native flight permission and rises";
            else if(actions.contains("detect_magic")) {
                var ward=NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_shield"));
                NativeMagic.session(server).runtime().cast(ward,SpellEvent.of(SpellTriggerTypes.INTERACT,target.getUUID(),new SpellSubject.Entity(target.getUUID())),List.of(),true,Optional.empty(),true);
                scenario="Player detects an actual nearby native ward";
            } else if(actions.contains("inspect_item"))scenario="Player reads the aura of the held iron sword";
            else {
                scenario="Player view of the spell’s native private information or perception";
                if(manifestations.stream().anyMatch(m->m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("camera"))) {
                    target.teleportTo(6,65,0);scenario="Player casts a remote sensor into clear space and sees a villager from its actual viewpoint";
                }
                if(manifestations.stream().anyMatch(m->m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath().equals("unseen")))target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,300,0,false,false));
            }
        }
    }
    private void fixtureBlock(MinecraftServer server,BlockPos pos,net.minecraft.world.level.block.Block block) {
        server.overworld().setBlock(pos,block.defaultBlockState(),3);fixtureBlocks.add(pos);
        initialBlocks.put(pos,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString());
    }
    private void ally(MinecraftServer server,LivingEntity ally) {
        var board=server.getScoreboard();var team=board.getPlayerTeam("capture_allies");if(team==null)team=board.addPlayerTeam("capture_allies");
        board.addPlayerToTeam(caster.getScoreboardName(),team);board.addPlayerToTeam(ally.getScoreboardName(),team);
    }
    private void replaceTarget(MinecraftServer server,EntityType<? extends Mob> type) {
        Vec3 position=target.position();target.discard();subjects.remove(target);roles.remove(target.getUUID());
        var mob=type.create(server.overworld());mob.setPos(position);mob.setNoAi(true);mob.setPersistenceRequired();
        var helmet=new ItemStack(Items.LEATHER_HELMET);helmet.set(net.minecraft.core.component.DataComponents.UNBREAKABLE,new net.minecraft.world.item.component.Unbreakable(true));mob.setItemSlot(EquipmentSlot.HEAD,helmet);
        server.overworld().addFreshEntity(mob);subjects.add(mob);target=mob;label(target,"Target");
    }
    private void aim(Vec3 point) {
        Vec3 direction=point.subtract(caster.getEyePosition());yaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));
        pitch=(float)Math.toDegrees(Math.atan2(-direction.y,Math.hypot(direction.x,direction.z)));
        caster.setYRot(yaw);caster.setYHeadRot(yaw);caster.setXRot(pitch);
    }
    private void tickFixtures(MinecraftServer server) {
        var runtime=NativeMagic.session(server).runtime();var spell=NativeMagic.spells().spells().get(ResourceLocation.parse(job.spell()));
        if(spatialRecast && cast.status()==SpellRuntime.Status.AWAITING_RECAST && recasts==0 && elapsed>=25) {
            aim(actions.contains("shape_stone")?new Vec3(4.5,65,2.5):new Vec3(5,65,2));
            runtime.cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(target.getUUID())),List.of(),true,Optional.empty(),true);recasts++;
        }
        if((outgoingAttacks || defensive) && elapsed%30==0 && elapsed<=120 && caster.isAlive() && target.isAlive() && !defensive) {
            target.invulnerableTime=0;target.hurt(caster.damageSources().mobAttack(caster),2);
        }
        if(blockScene && elapsed==100 && manifestations.stream().anyMatch(m->m.kind().getPath().equals("block_lock")))runtime.cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(target.getUUID())),List.of(),true,Optional.empty(),true);
        if(summonCohort && elapsed==140) {
            if(cast.status()==SpellRuntime.Status.AWAITING_RECAST)runtime.cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(caster.getUUID())),List.of(),true,Optional.empty(),true);
            else runtime.dispelActor(caster.getUUID());
        }
        if(actions.contains("food_mana"))eating=elapsed>=40 && caster.getPersistentData().getDouble("vestige:mana")==0 && elapsed<150;
        if(actions.contains("reduce_pending_heal") && (elapsed==50 || elapsed==80))target.heal(4);
        if(sharedGuard && (elapsed==50 || elapsed==80 || elapsed==110 || elapsed==140)) {target.invulnerableTime=0;target.hurt(caster.damageSources().mobAttack(caster),4);}
        if(actions.contains("flight") && caster instanceof ServerPlayer player && elapsed==40) {player.getAbilities().flying=true;player.onUpdateAbilities();player.setDeltaMovement(0,.3,0);player.hurtMarked=true;}
        if((waterWalking || climbing) && elapsed>=30 && elapsed<=140) {
            // End the scripted water crossing on the landing rather than keep
            // forcing the actor forward after its native movement lease ends.
            double stride=waterWalking && caster.getX()>=1.5?0:.12;
            caster.setDeltaMovement(stride,caster.getDeltaMovement().y,0);caster.hurtMarked=true;
        }
        if(fireDamage && !defensive && elapsed==100) {caster.invulnerableTime=0;caster.hurt(caster.damageSources().onFire(),2);}
        if(elapsed%10==0) {
            for(var subject:subjects)for(var effect:subject.getActiveEffects())observations.add(roles.getOrDefault(subject.getUUID(),"Subject")+": "+net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).getPath().replace('_',' ')+" "+(effect.getAmplifier()+1));
            for(var entry:initialBlocks.entrySet()) {
                String current=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(server.overworld().getBlockState(entry.getKey()).getBlock()).toString();
                if(!current.equals(entry.getValue()))observations.add(entry.getValue()+" → "+current);
            }
            if(actions.contains("food_mana"))observations.add("Mana: "+caster.getPersistentData().getDouble("vestige:mana"));
            if(caster.getScale()!=1)observations.add("Caster scale: "+caster.getScale());
            if(blockScene && manifestations.stream().anyMatch(m->m.kind().getPath().equals("block_lock"))) {
                var chest=server.overworld().getBlockEntity(new BlockPos(2,65,0));
                if(chest!=null)observations.add("Chest lock: "+(chest.getPersistentData().contains("vestige:lock_owner")?"locked":"unlocked"));
            }
            if(playerView)observations.add("Player dimension: "+caster.level().dimension().location());
            if(airGuard)observations.add("Submerged caster breath: "+caster.getAirSupply());
            if(summonCohort)for(var living:server.overworld().getEntitiesOfClass(LivingEntity.class,caster.getBoundingBox().inflate(24)))if(NativeMagic.session(server).world().isOwnedBy(living,caster.getUUID()) && !subjects.contains(living)) {
                subjects.add(living);label(living,"Summoned "+living.getType().toShortString());startingHealth.put(living.getUUID(),living.getHealth());startingPositions.put(living.getUUID(),living.position());
            }
        }
    }
    private static boolean needsUndead(List<SpellEffect> effects) {
        for(var effect:effects) {
            if(effect instanceof SpellEffects.Branch b && b.whenFalse().isEmpty() && b.condition() instanceof com.quzzar.vestige.magic.condition.BuiltInCondition.Tagged t && t.tag().equals(ResourceLocation.parse("minecraft:undead")))return true;
            if(effect instanceof SpellEffects.ForEach each && needsUndead(each.effects()))return true;
        }return false;
    }
    /** Walks executable composition, including callback and reactive behavior, for fixture eligibility. */
    private static void collectPlan(List<SpellEffect> effects,Set<String> actions,List<SpellEffects.Manifestation> manifestations) {
        for(var effect:effects) {
            if(effect instanceof SpellEffects.Action a)actions.add(a.type().getPath());
            if(effect instanceof SpellEffects.ForEach e)collectPlan(e.effects(),actions,manifestations);
            if(effect instanceof SpellEffects.Sequence e)collectPlan(e.effects(),actions,manifestations);
            if(effect instanceof SpellEffects.Repeat e)collectPlan(e.effects(),actions,manifestations);
            if(effect instanceof SpellEffects.Branch e) {collectPlan(e.whenTrue(),actions,manifestations);collectPlan(e.whenFalse(),actions,manifestations);}
            if(effect instanceof SpellEffects.InstallBinding e)collectPlan(e.binding().effects(),actions,manifestations);
            if(effect instanceof SpellEffects.CreateManifestation e) {
                var m=e.manifestation();manifestations.add(m);collectPlan(m.onHit(),actions,manifestations);collectPlan(m.onTick(),actions,manifestations);collectPlan(m.onEnd(),actions,manifestations);
                for(var b:m.bindings())collectPlan(b.effects(),actions,manifestations);
            }
        }
    }
    private void label(net.minecraft.world.entity.LivingEntity entity,String role) { roles.put(entity.getUUID(),role);entity.setCustomName(net.minecraft.network.chat.Component.literal(role));entity.setCustomNameVisible(true); }
    private static String findFormation(List<SpellEffect> effects) {
        for(SpellEffect effect:effects) {
            if(effect instanceof SpellEffects.ForEach each) { String nested=findFormation(each.effects());if(!nested.isEmpty())return nested; }
            if(effect instanceof SpellEffects.CreateManifestation create) {
                var value=create.manifestation().identifiers().get("formation");if(value!=null)return value.getPath();
            }
        }
        return "";
    }
    private static void collectSelections(List<SpellEffect> effects,List<SpellEffects.ForEach> result) {
        for(SpellEffect effect:effects) {
            if(effect instanceof SpellEffects.ForEach each) { result.add(each);collectSelections(each.effects(),result); }
            if(effect instanceof SpellEffects.CreateManifestation create) { collectSelections(create.manifestation().onHit(),result);collectSelections(create.manifestation().onTick(),result); }
            if(effect instanceof SpellEffects.Repeat repeat)collectSelections(repeat.effects(),result);
            if(effect instanceof SpellEffects.Sequence sequence)collectSelections(sequence.effects(),result);
            if(effect instanceof SpellEffects.Branch branch) {collectSelections(branch.whenTrue(),result);collectSelections(branch.whenFalse(),result);}
            if(effect instanceof SpellEffects.InstallBinding binding)collectSelections(binding.binding().effects(),result);
        }
    }
    public Map<String,Object> snapshot() {
        var result=new LinkedHashMap<String,Object>();result.put("outcome",outcome());
        if(cast!=null) {
            result.put("castContext",Map.of("scenario",scenario,"observations",List.copyOf(observations),
                    "subjects",subjects.stream().map(this::subjectSnapshot).toList()));
            cast.failure().ifPresent(failure->result.put("failure",failure.toString()));
        }
        return result;
    }
    private Map<String,Object> subjectSnapshot(LivingEntity original) {
        var current=NativeMagic.session(captureServer).world().entity(original.getUUID());
        LivingEntity mob=current instanceof LivingEntity living?living:original;
        double distance=mob.level().dimension().location().equals(startingDimensions.getOrDefault(mob.getUUID(),mob.level().dimension().location()))
                ?Math.sqrt(mob.position().distanceToSqr(startingPositions.getOrDefault(mob.getUUID(),mob.position()))):0;
        return Map.of("role",roles.getOrDefault(mob.getUUID(),"Subject"),"before",startingHealth.getOrDefault(mob.getUUID(),20f),"after",mob.getHealth(),"alive",mob.isAlive(),"displacement",distance);
    }
    private Villager villager(MinecraftServer server,double x,double y,double z) {
        var level=server.overworld(); var mob=EntityType.VILLAGER.create(level);
        // Keep vanilla movement/falling active; a zero walking speed keeps the scene readable.
        mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);
        mob.setPersistenceRequired(); mob.setPos(x,y,z); level.addFreshEntity(mob);subjects.add(mob); return mob;
    }
}
