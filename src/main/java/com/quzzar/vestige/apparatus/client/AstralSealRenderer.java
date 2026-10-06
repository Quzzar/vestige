package com.quzzar.vestige.apparatus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.quzzar.vestige.apparatus.ApparatusShapes;
import com.quzzar.vestige.apparatus.RitualPresentation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Native Astral Seal: continuous emissive ribbons, normal depth testing, no raster glyph. */
final class AstralSealRenderer {
    static final double HOVER = .002;
    private static final double SURFACE = ApparatusShapes.SPELLSTONE_SURFACE + .0025;
    private static final int VIOLET = 0xa45bff, LAVENDER = 0xddacff, CORE = RitualPresentation.RUNE_COLOR;
    private static final List<Stroke> ASTRAL_CENTER = astralCenter();
    private record Stroke(Vec3 a, Vec3 b) {}
    private AstralSealRenderer() {}
    static void render(PoseStack pose, MultiBufferSource buffers, double seconds, double hover) {
        var light = buffers.getBuffer(RenderType.lightning());
        double y = SURFACE + hover + .001 * Math.sin(seconds * 1.1);
        for (int i = 0; i < 4; i++) {
            double a = i * Math.PI / 2;
            arc(light, pose, .307, y, a + seconds * .14, a + seconds * .14 + 1.08, .006, .85);
            arc(light, pose, .251, y + .002, a - seconds * .20 + .12, a - seconds * .20 + .99, .005, .62);
            Vec3 bottom = radial(.307, SURFACE + .001, a), top = radial(.307, y, a);
            // The seal sits just above the stone and below flat resting offerings.
            ribbon(light, pose, bottom, top, .003, VIOLET, .24);
            Vec3 p = radial(.322, y, a + seconds * .14);
            diamond(light, pose, p, .018, .007, .9);
        }
        for (var stroke : ASTRAL_CENTER) glow(light, pose, at(stroke.a, y + .001), at(stroke.b, y + .001), .006, .9);
    }

    private static List<Stroke> astralCenter() {
        var strokes = new ArrayList<Stroke>();
        polygon(strokes, .116, 4, 0);
        polygon(strokes, .116, 4, Math.PI / 4);
        for (int arm = 0; arm < 4; arm++) {
            double a = arm * Math.PI / 2;
            local(strokes, a, .126, 0, .200, 0);
            local(strokes, a, .184, -.029, .200, 0);
            local(strokes, a, .184, .029, .200, 0);
        }
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

    private static void diamond(VertexConsumer out, PoseStack pose, Vec3 c, double radius, double width, double strength) {
        Vec3 a = c.add(-radius, 0, 0), b = c.add(0, 0, radius), d = c.add(radius, 0, 0), e = c.add(0, 0, -radius);
        glow(out, pose, a, b, width, strength); glow(out, pose, b, d, width, strength);
        glow(out, pose, d, e, width, strength); glow(out, pose, e, a, width, strength);
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
