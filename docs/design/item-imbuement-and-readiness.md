# Item imbuement and readiness

**Owner direction, October 8, 2026.** Embrace crafted imbuements across native magical items, alongside the existing shared trait system. The owner requested an audit and follow-up changes to existing items, plus an animated native review of independent mana and recovery overlays. The v3 vanilla overlay appearance is approved below; production readiness integration is implemented with Wayfarer; other item conversions remain follow-up work.

## Accepted item-name rarities

**Owner approved October 8:** use Minecraft's native base item rarity for the reviewed equipment and devices.

| Base rarity | Items |
| --- | --- |
| Uncommon / yellow | Dissentient Diamond, Attunement Shard, Homebound Eye, Whispering Shell, magical Staff, Wardweave Robes, Cinderweave Robes, Wayfarer Boots |
| Rare / aqua | Crane Bag, Fluxed Flint |
| Common / white | All five magical threads, Scroll Fragments, all seven mundane Staffs, every Spellstone/Plinth/Standing Stone finish |

Scrolls and Wands retain their existing knowledge-aware spell-rarity name colors; unidentified spells remain white. Magical Staff names stay Uncommon across affinities, selection and two/four/six-slot capacities. Item rarity does not change spell rarity, Staff cast wear, resources, effects, crafting or durability. Crafted adjective prefixes inherit the name color and retain their italics; the stored variant remains authoritative. Existing thread/shard shimmer is independent of rarity and is retained. Compatible armor enchantments keep vanilla's displayed rarity promotion rather than receiving a forced text color. No new Epic base item is selected, and this decision does not implement the pending hourglass or other clothing packages.

[Native verification and packaging](../verification/item-rarity-2026-10-08/README.md) record actual results; changing the testing-pack installation is separate work.


## Traits and crafted variants

Numerical ability values use the same authored trait relationships as spells. Duration, protection, movement, range and other numerical outcomes can consume the appropriate traits. Numerical relationships remain explicit: a descriptive trait does not acquire a hidden runtime rule merely because an item has a number. Activation snapshots and passive reevaluation keep their existing ownership rules.

Imbuement is a persistent crafting choice. It can adjust the math, change or extend the ability, change payment/recovery, or change maximum durability and authored wear. An imbued ability still resolves the wearer's current trait contributions through the common resolver. There should be no second unrelated item-magic runtime, and no double application of source shaping.

Keep immutable base definitions and compile trusted authored variants from stored identifiers, degrees and bounded parameters. Client/item data must not supply executable effect graphs. Reuse/generalize the existing Spellshaping compilation and the shared item-ability/effect/payment foundation where compatible; item-specific selectors can remain explicit policies. A spell rider is not automatically compatible with every passive item or storage device.

Match an item's accepted base recipe first, then resolve supported offering/socket contributions and store the resulting variant in the output. Preserve ingredient roles, relative recipe shape, whole-rotation behavior, inactive layers and retained socket materials. Existing recipe quantities and selectors are unchanged until their concrete extensions are authored. Read [material crafting](material-crafting.md), the complete [pinned material reference](../research/iron-material-uses.md) and [Spellshaping](spellshaping.md) before selecting new material/offering routes.

## Shared payment balance

The owner requested coordination with the resource-balancing work on October 8. Resource-backed equipment variants use the canonical [resource payment values](resource-payments.md) and `ResourceValuation`: **1 heart (2 HP) = 4 food points (2 full hunger icons) = 30 XP points = 30 mana**. Use that shared authority, including the existing exchange ceiling and rounding, rather than a separate equipment conversion rate. The resource-balancing chat owns the common rates; equipment work authors compatible ability, crafting and durability variants against them.

Low-cost abilities require their own practical payment review. The approved five-mana Wayfarer activation cannot blindly inherit the hourglass's thirty-mana exchange amount; whole-heart/food/XP rounding may make a small exchange poor value. No alternate-payment route is selected for the boots here. Wayfarer now has explicit duration, speed, recovery and durability trade-offs; other items still need authored choices; resource equivalence does not automatically price those changes or grant passive equipment new costs. Shared storage, naming and combination conventions must stay coordinated with the other imbuement work.

