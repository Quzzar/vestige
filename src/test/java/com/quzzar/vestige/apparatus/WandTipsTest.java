package com.quzzar.vestige.apparatus;

import com.google.gson.*;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.CastShaping;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WandTipsTest {
    private static SpellDefinition read(String name) throws Exception {
        try(var reader=new InputStreamReader(Objects.requireNonNull(WandTipsTest.class.getResourceAsStream("/data/vestige/runtime_spells/"+name+".json")))) {
            return SpellJson.read(VestigeMainMod.location(name),JsonParser.parseReader(reader).getAsJsonObject());
        }
    }
    private static WandComponents.Compiled compile(SpellDefinition spell,WandTips.Tip tip) {
        return WandComponents.compile(spell,new ScrollItems.Scroll(spell.id(),List.of(),CastShaping.NONE,List.of()),
                WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,Optional.of(tip));
    }
    @Test void theTipIsAnAdditionalContributionAndCannotSelfQualifyBaseAffinity() throws Exception {
        var spell=read("heal");var scroll=new ScrollItems.Scroll(spell.id(),List.of(),CastShaping.NONE,List.of());
        var untipped=WandComponents.compile(spell,scroll,WandComponents.Base.LIGHTNING_ROD,MagicalThreadRecipe.Type.CALLOUS);
        var tipped=WandComponents.compile(spell,scroll,WandComponents.Base.LIGHTNING_ROD,MagicalThreadRecipe.Type.CALLOUS,Optional.of(WandTips.Tip.AMETHYST));
        assertEquals(untipped.cast().spell().effects(),tipped.cast().spell().effects());
        assertEquals(spell.traits().resolve(untipped.cast().modifiers()).rating(VestigeMainMod.location("amplify")),
                spell.traits().resolve(tipped.cast().modifiers()).rating(VestigeMainMod.location("amplify")));
        assertEquals(1,tipped.cast().spell().traits().resolve(tipped.cast().modifiers()).rating(VestigeMainMod.location("sonic")));
        assertEquals(1,tipped.wear());assertEquals(30,tipped.durability());
    }
    @Test void capabilityEligibilityIncludesCallbacksAndProtectiveBindings() throws Exception {
        var damage=read("force_arrow");var healing=read("heal");var protection=read("pf2_shield");var utility=read("pf2_detect_magic");
        var modeOnly=new SpellDefinition(utility.id(),utility.rarity(),utility.traditions(),utility.traits(),utility.costs(),utility.triggers(),utility.effects(),
                Map.of(VestigeMainMod.location("fang_mode"),new SpellMode(VestigeMainMod.location("fang_mode"),List.of(),
                        List.of(new SpellEffects.Action(VestigeMainMod.location("fangs"),Map.of(),Map.of())))),utility.source());
        assertTrue(WandTips.capabilities(modeOnly).damage());
        assertTrue(WandTips.capabilities(damage).damage());assertTrue(WandTips.capabilities(healing).heal());assertTrue(WandTips.capabilities(protection).protection());
        assertThrows(IllegalArgumentException.class,() -> compile(utility,WandTips.Tip.COPPER));
        assertThrows(IllegalArgumentException.class,() -> compile(utility,WandTips.Tip.DIAMOND));
        assertDoesNotThrow(() -> compile(protection,WandTips.Tip.IRON));
        for(var tip:List.of(WandTips.Tip.EMERALD,WandTips.Tip.ENDER_PEARL,WandTips.Tip.GHAST_TEAR,WandTips.Tip.NETHERITE)) assertDoesNotThrow(() -> compile(utility,tip));
    }
    @Test void zeroManaCannotBuyAFreeTipOrGenerateARefund() {
        var definition=new SpellDefinition(VestigeMainMod.location("fixture"),Set.of(Tradition.ARCANE),TraitProfile.empty(),List.of(),
                List.of(new SpellTrigger(VestigeMainMod.location("primary"),VestigeMainMod.location("interact"),List.of())),
                List.of(new SpellEffects.Action(VestigeMainMod.location("detect_magic"),Map.of(),Map.of())));
        assertThrows(IllegalArgumentException.class,() -> compile(definition,WandTips.Tip.EMERALD));
        for(var tip:List.of(WandTips.Tip.ENDER_PEARL,WandTips.Tip.GHAST_TEAR,WandTips.Tip.NETHERITE)) {
            var c=compile(definition,tip);assertTrue(c.cast().shaping().costs(c.cast().spell().costs()).contains(new SpellCost.Mana(1)));
        }
        var steadfast=compile(definition,WandTips.Tip.NETHERITE);
        assertTrue(steadfast.cast().shaping().costs(definition.costs()).contains(new SpellCost.Time(30)));
    }
    @Test void eachTipPersistsAndMatchesAllWholeRotationsWithSixExactOfferings() {
        var source=ScrollItems.shapedScroll(VestigeMainMod.location("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,1.1),List.of());
        for(var base:WandComponents.Base.values()) for(var thread:MagicalThreadRecipe.types()) for(var tip:WandTips.Tip.values()) for(int r=0;r<8;r+=2) {
            var inputs=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));inputs.set(r,new ItemStack(base.ingredient()));inputs.set((r+1)%8,new ItemStack(thread.item()));inputs.set((r+3)%8,new ItemStack(tip.item()));
            for(int offset:List.of(2,4,6))inputs.set((r+offset)%8,source.copy());
            var match=WandRecipe.match(inputs).orElseThrow();assertEquals(Optional.of(tip),match.tip());assertEquals(6,match.occupied().size());
            var wand=WandData.create(base,thread,Optional.of(tip),source);assertEquals(Optional.of(tip),WandData.binding(wand).orElseThrow().tip());
            assertEquals(ScrollItems.scroll(source).orElseThrow(),WandData.binding(wand).orElseThrow().scroll());
        }
    }
    @Test void fullCatalogCoverageIncludesUtilityAndEveryBaseThreadTipCombination() throws Exception {
        var directory=Path.of(Objects.requireNonNull(getClass().getResource("/data/vestige/runtime_spells")).toURI());
        var coverage=new TreeMap<String,List<String>>();int total=0,combinations=0;var utility=new ArrayList<String>();
        try(var files=Files.list(directory)) {
            for(var path:files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                var spell=read(path.getFileName().toString().replace(".json",""));total++;
                var source=new ScrollItems.Scroll(spell.id(),List.of(),CastShaping.NONE,List.of());
                var caps=WandTips.capabilities(spell);
                if(!caps.damage() && !caps.heal() && !caps.protection())utility.add(spell.id().toString());
                for(var tip:WandTips.Tip.values()) {
                    boolean compatible=false;
                    for(var base:WandComponents.Base.values()) for(var thread:MagicalThreadRecipe.types()) try {
                        var c=WandComponents.compile(spell,source,base,thread,Optional.of(tip));combinations++;compatible=true;
                        assertEquals(spell.id(),c.cast().spell().id());assertEquals(spell.source(),c.cast().spell().source());
                        assertTrue(c.wear()>=1 && c.wear()<=3);
                    } catch(IllegalArgumentException unsupported) { }
                    if(compatible)coverage.computeIfAbsent(tip.id(),key -> new ArrayList<>()).add(spell.id().toString());
                }
            }
        }
        assertTrue(total>=214);assertFalse(utility.isEmpty());
        for(var tip:WandTips.Tip.values())assertTrue(coverage.getOrDefault(tip.id(),List.of()).size()>20,tip.id());
        for(String spell:utility)for(var tip:List.of(WandTips.Tip.ENDER_PEARL,WandTips.Tip.GHAST_TEAR,WandTips.Tip.NETHERITE))assertTrue(coverage.get(tip.id()).contains(spell));
        var output=Path.of("build/native-wands/wand-tip-compatibility.json");Files.createDirectories(output.getParent());
        Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("spell_count",total,"supported_combinations",combinations,"utility_spells",utility,"compatible",coverage))+"\n");
    }
}
