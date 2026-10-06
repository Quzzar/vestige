# Spell rarity and balance

Vestige spells have four rarities: **common, uncommon, rare, and mythic**. Rarity expresses expected overall value and eventual availability. Spells of one rarity should offer comparable value after their costs, reliability, timing, and constraints are considered. A rarer spell can offer stronger outcomes, greater flexibility, or unusual utility; it does not need to deal more damage than every spell below it.

This is the balancing approach as of October 2, 2026. All 214 current spells have explicit rarity and a complete initial native balance pass, documented in the [per-spell review](../spell-balance-review.md). Balance is assessed through resolved outcomes, costs, timing, reliability, and constraints. Trait magnitudes and totals are relative authoring choices and do not determine rarity or establish equal gameplay power.

## Rarity in definitions

```json
{
  "rarity": "uncommon",
  "traditions": ["arcane", "primal"],
  "traits": {
    "vestige:fire": 4,
    "vestige:evocation": 4,
    "vestige:amplify": 1,
    "vestige:range": 1,
    "vestige:area": 1
  }
}
```

This fragment shows Fireball's classification. [SpellDefinition](../../src/main/java/com/quzzar/vestige/magic/definition/SpellDefinition.java) owns [SpellRarity](../../src/main/java/com/quzzar/vestige/magic/definition/SpellRarity.java) independently of its trait profile. `vestige:spell/rarity` exposes the serialized name as a text condition value. Cast modes and trait modifiers keep the spell's rarity.

The parser accepts exactly `common`, `uncommon`, `rare`, and `mythic`. Older definitions without rarity default to common; the checked-in catalog always authors it explicitly. Rarity does not implicitly multiply damage, change mana, install a cooldown, or create discovery/progression behavior.

## Relative ratings and formula calibration

A rating acquires numerical meaning through the formula that reads it. Fire 100 can produce 200 damage with coefficient 2, or one damage with coefficient 0.01. Raw magnitude alone cannot distinguish those outcomes, and summing ratings across different spells does not give a useful power score.

The ratios within a profile can express the spell's character. A fire-to-range ratio of 100 to 200 has the same proportions as 1 to 2. We can choose a simpler representation when all affected formulas and other consumers use the corresponding units:

| Representation | Fire | Range | Damage formula | Reach formula | Resolved outcomes |
|---|---|---|---|---|---|
| Larger units | 100 | 200 | `0.01 × fire` | `0.1 × range` | 1 damage, 20 blocks |
| Reduced ratio | 1 | 2 | `1 × fire` | `10 × range` | 1 damage, 20 blocks |

Both representations also respond the same way to doubling fire or range. The coefficient compensation preserves that behavior. Reducing ratings without adjusting their consumers would change the spell.

For original ratings `T`, a common scale factor `k`, and reduced ratings `T' = T / k`, an equivalent formula is:

```text
F'(T') = F(k × T')
```

A linear coefficient absorbs `k`; a term multiplying two rescaled traits absorbs `k²`. Sums, branches, thresholds, captured values, and external consumers must preserve their own meaning too. Use simple ratios and clean formulas when helpful; there is no requirement to minimize every profile or make every spell share one raw scale. The runtime does not automatically normalize definitions or resolved profiles.

The 110 Iron adaptations retain descriptive ratings of four and scaling baselines of one as authoring conventions. The 100 Pathfinder adaptations use descriptive unit one with independently calibrated outcome coefficients. Source rank, cantrip status and source-common rarity are reference metadata rather than native multipliers. Their costs and native rarity are reviewed alongside the existing catalog. Interposing Earth retains its original descriptive profile: earth 5, stone 3, abjuration 4, and conjuration 2. Those numbers do not determine the ward's common rarity or its protection; its plan reduces the next hit by `2 × amplify`.

## Rarity and balancing criteria

Explicit assignments for all 214 spells live in [spell-balance-policy.json](../../tools/spell-balance-policy.json). The file records rarity, role, mana, charge, native cooldown, and a tuning rationale for every spell. It has no trait-point allowances, weighted totals, or quadratic load checks.

| Rarity | Initial authoring role |
|---|---|
| Common | Basic attacks, modest healing, limited protection, focused utility |
| Uncommon | Broader attacks, buffs, movement combinations, useful interactions |
| Rare | Sustained areas, major control, summon cohorts, advanced utility |
| Mythic | Exceptional protection, singularities, large sustained attacks, private spaces |

Comparable gameplay value within a rarity comes from actual outcomes and constraints. The ratings provide inputs to those outcomes; changing their raw scale is not a balancing intervention when the behavior remains equivalent.

