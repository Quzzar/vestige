package com.quzzar.vestige.magic.presentation;

import com.google.gson.JsonParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Names only: exact aliases never replace, compile or infer magical contributions. */
public final class MagicAdjectives {
    public record Adjustment(ResourceLocation id, int degree) {
        public Adjustment {
            Objects.requireNonNull(id);
            if (degree < 1 || degree > 8) throw new IllegalArgumentException("Invalid adjective degree");
        }
        public static Adjustment spellshaping(ResourceLocation rule, int degree) {
            return new Adjustment(ResourceLocation.fromNamespaceAndPath(rule.getNamespace(), "spellshaping/" + rule.getPath()), degree);
        }
        public static Adjustment wandTip(String tip) {
            return new Adjustment(ResourceLocation.fromNamespaceAndPath("vestige", "wand_tip/" + tip), 1);
        }
    }
    private record Term(String word, int maximumDegree) { }
    private record Combination(ResourceLocation family, Map<ResourceLocation, Integer> adjustments) {
        private Combination { adjustments = Map.copyOf(adjustments); }
    }
    private record Catalog(Map<ResourceLocation, Term> terms, Map<Combination, String> combinations, String mixedWord) {
        private Catalog { terms = Map.copyOf(terms); combinations = Map.copyOf(combinations); }
    }
    private static final Catalog CATALOG = load();
    private MagicAdjectives() { }

    /** Exact full-set aliases take precedence; other mixtures have one neutral display name. */
    public static List<String> words(ResourceLocation family, List<Adjustment> adjustments) {
        Objects.requireNonNull(family);
        adjustments = List.copyOf(adjustments);
        if (adjustments.size() > 16) throw new IllegalArgumentException("Too many adjective contributions");
        var identity = new HashMap<ResourceLocation, Integer>();
        String single = null;
        for (var adjustment : adjustments) {
            var term = CATALOG.terms().get(adjustment.id());
            if (term == null || adjustment.degree() > term.maximumDegree()
                    || identity.putIfAbsent(adjustment.id(), adjustment.degree()) != null)
                throw new IllegalArgumentException("Unknown, duplicate or excessive adjective contribution");
            single = (adjustment.degree() == 1 ? "" : adjustment.degree() == 2 ? "Greater " : "Grand ") + term.word();
        }
        if (identity.isEmpty()) return List.of();
        if (identity.size() == 1) return List.of(single);
        var combined = CATALOG.combinations().get(new Combination(family, identity));
        return List.of(combined == null ? CATALOG.mixedWord() : combined);
    }

    /** The trailing space and italic style belong only to the adjustment, never the item title. */
    public static MutableComponent prefix(ResourceLocation family, List<Adjustment> adjustments) {
        var result = Component.empty();
        for (var word : words(family, adjustments))
            result.append(Component.literal(word + " ").withStyle(ChatFormatting.ITALIC));
        return result;
    }

    private static Catalog load() {
        try (var input = MagicAdjectives.class.getResourceAsStream("/data/vestige/magic_adjectives.json")) {
            if (input == null) throw new IllegalStateException("Missing magical adjective catalog");
            var root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("version").getAsInt() != 1) throw new IllegalArgumentException("Unsupported adjective catalog");
            var terms = new HashMap<ResourceLocation, Term>();
            for (var entry : root.getAsJsonArray("terms")) {
                var term = entry.getAsJsonObject();
                var id = ResourceLocation.parse(term.get("id").getAsString());
                var word = term.get("word").getAsString();
                int maximum = term.get("max_degree").getAsInt();
                if (!word.matches("[A-Z][a-z]+") || maximum < 1 || maximum > 8
                        || terms.putIfAbsent(id, new Term(word, maximum)) != null)
                    throw new IllegalArgumentException("Invalid adjective catalog term");
            }
            var combinations = new HashMap<Combination, String>();
            for (var entry : root.getAsJsonArray("combinations")) {
                var alias = entry.getAsJsonObject();
                var requirements = new HashMap<ResourceLocation, Integer>();
                alias.getAsJsonObject("requires").entrySet().forEach(e -> {
                    var id = ResourceLocation.parse(e.getKey());
                    int degree = e.getValue().getAsInt();
                    if (!terms.containsKey(id) || degree < 1 || degree > terms.get(id).maximumDegree())
                        throw new IllegalArgumentException("Invalid combined adjective contribution");
                    requirements.put(id, degree);
                });
                var key = new Combination(ResourceLocation.parse(alias.get("family").getAsString()), requirements);
                var word = alias.get("word").getAsString();
                if (requirements.size() < 2 || requirements.values().stream().mapToInt(Integer::intValue).sum() > 16
                        || !word.matches("[A-Z][a-z]+") || terms.values().stream().anyMatch(t -> t.word().equals(word))
                        || combinations.putIfAbsent(key, word) != null)
                    throw new IllegalArgumentException("Invalid combined adjective");
            }
            var mixedWord = root.get("mixed_word").getAsString();
            if (!mixedWord.matches("[A-Z][a-z]+") || terms.values().stream().anyMatch(t -> t.word().equals(mixedWord))
                    || combinations.values().contains(mixedWord))
                throw new IllegalArgumentException("Invalid mixed adjective");
            return new Catalog(terms, combinations, mixedWord);
        } catch (IOException e) { throw new java.io.UncheckedIOException(e); }
    }
}
