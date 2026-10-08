# Whispering Shell

**Recipe order correction, October 8:** the shared inner-layer pattern is **Attunement Shard → Sculk Sensor → Nautilus Shell → empty**, equivalent under whole quarter-turns. Outer nodes are inactive; the required empty inner position participates in cancellation. Older captures and shipment records below preserve their historical recipe layout.

**Native implementation, October 6, 2026.** The owner locked **Shell B, Deep coil**, together with **Homebound Eye A, Centered pearl**, and authorized finishing the items. The registered Shell now copies a valid shard's full verified blueprint, crafts from **1 Nautilus Shell + 1 Attunement Shard + 1 Sculk Sensor**, and transmits ordinary chat from every hotbar seat. The selected hotbar slot does not matter. Offhand transmission, multiple simultaneous channels, unlimited online cross-dimension range and no per-message cost are implementation defaults following the owner's EchoLink precedent. These defaults were selected during closeout; they were earlier proposals, rather than separately approved balance decisions. Final verification is recorded in [the closeout archive](../art/whispering-shell-finished/README.md).

## Experience

**Shipped October 6:** the owner approved this presentation and authorized installation into **Prism → Kithkyn Testing**. The complete registered item, ritual recipe, native chat/mixins and public recipe presentation are installed with the compact symbols and no center flash. [Shipment and exact-package evidence](../art/whispering-shell-shipped/README.md) record the backup and verified replacement. Restart Minecraft to load the feature. Other installed features and the pack's current one-scroll limit are preserved; the workspace's separate sixteen-scroll and thread work retain their own release scope.

**Owner presentation corrections, October 6:** conversation channels use the single colored symbol drawn in each Shell's inventory corner, joined by gray ` & `, following EchoLink's `symbols > sender - message` layout. Received messages show only shared channel symbols and the sender's name. The owner subsequently rejected the center flash; there is no Shell HUD overlay. The [current native situation gallery](../art/whispering-shell-chat-only/README.md) shows unselected hotbar activation, main-inventory reception, offhand use, simultaneous channels, shared symbols, duplicates, ordinary-chat return and a mismatched listener. The quiet send/receive sound remains. The [preceding compact-symbol review](../art/whispering-shell-symbol-chat/README.md) and [four-rune conversation review](../art/whispering-shell-chat-review/README.md) are historical presentation evidence.

Make matching shells, give one to a friend, carry yours to hear them, and keep yours in the hotbar to talk back. The selected hotbar slot does not matter, leaving the main hand available for exploring or building. Communication is a live conversation between online players carrying matching keys.

The reference is the owner's EchoLink, pinned to `3995850297e893809bcd79efb715d02d65deb9e2`. Its actual implementation transmits from every hotbar, offhand and head shell, receives from player inventory, replaces ordinary chat while active, and works across worlds without distance checks. The [source review](../research/echolink-whispering-shell.md) records exact recipe, identity, commands and limitations. These facts are distinct from the native choices below.

## Implemented recipe

Three offerings in the clockwise inner-layer pattern **Attunement Shard → Sculk Sensor → Nautilus Shell → empty**, equivalent under whole quarter-turns:

| Quantity | Offering | Native construction role |
| --- | --- | --- |
| 1 | Valid Attunement Shard | Supplies the complete existing channel identity. |
| 1 | Nautilus Shell | Physical vessel for a voice; its ordinary beige appearance does not constrain the result. |
| 1 | Sculk Sensor | Vibration-sensitive material for the communication mechanism. |

Produce **one Whispering Shell**, consuming one of each offering. Keep a complete four-Plinth inner layer; three offerings do not create a three-Plinth geometry. The fourth inner seat must stay empty. Moving individual offerings changes matching; rotating the whole pattern remains valid. The inherited key is unchanged. The center is empty, with no Paper or reference scroll. Outer offerings and all device-craft imbuements have no effect on this recipe; installed imbuements stay in their sockets. This follows the existing device ritual and atomic offering-consumption/output lifecycle in [ritual crafting](ritual-crafting.md).

