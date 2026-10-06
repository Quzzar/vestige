package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.condition.ConditionContext;
import com.quzzar.vestige.magic.condition.ConditionPaths;
import com.quzzar.vestige.magic.condition.ConditionValue;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Server-thread runtime for casts, delayed plans, bindings, and manifestations.
 * Immutable definitions are shared; each cast owns its resolved traits and state.
 */
public final class SpellRuntime implements AutoCloseable {
    private static final int MAX_STEPS_PER_TASK = 4096;
    private final SpellWorld world;
    private final Map<UUID, Cast> casts = new LinkedHashMap<>();
    private final Map<UUID, ActiveManifestation> manifestations = new LinkedHashMap<>();
    private final Map<UUID, ActiveBinding> bindings = new LinkedHashMap<>();
    private record Recovery(UUID actor, ResourceLocation spell) { }
    private final Map<Recovery, Long> cooldowns = new HashMap<>();
    private final PriorityQueue<Task> tasks = new PriorityQueue<>(Comparator.comparingLong((Task t) -> t.due).thenComparingLong(t -> t.order));
    private long tick;
    private long order;
    private boolean closed;

    public SpellRuntime(SpellWorld world) { this.world = Objects.requireNonNull(world); }

    public Cast cast(SpellDefinition spell, SpellEvent event, Collection<TraitModifier> modifiers, boolean discovered) {
        return cast(spell, event, modifiers, discovered, Optional.empty());
    }

    public Cast cast(SpellDefinition spell, SpellEvent event, Collection<TraitModifier> modifiers,
                     boolean discovered, Optional<ResourceLocation> mode) {
        return cast(spell, event, modifiers, discovered, mode, false);
    }

    /** Resource bypass is restricted to explicit development callers; time still executes normally. */
    public Cast cast(SpellDefinition spell, SpellEvent event, Collection<TraitModifier> modifiers,
                     boolean discovered, Optional<ResourceLocation> mode, boolean freeResources) {
        return cast(spell,event,modifiers,discovered,mode,freeResources,CastShaping.NONE);
    }
    public Cast cast(SpellDefinition spell, SpellEvent event, Collection<TraitModifier> modifiers,
                     boolean discovered, Optional<ResourceLocation> mode, boolean freeResources, CastShaping shaping) {
        Objects.requireNonNull(shaping);
        ensureOpen();
        if (!world.canActivate(event.actor(),spell)) return rejected(spell,event,Status.CONDITIONS_FAILED);
        if (mode.isPresent() && !spell.modes().containsKey(mode.get())) return rejected(spell, event, Status.CONDITIONS_FAILED);
        // One charge/channel at a time. Dormant recast sessions do not block other spells.
        if (casts.values().stream().anyMatch(c -> c.actor.equals(event.actor())
                && !c.spell.id().equals(spell.id()) && c.status != Status.AWAITING_RECAST)) {
            return rejected(spell, event, Status.BUSY);
        }
        for (Cast existing : List.copyOf(casts.values())) {
            if (existing.actor.equals(event.actor()) && existing.spell.id().equals(spell.id())) {
                if (!existing.mode.equals(mode) || existing.status != Status.AWAITING_RECAST) return rejected(spell, event, Status.BUSY);
                if (!event.type().equals(existing.context.event.type())) return rejected(spell, event, Status.CONDITIONS_FAILED);
                Context recastContext = existing.context.withEvent(event);
                if (existing.spell.triggers().stream().filter(t -> t.event().equals(event.type()))
                        .noneMatch(t -> t.conditions().stream().allMatch(c -> c.matches(recastContext)))) {
                    return rejected(spell, event, Status.CONDITIONS_FAILED);
                }
                Task continuation = existing.continuation;
                existing.continuation = null;
                existing.status = Status.RUNNING;
                continuation.steps.replaceAll(step -> new Step(step.effect, step.context.withEvent(event)));
                continuation.due = tick;
                process(continuation);
                return existing;
            }
        }
        if (!freeResources && cooldowns.getOrDefault(new Recovery(event.actor(), spell.id()), 0L) > tick) {
            return rejected(spell, event, Status.COOLDOWN);
        }
        Cast cast = new Cast(spell, event.actor(), spell.traits().resolve(modifiers), discovered);
        cast.mode = mode;
        cast.freeResources = freeResources;
        cast.shaping = shaping;
        cast.costs = shaping.costs(mode.map(spell.modes()::get).map(SpellMode::costs).orElse(spell.costs()));
        List<SpellEffect> effects = mode.map(spell.modes()::get).map(SpellMode::effects).orElse(spell.effects());
        Context triggerContext = new Context(cast, event, new SpellSubject.Entity(event.actor()), event.cause(), null);
        Optional<SpellTrigger> trigger = spell.triggers().stream()
                .filter(t -> t.event().equals(event.type()))
                .filter(t -> t.conditions().stream().allMatch(c -> c.matches(triggerContext))).findFirst();
        if (trigger.isEmpty()) { cast.status = Status.CONDITIONS_FAILED; return cast; }
        Optional<CausalChain> branch = event.cause().enter(new CausalChain.ActivationKey(spell.id(), trigger.get().id()));
        if (branch.isEmpty()) { cast.status = Status.LOOP_REJECTED; return cast; }
        Context initial = new Context(cast, event, triggerContext.target, branch.get(), null);
        if (!world.active(initial)) { cast.status = Status.INTERRUPTED; return cast; }
        long chargeTicks = cast.costs.stream().filter(SpellCost.Time.class::isInstance)
                .map(SpellCost.Time.class::cast).mapToLong(SpellCost.Time::ticks).sum();
        if (chargeTicks > 0 && event.pending().isPresent()) { cast.status = Status.CONDITIONS_FAILED; return cast; }
        cast.context = initial;
        casts.put(cast.id, cast);
        Task task = task(cast, effects, initial);
        task.needsPayment = true;
        if (chargeTicks > 0) {
            cast.status = Status.CHARGING;
            task.due = Math.addExact(tick, chargeTicks);
            tasks.add(task);
        } else process(task);
        return cast;
    }

