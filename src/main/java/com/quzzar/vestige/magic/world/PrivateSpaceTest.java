package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PrivateSpaceTest {
    private PrivateSpaceTest() { }
    @GameTest(template="empty_3x3x3", batch="private_spaces", timeoutTicks=100)
    public static void roomsAreOwnedStableAndReturnToEntry(GameTestHelper helper) {
        var server=helper.getLevel().getServer();
        if (server.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).get(VestigeMainMod.location("pocket"))==null) helper.fail("Private dimension type data did not load");
        // Vanilla GameTestServer bakes only the flat preset's three dimensions.
        // Test the same placement/persistence/return code against its loaded End world.
        var first=helper.makeMockServerPlayerInLevel(); var second=helper.makeMockServerPlayerInLevel();
        Vec3 entry=helper.absoluteVec(new Vec3(1.5,1,1.5)); first.setPos(entry);second.setPos(entry);
        if (!PrivateSpaces.visit(first,Level.END) || !PrivateSpaces.visit(second,Level.END)) helper.fail("Private room placement failed");
        Vec3 ownRoom=first.position();
        if (first.position().distanceTo(second.position())<90) helper.fail("Different owners shared a room");
        first.teleportTo(ownRoom.x+100,ownRoom.y,ownRoom.z); PrivateSpaces.enforce(first,Level.END);
        if (first.position().distanceTo(ownRoom)>.01) helper.fail("Room boundary did not enforce ownership");
        if (!PrivateSpaces.visit(first,Level.END) || first.level()!=helper.getLevel() || first.position().distanceTo(entry)>.01) helper.fail("Return point was lost");
        if (!PrivateSpaces.visit(first,Level.END) || first.position().distanceTo(ownRoom)>.01) helper.fail("Revisit allocated a different room");
        PrivateSpaces.visit(first,Level.END);PrivateSpaces.visit(second,Level.END);
        server.getPlayerList().remove(first);server.getPlayerList().remove(second);first.discard();second.discard();
        helper.succeed();
    }
}
