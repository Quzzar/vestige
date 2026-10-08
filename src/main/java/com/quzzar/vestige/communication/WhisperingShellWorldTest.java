package com.quzzar.vestige.communication;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.*;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.gametest.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Real server packet listeners/player-list broadcasts, with inspectable in-memory client connections. */
@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WhisperingShellWorldTest {
    public static ItemStack shard(Item material) {
        var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        var ingredients = AttunementShardItem.ingredients(); var nodes = new ArrayList<RitualInputs.Node>();
        for (int i = 0; i < 8; i++) nodes.add(new RitualInputs.Node(i, geometry.offset(i), i < ingredients.size()
                ? new ItemStack(BuiltInRegistries.ITEM.get(ingredients.get(i))) : ItemStack.EMPTY,
                i == 0 ? new ItemStack(material) : ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(geometry, nodes));
    }
    private static RitualCrafting.Layout ritual(GameTestHelper h) {
        var center = new BlockPos(4,1,4); h.setBlock(center, ApparatusBlocks.SPELLSTONE.get());
        var geometry = new LeylineShaping.Geometry(4, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.CROSS, 1, 0);
        for (int i : List.of(0,2,4,6)) h.setBlock(center.offset(geometry.offset(i)), ApparatusBlocks.PLINTH.get());
        var layout = RitualCrafting.layout((OfferingBlockEntity) h.getBlockEntity(center));
        layout.stands().get(0).insert(shard(Items.COPPER_BLOCK));
        layout.stands().get(2).insert(new ItemStack(Items.SCULK_SENSOR, 2));
        layout.stands().get(4).insert(new ItemStack(Items.NAUTILUS_SHELL, 2)); return layout;
    }
    @GameTest(template="empty_9x3x9", batch="shell_craft", timeoutTicks=80)
    public static void ritualConsumesThreeOfferingsCopiesBlueprintAndRetainsSockets(GameTestHelper h) {
        var layout = ritual(h); var signature = AttunementShardItem.signature(layout.items().get(0)).orElseThrow();
        layout.stands().get(6).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Shell ritual rejected");
        h.runAfterDelay(45, () -> {
            var result = RitualTestOutput.stack(layout.center());
            h.assertTrue(WhisperingShellItem.signature(result).orElseThrow().equals(signature), "Craft rehashed or lost the blueprint");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty), "Wrong consumption");
            h.assertTrue(layout.stands().get(6).materialItem().is(Items.GOLD_BLOCK), "Empty seat socket was consumed"); h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9", batch="shell_craft_cancel", timeoutTicks=80)
    public static void breakingTheEmptyInnerSeatCancelsWithoutConsumption(GameTestHelper h) {
        var layout = ritual(h);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Shell ritual rejected");
        h.runAfterDelay(10, () -> h.getLevel().destroyBlock(layout.stands().get(6).getBlockPos(), false));
        h.runAfterDelay(50, () -> {
            h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().get(0).getCount()==1
                    && layout.items().get(2).getCount()==1 && layout.items().get(4).getCount()==1, "Canceled shell craft partially committed"); h.succeed();
        });
    }
    public static final class RecordingConnection extends Connection {
        public final List<Packet<?>> packets = new ArrayList<>();
        public RecordingConnection() { super(PacketFlow.SERVERBOUND); new EmbeddedChannel(this); }
        @Override public void send(Packet<?> packet, PacketSendListener listener, boolean flush) { packets.add(packet); }
    }
    public static final class ChatPlayer extends ServerPlayer implements AutoCloseable {
        public final RecordingConnection wire;
        public final List<CompletableFuture<FilteredText>> pending = new ArrayList<>();
        public boolean delay;
        private final TextFilter filter = new TextFilter() {
            public void join() { } public void leave() { }
            public CompletableFuture<FilteredText> processStreamMessage(String text) {
                if (!delay) return CompletableFuture.completedFuture(FilteredText.passThrough(text));
                var result = new CompletableFuture<FilteredText>(); pending.add(result); return result;
            }
            public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> texts) { return TextFilter.DUMMY.processMessageBundle(texts); }
        };
        public ChatPlayer(ServerLevel level, String name) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), name), ClientInformation.createDefault());
            wire = new RecordingConnection();
            net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateCommonChannels(wire, ConnectionProtocol.PLAY)
                    .add(ShellCuePayload.TYPE.id());
            var cookie = CommonListenerCookie.createInitial(getGameProfile(), false);
            level.getServer().getPlayerList().placeNewPlayer(wire, this, cookie); setGameMode(GameType.SURVIVAL); wire.packets.clear();
        }
        @Override public TextFilter getTextFilter() { return filter; }
        public void options(ChatVisiblity visibility, boolean filtering) {
            updateOptions(new ClientInformation("en_us", 2, visibility, true, 0, HumanoidArm.RIGHT, filtering, true));
        }
        public void speak(String text) { connection.handleChat(new ServerboundChatPacket(text, Instant.now(), 0, null, new LastSeenMessages.Update(0, new BitSet(20)))); }
        public List<ClientboundPlayerChatPacket> chat() { return wire.packets.stream().filter(ClientboundPlayerChatPacket.class::isInstance).map(ClientboundPlayerChatPacket.class::cast).toList(); }
        public long cues() { return wire.packets.stream().filter(p -> p instanceof ClientboundCustomPayloadPacket packet && packet.payload() instanceof ShellCuePayload).count(); }
        @Override public void close() { getServer().getPlayerList().remove(this); discard(); }
    }
    @GameTest(template="empty_9x3x9", batch="shell_chat", timeoutTicks=100)
    public static void packetChatDeduplicatesChannelsReachesAnotherDimensionAndRestoresPublicSpeech(GameTestHelper h) {
        var shellA = WhisperingShellItem.bound(shard(Items.COPPER_BLOCK)); var shellB = WhisperingShellItem.bound(shard(Items.GOLD_BLOCK));
        var sender = new ChatPlayer(h.getLevel(), "ShellSender"); var peer = new ChatPlayer(h.getLevel().getServer().getLevel(Level.NETHER), "ShellPeer");
        var outsider = new ChatPlayer(h.getLevel(), "ShellOutsider");
        sender.getInventory().setItem(8, shellA); sender.getInventory().setItem(7, shellB); sender.getInventory().setItem(6, shellA.copy());
        peer.getInventory().setItem(20, shellA.copy()); peer.getInventory().setItem(21, shellB.copy()); peer.setPos(200000, 100, 200000);
        sender.speak("shell-private");
        h.runAfterDelay(5, () -> {
            h.assertTrue(sender.chat().size()==1 && peer.chat().size()==1 && outsider.chat().isEmpty(), "Private message duplicated or leaked");
            var channelKeys = new TreeSet<>(List.of(WhisperingShellItem.key(shellA).orElseThrow(), WhisperingShellItem.key(shellB).orElseThrow()));
            var marks = String.join(" & ", channelKeys.stream().map(key -> AttunementMark.fromKey(key).symbol().getString()).toList());
            h.assertTrue(peer.chat().getFirst().chatType().chatType().is(WhisperingShellChat.SHELL_CHAT)
                    && peer.chat().getFirst().chatType().name().getString().equals(marks + " > ShellSender"), "Native chat lost compact shared symbols or sender");
            h.assertTrue(sender.cues()==1 && peer.cues()==1 && outsider.cues()==0, "Cue count differs from actual delivery");
            sender.getInventory().clearContent(); sender.getInventory().setItem(15, shellA); sender.speak("ordinary-public");
        });
        h.runAfterDelay(10, () -> {
            h.assertTrue(outsider.chat().size()==1 && outsider.chat().getFirst().body().content().equals("ordinary-public"), "Putting Shell away did not restore ordinary chat");
            sender.getInventory().setItem(0, new ItemStack(ScrollItems.WHISPERING_SHELL.get())); sender.speak("invalid-private");
        });
        h.runAfterDelay(15, () -> {
            try { h.assertTrue(outsider.chat().size()==1 && peer.chat().size()==2, "Invalid Shell fell through to public chat"); h.succeed(); }
            finally { sender.close(); peer.close(); outsider.close(); }
        });
    }
    @GameTest(template="empty_9x3x9", batch="shell_filter", timeoutTicks=100)
    public static void queuedSpeechKeepsItsRouteAndOrderingAndHonorsCancellationFilteringAndVisibility(GameTestHelper h) {
        var shell = WhisperingShellItem.bound(shard(Items.COPPER_BLOCK));
        var sender = new ChatPlayer(h.getLevel(), "FilterSender"); var peer = new ChatPlayer(h.getLevel(), "FilterPeer"); var outsider = new ChatPlayer(h.getLevel(), "FilterOutsider");
        sender.getInventory().setItem(0, shell); peer.getInventory().setItem(9, shell.copy()); peer.options(ChatVisiblity.FULL, true);
        sender.delay = true; sender.speak("first"); sender.speak("second");
        h.runAfterDelay(5, () -> {
            h.assertTrue(sender.pending.size()==2, "Packets did not reach the native text filter");
            sender.getInventory().clearContent(); sender.getInventory().setItem(10, shell);
            sender.pending.get(1).complete(FilteredText.passThrough("second"));
            h.assertTrue(peer.chat().isEmpty(), "Filter completions reordered conversation");
            sender.pending.get(0).complete(FilteredText.passThrough("first"));
        });
        h.runAfterDelay(10, () -> {
            h.assertTrue(peer.chat().stream().map(p -> p.body().content()).toList().equals(List.of("first", "second")) && outsider.chat().isEmpty(), "Queued private speech changed route/order");
            sender.getInventory().setItem(0, shell); sender.speak("fully-filtered");
        });
        h.runAfterDelay(15, () -> sender.pending.get(2).complete(FilteredText.fullyFiltered("fully-filtered")));
        h.runAfterDelay(20, () -> {
            h.assertTrue(peer.chat().size()==2 && peer.cues()==2, "Fully filtered recipient got content or cue");
            peer.options(ChatVisiblity.HIDDEN, false); sender.delay = false; sender.speak("hidden-client");
        });
        h.runAfterDelay(25, () -> {
            h.assertTrue(peer.chat().size()==2 && peer.cues()==2, "Hidden-chat client got conversation/cue");
            java.util.function.Consumer<ServerChatEvent> cancel = event -> { if (event.getPlayer()==sender) event.setCanceled(true); };
            NeoForge.EVENT_BUS.addListener(cancel);
            peer.options(ChatVisiblity.FULL, false); sender.speak("canceled");
            h.runAfterDelay(5, () -> {
                try { h.assertTrue(peer.chat().size()==2 && outsider.chat().isEmpty(), "Other handler cancellation bypassed"); h.succeed(); }
                finally { NeoForge.EVENT_BUS.unregister(cancel); sender.close(); peer.close(); outsider.close(); }
            });
        });
    }
    @GameTest(template="empty_9x3x9", batch="shell_signed", timeoutTicks=40)
    public static void signedMessageBodyAndSignatureReachMatchingClientsWithoutSystemTextConversion(GameTestHelper h) {
        try (var sender = new ChatPlayer(h.getLevel(), "SignedSender"); var peer = new ChatPlayer(h.getLevel(), "SignedPeer"); var outsider = new ChatPlayer(h.getLevel(), "SignedOutsider")) {
            var shell = WhisperingShellItem.bound(shard(Items.COPPER_BLOCK)); sender.getInventory().setItem(0, shell); peer.getInventory().setItem(9, shell.copy());
            byte[] signatureBytes = new byte[256]; new Random(42).nextBytes(signatureBytes); var signature = new MessageSignature(signatureBytes);
            var body = new SignedMessageBody("signed-body", Instant.now(), 731, LastSeenMessages.EMPTY);
            var message = new PlayerChatMessage(new SignedMessageLink(0, sender.getUUID(), UUID.randomUUID()), signature, body, Component.literal("decorated body"), FilterMask.PASS_THROUGH);
            h.getLevel().getServer().getPlayerList().broadcastChatMessage(message, sender, ChatType.bind(ChatType.CHAT, sender));
            h.assertTrue(peer.chat().size()==1 && peer.chat().getFirst().signature().equals(signature)
                    && peer.chat().getFirst().body().content().equals(body.content()) && outsider.chat().isEmpty(), "Signed payload replaced or leaked");
            h.assertTrue(peer.chat().getFirst().chatType().chatType().is(WhisperingShellChat.SHELL_CHAT), "Signed player chat did not use native Shell decoration");
            h.succeed();
        }
    }
    @GameTest(template="empty_9x3x9", batch="shell_spam", timeoutTicks=40)
    public static void privateMessagesStillSpendVanillaSpamBudget(GameTestHelper h) {
        var sender = new ChatPlayer(h.getLevel(), "SpamSender"); sender.getInventory().setItem(0, WhisperingShellItem.bound(shard(Items.COPPER_BLOCK)));
        for (int i=0; i<11; i++) sender.speak("shell-spam-"+i);
        h.runAfterDelay(5, () -> {
            try { h.assertTrue(sender.wire.packets.stream().anyMatch(ClientboundDisconnectPacket.class::isInstance), "Private messages bypassed vanilla spam disconnect"); h.succeed(); }
            finally { sender.close(); }
        });
    }
}