    /** Dispatches matching temporary behavior on the event's subject. */
    public void emit(SpellEvent event) {
        ensureOpen();
        SpellSubject subject = event.target().orElseGet(() -> new SpellSubject.Entity(event.actor()));
        emit(event, subject);
    }
    public void emit(SpellEvent event, SpellSubject subject) {
        ensureOpen();
        for (ActiveBinding binding : List.copyOf(bindings.values())) {
            if (binding.charges > 0 && bindings.containsKey(binding.id) && binding.target.equals(subject)) fire(binding, event);
        }
    }

    /** Advances exactly one server tick. */
    public void tick() {
        ensureOpen();
        tick++;
        cooldowns.values().removeIf(deadline -> deadline <= tick);
        for (Cast cast : List.copyOf(casts.values())) {
            if (!world.active(cast.context)) { end(cast, Status.INTERRUPTED); continue; }
            if (cast.status == Status.AWAITING_RECAST && tick >= cast.recastDeadline) end(cast, Status.TIMED_OUT);
        }
        for (ActiveManifestation manifestation : List.copyOf(manifestations.values())) {
            if (!manifestation.handle.alive()) remove(manifestation, EndReason.BACKING_REMOVED);
            else if (!world.active(manifestation.context)) remove(manifestation, EndReason.OWNER_UNAVAILABLE);
            else {
                manifestation.handle.tick();
                if (tick >= manifestation.nextPulse && !manifestation.definition.onTick().isEmpty()) {
                    manifestation.nextPulse = Math.addExact(tick, manifestation.definition.interval());
                    process(task(manifestation.context.cast, manifestation.definition.onTick(), manifestation.context));
                }
                if (tick >= manifestation.expires) remove(manifestation, EndReason.EXPIRED);
            }
        }
        bindings.values().removeIf(binding -> {
            if (tick < binding.expires && binding.context.live() && world.active(binding.context)) return false;
            binding.valid = false;
            return true;
        });
        for (ActiveManifestation manifestation : manifestations.values()) {
            if (!manifestation.bindingsNotified && !manifestation.definition.bindings().isEmpty() && bindings.values().stream().noneMatch(b -> b.context.manifestation == manifestation)) {
                manifestation.bindingsNotified = true;
                manifestation.handle.bindingsExhausted(false);
            }
        }
        for (ActiveBinding binding : List.copyOf(bindings.values())) {
            if (binding.charges > 0 && bindings.containsKey(binding.id)) fire(binding, new SpellEvent(SpellTriggerTypes.TICK,
                    binding.context.cast.actor, Optional.of(binding.target), Optional.empty(), binding.context.chain));
        }
        // Work scheduled during processing always resumes on a later tick; no zero-delay busy loops.
        while (!tasks.isEmpty() && tasks.peek().due <= tick) process(tasks.remove());
    }

