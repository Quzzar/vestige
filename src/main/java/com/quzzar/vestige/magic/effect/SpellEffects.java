package com.quzzar.vestige.magic.effect;

import com.quzzar.vestige.magic.condition.ConditionValue;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.presentation.SpellVisual;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Reusable effect composition. Leaf outcomes are executed by the world adapter. */
public sealed interface SpellEffects extends SpellEffect {
    @Override
    default List<SpellCondition> conditions() { return List.of(); }

    record Sequence(List<SpellEffect> effects) implements SpellEffects {
        public Sequence { effects = List.copyOf(effects); }
    }

    record Branch(SpellCondition condition, List<SpellEffect> whenTrue, List<SpellEffect> whenFalse) implements SpellEffects {
        public Branch {
            Objects.requireNonNull(condition);
            whenTrue = List.copyOf(whenTrue);
            whenFalse = List.copyOf(whenFalse);
        }
    }

    /** The remainder of the current sequence resumes after this delay. */
    record Delay(int ticks) implements SpellEffects {
        public Delay { positive(ticks); }
    }

    /** Repeats a finite plan, with a positive tick interval between iterations. */
    record Repeat(int count, int interval, List<SpellEffect> effects) implements SpellEffects {
        public Repeat { positive(count); positive(interval); effects = List.copyOf(effects); }
    }

    record ForEach(TargetSpec target, List<SpellEffect> effects, Optional<SpellVisual> visual) implements SpellEffects {
        public ForEach(TargetSpec target, List<SpellEffect> effects) { this(target, effects, Optional.empty()); }
        public ForEach { Objects.requireNonNull(target); effects = List.copyOf(effects); Objects.requireNonNull(visual); }
    }

    /** A finite cosmetic cue at the current subject; it has no derived gameplay capability. */
    record Visual(SpellVisual visual) implements SpellEffects {
        public Visual { Objects.requireNonNull(visual); }
    }

    record Action(ResourceLocation type, Map<String, SpellValue> values, Map<String, ResourceLocation> identifiers) implements SpellEffects {
        public Action {
            Objects.requireNonNull(type);
            values = Map.copyOf(values);
            identifiers = Map.copyOf(identifiers);
        }
    }

    record SetValue(ResourceLocation key, ConditionValue value) implements SpellEffects {
        public SetValue { Objects.requireNonNull(key); Objects.requireNonNull(value); }
    }

    /** Snapshots a resolved numerical fact for a later step or asynchronous impact. */
    record CaptureValue(ResourceLocation key, SpellValue value) implements SpellEffects {
        public CaptureValue { Objects.requireNonNull(key); Objects.requireNonNull(value); }
    }

    record StoreTarget(ResourceLocation key) implements SpellEffects {
        public StoreTarget { Objects.requireNonNull(key); }
    }

    /** Suspends the remainder until the same spell and actor recast, or the timeout expires. */
    record AwaitRecast(int timeoutTicks) implements SpellEffects {
        public AwaitRecast { positive(timeoutTicks); }
    }

    record InstallBinding(Binding binding, TargetSpec target) implements SpellEffects {
        public InstallBinding { Objects.requireNonNull(binding); Objects.requireNonNull(target); }
    }

    record CreateManifestation(Manifestation manifestation, TargetSpec target) implements SpellEffects {
        public CreateManifestation { Objects.requireNonNull(manifestation); Objects.requireNonNull(target); }
    }

    /** Ends the current manifestation and all behavior it owns. */
    record EndManifestation() implements SpellEffects { }

    record Binding(ResourceLocation id, List<SpellTrigger> triggers, List<SpellEffect> effects, int durationTicks, int charges) {
        public Binding {
            Objects.requireNonNull(id);
            triggers = List.copyOf(triggers);
            effects = List.copyOf(effects);
            positive(durationTicks);
            positive(charges);
            if (triggers.isEmpty() || effects.isEmpty()) throw new IllegalArgumentException("Bindings need triggers and effects");
        }
    }

    /** Duration -1 denotes persistent backing state, such as an attached block lock. */
    record Manifestation(ResourceLocation kind, int durationTicks, Map<String, SpellValue> values,
                         Map<String, ResourceLocation> identifiers, List<Binding> bindings,
                         List<SpellEffect> onHit, List<SpellEffect> onTick, List<SpellEffect> onEnd, int interval,
                         Optional<SpellVisual> visual) {
        public Manifestation(ResourceLocation kind, int durationTicks, Map<String, SpellValue> values,
                             Map<String, ResourceLocation> identifiers, List<Binding> bindings,
                             List<SpellEffect> onHit, List<SpellEffect> onTick, List<SpellEffect> onEnd, int interval) {
            this(kind, durationTicks, values, identifiers, bindings, onHit, onTick, onEnd, interval, Optional.empty());
        }
        public Manifestation(ResourceLocation kind, int durationTicks, Map<String, SpellValue> values,
                             Map<String, ResourceLocation> identifiers, List<Binding> bindings) {
            this(kind, durationTicks, values, identifiers, bindings, List.of(), List.of(), List.of(), 1);
        }
        public Manifestation {
            Objects.requireNonNull(kind);
            if (durationTicks != -1) positive(durationTicks);
            values = Map.copyOf(values);
            identifiers = Map.copyOf(identifiers);
            bindings = List.copyOf(bindings);
            onHit = List.copyOf(onHit); onTick = List.copyOf(onTick); onEnd = List.copyOf(onEnd); positive(interval);
            Objects.requireNonNull(visual);
        }
    }

    private static void positive(int value) {
        if (value <= 0) throw new IllegalArgumentException("Counts, intervals, and lifetimes must be positive");
    }
}
