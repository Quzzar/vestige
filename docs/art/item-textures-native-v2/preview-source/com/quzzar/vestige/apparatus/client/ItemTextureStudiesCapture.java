package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;

/** Appearance-only native menu study. Ordinary builds exclude these sources and assets. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class ItemTextureStudiesCapture {
    private static final boolean ENABLED = "item_texture_studies".equals(System.getProperty("vestige.capture.kind"));
    private static final List<String> IDS = List.of("eye_a_soft_flint", "eye_b_dark_pebble", "eye_c_chipped_face", "eye_d_simple_talisman",
            "shell_a_soft_ash", "shell_b_compact_conch", "shell_c_slate", "shell_d_deep_dusk", "previous_eye", "previous_shell");
    private static final List<String> NAMES = List.of("Eye A · Soft Flint", "Eye B · Dark Pebble", "Eye C · Chipped Face", "Eye D · Simple Talisman",
            "Shell A · Soft Ash", "Shell B · Compact Conch", "Shell C · Slate", "Shell D · Deep Dusk", "Previous Homebound Eye", "Previous Whispering Shell");
    private static final int[] SCALES = {2, 3, 3, 3, 3};
    private static final List<String> FILENAMES = List.of("gui-2.png", "gui-3.png", "eye-hover.png", "shell-hover.png", "close-ups.png");
    private static final int[] COLUMNS = {2, 4, 6, 8};
    private static CaptureHost screen;
    private static long ready;
    private static int phase;
    private static boolean complete;
    private ItemTextureStudiesCapture() { }

    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) throws Exception {
        if (!ENABLED || complete) return;
        var mc = Minecraft.getInstance();
        if (screen == null) {
            if (!(mc.screen instanceof TitleScreen) || mc.getOverlay() != null) return;
            show(mc);
            return;
        }
        if (mc.getOverlay() != null || mc.screen != screen || !screen.rendered) {
            ready = System.nanoTime() + 1_500_000_000L;
            screen.rendered = false;
            if (mc.screen instanceof TitleScreen && mc.getOverlay() == null) show(mc);
            return;
        }
        screen.rendered = false;
        if (System.nanoTime() < ready) return;
        var out = Path.of(System.getProperty("vestige.capture.output"));
        Files.createDirectories(out);
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(out.resolve(FILENAMES.get(phase))); }
        if (++phase < SCALES.length) { show(mc); return; }
        complete = true;
        var paths = new ArrayList<String>();
        for (String id : IDS) {
            paths.add("vestige:textures/item/" + id + ".png");
            paths.add("vestige:models/item/" + id + ".json");
        }
        paths.addAll(List.of("minecraft:models/item/paper.json", "minecraft:textures/gui/container/generic_54.png", "minecraft:textures/item/flint.png",
                "minecraft:textures/item/spider_eye.png", "minecraft:textures/item/ender_pearl.png", "minecraft:textures/item/nautilus_shell.png",
                "minecraft:textures/item/echo_shard.png", "minecraft:textures/item/ink_sac.png"));
        var hashes = new LinkedHashMap<String, String>();
        for (String path : paths) {
            try (var input = mc.getResourceManager().getResourceOrThrow(ResourceLocation.parse(path)).open()) {
                hashes.put(path, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes())));
            }
        }
        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("engine", "Minecraft Java 1.21.1 / NeoForge 21.1.72");
        metadata.put("source", "Unedited main framebuffer after real vanilla ContainerScreen and item rendering");
        metadata.put("screens", FILENAMES);
        metadata.put("scales", List.of(2, 3, 3, 3, 3));
        metadata.put("variantOrder", IDS);
        metadata.put("columns", List.of(2, 4, 6, 8));
        metadata.put("assetSha256", hashes);
        var resolved = new LinkedHashMap<String, String>();
        for (int i = 0; i < IDS.size(); i++) {
            String actual = mc.getItemRenderer().getModel(study(i), null, null, 0).getParticleIcon().contents().name().toString();
            String expected = "vestige:item/" + IDS.get(i);
            if (!expected.equals(actual)) throw new IllegalStateException("Preview resolved " + actual + " instead of " + expected);
            resolved.put(NAMES.get(i), actual);
        }
        metadata.put("resolvedModels", resolved);
        metadata.put("worldOpened", false);
        metadata.put("previewOnly", "Paper CustomModelData 261070–261079, native item/generated models; no production texture replacement or gameplay change");
        metadata.put("menu", "Actual six-row ChestMenu / ContainerScreen. Label rows above the four Eyes and four Shells. Middle comparison row includes vanilla items and the two previous draft textures.");
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(metadata) + "\n");
        mc.stop();
    }

    private static void show(Minecraft mc) {
        mc.options.guiScale().set(SCALES[phase]);
        mc.options.pauseOnLostFocus = false;
        mc.resizeDisplay();
        if (phase == 4) {
            screen = new CaptureHost(new NativeGallery());
            mc.setScreen(screen);
            ready = System.nanoTime() + 1_500_000_000L;
            return;
        }
        var inventory = new Inventory(null);
        var contents = new SimpleContainer(54);
        for (int i = 0; i < 4; i++) {
            contents.setItem(9 + COLUMNS[i], study(i));
            contents.setItem(36 + COLUMNS[i], study(4 + i));
            inventory.setItem(9 + COLUMNS[i], study(i));
        }
        var comparisons = List.of(new ItemStack(Items.FLINT), new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.ENDER_PEARL), study(8),
                new ItemStack(Items.INK_SAC), new ItemStack(Items.NAUTILUS_SHELL), new ItemStack(Items.ECHO_SHARD), new ItemStack(Items.AMETHYST_SHARD), study(9));
        for (int i = 0; i < comparisons.size(); i++) contents.setItem(18 + i, comparisons.get(i));
        inventory.setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        inventory.setItem(1, study(0));
        inventory.setItem(2, study(4));
        inventory.setItem(3, new ItemStack(Items.COBBLESTONE, 64));
        inventory.setItem(4, new ItemStack(Items.TORCH, 32));
        inventory.setItem(5, new ItemStack(Items.BREAD, 8));
        screen = new CaptureHost(new ReviewScreen(ChestMenu.sixRows(0, inventory, contents), inventory, phase));
        mc.setScreen(screen);
        ready = System.nanoTime() + 1_500_000_000L;
    }

    private static ItemStack study(int index) {
        var stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(261070 + index));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(NAMES.get(index)));
        return stack;
    }

    private static final class CaptureHost extends Screen {
        private final Screen menuScreen;
        private boolean rendered;
        private CaptureHost(Screen menuScreen) { super(menuScreen.getTitle()); this.menuScreen = menuScreen; }
        @Override protected void init() { menuScreen.init(minecraft, width, height); }
        @Override public boolean mouseClicked(double x, double y, int button) { return true; }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            menuScreen.render(graphics, mouseX, mouseY, partialTick);
            rendered = true;
        }
    }

    /** Native item renderer at a larger GUI scale, with equal-scale vanilla references. */
    private static final class NativeGallery extends Screen {
        private NativeGallery() { super(Component.literal("Native texture close-ups")); }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xFF151515);
            graphics.drawString(font, "Homebound Eye", 20, 15, 0xFFECECEC, false);
            String[] eyeNames = {"A Soft Flint", "B Pebble", "C Chipped", "D Talisman"};
            String[] shellNames = {"A Soft Ash", "B Compact", "C Slate", "D Dusk"};
            for (int i = 0; i < 4; i++) {
                int center = (width - 320) / 2 + 40 + i * 80;
                graphics.drawCenteredString(font, eyeNames[i], center, 34, 0xFFECECEC);
                renderSlot(graphics, study(i), center - 24, 53);
                graphics.drawCenteredString(font, shellNames[i], center, 142, 0xFFECECEC);
                renderSlot(graphics, study(4 + i), center - 24, 161);
            }
            graphics.drawString(font, "Whispering Shell", 20, 123, 0xFFECECEC, false);
            graphics.drawString(font, "Vanilla and previous drafts · same scale", 20, 232, 0xFFBFBFBF, false);
            var refs = List.of(new ItemStack(Items.FLINT), new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.ENDER_PEARL),
                    new ItemStack(Items.NAUTILUS_SHELL), new ItemStack(Items.ECHO_SHARD), study(8), study(9));
            String[] names = {"Flint", "Spider", "Pearl", "Nautilus", "Echo", "Old Eye", "Old Shell"};
            for (int i = 0; i < refs.size(); i++) {
                int center = (width - 378) / 2 + 27 + i * 54;
                graphics.drawCenteredString(font, names[i], center, 249, 0xFFBFBFBF);
                renderSlot(graphics, refs.get(i), center - 24, 265);
            }
        }
        private void renderSlot(GuiGraphics graphics, ItemStack stack, int x, int y) {
            graphics.fill(x - 3, y - 3, x + 51, y + 51, 0xFF373737);
            graphics.fill(x - 2, y - 2, x + 50, y + 50, 0xFFC6C6C6);
            graphics.fill(x - 1, y - 1, x + 49, y + 49, 0xFF8B8B8B);
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(3.0F, 3.0F, 1.0F);
            graphics.renderFakeItem(stack, 0, 0);
            graphics.pose().popPose();
        }
    }

    private static final class ReviewScreen extends ContainerScreen {
        private final int phase;
        private ReviewScreen(ChestMenu menu, Inventory inventory, int phase) {
            super(menu, inventory, Component.literal("Item texture studies"));
            this.phase = phase;
        }
        @Override public boolean mouseClicked(double x, double y, int button) { return true; }
        @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
            super.renderLabels(graphics, mouseX, mouseY);
            graphics.drawString(font, "Eye", 8, 22, 0x404040, false);
            graphics.drawString(font, "Shell", 8, 76, 0x404040, false);
            for (int i = 0; i < COLUMNS.length; i++) {
                String letter = String.valueOf((char)('A' + i));
                graphics.drawString(font, letter, 8 + COLUMNS[i] * 18 + 5, 22, 0x404040, false);
                graphics.drawString(font, letter, 8 + COLUMNS[i] * 18 + 5, 76, 0x404040, false);
            }
        }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = phase >= 2 ? leftPos + 8 + COLUMNS[0] * 18 + 8 : 0;
            int y = phase == 2 ? topPos + 18 + 18 + 8 : phase == 3 ? topPos + 18 + 4 * 18 + 8 : 0;
            super.render(graphics, x, y, partialTick);
        }
    }
}
