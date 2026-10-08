package com.quzzar.vestige.magic.data;

import com.google.gson.JsonParser;
import com.quzzar.vestige.magic.definition.ItemAbilityDefinition;
import com.quzzar.vestige.magic.definition.TraitModifier;
import com.quzzar.vestige.magic.runtime.MagicResolution;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentFormulaTest {
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("vestige", name); }
    private static Map<String, ItemAbilityDefinition> proposals() throws Exception {
        var proposals = new LinkedHashMap<String, ItemAbilityDefinition>();
        try (var reader = new InputStreamReader(Objects.requireNonNull(EquipmentFormulaTest.class.getResourceAsStream("/ability-proposals.json")),StandardCharsets.UTF_8)) {
            for (var element : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("items")) {
                var item = element.getAsJsonObject(); var name = item.get("id").getAsString();
                proposals.put(name, SpellJson.readAbility(id("equipment/" + name), item.getAsJsonObject("definition")));
            }
        }
        return proposals;
    }
    private static double value(ItemAbilityDefinition ability, String variable, TraitModifier... modifiers) {
        return MagicResolution.resolve(ability, List.of(modifiers)).variable(id(variable));
    }
    private static TraitModifier boost(String trait, double multiplier) { return new TraitModifier(id(trait), TraitModifier.Operation.MULTIPLY, multiplier); }

    @Test void allSixProposalsUseTheNativeGrammarAndPreserveTheirReviewedBaselines() throws Exception {
        var items = proposals(); assertEquals(6, items.size());
        for (var item : items.values()) {
            assertEquals(ItemAbilityDefinition.Activation.PASSIVE, item.activation());
            assertTrue(item.triggers().isEmpty()); assertTrue(item.effects().isEmpty());
        }
        assertEquals(4, value(items.get("wardweave"), "ward_duration"));
        assertEquals(2, value(items.get("wardweave"), "protection"));
        assertEquals(.25, value(items.get("cinderweave"), "fire_reduction"));
        assertEquals(2, value(items.get("cinderweave"), "fire_cap"));
        assertEquals(3, value(items.get("wayfarer"), "burst_duration"));
        assertEquals(.2, value(items.get("wayfarer"), "movement_bonus"));
        assertEquals(4, value(items.get("wayfarer"), "landing_protection"));
        assertEquals(6, value(items.get("dawnsight"), "reveal_duration"));
        assertEquals(12, value(items.get("dawnsight"), "reveal_distance"));
        assertEquals(8, value(items.get("dawnsight"), "target_limit"));
        assertEquals(4, value(items.get("patchwork"), "pockets"));
        assertEquals(2, value(items.get("spiderstep"), "climb_speed"));
        assertEquals(2, value(items.get("spiderstep"), "mana_per_second"));
    }

    @Test void theSameTimeBoostChangesEveryDurationThatReadsTime() throws Exception {
        var items = proposals(); var time = boost("time", 1.25);
        assertEquals(5, value(items.get("wardweave"), "ward_duration", time));
        assertEquals(100, value(items.get("wardweave"), "ward_ticks", time));
        assertEquals(3.75, value(items.get("wayfarer"), "burst_duration", time));
        assertEquals(7.5, value(items.get("dawnsight"), "reveal_duration", time));
        assertEquals(12, value(items.get("dawnsight"), "reveal_distance", time));
        assertEquals(2, value(items.get("wardweave"), "protection", time));
    }

    @Test void traitPowerKeepsGrowingWhileDamageFractionsStorageAndPaymentsStayValid() throws Exception {
        var items = proposals(); var amplify = boost("amplify", 20);
        assertEquals(80, value(items.get("wardweave"), "protection", boost("force", 2), amplify));
        assertEquals(80, value(items.get("wardweave"), "ward_duration", boost("time", 20)));
        assertEquals(1, value(items.get("cinderweave"), "fire_reduction", boost("fire", 20)));
        assertEquals(40, value(items.get("cinderweave"), "fire_cap", amplify));
        assertEquals(60, value(items.get("wayfarer"), "burst_duration", boost("time", 20)));
        assertEquals(80, value(items.get("wayfarer"), "landing_protection", amplify));
        assertEquals(240, value(items.get("dawnsight"), "reveal_distance", boost("range", 20)));
        assertEquals(4, value(items.get("patchwork"), "pockets", boost("space", 20), amplify));
        assertEquals(25, value(items.get("patchwork"), "mana_bonus", amplify));
        assertEquals(40, value(items.get("spiderstep"), "climb_speed", boost("motion", 20)));
        assertEquals(2, value(items.get("spiderstep"), "mana_per_second", boost("motion", 20)));
    }
}