## Modifiers and trait units

Multiplicative boosts are preserved by a compensated change of units. Fixed additions are unit-sensitive. For the one-damage fire example, adding one to fire 100 with coefficient 0.01 produces 1.01 damage. Adding one to fire 1 with coefficient 1 produces two damage. To preserve equivalence, the additive adjustment must be converted with the trait units as well.

The existing trait resolver adds adjustments before applying multipliers. Equipment bonuses, absolute trait thresholds, and integration rules must respect the chosen units when ratings are rewritten. None of those consumers is silently changed by simplifying a profile. `volatile` is the existing exception to descriptive traits: its inherent forfeit policy interprets the absolute rating, so ordinary ratio simplification does not apply to it.

The October 2 [scroll discovery design](spell-discovery.md) adds a planned acquisition consumer: dismantling weights traits by their proportions within one spell, while discovery first filters spells to those containing every trait supplied by at least four placed fragments. Traits may differ. Eligible spells are weighted by their average base rating across the fragment slots, counting duplicates; an all-Fire setup therefore retains the candidate's Fire rating as its weight. Supporting-block effects remain open. Fire 5 / evocation 5 and fire 1 / evocation 1 yield the same fragment proportions, while the former has five times the latter's weight in a fire reconstruction pool. Compensating effect formulas cannot preserve those odds after independently rescaling a profile. Review acquisition weights alongside unit changes; they affect access to spells without turning raw trait totals into a combat power budget. Equipment multipliers do not affect these base weights.

## Numerical boost response

For outcome parameter `y`, resolved traits `T`, and multiplier `k` applied to trait `t`, compare:

```text
response ratio = y(T with t multiplied by k) / y(T)
finite boost sensitivity = (response ratio - 1) / (k - 1)
```

The audit evaluates doubling every listed trait, plus the three scaling traits and evocation when absent. It also records amplify and area together, all three scaling traits together, and two stacked double-amplify modifiers. Two multipliers of two produce a multiplier of four under the existing trait resolver.

| Parameter | Formula | Response when its scaling trait doubles |
|---|---|---|
| Fireball hit | `8 × amplify` | 8 to 16 authored damage |
| Fireball travel budget | `32 × range` | 32 to 64 blocks |
| Fireball impact radius | `3 × area` | 3 to 6 blocks |
| Echoing Strikes extra hit | `0.5 × event damage × amplify` | Exactly twice the extra hit for a fixed event |
| Throw impact | `amplify + captured weapon damage` | Context dependent; only the first term doubles |
| Heartstop repayment | `0.5 × deferred damage` | Its plan does not read scaling traits |

World facts remain symbolic. The audit reports an exact ratio only when every symbolic coefficient changes by the same ratio. It preserves mixed responses instead of inventing health, weapon damage, nutrition, or event damage values. Zero baselines are handled without division by zero.

## Geometry and combined boosts

A circular footprint grows with radius squared; a spherical volume grows with radius cubed. Under the corresponding uniform target-density assumptions:

```text
2× radius → 4× planar footprint
2× radius → 8× volume
2× damage and 2× radius → 8× aggregate damage in planar density
2× damage and 2× radius → 16× aggregate damage in volumetric density
```

These are geometric proxies, not guaranteed extra victims. Target caps, visibility, terrain, clustered encounters, ally filtering, accuracy, immunity, and overlap change actual results. The report keeps queried-radius coverage separate from numerical damage and does not treat it as an observed combat multiplier.

A future item doubling both amplify and area therefore needs a separate review from one doubling amplify alone. Range can increase coverage on cones or melee sweeps and reach new targets on beams and chains. The current adapter bounds target distances and other parameters, so measurements near those bounds may be smaller than the raw authored response.

## Comparing spells within one rarity

Compare spells by role and by total value over a useful encounter window. A single scalar cannot safely compare healing, a private room, teleportation, and an attack.

| Family | Values to compare |
|---|---|
| Direct attacks | Damage per hit, accuracy, target count, pierce, weapon contribution, burn or status follow-up |
| Channels and fields | Damage per pulse, pulses over lifetime, uptime, coverage, interruption risk, setup time |
| Healing and protection | Effective healing, prevented damage, charges, duration, overheal, eligibility, drawbacks or repayment |
| Control | Duration, severity, reach, escape and destruction options, number of targets |
| Summons | Number, health, combat throughput, ownership behavior, lifetime, AI reliability, dismissal |
| Utility | Tasks enabled or bypassed, frequency of useful opportunities, limits, persistence, risk, alternatives |

