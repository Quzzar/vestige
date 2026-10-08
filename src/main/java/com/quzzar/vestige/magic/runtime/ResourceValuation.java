package com.quzzar.vestige.magic.runtime;

/** Owner-accepted payment values: 1 heart = 2 full hunger icons = 30 XP points = 30 mana. */
public final class ResourceValuation {
    public static final int MANA_PER_HEART = 30;
    public static final int FOOD_PER_HEART = 4;
    public static final int XP_PER_HEART = 30;
    public static final double MANA_PER_FOOD = (double) MANA_PER_HEART / FOOD_PER_HEART;
    private ResourceValuation() { }
    public static int healthForMana(double mana) { return units(mana / MANA_PER_HEART, 2); }
    public static int foodForMana(double mana) { return units(mana / MANA_PER_FOOD, 1); }
    public static int experienceForMana(double mana) { return units(mana * XP_PER_HEART / MANA_PER_HEART, 1); }
    private static int units(double amount, int size) {
        double rounded = Math.ceil(amount) * size;
        if (!Double.isFinite(amount) || amount < 0 || rounded > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Resource payment exceeds supported amount");
        return (int) rounded;
    }
}
