package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.MundaneStaffs;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Captures a real third-person Staff guard orbit, left-click hit, and returned guard as video source frames. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeMundaneStaffMotionCapture {
    private static final boolean ENABLED = "mundane_staff_motion".equals(System.getProperty("vestige.capture.kind"));
    private static final int[] GUARD_ANGLES = {0, 23, 45, 68, 90, 113, 135, 158, 180, 203, 225, 248, 270, 293, 315, 338};
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>();
    private static int state;
    private static int frame, guardAngle, attackFrame, finalGuardFrame;
    private static long started, next;
    private static CompletableFuture<Void> pending;
    private static UUID target;
    private static UUID camera;
    private static double cameraX, cameraY, cameraZ;
    private static float cameraYaw, cameraPitch;

    private NativeMundaneStaffMotionCapture() { }

    private static Path output() { return Path.of(System.getProperty("vestige.capture.output")); }

    private static void capture(Minecraft minecraft, String phase) throws Exception {
        Files.createDirectories(output());
        String name = String.format("frame-%03d", frame++);
        try (var image = Screenshot.takeScreenshot(minecraft.getMainRenderTarget())) {
            image.writeToFile(output().resolve(name + ".png"));
            CHECKS.add(Map.of("frame", name, "phase", phase, "pixels", List.of(image.getWidth(), image.getHeight())));
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
        player.setHealth(player.getMaxHealth());
        player.setNoGravity(true);
        player.connection.teleport(.5, 1, .5, 0, 0);
        player.setYRot(0);
        player.setYHeadRot(0);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) player.getInventory().setItem(slot, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.STICK)));
        var zombie = EntityType.ZOMBIE.create(level);
        zombie.setNoAi(true);
        zombie.setPos(.5, 1, 4.5);
        level.addFreshEntity(zombie);
        target = zombie.getUUID();
        var cameraStand = EntityType.ARMOR_STAND.create(level);
        cameraStand.setInvisible(true);
        cameraStand.setNoGravity(true);
        cameraStand.setInvulnerable(true);
        cameraStand.setPos(.5, 2.7, 6.5);
        level.addFreshEntity(cameraStand);
        camera = cameraStand.getUUID();
        player.inventoryMenu.broadcastChanges();
    }

    private static void aim(Minecraft minecraft, int angle) {
        minecraft.player.setYRot(angle);
        minecraft.player.setYHeadRot(angle);
        minecraft.player.yRotO = angle;
    }

    private static void orbitCamera(Minecraft minecraft, int angle) {
        double radians = Math.toRadians(angle);
        cameraX = .5 + Math.sin(radians) * 5.5;
        cameraY = 2.8;
        cameraZ = .5 + Math.cos(radians) * 5.5;
        double dx = .5 - cameraX;
        double dy = 2.2 - cameraY;
        double dz = .5 - cameraZ;
        cameraYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        cameraPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        applyOrbitCamera(minecraft);
    }

    private static void applyOrbitCamera(Minecraft minecraft) {
        if (minecraft.level == null || camera == null) return;
        var cameraStand = minecraft.level.getEntitiesOfClass(ArmorStand.class, new AABB(-8, 0, -8, 8, 8, 8),
                entity -> entity.getUUID().equals(camera)).stream().findFirst()
                .orElse(null);
        if (cameraStand == null) return;
        cameraStand.setPos(cameraX, cameraY, cameraZ);
        cameraStand.setYRot(cameraYaw);
        cameraStand.yRotO = cameraYaw;
        cameraStand.setYHeadRot(cameraYaw);
        cameraStand.yHeadRotO = cameraYaw;
        cameraStand.yBodyRot = cameraYaw;
        cameraStand.yBodyRotO = cameraYaw;
        cameraStand.setXRot(cameraPitch);
        cameraStand.xRotO = cameraPitch;
        minecraft.setCameraEntity(cameraStand);
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
    }

    @SubscribeEvent
    public static void holdGuard(ClientTickEvent.Pre event) {
        if (!ENABLED) return;
        Minecraft minecraft = Minecraft.getInstance();
        if ((state >= 4 && state <= 6) || (state >= 11 && state <= 12)) minecraft.options.keyUse.setDown(true);
        if (state >= 3 && state <= 12) applyOrbitCamera(minecraft);
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 99) return;
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        try {
            if (state > 1 && started == 0) started = now;
            if (started != 0 && now - started > 180_000_000_000L) {
                throw new IllegalStateException("Mundane Staff motion capture timed out at step " + state);
            }
            minecraft.getToasts().clear();
            minecraft.gui.getChat().clearMessages(false);
            if (state == 0 && minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
                minecraft.options.pauseOnLostFocus = false;
                minecraft.options.hideGui = true;
                minecraft.options.fov().set(70);
                minecraft.options.renderDistance().set(4);
                minecraft.options.simulationDistance().set(5);
                minecraft.options.enableVsync().set(false);
                minecraft.getWindow().setFramerateLimit(60);
                minecraft.getWindow().setWindowed(960, 540);
                minecraft.resizeDisplay();
                minecraft.getTutorial().setStep(TutorialSteps.NONE);
                state = 1;
                minecraft.createWorldOpenFlows().createFreshLevel("vestige-mundane-staff-motion-" + UUID.randomUUID(),
                        new LevelSettings("Mundane Staff motion review", GameType.SURVIVAL, false, Difficulty.EASY, true,
                                new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(702891, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                        minecraft.screen);
            } else if (state == 1 && minecraft.player != null && minecraft.getSingleplayerServer() != null && minecraft.screen == null
                    && !minecraft.getSingleplayerServer().getPlayerList().getPlayers().isEmpty()) {
                var server = minecraft.getSingleplayerServer();
                pending = server.submit(() -> prepare(server));
                state = 2;
                next = now + 1_500_000_000L;
            } else if (state == 2 && pending.isDone() && minecraft.level != null && now >= next) {
                pending.join();
                aim(minecraft, GUARD_ANGLES[0]);
                orbitCamera(minecraft, GUARD_ANGLES[0]);
                minecraft.options.keyUse.setDown(true);
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
                state = 3;
                next = now + 1_000_000_000L;
            } else if (state == 3 && now >= next) {
                state = 4;
            } else if (state == 4) {
                if (guardAngle >= GUARD_ANGLES.length) {
                    minecraft.options.keyUse.setDown(false);
                    state = 7;
                    next = now + 500_000_000L;
                } else {
                    aim(minecraft, GUARD_ANGLES[guardAngle]);
                    orbitCamera(minecraft, GUARD_ANGLES[guardAngle]);
                    state = 5;
                    next = now + 160_000_000L;
                }
            } else if (state == 5 && now >= next) {
                capture(minecraft, "guard-orbit-" + GUARD_ANGLES[guardAngle] + "deg");
                guardAngle++;
                state = 4;
            } else if (state == 7 && now >= next) {
                aim(minecraft, 0);
                orbitCamera(minecraft, 45);
                var zombie = minecraft.level.getEntitiesOfClass(Zombie.class, new AABB(0, 0, 3, 1, 4, 6), entity -> entity.getUUID().equals(target))
                        .stream().findFirst().orElseThrow(() -> new IllegalStateException("Motion target disappeared"));
                minecraft.gameMode.attack(minecraft.player, zombie);
                state = 8;
                next = now + 65_000_000L;
            } else if (state == 8 && now >= next) {
                capture(minecraft, "left-click-attack-" + attackFrame);
                attackFrame++;
                if (attackFrame >= 8) {
                    pending = minecraft.getSingleplayerServer().submit(() -> {
                        var serverZombie = (Zombie) minecraft.getSingleplayerServer().overworld().getEntity(target);
                        if (serverZombie == null || serverZombie.getHealth() >= serverZombie.getMaxHealth()) {
                            throw new IllegalStateException("Real left-click Staff hit did not reach the target");
                        }
                    });
                    state = 9;
                    next = now + 100_000_000L;
                } else {
                    next = now + 65_000_000L;
                }
            } else if (state == 9 && pending.isDone() && now >= next) {
                pending.join();
                aim(minecraft, 0);
                orbitCamera(minecraft, 45);
                minecraft.options.keyUse.setDown(true);
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
                state = 11;
                next = now + 400_000_000L;
            } else if (state == 11 && now >= next) {
                capture(minecraft, "guard-return-" + finalGuardFrame);
                finalGuardFrame++;
                if (finalGuardFrame >= 4) {
                    Files.writeString(output().resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                            "engine", "Minecraft 1.21.1 / NeoForge 21.1.72",
                            "source", "Unedited Minecraft framebuffer; the swing is a real left-click hit on an integrated-server zombie",
                            "sequence", List.of("16-angle held guard orbit", "8 frames of a real left-click hit", "4 returned guard frames"),
                            "checks", CHECKS)) + "\n");
                    state = 99;
                    minecraft.options.keyUse.setDown(false);
                    minecraft.stop();
                } else {
                    next = now + 65_000_000L;
                }
            }
        } catch (Exception error) {
            state = 99;
            minecraft.options.keyUse.setDown(false);
            LogUtils.getLogger().error("Mundane Staff motion capture failed", error);
            try {
                Files.createDirectories(output());
                Files.writeString(output().resolve("error.txt"), error.toString());
            } catch (Exception ignored) { }
            minecraft.stop();
        }
    }
}
