package com.quzzar.vestige.magic.data;

import com.google.gson.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellCapabilities;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class SpellJsonTest {
    @Test void everyFrozenPathfinderSpellRetainsRulesProvenanceAndIndependentNativeBalance() throws Exception {
        var catalog = JsonParser.parseReader(new InputStreamReader(getClass().getResourceAsStream("/pathfinder-spells.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        var names = new HashSet<String>(); var sourceIds = new HashSet<Integer>();
        for (var entry : catalog.getAsJsonArray("spells")) {
            var row = entry.getAsJsonObject(); String name = "pf2_" + row.get("id").getAsString();
            assertTrue(names.add(name)); assertTrue(sourceIds.add(row.get("aon_id").getAsInt()));
            try (var stream = getClass().getResourceAsStream("/data/vestige/runtime_spells/" + name + ".json")) {
                assertNotNull(stream, name);
                var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                var spell = SpellJson.read(id(name), json); var source = spell.source().orElseThrow(); var ref = source.reference().orElseThrow();
                assertEquals(ResourceLocation.parse("pathfinder2e:aon/" + row.get("aon_id").getAsInt()), source.spell());
                assertEquals(row.get("revision").getAsString(), source.revision());
                assertEquals(row.get("display_name").getAsString(), source.displayName());
                assertEquals(row.get("rank").getAsInt(), ref.rank()); assertEquals(row.get("cantrip").getAsBoolean(), ref.cantrip());
                assertEquals(row.get("rarity").getAsString(), ref.rarity()); assertEquals(row.get("url").getAsString(), ref.url().toString());
                assertEquals(row.get("publication").getAsString() + " p. " + row.get("page").getAsInt(), ref.publication());
                assertEquals(row.get("edition").getAsString(), ref.edition()); assertEquals("pathfinder_second_edition", ref.system());
                assertTrue(source.school().isEmpty() && source.castType().isEmpty() && source.cooldownTicks().isEmpty(), name);
                var traditions = new HashSet<Tradition>();
                row.getAsJsonArray("traditions").forEach(t -> traditions.add(Tradition.fromId(t.getAsString()).orElseThrow()));
                assertEquals(traditions, spell.traditions(), name);
                assertFalse(SpellCapabilities.of(spell).isEmpty(), name);
                assertTrue(spell.costs().stream().anyMatch(c -> c instanceof SpellCost.Mana), name);
                assertFalse(spell.costs().stream().anyMatch(c -> c instanceof SpellCost.Cooldown), name);
                assertTrue(spell.modes().values().stream().noneMatch(mode -> mode.costs().stream().anyMatch(SpellCost.Cooldown.class::isInstance)), name);
                assertEquals(1, spell.traits().rating(id("amplify")), name);
            }
        }
        assertEquals(100, names.size());
        assertEquals(SpellRarity.MYTHIC, readDefinition("pf2_cataclysm").rarity());
        assertEquals("common", readDefinition("pf2_cataclysm").source().orElseThrow().reference().orElseThrow().rarity());
    }
    @Test void invalidRulesReferencesAndIncompleteModProvenanceAreRejected() throws Exception {
        var original = readDefinitionJson("pf2_electric_arc");
        var fractional = original.deepCopy(); fractional.getAsJsonObject("source").getAsJsonObject("reference").addProperty("rank", 1.5);
        assertThrows(JsonParseException.class, () -> SpellJson.read(id("sample"), fractional));
        var invalidRank = original.deepCopy(); invalidRank.getAsJsonObject("source").getAsJsonObject("reference").addProperty("rank", 11);
        assertThrows(IllegalArgumentException.class, () -> SpellJson.read(id("sample"), invalidRank));
        var nonBoolean = original.deepCopy(); nonBoolean.getAsJsonObject("source").getAsJsonObject("reference").addProperty("cantrip", "true");
        assertThrows(JsonParseException.class, () -> SpellJson.read(id("sample"), nonBoolean));
        var relativeUrl = original.deepCopy(); relativeUrl.getAsJsonObject("source").getAsJsonObject("reference").addProperty("url", "/spell");
        assertThrows(IllegalArgumentException.class, () -> SpellJson.read(id("sample"), relativeUrl));
        var incomplete = original.deepCopy(); incomplete.getAsJsonObject("source").remove("reference");
        assertThrows(IllegalArgumentException.class, () -> SpellJson.read(id("sample"), incomplete));
    }
    @Test void occupancyHealingAndRandomRelocationExposeTheirExecutableOutcomeCapabilities() throws Exception {
        assertTrue(SpellCapabilities.of(readDefinition("pf2_gentle_breeze")).contains(id("heal")));
        assertTrue(SpellCapabilities.of(readDefinition("pf2_flicker")).contains(id("teleport")));
        assertFalse(SpellCapabilities.of(readDefinition("pf2_detect_magic")).contains(id("damage")));
        assertFalse(SpellCapabilities.of(readDefinition("pf2_illusory_creature")).contains(id("damage")));
    }
    private SpellDefinition readDefinition(String name) throws Exception { return SpellJson.read(id(name), readDefinitionJson(name)); }
    private JsonObject readDefinitionJson(String name) throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/vestige/runtime_spells/" + name + ".json")) {
            assertNotNull(stream, name);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    @Test void everyPinnedIronSpellHasAnExecutableNativeDefinitionAndExactProvenance() throws Exception {
        var catalog = JsonParser.parseReader(new InputStreamReader(getClass().getResourceAsStream("/irons-spells.json"), StandardCharsets.UTF_8)).getAsJsonObject();
        var names = new HashSet<String>();
        for (var entry : catalog.getAsJsonArray("spells")) {
            var row = entry.getAsJsonObject(); String name = row.get("id").getAsString();
            assertTrue(names.add(name), "Duplicate source ID " + name);
            try (var stream = getClass().getResourceAsStream("/data/vestige/runtime_spells/" + name + ".json")) {
                assertNotNull(stream, name);
                var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                assertTrue(json.has("rarity"), name + " must author its rarity explicitly");
                var definition = SpellJson.read(id(name), json);
                assertEquals(json.get("rarity").getAsString(), definition.rarity().id(), name);
                assertFalse(definition.effects().isEmpty(), name);
                assertFalse(definition.costs().stream().anyMatch(SpellCost.Cooldown.class::isInstance), name);
                assertTrue(definition.modes().values().stream().noneMatch(mode -> mode.costs().stream().anyMatch(SpellCost.Cooldown.class::isInstance)), name);
                assertFalse(SpellCapabilities.of(definition).isEmpty(), name);
                var source = definition.source().orElseThrow();
                assertEquals(ResourceLocation.parse("irons_spellbooks:" + name), source.spell());
                assertEquals(ResourceLocation.parse("irons_spellbooks:" + row.get("school").getAsString()), source.school().orElseThrow());
                assertEquals(catalog.get("revision").getAsString(), source.revision());
                assertEquals(1, definition.traits().rating(id("amplify")), name);
            }
        }
        assertEquals(110, names.size());
    }
    @Test void loadsEveryAuthoredNativeCanaryAndDerivesCapabilities() throws Exception {
        for (String name : List.of("force_arrow", "summon_zombie", "arcane_lock", "interposing_earth")) {
            try (var stream = getClass().getResourceAsStream("/data/vestige/runtime_spells/" + name + ".json")) {
                assertNotNull(stream, name);
                var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                var spell = SpellJson.read(id(name), json);
                assertTrue(json.has("rarity"), name + " must author its rarity explicitly");
                assertEquals(json.get("rarity").getAsString(), spell.rarity().id());
                assertEquals(id(name), spell.id()); assertFalse(spell.effects().isEmpty());
                assertFalse(spell.costs().stream().anyMatch(SpellCost.Cooldown.class::isInstance), name);
                assertFalse(SpellCapabilities.of(spell).isEmpty(), name);
            }
        }
    }
    @Test void rejectsUnknownExecutionTypesFractionalTicksAndDuplicateTriggers() {
        JsonObject json = valid();
        json.getAsJsonArray("effects").get(0).getAsJsonObject().addProperty("type", "invented");
        JsonObject unknown = json;
        assertThrows(JsonParseException.class, () -> SpellJson.read(id("sample"), unknown));
        json = valid();
        JsonObject delay = JsonParser.parseString("{\"type\":\"delay\",\"ticks\":1.5}").getAsJsonObject();
        json.getAsJsonArray("effects").add(delay);
        JsonObject fractional = json;
        assertThrows(JsonParseException.class, () -> SpellJson.read(id("sample"), fractional));
        json = valid(); json.getAsJsonArray("triggers").add(json.getAsJsonArray("triggers").get(0).deepCopy());
        JsonObject duplicates = json;
        assertThrows(IllegalArgumentException.class, () -> SpellJson.read(id("sample"), duplicates));
    }
    @Test void castModesRemainPartOfTheSameSpell() {
        JsonObject json = valid();
        json.add("modes", JsonParser.parseString("[{\"id\":\"charged\",\"costs\":[{\"type\":\"time\",\"ticks\":40}],\"effects\":[{\"type\":\"damage\",\"values\":{\"amount\":10}}]}]"));
        var spell = SpellJson.read(id("sample"), json);
        assertEquals(1, spell.modes().size());
        assertEquals(new SpellCost.Time(40), spell.modes().get(id("charged")).costs().getFirst());
        assertTrue(SpellCapabilities.of(spell).contains(id("damage")));
    }
    @Test void cooldownCostsParseAndRejectNonpositiveOrFractionalDurations() {
        JsonObject json = valid();
        json.add("costs", JsonParser.parseString("[{\"type\":\"cooldown\",\"ticks\":60}]"));
        assertEquals(List.of(new SpellCost.Cooldown(60)), SpellJson.read(id("sample"),json).costs());
        for (double ticks : List.of(0.0,-1.0,1.5)) {
            json.getAsJsonArray("costs").get(0).getAsJsonObject().addProperty("ticks",ticks);
            assertThrows(RuntimeException.class,()->SpellJson.read(id("sample"),json));
        }
    }
    @Test void rarityDefaultsToCommonAndParsesAllFourNames() {
        assertEquals(SpellRarity.COMMON, SpellJson.read(id("sample"), valid()).rarity());
        for (SpellRarity rarity : SpellRarity.values()) {
            JsonObject json = valid(); json.addProperty("rarity", rarity.id());
            assertEquals(rarity, SpellJson.read(id("sample"), json).rarity());
        }
    }
    @Test void rarityRejectsUnsupportedNamesAndNonTextValues() {
        for (String value : List.of("legendary", "mythical", "", "Rare")) {
            JsonObject json = valid(); json.addProperty("rarity", value);
            assertThrows(JsonParseException.class, () -> SpellJson.read(id("sample"), json), value);
        }
        for (JsonElement value : List.of(new JsonPrimitive(2), new JsonPrimitive(true), JsonNull.INSTANCE, new JsonObject())) {
            JsonObject json = valid(); json.add("rarity", value);
            assertThrows(JsonParseException.class, () -> SpellJson.read(id("sample"), json));
        }
    }
    private static JsonObject valid() {
        return JsonParser.parseString("{\"traditions\":[\"arcane\"],\"traits\":{},\"triggers\":[{\"id\":\"primary\",\"event\":\"interact\"}],\"effects\":[{\"type\":\"damage\",\"values\":{\"amount\":1}}]}").getAsJsonObject();
    }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("vestige", path); }
}
