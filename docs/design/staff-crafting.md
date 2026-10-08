# Native staffs

**Owner-authorized implementation, October 7, 2026.** The owner assigned staff work to this chat and authorized retrieving the clothing chat’s proposal. The native staff now has persistent bindings, rituals, a selection menu and casting on the shared spell runtime. The separate chats still own wand components and clothing. See [development status](../development-status.md) for verification and shipment boundaries.

## Accepted rules

A staff is permanently tied to one namespaced trait and holds related native spells. A scroll can enter it only after the inserting player has identified its spell and only if that spell has a positive native base rating in the staff's trait. A Fire staff accepts identified Fire/Evocation scrolls, for example. Schools are ordinary traits. Fire 1 and Fire 4 both qualify; trait magnitude and rarity are not staff ranks. Stored augments or equipment contributions cannot add an otherwise missing qualifying trait.

The owner explicitly accepted these choices:

- Exclude **Amplify, Range and Area** as staff affinities. Schools, elements and other descriptive traits remain eligible. Bound spells retain their ordinary scaling traits.
- Begin with **two slots**, upgrade to **four**, then **six**.
- Each menu slot holds **one identified matching scroll**. Putting it onto the staff removes one item from inventory; taking it off returns its exact stored native spell/shaping variant. Swapping scrolls returns the old scroll to the cursor. This October 7 menu decision supersedes the earlier consuming ritual/no-refund proposal. Identification belongs to the inserting player, including when using someone else's staff.
- Casting pays the spell’s **normal shaped resource and preparation costs**, plus **staff durability based on native spell rarity**. Paid chaotic outcomes also spend durability. There is no additional wand-style one-minute recovery.
- A staff never identifies a spell. Successful scroll casting remains the sole identification route.

The controls are normal right-click to cast and **Shift-right-click** to open the staff menu. Click an occupied spell button to select it; click the adjacent scroll slot to insert, take or swap its scroll. Selection stays open and neither casts nor spends resources. Empty selection buttons are disabled, while their scroll slots accept identified matching scrolls. Shift-click from inventory distributes the stack into available empty slots, one scroll per slot, beginning with the selected slot if it is empty. Shift-click from the staff returns a scroll to inventory. Invalid insertion leaves the scroll where it was, without explanatory UI, chat or actionbar text. The menu preserves unknown spell concealment for saved or borrowed bindings and identified augment names.

## Initial tuning and construction

These executable recipes and numeric values are starting implementation choices for playtesting, rather than owner-approved final balance. The owner requires rarity-based durability and wear on chaotic outcomes. `StaffData.wear` explicitly interprets the trusted native spell rarity; item-owned metadata, trait magnitude and upstream reference rarity do not set wear.

| Native spell rarity | Durability spent per initial cast |
| --- | ---: |
| Common | 1 |
| Uncommon | 2 |
| Rare | 3 |
| Mythic | 4 |

Paid forfeits spend the same rarity-based amount, once, even when the intended spell is replaced by chaos. There is no additional chaos surcharge. The two/four/six-slot capacities currently have **40/80/120 maximum durability**. A capacity upgrade retains used damage, giving more remaining uses through the higher maximum. It does not repair away used damage. Failed payment and canceled preparation use no durability. Creative casting uses no durability.

Construction and capacity upgrades use an empty Spellstone center and the ordinary Plinth ritual. Each occupied seat holds one item. Embedded sockets remain installed; a successful ritual lifts ingredients and drops one output above the Spellstone. Cancellation or canceled output spawning leaves the original ingredients intact. The staff is a consumed offering replaced atomically by its resulting copy.

The recipe seats below are clockwise within each layer. For an inner cross, I0/I1/I2/I3 are north/east/south/west. An inner diagonal instead starts northeast and proceeds clockwise. O0/O1/O2/O3 use the same ordered directions of the outer layer’s shape. Rotate the entire arrangement by quarter-turns if desired; reflection is not equivalent.

