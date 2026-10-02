package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.presentation.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Replays authored cues in Minecraft without executing damage, resources, terrain or summons. */
public final class SpellEffectGallery {
    public record Phase(String label,SpellVisual visual, Map<String, com.quzzar.vestige.magic.expression.SpellValue> geometry) {
        public Phase { geometry=Map.copyOf(geometry); }
        public Phase(String label, SpellVisual visual) { this(label,visual,Map.of()); }
    }
    private SpellEffectGallery() { }
    public static List<Phase> phases(SpellDefinition spell) {
        List<Phase> result=new ArrayList<>(); walk(spell.effects(),"Primary",result);
        spell.modes().values().stream().sorted(Comparator.comparing(m -> m.id().toString())).forEach(mode -> walk(mode.effects(),mode.id().getPath(),result));
        return List.copyOf(result);
    }
    private static void walk(List<SpellEffect> effects,String path,List<Phase> phases) {
        for (SpellEffect effect : effects) switch (effect) {
            case SpellEffects.Visual visual -> phases.add(new Phase(path,visual.visual()));
            case SpellEffects.ForEach each -> { each.visual().ifPresent(v -> phases.add(new Phase(path+" / selection",v))); walk(each.effects(),path,phases); }
            case SpellEffects.Sequence sequence -> walk(sequence.effects(),path,phases);
            case SpellEffects.Repeat repeat -> walk(repeat.effects(),path+" / repeated",phases);
            case SpellEffects.Branch branch -> { walk(branch.whenTrue(),path+" / conditional",phases); walk(branch.whenFalse(),path+" / otherwise",phases); }
            case SpellEffects.InstallBinding binding -> walk(binding.binding().effects(),path+" / reaction",phases);
            case SpellEffects.CreateManifestation create -> {
                var m=create.manifestation(); String kind=m.kind().getPath();
                m.visual().ifPresent(v -> phases.add(new Phase(path+" / "+kind,v,m.values())));
                walk(m.onHit(),path+" / impact or destruction",phases); walk(m.onTick(),path+" / active pulse",phases); walk(m.onEnd(),path+" / release",phases);
                m.bindings().forEach(b -> walk(b.effects(),path+" / reaction",phases));
            }
            default -> { }
        }
    }
    public static int show(CommandSourceStack source,SpellDefinition spell,int phase) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (spell==null) { source.sendFailure(Component.literal("Unknown native spell")); return 0; }
        var phases=phases(spell);
        if (phase>=phases.size()) { source.sendFailure(Component.literal("Choose phase 0.."+(phases.size()-1))); return 0; }
        ServerPlayer player=source.getPlayerOrException(); Phase selected=phases.get(phase);
        Vec3 point=player.position().add(player.getLookAngle().multiply(1,0,1).normalize().scale(4)).add(0,selected.visual().height(),0);
        var points=List.of(new SpellVisualPayload.Point(player.getEyePosition(),-1,new UUID(0,0),0),new SpellVisualPayload.Point(point,-1,new UUID(0,0),0));
        double radius=selected.visual().radius().resolve(spell.traits());
        SpellVisual visual=new SpellVisual(Math.min(200,selected.visual().duration()),selected.visual().radius(),selected.visual().height(),selected.visual().layers(),false,selected.visual().sound());
        NativeMagic.session(player.getServer()).world().visuals.start(player.serverLevel(),visual,radius,false,()->points,()->true);
        source.sendSuccess(()->Component.literal(spell.id()+" — "+phase+" / "+(phases.size()-1)+": "+selected.label()+". Renderer preview; actual casts use world targets."),false);
        for(int i=0;i<phases.size();i++) { int index=i; source.sendSuccess(()->Component.literal(index+": "+phases.get(index).label()),false); }
        return 1;
    }
}
