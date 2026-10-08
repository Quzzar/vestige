# Native magical equipment

**October 8 next work:** the owner requested [shared item imbuements and independent readiness overlays](item-imbuement-and-readiness.md). Existing robes/devices need an audit and explicit crafted-variant support; remaining clothing should use that same path. The native overlay study comes before choosing its final appearance. This adds implementation scope without changing accepted unmodified item behavior or approving the remaining equipment packages.

**October 8 worn-art lock:** the owner approved the [mage armor v2 Wardweave and Cinderweave designs](../art/magic-equipment-armor-v2/approval.json), including dyeable cloth, fixed trim and both player models. These replace the rejected cape/skirt candidates. The earlier robe artwork comparisons below are historical; the other four clothing packages still require their own approvals.

The October 7 owner-approved [shared item magic foundation](item-abilities.md) now supplies traits, named variables, shared player modifiers and active/reactive/passive execution. The [review page](../art/magic-equipment-review/index.html) has proposed scaling controls based on [native-readable formula fixtures](../art/magic-equipment-review/ability-proposals.json). Wardweave and Cinderweave were subsequently locked in by the owner and are implemented with native crafting, armor, mana capacity, shared ability definitions and durability. Wayfarer is also approved and implemented; Dawnsight, Patchwork and Spiderstep remain proposals. Values below describe unboosted baselines.

The current review starts with [Wardweave and Cinderweave](robe-behavior-review.md): one-charge versus ongoing fire protection, damage ordering, equipment removal and shared armor/mana values. The owner accepted removing arbitrary trait-power ceilings and using durability, including wear on absorbed hits, as the ongoing cost. Ordinary compatible armor enchantments are permitted with Unbreaking and Mending excluded. The owner approved the two robe packages, including 80 durability and matching-thread quarter-capacity repair; their examples agree with production formulas.

**Owner-authorized planning and artwork, October 6, 2026.** This document separates accepted directions from proposed initial mechanics. The six item families below are in the requested art review. Wardweave and Cinderweave now have gameplay, worn textures and survival recipes; Wayfarer is also approved and implemented; the other three remain proposals. Native wand implementation is owned by the separate wand work; do not modify its compiler or reuse inherited Wizardry equipment.

**October 7 behavior review:** the owner requested continued artwork and explicitly retained final approval of exact item behavior. [The six-item review](../art/magic-equipment-review/index.html) pairs artwork with proposed stats, effects, controls, costs and recipes. Its [approval record](../art/magic-equipment-review/README.md) distinguishes accepted recipes from pending gameplay. Moving artwork forward does not authorize those mechanics.

## Accepted scope

- Review Wardweave Robes, Cinderweave Robes, Wayfarer Boots, Dawnsight Hood, Patchwork Robes and Spiderstep Boots. Produce four distinct inventory artwork options for each, based on Minecraft's actual item textures, with each option captured alone among vanilla items in Minecraft.
- Animated Rope and Revealing Lantern are outside this first set.
- Robes occupy the chest armor slot and provide little armor, extra maximum mana and an additional effect.
- Wardweave consumes four Wool, two Callous Thread, one Iron Ingot and one Pufferfish. Cinderweave consumes four Wool, two Smoldering Thread, one Blaze Powder and one Magma Cream. Both require all eight Plinth offerings, with an empty Spellstone center.
- All four wool offerings must have the **same vanilla wool color**. That color determines the finished robe's initial fabric color. Mixed colors reject before any consumption. The owner's correction supersedes the earlier any-mix proposal.
- Finished clothing may be recolored using normal Minecraft armor dyeing. Cloth color is cosmetic; identifying trim and symbols retain their own colors.
- Everything in this set should have a standalone craftable route. Iron's materials inform flavor and may later supply explicit optional alternatives; they are not dependencies.

The proposed shared robe values are **2 armor points, zero toughness and +25 maximum mana**, taking the unequipped native pool from 100 to 125 while a robe is worn. These are starting balance values for review, not verified encounter tuning. Leather's chest piece is 3 armor points; its full set is 7. One HUD shield icon represents two armor points.

## October 7 artwork preferences and fabric colors (historical for robes)

