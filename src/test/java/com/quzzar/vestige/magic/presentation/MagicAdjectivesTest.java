package com.quzzar.vestige.magic.presentation;

import com.quzzar.vestige.apparatus.Spellshaping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MagicAdjectivesTest {
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("vestige", path); }
    private static MagicAdjectives.Adjustment boot(String name) { return new MagicAdjectives.Adjustment(id("wayfarer/" + name), 1); }

    @Test void allSixteenBootSetsHaveOneWordIndependentOfEncounterOrder() {
        var choices = List.of(boot("swift"), boot("enduring"), boot("reinforced"), boot("quickened"));
        var expected = List.of("", "Swift", "Enduring", "Striding", "Reinforced", "Surefooted", "Stalwart", "Dauntless",
                "Quickened", "Nimble", "Tireless", "Restless", "Dependable", "Agile", "Resolute", "Unfaltering");
        for (int mask = 0; mask < 16; mask++) {
            var selected = new ArrayList<MagicAdjectives.Adjustment>();
            for (int bit = 0; bit < 4; bit++) if ((mask & (1 << bit)) != 0) selected.add(choices.get(bit));
            var words = MagicAdjectives.words(id("wayfarer_boots"), selected);
            assertEquals(mask == 0 ? List.of() : List.of(expected.get(mask)), words);
            Collections.reverse(selected);
            assertEquals(words, MagicAdjectives.words(id("wayfarer_boots"), selected));
            assertTrue(words.stream().allMatch(w -> w.matches("[A-Z][a-z]+")));
        }
    }

    @Test void aliasesRequireTheCompleteSetAndCorrectItemFamily() {
        var selected = List.of(boot("enduring"), boot("reinforced"));
        assertEquals(List.of("Stalwart"), MagicAdjectives.words(id("wayfarer_boots"), selected));
        assertEquals(List.of("Confluent"), MagicAdjectives.words(id("other_item"), selected));
        var additional = new ArrayList<>(selected);
        additional.add(MagicAdjectives.Adjustment.spellshaping(id("reaching"), 1));
        assertEquals(List.of("Confluent"), MagicAdjectives.words(id("wayfarer_boots"), additional));
    }

    @Test void currentSpellWordsAndDegreesRemainExactIncludingMechanicalCompounds() {
        for (var rule : Spellshaping.rules().values()) for (int degree = 1; degree <= rule.maxDegree(); degree++) {
            var selection = new Spellshaping.Selection(rule.id(), degree);
            var token = MagicAdjectives.Adjustment.spellshaping(rule.id(), degree);
            assertEquals(List.of(Spellshaping.name(selection)), MagicAdjectives.words(id("spell_scroll"), List.of(token)));
        }
    }

    @Test void sharedWordDoesNotMergeIndependentScrollAndTipContributions() {
        var selected = List.of(MagicAdjectives.Adjustment.spellshaping(id("repelling"), 2), MagicAdjectives.Adjustment.wandTip("iron"));
        assertEquals(List.of("Confluent"), MagicAdjectives.words(id("wand"), selected));
        assertEquals(List.of("Forceful"), MagicAdjectives.words(id("wand"), List.of(
                MagicAdjectives.Adjustment.spellshaping(id("repelling"), 1), MagicAdjectives.Adjustment.wandTip("iron"))));
        var degree = List.of(MagicAdjectives.Adjustment.spellshaping(id("enduring"), 2), MagicAdjectives.Adjustment.spellshaping(id("focused"), 1));
        assertEquals(List.of("Confluent"), MagicAdjectives.words(id("spell_scroll"), degree));
    }

    @Test void spellAndWandNamesMatchWholeContributionsAndKeepSingleDegreeLabels() {
        var reaching = MagicAdjectives.Adjustment.spellshaping(id("reaching"), 1);
        var widening = MagicAdjectives.Adjustment.spellshaping(id("widening"), 1);
        for (String family : List.of("spell_scroll", "wand")) {
            assertEquals(List.of("Expansive"), MagicAdjectives.words(id(family), List.of(reaching, widening)));
            assertEquals(List.of("Dominating"), MagicAdjectives.words(id(family), List.of(
                    widening, MagicAdjectives.Adjustment.spellshaping(id("focused"), 1), reaching)));
            assertEquals(List.of("Confluent"), MagicAdjectives.words(id(family), List.of(
                    reaching, widening, MagicAdjectives.Adjustment.spellshaping(id("bleeding"), 1))));
        }
        assertEquals(List.of("Galvanic"), MagicAdjectives.words(id("wand"), List.of(
                MagicAdjectives.Adjustment.wandTip("copper"), MagicAdjectives.Adjustment.spellshaping(id("shocking"), 1))));
        assertEquals(List.of("Confluent"), MagicAdjectives.words(id("spell_scroll"), List.of(
                MagicAdjectives.Adjustment.wandTip("copper"), MagicAdjectives.Adjustment.spellshaping(id("shocking"), 1))));
    }

    @Test void everySpellPairAtEverySupportedDegreeHasOneOrderIndependentAdjective() {
        var rules = List.copyOf(Spellshaping.rules().values());
        for (int first = 0; first < rules.size(); first++) for (int second = first + 1; second < rules.size(); second++) {
            var a = rules.get(first); var b = rules.get(second);
            for (int da = 1; da <= a.maxDegree(); da++) for (int db = 1; db <= b.maxDegree(); db++) {
                var ta = MagicAdjectives.Adjustment.spellshaping(a.id(), da);
                var tb = MagicAdjectives.Adjustment.spellshaping(b.id(), db);
                for (String family : List.of("spell_scroll", "wand")) {
                    var words = MagicAdjectives.words(id(family), List.of(ta, tb));
                    assertEquals(1, words.size());
                    assertTrue(words.getFirst().matches("[A-Z][a-z]+"));
                    assertEquals(words, MagicAdjectives.words(id(family), List.of(tb, ta)));
                }
            }
        }
    }

    @Test void allThirtySixHourglassDesignSetsHaveSpecificNamesAndEyePaymentsReuseWords() {
        var time = List.of("", "fleeting", "enduring");
        var vessel = List.of("", "reinforced", "frugal");
        var payment = List.of("", "bloodbound", "fasting", "erudite");
        for (String t : time) for (String v : vessel) for (String p : payment) {
            var choices = java.util.stream.Stream.of(t, v, p).filter(s -> !s.isEmpty())
                    .map(s -> new MagicAdjectives.Adjustment(id("hourglass/" + s), 1)).toList();
            var words = MagicAdjectives.words(id("kairotic_hourglass"), choices);
            assertEquals(choices.isEmpty() ? 0 : 1, words.size());
            assertFalse(words.contains("Confluent"));
        }
        assertEquals(List.of("Erudite"), MagicAdjectives.words(id("homebound_eye"), List.of(
                new MagicAdjectives.Adjustment(id("homebound_eye/experience"), 1))));
    }

    @Test void duplicateUnknownAndExcessiveContributionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> MagicAdjectives.words(id("wayfarer_boots"), List.of(boot("swift"), boot("swift"))));
        assertThrows(IllegalArgumentException.class, () -> MagicAdjectives.words(id("wayfarer_boots"), List.of(new MagicAdjectives.Adjustment(id("unknown"), 1))));
        assertThrows(IllegalArgumentException.class, () -> MagicAdjectives.words(id("wayfarer_boots"), List.of(new MagicAdjectives.Adjustment(id("wayfarer/swift"), 2))));
    }

    @Test void onlyThePrefixIsItalicAndTheInputIsUnchanged() {
        var selected = List.of(boot("swift"), boot("quickened"));
        var name = MagicAdjectives.prefix(id("wayfarer_boots"), selected).append(Component.literal("Wayfarer Boots"));
        assertEquals("Nimble Wayfarer Boots", name.getString());
        assertEquals(2, name.getSiblings().size());
        assertTrue(name.getSiblings().getFirst().getStyle().isItalic());
        assertFalse(name.getSiblings().getLast().getStyle().isItalic());
        assertEquals(List.of(boot("swift"), boot("quickened")), selected);
    }
}
