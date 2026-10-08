package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.communication.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WhisperingShellTest {
    private static ItemStack shard() {
        var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        var ingredients = AttunementShardItem.ingredients(); var nodes = new ArrayList<RitualInputs.Node>();
        for (int i = 0; i < 8; i++) nodes.add(new RitualInputs.Node(i, geometry.offset(i), i < ingredients.size()
                ? new ItemStack(BuiltInRegistries.ITEM.get(ingredients.get(i))) : ItemStack.EMPTY, ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(geometry, nodes));
    }
    @Test void bindingRetainsVerifiedBlueprintDespiteNamesAndRejectsTampering() {
        var shard = shard(); var shell = WhisperingShellItem.bound(shard);
        assertEquals(AttunementShardItem.signature(shard), WhisperingShellItem.signature(shell));
        CustomData.update(DataComponents.CUSTOM_DATA, shard, tag -> tag.putString("incidental", "not a blueprint field"));
        var bounded = WhisperingShellItem.bound(shard).get(DataComponents.CUSTOM_DATA).copyTag();
        assertFalse(bounded.getCompound("attunement").contains("incidental"));
        shell.set(DataComponents.CUSTOM_NAME, Component.literal("A friend's shell"));
        assertEquals(AttunementShardItem.signature(shard).orElseThrow().key(), WhisperingShellItem.key(shell).orElseThrow());
        CustomData.update(DataComponents.CUSTOM_DATA, shell, tag -> tag.getCompound("attunement").putString("key", "a".repeat(64)));
        assertTrue(WhisperingShellItem.key(shell).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> WhisperingShellItem.bound(new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get())));
    }
    @Test void onlyWholeInnerRotationsWorkAndWrongOrInvalidOfferingsReject() {
        var ingredients = List.of(shard(), new ItemStack(Items.SCULK_SENSOR), new ItemStack(Items.NAUTILUS_SHELL));
        for (int a = 0; a < 8; a++) for (int b = 0; b < 8; b++) for (int c = 0; c < 8; c++) {
            if (a == b || a == c || b == c) continue;
            var seats = new ArrayList<>(Collections.nCopies(8, ItemStack.EMPTY));
            seats.set(a, ingredients.get(0)); seats.set(b, ingredients.get(1)); seats.set(c, ingredients.get(2));
            var result = WhisperingShellRecipe.result(seats);
            boolean valid = (a & 1) == 0 && b == (a + 2) % 8 && c == (a + 4) % 8;
            assertEquals(valid, result.isPresent(), a + "/" + b + "/" + c);
            if (valid) {
                assertEquals(AttunementShardItem.signature(ingredients.getFirst()).orElseThrow().key(), WhisperingShellItem.key(result.orElseThrow()).orElseThrow());
                seats.set((a + 6) % 8, new ItemStack(Items.PAPER));
                assertTrue(WhisperingShellRecipe.result(seats).isEmpty());
            }
        }
        var seats = new ArrayList<>(List.of(shard(), ItemStack.EMPTY, new ItemStack(Items.SCULK_SENSOR), ItemStack.EMPTY,
                new ItemStack(Items.NAUTILUS_SHELL), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
        seats.set(2, new ItemStack(Items.SCULK)); assertTrue(WhisperingShellRecipe.result(seats).isEmpty());
        seats.set(2, new ItemStack(Items.SCULK_SENSOR)); seats.set(0, new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()));
        assertTrue(WhisperingShellRecipe.result(seats).isEmpty());
        var display = com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays.whisperingShell();
        assertEquals(3, display.offerings().size()); assertEquals(4, display.capacity()); assertFalse(display.shapeless());
        assertTrue(display.output().is(ScrollItems.WHISPERING_SHELL.get()));
    }
    @Test void nineHotbarSeatsAndOffhandTransmitWhileMainInventoryOnlyListens() {
        var inventory = new Inventory(null); var shell = WhisperingShellItem.bound(shard());
        for (int slot = 0; slot < 9; slot++) {
            inventory.clearContent(); inventory.selected = (slot + 1) % 9; inventory.setItem(slot, shell);
            assertTrue(WhisperingShellChat.transmitting(inventory).active());
            assertEquals(1, WhisperingShellChat.transmitting(inventory).keys().size());
        }
        inventory.clearContent(); inventory.offhand.set(0, shell);
        assertEquals(1, WhisperingShellChat.transmitting(inventory).keys().size());
        inventory.clearContent(); inventory.setItem(9, shell);
        assertFalse(WhisperingShellChat.transmitting(inventory).active()); assertEquals(1, WhisperingShellChat.listening(inventory).size());
        inventory.setItem(0, shell); inventory.setItem(1, shell.copy()); assertEquals(1, WhisperingShellChat.transmitting(inventory).keys().size());
    }
    @Test void invalidActiveShellFailsClosedAndNestedShellsDoNotParticipate() {
        var invalid = new ItemStack(ScrollItems.WHISPERING_SHELL.get()); var route = WhisperingShellChat.transmitting(List.of(invalid));
        assertTrue(route.active()); assertTrue(route.keys().isEmpty()); assertTrue(WhisperingShellChat.listening(List.of(invalid)).isEmpty());
        var bundle = new ItemStack(Items.BUNDLE);
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(WhisperingShellItem.bound(shard()))));
        assertFalse(WhisperingShellChat.transmitting(List.of(bundle)).active()); assertTrue(WhisperingShellChat.listening(List.of(bundle)).isEmpty());
    }
    @Test void matchingDisplayMarksNeverSubstituteForMatchingFullKeysAndSharedChannelsAreStable() {
        String a = "a".repeat(64), b = a.substring(0, 10) + "b" + a.substring(11);
        assertEquals(AttunementMark.fromKey(a), AttunementMark.fromKey(b));
        var route = new WhisperingShellChat.Transmission(true, Set.of(a));
        assertTrue(WhisperingShellChat.shared(route, Set.of(b)).isEmpty());
        assertEquals(List.of(a, b), WhisperingShellChat.shared(new WhisperingShellChat.Transmission(true, Set.of(b, a)), Set.of(a, b)));
        var symbol = AttunementMark.fromKey(a).runes().getFirst().component();
        assertEquals(symbol, AttunementMark.fromKey(a).symbol());
        var display = WhisperingShellChat.channelMarks(List.of(a, b));
        assertEquals(symbol.getString() + " & " + symbol.getString(), display.getString());
        assertEquals(symbol.getStyle(), display.getSiblings().getFirst().getStyle());
        assertEquals(net.minecraft.ChatFormatting.GRAY.getColor().intValue(), display.getSiblings().get(1).getStyle().getColor().getValue());
        assertEquals(net.minecraft.resources.ResourceLocation.withDefaultNamespace("default"), display.getSiblings().get(1).getStyle().getFont());
    }
    @Test void cueIsBoundedAndCustomTooltipIsOnlyTheSharedSignature() {
        assertThrows(IllegalArgumentException.class, () -> new ShellCuePayload(false, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ShellCuePayload(false, List.of("short")));
        assertThrows(IllegalArgumentException.class, () -> new ShellCuePayload(false, List.of("a".repeat(64), "a".repeat(64))));
        var shell = WhisperingShellItem.bound(shard());
        for (var flag : List.of(TooltipFlag.NORMAL, TooltipFlag.ADVANCED)) {
            var custom = new ArrayList<Component>(); shell.getItem().appendHoverText(shell, Item.TooltipContext.EMPTY, custom, flag);
            assertEquals(List.of(AttunementMark.fromKey(WhisperingShellItem.key(shell).orElseThrow()).component()), custom);
        }
    }
    @Test void cueCodecRoundTripsAndRejectsOversizedCountsBeforeReadingKeys() {
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), net.minecraft.core.RegistryAccess.EMPTY);
        try {
            var cue = new ShellCuePayload(true, List.of("a".repeat(64), "b".repeat(64)));
            ShellCuePayload.CODEC.encode(buffer, cue); assertEquals(cue, ShellCuePayload.CODEC.decode(buffer));
            buffer.clear(); buffer.writeBoolean(false); buffer.writeVarInt(11);
            assertThrows(IllegalArgumentException.class, () -> ShellCuePayload.CODEC.decode(buffer));
        } finally { buffer.release(); }
    }
}
