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
import net.minecraft.core.registries.BuiltInRegistries;
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
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/** Locked production artwork per real vanilla menu, with identical vanilla neighbors. Review only. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class LockedItemTextureCapture {
    private static final boolean ENABLED = "locked_item_texture".equals(System.getProperty("vestige.capture.kind"));
    private static final List<String> IDS = List.of("homebound_eye", "whispering_shell");
    private static final List<String> NAMES = List.of("Eye A · Centered pearl", "Shell B · Deep coil");
    private static final List<String> FILENAMES = List.of("eye-a.png", "shell-b.png");
    private static final List<Item> NEIGHBORS = List.of(Items.BONE, Items.COAL, Items.IRON_INGOT, Items.FLINT, Items.ENDER_PEARL, Items.SPIDER_EYE, Items.NAUTILUS_SHELL, Items.STRING, Items.LEATHER,
            Items.FEATHER, Items.REDSTONE, Items.LAPIS_LAZULI, Items.AMETHYST_SHARD, Items.PAPER, Items.ECHO_SHARD, Items.GLASS_BOTTLE, Items.HONEY_BOTTLE, Items.SNOWBALL,
            Items.ARROW, Items.IRON_SWORD, Items.IRON_PICKAXE, Items.BOW, Items.SHEARS, Items.FISHING_ROD, Items.BUCKET, Items.BREAD, Items.APPLE);
    private static final List<Item> INVENTORY = List.of(Items.COBBLESTONE, Items.OAK_LOG, Items.OAK_PLANKS, Items.TORCH, Items.DIRT, Items.WHEAT, Items.CARROT, Items.POTATO, Items.COOKED_BEEF);
    private static final List<Item> HOTBAR = List.of(Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.IRON_AXE, Items.IRON_SHOVEL, Items.BOW, Items.TORCH, Items.BREAD, Items.WATER_BUCKET, Items.COMPASS);
    private static CaptureHost screen;
    private static long ready;
    private static int phase;
    private static boolean complete;
    private static final List<Object> records = new ArrayList<>();
    private LockedItemTextureCapture() { }

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
        records.add(java.util.Map.of("file", FILENAMES.get(phase), "variant", IDS.get(phase), "customItems", 1, "customSlot", 13,
                "width", mc.getWindow().getWidth(), "height", mc.getWindow().getHeight(), "guiScale", mc.getWindow().getGuiScale()));
        if (++phase < IDS.size()) { show(mc); return; }
        complete = true;
        var paths = new LinkedHashSet<String>();
        for (String id : IDS) {
            paths.add("vestige:textures/item/" + id + ".png");
            paths.add("vestige:models/item/" + id + ".json");
        }
        paths.add("minecraft:models/item/paper.json");
        paths.add("minecraft:textures/gui/container/generic_54.png");
        var vanillaItems = new LinkedHashSet<Item>(NEIGHBORS);
        vanillaItems.remove(Items.PAPER);
        vanillaItems.addAll(INVENTORY);
        vanillaItems.addAll(HOTBAR);
        for (Item item : vanillaItems) {
            var sprite = mc.getItemRenderer().getModel(new ItemStack(item), null, null, 0).getParticleIcon().contents().name();
            paths.add(sprite.getNamespace() + ":textures/" + sprite.getPath() + ".png");
        }
        var hashes = new LinkedHashMap<String, String>();
        for (String path : paths) {
            try (var input = mc.getResourceManager().getResourceOrThrow(ResourceLocation.parse(path)).open()) {
                hashes.put(path, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes())));
            }
        }
        var resolved = new LinkedHashMap<String, String>();
        for (int i = 0; i < IDS.size(); i++) {
            String actual = mc.getItemRenderer().getModel(study(i), null, null, 0).getParticleIcon().contents().name().toString();
            String expected = "vestige:item/" + IDS.get(i);
            if (!expected.equals(actual)) throw new IllegalStateException("Resolved " + actual + " instead of " + expected);
            resolved.put(NAMES.get(i), actual);
        }
        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("engine", "Minecraft Java 1.21.1 / NeoForge 21.1.72");
        metadata.put("source", "Unedited native framebuffer, real vanilla three-row ChestMenu/ContainerScreen");
        metadata.put("screens", records);
        metadata.put("neighbors", NEIGHBORS.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList());
        metadata.put("inventory", INVENTORY.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList());
        metadata.put("hotbar", HOTBAR.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList());
        metadata.put("assetSha256", hashes);
        metadata.put("resolvedModels", resolved);
        metadata.put("worldOpened", false);
        metadata.put("artScope", "Production Homebound Eye item/model, without a binding or glint. Shell is an appearance-only Paper carrier referencing the locked production Shell model; its gameplay is not implemented. PNGs only add transparent atlas padding, preserving every approved pixel and alpha.");
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(metadata) + "\n");
        mc.stop();
    }

    private static void show(Minecraft mc) {
        mc.options.guiScale().set(3);
        mc.options.pauseOnLostFocus = false;
        mc.resizeDisplay();
        var inventory = new Inventory(null);
        var contents = new SimpleContainer(27);
        for (int i = 0; i < NEIGHBORS.size(); i++) contents.setItem(i, new ItemStack(NEIGHBORS.get(i)));
        contents.setItem(13, study(phase));
        for (int i = 0; i < INVENTORY.size(); i++) inventory.setItem(9 + i, new ItemStack(INVENTORY.get(i)));
        for (int i = 0; i < HOTBAR.size(); i++) inventory.setItem(i, new ItemStack(HOTBAR.get(i)));
        int customCount = 0;
        for (int i = 0; i < contents.getContainerSize(); i++) if (contents.getItem(i).has(DataComponents.CUSTOM_MODEL_DATA) || contents.getItem(i).is(ScrollItems.HOMEBOUND_EYE.get())) customCount++;
        for (int i = 0; i < inventory.getContainerSize(); i++) if (inventory.getItem(i).has(DataComponents.CUSTOM_MODEL_DATA) || inventory.getItem(i).is(ScrollItems.HOMEBOUND_EYE.get())) customCount++;
        if (customCount != 1) throw new IllegalStateException("Expected one review item, got " + customCount);
        screen = new CaptureHost(new ReviewScreen(ChestMenu.threeRows(0, inventory, contents), inventory, NAMES.get(phase)));
        mc.setScreen(screen);
        ready = System.nanoTime() + 1_500_000_000L;
    }

    private static ItemStack study(int index) {
        if (index == 0) return new ItemStack(ScrollItems.HOMEBOUND_EYE.get());
        var stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(261400));
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
            menuScreen.render(graphics, 0, 0, partialTick);
            rendered = true;
        }
    }

    private static final class ReviewScreen extends ContainerScreen {
        private ReviewScreen(ChestMenu menu, Inventory inventory, String name) { super(menu, inventory, Component.literal(name)); }
        @Override public boolean mouseClicked(double x, double y, int button) { return true; }
    }
}
