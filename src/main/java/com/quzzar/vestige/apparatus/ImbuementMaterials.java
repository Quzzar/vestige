package com.quzzar.vestige.apparatus;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import java.util.HashMap;
import java.util.Map;

/** Cosmetic colors share one material identity; installed stacks retain their actual appearance. */
public final class ImbuementMaterials {
    private static final Map<ResourceLocation,ResourceLocation> COLOR_FAMILIES=colorFamilies();
    private ImbuementMaterials() { }

    public static ResourceLocation canonical(ResourceLocation material) {
        return COLOR_FAMILIES.getOrDefault(material,material);
    }

    private static Map<ResourceLocation,ResourceLocation> colorFamilies() {
        var families=new HashMap<ResourceLocation,ResourceLocation>();
        for(var type:new String[]{"wool","concrete"}) {
            var family=ResourceLocation.withDefaultNamespace("white_"+type);
            for(var color:DyeColor.values())
                families.put(ResourceLocation.withDefaultNamespace(color.getName()+"_"+type),family);
        }
        return Map.copyOf(families);
    }
}
