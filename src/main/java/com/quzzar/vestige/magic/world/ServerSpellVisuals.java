package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.presentation.SpellVisual;
import com.quzzar.vestige.magic.presentation.SpellVisualPayload;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Per-server cosmetic leases, observer snapshots and early cleanup. */
final class ServerSpellVisuals {
    private final Map<UUID, Active> active = new LinkedHashMap<>();
    UUID start(ServerLevel level, SpellVisual definition, double radius, boolean follow,
               Supplier<List<SpellVisualPayload.Point>> points, BooleanSupplier alive) {
        return start(level,definition,radius,follow,points,alive,Optional.empty());
    }
    UUID start(ServerLevel level, SpellVisual definition, double radius, boolean follow,
               Supplier<List<SpellVisualPayload.Point>> points, BooleanSupplier alive,Optional<UUID> observer) {
        if (active.size() >= 256) return null;
        SpellVisual.Resolved visual = definition.resolve(radius);
        UUID id = UUID.randomUUID();
        Active cue = new Active(id, level, visual, follow, points, alive, level.getGameTime(), id.getLeastSignificantBits() ^ id.getMostSignificantBits(),observer);
        cue.payload(false, false); // Validate anchor bounds before installing a lease.
        active.put(id, cue);
        refresh(cue);
        if (observer.isEmpty()) definition.sound().ifPresent(sound -> BuiltInRegistries.SOUND_EVENT.getOptional(sound.id()).ifPresent(event -> {
            var at = points.get().getLast().position();
            level.playSound(null, at.x, at.y, at.z, event, SoundSource.PLAYERS, sound.volume(), sound.pitch());
        }));
        return id;
    }
    void stop(UUID id, boolean burst) {
        Active cue = active.remove(id); if (cue == null) return;
        SpellVisualPayload payload = cue.payload(true, burst);
        for (UUID observer : cue.observers) {
            ServerPlayer player = cue.level.getServer().getPlayerList().getPlayer(observer);
            if (player != null && player.level() == cue.level) PacketDistributor.sendToPlayer(player, payload);
        }
    }
    void tick() {
        for (Active cue : List.copyOf(active.values())) {
            if (!cue.alive.getAsBoolean() || cue.age() >= cue.visual.duration()) stop(cue.id, false);
            else refresh(cue);
        }
    }
    void close() { for (UUID id : List.copyOf(active.keySet())) stop(id, false); }
    List<SpellVisualPayload> snapshot() { return active.values().stream().map(cue -> cue.payload(false, false)).toList(); }
    private void refresh(Active cue) {
        Set<UUID> visible = new HashSet<>();
        List<SpellVisualPayload.Point> points = cue.points.get();
        double range = 64 + cue.visual.radius();
        boolean snapshotDue = cue.follow && cue.age() >= cue.nextSnapshot;
        for (ServerPlayer player : cue.level.players()) {
            if (cue.observer.isPresent() && !cue.observer.get().equals(player.getUUID())) continue;
            if (points.stream().noneMatch(point -> point.position().distanceToSqr(player.position()) <= range * range)) continue;
            visible.add(player.getUUID());
            if (snapshotDue || !cue.observers.contains(player.getUUID())) PacketDistributor.sendToPlayer(player, cue.payload(false, false));
        }
        for (UUID observer : cue.observers) {
            if (visible.contains(observer)) continue;
            ServerPlayer player = cue.level.getServer().getPlayerList().getPlayer(observer);
            if (player != null && player.level() == cue.level) PacketDistributor.sendToPlayer(player, cue.payload(true, false));
        }
        cue.observers = visible;
        if (snapshotDue) cue.nextSnapshot = cue.age() + 20;
    }
    private static final class Active {
        final UUID id;
        final ServerLevel level;
        final SpellVisual.Resolved visual;
        final boolean follow;
        final Supplier<List<SpellVisualPayload.Point>> points;
        final BooleanSupplier alive;
        final long started, seed;
        final Optional<UUID> observer;
        Set<UUID> observers = new HashSet<>();
        int nextSnapshot = 20;
        Active(UUID id, ServerLevel level, SpellVisual.Resolved visual, boolean follow,
               Supplier<List<SpellVisualPayload.Point>> points, BooleanSupplier alive, long started, long seed,Optional<UUID> observer) {
            this.id = id; this.level = level; this.visual = visual; this.follow = follow;
            this.points = points; this.alive = alive; this.started = started; this.seed = seed;
            this.observer=observer;
        }
        int age() { return (int) Math.max(0, level.getGameTime() - started); }
        SpellVisualPayload payload(boolean stop, boolean burst) {
            return new SpellVisualPayload(id, level.dimension().location(), visual, points.get(), seed,
                    Math.min(visual.duration() - 1, age()), follow, stop, burst);
        }
    }
}
