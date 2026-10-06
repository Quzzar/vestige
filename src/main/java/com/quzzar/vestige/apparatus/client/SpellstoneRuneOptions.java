package com.quzzar.vestige.apparatus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.quzzar.vestige.apparatus.ApparatusBlocks;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import com.quzzar.vestige.apparatus.OfferingBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Opt-in art comparison: continuous native strokes, without a magnified raster texture. */
final class SpellstoneRuneOptions {
    private static final boolean ENABLED = System.getProperty("vestige.capture.output") != null
            && System.getProperty("vestige.capture.apparatus_preview", "current").equals("rune-options");
    private static final double SURFACE = 7.04 / 16;
    private static final int VIOLET = 0xa45bff, LAVENDER = 0xddacff, CORE = 0xf4e5ff;
    private static final List<Stroke> ENGRAVING = engraving();
    private static final List<Stroke> SCRIPT = script();

    private record Stroke(Vec3 a, Vec3 b) {}

    private SpellstoneRuneOptions() {}

    /** Returns false for every ordinary client, leaving the installed presentation untouched. */
    static boolean render(OfferingBlockEntity node, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        if (!ENABLED) return false;
        double seconds = (node.getLevel().getGameTime() + partialTick) / 20.0;
        var block = node.getBlockState().getBlock();
        if (block == ApparatusBlocks.SPELLSTONES.get(ApparatusMaterials.STONE_BRICKS).get())
            illuminated(pose, buffers, seconds);
        else if (block == ApparatusBlocks.SPELLSTONES.get(ApparatusMaterials.TUFF).get())
            astral(pose, buffers, seconds);
        else if (block == ApparatusBlocks.SPELLSTONES.get(ApparatusMaterials.QUARTZ).get())
            living(node, pose, buffers, seconds);
        return true;
    }

    private static void illuminated(PoseStack pose, MultiBufferSource buffers, double seconds) {
        var dark = buffers.getBuffer(RenderType.debugQuads());
        for (var stroke : ENGRAVING) ribbon(dark, pose, at(stroke.a, SURFACE), at(stroke.b, SURFACE), .023, 0x191421, .82);
        var light = buffers.getBuffer(RenderType.lightning());
        double pulse = .76 + .09 * Math.sin(seconds * Math.PI / 2);
        for (var stroke : ENGRAVING) glow(light, pose, at(stroke.a, SURFACE + .0008), at(stroke.b, SURFACE + .0008), .0065, pulse);
        // Light moves along the inscription, rather than rotating the carved mark itself.
        double angle = seconds * .45;
        arc(light, pose, .302, SURFACE + .0016, angle, angle + .28, .009, 1);
        arc(light, pose, .302, SURFACE + .0016, angle + Math.PI, angle + Math.PI + .28, .009, 1);
    }

    private static void astral(PoseStack pose, MultiBufferSource buffers, double seconds) {
        AstralSealRenderer.render(pose,buffers,seconds,.085);
    }

    private static void living(OfferingBlockEntity node, PoseStack pose, MultiBufferSource buffers, double seconds) {
        var dark = buffers.getBuffer(RenderType.debugQuads());
        for (var stroke : SCRIPT) ribbon(dark, pose, at(stroke.a, SURFACE), at(stroke.b, SURFACE), .020, 0x22152e, .68);
        var light = buffers.getBuffer(RenderType.lightning());
        double head = fract(seconds / 4.8);
        for (int i = 0; i < SCRIPT.size(); i++) {
            var stroke = SCRIPT.get(i);
            double age = fract(head - (i + .5) / SCRIPT.size());
            double strength = .18 + .82 * Math.exp(-age * 5.5);
            glow(light, pose, at(stroke.a, SURFACE + .001), at(stroke.b, SURFACE + .001), .006, strength);
        }
        Vec3 headPoint = along(SCRIPT, head, SURFACE + .008);
        glint(light, pose, node, headPoint, .027, 1);
        // Bounded local motes are part of the native renderer, not world entities or network particles.
        for (int i = 0; i < 14; i++) {
            double phase = fract(seconds * .28 + i * .61803398875);
            Vec3 start = along(SCRIPT, fract(i * .38196601125), SURFACE + .012);
            Vec3 p = start.add(Math.sin(i * 2.4 + seconds) * .015 * phase,
                    .16 * phase, Math.cos(i * 1.7 + seconds) * .015 * phase);
            glint(light, pose, node, p, .009 + .003 * Math.sin(i), Math.sin(Math.PI * phase) * .8);
        }
    }

    private static List<Stroke> engraving() {
        var strokes = new ArrayList<Stroke>();
        for (int arm = 0; arm < 8; arm++) {
            double a = arm * Math.PI / 4;
            strokes.add(new Stroke(radial(.066, 0, a), radial(.244, 0, a)));
            local(strokes, a, .214, -.054, .214, .054);
            local(strokes, a, .214, -.054, .245, -.054);
            local(strokes, a, .214, .054, .245, .054);
            local(strokes, a, .155, -.028, .155, .028);
            double begin = a + .045, end = a + Math.PI / 4 - .045;
            appendArc(strokes, .302, begin, end, 7);
        }
        polygon(strokes, .066, 4, Math.PI / 4);
        return List.copyOf(strokes);
    }

