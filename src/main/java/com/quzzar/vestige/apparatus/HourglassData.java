package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.ItemImbuements;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.magic.runtime.ResourceValuation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.*;

/** Trusted choices and saved physical geometry; item data cannot supply executable effects or coefficients. */
public final class HourglassData {
    public static final ResourceLocation FAMILY = VestigeMainMod.location("kairotic_hourglass");
    public static final LeylineShaping.Geometry NEUTRAL = new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.CROSS,0,0);
    private static final String GEOMETRY = "vestige_hourglass_geometry";
    public enum Choice {
        FLEETING(0,"minecraft:copper_block"), ENDURING(0,"minecraft:amethyst_block"),
        REINFORCED(1,"minecraft:iron_block"), FRUGAL(1,"minecraft:quartz_block"),
        BLOODBOUND(2,"minecraft:soul_sand"), FASTING(2,"minecraft:moss_block"), ERUDITE(2,"minecraft:lapis_block");
        public final int group;
        public final ResourceLocation material;
        Choice(int group,String material) { this.group=group;this.material=ResourceLocation.parse(material); }
        public ResourceLocation id() { return VestigeMainMod.location("hourglass/"+name().toLowerCase(Locale.ROOT)); }
        public int seat() { return group==0 ? 0 : group==1 ? 2 : 4; }
    }
    private static final Set<ResourceLocation> IDS = Arrays.stream(Choice.values()).map(Choice::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
    public record Variant(Set<Choice> choices,LeylineShaping.Geometry geometry) {
        public Variant {
            choices=Set.copyOf(choices);Objects.requireNonNull(geometry);
            if (geometry.slots()!=4 || choices.stream().map(c -> c.group).distinct().count()!=choices.size())
                throw new IllegalArgumentException("Incompatible hourglass selections");
        }
        public List<MagicAdjectives.Adjustment> selections() { return choices.stream().sorted().map(c -> new MagicAdjectives.Adjustment(c.id(),1)).toList(); }
        public LeylineShaping.Modifiers layout(MagicDefinition ability) { return LeylineShaping.resolve(geometry,ability.traits().ratings().keySet()); }
        public List<TraitModifier> modifiers(MagicDefinition ability) {
            var result=new ArrayList<>(layout(ability).traits());
            double time=choices.contains(Choice.FLEETING) ? 2d/3 : choices.contains(Choice.ENDURING) ? 1.2 : 1;
            if(time!=1)result.add(new TraitModifier(VestigeMainMod.location("time"),TraitModifier.Operation.MULTIPLY,time));
            return List.copyOf(result);
        }
        public int durability() { return choices.contains(Choice.REINFORCED) ? 15 : choices.contains(Choice.FRUGAL) ? 6 : 10; }
        public List<SpellCost> costs(MagicDefinition ability) {
            double mana=ability.costs().stream().filter(SpellCost.Mana.class::isInstance).map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum();
            mana*=layout(ability).cost()*(choices.contains(Choice.FLEETING) ? .75 : choices.contains(Choice.ENDURING) ? 1.12 : 1)
                    *(choices.contains(Choice.REINFORCED) ? 1.25 : choices.contains(Choice.FRUGAL) ? .75 : 1);
            var result=new ArrayList<SpellCost>();
            double exchanged=choices.stream().anyMatch(c -> c.group==2) ? Math.min(30,.75*mana) : 0;
            int remaining=(int)Math.floor(mana-exchanged+.5);
            if(remaining>0)result.add(new SpellCost.Mana(remaining));
            if(choices.contains(Choice.BLOODBOUND))result.add(new SpellCost.Health(ResourceValuation.healthForMana(exchanged)));
            if(choices.contains(Choice.FASTING))result.add(new SpellCost.Hunger(ResourceValuation.foodForMana(exchanged)));
            if(choices.contains(Choice.ERUDITE))result.add(new SpellCost.Experience(ResourceValuation.experienceForMana(exchanged)));
            return List.copyOf(result);
        }
    }
    private HourglassData() { }
    public static List<Variant> variants() {
        var result=new ArrayList<Variant>();
        for(var temporal:List.of(Set.<Choice>of(),Set.of(Choice.FLEETING),Set.of(Choice.ENDURING)))
            for(var vessel:List.of(Set.<Choice>of(),Set.of(Choice.REINFORCED),Set.of(Choice.FRUGAL)))
                for(var payment:List.of(Set.<Choice>of(),Set.of(Choice.BLOODBOUND),Set.of(Choice.FASTING),Set.of(Choice.ERUDITE))) {
                    var choices=EnumSet.noneOf(Choice.class);choices.addAll(temporal);choices.addAll(vessel);choices.addAll(payment);
                    result.add(new Variant(choices,NEUTRAL));
                }
        return List.copyOf(result);
    }
    public static ItemStack create(Variant variant) {
        var stack=new ItemStack(ScrollItems.KAIROTIC_HOURGLASS.get());
        stack.set(DataComponents.MAX_DAMAGE,variant.durability());
        ItemImbuements.write(stack,FAMILY,variant.selections());
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag -> {
            var geometry=new CompoundTag();geometry.putInt("version",1);
            geometry.putString("shape",variant.geometry().innerShape().name());
            geometry.putInt("distance",variant.geometry().inner());geometry.putInt("height",variant.geometry().innerHeight());
            tag.put(GEOMETRY,geometry);
        });
        return stack;
    }
    public static Optional<Variant> read(ItemStack stack) {
        if(!stack.is(ScrollItems.KAIROTIC_HOURGLASS.get()) || stack.getCount()!=1)return Optional.empty();
        try {
            var selections=ItemImbuements.read(stack,FAMILY,IDS);if(selections.isEmpty())return Optional.empty();
            var choices=EnumSet.noneOf(Choice.class);
            for(var selection:selections.get())for(var choice:Choice.values())if(choice.id().equals(selection.id()))choices.add(choice);
            var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var geometry=NEUTRAL;
            if(tag.contains(GEOMETRY)) {
                if(!tag.contains(GEOMETRY,Tag.TAG_COMPOUND))return Optional.empty();
                var saved=tag.getCompound(GEOMETRY);
                if(!saved.getAllKeys().equals(Set.of("version","shape","distance","height")) || !saved.contains("version",Tag.TAG_INT) || saved.getInt("version")!=1
                        || !saved.contains("shape",Tag.TAG_STRING) || !saved.contains("distance",Tag.TAG_INT) || !saved.contains("height",Tag.TAG_INT))return Optional.empty();
                geometry=new LeylineShaping.Geometry(4,LeylineShaping.Shape.valueOf(saved.getString("shape")),saved.getInt("distance"),saved.getInt("height"),LeylineShaping.Shape.CROSS,0,0);
            }
            var variant=new Variant(choices,geometry);
            return stack.getMaxDamage()==variant.durability() && stack.getDamageValue()>=0 && stack.getDamageValue()<variant.durability() ? Optional.of(variant) : Optional.empty();
        } catch(IllegalArgumentException invalid) { return Optional.empty(); }
    }
}
