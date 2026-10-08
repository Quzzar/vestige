# Shared resource payment values

**October 8, 2026: owner-accepted shared equivalence.** The owner explicitly locked the ratio below after reviewing the visible heart/hunger icons, XP-point costs and live mana recovery. Use it for all cross-resource payment conversions; any deliberate discount or premium must be named separately. The owner subsequently revised the mana equivalent from forty to thirty, retaining one heart, four food points and thirty XP. This supersedes both earlier October 8 ratios.

| Sacrifice | Equivalent starting budget |
| --- | ---: |
| 1 full heart (2 HP) | 30 mana |
| 2 full hunger icons (4 food points) | 30 mana |
| 30 ordinary XP points | 30 mana |

Thus **1 heart = 2 full hunger icons (4 food points) = 30 XP points = 30 mana**. XP means points, never levels. Health is the most valuable sacrifice, food next, XP weakest per point. This is a balancing convention, not a claim that every player's recovery burden is identical.

`ResourceValuation` is the shared executable authority. Bloodbound converts its exchanged portion at one heart per thirty adjusted mana; Fasting converts at one food point per 7.5 mana. Fractions aggregate across contributions, with at most 75% of mana exchanged. The mana price includes typed mana adjustments before exchange. Resource conversions round upward to whole hearts/food points; the existing common layout multiplier then rounds final payments once in their own units. This means tiny exchanges still cost one whole heart or food point, and rounding can make small budgets less efficient. Health payments must leave the caster alive. A missing resource rejects the entire payment atomically; there is no automatic substitution.

Bloodbound retains its separate Amplify ×1.05 and mana ×1.02 per contribution; Fasting retains mana ×1.02. These are authored augment effects, separate from the common exchange rate. Typed Health costs remain HP, Hunger remains food points, and existing typed costs are not reinterpreted as mana.

## Units and recovery

A heart is two health points, and a full hunger icon is two food points. The visible heart-to-hunger ratio is therefore **1:2 icons**, while the health-point-to-food-point ratio is **2:4 points**. XP payments use fixed points, never levels. Thirty XP is close to the 31 points needed for level 12 to 13; level 4 to 5 needs 15.

The ordinary player pool is 100 mana; approved robes can raise its maximum to 125. Recovery stays at two mana per second after five seconds without another successful mana expenditure. Replacing thirty spent mana takes about twenty seconds, and refilling an empty ordinary pool takes about fifty-five seconds at normal tick speed. The ratio does not change mana recovery, durability prices, spell outcomes or the value of preparation time.

## Homebound Eye

Maximum durability stays 30. Default returns spend six durability without another payment: five returns from a fresh Eye. Every resource-backed return spends two durability plus the same **30-mana-equivalent** payment: **1 heart (2 HP), 4 food points (2 full hunger icons), 30 XP points, or 30 mana**. Each route allows fifteen successful returns before repair, assuming sufficient resources. The old health wear advantage and earlier provisional food/XP quantities are superseded. A saved Eye stores its route; current tuning applies when used, without changing its key or origin.

## Exhausting

The former cooldown drawback did nothing on the packaged spell catalog. Exhausting now multiplies **Amplify by 1.25 and mana by 1.35 per degree**. Repeated degrees multiply both factors. It preserves Phantom Membrane / Iron Block as its route and requires an actual Amplify consumer and positive mana cost. It adds no cooldown. Focused remains the smaller +12% power / +10% mana option; Charged gives +15% power with +10% mana and additional preparation. Exhausting offers the largest immediate power increase at the highest mana premium of those three.

## Scope and future prices

Standing Stone's accepted ordinary XP distance fare remains unchanged; alternate travel payments are not implemented. When authored, convert the same unrounded XP budget through this ratio before the resource's final rounding, minimum and any explicit route discount. That is a future implementation requirement, not a shipped alternate route.

The [Kairotic Hourglass](kairotic-hourglass.md) now implements the locked fifteen seconds, sixty mana, ten durability and one wear per successful return. Its Bloodbound/Fasting/Erudite choices use these rates through shared typed Health/Hunger/Experience costs. For this device, temporal and vessel factors and saved geometry resolve the complete mana budget before its capped exchange: `E = min(30, 0.75 × M)`. This device order is explicit; existing spell exchange-before-layout semantics remain unchanged. Failed or canceled physical returns restore payment and mana recovery delay and spend no wear. Wands, staffs and robes retain their existing component costs and wear; their already-shaped spells inherit the common Bloodbound/Fasting and Exhausting changes through the same runtime.

The [follow-up balance review](../resource-payment-balance-review.md) inventories live conversions, distinct authored costs and outstanding work. The earlier provisional ratios are retained only as explicitly historical evidence; this accepted equivalence governs future agents and existing stored payment routes. Verification and exact limitations are recorded in [development status](../development-status.md).
