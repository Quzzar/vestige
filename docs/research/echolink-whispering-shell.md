# EchoLink and Whispering Shell

**Status:** primary-source research, checked October 6, 2026. EchoLink behavior below is upstream evidence. Recommendations are a first Vestige design iteration, not accepted recipe, art, chat policy or gameplay behavior. No EchoLink code or assets were copied into the native runtime, and the archived plugin was not run.

**Source:** the owner's [Quzzar/echolink](https://github.com/Quzzar/echolink), pinned to [commit `3995850297e893809bcd79efb715d02d65deb9e2`](https://github.com/Quzzar/echolink/commit/3995850297e893809bcd79efb715d02d65deb9e2), recorded September 29, 2023. The Maven project is EchoLink 1.1.0, uses Java 8 source/target and Paper API 1.20.1; the plugin descriptor declares Bukkit API 1.20. [Project configuration][echo-pom], [plugin descriptor][echo-plugin]. It is the newer rework of the owner's RPChat, already examined in [Sending Shell research](legacy-radio-attunement.md).

## What EchoLink actually does

| Concern | Pinned upstream behavior | Primary source |
| --- | --- | --- |
| Transmitting slots | Every EchoLink in the nine hotbar slots, offhand or helmet slot participates. The selected hotbar slot does not matter. Stacks are cloned and normalized to amount one. | [Sender scan][echo-scan] |
| Listening slots | The recipient scan reads the complete player inventory contents and normalizes stack amounts. It does not search a chest, Ender Chest or items inside another container. | [Recipient scan][echo-recipients] |
| Normal chat | If the sender has any active EchoLink, the listener cancels ordinary chat and routes its text only to matching EchoLink holders. It does not also deliver a local copy. Without an active item, configurable local chat takes over; with local chat disabled, ordinary chat proceeds. | [Chat listener][echo-chat] |
| Multiple channels | All active items transmit together. Each qualifying player receives one message, prefixed with that player's shared frequency glyphs. Duplicate copies of the same item do not create duplicate recipient messages. | [Chat listener][echo-chat], [unique item scan][echo-unique] |
| Self delivery | The sender is included by the same online-player inventory scan, so their transmitted conversation appears back to them. | [Recipient scan][echo-recipients] |
| Distance and dimensions | The EchoLink route has no distance or world check. It therefore reaches matching online holders across worlds; the distance/concealment rules belong to local chat instead. | [Chat listener][echo-chat], [recipient scan][echo-recipients], [local processor][echo-local] |
| Offline players | Only currently online players are scanned. There is no message queue, mailbox or replay storage in the route. | [Recipient scan][echo-recipients], [chat listener][echo-chat] |
| Costs | Transmission does not spend durability, hunger, XP, health or another resource. | [Chat listener][echo-chat] |

The inventory distinction is the strongest reusable interaction: a device put away can listen without making all outgoing speech broadcast, while a device deliberately moved into an active slot transmits. The precise native slots remain a Vestige decision. The original head-slot support follows from using a wearable player head; it is not evidence that a shell sprite should also be wearable.

### Recipe and item presentation

The default recipe is shapeless and consumes **Copper Block, Iron Bars, Lightning Rod, Redstone Torch, Redstone Dust, Nautilus Shell, Emerald, Amethyst Shard and Echo Shard**, one of each, yielding **one EchoLink**. The result uses the one-head frame created by `new ItemStack(Material.PLAYER_HEAD)`. Configuration can supply one through nine materials. The craft listener writes the frequency when the result is picked up, permits only `PICKUP_ALL`, and cancels other inventory actions such as bulk crafting. [Default recipe][echo-config-item], [recipe registration][echo-recipe], [item frame][echo-frame], [craft listener][echo-craft].

The output is a **Player Head** named EchoLink. Its appearance comes from a configurable `textures.minecraft.net` skin URL stored through a Paper skull profile; it is not a packaged native item sprite. The item has a frequency lore line, and placing it as a block is canceled. This research inspected the construction and configured URL, not the texture's rendered appearance. [Item frame][echo-frame], [block placement handler][echo-place], [default configuration][echo-config-item].

