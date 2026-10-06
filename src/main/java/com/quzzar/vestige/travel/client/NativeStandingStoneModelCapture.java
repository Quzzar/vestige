package com.quzzar.vestige.travel.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.travel.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in real-world model inspection. Exports Minecraft's render target without desktop capture. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeStandingStoneModelCapture {
    private static final boolean FAMILY = "standing_family".equals(System.getProperty("vestige.capture.kind"));
    private static final boolean ENABLED = FAMILY || "standing_models".equals(System.getProperty("vestige.capture.kind"));
    private static final List<String> KEYS = List.of(
            "76f32f1c38d3f867b7314795817de918434f62f7742e565283a18dff843ed500",
            "d9943647cbf7789d06ff728ebe1ecd8973b9fef68ddb481a7f68a213c91dcfc4",
            "fd6beea2466e6b35d7d933b94217d9152e658d0b2a3fd7d409c3378c04c38811");
    private static final Map<StandingStoneShape, ItemStack> FAMILY_SHARDS = new EnumMap<>(StandingStoneShape.class);
    private record View(String name, Vec3 eye, Vec3 target, boolean finishes, boolean night, ParticleStatus particles,
                        List<StandingStoneShape> profiles, boolean grid) {
        View(String name, Vec3 eye, Vec3 target, boolean finishes, boolean night) {
            this(name, eye, target, finishes, night, ParticleStatus.ALL);
        }
        View(String name, Vec3 eye, Vec3 target, boolean finishes, boolean night, ParticleStatus particles) {
            this(name, eye, target, finishes, night, particles,
                    List.of(StandingStoneShape.BLADE, StandingStoneShape.SHOULDER, StandingStoneShape.LEANING), false);
        }
    }
    private static final List<View> VIEWS = FAMILY ? familyViews() : List.of(
            new View("three-shapes-day", new Vec3(4.5, 67.2, -7.5), new Vec3(.5, 66, .5), false, false),
            new View("shouldered-stone-close", new Vec3(2.4, 66.7, -2.8), new Vec3(.5, 66, .5), false, false),
            new View("matching-key-three-finishes", new Vec3(4.5, 67.2, -7.5), new Vec3(.5, 66, .5), true, false),
            new View("three-shapes-night", new Vec3(4.5, 67.2, -7.5), new Vec3(.5, 66, .5), false, true),
            new View("runes-night-minimal", new Vec3(1.8, 66.55, -2.5), new Vec3(.5, 66, .5), false, true, ParticleStatus.MINIMAL),
            new View("runes-night-decreased", new Vec3(1.8, 66.55, -2.5), new Vec3(.5, 66, .5), false, true, ParticleStatus.DECREASED),
            new View("runes-night-close", new Vec3(1.8, 66.55, -2.5), new Vec3(.5, 66, .5), false, true));
    private static List<View> familyViews() {
        var profiles = List.of(StandingStoneShape.values());
        var views = new ArrayList<View>();
        var overview = new Vec3(14, 76, -18);
        var center = new Vec3(0, 66, 7.5);
        views.add(new View("family-overview-day", overview, center, false, false, ParticleStatus.ALL, profiles, true));
        for (int row = 0; row < 4; row++) {
            views.add(new View("family-row-" + (row + 1), new Vec3(2.6, 67.2, -10.5), new Vec3(0, 66, 0),
                    false, false, ParticleStatus.ALL, profiles.subList(row * 4, row * 4 + 4), false));
        }
        views.add(new View("family-matching-finishes", new Vec3(2.6, 67.2, -10.5), new Vec3(0, 66, 0),
                true, false, ParticleStatus.ALL, Collections.nCopies(4, StandingStoneShape.CLEFT), false));
        views.add(new View("family-overview-night", overview, center, false, true, ParticleStatus.ALL, profiles, true));
        return List.copyOf(views);
    }
    private static final int CLIP_FRAMES = 120;
    private static final long CLIP_INTERVAL = 50_000_000L;
    private static final long CLIP_DURATION = 6_000_000_000L;
    private static int state, index;
    private static int clipFrame;
    private static long next, clipStart;
    private static CompletableFuture<Void> pending;
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>();
    private NativeStandingStoneModelCapture() { }
    private static float yaw(View view) {
        var delta = view.target.subtract(view.eye); return (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
    }
    private static float pitch(View view) {
        var delta = view.target.subtract(view.eye); return (float) -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)));
    }
    private static String key(int profile) { return KEYS.get(profile); }
    private static String key(StandingStoneShape profile) {
        if (!FAMILY) return key(profile.ordinal());
        return AttunementShardItem.signature(FAMILY_SHARDS.get(profile)).orElseThrow().key();
    }
    /** Use valid native shard blueprints, rather than hand-filled keys that repeat rune glyphs. */
    private static void prepareFamilyShards() {
        if (!FAMILY || !FAMILY_SHARDS.isEmpty()) return;
        var ingredients = List.of(Items.AMETHYST_SHARD, Items.AMETHYST_SHARD, Items.ECHO_SHARD, Items.IRON_INGOT,
                Items.DIAMOND, Items.LAPIS_LAZULI, Items.AIR, Items.AIR);
        for (int distance = 1; distance <= 4; distance++) for (int dy1 = -6; dy1 <= 6; dy1++) for (int dy2 = -6; dy2 <= 6; dy2++) {
            var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, distance, dy1, LeylineShaping.Shape.DIAGONAL, 5, dy2);
            var nodes = new ArrayList<RitualInputs.Node>();
            for (int i = 0; i < 8; i++) nodes.add(new RitualInputs.Node(i, geometry.offset(i), new ItemStack(ingredients.get(i)), ItemStack.EMPTY));
            var shard = AttunementShardItem.create(new RitualInputs(geometry, nodes));
            String key = AttunementShardItem.signature(shard).orElseThrow().key();
            FAMILY_SHARDS.putIfAbsent(StandingStoneShape.fromKey(key), shard);
            if (FAMILY_SHARDS.size() == StandingStoneShape.values().length) return;
        }
        throw new IllegalStateException("Native shard blueprints did not cover every Standing Stone form");
    }
    private static void prepare(MinecraftServer server, View view) {
        prepareFamilyShards();
        var level = server.overworld();
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        level.setWeatherParameters(6000, 0, false, false); level.setDayTime(view.night ? 18000 : 5500);
        for (int x = -16; x <= 16; x++) for (int z = -16; z <= 16; z++) {
            level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            for (int y = 65; y <= 68; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
        for (int i = 0; i < view.profiles.size(); i++) {
            var finishes = FAMILY
                    ? List.of(ApparatusMaterials.QUARTZ, ApparatusMaterials.COBBLED_DEEPSLATE, ApparatusMaterials.PRISMARINE_BRICKS, ApparatusMaterials.BRICKS)
                    : List.of(ApparatusMaterials.TUFF, ApparatusMaterials.STONE_BRICKS, ApparatusMaterials.SANDSTONE);
            var material = view.finishes ? finishes.get(i) : ApparatusMaterials.TUFF;
            var pos = new BlockPos(FAMILY ? (3 - 2 * (i % 4)) * 2 : (i - 1) * 3, 65, view.grid ? i / 4 * 5 : 0);
            var block = StandingStones.STONES.get(material).get();
            var profile = view.finishes && !FAMILY ? StandingStoneShape.SHOULDER : view.profiles.get(i);
            var lower = block.defaultBlockState().setValue(StandingStoneBlock.PROFILE, profile);
            level.setBlock(pos, lower, 3); level.setBlock(pos.above(), lower.setValue(StandingStoneBlock.HALF, DoubleBlockHalf.UPPER), 3);
            var bound = FAMILY ? StandingStones.fromShard(FAMILY_SHARDS.get(profile), material) : StandingStones.bound(key(profile), material);
            var stone = (StandingStoneEntity) level.getBlockEntity(pos); stone.configure(StandingStones.key(bound).orElseThrow(), "Prototype " + profile.getSerializedName());
            if (level.getBlockEntity(pos.above()) != null || !level.getBlockState(pos.above()).is(block)) throw new IllegalStateException("Invalid upper half");
            CHECKS.add(Map.of("view", view.name, "position", List.of(pos.getX(), pos.getY(), pos.getZ()), "material", material.id(),
                    "profile", profile.getSerializedName(), "key", stone.key(), "upperHasEndpoint", false,
                    "keySource", FAMILY ? "native-shard-blueprint" : "original-review-key"));
        }
        var player = server.getPlayerList().getPlayers().getFirst();
        player.setNoGravity(true); player.getAbilities().flying = true; player.onUpdateAbilities();
        player.teleportTo(view.eye.x, view.eye.y - player.getEyeHeight(), view.eye.z);
        player.setYRot(yaw(view)); player.setXRot(pitch(view));
    }
    @SubscribeEvent public static void input(ClientTickEvent.Pre event) {
        if (!ENABLED || (state != 4 && state != 5)) return;
        var mc = Minecraft.getInstance(); if (mc.player == null) return;
        var view = VIEWS.get(index); var eye = view.eye; double feet = eye.y - mc.player.getEyeHeight();
        mc.mouseHandler.releaseMouse();
        for (var key : List.of(mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump,
                mc.options.keyShift, mc.options.keySprint, mc.options.keyUse, mc.options.keyAttack)) key.setDown(false);
        mc.player.setPos(eye.x, feet, eye.z); mc.player.setDeltaMovement(Vec3.ZERO);
        mc.player.xo = eye.x; mc.player.yo = feet; mc.player.zo = eye.z;
        mc.player.setYRot(yaw(view)); mc.player.yRotO = yaw(view); mc.player.setXRot(pitch(view)); mc.player.xRotO = pitch(view);
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 9) return;
        var mc = Minecraft.getInstance(); long now = System.nanoTime();
        try {
            if (state == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                var repository = mc.getResourcePackRepository(); repository.reload();
                var selected = new ArrayList<>(repository.getSelectedIds()); selected.removeIf(id -> id.startsWith("file/vestige-"));
                repository.setSelected(selected); pending = mc.reloadResourcePacks(); state = 1;
            } else if (state == 1 && pending.isDone() && mc.getOverlay() == null) {
                pending.join(); state = 2;
                mc.options.pauseOnLostFocus = false; mc.options.hideGui = true; mc.options.fov().set(48);
                mc.options.renderDistance().set(4); mc.options.enableVsync().set(false); mc.getWindow().setFramerateLimit(60);
                mc.options.setCameraType(CameraType.FIRST_PERSON); mc.getTutorial().setStep(TutorialSteps.NONE);
                mc.createWorldOpenFlows().createFreshLevel("vestige-standing-models-" + UUID.randomUUID(),
                        new LevelSettings("Standing Stone prototypes", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
            } else if (state == 2 && mc.level != null && mc.player != null && mc.getSingleplayerServer() != null && mc.screen == null) {
                state = 3; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer(), VIEWS.get(index)));
            } else if (state == 3 && pending.isDone()) {
                pending.join(); mc.options.particles().set(VIEWS.get(index).particles);
                state = 4; next = now + 6_000_000_000L;
            } else if (state == 4 && now >= next && mc.screen == null) {
                Path out = Path.of(System.getProperty("vestige.capture.output")); Files.createDirectories(out);
                var view = VIEWS.get(index);
                try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                    image.writeToFile(out.resolve(view.name + ".png"));
                    CHECKS.add(Map.of("capture", view.name, "width", image.getWidth(), "height", image.getHeight(), "dayTime", view.night ? 18000 : 5500,
                            "particles", view.particles.name(),
                            "camera", List.of(view.eye.x, view.eye.y, view.eye.z), "yaw", yaw(view), "pitch", pitch(view)));
                }
                if (index == VIEWS.size() - 1) {
                    Files.createDirectories(out.resolve("rune-drift-frames"));
                    state = 5; clipStart = now; next = now + CLIP_INTERVAL;
                } else {
                    index++;
                    state = 3; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer(), VIEWS.get(index)));
                }
            } else if (state == 5 && now >= next && mc.screen == null) {
                Path out = Path.of(System.getProperty("vestige.capture.output"));
                try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                    image.writeToFile(out.resolve("rune-drift-frames/frame-%03d.png".formatted(clipFrame)));
                }
                CHECKS.add(Map.of("frame", clipFrame, "elapsedSeconds", (now - clipStart) / 1_000_000_000.0,
                        "gameTime", mc.level.getGameTime()));
                clipFrame++; next = now + CLIP_INTERVAL;
                if (clipFrame == CLIP_FRAMES || now - clipStart >= CLIP_DURATION) {
                    writeMetadata(out); state = 9; mc.stop();
                }
            }
        } catch (Exception failure) {
            state = 9; LogUtils.getLogger().error("Standing Stone capture failed", failure); mc.stop();
        }
    }

    private static void writeMetadata(Path out) throws Exception {
        var hashes = new LinkedHashMap<String, String>();
        for (var profile : StandingStoneShape.values()) for (var half : List.of("lower", "upper", "whole")) {
            String path = "assets/vestige/models/block/standing_stone_" + profile.getSerializedName() + "_" + half + ".obj";
            try (var stream = NativeStandingStoneModelCapture.class.getResourceAsStream("/" + path)) {
                hashes.put(path, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(stream).readAllBytes())));
            }
        }
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "engine", "Minecraft 1.21.1 / NeoForge 21.1.72", "source", "Minecraft main render target", "checks", CHECKS, "meshSha256", hashes)) + "\n");
    }
}