    public int interruptActor(UUID actor) {
        int count = 0;
        for (Cast cast : List.copyOf(casts.values())) {
            if (cast.actor.equals(actor) && interrupt(cast.id)) count++;
        }
        return count;
    }

    public boolean interrupt(UUID castId) {
        Cast cast = casts.get(castId);
        if (cast == null) return false;
        end(cast, Status.INTERRUPTED);
        return true;
    }

    public int dispelActor(UUID actor) {
        int count = 0;
        for (ActiveManifestation manifestation : List.copyOf(manifestations.values())) {
            if (manifestation.context.cast.actor.equals(actor) && dispel(manifestation.id)) count++;
        }
        return count;
    }

    public boolean dispel(UUID manifestationId) {
        ActiveManifestation manifestation = manifestations.get(manifestationId);
        if (manifestation == null) return false;
        remove(manifestation, EndReason.DISPELLED);
        return true;
    }
    public int dispelSubject(SpellSubject subject) {
        int count = 0;
        for (ActiveManifestation manifestation : List.copyOf(manifestations.values())) {
            if (manifestation.context.target.equals(subject) && dispel(manifestation.id)) count++;
        }
        return count;
    }

    public int activeCasts() { return casts.size(); }
    public int activeBindings() { return bindings.size(); }
    public int activeManifestations() { return manifestations.size(); }
    public List<UUID> manifestations(UUID castId) {
        return manifestations.values().stream().filter(m -> m.context.cast.id.equals(castId)).map(m -> m.id).toList();
    }

    @Override
    public void close() {
        if (closed) return;
        for (Cast cast : List.copyOf(casts.values())) end(cast, Status.INTERRUPTED);
        for (ActiveManifestation manifestation : List.copyOf(manifestations.values())) remove(manifestation, EndReason.SERVER_STOP);
        tasks.clear(); bindings.clear(); cooldowns.clear(); closed = true;
    }

    private void fire(ActiveBinding binding, SpellEvent event) {
        CausalChain cause=binding.context.chain.secondary() ? event.cause().asSecondary() : event.cause();
        Context context = new Context(binding.context.cast, event, binding.target, cause, binding.context.manifestation);
        context.local=binding.context.local;
        for (SpellTrigger trigger : binding.definition.triggers()) {
            if (!trigger.event().equals(event.type()) || !trigger.conditions().stream().allMatch(c -> c.matches(context))) continue;
            Optional<CausalChain> branch = cause.enter(new CausalChain.ActivationKey(context.cast.spell.id(), trigger.id()));
            if (branch.isEmpty()) continue;
            binding.charges--;
            Context activation = new Context(context.cast, event, binding.target, branch.get(), context.manifestation);
            activation.binding = binding; activation.local=context.local;
            Task task = task(context.cast, binding.definition.effects(), activation);
            task.binding = binding;
            binding.work++;
            process(task);
            break;
        }
    }

    private Task task(Cast cast, List<SpellEffect> effects, Context context) {
        Task task = new Task(cast, tick, order++);
        append(task, effects, context);
        cast.work++;
        return task;
    }

    private void append(Task task, List<SpellEffect> effects, Context context) {
        for (int i = effects.size() - 1; i >= 0; i--) task.steps.addFirst(new Step(effects.get(i), context));
    }

