package com.quzzar.vestige.apparatus;

import com.google.gson.*;
import com.quzzar.vestige.magic.definition.TraitModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Pure evaluator of the accepted matrix. No world state, trait ratings or recipe identities enter its math. */
public final class LeylineShaping {
    private static final List<String> AXES = List.of("amplify", "range", "area", "cost");
    private static final JsonObject RULES = loadRules();
    public enum Shape { CROSS, DIAGONAL;
        String key() { return name().toLowerCase(Locale.ROOT); }
        double radius(int offset) { return offset * (this == DIAGONAL ? Math.sqrt(2) : 1); }
    }
    public record Geometry(int slots, Shape innerShape, int inner, int innerHeight,
                           Shape outerShape, int outer, int outerStep) {
        public Geometry {
            Objects.requireNonNull(innerShape); Objects.requireNonNull(outerShape);
            var limits = RULES.getAsJsonObject("layoutBounds");
            if ((slots != 4 && slots != 8) || inner < 1 || innerShape.radius(inner) > limits.get("innerRadius").getAsInt()
                    || Math.abs(innerHeight) > limits.get("heightStep").getAsInt()) throw new IllegalArgumentException("Invalid inner layer");
            if (slots == 8 && (outer < 1 || outerShape.radius(outer) <= innerShape.radius(inner)
                    || outerShape.radius(outer) > limits.get("outerRadius").getAsInt()
                    || Math.abs(outerStep) > limits.get("heightStep").getAsInt())) throw new IllegalArgumentException("Invalid outer layer");
            if (slots == 4) { outerShape = Shape.CROSS; outer = 0; outerStep = 0; }
        }
        public double d1() { return innerShape.radius(inner); }
        public double d2() {
            double r = outerShape.radius(outer), d = d1();
            return Math.sqrt(d*d + r*r - 2*d*r*(innerShape == outerShape ? 1 : Math.sqrt(.5)));
        }
        public Geometry innerOnly() { return new Geometry(4, innerShape, inner, innerHeight, Shape.CROSS, 0, 0); }
        /** Relative recipe seats: inner even, outer odd, clockwise from North or North-East. */
        public BlockPos offset(int seat) {
            if (seat<0 || seat>=8 || slots==4 && (seat&1)==1) throw new IllegalArgumentException("Inactive recipe seat");
            boolean outside=(seat&1)==1;
            Shape shape=outside ? outerShape : innerShape;
            int distance=outside ? outer : inner, height=outside ? innerHeight+outerStep : innerHeight;
            int[][] directions=shape==Shape.CROSS ? new int[][]{{0,-1},{1,0},{0,1},{-1,0}} : new int[][]{{1,-1},{1,1},{-1,1},{-1,-1}};
            int[] direction=directions[seat/2];
            return new BlockPos(direction[0]*distance,height,direction[1]*distance);
        }
    }
    public record Modifiers(double amplify, double range, double area, double cost) {
        public List<TraitModifier> traits() {
            return List.of(modifier("amplify", amplify), modifier("range", range), modifier("area", area));
        }
        private static TraitModifier modifier(String name, double value) {
            return new TraitModifier(ResourceLocation.fromNamespaceAndPath("vestige", name), TraitModifier.Operation.MULTIPLY, value);
        }
    }
    private LeylineShaping() { }
    public static Modifiers resolve(Geometry geometry, Collection<ResourceLocation> traits) {
        JsonObject matrices = RULES.getAsJsonObject("traits"), factors = RULES.getAsJsonObject("factors");
        List<String> selected = traits.stream().filter(t -> t.getNamespace().equals("vestige") && matrices.has(t.getPath()))
                .map(ResourceLocation::getPath).distinct().sorted().toList();
        Map<String, JsonElement> actual = Map.of("innerShape", new JsonPrimitive(geometry.innerShape.key()),
                "outerShape", new JsonPrimitive(geometry.outerShape.key()), "d1", new JsonPrimitive(geometry.d1()),
                "d2", new JsonPrimitive(geometry.slots == 8 ? geometry.d2() : 2),
                "dy1", new JsonPrimitive(geometry.innerHeight), "dy2", new JsonPrimitive(geometry.outerStep));
        double[] logs = new double[4];
        for (var entry : factors.entrySet()) {
            JsonObject factor = entry.getValue().getAsJsonObject();
            if (factor.get("outer").getAsBoolean() && geometry.slots != 8) continue;
            for (int axis = 0; axis < 4; axis++) for (String trait : selected) {
                JsonArray cell = matrices.getAsJsonObject(trait).getAsJsonArray(entry.getKey()).get(axis).getAsJsonArray();
                logs[axis] += cell.get(1).getAsDouble() * (response(factor, actual.get(entry.getKey()), cell.get(0))
                        - response(factor, factor.get("reference"), cell.get(0))) / selected.size();
            }
        }
        JsonObject scaling = RULES.getAsJsonObject("scaling");
        double d1 = clamp(log2(geometry.d1()/2), scaling.getAsJsonArray("distanceLogBounds"));
        double d2 = geometry.slots == 8 ? clamp(log2(geometry.d2()/2), scaling.getAsJsonArray("distanceLogBounds")) : 0;
        logs[0] += number(scaling,"amplifyInnerHeight")*geometry.innerHeight + (geometry.slots == 8 ? number(scaling,"amplifyOuterHeight")*geometry.outerStep : 0);
        logs[1] += number(scaling,"rangeDistance")*d1 + (geometry.innerShape == Shape.DIAGONAL ? number(scaling,"rangeDiagonal") : 0);
        logs[2] += number(scaling,"areaInnerDistance")*d1 + (geometry.slots == 8
                ? number(scaling,"areaOuterDistance")*d2 + (geometry.outerShape == Shape.DIAGONAL ? number(scaling,"areaOuterDiagonal") : 0)
                : geometry.innerShape == Shape.DIAGONAL ? number(scaling,"areaInnerDiagonal") : 0);
        double[] output = new double[4]; JsonObject bounds = RULES.getAsJsonObject("bounds"), cost = RULES.getAsJsonObject("cost");
        for (int axis = 0; axis < 3; axis++) {
            double bounded = clamp(Math.pow(2,logs[axis]), bounds.getAsJsonArray(AXES.get(axis)));
            output[axis] = bounded < 1 ? Math.pow(bounded, number(scaling,"outcomeDownsideExponent")) : bounded;
            double log = log2(output[axis]);
            logs[3] += number(cost, AXES.get(axis)+"Weight")*log*number(cost,log >= 0 ? "benefitExponent" : "reductionExponent");
        }
        output[3] = clamp(Math.pow(2, logs[3] > 0 ? logs[3]*number(cost,"increaseExponent") : logs[3]), bounds.getAsJsonArray("cost"));
        return new Modifiers(output[0],output[1],output[2],output[3]);
    }
    private static double response(JsonObject factor, JsonElement value, JsonElement preference) {
        String curve = factor.get("curve").getAsString();
        if (curve.equals("shape")) return value.getAsString().equals(preference.getAsString()) ? 1 : -1;
        double delta = curve.equals("distance") ? log2(value.getAsDouble()/preference.getAsDouble()) : value.getAsDouble()-preference.getAsDouble();
        return Math.exp(-.5*Math.pow(delta/number(factor,"width"),2));
    }
    private static double number(JsonObject object, String key) { return object.get(key).getAsDouble(); }
    private static double log2(double value) { return Math.log(value)/Math.log(2); }
    private static double clamp(double value, JsonArray bounds) { return Math.max(bounds.get(0).getAsDouble(),Math.min(bounds.get(1).getAsDouble(),value)); }
    private static JsonObject loadRules() {
        try (var input = LeylineShaping.class.getResourceAsStream("/data/vestige/leyline_rules.json")) {
            if (input == null) throw new IllegalStateException("Missing accepted leyline rules");
            var rules = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!rules.get("version").getAsString().equals("leyline-calculator-v3") || !rules.get("status").getAsString().equals("accepted_design")) throw new IllegalStateException("Unaccepted leyline rules");
            return rules;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
