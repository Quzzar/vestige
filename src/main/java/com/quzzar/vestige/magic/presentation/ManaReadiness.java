package com.quzzar.vestige.magic.presentation;

/** Mana affordability, independent of capacity, item count and elapsed time. */
public final class ManaReadiness {
    private ManaReadiness() { }
    public static float shortage(double available, double required) {
        if (!Double.isFinite(available) || available < 0 || !Double.isFinite(required) || required < 0)
            throw new IllegalArgumentException("Invalid mana affordability");
        return required == 0 || available >= required ? 0 : (float) (1 - available / required);
    }
}
