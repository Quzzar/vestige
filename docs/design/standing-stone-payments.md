# Standing Stone travel payments

**Implemented owner-approved package, October 10, 2026.** The complete rules and examples live in [Standing Stone payment imbuements](standing-stone-imbuement-plan.md). This supersedes the earlier XP-only implementation and deferred alternate-payment proposals.

The four inner offerings retain their relative ordered pattern: **Attunement Shard → matching masonry → Ender Pearl → matching masonry**. Whole quarter-turns match. Because the masonry pair is identical, this particular reflected layout also matches. The installed material in the Plinth actually carrying the Pearl selects one payment route; other sockets do not adjust payment. Unsupported Pearl materials reject silently. Sockets stay installed after crafting.

| Pearl socket | Route | Display adjective |
| --- | --- | --- |
| Empty | Ordinary distance XP fare | None |
| Lapis Block | 25% less XP | *Erudite* |
| Amethyst Block | Mana fare | *Charged* |
| Moss Block | Hunger fare | *Fasting* |
| Soul Sand | Nonlethal health fare | *Bloodbound* |

These are native Vestige effects, informed by the [pinned material reference](../research/iron-material-uses.md), rather than exact Iron mechanics. All four materials already pass shared Plinth eligibility; Lapis does not gain an invented spell augment.

The full verified shard key is copied unchanged. Finish, recipe rotation, local geometry and payment route never recompute it. One saved route per stone is independent of key, shape, runes and editable destination label. Mining either half, placement and saving retain the route. Existing native stones without route data use ordinary XP; malformed explicit data denies travel. No recrafting route is implemented.

## Fare and shared valuation

For straight-line three-dimensional block distance d, the unrounded budget is **B = 12 + 12 × sqrt(d / 1024)**. Ordinary XP and mana cost max(1, round(B)); Lapis costs max(1, round(0.75 × B)). Health uses ResourceValuation.healthForMana(B), currently 2 × ceil(B / 30) HP. Hunger uses ResourceValuation.foodForMana(B), currently ceil(B / 7.5) food points.

The shared ratio remains **1 full heart = 2 full hunger icons = 30 XP points = 30 mana**. Health is 2 HP per heart; hunger is 2 food points per icon. Whole-heart rounding creates an explicit minimum one-heart sacrifice. XP is points, never levels, and available XP is derived from current level/progress rather than the historical totalExperience field. No hidden cap, discount or fallback changes the conversions.

Only the departure stone chooses payment. Health must leave the player alive; food can reach zero. Creative bypasses payment. The destination does not apply its own route or charge again. There is no added durability, cooldown, range change, mixed payment or repeated-degree stacking.

## Native menu and optional recipe viewers

Server-authored quotes show the actual selected resource. XP and mana use numbers with resource marks; health and food use counted full/half native icons. Costs exceeding ten full icons use a bounded number of full-icon units with one symbol; narration retains exact HP/food-point amounts. The resource cost remains unchanged. Disabled rows dim; affordability refreshes an existing menu as balances change without reopening a closed menu. The native six-row pages, names and signature remain intact. Travel interactions add no chat or actionbar messages.

JEI and EMI derive 180 primary finish/route combinations and five separate Chiseled Stone Bricks alternatives from the executable recipe catalog. Both masonry inputs remain coordinated. Only the Pearl's socket shows the selector. Public previews omit private attunement keys; bound output lookup ignores the key and custom name while matching finish and route. Neither viewer is required for crafting or travel.

## Shared transaction

Resolve membership, source session, displayed quote, live target and shared nearby arrival first. The existing occupied-arrival fallback remains accepted. Commit shared typed payment and teleport on the server thread with no intervening tick. A teleport event can cancel but cannot redirect the quoted trip. Reentrant requests are rejected. Endpoint changes, stale routes, canceled/failed/throwing travel and payment callback invalidation restore the selected resource, including mana recovery delay. Sacrificial health uses the shared non-damage payment and therefore does not trigger damage-reactive equipment.

Only successful travel consumes the source session and plays portal effects. Travel payment adds no inherent trait semantics and does not alter scroll costs or Spellshaping. See [development status](../development-status.md) for actual verification and remaining balance playtesting.
