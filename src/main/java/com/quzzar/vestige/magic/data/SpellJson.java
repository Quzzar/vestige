package com.quzzar.vestige.magic.data;

import com.google.gson.*;
import com.quzzar.vestige.magic.condition.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.presentation.SpellVisual;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.net.URI;

/** Reads the native runtime's authored JSON grammar with bounded nesting and strict scalar types. */
public final class SpellJson {
    private SpellJson() { }
    public static SpellDefinition read(ResourceLocation id, JsonObject json) {
        SpellRarity rarity = json.has("rarity")
                ? SpellRarity.fromId(text(json, "rarity")).orElseThrow(() -> new JsonParseException("Unknown spell rarity: " + json.get("rarity")))
                : SpellRarity.COMMON;
        Set<Tradition> traditions = new LinkedHashSet<>();
        for (JsonElement value : required(json, "traditions").getAsJsonArray()) {
            traditions.add(Tradition.fromId(value.getAsString()).orElseThrow(() -> new JsonParseException("Unknown tradition: " + value)));
        }
        Map<ResourceLocation, Double> traits = new LinkedHashMap<>();
        object(json, "traits").entrySet().forEach(entry -> traits.put(identifier(entry.getKey()), number(entry.getValue())));
        Map<ResourceLocation, SpellMode> modes = new LinkedHashMap<>();
        for (JsonElement entry : array(json, "modes")) {
            JsonObject mode = entry.getAsJsonObject();
            ResourceLocation modeId = identifier(text(mode, "id"));
            if (modes.put(modeId, new SpellMode(modeId, costs(array(mode, "costs")), effects(array(mode, "effects"), 0))) != null) {
                throw new JsonParseException("Duplicate cast mode: " + modeId);
            }
        }
        Optional<SpellSource> source = Optional.empty();
        if (json.has("source")) {
            JsonObject origin = object(json, "source");
            Optional<SpellSource.Reference> reference = Optional.empty();
            if (origin.has("reference")) {
                JsonObject ref = object(origin, "reference");
                reference = Optional.of(new SpellSource.Reference(text(ref, "system"), text(ref, "edition"), text(ref, "publication"),
                        integer(ref, "rank"), bool(required(ref, "cantrip")), text(ref, "rarity"), URI.create(text(ref, "url"))));
            }
            source = Optional.of(new SpellSource(identifier(text(origin, "spell")),
                    origin.has("school") ? Optional.of(identifier(text(origin, "school"))) : Optional.empty(),
                    text(origin, "revision"), text(origin, "name"),
                    origin.has("cast_type") ? Optional.of(text(origin, "cast_type")) : Optional.empty(),
                    origin.has("cooldown_ticks") ? OptionalInt.of(integer(origin, "cooldown_ticks")) : OptionalInt.empty(), reference));
        }
        return new SpellDefinition(id, rarity, traditions, new TraitProfile(traits), costs(array(json, "costs")), triggers(array(json, "triggers"), 0), effects(array(json, "effects"), 0), modes, source);
    }

    private static List<SpellCost> costs(JsonArray json) {
        List<SpellCost> costs = new ArrayList<>();
        for (JsonElement entry : json) {
            JsonObject cost = entry.getAsJsonObject();
            switch (text(cost, "type")) {
                case "time" -> costs.add(new SpellCost.Time(integer(cost, "ticks")));
                case "cooldown" -> costs.add(new SpellCost.Cooldown(integer(cost, "ticks")));
                case "mana" -> costs.add(new SpellCost.Mana(number(required(cost, "amount"))));
                case "health" -> costs.add(new SpellCost.Health(number(required(cost, "amount"))));
                case "hunger" -> costs.add(new SpellCost.Hunger(integer(cost, "amount")));
                case "material" -> costs.add(new SpellCost.Material(identifier(text(cost, "item")),
                        SpellCost.Material.Operation.valueOf(text(cost, "operation").toUpperCase(Locale.ROOT)), integer(cost, "amount")));
                default -> throw new JsonParseException("Unknown cost type: " + cost);
            }
        }
        return List.copyOf(costs);
    }

