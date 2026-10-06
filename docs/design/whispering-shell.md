# Whispering Shell: first iteration

**Status: design iteration, October 6, 2026.** The name **Whispering Shell** and inheritance of the complete verified Attunement Shard key are accepted in [attuned devices](attuned-devices.md). The owner selected **any shell in the hotbar transmits, like EchoLink**, then requested a simpler recipe of **Nautilus Shell + Attunement Shard + one whispery/dark ingredient**, without extra Amethyst or required Copper. The owner likes the sculk ingredient direction. The earlier hollow-conch art selection is superseded: the owner chose **Shell A, Coiled spiral**, from the fresh forms as the basis for refinement, with slightly less fine detail. The item should have its own dark, eerie appearance rather than a literal ingredient illustration. **Sculk Sensor** is the current recommended third ingredient, pending selection. Offhand support, multiple-channel details, range, costs and final texture approval remain proposals. Research and appearance studies exist; the Shell is not yet registered and chat gameplay remains unimplemented.

## Experience

Make matching shells, give one to a friend, carry yours to hear them, and keep yours in the hotbar to talk back. The selected hotbar slot does not matter, leaving the main hand available for exploring or building. Communication is a live conversation between online players carrying matching keys.

The reference is the owner's EchoLink, pinned to `3995850297e893809bcd79efb715d02d65deb9e2`. Its actual implementation transmits from every hotbar, offhand and head shell, receives from player inventory, replaces ordinary chat while active, and works across worlds without distance checks. The [source review](../research/echolink-whispering-shell.md) records exact recipe, identity, commands and limitations. These facts are distinct from the native choices below.

## Proposed recipe

Three offerings among the **four inner Plinths**, in any order, with **one inner offering surface empty**:

| Quantity | Offering | Native construction role |
| --- | --- | --- |
| 1 | Valid Attunement Shard | Supplies the complete existing channel identity. |
| 1 | Nautilus Shell | Physical vessel for a voice; its ordinary beige appearance does not constrain the result. |
| 1 | Sculk Sensor (proposed) | Vibration-sensitive material for the communication mechanism. |

Produce **one Whispering Shell**, consuming one of each offering. Keep all four inner Plinths; this is a four-slot recipe containing one empty slot, not a new three-Plinth geometry. Which inner seat is empty does not change matching or the inherited key. The center is empty, with no Paper or reference scroll. Outer offerings and all device-craft imbuements have no effect on this proposed recipe; installed imbuements stay in their sockets. This follows the existing device ritual and atomic offering-consumption/output lifecycle in [ritual crafting](ritual-crafting.md).

Copy the shard's verified key and reproducible blueprint unchanged. Neither offering placement nor the device ritual's geometry or sockets hashes a new channel. Matching shells require matching shards; the established shard recipe can be repeated to supply them.

EchoLink's nine ingredients made the tuner and communicator together. Vestige's Attunement Shard already incorporates Echo Shard, Amethyst and inscription materials. The smaller recipe lets that item supply the tuning and keeps the remaining offering focused on communication. The ingredient roles above are native construction flavor, not inherited Iron attribute bonuses or a new power ladder. Crafting ingredients need not be drawn literally on the resulting item. The [complete pinned material reference](../research/iron-material-uses.md) remains upstream evidence.

### Third-ingredient alternatives

