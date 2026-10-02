package com.quzzar.vestige;

import com.quzzar.vestige.magic.world.SpellEntities;
import com.quzzar.vestige.magic.world.SpellBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Spell-only bootstrap. Equipment, discovery, and progression are intentionally deferred. */
@Mod(VestigeMainMod.MOD_ID)
public final class VestigeMod {
    public VestigeMod(IEventBus bus) { SpellEntities.TYPES.register(bus); SpellBlocks.BLOCKS.register(bus); com.quzzar.vestige.magic.world.SpellFluids.FLUIDS.register(bus); }
}
