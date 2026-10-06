package com.quzzar.vestige.travel.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.travel.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/** Emissive signature ink and bounded, client-only glyph motes share the shard's exact mark. */
public final class StandingStoneRenderer implements BlockEntityRenderer<StandingStoneEntity> {
    private static final float INK_SCALE = .022f;
    private static final int MOTE_CYCLE_TICKS = 100;
    private static final int MOTE_LIFE_TICKS = 72;
    private static final int MOTE_DISTANCE = 24;
    private final Font font;
    private final BlockEntityRenderDispatcher dispatcher;

    public StandingStoneRenderer(BlockEntityRendererProvider.Context context) {
        font = context.getFont();
        dispatcher = context.getBlockEntityRenderDispatcher();
    }

    @Override public void render(StandingStoneEntity stone, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!StoneNetwork.validKey(stone.key())) return;
        var state = stone.getBlockState(); var shape = state.getValue(StandingStoneBlock.PROFILE);
        var mark = AttunementMark.fromKey(stone.key());
        double ticks = stone.getLevel() == null ? 0 : stone.getLevel().getGameTime() + partialTick;
        double phase = Math.floorMod(stone.getBlockPos().asLong(), MOTE_CYCLE_TICKS);
        float rotation = switch (state.getValue(StandingStoneBlock.FACING)) {
            case NORTH -> 0; case EAST -> -90; case SOUTH -> 180; case WEST -> 90;
            default -> throw new IllegalStateException("Horizontal facing required");
        };
        pose.pushPose();
        pose.translate(.5, 0, .5); pose.mulPose(Axis.YP.rotationDegrees(rotation)); pose.translate(-.5, 0, -.5);
        pose.pushPose();
        pose.translate(shape.runeX(), shape.runeY(), shape.runeZ() - .002);
        // Native text's front faces +Z. Turn it toward the north face, retaining upright glyphs.
        pose.mulPose(Axis.YP.rotationDegrees(180)); pose.scale(INK_SCALE, -INK_SCALE, INK_SCALE);
        var runes = mark.runes();
        for (int i = 0; i < runes.size(); i++) {
            var glyph = runes.get(i).component();
            float pulse = (float) (.5 + .5 * Math.sin((ticks + phase) * Math.PI / 90 + i * .7));
            float x = -font.width(glyph) / 2f, y = i * 12;
            // Sub-pixel copies soften the luminous edge without a shader or a rectangle behind the rune.
            float glow = .10f + .045f * pulse;
            drawGlyph(glyph, x - .55f, y, glow, pose, buffers);
            drawGlyph(glyph, x + .55f, y, glow, pose, buffers);
            drawGlyph(glyph, x, y - .55f, glow, pose, buffers);
            drawGlyph(glyph, x, y + .55f, glow, pose, buffers);
            drawGlyph(glyph, x, y, .86f + .14f * pulse, pose, buffers, Font.DisplayMode.POLYGON_OFFSET);
        }
        pose.popPose();
        renderMotes(stone, mark, shape, rotation, ticks + phase, pose, buffers);
        pose.popPose();
    }

    /** A fixed number of analytic trajectories avoids particle accumulation and disappears with the stone. */
    private void renderMotes(StandingStoneEntity stone, AttunementMark mark, StandingStoneShape shape, float rotation,
                             double ticks, PoseStack pose, MultiBufferSource buffers) {
        if (stone.getLevel() == null || dispatcher.camera == null
                || !Vec3.atCenterOf(stone.getBlockPos()).closerThan(dispatcher.camera.getPosition(), MOTE_DISTANCE)) return;
        ParticleStatus setting = Minecraft.getInstance().options.particles().get();
        int count = switch (setting) { case ALL -> 3; case DECREASED -> 1; case MINIMAL -> 0; };
        for (int slot = 0; slot < count; slot++) {
            double clock = ticks + slot * MOTE_CYCLE_TICKS / 3.0;
            long cycle = (long) Math.floor(clock / MOTE_CYCLE_TICKS);
            double age = clock - cycle * MOTE_CYCLE_TICKS;
            if (age >= MOTE_LIFE_TICKS) continue;
            double progress = age / MOTE_LIFE_TICKS;
            float alpha = (float) (Math.sin(Math.PI * progress) * .72);
            int runeIndex = (int) Math.floorMod(cycle + slot, mark.runes().size());
            var glyph = mark.runes().get(runeIndex).component();
            double sideways = Math.sin(progress * Math.PI * 1.3 + slot * 2.1) * .12 * progress;
            pose.pushPose();
            pose.translate(shape.runeX() + sideways, shape.runeY() - runeIndex * 12 * INK_SCALE - .075 + .32 * progress,
                    shape.runeZ() - .025 - .34 * progress);
            // Undo the stone's local facing so these small glyphs face the camera like vanilla particles.
            pose.mulPose(Axis.YP.rotationDegrees(-rotation));
            pose.mulPose(dispatcher.camera.rotation());
            float scale = (float) (.012 * (1 - .20 * progress));
            pose.scale(scale, -scale, scale);
            drawGlyph(glyph, -font.width(glyph) / 2f, -4, alpha, pose, buffers);
            pose.popPose();
        }
    }

    private void drawGlyph(Component glyph, float x, float y, float alpha, PoseStack pose, MultiBufferSource buffers) {
        drawGlyph(glyph, x, y, alpha, pose, buffers, Font.DisplayMode.NORMAL);
    }

    private void drawGlyph(Component glyph, float x, float y, float alpha, PoseStack pose, MultiBufferSource buffers,
                           Font.DisplayMode mode) {
        int opacity = Math.round(alpha * 255);
        // Font treats alpha bytes below four as unspecified/opaque. Skip the ends of a fade instead.
        if (opacity < 4) return;
        font.drawInBatch(glyph, x, y, opacity << 24 | 0x00ffffff, false,
                pose.last().pose(), buffers, mode, 0, LightTexture.FULL_BRIGHT);
    }
    @Override public boolean shouldRenderOffScreen(StandingStoneEntity stone) { return true; }
}
