package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.definition.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Compact server-approved component combinations; displays derive from the actual binding compiler. */
public final class WandDisplays {
    private WandDisplays() { }
    public record Option(MagicalThreadRecipe.Type thread,Optional<WandTips.Tip> tip,int bases) {
        public Option {if(thread==null || tip==null || bases<1 || bases>127)throw new IllegalArgumentException("Invalid wand display option");}
    }
    public record Source(CompoundTag magic,List<Option> options) {
        public Source {
            magic=magic.copy();options=List.copyOf(options);
            if(magic.toString().length()>16384 || options.isEmpty() || options.size()>45 || ScrollItems.scroll(stack(magic)).isEmpty()
                    || options.stream().distinct().count()!=options.size())throw new IllegalArgumentException("Invalid wand display source");
            // Validate the exact trusted scroll schema through the same item parser as casting.
            var sample=WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,stack(magic));
            if(WandData.binding(sample).isEmpty() || !magic.equals(sample.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("scroll")))throw new IllegalArgumentException("Invalid wand source data");
        }
        @Override public CompoundTag magic(){return magic.copy();}
        public ItemStack scroll(){return stack(magic);}
        public String key(){return UUID.nameUUIDFromBytes(magic.toString().getBytes(StandardCharsets.UTF_8)).toString();}
    }
    public record Entry(Source source,Option option) {
        public ResourceLocation id(){return VestigeMainMod.location("wand/"+source.key()+"/"+option.thread().id().getPath()+"/"+option.tip().map(WandTips.Tip::id).orElse("untipped"));}
        public List<WandComponents.Base> bases(){return Arrays.stream(WandComponents.Base.values()).filter(b -> (option.bases() & (1<<b.ordinal()))!=0).toList();}
        public ItemStack output(WandComponents.Base base){return WandData.create(base,option.thread(),option.tip(),source.scroll());}
        public List<ItemStack> outputs(){return bases().stream().map(this::output).toList();}
        public ItemStack offering(int seat,WandComponents.Base base){return switch(seat){
            case WandRecipe.BASE -> new ItemStack(base.ingredient());case WandRecipe.THREAD -> new ItemStack(option.thread().item());
            case WandRecipe.SCROLL_A,WandRecipe.SCROLL_B,WandRecipe.SCROLL_C -> source.scroll();
            case WandRecipe.TIP -> option.tip().map(t -> new ItemStack(t.item())).orElse(ItemStack.EMPTY);default -> ItemStack.EMPTY;};}
        public RitualDisplays.Entry diagram(){
            var base=bases().getFirst();var parts=new ArrayList<RitualDisplays.Offering>();
            for(int seat=0;seat<8;seat++){var stack=offering(seat,base);if(!stack.isEmpty())parts.add(new RitualDisplays.Offering(seat,new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(stack.getItem())),List.of())));}
            return new RitualDisplays.Entry(id(),Optional.empty(),false,SpellRarity.COMMON,8,parts);
        }
    }
    private static ItemStack stack(CompoundTag magic){var s=new ItemStack(ScrollItems.SCROLL.get());s.set(DataComponents.CUSTOM_DATA,CustomData.of(magic));return s;}
    public static CompoundTag magic(ItemStack source){
        var normalized=WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,source);
        return normalized.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("scroll").copy();
    }
    public static Source compile(SpellDefinition spell,ItemStack stack){
        var scroll=ScrollItems.scroll(stack).orElseThrow();var options=new ArrayList<Option>();
        for(var core:MagicalThreadRecipe.types())for(int tip=-1;tip<WandTips.Tip.values().length;tip++) {
            var chosen=tip<0?Optional.<WandTips.Tip>empty():Optional.of(WandTips.Tip.values()[tip]);int bases=0;
            for(var base:WandComponents.Base.values())try{WandComponents.compile(spell,scroll,base,core,chosen);bases|=1<<base.ordinal();}catch(IllegalArgumentException unsupported){ }
            if(bases!=0)options.add(new Option(core,chosen,bases));
        }
        return new Source(magic(stack),options);
    }
    /** Immutable structured identity avoids parsing/formatting a bound scroll in viewer hash loops. */
    public record Identity(String base,String thread,String tip,CustomData magic) { }
    private static final Map<CustomData,Identity> IDENTITIES=Collections.synchronizedMap(new WeakHashMap<>());
    public static Identity identity(CustomData data){
        return IDENTITIES.computeIfAbsent(data,value -> {
            var tag=value.copyTag();
            return new Identity(tag.getString("base"),tag.getString("thread"),tag.getString("tip"),CustomData.of(tag.getCompound("scroll")));
        });
    }
    /** Wear never changes recipe identity; component choices and the exact source magic do. */
    public static String subtype(ItemStack wand){
        return WandData.binding(wand).map(b -> b.base().id()+"|"+b.thread().id()+"|"+b.tip().map(WandTips.Tip::id).orElse("untipped")+"|"+
                wand.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("scroll")).orElse("");
    }
}