| Candidate | Vanilla reference fact | Proposed native mood |
| --- | --- | --- |
| **Sculk Sensor, recommended** | Detects vibrations. Its block item requires Silk Touch when mined, but can also appear in Ancient City chest loot. [Verified 1.21.1 acquisition](../research/whispering-shell-sculk.md) | Dark listening magic, matching the owner's sculk preference without requiring a Silk Touch tool for acquisition. |
| Sculk Vein | Grows around Sculk patches; requires Silk Touch to drop itself, with no shears route in 1.21.1. Veins themselves are not sensors. [Verified 1.21.1 acquisition](../research/whispering-shell-sculk.md) | Dark, living magic; introduces a mining-enchantment gate. |
| Soul Sand | Mojang describes the face-like markings on its texture. [Official block feature](https://www.minecraft.net/en-us/article/block-week-soul-sand) | An old vessel carrying trapped or distant voices. Its earlier literal-grain art interpretation was rejected. |
| Phantom Membrane | A membrane from nocturnal flying Phantoms, used in Slow Falling brewing and Elytra repair. [Official item feature](https://www.minecraft.net/en-us/article/taking-inventory--phantom-membrane) | Airy, ghostly whispers. |

The voice-bearing, listening and whisper descriptions are Vestige flavor proposals, not claims that these vanilla ingredients already transmit chat. The exact third ingredient remains open. All sculk-family block items require Silk Touch when mined, including the calibrated sensor, catalyst and shrieker. Ordinary Ancient City chests have a **23.24%** chance of at least one Sensor, with **1–3** supplied per selected Sensor entry; chest loot needs no Silk Touch. The attunement craft already uses an Echo Shard, found in **29.81%** of those chests. [Exact pinned-table calculation](../research/whispering-shell-sculk.md#how-common-are-chest-sensors) makes a single Sensor a plausible extra offering for the same expedition, while finding an Ancient City remains the main gate. A Warden also drops a Catalyst, but fighting one is unnecessary for this route.

**Echo Shard acquisition, accepted October 6:** the owner approved a normal shapeless craft of **1 Amethyst Shard + 1 Soul Sand + 1 Ghast Tear → 1 Echo Shard**. The executable [crafting recipe](../../src/main/resources/data/vestige/recipe/echo_shard.json) uses the vanilla Echo Shard and is available in an ordinary player crafting grid or crafting table. Its [recipe-book unlock](../../src/main/resources/data/vestige/advancement/recipes/echo_shard.json) triggers on obtaining a Ghast Tear. The resonant crystal, souls and ghostly cry are native flavor for a repeatable Nether material cost. Earlier multiple-output and Ender Pearl proposals are superseded. This removes the Ancient City requirement for an Echo Shard; the proposed Shell's Sculk Sensor acquisition is still separate.

## Communication rules

**Accepted activation:** every valid shell in any of the nine hotbar slots transmits, regardless of the selected slot. The remaining entries form the proposed first implementation around that choice.

| Situation | Behavior |
| --- | --- |
| Shell in the 27 main inventory slots below the hotbar | Listen to its matching channel. |
| Shell in any hotbar slot | Ordinary typed chat transmits to that channel. The selected slot does not matter. |
| Shell in offhand | Proposed: transmit too, matching EchoLink's offhand behavior. |
| Several hotbar/offhand shells with different keys | Proposed: transmit the same message to all active keys, like EchoLink. |
| Several carried shells with different keys | Listen to all carried channels. Only hotbar/offhand shells also transmit. |
| Several copies with the same key | Each conversation message is delivered once per player. |
| No shell in hotbar or offhand | Use the server's usual chat behavior. |
| Shell in a chest, Ender Chest, dropped item or nested container | Inactive. Only direct player inventory slots and offhand participate. |
| Matching listener far away or in another available dimension | Receive normally on the same server. |
| Matching owner offline | No queued message or replay. |

The sender sees their own transmission once. One channel may contain any number of matching online holders. For multiple active channels, a recipient receives the message when their carried-key set intersects the sender's active-key set. Decorate the conversation with only the shared channel marks, in a stable order; carrying two matching shells or sharing two active channels still yields one delivered message and one cue. This follows EchoLink's group-broadcast precedent while replacing its item equality with verified key equality.

Moving every shell out of the hotbar and offhand returns speech to ordinary chat while retaining reception from the main inventory. The native shell is a handheld item, so this draft has no head equipment, right-click toggle, dedicated commands or separate chat screen.

There is **no per-message durability or mana charge** in this first proposal. The attunement and shell crafts supply the acquisition cost. Homebound Eye's accepted travel wear/payment routes do not automatically apply to communication. Range upgrades, stored messages and alternate payments would require separate design decisions.

Compare full validated keys independently of names, display runes, stack count and cosmetics. Existing key-derived attunement marks identify the channel visually; a matching short mark alone does not connect devices. Renaming a shell labels it without changing its channel. A recognized shell with invalid binding cannot transmit or receive, and attempting to speak through only invalid active shells must not fall through into public chat. Shared access comes from possessing a matching device, rather than an owner or membership registry.

## Texture and feedback

**Accepted art requirement:** a dark, eerie shell with its own identity, independent of recipe components, fitting ordinary Minecraft item surroundings. After discarding the old conch and reviewing four fresh forms, the owner chose **Shell A, Coiled spiral** as the current basis. Its shape fits Minecraft fairly well; finer shading needs reduction without losing the spiral or hollow mouth. The [current focused review](../art/item-textures-native-v5/README.md) shows each coil refinement separately in a normal Minecraft chest among vanilla items. Homebound Eye uses Pearl heart as an iteration basis; no Eye texture is approved. Reuse Vestige's native attunement mark separately from the base texture. The item tooltip proposal is its name and bare colored signature, with no instruction paragraph.

The focused refinements use separate built-in imagegen edits of the chosen bases, with pinned vanilla textures as style references. Original output PNG bytes and alpha are preserved. Model GUI transforms preserve natural aspect and fit the solid body within fourteen native menu pixels. These generated shape/color studies remain high-resolution concepts, not final literal 16×16 assets. The [fresh eight-direction gallery](../art/item-textures-native-v4/README.md) preserves the selected bases; earlier conch, smooth and ridged studies are historical. Final selection, exact pixel discipline, attunement marks and held/offhand appearance remain outstanding.

Propose a brief local signature pulse and quiet hollow breath on sending; a softer breath and brief local mark pulse on receiving. Send cues mean a transmission was submitted, not that another person read it. Cue delivery must follow actual delivered conversation, and multiple matching copies must not multiply sounds. Inventory-only reception remains audible without requiring an open inventory. No repeated idle noise, recipient count, connection announcements, instructional chat/actionbar text or error text. The player's conversation itself remains readable chat. Sound volume and exact pulse appearance need actual client review.

## Native implementation boundary

Use native registered items and bounded, versioned attunement components. Reuse shard verification and ordinary device ritual matching/atomic commit instead of importing Bukkit skull/lore handling or adding another channel registry. Determine slots and recipients on the server thread, deduplicate by player, and reevaluate carried keys for each delivery rather than maintaining stale subscriptions.

Keep the server's existing proximity/global chat policy for ordinary speech. EchoLink's shout/whisper/tell/staff commands, permissions, rank styling and partial-distance concealment are outside this device. No legacy migration or plugin dependency is proposed.

The native chat path needs implementation care: on pinned NeoForge 21.1.72, canceling `ServerChatEvent` and sending raw system strings bypasses the later vanilla filter/broadcast path, with logging and spam handling affected. Preserve signed/filtered player-message delivery, cancellation, client visibility, ordering, server logging and spam accounting. The [pinned source investigation](../research/echolink-whispering-shell.md#native-compatibility-constraint) explains the seam; this document does not claim a complete chat integration.

## Verification required for a built version

Verify atomic device crafting and rejection of invalid shards; matching full keys despite cosmetic changes; mismatched full keys despite matching display marks; all nine hotbar positions with another selected slot; offhand activation; immediate slot changes; direct versus nested inventory; multiple active keys with recipient-specific marks and one delivery; duplicate copies; sender echo; cross-dimension and distant listeners; offline listeners; invalid-binding behavior; other-handler cancellation; filtered messages, hidden-chat clients, ordering, logging and spam accounting. Use actual multiplayer delivery for chat evidence. Inspect the real 16x16 item, signature and send/receive cues in the native client before calling presentation complete.

**Latest art/acquisition pass:** see the [selected-base refinement review](../art/item-textures-native-v5/README.md) for the current appearance choices. Previous Sensor/Echo Shard acquisition research remains applicable: exact Java 1.21.1 chest probabilities come from the pinned table. The normal Amethyst Shard + Soul Sand + Ghast Tear Echo Shard craft is implemented and verified. Shell gameplay, binding marks, held/offhand forms and final asset selection remain outstanding.
