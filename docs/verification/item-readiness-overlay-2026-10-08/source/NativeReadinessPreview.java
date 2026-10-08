package com.quzzar.vestige.magic.world.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.equipment.MagicEquipment;
import com.quzzar.vestige.magic.presentation.ManaReadiness;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Isolated UI study. Never compiled into the production source tree or installed pack. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeReadinessPreview {
    private static final boolean ENABLED = "item_readiness".equals(System.getProperty("vestige.capture.kind"));
    private record View(String name, boolean opposed, int scale) { }
    private static final List<View> VIEWS = List.of(new View("opposed-scale-3", true, 3),
            new View("matched-scale-3", false, 3), new View("opposed-scale-2", true, 2), new View("matched-scale-2", false, 2));
    private static final List<Map<String, Object>> FRAMES = new ArrayList<>();
    private static int state, index, frame;
    private static long boot, started, next, doneAt;
    private static CompletableFuture<Void> pending;
    private static final int AMBER = 0x669C783C, BLUE_GRAY = 0x66758EAB;
    private static final Path OUT = Path.of(System.getProperty("vestige.capture.output", "."));
    private NativeReadinessPreview() { }

    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 9) return;
        var mc = Minecraft.getInstance(); long now = System.nanoTime();
        if (boot == 0) boot = now;
        try {
            if (now - boot > 900_000_000_000L) throw new IllegalStateException("Readiness preview timed out in state " + state);
            if (state == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                mc.options.pauseOnLostFocus = false; mc.options.hideGui = false;
                mc.options.renderDistance().set(2); mc.options.simulationDistance().set(5);
                mc.options.enableVsync().set(false); mc.getWindow().setFramerateLimit(60);
                mc.getTutorial().setStep(TutorialSteps.NONE); state = 1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-readiness-" + UUID.randomUUID(),
                        new LevelSettings("Readiness UI study", GameType.SURVIVAL, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
            } else if (state == 1 && mc.player != null && mc.level != null && mc.getSingleplayerServer() != null && mc.screen == null) {
                reset(mc); state = 2;
            } else if (state == 2 && pending.isDone()) {
                pending.join(); var view = VIEWS.get(index);
                mc.options.guiScale().set(view.scale); mc.resizeDisplay();
                mc.player.getCooldowns().addCooldown(Items.ENDER_PEARL, 300);
                mc.player.getCooldowns().addCooldown(Items.LEATHER_BOOTS, 300);
                mc.setScreen(new Study(view)); mc.mouseHandler.releaseMouse();
                frame = 0; started = now; next = now + 200_000_000L; doneAt = 0; state = 3;
            } else if (state == 3 && now >= next) {
                var view = VIEWS.get(index);
                float recovery = mc.player.getCooldowns().getCooldownPercent(Items.ENDER_PEARL, mc.getTimer().getGameTimeDeltaPartialTick(true));
                float mana = ManaReadiness.shortage(ItemManaOverlay.amount(), 12);
                Files.createDirectories(OUT.resolve(view.name));
                String file = view.name + "/frame-" + String.format(Locale.ROOT, "%04d", frame++) + ".png";
                try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(OUT.resolve(file)); }
                FRAMES.add(Map.of("file", file, "seconds", (now - started) / 1e9,
                        "mana", ItemManaOverlay.amount(), "shortage", mana, "recovery", recovery,
                        "guiScale", view.scale, "opposed", view.opposed));
                next = now + 100_000_000L;
                if (recovery == 0 && mana == 0 && doneAt == 0) doneAt = now + 1_000_000_000L;
                if (doneAt > 0 && now >= doneAt) {
                    if (++index == VIEWS.size()) {
                        Files.writeString(OUT.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                                "engine", "Minecraft 1.21.1 / NeoForge 21.1.72", "source", "Minecraft main render target",
                                "scope", "Native UI prototype; actual server mana regeneration and native client cooldown timer. Candidate overlays do not add item abilities.",
                                "exampleManaPrice", 12, "cooldownTicks", 300, "manaArgb", "7FFFFFFF",
                                "amberArgb", "669C783C", "blueGrayArgb", "66758EAB", "frames", FRAMES)) + "\n");
                        state = 9; mc.stop();
                    } else { reset(mc); state = 2; }
                }
            }
        } catch (Exception failure) {
            state = 9; LogUtils.getLogger().error("Readiness UI study failed", failure); mc.stop();
        }
    }

    private static void reset(Minecraft mc) {
        pending = mc.getSingleplayerServer().submit(() -> {
            var server = mc.getSingleplayerServer(); var player = server.getPlayerList().getPlayers().getFirst();
            var level = server.overworld();
            level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
            level.setDayTime(5500); level.setWeatherParameters(6000, 0, false, false);
            player.getInventory().clearContent(); player.getInventory().setItem(0, new ItemStack(Items.ENDER_PEARL));
            player.setNoGravity(true); player.connection.teleport(.5, -59, .5, 0, 8);
            NativeMana.set(player, 100); NativeMana.set(player, 0);
        });
    }

    private static final class Study extends Screen {
        private static final ResourceLocation INVENTORY = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");
        private final View view;
        private final ItemStack eye = new ItemStack(ScrollItems.HOMEBOUND_EYE.get());
        private final ItemStack robe = new ItemStack(MagicEquipment.WARDWEAVE.get());
        private final ItemStack pearl = new ItemStack(Items.ENDER_PEARL), boots = new ItemStack(Items.LEATHER_BOOTS);
        Study(View view) { super(Component.literal("Mana and recovery UI study")); this.view = view; }
        @Override public boolean isPauseScreen() { return false; }
        @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
            int x = (width - 304) / 2, y = (height - 172) / 2;
            g.fill(x, y, x + 304, y + 172, 0xEC202020);
            g.drawString(font, "MANA + RECOVERY", x + 12, y + 8, 0xFFFFFF, false);
            g.drawString(font, view.opposed ? "Opposite clearing directions" : "Same clearing direction", x + 12, y + 21, 0xBDBDBD, false);
            String[] labels = {"Vanilla", "Mana", "Recovery", "Both", "Ready"};
            for (int i = 0; i < labels.length; i++) g.drawString(font, labels[i], x + 12 + i * 56, y + 35, 0xD6D6D6, false);
            float recovery = minecraft.player.getCooldowns().getCooldownPercent(Items.ENDER_PEARL, partial);
            float missing = ManaReadiness.shortage(ItemManaOverlay.amount(), 12);
            row(g, x, y + 49, "Warm amber", 0xD5B37B, AMBER, missing, recovery);
            row(g, x, y + 89, "Blue-gray", 0xA8BFD9, BLUE_GRAY, missing, recovery);
            g.drawString(font, String.format(Locale.ROOT, "Mana %.1f / 12   Recovery %.1fs", Math.min(12, ItemManaOverlay.amount()), recovery * 15), x + 12, y + 132, 0xD6D6D6, false);
            g.drawString(font, "Native icons + slots, standard GUI scale " + view.scale, x + 12, y + 145, 0xBDBDBD, false);
            g.drawString(font, "Visual prototype; item abilities unchanged", x + 12, y + 158, 0xBDBDBD, false);
        }
        private void row(GuiGraphics g, int x, int y, String label, int textColor, int tint, float mana, float recovery) {
            g.drawString(font, label, x + 12, y, textColor, false);
            for (int column = 0; column < 5; column++) for (int item = 0; item < 2; item++) {
                int sx = x + 13 + column * 56 + item * 22, sy = y + 13;
                ItemStack stack = column == 0 ? (item == 0 ? pearl : boots) : (item == 0 ? eye : robe);
                g.blit(INVENTORY, sx - 1, sy - 1, 7, 83, 18, 18);
                g.renderItem(stack, sx, sy); g.renderItemDecorations(font, stack, sx, sy);
                g.pose().pushPose(); g.pose().translate(0, 0, 210);
                if (column == 1 || column == 3) shade(g, sx, sy, mana, Integer.MAX_VALUE, false);
                if (column == 2 || column == 3) shade(g, sx, sy, recovery, tint, view.opposed);
                g.pose().popPose();
            }
        }
        private static void shade(GuiGraphics g, int x, int y, float fraction, int color, boolean top) {
            if (fraction <= 0) return;
            if (top) g.fill(RenderType.guiOverlay(), x, y, x + 16, y + Mth.ceil(16 * fraction), color);
            else {
                int first = y + Mth.floor(16 * (1 - fraction));
                g.fill(RenderType.guiOverlay(), x, first, x + 16, first + Mth.ceil(16 * fraction), color);
            }
        }
    }
}
