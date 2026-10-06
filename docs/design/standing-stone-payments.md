# Standing Stone payments

**First XP rate accepted October 6, 2026; alternate resource routes remain proposed.** The owner accepts distance-based pricing for all payment resources, including XP, hunger, health and mana, and the **imbuement in the Plinth holding the Ender Pearl** as the payment selector. Same-dimension travel is accepted. The owner subsequently accepted the ordinary XP-point formula below as the first implementation. Other resource conversions and discounts remain proposals.

## Accepted interaction

Distance sets a common travel budget, with a separate conversion rate for each resource. **The Plinth holding the Ender Pearl selects the crafted device's payment route through its installed material**. The device recipe already has exactly one Pearl, so this gives it one unambiguous travel contribution. The stone retains that payment configuration separately from its copied attunement key. The Plinths holding the shard and masonry do not need payment imbuements or supply stacked travel discounts. The body finish follows the two matching masonry offerings, independently of payment.

This accepted use of the existing offering/material pairing system supersedes the earlier recommendation of an additional socket on the finished Standing Stone. The first implementation uses ordinary XP. Material-selected payment routes are not implemented yet. A later device-augmentation ritual could change a crafted stone's payment configuration while retaining its network key. No such recrafting route is currently implemented.

The payment configuration must not alter the copied key or reinterpret the shard's original Plinth imbuements, which already participate in reproducible identity. A shard sets the network; the separately authored Pearl/material contribution sets travel payment. Two stones can share one network and have different payment configurations.

Use straight-line three-dimensional block distance between the two stones. A modest base fee discourages repeated tiny hops; square-root growth keeps a remote base useful. The owner accepted this first fare, expressed in XP points:

`B(d) = 12 + 12 × sqrt(d / 1024)`

Apply a route conversion/discount to this unrounded budget, then round the final charge once to the nearest whole payment unit. Keep a minimum positive charge. This is the accepted first XP fare. It is not a calibrated exchange rate between XP and renewable resources.

| Distance | Ordinary XP | Lapis proposal: 25% less XP |
| --- | ---: | ---: |
| 64 blocks | 15 | 11 |
| 256 blocks | 18 | 14 |
| 1,024 blocks | 24 | 18 |
| 4,096 blocks | 36 | 27 |
| 16,384 blocks | 60 | 45 |

XP **points** make discounts and conversions predictable. Vanilla levels contain different amounts of XP at different levels: the inspected Minecraft 1.21.1 `Player.getXpNeededForNextLevel()` uses `7 + 2L` below level 15, `37 + 5(L−15)` below 30, then `112 + 9(L−30)`. Charging whole levels therefore prices the same trip differently depending on the traveler's current level. If the owner prefers level payment, use explicit whole-level pricing rather than silently presenting XP points as levels.

For implementation, available XP must be computed from the current level/progress, not trusted solely from `totalExperience`: vanilla enchanting and level changes can leave that historical counter unsuitable as the player's current balance. XP removal must also account for NeoForge's cancellable XP events before a trip can commit.

## Route design

| Material installed in the Pearl's Plinth | Crafted payment route | Reasoning |
| --- | --- | --- |
| Empty | Ordinary XP | Accessible default with no further configuration |
| Lapis Block | XP discount; 25% is the proposed starting amount | The pinned Iron Jewelry Lapis channel concerns experience; the teleport discount is a native Vestige choice |
| Amethyst Block | Mana payment | Follows existing Amethyst/mana flavor; rate needs playtesting |
| Moss Block | Hunger payment | Follows Vestige's existing hunger/nourishment flavor; rate needs playtesting |
| Soul Sand | Health payment | Follows Vestige's existing sacrificial/Bloodbound socket flavor; quotes show hearts, with exact native points in narration, and affordability must leave the player alive |

These are native travel rules, not claims that Iron assigns these exact effects to those blocks. Refer to the [complete upstream material reference](../research/iron-material-uses.md) and existing [Spellshaping pairings](../spellshaping-recipes.md). Core routes must remain vanilla-accessible; future optional Iron alternatives should feed the same native routes.

The socket eligibility table currently belongs to executable Spellshaping rules. Payment implementation must explicitly add any newly supported travel material, including Lapis Block, to the shared eligibility check without granting it a fabricated spell augment. Do not assume that the route table above already makes these payment pairings functional.

Begin with one selected contribution and one route, rather than stacked discounts or automatic substitution. The source stone's retained configuration decides payment; the destination does not apply an additional hidden discount. Arrival direction can therefore have a different resource choice when two stones were crafted with different Pearl/material pairings. XP/mana amounts appear before their resource mark on the right of each button. Health/food show the actual full/half icon count, with underlying integer points retained in narration. There is no Travel confirmation or separate travel-cost line. A destination without enough of the selected resource is disabled. Clicking an affordable destination travels directly, with server-side affordability and price checks. Do not switch to health automatically when another payment is unaffordable.

Hunger/health need affordable long-trip curves so those modes do not become unusable solely because their reservoirs are smaller. Their conversion coefficients or caps require their own playtest decision; 1 XP must not be assumed equivalent to 1 mana, hunger point or heart. Mana and food replenish differently from earned XP, and health has combat risk, so numeric equality would not establish balance.

### Native cost presentation

The [current native icon/corner review](../art/standing-stone-icons-corner/README.md) uses number-then-mark for XP/mana and counted full/half sprites for health/food. **Two health points draw one full heart; five draw two full hearts and a half.** Food uses the same two-point convention. Exact points remain in tooltip/narration. Rows reserve the exact width of their cost, truncating only the destination name, and disabled costs dim. Every button is twenty GUI pixels high; six rows per page keep that height consistent in compact windows. The shared [mana standard](mana-display.md) also defines the bottom-right horizontal HUD. Prior numeric heart/food previews and centered mana are historical.

XP remains the only live payment. The opt-in native capture fixture supplies illustrative health/mana/hunger amounts and balances; it cannot send travel, rename or paging requests. These screenshots do not establish conversion rates, nonlethal health affordability, food-point accounting or material-selected payments. A later payment implementation must connect those units to server-authored quotes and independently verified resource transactions.

## Transaction requirements

Resolve destination membership and a live arrival through the shared `NearbyTeleport` helper first. The accepted October 6 policy deliberately allows an occupied nearby fallback when no opening exists. Then calculate the latest quote from actual source/destination positions and source payment configuration; verify the player can pay. Charge and teleport on the server without an intervening tick, and retain a refund/rollback path if teleportation is canceled or fails. A rejected request, changed payment configuration, broken stone or unavailable arrival must not spend anything. A future mutable payment configuration must refresh its displayed quote or safely reject a stale request; the selected UI has no separate Travel confirmation.

Travel-cost imbuement is a new device mechanic, not an additional inherent semantic trait. Existing scroll costs and Spellshaping remain unchanged. The first XP implementation replaces free Standing Stone travel. It debits current XP points through the shared event-aware payment helper, restores payment when a payment event invalidates an endpoint or when transfer fails, and consumes the source session only on success. New survival/world and native menu verification are tracked in development status. Material discounts and health/hunger/mana routes remain deferred.