    private static List<SpellTrigger> triggers(JsonArray json, int depth) {
        List<SpellTrigger> result = new ArrayList<>();
        for (JsonElement element : json) {
            JsonObject trigger = element.getAsJsonObject();
            result.add(new SpellTrigger(identifier(text(trigger, "id")), identifier(text(trigger, "event")),
                    conditions(array(trigger, "conditions"), depth + 1)));
        }
        return List.copyOf(result);
    }

    private static List<SpellEffect> effects(JsonArray json, int depth) {
        checkDepth(depth);
        if (json.size() > 1024) throw new JsonParseException("An effect list exceeds 1024 entries");
        List<SpellEffect> result = new ArrayList<>();
        for (JsonElement element : json) {
            JsonObject effect = element.getAsJsonObject();
            String type = text(effect, "type");
            if (type.startsWith("vestige:")) type = type.substring(8);
            SpellEffect plan = switch (type) {
                case "sequence" -> new SpellEffects.Sequence(effects(array(effect, "effects"), depth + 1));
                case "branch" -> new SpellEffects.Branch(condition(required(effect, "condition").getAsJsonObject(), depth + 1),
                        effects(array(effect, "then"), depth + 1), effects(array(effect, "else"), depth + 1));
                case "delay" -> new SpellEffects.Delay(integer(effect, "ticks"));
                case "repeat" -> new SpellEffects.Repeat(integer(effect, "count"), integer(effect, "interval"), effects(array(effect, "effects"), depth + 1));
                case "for_each" -> new SpellEffects.ForEach(target(object(effect, "target"), depth + 1), effects(array(effect, "effects"), depth + 1), optionalVisual(effect, depth + 1));
                case "visual" -> new SpellEffects.Visual(visual(object(effect, "visual"), depth + 1));
                case "set_value" -> new SpellEffects.SetValue(identifier(text(effect, "key")), scalar(required(effect, "value")));
                case "capture_value" -> new SpellEffects.CaptureValue(identifier(text(effect, "key")), value(required(effect, "value"), depth + 1));
                case "store_target" -> new SpellEffects.StoreTarget(identifier(text(effect, "key")));
                case "await_recast" -> new SpellEffects.AwaitRecast(integer(effect, "timeout"));
                case "install_binding" -> new SpellEffects.InstallBinding(binding(object(effect, "binding"), depth + 1), target(object(effect, "target"), depth + 1));
                case "create_manifestation" -> new SpellEffects.CreateManifestation(manifestation(object(effect, "manifestation"), depth + 1), target(object(effect, "target"), depth + 1));
                case "end_manifestation" -> new SpellEffects.EndManifestation();
                default -> {
                    if (!SpellActionTypes.ACTIONS.contains(type)) throw new JsonParseException("Unknown effect: " + type);
                    yield new SpellEffects.Action(identifier(type), values(object(effect, "values"), depth + 1), identifiers(object(effect, "identifiers")));
                }
            };
            List<SpellCondition> guards = conditions(array(effect, "conditions"), depth + 1);
            result.add(guards.isEmpty() ? plan : new SpellEffects.Branch(new BuiltInCondition.All(guards), List.of(plan), List.of()));
        }
        return List.copyOf(result);
    }

