package com.quzzar.vestige.travel;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Server-owned positional history. Recent samples stay exact; long windows compact older samples without capping lookback. */
public final class PositionTrail {
    public static final int MAX_SAMPLES=4096;
    public record Sample(long tick,ResourceLocation dimension,Vec3 position) { }
    private final ArrayList<Sample> samples=new ArrayList<>();
    public void record(long tick,ResourceLocation dimension,Vec3 position,long lookback) {
        if(!samples.isEmpty() && (!samples.getLast().dimension().equals(dimension) || tick<samples.getLast().tick()))samples.clear();
        var next=new Sample(tick,dimension,position);
        if(!samples.isEmpty() && samples.getLast().tick()==tick)samples.set(samples.size()-1,next);else samples.add(next);
        long cutoff=tick-Math.max(1,lookback);
        int discard=0;while(discard+1<samples.size() && samples.get(discard+1).tick()<=cutoff)discard++;
        if(discard>0)samples.subList(0,discard).clear();
        // Preserve the oldest boundary and the latest 2048 ticks when a very large Time boost needs more history.
        if(samples.size()>MAX_SAMPLES)for(int i=2047;i>0;i-=2)samples.remove(i);
    }
    public Optional<Sample> destination(long now,long lookback) {
        if(samples.isEmpty())return Optional.empty();
        long desired=now-Math.max(1,lookback);
        int low=0,high=samples.size()-1;
        while(low<high) {int mid=(low+high+1)/2;if(samples.get(mid).tick()<=desired)low=mid;else high=mid-1;}
        return Optional.of(samples.get(low));
    }
    public int size() { return samples.size(); }
}
