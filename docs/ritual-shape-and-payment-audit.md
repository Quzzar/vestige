# Ritual shape and resource payment audit

**Historical pre-fix snapshot.** The owner subsequently authorized the five ordered construction families and common resource rebalance. [Current ritual patterns](design/ritual-crafting.md) and [resource values](design/resource-payments.md) supersede the recommendations and old runtime prices below. The saved source hashes record the audited state, not the corrected implementation.

**October 8, 2026.** The owner locked the [Kairotic Hourglass](design/kairotic-hourglass.md) ingredients and unmodified 15-second / 60-mana / 10-durability / one-wear baseline, then requested a review of shapeless crafting and resource valuation before proceeding with that item. This audit examines the current shared working tree. It changes no gameplay, existing recipe acceptance or resource prices. Recommendations below remain proposals; previously documented approvals are not silently revoked.

## Findings

1. **Spell and equipment crafting still preserve shape.** All 214 spell recipes are shaped: 139 use four slots and 75 use eight. Wands, Staff construction/expansion and both robes use relative patterns with whole quarter-turn matching. They have not become arbitrary ingredient permutations.
2. **Shapeless matching has spread across component/device construction.** Eight operation families currently accept free placement, listed below. Five are candidates for shaped construction under the owner's puzzle direction. This is a design review, not evidence that their matchers violate their current documentation.
3. **Bloodbound/Fasting do not encode the desired payment hierarchy.** Their exchanged-mana portion charges one whole heart or one food point per five mana. Bloodbound separately adds Amplify ×1.05; the exchange rate itself still treats those sacrifices equally.
4. **Exhausting has a confirmed missing drawback.** It increases Amplify and reduces mana in exchange for multiplying cooldown. There are no executable cooldown costs in any of the 214 packaged spells or their modes. Source cooldown metadata does not supply a native cooldown. The multiplier therefore adds no price on the shipped catalog.
5. **Homebound Eye and future travel rates need valuation review.** Eye health payment buys more useful life than food/XP, but food and XP buy the same wear reduction. That alone does not prove bad balance: their quantities and recovery costs differ. Standing Stone alternate payments are not implemented, so no bad live health/food conversion exists there yet.
6. **Passing balance checks do not establish resource economy balance.** Current checks verify authored data, outcomes and numerical responses. They do not enforce health > food > XP or detect that Exhausting's intended price disappeared when ordinary cooldowns were removed.

## What is shapeless now?

Here, **shaped** means relative ingredient relationships matter, while rotating the whole pattern remains valid. **Shapeless acceptance** means ingredients can occupy any available seats in the selected table. It does not necessarily mean arrangement has no effect on the output.

| Ritual operation | Current behavior | Audit recommendation |
| --- | --- | --- |
| Fragment discovery | At least four fragments; order does not select the weighted trait-intersection pool. | Keep unordered. Discovery is a trait-combination operation, distinct from reproducing a known spell recipe. |
| Attunement Shard | Six fixed offerings on eight nodes, in any arrangement. Positions, empty seats, local imbuements and geometry determine the key. | Keep free arrangement. Shape already matters to identity; a fixed pattern would unnecessarily restrict the signature space. |
| Magical threads | String + Amethyst Shard + Honeycomb in any three seats; the String's own socket selects one of five outputs. | Review for one shared shaped three-ingredient pattern with an intentional empty seat. Preserve all five local selectors. |
| Homebound Eye | Shard + Spider Eye + Pearl + Flint in any four seats; the Eye's socket selects payment. | Review for shaped construction. Keep the copied key, actual crafting origin and offering-local payment selector. |
| Whispering Shell | Shard + Nautilus Shell + Sculk Sensor in any three seats. | Review for shaped construction with one intentional empty seat. Continue copying the existing key unchanged. |
| Standing Stone | Shard + Pearl + two matching masonry blocks in any four seats; 36 body finishes. | Review for shaped construction shared across finishes. Appearance must not create 36 different mechanical patterns. |
| Fluxed Flint construction | Flint + Diamond Block + Netherite Ingot + Echo Shard in any four seats; composition is provisional. | Prefer a shaped construction recipe after the current ingredient-design hold is resolved. Do not implement another revision while the owner is defining Dissentient Diamond. |
| Fluxed Flint repair | Catalyst + one damaged magical item in any two seats. | Keep shapeless. This is a reusable maintenance operation, not a new hidden construction puzzle for each target. |

The October 8 capacity clarification says existing shapeless operations may use either ring on an eight-slot table. That is a rule about available seats, not an independent argument that future device construction should be shapeless. Converting a family would require explicit patterned matching, empty-seat validation and corresponding viewer/test updates; it cannot be done by merely changing its label.

Attunement deserves special care: swapping two different offerings generally creates a **different** shard key even though the recipe still succeeds. Whole rotations preserve the complete paired arrangement. Its ordering, layer, empty-node and color-family tests already distinguish these behaviors. Device construction subsequently copies that key; the device's own ingredient order currently has no identity effect.

