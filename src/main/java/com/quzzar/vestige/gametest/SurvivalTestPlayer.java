package com.quzzar.vestige.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import java.util.UUID;

/** Vanilla's deprecated GameTest server player overrides isCreative; payment tests need Survival. */
public final class SurvivalTestPlayer extends ServerPlayer implements AutoCloseable {
    private SurvivalTestPlayer(GameTestHelper helper, CommonListenerCookie cookie) {
        super(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
    }
    public static SurvivalTestPlayer create(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "survival-test-player"), false);
        var player = new SurvivalTestPlayer(helper, cookie); var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection); helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL); player.setPos(helper.absolutePos(new BlockPos(1, 1, 1)).getBottomCenter());
        return player;
    }
    @Override public void close() { getServer().getPlayerList().remove(this); discard(); }
}
