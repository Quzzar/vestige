package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.presentation.SpellVisual;
import com.quzzar.vestige.magic.presentation.SpellVisualPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** The owner's diamond/spokes and advanced crossed graph, using the shared native visual renderer. */
public final class RitualPresentation {
    public static final int RUNE_COLOR = 0xf4e5ff;
    private RitualPresentation() { }
    public static void connections(RitualCrafting.Layout layout,int duration,boolean success) {
        List<int[]> edges=new ArrayList<>();
        for (int inner:new int[]{0,2,4,6}) { edges.add(new int[]{-1,inner}); edges.add(new int[]{inner,(inner+2)%8}); }
        if (layout.advanced()) for (int outer:new int[]{1,3,5,7}) {
            double nearest=Double.POSITIVE_INFINITY;
            for (int inner:new int[]{0,2,4,6}) nearest=Math.min(nearest,layout.stands().get(outer).getBlockPos().distSqr(layout.stands().get(inner).getBlockPos()));
            for (int inner:new int[]{0,2,4,6}) if (Math.abs(layout.stands().get(outer).getBlockPos().distSqr(layout.stands().get(inner).getBlockPos())-nearest)<1e-8) edges.add(new int[]{outer,inner});
        }
        for (int[] edge:edges) {
            var a=point(layout,edge[0],success); var b=point(layout,edge[1],success);
            var visual=new SpellVisual.Resolved(duration,.06f,List.of(new SpellVisual.Layer(SpellVisual.Shape.BEAM,RUNE_COLOR,.8f,.023f,1,0,0,1)));
            send(layout,new SpellVisualPayload(UUID.randomUUID(),layout.level().dimension().location(),visual,List.of(a,b),0,0,false,false,false));
        }
        Vec3 center=Vec3.atBottomCenterOf(layout.center().getBlockPos()).add(0,((ApparatusBlock)layout.center().getBlockState().getBlock()).offeringHeight()+.04,0);
        var visual=new SpellVisual.Resolved(duration,success ? 1.25f : .8f,List.of(
                new SpellVisual.Layer(SpellVisual.Shape.SIGIL,RUNE_COLOR,.7f,.018f,1,success ? .4f : 0,0,8),
                new SpellVisual.Layer(SpellVisual.Shape.MOTES,RUNE_COLOR,.7f,.03f,.5f,.5f,0,8)));
        send(layout,new SpellVisualPayload(UUID.randomUUID(),layout.level().dimension().location(),visual,
                List.of(new SpellVisualPayload.Point(center,-1,new UUID(0,0),0)),0,0,false,false,false));
    }
    private static SpellVisualPayload.Point point(RitualCrafting.Layout layout,int seat,boolean raised) {
        var stand=seat<0 ? layout.center() : layout.stands().get(seat);
        var block=(ApparatusBlock)stand.getBlockState().getBlock();
        Vec3 at=Vec3.atBottomCenterOf(stand.getBlockPos()).add(0,block.offeringHeight()+.035+(raised && seat>=0 ? .12 : 0),0);
        return new SpellVisualPayload.Point(at,-1,new UUID(0,0),0);
    }
    private static void send(RitualCrafting.Layout layout,SpellVisualPayload payload) {
        Vec3 center=Vec3.atCenterOf(layout.center().getBlockPos());
        for (var player:layout.level().players()) if (player.distanceToSqr(center)<64*64) PacketDistributor.sendToPlayer(player,payload);
    }
}