    private static List<Stroke> script() {
        var strokes = new ArrayList<Stroke>();
        // A woven angular inscription: an outer octagon, four folded arms and one inner diamond.
        polygon(strokes, .285, 8, Math.PI / 8);
        for (int arm = 0; arm < 4; arm++) {
            double a = arm * Math.PI / 2;
            local(strokes, a, .230, -.055, .152, -.055);
            local(strokes, a, .152, -.055, .105, 0);
            local(strokes, a, .105, 0, .152, .055);
            local(strokes, a, .152, .055, .230, .055);
            local(strokes, a, .230, .055, .230, -.055);
        }
        polygon(strokes, .070, 4, 0);
        return List.copyOf(strokes);
    }

    private static void polygon(List<Stroke> strokes, double radius, int sides, double rotation) {
        for (int i = 0; i < sides; i++) strokes.add(new Stroke(radial(radius, 0, rotation + i * 2 * Math.PI / sides),
                radial(radius, 0, rotation + (i + 1) * 2 * Math.PI / sides)));
    }

    private static void local(List<Stroke> strokes, double a, double x, double z, double ex, double ez) {
        strokes.add(new Stroke(point(a, x, z), point(a, ex, ez)));
    }

    private static Vec3 point(double a, double x, double z) {
        return new Vec3(.5 + x * Math.cos(a) - z * Math.sin(a), 0, .5 + x * Math.sin(a) + z * Math.cos(a));
    }

    private static Vec3 radial(double r, double y, double a) { return new Vec3(.5 + r * Math.cos(a), y, .5 + r * Math.sin(a)); }
    private static Vec3 at(Vec3 p, double y) { return new Vec3(p.x, y, p.z); }
    private static double fract(double value) { return value - Math.floor(value); }

    private static Vec3 along(List<Stroke> strokes, double fraction, double y) {
        double scaled = fraction * strokes.size();
        var stroke = strokes.get(Math.min(strokes.size() - 1, (int) scaled));
        return at(stroke.a.lerp(stroke.b, fract(scaled)), y);
    }

    private static void appendArc(List<Stroke> strokes, double r, double a, double b, int steps) {
        for (int i = 0; i < steps; i++) strokes.add(new Stroke(radial(r, 0, a + (b - a) * i / steps),
                radial(r, 0, a + (b - a) * (i + 1) / steps)));
    }

    private static void arc(VertexConsumer out, PoseStack pose, double r, double y, double a, double b, double width, double strength) {
        int steps = Math.max(3, (int) Math.ceil((b - a) * 24));
        for (int i = 0; i < steps; i++) glow(out, pose, radial(r, y, a + (b - a) * i / steps),
                radial(r, y, a + (b - a) * (i + 1) / steps), width, strength);
    }

    private static void glow(VertexConsumer out, PoseStack pose, Vec3 a, Vec3 b, double width, double strength) {
        ribbon(out, pose, a, b, width * 5, VIOLET, .045 * strength);
        ribbon(out, pose, a.add(0, .00015, 0), b.add(0, .00015, 0), width * 2.7, VIOLET, .16 * strength);
        ribbon(out, pose, a.add(0, .0003, 0), b.add(0, .0003, 0), width, LAVENDER, .70 * strength);
        ribbon(out, pose, a.add(0, .00045, 0), b.add(0, .00045, 0), width * .40, CORE, .92 * strength);
    }

    private static void glint(VertexConsumer out, PoseStack pose, OfferingBlockEntity node, Vec3 c, double size, double strength) {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 view = camera.getPosition().subtract(Vec3.atLowerCornerOf(node.getBlockPos()).add(c)).normalize();
        Vec3 right = new Vec3(0, 1, 0).cross(view).normalize(), up = view.cross(right).normalize();
        // Three additive layers plus a small crossed sparkle, rendered in actual camera-facing space.
        for (int layer = 0; layer < 3; layer++) {
            double scale = size * (layer == 0 ? 1.4 : layer == 1 ? .7 : .25);
            double alpha = strength * (layer == 0 ? .09 : layer == 1 ? .38 : .95);
            Vec3 r = right.scale(scale), u = up.scale(scale), front = c.add(view.scale(layer * .0002));
            quad(out, pose, front.subtract(r), front.add(u), front.add(r), front.subtract(u), layer == 2 ? CORE : VIOLET, alpha);
            quad(out, pose, front.subtract(u), front.add(r), front.add(u), front.subtract(r), layer == 2 ? CORE : VIOLET, alpha);
        }
    }

    private static void ribbon(VertexConsumer out, PoseStack pose, Vec3 a, Vec3 b, double width, int color, double alpha) {
        Vec3 direction = b.subtract(a), side = new Vec3(-direction.z, 0, direction.x).normalize().scale(width / 2);
        if (side.lengthSqr() < 1e-12) side = new Vec3(width / 2, 0, 0);
        quad(out, pose, a.subtract(side), a.add(side), b.add(side), b.subtract(side), color, alpha);
    }

    private static void quad(VertexConsumer out, PoseStack pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color, double alpha) {
        for (var p : List.of(a, b, c, d)) out.addVertex(pose.last().pose(), (float) p.x, (float) p.y, (float) p.z)
                .setColor((color >> 16 & 255) / 255f, (color >> 8 & 255) / 255f, (color & 255) / 255f, (float) alpha);
    }
}