### Channel identity

EchoLink improves on RPChat's single-letter ingredient abbreviations: its crafting serializer visits every matrix position and writes the **full Bukkit material name**, with a separate empty-slot marker. Ingredient position matters, but amount, name and other ingredient metadata do not enter that serialization. It takes the Java string hash, uses the same integer to select a formatting/color code and a Unicode character, and stores only that resulting styled glyph in lore. It does **not** retain the matrix or a complete hash in a dedicated component, and it does not allocate channels through RPChat's persistent random registry. [Frequency construction][echo-frequency].

Transmission recognizes the item frame while ignoring lore. Recipient matching then compares complete item stacks, including their metadata and frequency lore, after normalizing amount. The displayed glyph is therefore part of the actual channel identity, and other cosmetic metadata can also affect eligibility or matching. [Template recognition][echo-recognition], [sender scan][echo-scan], [recipient scan][echo-recipients].

Vestige already has the replacement for this entire identity mechanism: a verified full SHA-256 [Attunement Shard key](../design/attunement-shards.md). Whispering Shells should inherit that key unchanged, as specified in [attuned devices](../design/attuned-devices.md). Device ingredients must not hash a second channel, and a displayed rune must not become the comparison key.

### Local chat, commands and feedback

Local chat is enabled by default. The normal preset has `base_range=400` and `extended_conceal_range=150`; shout uses 800/200 and whisper uses 50/25. The actual code starts concealment at `base_range - extended_conceal_range` and stops delivery beyond `base_range + extended_conceal_range`. Thus the normal preset is fully clear below 250 blocks, partially concealed from 250 through 550, and absent farther away. These are deductions from the processor, not the configuration comments' advertised interpretation. Local recipients must share the sender's world. [Local defaults][echo-config-local], [distance and concealment logic][echo-local].

The plugin also owns `/shout`, `/s`, `/whisper`, `/w`, `/tell` and `/staff`, plus permission-based rank prefixes. Shout and whisper call the local processors directly, so an active EchoLink does not redirect those commands. Tell and staff are separate direct-message/staff routes. These belong to a server chat overhaul; none is necessary to make a native linked shell work. [Registration][echo-enable], [command descriptor][echo-plugin], [shout][echo-shout], [whisper][echo-whisper], [tell][echo-tell], [staff][echo-staff].

Conversation is sent as formatted text containing frequency marks, the sender's display name, optional rank prefix and the message. The device route has no send/receive sound, particle, use animation or delivery acknowledgment. Local debug mode can emit range/chance text and a suggestion when nobody hears the sender; commands emit usage and permission errors. [Message formatting][echo-format], [chat route][echo-chat], [local debug feedback][echo-local], [command implementations][echo-shout].

Vestige's visual/audio-only device feedback rule concerns **instructions, status and errors**. The player's actual transmitted conversation is the communication feature. It can remain readable conversation while activation, reception and failure cues use sound/visuals, with no automatic “connected,” “no listeners” or resource-error chat/actionbar text. This distinction follows the owner's current device/UI instructions; it is not an upstream EchoLink feature.

## Source-level limitations to avoid

These are deductions from inspection, not failures reproduced by running EchoLink:

- **Different recipes can merge.** A matrix is reduced first to a Java integer hash and then to a styled single-character label. Neither comparison nor the item retains the original inputs, so a collision can connect recipes that were intended to differ. The startup frequency count is an upper-bound calculation, not an enumeration of actual unique channel identities. [Serializer][echo-frequency], [frequency estimate][echo-estimate].
- **Cosmetic changes can break connections.** Template and recipient checks depend on item metadata rather than an independent device ID and channel component. A renamed item or a changed configured head texture can cease to match the current template; adding unrelated lore can change equality between peers. Changing the Unicode configuration can make newly crafted items differ from old items made with the same arrangement. [Recognition][echo-recognition], [frame][echo-frame], [frequency construction][echo-frequency].
- **Malformed data is not guarded.** `getFreqFromItem` directly reads the first lore line. A template-matching item with no lore can reach that read and throw. An empty Unicode list can cause modulo-by-zero/index failure while crafting. [Frequency read and construction][echo-frequency], [Unicode list construction][echo-unicode].
- **The glyph pool is not font-safe.** Configured ranges include combining characters and supplementary code points, but the builder casts each code point to a Java `char`. Supplementary values are truncated instead of encoded as full code points, and there is no Minecraft glyph whitelist. [Unicode builder][echo-unicode], [Unicode defaults][echo-config-unicode].
- **Async access needs review.** The listener uses Paper's `AsyncChatEvent`, immediately reads all player inventories and sends messages, and does not marshal those operations onto the server thread. This is an evident concurrency risk, not a claim that every invocation is asynchronous or that a crash was observed. [Listener][echo-chat], [inventory scans][echo-scan].
- **Other chat handlers can be bypassed.** The listener has no cancellation check or `ignoreCancelled` option, and its EchoLink route builds recipients from all online players rather than the event's viewers. It flattens the message to `TextComponent.content()`, drops component children/style, and manually sends strings. There is no explicit conversation logging call in that route. Canceled-event policy, recipient exclusions, rich content, secure chat and moderation integration need explicit native treatment. [Listener][echo-chat], [string delivery][echo-format].

## Recommended Vestige direction

These are proposals for iteration, not accepted decisions:

| Keep | Adapt or drop |
| --- | --- |
| Matching items form a many-person channel. | Compare the complete verified attunement key, independently of names, runes, stack counts or art. |
| A put-away item listens; an item moved into an active slot transmits. | Start with hotbar and offhand activation as the closest precedent. Omit head wear unless the owner wants it as equipment. A held-only variant is a deliberate alternative if broadcasting from any hotbar seat feels too easy to trigger. |
| One message can reach several matching listeners. | Deduplicate receivers. Decide explicitly whether several active keys broadcast together or only the held shell transmits. Keep a visible channel mark on conversation if multiple keys are enabled. |
| Long-distance, cross-dimension conversation is an established precedent. | Propose unlimited distance across available dimensions, online only, with no stored messages for the first version. Range limits should be a new gameplay decision, not inherited local-chat settings. |
| Channel access can be reproduced by repeating the attunement craft. | Treat a key as shared access, not ownership or secrecy. Recreating or acquiring a valid key grants listening access. |
| A shell supplies an understandable physical vessel for a voice. | Use newly authored native shell art rather than the player-head URL, and reuse Vestige's existing attunement marks/glint where appropriate. |

The old nine-ingredient recipe combined the tuning mechanism and physical communicator. In Vestige, the Attunement Shard already supplies the tuning and costly Echo/Amethyst/inscription ingredients. A smaller shell ritual can concentrate on the shell vessel, sound/resonance and binding. **Nautilus Shell should be the strongest recipe candidate**; the remaining offerings should be chosen with the existing four-inner-Plinth device conventions and pinned [material reference](iron-material-uses.md) in view. Copper/Amethyst or a Note Block are possible native flavor candidates, not accepted bonuses or upstream requirements.

For the first art study, propose an ivory nautilus spiral, a dark opening and a restrained Echo-colored inner seam, keeping the silhouette readable at 16×16. A narrow copper binding or a small crystal accent can connect the sprite to whichever recipe is selected. This is a newly authored texture direction; the old head skin was not visually verified and should not constrain it.

The largest open gameplay decision is **how ordinary speech interacts with active shells**. EchoLink's exact precedent is replacement: hotbar/offhand/head items divert ordinary chat, and putting them away restores the other chat route. Keeping this gives a simple inventory gesture, but broad hotbar activation also makes accidental group broadcasts possible. A selected-hand transmission rule reduces that risk and makes choosing among channels more explicit. Either version should leave server chat policy outside the Shell's scope; introducing proximity chat, concealment, shout, staff chat or rank styling would be separate work.

