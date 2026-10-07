package com.quzzar.vestige.magic.runtime;

/** Cast-owned equipment hooks. Ordinary casts use NONE; secondary outcomes never notify observers. */
public interface CastObserver {
    enum Kind { DAMAGE, HEAL, PROTECTION, UTILITY }
    CastObserver NONE = new CastObserver() { };
    default void preparing(SpellRuntime.Context context) { }
    /** After payment and the volatile gate, before the primary plan. */
    default void activated(SpellRuntime.Context context) { }
    /** A real primary outcome, not an attempted hit or a merely visual delivery. */
    default void resolved(Kind kind,double amount,SpellRuntime.Context context) { }
    default void ended(SpellRuntime.Status status) { }
}
