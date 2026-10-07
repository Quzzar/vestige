package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;
import java.util.*;

@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class WandViewerClient {
    private static List<WandDisplays.Entry> entries=List.of();
    private static final List<WandDisplays.Source> pending=new ArrayList<>();private static int next;
    private WandViewerClient(){ }
    public static List<WandDisplays.Entry> displays(){return entries;}
    public static void accept(WandDisplayPayload p){
        if(p.page()==0){pending.clear();next=0;}if(p.page()!=next++)throw new IllegalArgumentException("Out-of-order wand page");
        pending.addAll(p.sources());if(!p.last())return;
        if(pending.stream().map(WandDisplays.Source::key).distinct().count()!=pending.size())throw new IllegalArgumentException("Duplicate wand source");
        entries=pending.stream().flatMap(s -> s.options().stream().map(o -> new WandDisplays.Entry(s,o))).toList();pending.clear();
        var connection=Minecraft.getInstance().getConnection();
        if(p.refresh() && connection!=null){
            if(ModList.get().isLoaded("emi")){
                com.quzzar.vestige.apparatus.recipeviewer.emi.EmiLiveRefresh.request();
            }else if(ModList.get().isLoaded("jei"))com.quzzar.vestige.apparatus.recipeviewer.jei.VestigeJeiPlugin.refreshWands();
        }
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){entries=List.of();pending.clear();next=0;}
}
