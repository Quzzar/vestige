package com.quzzar.vestige.equipment;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import java.util.*;

/** Bounded trusted item selections. Stored data never contains executable effects or arbitrary modifiers. */
public final class ItemImbuements {
    private static final String KEY = "vestige_item_imbuements";
    private ItemImbuements() { }
    public static Optional<List<MagicAdjectives.Adjustment>> read(ItemStack stack, ResourceLocation family,
                                                                 Set<ResourceLocation> supported) {
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!root.contains(KEY)) return Optional.of(List.of());
        if (!root.contains(KEY, Tag.TAG_COMPOUND)) return Optional.empty();
        CompoundTag tag = root.getCompound(KEY);
        if (!tag.getAllKeys().equals(Set.of("version", "family", "adjustments"))
                || !tag.contains("version", Tag.TAG_INT) || tag.getInt("version") != 1
                || !tag.contains("family", Tag.TAG_STRING) || !tag.getString("family").equals(family.toString())
                || !(tag.get("adjustments") instanceof ListTag entries) || entries.size() > 8
                || !entries.isEmpty() && entries.getElementType() != Tag.TAG_COMPOUND) return Optional.empty();
        var result = new ArrayList<MagicAdjectives.Adjustment>();
        var seen = new HashSet<ResourceLocation>();
        for (Tag entry : entries) {
            CompoundTag selection = (CompoundTag) entry;
            if (!selection.getAllKeys().equals(Set.of("id", "degree")) || !selection.contains("id", Tag.TAG_STRING)
                    || !selection.contains("degree", Tag.TAG_INT) || selection.getInt("degree") != 1) return Optional.empty();
            ResourceLocation id = ResourceLocation.tryParse(selection.getString("id"));
            if (id == null || !supported.contains(id) || !seen.add(id)) return Optional.empty();
            result.add(new MagicAdjectives.Adjustment(id, 1));
        }
        result.sort(Comparator.comparing(a -> a.id().toString()));
        return Optional.of(List.copyOf(result));
    }
    public static void write(ItemStack stack, ResourceLocation family, List<MagicAdjectives.Adjustment> selections) {
        if (selections.size() > 8 || selections.stream().anyMatch(a -> a.degree() != 1)
                || selections.stream().map(MagicAdjectives.Adjustment::id).distinct().count() != selections.size())
            throw new IllegalArgumentException("Invalid item imbuements");
        var list = new ListTag();
        selections.stream().sorted(Comparator.comparing(a -> a.id().toString())).forEach(a -> {
            var entry = new CompoundTag(); entry.putString("id", a.id().toString()); entry.putInt("degree", a.degree()); list.add(entry);
        });
        var tag = new CompoundTag(); tag.putInt("version", 1); tag.putString("family", family.toString()); tag.put("adjustments", list);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, root -> root.put(KEY, tag));
    }
}
