package com.quzzar.vestige.magic.runtime;

/**
 * A server-owned cast source reserved through preparation. Validation is read-only;
 * commitment must be infallible after the world has paid on the same server thread.
 * Item sources must exclude themselves from the spell's ordinary material payments.
 */
public interface CastReservation {
    CastReservation NONE = new CastReservation() {
        public boolean valid() { return true; }
        public void commit() { }
    };
    default CastObserver observer() { return CastObserver.NONE; }
    /** Sources such as worn armor may revoke their future bindings after removal. */
    default boolean continues() { return true; }
    boolean valid();
    void commit();

    /** A fallible physical commitment; adapters must refund payment when it rejects. */
    interface Atomic extends CastReservation {
        boolean tryCommit();
        @Override default void commit() { throw new IllegalStateException("Atomic source requires transactional payment"); }
    }
}
