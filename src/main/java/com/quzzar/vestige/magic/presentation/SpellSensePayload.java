package com.quzzar.vestige.magic.presentation;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

/** Finite client perception/movement lease. Never accepts casts or movement from the client. */
public record SpellSensePayload(UUID lease, ResourceLocation dimension, String kind, int entityId, UUID entity,
                                Vec3 center, double radius, int duration, boolean stop) implements CustomPacketPayload {
    public static final Type<SpellSensePayload> TYPE = new Type<>(VestigeMainMod.location("spell_sense"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellSensePayload> STREAM_CODEC = StreamCodec.of((b,p) -> {
        b.writeUUID(p.lease); b.writeResourceLocation(p.dimension); b.writeUtf(p.kind,32); b.writeInt(p.entityId);
        b.writeUUID(p.entity); b.writeDouble(p.center.x); b.writeDouble(p.center.y); b.writeDouble(p.center.z);
        b.writeDouble(p.radius); b.writeVarInt(p.duration); b.writeBoolean(p.stop);
    }, b -> new SpellSensePayload(b.readUUID(), b.readResourceLocation(), b.readUtf(32), b.readInt(), b.readUUID(),
            new Vec3(b.readDouble(),b.readDouble(),b.readDouble()), b.readDouble(), b.readVarInt(), b.readBoolean()));
    public SpellSensePayload {
        if (!java.util.Set.of("camera","silence","privacy","rain","water_walk","climb","absence","facade").contains(kind)
                || duration < 1 || duration > 2400 || !Double.isFinite(radius) || radius < 0 || radius > 128
                || !Double.isFinite(center.x) || !Double.isFinite(center.y) || !Double.isFinite(center.z))
            throw new IllegalArgumentException("Invalid perception lease");
    }
    @Override public Type<SpellSensePayload> type() { return TYPE; }
}
