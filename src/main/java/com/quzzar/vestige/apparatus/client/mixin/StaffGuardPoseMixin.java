package com.quzzar.vestige.apparatus.client.mixin;

import com.quzzar.vestige.apparatus.MundaneStaffItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies the Staff brace after vanilla has resolved the ordinary held-item arm pose. */
@Mixin(LivingEntityRenderer.class)
abstract class StaffGuardPoseMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow protected M model;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V", shift = At.Shift.AFTER))
    private void vestige$staffBrace(T entity, float yaw, float partialTick,
                                    com.mojang.blaze3d.vertex.PoseStack poses,
                                    net.minecraft.client.renderer.MultiBufferSource buffers,
                                    int light, CallbackInfo callback) {
        if (!(entity.getUseItem().getItem() instanceof MundaneStaffItem) || !(model instanceof HumanoidModel<?> humanoid)) return;
        boolean right = entity.getUsedItemHand() == InteractionHand.OFF_HAND
                ? entity.getMainArm() != HumanoidArm.RIGHT
                : entity.getMainArm() == HumanoidArm.RIGHT;
        ModelPart arm = right ? humanoid.rightArm : humanoid.leftArm;
        // A wizard's hold: the arm reaches outward around chest height, while the full-length
        // Staff rises above the hand and plants below it.
        arm.xRot = -1.00F;
        arm.yRot = 0.10F;
        arm.zRot = right ? 0.42F : -0.42F;
    }
}
