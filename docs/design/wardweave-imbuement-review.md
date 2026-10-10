# Wardweave: crafted imbuement package

**October 9, 2026 — owner-approved and implemented.** The owner asked this chat to take Wardweave next if another agent was not actively working on it, then move to Crane Bag. The robe chat confirmed it owns Cinderweave and that Wardweave is free. The owner approved the presented package on October 9: “Yeah, I think this is cool. This is great.” [The approval record](wardweave-imbuement-approval.json) pins the values and complete review presented before approval. Production behavior and native client verification are complete. Cinderweave's separately approved package stays with its owning chat.

[Exact values](wardweave-imbuement-values.json), [complete naming entries](wardweave-adjective-proposal.json), and [offline verification](../verification/wardweave-imbuement-review-2026-10-09.json) preserve the reviewed package.

## Keep the existing ward

The shipped robe grants one charge after a hit deals positive health damage. That charge lasts **four seconds** and protects the next eligible hit by **up to 2 HP, one full heart**, before ordinary armor mitigation. Recovery starts at formation and lasts **twelve seconds**. The triggering hit cannot use the new charge. Small eligible hits consume the whole charge, occupied charges cannot refresh or stack, and the hit that spends a charge cannot rearm it.

Keep 2 armor, zero toughness, +25 maximum mana, 80 base durability, current armor wear, same-color wool crafting, dyeing, approved artwork and the existing enchantment policy. Equipping does not refill mana. The ability remains free of mana costs; idle time, activation and expiration do not acquire new durability charges. Neither these variants nor their names add a new implicit trait behavior.

## Four compatible choices

Each choice is optional and degree one. A material embedded in the Plinth affects the offering on that Plinth; it is not another consumed ingredient.

| Adjustment | Offering / embedded material | Benefit | Trade-off | Alone at neutral wearer traits |
| --- | --- | --- | --- | --- |
| **Warded** | Pufferfish / Iron Block | Force ×1.5 | Time ×0.8 | Up to **3 HP** protection for **3.2 seconds**, twelve-second recovery |
| **Enduring** | Any one matching Wool / Amethyst Block | Time ×1.5 | Recovery ×1.2 | **Six-second** charge, up to 2 HP, **14.4-second** recovery |
| **Quickened** | Either Callous Thread / Copper Block | Recovery ×0.8 | Amplify ×0.8 | **9.6-second** recovery, up to **1.6 HP**, four-second charge |
| **Reinforced** | Either Callous Thread / Iron Block | Maximum durability ×1.5 | Amplify ×0.8 | **120 durability**, up to **1.6 HP**, four-second charge and twelve-second recovery |

Warded favors a stronger immediate follow-up defense. Enduring gives a longer opportunity to use it. Quickened can arm sooner after recovery. Reinforced extends the garment's service life. Taking every choice preserves all disadvantages rather than making every number better.

Iron/Pufferfish follows the existing Protective Rune and Warded material direction. Copper's recovery association and Iron's durability/protection association inform the other choices. The Amethyst duration contribution is a native Vestige Enduring convention; it is not a claim that Iron assigns Amethyst a universal Time property. All coefficients and offering/material routes here are native balancing decisions, independently of upstream quality values. See the pinned [material reference](../research/iron-material-uses.md).

## Compose through the shared trait runtime

Resolve wearer and robe-local contributions once. Protection is `2 × resolved Force × resolved Amplify` HP. Lifetime is `4 × resolved Time` seconds. Abjuration keeps its descriptive/filter role. Recovery uses the shared typed cost adjustment, separately from Time. Durability is an authored carrier property and does not scale with temporary wearer traits.

Compose recovery factors before final tick rounding. Enduring + Quickened gives `240 × 1.2 × 0.8 = 230.4` raw ticks, rounded once to **230 ticks / 11.5 seconds**. Lifetime uses the existing binding tick conversion and work bound. This package introduces no tuned cap on trait growth; prevention still cannot exceed the eligible hit.

Protection and lifetime snapshot when the ward forms. Wearer/ability recovery survives swapping, dyeing or replacing the robe. A shorter-recovery copy cannot shorten an already running recovery. Removing or replacing the worn robe clears its charge. A long charge outlasting recovery still cannot stack or refresh.

For example, Warded + Reinforced has **2.4 HP** protection and a **3.2-second** lifetime. A wearer with Force ×1.25, Amplify ×1.25 and Time ×1.25 gets **3.75 HP** and **four seconds**, composed once at formation.

## Same ordered recipe, reachable combinations

Use the current eight-offering structure with an empty Spellstone. The four inner Plinths hold four matching-color Wool. Two opposite outer Plinths hold Callous Thread; the remaining opposite outer Plinths hold Iron Ingot and Pufferfish. A reference orientation is:

| Seat | Offering |
| --- | --- |
| Inner 0 / 2 / 4 / 6 | Four same-color Wool |
| Outer 1 / 5 | Callous Thread |
| Outer 3 | Iron Ingot |
| Outer 7 | Pufferfish |

Whole quarter-turns preserve the layout and each local offering/material pair. No compass direction is special. Equal thread offerings can exchange places; Enduring can use any one of the four matching wool offerings. With repeated ingredients, a reflected arrangement that is identical to a valid whole rotation remains valid; do not invent chirality that the recipe cannot express. Incorrect layer placement, adjacent rather than opposite threads, mixed wool colors, wrong items or counts reject before consumption.