For an attack with measured or explicitly assumed hit probability `h`, damage per hit `d`, pulse count `n`, and eligible victims per pulse `v`, a useful comparison is `h × d × n × v`. Record assumptions beside the measurement. Mana and time efficiency divide a comparable outcome by actual spent mana or the relevant execution window. They are undefined when the denominator is zero and are poor cross-family measures when outcome units differ.

Native cooldowns and mana are authored independently of source provenance and enforced by paid casts. Use `/vestige_magic mana 100` and `cast_balanced` in Survival to exercise them. The ordinary development `cast` command bypasses payment and cooldowns, so its observed cast frequency is not a balance measurement. Player mana recovers according to the accepted baseline below; capacity progression remains deferred. Recasts share their initial payment and cooldown, and only one active charge/channel per actor is allowed. Interrupted charges and failed payment do not start cooldown; a paid cast retains its cooldown through effect failure or interruption.

## Planned player mana baseline

**October 6, 2026: accepted and implemented baseline.** Players have **100 maximum mana**, start with full mana on first spawn and after death/respawn, and have a mana bar that is hidden at full mana and visible while mana is missing, including at zero. The native runtime implements this player pool, recovery and synchronized mana HUD. Relogging preserves the current balance and pending recovery delay; nondeath clones preserve both as well.

The catalog already contains explicit native costs for all 214 spells. Inspection of the packaged base definitions and successful balance checks gives:

| Rarity | Spells | Base mana range | Median base mana |
| --- | ---: | ---: | ---: |
| Common | 51 | 3–16 | 10 |
| Uncommon | 89 | 16–36 | 24 |
| Rare | 64 | 32–60 | 40 |
| Mythic | 10 | 60–90 | 76 |

At 100 mana, with no replenishment, an unshaped Firebolt costs 12 (eight casts), Heal costs 15 (six), Fireball costs 28 (three), Recall costs 30 (three), and Cataclysm costs 90 (one). Every unmodified base spell can be afforded from a full pool. Crafted cost increases and added payments can exceed the base values, so that statement is not a guarantee for every shaped scroll. Keep authored spell costs for the first pool/recovery playtest rather than rescaling the catalog solely to change the test cap.

**Accepted recovery:** recover 2 mana per second after five seconds without a successful mana expenditure. An empty 100-mana pool then fills in 55 seconds. Spending 30 mana would take 20 seconds to recover when no other mana is spent. A successful mana payment from a spell or device restarts the delay; rejected payment does not. Keep this delay separate from each spell's authored cooldown and charge time. These timings are arithmetic forecasts, not observed survival-playtest results.

The HUD should display current mana while below the maximum, including during any recovery delay, and disappear when completely refilled. Player initialization, death reset, passive recovery and authoritative server-to-client snapshots are implemented. The violet gauge occupies the gap between the XP strip and hotbar, leaving the vanilla health, hunger and XP displays in place. No capacity progression is selected by this baseline.

[Homebound Eye](attuned-devices.md#durability-and-imbuement) has an accepted price of 30 mana plus 2 durability per return, with separate accepted health, hunger and XP prices. Review the starting device prices against the implemented recovery rate and real travel/escape opportunities during playtesting.

## Audit and verification

The [current balance audit](../spell-balance-audit.md) covers every definition. Its [JSON report](../spell-balance-audit.json) includes baseline parameters after trait substitution, per-expression paths and responses, individual trait boosts, combined scaling boosts, and review flags for repeated, reactive, persistent, summon, and utility behavior. World facts remain symbolic in the baseline terms.

```bash
python3 tools/audit_spell_balance.py
python3 tools/audit_spell_balance.py --check
python3 tools/test_spell_balance.py
python3 tools/document_spell_balance.py --check
```

Check mode verifies policy coverage, rarity agreement, and report freshness. Python behavior tests exercise equivalent trait scales with compensated formulas, unit-sensitive additions, symbolic facts, mixed responses, stacked amplification, spatial growth, addon traits with large ratings, and zero baselines. Java tests exercise parsing, catalog coverage, and rarity conditions through the runtime.

The [complete balance review](../spell-balance-review.md) records actual outcome tuning, role comparisons, native costs, and direct per-creature ceilings for all 214 spells. Regression encounters test actual Minecraft health changes, modifier scaling, crowd and hit caps, cooldown payment, protection, control, and summons. This completes the initial catalog pass; future PvP, AI, terrain and multiplayer playtesting can refine it. Formula response remains separate from observed gameplay value.
