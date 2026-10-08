package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.runtime.SpellRuntime;
import com.quzzar.vestige.apparatus.ScrollCasting;
import com.quzzar.vestige.apparatus.WandCasting;
import com.quzzar.vestige.apparatus.StaffCasting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.*;

/** Server-owned progress for all native casting sources, with explicit clearing at every end path. */
final class NativePreparation {
    private static final Map<MinecraftServer, Set<UUID>> SENT = new IdentityHashMap<>();
    private NativePreparation() { }
    static void sync(MinecraftServer server, SpellRuntime runtime) {
        Set<UUID> previous = SENT.getOrDefault(server, Set.of());
        Set<UUID> current = new HashSet<>();
        for (var player : server.getPlayerList().getPlayers()) {
            if (player.connection == null || !NetworkRegistry.hasChannel(player.connection, PreparationPayload.TYPE.id())) continue;
            var preparation = runtime == null ? Optional.<SpellRuntime.Preparation>empty() : runtime.preparation(player.getUUID());
            if (preparation.isPresent()) current.add(player.getUUID());
            if (preparation.isPresent() || previous.contains(player.getUUID()))
                PacketDistributor.sendToPlayer(player, new PreparationPayload(preparation, preparation.flatMap(p -> heldSource(player, p))));
        }
        if (current.isEmpty()) SENT.remove(server); else SENT.put(server, current);
    }
    static void close(MinecraftServer server) { sync(server, null); SENT.remove(server); }
    /** Operator casts retain their bar without borrowing either held item's presentation. */
    static Optional<PreparationPayload.HeldSource> heldSource(Player player, SpellRuntime.Preparation preparation) {
        var hand = ScrollCasting.preparingHand(player, preparation.castId())
                .or(() -> WandCasting.preparingHand(player, preparation.castId()))
                .or(() -> StaffCasting.preparingHand(player, preparation.castId()));
        return hand.flatMap(h -> PreparationPayload.HeldSource.of(h, player.getItemInHand(h)));
    }
}