    private static SpellEffects.Binding binding(JsonObject json, int depth) {
        checkDepth(depth);
        return new SpellEffects.Binding(identifier(text(json, "id")), triggers(array(json, "triggers"), depth + 1), effects(array(json, "effects"), depth + 1),
                integer(json, "duration"), integer(json, "charges"));
    }
    private static SpellEffects.Manifestation manifestation(JsonObject json, int depth) {
        checkDepth(depth);
        ResourceLocation kind = identifier(text(json, "kind"));
        if (!kind.getNamespace().equals("vestige") || !SpellActionTypes.MANIFESTATIONS.contains(kind.getPath())) {
            throw new JsonParseException("Unknown manifestation kind: " + kind);
        }
        List<SpellEffects.Binding> bindings = new ArrayList<>();
        for (JsonElement entry : array(json, "bindings")) bindings.add(binding(entry.getAsJsonObject(), depth + 1));
        return new SpellEffects.Manifestation(kind, integer(json, "duration"), values(object(json, "values"), depth + 1), identifiers(object(json, "identifiers")), bindings,
                effects(array(json, "on_hit"), depth + 1), effects(array(json, "on_tick"), depth + 1), effects(array(json, "on_end"), depth + 1), json.has("interval") ? integer(json, "interval") : 1, optionalVisual(json, depth + 1));
    }
    private static Optional<SpellVisual> optionalVisual(JsonObject json, int depth) {
        return json.has("visual") ? Optional.of(visual(object(json, "visual"), depth)) : Optional.empty();
    }
    private static SpellVisual visual(JsonObject json, int depth) {
        checkDepth(depth);
        List<SpellVisual.Layer> layers = new ArrayList<>();
        for (JsonElement element : array(json, "layers")) {
            JsonObject layer = element.getAsJsonObject();
            String color = text(layer, "color");
            if (!color.matches("[0-9a-fA-F]{6}")) throw new JsonParseException("Visual color needs six RGB hexadecimal digits");
            layers.add(new SpellVisual.Layer(SpellVisual.Shape.valueOf(text(layer, "shape").toUpperCase(Locale.ROOT)),
                    Integer.parseInt(color, 16), (float) number(required(layer, "alpha")),
                    (float) number(required(layer, "width")), (float) number(required(layer, "scale")),
                    layer.has("speed") ? (float) number(layer.get("speed")) : 1,
                    layer.has("phase") ? (float) number(layer.get("phase")) : 0,
                    layer.has("count") ? integer(layer, "count") : 8));
        }
        Optional<SpellVisual.Sound> sound = Optional.empty();
        if (json.has("sound")) {
            JsonObject audio = object(json, "sound");
            sound = Optional.of(new SpellVisual.Sound(identifier(text(audio, "id")),
                    (float) number(required(audio, "volume")), (float) number(required(audio, "pitch"))));
        }
        return new SpellVisual(integer(json, "duration"), value(required(json, "radius"), depth + 1),
                json.has("height") ? number(json.get("height")) : 0, layers,
                json.has("ends_with_bindings") && bool(json.get("ends_with_bindings")), sound);
    }
    private static TargetSpec target(JsonObject json, int depth) {
        return new TargetSpec(TargetSpec.Selection.valueOf(text(json, "selection").toUpperCase(Locale.ROOT)),
                json.has("distance") ? value(json.get("distance"), depth + 1) : new SpellValue.Constant(0),
                !json.has("required") || bool(json.get("required")), values(object(json, "options"), depth + 1),
                json.has("relationship") ? TargetSpec.Relationship.valueOf(text(json, "relationship").toUpperCase(Locale.ROOT)) : TargetSpec.Relationship.ANY);
    }
    private static Map<String, SpellValue> values(JsonObject json, int depth) {
        Map<String, SpellValue> values = new LinkedHashMap<>();
        json.entrySet().forEach(entry -> values.put(entry.getKey(), value(entry.getValue(), depth + 1)));
        return Map.copyOf(values);
    }
    private static Map<String, ResourceLocation> identifiers(JsonObject json) {
        Map<String, ResourceLocation> ids = new LinkedHashMap<>();
        json.entrySet().forEach(entry -> ids.put(entry.getKey(), identifier(entry.getValue().getAsString())));
        return Map.copyOf(ids);
    }
    private static SpellValue value(JsonElement json, int depth) {
        checkDepth(depth);
        if (json.isJsonPrimitive()) return new SpellValue.Constant(number(json));
        JsonObject object = json.getAsJsonObject();
        if (object.size() != 1) throw new JsonParseException("A value expression needs one operator");
        if (object.has("trait")) return new SpellValue.Trait(identifier(text(object, "trait")));
        if (object.has("fact")) return new SpellValue.Fact(identifier(text(object, "fact")));
        List<SpellValue> terms = new ArrayList<>();
        if (object.has("sum")) {
            for (JsonElement term : object.getAsJsonArray("sum")) terms.add(value(term, depth + 1));
            return new SpellValue.Sum(terms);
        }
        if (object.has("product")) {
            for (JsonElement term : object.getAsJsonArray("product")) terms.add(value(term, depth + 1));
            return new SpellValue.Product(terms);
        }
        throw new JsonParseException("Unknown numerical expression: " + object);
    }
    private static List<SpellCondition> conditions(JsonArray json, int depth) {
        List<SpellCondition> result = new ArrayList<>();
        for (JsonElement entry : json) result.add(condition(entry.getAsJsonObject(), depth + 1));
        return List.copyOf(result);
    }
    private static SpellCondition condition(JsonObject json, int depth) {
        checkDepth(depth);
        return switch (text(json, "type")) {
            case "all" -> new BuiltInCondition.All(conditions(array(json, "conditions"), depth + 1));
            case "any" -> new BuiltInCondition.Any(conditions(array(json, "conditions"), depth + 1));
            case "not" -> new BuiltInCondition.Not(condition(object(json, "condition"), depth + 1));
            case "exists" -> new BuiltInCondition.Exists(identifier(text(json, "path")));
            case "chance" -> new BuiltInCondition.Chance(number(required(json, "probability")));
            case "tagged" -> new BuiltInCondition.Tagged(identifier(text(json, "path")), identifier(text(json, "tag")));
            case "matches" -> new BuiltInCondition.Matches(identifier(text(json, "path")), identifier(text(json, "predicate")));
            case "compare" -> new BuiltInCondition.Compare(identifier(text(json, "path")),
                    BuiltInCondition.Comparison.valueOf(text(json, "operation").toUpperCase(Locale.ROOT)), scalar(required(json, "expected")),
                    json.has("upper") ? Optional.of(scalar(json.get("upper"))) : Optional.empty());
            default -> throw new JsonParseException("Unknown condition: " + json);
        };
    }
    private static ConditionValue scalar(JsonElement json) {
        if (json.isJsonObject()) return new ConditionValue.Identifier(identifier(text(json.getAsJsonObject(), "id")));
        JsonPrimitive primitive = json.getAsJsonPrimitive();
        if (primitive.isBoolean()) return new ConditionValue.Flag(primitive.getAsBoolean());
        if (primitive.isNumber()) return new ConditionValue.Decimal(number(json));
        if (primitive.isString()) return new ConditionValue.Text(primitive.getAsString());
        throw new JsonParseException("Expected a scalar value");
    }
    private static double number(JsonElement element) {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) throw new JsonParseException("Expected a number: " + element);
        double value = element.getAsDouble();
        if (!Double.isFinite(value)) throw new JsonParseException("Numbers must be finite");
        return value;
    }
    private static int integer(JsonObject json, String key) {
        double value = number(required(json, key));
        if (value != (int) value) throw new JsonParseException("Expected an integer: " + key);
        return (int) value;
    }
    private static String text(JsonObject json, String key) {
        JsonElement value = required(json, key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new JsonParseException("Expected text: " + key);
        return value.getAsString();
    }
    private static boolean bool(JsonElement json) {
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isBoolean()) throw new JsonParseException("Expected a boolean");
        return json.getAsBoolean();
    }
    private static ResourceLocation identifier(String text) {
        return text.contains(":") ? ResourceLocation.parse(text) : ResourceLocation.fromNamespaceAndPath("vestige", text);
    }
    private static JsonElement required(JsonObject json, String key) {
        if (!json.has(key)) throw new JsonParseException("Missing field: " + key);
        return json.get(key);
    }
    private static JsonObject object(JsonObject json, String key) { return json.has(key) ? json.getAsJsonObject(key) : new JsonObject(); }
    private static JsonArray array(JsonObject json, String key) { return json.has(key) ? json.getAsJsonArray(key) : new JsonArray(); }
    private static void checkDepth(int depth) { if (depth > 64) throw new JsonParseException("Spell nesting exceeds 64 levels"); }
}
