package com.quzzar.vestige.magic.presentation;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Bounded client-only presentation notification; no client-to-server casting channel. */
public record SpellVisualPayload(UUID id, ResourceLocation dimension, SpellVisual.Resolved visual,
                                 List<Point> points, long seed, int elapsed, boolean follow,
                                 boolean stop, boolean burst) implements CustomPacketPayload {
    public static final Type<SpellVisualPayload> TYPE = new Type<>(VestigeMainMod.location("spell_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SpellVisualPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.write(buffer), SpellVisualPayload::read);
    public SpellVisualPayload {
        Objects.requireNonNull(id); Objects.requireNonNull(dimension); Objects.requireNonNull(visual);
        points = List.copyOf(points);
        if (points.isEmpty() || points.size() > 33) throw new IllegalArgumentException("Visuals need 1..33 points");
        if (elapsed < 0 || elapsed >= visual.duration()) throw new IllegalArgumentException("Invalid visual age");
    }
    public record Point(Vec3 position, int entityId, UUID entityUuid, float height) {
        public Point {
            Objects.requireNonNull(position); Objects.requireNonNull(entityUuid);
            if (!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)
                    || Math.abs(position.x) > 3.1e7 || Math.abs(position.y) > 3.1e7 || Math.abs(position.z) > 3.1e7
                    || entityId < -1 || !Float.isFinite(height) || height < 0 || height > 4)
                throw new IllegalArgumentException("Invalid visual anchor");
        }
    }
    @Override public Type<SpellVisualPayload> type() { return TYPE; }
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(id); buffer.writeResourceLocation(dimension);
        buffer.writeVarInt(visual.duration()); buffer.writeFloat(visual.radius());
        buffer.writeVarInt(visual.layers().size());
        for (SpellVisual.Layer layer : visual.layers()) {
            buffer.writeEnum(layer.shape()); buffer.writeInt(layer.color());
            buffer.writeFloat(layer.alpha()); buffer.writeFloat(layer.width()); buffer.writeFloat(layer.scale());
            buffer.writeFloat(layer.speed()); buffer.writeFloat(layer.phase()); buffer.writeVarInt(layer.count());
        }
        buffer.writeVarInt(points.size());
        for (Point point : points) {
            buffer.writeDouble(point.position().x); buffer.writeDouble(point.position().y); buffer.writeDouble(point.position().z);
            buffer.writeInt(point.entityId()); buffer.writeUUID(point.entityUuid()); buffer.writeFloat(point.height());
        }
        buffer.writeLong(seed); buffer.writeVarInt(elapsed);
        buffer.writeBoolean(follow); buffer.writeBoolean(stop); buffer.writeBoolean(burst);
    }
    private static SpellVisualPayload read(RegistryFriendlyByteBuf buffer) {
        UUID id = buffer.readUUID(); ResourceLocation dimension = buffer.readResourceLocation();
        int duration = buffer.readVarInt(); float radius = buffer.readFloat();
        int count = count(buffer, 8); List<SpellVisual.Layer> layers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) layers.add(new SpellVisual.Layer(buffer.readEnum(SpellVisual.Shape.class),
                buffer.readInt(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readVarInt()));
        count = count(buffer, 33); List<Point> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) points.add(new Point(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readInt(), buffer.readUUID(), buffer.readFloat()));
        return new SpellVisualPayload(id, dimension, new SpellVisual.Resolved(duration, radius, layers), points,
                buffer.readLong(), buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }
    private static int count(RegistryFriendlyByteBuf buffer, int max) {
        int count = buffer.readVarInt();
        if (count < 1 || count > max) throw new IllegalArgumentException("Visual list exceeds its bound");
        return count;
    }
}
