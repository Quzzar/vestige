# Standing Stone payment imbuements

**Approved implementation, October 10, 2026.** The owner approved the complete five-route package below. Crafting, saved route identity, shared typed payments, native menus and dynamic recipe viewers are implemented; verification and release evidence are recorded in [development status](../development-status.md). Starting coefficients remain subject to playtesting.

## Crafting and identity

Keep the four active inner offerings in relative cyclic order:

**Attunement Shard → matching masonry → Ender Pearl → matching masonry.**

Whole quarter-turns are equivalent. The two masonry ingredients are identical, so reflecting this particular pattern is equivalent to a rotation. Mixed masonry, wrong cyclic order, extra active offerings and multiple items on one offering surface reject. Outer nodes on an eight-node structure remain inactive for this recipe. The Spellstone's reference surface is empty.

Read the installed material from the Plinth that actually carries the Ender Pearl after matching the rotated pattern. The selector must move with that offering; it is not a fixed north/east/south/west slot. The shard and masonry sockets remain neutral for device payment, and materials remain installed after crafting. An unsupported material in the Pearl's socket rejects the recipe silently rather than producing an ordinary XP stone. Other socket materials must still satisfy normal Plinth eligibility.

Copy the verified full shard key unchanged. The source shard's blueprint and imbuements establish network identity; the device recipe's selector establishes payment independently. Two stones with the same key and different payment routes remain connected. Masonry, payment choice, local geometry and crafting location do not recompute the key. The existing 36 finishes, sixteen key-selected silhouettes and signature runes keep their present behavior.

## Five mutually exclusive routes

| Material in the Pearl's Plinth | Stored route | Effect | Item adjective |
| --- | --- | --- | --- |
| Empty | Ordinary XP | Existing distance fare in XP points | None |
| Lapis Block | XP + mana | 25% less XP, plus mana at one-third of the unrounded ordinary budget | Erudite |
| Amethyst Block | Mana | Pay the same budget in mana | Charged |
| Moss Block | Hunger | Convert the budget into food points | Fasting |
| Soul Sand | Health | Convert the budget into health points | Bloodbound |

These are native Vestige rules. The material reference supports flavor associations; it does not establish these exact travel effects in Iron. The owner's October 10 revision replaces the strictly better Lapis discount with a resource trade-off: saving 25% XP also spends mana. Its exchanged portion carries an explicit 4/3 premium, giving 75% XP + one-third mana, or 13/12 of the ordinary budget before rounding. Ordinary stones require no mana. Other routes change the resource being sacrificed. Erudite saves XP at an actual mana use cost; acquisition expense alone does not balance an upgrade.

One stone has exactly one route at degree one. Erudite is one authored route with paired XP/mana costs. There is no player-selected mixture, repeated-degree stacking or automatic fallback to another resource. The departure stone chooses the route; the destination contributes no discount or second charge. For example, a Charged stone can send someone to a Bloodbound stone using mana, and the return trip can cost health.

Use the shared `MagicAdjectives` vocabulary with distinct `standing_stone/*` contribution identities. Only the adjective is italicized. Route naming covers five outcomes on all 36 finishes. Endpoint location labels remain separately editable player names; renaming a destination cannot change its payment or key. Keep the name/signature item presentation concise and explain costs through the existing resource symbols in the destination menu.

## Distance budget and rounding

Keep the accepted straight-line three-dimensional distance budget:

`B(d) = 12 + 12 × sqrt(d / 1024)`

Retain the shared valuation: **1 full heart = 2 full hunger icons = 30 XP points = 30 mana**. One heart is 2 HP; one hunger icon is 2 food points. XP is charged as points, never levels.

Resolve every component from the unrounded distance budget, then round once in its own resource units:

- Ordinary XP: `max(1, round(B))`, preserving the live fare exactly.
- Erudite: `max(1, round(0.75 × B))` XP **plus** `max(1, ceil(B / 3))` mana. Mana rounds upward, preserving the premium. Both are required; no partial payment occurs.
- Mana: `max(1, round(B))`.
- Health: `ResourceValuation.healthForMana(B)`; this currently charges `2 × ceil(B / 30)` HP, in whole hearts.
- Hunger: `ResourceValuation.foodForMana(B)`; this currently charges `ceil(B / 7.5)` food points, allowing half hunger icons.

The approved Health/Hunger rounding follows the existing shared conversion helpers. It replaces the older unimplemented proposal to round both to the nearest individual native point. Do not change the common helper or the ordinary XP fare to accommodate this device. Whole-heart rounding deliberately creates a minimum one-heart health sacrifice, so very short health-paid trips are relatively expensive. This is visible quantization, not a different exchange ratio or a hidden route multiplier.

| Distance in blocks | Ordinary XP points | Erudite XP + mana | Charged mana | Hunger icons | Hearts |
| ---: | ---: | ---: | ---: | ---: | ---: |
| 64 | 15 | 11 XP + 5 mana | 15 | 1 | 1 |
| 256 | 18 | 14 XP + 6 mana | 18 | 1.5 | 1 |
| 1,024 | 24 | 18 XP + 8 mana | 24 | 2 | 1 |
| 4,096 | 36 | 27 XP + 12 mana | 36 | 2.5 | 2 |
| 16,384 | 60 | 45 XP + 20 mana | 60 | 4 | 2 |

Health must leave the player alive. Hunger consumes food points, not saturation or exhaustion. Mana follows the normal expenditure/recovery-delay rules. XP uses the existing event-aware current level/progress accounting. Health is a direct resource sacrifice; armor, absorption and damage-triggered robes must not turn it into free payment or a defensive/counterattack trigger.

