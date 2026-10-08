package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/** Vanilla anvil combining bypasses Item.isRepairable and can merge incompatible stored magic. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID)
public final class MagicalRepairPolicy {
    private MagicalRepairPolicy() { }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST,receiveCanceled=true)
    public static void anvil(AnvilUpdateEvent event) {
        var left=event.getLeft();var right=event.getRight();
        if (right.isEmpty()) return; // Renaming alone keeps the exact stack.
        if (left.is(ScrollItems.FLUXED_FLINT.get()) || right.is(ScrollItems.FLUXED_FLINT.get())
                || (left.is(ScrollItems.WAND.get()) || left.is(ScrollItems.STAFF.get())) && right.is(left.getItem())) {
            // NeoForge 21.1.72's canceled hook leaves any previous rename output in the menu.
            if (event.getPlayer().containerMenu instanceof net.minecraft.world.inventory.AnvilMenu menu) {
                menu.getSlot(2).set(net.minecraft.world.item.ItemStack.EMPTY);
                menu.setMaximumCost(0);
            }
            event.setCanceled(true);
        }
    }
}
