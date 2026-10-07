package com.quzzar.vestige.apparatus;

import com.google.gson.*;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.*;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.CastShaping;
import com.quzzar.vestige.magic.condition.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.UnaryOperator;

/** Compiles trusted reusable rules into an immutable cast view. Items retain only rule IDs and degrees. */
public final class Spellshaping {
    public record Selection(ResourceLocation id,int degree) {
        public Selection { if (id==null || degree<1 || degree>8) throw new IllegalArgumentException("Invalid augment selection"); }
    }
    public record Pair(ResourceLocation offering,ResourceLocation material) {
        public Pair { material=ImbuementMaterials.canonical(material); }
    }
    public record Rule(ResourceLocation id,String name,String description,List<Pair> pairs,boolean compound,Set<String> capabilities,Set<String> reads,
                       List<TraitModifier> traits,Map<String,Double> costFactors,List<SpellCost> additional,double healthExchange,double hungerExchange,
                       String attachment,List<SpellEffect> effects,Set<String> kinds,Map<String,SpellValue> parameters,Map<String,Double> defaults,
                       boolean lifetime,int delay,boolean quiet,Set<String> seeds,int maxDegree,
                       Set<String> afterActions,Set<String> afterStatuses,boolean anchor) {
        public Rule {
            pairs=List.copyOf(pairs);capabilities=Set.copyOf(capabilities);reads=Set.copyOf(reads);traits=List.copyOf(traits);
            costFactors=Map.copyOf(costFactors);additional=List.copyOf(additional);effects=List.copyOf(effects);kinds=Set.copyOf(kinds);
            parameters=Map.copyOf(parameters);defaults=Map.copyOf(defaults);seeds=Set.copyOf(seeds);
            afterActions=Set.copyOf(afterActions);afterStatuses=Set.copyOf(afterStatuses);
            new CastShaping.CostAdjustment(costFactors,additional,healthExchange,hungerExchange);
            if (id==null || name.isBlank() || description.isBlank() || pairs.isEmpty() || pairs.size()>8 || maxDegree<1 || maxDegree>8
                    || delay<0 || delay>1200 || !Set.of("none","damage","heal","before","protection","after").contains(attachment)
                    || attachment.equals("after") && afterActions.isEmpty() && afterStatuses.isEmpty()
                    || !parameters.keySet().containsAll(defaults.keySet()) || parameters.size()>8
                    || compound && pairs.size()<2 || defaults.values().stream().anyMatch(v->!Double.isFinite(v) || v<0)) throw new IllegalArgumentException("Invalid Spellshaping rule");
        }
    }
    public record Compiled(SpellDefinition spell,List<TraitModifier> modifiers,CastShaping shaping) { }
    private static final Map<ResourceLocation,Rule> RULES=load();
    private static final Set<ResourceLocation> IMBUEMENT_MATERIALS=RULES.values().stream()
            .flatMap(rule -> rule.pairs().stream()).map(Pair::material)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    private Spellshaping() { }
    public static Map<ResourceLocation,Rule> rules() { return RULES; }
    public static boolean isImbuementMaterial(ResourceLocation item) { return IMBUEMENT_MATERIALS.contains(ImbuementMaterials.canonical(item)); }
    public static String name(Selection selection) { return (selection.degree()==1 ? "" : selection.degree()==2 ? "Greater " : "Grand ")+RULES.get(selection.id()).name(); }

