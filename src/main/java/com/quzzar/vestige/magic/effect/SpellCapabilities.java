package com.quzzar.vestige.magic.effect;

import com.quzzar.vestige.magic.definition.SpellDefinition;
import com.quzzar.vestige.magic.definition.SpellEffect;
import net.minecraft.resources.ResourceLocation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Derives capabilities by walking effects, including temporary behavior and persistent outcomes. */
public final class SpellCapabilities {
    private SpellCapabilities() { }
    public static Set<ResourceLocation> of(SpellDefinition spell) {
        Set<ResourceLocation> result = new HashSet<>();
        collect(spell.effects(), result);
        spell.modes().values().forEach(mode -> collect(mode.effects(), result));
        return Set.copyOf(result);
    }
    public static Set<ResourceLocation> ofPlan(List<SpellEffect> effects) {
        Set<ResourceLocation> result=new HashSet<>();collect(effects,result);return Set.copyOf(result);
    }
    private static void collect(List<SpellEffect> effects, Set<ResourceLocation> result) {
        for (SpellEffect effect : effects) {
            if (!(effect instanceof SpellEffects plan)) continue;
            switch (plan) {
                case SpellEffects.Action action -> {
                    result.add(action.type());
                    if (action.type().equals(id("dwell_heal"))) result.add(id("heal"));
                    if (action.type().equals(id("random_teleport"))) result.add(id("teleport"));
                    if (action.type().equals(id("transpose"))) result.add(id("teleport"));
                    if (action.type().equals(id("shape_stone")) || action.type().equals(id("create_water"))) result.add(id("alter_blocks"));
                }
                case SpellEffects.Sequence sequence -> collect(sequence.effects(), result);
                case SpellEffects.Branch branch -> { collect(branch.whenTrue(), result); collect(branch.whenFalse(), result); }
                case SpellEffects.ForEach each -> collect(each.effects(), result);
                case SpellEffects.Repeat repeat -> collect(repeat.effects(), result);
                case SpellEffects.Secondary secondary -> collect(secondary.effects(), result);
                case SpellEffects.Limited limited -> collect(limited.effects(), result);
                case SpellEffects.InstallBinding binding -> collect(binding.binding().effects(), result);
                case SpellEffects.CreateManifestation manifestation -> {
                    String kind = manifestation.manifestation().kind().toString();
                    result.add(manifestation.manifestation().kind());
                    String behavior=manifestation.manifestation().identifiers().getOrDefault("behavior",id("none")).getPath();
                    if (kind.equals("vestige:construct")) {
                        if (behavior.equals("heal")) result.add(id("heal"));
                        if (behavior.equals("pressure")) result.add(id("damage"));
                        if (behavior.equals("protect")) result.add(id("shield"));
                    }
                    if (manifestation.manifestation().identifiers().containsKey("formation")) result.add(id("alter_blocks"));
                    if (kind.equals("vestige:guard")) result.add(id("shield"));
                    if (kind.equals("vestige:passage")) result.add(id("alter_blocks"));
                    if (kind.equals("vestige:block_wall")) { result.add(id("alter_blocks"));result.add(id("lift")); }
                    if (kind.equals("vestige:projectile")) result.add(id("damage"));
                    if (kind.equals("vestige:summon")) result.add(id("summon"));
                    if (kind.equals("vestige:decoy")) { result.add(id("summon")); result.add(id("aggro_decoy")); }
                    if (kind.equals("vestige:block_lock")) result.add(id("alter_blocks"));
                    for (SpellEffects.Binding binding : manifestation.manifestation().bindings()) collect(binding.effects(), result);
                    collect(manifestation.manifestation().onHit(), result);
                    collect(manifestation.manifestation().onTick(), result);
                    collect(manifestation.manifestation().onEnd(), result);
                    if (kind.equals("vestige:portal")) result.add(id("teleport"));
                    if (kind.equals("vestige:barrier")) result.add(id("shield"));
                    if (kind.equals("vestige:tether")) result.add(id("tether"));
                    if (kind.equals("vestige:wall")) { result.add(id("shield")); if (manifestation.manifestation().values().containsKey("damage")) result.add(id("damage")); }
                }
                default -> { }
            }
        }
    }
    private static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("vestige", name); }
}
