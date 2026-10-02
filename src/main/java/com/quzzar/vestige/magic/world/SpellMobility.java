package com.quzzar.vestige.magic.world;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Same bounded contact movement on the logical server and local predicting client. */
public final class SpellMobility {
    private SpellMobility() { }
    public static void apply(LivingEntity target, String kind) {
        if (kind.equals("climb") && target.horizontalCollision && !target.isCrouching()) {
            Vec3 motion=target.getDeltaMovement(); target.setDeltaMovement(motion.x,.16,motion.z); target.fallDistance=0; target.hurtMarked=true;
        }
        if (kind.equals("water_walk") && !target.isCrouching()) {
            var below = net.minecraft.core.BlockPos.containing(target.getX(),target.getY()-.12,target.getZ());
            var fluid = target.level().getFluidState(below);
            double surface = below.getY()+fluid.getHeight(target.level(),below);
            if (fluid.is(FluidTags.WATER) && target.getY() >= surface-.22 && target.getY() <= surface+.3 && target.getDeltaMovement().y <= 0) {
                target.setPos(target.getX(),surface+.01,target.getZ());
                target.setDeltaMovement(target.getDeltaMovement().multiply(1,0,1)); target.setOnGround(true); target.fallDistance=0; target.hurtMarked=true;
            }
        }
    }
}
