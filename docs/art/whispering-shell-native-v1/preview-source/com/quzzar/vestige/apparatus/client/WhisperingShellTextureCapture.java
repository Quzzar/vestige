package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
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
import net.minecraft.world.item.Item;
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
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Preview-only addon: actual vanilla container/item rendering, without opening a world. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class WhisperingShellTextureCapture {
    private static final boolean ENABLED = "shell_texture".equals(System.getProperty("vestige.capture.kind"));
    private static CaptureHost screen;
    private static long ready;
    private static int phase;
    private static boolean complete;
    private static final int[] SCALES = {2, 2, 3, 3};
    private WhisperingShellTextureCapture() { }

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
        String filename = "gui-" + SCALES[phase] + (phase % 2 == 1 ? "-hover" : "") + ".png";
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(out.resolve(filename)); }
        if (++phase < SCALES.length) { show(mc); return; }
        complete = true;
        var hashes = new LinkedHashMap<String, String>();
        for (String path : List.of("vestige:textures/item/whispering_shell_preview.png", "vestige:models/item/whispering_shell_preview.json",
                "minecraft:models/item/paper.json", "minecraft:textures/gui/container/generic_54.png", "minecraft:textures/item/nautilus_shell.png",
                "minecraft:textures/item/echo_shard.png", "minecraft:textures/item/ender_pearl.png")) {
            try (var input = mc.getResourceManager().getResourceOrThrow(ResourceLocation.parse(path)).open()) {
                hashes.put(path, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes())));
            }
        }
        var metadata = Map.of("engine", "Minecraft 1.21.1 / NeoForge 21.1.72", "source", "Unchanged main framebuffer after actual vanilla ContainerScreen rendering",
                "screens", List.of("gui-2.png", "gui-2-hover.png", "gui-3.png", "gui-3-hover.png"), "textureSize", "16x16", "assetSha256", hashes,
                "previewOnly", "Paper with CustomModelData 202610060; native generated item model. No Shell registered or gameplay implemented.",
                "worldOpened", false, "nativeMenu", "ChestMenu / ContainerScreen with ordinary player inventory and hotbar slots");
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(metadata) + "\n");
        mc.stop();
    }

    private static void show(Minecraft mc) {
        mc.options.guiScale().set(SCALES[phase]);
        mc.options.pauseOnLostFocus = false;
        mc.resizeDisplay();
        var inventory = new Inventory(null);
        var contents = new SimpleContainer(27);
        List<Item> neighbors = List.of(Items.NAUTILUS_SHELL, Items.ECHO_SHARD, Items.ENDER_PEARL, Items.HEART_OF_THE_SEA,
                Items.AMETHYST_SHARD, Items.PHANTOM_MEMBRANE, Items.INK_SAC, Items.SCULK_SENSOR);
        contents.setItem(4, shell());
        for (int i = 0; i < neighbors.size(); i++) contents.setItem(9 + i, new ItemStack(neighbors.get(i)));
        contents.setItem(17, shell());
        contents.setItem(22, new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()));
        contents.setItem(23, new ItemStack(ScrollItems.HOMEBOUND_EYE.get()));
        inventory.setItem(0, new ItemStack(Items.DIAMOND_PICKAXE));
        inventory.setItem(1, new ItemStack(Items.COBBLESTONE, 64));
        inventory.setItem(2, shell());
        inventory.setItem(3, new ItemStack(Items.TORCH, 32));
        inventory.setItem(4, new ItemStack(Items.BREAD, 8));
        inventory.setItem(14, shell());
        screen = new CaptureHost(new ReviewScreen(ChestMenu.threeRows(0, inventory, contents), inventory, phase % 2 == 1));
        mc.setScreen(screen);
        ready = System.nanoTime() + 1_500_000_000L;
    }

    private static ItemStack shell() {
        var stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(202610060));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Whispering Shell"));
        return stack;
    }

    /** Keeps the native menu's player-dependent tick out of this appearance-only, worldless preview. */
    private static final class CaptureHost extends Screen {
        private final ReviewScreen menuScreen;
        private boolean rendered;
        private CaptureHost(ReviewScreen menuScreen) {
            super(menuScreen.getTitle());
            this.menuScreen = menuScreen;
        }
        @Override protected void init() { menuScreen.init(minecraft, width, height); }
        @Override public boolean mouseClicked(double x, double y, int button) { return true; }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            menuScreen.render(graphics, mouseX, mouseY, partialTick);
            rendered = true;
        }
    }

    private static final class ReviewScreen extends ContainerScreen {
        private final boolean hover;
        private ReviewScreen(ChestMenu menu, Inventory inventory, boolean hover) {
            super(menu, inventory, Component.literal("Whispering Shell · texture study"));
            this.hover = hover;
        }
        @Override public boolean mouseClicked(double x, double y, int button) { return true; }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = hover ? leftPos + 8 + 4 * 18 + 8 : 0;
            int y = hover ? topPos + 18 + 8 : 0;
            super.render(graphics, x, y, partialTick);
        }
    }
}