    public static List<Selection> resolve(RitualInputs inputs,SpellDefinition spell) {
        Map<Integer,Pair> nodes=new LinkedHashMap<>();
        for (var n:inputs.nodes()) if (!n.offering().isEmpty() && !n.material().isEmpty()) nodes.put(n.seat(),new Pair(BuiltInRegistries.ITEM.getKey(n.offering().getItem()),BuiltInRegistries.ITEM.getKey(n.material().getItem())));
        return resolve(nodes,spell);
    }
    /** The pure matching seam also audits every shipped recipe without loading item registries. */
    public static List<Selection> resolve(Map<Integer,Pair> nodes,SpellDefinition spell) {
        if (nodes.size()>8 || nodes.keySet().stream().anyMatch(i->i<0 || i>7)) throw new IllegalArgumentException("Invalid active nodes");
        var ordered=new TreeMap<>(nodes);var counts=new TreeMap<ResourceLocation,Integer>(Comparator.comparing(Object::toString));var consumed=new HashSet<Integer>();
        var compounds=RULES.values().stream().filter(Rule::compound).sorted(Comparator.<Rule>comparingInt(r->r.pairs().size()).reversed().thenComparing(r->r.id().toString())).toList();
        for (var rule:compounds) while (true) {
            var found=new ArrayList<Integer>();
            for (var pair:rule.pairs()) {
                var seat=ordered.entrySet().stream().filter(n->!consumed.contains(n.getKey()) && !found.contains(n.getKey()) && n.getValue().equals(pair)).map(Map.Entry::getKey).findFirst();
                if (seat.isEmpty()) break;found.add(seat.get());
            }
            if (found.size()!=rule.pairs().size()) break;
            requireCompatible(rule,spell);consumed.addAll(found);counts.merge(rule.id(),1,Integer::sum);
        }
        for (var node:ordered.entrySet()) {
            if (consumed.contains(node.getKey())) continue;
            var rule=RULES.values().stream().filter(r->!r.compound() && r.pairs().contains(node.getValue())).findFirst().orElse(null);
            if (rule==null) continue;
            requireCompatible(rule,spell);counts.merge(rule.id(),1,Integer::sum);
        }
        var result=counts.entrySet().stream().map(e->new Selection(e.getKey(),e.getValue())).toList();
        if (!validSelections(result)) throw new IllegalArgumentException("An augment exceeds its supported degree");
        return result;
    }
    public static boolean validSelections(List<Selection> selections) {
        return selections.size()<=8 && selections.stream().map(Selection::id).distinct().count()==selections.size()
                && selections.stream().allMatch(s->RULES.containsKey(s.id()) && s.degree()<=RULES.get(s.id()).maxDegree())
                && selections.stream().mapToInt(s->s.degree()*(RULES.get(s.id()).compound() ? RULES.get(s.id()).pairs().size() : 1)).sum()<=8;
    }
    public static Compiled compile(SpellDefinition base,List<Selection> selections,List<TraitModifier> geometry,CastShaping shaping) {
        if (!validSelections(selections) || !shaping.adjustment().equals(CastShaping.CostAdjustment.NONE)) throw new IllegalArgumentException("Invalid stored Spellshaping");
        List<SpellEffect> plan=base.effects();var modes=new LinkedHashMap<>(base.modes());var modifiers=new ArrayList<>(geometry);
        Map<String,Double> factors=new HashMap<>();List<SpellCost> additional=new ArrayList<>();double health=0,hunger=0;var seeded=new HashSet<String>();
        for (var selected:selections.stream().sorted(Comparator.comparing(s->s.id().toString())).toList()) {
            var rule=RULES.get(selected.id());requireCompatible(rule,base);
            for (String trait:rule.seeds()) if (seeded.add(trait) && base.traits().rating(id(trait))==0) modifiers.add(new TraitModifier(id(trait),TraitModifier.Operation.ADD,1));
            for (int degree=0;degree<selected.degree();degree++) {
                modifiers.addAll(rule.traits());rule.costFactors().forEach((kind,value)->factors.merge(kind,value,(a,b)->a*b));
                additional.addAll(rule.additional());health+=rule.healthExchange();hunger+=rule.hungerExchange();
            }
            plan=transform(plan,rule,base);
            modes.replaceAll((key,mode)->new SpellMode(key,mode.costs(),transform(mode.effects(),rule,base)));
        }
        if(selections.stream().anyMatch(s->Set.of("forked","piercing").contains(s.id().getPath()))){
            UnaryOperator<SpellEffect> budget=e->e instanceof SpellEffects.Action a && Set.of("damage","weapon_damage").contains(a.type().getPath()) ? new SpellEffects.Limited(id("spellshaping_primary/delivery"),1,8,List.of(a)) : e;
            plan=walk(plan,budget);modes.replaceAll((key,mode)->new SpellMode(key,mode.costs(),walk(mode.effects(),budget)));
        }
        // Payment channels share a maximum 75% exchanged portion; duplicates still contribute trait operations.
        double sum=health+hunger;if(sum>.75){health*=.75/sum;hunger*=.75/sum;}
        var payment=new CastShaping(shaping.castingCost(),shaping.roundAmounts(),new CastShaping.CostAdjustment(factors,additional,health,hunger));
        payment.costs(base.costs());modes.values().forEach(m->payment.costs(m.costs())); // Validate before a ritual consumes anything.
        return new Compiled(new SpellDefinition(base.id(),base.rarity(),base.traditions(),base.traits(),base.costs(),base.triggers(),plan,modes,base.source()),List.copyOf(modifiers),payment);
    }
    /** Trusted equipment contributions compose with an already compiled scroll; they are not stored augments. */
    static Compiled contribute(Compiled source,SpellDefinition base,Rule rule) {
        requireCompatible(rule,source.spell());
        var modifiers=new ArrayList<>(source.modifiers());
        var traits=source.spell().traits().resolve(modifiers);
        for (var seed:rule.seeds()) if (traits.rating(id(seed))==0)
            modifiers.add(new TraitModifier(id(seed),TraitModifier.Operation.ADD,1));
        modifiers.addAll(rule.traits());
        var costs=source.shaping().adjustment();
        var factors=new HashMap<>(costs.factors());
        rule.costFactors().forEach((kind,value)->factors.merge(kind,value,(a,b)->a*b));
        var additional=new ArrayList<>(costs.additional()); additional.addAll(rule.additional());
        var shaping=new CastShaping(source.shaping().castingCost(),source.shaping().roundAmounts(),
                new CastShaping.CostAdjustment(factors,additional,costs.healthFraction(),costs.hungerFraction(),
                        costs.additionalPreparationTicks(),costs.minimumMana()));
        var definition=source.spell();var modes=new LinkedHashMap<>(definition.modes());
        modes.replaceAll((key,mode)->new SpellMode(key,mode.costs(),transform(mode.effects(),rule,base)));
        return new Compiled(new SpellDefinition(definition.id(),definition.rarity(),definition.traditions(),definition.traits(),definition.costs(),
                definition.triggers(),transform(definition.effects(),rule,base),modes,definition.source()),List.copyOf(modifiers),shaping);
    }
    public static String paymentText(List<SpellCost> costs) {
        return costs.stream().map(c->switch(c){
            case SpellCost.Mana m->String.format(Locale.ROOT,"%.0f mana",m.amount());
            case SpellCost.Health h->String.format(Locale.ROOT,"%.0f hearts",h.amount()/2);
            case SpellCost.Hunger h->h.amount()+" hunger";
            case SpellCost.Time t->String.format(Locale.ROOT,"%.2g s charge",t.ticks()/20d);
            case SpellCost.Cooldown recovery->String.format(Locale.ROOT,"%.2g s recovery",recovery.ticks()/20d);
            case SpellCost.Material m->m.amount()+" "+m.item().getPath().replace('_',' ')+(m.operation()==SpellCost.Material.Operation.DAMAGE ? " durability" : "");
        }).collect(java.util.stream.Collectors.joining(" · "));
    }
    private static void requireCompatible(Rule rule,SpellDefinition spell) {
        if (!compatible(rule,spell)) throw new IllegalArgumentException(rule.name()+" is incompatible with this spell's supported outcomes");
    }
    public static boolean compatible(Rule rule,SpellDefinition spell) {
        // Scrolls cast the primary plan. A compatible mode alone cannot justify a primary named no-op.
        Set<String> capabilities=new HashSet<>();SpellCapabilities.ofPlan(spell.effects()).forEach(i->capabilities.add(i.getPath()));
        var reads=new HashSet<String>();var leaves=new HashSet<String>();var parameters=new HashSet<String>();var statuses=new HashSet<ResourceLocation>();var flags=new boolean[4];
        walk(spell.effects(),e->{
            collectReads(e,reads);
            if(e instanceof SpellEffects.Action a){leaves.add(a.type().getPath());if(a.type().getPath().equals("status"))statuses.add(a.identifiers().get("effect"));if(rule.kinds().contains(a.type().getPath()))collectParameters(a.values(),rule,spell,parameters);if(matchesAfter(a,rule))flags[2]=true;}
            if(e instanceof SpellEffects.CreateManifestation c){var m=c.manifestation();if(rule.kinds().contains(m.kind().getPath()))collectParameters(m.values(),rule,spell,parameters);if(m.durationTicks()>0 && m.bindings().isEmpty() && LIFETIME_KINDS.contains(m.kind().getPath()))flags[0]=true;if(anchorable(m,spell))flags[3]=true;}
            if(e instanceof SpellEffects.ForEach f && rule.kinds().contains(f.target().selection().name().toLowerCase(Locale.ROOT))) collectParameters(f.target().options(),rule,spell,parameters);
            if(hasSound(e))flags[1]=true;return e;
        });
        if (!rule.capabilities().isEmpty() && Collections.disjoint(rule.capabilities(),capabilities) || !reads.containsAll(rule.reads()))return false;
        if(rule.id().getPath().equals("hurried") && spell.costs().stream().noneMatch(c->c instanceof SpellCost.Time t && t.ticks()>0))return false;
        if(rule.id().getPath().equals("veiled") && capabilities.contains("damage"))return false;
        if(rule.attachment().equals("damage") && Collections.disjoint(leaves,Set.of("damage","weapon_damage")))return false;
        if(rule.attachment().equals("heal") && !leaves.contains("heal"))return false;
        if(rule.attachment().equals("protection") && !hasProtection(spell.effects(),true))return false;
        if(rule.attachment().equals("after") && !flags[2] || rule.anchor() && !flags[3])return false;
        if(rule.lifetime() && !flags[0] || rule.quiet() && !flags[1])return false;
        if(!parameters.containsAll(rule.parameters().keySet()))return false;
        if((rule.healthExchange()>0 || rule.hungerExchange()>0 || rule.costFactors().containsKey("mana")) && spell.costs().stream().noneMatch(c->c instanceof SpellCost.Mana m && m.amount()>0))return false;
        if(rule.id().getPath().equals("kindled") && leaves.contains("ignite"))return false;
        var addedStatuses=new HashSet<ResourceLocation>();walk(rule.effects(),e->{if(e instanceof SpellEffects.Action a && a.type().getPath().equals("status"))addedStatuses.add(a.identifiers().get("effect"));return e;});
        return rule.compound() || Collections.disjoint(statuses,addedStatuses);
    }
    private static boolean entityTarget(TargetSpec target,boolean inherited) {
        return switch(target.selection()) {
            case SELF,ENTITY_RAY,ANY_ENTITY_RAY,NEARBY_ENTITIES,NEAR_TARGET,BEAM,CONE,CHAIN,MELEE,EVENT_TARGET,EVENT_ATTACKER -> true;
            case CURRENT -> inherited;
            default -> false;
        };
    }
    private static boolean protective(SpellEffects.Manifestation m) {
        return Set.of("guard","barrier").contains(m.kind().getPath()) || m.bindings().stream().anyMatch(b->SpellCapabilities.ofPlan(b.effects()).stream().anyMatch(c->Set.of("reduce_pending_damage","defer_pending_damage").contains(c.getPath())));
    }
    private static boolean hasProtection(List<SpellEffect> plan,boolean entity) {
        for(var e:plan){
            if(e instanceof SpellEffects.CreateManifestation c && protective(c.manifestation()) && entityTarget(c.target(),entity))return true;
            if(e instanceof SpellEffects.ForEach f && hasProtection(f.effects(),entityTarget(f.target(),entity)))return true;
            if(e instanceof SpellEffects.Sequence s && hasProtection(s.effects(),entity))return true;
            if(e instanceof SpellEffects.Branch b && (hasProtection(b.whenTrue(),entity)||hasProtection(b.whenFalse(),entity)))return true;
            if(e instanceof SpellEffects.Repeat r && hasProtection(r.effects(),entity))return true;
        }return false;
    }
    private static final Set<String> LIFETIME_KINDS=Set.of("status","area","barrier","zone","guard","wall","block_wall","sensor","construct","summon","decoy","tether");
    private static boolean matchesAfter(SpellEffects.Action a,Rule rule) {
        return rule.afterActions().contains(a.type().getPath()) || a.type().getPath().equals("status")
                && a.identifiers().containsKey("effect") && rule.afterStatuses().contains(a.identifiers().get("effect").toString());
    }
    private static boolean anchorable(SpellEffects.Manifestation m,SpellDefinition base) {
        if(!m.kind().getPath().equals("area") || m.durationTicks()<1 || !m.bindings().isEmpty())return false;
        boolean moving=false;
        for(String key:List.of("follow_target","motion")) {
            try { if(m.values().containsKey(key) && m.values().get(key).resolve(base.traits())>0)moving=true; }
            catch(IllegalStateException contextual){return false;}
        }
        if(!moving)return false;
        boolean[] spatial={false},locked={false};
        walk(m.onTick(),e->{
            if(e instanceof SpellEffects.ForEach f){
                if(f.target().selection()==TargetSpec.Selection.NEAR_TARGET)spatial[0]=true;
                else if(f.target().selection()!=TargetSpec.Selection.CURRENT)locked[0]=true;
            }
            // A nested delivery owns a different origin; anchoring its parent would be cosmetic only.
            if(e instanceof SpellEffects.CreateManifestation || e instanceof SpellEffects.InstallBinding)locked[0]=true;
            return e;
        });
        return spatial[0] && !locked[0];
    }
    private static List<SpellEffect> transform(List<SpellEffect> original,Rule rule,SpellDefinition base) {
        var rewritten=walk(original,e->{
            if(e instanceof SpellEffects.Action a && rule.attachment().equals("after") && matchesAfter(a,rule)) {
                var rider=new SpellEffects.Secondary(List.of(new SpellEffects.Limited(id("spellshaping/"+rule.id().getPath()),1,8,normalized(rule.effects(),base))));
                var primary=BuiltInCondition.Compare.to(id("event/secondary"),BuiltInCondition.Comparison.EQUAL,new ConditionValue.Flag(false));
                return new SpellEffects.Sequence(List.of(a,new SpellEffects.Branch(primary,List.of(rider),List.of())));
            }
            if (e instanceof SpellEffects.Action a && (rule.attachment().equals("damage") && Set.of("damage","weapon_damage").contains(a.type().getPath()) || rule.attachment().equals("heal") && a.type().getPath().equals("heal"))) {
                String fact=rule.attachment().equals("heal") ? "last_heal" : "last_damage";
                SpellEffect rider=new SpellEffects.Secondary(List.of(new SpellEffects.Limited(id("spellshaping/"+rule.id().getPath()),1,8,normalized(rule.effects(),base))));
                var guard=new BuiltInCondition.All(List.of(BuiltInCondition.Compare.to(id(fact),BuiltInCondition.Comparison.GREATER_THAN,new ConditionValue.Decimal(0)),
                        BuiltInCondition.Compare.to(id("event/secondary"),BuiltInCondition.Comparison.EQUAL,new ConditionValue.Flag(false))));
                return new SpellEffects.Sequence(List.of(a,new SpellEffects.Branch(guard,List.of(rider),List.of())));
            }
            if(e instanceof SpellEffects.CreateManifestation c && rule.attachment().equals("protection") && protective(c.manifestation())){
                var originalCreate=new SpellEffects.CreateManifestation(c.manifestation(),new TargetSpec(TargetSpec.Selection.CURRENT,new SpellValue.Constant(0)));
                var added=new SpellEffects.Secondary(List.of(new SpellEffects.Limited(id("spellshaping/"+rule.id().getPath()),1,8,normalized(rule.effects(),base))));
                return new SpellEffects.ForEach(c.target(),List.of(originalCreate,added));
            }
            if (e instanceof SpellEffects.CreateManifestation c) {
                var m=c.manifestation();var values=new HashMap<>(m.values());
                if(rule.kinds().contains(m.kind().getPath()))applyParameters(values,rule,base);
                if(rule.anchor() && anchorable(m,base)) {
                    values.put("follow_target",new SpellValue.Constant(0));values.put("motion",new SpellValue.Constant(0));
                }
                if(rule.lifetime() && m.durationTicks()>0 && m.bindings().isEmpty() && LIFETIME_KINDS.contains(m.kind().getPath()))values.put("lifetime",new SpellValue.Clamp(new SpellValue.Product(List.of(new SpellValue.Constant(m.durationTicks()),normalize(new SpellValue.Trait(id("time")),base))),1,240000));
                return new SpellEffects.CreateManifestation(new SpellEffects.Manifestation(m.kind(),m.durationTicks(),values,m.identifiers(),m.bindings(),m.onHit(),m.onTick(),m.onEnd(),m.interval(),quiet(m.visual(),rule.quiet())),c.target());
            }
            if(e instanceof SpellEffects.Action a && rule.kinds().contains(a.type().getPath())){var values=new HashMap<>(a.values());applyParameters(values,rule,base);return new SpellEffects.Action(a.type(),values,a.identifiers());}
            if(e instanceof SpellEffects.ForEach f){var t=f.target();var options=new HashMap<>(t.options());if(rule.kinds().contains(t.selection().name().toLowerCase(Locale.ROOT)))applyParameters(options,rule,base);return new SpellEffects.ForEach(new TargetSpec(t.selection(),t.distance(),t.required(),options,t.relationship()),f.effects(),quiet(f.visual(),rule.quiet()));}
            if(e instanceof SpellEffects.Visual v && rule.quiet())return new SpellEffects.Visual(quiet(Optional.of(v.visual()),true).orElseThrow());
            return e;
        });
        var result=new ArrayList<SpellEffect>();if(rule.attachment().equals("before"))result.addAll(normalized(rule.effects(),base));
        if(rule.delay()>0)result.add(new SpellEffects.Delay(rule.delay()));result.addAll(rewritten);return List.copyOf(result);
    }
    private static void applyParameters(Map<String,SpellValue> values,Rule rule,SpellDefinition base) {
        rule.parameters().forEach((key,expression)->{
            SpellValue previous=values.get(key);if(rule.defaults().containsKey(key) && (previous==null || previous instanceof SpellValue.Constant c && c.value()<=0))previous=new SpellValue.Constant(rule.defaults().get(key));
            if(previous!=null && canIncrease(previous,key,base)){SpellValue adjusted=new SpellValue.Product(List.of(previous,normalize(expression,base)));
                if(Set.of("count","pierce","homing","health").contains(key))adjusted=new SpellValue.Clamp(adjusted,0,key.equals("count") ? 8 : key.equals("pierce") ? 4 : key.equals("homing") ? .3 : 100);
                values.put(key,adjusted);
            }
        });
    }
    private static void collectParameters(Map<String,SpellValue> values,Rule rule,SpellDefinition base,Set<String> supported){
        for(String key:rule.parameters().keySet()){
            SpellValue previous=values.get(key);
            if(rule.defaults().containsKey(key) && (previous==null || previous instanceof SpellValue.Constant c && c.value()<=0))previous=new SpellValue.Constant(rule.defaults().get(key));
            if(previous!=null && canIncrease(previous,key,base))supported.add(key);
        }
    }
    private static boolean canIncrease(SpellValue previous,String key,SpellDefinition base){
        double maximum=switch(key){case "count"->8;case "pierce"->4;case "homing"->.3;case "health"->100;default->Double.POSITIVE_INFINITY;};
        try{double value=previous.resolve(base.traits());return value>0 && value<maximum;}
        catch(IllegalStateException contextual){return true;}
    }
    private static boolean hasSound(SpellEffect e){return e instanceof SpellEffects.Visual v && v.visual().sound().isPresent() || e instanceof SpellEffects.ForEach f && f.visual().flatMap(v->v.sound()).isPresent() || e instanceof SpellEffects.CreateManifestation c && c.manifestation().visual().flatMap(v->v.sound()).isPresent();}
    private static Optional<com.quzzar.vestige.magic.presentation.SpellVisual> quiet(Optional<com.quzzar.vestige.magic.presentation.SpellVisual> visual,boolean quiet){return !quiet ? visual : visual.map(v->new com.quzzar.vestige.magic.presentation.SpellVisual(v.duration(),v.radius(),v.height(),v.layers(),v.endsWithBindings(),Optional.empty()));}