Copy the shard's verified key and reproducible blueprint unchanged. Neither offering placement nor the device ritual's geometry or sockets hashes a new channel. Matching shells require matching shards; the established shard recipe can be repeated to supply them.

EchoLink's nine ingredients made the tuner and communicator together. Vestige's Attunement Shard already incorporates Echo Shard, Amethyst and inscription materials. The smaller recipe lets that item supply the tuning and keeps the remaining offering focused on communication. The ingredient roles above are native construction flavor, not inherited Iron attribute bonuses or a new power ladder. Crafting ingredients need not be drawn literally on the resulting item. The [complete pinned material reference](../research/iron-material-uses.md) remains upstream evidence.

### Third-ingredient alternatives

| Candidate | Vanilla reference fact | Proposed native mood |
| --- | --- | --- |
| **Sculk Sensor, implemented** | Detects vibrations. Its block item requires Silk Touch when mined, but can also appear in Ancient City chest loot. [Verified 1.21.1 acquisition](../research/whispering-shell-sculk.md) | Dark listening magic, matching the owner's sculk preference without requiring a Silk Touch tool for acquisition. |
| Sculk Vein | Grows around Sculk patches; requires Silk Touch to drop itself, with no shears route in 1.21.1. Veins themselves are not sensors. [Verified 1.21.1 acquisition](../research/whispering-shell-sculk.md) | Dark, living magic; introduces a mining-enchantment gate. |
| Soul Sand | Mojang describes the face-like markings on its texture. [Official block feature](https://www.minecraft.net/en-us/article/block-week-soul-sand) | An old vessel carrying trapped or distant voices. Its earlier literal-grain art interpretation was rejected. |
| Phantom Membrane | A membrane from nocturnal flying Phantoms, used in Slow Falling brewing and Elytra repair. [Official item feature](https://www.minecraft.net/en-us/article/taking-inventory--phantom-membrane) | Airy, ghostly whispers. |

The voice-bearing, listening and whisper descriptions are Vestige flavor proposals, not claims that these vanilla ingredients already transmit chat. Sculk Sensor is the implemented third ingredient; the other entries retain the alternatives considered during design. All sculk-family block items require Silk Touch when mined, including the calibrated sensor, catalyst and shrieker. Ordinary Ancient City chests have a **23.24%** chance of at least one Sensor, with **1–3** supplied per selected Sensor entry; chest loot needs no Silk Touch. The attunement craft already uses an Echo Shard, found in **29.81%** of those chests. [Exact pinned-table calculation](../research/whispering-shell-sculk.md#how-common-are-chest-sensors) makes a single Sensor a plausible extra offering for the same expedition, while finding an Ancient City remains the main gate. A Warden also drops a Catalyst, but fighting one is unnecessary for this route.

**Echo Shard acquisition, accepted October 6:** the owner approved a normal shapeless craft of **1 Amethyst Shard + 1 Soul Sand + 1 Ghast Tear → 1 Echo Shard**. The executable [crafting recipe](../../src/main/resources/data/vestige/recipe/echo_shard.json) uses the vanilla Echo Shard and is available in an ordinary player crafting grid or crafting table. Its [recipe-book unlock](../../src/main/resources/data/vestige/advancement/recipes/echo_shard.json) triggers on obtaining a Ghast Tear. The resonant crystal, souls and ghostly cry are native flavor for a repeatable Nether material cost. Earlier multiple-output and Ender Pearl proposals are superseded. This removes the Ancient City requirement for an Echo Shard; the Shell's Sculk Sensor acquisition is still separate.

## Communication rules

**Accepted activation:** every valid shell in any of the nine hotbar slots transmits, regardless of the selected slot. The remaining entries are the first implementation defaults around that choice.

| Situation | Behavior |
| --- | --- |
| Shell in the 27 main inventory slots below the hotbar | Listen to its matching channel. |
| Shell in any hotbar slot | Ordinary typed chat transmits to that channel. The selected slot does not matter. |
| Shell in offhand | Transmit too, matching EchoLink's offhand behavior. |
| Several hotbar/offhand shells with different keys | Transmit the same message to all active keys, like EchoLink. |
| Several carried shells with different keys | Listen to all carried channels. Only hotbar/offhand shells also transmit. |
| Several copies with the same key | Each conversation message is delivered once per player. |
| No shell in hotbar or offhand | Use the server's usual chat behavior. |
| Shell in a chest, Ender Chest, dropped item or nested container | Inactive. Only direct player inventory slots and offhand participate. |
| Matching listener far away or in another available dimension | Receive normally on the same server. |
| Matching owner offline | No queued message or replay. |

The sender sees their own transmission once. One channel may contain any number of matching online holders. For multiple active channels, a recipient receives the message when their carried-key set intersects the sender's active-key set. Decorate the conversation with one inventory-corner symbol for each shared full key, in stable key order, separated by gray ` & `; carrying two matching shells or sharing two active channels still yields one delivered message and one cue. The name follows gray ` > ` and the message follows ` - `. This follows EchoLink's group-broadcast and compact-format precedent while replacing its item equality with verified key equality. Two distinct keys with identical short symbols still remain distinct channels.

Moving every shell out of the hotbar and offhand returns speech to ordinary chat while retaining reception from the main inventory. The native shell is a handheld item, with no head equipment, right-click toggle, dedicated commands or separate chat screen.

There is **no per-message durability or mana charge** in this implementation. The attunement and shell crafts supply the acquisition cost. Homebound Eye's accepted travel wear/payment routes do not automatically apply to communication. Range upgrades, stored messages and alternate payments would require separate design decisions.

Compare full validated keys independently of names, display runes, stack count and cosmetics. Existing key-derived attunement marks identify the channel visually; a matching short mark alone does not connect devices. Renaming a shell labels it without changing its channel. A recognized shell with invalid binding cannot transmit or receive, and attempting to speak through only invalid active shells must not fall through into public chat. Shared access comes from possessing a matching device, rather than an owner or membership registry.

## Texture and feedback

**Locked artwork:** a dark, eerie shell independent of its recipe components, fitting ordinary Minecraft item surroundings. The owner chose **Shell B, Deep coil**, after refining the Coiled spiral basis, and locked **Homebound Eye A, Centered pearl**, at the same time. The [canonical approved assets](../art/attuned-items-locked/README.md) preserve the exact original artwork pixels and alpha, adding transparent power-of-two atlas padding. Model normalization retains the approved size and placement. The registered native Shell uses these exact production resources, with independently rendered binding marks and glint. Reuse Vestige's native attunement mark separately from the base texture. The item tooltip is its name and bare colored signature, with no instruction paragraph.

The selected images came from separate built-in imagegen edits of the chosen bases, with pinned vanilla textures as style references. [The five-option review](../art/item-textures-native-v5/README.md) retains the exact prompts and original outputs; unselected variants and earlier conch/smooth/ridged studies are historical. The approved artwork is high-resolution pixel art, not a literal 16×16 source texture. No downsampling, recoloring, redrawing or alpha changes are part of lock-in. Native GUI approval and model loading are separate from held/offhand appearance, binding marks and Shell gameplay.

Delivered conversations produce one quiet, lower-pitched Allay voice note: volume 0.22 / pitch 0.65 for sending, volume 0.13 / pitch 0.8 for receiving. The owner explicitly removed the center symbol flash; no Shell GUI layer, pulse timer or temporary visual state remains. Channel symbols appear in normal chat and Shell inventory corners. Send cues mean a transmission was submitted, not that another person read it. Cue delivery must follow actual delivered conversation, and multiple matching copies must not multiply sounds. Inventory-only reception remains audible without requiring an open inventory. No repeated idle noise, recipient count, connection announcements, instructional chat/actionbar text or error text. The player's conversation itself remains readable chat; subjective audio tuning remains available during playtesting.

## Native implementation boundary

Use native registered items and bounded, versioned attunement components. Reuse shard verification and ordinary device ritual matching/atomic commit instead of importing Bukkit skull/lore handling or adding another channel registry. Determine slots and recipients on the server thread, deduplicate by player, and reevaluate carried keys for each delivery rather than maintaining stale subscriptions.

Keep the server's existing proximity/global chat policy for ordinary speech. EchoLink's shout/whisper/tell/staff commands, permissions, rank styling and partial-distance concealment are outside this device. No legacy migration or plugin dependency is proposed.

Two narrowly scoped, required mixins pin the Java 1.21.1 delivery seam. Submission records the current hotbar/offhand route against the original signed-message body before filtering. Weak identity keys release canceled/completed submissions without retaining players or servers. A queued private message therefore stays private if its Shell is moved or removed while filtering; a queued ordinary message also retains its ordinary route. The sender sees their own valid transmission once. Listeners are reevaluated at delivery.

At `PlayerList`'s existing recipient/filter calls, matching holders receive the original `OutgoingChatMessage` with recipient-specific corner symbols in its bound sender name. The data-defined native `vestige:whispering_shell` chat type decorates `sender` and `content` using `%s - %s` and the existing player-chat narration route. Vanilla owns signature/body delivery, asynchronous filtering and ordered completion, chat visibility, moderation cancellation, logging, filter notices and spam accounting. Commands and non-player chat retain their original route. This does not introduce proximity chat or override another handler's canceled `ServerChatEvent`. The required mixins deliberately fail at startup if the pinned method seam changes; a platform update must verify it again.

## Verification

**Installed package:** the exact scoped jar passes **fourteen fresh-world GameTests** and **ten native client conversation scenarios**, both with the installed Kithkyn jar loaded. All thirteen 960×720 frames were visually inspected; twenty-nine loaded class hashes and four asset hashes match the installed candidate. Native assertions verify the public Shell recipe, unchanged pack scroll limit and compact per-recipient chat. The backup and installed jar hashes are verified, and all 3,286 unrelated installed ZIP members are byte-identical. [Shipment verification](../art/whispering-shell-shipped/verification.json) separates this package evidence from the earlier full-source unit runs. Existing worlds and the running game were not restarted.

**Current center-flash removal:** production build and Kithkyn compatibility pass, with **121 unit tests** and all **ten native chat scenarios** passing. All thirteen unedited 960×720 captures were visually inspected, including outgoing and incoming messages with no center symbols. Only the two client-feedback class files changed from the preceding review; the fourteen unchanged server/binding class files retain the prior fourteen passing focused world tests. [Current captures, package and verification](../art/whispering-shell-chat-only/README.md) pin the result. The local browser tab could not be refreshed because of its URL security policy; the new captures and gallery are saved. No installed pack or existing world was replaced.

**Earlier compact-symbol review:** production build and Kithkyn compatibility pass, with **118 unit tests and all fourteen focused GameTests** passing. Existing Shell tests verify the native chat type, exact shared-symbol sender decoration, short-symbol collisions and preservation of original synthetic signature/body bytes. All ten native client situations pass, with thirteen unedited 960×720 conversation/inventory frames visually inspected. One actual Survival client and four in-memory server packet clients provide local evidence; authenticated Mojang signing and remote human multiplayer remain outside the review. [Archived captures, package and verification](../art/whispering-shell-symbol-chat/README.md) pin that result before removal of the center flash. No installed pack or existing world was replaced.

**Original item closeout:** the following counts and five captures describe the earlier initial implementation.

Verification covers atomic device crafting and invalid shards; full keys despite cosmetics and short-mark collisions; every hotbar slot, offhand and main-inventory roles; direct versus nested inventory; multiple channels and deduplicated delivery; sender echo; distant/cross-dimension listeners; invalid-binding privacy; queued private speech ordering; cancellation, filtering, hidden-chat clients, preserved signature/body bytes and spam accounting. **114 JUnit tests and 14 focused Minecraft GameTests pass**, with six native Shell checks. The real registered item, normal/advanced custom tooltip, both conversation directions and rune pulse were inspected in five native client captures. One actual client and two in-memory server packet clients supplied the delivery evidence; remote human multiplayer and authenticated Mojang signing were not exercised.

**Closeout evidence:** [native checks, captures and packaged result](../art/whispering-shell-finished/README.md). The approved source pixels and alpha remain unchanged. The normal Amethyst Shard + Soul Sand + Ghast Tear Echo Shard craft remains implemented. Historical source/appearance-only reviews retain their original scope; current production item binding and communication are implemented here.
