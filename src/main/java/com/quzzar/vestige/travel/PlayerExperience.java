package com.quzzar.vestige.travel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/** Point payments use current level/progress, not vanilla's historical totalExperience counter. */
public final class PlayerExperience {
    private PlayerExperience() { }
    public static long atLevel(int level) {
        double value = level <= 16 ? (double) level * level + 6d * level
                : level <= 31 ? 2.5 * level * level - 40.5 * level + 360
                : 4.5 * level * level - 162.5 * level + 2220;
        return (long) Math.clamp(value, 0, Integer.MAX_VALUE);
    }
    public static long available(Player player) {
        if (player.experienceLevel < 0 || !Float.isFinite(player.experienceProgress)
                || player.experienceProgress < 0 || player.experienceProgress >= 1) return 0;
        return Math.min(Integer.MAX_VALUE, atLevel(player.experienceLevel)
                + Math.round((double) player.experienceProgress * player.getXpNeededForNextLevel()));
    }
    public record Snapshot(int level, float progress, int total, int score) {
        public static Snapshot of(Player player) { return new Snapshot(player.experienceLevel, player.experienceProgress, player.totalExperience, player.getScore()); }
        public void restore(ServerPlayer player) {
            player.setExperienceLevels(level); player.experienceProgress = progress;
            player.totalExperience = total; player.setScore(score);
        }
    }
    public static boolean spend(ServerPlayer player, int points) {
        long before = available(player);
        if (points <= 0 || before < points) return false;
        var snapshot = Snapshot.of(player);
        long remaining = before - points;
        int low = 0, high = 23861;
        while (low < high) {
            int middle = (low + high + 1) / 2;
            if (atLevel(middle) <= remaining) low = middle; else high = middle - 1;
        }
        int level = low;
        var change = new PlayerXpEvent.XpChange(player, -points);
        if (NeoForge.EVENT_BUS.post(change).isCanceled() || change.getAmount() != -points) return false;
        int delta = level - snapshot.level();
        if (delta != 0) {
            var levels = new PlayerXpEvent.LevelChange(player, delta);
            if (NeoForge.EVENT_BUS.post(levels).isCanceled() || levels.getLevels() != delta) return false;
        }
        // An event listener may change the balance; never apply a stale debit.
        if (!snapshot.equals(Snapshot.of(player))) return false;
        player.setExperienceLevels(level);
        player.setExperiencePoints((int) (remaining - atLevel(level)));
        player.totalExperience = Math.max(0, snapshot.total() - points);
        player.increaseScore(-points);
        return true;
    }
}
