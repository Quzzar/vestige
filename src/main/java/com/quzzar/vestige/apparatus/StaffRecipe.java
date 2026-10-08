package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.world.item.*;
import java.util.*;

/** Fixed relative recipes: quarter-turns preserve them; ingredients and actual capacity are authoritative. */
public final class StaffRecipe {
    public enum Operation { CONSTRUCT,EXPAND }
    public record Match(Operation operation,int staffSeat,List<Integer> occupied) {
        public Match { occupied=List.copyOf(occupied); }
    }
    private StaffRecipe() { }
    public static Optional<Match> match(List<ItemStack> seats,int capacity) {
        if(seats.size()!=8 || !List.of(4,8).contains(capacity)
                || seats.stream().anyMatch(s -> !s.isEmpty() && s.getCount()!=1)) return Optional.empty();
        for(int r=0;r<8;r+=2) {
            if(capacity==8 && MundaneStaffs.shaft(at(seats,r,0)).isPresent()
                    && ScrollItems.fragment(at(seats,r,4)).filter(StaffData::affinityAllowed).isPresent()
                    && at(seats,r,6).is(Items.AMETHYST_SHARD) && at(seats,r,1).is(Items.IRON_INGOT)
                    && at(seats,r,3).is(ScrollItems.ENSORCELLED_THREAD.get()) && empty(seats,r,2,5,7))
                return Optional.of(new Match(Operation.CONSTRUCT,-1,indices(r,0,4,6,1,3)));
            var staff=StaffData.binding(at(seats,r,0)).orElse(null);if(staff==null) continue;
            if(capacity==4 && staff.capacity()==2 && at(seats,r,2).is(Items.DIAMOND)
                    && at(seats,r,4).is(Items.AMETHYST_SHARD) && at(seats,r,6).is(ScrollItems.ENSORCELLED_THREAD.get()))
                return Optional.of(new Match(Operation.EXPAND,r,indices(r,0,2,4,6)));
            if(capacity==8 && staff.capacity()==4 && at(seats,r,2).is(Items.ECHO_SHARD)
                    && at(seats,r,4).is(Items.DIAMOND) && at(seats,r,6).is(Items.AMETHYST_SHARD)
                    && at(seats,r,1).is(Items.IRON_INGOT) && at(seats,r,3).is(ScrollItems.ENSORCELLED_THREAD.get()) && empty(seats,r,5,7))
                return Optional.of(new Match(Operation.EXPAND,r,indices(r,0,2,4,6,1,3)));
        }
        return Optional.empty();
    }
    private static ItemStack at(List<ItemStack> seats,int r,int offset) { return seats.get((r+offset)%8); }
    private static boolean empty(List<ItemStack> seats,int r,int... offsets) { return Arrays.stream(offsets).allMatch(i -> at(seats,r,i).isEmpty()); }
    private static List<Integer> indices(int r,int... offsets) { return Arrays.stream(offsets).map(i -> (r+i)%8).boxed().toList(); }
    public static Optional<ItemStack> result(List<ItemStack> seats,int capacity) {
        var match=match(seats,capacity).orElse(null);if(match==null) return Optional.empty();
        try {
            return switch(match.operation()) {
                case CONSTRUCT -> {
                    var trait=match.occupied().stream().map(seats::get).map(ScrollItems::fragment).flatMap(Optional::stream).findFirst().orElseThrow();
                    yield NativeMagic.spells().spells().values().stream().anyMatch(s -> StaffData.accepts(trait,s))
                            ? Optional.of(StaffData.create(trait)) : Optional.empty();
                }
                case EXPAND -> Optional.of(StaffData.expand(seats.get(match.staffSeat())));

            };
        } catch(IllegalArgumentException invalid) { return Optional.empty(); }
    }
}
