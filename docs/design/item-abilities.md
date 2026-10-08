# Shared item magic

**October 8 extension direction:** [Item imbuement and readiness](item-imbuement-and-readiness.md) records the owner's requested existing-item audit: crafted variants may change numerical formulas, abilities, payments/recovery and durability while retaining shared trait scaling. It also records the requested native two-overlay visual review. These conversions and the overlay appearance remain pending implementation/review; the older foundation-only limitations below do not cancel this follow-up scope.

October 7, 2026. The owner approved a shared trait and variable foundation for spells and magical items. This implementation provides that foundation. The owner subsequently locked in Wardweave and Cinderweave. Their native adapters now consume this foundation; the other four clothing packages remain proposals in the [equipment review](../art/magic-equipment-review/index.html). Worn protection uses the shared pre-armor event, binding runtime and source validity; absorbed-hit wear uses actual mitigation notifications.

## One definition and resolution path

`MagicDefinition` is the common immutable execution contract: identity, base traits, named variables, typed costs, triggers, composed effects and optional modes. `SpellDefinition` retains its rarity, traditions, discovery identity and source provenance. `ItemAbilityDefinition` uses the same program without adding spell metadata or scroll discovery.

The server loads spells from `runtime_spells` and item abilities from `item_abilities`, using one shared catalog loader and the same JSON grammar. Wardweave and Cinderweave now supply the approved production item-ability definitions. A client stack may select a trusted server definition through an item adapter; it cannot supply executable graphs or arbitrary trait modifiers in NBT.

`SpellRuntime.resolve` combines source-local modifiers, trusted equipped-item contributions and active actor trait boosts exactly once. It returns a `MagicResolution` containing immutable resolved traits and named numerical variables. Scroll, wand and staff casting enter this same resolver after their existing source compilation. Wand/staff affinity and scroll identity still use their unchanged source definitions. A source-local wand contribution does not become a global wearer bonus.

An active ability calls `SpellRuntime.activate` with an event and a source reservation, then shares payment, effects, causal lineage, bindings and cleanup with spells. A passive item or specialized device can call `resolve` to read its values without creating a cast or paying resources. `TraitProvidingItem` is a trusted native item hook; implementations explicitly choose supported equipment slots and eligible base definitions. No existing item has gained an unapproved wearer bonus.

## Variables and lifetime

Both definition kinds accept a `variables` object. Expressions support constants, explicit trait references, sums, products, clamps and references to another named variable. Variables resolve once at activation, including forward references. At most 64 variables and 32 nesting levels are supported; unknown references, cycles and nonfinite outcomes fail. Pure variables do not read changing world facts: existing `capture_value` effects snapshot those when needed.

For example, a proposed Wardweave profile can author these variables with baseline Time 1:

```json
"variables": {
  "vestige:ward_seconds": {"product": [4, {"trait": "vestige:time"}]},
  "vestige:ward_ticks": {"product": [20, {"variable": "vestige:ward_seconds"}]}
}
```

A 25% Time multiplier yields five seconds and 100 ticks. An effect reads a variable with `{"variable":"vestige:ward_ticks"}`. A binding's optional `lifetime` expression determines its expiry; its existing integer `duration` remains the fallback. Manifestations continue to support a formula in `values.lifetime`. The manifestation's duration and attached visual lifetime use that resolved lifetime, bounded by their existing limits. A binding snapshots its lifetime on installation; actor boost expiry does not shorten an already active ward. Future activations resolve the current boosts again. Recasts retain the original activation snapshot and payment.

Traits retain their existing meaning: every numerical relationship is authored explicitly. Time does not inherently lengthen an effect, Amplify does not inherently change mana or recovery, and descriptive tags do not imply hidden bonuses. Typed payment amounts and source recovery remain explicit constants/adjustments in this phase. Only `volatile` has inherent trait semantics; item abilities have no unknown-scroll risk but retain any explicitly authored volatility.

## Active, reactive and passive abilities

- `active` uses the ordinary per-actor casting lane. Preparing or channelled abilities reserve it just as spells do.
- `reactive` can protect or respond while another spell prepares. It cannot reserve preparation time or await a manual recast. It still shares payments, actor recovery, targeting and causal safeguards.
- `passive` supplies resolved values without triggers, costs or effects. It is evaluated by its item/stat adapter.

