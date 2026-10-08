package com.quzzar.vestige.magic.presentation.client;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** Shared mana asset for resource-cost artwork. The pooled mana balance has no HUD meter. */
public final class ManaDisplay {
    public static final int SYMBOL_SIZE = 9;
    public static final ResourceLocation SYMBOL = VestigeMainMod.location("textures/gui/mana.png");
    private ManaDisplay() { }

    /** Native-sized resource mark, shared with mana-cost menu previews. */
    public static void drawSymbol(GuiGraphics graphics, int x, int y, boolean enabled) {
        float ink = enabled ? 1 : .4f;
        graphics.setColor(ink, ink, ink, 1);
        graphics.blit(SYMBOL, x, y, 0, 0, SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE, SYMBOL_SIZE);
        graphics.setColor(1, 1, 1, 1);
    }
}