    private void process(Task task) {
        if (task.steps.isEmpty()) { finish(task); return; }
        Context first = task.steps.getFirst().context;
        if (!first.live()) { task.steps.clear(); finish(task); return; }
        if (!world.active(first)) { end(task.cast, Status.INTERRUPTED); finish(task); return; }
        if (casts.containsKey(task.cast.id)) task.cast.status = Status.RUNNING;
        try {
            if (task.needsPayment) {
                task.needsPayment = false;
                if (!task.cast.freeResources && !world.pay(task.cast.costs, first)) { end(task.cast, Status.COST_FAILED); finish(task); return; }
                task.cast.paymentCommitted = true;
                if (!task.cast.freeResources) {
                    long recovery = task.cast.costs.stream().filter(SpellCost.Cooldown.class::isInstance)
                            .map(SpellCost.Cooldown.class::cast).mapToLong(SpellCost.Cooldown::ticks).sum();
                    if (recovery > 0) cooldowns.put(new Recovery(task.cast.actor, task.cast.spell.id()), Math.addExact(tick, recovery));
                }
                double risk = ForfeitPolicy.DEFAULT.chance(task.cast.traits, task.cast.discovered);
                if (risk > 0 && first.random() < risk) {
                    world.forfeit(first); end(task.cast, Status.FORFEITED); finish(task); return;
                }
            }
            int steps = 0;
            while (!task.steps.isEmpty()) {
                if (++steps > MAX_STEPS_PER_TASK) throw new IllegalStateException("Spell effect plan exceeded its execution budget");
                Step step = task.steps.removeFirst();
                Context context = step.context;
                if (!context.live()) continue;
                if (!step.effect.conditions().stream().allMatch(c -> c.matches(context))) continue;
                if (!(step.effect instanceof SpellEffects effect)) throw new IllegalArgumentException("Unregistered runtime effect: " + step.effect.getClass());
                switch (effect) {
                    case SpellEffects.Sequence sequence -> append(task, sequence.effects(), context);
                    case SpellEffects.Branch branch -> append(task, branch.condition().matches(context) ? branch.whenTrue() : branch.whenFalse(), context);
                    case SpellEffects.Delay delay -> {
                        if (!task.steps.isEmpty()) { task.due = Math.addExact(tick, delay.ticks()); tasks.add(task); return; }
                    }
                    case SpellEffects.Repeat repeat -> {
                        if (repeat.count() > 1024) throw new IllegalArgumentException("Repeat count exceeds 1024");
                        for (int i = repeat.count() - 1; i >= 0; i--) {
                            append(task, repeat.effects(), context);
                            if (i > 0) task.steps.addFirst(new Step(new SpellEffects.Delay(repeat.interval()), context));
                        }
                    }
                    case SpellEffects.ForEach each -> {
                        List<SpellSubject> targets = List.copyOf(world.select(each.target(), context));
                        if (targets.isEmpty() && each.target().required()) throw new IllegalStateException("No valid subject for required target");
                        if (!targets.isEmpty()) each.visual().ifPresent(visual -> {
                            List<SpellSubject> points = new ArrayList<>(); points.add(context.target); points.addAll(targets);
                            world.present(visual, List.copyOf(points), context);
                        });
                        for (int i = targets.size() - 1; i >= 0; i--) append(task, each.effects(), context.withTarget(targets.get(i)));
                    }
                    case SpellEffects.Secondary secondary -> process(task(task.cast,secondary.effects(),context.secondary()));
                    case SpellEffects.Limited limited -> {
                        if (context.target instanceof SpellSubject.Entity entity && context.claimContact(entity.id(),limited.group(),limited.perTarget(),limited.total()))
                            append(task,limited.effects(),context);
                    }
                    case SpellEffects.Visual visual -> world.present(visual.visual(), List.of(context.target), context);
                    case SpellEffects.SetValue value -> context.state().put(value.key(), value.value());
                    case SpellEffects.CaptureValue value -> context.setNumber(value.key(), context.number(value.value()));
                    case SpellEffects.StoreTarget value -> context.cast.anchors.put(value.key(), context.target);
                    case SpellEffects.AwaitRecast wait -> {
                        if (context.manifestation != null || task.cast.continuation != null) throw new IllegalStateException("Only one cast continuation can await a recast");
                        task.cast.status = Status.AWAITING_RECAST;
                        task.cast.recastDeadline = Math.addExact(tick, wait.timeoutTicks());
                        task.cast.continuation = task;
                        return;
                    }
                    case SpellEffects.Action action -> {
                        if (action.type().equals(id("reduce_pending_damage")) || action.type().equals(id("defer_pending_damage")) || action.type().equals(id("reduce_pending_heal"))) {
                            SpellEvent.PendingOutcome pending = context.event.pending().orElseThrow(() -> new IllegalStateException("No calculating-phase outcome"));
                            double reduction = Math.min(pending.amount(), context.amount(context.number(action.values().get("amount"))));
                            if (action.type().equals(id("defer_pending_damage"))) context.add(id("deferred_damage"), reduction);
                            pending.reduce(reduction);
                        } else if (!world.execute(action, context)) throw new IllegalStateException("Leaf effect failed: " + action.type());
                    }
                    case SpellEffects.InstallBinding install -> {
                        for (SpellSubject target : world.select(install.target(), context)) bind(install.binding(), context.withTarget(target));
                    }
                    case SpellEffects.CreateManifestation create -> {
                        List<SpellSubject> targets = world.select(create.target(), context);
                        if (targets.isEmpty()) throw new IllegalStateException("Manifestation has no valid target");
                        for (SpellSubject target : targets) manifest(create.manifestation(), context.withTarget(target));
                    }
                    case SpellEffects.EndManifestation ignored -> {
                        if (context.manifestation == null) throw new IllegalStateException("No current manifestation");
                        remove(context.manifestation, EndReason.DESTROYED);
                    }
                }
            }
        } catch (RuntimeException exception) {
            task.cast.failure = exception.getMessage();
            end(task.cast, Status.EFFECT_FAILED);
        }
        finish(task);
    }

