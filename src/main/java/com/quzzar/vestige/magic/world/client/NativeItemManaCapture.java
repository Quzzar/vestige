package com.quzzar.vestige.magic.world.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in native framebuffer evidence, using actual private prices, payments and mana regeneration. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeItemManaCapture {
    private static final boolean ENABLED = "item_mana".equals(System.getProperty("vestige.capture.kind"));
    private record View(String name, int mana, int width, int height, int scale, int seconds) { }
    private static final List<View> VIEWS = List.of(new View("ready", 100, 960, 540, 4, 2),
            new View("partial", 14, 960, 540, 4, 2), new View("empty", 0, 960, 540, 4, 2),
            new View("inventory", 14, 960, 540, 4, 2), new View("staff-selection", 14, 960, 540, 4, 2),
            new View("repeated-casts", 100, 960, 540, 4, 3), new View("recovery", 0, 960, 540, 4, 18),
            new View("compact", 14, 640, 360, 2, 2));
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>(), FRAMES = new ArrayList<>();
    private static int state, index, frame; private static long next, started, warm, boot;
    private static CompletableFuture<Void> pending; private static boolean exhausted;
    private NativeItemManaCapture() { }
    private static void prepare(MinecraftServer server) {
        var level = server.overworld(); var player = server.getPlayerList().getPlayers().getFirst();
        if (index == 0) {
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
            level.setWeatherParameters(6000, 0, false, false); level.setDayTime(5500);
            for (int x = -10; x <= 10; x++) for (int z = -10; z <= 10; z++) {
                level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                for (int y = 65; y < 70; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            }
            player.setNoGravity(true); player.connection.teleport(.5, 65, -5.5, 0, 8);
        }
        var fire = ScrollItems.scroll(VestigeMainMod.location("fireball"));
        var arrow = ScrollItems.scroll(VestigeMainMod.location("force_arrow"));
        var shield = ScrollItems.scroll(VestigeMainMod.location("pf2_shield"));
        var staff = StaffData.bind(StaffData.create(VestigeMainMod.location("evocation")), fire, NativeMagic.spells().spells().get(VestigeMainMod.location("fireball")));
        staff = StaffData.bind(StaffData.select(staff, 1), arrow, NativeMagic.spells().spells().get(VestigeMainMod.location("force_arrow")));
        staff = StaffData.select(staff, VIEWS.get(index).name.equals("staff-selection") ? 1 : 0);
        var items = List.of(shield.copyWithCount(16), arrow.copyWithCount(8), fire.copyWithCount(8),
                ScrollItems.scroll(VestigeMainMod.location("greater_heal")).copyWithCount(8),
                ScrollItems.shapedScroll(VestigeMainMod.location("fireball"), new LeylineShaping.Modifiers(1, 1, 1, 2)).copyWithCount(8),
                WandData.create(WandComponents.Base.STICK, MagicalThreadRecipe.Type.ENSORCELLED, fire), staff,
                HomeboundEyeItem.bound("a".repeat(64), Level.OVERWORLD, BlockPos.ZERO, HomeboundEyeItem.Payment.MANA), shield.copyWithCount(16));
        player.getInventory().clearContent(); player.getInventory().selected = 0;
        for (int i = 0; i < items.size(); i++) player.getInventory().setItem(i, items.get(i));
        for (var name : List.of("pf2_shield", "force_arrow", "fireball", "greater_heal")) SpellKnowledge.identify(player, VestigeMainMod.location(name));
        NativeMana.set(player, 100); NativeMana.set(player, VIEWS.get(index).mana);
        if (VIEWS.get(index).name.equals("repeated-casts")) {
            for (int i = 0; i < 10; i++) if (!ScrollCasting.cast(player, player.getMainHandItem())) throw new IllegalStateException("Repeated shield cast rejected");
            if (NativeMana.amount(player) != 40 || player.getMainHandItem().getCount() != 6) throw new IllegalStateException("Ten casts did not pay exactly once each");
            CHECKS.add(Map.of("view", "ten-real-casts", "casts", 10, "mana", NativeMana.amount(player), "remainingScrolls", 6));
        }
    }
    private static void exhaust(MinecraftServer server) {
        var player = server.getPlayerList().getPlayers().getFirst();
        for (int i = 0; i < 6; i++) if (!ScrollCasting.cast(player, player.getMainHandItem())) throw new IllegalStateException("Affordable shield cast rejected");
        if (NativeMana.amount(player) != 4 || !player.getMainHandItem().isEmpty()) throw new IllegalStateException("Final affordable casts did not commit");
        CHECKS.add(Map.of("view", "sixteen-real-casts", "casts", 16, "mana", NativeMana.amount(player), "spareScrolls", player.getInventory().getItem(8).getCount()));
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 9) return;
        var mc = Minecraft.getInstance(); long now = System.nanoTime(); if (boot == 0) boot = now;
        try {
            if (now - boot > 240_000_000_000L) throw new IllegalStateException("Item mana capture timed out: " + state);
            if (state == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                mc.options.pauseOnLostFocus = false; mc.options.hideGui = false; mc.options.renderDistance().set(4); mc.options.simulationDistance().set(5);
                mc.options.enableVsync().set(false); mc.getWindow().setFramerateLimit(60); mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.getTutorial().setStep(TutorialSteps.NONE); state = 1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-item-mana-" + UUID.randomUUID(),
                        new LevelSettings("Item mana inspection", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
            } else if (state == 1 && mc.level != null && mc.player != null && mc.getSingleplayerServer() != null && mc.screen == null) {
                state = 2; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer()));
            } else if (state == 2 && pending.isDone()) {
                pending.join(); var view = VIEWS.get(index);
                org.lwjgl.glfw.GLFW.glfwSetWindowSize(mc.getWindow().getWindow(), view.width, view.height);
                mc.options.guiScale().set(view.scale); mc.resizeDisplay(); mc.mouseHandler.releaseMouse();
                if (view.name.equals("inventory")) mc.setScreen(new InventoryScreen(mc.player));
                warm = now + 1_000_000_000L; state = 3;
            } else if (state == 3 && now >= warm) {
                started = now; next = now; frame = 0; exhausted = false; state = 4;
            } else if (state == 4 && now >= next) {
                var view = VIEWS.get(index); double seconds = (now - started) / 1e9;
                if (view.name.equals("repeated-casts") && seconds > 1 && !exhausted) {
                    exhausted = true; pending = mc.getSingleplayerServer().submit(() -> exhaust(mc.getSingleplayerServer()));
                }
                double mana = ItemManaOverlay.amount(); var shortages = new ArrayList<Float>();
                for (int i = 0; i < 9; i++) shortages.add(ItemManaOverlay.shortage(mc.player.getInventory().getItem(i)));
                if (view.name.equals("partial") && (shortages.get(1) != 0 || shortages.get(2) != .5f || shortages.get(4) != .75f))
                    throw new IllegalStateException("Different exact mana prices did not reach the native decorations: " + shortages);
                Path out = Path.of(System.getProperty("vestige.capture.output")); Files.createDirectories(out.resolve(view.name));
                String file = view.name + "/frame-" + String.format(Locale.ROOT, "%03d", frame++) + ".png";
                try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(out.resolve(file)); }
                FRAMES.add(Map.of("file", file, "seconds", seconds, "mana", mana, "shortages", shortages,
                        "width", mc.getWindow().getWidth(), "height", mc.getWindow().getHeight(), "guiScale", view.scale));
                next = now + 200_000_000L;
                if (seconds >= view.seconds) {
                    pending.join(); mc.setScreen(null);
                    if (++index == VIEWS.size()) {
                        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                                "engine", "Minecraft 1.21.1 / NeoForge 21.1.72", "source", "Native item decorations / private server packets",
                                "checks", CHECKS, "frames", FRAMES)) + "\n"); state = 9; mc.stop();
                    } else { state = 2; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer())); }
                }
            }
        } catch (Exception failure) { state = 9; LogUtils.getLogger().error("Item mana inspection failed", failure); mc.stop(); }
    }
}