**Coordination findings, October 8:** the resource-balancing chat confirmed it is not generalizing equipment storage/compilation or editing equipment code. Reuse the payment/runtime foundation; generic equipment variant persistence, compilation and naming still need implementation. `Spellshaping.compile` currently accepts a spell definition, so share its underlying machinery through the common item-ability contract. `Spellshaping.contribute` does not carry a contributed rule's health/hunger exchange, and the existing 75% exchange ceiling is enforced by the spell compiler rather than universally by `CostAdjustment`. Equipment compilation must deliberately preserve both exchange contributions and the ceiling. XP conversion exists in `ResourceValuation`, but XP is not yet a shared typed `SpellCost`; use one shared extension when adding that capability. Italic scroll augment words and wand tip adjectives exist, while a universal equipment helper and multi-adjustment display-name policy remain open. These are follow-up requirements, not newly implemented mechanics.

## Coordinated equipment proposal

The October 8 follow-up with the resource/imbuement chat confirmed explicit per-contribution degrees, rotation-independent local matching, and a distinction between a combined display adjective and a true compound that replaces mechanics. That chat owns the shared [adjective catalog](../magic-adjectives.md) and has verified `MagicAdjectives.prefix` against all sixteen Wayfarer sets; its [verification receipt](../verification/magic-adjectives-2026-10-08/README.md) records 195 passing unit tests, packaging and Kithkyn compatibility. This supersedes the earlier naming gap recorded above; the boots now persist trusted bounded selections and compile through the common runtime; extending compatible policies to other families remains outstanding. The approved [Wayfarer package](wayfarer-imbuement-review.md) implements four compatible choices—Swift, Enduring, Reinforced and Quickened—with exact selectors, costs, duplicate handling and combination examples. All fifteen nonempty sets have authored display names and approved, implemented adjustments. The owner subsequently approved that complete package on October 8; the boots routes and all sixteen variants are implemented, with verification tracked in the [boots receipt](../verification/wayfarer-2026-10-08/README.md). It uses the current shared final typed-cost rounding (`floor(value + 0.5)`), rather than introducing an equipment-specific rounding rule. Single-adjective display aliases must preserve every selected contribution and its degree.

## Existing-item work queue