    /** Traverses original executable plans, including bindings and delivery callbacks, leaving secondary plans terminal. */
    public static List<SpellEffect> walk(List<SpellEffect> effects,UnaryOperator<SpellEffect> visitor) {
        return walk(effects,visitor,false);
    }
    /** Trusted equipment policies may inspect riders too; ordinary shaping never reshapes secondary plans. */
    public static List<SpellEffect> walk(List<SpellEffect> effects,UnaryOperator<SpellEffect> visitor,boolean includeSecondary) {
        return effects.stream().map(effect->{
            if(effect instanceof SpellEffects.Secondary && !includeSecondary)return effect;
            SpellEffect nested=switch(effect){
                case SpellEffects.Sequence s->new SpellEffects.Sequence(walk(s.effects(),visitor,includeSecondary));
                case SpellEffects.Branch b->new SpellEffects.Branch(b.condition(),walk(b.whenTrue(),visitor,includeSecondary),walk(b.whenFalse(),visitor,includeSecondary));
                case SpellEffects.Repeat r->new SpellEffects.Repeat(r.count(),r.interval(),walk(r.effects(),visitor,includeSecondary));
                case SpellEffects.Limited l->new SpellEffects.Limited(l.group(),l.perTarget(),l.total(),walk(l.effects(),visitor,includeSecondary));
                case SpellEffects.ForEach f->new SpellEffects.ForEach(f.target(),walk(f.effects(),visitor,includeSecondary),f.visual());
                case SpellEffects.InstallBinding b->new SpellEffects.InstallBinding(binding(b.binding(),visitor,includeSecondary),b.target());
                case SpellEffects.CreateManifestation c->{
                    var m=c.manifestation();var onHit=m.onHit();
                    if(m.kind().getPath().equals("projectile") && onHit.isEmpty() && m.values().containsKey("damage")){
                        var explicit=new ArrayList<SpellEffect>();explicit.add(new SpellEffects.Branch(new BuiltInCondition.Exists(ConditionPaths.TARGET_HEALTH),List.of(new SpellEffects.Action(id("damage"),Map.of("amount",m.values().get("damage")),Map.of())),List.of()));
                        m.visual().ifPresent(v->explicit.add(new SpellEffects.Visual(new com.quzzar.vestige.magic.presentation.SpellVisual(24,new SpellValue.Constant(1),0,v.layers(),false,Optional.empty()))));onHit=List.copyOf(explicit);
                    }
                    yield new SpellEffects.CreateManifestation(new SpellEffects.Manifestation(m.kind(),m.durationTicks(),m.values(),m.identifiers(),m.bindings().stream().map(b->binding(b,visitor,includeSecondary)).toList(),walk(onHit,visitor,includeSecondary),walk(m.onTick(),visitor,includeSecondary),walk(m.onEnd(),visitor,includeSecondary),m.interval(),m.visual()),c.target());
                }
                case SpellEffects.Secondary secondary->new SpellEffects.Secondary(walk(secondary.effects(),visitor,includeSecondary));
                default->effect;
            };return visitor.apply(nested);
        }).toList();
    }
    private static SpellEffects.Binding binding(SpellEffects.Binding b,UnaryOperator<SpellEffect> visitor,boolean includeSecondary){return new SpellEffects.Binding(b.id(),b.triggers(),walk(b.effects(),visitor,includeSecondary),b.durationTicks(),b.charges());}
    private static void collectReads(SpellEffect e,Set<String> reads){
        if(e instanceof SpellEffects.Action a)a.values().values().forEach(v->read(v,reads));
        if(e instanceof SpellEffects.ForEach f)targetReads(f.target(),reads);
        if(e instanceof SpellEffects.CreateManifestation c){c.manifestation().values().values().forEach(v->read(v,reads));targetReads(c.target(),reads);}
        if(e instanceof SpellEffects.InstallBinding b)targetReads(b.target(),reads);
    }
    private static void targetReads(TargetSpec t,Set<String> reads){read(t.distance(),reads);t.options().values().forEach(v->read(v,reads));}
    private static void read(SpellValue value,Set<String> reads){switch(value){case SpellValue.Trait t->reads.add(t.trait().getPath());case SpellValue.Product p->p.factors().forEach(v->read(v,reads));case SpellValue.Sum s->s.terms().forEach(v->read(v,reads));case SpellValue.Clamp c->read(c.value(),reads);default->{}}}
    private static List<SpellEffect> normalized(List<SpellEffect> effects,SpellDefinition base){return walk(effects,e->{
        if(e instanceof SpellEffects.Action a)return new SpellEffects.Action(a.type(),expressions(a.values(),base),a.identifiers());
        if(e instanceof SpellEffects.CaptureValue c)return new SpellEffects.CaptureValue(c.key(),normalize(c.value(),base));
        if(e instanceof SpellEffects.ForEach f)return new SpellEffects.ForEach(normalize(f.target(),base),f.effects(),f.visual());
        if(e instanceof SpellEffects.CreateManifestation c){var m=c.manifestation();return new SpellEffects.CreateManifestation(new SpellEffects.Manifestation(m.kind(),m.durationTicks(),expressions(m.values(),base),m.identifiers(),m.bindings(),m.onHit(),m.onTick(),m.onEnd(),m.interval(),m.visual()),normalize(c.target(),base));}
        return e;
    });}
    private static TargetSpec normalize(TargetSpec t,SpellDefinition base){return new TargetSpec(t.selection(),normalize(t.distance(),base),t.required(),expressions(t.options(),base),t.relationship());}
    private static Map<String,SpellValue> expressions(Map<String,SpellValue> values,SpellDefinition base){var result=new HashMap<String,SpellValue>();values.forEach((k,v)->result.put(k,normalize(v,base)));return result;}
    private static SpellValue normalize(SpellValue value,SpellDefinition base){return switch(value){
        case SpellValue.Trait t->new SpellValue.Product(List.of(t,new SpellValue.Constant(1/Math.max(1,base.traits().rating(t.trait())))));
        case SpellValue.Product p->new SpellValue.Product(p.factors().stream().map(v->normalize(v,base)).toList());
        case SpellValue.Sum s->new SpellValue.Sum(s.terms().stream().map(v->normalize(v,base)).toList());
        case SpellValue.Clamp c->new SpellValue.Clamp(normalize(c.value(),base),c.minimum(),c.maximum());default->value;
    };}
    private static Map<ResourceLocation,Rule> load(){
        try(var stream=Spellshaping.class.getResourceAsStream("/data/vestige/spellshaping_rules.json")){
            if(stream==null)throw new IllegalStateException("Missing Spellshaping rules");
            var root=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();if(root.get("version").getAsInt()!=1)throw new IllegalArgumentException("Unknown Spellshaping rules version");
            var result=new LinkedHashMap<ResourceLocation,Rule>();var singlePairs=new HashSet<Pair>();var compounds=new HashSet<Set<Pair>>();
            for(var value:root.getAsJsonArray("rules")){
                var r=value.getAsJsonObject();var pairs=new ArrayList<Pair>();for(var p:r.getAsJsonArray("pairs")){var pair=p.getAsJsonObject();pairs.add(new Pair(ResourceLocation.parse(pair.get("offering").getAsString()),ResourceLocation.parse(pair.get("material").getAsString())));}
                boolean compound=r.has("compound") && r.get("compound").getAsBoolean();if(!compound){for(var p:pairs)if(!singlePairs.add(p))throw new IllegalArgumentException("Ambiguous material pairing "+p);}else if(!compounds.add(Set.copyOf(pairs)))throw new IllegalArgumentException("Duplicate compound pattern");
                var traits=new ArrayList<TraitModifier>();if(r.has("traits"))for(var t:r.getAsJsonArray("traits")){var m=t.getAsJsonObject();traits.add(new TraitModifier(id(m.get("trait").getAsString()),TraitModifier.Operation.valueOf(m.get("operation").getAsString()),m.get("amount").getAsDouble()));}
                var parameters=new HashMap<String,SpellValue>();if(r.has("parameters"))r.getAsJsonObject("parameters").entrySet().forEach(e->parameters.put(e.getKey(),SpellJson.readValue(e.getValue())));
                var rule=new Rule(id(r.get("id").getAsString()),r.get("name").getAsString(),r.get("description").getAsString(),pairs,compound,strings(r,"capabilities"),strings(r,"reads"),traits,numbers(r,"cost_factors"),r.has("costs") ? SpellJson.readCosts(r.getAsJsonArray("costs")) : List.of(),number(r,"health_exchange",0),number(r,"hunger_exchange",0),r.has("attachment") ? r.get("attachment").getAsString() : "none",r.has("effects") ? SpellJson.readPlan(r.getAsJsonArray("effects")) : List.of(),strings(r,"kinds"),parameters,numbers(r,"defaults"),flag(r,"lifetime"),(int)number(r,"delay",0),flag(r,"quiet"),strings(r,"seeds"),(int)number(r,"max_degree",8),strings(r,"after_actions"),strings(r,"after_statuses"),flag(r,"anchor"));
                if(result.put(rule.id(),rule)!=null)throw new IllegalArgumentException("Duplicate augment identity");
            }return Collections.unmodifiableMap(result);
        }catch(IOException invalid){throw new IllegalStateException(invalid);}
    }
    private static boolean flag(JsonObject r,String key){return r.has(key) && r.get(key).getAsBoolean();}
    private static Set<String> strings(JsonObject r,String key){return !r.has(key) ? Set.of() : r.getAsJsonArray(key).asList().stream().map(JsonElement::getAsString).collect(java.util.stream.Collectors.toSet());}
    private static Map<String,Double> numbers(JsonObject r,String key){var result=new HashMap<String,Double>();if(r.has(key))r.getAsJsonObject(key).entrySet().forEach(e->result.put(e.getKey(),e.getValue().getAsDouble()));return result;}
    private static double number(JsonObject r,String key,double fallback){return r.has(key) ? r.get(key).getAsDouble() : fallback;}
    private static ResourceLocation id(String value){return value.contains(":") ? ResourceLocation.parse(value) : ResourceLocation.fromNamespaceAndPath("vestige",value);}
}
