# Attuned devices

**Accepted item direction and names, October 6, 2026.** The owner selected **Crane Bag** for shared portable storage, **Whispering Shell** for communication and **Homebound Eye** for return teleportation. The proposed origin-pointing compass is superseded by Homebound Eye, a handheld item that teleports its user back to the Runic Circle that crafted it when right-clicked. Homebound Eye and Crane Bag gameplay are implemented; Crane Bag's recipe/art review and Whispering Shell gameplay remain unfinished.

**Runic Circle is internal and documentation terminology only.** It must not appear in UI, tooltips, messages or recipe viewers. Spellstone remains the name of the central block.

## Attunement and origin

[Attunement Shards](attunement-shards.md) provide a reproducible signature. Devices using that signature inherit the complete verified key; their own ingredients do not generate a new identity. Matching abbreviated rune marks do not establish a connection.

Crane Bags and Whispering Shells group by signature. Homebound Eye targets its own actual crafting site, interpreted as the Spellstone where the device was crafted. Identical Runic Circles built elsewhere can produce matching signatures, so the key alone cannot identify that origin. Homebound Eye therefore needs a separate crafting-origin address; the existing shard blueprint retains relative geometry rather than world coordinates.

**Accepted common item presentation, October 6:** every item inheriting a shard's full attunement key shows its item name followed by the shard's same four colored rune marks. All Standing Stone finishes and Homebound Eye already append that line with the shared `AttunementMark` formatter. Crane Bag and Whispering Shell must do the same when implemented. Item type, finish, crafting origin and payment route do not change those marks for a copied key; the complete key remains the actual identity.

## Homebound Eye

Right-clicking Homebound Eye teleports its user back to its crafting origin. This replaces the compass concept because a vanilla Lodestone already provides the central navigation use case.

**Homebound Eye** is the accepted name. The owner suggested a poisonous spider eye, interpreted as the vanilla **Spider Eye** (`minecraft:spider_eye`). The implemented four-inner-Plinth recipe consumes one valid Attunement Shard, one Spider Eye, one Ender Pearl and one Flint, producing one Homebound Eye. The owner selected Flint on October 6, superseding Gold Ingot. It is shapeless, uses no reference scroll or Paper and ignores the outer layer. The Spider Eye is the payment-selector offering; embedded materials remain installed. This does not introduce a new Spider Eye item type. The earlier Hearth Charm/Homeward Stone/Recall Stone names and Lodestar/Lode Needle/Hearth Needle compass proposals are superseded.

### Visual and fourth-ingredient exploration

**Historical visual selection, October 6, 2026 (superseded by the fresh-art reset below):** the owner selected H (Chipped Flint) from the [eight generated concepts](../art/homebound-eye-concepts-v1/README.md), requested a simpler Minecraft-style texture and a red central eyepiece inspired by the Spider Eye, then selected Ender Pearl teal-green for the surrounding eye recess/frame. The selected [16×16 flint refinement](../art/homebound-eye-flint-v2/README.md) retains the irregular grey stone silhouette with a teal-green engraved eye and crimson center. The finished item does not need to resemble all its individual ingredients or an Eye of Ender. Flint is the accepted fourth ingredient; the other material concepts remain historical proposals. Gameplay and imbuement decisions below are unaffected.

**Current art direction, October 6:** after discarding earlier art and viewing [four fresh directions per item](../art/item-textures-native-v4/README.md) separately among vanilla items, the owner selected **Shell A, Coiled spiral** as the Shell basis, with less fine texture detail. The owner did not approve any Eye texture, but chose **Eye B, Pearl heart** as the basis for iteration. The [focused native menu review](../art/item-textures-native-v5/README.md) explores cleaner coils and a clearer pearl eye with a smaller flint cradle. Final pixel assets remain unapproved. Previous appearance selections are historical; recipes, materials and gameplay remain accepted.

### Durability and imbuement

**Accepted behavior:** Homebound Eye has finite durability and supports multiple returns. By default, a return spends durability alone, with substantial wear per use. Imbuement can select health, hunger, mana or experience as an additional payment, substantially reducing the durability spent on each return. Every route still causes durability wear.

**Accepted balance baseline, locked by the owner October 6, 2026:** **30 maximum durability**, superseding the earlier 60- and 200-durability possibilities. The owner accepted the following resource charges and wear values. Implementation is complete; broader survival playtesting can refine these starting values.

| Payment route | Durability per return | Additional cost | Returns from a fresh 30-durability Eye |
| --- | --- | --- | --- |
| Default | 6 | None | 5 |
| Health | 1 | 3 hearts (6 HP) | 30 |
| Hunger | 2 | 3 hunger icons (6 food points) | 15 |
| Experience | 2 | 25 XP points | 15 |
| Mana | 2 | 30 mana | 15 |

The counts assume sufficient resources and no repairs. Supplying another resource extends the item's useful life without eliminating wear. These are the accepted starting values; later tuning must be based on playtest evidence.

