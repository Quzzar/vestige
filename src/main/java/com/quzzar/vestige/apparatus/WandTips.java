package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.CastShaping;
import net.minecraft.world.item.*;
import java.util.*;

/** One trusted optional tip. Eligibility follows primary executable capabilities, including callbacks. */
public final class WandTips {
    public enum Tip {
        AMETHYST(Items.AMETHYST_SHARD,"Resonating",1.15,"sonic","time"),
        DIAMOND(Items.DIAMOND,"Refracting",1.20,"force","life","area"),
        EMERALD(Items.EMERALD,"Reclaiming",1),
        ENDER_PEARL(Items.ENDER_PEARL,"Elusive",1.20,"space","motion"),
        COPPER(Items.COPPER_INGOT,"Conductive",1.15,"lightning"),
        IRON(Items.IRON_INGOT,"Repelling",1.08,"motion"),
        GHAST_TEAR(Items.GHAST_TEAR,"Renewing",1.15,"life","time"),
        NETHERITE(Items.NETHERITE_INGOT,"Steadfast",1.10,"metal","motion");
        private final Item item;private final String adjective;private final double mana;
        private final List<String> seeds;
        Tip(Item item,String adjective,double mana,String... seeds){this.item=item;this.adjective=adjective;this.mana=mana;this.seeds=List.of(seeds);}
        public Item item(){return item;}public String adjective(){return adjective;}
        public String id(){return name().toLowerCase(Locale.ROOT);}public double manaFactor(){return mana;}
    }
    public record Capabilities(boolean damage,boolean heal,boolean protection) { }
    private WandTips(){ }
    public static Optional<Tip> of(ItemStack stack){return Arrays.stream(Tip.values()).filter(t -> stack.is(t.item())).findFirst();}
    public static Capabilities capabilities(SpellDefinition source){
        boolean[] result=new boolean[3];
        java.util.function.UnaryOperator<SpellEffect> visitor=effect -> {
            if(effect instanceof SpellEffects.Action a){
                if(Set.of("damage","weapon_damage","explode","grip","fangs").contains(a.type().getPath()))result[0]=true;
                if(Set.of("heal","dwell_heal","leech").contains(a.type().getPath()))result[1]=true;
            }
            if(effect instanceof SpellEffects.InstallBinding b && protects(b.binding()))result[2]=true;
            if(effect instanceof SpellEffects.CreateManifestation c){var m=c.manifestation();String kind=m.kind().getPath();
                String behavior=m.identifiers().getOrDefault("behavior",VestigeMainMod.location("none")).getPath();
                if(kind.equals("projectile") && m.values().containsKey("damage") || behavior.equals("pressure"))result[0]=true;
                if(behavior.equals("heal"))result[1]=true;
                if(Set.of("guard","barrier").contains(kind) || behavior.equals("protect") || m.bindings().stream().anyMatch(WandTips::protects))result[2]=true;
            }return effect;
        };
        Spellshaping.walk(source.effects(),visitor);
        source.modes().values().forEach(mode -> Spellshaping.walk(mode.effects(),visitor));
        return new Capabilities(result[0],result[1],result[2]);
    }
    private static boolean protects(SpellEffects.Binding b){return com.quzzar.vestige.magic.effect.SpellCapabilities.ofPlan(b.effects()).stream()
            .anyMatch(c -> Set.of("reduce_pending_damage","defer_pending_damage").contains(c.getPath()));}
    public static WandComponents.Compiled contribute(WandComponents.Compiled source,Tip tip){
        var cast=source.cast();var capabilities=capabilities(cast.spell());
        if ((tip==Tip.AMETHYST || tip==Tip.DIAMOND) && !capabilities.damage() && !capabilities.heal()
                || tip==Tip.COPPER && !capabilities.damage()
                || tip==Tip.IRON && !capabilities.damage() && !capabilities.heal() && !capabilities.protection())
            throw new IllegalArgumentException("Tip has no supported primary outcome");
        double mana=cast.shaping().costs(cast.spell().costs()).stream().filter(SpellCost.Mana.class::isInstance).map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum();
        if(tip==Tip.EMERALD && mana<=0)throw new IllegalArgumentException("Reclaiming requires actual mana payment");
        var modifiers=new ArrayList<>(cast.modifiers());var traits=cast.spell().traits().resolve(modifiers);
        for(String seed:tip.seeds)if(traits.rating(VestigeMainMod.location(seed))==0)modifiers.add(new TraitModifier(VestigeMainMod.location(seed),TraitModifier.Operation.ADD,1));
        var old=cast.shaping().adjustment();var factors=new HashMap<>(old.factors());factors.merge("mana",tip.mana,(a,b)->a*b);
        var shaping=new CastShaping(cast.shaping().castingCost(),cast.shaping().roundAmounts(),new CastShaping.CostAdjustment(
                factors,old.additional(),old.healthFraction(),old.hungerFraction(),old.additionalPreparationTicks()+(tip==Tip.NETHERITE ? 10 : 0),
                Math.max(old.minimumMana(),tip.mana>1 ? 1 : 0)));
        return new WandComponents.Compiled(new Spellshaping.Compiled(safety(cast.spell(),tip),List.copyOf(modifiers),shaping),source.durability(),source.wear()+(tip==Tip.EMERALD ? 1 : 0));
    }

    /** Components share caps with existing shaping without changing the stored source scroll. */
    private static SpellDefinition safety(SpellDefinition spell,Tip tip) {
        java.util.function.UnaryOperator<SpellEffect> visitor=effect -> {
            if (tip==Tip.COPPER && effect instanceof SpellEffects.Limited l && l.group().equals(VestigeMainMod.location("spellshaping/shocking"))) {
                var plan=Spellshaping.walk(l.effects(),e -> {
                    if (e instanceof SpellEffects.Action a && a.type().getPath().equals("damage")) {
                        var values=new HashMap<>(a.values());var identifiers=new HashMap<>(a.identifiers());
                        values.put("budget_maximum",new com.quzzar.vestige.magic.expression.SpellValue.Constant(35));
                        identifiers.put("amount_budget",VestigeMainMod.location("equipment/electrical"));
                        return new SpellEffects.Action(a.type(),values,identifiers);
                    }return e;
                },true);
                return new SpellEffects.Limited(l.group(),l.perTarget(),l.total(),plan);
            }
            if (tip==Tip.IRON && effect instanceof SpellEffects.Action a && Set.of("knockback","pull","launch").contains(a.type().getPath())) {
                var values=new HashMap<>(a.values());values.put("maximum_speed",new com.quzzar.vestige.magic.expression.SpellValue.Constant(1.5));
                return new SpellEffects.Action(a.type(),values,a.identifiers());
            }return effect;
        };
        var modes=new HashMap<net.minecraft.resources.ResourceLocation,SpellMode>();
        spell.modes().forEach((key,mode) -> modes.put(key,new SpellMode(key,mode.costs(),Spellshaping.walk(mode.effects(),visitor,true))));
        return new SpellDefinition(spell.id(),spell.rarity(),spell.traditions(),spell.traits(),spell.costs(),spell.triggers(),
                Spellshaping.walk(spell.effects(),visitor,true),modes,spell.source());
    }
}
