package com.quzzar.vestige.travel;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.HashSet;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NearbyTeleportTest {
    private static BlockPos room(GameTestHelper h, boolean blocked) {
        var origin = h.absolutePos(new BlockPos(4, 1, 4));
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            h.getLevel().setBlock(origin.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
            for (int y = 0; y < 3; y++) h.getLevel().setBlock(origin.offset(x, y, z),
                    (blocked ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 3);
        }
        return origin;
    }
    @GameTest(template = "empty_9x3x9", batch = "nearby_travel")
    public static void arrivalsRandomlyUseOpenPositionsWithinTwoHorizontalBlocks(GameTestHelper h) {
        var origin = room(h, false); var player = h.makeMockPlayer(GameType.SURVIVAL);
        player.getRandom().setSeed(8493); var positions = new HashSet<BlockPos>();
        for (int i = 0; i < 120; i++) {
            var arrival = NearbyTeleport.arrival(player, h.getLevel(), origin).orElseThrow();
            var feet = BlockPos.containing(arrival); positions.add(feet);
            h.assertTrue(Math.abs(feet.getX() - origin.getX()) <= 2 && Math.abs(feet.getZ() - origin.getZ()) <= 2
                    && !feet.equals(origin) && feet.getY() == origin.getY(), "Arrival escaped the nearby floor");
            h.assertTrue(h.getLevel().noCollision(player, player.getBoundingBox().move(arrival.subtract(player.position()))), "Open arrival overlaps terrain");
        }
        h.assertTrue(positions.size() > 12, "Arrival always chose the same side"); h.succeed();
    }
    @GameTest(template = "empty_9x3x9", batch = "nearby_travel")
    public static void theOnlyOpeningAlwaysWinsBeforeTheOccupiedFallback(GameTestHelper h) {
        var origin = room(h, true); var hole = origin.offset(2, 0, -1); var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (int y = 0; y < 3; y++) h.getLevel().setBlock(hole.above(y), Blocks.AIR.defaultBlockState(), 3);
        for (int i = 0; i < 10; i++) h.assertTrue(BlockPos.containing(NearbyTeleport.arrival(player, h.getLevel(), origin).orElseThrow()).equals(hole), "Blocked candidate masked the opening");
        for (int y = 0; y < 3; y++) h.getLevel().setBlock(hole.above(y), Blocks.STONE.defaultBlockState(), 3);
        var arrival = NearbyTeleport.arrival(player, h.getLevel(), origin).orElseThrow();
        h.assertTrue(!h.getLevel().noCollision(player, player.getBoundingBox().move(arrival.subtract(player.position())))
                && h.getLevel().getBlockState(BlockPos.containing(arrival)).is(Blocks.STONE), "Fallback cleared terrain or rejected a crowded site"); h.succeed();
    }
}