## Native compatibility constraint

The active project baseline is Minecraft 1.21.1, NeoForge 21.1.72 and Java 21. EchoLink's Bukkit inventories, skull profiles, Paper event and permission commands cannot execute as the native mechanism. Use native registered items, verified attunement components, server-owned inventory reads and the native chat pipeline. [Vestige baseline](../../gradle.properties), [EchoLink dependencies][echo-pom].

NeoForge 21.1.72 supplies a cancellable logical-server `ServerChatEvent` on the game event bus, exposing player, raw text and decorated component. Its default event policy skips canceled events unless a handler explicitly opts in. [Pinned 21.1.72 sources artifact](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.72/neoforge-21.1.72-sources.jar), `net/neoforged/neoforge/event/ServerChatEvent.java`; [NeoForge event documentation](https://docs.neoforged.net/docs/1.21.1/concepts/events/).

**Do not assume that canceling this event and immediately sending raw text reproduces normal player chat.** Inspection of the pinned userdev patch and generated Minecraft sources shows the event/decorator runs before the asynchronous text filter completes. Cancellation causes the queued completion to return before normal broadcast. In that baseline, the normal broadcast path also performs chat logging and rate-spam accounting, and recipient delivery uses signed/filtered chat objects. A native design must intentionally preserve or replace those responsibilities, respect cancellation and client chat settings, and avoid presenting raw unfiltered strings as equivalent secure player chat. [Pinned 21.1.72 userdev artifact](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.72/neoforge-21.1.72-userdev.jar), `patches/net/minecraft/server/network/ServerGamePacketListenerImpl.java.patch`; locally inspected `build/moddev/artifacts/neoforge-21.1.72-minecraft-sources.jar`, `ServerGamePacketListenerImpl.handleChat`, `tryHandleChat`, `broadcastChatMessage` and `PlayerList.broadcastChatMessage`.

Before calling implementation complete, test sender/listener slot changes, full-key mismatch despite matching displayed runes, duplicate items/channels, cross-dimension recipients, offline recipients, renamed/malformed items, cancellation by another chat handler, filtering/client visibility, ordering and logging/spam behavior. Verify actual shell appearance and audio cues in the client. This research does not certify those behaviors.

[echo-pom]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/pom.xml
[echo-plugin]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/resources/plugin.yml
[echo-chat]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/ChatListener.java#L17-L70
[echo-recognition]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Utils.java#L26-L44
[echo-format]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Utils.java#L59-L80
[echo-scan]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Utils.java#L90-L121
[echo-recipients]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Utils.java#L123-L139
[echo-unique]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Utils.java#L141-L156
[echo-frame]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Utils.java#L158-L201
[echo-frequency]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Utils.java#L204-L241
[echo-unicode]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/UnicodeHandler.java#L16-L59
[echo-config-item]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/resources/config.yml#L69-L101
[echo-config-local]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/resources/config.yml#L17-L38
[echo-config-unicode]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/resources/config.yml#L103-L382
[echo-recipe]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Echolink.java#L93-L119
[echo-enable]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Echolink.java#L39-L83
[echo-estimate]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/Echolink.java#L164-L189
[echo-craft]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/RecipeListener.java#L12-L28
[echo-place]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/RecipeListener.java#L30-L35
[echo-local]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/localchat/ChatProcessor.java#L23-L112
[echo-shout]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/commands/ShoutCommand.java
[echo-whisper]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/commands/WhisperCommand.java
[echo-tell]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/commands/TellCommand.java
[echo-staff]: https://github.com/Quzzar/echolink/blob/3995850297e893809bcd79efb715d02d65deb9e2/src/main/java/com/quzzar/echolink/commands/StaffCommand.java
