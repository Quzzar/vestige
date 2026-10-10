package com.quzzar.vestige.travel.client;

import com.quzzar.vestige.magic.presentation.client.ManaDisplay;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Exact native costs: counted full/half hearts or food, and numbered XP/mana marks. */
record TravelCostDisplay(Type type, int amount, boolean affordable) {
    enum Type {
        XP("screen.vestige.xp_points"), HEALTH("screen.vestige.health_points"),
        MANA("screen.vestige.mana_points"), HUNGER("screen.vestige.hunger_points");
        private final String label;
        Type(String label) { this.label = label; }
    }
    private static final int ICON_SIZE = 9, ICON_SPACING = 10;
    private static final ResourceLocation XP_ORB = ResourceLocation.withDefaultNamespace("textures/entity/experience_orb.png");
    private static final ResourceLocation HEART = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation HALF_HEART = ResourceLocation.withDefaultNamespace("hud/heart/half");
    private static final ResourceLocation EMPTY_HEART = ResourceLocation.withDefaultNamespace("hud/heart/container");
    private static final ResourceLocation FOOD = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final ResourceLocation HALF_FOOD = ResourceLocation.withDefaultNamespace("hud/food_half");
    private static final ResourceLocation EMPTY_FOOD = ResourceLocation.withDefaultNamespace("hud/food_empty");
    TravelCostDisplay {
        if (type == null || amount < 1) throw new IllegalArgumentException("Positive typed display cost required");
    }
    Component description() { return Component.translatable(type.label, amount); }
    boolean usesCountedIcons() { return (type == Type.HEALTH || type == Type.HUNGER) && amount <= 20; }
    int fullIcons() { return usesCountedIcons() ? amount / 2 : 0; }
    boolean hasHalfIcon() { return usesCountedIcons() && amount % 2 != 0; }
    private String number() {
        return type == Type.HEALTH || type == Type.HUNGER ? Integer.toString(amount / 2) + (amount % 2 == 0 ? "" : ".5") : Integer.toString(amount);
    }
    int width(Font font) {
        return usesCountedIcons() ? (fullIcons() + (hasHalfIcon() ? 1 : 0)) * ICON_SPACING - 1
                : font.width(number()) + 4 + ICON_SIZE;
    }
    /** Right-aligned amounts; full and half resource icons retain the actual point count. */
    void draw(GuiGraphics graphics, Font font, int right, int y, int textColor) {
        if (!usesCountedIcons()) graphics.drawString(font, number(), right - width(font), y, textColor, true);
        float shade = affordable ? 1 : .4f;
        graphics.setColor(shade, shade, shade, 1);
        switch (type) {
            case XP -> {
                graphics.setColor(affordable ? .5f : .35f, affordable ? 1 : .35f, affordable ? .12f : .35f, 1);
                graphics.blit(XP_ORB, right - ICON_SIZE, y, ICON_SIZE, ICON_SIZE, 0f, 0f, 16, 16, 64, 64);
            }
            case HEALTH, HUNGER -> {
                var full = type == Type.HEALTH ? HEART : FOOD;
                if (!usesCountedIcons()) {
                    graphics.blitSprite(full, right - ICON_SIZE, y, ICON_SIZE, ICON_SIZE);
                    break;
                }
                int left = right - width(font);
                for (int i = 0; i < fullIcons(); i++) graphics.blitSprite(full, left + i * ICON_SPACING, y, ICON_SIZE, ICON_SIZE);
                if (hasHalfIcon()) {
                    int x = left + fullIcons() * ICON_SPACING;
                    graphics.blitSprite(type == Type.HEALTH ? EMPTY_HEART : EMPTY_FOOD, x, y, ICON_SIZE, ICON_SIZE);
                    graphics.blitSprite(type == Type.HEALTH ? HALF_HEART : HALF_FOOD, x, y, ICON_SIZE, ICON_SIZE);
                }
            }
            case MANA -> {
                graphics.setColor(1, 1, 1, 1);
                ManaDisplay.drawSymbol(graphics, right - ICON_SIZE, y, affordable);
            }
        }
        graphics.setColor(1, 1, 1, 1);
    }
}