Sources: [ritual routing](../src/main/java/com/quzzar/vestige/apparatus/RitualCrafting.java), [thread recipe](../src/main/java/com/quzzar/vestige/apparatus/MagicalThreadRecipe.java), [Eye recipe](../src/main/java/com/quzzar/vestige/apparatus/HomeboundEyeRecipe.java), [Shell recipe](../src/main/java/com/quzzar/vestige/apparatus/WhisperingShellRecipe.java), [Stone recipe](../src/main/java/com/quzzar/vestige/travel/StandingStoneRecipe.java), [Flint recipes](../src/main/java/com/quzzar/vestige/apparatus/FluxedFlintRecipe.java), [attunement identity](design/attunement-shards.md).

### Possible replacement shapes, not approved changes

Each row is an **inner four-seat clockwise order**, with no absolute north/left requirement. The ingredient's imbuement moves with that ingredient during a whole rotation.

| Construction | Relative clockwise pattern |
| --- | --- |
| Any magical thread | String → Amethyst Shard → Honeycomb → empty |
| Homebound Eye | Attunement Shard → Flint → Spider Eye → Ender Pearl |
| Whispering Shell | Attunement Shard → Sculk Sensor → Nautilus Shell → empty |
| Any Standing Stone finish | Attunement Shard → masonry → Ender Pearl → same masonry |

The two matching Stone blocks make that proposed pattern naturally symmetric: a reflected arrangement can also be a rotated valid arrangement. Distinct-reflection matching must not pretend identical ingredients are distinguishable. The other distinct-item patterns retain their handedness. These examples need collision checks against all public recipes, maintained offering/socket locality, native cancellation tests and correct public diagrams before implementation.

### Ordinary crafting-grid recipes

The recursive native recipe directory contains **83 JSON recipes**: **79 shaped**, **three shapeless** and **one custom scroll-dismantling recipe**.

- The substantial shapeless grid recipe is the previously accepted **Amethyst Shard + Soul Sand + Ghast Tear → Echo Shard**. It is ordinary ingredient synthesis, usable in the player grid, rather than a Spellstone placement puzzle. Keeping it shapeless is reasonable under that distinction; changing it remains a separate decision.
- The other two shapeless recipes are optional single-pedestal conversions from Iron's and Supplementaries to a matching Plinth. A one-input conversion has no meaningful relative pattern to solve.
- Scroll dismantling consumes one scroll to yield fragments. There is likewise no multi-ingredient order to preserve.

Plinth/Spellstone construction, the mundane Staff bases and the other ordinary construction grids are shaped. Crane Bag still has no survival recipe. The hourglass is a locked design without an executable acquisition recipe, so it is not included in these implementation counts.

Sources: [ordinary recipes](../src/main/resources/data/vestige/recipe), [shaped spell matcher](../src/main/java/com/quzzar/vestige/apparatus/RitualRecipe.java), [wand pattern](../src/main/java/com/quzzar/vestige/apparatus/WandRecipe.java), [Staff patterns](../src/main/java/com/quzzar/vestige/apparatus/StaffRecipe.java), [robe patterns](../src/main/java/com/quzzar/vestige/equipment/MagicArmorRecipe.java).

## Resource valuation

The accepted direction is **health > food > experience in sacrifice value**. This is not a unit conversion: one HP, one heart, one food point, one hunger icon and one XP point are different quantities. One heart is two HP; one full hunger icon is two food points. XP is charged in points, not levels.

To compare two routes, hold the useful outcome constant and compare the mana saved or wear avoided, actual resource charge, reservoir left afterward, and practical recovery burden. For the same relief, a health route should need a modest sacrifice; food should require a larger burden; cheap renewable XP should buy the weakest relief or require a substantial quantity. Do not declare every health route best in every situation: healing access, hunger-enabled natural recovery, enchantment demand and XP farms change practical costs.

### Bloodbound and Fasting: exchange correction needed

The [shared cost composer](../src/main/java/com/quzzar/vestige/magic/runtime/CastShaping.java) calculates both exchanged portions from adjusted mana and divides each by **five**, rounding that quantity up. It converts the health result to HP by multiplying by two. The [authoring rules](../tools/author_spellshaping.py) make each contribution exchange 25% of adjusted mana, with a shared combined maximum of 75%.

At neutral geometry, on a controlled **20-mana source**, one contribution gives:

| Adjustment | Final mana | Additional payment | Separate potency contribution |
| --- | ---: | --- | --- |
| None | 20 | None | None |
| Bloodbound | 15 | **2 hearts / 4 HP** | Amplify ×1.05 |
| Fasting | 15 | **2 food points / one hunger icon** | None |

Both use `20 × 1.02 = 20.4` adjusted mana; 25% is 5.1, whose five-mana exchange units round up to two. Remaining mana rounds to 15. These are arithmetic witnesses from the current formula, not new gameplay measurements. A small Amplify increase can itself disappear in final rounding on a small numerical outcome; it does not reliably compensate for the exchange disparity.

**Recommendation:** give health and hunger independently authored rates, review the 25% contribution and 75% combined ceiling together, and test both low-cost rounding and expensive casts. Mixed contributions must still be affordable together and commit atomically. Do not change the global meaning of a Health or Hunger cost to implement valuation; change the authored exchange prices.

