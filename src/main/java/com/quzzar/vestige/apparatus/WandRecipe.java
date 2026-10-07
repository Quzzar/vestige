package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** One relative binding pattern; an entire quarter-turn preserves it, while reflection reverses it. */
public final class WandRecipe {
    // Inner: base, scroll, scroll, scroll. Outer: thread, reserved tip, empty, empty.
    // The first implementation slice binds untipped wands; the reserved tip seat is empty.
    public static final int BASE=0, THREAD=1, SCROLL_A=2, TIP=3, SCROLL_B=4, SCROLL_C=6;
    public record Match(WandComponents.Base base,MagicalThreadRecipe.Type thread,ItemStack scroll,List<Integer> occupied) {
        public Match { scroll=scroll.copyWithCount(1);occupied=List.copyOf(occupied); }
        @Override public ItemStack scroll() { return scroll.copy(); }
    }
    private WandRecipe() { }
    public static Optional<Match> match(List<ItemStack> seats) {
        if (seats.size()!=8 || seats.stream().anyMatch(s -> !s.isEmpty() && s.getCount()!=1)) return Optional.empty();
        for (int rotation=0;rotation<8;rotation+=2) {
            int r=rotation;
            var base=Arrays.stream(WandComponents.Base.values()).filter(b -> seats.get(r).is(b.ingredient())).findFirst().orElse(null);
            var core=MagicalThreadRecipe.types().stream().filter(t -> seats.get((THREAD+r)%8).is(t.item())).findFirst().orElse(null);
            var a=seats.get((SCROLL_A+r)%8);var b=seats.get((SCROLL_B+r)%8);var c=seats.get((SCROLL_C+r)%8);
            if (base==null || core==null || ScrollItems.scroll(a).isEmpty()
                    || !ItemStack.isSameItemSameComponents(a,b) || !ItemStack.isSameItemSameComponents(a,c)
                    || !seats.get((TIP+r)%8).isEmpty() || !seats.get((5+r)%8).isEmpty() || !seats.get((7+r)%8).isEmpty()) continue;
            return Optional.of(new Match(base,core,a,List.of(r,(THREAD+r)%8,(SCROLL_A+r)%8,(SCROLL_B+r)%8,(SCROLL_C+r)%8)));
        }
        return Optional.empty();
    }
    public static Optional<ItemStack> result(List<ItemStack> seats) {
        var matched=match(seats).orElse(null);
        if (matched==null) return Optional.empty();
        var scroll=ScrollItems.scroll(matched.scroll()).orElseThrow();
        var spell=NativeMagic.spells().spells().get(scroll.spell());
        if (spell==null) return Optional.empty();
        try {
            WandComponents.compile(spell,scroll,matched.base(),matched.thread());
            return Optional.of(WandData.create(matched.base(),matched.thread(),matched.scroll()));
        } catch (IllegalArgumentException incompatible) { return Optional.empty(); }
    }
}
