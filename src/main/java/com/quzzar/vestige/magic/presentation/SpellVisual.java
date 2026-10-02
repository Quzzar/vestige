package com.quzzar.vestige.magic.presentation;

import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable cosmetic recipe. No layer decides targets, damage, costs or trait semantics. */
public record SpellVisual(int duration, SpellValue radius, double height, List<Layer> layers,
                          boolean endsWithBindings, Optional<Sound> sound) {
    public SpellVisual {
        if (duration < 1 || duration > 2400) throw new IllegalArgumentException("Visual lifetime must be 1..2400 ticks");
        Objects.requireNonNull(radius);
        bounded(height, 0, 4);
        layers = List.copyOf(layers);
        if (layers.isEmpty() || layers.size() > 8) throw new IllegalArgumentException("Visuals need 1..8 layers");
        Objects.requireNonNull(sound);
    }
    public enum Shape { ARC, BEAM, SPHERE, RING, SPARKS, FIRE, SMOKE, BOX, BODY, TREE, WAVE, RAIN, VEIL, JET, SPLASH, SHIELD,
        SIGIL, HELIX, SHARDS, TENDRILS, FLARE, VORTEX, MOTES, LEAVES, RAYS, RIPPLE, SLASH, CHAIN, WINGS, FANGS, CLOCK, EYE }
    public record Layer(Shape shape, int color, float alpha, float width, float scale,
                        float speed, float phase, int count) {
        public Layer(Shape shape, int color, float alpha, float width, float scale) {
            this(shape, color, alpha, width, scale, 1, 0, 8);
        }
        public Layer {
            Objects.requireNonNull(shape);
            if (color < 0 || color > 0xffffff) throw new IllegalArgumentException("Visual color must be RGB");
            bounded(alpha, 0, 1); bounded(width, .005, 2); bounded(scale, .05, 4);
            bounded(speed, -3, 3); bounded(phase, 0, Math.PI * 2); bounded(count, 1, 24);
        }
    }
    public record Sound(ResourceLocation id, float volume, float pitch) {
        public Sound { Objects.requireNonNull(id); bounded(volume, 0, 2); bounded(pitch, .2, 2); }
    }
    public record Resolved(int duration, float radius, List<Layer> layers) {
        public Resolved {
            if (duration < 1 || duration > 2400) throw new IllegalArgumentException("Invalid resolved lifetime");
            bounded(radius, .01, 128);
            layers = List.copyOf(layers);
            if (layers.isEmpty() || layers.size() > 8) throw new IllegalArgumentException("Invalid resolved layers");
        }
        public Resolved remaining(int ticks) { return new Resolved(ticks, radius, layers); }
    }
    public Resolved resolve(double radius) { return new Resolved(duration, (float) radius, layers); }
    private static void bounded(double value, double min, double max) {
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException("Visual value outside " + min + ".." + max);
    }
}