| Operation | Active nodes | I0 | I1 | I2 | I3 | O0 | O1 | O2 | O3 |
| --- | ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| Construct empty two-slot staff | 8 | Any Mundane Staff | Iron Ingot | Empty | Ensorcelled Thread | Trait Fragment | Empty | Amethyst Shard | Empty |
| Expand two slots to four | 4 | Two-slot Staff | Diamond | Amethyst Shard | Ensorcelled Thread | Inactive | Inactive | Inactive | Inactive |
| Expand four slots to six | 8 | Four-slot Staff | Echo Shard | Diamond | Amethyst Shard | Iron Ingot | Ensorcelled Thread | Empty | Empty |

`StaffRecipe` is the authoritative matcher. It checks complete active-layer offerings and actual capacity, rejects ambiguous layouts, and requires at least one currently loaded compatible spell when constructing a trait staff. Construction consumes five items on eight nodes; its remaining three seats are deliberately empty. The first expansion uses only the inner layer, so unused outer offerings are irrelevant.

The mundane Staff supplies the shaft, Iron its retaining frame, thread the binding, Amethyst its focus and the fragment its fixed affinity. Diamond/Echo upgrades suggest increasing spell memory. Using Ensorcelled Thread does **not** inherit its wand profile. No foreign mod, upstream quality scale or legacy Wizardry component is required.

## Mundane Staff shafts

**Owner direction, October 8:** a Staff begins as a real, non-magical weapon. Seven vanilla-material variants are crafted in an ordinary crafting grid from two matching materials: Stick (**Wooden Staff**), Bamboo, Bone, Blaze Rod, Breeze Rod, End Rod or Lightning Rod. They consume one durability on each successful left-click attack. Holding right-click raises a weak, frontal Staff guard after the normal five-tick raise delay; it spends one Staff durability per guarded hit and does not use vanilla shield knockback, axe disabling or shield durability rules. It has no spell runtime, innate element or passive rider.

The Wooden Staff is the wooden-pickaxe reference point: **2 total melee damage**, **1.2 attacks per second**, and **40%** frontal reduction. Other mundane bodies get only a small physical tradeoff; none becomes a substitute for a sword or shield:

| Staff body | Total damage | Attack speed | Frontal reduction | Durability |
| --- | ---: | ---: | ---: | ---: |
| Wooden | 2 | 1.2 | 40% | 59 |
| Bamboo | 1 | 1.6 | 30% | 59 |
| Bone | 3 | 1.0 | 35% | 131 |
| Blaze Rod | 2 | 1.4 | 35% | 203 |
| Breeze Rod | 1 | 1.7 | 30% | 203 |
| End Rod | 2 | 1.2 | 45% | 250 |
| Lightning Rod | 3 | 0.9 | 45% | 250 |

Magical Staffs retain the Wooden Staff physical profile. Their chosen mundane shaft is a construction ingredient, not a hidden cast or melee bonus after construction.

This is the magical Staff's **Shaft** ingredient. The Shaft is the durable physical body and visual basis of the magical item. The fixed affinity fragment, magical thread binding and mounted focus remain separate construction roles. Any of the seven mundane Staff variants can occupy the Shaft seat; the initial magical Staff output keeps the shared magical Staff appearance and adds no shaft-specific cast bonus. Magical Staffs keep the same ordinary left-click combat behavior. Right-click casts the selected scroll and Shift-right-click opens the menu; if the selected slot is empty, holding right-click instead raises the same 40% frontal guard. A selected spell that fails payment or conditions remains a failed cast rather than becoming a guard. This replaces the earlier two-loose-Sticks construction input.

## Crafting and maintenance review — proposal

**Superseded repair proposal, October 8:** the owner now wants the [shared finite-use volatile repair catalyst](magical-repair.md). The older repair composition and half-durability recommendation below remain historical proposals; construction and expansion keep their current rules.

**October 7 owner request: settle crafting, upgrading and repairing.** The construction and expansion rows above are implemented provisional recipes; the maintenance rules below are proposed, not implemented or locked. Wand repair remains a separate undecided topic.

