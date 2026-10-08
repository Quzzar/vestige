package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.MundaneStaffs;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Opt-in real-client views of every mundane Staff beside comparable vanilla equipment and held in third person. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeMundaneStaffCapture {
    private static final boolean ENABLED = "mundane_staffs".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>();
    private static int state;
    private static long started, next;
    private static CompletableFuture<Void> pending;

    private NativeMundaneStaffCapture() { }

    private static Path output() { return Path.of(System.getProperty("vestige.capture.output")); }

    private static void capture(Minecraft minecraft, String name) throws Exception {
        Files.createDirectories(output());
        try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
            image.writeToFile(output().resolve(name + ".png"));
            CHECKS.add(Map.of("capture", name, "pixels", List.of(image.getWidth(), image.getHeight())));
        }
    }

    private static void prepare(net.minecraft.server.MinecraftServer server) {
        var level = server.overworld();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        level.setDayTime(6000);
        level.setWeatherParameters(6000, 0, false, false);
        for (int x = -7; x <= 7; x++) for (int z = -7; z <= 7; z++) {
            level.setBlockAndUpdate(new BlockPos(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
        }
        ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
        player.setGameMode(GameType.SURVIVAL);
        player.setNoGravity(true);
        player.connection.teleport(.5, 1, .5, 0, 0);
        player.setYRot(0);
        player.setYHeadRot(0);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) player.getInventory().setItem(slot, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.STICK)));
        // Leave the centre of the inventory clear: the client opens the screen with its pointer there,
        // and a tooltip would otherwise cover the final two Staff sprites in the review capture.
        int[] staffSlots = {9, 10, 11, 16, 17, 18, 19};
        int index = 0;
        for (var shaft : MundaneStaffs.Shaft.values()) {
            player.getInventory().setItem(staffSlots[index++], new ItemStack(MundaneStaffs.item(shaft)));
        }
        int slot = 27;
        for (var neighbor : List.of(Items.STICK, Items.WOODEN_PICKAXE, Items.WOODEN_SWORD, Items.SHIELD)) {
            player.getInventory().setItem(slot++, new ItemStack(neighbor));
        }
        player.inventoryMenu.broadcastChanges();
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 99) return;
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        try {
            // Resource loading can take several minutes on a cold client. Start the short capture timeout
            // only after the fresh integrated world has been requested.
            if (state > 1 && started == 0) started = now;
            // Cold resource reloads can take several minutes on the development client.  This is
            // capture-only tooling, so allow the world to finish loading before judging a pose.
            if (started != 0 && now - started > 420_000_000_000L) throw new IllegalStateException("Mundane Staff capture timed out at step " + state);
            minecraft.getToasts().clear();
            minecraft.gui.getChat().clearMessages(false);
            if (state == 0 && minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
                minecraft.options.pauseOnLostFocus = false;
                minecraft.options.hideGui = false;
                minecraft.options.renderDistance().set(4);
                minecraft.options.simulationDistance().set(5);
                minecraft.options.enableVsync().set(false);
                minecraft.getWindow().setFramerateLimit(60);
                minecraft.getWindow().setWindowed(960, 720);
                minecraft.options.guiScale().set(3);
                minecraft.resizeDisplay();
                minecraft.getTutorial().setStep(TutorialSteps.NONE);
                state = 1;
                minecraft.createWorldOpenFlows().createFreshLevel("vestige-mundane-staffs-" + UUID.randomUUID(),
                        new LevelSettings("Mundane Staff review", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true,
                                new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(491237, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                        minecraft.screen);
            } else if (state == 1 && minecraft.player != null && minecraft.getSingleplayerServer() != null && minecraft.screen == null
                    && !minecraft.getSingleplayerServer().getPlayerList().getPlayers().isEmpty()) {
                pending = minecraft.getSingleplayerServer().submit(() -> prepare(minecraft.getSingleplayerServer()));
                state = 2;
                next = now + 1_500_000_000L;
            } else if (state == 2 && pending.isDone() && now >= next) {
                pending.join();
                minecraft.setScreen(new InventoryScreen(minecraft.player));
                state = 3;
                next = now + 900_000_000L;
            } else if (state == 3 && minecraft.screen instanceof InventoryScreen && now >= next) {
                capture(minecraft, "inventory-beside-vanilla");
                minecraft.screen.onClose();
                minecraft.options.hideGui = true;
                minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                state = 4;
                next = now + 700_000_000L;
            } else if (state == 4 && minecraft.screen == null && now >= next) {
                // Keep the key held as well as invoking the item use.  Calling useItem alone is a
                // one-tick interaction in this automated client, so it otherwise photographs the
                // idle stance instead of the sustained guard pose players see while right-clicking.
                minecraft.options.keyUse.setDown(true);
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
                state = 5;
                next = now + 1_000_000_000L;
            } else if (state == 5 && now >= next) {
                capture(minecraft, "wooden-staff-raised-third-person");
                minecraft.player.setYRot(45);
                minecraft.player.setYHeadRot(45);
                state = 6;
                next = now + 700_000_000L;
            } else if (state == 6 && now >= next) {
                capture(minecraft, "wooden-staff-raised-three-quarter");
                Files.writeString(output().resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                        "engine", "Minecraft 1.21.1 / NeoForge 21.1.72",
                        "source", "Minecraft main render target with a real InventoryScreen and raised in-world Staff",
                        "checks", CHECKS)) + "\n");
                state = 99;
                minecraft.options.keyUse.setDown(false);
                minecraft.stop();
            }
        } catch (Exception error) {
            state = 99;
            minecraft.options.keyUse.setDown(false);
            LogUtils.getLogger().error("Mundane Staff capture failed", error);
            try {
                Files.createDirectories(output());
                Files.writeString(output().resolve("error.txt"), error.toString());
            } catch (Exception ignored) { }
            minecraft.stop();
        }
    }
}
