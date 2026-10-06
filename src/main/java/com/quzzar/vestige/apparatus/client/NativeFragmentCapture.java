package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Opt-in native fragment/font inspection without creating or changing a world. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeFragmentCapture {
    private static final boolean ENABLED = "fragments".equals(System.getProperty("vestige.capture.kind"));
    private static long ready;
    private static boolean complete;
    private static FragmentScreen screen;
    private NativeFragmentCapture() { }

    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) throws Exception {
        if (!ENABLED || complete) return;
        var mc = Minecraft.getInstance();
        if (screen == null) {
            if (!(mc.screen instanceof TitleScreen) || mc.getOverlay() != null) return;
            mc.options.guiScale().set(3);
            mc.options.pauseOnLostFocus = false;
            screen = new FragmentScreen();
            mc.setScreen(screen);
            ready = System.nanoTime() + 2_000_000_000L;
            return;
        }
        // A loading overlay can finish/reopen after the first TitleScreen frame.
        // Require this inspection screen to have actually drawn in the current frame.
        if (mc.screen != screen || mc.getOverlay() != null || !screen.rendered) {
            screen.rendered = false;
            ready = System.nanoTime() + 2_000_000_000L;
            if (mc.screen instanceof TitleScreen && mc.getOverlay() == null) mc.setScreen(screen);
            return;
        }
        screen.rendered = false;
        if (System.nanoTime() < ready) return;
        complete = true;
        Path out = Path.of(System.getProperty("vestige.capture.output"), "fragments");
        Files.createDirectories(out);
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(out.resolve("fragments.png")); }
        var hashes = new LinkedHashMap<String, String>();
        for (String path : List.of("textures/item/scroll_fragment.png", "models/item/scroll_fragment.json", "font/fragment_symbols.json", "textures/font/fragment_symbols.png", "fragment_symbols.json")) {
            try (var asset = mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()) {
                hashes.put(path, HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes())));
            }
        }
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "source", "Minecraft main render target after native item/font/tooltip rendering", "symbols", screen.traits.size(),
                "font", "vestige:fragment_symbols", "assetSha256", hashes,
                "symbolColor", String.format("#%06x", FragmentSymbols.label(VestigeMainMod.location("fire")).getStyle().getColor().getValue()),
                "unknownFallback", screen.foreign.getHoverName().getString())) + "\n");
        mc.stop();
    }

    private static final class FragmentScreen extends Screen {
        private boolean rendered;
        private final List<ResourceLocation> traits;
        private final ItemStack fire = ScrollItems.fragment(VestigeMainMod.location("fire"));
        private final ItemStack foreign = ScrollItems.fragment(ResourceLocation.parse("addon:fire"));
        private FragmentScreen() {
            super(Component.literal("Scroll fragments · 52 distinct trait symbols"));
            try (var input = NativeFragmentCapture.class.getResourceAsStream("/assets/vestige/fragment_symbols.json")) {
                traits = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject()
                        .keySet().stream().map(ResourceLocation::parse).toList();
            } catch (java.io.IOException exception) { throw new java.io.UncheckedIOException(exception); }
        }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xff202126);
            graphics.drawString(font, title, 16, 14, 0xffffff, false);
            int columnWidth = (width - 32) / 5;
            for (int i = 0; i < traits.size(); i++) {
                int x = 16 + i / 11 * columnWidth, y = 38 + i % 11 * 18;
                var trait = traits.get(i);
                graphics.renderItem(ScrollItems.fragment(trait), x, y);
                graphics.drawString(font, FragmentSymbols.label(trait), x + 20, y + 4, 0xf1debb, false);
                graphics.drawString(font, trait.getPath(), x + 32, y + 4, 0xc5c5c9, false);
            }
            int bottom = 250;
            graphics.drawString(font, "Native item renderer and hover names", 16, bottom, 0xbdbfc9, false);
            graphics.pose().pushPose();
            graphics.pose().translate(24, bottom + 18, 0);
            graphics.pose().scale(3, 3, 3);
            graphics.renderItem(fire, 0, 0);
            graphics.pose().popPose();
            graphics.renderItem(new ItemStack(ScrollItems.SCROLL.get()), 88, bottom + 30);
            graphics.renderItem(new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()), 112, bottom + 30);
            graphics.renderTooltip(font, List.of(fire.getHoverName()), Optional.empty(), 152, bottom + 28);
            graphics.renderTooltip(font, List.of(foreign.getHoverName()), Optional.empty(), 342, bottom + 28);
            rendered = true;
        }
        @Override public boolean isPauseScreen() { return false; }
    }
}
