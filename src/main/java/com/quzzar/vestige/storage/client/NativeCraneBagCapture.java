package com.quzzar.vestige.storage.client;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.storage.CraneBagItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in native presentation inspection; the shown snapshots are fixtures, not a live storage test. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeCraneBagCapture {
    private static final boolean ENABLED = "crane_bag".equals(System.getProperty("vestige.capture.kind"));
    private static BagScreen screen;
    private static long ready;
    private static boolean complete;
    private static boolean normalOrder, advancedOrder;
    private NativeCraneBagCapture() { }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) throws Exception {
        if (!ENABLED || complete) return;
        var mc = Minecraft.getInstance();
        if (screen == null) {
            if (!(mc.screen instanceof TitleScreen) || mc.getOverlay() != null) return;
            mc.options.guiScale().set(2); mc.options.pauseOnLostFocus = false;
            screen = new BagScreen(); mc.setScreen(screen); ready = System.nanoTime() + 2_000_000_000L; return;
        }
        if (mc.screen != screen || mc.getOverlay() != null || !screen.rendered) {
            screen.rendered = false; ready = System.nanoTime() + 2_000_000_000L;
            if (mc.screen instanceof TitleScreen && mc.getOverlay() == null) mc.setScreen(screen);
            return;
        }
        screen.rendered = false;
        if (System.nanoTime() < ready) return;
        complete = true;
        if (!normalOrder || !advancedOrder) throw new IllegalStateException("Native Crane Bag name/runes/contents order was not verified");
        var out = Path.of(System.getProperty("vestige.capture.output"), "crane-bag"); Files.createDirectories(out);
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { image.writeToFile(out.resolve("native-tooltips.png")); }
        String hash;
        try (var asset = mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location("models/item/crane_bag.json")).open()) {
            hash = HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes()));
        }
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "source", "Minecraft main framebuffer; native item/bar/rune/BundleTooltip rendering",
                "modelSha256", hash, "key", CraneBagItem.key(screen.partial).orElseThrow(), "normalAndAdvancedRunes", true,
                "previewWeight", screen.partial.get(DataComponents.BUNDLE_CONTENTS).weight().toString(),
                "fixtureOnly", true, "temporaryTexture", "minecraft:item/bundle",
                "nativeGatherOrder", "name, four runes, bundle contents, fullness, optional advanced details")) + "\n");
        mc.stop();
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tooltipOrder(RenderTooltipEvent.GatherComponents event) {
        if (!ENABLED || CraneBagItem.key(event.getItemStack()).isEmpty()) return;
        var elements = event.getTooltipElements();
        var mark = AttunementMark.fromKey(CraneBagItem.key(event.getItemStack()).orElseThrow()).component();
        if (elements.size() < 3 || elements.get(1).left().filter(mark::equals).isEmpty()
                || elements.get(2).right().filter(component -> component instanceof net.minecraft.world.inventory.tooltip.BundleTooltip).isEmpty())
            throw new IllegalStateException("Native gathered tooltip does not put runes directly after the name");
        if (Minecraft.getInstance().options.advancedItemTooltips) advancedOrder = true; else normalOrder = true;
    }
    private static final class BagScreen extends Screen {
        private boolean rendered;
        private final ItemStack partial, empty, full;
        private BagScreen() {
            super(Component.literal("Crane Bag · temporary Bundle art"));
            var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
            var ingredients = AttunementShardItem.ingredients(); var nodes = new ArrayList<RitualInputs.Node>();
            for (int i = 0; i < 8; i++) nodes.add(new RitualInputs.Node(i, geometry.offset(i), i < ingredients.size()
                    ? new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))) : ItemStack.EMPTY, ItemStack.EMPTY));
            partial = CraneBagItem.fromShard(AttunementShardItem.create(new RitualInputs(geometry, nodes))).orElseThrow();
            empty = partial.copy(); full = partial.copy();
            partial.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(new ItemStack(Items.COBBLESTONE, 16), new ItemStack(Items.ENDER_PEARL, 4), new ItemStack(Items.DIAMOND, 12))));
            full.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(new ItemStack(Items.COBBLESTONE, 64))));
            for (var flag : List.of(TooltipFlag.NORMAL, TooltipFlag.ADVANCED)) {
                var lines = partial.getTooltipLines(Item.TooltipContext.EMPTY, null, flag);
                if (!lines.get(1).equals(AttunementMark.fromKey(CraneBagItem.key(partial).orElseThrow()).component()))
                    throw new IllegalStateException("Crane Bag signature tooltip moved");
            }
        }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(0, 0, width, height, 0xff26272b); graphics.drawString(font, title, 16, 14, 0xffffff, false);
            graphics.drawString(font, "Empty          Partial          Full", 16, 38, 0xc7c7c7, false);
            int x = 22;
            for (var bag : List.of(empty, partial, full)) {
                graphics.fill(x - 2, 54, x + 18, 74, 0xff777777);
                graphics.renderItem(bag, x, 56); graphics.renderItemDecorations(font, bag, x, 56); x += 78;
            }
            graphics.drawString(font, "Normal hover", 16, 100, 0xc7c7c7, false);
            graphics.drawString(font, "Advanced hover · same signature", 245, 100, 0xc7c7c7, false);
            var options = Minecraft.getInstance().options; boolean advanced = options.advancedItemTooltips;
            try {
                options.advancedItemTooltips = false; graphics.renderTooltip(font, partial, 20, 145);
                options.advancedItemTooltips = true; graphics.renderTooltip(font, partial, 245, 145);
            } finally { options.advancedItemTooltips = advanced; }
            rendered = true;
        }
        @Override public boolean isPauseScreen() { return false; }
    }
}