Recommend retaining the current construction/expansion compositions and using a four-node Spellstone ritual for Staff repair:

| Operation | Structure | Offerings | Output |
| --- | --- | --- | --- |
| Construct | Eight Plinths; five filled | Any Mundane Staff, Trait Fragment, Amethyst Shard, Iron Ingot, Ensorcelled Thread | Empty two-slot Staff with the fragment's fixed affinity; 40 durability |
| Upgrade 2 → 4 | Four Plinths | Staff, Diamond, Amethyst Shard, Ensorcelled Thread | Same Staff, four slots, 80 maximum durability |
| Upgrade 4 → 6 | Eight Plinths; six filled | Staff, Echo Shard, Diamond, Amethyst Shard, Iron Ingot, Ensorcelled Thread | Same Staff, six slots, 120 maximum durability |
| Repair — proposed | Four Plinths | Damaged Staff, Amethyst Shard, Iron Ingot, Ensorcelled Thread | Same Staff with up to half its maximum durability restored: 20 / 40 / 60 |

Construction/expansion retain their authored relative seats and allow whole quarter-turns. The proposed repair's clockwise inner seats are Staff, Amethyst Shard, Iron Ingot, Ensorcelled Thread, with an empty Spellstone center and inactive outer nodes. Amethyst restores the focus, Iron the frame and thread the bindings. No new consumable, additional affinity fragment or scroll is required for repair.

An upgrade preserves used damage, affinity, exact scroll variants, selected slot and other item components; the larger maximum supplies forty additional remaining durability. For example, 10 remaining of 40 becomes 50 remaining of 80. It does not replace the scrolls or reset the used-damage value. Repair should preserve the same identity/components and only reduce used damage, clamped at fully repaired. A full Staff should reject the repair without consumption. The normal atomic ritual lifecycle should protect the offered Staff and other ingredients when canceled or when output spawning fails. Geometry/imbuements should not add a second shaping bonus to stored scrolls.

**Exhaustion decision pending:** recommend retaining an exhausted Staff and all its scrolls at zero durability, disabling new casts until repaired while permitting selection/removal. The current implementation instead breaks the Staff at the last paid use. The owner was asked to choose between those behaviors; no exhaustion change is authorized by this proposal alone. A paid continuation must retain its original spell regardless of that choice.

**Implemented with Fluxed Flint October 8:** ordinary two-item crafting/grindstone/anvil combining is excluded from native Staffs. Ordinary two-item crafting repair must not become a second Staff route: Minecraft 1.21.1's `RepairItemRecipe.assemble` creates a fresh item and carries only repair values and curse enchantments, losing the native Staff's affinity and bindings. An implementation of the approved maintenance rules should exclude Staffs from that route rather than merge collections or silently discard scrolls. No additional repair UI, instruction messages or required foreign mod is proposed.

## Exact source variants and casting

Each binding stores the source scroll’s spell ID, trait modifiers, leyline shaping, Casting Cost multiplier and augment IDs/degrees. The immutable native compiler resolves that source once when casting. Binding and expansion do not add geometry or socket modifiers to an already-shaped spell. Capacity increases choice, not potency. Staff-specific shaping would require a separately authored rule.

The exact held staff and selected source are reserved throughout preparation. Swapping it, changing selection or altering components cancels before commitment. Casting uses the shared typed payment, preparation, volatility, continuation and cleanup rules. Ordinary spell cooldowns were removed on October 7, 2026; repeated casts still pay resources and wear once each. The casting-source guard currently rejects material costs targeting `vestige:staff`, preventing the reservation from consuming or damaging its own source.

Initial payment and deterministic rarity wear commit together, before the volatility roll. The final use still casts and breaks only the held staff, including when remaining durability is less than that cast's wear or the cast becomes chaotic; spare inventory staffs are untouched. Unbreaking randomness does not alter this wear. A continuation retains its original compiled source even after selecting another slot or breaking the staff. Recasts do not pay again or spend wear a second time. Scrolls and wands cannot steal a staff continuation, and empty-hand interaction can finish a paid continuation after the last use.

