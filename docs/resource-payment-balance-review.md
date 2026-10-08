# Resource payment balance follow-up

**October 8, 2026: owner-accepted ratio applied.** The authority is [resource payments](design/resource-payments.md): **one full heart = two full hunger icons = 30 XP points = 30 mana**. A heart is 2 HP; two full hunger icons are 4 food points. Earlier provisional rates in the [first audit](ritual-shape-and-payment-audit.md) and its receipts are historical.

## Live conversion review

| System | Review and current result |
| --- | --- |
| Bloodbound | Uses `ResourceValuation.healthForMana`: one whole heart per thirty exchanged mana, rounding up. Its independent Amplify ×1.05 and mana ×1.02 contribution stays intact. |
| Fasting | Uses `ResourceValuation.foodForMana`: one food point per 7.5 exchanged mana, rounding up. Its independent mana ×1.02 contribution stays intact. |
| Mixed exchanges | Bloodbound and Fasting retain the shared 75% exchange ceiling. Their complete typed payment commits atomically. Wands and staffs inherit this same compiled source shaping once. |
| Homebound Eye | All resource routes spend two durability plus the same thirty-mana budget: **1 heart / 2 full hunger icons / 30 XP / 30 mana**. Default remains six durability; maximum remains thirty. Current level/progress determines available XP; cancellation/failure retains rollback. |
| Standing Stone | Live distance fare remains `round(12 + 12 × sqrt(distance / 1024))` XP points, with minimum one. It has no live alternative resource conversion to correct. Future alternatives convert that same unrounded budget at the locked ratio, with explicit discounts separate. |
| Mana recovery | Unchanged: base maximum 100, two per second after five seconds without successful expenditure. Thirty spent mana takes about twenty seconds to replace at normal tick speed. |

Whole-heart/food-point exchange rounding happens before the existing final layout multiplier and resource-specific rounding. Exact thirty-mana equivalents agree; tiny exchanges can be less efficient because they still spend a whole heart or food point. No minimum is silently waived, and health payment must leave the player alive. Direct authored health/hunger costs retain their own units.

The catalog inventory covers **214 definitions with 220 mana cost entries**, including modes. There are no authored health, hunger or cooldown cost entries in those base definitions. The shared price correction therefore belongs to the exchange runtime and device policy; no blanket base-spell cost rewrite is warranted.

## Other premiums and resource outcomes

The **56 executable Spellshaping rules** contain two fractional exchange routes, Bloodbound and Fasting. None retains a cooldown-only cost premium. Exhausting remains the corrected **Amplify ×1.25 / mana ×1.35 per degree**, while Focused and Charged retain their distinct smaller power/price/preparation profiles.

Famished's one food point and Sustaining's two food points are separately authored fixed costs, equivalent to 7.5 and fifteen mana for review. They purchase their own power/healing contribution rather than replace a stated portion of mana. Their existing bounds and mana premiums remain. Preparation, consumed materials and tool wear have no newly invented exchange rate.

Gluttony restores **six mana per food nutrition**, for at most three actual food-consumption events in ten seconds. This is a bounded spell outcome on eating an inventory food, rather than a payment route subtracting hunger points; preserve its separately authored coefficient. Food still performs its normal feeding role. Emerald's Reclaiming wand tip refunds at most ten percent of mana actually paid, capped at five per cast, and adds wear. Neither creates a health/food/XP substitute price. Healing and damage outcomes likewise retain their authored balance rather than being automatically priced by their HP quantity.

## Recipe shape clarification

**Scroll → fragments** is an unordered recipe in the normal crafting grid. **Fragments → a selected spell scroll** is a separate unordered Spellstone discovery ritual. Attunement Shard ingredients are accepted unordered, but their actual arrangement, geometry and paired imbuements determine identity. Two-item repair remains unordered. Threads, Homebound Eye, Whispering Shell, Standing Stone and Fluxed Flint construction retain their corrected ordered inner-layer patterns with whole quarter-turn equivalence.

## Project-wide outstanding work

| Work | Status after this correction |
| --- | --- |
| Kairotic Hourglass | Still a design, not a registered playable item. Locked baseline: fifteen-second position history, sixty mana, ten durability and one wear per success. Implement history/arrival/payment/crafting/art and decide the dimension policy and exact imbuement controls. The XP variant requires a reusable typed XP cost in the shared runtime. |
| Standing Stone alternate payments | Health, hunger and mana routes, local Pearl selectors and any Lapis discount remain unimplemented. Apply the locked ratio when authored; preserve the accepted ordinary XP distance fare. |
| Fluxed Flint redesign | Construction shape is corrected. Ingredient/art revision remains on hold for the separate Dissentient Diamond work. |
| Recipe viewer native inspection | Ordered positions are model-tested. The preceding JEI/EMI and EMI-only capture attempts timed out without screenshots; actual visual confirmation remains incomplete. This numeric correction does not rerun the failed visual harness or claim new presentation evidence. |
| Survival playtesting | Check resource renewability and repeated escape/casting, including health-funded casts alongside healing, food/XP farms, long-distance travel and degrees of shaped power. The ratio is accepted; encounter balance is still empirical. No exploit is claimed solely from arithmetic comparisons of different outcomes. |
| Related deferred device work | Crane Bag survival recipe and final art remain separately deferred; later wand tips remain ideas beyond the initial eight. These do not block the payment correction. |
| Delivery | This pass verifies source and package behavior. Testing-pack installation and publication require their own delivery receipt; neither is claimed here. |

Executed checks, source/package hashes and limitations are recorded in [development status](development-status.md) and the [current verification receipt](verification/resource-ratio-thirty-2026-10-08/README.md). The earlier shape/payment receipt remains evidence of its own provisional build.
