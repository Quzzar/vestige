package com.quzzar.vestige.apparatus.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.SpellScrollItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Scroll identity is the whole tooltip, including when advanced tooltips are enabled. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class ScrollTooltip {
    private ScrollTooltip() { }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void tooltip(ItemTooltipEvent event) {
        if ((event.getItemStack().getItem() instanceof SpellScrollItem || event.getItemStack().getItem() instanceof com.quzzar.vestige.apparatus.SpellWandItem)
                && event.getToolTip().size()>1)
            event.getToolTip().subList(1,event.getToolTip().size()).clear();
    }
}