Only stored identifiers and bounded source metadata are trusted. A staff cannot supply an executable effect graph. Invalid versions, slot counts, selection, capacities, damage limits and injected executable fields reject. Namespaces remain open; only the three named scaling affinities are excluded.

A removed or newly incompatible spell remains saved in its slot but cannot cast. Save/reload, selection and expansion preserve that identity, other bindings, source shaping, cosmetic components and used damage. A six-slot staff cannot expand further. The owner rejects legacy migrations; this uses only version 1 native staff data.

## Menu and appearance

The item name and native menu title share one knowledge-aware name: **Fire Staff: Fireball**, for example. Selecting another binding updates the item name. Known source augments retain their italic names; unidentified sources show **Fire Staff: Unknown Scroll**. An empty selected slot uses **Fire Staff** alone. The affinity uses a readable, localizable trait name rather than its fragment glyph. There is no duplicate active-spell tooltip line. Long menu titles use an ellipsis, while the item name retains the full text.

The compact native menu has an **unnumbered scrolling list**, with at most three fixed-height rows visible. Every capacity slot has a real scroll slot beside a Minecraft selection button. An empty row shows the ordinary empty item square and a dimmed, disabled **Empty** button; placing a compatible identified scroll enables selection. Clicking its disabled button preserves the active spell. The active button has a persistent inset face, bright outline and pixel checkmark, distinct from ordinary hover/focus. The ordinary player inventory appears below. Long row names use an ellipsis and reveal the full name on hover. There is no permanent “Held” label; the carried item follows the cursor. The active name updates in the open menu as well as the held item. Scroll wheel and a draggable scrollbar expose the remaining slots.

Native container IDs and vanilla inventory packets own transfers and selection. The authoritative server checks the inserting player's current identification, the fixed base affinity and compiled stored shaping before accepting a scroll. All insertion routes share that check, including swaps, dragging and Shift-click. Client prediction also rejects unknown scrolls using its private identification snapshot. The opened staff is locked against inventory pickup and hotbar/offhand swapping. A change to the held source, slot, wear or components invalidates the menu before further mutation. Closing returns the ordinary carried item through vanilla inventory cleanup; stored staff slots are never dropped or refunded a second time. Reload closes open staff menus. The old nonce/select-only payloads and consuming scroll-binding ritual have been retired.

Each slot stores only trusted native spell metadata, rather than arbitrary copied item components. Removing a saved spell that is no longer loaded still returns its saved native scroll identity; it cannot be reinserted until a compatible definition is available. Removal does not identify it.

The first item art is an original **16×16** pixel grid: wooden shaft, restrained iron/thread collar and an Amethyst head. `tools/author_staff_texture.py --check` verifies its PNG. The standard native handheld item model supplies inventory, dropped and held presentation; no copied Iron asset or inherited renderer is used. This is an initial art direction, not an owner-selected final design. There is no extra HUD or instructional chat/actionbar text.

## Coverage and verification

[The generated report](../staff-trait-coverage.md) and JSON pools audit all 214 checked-in native profiles: 47 represented traits, of which 44 are permitted affinities. Fire admits 23 spells and Illusion 14. Amplify/Range/Area remain in the report as exclusion evidence. Datapack changes are evaluated against the successfully loaded server catalog rather than a frozen item list.

Model tests cover malformed data, exact shaping, affinity rejection, quarter-turn recipes, replacement and expansion. Minecraft tests exercise atomic construction/upgrades and real inventory insertion/removal/swaps, retained sockets, cancellation and canceled output spawning, payment/recovery/wear, preparation changes, final use, paid recasts, unavailable saved spells and authenticated menu selection. Actual client capture is separate evidence for appearance, packets and controls. Results are recorded in [development status](../development-status.md).

The optional JEI/EMI staff displays and a testing-pack installation are not part of this first gameplay implementation. Existing wand/clothing work is preserved in the shared checkout; this work does not publish their changes or claim their completion.
