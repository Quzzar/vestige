package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.*;

/** Bounded native bindings store trusted scroll identities, never item-owned executable plans. */
public final class StaffData {
    private static final List<String> MAGIC_KEYS=List.of("vestige_spell","vestige_modifiers","vestige_shaping",
            "vestige_casting_cost","vestige_spellshaping","vestige_augments");
    private static final Set<ResourceLocation> EXCLUDED=Set.of(id("amplify"),id("range"),id("area"));
    public record Binding(ResourceLocation affinity,int capacity,int selected,List<Optional<ScrollItems.Scroll>> slots) {
        public Binding { slots=List.copyOf(slots); }
        public Optional<ScrollItems.Scroll> active() { return slots.get(selected); }
    }
    private StaffData() { }
    private static ResourceLocation id(String path) { return VestigeMainMod.location(path); }
    public static boolean affinityAllowed(ResourceLocation trait) { return trait!=null && !EXCLUDED.contains(trait); }
    public static boolean accepts(ResourceLocation trait,SpellDefinition spell) {
        return affinityAllowed(trait) && spell.traits().rating(trait)>0;
    }
    /** Starting playtest capacity, independent of affinity and spell rarity. */
    public static int durability(int capacity) {
        if (!List.of(2,4,6).contains(capacity)) throw new IllegalArgumentException("Invalid staff capacity");
        return 20*capacity;
    }
    /** Initial playtest wear, from the trusted native spell rarity rather than item-owned metadata. */
    public static int wear(SpellRarity rarity) {
        return switch(rarity) {case COMMON -> 1;case UNCOMMON -> 2;case RARE -> 3;case MYTHIC -> 4;};
    }
    public static ItemStack create(ResourceLocation trait) {
        if (!affinityAllowed(trait)) throw new IllegalArgumentException("Invalid staff affinity");
        var stack=new ItemStack(ScrollItems.STAFF.get());stack.set(DataComponents.MAX_DAMAGE,durability(2));
        var slots=new ListTag();for(int i=0;i<2;i++) slots.add(new CompoundTag());
        CustomData.update(DataComponents.CUSTOM_DATA,stack,t -> {
            t.putInt("vestige_staff_version",1);t.putString("affinity",trait.toString());
            t.putInt("capacity",2);t.putInt("selected",0);t.put("slots",slots);
        });
        return stack;
    }
    public static Optional<Binding> binding(ItemStack stack) {
        if (!stack.is(ScrollItems.STAFF.get()) || stack.getCount()!=1) return Optional.empty();
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if (!tag.contains("vestige_staff_version",Tag.TAG_INT) || tag.getInt("vestige_staff_version")!=1
                || !tag.contains("capacity",Tag.TAG_INT) || !tag.contains("selected",Tag.TAG_INT)
                || !tag.contains("affinity",Tag.TAG_STRING) || !tag.contains("slots",Tag.TAG_LIST)) return Optional.empty();
        int capacity=tag.getInt("capacity"),selected=tag.getInt("selected");
        var trait=ResourceLocation.tryParse(tag.getString("affinity"));
        if (!affinityAllowed(trait) || !List.of(2,4,6).contains(capacity) || selected<0 || selected>=capacity
                || stack.getMaxDamage()!=durability(capacity) || stack.getDamageValue()<0 || stack.getDamageValue()>=stack.getMaxDamage()) return Optional.empty();
        var raw=tag.get("slots");
        if (!(raw instanceof ListTag entries) || entries.size()!=capacity || entries.getElementType()!=Tag.TAG_COMPOUND) return Optional.empty();
        var slots=new ArrayList<Optional<ScrollItems.Scroll>>();
        for(int i=0;i<capacity;i++) {
            var entry=entries.getCompound(i);
            if (entry.isEmpty()) {slots.add(Optional.empty());continue;}
            if (entry.getAllKeys().stream().anyMatch(k -> !MAGIC_KEYS.contains(k))) return Optional.empty();
            var source=new ItemStack(ScrollItems.SCROLL.get());source.set(DataComponents.CUSTOM_DATA,CustomData.of(entry));
            var scroll=ScrollItems.scroll(source);if(scroll.isEmpty()) return Optional.empty();slots.add(scroll);
        }
        return Optional.of(new Binding(trait,capacity,selected,slots));
    }
    public static ItemStack select(ItemStack staff,int slot) {
        var binding=binding(staff).orElseThrow(() -> new IllegalArgumentException("Invalid staff"));
        if(slot<0 || slot>=binding.capacity()) throw new IllegalArgumentException("Invalid staff slot");
        var result=staff.copy();CustomData.update(DataComponents.CUSTOM_DATA,result,t -> t.putInt("selected",slot));return result;
    }
    public static ItemStack bind(ItemStack staff,ItemStack source,SpellDefinition spell) {
        var binding=binding(staff).orElseThrow(() -> new IllegalArgumentException("Invalid staff"));
        var scroll=ScrollItems.scroll(source).orElseThrow(() -> new IllegalArgumentException("Invalid scroll"));
        if (!spell.id().equals(scroll.spell()) || !accepts(binding.affinity(),spell)) throw new IllegalArgumentException("Incompatible staff affinity");
        // Validate the source before storing it. No binding-layout bonus is applied.
        Spellshaping.compile(spell,scroll.augments(),scroll.modifiers(),scroll.shaping());
        var magic=new CompoundTag();var sourceTag=source.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        for(var key:MAGIC_KEYS) if(sourceTag.contains(key)) magic.put(key,Objects.requireNonNull(sourceTag.get(key)).copy());
        var result=staff.copy();CustomData.update(DataComponents.CUSTOM_DATA,result,t -> t.getList("slots",Tag.TAG_COMPOUND).set(binding.selected(),magic));
        return result;
    }
    /** Reconstitute the stored native variant without applying shaping a second time. */
    public static ItemStack source(ItemStack staff,int slot) {
        var binding=binding(staff).orElseThrow(() -> new IllegalArgumentException("Invalid staff"));
        if(slot<0 || slot>=binding.capacity()) throw new IllegalArgumentException("Invalid staff slot");
        var entry=staff.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag()
                .getList("slots",Tag.TAG_COMPOUND).getCompound(slot);
        if(entry.isEmpty()) return ItemStack.EMPTY;
        var result=new ItemStack(ScrollItems.SCROLL.get());result.set(DataComponents.CUSTOM_DATA,CustomData.of(entry.copy()));return result;
    }
    /** Inventory storage only: the authoritative menu validates newly offered spells before insertion. */
    public static ItemStack withScrolls(ItemStack staff,List<ItemStack> sources) {
        var binding=binding(staff).orElseThrow(() -> new IllegalArgumentException("Invalid staff"));
        if(sources.size()!=binding.capacity()) throw new IllegalArgumentException("Invalid staff capacity");
        var entries=new ListTag();
        for(var source:sources) {
            var magic=new CompoundTag();
            if(!source.isEmpty()) {
                if(source.getCount()!=1 || ScrollItems.scroll(source).isEmpty()) throw new IllegalArgumentException("Invalid scroll slot");
                var tag=source.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
                for(var key:MAGIC_KEYS) if(tag.contains(key)) magic.put(key,Objects.requireNonNull(tag.get(key)).copy());
            }
            entries.add(magic);
        }
        var result=staff.copy();CustomData.update(DataComponents.CUSTOM_DATA,result,t -> t.put("slots",entries));return result;
    }
    public static ItemStack expand(ItemStack staff) {
        var binding=binding(staff).orElseThrow(() -> new IllegalArgumentException("Invalid staff"));
        if(binding.capacity()==6) throw new IllegalArgumentException("Staff already at capacity");
        var result=staff.copy();result.set(DataComponents.MAX_DAMAGE,durability(binding.capacity()+2));
        CustomData.update(DataComponents.CUSTOM_DATA,result,t -> {
            var slots=t.getList("slots",Tag.TAG_COMPOUND);slots.add(new CompoundTag());slots.add(new CompoundTag());t.putInt("capacity",binding.capacity()+2);
        });
        return result;
    }
}
