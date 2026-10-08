package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.condition.ConditionContext;
import com.quzzar.vestige.magic.definition.SpellCost;
import com.quzzar.vestige.magic.definition.TargetSpec;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.presentation.SpellVisual;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** World-dependent spell behavior, used by both the Minecraft adapter and deterministic runtime tests. */
public interface SpellWorld {
    ConditionContext conditions(SpellRuntime.Context context);
    List<SpellSubject> select(TargetSpec target, SpellRuntime.Context context);
    /** Checks and spends the whole cost list atomically, excluding time already handled by the runtime. */
    boolean pay(List<SpellCost> costs, SpellRuntime.Context context);
    /** Nontransactional adapters reject fallible sources before spending anything. */
    default boolean payAndCommit(List<SpellCost> costs, SpellRuntime.Context context, CastReservation source) {
        if (source instanceof CastReservation.Atomic || !pay(costs, context)) return false;
        source.commit();
        return true;
    }
    boolean active(SpellRuntime.Context context);
    /** Activation gate checked before charge/payment and before continuation input. */
    default boolean canActivate(java.util.UUID actor, com.quzzar.vestige.magic.definition.MagicDefinition definition) { return true; }
    /** Trusted wearer/world contributions; source-local components are supplied by the activation caller. */
    default List<com.quzzar.vestige.magic.definition.TraitModifier> traitModifiers(java.util.UUID actor,
            com.quzzar.vestige.magic.definition.MagicDefinition definition) { return List.of(); }
    void forfeit(SpellRuntime.Context context);
    boolean execute(SpellEffects.Action action, SpellRuntime.Context context);
    /** Ordered, already-selected points. Cosmetic adapters must not perform another target query. */
    default void present(SpellVisual visual, List<SpellSubject> points, SpellRuntime.Context context) { }
    Optional<ManifestationHandle> manifest(SpellEffects.Manifestation definition, Map<String, Double> values,
                                         SpellRuntime.Context context);

    interface ManifestationHandle {
        SpellSubject subject();
        boolean alive();
        default void tick() { }
        /** The final owned binding has completed or expired; gameplay backing may still be alive. */
        default void bindingsExhausted(boolean consumed) { }
        void close(SpellRuntime.EndReason reason);
    }
}
