package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.SpellKnowledge;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import java.util.*;

/** No optional viewer classes are loaded here; both adapters consume the same completed snapshot. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class RitualViewerClient {
    private static List<RitualDisplays.Entry> spells=List.of();
    private static final List<RitualDisplays.Entry> pending=new ArrayList<>();
    private static int nextPage;
    private RitualViewerClient() { }
    public static List<RitualDisplays.Entry> displays(){var all=new ArrayList<>(spells);all.add(RitualDisplays.shard());all.add(RitualDisplays.dissentientDiamond());all.add(RitualDisplays.fluxedFlint());all.add(RitualDisplays.homeboundEye());all.add(RitualDisplays.whisperingShell());all.addAll(RitualDisplays.threads());all.addAll(HourglassDisplays.entries());all.addAll(com.quzzar.vestige.equipment.MagicArmorDisplays.entries());all.addAll(com.quzzar.vestige.equipment.WayfarerDisplays.entries());return List.copyOf(all);}
    public static void accept(RitualDisplayPayload payload) {
        if(payload.page()==0){pending.clear();nextPage=0;}
        if(payload.page()!=nextPage++)throw new IllegalArgumentException("Out-of-order ritual display page");
        pending.addAll(payload.entries());
        if(payload.last()) {
            if(pending.stream().map(RitualDisplays.Entry::id).distinct().count()!=pending.size())throw new IllegalArgumentException("Duplicate ritual display ID");
            spells=List.copyOf(pending);pending.clear();
            SpellKnowledge.updateVisible(spells.stream().filter(RitualDisplays.Entry::identified)
                    .collect(java.util.stream.Collectors.toMap(entry -> entry.spell().orElseThrow(),RitualDisplays.Entry::rarity)));
            // Live knowledge changes must rebuild viewer inputs and searchable names immediately.
            var connection=Minecraft.getInstance().getConnection();
            if(payload.refresh() && connection!=null) {
                if(ModList.get().isLoaded("emi")) {
                    com.quzzar.vestige.apparatus.recipeviewer.emi.EmiLiveRefresh.request();
                }
                else if(ModList.get().isLoaded("jei"))com.quzzar.vestige.apparatus.recipeviewer.jei.VestigeJeiPlugin.refresh();
            }
            // Login and datapack synchronization use the ensuing vanilla recipe event.
        }
    }
    @SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){
        if(ModList.get().isLoaded("emi"))com.quzzar.vestige.apparatus.recipeviewer.emi.EmiLiveRefresh.tick();
    }
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){spells=List.of();pending.clear();nextPage=0;SpellKnowledge.updateVisible(Map.of());if(ModList.get().isLoaded("emi"))com.quzzar.vestige.apparatus.recipeviewer.emi.EmiLiveRefresh.clear();}
}
