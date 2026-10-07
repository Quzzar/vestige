package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.runtime.CastShaping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import java.util.*;

/** Trusted equipment profiles. The scroll is compiled once, then equipment contributes separately. */
public final class WandComponents {
    public static final int COOLDOWN_TICKS = 1200;
    public enum Base {
        STICK(Items.STICK,24), BAMBOO(Items.BAMBOO,18,"plant","wood"),
        BONE(Items.BONE,20,"death","necromancy"), BLAZE_ROD(Items.BLAZE_ROD,20,"fire"),
        BREEZE_ROD(Items.BREEZE_ROD,18,"air","motion"), END_ROD(Items.END_ROD,20,"ender","space"),
        LIGHTNING_ROD(Items.LIGHTNING_ROD,22,"lightning");
        private final Item ingredient;
        private final int durability;
        private final Set<ResourceLocation> affinities;
        Base(Item ingredient,int durability,String... traits) {
            this.ingredient=ingredient;this.durability=durability;
            this.affinities=Arrays.stream(traits).map(VestigeMainMod::location).collect(java.util.stream.Collectors.toUnmodifiableSet());
        }
        public Item ingredient() { return ingredient; }
        public int durability() { return durability; }
        public String id() { return name().toLowerCase(Locale.ROOT); }
        public boolean matches(TraitProfile source) { return affinities.stream().anyMatch(t -> source.rating(t)>0); }
    }
    public record Compiled(Spellshaping.Compiled cast,int durability,int wear) { }
    private WandComponents() { }
    public static int durability(Base base,MagicalThreadRecipe.Type thread) {
        return base.durability()+(thread==MagicalThreadRecipe.Type.CALLOUS ? 8 : 0);
    }
    public static Compiled compile(SpellDefinition definition,ScrollItems.Scroll source,Base base,MagicalThreadRecipe.Type thread) {
        if (!definition.id().equals(source.spell())) throw new IllegalArgumentException("Wrong bound spell");
        var compiled=Spellshaping.compile(definition,source.augments(),source.modifiers(),source.shaping());
        // Resolve before any equipment seed: a new elemental rider cannot qualify its own base affinity.
        boolean affinity=base.matches(definition.traits().resolve(compiled.modifiers()));
        if (affinity) {
            var modifiers=new ArrayList<>(compiled.modifiers());
            modifiers.add(new TraitModifier(VestigeMainMod.location("amplify"),TraitModifier.Operation.MULTIPLY,1.2));
            compiled=new Spellshaping.Compiled(compiled.spell(),List.copyOf(modifiers),compiled.shaping());
        }
        double manaFactor=1,timeFactor=1;int extraTime=0,wear=1;
        switch (thread) {
            case ENSORCELLED -> {
                if (mana(compiled)==0) throw new IllegalArgumentException("Economy core needs a mana payment");
                manaFactor=.85;extraTime=10;
            }
            case CALLOUS -> extraTime=20;
            case LACED -> {
                if (preparation(compiled)<2) throw new IllegalArgumentException("No reducible preparation");
                manaFactor=1.12;timeFactor=.85;
            }
            case SMOLDERING -> {
                rejectOverlap(source,"ignite");
                var ignites=new boolean[1];
                Spellshaping.walk(compiled.spell().effects(),effect -> {
                    if (effect instanceof com.quzzar.vestige.magic.effect.SpellEffects.Action a && a.type().getPath().equals("ignite")) ignites[0]=true;
                    return effect;
                });
                if (ignites[0]) throw new IllegalArgumentException("Source already ignites");
                var rule=equipmentRule("kindled","damage",true);
                compiled=Spellshaping.contribute(compiled,definition,rule);manaFactor=1.12;wear=2;
            }
            case CONSECRATED -> {
                rejectOverlap(source,"remove_status");
                var healing=equipmentRule("purifying","heal",true);
                var protection=equipmentRule("purifying","protection",false);
                boolean heals=Spellshaping.compatible(healing,compiled.spell());
                boolean protects=Spellshaping.compatible(protection,compiled.spell());
                if (!heals && !protects) throw new IllegalArgumentException("No supported healing/protection");
                if (heals) compiled=Spellshaping.contribute(compiled,definition,healing);
                if (protects) compiled=Spellshaping.contribute(compiled,definition,
                        equipmentRule("purifying","protection",!heals));
                manaFactor=1.14;
            }
        }
        var before=compiled;
        var adjustment=compiled.shaping().adjustment();
        var factors=new HashMap<>(adjustment.factors());
        factors.merge("mana",manaFactor,(a,b)->a*b);factors.merge("time",timeFactor,(a,b)->a*b);
        var shaping=new CastShaping(compiled.shaping().castingCost(),compiled.shaping().roundAmounts(),
                new CastShaping.CostAdjustment(factors,adjustment.additional(),adjustment.healthFraction(),adjustment.hungerFraction(),
                        extraTime,manaFactor>1 ? 1 : 0));
        compiled=new Spellshaping.Compiled(compiled.spell(),compiled.modifiers(),shaping);
        if (thread==MagicalThreadRecipe.Type.LACED && preparation(compiled)>=preparation(before))
            throw new IllegalArgumentException("Preparation reduction rounded away");
        if (shaping.costs(definition.costs()).stream().anyMatch(c -> c instanceof SpellCost.Material m && m.item().equals(VestigeMainMod.location("wand"))))
            throw new IllegalArgumentException("The reserved source cannot also be a material payment");
        return new Compiled(compiled,durability(base,thread),wear);
    }
    private static double mana(Spellshaping.Compiled source) {
        return source.shaping().costs(source.spell().costs()).stream().filter(SpellCost.Mana.class::isInstance)
                .map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum();
    }
    private static int preparation(Spellshaping.Compiled source) {
        return source.shaping().costs(source.spell().costs()).stream().filter(SpellCost.Time.class::isInstance)
                .map(SpellCost.Time.class::cast).mapToInt(SpellCost.Time::ticks).sum();
    }
    private static void rejectOverlap(ScrollItems.Scroll source,String action) {
        for (var selection:source.augments()) {
            var found=new boolean[1];
            Spellshaping.walk(Spellshaping.rules().get(selection.id()).effects(),effect -> {
                if (effect instanceof com.quzzar.vestige.magic.effect.SpellEffects.Action a && a.type().getPath().equals(action)) found[0]=true;
                return effect;
            });
            if (found[0]) throw new IllegalArgumentException("Core overlaps stored Spellshaping");
        }
    }
    private static Spellshaping.Rule equipmentRule(String id,String attachment,boolean scaling) {
        var rule=Spellshaping.rules().get(VestigeMainMod.location(id));
        return new Spellshaping.Rule(VestigeMainMod.location("wand_core/"+id),rule.name(),rule.description(),rule.pairs(),false,
                rule.capabilities(),rule.reads(),scaling ? rule.traits() : List.of(),Map.of(),List.of(),0,0,
                attachment,rule.effects(),rule.kinds(),rule.parameters(),rule.defaults(),rule.lifetime(),rule.delay(),rule.quiet(),
                scaling ? rule.seeds() : Set.of(),1,rule.afterActions(),rule.afterStatuses(),rule.anchor());
    }
}