Quickened + Reinforced uses Copper under one thread and Iron under the other. The sockets remain installed. Two Wool/Amethyst pairs, two Thread/Copper pairs or two Thread/Iron pairs propose the same choice twice and reject before consumption; this initial palette has no stronger duplicate degree. An empty or unrecognized local pair is neutral. A recognized common Spellshaping pair without a compatible Wardweave consumer rejects silently rather than pretending to imbue the robe. Known spell riders are not imported into this item's ward.

Body dye, stone finish and other leyline geometry do not add a second robe shaping contribution. Four matching Wool colors determine dye; every color has the same imbuement effects.

## Durability and repairs

Plain, Warded, Enduring and Quickened keep **80 maximum durability** and **20 repaired durability per Callous Thread**. Any set containing Reinforced has **120 maximum durability** and **30 repaired durability per Callous Thread**. Repair is one quarter of the authored maximum, clamped to missing damage. It preserves the full variant, dye, name and other components. Changing maximum durability must preserve used damage rather than refill the robe. The existing Fluxed Flint repair route remains separate and preserves exact target data.

## Complete names and outcomes

One single adjective or one full-set combination adjective precedes Wardweave Robes. Only the adjective is italicized; the existing item rarity/color and base name stay intact. Names are presentation aliases and do not replace mechanics. Actual contribution IDs remain the saved identity.

The earlier three-choice name **Staunch** conflicts with the now-approved Cinderweave Repelling + Enduring alias. This package uses **Unflinching** for Warded + Quickened + Reinforced instead. Tireless, Stalwart, Dependable and Resolute reuse existing words for exactly the same constituent adjective sets.

| Full-set adjective | Contributions | Charge lifetime | Protection budget | Recovery | Durability |
| --- | --- | ---: | ---: | ---: | ---: |
| Plain | None | 4 s | 2 HP | 12 s | 80 |
| Enduring | Enduring | 6 s | 2 HP | 14.4 s | 80 |
| Quickened | Quickened | 4 s | 1.6 HP | 9.6 s | 80 |
| Reinforced | Reinforced | 4 s | 1.6 HP | 12 s | 120 |
| Warded | Warded | 3.2 s | 3 HP | 12 s | 80 |
| Tireless | Enduring + Quickened | 6 s | 1.6 HP | 11.5 s | 80 |
| Stalwart | Enduring + Reinforced | 6 s | 1.6 HP | 14.4 s | 120 |
| Dependable | Quickened + Reinforced | 4 s | 1.28 HP | 9.6 s | 120 |
| Vigilant | Warded + Enduring | 4.8 s | 3 HP | 14.4 s | 80 |
| Watchful | Warded + Quickened | 3.2 s | 2.4 HP | 9.6 s | 80 |
| Fortified | Warded + Reinforced | 3.2 s | 2.4 HP | 12 s | 120 |
| Resolute | Enduring + Quickened + Reinforced | 6 s | 1.28 HP | 11.5 s | 120 |
| Untiring | Warded + Enduring + Quickened | 4.8 s | 2.4 HP | 11.5 s | 80 |
| Indomitable | Warded + Enduring + Reinforced | 4.8 s | 2.4 HP | 14.4 s | 120 |
| Unflinching | Warded + Quickened + Reinforced | 3.2 s | 1.92 HP | 9.6 s | 120 |
| Unshaken | Warded + Enduring + Quickened + Reinforced | 4.8 s | 1.92 HP | 11.5 s | 120 |

## Verification and implementation

The production matcher accepts **4,480 authored layouts** covering all sixteen matching wool colors, four whole rotations, every allowed Wool/Amethyst position, either thread position and both orders of the two different thread materials. The earlier [offline design receipt](../verification/wardweave-imbuement-review-2026-10-09.json) preserves the proposal calculations and checks that the Wardweave and Cinderweave names coexist. Its original source hashes remain historical approval evidence.

The implemented palette uses shared bounded item selections, source-local trait/cost composition, atomic ritual input snapshots and exact full-set naming. Actual tests cover all sixteen protection/recovery/durability outcomes, duplicates and unsupported pairs, invalid saved families/degrees/budgets, wearer composition, snapshots, expiration, robe swapping, in-place variant edits, breakage, dyeing, repair, renaming, save/load and socket changes during crafting. Existing plain-color viewer IDs remain valid; all **256 color/variant patterns** resolve to their real crafted outputs. No separate recipe registration is needed per viewer.

The full local run passed **221 model tests** and **385 Minecraft behavior tests** with Kithkyn co-loaded. [Native client evidence](../art/wardweave-imbuements-native/README.md) records the actual inventory, adjective styling, unchanged loaded artwork, protected wear and the private recovery display. This establishes the implemented package and its tested behavior; survival balancing and multiplayer playtesting remain normal follow-up work.

The earlier combined [robe draft](robe-imbuement-review.md) is historical context. This file is the current accepted Wardweave package. The separate Cinderweave implementation is coordinated through the robe chat; this scoped change preserves its baseline without replacing that chat's newer work. Crane Bag follows after Wardweave.
