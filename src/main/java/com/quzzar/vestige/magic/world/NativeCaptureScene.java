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
    public record CameraPose(Vec3 eye, float yaw, float pitch) { }
    private volatile CameraPose apparatusCamera;
    public CameraPose apparatusCamera() { return apparatusCamera; }
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
    private com.quzzar.vestige.apparatus.OfferingBlockEntity ritualCenter;
    private String ritualOutcome;
    public int elapsedTicks() { return elapsed; }
    private float pitch;
    private boolean defensive;
    private boolean repeatedRecast;
    private float yaw;
    private boolean playerView, blockScene, outgoingAttacks, spatialRecast, summonCohort;
    private boolean fireDamage, waterWalking, climbing, airGuard, sharedGuard, reflectingFixture, anchoredFixture;
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
        if (kind.equals("ritual")) return List.of(new Job("vestige:ritual_idle",0,"Ready ingredients before activation",kind),
                new Job("vestige:ritual_capacity",0,"Missing outer Plinths: outward reach and collapse",kind),
                new Job("vestige:ritual_hints",0,"Reference scroll hints and placement movement",kind),
                new Job("vestige:ritual_success",0,"Solved five-part recipe",kind),
                new Job("vestige:ritual_reference_success",0,"Flat reference and finished scroll",kind),
                new Job("vestige:ritual_discovery",0,"Mixed-fragment discovery",kind),
                new Job("vestige:ritual_failure",0,"Ingredient and creature blast",kind),
                new Job("vestige:ritual_failure_eight",0,"Eight-node local backfire",kind),
                new Job("vestige:ritual_failure_spread",0,"Backfire across distant elevated nodes",kind),
                new Job("vestige:ritual_wrong",0,"Completed wrong recipe without a blast",kind),
                new Job("vestige:ritual_spellshaping",0,"Automatic material Spellshaping",kind),
                new Job("vestige:ritual_attunement",0,"Eight-slot Attunement Shard",kind),
                new Job("vestige:leyline_henge",0,"Nature henge crafting",kind),
                new Job("vestige:leyline_pyramid",0,"Pyramid crafting",kind)).stream()
                .filter(job -> filter.isBlank() || Arrays.stream(filter.split(",")).anyMatch(id -> job.spell().equals(id.contains(":") ? id : "vestige:"+id))).toList();
        if (kind.equals("apparatus")) {
            var jobs=new ArrayList<>(List.of(new Job("vestige:spellstone",0,"Spellstone",kind),
                    new Job("vestige:spellstone_astral",0,"Astral Spellstone",kind),
                    new Job("vestige:spellstone_astral_side",0,"Spellstone tent opening",kind),
                    new Job("vestige:spellstone_astral_night",0,"Astral Spellstone at night",kind),
                    new Job("vestige:spellstone_astral_scrolls",0,"Astral Spellstone with reference and result",kind),
                    new Job("vestige:spellstone_astral_hover",0,"Straight stone edges when selected",kind),
                    new Job("vestige:spellstone_astral_reference",0,"Runes below a resting reference scroll",kind),
                    new Job("vestige:spellstone_astral_underwater",0,"Submerged Astral Spellstone",kind),
                    new Job("vestige:spellstone_astral_finishes",0,"Stone Brick, Tuff and Quartz Spellstones",kind),
                    new Job("vestige:plinth",0,"Plinth",kind),
                    new Job("vestige:spellstone_cap_clearance",0,"Raised Spellstone cap",kind),
                    new Job("vestige:apparatus_trim_plinth",0,"Approved Plinth edging",kind),
                    new Job("vestige:apparatus_trim_spellstone",0,"Approved Spellstone edging",kind),
                    new Job("vestige:apparatus_trim_variants",0,"Approved edging across native finishes",kind),
                    new Job("vestige:plinth_columns",0,"Connected Plinth columns",kind),
                    new Job("vestige:plinth_column_detail",0,"Continuous column texture detail",kind),
                    new Job("vestige:plinth_column_head",0,"Close-up of column-head framing",kind),
                    new Job("vestige:plinth_column_imbuements",0,"Independent imbuements on every column segment",kind),
                    new Job("vestige:apparatus_underwater",0,"Waterlogged Spellstone and Plinths",kind),
                    new Job("vestige:leyline_henge",0,"Nature henge",kind),
                    new Job("vestige:leyline_pyramid",0,"Divination pyramid",kind),
                    new Job("vestige:apparatus_layout",0,"Apparatus item-display review",kind),
                    new Job("vestige:apparatus_item_scale",0,"Offerings and real dropped items",kind)));
            String preview=System.getProperty("vestige.capture.apparatus_preview","current");
            if(preview.equals("plinth-framing")) jobs.addAll(List.of(
                    new Job("vestige:plinth_frame_bands",0,"A — Rim bands",kind),
                    new Job("vestige:plinth_frame_corners",0,"B — Corner strips",kind),
                    new Job("vestige:plinth_frame_panels",0,"C — Framed sides",kind),
                    new Job("vestige:plinth_frame_corner_ribs",0,"D — Corner strips + ribs",kind),
                    new Job("vestige:plinth_frame_corner_edges",0,"E — Corner strips + rim edges",kind),
                    new Job("vestige:spellstone_frame_plain",0,"Plain Spellstone",kind),
                    new Job("vestige:spellstone_frame_edges",0,"Spellstone with matching edge trim",kind)));
            if(preview.equals("corner-details")) jobs.addAll(List.of(
                    new Job("vestige:corner_chip",0,"Corner chip",kind),
                    new Job("vestige:corner_notch",0,"Stepped corner inset",kind),
                    new Job("vestige:corner_vein",0,"Broken corner vein",kind),
                    new Job("vestige:corner_crust",0,"Crystal corner flecks",kind),
                    new Job("vestige:corner_blackstone",0,"Polished Blackstone corner study",kind),
                    new Job("vestige:corner_quartz",0,"Smooth Quartz corner study",kind),
                    new Job("vestige:corner_sandstone",0,"Smooth Sandstone corner study",kind)));
            if(List.of("spellstone-designs","seal-refinements").contains(preview)) {
                boolean seals=preview.equals("seal-refinements");
                jobs.addAll(List.of(new Job("vestige:spellstone_design_tablet",0,seals?"Rimmed Sealstone":"Runic Tablet",kind),
                        new Job("vestige:spellstone_design_altar",0,seals?"Engraved Sealstone":"Twin-Pier Altar",kind),
                        new Job("vestige:spellstone_design_seal",0,seals?"Banded Sealstone":"Sealstone",kind),
                        new Job("vestige:spellstone_designs",0,seals?"Three Sealstone refinements":"Three Spellstone designs",kind)));
            }
            if(preview.equals("rune-options")) {
                for(String lighting:List.of("","_night")) jobs.addAll(List.of(
                        new Job("vestige:rune_engraving"+lighting,0,"Illuminated Engraving",kind),
                        new Job("vestige:rune_astral"+lighting,0,"Astral Seal",kind),
                        new Job("vestige:rune_living"+lighting,0,"Living Script",kind),
                        new Job("vestige:rune_comparison"+lighting,0,"Three native rune treatments",kind)));
            }
            if(preview.equals("astral-bodies")) jobs.addAll(List.of(
                    new Job("vestige:astral_body_relic",0,"Inscribed Relic",kind),
                    new Job("vestige:astral_body_tablet",0,"Bound Tablet",kind),
                    new Job("vestige:astral_body_vault",0,"Vaulted Seal",kind),
                    new Job("vestige:astral_body_comparison",0,"Three Astral stonework designs",kind)));
            if(preview.equals("gilded-finishes")) jobs.addAll(List.of(
                    new Job("vestige:gilded_stone",0,"Gilded Smooth Stone",kind),
                    new Job("vestige:gilded_blackstone",0,"Gilded Polished Blackstone",kind),
                    new Job("vestige:gilded_quartz",0,"Gilded Smooth Quartz",kind),
                    new Job("vestige:gilded_sandstone",0,"Gilded Smooth Sandstone",kind),
                    new Job("vestige:gilded_comparison",0,"Four gilded material finishes",kind)));
            if(preview.equals("diamond-trim")) jobs.addAll(List.of(
                    new Job("vestige:trim_stone",0,"Smooth Stone with stone trim and Diamond inlays",kind),
                    new Job("vestige:trim_blackstone",0,"Polished Blackstone with stone trim and Diamond inlays",kind),
                    new Job("vestige:trim_quartz",0,"Smooth Quartz with stone trim and Diamond inlays",kind),
                    new Job("vestige:trim_sandstone",0,"Smooth Sandstone with stone trim and Diamond inlays",kind),
                    new Job("vestige:trim_comparison",0,"Four stone borders with Diamond top inlays",kind)));
            if(preview.equals("crystal-core")) jobs.addAll(List.of(
                    new Job("vestige:crystal_stone",0,"Crystal-edge Spellstone",kind),
                    new Job("vestige:crystal_side",0,"Crystal-edge side view",kind),
                    new Job("vestige:crystal_night",0,"Crystal-edge Spellstone at night",kind)));
            if(preview.equals("fractured")) jobs.addAll(List.of(
                    new Job("vestige:relic_stone",0,"Fractured relic Spellstone",kind),
                    new Job("vestige:relic_top",0,"Fractured relic from above",kind),
                    new Job("vestige:relic_night",0,"Fractured relic at night",kind),
                    new Job("vestige:relic_scrolls",0,"Fractured relic with real scrolls",kind)));
            if(filter.isBlank())return jobs;
            var selected=java.util.Arrays.stream(filter.split(",")).map(String::trim).map(s->s.contains(":")?s:"vestige:"+s).collect(java.util.stream.Collectors.toSet());
            return jobs.stream().filter(j->selected.contains(j.spell())).toList();
        }
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
        apparatusCamera=null;
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
        // Breaking yesterday's offerings can produce real dropped items; keep those out of the next fixture.
        for (Entity entity:List.copyOf(com.google.common.collect.Lists.newArrayList(level.getAllEntities()))) if (!(entity instanceof ServerPlayer)) entity.discard();
        roles.clear();startingPositions.clear();startingDimensions.clear();scenario="Caster aims at a villager";formation="";formationBase=null;summonScene=false;chainCount=0;sceneRadius=0;
        job=next; cast=null; geometry=null;elapsed=0;subjects.clear();startingHealth.clear();defensive=false;repeatedRecast=false;eating=false;thirdPerson=false;
        actions.clear();manifestations.clear();observations.clear();initialBlocks.clear();recasts=0;blockScene=false;outgoingAttacks=false;spatialRecast=false;summonCohort=false;playerView=false;showHud=false;yaw=-90;
        fireDamage=false;waterWalking=false;climbing=false;airGuard=false;sharedGuard=false;reflectingFixture=false;anchoredFixture=false;
        ritualCenter=null;ritualOutcome=null;
        if (job.kind().equals("ritual")) { caster=null;target=null;prepareRitual(server);return; }
        if (job.kind().equals("apparatus")) {
            caster=null;target=null;prepareApparatus(server);return;
        }
        caster=villager(server,-3,65,0); target=villager(server,3,65,0);
        caster.setYRot(-90); caster.setYHeadRot(-90); caster.setXRot(0);
        session.world().registerActor(caster); session.world().registerActor(target);
        SpellDefinition spell=captureSpell(job).spell();
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
            var plans=new ArrayList<SpellEffects.ForEach>();collectSelections(spell.effects(),plans);
            chainCount=plans.stream().filter(e->e.target().selection()==TargetSpec.Selection.CHAIN).mapToInt(e->(int)e.target().options().getOrDefault("count",new com.quzzar.vestige.magic.expression.SpellValue.Constant(2)).resolve(spell.traits())).max().orElse(0);
            if(chainCount>0) {
                for(int i=1;i<chainCount;i++) {
                    var nextTarget=villager(server,3+(i%2)*2.0,65,(i/2)*2.0+(i%2)*1.2);
                    label(nextTarget,"Hop "+(i+1));session.world().registerActor(nextTarget);
                }
                var beyond=villager(server,10,65,-3);label(beyond,"Beyond jump range");session.world().registerActor(beyond);
                scenario="Ordered lightning chain: "+chainCount+" targets; a distant villager stays outside jump range";
            }
            sceneRadius=plans.stream().filter(e->e.target().selection()==TargetSpec.Selection.NEAR_TARGET && SpellCapabilities.ofPlan(e.effects()).stream().anyMatch(action->Set.of("damage","typed_damage").contains(action.getPath())))
                    .mapToDouble(e->e.target().distance().resolve(spell.traits())).max().orElse(0);
            if(sceneRadius>0 && chainCount==0) {
                double r=Math.min(5,sceneRadius);
                boolean selfField=spell.effects().stream().anyMatch(e->e instanceof SpellEffects.CreateManifestation c
                        && c.manifestation().kind().getPath().equals("area") && c.target().selection()==TargetSpec.Selection.SELF);
                double centerX=selfField ? caster.getX() : target.getX();
                double centerZ=selfField ? caster.getZ() : target.getZ();
                if(selfField)target.teleportTo(centerX+Math.min(1,r*.3),65,centerZ);
                label(target,"Center");
                label(villager(server,centerX,65,centerZ-r*.55),"Inside A");
                label(villager(server,centerX+r*.55,65,centerZ+r*.4),"Inside B");
                label(villager(server,centerX,65,centerZ+r+1.4),"Outside A");
                label(villager(server,centerX+r+1.4,65,centerZ-1.5),"Outside B");
                for(int x=(int)centerX-7;x<=centerX+7;x++) for(int z=(int)centerZ-7;z<=centerZ+7;z++) {
                    double d=Math.hypot(x+.5-centerX,z+.5-centerZ);
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
        if (job.kind().equals("ritual")) {
            if(job.spell().endsWith("idle")) { ritualOutcome="READY";return; }
            var player=server.getPlayerList().getPlayers().getFirst(); player.getRandom().setSeed(job.spell().endsWith("wrong")?0:4096);
            ritualOutcome=com.quzzar.vestige.apparatus.RitualCrafting.activate(player,ritualCenter).name();return;
        }
        if (job.kind().equals("apparatus")) return;
        var level=server.overworld(); var session=NativeMagic.session(server);
        var compiled=captureSpell(job);SpellDefinition spell=compiled.spell();
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
                    new SpellSubject.Entity(target.getUUID())),compiled.modifiers(),true,Optional.empty(),true,compiled.shaping());
        } else {
            var phase=SpellEffectGallery.phases(spell).get(job.phase());
            var start=new SpellVisualPayload.Point(caster.getEyePosition(),-1,new UUID(0,0),0);
            var end=geometry==null?new SpellVisualPayload.Point(new Vec3(0,65+phase.visual().height(),0),-1,new UUID(0,0),0)
                    :new SpellVisualPayload.Point(geometry.position().add(0,phase.visual().height(),0),geometry.getId(),geometry.getUUID(),(float)phase.visual().height());
            session.world().visuals.start(level,phase.visual(),phase.visual().radius().resolve(spell.traits()),geometry!=null,()->List.of(start,end),()->true);
        }
    }
    public String outcome() { return cast==null?"cosmetic replay":cast.status().name(); }
    private static com.quzzar.vestige.apparatus.Spellshaping.Compiled captureSpell(Job job) {
        var base=NativeMagic.spells().spells().get(ResourceLocation.parse(job.spell()));
        var selections=job.kind().equals("cast") ? Arrays.stream(System.getProperty("vestige.capture.augments","").split(",")).filter(s->!s.isBlank())
                .map(s->new com.quzzar.vestige.apparatus.Spellshaping.Selection(ResourceLocation.parse(s.contains(":") ? s : "vestige:"+s),1)).toList() : List.<com.quzzar.vestige.apparatus.Spellshaping.Selection>of();
        return com.quzzar.vestige.apparatus.Spellshaping.compile(base,selections,List.of(),selections.isEmpty() ? com.quzzar.vestige.magic.runtime.CastShaping.NONE : new com.quzzar.vestige.magic.runtime.CastShaping(1,true));
    }
    public double minimumSeconds(Job next,double requested) {
        if (next.kind().equals("ritual")) return Math.max(4,requested);
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
        if (job.kind().equals("ritual")) { elapsed++; return; }
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
        if(reflectingFixture && Set.of(8,28,48,85).contains(elapsed) && caster.isAlive() && target.isAlive()) {
            var arrow=EntityType.ARROW.create(server.overworld());
            if(arrow!=null){
                arrow.setOwner(target);arrow.setBaseDamage(3);arrow.setNoGravity(true);
                arrow.setPos(target.getEyePosition().add(-.6,0,0));arrow.setDeltaMovement(caster.getBoundingBox().getCenter().subtract(arrow.position()).normalize().scale(.65));
                server.overworld().addFreshEntity(arrow);
            }
        }
        if(anchoredFixture && elapsed==30)caster.teleportTo(-3,65,-5);
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
        reflectingFixture=actions.contains("reflect_projectiles");
        anchoredFixture=Arrays.stream(System.getProperty("vestige.capture.augments","").split(",")).anyMatch(s->s.equals("anchored") || s.equals("vestige:anchored"));
        if(actions.contains("reveal_hidden")) {
            target.teleportTo(-1,65,0);target.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,300));label(target,"Hidden A");
            var second=villager(server,-2,65,2);second.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,300));label(second,"Hidden B");
            var outside=villager(server,4,65,3);outside.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,300));label(outside,"Outside reveal radius");
            scenario="Detect Magic outlines real invisible villagers without removing their invisibility";
        }
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
        if(reflectingFixture){defensive=false;target.teleportTo(3,65,0);scenario="Glass Shield returns the first two incoming arrows; later arrows exhaust the budget or arrive after expiry";}
        if(anchoredFixture){scenario="Gravity Fissure remains at its initial position while the caster moves away";}
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
        fixtureBlock(server,pos,block.defaultBlockState());
    }
    private void fixtureBlock(MinecraftServer server,BlockPos pos,net.minecraft.world.level.block.state.BlockState state) {
        state=net.minecraft.world.level.block.Block.updateFromNeighbourShapes(state,server.overworld(),pos);
        server.overworld().setBlock(pos,state,3);fixtureBlocks.add(pos);
        initialBlocks.put(pos,net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
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
            if(effect instanceof SpellEffects.Limited e)collectPlan(e.effects(),actions,manifestations);
            if(effect instanceof SpellEffects.Secondary e)collectPlan(e.effects(),actions,manifestations);
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
            if(effect instanceof SpellEffects.Limited e)collectSelections(e.effects(),result);
            if(effect instanceof SpellEffects.Secondary e)collectSelections(e.effects(),result);
            if(effect instanceof SpellEffects.Sequence sequence)collectSelections(sequence.effects(),result);
            if(effect instanceof SpellEffects.Branch branch) {collectSelections(branch.whenTrue(),result);collectSelections(branch.whenFalse(),result);}
            if(effect instanceof SpellEffects.InstallBinding binding)collectSelections(binding.binding().effects(),result);
        }
    }
    public Map<String,Object> snapshot() {
        if (job.kind().equals("ritual")) {
            var layout=com.quzzar.vestige.apparatus.RitualCrafting.layout(ritualCenter);
            var scroll=com.quzzar.vestige.apparatus.ScrollItems.scroll(com.quzzar.vestige.apparatus.RitualTestOutput.stack(ritualCenter));
            Map<String,Object> shaping=scroll.<Map<String,Object>>map(s -> Map.of("spell",s.spell().toString(),
                    "castingCost",s.shaping().castingCost(),"roundAmounts",s.shaping().roundAmounts(),
                    "modifiers",s.modifiers().stream().map(m -> Map.of("trait",m.trait().toString(),"multiplier",m.amount())).toList(),
                    "augments",s.augments().stream().map(a->Map.of("id",a.id().toString(),"degree",a.degree())).toList())).orElse(Map.of());
            var attunement=com.quzzar.vestige.apparatus.AttunementShardItem.signature(com.quzzar.vestige.apparatus.RitualTestOutput.stack(ritualCenter)).map(a->Map.of("key",a.key(),"blueprint",a.blueprint())).orElse(Map.of());
            return Map.of("outcome",ritualOutcome,"scenario",scenario,"output",com.quzzar.vestige.apparatus.RitualTestOutput.stack(ritualCenter).toString(),
                    "reference",ritualCenter.displayedItem().toString(),"offerings",layout.items().stream().map(ItemStack::toString).toList(),
                    "subjects",subjects.stream().map(this::subjectSnapshot).toList(),"elapsedTicks",elapsed,"shaping",shaping,"geometry",layout.geometry().toString(),"attunement",attunement);
        }
        if (job.kind().equals("apparatus")) {
            var apparatus=new LinkedHashMap<String,Object>();
            for(var pos:initialBlocks.keySet())if(captureServer.overworld().getBlockEntity(pos) instanceof com.quzzar.vestige.apparatus.OfferingBlockEntity node) {
                apparatus.put(pos.toString(),Map.of("state",node.getBlockState().toString(),
                        "socket",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(node.materialItem().getItem()).toString(),
                        "offering",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(node.displayedItem().getItem()).toString(),
                        "offeringSpace",node.hasOfferingSpace()));
            }
            return Map.of("outcome","PLACED","scenario",scenario,"blocks",Map.copyOf(initialBlocks),
                    "apparatusState",apparatus,"source","Registered blocks and synchronized offering block entities");
        }
        var result=new LinkedHashMap<String,Object>();result.put("outcome",outcome());
        if(job.kind().equals("cast"))result.put("augments",Arrays.stream(System.getProperty("vestige.capture.augments","").split(",")).filter(s->!s.isBlank()).toList());
        if(cast!=null) {
            result.put("castContext",Map.of("scenario",scenario,"observations",List.copyOf(observations),
                    "subjects",subjects.stream().map(this::subjectSnapshot).toList()));
            cast.failure().ifPresent(failure->result.put("failure",failure.toString()));
        }
        return result;
    }

    private void prepareApparatus(MinecraftServer server) {
        var material=com.quzzar.vestige.apparatus.ApparatusMaterials.valueOf(System.getProperty("vestige.capture.material","stone_bricks").toUpperCase(java.util.Locale.ROOT));
        var stone=com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(material).get();
        var plinth=com.quzzar.vestige.apparatus.ApparatusBlocks.PLINTHS.get(material).get();
        Vec3 camera,focus;
        if(job.spell().equals("vestige:apparatus_trim_plinth")) {
            server.overworld().setDayTime(6000);fixtureBlock(server,new BlockPos(0,65,0),plinth);
            camera=new Vec3(1.45,66.05,1.7);focus=new Vec3(.5,65.45,.5);
            scenario="Production Plinth model and color registration, without a preview resource pack; approved corner and outer rim strips.";
        } else if(job.spell().equals("vestige:apparatus_trim_spellstone")) {
            server.overworld().setDayTime(6000);fixtureBlock(server,new BlockPos(0,65,0),stone);
            camera=new Vec3(1.2,65.8,1.65);focus=new Vec3(.5,65.38,.5);
            scenario="Production Spellstone model and color registration, without a preview resource pack; approved cap and support strips with original Diamond corners and Astral Seal.";
        } else if(job.spell().equals("vestige:apparatus_trim_variants")) {
            server.overworld().setDayTime(6000);
            var variants=List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.QUARTZ,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.POLISHED_DEEPSLATE);
            for(int i=0;i<variants.size();i++) {
                var finish=variants.get(i);int x=(i-1)*3;
                fixtureBlock(server,new BlockPos(x,65,2),com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(finish).get());
                var column=com.quzzar.vestige.apparatus.ApparatusBlocks.PLINTHS.get(finish).get();
                for(int y=65;y<=66;y++)fixtureBlock(server,new BlockPos(x,y,-1),column);
            }
            camera=new Vec3(5.8,70,10);focus=new Vec3(.5,65.8,.5);
            scenario="Production Stone, Quartz and Polished Deepslate Spellstones and two-block Plinths. The approved shade follows each native finish; Quartz corner strips follow its pillar shaft.";
        } else if(job.spell().startsWith("vestige:plinth_frame_")) {
            server.overworld().setDayTime(6000);
            var carriers=List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.ANDESITE,com.quzzar.vestige.apparatus.ApparatusMaterials.DIORITE,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.GRANITE,com.quzzar.vestige.apparatus.ApparatusMaterials.COBBLESTONE);
            int index=List.of("bands","corners","panels","corner_ribs","corner_edges").indexOf(job.spell().substring("vestige:plinth_frame_".length()));
            if(index<0)throw new IllegalArgumentException("Unknown Plinth framing study");
            fixtureBlock(server,new BlockPos(0,65,0),com.quzzar.vestige.apparatus.ApparatusBlocks.PLINTHS.get(carriers.get(index)).get());
            camera=new Vec3(1.45,66.05,1.7);focus=new Vec3(.5,65.45,.5);
            scenario="Capture-only Stone Plinth framing. All five studies retain the production body, original Minecraft textures, camera and native lighting; corner_ribs adds shallow raised ribs and corner_edges combines B corners with A rim edges using lightly darkened native Stone. Earlier studies use Polished Deepslate. No production frame was applied.";
        } else if(job.spell().startsWith("vestige:spellstone_frame_")) {
            server.overworld().setDayTime(6000);
            boolean framed=job.spell().equals("vestige:spellstone_frame_edges");
            var carrier=framed?com.quzzar.vestige.apparatus.ApparatusMaterials.STONE:com.quzzar.vestige.apparatus.ApparatusMaterials.ANDESITE;
            fixtureBlock(server,new BlockPos(0,65,0),com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(carrier).get());
            camera=new Vec3(1.2,65.8,1.65);focus=new Vec3(.5,65.38,.5);
            scenario="Capture-only Stone Spellstone framing comparison with unchanged thick cap, tilted supports, Diamond corner pixels and native Astral Seal. The framed view adds flat strips of lightly darkened native Stone at cap edges and along the supports. No production art was applied.";
        } else if(job.spell().equals("vestige:spellstone_cap_clearance")) {
            server.overworld().setDayTime(6000);fixtureBlock(server,new BlockPos(0,65,0),stone);
            camera=new Vec3(1.2,65.8,1.65);focus=new Vec3(.5,65.38,.5);
            scenario="Production Spellstone: cap lifted 0.25 model units with both support coordinates unchanged; flat Diamond corner pixels and hovering Astral Seal follow the top.";
        } else if(job.spell().startsWith("vestige:corner_")) {
            server.overworld().setDayTime(6000);
            var carriers=List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE_BRICKS,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.TUFF,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.QUARTZ,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.SANDSTONE,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.POLISHED_BLACKSTONE,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.SMOOTH_QUARTZ,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.SMOOTH_SANDSTONE);
            var finishes=List.of("chip","notch","vein","crust","blackstone","quartz","sandstone");
            int index=finishes.indexOf(job.spell().substring("vestige:corner_".length()));
            if(index<0)throw new IllegalArgumentException("Unknown corner study");
            fixtureBlock(server,new BlockPos(0,65,0),com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(carriers.get(index)).get());
            camera=new Vec3(1.22,65.92,1.46);
            focus=new Vec3(.5,65.3,.5);
            scenario="Capture-only native corner texture study: four pixel details wrap the vertical corners of the compact three-piece top stone. Original 16x16 Minecraft tiles supply stone and Diamond colours. Same body and Astral Seal in all seven views; recipes, collision and production installation unchanged.";
        } else if(job.spell().startsWith("vestige:relic_")) {
            server.overworld().setDayTime(job.spell().endsWith("night")?18000:6000);
            fixtureBlock(server,new BlockPos(0,65,0),stone);
            if(job.spell().endsWith("scrolls")) {
                var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(0,65,0));
                var spellId=com.quzzar.vestige.VestigeMainMod.location("fireball");
                if(!node.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(spellId)))throw new IllegalStateException("Reference fixture rejected scroll");
                if(!node.insertResult(com.quzzar.vestige.apparatus.ScrollItems.scroll(VestigeMainMod.location("pf2_flicker"))))throw new IllegalStateException("Result fixture rejected scroll");
            }
            camera=job.spell().endsWith("top")?new Vec3(1.25,66.8,1.5):new Vec3(1.35,66.1,1.7);
            focus=new Vec3(.5,65.25,.5);
            scenario="Capture-only fractured relic: three substantial interlocking stone fragments with narrow breaks and the unchanged lowered Astral Seal. Existing construction recipes, production collision and Prism installation remain unchanged.";
        } else if(job.spell().startsWith("vestige:crystal_")) {
            server.overworld().setDayTime(job.spell().endsWith("night")?18000:6000);
            fixtureBlock(server,new BlockPos(0,65,0),stone);
            camera=job.spell().endsWith("side")?new Vec3(1.5,65.65,1.9):new Vec3(1.35,66.1,1.7);
            focus=new Vec3(.5,65.25,.5);
            scenario="One capture-only crystal-edge Spellstone: solid Amethyst edge facets form part of the stone body; Diamond is cut into two opposing upper crystal edges. Accepted lowered Astral Seal, production collision, construction recipes and Prism installation are unchanged.";
        } else if(job.spell().startsWith("vestige:gilded_") || job.spell().startsWith("vestige:trim_")) {
            boolean trim=job.spell().startsWith("vestige:trim_");
            String prefix=trim?"vestige:trim_":"vestige:gilded_";
            server.overworld().setDayTime(6000);
            var carriers=List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE_BRICKS,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.TUFF,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.QUARTZ,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.SANDSTONE);
            var finishes=List.of("stone","blackstone","quartz","sandstone");
            boolean comparison=job.spell().endsWith("comparison");
            for(int i=0;i<4;i++)if(comparison || job.spell().equals(prefix+finishes.get(i)))
                fixtureBlock(server,new BlockPos(comparison?i*2-3:0,65,0),com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(carriers.get(i)).get());
            camera=comparison?new Vec3(3,67,6.5):new Vec3(1.35,66.1,1.7);
            focus=new Vec3(.5,65.25,.5);
            scenario=(trim?"Capture-only stone borders and two fitted Diamond top inlays, same low octagon and accepted lowered Astral Seal. ":
                    "Capture-only gilded texture studies: identical low octagon, folded gold edge and accepted lowered Astral Seal. ")+
                    (comparison?"Left to right: Smooth Stone, Polished Blackstone, Smooth Quartz, Smooth Sandstone.":job.label()+" at the same camera and scale.")+
                    " Existing collision, construction recipes and Prism installation are unchanged.";
        } else if(job.spell().startsWith("vestige:astral_body_")) {
            server.overworld().setDayTime(6000);
            var carriers=List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE_BRICKS,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.TUFF,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.QUARTZ);
            boolean comparison=job.spell().endsWith("comparison");
            int selected=job.spell().endsWith("tablet")?1:job.spell().endsWith("vault")?2:0;
            for(int i=0;i<3;i++)if(comparison || i==selected)
                fixtureBlock(server,new BlockPos(comparison?(i-1)*2:0,65,0),com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(carriers.get(i)).get());
            camera=comparison?new Vec3(2.5,66.5,5):new Vec3(1.35,66.1,1.7);
            focus=new Vec3(.5,65.25,.5);
            scenario="Capture-only Astral stonework studies in one Stone Brick palette, with the accepted lowered native seal. "+
                    (comparison?"Left to right: Inscribed Relic, Bound Tablet, Vaulted Seal.":job.label()+" at the same camera and scale.")+
                    " Production models, collision, recipes and Prism installation are unchanged.";
        } else if(job.spell().startsWith("vestige:spellstone_astral")) {
            server.overworld().setDayTime(job.spell().endsWith("night")?18000:6000);
            boolean finishes=job.spell().endsWith("finishes"),underwater=job.spell().endsWith("underwater");
            var materials=finishes?List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE_BRICKS,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.TUFF,com.quzzar.vestige.apparatus.ApparatusMaterials.QUARTZ):List.of(material);
            if(underwater)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)fixtureBlock(server,new BlockPos(x,65,z),Blocks.WATER);
            for(int i=0;i<materials.size();i++) {
                var pos=new BlockPos(finishes?(i-1)*2:0,65,0);
                var state=com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(materials.get(i)).get().defaultBlockState();
                fixtureBlock(server,pos,state.setValue(com.quzzar.vestige.apparatus.ApparatusBlock.WATERLOGGED,underwater));
                if(job.spell().endsWith("scrolls")) {
                    var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(pos);
                    node.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(VestigeMainMod.location("fireball")));
                    if(!node.insertResult(com.quzzar.vestige.apparatus.ScrollItems.scroll(VestigeMainMod.location("pf2_flicker"))))throw new IllegalStateException("Result fixture rejected scroll");
                }
            }
            camera=finishes?new Vec3(2.5,66.5,5):job.spell().endsWith("side")?new Vec3(.5,65.5,2.15):new Vec3(1.35,66.1,1.7);
            focus=new Vec3(.5,65.25,.5);
            if(job.spell().endsWith("hover")) {
                for(var player:server.overworld().players()) {
                    player.setGameMode(GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
                }
            }
            if(job.spell().endsWith("reference")) {
                var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(0,65,0));
                node.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(VestigeMainMod.location("fireball")));
            }
            scenario="Production Spellstone: two thick inward-leaning supports and one smaller thick cap at y10/16, four smaller A Diamond chips wrapping the vertical corners, and native Astral Seal below resting items. Straight authored stone edges replace collision-slice selection lines. "+
                    (finishes?"Left to right: Stone Bricks, Tuff, Quartz.":job.label())+" No resource-pack override.";
        } else if(job.spell().startsWith("vestige:rune_")) {
            boolean night=job.spell().endsWith("_night");
            server.overworld().setDayTime(night?18000:6000);
            var carriers=List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE_BRICKS,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.TUFF,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.QUARTZ);
            boolean comparison=job.spell().contains("comparison");
            int selected=job.spell().contains("astral")?1:job.spell().contains("living")?2:0;
            for(int i=0;i<3;i++)if(comparison || i==selected)
                fixtureBlock(server,new BlockPos(comparison?(i-1)*2:0,65,0),com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(carriers.get(i)).get());
            camera=comparison?new Vec3(2.5,66.5,5):new Vec3(1.35,66.1,1.7);
            focus=new Vec3(.5,65.25,.5);
            scenario="Capture-only continuous native rune treatments on the same seven-cuboid Sealstone. "+
                    (comparison?"Left to right: Illuminated Engraving, Astral Seal, Living Script.":job.label()+" at the same camera and scale.")+
                    (night?" Midnight lighting.":" Noon lighting.")+" Native additive glow; no external shaders or dynamic world light. Production art and Prism installation unchanged.";
        } else if(job.spell().startsWith("vestige:spellstone_design")) {
            var carriers=List.of(com.quzzar.vestige.apparatus.ApparatusMaterials.STONE_BRICKS,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.TUFF,
                    com.quzzar.vestige.apparatus.ApparatusMaterials.QUARTZ);
            boolean comparison=job.spell().equals("vestige:spellstone_designs");
            int selected=job.spell().endsWith("altar")?1:job.spell().endsWith("seal")?2:0;
            for(int i=0;i<3;i++)if(comparison || i==selected) {
                var block=com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(carriers.get(i)).get();
                fixtureBlock(server,new BlockPos(comparison?(i-1)*2:0,65,0),block);
            }
            camera=comparison?new Vec3(2.5,66.5,5):new Vec3(1.35,66.1,1.7);
            focus=new Vec3(.5,65.25,.5);
            boolean seals=System.getProperty("vestige.capture.apparatus_preview","current").equals("seal-refinements");
            scenario="Capture-only Spellstone alternatives with the same Stone Brick textures and original native rune. "+
                    (comparison?(seals?"Left to right: Rimmed, Engraved, Banded Sealstone.":"Left to right: Runic Tablet, Twin-Pier Altar, Sealstone."):job.label()+" at the same camera and scale.")+
                    " Production models, collision, recipes and Prism installation are unchanged.";
        } else if (job.spell().equals("vestige:leyline_henge") || job.spell().equals("vestige:leyline_pyramid")) {
            boolean pyramid=job.spell().endsWith("pyramid");
            var geometry=new com.quzzar.vestige.apparatus.LeylineShaping.Geometry(8,
                    com.quzzar.vestige.apparatus.LeylineShaping.Shape.CROSS,pyramid?8:5,pyramid?-6:0,
                    pyramid?com.quzzar.vestige.apparatus.LeylineShaping.Shape.CROSS:com.quzzar.vestige.apparatus.LeylineShaping.Shape.DIAGONAL,pyramid?16:4,pyramid?-6:0);
            BlockPos center=new BlockPos(0,pyramid?77:67,0);
            if (pyramid) for (int x=-16;x<=16;x++) for (int z=-16;z<=16;z++) {
                int top=76-(int)Math.ceil(.75*Math.max(Math.abs(x),Math.abs(z)));
                for (int y=65;y<=top;y++) fixtureBlock(server,new BlockPos(x,y,z),Blocks.STONE_BRICKS);
            }
            fixtureBlock(server,center,stone);

            for (int radius:new int[]{geometry.inner(),geometry.outer()}) {
                boolean diagonal=radius==geometry.outer() && geometry.outerShape()==com.quzzar.vestige.apparatus.LeylineShaping.Shape.DIAGONAL;
                int height=radius==geometry.inner()?geometry.innerHeight():geometry.innerHeight()+geometry.outerStep();
                for (int[] d:diagonal?new int[][]{{1,-1},{1,1},{-1,1},{-1,-1}}:new int[][]{{0,-1},{1,0},{0,1},{-1,0}}) {
                    BlockPos node=center.offset(d[0]*radius,height,d[1]*radius);fixtureBlock(server,node,plinth);
                    if (!pyramid) for (int y=65;y<node.getY();y++) fixtureBlock(server,new BlockPos(node.getX(),y,node.getZ()),Blocks.MOSSY_STONE_BRICKS);
                }
            }
            if (!pyramid) {
                fixtureBlock(server,center.below(),Blocks.CHISELED_STONE_BRICKS);
                for (int x:new int[]{-4,4}) for (int z=-3;z<=3;z++) fixtureBlock(server,new BlockPos(x,66,z),Blocks.MOSSY_STONE_BRICKS);
            }
            var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(center);
            var layout=com.quzzar.vestige.apparatus.RitualCrafting.layout(node);
            if (layout==null || !layout.geometry().equals(geometry)) throw new IllegalStateException("Capture geometry differs from requested layout");
            var recipe=com.quzzar.vestige.apparatus.RitualCrafting.catalog().recipes().get(VestigeMainMod.location(pyramid?"pf2_clairvoyance":"pf2_protector_tree"));
            node.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(recipe.spell()));
            for (var part:recipe.parts()) layout.stands().get(part.seat()).insert(part.ingredient().hint());
            layout.stands().get(0).installMaterial(new ItemStack(Items.IRON_BLOCK));
            layout.stands().get(2).installMaterial(new ItemStack(Items.GOLD_BLOCK));
            com.quzzar.vestige.apparatus.RitualPresentation.connections(layout,200,false);
            camera=pyramid?new Vec3(29,95,34):new Vec3(11,76,13);focus=Vec3.atCenterOf(center).add(0,pyramid?-5:0,0);
            scenario="Registered native leyline nodes in "+(pyramid?"33×33 pyramid, independent -6/-6 height steps":"11×11 Nature henge, flat Cross/Diagonal rings")+"; real ingredient offerings, embedded sockets and server geometry beams. Masonry is decoration; only nodes determine shaping.";
        } else if(job.spell().equals("vestige:plinth_column_head")) {
            for(int i=0;i<4;i++)fixtureBlock(server,new BlockPos(0,65+i,0),plinth);
            var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(0,68,0));
            node.installMaterial(new ItemStack(Items.GOLD_BLOCK));
            camera=new Vec3(1.4,68.8,1.8);focus=new Vec3(.5,68.43,.5);
            scenario="Close native view of the column cap and centered Gold inset protruding half a model unit. Empty columns have their continuous native stone texture; installed material plates appear on every imbued segment.";
        } else if(job.spell().equals("vestige:plinth_column_imbuements")) {
            var imbuements=new net.minecraft.world.item.Item[]{Items.AMETHYST_BLOCK,Items.IRON_BLOCK,Items.LAPIS_BLOCK,Items.GOLD_BLOCK};
            for(int x:new int[]{-1,1})for(int i=0;i<4;i++) {
                var pos=new BlockPos(x,65+i,0);fixtureBlock(server,pos,plinth);
                if(x==1) {
                    var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(pos);
                    if(!node.installMaterial(new ItemStack(imbuements[i])))throw new IllegalStateException("Column segment rejected its independent imbuement");
                }
            }
            camera=new Vec3(4.8,69.3,6.8);focus=new Vec3(.5,66.9,.5);
            scenario="Identical empty and fully imbued columns. The right column holds separate Amethyst, Iron, Lapis and Gold sockets from bottom to top; stone geometry/texture is identical. All segments remain imbueable, including covered shafts.";
        } else if(job.spell().equals("vestige:apparatus_underwater")) {
            for(int x=-3;x<=3;x++)for(int z=-2;z<=2;z++) {
                fixtureBlock(server,new BlockPos(x,64,z),Blocks.STONE_BRICKS);
                for(int y=65;y<=66;y++)fixtureBlock(server,new BlockPos(x,y,z),Blocks.WATER);
            }
            fixtureBlock(server,new BlockPos(-1,65,0),stone.defaultBlockState().setValue(com.quzzar.vestige.apparatus.ApparatusBlock.WATERLOGGED,true));
            fixtureBlock(server,new BlockPos(1,65,0),plinth.defaultBlockState().setValue(com.quzzar.vestige.apparatus.ApparatusBlock.WATERLOGGED,true));
            var center=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(-1,65,0));
            center.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(VestigeMainMod.location("fireball")));
            var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(1,65,0));
            node.installMaterial(new ItemStack(Items.GOLD_BLOCK));
            if(!node.insert(new ItemStack(Items.DIAMOND)))throw new IllegalStateException("Submerged Plinth rejected offering with water above");
            camera=new Vec3(2.8,66.4,1.8);focus=new Vec3(.5,65.4,.5);
            scenario="Waterlogged Spellstone with a flat reference scroll and waterlogged Plinth with a Gold socket and Diamond offering. Water above the Plinth leaves its offering surface usable.";
        } else if(job.spell().equals("vestige:plinth_column_detail")) {
            for(int i=0;i<4;i++)fixtureBlock(server,new BlockPos(0,65+i,0),plinth);
            var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(0,68,0));
            node.installMaterial(new ItemStack(Items.GOLD_BLOCK));node.insert(new ItemStack(Items.DIAMOND));
            camera=new Vec3(3.4,69.3,5.3);focus=new Vec3(.5,66.9,.5);
            scenario="Four stacked Plinths, close view of fixed-scale masonry through block joins. Empty shafts keep the normal texture; only the top has an offering surface.";
        } else if(job.spell().equals("vestige:plinth_columns")) {
            for(int[] column:new int[][]{{-2,1},{0,2},{2,4}}) {
                for(int i=0;i<column[1];i++)fixtureBlock(server,new BlockPos(column[0],65+i,0),plinth);
                var node=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(column[0],65+column[1]-1,0));
                node.installMaterial(new ItemStack(Items.GOLD_BLOCK));node.insert(new ItemStack(Items.DIAMOND));
            }
            camera=new Vec3(6.5,70.2,8);focus=new Vec3(.5,66.7,.5);
            scenario="One, two and four stacked Plinth blocks. Neighbor-derived foot/shaft/cap forms join without internal caps or vertical gaps. Actual Diamond offerings on exposed caps and independent Gold sockets.";
        } else if(job.spell().equals("vestige:apparatus_item_scale")) {
            fixtureBlock(server,new BlockPos(-1,65,0),plinth);
            fixtureBlock(server,new BlockPos(1,65,0),plinth);
            offering(server,new BlockPos(-1,65,0),Items.DIAMOND);
            offering(server,new BlockPos(1,65,0),Items.AMETHYST_BLOCK);
            for (var entry:Map.of(new BlockPos(-1,65,1),Items.DIAMOND,new BlockPos(1,65,1),Items.AMETHYST_BLOCK).entrySet()) {
                var pos=entry.getKey();var dropped=new net.minecraft.world.entity.item.ItemEntity(server.overworld(),pos.getX()+.5,pos.getY()+.05,pos.getZ()+.5,new ItemStack(entry.getValue()));
                dropped.setDeltaMovement(Vec3.ZERO);dropped.setNoGravity(true);dropped.setUnlimitedLifetime();dropped.setNeverPickUp();server.overworld().addFreshEntity(dropped);
            }
            camera=new Vec3(3.5,67.2,5);focus=new Vec3(.5,65.2,1);
            scenario="Diamond and Amethyst Block offerings use unscaled native GROUND transforms; actual vanilla ItemEntity copies in front show the same model scale, with normal dropped-item orientation/bobbing.";
        } else if(job.spell().equals("vestige:apparatus_layout")) {
            fixtureBlock(server,new BlockPos(0,65,0),stone);
            for(int[] p:new int[][]{{0,-2},{2,0},{0,2},{-2,0}})fixtureBlock(server,new BlockPos(p[0],65,p[1]),plinth);
            for(int x:new int[]{-3,3})for(int z:new int[]{-3,3})fixtureBlock(server,new BlockPos(x,65,z),plinth);
            offering(server,new BlockPos(0,65,0),Items.ENCHANTED_BOOK);
            offering(server,new BlockPos(2,65,0),Items.AMETHYST_SHARD);
            offering(server,new BlockPos(-2,65,0),Items.DIAMOND);
            offering(server,new BlockPos(3,65,3),Items.AMETHYST_BLOCK);
            camera=new Vec3(7,71,9);focus=new Vec3(.5,65.25,.5);
            scenario="Visual fixture: center, four inner and four outer Plinths; flat and 3D resting items at native dropped-item scale. Positions match the playable ritual layout.";
        } else {
            var block=job.spell().endsWith(":spellstone")?stone:plinth;
            fixtureBlock(server,new BlockPos(0,65,0),block);
            if (block==plinth) ((com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(0,65,0))).installMaterial(new ItemStack(Items.GOLD_BLOCK));
            camera=block==stone?new Vec3(2,66.45,2.6):new Vec3(1.55,66.65,1.95);
            focus=new Vec3(.5,65+block.offeringHeight()/2,.5);
            scenario="Registered "+material.id()+" "+job.label()+" placed on ordinary terrain, viewed with native Minecraft lighting.";
        }
        Vec3 delta=focus.subtract(camera);
        float cameraYaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z));
        float cameraPitch=(float)Math.toDegrees(Math.atan2(-delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z)));
        apparatusCamera=new CameraPose(camera,cameraYaw,cameraPitch);
        for(ServerPlayer player:server.overworld().players())player.teleportTo(server.overworld(),camera.x,camera.y-player.getEyeHeight(),camera.z,Set.of(),cameraYaw,cameraPitch);
    }

    private void prepareRitual(MinecraftServer server) {
        if (job.spell().endsWith("leyline_henge") || job.spell().endsWith("leyline_pyramid")) {
            prepareApparatus(server);
            ritualCenter=(com.quzzar.vestige.apparatus.OfferingBlockEntity)server.overworld().getBlockEntity(new BlockPos(0,job.spell().endsWith("pyramid")?77:67,0));
            return;
        }
        placeRitual(server,new BlockPos(0,65,0),job.spell());
        boolean spread=job.spell().endsWith("failure_spread");
        Vec3 camera=spread ? new Vec3(.5,93,4) : job.spell().endsWith("reference_success") ? new Vec3(2.6,67.4,3.4) : new Vec3(6.3,71.2,8.6);
        Vec3 focus=new Vec3(.5,spread ? 68 : 65.3,.5),delta=focus.subtract(camera);
        float yaw=(float)Math.toDegrees(Math.atan2(-delta.x,delta.z)),pitch=(float)Math.toDegrees(Math.atan2(-delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z)));
        apparatusCamera=new CameraPose(camera,yaw,pitch);
        for (var player:server.overworld().players()) player.teleportTo(server.overworld(),camera.x,camera.y-player.getEyeHeight(),camera.z,Set.of(),yaw,pitch);
    }

    /** Shared recorded and hands-on fixtures, confined to the newly created capture world. */
    private void placeRitual(MinecraftServer server,BlockPos center,String spell) {
        var material=com.quzzar.vestige.apparatus.ApparatusMaterials.valueOf(System.getProperty("vestige.capture.material","stone_bricks").toUpperCase(java.util.Locale.ROOT));
        var blocks=com.quzzar.vestige.apparatus.ApparatusBlocks.SPELLSTONES.get(material).get(); var level=server.overworld();
        boolean spread=spell.endsWith("failure_spread"),distributed=spread || spell.endsWith("failure_eight");
        boolean advanced=distributed || spell.endsWith("hints") || spell.endsWith("success") || spell.endsWith("attunement") || spell.endsWith("idle");
        fixtureBlock(server,center,blocks);
        var geometry=new com.quzzar.vestige.apparatus.LeylineShaping.Geometry(advanced ? 8 : 4,com.quzzar.vestige.apparatus.LeylineShaping.Shape.CROSS,spread ? 8 : 2,spread ? 3 : 0,
                spread ? com.quzzar.vestige.apparatus.LeylineShaping.Shape.CROSS : com.quzzar.vestige.apparatus.LeylineShaping.Shape.DIAGONAL,spread ? 16 : 3,spread ? 3 : 0);
        for (int i=0;i<8;i++) if (advanced || (i&1)==0) {
            var pos=center.offset(geometry.offset(i));
            if(spread) {
                for(int y=center.getY();y<pos.getY()-1;y++)fixtureBlock(server,new BlockPos(pos.getX(),y,pos.getZ()),Blocks.STONE_BRICKS);
                for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)fixtureBlock(server,pos.offset(x,-1,z),Blocks.SMOOTH_STONE);
            }
            fixtureBlock(server,pos,com.quzzar.vestige.apparatus.ApparatusBlocks.PLINTHS.get(material).get());
        }
        ritualCenter=(com.quzzar.vestige.apparatus.OfferingBlockEntity)level.getBlockEntity(center);
        var layout=com.quzzar.vestige.apparatus.RitualCrafting.layout(ritualCenter);
        var recipe=com.quzzar.vestige.apparatus.RitualCrafting.catalog().recipes().get(VestigeMainMod.location(advanced || spell.endsWith("capacity") ? "pf2_flicker" : "fireball"));
        if(spell.endsWith("capacity")) {
            ritualCenter.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(recipe.spell()));
            layout.stands().get(0).insert(new ItemStack(Items.PAPER));
            layout.stands().get(2).insert(new ItemStack(Items.CLOCK));
            scenario="Eight-slot Flicker reference on four inner Plinths. The pale rune ring reaches beyond the existing nodes, then collapses; offerings and reference remain untouched.";
        } else if(distributed) {
            ritualCenter.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(recipe.spell()));
            for(var part:recipe.parts())layout.stands().get(part.seat()).insert(new ItemStack(Items.DIRT));
            label(villager(server,center.getX()+1.5,center.getY(),center.getZ()+1.5),"Center blast");
            int empty=java.util.stream.IntStream.range(0,8).filter(i->(i&1)==1 && recipe.parts().stream().noneMatch(p->p.seat()==i)).findFirst().orElseThrow();
            var offset=geometry.offset(empty);var pos=center.offset(offset);
            Vec3 side=new Vec3(-offset.getZ(),0,offset.getX()).normalize().scale(1.25);
            label(villager(server,pos.getX()+.5+side.x,pos.getY(),pos.getZ()+.5+side.z),"Empty outer Plinth");
            label(villager(server,center.getX()+(spread ? 4.5 : 6.5),center.getY(),center.getZ()+.5),spread ? "Safe gap" : "Outside all bursts");
            scenario="Real eight-slot failure: four-block Spellstone blast and two-block local bursts at all eight active Plinths, including three empty recipe positions. "+
                    (spread ? "Inner/outer radii 8/16 at heights +3/+6; empty-node creature is hurt while the distant gap stays safe." : "Compact Cross/Diagonal layout; overlapping damage resolves to one strongest hit.");
        } else if (spell.endsWith("attunement")) {
            var ingredients=com.quzzar.vestige.apparatus.AttunementShardItem.ingredients();
            for(int i=0;i<8;i++) {
                if(i<ingredients.size())layout.stands().get(i).insert(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))));
                layout.stands().get(i).installMaterial(new ItemStack(i%2==0 ? Items.COPPER_BLOCK : Items.IRON_BLOCK));
            }
            scenario="Six fixed offerings, including two separate Amethyst Shards, rise and form one keyed Attunement Shard. Two offering seats stay empty; all eight nodes and their paired sockets determine its reproducible blueprint.";
        } else if(spell.endsWith("spellshaping")) {
            for(var part:recipe.parts())layout.stands().get(part.seat()*2).insert(part.ingredient().hint());
            layout.stands().get(2).installMaterial(new ItemStack(Items.END_STONE));layout.stands().get(4).installMaterial(new ItemStack(Items.BONE_BLOCK));layout.stands().get(6).installMaterial(new ItemStack(Items.COPPER_BLOCK));
            ritualCenter.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(recipe.spell()));
            scenario="Unchanged Fireball recipe automatically creates Bleeding Reaching Shocking Fireball; installed materials and the flat reference remain.";
        } else if (spell.endsWith("hints")) {
            ritualCenter.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(recipe.spell()));
            layout.stands().get(0).insert(new ItemStack(Items.PAPER));
            layout.stands().get(2).insert(new ItemStack(Items.ENDER_PEARL)); // Belongs in the outer layer.
            layout.stands().get(7).insert(new ItemStack(Items.CLOCK)); // Belongs in the inner layer.
            layout.stands().get(4).insert(new ItemStack(Items.PHANTOM_MEMBRANE)); // Same layer, wrong position.
            scenario="Real server inspection: flat reference scroll, Paper correct, item-shaped colored shadow for missing membrane; all wrong placements shake sideways without vertical pedestal movement.";
        } else if (spell.endsWith("success") || spell.endsWith("idle")) {
            for (var part:recipe.parts()) layout.stands().get(part.seat()).insert(part.ingredient().hint());
            if (spell.endsWith("reference_success") || spell.endsWith("idle")) {
                ritualCenter.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(recipe.spell()));
                scenario="Exact five-part Flicker recipe: ingredients rise above stationary pedestals; the reference stays flat and the finished scroll drops exactly above the center for ordinary pickup.";
            } else scenario="Exact five-part Flicker recipe with no reference scroll: ingredients rise above stationary pedestals, are consumed and a native scroll drops exactly above the center with no sideways toss.";
        } else if (spell.endsWith("discovery")) {
            for (int i:new int[]{0,2,4,6}) layout.stands().get(i).insert(com.quzzar.vestige.apparatus.ScrollItems.fragment(VestigeMainMod.location(i==0 ? "evocation" : "fire")));
            scenario="Three Fire fragments and one Evocation fragment roll a scroll from the intersection with slot-counted average weighting.";
        } else {
            ritualCenter.insert(com.quzzar.vestige.apparatus.ScrollItems.scroll(recipe.spell()));
            for (int i:new int[]{0,2,4,6}) layout.stands().get(i).insert(new ItemStack(Items.DIRT));
            if(spell.endsWith("failure")) {
                label(villager(server,center.getX()+1.5,center.getY(),center.getZ()+1.5),"Within blast");
                label(villager(server,center.getX()+6.5,center.getY(),center.getZ()+.5),"Outside blast");
            }
            scenario="Completed wrong unknown Fireball arrangement: ordinary seeded gameplay roll schedules a blast, destroys ingredients, hurts nearby villagers and preserves terrain/reference.";
        }
    }

    /** Leave four unactivated stations and spare ingredients for the owner's manual review. */
    public void review(MinecraftServer server) {
        prepare(server,new Job("vestige:ritual_idle",0,"Hands-on review","ritual"));
        reviewLabel(server,new BlockPos(0,65,0),"Ready to craft");
        placeRitual(server,new BlockPos(12,65,0),"vestige:ritual_hints");
        reviewLabel(server,new BlockPos(12,65,0),"Missing and misplaced items");
        placeRitual(server,new BlockPos(-12,65,0),"vestige:ritual_wrong");
        reviewLabel(server,new BlockPos(-12,65,0),"Wrong recipe: may backfire");
        placeRitual(server,new BlockPos(0,65,12),"vestige:ritual_discovery");
        reviewLabel(server,new BlockPos(0,65,12),"Fragment discovery");
        var recipe=com.quzzar.vestige.apparatus.RitualCrafting.catalog().recipes().get(VestigeMainMod.location("pf2_flicker"));
        for(var player:server.getPlayerList().getPlayers()) {
            player.setGameMode(GameType.CREATIVE);
            for(var part:recipe.parts())player.getInventory().add(part.ingredient().hint().copyWithCount(16));
            player.getInventory().selected=8;
            player.getAbilities().flying=true;player.onUpdateAbilities();
            player.teleportTo(server.overworld(),4.5,66,7,Set.of(),145,20);
        }
    }

    private void reviewLabel(MinecraftServer server,BlockPos center,String text) {
        var marker=EntityType.ARMOR_STAND.create(server.overworld());
        if(marker==null)throw new IllegalStateException("Review label could not be created");
        marker.setInvisible(true);marker.setNoGravity(true);
        marker.setCustomName(net.minecraft.network.chat.Component.literal(text));marker.setCustomNameVisible(true);
        marker.setPos(center.getX()+.5,center.getY()+1.25,center.getZ()+.5);
        server.overworld().addFreshEntity(marker);
    }

    private void offering(MinecraftServer server,BlockPos pos,net.minecraft.world.item.Item item) {
        var entity=server.overworld().getBlockEntity(pos);
        if(!(entity instanceof com.quzzar.vestige.apparatus.OfferingBlockEntity stand) || !stand.insert(new ItemStack(item)))
            throw new IllegalStateException("Apparatus offering fixture failed at "+pos);
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