### Exhausting: current penalty is ineffective

The executable rule is **Amplify ×1.05, mana ×0.94, cooldown ×1.20**, selected by Phantom Membrane / Iron Block. Compatible unchanged recipes include Cone of Cold and Angel Wing. On an unmodified Cone of Cold, its 42 mana becomes **39** after final rounding, while its existing Amplify consumers increase. Its cooldown remains absent.

All packaged base and mode costs comprise **220 mana entries and 132 preparation-time entries**, with **zero executable cooldown, health, hunger or XP entries**. Source `cooldown_ticks` is provenance only. There is no additional wand/Staff recovery to multiply. Thus the supposed Exhausting cooldown price cannot take effect on any shipped source.

**Recommendation:** replace the vanished price with an explicit, nonzero cost such as hunger or added preparation, or remove the rule until a real drawback is selected. Do not restore ordinary cooldowns through this audit; their removal was an owner decision. A regression check should require an advertised drawback to affect at least one actual cost or constraint on every accepted source.

### Homebound Eye: review, not a proven conversion defect

The [implemented Eye](../src/main/java/com/quzzar/vestige/apparatus/HomeboundEyeItem.java) has 30 durability and owner-approved October 6 starting prices:

| Route | Wear | Additional payment | Fresh-item returns | Wear saved versus default |
| --- | ---: | --- | ---: | ---: |
| Default | 6 | None | 5 | 0 |
| Health | 1 | 3 hearts / 6 HP | 30 | 5 |
| Food | 2 | 6 food points / 3 icons | 15 | 4 |
| XP | 2 | 25 XP points | 15 | 4 |
| Mana | 2 | 30 mana | 15 | 4 |

Health already buys the strongest wear reduction. Food and XP buy the same reduction for different quantities, so this is **not** evidence that one food point equals one XP point. However, no practical evidence currently establishes that 25 XP is as burdensome as six food points, and the latest owner preference calls for reviewing that relationship. Candidate tuning could lower XP's wear savings or increase its charge; no replacement numbers are approved by this audit. Preserve the existing table until a reviewed revision is selected.

### Other systems

- **Standing Stone:** only the accepted distance-based XP fare is live: `12 + 12 × sqrt(distance / 1024)`, rounded once. Health/food/mana routes and Lapis discounts are proposals. Give each future route its own calibration under the owner's hierarchy; do not derive them with a one-for-one resource substitution. [Payment design](design/standing-stone-payments.md).
- **Famished and Sustaining:** introduce explicit food prices for different benefits: Amplify ×1.10 plus mana ×1.02 / one food point, and a bounded healing continuation plus mana ×1.08 / two food points. These are not health/food/XP exchange rates. They need recovery-economy playtests, rather than being automatically declared wrong because their numbers are small.
- **Gluttony and healing:** food consumption can restore mana through Gluttony, and healing can replenish health payments. Future rate tests should include these interactions and repeated use, rather than only full-resource fixtures. No infinite-resource exploit is established by this source audit.
- **Wands and Staffs:** inherit typed source payments through shared compilation. Their own current component profiles add mana/preparation/wear changes, rather than another independent health/food/XP exchange table. Fixing the common exchange will therefore affect their shaped source scrolls too; avoid applying it a second time at equipment binding.
- **Whispering Shell and Crane Bag:** have no per-message/per-access health, hunger or XP payment. Construction/access balance is a separate question.
- **Fluxed Flint:** trades finite catalyst budget and risk for durability repair, not health/food/XP. Repair makes total item lifetime different from uses on one fresh durability bar. Future hourglass repair eligibility remains unspecified.
- **Kairotic Hourglass:** retain its locked unmodified baseline. Its alternate payment rates and durability modifiers are still unimplemented; the withdrawn equal-heart/food proposal must not return through copied shared constants.

## Verification and limits

Executed against this working tree:

- `python3 tools/author_ritual_recipes.py --check`: 214 explicit recipes, index and ledger agree; no rotational/alternative collisions.
- `python3 tools/test_ritual_recipes.py`: **11 tests pass**.
- `python3 tools/author_spellshaping.py --check`: **56 executable rules**, unique automatic pairings and candidate unchanged ingredient routes.
- `python3 tools/test_spell_balance.py`: **17 tests pass**.
- Recursive JSON inventory, arithmetic witnesses, selected cost-rule extraction and source hashes are saved in [audit evidence](verification/ritual-shape-payment-audit-2026-10-08/evidence.json).

These checks explain why the present data can be internally consistent while still containing the identified design problems. No gameplay code, prices, recipe matchers, textures, installed pack or publication changed. No new Java/world/client run or survival-economy measurement is claimed. The current working tree contains concurrent work; the evidence hashes identify the inspected inputs.

Next review decisions are whether to adopt the proposed shaped construction families, which actual drawback should replace Exhausting's cooldown factor, and the replacement resource rates. After those choices are implemented and checked, return to hourglass shaping and implementation.
