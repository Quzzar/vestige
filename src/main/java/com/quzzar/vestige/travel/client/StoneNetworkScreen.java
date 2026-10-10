package com.quzzar.vestige.travel.client;

import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.travel.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import java.util.*;

/** Compact native buttons show each exact fare; the source name is edited only on request. */
public final class StoneNetworkScreen extends Screen {
    private static final int BACKGROUND = 0xff303030, FRAME = 0xff737373, TEXT = 0xffeeeeee, MUTED = 0xffbcbcbc;
    private static final int INSET = 0xff252525, SHADOW = 0xff161616, EDGE = 0xff494949;
    private static final int ROW_HEIGHT = 20, ROW_SPACING = 22;
    private StoneTravelPayloads.View view;
    private final Optional<TravelCostDisplay.Type> previewType;
    private final List<DestinationButton> destinations = new ArrayList<>();
    private int left, top, panelWidth, panelHeight;
    private boolean editing;
    private EditBox nameField;
    private Button save, edit;

    public StoneNetworkScreen(StoneTravelPayloads.View view) { this(view, Optional.empty()); }
    private StoneNetworkScreen(StoneTravelPayloads.View view, Optional<TravelCostDisplay.Type> previewType) {
        super(Component.literal(view.sourceName())); this.view = view; this.previewType = previewType;
    }
    /** Opt-in appearance fixture: alternate costs cannot send rename, paging or travel requests. */
    static StoneNetworkScreen costPreview(StoneTravelPayloads.View view, TravelCostDisplay.Type type) {
        if (!"stone_network".equals(System.getProperty("vestige.capture.kind"))) throw new IllegalStateException("Native capture only");
        return new StoneNetworkScreen(view, Optional.of(type));
    }
    public static void open(StoneTravelPayloads.View view) {
        var minecraft = Minecraft.getInstance(); var next = new StoneNetworkScreen(view);
        if (view.refreshOnly()) {
            if (minecraft.screen instanceof StoneNetworkScreen current && current.previewType.isEmpty()
                    && current.view.source().equals(view.source()) && current.view.page() == view.page()) {
                current.view = view; current.updateAffordability();
            }
            return;
        }
        if (minecraft.screen instanceof StoneNetworkScreen current && current.view.source().equals(view.source())
                && current.view.sourceName().equals(view.sourceName())) {
            next.editing = current.editing;
            minecraft.setScreen(next);
            next.nameField.setValue(current.nameField.getValue());
        } else minecraft.setScreen(next);
    }
    String sourceName() { return view.sourceName(); }
    public StoneTravelPayloads.View view() { return view; }
    int panelWidth() { return panelWidth; }
    static String ellipsize(Font font, String text, int width) {
        if (font.width(text) <= width) return text;
        String ellipsis = "…";
        if (font.width(ellipsis) > width) return "";
        return font.plainSubstrByWidth(text, width - font.width(ellipsis)) + ellipsis;
    }
    @Override protected void init() {
        String draft = nameField == null ? view.sourceName() : nameField.getValue();
        destinations.clear();
        panelWidth = Math.min(210, width - 16);
        int pages = view.total() > StoneTravel.PAGE_SIZE ? 22 : 0;
        panelHeight = 76 + view.destinations().size() * ROW_SPACING + pages;
        left = (width - panelWidth) / 2; top = (height - panelHeight) / 2;
        edit = new EditButton(left + panelWidth - 25, top + 13);
        edit.setTooltip(Tooltip.create(Component.translatable("screen.vestige.edit_stone_name")));
        nameField = new EditBox(font, left + 8, top + 8, panelWidth - 68, 20,
                Component.translatable("screen.vestige.edit_stone_name"));
        nameField.setMaxLength(64); nameField.setValue(draft); nameField.setCursorPosition(0); nameField.setHighlightPos(0);
        save = Button.builder(Component.translatable("screen.vestige.save_name"), button -> submitRename())
                .bounds(left + panelWidth - 54, top + 8, 46, 20).build();
        nameField.setResponder(value -> updateSave()); updateSave();
        addRenderableWidget(edit); addRenderableWidget(nameField); addRenderableWidget(save); setEditing(editing);
        for (int i = 0; i < view.destinations().size(); i++) {
            var node = view.destinations().get(i);
            var row = new DestinationButton(left + 8, top + 38 + i * ROW_SPACING, panelWidth - 16, ROW_HEIGHT, node);
            row.setTooltip(Tooltip.create(Component.literal(node.name() + "\n").append(row.cost().description())));
            destinations.add(row); addRenderableWidget(row);
        }
        if (pages != 0) {
            int pageY = top + panelHeight - 52;
            var previous = Button.builder(Component.literal("<"), button -> requestPage(view.page() - 1))
                    .bounds(left + 8, pageY, 20, 18).build();
            previous.active = view.page() > 0; addRenderableWidget(previous);
            var next = Button.builder(Component.literal(">"), button -> requestPage(view.page() + 1))
                    .bounds(left + panelWidth - 28, pageY, 20, 18).build();
            next.active = (view.page() + 1) * StoneTravel.PAGE_SIZE < view.total(); addRenderableWidget(next);
        }
        updateAffordability();
    }
    private void setEditing(boolean value) {
        editing = value; edit.visible = !value; nameField.visible = value; save.visible = value;
        nameField.setFocused(value);
        if (value) { setFocused(nameField); nameField.setHighlightPos(0); nameField.setCursorPosition(nameField.getValue().length()); }
    }
    private void updateSave() { save.active = !nameField.getValue().isBlank() && !nameField.getValue().strip().equals(view.sourceName()); }
    private void submitRename() {
        if (previewType.isEmpty() && editing && save.active) PacketDistributor.sendToServer(new StoneTravelPayloads.Rename(view.source(), nameField.getValue(), view.page()));
    }
    private void requestPage(int page) { if (previewType.isEmpty()) PacketDistributor.sendToServer(new StoneTravelPayloads.Page(view.source(), page)); }
    private void updateAffordability() {
        var player = Minecraft.getInstance().player;
        for (var row : destinations) row.active = player != null && player.isAlive() && row.cost().affordable();
    }
    @Override public void tick() { super.tick(); updateAffordability(); }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (editing && key == GLFW.GLFW_KEY_ESCAPE) { setEditing(false); return true; }
        if (editing && nameField.isFocused() && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) { submitRename(); return true; }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) { }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateAffordability();
        graphics.fill(0, 0, width, height, 0xcc101215);
        graphics.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, FRAME);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, BACKGROUND);
        inset(graphics, left + 8, top + 8, panelWidth - 16, 22);
        inset(graphics, left + 8, top + panelHeight - 30, panelWidth - 16, 22);
        if (!editing) {
            String name = ellipsize(font, view.sourceName(), panelWidth - 56);
            graphics.drawString(font, name, left + (panelWidth - font.width(name)) / 2, top + 15, TEXT, false);
        }
        if (view.total() > StoneTravel.PAGE_SIZE)
            graphics.drawCenteredString(font, (view.page() + 1) + "/" + ((view.total() + StoneTravel.PAGE_SIZE - 1) / StoneTravel.PAGE_SIZE),
                    left + panelWidth / 2, top + panelHeight - 47, MUTED);
        var signature = AttunementMark.fromKey(view.key()).component();
        graphics.drawString(font, signature, left + (panelWidth - font.width(signature)) / 2, top + panelHeight - 23, TEXT, false);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!editing && font.width(view.sourceName()) > panelWidth - 56 && mouseX >= left + 28 && mouseX < left + panelWidth - 28
                && mouseY >= top + 8 && mouseY < top + 30) graphics.renderTooltip(font, Component.literal(view.sourceName()), mouseX, mouseY);
    }
    private static void inset(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, INSET);
        graphics.fill(x, y, x + width, y + 1, SHADOW); graphics.fill(x, y, x + 1, y + height, SHADOW);
        graphics.fill(x, y + height - 1, x + width, y + height, EDGE); graphics.fill(x + width - 1, y, x + width, y + height, EDGE);
    }
    @Override public boolean isPauseScreen() { return false; }
    private final class EditButton extends Button {
        private EditButton(int x, int y) { super(x, y, 12, 12, Component.literal("✎"), button -> setEditing(true), DEFAULT_NARRATION); }
        @Override protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isHoveredOrFocused()) {
                graphics.fill(getX() - 2, getY() - 2, getX() + 14, getY() + 14, EDGE);
                if (isFocused()) graphics.renderOutline(getX() - 2, getY() - 2, 16, 16, MUTED);
            }
            graphics.drawCenteredString(font, "✎", getX() + 6, getY() + 2, isHoveredOrFocused() ? TEXT : MUTED);
        }
    }
    private final class DestinationButton extends Button {
        private final StoneTravelPayloads.Destination node;
        private DestinationButton(int x, int y, int width, int height, StoneTravelPayloads.Destination node) {
            super(x, y, width, height, Component.literal(node.name()).append(" · ").append(displayCost(node).description()),
                    button -> { if (button.active && previewType.isEmpty()) { PacketDistributor.sendToServer(new StoneTravelPayloads.Request(view.source(), node.id())); onClose(); } }, DEFAULT_NARRATION);
            this.node = node;
        }
        private TravelCostDisplay cost() { return displayCost(node); }
        @Override public void renderString(GuiGraphics graphics, Font font, int color) {
            var cost = cost(); int right = getX() + getWidth() - 7;
            int costX = right - cost.width(font), y = getY() + (getHeight() - 9) / 2;
            String name = ellipsize(font, node.name(), costX - getX() - 14);
            graphics.drawString(font, name, getX() + 7, y, color, true);
            cost.draw(graphics, font, right, y, color);
        }
    }
    private TravelCostDisplay displayCost(StoneTravelPayloads.Destination node) {
        if (previewType.isPresent()) {
            int index = view.destinations().indexOf(node);
            int amount = switch (previewType.get()) {
                case XP -> 12 + index * 6;
                case HEALTH -> 2 + index * 3;
                case MANA -> 10 + index * 10;
                case HUNGER -> 2 + index * 3;
            };
            // Illustration balances deliberately leave the last row unaffordable; no resource conversion is implied.
            return new TravelCostDisplay(previewType.get(), amount, index < 2);
        }
        var player = Minecraft.getInstance().player;
        var current = view.destinations().stream().filter(value -> value.id().equals(node.id())).findFirst().orElse(node);
        var type = switch (current.quote().route()) {
            case EXPERIENCE, ERUDITE -> TravelCostDisplay.Type.XP;
            case MANA -> TravelCostDisplay.Type.MANA;
            case HUNGER -> TravelCostDisplay.Type.HUNGER;
            case HEALTH -> TravelCostDisplay.Type.HEALTH;
        };
        return new TravelCostDisplay(type, current.quote().amount(), current.affordable() && current.quote().affordable(player));
    }
}
