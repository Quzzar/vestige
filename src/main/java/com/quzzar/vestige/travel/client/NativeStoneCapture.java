package com.quzzar.vestige.travel.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.travel.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in native menu inspection using actual endpoints, server payloads and rename requests. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeStoneCapture {
    private static final boolean ENABLED = "stone_network".equals(System.getProperty("vestige.capture.kind"));
    private static final String KEY = "76f32f1c38d3f867b7314795817de918434f62f7742e565283a18dff843ed500";
    private static final BlockPos SOURCE = new BlockPos(0, 65, 0), SINGLE = new BlockPos(-4, 65, 0);
    private static final List<String> NAMES = List.of("Ancient Henge", "Ashen Gate", "A Very Long Standing Stone Destination Name",
            "Desert Pyramid", "Forest Shrine", "Harbor", "High Pass", "Island Retreat", "Library", "Marsh Ruins",
            "Mountain Observatory", "Old Ruins", "Quarry", "River Crossing", "Southwatch", "Sunken Temple",
            "Valley Camp", "Westgate", "Windmill", "Winter Hold");
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>();
    private static StoneNetworkScreen previous;
    private static UUID sourceId;
    private static int state;
    private static long next, started;
    private static CompletableFuture<Void> pending;
    private NativeStoneCapture() { }
    private static StandingStoneEntity place(MinecraftServer server, BlockPos pos, String key, String name) {
        var level = server.overworld(); var lower = StandingStones.STONE.get().defaultBlockState();
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pos, lower, 3); level.setBlock(pos.above(), lower.setValue(StandingStoneBlock.HALF, DoubleBlockHalf.UPPER), 3);
        var stone = (StandingStoneEntity) level.getBlockEntity(pos); stone.configure(key, name); return stone;
    }
    private static void prepare(MinecraftServer server) {
        var level = server.overworld();
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        level.setWeatherParameters(6000, 0, false, false); level.setDayTime(5500);
        for (int x = -7; x <= 20; x++) for (int z = -7; z <= 15; z++) {
            level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            for (int y = 65; y <= 68; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
        var source = place(server, SOURCE, KEY, "Wizard Tower"); sourceId = source.id();
        for (int i = 0; i < NAMES.size(); i++) place(server, new BlockPos(5 + (i % 5) * 3, 65, (i / 5) * 3), KEY, NAMES.get(i));
        place(server, SINGLE, "d9943647cbf7789d06ff728ebe1ecd8973b9fef68ddb481a7f68a213c91dcfc4", "Lone Standing Stone");
        var player = server.getPlayerList().getPlayers().getFirst();
        player.setNoGravity(true); player.teleportTo(.5, 65, -3.5); player.setYRot(0); player.setXRot(0);
        StoneTravel.open(player, source, 0);
    }
    private static EditBox editor(StoneNetworkScreen screen) {
        return screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow();
    }
    private static Button button(StoneNetworkScreen screen, String label) {
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(value -> value.getMessage().getString().equals(label)).findFirst().orElseThrow();
    }
    private static void click(StoneNetworkScreen screen, Button button) {
        if (!button.active) throw new IllegalStateException("Disabled button: " + button.getMessage().getString());
        screen.mouseClicked(button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0, 0);
        screen.mouseReleased(button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0, 0);
    }
    private static void movePointerAway(Minecraft mc) {
        GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), 0, 0);
    }
    private static void capture(Minecraft mc, StoneNetworkScreen screen, String name) throws Exception {
        Path out = Path.of(System.getProperty("vestige.capture.output")); Files.createDirectories(out);
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(out.resolve(name + ".png"));
            CHECKS.add(Map.of("capture", name, "pixels", List.of(image.getWidth(), image.getHeight()),
                    "gui", List.of(screen.width, screen.height), "selected", screen.selectedId().toString()));
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
        CHECKS.add(Map.of("check", message, "passed", true));
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 9) return;
        var mc = Minecraft.getInstance(); long now = System.nanoTime();
        if (started == 0) started = now;
        try {
            if (now - started > 180_000_000_000L) throw new IllegalStateException("Native menu inspection timed out at step " + state);
            if (mc.screen instanceof StoneNetworkScreen) movePointerAway(mc);
            if (state == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                mc.options.guiScale().set(3); mc.resizeDisplay(); mc.options.pauseOnLostFocus = false;
                mc.options.renderDistance().set(4); mc.options.enableVsync().set(false); mc.getWindow().setFramerateLimit(60);
                mc.getTutorial().setStep(TutorialSteps.NONE);
                state = 1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-standing-menu-" + UUID.randomUUID(),
                        new LevelSettings("Standing Stone menu review", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
            } else if (state == 1 && mc.level != null && mc.player != null && mc.getSingleplayerServer() != null && mc.screen == null) {
                state = 2; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer())); next = now + 2_000_000_000L;
            } else if (state == 2 && pending.isDone() && mc.screen instanceof StoneNetworkScreen screen && now >= next) {
                pending.join(); capture(mc, screen, "list-wide");
                require(!button(screen, "Save").active, "Unchanged name disables Save");
                var field = editor(screen); field.setValue("");
                require(!button(screen, "Save").active, "Blank name disables Save");
                field.setValue("Northwatch Sanctum");
                var row = button(screen, "Desert Pyramid"); click(screen, row);
                require(field.getValue().equals("Northwatch Sanctum") && button(screen, "Save").active,
                        "Destination selection preserves the unsaved name and enables Save");
                previous = screen; click(screen, button(screen, "Save")); state = 3; next = now + 800_000_000L;
                pending = mc.getSingleplayerServer().submit(() -> {
                    var stone = (StandingStoneEntity) mc.getSingleplayerServer().overworld().getBlockEntity(SOURCE);
                    require(stone.id().equals(sourceId) && stone.key().equals(KEY)
                                    && AttunementMark.fromKey(stone.key()).equals(AttunementMark.fromKey(KEY)),
                            "Real rename keeps endpoint, network and rune signature unchanged");
                });
            } else if (state == 3 && mc.screen instanceof StoneNetworkScreen screen && screen != previous
                    && editor(screen).getValue().equals("Northwatch Sanctum") && now >= next && pending.isDone()) {
                pending.join();
                require(!button(screen, "Save").active && button(screen, "Travel").active, "Server rename refreshes the native menu and keeps Travel available");
                capture(mc, screen, "renamed-wide"); mc.options.guiScale().set(4); mc.resizeDisplay(); state = 4; next = now + 800_000_000L;
            } else if (state == 4 && mc.screen instanceof StoneNetworkScreen screen && now >= next) {
                capture(mc, screen, "list-narrow"); previous = screen; click(screen, button(screen, ">")); state = 5; next = now + 800_000_000L;
            } else if (state == 5 && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                require(editor(screen).getValue().equals("Northwatch Sanctum"), "Second page retains the current stone name independently of destination rows");
                capture(mc, screen, "second-page"); previous = screen; click(screen, button(screen, ">")); state = 6; next = now + 800_000_000L;
            } else if (state == 6 && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                require(!button(screen, ">").active && button(screen, "<").active, "All 21 live endpoints are reachable on three bounded pages");
                capture(mc, screen, "last-page"); previous = screen; state = 7; next = now + 800_000_000L;
                pending = mc.getSingleplayerServer().submit(() -> {
                    var server = mc.getSingleplayerServer(); var stone = (StandingStoneEntity) server.overworld().getBlockEntity(SINGLE);
                    StoneTravel.open(server.getPlayerList().getPlayers().getFirst(), stone, 0);
                });
            } else if (state == 7 && pending.isDone() && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                pending.join(); require(editor(screen).getValue().equals("Lone Standing Stone") && !button(screen, "Travel").active,
                        "A network containing only the current endpoint disables Travel");
                capture(mc, screen, "only-current-stone"); writeMetadata(); state = 9; mc.stop();
            }
        } catch (Exception failure) {
            state = 9; LogUtils.getLogger().error("Standing Stone menu inspection failed", failure);
            try {
                Path out = Path.of(System.getProperty("vestige.capture.output")); Files.createDirectories(out);
                Files.writeString(out.resolve("error.txt"), failure.toString());
            } catch (Exception ignored) { }
            mc.stop();
        }
    }
    private static void writeMetadata() throws Exception {
        var hashes = new LinkedHashMap<String, String>();
        for (var type : List.of(StoneNetworkScreen.class, StoneTravelPayloads.class, StoneTravelPayloads.View.class, StoneTravelPayloads.Rename.class,
                StoneTravelPayloads.Page.class, StoneTravelPayloads.Request.class, StoneTravel.class, StandingStoneEntity.class,
                StoneDirectory.class, StoneNetwork.class, NativeStoneCapture.class)) {
            String path = type.getName().replace('.', '/') + ".class";
            try (var stream = type.getResourceAsStream("/" + path)) {
                hashes.put(path, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(stream).readAllBytes())));
            }
        }
        Path out = Path.of(System.getProperty("vestige.capture.output"));
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "engine", "Minecraft 1.21.1 / NeoForge 21.1.72", "source", "Minecraft main render target",
                "world", "isolated creative flat world", "menu", "real server-backed endpoints and payloads",
                "checks", CHECKS, "classSha256", hashes)) + "\n");
    }
}
