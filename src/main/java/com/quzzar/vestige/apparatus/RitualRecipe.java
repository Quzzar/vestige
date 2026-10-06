package com.quzzar.vestige.apparatus;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A rotation-invariant circular recipe; a mirror reverses its authored relationships. */
public record RitualRecipe(ResourceLocation spell, String name, int circle, int color, List<Part> parts, List<String> clues) {
    public RitualRecipe {
        parts = List.copyOf(parts); clues = List.copyOf(clues);
        if (circle != 4 && circle != 8 || color < 0 || color > 0xffffff || parts.size() < 4 || parts.size() > 8) throw new IllegalArgumentException("Invalid ritual bounds");
        Set<Integer> seats = new HashSet<>();
        for (Part part : parts) if (part.seat < 0 || part.seat >= circle || !seats.add(part.seat)) throw new IllegalArgumentException("Duplicate or invalid seat");
        if (circle == 4 && parts.size() != 4) throw new IllegalArgumentException("Basic recipes fill all four slots");
        if (circle == 8 && !seats.containsAll(List.of(0,2,4,6))) throw new IllegalArgumentException("Advanced recipes retain four inner ingredients");
        if (parts.stream().filter(p -> p.ingredient.items.contains(ResourceLocation.withDefaultNamespace("paper"))).count() != 1) throw new IllegalArgumentException("A crafting recipe needs exactly one Paper component");
    }
    public record Ingredient(List<ResourceLocation> items, List<ResourceLocation> tags) {
        public Ingredient {
            items = List.copyOf(items); tags = List.copyOf(tags);
            if (items.isEmpty() || items.size() > 8 || tags.size() > 4 || !Set.of("minecraft", "vestige").contains(items.getFirst().getNamespace())) throw new IllegalArgumentException("Ingredients require a standalone baseline");
        }
        public boolean matches(ItemStack stack) {
            return !stack.isEmpty() && (items.contains(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                    || tags.stream().anyMatch(tag -> stack.is(TagKey.create(Registries.ITEM, tag))));
        }
        public ItemStack hint() { return new ItemStack(BuiltInRegistries.ITEM.get(items.getFirst())); }
    }
    public record Part(int seat, String role, Ingredient ingredient) { }
    public enum Feedback { NONE, HINT, CORRECT, SHAKE, WRONG, SUCCESS }
    public record Evaluation(boolean correct, boolean complete, int rotation, List<Feedback> feedback, Map<Integer, Part> expected) {
        public Evaluation { feedback = List.copyOf(feedback); expected = Map.copyOf(expected); }
    }

    /** Evaluates the most informative quarter-turn without changing ingredients or using RNG. */
    public Evaluation evaluate(List<ItemStack> stands) {
        if (stands.size() != 8) throw new IllegalArgumentException("A layout supplies eight addressable positions");
        int best = -1, rotation = 0;
        for (int turn = 0; turn < 4; turn++) {
            int score = 0;
            for (Part part : parts) if (part.ingredient.matches(stands.get(index(part.seat, turn)))) score++;
            if (score > best) { best = score; rotation = turn; }
        }
        var expected = new java.util.LinkedHashMap<Integer, Part>();
        for (Part part : parts) expected.put(index(part.seat, rotation), part);
        List<Feedback> feedback = new ArrayList<>(); boolean correct = true, complete = true;
        for (int i = 0; i < 8; i++) {
            if (circle==4 && (i&1)==1) { feedback.add(Feedback.NONE); continue; }
            ItemStack stack = stands.get(i); Part wanted = expected.get(i); Feedback result;
            if (wanted != null && stack.isEmpty()) { result = Feedback.HINT; complete = false; correct = false; }
            else if (wanted != null && wanted.ingredient.matches(stack)) result = Feedback.CORRECT;
            else if (stack.isEmpty()) result = Feedback.NONE;
            else {
                correct = false;
                boolean any = expected.values().stream().anyMatch(p -> p.ingredient.matches(stack));
                result = any ? Feedback.SHAKE : Feedback.WRONG;
            }
            feedback.add(result);
        }
        return new Evaluation(correct, complete, rotation, feedback, expected);
    }
    private int index(int seat, int turn) { return circle == 4 ? ((seat + turn) % 4) * 2 : (seat + turn * 2) % 8; }
    public static RitualRecipe read(ResourceLocation id, JsonObject json) {
        ResourceLocation spell = ResourceLocation.parse(json.get("spell").getAsString());
        if (!spell.equals(id)) throw new IllegalArgumentException("Recipe identity must match its spell");
        List<Part> parts = new ArrayList<>();
        for (var entry : json.getAsJsonArray("parts")) {
            JsonObject p = entry.getAsJsonObject(), ingredient = p.getAsJsonObject("ingredient");
            List<ResourceLocation> items = ingredient.getAsJsonArray("items").asList().stream().map(e -> ResourceLocation.parse(e.getAsString())).toList();
            List<ResourceLocation> tags = ingredient.has("tags") ? ingredient.getAsJsonArray("tags").asList().stream().map(e -> ResourceLocation.parse(e.getAsString())).toList() : List.of();
            parts.add(new Part(p.get("seat").getAsInt(), p.get("role").getAsString(), new Ingredient(items, tags)));
        }
        return new RitualRecipe(spell, json.get("name").getAsString(), json.get("circle").getAsInt(), Integer.parseInt(json.get("color").getAsString(),16), parts,
                json.getAsJsonArray("clues").asList().stream().map(e -> e.getAsString()).toList());
    }
}
