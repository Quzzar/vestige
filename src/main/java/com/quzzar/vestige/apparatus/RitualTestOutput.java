package com.quzzar.vestige.apparatus;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Native test observations of ordinary world drops, independent of apparatus storage. */
public final class RitualTestOutput {
    private RitualTestOutput() { }
    public static ItemEntity entity(OfferingBlockEntity center) {
        var p=center.getBlockPos();
        var drops=center.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(p.getX()+.1,p.getY()-3,p.getZ()+.1,p.getX()+.9,p.getY()+4,p.getZ()+.9),ItemEntity::isAlive);
        if(drops.size()>1)throw new AssertionError("Ritual produced multiple centered drops: "+drops.size());
        return drops.isEmpty()?null:drops.getFirst();
    }
    public static ItemStack stack(OfferingBlockEntity center) {
        var item=entity(center);return item==null?ItemStack.EMPTY:item.getItem();
    }
    public static ItemStack take(OfferingBlockEntity center) {
        var item=entity(center);if(item==null)return ItemStack.EMPTY;
        var stack=item.getItem().copy();item.discard();return stack;
    }
}
