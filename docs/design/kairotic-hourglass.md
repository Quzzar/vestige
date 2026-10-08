# Kairotic Hourglass

**October 8, 2026: behavior, ingredients and baseline locked; implementation pending.** The owner chose a reusable hourglass that returns its user to their position fifteen seconds earlier. Right-click activates it. It records recent positions while carried in the player's inventory; an incompletely recorded window returns to the oldest available position without requiring a fifteen-second wait. The selected ingredients are Clock, Glass, Echo Shard and Ender Pearl; the baseline is 60 mana and ten durability, with one wear per successful use. This supersedes the speed-and-fatigue Hourglass candidate in the [original research](../research/pathfinder-kairotic-item-inspiration.md). The prerequisite [shape/payment review](../resource-payment-balance-review.md) is complete; the shared equivalence is locked, while hourglass implementation and its device-specific imbuements remain outstanding.

## Accepted direction

- Record while the hourglass is in the player's inventory, rather than requiring it to be held throughout the recording period.
- Right-click returns to the recorded position fifteen seconds earlier, or as far back as the available history reaches.
- The unmodified baseline is **60 mana per successful return and 10 maximum durability**, spending **one durability per successful return**. A fresh unmodified hourglass therefore provides ten successful uses. This supersedes the earlier proposed 50-mana/20-durability baseline.
- Relative recipe shape matters. Rotating the whole arrangement preserves the recipe; ingredients cannot be freely permuted as in a shapeless recipe.
- Imbuement can create alternatives that reduce mana payment while adding health, hunger or experience payment. These are alternative costs, not free reductions.
- For payment balance, health is more valuable than food, and food more valuable than experience. The [shared starting valuation](resource-payments.md) is 1 heart = 2 full hunger icons (4 food points) = 30 XP points = 30 mana. Exact hourglass exchange fractions and selectors remain open.
- Other shaping may extend or shorten the return interval, with corresponding cost adjustments. Exact materials, recipes, formulas and amounts remain open.
- Durability may also be increased or decreased by authored shaping, with a meaningful trade-off. Specific selectors and amounts remain open.
- Use the existing shared immutable item-ability, trait resolution and payment foundation. Numerical effects explicitly consume traits; the item does not introduce an engine-semantic Kairotic trait.

## Recording and selection

The proposed implementation keeps one server-owned trail per player. Multiple carried hourglasses share that trail; the item's previous holder does not become a destination when it changes hands. Each sample contains a server tick, dimension and exact player position. Client-supplied coordinates cannot establish history.

Record once per game tick and discard samples older than the supported return window, retaining the boundary sample needed for selection. At the normal twenty ticks per game second, the baseline fifteen-second trail needs about 300 samples. This uses game time, so lag does not create invented movement between samples.

At activation, calculate the requested destination time from the selected hourglass's resolved return interval. Select the latest recorded sample at or before that time. If recording started later, select the oldest available sample instead. For example, collecting an hourglass, walking for five seconds and activating it returns to the position recorded when carrying began. After twenty seconds of continuous carrying, activation returns to the position from fifteen seconds ago.

Handle acquiring and using the item between recording ticks by taking an initial sample during activation if necessary. With no earlier movement recorded, the destination is the current position; the proposed behavior is a silent no-op with no payment or wear.

The retained history must cover the supported authored return intervals, not just fifteen seconds hardcoded into the buffer. If a longer interval becomes available after recording begins, use the oldest retained sample while the longer window fills; never fabricate older history. The memory retention policy and maximum supported interval need to be explicit when those variants are implemented.

## Proposed lifecycle and arrival rules

Clear the trail when the player stops carrying every hourglass, dies, logs out or the server stops. Reacquiring one begins a fresh trail. Keep recording after a successful use; return movement becomes part of the actual ongoing history rather than erasing or rewriting it.