The owner selected **Wayfarer C, Feather Tabs**. Current preferences are **Wardweave B, Folded Mantle**, **Dawnsight D, Side Fold**, and **Patchwork C, Pocket Coat**. Those three are preferred directions, not final asset locks. **Cinderweave A, Coal Cuffs**, and **B, Brick Coat**, remain under comparison pending clearer recoloring examples. Spiderstep has no selected option.

The original twenty-four PNGs are full-color example palettes. They do not establish a shared white-wool baseline or implement actual dye layers. The blue Pocket Coat is an example blue palette, and the Brick Coat is an example red palette; neither fixes the garment's color independently of the recipe's wool.

The owner's request to see recoloring is now answered by the [native four-color comparison](../art/magic-equipment-dyes-v2/README.md). Five neutral adaptations of the shortlisted shapes establish a pale fabric baseline; Minecraft applies its actual white/red/blue/black texture tint to the same unchanged PNG/model for each shape. Fixed material UV planes retain the identifying detail colors. Thirty real native screenshots include all five side-by-side comparisons at GUI scales 3 and 2 plus twenty individual color screens. This is an inventory appearance proof, not production equipment dyeing or worn rendering. Black makes the fold contrast much subtler; the review preserves that result for the owner's choice. All preferences and the unresolved Cinderweave A/B choice remain unchanged.

For the next layered artwork, white wool should produce pale cloth with gray fold shading. All the garment's main fabric, including cloth sleeves, mantle and hood folds, takes the chosen color together. The identifying details below remain fixed. Preserve the current silhouettes and fold patterns when changing fabric color.

| Preferred or candidate artwork | Fabric that changes color | Fixed details | White-wool appearance proposal |
| --- | --- | --- | --- |
| Wardweave B · Folded Mantle | Robe body, cloth sleeves and folded mantle | Pale iron-gray edging and dark belt | Pale folded robe with gray shadows, iron-gray edging and dark belt |
| Cinderweave A · Coal Cuffs | Main robe and cloth sleeves, excluding ember cuffs | Ember-orange cuff tips, dark belt and brass clasp | Pale robe with orange cuffs; dark/black wool supplies the charcoal-body direction |
| Cinderweave B · Brick Coat | All red cloth in the coat and sleeves | Gold collar and dark brown belt | Pale coat with gold collar and brown belt; red wool supplies the red-body direction |
| Dawnsight D · Side Fold | Hood cloth and side fold | Ochre fastening and dark face opening | Pale hood close to the current ivory study, with the fixed fastening |
| Patchwork C · Pocket Coat | All blue-gray cloth in the body and sleeves | Brown pockets, ochre sleeve patch and brown belt | Pale robe with brown pockets and ochre patch; blue wool supplies a blue-body direction |

Wayfarer C is made from leather in the proposed recipe, so its brown body is not a white-wool output. If later leather dyeing is enabled for boots, dye only the leather body and keep its feather tabs ivory. Fixed patch colors identify Patchwork even when its cloth is recolored; those colors do not imply mixed wool ingredients.

For production, author the fabric as a neutral grayscale layer and the fixed details as a separate overlay. Applying a tint to the original red or blue full-color PNG cannot establish the intended white baseline or clean alternative colors. Recolor the neutral fabric in Minecraft while retaining its shade pattern; render the overlay without the fabric tint. The isolated proof uses native UV planes over the same PNG and preserves its original alpha. The [all-sixteen-color extension](../art/magic-equipment-dyes-v3/README.md) now verifies all vanilla wool tints on the five shapes in forty native screens at GUI scales 3 and 2, including every nonwhite hood boundary. Pixel-grid finishing, exact opaque production material masks and worn sheets remain future artwork work. Wardweave's current proof fixes its gray belt and dark neckline; the proposed pale iron collar edging still needs final material-mask authoring if that detail is retained.

## Recipe layout and colors

The proposed layout puts the four matching wool offerings on inner seats 0/2/4/6. Outer seats 1/5 hold the two matching thread items; seats 3/7 hold the two distinct defining reagents. Whole quarter-turns match the usual ritual convention. Keep the two defining reagents' order explicit so the matcher does not accidentally adopt a reflection rule. Recipe data and viewers should share one authoritative definition and show all eight offerings.