The mana price matches the existing unshaped Recall spell's 30-mana return cost and consumes 30% of the [accepted 100-mana player pool](spell-balance.md#planned-player-mana-baseline). Health's longer item life is paid for with immediate vulnerability; hunger competes with sprinting and natural healing; XP consumes earned points. These resources are not universally interchangeable. Health payment must leave the player alive. Charge XP points rather than levels, whose value changes with current level; the payment uses actual level/progress rather than the historical XP counter and respects cancellable XP events. The accepted charges still need broader survival playtests with mana recovery active; they are not measured gameplay balance.

**Crafting-time selector:** the imbuement installed in the Plinth holding the Spider Eye selects the output's payment route. An empty selector gives durability-only payment. The completed device stores the selected route and its corresponding accepted wear/resource charge; using it later does not rescan nearby Plinths. The imbuements under other device offerings do not stack payment routes. The installed material selects Health with Soul Sand, Hunger with Moss Block, Experience with Lapis Block or Mana with Amethyst Block. Any other material in the Spider Eye's socket rejects the craft; other device sockets do not change the payment. Lapis Block is accepted as a device socket without inventing a scroll Spellshaping effect.

The selected payment configuration belongs to the device, separately from its inherited attunement key and crafting-origin address. A payment choice must not rehash that key or redirect the origin. [Standing Stone payment design](standing-stone-payments.md) offers related payment and transaction concepts; its distance scaling, default experience route and Pearl-Plinth selector are not decisions for Homebound Eye.

**Implemented transaction behavior:** validate the destination and affordability before travel; spend the selected resource and durability only for a successful return. A failed return spends neither. A canceled dimension transfer refunds the selected resource and leaves durability intact. Creative bypasses resource payment and wear.

Returns support any available dimension and require a live Spellstone at the stored crafting address. The selected destination chunk is loaded for validation, with no permanent chunk ticket. The shared `NearbyTeleport` helper randomly selects an open, dry position one or two blocks horizontally from the destination, allowing a one-block height step and requiring supporting ground. If there is no opening, it deliberately uses a nearby occupied position at the destination’s height. No terrain is changed. Standing Stones use the same helper. Missing Spellstones, unavailable nearby chunks/border positions, insufficient durability or resources and invalid item bindings reject without payment. Placing a new Spellstone at the same address restores that destination; this is a site address, not a persistent block-instance identity. The last successful use consumes the depleted Eye. Wear follows the stored route exactly, without an Unbreaking discount. Riding players must dismount before using the Eye. No extra cooldown or repair recipe is implemented.

## Whispering Shell

Whispering Shells with the same full signature share a communication channel. The owner's [EchoLink plugin](https://github.com/Quzzar/echolink) is the behavioral inspiration, including inventory-position-dependent transmission and reception. The [pinned source review](../research/echolink-whispering-shell.md) records hotbar/offhand/head transmission, carried-inventory reception, simultaneous active-channel broadcasts and cross-world online delivery. Those are upstream facts, not a completed Vestige chat integration.

**Accepted activation, October 6:** any shell in the hotbar transmits, like EchoLink; the selected hotbar slot does not matter. The owner requested Nautilus Shell + Attunement Shard + one whispery/dark ingredient, removing the initial extra Amethyst and Copper, and prefers an independent dark, eerie item appearance rather than literal ingredient decoration. The [current native design](whispering-shell.md) recommends Sculk Sensor as the third offering, using three offerings and one empty seat among four inner Plinths. [Pinned 1.21.1 loot evidence](../research/whispering-shell-sculk.md) confirms that mining all sculk blocks requires Silk Touch for the item, while ordinary Ancient City chests have a 23.24% chance of supplying Sensors without it. The earlier **A, hollow conch** selection is historical; the owner discarded the art basis and now iterates on **Shell A, Coiled spiral**, reducing its fine detail in [separate vanilla menu views](../art/item-textures-native-v5/README.md). The draft also proposes listen-only main inventory, offhand transmission, deduplicated multi-channel delivery and unlimited online cross-dimension range. The third ingredient, those additional details and final native texture approval remain proposals. Exact recipe, offhand/head behavior, multiple-channel presentation, local/global interaction, range, dimension rules and payments are not yet locked or implemented. The accepted name is **Whispering Shell**; the archived Sending Shell and EchoLink names identify inspirations.

## Crane Bag

Every Crane Bag with the same full signature accesses one shared inventory. Items placed through one matching bag can be taken out through another. The owner selected vanilla-style Bundle behavior and capacity: a small mixed-item pool measured by item weight, not a chest-slot layout. Additional matching bags do not increase that capacity. This is shared storage, not merely sending a parcel between separate inventories. The [Crane Bag design](crane-bag.md) records the accepted behavior, signature presentation and implemented server-owned transaction/synchronization approach.

The implemented starting policy shares contents across dimensions of the same server save, retains them after all physical bags are destroyed and rejects nesting in either direction with Bundles/Crane Bags. Normal server inventory slots and cursors use vanilla secondary-click insertion/extraction; right-click drops contents, retaining any stack whose spawn is canceled. Read-only/output-only/fake slots and container-component items are excluded. The temporary test command was removed at the owner's request. The user explicitly deferred the survival recipe and final image until after gameplay implementation; the current item references vanilla Bundle art. No automation capability is exposed. The accepted name draws on the Irish literary association documented in [naming research](../research/attuned-device-names.md); the shared-storage mechanic is an independently authored Vestige concept.
