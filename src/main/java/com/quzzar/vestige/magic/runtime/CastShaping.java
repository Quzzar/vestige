package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.definition.SpellCost;
import java.util.*;

/** Item-owned adjustments, retained by a cast and all its continuations. Base definitions stay immutable. */
public record CastShaping(double castingCost, boolean roundAmounts, CostAdjustment adjustment) {
    public CastShaping(double castingCost,boolean roundAmounts) { this(castingCost,roundAmounts,CostAdjustment.NONE); }
    public static final CastShaping NONE = new CastShaping(1,false);
    private static final Set<String> QUANTITIES = Set.of("amount","duration","amplifier","radius","distance","health","count",
            "seconds","depth","width","height","ticks","attack_damage","max_hits_per_target","budget","max_targets",
            "damage","impact_damage","deferred_damage","per_hit","required_ticks","interval","range","max_length","rise_ticks","collapse_ticks","lifetime","pierce");
    public CastShaping {
        Objects.requireNonNull(adjustment);
        if (!Double.isFinite(castingCost) || castingCost<.65 || castingCost>2) throw new IllegalArgumentException("Invalid casting cost multiplier");
    }
    public double amount(double resolved) {
        if (!Double.isFinite(resolved)) throw new IllegalArgumentException("Nonfinite final amount");
        return roundAmounts ? Math.floor(resolved+.5) : resolved;
    }
    public double value(String key,double resolved) { return QUANTITIES.contains(key) ? amount(resolved) : resolved; }
    public List<SpellCost> costs(List<SpellCost> base) {
        if (!roundAmounts && castingCost==1 && adjustment.equals(CostAdjustment.NONE)) return base;
        // Compose duplicates before rounding once. Health costs cross the HP/heart boundary here.
        record Key(Class<?> type,Object item,Object operation) { }
        var totals=new LinkedHashMap<Key,Double>(); var examples=new HashMap<Key,SpellCost>();
        var composed=new ArrayList<>(base); composed.addAll(adjustment.additional());
        for (var cost:composed) {
            Key key=cost instanceof SpellCost.Material m ? new Key(cost.getClass(),m.item(),m.operation()) : new Key(cost.getClass(),null,null);
            double value=switch(cost) {
                case SpellCost.Mana m -> m.amount(); case SpellCost.Health h -> h.amount(); case SpellCost.Hunger h -> h.amount(); case SpellCost.Experience e -> e.amount();
                case SpellCost.Time t -> t.ticks(); case SpellCost.Cooldown c -> c.ticks(); case SpellCost.Material m -> m.amount();
            };
            totals.merge(key,value,Double::sum); examples.put(key,cost);
        }
        double mana=totals.entrySet().stream().filter(e->e.getKey().type()==SpellCost.Mana.class).mapToDouble(Map.Entry::getValue).sum()*adjustment.factors().getOrDefault("mana",1d);
        if (mana>0 && adjustment.healthFraction()>0) {
            Key key=new Key(SpellCost.Health.class,null,null);
            totals.merge(key,(double)ResourceValuation.healthForMana(mana*adjustment.healthFraction()),Double::sum); examples.putIfAbsent(key,new SpellCost.Health(2));
        }
        if (mana>0 && adjustment.hungerFraction()>0) {
            Key key=new Key(SpellCost.Hunger.class,null,null);
            totals.merge(key,(double)ResourceValuation.foodForMana(mana*adjustment.hungerFraction()),Double::sum); examples.putIfAbsent(key,new SpellCost.Hunger(1));
        }
        var result=new ArrayList<SpellCost>();
        totals.forEach((key,total)->{
            String kind=switch(examples.get(key)) { case SpellCost.Mana m->"mana";case SpellCost.Health h->"health";case SpellCost.Hunger h->"hunger";case SpellCost.Experience e->"experience";case SpellCost.Time t->"time";case SpellCost.Cooldown c->"cooldown";case SpellCost.Material m->"material"; };
            double adjusted=total*adjustment.factors().getOrDefault(kind,1d);
            if (kind.equals("mana")) adjusted*=1-adjustment.healthFraction()-adjustment.hungerFraction();
            double value=examples.get(key) instanceof SpellCost.Health ? Math.floor(adjusted/2*castingCost+.5)*2 : Math.floor(adjusted*castingCost+.5);
            if (value<=0) return;
            if (value>Integer.MAX_VALUE) throw new IllegalArgumentException("Shaped cost exceeds supported amount");
            result.add(switch(examples.get(key)) {
                case SpellCost.Mana m -> new SpellCost.Mana(value); case SpellCost.Health h -> new SpellCost.Health(value);
                case SpellCost.Experience e -> new SpellCost.Experience((int)value);
                case SpellCost.Hunger h -> new SpellCost.Hunger((int)value); case SpellCost.Time t -> new SpellCost.Time((int)value);
                case SpellCost.Cooldown c -> new SpellCost.Cooldown((int)value);
                case SpellCost.Material m -> new SpellCost.Material(m.item(),m.operation(),(int)value);
            });
        });
        if (adjustment.additionalPreparationTicks()>0) {
            int time=result.stream().filter(SpellCost.Time.class::isInstance).map(SpellCost.Time.class::cast).mapToInt(SpellCost.Time::ticks).sum();
            result.removeIf(SpellCost.Time.class::isInstance);
            result.add(new SpellCost.Time(Math.addExact(time,adjustment.additionalPreparationTicks())));
        }
        if (adjustment.minimumMana()>0 && result.stream().filter(SpellCost.Mana.class::isInstance).map(SpellCost.Mana.class::cast)
                .mapToDouble(SpellCost.Mana::amount).sum()<adjustment.minimumMana()) {
            result.removeIf(SpellCost.Mana.class::isInstance);
            result.add(new SpellCost.Mana(adjustment.minimumMana()));
        }
        return List.copyOf(result);
    }
    public record CostAdjustment(Map<String,Double> factors,List<SpellCost> additional,double healthFraction,double hungerFraction,
                                 int additionalPreparationTicks,int minimumMana) {
        public CostAdjustment(Map<String,Double> factors,List<SpellCost> additional,double healthFraction,double hungerFraction) {
            this(factors,additional,healthFraction,hungerFraction,0,0);
        }
        public static final CostAdjustment NONE=new CostAdjustment(Map.of(),List.of(),0,0);
        public CostAdjustment {
            factors=Map.copyOf(factors); additional=List.copyOf(additional);
            if (additional.size()>32 || !Double.isFinite(healthFraction) || !Double.isFinite(hungerFraction) || healthFraction<0 || hungerFraction<0 || healthFraction+hungerFraction>1) throw new IllegalArgumentException("Invalid cost exchange");
            if (additionalPreparationTicks<0 || additionalPreparationTicks>240000 || minimumMana<0 || minimumMana>100)
                throw new IllegalArgumentException("Invalid equipment payment");
            factors.forEach((kind,factor)->{if (!Set.of("mana","health","hunger","experience","time","cooldown","material").contains(kind) || !Double.isFinite(factor) || factor<.1 || factor>8)throw new IllegalArgumentException("Invalid typed cost factor");});
        }
    }
}
