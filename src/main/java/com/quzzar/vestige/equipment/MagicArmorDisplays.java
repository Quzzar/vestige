package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import java.util.*;

/** Each color is a concrete four-matching-wool recipe; the viewer never suggests independently cycling wool. */
public final class MagicArmorDisplays {
    private MagicArmorDisplays() { }
    public static boolean owns(ResourceLocation id) {
        return id.getNamespace().equals("vestige") && (id.getPath().startsWith("ritual/wardweave_robes/") || id.getPath().startsWith("ritual/cinderweave_robes/"));
    }
    public static Optional<ItemStack> output(ResourceLocation id) {
        if (!owns(id)) return Optional.empty();
        String[] parts=id.getPath().split("/");if(parts.length!=3)return Optional.empty();
        DyeColor color=DyeColor.byName(parts[2],null);if(color==null)return Optional.empty();
        var result=new ItemStack(parts[1].equals("wardweave_robes")?MagicEquipment.WARDWEAVE.get():MagicEquipment.CINDERWEAVE.get());
        result.set(DataComponents.DYED_COLOR,new DyedItemColor(color.getTextureDiffuseColor(),false));return Optional.of(result);
    }
    public static List<RitualDisplays.Entry> entries() {
        var result=new ArrayList<RitualDisplays.Entry>();
        for(boolean cinder:List.of(false,true))for(DyeColor color:DyeColor.values()) {
            var offerings=new ArrayList<RitualDisplays.Offering>();
            for(int i=0;i<8;i++) {
                Item item=i%2==0?MagicArmorRecipe.wool(color):i==1||i==5?cinder?ScrollItems.SMOLDERING_THREAD.get():ScrollItems.CALLOUS_THREAD.get()
                        :i==3?cinder?Items.BLAZE_POWDER:Items.IRON_INGOT:cinder?Items.MAGMA_CREAM:Items.PUFFERFISH;
                offerings.add(new RitualDisplays.Offering(i,new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(item)),List.of())));
            }
            result.add(new RitualDisplays.Entry(VestigeMainMod.location("ritual/"+(cinder?"cinderweave":"wardweave")+"_robes/"+color.getName()),Optional.empty(),false,SpellRarity.COMMON,8,offerings));
        }
        return List.copyOf(result);
    }
}