    private void bind(SpellEffects.Binding definition, Context context) {
        // Replace only the same behavior from the same caster and spell on the same subject.
        bindings.values().removeIf(b -> {
            if (!b.definition.id().equals(definition.id()) || !b.target.equals(context.target)
                    || !b.context.cast.actor.equals(context.cast.actor) || !b.context.cast.spell.id().equals(context.cast.spell.id())) return false;
            b.valid = false;
            return true;
        });
        ActiveBinding binding = new ActiveBinding(definition, context, Math.addExact(tick, definition.durationTicks()));
        bindings.put(binding.id, binding);
    }

    private void manifest(SpellEffects.Manifestation definition, Context context) {
        for (ActiveManifestation existing : List.copyOf(manifestations.values())) {
            if (existing.definition.kind().equals(definition.kind()) && existing.originTarget.equals(context.target)
                    && !definition.kind().equals(id("projectile")) && !existing.context.cast.id.equals(context.cast.id)
                    && existing.context.cast.actor.equals(context.cast.actor) && existing.context.cast.spell.id().equals(context.cast.spell.id())) {
                remove(existing, EndReason.REPLACED);
            }
        }
        Map<String, Double> values = new LinkedHashMap<>();
        definition.values().forEach((key, value) -> values.put(key, context.gameplayValue(key,value)));
        if (values.containsKey("lifetime")) {
            if (definition.durationTicks()<0) throw new IllegalArgumentException("Persistent manifestations cannot be reshaped into timed ones");
            int lifetime=(int)Math.max(1,Math.min(240000,context.amount(values.get("lifetime"))));
            definition=new SpellEffects.Manifestation(definition.kind(),lifetime,definition.values(),definition.identifiers(),definition.bindings(),definition.onHit(),definition.onTick(),definition.onEnd(),definition.interval(),definition.visual());
        }
        ResourceLocation kind=definition.kind();
        SpellWorld.ManifestationHandle handle = world.manifest(definition, Map.copyOf(values), context)
                .orElseThrow(() -> new IllegalStateException("Cannot create manifestation: " + kind));
        ActiveManifestation manifestation = new ActiveManifestation(definition, handle,
                definition.durationTicks() == -1 ? Long.MAX_VALUE : Math.addExact(tick, definition.durationTicks()));
        manifestation.originTarget = context.target;
        manifestation.nextPulse = Math.addExact(tick, definition.interval());
        manifestation.context = new Context(context.cast, context.event, handle.subject(), context.chain, manifestation);
        manifestation.context.local=context.local;
        values.forEach((key, value) -> manifestation.state.put(id("manifestation/" + key), new ConditionValue.Decimal(value)));
        manifestations.put(manifestation.id, manifestation);
        for (SpellEffects.Binding binding : definition.bindings()) bind(binding, manifestation.context);
    }

