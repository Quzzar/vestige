package com.quzzar.vestige.apparatus;

import com.google.gson.*;
import com.quzzar.vestige.magic.condition.*;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SpellshapingTest {
    private static ResourceLocation id(String s){return ResourceLocation.fromNamespaceAndPath("vestige",s);}
    private static SpellDefinition read(String s)throws Exception{try(var reader=new InputStreamReader(Objects.requireNonNull(SpellshapingTest.class.getResourceAsStream("/data/vestige/runtime_spells/"+s+".json")))){return SpellJson.read(id(s),JsonParser.parseReader(reader).getAsJsonObject());}}
    private static Spellshaping.Selection selection(String s,int degree){return new Spellshaping.Selection(id(s),degree);}
    private static Spellshaping.Pair pair(String ingredient,String material){return new Spellshaping.Pair(ResourceLocation.withDefaultNamespace(ingredient),ResourceLocation.withDefaultNamespace(material));}
    @Test void onlyAuthoredImbuementMaterialsQualifyIncludingCompoundOnlyMaterials(){
        var expected=Set.of("amethyst_block","blue_ice","bone_block","bookshelf","copper_block",
                "diamond_block","emerald_block","end_stone","glowstone","gold_block","honey_block",
                "iron_block","lapis_block","lodestone","magma_block","moss_block","quartz_block",
                "quartz_pillar","redstone_block","sculk","sea_lantern","slime_block","soul_sand",
                "stone","white_concrete","white_wool");
        var authored=new HashSet<ResourceLocation>();
        Spellshaping.rules().values().forEach(rule -> rule.pairs().forEach(pair -> authored.add(pair.material())));
        assertEquals(expected.stream().map(ResourceLocation::withDefaultNamespace).collect(java.util.stream.Collectors.toSet()),authored);
        for(var name:expected)assertTrue(Spellshaping.isImbuementMaterial(ResourceLocation.withDefaultNamespace(name)),name);
        for(var name:List.of("dirt","oak_planks","stone_bricks","red_carpet","blue_concrete_powder","diamond","amethyst_shard","air"))
            assertFalse(Spellshaping.isImbuementMaterial(ResourceLocation.withDefaultNamespace(name)),name);
        assertFalse(Spellshaping.isImbuementMaterial(id("plinth")));
    }
    @Test void allWoolAndConcreteColorsSelectIdenticalEffectsWithoutChangingOfferingIdentity()throws Exception{
        var base=read("fireball");var utility=read("pf2_detect_magic");
        for(var color:DyeColor.values()) {
            for(var type:List.of("wool","concrete"))
                assertTrue(Spellshaping.isImbuementMaterial(ResourceLocation.withDefaultNamespace(color.getName()+"_"+type)));
            assertEquals(List.of(selection("quieting",1)),Spellshaping.resolve(Map.of(0,pair("paper",color.getName()+"_wool")),base));
            var colored=Spellshaping.resolve(Map.of(0,pair("paper",color.getName()+"_concrete")),utility);
            var white=Spellshaping.resolve(Map.of(0,pair("paper","white_concrete")),utility);
            assertEquals(List.of(selection("veiled",1)),colored);assertEquals(white,colored);
            assertEquals(Spellshaping.compile(utility,white,List.of(),new CastShaping(1,true)),
                    Spellshaping.compile(utility,colored,List.of(),new CastShaping(1,true)));
        }
        assertEquals(ResourceLocation.withDefaultNamespace("red_wool"),pair("red_wool","blue_wool").offering());
        assertFalse(Spellshaping.isImbuementMaterial(ResourceLocation.fromNamespaceAndPath("foreign","red_wool")));
    }
    @Test void everyExecutableRuleHasACompatibleUnchangedBaseRecipe()throws Exception{
        var found=new TreeMap<String,List<String>>();var recipes=new ArrayList<RitualRecipe>();
        try(var files=Files.list(Path.of(Objects.requireNonNull(getClass().getResource("/data/vestige/ritual_recipes")).toURI()))){for(var p:files.sorted().toList())try(var r=Files.newBufferedReader(p)){recipes.add(RitualRecipe.read(id(p.getFileName().toString().replace(".json","")),JsonParser.parseReader(r).getAsJsonObject()));}}
        for(var rule:Spellshaping.rules().values()){
            var matches=new ArrayList<String>();
            for(var recipe:recipes){var spell=read(recipe.spell().getPath());if(!Spellshaping.compatible(rule,spell))continue;
                List<List<Spellshaping.Pair>> routes=rule.compound() ? List.of(rule.pairs()) : rule.pairs().stream().map(List::of).toList();
                for(var route:routes){var nodes=new HashMap<Integer,Spellshaping.Pair>();boolean okay=true;
                    for(var pair:route){var part=recipe.parts().stream().filter(p->p.ingredient().items().contains(pair.offering()) && !nodes.containsKey(recipe.circle()==4 ? p.seat()*2 : p.seat())).findFirst();
                        if(part.isEmpty()){okay=false;break;}nodes.put(recipe.circle()==4 ? part.get().seat()*2 : part.get().seat(),pair);}
                    if(!okay)continue;var selections=Spellshaping.resolve(nodes,spell);assertEquals(List.of(selection(rule.id().getPath(),1)),selections,rule.id().toString());
                    var compiled=Spellshaping.compile(spell,selections,List.of(),new CastShaping(1,true));assertEquals(spell.id(),compiled.spell().id());assertEquals(spell.traits(),compiled.spell().traits());assertEquals(spell.source(),compiled.spell().source());matches.add(recipe.spell().getPath());break;
                }
            }
            found.put(rule.id().getPath(),matches);
        }
        Files.writeString(Path.of("spellshaping-compatibility.json"),new GsonBuilder().setPrettyPrinting().create().toJson(found));
        assertEquals(56,found.size());assertEquals(List.of(),found.entrySet().stream().filter(e->e.getValue().isEmpty()).map(Map.Entry::getKey).toList(),"A rule has no playable route");
    }
    @Test void automaticLocalPairingsMixWithoutCopyingReferenceOrBaseTraits()throws Exception{
        var base=read("fireball");var chosen=Spellshaping.resolve(Map.of(0,pair("blaze_rod","end_stone"),2,pair("gunpowder","bone_block"),4,pair("emerald","copper_block")),base);
        assertEquals(List.of(selection("bleeding",1),selection("reaching",1),selection("shocking",1)),chosen);
        var compiled=Spellshaping.compile(base,chosen,List.of(),new CastShaping(1,true));assertEquals(1.3,base.traits().resolve(compiled.modifiers()).rating(id("range")),1e-9);
        assertEquals(1,base.traits().rating(id("range")));assertEquals(39,compiled.shaping().costs(base.costs()).stream().filter(c->c instanceof SpellCost.Mana).map(c->((SpellCost.Mana)c).amount()).findFirst().orElseThrow());
        assertThrows(IllegalArgumentException.class,()->Spellshaping.compile(read("heal"),List.of(selection("reaching",1)),List.of(),new CastShaping(1,true)));
        assertFalse(Spellshaping.validSelections(List.of(selection("quieting",2))));assertFalse(Spellshaping.validSelections(List.of(selection("missing",1))));
    }
    @Test void compoundConsumesItsPiecesAndRepeatedContributionsUseTraitResolution()throws Exception{
        var base=read("ray_of_siphoning");var rule=Spellshaping.rules().get(id("vampiric"));var nodes=new HashMap<Integer,Spellshaping.Pair>();for(int i=0;i<rule.pairs().size();i++)nodes.put(i,rule.pairs().get(i));
        assertEquals(List.of(selection("vampiric",1)),Spellshaping.resolve(nodes,base));
        var greater=Spellshaping.compile(read("fireball"),List.of(selection("shocking",2)),List.of(),new CastShaping(1,true));
        assertEquals(1.15*1.15,greater.spell().traits().resolve(greater.modifiers()).rating(id("lightning")),1e-9);assertEquals("Greater Shocking",Spellshaping.name(selection("shocking",2)));assertEquals("Grand Shocking",Spellshaping.name(selection("shocking",3)));
    }
    private static SpellDefinition direct(List<SpellEffect> plan){return new SpellDefinition(id("test"),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("amplify"),1d,id("range"),1d,id("blood"),4d)),List.of(new SpellCost.Mana(10)),List.of(new SpellTrigger(id("cast"),SpellTriggerTypes.INTERACT,List.of())),plan);}
    @Test void saturatedDeliveryRejectsRatherThanChargingForNoBenefit()throws Exception{
        var original=read("fireball");
        var effects=Spellshaping.walk(original.effects(),e->{
            if(e instanceof SpellEffects.CreateManifestation c && c.manifestation().kind().getPath().equals("projectile")){
                var m=c.manifestation();var values=new HashMap<>(m.values());values.put("homing",new SpellValue.Constant(.8));
                return new SpellEffects.CreateManifestation(new SpellEffects.Manifestation(m.kind(),m.durationTicks(),values,m.identifiers(),m.bindings(),m.onHit(),m.onTick(),m.onEnd(),m.interval(),m.visual()),c.target());
            }return e;
        });
        var saturated=new SpellDefinition(original.id(),original.rarity(),original.traditions(),original.traits(),original.costs(),original.triggers(),effects,original.modes(),original.source());
        assertThrows(IllegalArgumentException.class,()->Spellshaping.compile(saturated,List.of(selection("seeking",1)),List.of(),new CastShaping(1,true)));
        assertDoesNotThrow(()->Spellshaping.compile(original,List.of(selection("seeking",1)),List.of(),new CastShaping(1,true)));
    }
    private static SpellEffects.Action damage(double n){return new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Constant(n)),Map.of());}
    @Test void positivePrimaryContactsHaveSharedFiniteBudgetsAndNoMissProcs(){
        var world=new World();for(int i=0;i<10;i++)world.targets.add(new SpellSubject.Entity(UUID.randomUUID()));world.targets.add(world.targets.getFirst());
        var plan=direct(List.of(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.NEARBY_ENTITIES,new SpellValue.Constant(10)),List.of(damage(20)))));
        var compiled=Spellshaping.compile(plan,List.of(selection("shocking",1),selection("bleeding",1)),List.of(),new CastShaping(1,true));var runtime=new SpellRuntime(world);
        var cast=runtime.cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,world.actor,null),compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());
        for(int i=0;i<90;i++)runtime.tick();assertEquals(SpellRuntime.Status.COMPLETED,cast.status());assertEquals(11,world.primary.size());assertEquals(32,world.secondary.size());assertEquals(8,world.secondary.stream().filter(d->d.amount==2).count());
        assertTrue(world.secondary.stream().allMatch(d->d.root.equals(world.root)));assertEquals(1,world.payments);
        var blocked=new World();blocked.blocked=true;blocked.targets.add(world.targets.getFirst());var r2=new SpellRuntime(blocked);var c2=r2.cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,blocked.actor,null),compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());for(int i=0;i<90;i++)r2.tick();assertEquals(SpellRuntime.Status.COMPLETED,c2.status());assertTrue(blocked.secondary.isEmpty());
    }
    @Test void delayedRidersKeepActualContactFactsRatherThanLaterHits(){
        var world=new World();var compiled=Spellshaping.compile(direct(List.of(damage(20),damage(3))),List.of(selection("echoing",1)),List.of(),new CastShaping(1,true));var runtime=new SpellRuntime(world);
        runtime.cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,world.actor,null),compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());for(int i=0;i<30;i++)runtime.tick();
        // One initial contact per recipient is the rule's budget: the second impact cannot schedule another echo.
        assertEquals(List.of(4d),world.secondary.stream().map(Damage::amount).toList());assertEquals(3d,world.lastPrimary);
    }
    @Test void enduringChangesBackingLifetimeAndRuntimeExpiryTogether(){
        int[] duration={0};boolean[] closed={false};
        var world=new World(){
            @Override public Optional<ManifestationHandle> manifest(SpellEffects.Manifestation definition,Map<String,Double> values,SpellRuntime.Context context){
                duration[0]=definition.durationTicks();return Optional.of(new ManifestationHandle(){
                    public SpellSubject subject(){return context.target();}public boolean alive(){return !closed[0];}
                    public void close(SpellRuntime.EndReason reason){assertEquals(SpellRuntime.EndReason.EXPIRED,reason);closed[0]=true;}
                });
            }
        };
        var manifestation=new SpellEffects.Manifestation(id("status"),10,Map.of(),Map.of(),List.of(),List.of(),List.of(),List.of(),1,Optional.empty());
        var base=direct(List.of(new SpellEffects.CreateManifestation(manifestation,new TargetSpec(TargetSpec.Selection.CURRENT,new SpellValue.Constant(0)))));
        var compiled=Spellshaping.compile(base,List.of(selection("enduring",1)),List.of(),new CastShaping(1,true));var runtime=new SpellRuntime(world);
        runtime.cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,world.actor,null),compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());
        assertEquals(12,duration[0]);for(int i=0;i<11;i++)runtime.tick();assertFalse(closed[0]);runtime.tick();assertTrue(closed[0]);
    }
    @Test void newRoutesRequireRealCompatibleOutcomes()throws Exception{
        var revealing=Spellshaping.rules().get(id("revealing"));var anchored=Spellshaping.rules().get(id("anchored"));var reflecting=Spellshaping.rules().get(id("reflecting"));
        assertTrue(Spellshaping.compatible(revealing,read("pf2_detect_magic")));
        assertTrue(Spellshaping.compatible(revealing,read("pf2_revealing_light")));
        assertFalse(Spellshaping.compatible(revealing,read("fireball")));
        assertTrue(Spellshaping.compatible(anchored,read("gravity_fissure")));
        assertTrue(Spellshaping.compatible(anchored,read("planar_sight")));
        assertFalse(Spellshaping.compatible(anchored,read("pf2_cinder_swarm")),"A target-locked swarm must not become a cosmetic anchor");
        assertFalse(Spellshaping.compatible(anchored,read("shield")),"An already stationary barrier has nothing to anchor");
        assertTrue(Spellshaping.compatible(reflecting,read("pf2_glass_shield")));
        assertFalse(Spellshaping.compatible(reflecting,read("fireball")));
        assertEquals(List.of(selection("revealing",1)),Spellshaping.resolve(Map.of(0,pair("compass","glowstone")),read("pf2_detect_magic")));
    }
    @Test void anchoringStopsTheFieldOriginAndRetainsItsActualPulse()throws Exception{
        var base=read("gravity_fissure");var compiled=Spellshaping.compile(base,List.of(selection("anchored",1)),List.of(),new CastShaping(1,true));
        var before=(SpellEffects.CreateManifestation)base.effects().getFirst();var after=(SpellEffects.CreateManifestation)compiled.spell().effects().getFirst();
        assertEquals(.2,before.manifestation().values().get("motion").resolve(base.traits()),1e-9);
        assertEquals(0,after.manifestation().values().get("motion").resolve(base.traits()));
        assertEquals(before.manifestation().onTick(),after.manifestation().onTick());
        assertEquals(before.manifestation().durationTicks(),after.manifestation().durationTicks());
        assertEquals(before.target(),after.target());
    }
    @Test void inspectionExtensionsAreSecondaryAndShareContactsAcrossCallbacks(){
        int[] scans={0},contacts={0};var identities=java.util.stream.IntStream.range(0,12).mapToObj(i->UUID.randomUUID()).toList();
        var world=new World(){public boolean execute(SpellEffects.Action a,SpellRuntime.Context c){
            if(a.type().getPath().equals("reveal_hidden")){
                assertTrue(c.cause().secondary());scans[0]++;
                for(var identity:identities)if(c.claimContact(identity,id("reveal_hidden"),1,8))contacts[0]++;
            }return true;
        }};
        var detect=new SpellEffects.Action(id("detect_magic"),Map.of(),Map.of());
        var base=direct(List.of(detect,detect));var compiled=Spellshaping.compile(base,List.of(selection("revealing",1)),List.of(),new CastShaping(1,true));
        var runtime=new SpellRuntime(world);runtime.cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,world.actor,null),compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());
        assertEquals(1,scans[0]);assertEquals(8,contacts[0]);
    }
    private record Damage(double amount,UUID root){ }
    private static class World implements SpellWorld {
        final UUID actor=UUID.randomUUID();final List<SpellSubject> targets=new ArrayList<>();final List<Damage> primary=new ArrayList<>(),secondary=new ArrayList<>();UUID root;int payments;boolean blocked;double lastPrimary;
        public ConditionContext conditions(SpellRuntime.Context c){return new ConditionContext(){public Optional<ConditionValue> value(ResourceLocation p){return Optional.empty();}public boolean isTagged(ResourceLocation p,ResourceLocation q){return false;}public boolean matches(ResourceLocation p,ResourceLocation q){return false;}public double random(){return .99;}};}
        public List<SpellSubject> select(TargetSpec t,SpellRuntime.Context c){return targets.isEmpty() ? List.of(c.target()) : targets;}
        public boolean pay(List<SpellCost> costs,SpellRuntime.Context c){payments++;return true;}
        public boolean active(SpellRuntime.Context c){return true;}
        public void forfeit(SpellRuntime.Context c){throw new AssertionError();}
        public boolean execute(SpellEffects.Action a,SpellRuntime.Context c){if(!a.type().getPath().equals("damage"))return true;double amount=c.gameplayValue("amount",a.values().get("amount"));c.setNumber(id("last_damage"),blocked ? 0 : amount);
            if(c.cause().secondary())secondary.add(new Damage(amount,c.cause().rootId()));else{root=c.cause().rootId();lastPrimary=amount;primary.add(new Damage(amount,root));}return true;}
        public Optional<ManifestationHandle> manifest(SpellEffects.Manifestation d,Map<String,Double> v,SpellRuntime.Context c){return Optional.empty();}
    }
}