The generic `grant_traits` effect grants a finite list of additive/multiplicative trait modifiers to selected entities. Its duration can read a named variable. Reapplying the same definition/group to the same recipient replaces that contribution; different authored groups/definitions compose normally. A boost remains after the triggering one-charge binding or delivery ends, owns its independent expiry, and is removed by expiry, dispel, unavailable owner, failed/closed source state, reload or server stop. It changes future activations rather than mutating current ones. These are temporary runtime leases, not persisted player progression.

## Equipment proposal mapping

The canonical [formula proposals](../art/magic-equipment-review/ability-proposals.json) are pure value declarations parsed by the native grammar during tests. They are review fixtures, excluded from production resources. Their baselines agree with the previous review. The owner subsequently accepted removing arbitrary trait-power ceilings and using durability, including wear on absorbed hits, as the ongoing cost. Scalar duration, reach, speed and protection declarations now continue growing; actual fire prevention is bounded at the whole incoming hit. Fixed persistent capacities and target-count proposals remain separate decisions. Exact starting formulas and controls are still in review; the sliders evaluate the current declarations.

| Item | Proposed numerical relationships | Fixed starting rules |
| --- | --- | --- |
| Wardweave | Time → ward duration; Force and Amplify → next-hit protection | One charge; 12-second recovery; +25 maximum mana |
| Cinderweave | Fire → mitigation fraction; Amplify → per-hit cap | Fire-tagged damage only; +25 maximum mana |
| Wayfarer | Time → burst duration; Motion → movement bonus; Amplify → landing protection | 5 mana; 15-second recovery; one landing |
| Dawnsight | Time → reveal duration; Range → reveal distance | Private reveal; line of sight; eight targets; 10 mana |
| Patchwork | Space/Conjuration describe its pocket magic | Four actual-object pockets; +25 maximum mana; contents persist |
| Spiderstep | Motion → climbing speed | 2 mana per second attached; vertical walls only |

Ordinary armor, durability, wool color and trusted item identity retain their own rules. The approved robe adapters and mana synchronization now add 25 maximum mana while worn, without refilling the pool, and persist the clamp on removal. The unequipped baseline remains 100. Persistent storage capacities cannot fluctuate with temporary boosts without an approved overflow/access policy.

The owner excluded Unbreaking and Mending from magical clothing, while allowing ordinary compatible armor enchantments. The approved robe adapters enforce that policy and preserve authored wear and material repairs, including absorbed-hit wear. The shared trait resolver does not itself implement enchantment eligibility; remaining clothing implementations must use the same equipment policy.

## Existing devices and next work

Homebound Eye is the next strong candidate for shared teleportation parameters and payment helpers, preserving its exact attunement, destination, accepted payment routes and rollback behavior. Crane Bag, Whispering Shell and Standing Stones can use the common resolver where an actual numerical ability needs it. Their stored pool contents, channel membership, network keys and destinations remain owned by their existing adapters. No distance limit, cost or capacity change is added merely to give a descriptive trait a numerical consumer.

Next: approve each clothing ability/control/formula, then implement its item adapter and crafting route against this foundation. Storage changes and a Homebound Eye conversion remain separate follow-up work. Artwork approval remains independent.

## Verification

`UnifiedMagicTest` covers shared player/source modifier composition, immutable snapshots, five-second binding expiry, actor/group scope, recasts/pay-once, discovery/volatility boundaries, failed payment, invalid variables, reactive casting and independent boost ownership. `NamedMagicSourceTest` covers preservation through scroll/wand compilation and thread riders. `EquipmentFormulaTest` parses all six review fixtures, verifies baselines and shared Time response, and checks large boosts beyond the former power ceilings, a valid fire fraction and stable storage/payment values.

`MagicAbilityTest` exercises actual Minecraft incoming damage, native mana payment, extended ward expiry and reactive protection during spell preparation. Executed totals and current limitations are recorded in [development status](../development-status.md). The browser tool blocked the local `file:` URL, so the new slider layout has not been visually verified; prior screenshots document the earlier review page only.
