package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.condition.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.presentation.SpellVisual;
import com.quzzar.vestige.magic.presentation.SpellVisualPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Predicate;

/** Native Minecraft adapter, independent of the retired Wizardry gameplay layer. */
public final class MinecraftSpellWorld implements SpellWorld {
    final MinecraftServer server;
    final Map<UUID, UUID> owners = new HashMap<>();
    final Map<UUID, CausalChain> causedEntities = new HashMap<>();
    private final Map<UUID, WeakReference<LivingEntity>> actors = new HashMap<>();
    final SpellActions actions;
    private final SpellManifestations manifestations;
    private final Set<ManifestationHandle> magicHandles = Collections.newSetFromMap(new IdentityHashMap<>());
    final ServerSpellVisuals visuals = new ServerSpellVisuals();
    final NativeSpellFeatures features;
    CausalChain executingCause;

    public MinecraftSpellWorld(MinecraftServer server) {
        this.server = Objects.requireNonNull(server);
        actions = new SpellActions(this); manifestations = new SpellManifestations(this); features = new NativeSpellFeatures(this);
    }
    public void registerActor(LivingEntity actor) { actors.put(actor.getUUID(), new WeakReference<>(actor)); }
    public boolean isOwnedBy(Entity entity, UUID actor) { return actor.equals(owners.get(entity.getUUID())); }
    boolean magical(net.minecraft.world.damagesource.DamageSource source) {
        return executingCause != null || source.getDirectEntity() != null && causedEntities.containsKey(source.getDirectEntity().getUUID())
                || source.is(net.minecraft.tags.DamageTypeTags.WITCH_RESISTANT_TO);
    }
    public CausalChain cause(Entity source) {
        return source != null && causedEntities.containsKey(source.getUUID()) ? causedEntities.get(source.getUUID())
                : executingCause == null ? CausalChain.start() : executingCause;
    }
    Entity entity(UUID id) {
        WeakReference<LivingEntity> reference = actors.get(id);
        LivingEntity actor = reference == null ? null : reference.get();
        if (actor != null && !actor.isRemoved()) return actor;
        actors.remove(id);
        for (ServerLevel level : server.getAllLevels()) { Entity entity = level.getEntity(id); if (entity != null) return entity; }
        return server.getPlayerList().getPlayer(id);
    }
    LivingEntity actor(SpellRuntime.Context context) { return entity(context.actor()) instanceof LivingEntity living ? living : null; }
    Entity target(SpellRuntime.Context context) { return context.target() instanceof SpellSubject.Entity e ? entity(e.id()) : null; }
    ServerLevel level(SpellSubject subject, LivingEntity fallback) {
        if (subject instanceof SpellSubject.Block b) return server.getLevel(b.dimension());
        if (subject instanceof SpellSubject.Position p) return server.getLevel(p.dimension());
        if (subject instanceof SpellSubject.Entity e && entity(e.id()) != null) return (ServerLevel) entity(e.id()).level();
        return (ServerLevel) fallback.level();
    }
    Vec3 position(SpellSubject subject, LivingEntity fallback) {
        if (subject instanceof SpellSubject.Block b) return b.position().getCenter();
        if (subject instanceof SpellSubject.Position p) return p.position();
        if (subject instanceof SpellSubject.Entity e && entity(e.id()) != null) return entity(e.id()).position();
        return fallback.position();
    }
    BlockEntity block(SpellSubject subject, LivingEntity fallback) {
        ServerLevel level = level(subject, fallback); BlockPos pos = BlockPos.containing(position(subject, fallback));
        return level != null && level.hasChunkAt(pos) ? level.getBlockEntity(pos) : null;
    }
    boolean ally(LivingEntity caster, Entity other) {
        return other == caster || caster.isAlliedTo(other) || caster.getUUID().equals(owners.get(other.getUUID()))
                || other instanceof OwnableEntity own && caster.getUUID().equals(own.getOwnerUUID());
    }
    boolean relation(TargetSpec.Relationship relation, LivingEntity caster, LivingEntity target) {
        return switch (relation) {
            case ANY -> target != caster;
            case ALLY -> ally(caster, target);
            case HOSTILE -> !ally(caster, target);
            case OWNED -> caster.getUUID().equals(owners.get(target.getUUID()));
        };
    }
    @Override public boolean active(SpellRuntime.Context context) { LivingEntity caster = actor(context); return caster != null && caster.isAlive() && !caster.isRemoved(); }
    @Override public boolean canActivate(UUID actor, SpellDefinition spell) {
        return !features.absent(actor) && !features.remote(actor)
                && (!(entity(actor) instanceof LivingEntity caster) || !com.quzzar.vestige.magic.effect.SpellCapabilities.of(spell).contains(ResourceLocation.parse("vestige:utterance"))
                || !features.silent(caster.position(),(ServerLevel)caster.level()));
    }
    @Override public ConditionContext conditions(SpellRuntime.Context context) {
        return new ConditionContext() {
            @Override public Optional<ConditionValue> value(ResourceLocation path) {
                LivingEntity caster = actor(context); Entity subject = target(context);
                if (caster == null) return Optional.empty();
                String name = path.toString();
                if (name.equals("vestige:actor/entity_type")) return identifier(BuiltInRegistries.ENTITY_TYPE.getKey(caster.getType()));
                if (path.equals(ConditionPaths.ACTOR_HEALTH)) return decimal(caster.getHealth());
                if (path.equals(ConditionPaths.ACTOR_MAX_HEALTH)) return decimal(caster.getMaxHealth());
                if (path.equals(ConditionPaths.ACTOR_HEALTH_PERCENT)) return decimal(caster.getHealth() / caster.getMaxHealth());
                if (path.equals(ConditionPaths.ACTOR_ALIVE)) return flag(caster.isAlive());
                if (path.equals(ConditionPaths.ACTOR_CROUCHING)) return flag(caster.isCrouching());
                if (path.equals(ConditionPaths.ACTOR_BURNING)) return flag(caster.isOnFire());
                if (path.equals(ConditionPaths.ACTOR_WET)) return flag(caster.isInWaterRainOrBubble());
                if (name.equals("vestige:actor/weapon_damage")) return decimal(caster.getAttribute(Attributes.ATTACK_DAMAGE) == null ? 0 : caster.getAttributeValue(Attributes.ATTACK_DAMAGE));
                if (path.equals(ConditionPaths.SOURCE_ITEM)) return identifier(BuiltInRegistries.ITEM.getKey(caster.getMainHandItem().getItem()));
                if (path.equals(ConditionPaths.SOURCE_COUNT)) return decimal(caster.getMainHandItem().getCount());
                if (path.equals(ConditionPaths.SOURCE_CUSTOM_NAME) && caster.getMainHandItem().has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) return Optional.of(new ConditionValue.Text(caster.getMainHandItem().getHoverName().getString()));
                if (path.equals(ConditionPaths.ACTOR_MANA)) return decimal(caster.getPersistentData().getDouble("vestige:mana"));
                if (path.equals(ConditionPaths.TARGET_ENTITY_TYPE) && subject != null) return identifier(BuiltInRegistries.ENTITY_TYPE.getKey(subject.getType()));
                if (path.equals(ConditionPaths.TARGET_HEALTH) && subject instanceof LivingEntity living) return decimal(living.getHealth());
                if (name.equals("vestige:target/max_health") && subject instanceof LivingEntity living) return decimal(living.getMaxHealth());
                if (name.equals("vestige:target/distance") && subject != null) return decimal(caster.distanceTo(subject));
                if (name.equals("vestige:target/owned")) return flag(subject != null && caster.getUUID().equals(owners.get(subject.getUUID())));
                if (name.startsWith("vestige:target/status/") && subject instanceof LivingEntity living) {
                    String[] parts = name.substring("vestige:target/status/".length()).split("/", 2);
                    if (parts.length == 2) return flag(BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(parts[0], parts[1])).map(living::hasEffect).orElse(false));
                }
                if (path.equals(ConditionPaths.TARGET_ALIVE) && subject != null) return flag(subject.isAlive());
                if (path.equals(ConditionPaths.DAMAGE_AMOUNT) && context.event().pending().isPresent()) return decimal(context.event().pending().get().amount());
                if (path.equals(ConditionPaths.DIMENSION)) return identifier(caster.level().dimension().location());
                if (path.equals(ConditionPaths.GAME_TIME)) return decimal(caster.level().getGameTime());
                if (name.equals("vestige:block/locked_by_actor")) {
                    BlockEntity block = block(context.target(), caster);
                    return flag(block != null && context.actor().toString().equals(block.getPersistentData().getString("vestige:lock_owner")));
                }
                if (path.equals(ConditionPaths.BLOCK_ID)) {
                    ServerLevel level = level(context.target(), caster);
                    BlockPos pos = BlockPos.containing(position(context.target(), caster));
                    if (level != null && level.hasChunkAt(pos)) return identifier(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()));
                }
                return Optional.empty();
            }
            @Override public boolean isTagged(ResourceLocation path, ResourceLocation tag) {
                LivingEntity caster = actor(context); if (caster == null) return false;
                if (path.equals(ConditionPaths.SOURCE_ITEM)) return caster.getMainHandItem().is(TagKey.create(Registries.ITEM, tag));
                Entity subject = path.equals(ConditionPaths.ACTOR_ENTITY_TYPE) ? caster : target(context);
                return subject != null && subject.getType().is(TagKey.create(Registries.ENTITY_TYPE, tag));
            }
            @Override public boolean matches(ResourceLocation path, ResourceLocation predicate) { return false; }
            @Override public double random() { LivingEntity caster = actor(context); return caster == null ? 0 : caster.getRandom().nextDouble(); }
        };
    }
    @Override public List<SpellSubject> select(TargetSpec spec, SpellRuntime.Context context) {
        LivingEntity caster = actor(context); if (caster == null) return List.of();
        double distance = context.number(spec.distance());
        if (distance < 0 || distance > 128) throw new IllegalArgumentException("Target distance must be between 0 and 128");
        ServerLevel level = (ServerLevel) caster.level(); Vec3 start = caster.getEyePosition(); Vec3 end = start.add(caster.getLookAngle().scale(distance));
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        boolean through = option(spec, context, "through_blocks", 0) > 0;
        Vec3 visibleEnd = through ? end : blockHit.getLocation();
        Predicate<LivingEntity> filter = e -> !features.obscured(caster.position(), e.position(), level) && (e.isAlive() && relation(spec.relationship(), caster, e) || e == caster && e.isAlive() && spec.relationship() == TargetSpec.Relationship.ANY && option(spec, context, "include_self", 0) > 0)
                && !(option(spec, context, "exclude_origin", 0) > 0 && context.origin().filter(s -> s instanceof SpellSubject.Entity subject && subject.id().equals(e.getUUID())).isPresent())
                && (option(spec,context,"consenting",0)==0 || features.consent(caster,e));
        switch (spec.selection()) {
            case SELF: return List.of(new SpellSubject.Entity(caster.getUUID()));
            case CURRENT: return List.of(context.target());
            case EVENT_TARGET: return context.event().target().map(List::of).orElseGet(List::of);
            case EVENT_ATTACKER: {
                var value = context.event().facts().get(ResourceLocation.parse("vestige:event/attacker"));
                if (!(value instanceof ConditionValue.Text text) || text.value().isEmpty()) return List.of();
                try {
                    Entity attacker = entity(UUID.fromString(text.value()));
                    return attacker instanceof LivingEntity living && filter.test(living) && (distance == 0 || living.distanceTo(caster) <= distance)
                            ? List.of(new SpellSubject.Entity(living.getUUID())) : List.of();
                } catch (IllegalArgumentException ignored) { return List.of(); }
            }
            case STORED_TARGET: return context.anchor(ResourceLocation.parse("vestige:target_anchor")).map(List::of).orElseGet(List::of);
            case ANY_ENTITY_RAY: {
                return level.getEntities(caster, new AABB(start, visibleEnd).inflate(1), e -> !e.isRemoved() && e.getBoundingBox().inflate(0.3).clip(start, visibleEnd).isPresent()).stream()
                        .min(Comparator.comparingDouble(e -> e.distanceToSqr(caster))).map(e -> List.<SpellSubject>of(new SpellSubject.Entity(e.getUUID()))).orElseGet(List::of);
            }
            case BLOCK_RAY: return blockHit.getType() == HitResult.Type.BLOCK ? List.of(new SpellSubject.Block(level.dimension(), blockHit.getBlockPos())) : List.of();
            case AIMED_POSITION: return List.of(new SpellSubject.Position(level.dimension(), blockHit.getLocation()));
            case NEAR_TARGET, NEARBY_ENTITIES, MELEE, CONE: {
                Vec3 center = spec.selection() == TargetSpec.Selection.NEAR_TARGET ? position(context.target(), caster) : caster.position();
                ServerLevel targetLevel = spec.selection() == TargetSpec.Selection.NEAR_TARGET ? level(context.target(), caster) : level;
                if (targetLevel == null) return List.of();
                List<LivingEntity> targets = targetLevel.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(distance), filter);
                double cosine = Math.cos(Math.toRadians(option(spec, context, "angle", 45)));
                return targets.stream().filter(e -> e.position().distanceToSqr(center) <= distance * distance)
                        .filter(e -> spec.selection() != TargetSpec.Selection.CONE && spec.selection() != TargetSpec.Selection.MELEE || e.position().subtract(caster.position()).normalize().dot(caster.getLookAngle()) >= cosine)
                        .filter(e -> through || spec.selection() == TargetSpec.Selection.NEAR_TARGET && option(spec, context, "line_of_sight", 0) == 0 || caster.hasLineOfSight(e))
                        .sorted(Comparator.comparingDouble(e -> e.position().distanceToSqr(center))).limit((long) Math.max(0, option(spec, context, "count", 128))).map(e -> (SpellSubject) new SpellSubject.Entity(e.getUUID())).toList();
            }
            default: {
                double radius = option(spec, context, "radius", spec.selection() == TargetSpec.Selection.BEAM ? 0.75 : 0.25);
                List<LivingEntity> hit = new ArrayList<>(level.getEntitiesOfClass(LivingEntity.class, new AABB(start, visibleEnd).inflate(radius + 1), filter));
                hit.removeIf(e -> e.getBoundingBox().inflate(radius).clip(start, visibleEnd).isEmpty());
                hit.sort(Comparator.comparingDouble(e -> e.position().distanceToSqr(start)));
                if (spec.selection() == TargetSpec.Selection.ENTITY_RAY) return hit.isEmpty() ? List.of() : List.of(new SpellSubject.Entity(hit.getFirst().getUUID()));
                if (spec.selection() == TargetSpec.Selection.CHAIN && !hit.isEmpty()) {
                    List<LivingEntity> chain = new ArrayList<>(); chain.add(hit.getFirst());
                    int count = (int) Math.min(32, option(spec, context, "count", 4)); double jump = option(spec, context, "jump", 6);
                    while (chain.size() < count) {
                        LivingEntity last = chain.getLast();
                        var next = level.getEntitiesOfClass(LivingEntity.class, last.getBoundingBox().inflate(jump), filter).stream()
                                .filter(e -> !chain.contains(e) && e.distanceToSqr(last) <= jump * jump && last.hasLineOfSight(e))
                                .min(Comparator.comparingDouble(e -> e.distanceToSqr(last))).orElse(null);
                        if (next == null) break; chain.add(next);
                    }
                    hit = chain;
                }
                return hit.stream().limit((long) Math.max(0, option(spec, context, "count", 128)))
                        .map(e -> (SpellSubject) new SpellSubject.Entity(e.getUUID())).toList();
            }
        }
    }
    private static double option(TargetSpec spec, SpellRuntime.Context context, String key, double fallback) {
        return spec.options().containsKey(key) ? context.number(spec.options().get(key)) : fallback;
    }
    @Override public boolean pay(List<SpellCost> costs, SpellRuntime.Context context) {
        LivingEntity caster = actor(context); if (caster == null) return false;
        if (caster instanceof Player player && player.isCreative()) return true;
        double health = 0, mana = 0; long hunger = 0;
        List<ItemStack> inventory = caster instanceof Player p ? p.getInventory().items.stream().map(ItemStack::copy).toList() : List.of();
        for (SpellCost cost : costs) {
            switch (cost) {
                case SpellCost.Time ignored -> { }
                case SpellCost.Cooldown ignored -> { }
                case SpellCost.Mana amount -> mana += amount.amount();
                case SpellCost.Health amount -> health += amount.amount();
                case SpellCost.Hunger amount -> hunger += amount.amount();
                case SpellCost.Material material -> {
                    int remaining = material.amount();
                    for (ItemStack stack : inventory) {
                        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(material.item())) continue;
                        if (material.operation() == SpellCost.Material.Operation.CONSUME) { int used = Math.min(remaining, stack.getCount()); stack.shrink(used); remaining -= used; }
                        else if (stack.isDamageableItem()) { int used = Math.min(remaining, stack.getMaxDamage() - stack.getDamageValue()); stack.setDamageValue(stack.getDamageValue() + used); remaining -= used; if (stack.getDamageValue() >= stack.getMaxDamage()) stack.shrink(1); }
                        if (remaining == 0) break;
                    }
                    if (remaining > 0) return false;
                }
            }
        }
        if (!Double.isFinite(health) || caster.getHealth() <= health || !Double.isFinite(mana) || caster.getPersistentData().getDouble("vestige:mana") < mana || hunger > 0 && (!(caster instanceof Player p) || p.getFoodData().getFoodLevel() < hunger)) return false;
        if (caster instanceof Player p) for (int i = 0; i < inventory.size(); i++) p.getInventory().setItem(i, inventory.get(i));
        if (health > 0) caster.setHealth((float) (caster.getHealth() - health));
        if (mana > 0) caster.getPersistentData().putDouble("vestige:mana", caster.getPersistentData().getDouble("vestige:mana") - mana);
        if (hunger > 0 && caster instanceof Player p) p.getFoodData().setFoodLevel(p.getFoodData().getFoodLevel() - (int) hunger);
        return true;
    }
    @Override public void forfeit(SpellRuntime.Context context) {
        LivingEntity caster = actor(context); if (caster == null) return;
        var effects = List.of(MobEffects.WEAKNESS, MobEffects.CONFUSION, MobEffects.MOVEMENT_SLOWDOWN);
        caster.addEffect(new MobEffectInstance(effects.get(caster.getRandom().nextInt(effects.size())), 200));
    }
    @Override public boolean execute(SpellEffects.Action action, SpellRuntime.Context context) {
        CausalChain previous = executingCause; executingCause = context.cause();
        try { return actions.execute(action, context); } finally { executingCause = previous; }
    }
    @Override public Optional<ManifestationHandle> manifest(SpellEffects.Manifestation definition, Map<String, Double> values, SpellRuntime.Context context) {
        return manifestations.create(definition, values, context).map(handle -> {
            UUID started = null;
            if (definition.visual().isPresent() && !definition.kind().getPath().equals("projectile")) {
                SpellVisual visual = definition.visual().get(); LivingEntity caster = actor(context);
                ServerLevel level = level(handle.subject(), caster);
                Entity attached = handle.subject() instanceof SpellSubject.Entity e ? entity(e.id()) : null;
                var point = visualPoint(handle.subject(), caster, visual.height());
                started = visuals.start(level, visual, context.number(visual.radius()), true,
                        () -> List.of(attached == null ? point : visualPoint(attached, visual.height())),
                        () -> handle.alive() && (attached == null || attached.level() == level && !attached.isRemoved()),
                        values.getOrDefault("private_visual",0d)>0 ? Optional.of(context.actor()) : Optional.empty());
            }
            UUID cue = started;
            magicHandles.add(handle);
            return new ManifestationHandle() {
                public SpellSubject subject() { return handle.subject(); }
                public boolean alive() { return handle.alive(); }
                public void tick() { handle.tick(); }
                public void bindingsExhausted(boolean consumed) {
                    handle.bindingsExhausted(consumed);
                    if (definition.visual().map(SpellVisual::endsWithBindings).orElse(false)) visuals.stop(cue, consumed);
                }
                public void close(SpellRuntime.EndReason reason) { magicHandles.remove(handle); visuals.stop(cue, false); handle.close(reason); }
            };
        });
    }
    @Override public void present(SpellVisual visual, List<SpellSubject> points, SpellRuntime.Context context) {
        LivingEntity caster = actor(context); if (caster == null || points.isEmpty()) return;
        ServerLevel level = level(points.getLast(), caster); if (level == null) return;
        var resolved = points.stream().map(subject -> visualPoint(subject, caster, visual.height())).toList();
        visuals.start(level, visual, context.number(visual.radius()), false, () -> resolved, () -> true);
    }
    static SpellVisualPayload.Point visualPoint(Entity entity, double height) {
        return new SpellVisualPayload.Point(entity.position().add(0, height, 0), entity.getId(), entity.getUUID(), (float) height);
    }
    private SpellVisualPayload.Point visualPoint(SpellSubject subject, LivingEntity caster, double height) {
        if (subject instanceof SpellSubject.Entity e && entity(e.id()) != null) return visualPoint(entity(e.id()), height);
        return new SpellVisualPayload.Point(position(subject, caster).add(0, height, 0), -1, new UUID(0, 0), (float) height);
    }
    /** Immutable resolved cues for development inspection and gameplay-to-presentation tests. */
    public List<SpellVisualPayload> visualCues() { return visuals.snapshot(); }
    public void tick() {
        visuals.tick();
        features.tickAnimations();
        for (var entry : List.copyOf(owners.entrySet())) {
            if (!(entity(entry.getKey()) instanceof Mob minion) || !(entity(entry.getValue()) instanceof LivingEntity owner)) continue;
            if (minion.getTarget() != null && ally(owner, minion.getTarget())) minion.setTarget(null);
            LivingEntity target = owner.getLastHurtByMob();
            if (target == null || !target.isAlive() || ally(owner, target)) target = owner.getLastHurtMob();
            if (target == null || !target.isAlive() || ally(owner, target)) target = minion.level().getEntitiesOfClass(LivingEntity.class, minion.getBoundingBox().inflate(12), e -> e instanceof Enemy && e.isAlive() && !ally(owner, e)).stream().findFirst().orElse(null);
            if (target != null && target.isAlive()) minion.setTarget(target);
            else if (minion.distanceToSqr(owner) > 16) minion.getNavigation().moveTo(owner, 1.1);
        }
    }
    boolean hasNativeMagic(LivingEntity caster, double radius) {
        for (ManifestationHandle handle : magicHandles) {
            if (handle.alive() && level(handle.subject(), caster) == caster.level()
                    && position(handle.subject(), caster).distanceToSqr(caster.position()) <= radius * radius) return true;
        }
        return caster.level().getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e.distanceToSqr(caster) <= radius * radius
                        && e.getAllSlots().iterator().hasNext()
                        && java.util.stream.StreamSupport.stream(e.getAllSlots().spliterator(), false).anyMatch(ItemStack::isEnchanted)).size() > 0;
    }
    public void close() { magicHandles.clear(); features.close(); visuals.close(); actions.close(); }
    private static Optional<ConditionValue> decimal(double value) { return Optional.of(new ConditionValue.Decimal(value)); }
    private static Optional<ConditionValue> flag(boolean value) { return Optional.of(new ConditionValue.Flag(value)); }
    private static Optional<ConditionValue> identifier(ResourceLocation value) { return Optional.of(new ConditionValue.Identifier(value)); }
}
