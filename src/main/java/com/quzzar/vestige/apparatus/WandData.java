package com.quzzar.vestige.apparatus;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.*;

/** Versioned component identities and one validated source scroll, without executable item-owned plans. */
public final class WandData {
    private static final List<String> SPELL_KEYS=List.of("vestige_spell","vestige_modifiers","vestige_shaping",
            "vestige_casting_cost","vestige_spellshaping","vestige_augments");
    public record Binding(WandComponents.Base base,MagicalThreadRecipe.Type thread,Optional<WandTips.Tip> tip,ScrollItems.Scroll scroll) {
        public Binding(WandComponents.Base base,MagicalThreadRecipe.Type thread,ScrollItems.Scroll scroll){this(base,thread,Optional.empty(),scroll);}
    }
    private WandData() { }
    public static ItemStack create(WandComponents.Base base,MagicalThreadRecipe.Type thread,ItemStack source) {
        return create(base,thread,Optional.empty(),source);
    }
    public static ItemStack create(WandComponents.Base base,MagicalThreadRecipe.Type thread,Optional<WandTips.Tip> tip,ItemStack source) {
        ScrollItems.scroll(source).orElseThrow(() -> new IllegalArgumentException("Invalid source scroll"));
        var stack=new ItemStack(ScrollItems.WAND.get());
        stack.set(DataComponents.MAX_DAMAGE,WandComponents.durability(base,thread));
        var magic=new CompoundTag();var sourceTag=source.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        for (var key:SPELL_KEYS) if (sourceTag.contains(key)) magic.put(key,Objects.requireNonNull(sourceTag.get(key)).copy());
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag -> {
            tag.putInt("vestige_wand_version",1);tag.putString("base",base.id());tag.putString("thread",thread.id().toString());tag.put("scroll",magic);tip.ifPresent(t -> tag.putString("tip",t.id()));
        });
        return stack;
    }
    public static Optional<Binding> binding(ItemStack stack) {
        if (!stack.is(ScrollItems.WAND.get()) || stack.getCount()!=1) return Optional.empty();
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if (!tag.contains("vestige_wand_version",Tag.TAG_INT) || tag.getInt("vestige_wand_version")!=1
                || !tag.contains("scroll",Tag.TAG_COMPOUND)) return Optional.empty();
        var base=Arrays.stream(WandComponents.Base.values()).filter(b -> b.id().equals(tag.getString("base"))).findFirst().orElse(null);
        var thread=MagicalThreadRecipe.types().stream().filter(t -> t.id().toString().equals(tag.getString("thread"))).findFirst().orElse(null);
        if (base==null || thread==null || stack.getMaxDamage()!=WandComponents.durability(base,thread)
                || stack.getDamageValue()<0 || stack.getDamageValue()>=stack.getMaxDamage()) return Optional.empty();
        Optional<WandTips.Tip> tip=Optional.empty();
        if(tag.contains("tip")){
            if(!tag.contains("tip",Tag.TAG_STRING))return Optional.empty();
            tip=Arrays.stream(WandTips.Tip.values()).filter(t -> t.id().equals(tag.getString("tip"))).findFirst();
            if(tip.isEmpty())return Optional.empty();
        }
        var chosenTip=tip;
        var magic=tag.getCompound("scroll");
        if (magic.getAllKeys().stream().anyMatch(key -> !SPELL_KEYS.contains(key))) return Optional.empty();
        var source=new ItemStack(ScrollItems.SCROLL.get());source.set(DataComponents.CUSTOM_DATA,CustomData.of(magic));
        return ScrollItems.scroll(source).map(scroll -> new Binding(base,thread,chosenTip,scroll));
    }
}
