package com.quzzar.vestige.apparatus.recipeviewer.emi;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Coalesces server snapshot changes until EMI's current index bake has completed. */
public final class EmiLiveRefresh {
    private static boolean pending;
    private EmiLiveRefresh(){ }
    public static void request(){pending=true;}
    public static void clear(){pending=false;}
    public static boolean ready(){
        // The pinned 1.1.24 public API exposes recipes before its asynchronous bake is complete.
        // Its readiness bridge is kept optional and reflective, outside ordinary client loading.
        try {
            boolean loaded=(boolean)Class.forName("dev.emi.emi.runtime.EmiReloadManager").getMethod("isLoaded").invoke(null);
            return loaded && Class.forName("dev.emi.emi.registry.EmiRecipes").getField("activeWorker").get(null)==null;
        } catch(ReflectiveOperationException unavailable){return false;}
    }
    public static void tick(){
        if(!pending || !ready())return;
        var connection=Minecraft.getInstance().getConnection();if(connection==null){clear();return;}
        pending=false;
        NeoForge.EVENT_BUS.post(new RecipesUpdatedEvent(connection.getRecipeManager()));
        NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.TagsUpdatedEvent(
                connection.registryAccess(),true,connection.getConnection().isMemoryConnection()));
    }
}
