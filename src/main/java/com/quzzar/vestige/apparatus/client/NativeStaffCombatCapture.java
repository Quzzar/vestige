package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.MundaneStaffs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.block.Blocks;
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

/** Opt-in first-person capture of the physical Staff's ordinary hit and raised partial guard. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeStaffCombatCapture {
    private static final boolean ENABLED = "staff_combat".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>();
    private static int state;
    private static long started, next;
    private static CompletableFuture<Void> pending;
    private static UUID target;

    private NativeStaffCombatCapture() { }

    private static void require(boolean condition, String detail) {
        if (!condition) throw new IllegalStateException(detail);
        CHECKS.add(Map.of("check", detail, "passed", true));
    }

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
        for (int x = -7; x <= 7; x++) for (int z = -3; z <= 9; z++) {
            level.setBlockAndUpdate(new BlockPos(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
        }
        var player = server.getPlayerList().getPlayers().getFirst();
        player.setGameMode(GameType.SURVIVAL);
        player.setHealth(player.getMaxHealth());
        player.setNoGravity(true);
        player.connection.teleport(0.5, 1, 0.5, 0, 0);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) player.getInventory().setItem(slot, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.STICK)));
        var zombie = EntityType.ZOMBIE.create(level);
        zombie.setNoAi(true);
        zombie.setPos(0.5, 1, 4.5);
        level.addFreshEntity(zombie);
        target = zombie.getUUID();
        player.inventoryMenu.broadcastChanges();
    }

    @SubscribeEvent
    public static void holdGuard(ClientTickEvent.Pre event) {
        if (!ENABLED || state < 5 || state > 7) return;
        Minecraft.getInstance().options.keyUse.setDown(true);
    }

    @SubscribeEvent
    public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 99) return;
        var minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        try {
            if (state > 1 && started == 0) started = now;
            if (started != 0 && now - started > 120_000_000_000L) throw new IllegalStateException("Staff combat capture timed out at step " + state);
            minecraft.getToasts().clear();
            minecraft.gui.getChat().clearMessages(false);
            if (state == 0 && minecraft.screen instanceof TitleScreen && minecraft.getOverlay() == null) {
                minecraft.options.pauseOnLostFocus = false;
                minecraft.options.hideGui = false;
                minecraft.options.fov().set(70);
                minecraft.options.renderDistance().set(4);
                minecraft.options.simulationDistance().set(5);
                minecraft.options.enableVsync().set(false);
                minecraft.getWindow().setFramerateLimit(60);
                minecraft.getWindow().setWindowed(960, 540);
                minecraft.options.guiScale().set(3);
                minecraft.resizeDisplay();
                minecraft.getTutorial().setStep(TutorialSteps.NONE);
                state = 1;
                minecraft.createWorldOpenFlows().createFreshLevel("vestige-staff-combat-" + UUID.randomUUID(),
                        new LevelSettings("Staff combat review", GameType.SURVIVAL, false, Difficulty.EASY, true,
                                new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(529413, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                        minecraft.screen);
            } else if (state == 1 && minecraft.player != null && minecraft.getSingleplayerServer() != null && minecraft.screen == null) {
                var server = minecraft.getSingleplayerServer();
                if (server.getPlayerList().getPlayers().isEmpty()) return;
                pending = server.submit(() -> prepare(server));
                state = 2;
                next = now + 1_500_000_000L;
            } else if (state == 2 && pending.isDone() && minecraft.level != null && now >= next) {
                pending.join();
                minecraft.player.setYRot(0); minecraft.player.setYHeadRot(0); minecraft.player.setXRot(0);
                minecraft.player.yRotO = 0; minecraft.player.xRotO = 0;
                capture(minecraft, "melee-ready");
                state = 3;
                next = now + 600_000_000L;
            } else if (state == 3 && now >= next) {
                var zombie = minecraft.level.getEntitiesOfClass(Zombie.class, new AABB(0, 0, 3, 1, 4, 6),
                        entity -> entity.getUUID().equals(target)).stream().findFirst().orElse(null);
                if (zombie == null) {
                    next = now + 100_000_000L;
                    return;
                }
                require(true, "Client received the melee target");
                minecraft.gameMode.attack(minecraft.player, zombie);
                state = 4;
                next = now + 200_000_000L;
            } else if (state == 4 && now >= next) {
                capture(minecraft, "melee-hit");
                pending = minecraft.getSingleplayerServer().submit(() -> {
                    var player = minecraft.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
                    var zombie = (Zombie) minecraft.getSingleplayerServer().overworld().getEntity(target);
                    require(zombie.getHealth() < zombie.getMaxHealth(), "Real left-click staff hit damages a target");
                    require(player.getMainHandItem().getDamageValue() == 1, "Successful staff hit spends one durability");
                });
                state = 5;
                next = now + 800_000_000L;
            } else if (state == 5 && pending.isDone() && now >= next) {
                pending.join();
                minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
                state = 6;
                next = now + 900_000_000L;
            } else if (state == 6 && now >= next) {
                capture(minecraft, "guard-raised");
                pending = minecraft.getSingleplayerServer().submit(() -> {
                    var server = minecraft.getSingleplayerServer();
                    var player = server.getPlayerList().getPlayers().getFirst();
                    var zombie = (Zombie) server.overworld().getEntity(target);
                    float health = player.getHealth();
                    player.hurt(player.damageSources().mobAttack(zombie), 10.0F);
                    require(Math.abs(player.getHealth() - (health - 6.0F)) < 0.001F,
                            "Raised Staff reduces a frontal ten-damage hit to six");
                    require(player.getMainHandItem().getDamageValue() == 2, "Guarded hit spends one additional durability");
                });
                state = 7;
                next = now + 800_000_000L;
            } else if (state == 7 && pending.isDone() && now >= next) {
                pending.join();
                capture(minecraft, "guard-impact");
                Files.writeString(output().resolve("verification.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                        "engine", "Minecraft 1.21.1 / NeoForge 21.1.72",
                        "source", "Unedited Minecraft framebuffer and integrated-server interactions",
                        "checks", CHECKS)) + "\n");
                state = 99;
                minecraft.options.keyUse.setDown(false);
                minecraft.stop();
            }
        } catch (Exception error) {
            state = 99;
            minecraft.options.keyUse.setDown(false);
            LogUtils.getLogger().error("Native Staff combat capture failed", error);
            try {
                Files.createDirectories(output());
                Files.writeString(output().resolve("error.txt"), error.toString());
            } catch (Exception ignored) { }
            minecraft.stop();
        }
    }
}
