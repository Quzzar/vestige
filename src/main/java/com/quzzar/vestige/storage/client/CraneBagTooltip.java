package com.quzzar.vestige.storage.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.storage.CraneBagItem;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Keep the shared signature next to the name; vanilla inserts a bundle image between text lines. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class CraneBagTooltip {
    private CraneBagTooltip() { }
    @SubscribeEvent public static void gather(RenderTooltipEvent.GatherComponents event) {
        var key = CraneBagItem.key(event.getItemStack()); if (key.isEmpty()) return;
        var mark = AttunementMark.fromKey(key.get()).component(); var elements = event.getTooltipElements();
        int runes = -1, image = -1;
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).left().filter(mark::equals).isPresent()) runes = i;
            if (elements.get(i).right().filter(component -> component instanceof BundleTooltip).isPresent()) image = i;
        }
        if (image >= 0 && runes > image) {
            var contents = elements.remove(image);
            elements.add(runes, contents); // Removing the earlier image moved the rune line back one position.
        }
    }
}
