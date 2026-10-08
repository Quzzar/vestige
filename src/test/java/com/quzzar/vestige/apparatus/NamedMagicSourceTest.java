package com.quzzar.vestige.apparatus;

import com.google.gson.JsonParser;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.TraitModifier;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.CastShaping;
import com.quzzar.vestige.magic.runtime.MagicResolution;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NamedMagicSourceTest {
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("vestige", path); }
    private static com.quzzar.vestige.magic.definition.SpellDefinition spell() {
        return SpellJson.read(id("named_fixture"), JsonParser.parseString("""
                {"traditions":["arcane"],"traits":{"amplify":1,"fire":1,"time":1},
                 "variables":{"damage":{"product":[5,{"trait":"amplify"}]},"ward_ticks":{"product":[80,{"trait":"time"}]}},
                 "costs":[{"type":"mana","amount":10}],"triggers":[{"id":"cast","event":"interact"}],
                 "effects":[{"type":"damage","values":{"amount":{"variable":"damage"}}},
                   {"type":"install_binding","target":{"selection":"self"},"binding":{"id":"ward","duration":80,
                    "lifetime":{"variable":"ward_ticks"},"charges":1,"triggers":[{"id":"hit","event":"damage_calculating"}],
                    "effects":[{"type":"reduce_pending_damage","values":{"amount":2}}]}}]}
                """).getAsJsonObject());
    }

    @Test void scrollAndWandCompilersKeepNamedVariablesAndBindingLifetimes() {
        var base = spell();
        var source = new ScrollItems.Scroll(base.id(), List.of(), CastShaping.NONE, List.of());
        var scroll = Spellshaping.compile(base, source.augments(), source.modifiers(), source.shaping());
        var wand = WandComponents.compile(base, source, WandComponents.Base.BLAZE_ROD, MagicalThreadRecipe.Type.CALLOUS);
        assertEquals(base.variables(), scroll.spell().variables());
        assertEquals(base.variables(), wand.cast().spell().variables());
        assertEquals(5, MagicResolution.resolve(scroll.spell(), scroll.modifiers()).variable(id("damage")));
        assertEquals(6, MagicResolution.resolve(wand.cast().spell(), wand.cast().modifiers()).variable(id("damage")), 1e-9);
        var original = (SpellEffects.InstallBinding) base.effects().get(1);
        var copied = (SpellEffects.InstallBinding) wand.cast().spell().effects().get(1);
        assertEquals(original.binding().lifetime(), copied.binding().lifetime());
        assertEquals(100, MagicResolution.resolve(wand.cast().spell(), List.of(new TraitModifier(id("time"), TraitModifier.Operation.MULTIPLY, 1.25)))
                .variable(id("ward_ticks")));
    }

    @Test void threadRidersPreserveTheSourceVariableDefinitions() {
        var base = spell();
        var source = new ScrollItems.Scroll(base.id(), List.of(), CastShaping.NONE, List.of());
        var wand = WandComponents.compile(base, source, WandComponents.Base.STICK, MagicalThreadRecipe.Type.SMOLDERING);
        assertEquals(base.variables(), wand.cast().spell().variables());
        assertEquals(base.source(), wand.cast().spell().source());
        assertTrue(wand.cast().spell().effects().size() >= base.effects().size());
    }
}