Quotes have no hidden distance cap, alternate currency or health fallback. A trip beyond the player's available selected resource is unavailable; improving mana capacity can make more distant mana trips affordable. The initial package adds no durability, travel cooldown or extra teleport range modifier.

The common conversion is a baseline, not evidence that all routes have identical practical value: mana replenishes, food is renewable, XP competes with enchanting, and health introduces immediate survival risk. Playtest repeated short hops, long-distance trips, armor/enchantment interactions and resource generation before changing route prices. Record any later premium explicitly.

## Saved state and quoted travel

Store a bounded, versioned payment route ID separately from the attunement key and endpoint name. Store no client-authored cost, discount percentage, executable plan or balance coefficients in an item or block entity. Current trusted policy resolves the price.

Preserve the route through placement, either-half breaking/drop collection, item/block saves, chunk unload and restart. Existing ordinary native stones without the new field remain ordinary XP; malformed explicit route data must not grant free travel or silently choose health. This is the native baseline's default, not an apparatus registry migration.

Introduce an immutable typed quote, replacing XP-only assumptions in the view, affordability check and transaction. Reuse shared resource valuation and typed payment/rollback primitives. Do not implement a separate payment engine for each finish or resource.

The server continues accepting source/destination IDs only. Resolve live membership, same dimension, source proximity/session, destination and arrival before payment. Compute the actual departure stone's latest route and fare on the server. Snapshot the affected resource, authorize/debit it, revalidate participants after cancellable callbacks, then teleport and commit. Failed payment, invalidation, canceled transfer or exceptions restore the device payment, including mana recovery timing and exact XP level/progress. Add a per-player transaction guard against reentrant travel requests from payment/teleport callbacks. A successful trip consumes the session and produces the existing departure/arrival sound and particles once. Creative follows the existing payment bypass convention.

Network view protocol 6 carries the route, positive primary amount, a positive additional mana amount only for Erudite, and server-authored affordability. A missing/zero Erudite mana component or extra component on another route is invalid. Saved route version 1 is unchanged; trusted current policy revises existing Erudite stones without rehashing their key. Treat client displays as previews; clicks always revalidate. Detect stale source configurations and refreshed affordability without spending a different route from the displayed one. Preserve the existing six-row pagination, endpoint validation and current-stone rename behavior.

## Menu and optional recipe viewers

Reuse the existing destination list and `TravelCostDisplay`: XP and mana show amount then symbol, with both amounts side by side for Erudite; food and health show counted full/half icons. Unaffordable rows dim and disable. The display and transaction must quote identical units and use the same nonlethal health condition. Synchronize fresh affordability when balances change, including natural mana recovery. Keep accessible narration; do not add chat/actionbar instructions, status or error messages, extra confirmation screens or explanatory item tooltip paragraphs.

Generate JEI/EMI displays from the shared finish/route recipe catalog. There are **180 primary combinations**: 36 finishes × five routes, plus the accepted same-Chiseled-Stone-Bricks alternative for the Stone Bricks finish. Show the four ordered offerings, matching masonry, relevant Pearl socket and correct route/finish output. If ingredient alternatives cycle, both masonry offerings must cycle together to avoid illustrating a rejected mixed-body recipe. Output lookup uses finish and route without requiring a particular private shard key; never expose or fabricate a network's shard blueprint or signal. Viewers remain optional.

## Implementation sequence

1. Define the five-route catalog, immutable typed quote and fare math; cover resource rounding and the unchanged ordinary fare.
2. Add saved item/block route data, shared adjective naming and finish-independent preservation on placement/break/reload.
3. Resolve the Pearl's actual matched socket in atomic ritual crafting, preserving ordered rotations and full copied key. Keep material eligibility sourced from the existing authoritative offering rules rather than duplicating a list.
4. Replace XP-only travel debit with the shared resource transaction and revalidation/rollback path; add reentrancy protection.
5. Connect real typed menu quotes/affordability and dynamically generated JEI/EMI recipe entries.
6. Run model, world, packaging and compatibility checks; inspect the actual native menu and optional viewers before publication/installation. Record exact results and remaining playtest limits in development status.

## Verification checklist

- Fare fixtures at the distances above, vertical/diagonal distance, each rounding boundary, positive minima and extreme world positions. Verify Health/Hunger against `ResourceValuation` and every ordinary XP fixture against its prior result.
- All five routes and all 36 finishes, every whole rotation, unsupported Pearl selectors, neutral other sockets, wrong order and reflection symmetry, unchanged full key and canceled/changed-input atomic crafting.
- Matching keys with different routes link normally; departure route alone charges. Wrong keys, cross-dimension destinations, stale sessions and out-of-range sources remain rejected.
- Native balances before/after actual paid travel: exact XP points, food points, health points and mana; insufficient funds, nonlethal health boundaries, zero-food allowance and creative bypass. Verify sacrifice does not activate damage-triggered equipment.
- Canceled/throwing teleports and payment listeners that invalidate or reenter travel restore the selected resource and mana recovery delay without duplicate effects or trips.
- Route/name/key retention after both-half removal, item placement, chunk unload and save/reload; missing baseline route and malformed explicit data cases.
- Payload bounds, typed quote round trips, live affordability changes and stale-quote handling. Actual client captures of all resource symbols, affordable/unaffordable rows, pagination and all five italic item names.
- Optional JEI and EMI actual generic-output and bound-output lookups, coordinated masonry alternatives, Pearl-socket depiction and absence of private attunement data.

Relevant baselines: [Standing Stones](standing-stones.md), [payment design/history](standing-stone-payments.md), [shared resource valuation](resource-payments.md), [attunement identity](attunement-shards.md), [shared adjective naming](magic-adjectives.md), and [material reference](../research/iron-material-uses.md).
