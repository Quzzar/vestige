# Pathfinder Second Edition: second 16-spell source batch

**Source research checked October 1, 2026.** This batch freezes 16 additional reviewed sources in [tools/pathfinder-spells.json](../../tools/pathfinder-spells.json), extending the source ledger from 48 to **64 entries**. All 16 were selected from the [32-candidate visual-library review](pathfinder-visual-library-batch.md); their canonical AoN pages and the current book errata pages were directly reread for this frozen batch. The previous 48 entries remain unchanged.

This note records source facts and intended native adaptation boundaries. The [conversion ledger](../design/pathfinder-spell-conversions.md), executable recipes, balance policy, and development status determine which adaptations are actually implemented and verified. A source ledger entry by itself does not establish native gameplay or presentation completion. Wands, discovery, and progression remain deferred.

## Provenance

The batch contains **four cantrips and 12 other spells**: 11 Player Core, one Player Core 2, and four Rage of Elements entries. Remaster core pages are used where available. Rage of Elements retains its actual publication and compatible newer vocabulary; the source ledger's `remaster` edition marker follows the existing batch convention and does not claim those spells were reprinted in Player Core.

Every selected page lists no uncommon, rare, or unique rarity trait, so its source rarity is **common** under the published default. This source rarity describes Pathfinder availability and remains independent of Vestige rarity, traits, outcomes, costs, and recovery. [Common rarity](https://2e.aonprd.com/Traits.aspx?ID=557)

| Publication | Current AoN errata marker reread for this batch | Source |
| --- | --- | --- |
| Player Core | Spring 2026, 1st Printing; applied February 6, 2026 | [Player Core](https://2e.aonprd.com/Sources.aspx?ID=216) |
| Player Core 2 | 1.1; applied December 16, 2024 | [Player Core 2](https://2e.aonprd.com/Sources.aspx?ID=227) |
| Rage of Elements | 2.0; applied May 17, 2024 | [Rage of Elements](https://2e.aonprd.com/Sources.aspx?ID=205) |

These are the source books' displayed update markers, not a claim that every selected spell changed in that release. `aon-2026-10-01` identifies the review date; AoN pages are mutable rather than a versioned repository commit. The existing row schema preserves the canonical URL/ID, printed rank/cantrip/traditions/rarity, publication/page, edition marker, errata reference, revision label, and a short independently written summary.

## Directly verified source facts

**Traditions:** A = arcane, D = divine, O = occult, P = primal. C1 indicates a base-rank-1 cantrip. PC = Player Core, PC2 = Player Core 2, RoE = Rage of Elements. Every behavior below is a short original paraphrase of the linked spell page. Traditions come from that page, not from the proposed native effect or its visual style.

| Source spell / native source slug | Rank; traditions | Printed publication | Recognizable source behavior |
| --- | --- | --- | --- |
| [Glass Shield](https://2e.aonprd.com/Spells.aspx?ID=1333) (`glass_shield`) | C1; AP | RoE p. 94 | A fragile glass defense blocks a hit, then shatters and can wound a nearby attacker. |
| [Translocate](https://2e.aonprd.com/Spells.aspx?ID=1724) (`translocate`) | 4; AO | PC p. 363 | Self-teleport reaches a visible empty destination; a higher-rank version reaches previously visited distant places. |
| [Figment](https://2e.aonprd.com/Spells.aspx?ID=1528) (`figment`) | C1; AO | PC p. 331 | A simple sustained sight or sound can distract observers, who can recognize and disbelieve it. |
| [Illusory Creature](https://2e.aonprd.com/Spells.aspx?ID=1567) (`illusory_creature`) | 2; AO | PC p. 336 | A creature illusion attacks belief, vanishes when hit, and ceases harming observers who disbelieve it. |
| [Invisibility](https://2e.aonprd.com/Spells.aspx?ID=1577) (`invisibility`) | 2; AO | PC p. 339 | Concealment from sight ends on expiry or a completed hostile action; a higher-rank form survives hostile actions. |
| [Grease](https://2e.aonprd.com/Spells.aspx?ID=1547) (`grease`) | 1; AP | PC p. 333 | Slippery ground disrupts footing; object coating instead affects handling, with different held and worn consequences. |
| [Mud Pit](https://2e.aonprd.com/Spells.aspx?ID=2009) (`mud_pit`) | 1; AP | PC2 p. 248 | Thick temporary mud creates difficult terrain on the ground. |
| [Heal](https://2e.aonprd.com/Spells.aspx?ID=1554) (`heal`) | 1; DP | PC p. 335 | Vital energy restores living recipients or harms undead; casting length changes touch, ranged, and area delivery. |
| [Harm](https://2e.aonprd.com/Spells.aspx?ID=1552) (`harm`) | 1; D | PC p. 334 | Void energy harms living recipients or restores undead, with several delivery choices based on casting length. |
| [Detect Magic](https://2e.aonprd.com/Spells.aspx?ID=1485) (`detect_magic`) | C1; ADOP | PC p. 323 | A pulse detects nearby magic; higher ranks add strength and rough location, with deceptive-illusion restrictions. |
| [Read Aura](https://2e.aonprd.com/Spells.aspx?ID=1646) (`read_aura`) | C1; ADOP | PC p. 352 | Extended object examination detects magic and supports identification; deceptive illusions have a relative-rank restriction. |
| [Rust Cloud](https://2e.aonprd.com/Spells.aspx?ID=1377) (`rust_cloud`) | 4; AP | RoE p. 145 | A cutting rust cloud obscures sight; rusting metal creatures can enlarge it and suffer lingering damage. |
| [Cinder Swarm](https://2e.aonprd.com/Spells.aspx?ID=1350) (`cinder_swarm`) | 4; AP | RoE p. 118 | Fiery insects surround a creature; variants combine repeated damage with forced movement or impaired vision. |
| [Summon Animal](https://2e.aonprd.com/Spells.aspx?ID=1694) (`summon_animal`) | 1; AP | PC p. 360 | A sustained animal ally fights for the caster, with stronger creature choices at higher ranks. |
| [Flicker](https://2e.aonprd.com/Spells.aspx?ID=1532) (`flicker`) | 4; AO | PC p. 332 | Planar flickering resists damage except force and repeatedly causes a short random teleport. |
| [Gentle Breeze](https://2e.aonprd.com/Spells.aspx?ID=1316) (`gentle_breeze`) | 2; DOP | RoE p. 70 | A breeze aids medical recovery and affliction resistance, eases heat, and heals living creatures that stay throughout. |

## Intended native adaptations and visual composition

The following choices are **Vestige proposals**, not source rules or a report of completed implementation. Exact formulas, caps, timings, native rarity, payment, and cleanup belong in explicit recipes and policy entries. Preserve relative trait units while balancing outcomes independently.

| Candidate | Intended adaptation boundary | Shared visual composition |
| --- | --- | --- |
| Glass Shield | One finite consumed damage reaction with close-attacker retaliation; preserve actual attacker lineage and avoid repeated retaliation. Shield-raising, saves, and source cooldown are independently adapted. | Attached glass shell + impact ripple + shard burst + immediate consumption fade. |
| Translocate | Safe visible-destination self-teleport first, with inventory preservation; distant remembered destinations remain outside this recipe. | Departure/arrival rings + inward motes + brief silhouette transition. |
| Figment | A short-lived nonphysical image or sound lure with bounded mob distraction; define inspection/reveal rather than inventing tabletop skill rolls. | Small projection + materialization motes + positional sound where applicable + reveal dissolve. |
| Illusory Creature | A destructible decoy can preserve its basic lure identity. Declare omission of reversible mental/nonlethal harm and observer-specific disbelief until those mechanics exist. | Creature/model silhouette + afterimage + one-hit dissolution. |
| Invisibility | Finite status with explicit end-on-hostility behavior. Specify held-item, armor, and observer behavior; decorative particles must not continually reveal its position. | Brief inward particles + fade at start + shimmer on reappearance. |
| Grease | Ground patch affecting movement/footing first; held and worn object coating remain deferred. A slowing approximation must be identified as such. | Surface overlay + low motes + movement streaks within the resolved patch. |
| Mud Pit | Finite slowing ground zone with recipient cleanup on departure; no permanent terrain mutation. | Surface overlay + mud particles + short footstep splashes. |
| Heal | Preserve living restoration and undead damage. Choose explicit native delivery variants instead of copying action counts or source dice directly. | Vitality ribbon/contact burst + ascending motes + optional capped-area ring. |
| Harm | Preserve living damage and undead restoration with separate eligibility and native tuning. State which delivery variants are actually authored. | Shared outcome ribbon/ring + inward void motes + endpoint pulse. |
| Detect Magic | Query recognized active native spell state and explicit enchanted-object facts; private results should not become spell discovery or progression awards. | Sensing ring + caster-only result indicator. |
| Read Aura | Examine an actual selected object, with channel/cancellation rules and explicit identification output; no invented full item-identification system. | Object orbit + focused ribbon + private completion glyph. |
| Rust Cloud | Bounded abrasive field; explicitly state any omitted concealment, metal eligibility, dynamic expansion, or persistent damage. Avoid silently damaging inventories. | Cloud emitter + agitated flecks + pulse ring with actual field extent. |
| Cinder Swarm | Target-attached finite field with capped victims/pulses. Preserve a chosen ant/firefly behavior or explicitly state the selected variant and omitted alternatives. | Captured-target swarm + orbiting insects/embers + timed pulse burst. |
| Summon Animal | One finite owned animal with explicit useful combat AI and native health/damage. A small curated choice is a valid first roster; source heightening remains reference data. | Shared summon ring + animal body reveal + native movement + dismissal motes. |
| Flicker | Bounded mitigation plus repeated safe random relocation. Specify force mapping or its omission; reject invalid destinations and stop on cleanup. | Intermittent afterimages + paired teleport pulses. |
| Gentle Breeze | Delayed living healing after continuous occupancy, with per-recipient dwell tracking. Medical checks and heat rules remain omitted where no native system models them. | Soft field ring + slow wind ribbons + completion motes. |

Reusing a ring, cloud, ribbon, shell, or model transition does not make the spells mechanically interchangeable. Gameplay selects the actual target, area, reaction, and result; cosmetic state follows that outcome and its lifetime. The [shared presentation design](../design/spell-presentation-and-expansion.md) records those responsibilities.

## Verification and limits

The source ledger was checked for exactly 16 appended records, 64 unique source IDs/names/AoN IDs/URLs, an unchanged row schema, and equality of all previous 48 records. All appended name/URL pairs match the previous 32-candidate review. Printed traditions, book pages, base rank, cantrip status, and source rarity were checked against the directly read spell pages rather than inferred from native plans or compendium tags.

No source prose, damage formulas, implementation code, artwork, or logos were imported. These references use short original summaries. Gameplay, balance, generated-document, and client-appearance verification are separate work; this research note claims only the source checks above. Full Pathfinder action economy, saving throws, illusion belief, medical skill checks, heightening, and creature rosters require deliberate Minecraft adaptation rather than a generic damage fallback.
