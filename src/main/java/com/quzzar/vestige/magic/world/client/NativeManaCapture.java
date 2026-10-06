package com.quzzar.vestige.magic.world.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in actual framebuffer inspection, isolated from normal worlds and player settings. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeManaCapture {
    private static final boolean ENABLED = "mana".equals(System.getProperty("vestige.capture.kind"));
    private static final boolean ITEM_REVIEW = Boolean.getBoolean("vestige.capture.review");
    private record View(String name, int mana, int seconds, int scale) { }
    private static final List<View> VIEWS = ITEM_REVIEW
            ? List.of(new View("flint-scale-four", 100, 3, 4), new View("flint-scale-two", 100, 3, 2))
            : List.of(new View("full-first-spawn", 100, 3, 2), new View("empty", 0, 3, 2),
            new View("half", 50, 3, 2), new View("recovering", 0, 11, 2), new View("full-scale-one", 100, 3, 1), new View("half-scale-one", 50, 3, 1));
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>();
    private static int state, index;
    private static long next;
    private static CompletableFuture<Void> pending;
    private NativeManaCapture() { }
    private static void prepare(MinecraftServer server) {
        var level = server.overworld(); var player = server.getPlayerList().getPlayers().getFirst();
        if (index == 0) {
            if (NativeMana.amount(player) != 100) throw new IllegalStateException("First spawn was not full");
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
            level.setWeatherParameters(6000, 0, false, false); level.setDayTime(5500);
            for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) {
                level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 65; y < 69; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            }
            var origin = new BlockPos(0, 65, 0); level.setBlock(origin, ApparatusBlocks.SPELLSTONE.get().defaultBlockState(), 3);
            for (var pos : List.of(origin.north(2), origin.east(2), origin.south(2), origin.west(2)))
                level.setBlock(pos, ApparatusBlocks.PLINTH.get().defaultBlockState(), 3);
            player.getInventory().setItem(0, HomeboundEyeItem.bound("a".repeat(64), Level.OVERWORLD, origin, HomeboundEyeItem.Payment.MANA));
            for (int i = 0; i < 4; i++) player.getInventory().setItem(i + 1, new ItemStack(List.of(Items.SOUL_SAND, Items.MOSS_BLOCK, Items.LAPIS_BLOCK, Items.AMETHYST_BLOCK).get(i)));
            if (ITEM_REVIEW) {
                var references = List.of(Items.FLINT, Items.SPIDER_EYE, Items.ENDER_PEARL, ScrollItems.ATTUNEMENT_SHARD.get());
                for (int i = 0; i < references.size(); i++) player.getInventory().setItem(i + 1, new ItemStack(references.get(i)));
            }
            player.setExperienceLevels(15); player.setExperiencePoints(18);
            player.setNoGravity(true); player.teleportTo(.5, 65, -5.5); player.setYRot(0); player.setXRot(8);
        } else {
            NativeMana.set(player, 100); NativeMana.set(player, VIEWS.get(index).mana);
        }
        NativeMana.sync(player, true);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 9) return;
        var mc = Minecraft.getInstance(); long now = System.nanoTime();
        try {
            if (state == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                mc.options.pauseOnLostFocus = false; mc.options.hideGui = false; mc.options.guiScale().set(2);
                mc.options.renderDistance().set(4); mc.options.enableVsync().set(false); mc.getWindow().setFramerateLimit(60);
                mc.options.setCameraType(CameraType.FIRST_PERSON); mc.getTutorial().setStep(TutorialSteps.NONE); state = 1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-mana-" + UUID.randomUUID(),
                        new LevelSettings("Mana and Homebound Eye inspection", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
            } else if (state == 1 && mc.level != null && mc.player != null && mc.getSingleplayerServer() != null && mc.screen == null) {
                state = 2; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer()));
            } else if (state == 2 && pending.isDone()) {
                pending.join(); mc.options.guiScale().set(VIEWS.get(index).scale); mc.resizeDisplay();
                mc.mouseHandler.releaseMouse(); state = 3; next = now + VIEWS.get(index).seconds * 1_000_000_000L;
            } else if (state == 3 && now >= next && mc.screen == null) {
                var view = VIEWS.get(index); float amount = ManaHud.amount(); boolean visible = ManaHud.visible();
                if (visible != (view.mana < 100) || view.name.equals("recovering") && amount <= 0
                        || !view.name.equals("recovering") && Math.abs(amount - view.mana) > .01) throw new IllegalStateException("Invalid mana HUD state: " + view + " / " + amount);
                Path out = Path.of(System.getProperty("vestige.capture.output")); Files.createDirectories(out);
                try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(out.resolve(view.name + ".png")); }
                CHECKS.add(Map.of("view", view.name, "mana", amount, "barVisible", visible, "guiScale", view.scale));
                if (++index == VIEWS.size()) {
                    Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                            "engine", "Minecraft 1.21.1 / NeoForge 21.1.72", "source", "Minecraft main render target", "checks", CHECKS)) + "\n");
                    state = 9; mc.stop();
                } else { state = 2; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer())); }
            }
        } catch (Exception failure) { state = 9; LogUtils.getLogger().error("Mana inspection failed", failure); mc.stop(); }
    }
}