Four leather offerings replace wool for the boot proposals below. Leather starts in its ordinary brown family, with optional later dyeing. For wool recipes, resolve the sixteen registered vanilla wool item IDs to their DyeColor before matching; require all four resolved colors to agree. Do not assign a default color to an unrecognized addon wool. Wool item names, custom components and apparatus finishes do not pick a dye color.

Consume one item from each of the eight offering surfaces. Retain every installed imbuement; the already-created thread supplies the selected component identity. Recommend fixed equipment stats initially: the construction layout and incidental sockets should not silently apply scroll Spellshaping, reroll mana capacity or add equipment effects. Geometry-derived equipment properties would need their own explicit decision.

| Item | Inner offerings | Outer offerings | Status |
| --- | --- | --- | --- |
| Wardweave Robes | 4 same-color Wool | 2 Callous Thread + Iron Ingot + Pufferfish | Owner accepted |
| Cinderweave Robes | 4 same-color Wool | 2 Smoldering Thread + Blaze Powder + Magma Cream | Owner accepted |
| Wayfarer Boots | 4 Leather | 2 Laced Thread + Feather + Rabbit's Foot | Approved and implemented |
| Dawnsight Hood | 4 same-color Wool | 2 Consecrated Thread + Glow Ink Sac + Glowstone Dust | Proposed |
| Patchwork Robes | 4 same-color Wool | 2 Ensorcelled Thread + Ender Pearl + Chest | Proposed |
| Spiderstep Boots | 4 Leather | 2 Laced Thread + Spider Eye + Slimeball | Proposed |

