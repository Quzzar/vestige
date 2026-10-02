package com.quzzar.vestige.magic.world;

import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Uses Minecraft's block-model renderer and display interpolation without custom slab geometry. */
final class SpellBlockDisplayRenderer extends DisplayRenderer.BlockDisplayRenderer {
    SpellBlockDisplayRenderer(EntityRendererProvider.Context context) { super(context); }
}
