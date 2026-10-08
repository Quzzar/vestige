# Spellshaping through imbued Plinths

**Implemented October 4, 2026:** 50 individual augments and six compounds. The [executable recipe ledger](../spellshaping-recipes.md) lists every supported pairing. The owner approved removing the unimplemented proposal catalog; there is no additional augment backlog in the canonical catalog.

## Crafting and automatic selection

Match the ordinary spell recipe first, preserving its ingredients, relative positions and whole quarter-turn equivalence. An ingredient placed on a Plinth is its offering. A material installed in that Plinth's side socket can shape that offering. These are separate retained inputs: crafting consumes offerings and preserves socket materials. Four-slot recipes ignore the complete outer layer.

**Owner restriction and color clarification, October 5:** sockets accept only block materials named in the executable Spellshaping rules, including compound-only pieces. The current set contains 26 material families: all 16 wool colors share White Wool's routes, and all 16 concrete colors share White Concrete's routes, for 56 accepted block IDs. Color is cosmetic for both Spellshaping and Attunement keys. Installed stacks and retained shard nodes preserve their actual color for display and recovery; matching/hash encoding uses the common family identity. Carpets, concrete powder and other unlisted materials remain unsupported. Unsupported blocks reject without consumption, becoming top offerings or falling through into vanilla placement beside the Plinth. Client interaction and server storage use the same eligibility check. Top recipe offerings remain independent of socket eligibility. A supported material with an unmatched offering remains neutral.

Each exact offering/material pairing selects one automatic rule. Several alternative pairs can select the same rule. Authoring and loading reject a pair assigned to two individual rules. There is no choice menu or random tie breaker. An unknown decorative pairing contributes nothing; a recognized pairing that cannot meaningfully modify the spell rejects safely before payment or ingredient consumption.

A complete compound pattern replaces its participating individual contributions with one named rule. Larger patterns match first; equal-size patterns use stable rule identifier order. Each physical node can participate once. Degree counts repeat compatible contributions: ordinary at one, Greater at two, Grand at three or more. Per-rule degree limits prevent saturated or unsupported results. Combined physical contributions fit within the eight active nodes.

Authored ingredient **roles are descriptive recipe clues**, not runtime selectors. Matching uses actual item/material identities. Compatibility reads the spell's executable plan, including callbacks and bindings. School names and recipe role labels do not grant an effect.

For example, Fireball keeps Paper, Blaze Rod, Gunpowder and Emerald in their ordinary relative order. End Stone with Blaze Rod or Emerald selects Reaching; Bone Block with Gunpowder selects Bleeding; Copper Block with Emerald selects Shocking. Keeping those local pairs together during a whole rotation preserves the result. Moving a socket material to a different offering can change it.

## Shared compilation and payment

A crafted scroll stores its base spell ID, v3 leyline modifiers and bounded version-1 augment IDs/degrees. Trusted packaged rules compile an immutable view for that cast. The item's data cannot supply arbitrary effects or costs. Original rarity, provenance, relative trait units and published definitions remain intact. A reference scroll provides clues and failure risk; it does not copy its existing augments into the output.

**October 8 naming direction:** the shared [adjective catalog](../magic-adjectives.md) inventories all fifty individual words and six mechanical compounds, plus wand/item vocabulary. Every mixed spell/wand set displays one italicized adjective: its exact authored full-set alias, or Confluent for other mixtures. Single adjustments retain their existing degree labels. This presentation does not replace independent effects or change stored scroll identity, stacking, binding or identification. See the [naming convention](magic-adjectives.md).

Rules use ordinary trait ADD/MULTIPLY operations and explicit composed effects. New consumers normalize against the original spell's trait rating, at least one, and explicitly seed missing traits. Existing consumers still see the same resolved traits. Volatile remains the sole trait with inherent engine semantics.

Typed cost factors and additions combine before the common layout Casting Cost multiplier. Existing mana, health, hunger, time, cooldown and material quantities retain their own units. Final amounts round once; health payment rounds in whole hearts. Bloodbound/Fasting exchange adjusted mana for health/hunger and share a maximum 75% exchanged portion. The [shared resource valuation](resource-payments.md) charges one heart per thirty exchanged mana or one food point per 7.5, rounded up. Exhausting now uses Amplify ×1.25 / mana ×1.35 per degree, replacing its ineffective cooldown drawback. Insufficient payment rejects atomically.

**Owner-directed rebalance, October 8:** the shared starting ratio is **1 heart = 2 full hunger icons (4 food points) = 30 XP points = 30 mana**. Homebound Eye uses the same valuation. Exhausting's former cooldown trade is superseded by +25% Amplify / +35% mana per degree. The [audit](../ritual-shape-and-payment-audit.md) retains the pre-fix findings; [resource payments](resource-payments.md) records current math, rounding and future device constraints.

## Conditional effects and ownership

Damage/healing extensions require positive actual primary contact. Every augment permits one initial contact per recipient and eight per cast across all deliveries, callbacks and pulses. Secondary plans retain causal lineage and scoped contact facts through delays, cannot retrigger another Spellshaping rider, and use shared finite budgets. Failed or fully blocked contacts do not emit riders.

Protection extensions attach to the actual protected living recipient, with owned duration, charges and cleanup. Enduring changes both runtime and backing lifetime for eligible finite manifestations; it does not independently extend event bindings. Delivery shaping respects native penetration, homing and recipient caps and rejects saturated paid no-ops.

## Newly supported pairings

| Augment | Offering / socket material | Actual behavior and compatibility |
| --- | --- | --- |
| Revealing | Glowstone Dust / Glowstone, or Compass / Glowstone | After a supported detection action or Glowing application, outline nearby invisible living creatures without removing Invisibility. Eight unique creatures per cast, nearest first; baseline five-block radius and 88-tick outline after trait resolution/rounding. Radius and duration remain bounded. Detect Magic's Compass route preserves its ordinary recipe. |
| Anchored | Ender Pearl / Lodestone | Stop an eligible moving area's motion/following while preserving its actual spatial pulses, duration and cleanup. Gravity Fissure and Planar Sight have playable routes. Target-locked swarms and stationary fields reject; merely freezing their visual marker would not anchor their outcomes. |
| Reflecting | Glass / Diamond Block | Add a three-second halo to a supported protected recipient. Return up to two incoming vanilla arrows, spectral arrows or tridents per cast at their original speed, toward a living shooter or backward if no shooter remains. Greater permits three. Friendly, stationary and outgoing projectiles are excluded. Glass Shield has a playable route. Native spell projectiles and foreign deliveries are excluded until ownership/targeting adapters exist. |

Revealing detects living invisible subjects within its sphere, including through walls; it is not a global reveal or an invisibility dispel. Reflecting's budget is shared across all protective recipients and pulses, not reset every tick. Returned projectiles carry secondary lineage until they are removed; their tracking is pruned and cleared on world close.

## Authoring and verification

Author all implemented routes, bounds and outcomes in `tools/author_spellshaping.py`. Regenerate its data and ledger, then run `--check`. Every rule must have at least one compatible route through an unchanged native base recipe; the catalog test compiles those routes and writes a compatibility audit. Matching ingredient names alone are not proof of gameplay support.

Run model/runtime tests, native GameTests and a build. Inspect native client captures before claiming presentation verification. Record actual results and limitations in [development status](../development-status.md). The [pinned material reference](../research/iron-material-uses.md) separates upstream facts from native interpretations: materials inspire contributions, while Iron quality values remain inert. Vanilla routes are standalone; explicitly accepted Iron offering aliases are optional.
