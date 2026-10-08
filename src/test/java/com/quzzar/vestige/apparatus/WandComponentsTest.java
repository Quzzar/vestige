package com.quzzar.vestige.apparatus;

import com.google.gson.*;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.CastShaping;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WandComponentsTest {
    private static ResourceLocation id(String path) { return VestigeMainMod.location(path); }
    private static SpellDefinition read(String name) throws Exception {
        try (var reader=new InputStreamReader(Objects.requireNonNull(WandComponentsTest.class.getResourceAsStream("/data/vestige/runtime_spells/"+name+".json")))) {
            return SpellJson.read(id(name),JsonParser.parseReader(reader).getAsJsonObject());
        }
    }
    private static ScrollItems.Scroll source(SpellDefinition spell) { return new ScrollItems.Scroll(spell.id(),List.of(),CastShaping.NONE,List.of()); }
    private static List<SpellCost> costs(WandComponents.Compiled compiled) { return compiled.cast().shaping().costs(compiled.cast().spell().costs()); }
    private static SpellDefinition instant() {
        return new SpellDefinition(id("fixture"),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("amplify"),1d)),List.of(),
                List.of(new SpellTrigger(id("primary"),id("interact"),List.of())),List.of(new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Trait(id("amplify"))),Map.of())));
    }
    @Test void affinityChecksSourceTraitsOnceAndDoesNotQualifyFromItsOwnCoreSeed() throws Exception {
        var fire=read("fireball");var boosted=WandComponents.compile(fire,source(fire),WandComponents.Base.BLAZE_ROD,MagicalThreadRecipe.Type.CALLOUS);
        assertEquals(1.2,fire.traits().resolve(boosted.cast().modifiers()).rating(id("amplify")),1e-9);
        var plain=instant();var smoldering=WandComponents.compile(plain,source(plain),WandComponents.Base.BLAZE_ROD,MagicalThreadRecipe.Type.SMOLDERING);
        var traits=plain.traits().resolve(smoldering.cast().modifiers());
        assertEquals(1,traits.rating(id("amplify")));assertTrue(traits.rating(id("fire"))>0);
        assertEquals(2,smoldering.wear());assertTrue(costs(smoldering).contains(new SpellCost.Mana(1)));
        var dual=new SpellDefinition(plain.id(),plain.traditions(),new TraitProfile(Map.of(id("plant"),1d,id("wood"),1d,id("amplify"),1d)),plain.costs(),plain.triggers(),plain.effects());
        var bamboo=WandComponents.compile(dual,source(dual),WandComponents.Base.BAMBOO,MagicalThreadRecipe.Type.CALLOUS);
        assertEquals(1.2,dual.traits().resolve(bamboo.cast().modifiers()).rating(id("amplify")),1e-9);
    }
    @Test void shapingCostsComposeOnceAndPreserveExplicitAuthoredCooldowns() throws Exception {
        var fire=read("fireball");
        var scroll=new ScrollItems.Scroll(fire.id(),new LeylineShaping.Modifiers(1.1,1.2,.9,.65).traits(),new CastShaping(.65,true),List.of(new Spellshaping.Selection(id("reaching"),2)));
        var compiled=WandComponents.compile(fire,scroll,WandComponents.Base.STICK,MagicalThreadRecipe.Type.ENSORCELLED);
        assertEquals(1.1,fire.traits().resolve(compiled.cast().modifiers()).rating(id("amplify")),1e-9);
        assertEquals(scroll.augments(),List.of(new Spellshaping.Selection(id("reaching"),2)));
        assertEquals(ScrollItems.scroll(ScrollItems.shapedScroll(fire.id(),new LeylineShaping.Modifiers(1.1,1.2,.9,.65),scroll.augments())).orElseThrow(),scroll);
        var expected=Spellshaping.compile(fire,scroll.augments(),scroll.modifiers(),scroll.shaping());
        assertEquals(expected.spell().effects(),compiled.cast().spell().effects());
        assertEquals(expected.modifiers(),compiled.cast().modifiers());
        double manaFactor=expected.shaping().adjustment().factors().getOrDefault("mana",1d)*.85;
        assertTrue(costs(compiled).contains(new SpellCost.Mana(Math.floor(28*manaFactor*.65+.5))));
        assertTrue(costs(compiled).contains(new SpellCost.Time(23)));
        assertTrue(costs(compiled).stream().noneMatch(SpellCost.Cooldown.class::isInstance));
        assertEquals(24,compiled.durability());assertEquals(1,compiled.wear());
        var longRecovery=new CastShaping(.65,true,new CastShaping.CostAdjustment(Map.of(),List.of(),0,0,20,0));
        assertEquals(List.of(new SpellCost.Cooldown(1950),new SpellCost.Time(20)),longRecovery.costs(List.of(new SpellCost.Cooldown(3000))));
    }
    @Test void incompatibleAndDuplicatePaidProfilesRejectBeforeCrafting() throws Exception {
        var instant=instant();
        assertThrows(IllegalArgumentException.class,() -> WandComponents.compile(instant,source(instant),WandComponents.Base.STICK,MagicalThreadRecipe.Type.ENSORCELLED));
        assertThrows(IllegalArgumentException.class,() -> WandComponents.compile(instant,source(instant),WandComponents.Base.STICK,MagicalThreadRecipe.Type.LACED));
        assertThrows(IllegalArgumentException.class,() -> WandComponents.compile(instant,source(instant),WandComponents.Base.STICK,MagicalThreadRecipe.Type.CONSECRATED));
        var fire=read("fireball");
        var kindled=new ScrollItems.Scroll(fire.id(),List.of(),CastShaping.NONE,List.of(new Spellshaping.Selection(id("kindled"),1)));
        assertThrows(IllegalArgumentException.class,() -> WandComponents.compile(fire,kindled,WandComponents.Base.STICK,MagicalThreadRecipe.Type.SMOLDERING));
        var heal=read("pf2_heal");var purifying=new ScrollItems.Scroll(heal.id(),List.of(),CastShaping.NONE,List.of(new Spellshaping.Selection(id("purifying"),1)));
        assertThrows(IllegalArgumentException.class,() -> WandComponents.compile(heal,purifying,WandComponents.Base.STICK,MagicalThreadRecipe.Type.CONSECRATED));
        var shield=read("pf2_shield");assertDoesNotThrow(() -> WandComponents.compile(shield,source(shield),WandComponents.Base.STICK,MagicalThreadRecipe.Type.CONSECRATED));
    }
    @Test void everyPackagedSpellHasAnUntippedRouteAndCoreCoverageIsAudited() throws Exception {
        var directory=Path.of(Objects.requireNonNull(getClass().getResource("/data/vestige/runtime_spells")).toURI());
        var audit=new TreeMap<String,List<String>>();int count=0;
        try (var files=Files.list(directory)) {
            for (var file:files.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                var spell=read(file.getFileName().toString().replace(".json",""));count++;
                for (var base:WandComponents.Base.values()) assertDoesNotThrow(() -> WandComponents.compile(spell,source(spell),base,MagicalThreadRecipe.Type.CALLOUS),spell.id().toString());
                for (var core:MagicalThreadRecipe.types()) {
                    try {
                        var compiled=WandComponents.compile(spell,source(spell),WandComponents.Base.STICK,core);
                        assertEquals(spell.id(),compiled.cast().spell().id());assertEquals(spell.source(),compiled.cast().spell().source());
                        audit.computeIfAbsent(core.id().toString(),ignored -> new ArrayList<>()).add(spell.id().toString());
                    } catch (IllegalArgumentException incompatible) { /* Authored executable capability rejection. */ }
                }
            }
        }
        assertTrue(count>=214);assertEquals(count,audit.get(MagicalThreadRecipe.Type.CALLOUS.id().toString()).size());
        for (var core:MagicalThreadRecipe.types()) assertFalse(audit.getOrDefault(core.id().toString(),List.of()).isEmpty());
        var output=Path.of("build/native-wands/wand-core-compatibility.json");Files.createDirectories(output.getParent());
        Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("spell_count",count,"compatible",audit))+"\n");
    }
}
