package com.quzzar.vestige.travel.client;

import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.travel.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Native peer list, current-stone naming and the same four-rune signature as its shard. */
public final class StoneNetworkScreen extends Screen {
    private static final int BACKGROUND = 0xff191c20, FRAME = 0xff555a61;
    private static final int ACCENT = 0xff83d9ef, TEXT = 0xffeeeeee, MUTED = 0xffb5bbc3;
    private final StoneTravelPayloads.View view;
    private UUID selected;
    private int left, top, panelWidth, panelHeight, listY, rowHeight;
    private EditBox nameField;
    private Button save, travel;

    public StoneNetworkScreen(StoneTravelPayloads.View view) {
        super(Component.translatable("screen.vestige.standing_stones")); this.view = view;
        selected = view.destinations().stream().filter(node -> !node.id().equals(view.source()))
                .map(StoneTravelPayloads.Destination::id).findFirst().orElse(view.source());
    }
    public static void open(StoneTravelPayloads.View view) {
        var minecraft = Minecraft.getInstance(); var next = new StoneNetworkScreen(view);
        if (minecraft.screen instanceof StoneNetworkScreen current && current.view.source().equals(view.source())
                && view.destinations().stream().anyMatch(node -> node.id().equals(current.selected))) next.selected = current.selected;
        minecraft.setScreen(next);
    }
    UUID selectedId() { return selected; }
    private Optional<StoneTravelPayloads.Destination> selected() {
        return view.destinations().stream().filter(node -> node.id().equals(selected)).findFirst();
    }
    private void select(StoneTravelPayloads.Destination node) {
        selected = node.id(); travel.active = !selected.equals(view.source());
    }
    @Override protected void init() {
        String draft = nameField == null ? view.sourceName() : nameField.getValue();
        panelWidth = Math.min(400, width - 16); panelHeight = Math.min(300, height - 16);
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        listY = top + 79;
        rowHeight = Math.min(22, (panelHeight - 79 - 48) / StoneTravel.PAGE_SIZE);
        save = Button.builder(Component.translatable("screen.vestige.save_name"), button -> submitRename())
                .bounds(left + panelWidth - 64, top + 40, 56, 20).build();
        nameField = new EditBox(font, left + 8, top + 40, panelWidth - 78, 20,
                Component.translatable("screen.vestige.current_stone"));
        nameField.setMaxLength(64); nameField.setValue(draft); nameField.setCursorPosition(0); nameField.setHighlightPos(0);
        nameField.setResponder(value -> updateSave());
        updateSave(); addRenderableWidget(nameField); addRenderableWidget(save);
        for (int i = 0; i < view.destinations().size(); i++) {
            var node = view.destinations().get(i);
            String suffix = node.id().equals(view.source()) ? " · " + Component.translatable("screen.vestige.here").getString() : "";
            String name = font.plainSubstrByWidth(node.name(), panelWidth - 34 - font.width(suffix));
            var label = Component.literal(name + suffix);
            if (node.id().equals(view.source())) label.withStyle(ChatFormatting.GRAY);
            addRenderableWidget(Button.builder(label, button -> select(node))
                    .bounds(left + 8, listY + i * rowHeight, panelWidth - 16, rowHeight - 1)
                    .tooltip(Tooltip.create(Component.literal(node.name() + "\n" + coordinates(node.position())))).build());
        }
        int footer = top + panelHeight - 25;
        travel = Button.builder(Component.translatable("screen.vestige.travel"), button -> {
            if (!travel.active) return;
            PacketDistributor.sendToServer(new StoneTravelPayloads.Request(view.source(), selected)); onClose();
        }).bounds(left + panelWidth - 96, footer, 88, 20).build();
        travel.active = selected().isPresent() && !selected.equals(view.source()); addRenderableWidget(travel);
        if (view.total() > StoneTravel.PAGE_SIZE) {
            int pageY = top + panelHeight - 47;
            var previous = Button.builder(Component.literal("<"), button -> requestPage(view.page() - 1))
                    .bounds(left + 8, pageY, 20, 18).build();
            previous.active = view.page() > 0; addRenderableWidget(previous);
            var next = Button.builder(Component.literal(">"), button -> requestPage(view.page() + 1))
                    .bounds(left + 76, pageY, 20, 18).build();
            next.active = (view.page() + 1) * StoneTravel.PAGE_SIZE < view.total(); addRenderableWidget(next);
        }
    }
    private void updateSave() {
        String value = nameField.getValue().strip();
        save.active = !value.isBlank() && !value.equals(view.sourceName());
    }
    private void submitRename() {
        if (!save.active) return;
        PacketDistributor.sendToServer(new StoneTravelPayloads.Rename(view.source(), nameField.getValue(), view.page()));
    }
    private void requestPage(int page) { PacketDistributor.sendToServer(new StoneTravelPayloads.Page(view.source(), page)); }
    private static String coordinates(net.minecraft.core.BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (nameField.isFocused() && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            submitRename(); return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // render() owns the backdrop; the default background would blur this already-drawn UI.
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xcc101215);
        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, FRAME);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, BACKGROUND);
        graphics.drawString(font, title, left + 8, top + 8, TEXT, false);
        graphics.drawString(font, Component.translatable("screen.vestige.current_stone"), left + 8, top + 27, MUTED, false);
        graphics.drawString(font, Component.translatable(view.total() == 1 ? "screen.vestige.one_stone" : "screen.vestige.destinations", view.total()), left + 8, top + 66, MUTED, false);
        if (view.total() > StoneTravel.PAGE_SIZE)
            graphics.drawCenteredString(font, (view.page() + 1) + "/" + ((view.total() + StoneTravel.PAGE_SIZE - 1) / StoneTravel.PAGE_SIZE),
                    left + 52, top + panelHeight - 42, MUTED);
        var signature = Component.translatable("screen.vestige.signature");
        graphics.drawString(font, signature, left + 8, top + panelHeight - 19, MUTED, false);
        graphics.drawString(font, AttunementMark.fromKey(view.key()).component(), left + 16 + font.width(signature), top + panelHeight - 19, TEXT, false);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (int i = 0; i < view.destinations().size(); i++) if (view.destinations().get(i).id().equals(selected))
            graphics.fill(left + 4, listY + i * rowHeight, left + 6, listY + (i + 1) * rowHeight - 1, ACCENT);
    }
    @Override public boolean isPauseScreen() { return false; }
}
