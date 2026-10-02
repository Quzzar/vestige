package com.quzzar.vestige.magic.presentation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.quzzar.vestige.magic.world.SpellProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/** A received procedural body replaces the item billboard; otherwise retain visible fallback delivery. */
public final class SpellProjectileRenderer extends ThrownItemRenderer<SpellProjectile> {
    public SpellProjectileRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public void render(SpellProjectile entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
        if (!entity.hasVisual() || !SpellVisualClient.follows(entity.getUUID())) super.render(entity, yaw, partial, pose, buffer, light);
    }
}