The recommended initial dimension policy is **current dimension only**, clearing the trail on a dimension change. The owner has been asked whether cross-dimension returns should be supported; that choice remains open.

Return position only. Current health, inventory, experience, status effects, other actors and the world remain current, subject to the actual activation payment. Preserve current facing. The proposed arrival check requires the recorded position to remain inside the world border/build bounds and open for the player's current collision volume. If it is blocked, reject silently without payment or wear rather than putting the player inside new terrain. Passenger behavior, momentum/fall handling and hazardous-but-open destinations need explicit implementation choices and tests.

Commit resource payment and deterministic wear only for a valid successful return, including cancellation handling. Existing Homebound Eye behavior demonstrates teleport rollback, but its separate manual payments are not a reason to bypass the shared foundation for the new item. All ordinary feedback remains visual/audio, without chat or actionbar instructions and errors.

## Time materials: upstream facts and native conventions

The complete [pinned Iron material reference](../research/iron-material-uses.md) establishes no dedicated time mineral or general rewind reagent. Keep the following narrower facts separate:

- Iron's Pyrium jewelry material supplies **5% casting-time reduction**, not historical-position recovery. [Pinned material definition](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/pyrium.json).
- Iron brews **Timeless Slurry from an Echo Shard and a Mundane Potion**. That is an actual temporal-named recipe connection, but does not isolate a universal time property in either ingredient. Echo Shard also serves as Iron's Eldritch focus. [Pinned brewing recipe](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/generated/resources/data/irons_spellbooks/recipe/alchemist_cauldron/brew_timeless_slurry.json), [school focuses](https://iron.wiki/schools/).
- Vanilla uses Echo Shards to craft a Recovery Compass that points to the player's last death location. This supports a past-location/memory motif; it is not literal time travel. [Minecraft's Echo Shard article](https://www.minecraft.net/en-us/article/echo-shard).
- Vestige already uses **Clocks** in Haste and Time Jump recipes. Its shipped Enduring augment pairs Clock or Phantom Membrane with Amethyst Block to raise Time by 20% for compatible finite effects. Patient uses Clock/Stone; Delayed uses Clock/Redstone. These specific offering/socket rules are the current conventions, not a universal raw mineral-to-Time table.

Amethyst is therefore not justified as a mandatory generic magical focus in this recipe. The earlier proposed Amethyst Shard ingredient and arbitrary five-/thirty-second Copper/Amethyst selectors are withdrawn pending a justified material design. No new ore or universal temporal material is selected by this document.

## Locked shaped recipe

**Owner locked October 8; not implemented:** one Clock, one Glass block, one Echo Shard and one Ender Pearl produce one hourglass. Clock supplies the time mechanism, Glass the vessel, Pearl relocation and Echo Shard the memory of a previous location. The Echo role is an independently authored Vestige use informed by the facts above. Sand is already represented by Glass's construction; Gold and Redstone by the Clock's construction. Ingredient roles remain descriptive clues, not inherent runtime traits.

Use the shared **shaped four-slot** recipe matcher and the reviewed clockwise order: **Clock → Glass → Echo Shard → Ender Pearl**. Clock faces Echo Shard across the center; Glass faces Pearl. The entire pattern accepts quarter-turn rotations while retaining its relative order; reflections remain distinct under the existing matcher. Keep Spellstone's reference surface empty. A larger apparatus can use this ordinary four-slot inner pattern under the existing shaped-recipe rules; it does not make the four ingredients freely interchangeable across eight seats.

Each socket affects its own offering. Rotating the complete offering/socket pattern preserves those local pairings. Consume the four offerings, retain installed materials and store trusted shaping on the result. No Attunement Shard is proposed: this device follows its holder's actual recent trail rather than a shared destination signature.

The October 8 construction correction also makes magical threads, Homebound Eye, Whispering Shell, Standing Stone and Fluxed Flint construction ordered. Attunement Shards retain unordered acceptance with arrangement-derived signatures; scroll-to-fragment normal crafting, fragment-to-scroll discovery and two-item repair retain their separate unordered operations. See [ritual matching](ritual-crafting.md).

## Imbuement and trait proposal

Reserve separate authored controls for return interval, payment exchange and durability. A promising division is Clock-associated temporal shaping, Echo-associated payment shaping and Glass-associated vessel reinforcement, but no material map or exact adjustment is selected yet. Do not announce twelve supported variants from the withdrawn selector table. The accepted direction permits longer/shorter returns and stronger/weaker durability; implementation must pair improvements with an explicit resource, wear or capability trade-off.

Soul Sand, Moss Block and Lapis Block are existing health/hunger/XP selectors on the [Homebound Eye](attuned-devices.md). Reusing that vocabulary is a proposal, not automatic compatibility for every Echo Shard. Clock/Amethyst can be evaluated through the existing Enduring meaning rather than silently redefining its 20% Time contribution as a doubled interval. Unrelated or unsupported pairings must not accidentally inherit a spell effect that has no meaningful hourglass consumer.

### October 8 imbuement review draft

**Proposed for owner review; not accepted or implemented.** Keep the accepted four offerings and their relative order. Give Clock the return-interval controls, Echo Shard the alternate-payment controls and Glass the vessel's durability/mana trade-off. Ender Pearl supplies relocation; the first set does not need an additional Pearl-socket effect merely to occupy every socket. Choose at most one contribution per offering, with independent contributions combining across the three roles. The one Clock does not supply repeated Enduring degrees, and the one Echo Shard does not select several payment resources simultaneously.

All table figures assume neutral four-slot geometry, no actor bonuses and only the listed contribution. Maximum durability equals the successful-use count here because wear remains exactly one. These are starting playtest proposals rather than measured survival-economy equivalences.

| Offering | Socket block | Proposed contribution | Result with other sockets neutral | Trade-off |
| --- | --- | --- | --- | --- |
| Clock | Copper Block | Fleeting: Time ×2/3; mana ×0.75 | 10-second return, 45 mana, 10 durability | Cheaper access to a more recent position; cannot reach the older fifteen-second destination. |
| Clock | Amethyst Block | Enduring: Time ×1.20; mana ×1.12 | 18-second return, 67 mana, 10 durability | Longer recall costs more mana. Preserve the shipped Enduring factors. |
| Echo Shard | Soul Sand | Health-paid: exchange up to 30 mana, bounded by 75% of the adjusted mana price | 15-second return, 30 mana + 1 full heart, 10 durability | Health is spent in the present and is not restored by the return. |
| Echo Shard | Moss Block | Hunger-paid: the same exchanged budget, priced through ResourceValuation | 15-second return, 30 mana + 2 full hunger icons, 10 durability | Food is spent in the present; use four food points, not two. |
| Echo Shard | Lapis Block | Experience-paid: the same exchanged budget, priced through ResourceValuation | 15-second return, 30 mana + 30 XP points, 10 durability | Fixed XP points are consumed, never levels. |
| Glass | Iron Block | Reinforced: maximum durability ×1.50; mana ×1.25 | 15-second return, 75 mana, 15 durability | More returns from the scarce crafted vessel, with a higher price per return. |
| Glass | Quartz Block | Frugal: maximum durability ×0.60; mana ×0.75 | 15-second return, 45 mana, 6 durability | Lower mana use consumes the crafted vessel's useful lifetime more quickly. |

Enduring reuses an established **Vestige** pairing and its exact trait/cost factors; extending historical lookback is a new explicit consumer, not proof the existing lifetime adapter already supports this device. The payment selectors reuse Homebound Eye's vocabulary, with new Echo-Shard offering routes. Copper/Clock, Iron/Glass and Quartz/Glass are new native interpretations: speed/recentness, reinforcement and a precise but fragile vessel. The [pinned reference](../research/iron-material-uses.md) gives Copper speed and Iron protection associations, but does not define these hourglass rules; Quartz has no universal Jewelry efficiency parameter. Do not describe any of these new prices as upstream Iron mechanics. Every proposed block is already socket-eligible; this draft adds no arbitrary-block acceptance.

Preserve existing exact pair meanings. **Glass / Diamond Block is Reflecting**, not a durability route. Clock / Stone remains Patient and Clock / Redstone remains Delayed; this draft does not redefine them as shorter or longer history. Those behaviors need separately reviewed compatible consumers before use on the hourglass. Ender Pearl / Lodestone likewise must not redefine Anchored into a fixed historical destination. Match supported device contributions explicitly and reject recognized incompatible paid effects rather than silently charging for a no-op. An unrecognized neutral pairing retains the existing neutral behavior.

### Proposed combination math and examples

Resolve the Time modifier through the common trait resolver and use the existing proposed normalized interval expression below. Keep mana and carrier durability as explicit authored properties; changing Time is not an engine-wide instruction to change durability. For these seven routes, apply the chosen Clock and Glass mana factors once, followed by the saved v3 Casting Cost factor. Temporary trait bonuses follow the same activation resolution as other item abilities.

For the proposed payment selector, let `M` be the unrounded adjusted mana price. Exchange `E = min(30, 0.75 × M)`, leaving `M − E` mana. Convert `E` with the common authority: health is `2 × ceil(E / 30)` HP, hunger is `ceil(E / 7.5)` food points and XP is `ceil(E)` points. Final mana uses the shared nearest-point convention, `floor(M − E + 0.5)`, rather than upward rounding. This corrects the earlier draft wording without changing runtime. The proposed geometry placement includes saved geometry cost in `M` before the exchange, not a second time afterward; its integration with existing `CastShaping` (which converts exchanges before final layout scaling) still needs an explicit shared implementation review. The alternate resource is mandatory; there is no automatic fallback to paying full mana. Rounding may make a small health exchange more expensive than its nominal budget, as in the existing shared whole-heart convention.

| Proposed combination | Return interval | Final payment | Maximum durability / uses |
| --- | ---: | --- | ---: |
| Enduring + Reinforced | 18 seconds | 84 mana | 15 |
| Enduring + Reinforced + Experience-paid | 18 seconds | 54 mana + 30 XP points | 15 |
| Reinforced + Health-paid | 15 seconds | 45 mana + 1 heart | 15 |
| Fleeting + Frugal | 10 seconds | 34 mana | 6 |
| Fleeting + Experience-paid | 10 seconds | 15 mana + 30 XP points | 10 |

The owner revised the shared equivalent to thirty mana after this draft was written. Each alternate-payment route now trades at most thirty mana for one heart, two full hunger icons or thirty XP. For a cap example, Fleeting + Frugal costs 33.75 unrounded mana: exchanging 75% is 25.3125, leaving 8.4375 mana. Its Experience-paid version therefore costs eight mana + twenty-six XP points, with six maximum durability. This preserves the exchange cap, resource-conversion upward rounding and shared final mana rounding. These examples assume neutral geometry.

**October 8 naming direction:** the owner approved giving combined adjustments one italicized adjective. The [shared catalog](../magic-adjectives.md) reserves all 36 proposed temporal/vessel/payment naming sets, including Ephemeral for Fleeting + Frugal, Stalwart for Enduring + Reinforced, and Relentless for Enduring + Reinforced + Bloodbound. Naming aliases preserve independent contributions; they do not implement this device, finalize its open gameplay details, change its recipes or turn the named sets into mechanic-replacing compounds. Revisit the reserved matrix if the eventual route palette changes.

These variants intentionally change repeat-use affordability. Fleeting and Frugal can each permit two mana payments from an ordinary full hundred-mana pool; an alternate payment can permit further payments while its required resource remains available. A valid historical destination, current resources and remaining durability are still required for every return. There is no added cooldown, free refund, random break chance or rewind of resources. A partially filled trail still uses the selected variant's normal price.

For implementation review, a proposed finite return bound is five to thirty game seconds, with enough retained history for thirty seconds even before an actor equips a temporary Time boost. This bound is not yet accepted. Reject a paid modifier that is already saturated and provides no other meaningful effect. A thirty-second crafted route, multi-socket compounds, Pearl-socket riders and lower per-use wear remain later design questions; this first set does not claim to implement them.

Author descriptive Time, Memory, Space, Teleportation and Transmutation traits with explicit effect consumers. Proposed return interval: `15 seconds × resolved Time / base Time × resolved Amplify`. Exact trait ratings, bounds and associated prices remain open. Apply each modifier once through shared resolution. Range and Area have no proposed consumer for this position-return ability; they do not invent passengers or an area rewind. Preserve the accepted leyline v3 math and the shaped recipe's selected active geometry.

Store crafted geometry, as with crafted scroll shaping; rebuilding the station does not rewrite an existing hourglass. Temporary actor boosts affect future activations through the common resolver. A longer resolved interval still uses real retained history, falling back to the oldest available sample while its window fills. Durability adjustment requires an explicit item-property rule; a generic spell-duration or casting-cost multiplier must not silently change maximum durability.

The present shared `SpellCost` repertoire supports mana, health and hunger but not experience. The XP route requires a reusable typed experience payment and atomic cancellation/refund handling in the shared foundation; the Eye-specific helper does not already provide that integration.

## Accepted baseline and outstanding balance

| Unmodified property | Owner-selected value |
| --- | ---: |
| Return interval | 15 seconds, or oldest available history |
| Mana per successful return | 60 |
| Maximum durability | 10 |
| Wear per successful return | 1 |
| Successful uses from a fresh item | 10 |

At the ordinary 100 maximum mana, one unmodified use leaves 40 mana: a second immediate full-price use cannot be paid. A larger mana capacity can change that, as intended. This is a resource limit, not an approved fixed cooldown. Deliberate cost-shaping or alternate-payment variants need their own repeat-use balance assessment; they do not automatically preserve the same two-use restriction.

The owner's balance direction is **health > food > experience** in sacrifice value. A modest health sacrifice should buy stronger relief than a comparably burdensome food sacrifice; experience should buy the weakest relief. Determine each exchange rate in its own units and test practical recovery/renewability. Do not reinstate the withdrawn equal five-heart/five-food proposal or call a heart, food point and XP point equal. The common numerical ratio is now locked in [resource payments](resource-payments.md); hourglass-specific fractions, selectors and any explicit discounts remain open. XP means points rather than levels.

Bloodbound/Fasting now use the shared ratio: one heart per thirty exchanged mana or one food point per 7.5, rounded upward before existing final layout scaling. Homebound Eye uses the same authority. These implemented payment corrections do not supply the hourglass or add a shared typed XP cost.

Health payment must leave the user alive, and all required resources must be available together. Proposed charging uses the selected/resolved interval even with a partially filled history, so deliberately selecting a shorter interval remains distinct from acquiring a longer-window item moments before use. Failed, canceled, obstructed or no-movement returns spend no resources or wear. No random break chance replaces deterministic durability.

## Implementation verification

Required behavior cases include a full fifteen-second history, a partially recorded history, acquisition/use between recording ticks, continuous trimming, multiple hourglasses, different players, ownership transfers, removal/reacquisition, death/logout and the chosen dimension policy. Verify exact position selection and that using the item does not rewind current inventory or health. Payment cases must cover insufficient resources, canceled or obstructed arrivals, successful wear, final durability use and each shipped imbuement route. Trait tests must demonstrate that an authored return-interval expression changes the selected historical time without changing other items' behavior.

No item registration, acquisition recipe, texture, gameplay implementation or testing-pack installation is supplied by this design note.
