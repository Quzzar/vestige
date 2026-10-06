package com.quzzar.vestige.magic.presentation.client;

import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Shared native mana ink: an open violet rune and a readable, whole-point meter. */
public final class ManaDisplay {
    public static final int SYMBOL_SIZE = 9, BAR_WIDTH = 128, METER_WIDTH = 167, METER_HEIGHT = 9;
    private static final int FRAME = 0xff21172f, TRACK = 0xff453458, INK = 0xffac73e8,
            HIGHLIGHT = 0xffdbc0fc, SHADE = 0xff68419a;
    private static final String[] RUNE = {
            "....s....", "...sls...", "..sl.ls..", ".sl...ls.", "sl..h..ls",
            ".sl...ls.", "..sl.ls..", "...sls...", "....s...."
    };
    private ManaDisplay() { }

    /** Draws a GUI-pixel rune, not an item icon or a network-signature glyph. */
    public static void drawSymbol(GuiGraphics graphics, int x, int y, boolean enabled) {
        for (int row = 0; row < RUNE.length; row++) for (int column = 0; column < RUNE[row].length(); column++) {
            int color = switch (RUNE[row].charAt(column)) {
                case 's' -> SHADE;
                case 'l' -> INK;
                case 'h' -> HIGHLIGHT;
                default -> 0;
            };
            if (color != 0) graphics.fill(x + column, y + row, x + column + 1, y + row + 1, enabled ? color : dim(color));
        }
    }
    private static int dim(int color) {
        return 0xff000000 | (int) (((color >> 16) & 255) * .4) << 16
                | (int) (((color >> 8) & 255) * .4) << 8 | (int) ((color & 255) * .4);
    }
    /** Slim horizontal fill followed by a whole-point balance and the shared mana rune. */
    public static void drawMeter(GuiGraphics graphics, Font font, int x, int y, float amount) {
        int points = Math.clamp(Math.round(amount), 0, NativeMana.MAX);
        String number = Integer.toString(points);
        graphics.drawString(font, number, x + METER_WIDTH - SYMBOL_SIZE - 4 - font.width(number), y + 1, HIGHLIGHT, true);
        drawSymbol(graphics, x + METER_WIDTH - SYMBOL_SIZE, y, true);
        int barX = x + 1, barY = y + 2;
        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + 6, FRAME);
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 5, TRACK);
        int filled = Math.clamp(Math.round(amount / NativeMana.MAX * BAR_WIDTH), 0, BAR_WIDTH);
        graphics.fill(barX, barY, barX + filled, barY + 5, INK);
        graphics.fill(barX, barY, barX + filled, barY + 1, HIGHLIGHT);
        graphics.fill(barX, barY + 4, barX + filled, barY + 5, SHADE);
    }
}