    private void remove(ActiveManifestation manifestation, EndReason reason) {
        if (manifestations.remove(manifestation.id) == null) return;
        bindings.values().removeIf(b -> { if (b.context.manifestation != manifestation) return false; b.valid = false; return true; });
        manifestation.handle.close(reason);
        if (reason != EndReason.SERVER_STOP && reason != EndReason.OWNER_UNAVAILABLE && reason != EndReason.BACKING_REMOVED
                && !manifestation.definition.onEnd().isEmpty()) {
            manifestation.closing = true;
            try { process(task(manifestation.context.cast, manifestation.definition.onEnd(), manifestation.context)); }
            finally { manifestation.closing = false; }
        }
    }

    private void finish(Task task) {
        if (task.binding != null && --task.binding.work == 0 && task.binding.charges == 0) {
            bindings.remove(task.binding.id);
            task.binding.valid = false;
            ActiveManifestation owner = task.binding.context.manifestation;
            if (owner != null && !owner.bindingsNotified && bindings.values().stream().noneMatch(b -> b.context.manifestation == owner)) {
                owner.bindingsNotified = true;
                owner.handle.bindingsExhausted(true);
            }
        }
        if (--task.cast.work == 0 && task.steps.isEmpty() && (task.cast.status == Status.RUNNING || task.cast.status == Status.CHARGING)) {
            casts.remove(task.cast.id);
            task.cast.status = Status.COMPLETED;
        }
    }

    private void end(Cast cast, Status status) {
        casts.remove(cast.id); cast.status = status; cast.continuation = null;
        tasks.removeIf(task -> task.cast == cast);
        bindings.values().removeIf(b -> { if (b.context.cast != cast) return false; b.valid = false; return true; });
        for (ActiveManifestation manifestation : List.copyOf(manifestations.values())) {
            if (manifestation.context.cast == cast) remove(manifestation, EndReason.INTERRUPTED);
        }
    }

