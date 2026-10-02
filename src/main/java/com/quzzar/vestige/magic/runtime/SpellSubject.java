package com.quzzar.vestige.magic.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Objects;
import java.util.UUID;

/** Stable references, so delayed effects never retain a live entity or chunk. */
public sealed interface SpellSubject {
    record Entity(UUID id) implements SpellSubject {
        public Entity { Objects.requireNonNull(id); }
    }
    record Block(ResourceKey<Level> dimension, BlockPos position) implements SpellSubject {
        public Block { Objects.requireNonNull(dimension); position = position.immutable(); }
    }
    record Position(ResourceKey<Level> dimension, net.minecraft.world.phys.Vec3 position) implements SpellSubject {
        public Position { Objects.requireNonNull(dimension); Objects.requireNonNull(position); }
    }
}
