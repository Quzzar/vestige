package com.quzzar.vestige;

import com.quzzar.vestige.magic.world.SpellEntities;
import com.quzzar.vestige.magic.world.SpellBlocks;
import com.quzzar.vestige.apparatus.ApparatusBlocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** Native spells, scrolls, fragment discovery and shared ritual apparatus. */
@Mod(VestigeMainMod.MOD_ID)
public final class VestigeMod {
    public VestigeMod(IEventBus bus) {
        SpellEntities.TYPES.register(bus);
        SpellBlocks.BLOCKS.register(bus);
        com.quzzar.vestige.magic.world.SpellFluids.FLUIDS.register(bus);
        ApparatusBlocks.BLOCKS.register(bus);
        ApparatusBlocks.ITEMS.register(bus);
        ApparatusBlocks.ENTITIES.register(bus);
        com.quzzar.vestige.travel.StandingStones.BLOCKS.register(bus);
        com.quzzar.vestige.travel.StandingStones.ITEMS.register(bus);
        com.quzzar.vestige.travel.StandingStones.ENTITIES.register(bus);
        com.quzzar.vestige.apparatus.ScrollItems.ITEMS.register(bus);
        com.quzzar.vestige.apparatus.ScrollItems.SERIALIZERS.register(bus);
    }
}