| Item family | Current support | Required follow-up |
| --- | --- | --- |
| Scrolls | Persisted leyline and executable Spellshaping variants | Keep this as the established behavior and verify shared compilation remains equivalent. |
| Wands | Bound scroll shaping plus authored base, thread-core and tip contributions | Audit preservation and application exactly once. Distinguish spell shaping from any newly authored carrier durability/wear variant. |
| Staffs | Each stored scroll retains its own shaping; fixed affinity and slot expansion are separate rules | Preserve exact stored scrolls, affinity and selection. Add carrier shaping only through authored compatible routes; do not consume or rewrite scroll variants. |
| Magical threads | Socket choice selects one of five accepted thread types | Preserve the five types, names, recipes and artwork. Audit how thread contributions become part of crafted equipment; a thread type is not an arbitrary stored ability graph. |
| Wardweave / Cinderweave | Shared trait-scaled abilities; robe crafting stores the item and wool color, without a crafted ability variant | Connect crafted imbuement to both reactive and passive resolution, costs/recovery where present, maximum durability and wear. Retain the approved unmodified behavior and appearance. |
| Homebound Eye | Spider-Eye socket already selects an explicit resource/wear route | Preserve those accepted routes, attunement key, destination and rollback. Audit shared variant resolution/payment and durability support without reinterpreting the channel key. |
| Kairotic Hourglass | Ordered construction and all 36 temporal/vessel/payment variants, shared traits, single adjectives and atomic returns | Review the native artwork and survival economy; preserve the approved baseline and current-dimension trail ownership. |
| Crane Bag | Persistent shared contents keyed by attunement | Audit meaningful ability/variant hooks. Any capacity variant needs an explicit shared-pool and overflow policy first; temporary traits must not discard stored items. |
| Whispering Shell | Communication keyed by attunement | Audit meaningful variants while preserving channel identity and accepted communication behavior. Do not invent a cost or range merely to add a trait consumer. |
| Standing Stones / Attunement Shards | Shared verified blueprint/channel identity; stone payment policy has its own design | Keep identity separate from ability shaping. Audit payment and applicable numerical behaviors without silently rehashing the key or changing existing membership. |
| Fluxed Flint | Finite repair budget, shared intrinsic volatility and all eight crafted Stabilized/Reinforced/Fractious sets | Owner authorized imbuement implementation October 8. Quartz on Dissentient Diamond selects Stabilized; Iron on either Netherite selects Reinforced. October 9: Magma on Flint selects Fractious, doubling the per-activation repair cap and intrinsic risk. Preserve exact copies, spent wear and independent input risk; survival tuning and dynamic repair displays remain follow-up work. See [magical repair](magical-repair.md#crafted-imbuements). |

Wayfarer Boots and the [Kairotic Hourglass](kairotic-hourglass.md) now implement this path. The hourglass adds seven compatible choices across 36 complete sets, trusted geometry, actor Time/Amplify scaling, typed mana/health/hunger/XP payments and deterministic durability. Shared `SpellCost.Experience` now supplies the XP extension identified in the earlier coordination findings. Dawnsight Hood, Patchwork Robes and Spiderstep Boots still need their reviewed implementation packages; the separate robe proposal remains unapproved.

Each conversion needs an unchanged unimbued route, supported variant behavior and a trait-boost composition case. Preserve shaping through dyeing, repairs, renaming, stack transfers, menus, save/load and networking as applicable. Durability changes must preserve existing damage rather than refilling the item. Cancellation, failed payment, exact wear, depleted-item behavior and typed resource substitutions need native behavior evidence when changed. Do not add legacy migrations; fresh test items/worlds remain the project policy.

## Independent readiness overlays

Minecraft 1.21.1 draws its ordinary cooldown shade from `y + floor(16 × (1 − remaining))` to the bottom of the icon, using `0x7FFFFFFF`: translucent white at approximately 50% opacity. As the remaining fraction falls, the uncovered region grows from top to bottom. The production mana affordability decoration uses the same geometry and compounds independently with recovery.

The **accepted appearance** uses two independent translucent layers with **the same vanilla white shade (`0x7FFFFFFF`) and normal top-to-bottom clearing**. Mana shortage uses the item's final composed mana price; recovery uses its final authored cooldown. Overlap composites both fills, becoming more opaque while either single layer retains vanilla opacity. Zero mana cost means no mana layer; no recovery means no recovery layer. Both clear when their own condition is satisfied, including when one clears before the other. Accepted compounded overlap supersedes the earlier mana decoration's overlap avoidance.

The historical [visual study](../art/item-readiness-overlay-v1/README.md) compares warm amber (`0x669C783C`) and blue-gray (`0x66758EAB`) recovery at 40% opacity, each with matching and opposite clearing directions, at GUI scales two and three. These superseded candidates remain evidence. No new texture is needed; the normal item pixels remain visible underneath.

**Historical October 8 follow-up:** the owner initially preferred blue-gray and requested crossing diagonal sweeps. The [v2 comparison](../art/item-readiness-overlay-v2/README.md) preserves that study. The subsequent request superseded the custom tint and direction. The owner accepted [v3](../art/item-readiness-overlay-v3/README.md): “Yeah, this is fine. This is sufficient.” [Acceptance](../art/item-readiness-overlay-v3/approval.json) pins the exact native preview. The production integration is now verified with real Wayfarer mana and actor/ability recovery.

Production recovery must come from the server's actual actor/ability recovery, or its explicitly authored shared group. Vanilla's item-type cooldown alone cannot represent all ability/variant identities. Synchronize private timing snapshots; render interpolation can be client-side, but the server remains authoritative. Equipping another copy, switching slots, dyeing, repairing or renaming must not reset recovery. The mana layer must use the same compiled variant/payment as activation, not a stale base price. Apply the decoration wherever the item icon normally appears, including equipped armor slots; no extra persistent equipment HUD is authorized here.

The approved Wayfarer timing is fifteen seconds **from activation**: a baseline three-second burst leaves twelve seconds before another activation. Its grounded sprint-jump control, baseline recovery and Quickened twelve-second variant are approved and implemented. Wardweave's accepted twelve-second recovery already starts when its ward forms. Ordinary shipped scroll/wand/staff spells retain their existing absence of base cooldowns; this study does not add cooldowns globally.

## Delivery order

1. **Complete:** owner accepted v3 vanilla white, normal clearing and compounded overlap.
2. Audit the existing item adapters, stored variants and crafting paths against the queue above.
3. Generalize shared compilation and readiness synchronization, then connect approved equipment and existing devices incrementally.
4. Author and review concrete imbuement routes, outcomes and durability trade-offs; verify actual effects and presentation before shipping.

Owner approval of this direction does not select new numerical balances, block selectors, storage policies or changes to the already-approved robe artwork.