    private Cast rejected(SpellDefinition spell, SpellEvent event, Status status) {
        Cast cast = new Cast(spell, event.actor(), spell.traits(), true); cast.status = status; return cast;
    }
    private void ensureOpen() { if (closed) throw new IllegalStateException("Spell runtime is closed"); }
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("vestige", name); }

    public enum Status { RUNNING, CHARGING, AWAITING_RECAST, COMPLETED, BUSY, COOLDOWN, CONDITIONS_FAILED, LOOP_REJECTED,
        COST_FAILED, EFFECT_FAILED, FORFEITED, INTERRUPTED, TIMED_OUT }
    public enum EndReason { EXPIRED, DISPELLED, DESTROYED, REPLACED, INTERRUPTED, OWNER_UNAVAILABLE, BACKING_REMOVED, SERVER_STOP }

    public static final class Cast {
        private final UUID id = UUID.randomUUID();
        private final SpellDefinition spell;
        private final UUID actor;
        private final TraitProfile traits;
        private final boolean discovered;
        private List<SpellCost> costs;
        private CastShaping shaping = CastShaping.NONE;
        private Optional<ResourceLocation> mode = Optional.empty();
        private final Map<ResourceLocation, ConditionValue> state = new LinkedHashMap<>();
        private final Map<ResourceLocation, SpellSubject> anchors = new LinkedHashMap<>();
        private final Map<ResourceLocation,Integer> contactBudgets = new HashMap<>();
        private Status status = Status.RUNNING;
        private String failure;
        private Task continuation;
        private Context context;
        private long recastDeadline;
        private int work;
        private boolean freeResources;
        private boolean paymentCommitted;
        private Cast(SpellDefinition spell, UUID actor, TraitProfile traits, boolean discovered) {
            this.spell = spell; this.actor = actor; this.traits = traits; this.discovered = discovered;
        }
        public UUID id() { return id; }
        public Status status() { return status; }
        /** True after successful resource commitment, including a subsequent chaotic forfeit. */
        public boolean paymentCommitted() { return paymentCommitted; }
        public Optional<String> failure() { return Optional.ofNullable(failure); }
        public Map<ResourceLocation, ConditionValue> state() { return Map.copyOf(state); }
    }

    /** Resolved cast facts available to reusable leaves and world adapters. */
    public final class Context implements ConditionContext {
        private final Cast cast;
        private final SpellEvent event;
        private final SpellSubject target;
        private final CausalChain chain;
        private final ActiveManifestation manifestation;
        private ActiveBinding binding;
        private Map<ResourceLocation,ConditionValue> local;
        private Context(Cast cast, SpellEvent event, SpellSubject target, CausalChain chain, ActiveManifestation manifestation) {
            this.cast = cast; this.event = event; this.target = target; this.chain = chain; this.manifestation = manifestation;
        }
        public UUID actor() { return cast.actor; }
        public UUID castId() { return cast.id; }
        public SpellDefinition spell() { return cast.spell; }
        public TraitProfile traits() { return cast.traits; }
        public SpellSubject target() { return target; }
        public SpellEvent event() { return event; }
        public CausalChain cause() { return chain; }
        /** Shared by deliveries in this cast, so overlapping projectiles cannot multiply a capped hit. */
        public boolean claimHit(UUID target, ResourceLocation group, int limit) {
            ResourceLocation key = id("runtime/hits/" + group.getNamespace() + "/" + group.getPath() + "/" + target);
            int previous = cast.state.get(key) instanceof ConditionValue.Decimal value ? (int) value.value() : 0;
            if (previous >= limit) return false;
            cast.state.put(key, new ConditionValue.Decimal(previous + 1));
            return true;
        }
        /** One budget across the entire cast, including overlapping fields and their secondary callbacks. */
        public boolean claimContact(UUID target, ResourceLocation group, int perTarget, int total) {
            if (perTarget<1 || total<1 || total>1024) throw new IllegalArgumentException("Invalid contact budget");
            int spent=cast.contactBudgets.getOrDefault(group,0);
            if (spent>=total || !claimHit(target,group,perTarget)) return false;
            cast.contactBudgets.put(group,spent+1);
            return true;
        }
        public double number(com.quzzar.vestige.magic.expression.SpellValue value) {
            double result = switch (Objects.requireNonNull(value, "effect value")) {
                case com.quzzar.vestige.magic.expression.SpellValue.Fact fact -> ((ConditionValue.Decimal) value(fact.path()).orElseThrow(() -> new IllegalStateException("Missing numerical fact: " + fact.path()))).value();
                case com.quzzar.vestige.magic.expression.SpellValue.Sum sum -> sum.terms().stream().mapToDouble(this::number).sum();
                case com.quzzar.vestige.magic.expression.SpellValue.Product product -> product.factors().stream().mapToDouble(this::number).reduce(1, (a, b) -> a * b);
                case com.quzzar.vestige.magic.expression.SpellValue.Clamp clamp -> Math.max(clamp.minimum(),Math.min(clamp.maximum(),number(clamp.value())));
                default -> value.resolve(cast.traits);
            };
            if (!Double.isFinite(result)) throw new IllegalArgumentException("Resolved spell values must be finite");
            return result;
        }
        /** Complete expressions keep precision; world leaves quantize final amounts only. */
        public double amount(double resolved) { return cast.shaping.amount(resolved); }
        public double gameplayValue(String key, com.quzzar.vestige.magic.expression.SpellValue value) { return cast.shaping.value(key,number(value)); }
        public void add(ResourceLocation key, double amount) {
            double previous = state().get(key) instanceof ConditionValue.Decimal number ? number.value() : 0;
            state().put(key, new ConditionValue.Decimal(previous + amount));
        }
        public void setNumber(ResourceLocation key, double amount) { state().put(key, new ConditionValue.Decimal(amount)); }
        public Optional<SpellSubject> anchor(ResourceLocation key) { return Optional.ofNullable(cast.anchors.get(key)); }
        public Optional<SpellSubject> origin() { return manifestation == null ? Optional.empty() : Optional.of(manifestation.context.target); }
        public void execute(List<SpellEffect> effects, SpellSubject target) { process(task(cast, effects, withTarget(target))); }
        private Context withTarget(SpellSubject target) {
            Context context = new Context(cast, event, target, chain, manifestation); context.binding = binding; context.local=local; return context;
        }
        private Context withEvent(SpellEvent event) {
            Context context = new Context(cast, event, target, chain, manifestation); context.binding = binding; context.local=local; return context;
        }
        private Context secondary() {
            Context context=new Context(cast,event,target,chain.asSecondary(),manifestation); context.binding=binding;
            context.local=new LinkedHashMap<>(cast.state);
            if (manifestation!=null) context.local.putAll(manifestation.state);
            if (local!=null) context.local.putAll(local);
            return context;
        }
        private Map<ResourceLocation, ConditionValue> state() { return local!=null ? local : manifestation == null ? cast.state : manifestation.state; }
        private boolean live() {
            return (cast.status == Status.RUNNING || cast.status == Status.CHARGING || cast.status == Status.AWAITING_RECAST || cast.status == Status.COMPLETED)
                    && (manifestation == null || manifestation.closing || manifestations.containsKey(manifestation.id))
                    && (binding == null || binding.valid && tick < binding.expires);
        }
        @Override public Optional<ConditionValue> value(ResourceLocation path) {
            if (path.equals(id("event/secondary"))) return Optional.of(new ConditionValue.Flag(chain.secondary()));
            if (manifestation!=null && manifestation.state.containsKey(path) && (local==null || !local.containsKey(path))) return Optional.of(manifestation.state.get(path));
            if (path.equals(ConditionPaths.SPELL_RARITY)) return Optional.of(new ConditionValue.Text(cast.spell.rarity().id()));
            if (state().containsKey(path)) return Optional.of(state().get(path));
            if (cast.state.containsKey(path)) return Optional.of(cast.state.get(path));
            if (event.facts().containsKey(path)) return Optional.of(event.facts().get(path));
            if (path.equals(ConditionPaths.SPELL_ID)) return Optional.of(new ConditionValue.Identifier(cast.spell.id()));
            if (path.equals(id("spell/mode"))) return cast.mode.map(ConditionValue.Identifier::new).map(v -> (ConditionValue) v);
            if (path.equals(ConditionPaths.EVENT_ID)) return Optional.of(new ConditionValue.Identifier(event.type()));
            if (path.equals(ConditionPaths.ACTIVATION_COUNT)) return Optional.of(new ConditionValue.Decimal(chain.activations().size()));
            String prefix = "spell/trait/";
            if (path.getNamespace().equals("vestige") && path.getPath().startsWith(prefix)) {
                String[] parts = path.getPath().substring(prefix.length()).split("/", 2);
                if (parts.length == 2) return Optional.of(new ConditionValue.Decimal(cast.traits.rating(ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]))));
            }
            if (path.getNamespace().equals("vestige") && path.getPath().startsWith("spell/capability/")) {
                ResourceLocation capability = id(path.getPath().substring("spell/capability/".length()));
                return Optional.of(new ConditionValue.Flag(com.quzzar.vestige.magic.effect.SpellCapabilities.of(cast.spell).contains(capability)));
            }
            return world.conditions(this).value(path);
        }
        @Override public boolean isTagged(ResourceLocation path, ResourceLocation tag) { return world.conditions(this).isTagged(path, tag); }
        @Override public boolean matches(ResourceLocation path, ResourceLocation predicate) { return world.conditions(this).matches(path, predicate); }
        @Override public double random() { return world.conditions(this).random(); }
    }

    private record Step(SpellEffect effect, Context context) { }
    private static final class Task {
        final Cast cast;
        final LinkedList<Step> steps = new LinkedList<>();
        final long order;
        long due;
        boolean needsPayment;
        ActiveBinding binding;
        Task(Cast cast, long due, long order) { this.cast = cast; this.due = due; this.order = order; }
    }
    private static final class ActiveBinding {
        final UUID id = UUID.randomUUID();
        final SpellEffects.Binding definition;
        final Context context;
        final SpellSubject target;
        final long expires;
        int charges;
        int work;
        boolean valid = true;
        ActiveBinding(SpellEffects.Binding definition, Context context, long expires) {
            this.definition = definition; this.context = context; this.target = context.target; this.expires = expires; this.charges = definition.charges();
        }
    }
    private static final class ActiveManifestation {
        boolean bindingsNotified;
        SpellSubject originTarget;
        long nextPulse;
        final UUID id = UUID.randomUUID();
        final SpellEffects.Manifestation definition;
        final SpellWorld.ManifestationHandle handle;
        final Map<ResourceLocation, ConditionValue> state = new LinkedHashMap<>();
        final long expires;
        Context context;
        boolean closing;
        ActiveManifestation(SpellEffects.Manifestation definition, SpellWorld.ManifestationHandle handle, long expires) {
            this.definition = definition; this.handle = handle; this.expires = expires;
        }
    }
}