Pufferfish follows the pinned [Protective Rune recipe](../research/iron-material-uses.md#school-focuses-and-rune-ingredients) and the native Pufferfish/Iron Warded pairing in [Spellshaping](../spellshaping-recipes.md). Magma Cream is vanilla's fire-resistance brewing reagent. These precedents justify ingredient flavor, not an inherited bundle of bonuses or upstream quality scaling. Wayfarer's recipe is approved and implemented; the other three new recipes remain explicit Vestige proposals.

## Proposed item behavior

All numerical values in this section are initial playtest proposals. Stored item identities select trusted native definitions. Implement effects through existing composed effects, conditions and typed payments where those consumers fit, rather than a second spell runtime. Equipment is not itself a spell and does not identify spells or teach recipe memory. No chat or actionbar instructions, activation errors or status messages are proposed.

### Wardweave Robes

Chest slot, 2 armor, +25 maximum mana. After a qualifying damaging hit, grant a **four-second, one-charge ward** that can prevent at most **2 HP** from the next damaging hit. The hit that creates the ward is not retroactively reduced. Start with a **twelve-second per-player recovery** after activation. No extra mana payment initially; the armor sacrifice is its basic tradeoff. Re-equipping another copy must not reset recovery or stack wards. Existing larger native wards should retain their own ownership and budgets; this equipment ward cannot overwrite them into a weaker ward or multiply mitigation through separate listeners.

Visual identity: plain cloth, restrained iron-colored seam or clasp. Activation gives one brief rune shimmer; absorption consumes the shimmer. Do not surround the wearer with a permanent particle cloud.

### Cinderweave Robes

Chest slot, proposed 2 armor and +25 maximum mana. At baseline, reduce damage tagged as fire by **25%**, with a **2 HP protection budget per damage event**. Fire scales the fraction toward complete prevention; Amplify scales the HP budget without an arbitrary upper ceiling. Prevented damage cannot exceed the hit. Lava and burning qualify by tag, and absorbed fire hits still wear this robe even when completely prevented or when their source normally bypasses armor wear. It does not prevent ordinary physical attacks or grant the Smoldering wand's ignition rider. No extra passive mana payment initially. Use the ordinary damage pipeline and keep enchantment/resistance ordering explicit so protection is applied once.

Visual identity: plain cloth with dull ember cuffs or hem and a small brass-colored fastening. A brief warm seam flash accompanies a prevented fire hit; no idle flames.

### Wayfarer Boots

Feet slot, **1 armor**, no maximum-mana bonus. While sprinting on the ground, a deliberate jump can trigger a **three-second 20% movement boost** for **5 mana**, with **fifteen seconds of shared recovery from activation**. Walk, normal jump and insufficient-mana behavior remain ordinary. During that burst, the next landing can prevent at most **4 HP of fall damage**, then spends that benefit; it expires with the burst rather than waiting indefinitely for a cliff. Do not refresh it from bunny-hopping, swapping boots or another movement bonus. The [coordinated imbuement review](wayfarer-imbuement-review.md) consolidates the package and proposes four compatible crafted trade-offs; new selectors and values remain for owner review.

The approved control is a grounded sprint-jump, avoiding a key per item. Keep collision, exhaustion and travel permissions normal. Native world tests must cover sprint/jump inputs, canceled jumps, swimming, mounts, flight, transitions and exactly one landing benefit before accepting this control.

Visual identity: familiar paired leather boots with muted green laces, rolled cuffs or small feather tabs. A short dust-and-feather cue marks a successful boost; avoid a constant trail.

### Dawnsight Hood

Head slot, **1 armor**; an optional **+10 maximum mana** is proposed, not an owner requirement. Holding sneak while stationary for **two seconds** activates a **six-second reveal** for **10 mana**, followed by **twenty seconds of recovery**. Reveal at most **eight invisible living creatures within twelve blocks**, with line of sight initially required. Ignore spectators and do not reveal through walls. Keep their invisibility intact; only this wearer should receive the outline. Ordinary Minecraft Glowing would expose the outline to other players, so reuse or extend the wearer-specific native presentation rather than adding a global status blindly.

Entering combat, movement, equipment removal or lack of mana cancels the preparation without payment. Recovery commits with a successful activation. A quiet pale-gold brow flash gives success feedback. This passive/sneak control is provisional and should be tested against ordinary crouching before implementation is locked.

Visual identity: a compact hollow cloth hood, recognizable against vanilla helmets, with a tiny gold brow mark. Its face opening remains dark; no face or floating eyeball appears in the inventory icon.

### Patchwork Robes

Chest slot, 2 armor, +25 maximum mana. Provide **four personal patch compartments**, each holding **one ordinary item** from a bounded initial palette: Torch, Ladder, Oak Boat and a normal Compass. This palette is proposed and may be revised before implementation. Each patch is filled by supplying its actual object. Unfolding releases that stored object and empties the compartment; it cannot synthesize a new item or reset a spent patch when dyed, repaired, moved or equipped by another player.

Propose a small native inventory interaction for selecting and unfolding patches, rather than automatically intercepting block use with an empty hand. The exact control needs review. Patch filling/unfolding should transfer atomically and preserve real item components. Initially reject containers, bound magical items and nested storage; do not accept arbitrary stacks or inventory graphs. A used slot can be refilled with its object without rebuilding the robe. The two Ensorcelled Threads in the main recipe supply the reusable magic; refilling does not consume another thread.

Patch contents travel with this individual robe. This differs from the implemented Crane Bag's shared attunement-channel storage. Use a small fixed-list item component with normal save/network bounds, no global bag channel, and ordinary item-drop ownership on unfolding. Preview art shows only sewn patch shapes, not an extra icon for every possible content state.

Visual identity: broad cloth shades with two to four small rectangular contrasting patches. No dense patchwork mosaic, tiny labels, ornamental tool collage or glint used to rescue readability.

### Spiderstep Boots

Feet slot, **1 armor**, no maximum-mana bonus. Holding jump against a solid wall permits a slow controlled climb at about **0.10 blocks per tick**, draining **2 mana per second** while actually climbing. Sneak pauses the climb while attached, continuing the drain. Releasing the control, running out of mana, losing a valid wall, entering water, mounting or unequipping ends attachment. Start with vertical walls only; ceiling walking is deferred. Do not bypass protected movement regions or unloaded collision.

Charge through a shared server-owned equipment ability, with an accumulated fractional payment and a defined initial affordable check. Avoid free sub-second climbs from repeatedly releasing jump. Existing Spider Climb spells retain their authored behavior; these boots provide a constrained equipment route, not a permanent stronger replacement.

Visual identity: paired dark leather boots with restrained burgundy laces or one simple crossed cuff seam. No spider-shaped silhouette or high-detail web lattice. Tiny contact cues appear only while climbing.

## Mana, wear and recoloring implementation plan

The current native pool has a fixed 100 maximum. Equipment capacity is new work: resolve the equipped item's trusted bonus on the server and expose the resulting capacity to recovery, payments, actor/max-mana conditions, operator tools and private balance synchronization. The owner-approved display now shades each casting item's mana affordability rather than drawing a pooled mana gauge. Preserve that display: a larger pool changes available balance, not a spell's next payment price. Do not add a pooled HUD or change regeneration speed simply because capacity rises.

Equipping capacity increases does not refill mana. Removing equipment clamps excess current mana to the new maximum once. Login, nondeath clones, save/reload and dimension changes retain the current balance within valid capacity. Death/respawn follows the existing full-pool policy with the equipment that is actually retained. Capacity from one chest item applies once. Later equipment capacity bonuses should compose explicitly without a hidden global multiplier.

Use native ArmorItem equipment slots. The owner accepted finite durability on every piece, normal armor wear plus exactly one robe wear charge for a hit its own magic absorbs, and no separate activation or passive-uptime drain. Calculate protective wear from the hit before the robe's own reduction so full prevention does not make it unbreakable. Initial durability totals, toughness and repair quantities remain unselected. The review proposes leather-equivalent totals (80 robes, 55 hood, 65 boots) and matching magical-thread anvil repair restoring up to a quarter per thread. The accepted enchantment policy permits ordinary compatible armor enchantments but excludes Unbreaking and Mending, preserving authored wear and material repairs. Enforce that exclusion across all equipment acquisition/combination paths; no current wand/staff gameplay changes here. Dyeing copies the existing stack and changes only the dyed-color component, preserving wear, patch contents and trusted item identity. Equipment recovery lives on the actor so copying or recoloring an item cannot reset it.

Artwork should ultimately separate a neutral tintable fabric layer from a fixed-color overlay for cuffs, fastenings and patches. Both inventory and worn artwork need this separation. The selected concepts in [the art review](../art/magic-equipment-v1/README.md) are full-color appearance proposals, not ready-to-ship layered armor textures or proof of worn rendering. Minecraft 1.21.1's native ArmorDyeRecipe, DyedItemColor component and NeoForge armor tint hook support this approach. Keep vanilla dye mixing, including mixing with an existing dye color, and avoid a separate robe recoloring recipe system.

## Verification required before gameplay shipment

- Ritual matching: each of sixteen matching wool colors, every mixed-color rejection, correct eight nodes and exact quantities, whole quarter-turns, reflection policy, canceled reservations, retained socket materials and one atomic centered output.
- Mana: equipment changes without refills, clamping, recovery above 100, payments across the old boundary, combined approved bonuses, death/login/clone behavior and a correctly synchronized capacity gauge.
- Effects: meaningful native world behavior for ward ownership/recovery, fire tags/mitigation order, sprint-jump activation and one landing budget, wearer-private reveals, atomic patch transfers, climbing collision and payment.
- Dyeing: color follows wool, later dyeing preserves all gameplay components, fixed trim remains distinct, inventory/held/worn models look correct on both player model types and multiple GUI scales.
- Compatibility: optional viewers use public recipe entries without exposing concealed spell recipes; standalone crafting and normal armor use remain available without Iron. Include Kithkyn co-loading and current NeoForge packaging checks.

This pass implements none of those equipment mechanics. It provides the planning and native inventory artwork evidence requested while the owner is away.

## Separate native staff implementation

The owner authorized fixed-trait native staffs on October 7. [Staff crafting](staff-crafting.md) is authoritative for accepted affinity, two/four/six slots, real-scroll menu insertion and return, selection, shaped costs and deterministic wear. It supersedes this document's earlier staff proposals. Staff work is owned by its separate implementation chat; clothing review does not reopen its accepted rules or authorize broader equipment progression.
