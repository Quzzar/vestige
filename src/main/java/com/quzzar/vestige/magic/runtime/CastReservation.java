package com.quzzar.vestige.magic.runtime;

/**
 * A server-owned cast source reserved through preparation. Validation is read-only;
 * commitment must be infallible after the world has paid on the same server thread.
 * Item sources must exclude themselves from the spell's ordinary material payments.
 */
public interface CastReservation {
    record Recovery(net.minecraft.resources.ResourceLocation group,int ticks) {
        public Recovery {
            java.util.Objects.requireNonNull(group);
            if (ticks<1 || ticks>240000) throw new IllegalArgumentException("Invalid source recovery");
        }
    }
    CastReservation NONE = new CastReservation() {
        public boolean valid() { return true; }
        public void commit() { }
    };
    default CastObserver observer() { return CastObserver.NONE; }
    boolean valid();
    void commit();
    /** An additional per-actor/per-spell source-family restriction; ordinary spell recovery remains separate. */
    default java.util.Optional<Recovery> recovery() { return java.util.Optional.empty(); }
}
