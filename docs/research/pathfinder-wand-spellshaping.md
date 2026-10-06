# Pathfinder 2e wands and Spellshaping inspiration

**Status:** upstream research, checked October 3, 2026. These findings describe Pathfinder rules and naming, not accepted Vestige modifier mappings, numerical balance, or native wand implementation. The user's proposed Vestige format is **Scroll of *Reaching* Fireball**, with the modifier italicized. Native wands remain deferred.

## Ordinary names and specialty rules

An ordinary magic wand contains a chosen spell at a chosen rank. Its name follows **Wand of [spell name]**. A specialty wand instead uses its own stat block: its craft requirements restrict which spells it can contain, and its activation changes the spell's effects or casting. Both rules are explicit in the [GM Core wand rules](https://2e.aonprd.com/Rules.aspx?ID=3218).

The inspected rules do not establish a universal system that attaches any adjective to any spell, a universal adjective ordering rule, or a rule for stacking arbitrary specialties. Some specialties are reusable across an eligible category; others are authored around one particular spell. That distinction follows from the individual craft requirements below and the [specialty wand rules](https://2e.aonprd.com/Equipment.aspx?Category=34&Subcategory=36).

## Verified examples

| Published item | Eligible spell or spells | Modification in Pathfinder | Relevant design distinction |
| --- | --- | --- | --- |
| [Wand of Reaching](https://2e.aonprd.com/Equipment.aspx?ID=2287), Treasure Vault (Remastered), p. 141 | A spell of the appropriate rank with a range and a qualifying casting time | Extends range, including touch spells, and adds an action to the qualifying casting time. | A reusable specialty category with explicit eligibility and a cost. It still contains one chosen spell. |
| [Wand of Dumbfounding Doom](https://2e.aonprd.com/Equipment.aspx?ID=2278), Treasure Vault (Remastered), p. 139 | Impending Doom of the appropriate rank | Adds stupefaction if the spell frightens the target, lasting for the spell's duration. | An individually authored variation of Impending Doom. The published name shortens the spell's title; this is not an adjective rule that applies to every spell. |
| [Wand of Contagious Frailty](https://2e.aonprd.com/Equipment.aspx?ID=2275), Treasure Vault (Remastered), p. 139 | Enfeeble | If the initial target becomes enfeebled, nearby creatures must also resist the spell, with more favorable outcomes for those secondary targets. | An individually authored spreading variation of Enfeeble, with a triggering condition and weaker secondary application. |
| [Wand of Shrouded Step](https://2e.aonprd.com/Equipment.aspx?ID=2291), Treasure Vault (Remastered), p. 142 | Fleet Step | Adds concealment while the user Strides during the spell's duration. | An individually authored variation of Fleet Step whose extra effect applies during a particular action. |

These entries demonstrate both categories and conditional extensions. They do not make Reaching, Dumbfounding, Contagious, or Shrouded universally compatible with every spell. [Reaching](https://2e.aonprd.com/Equipment.aspx?ID=2287), [Dumbfounding Doom](https://2e.aonprd.com/Equipment.aspx?ID=2278), [Contagious Frailty](https://2e.aonprd.com/Equipment.aspx?ID=2275), [Shrouded Step](https://2e.aonprd.com/Equipment.aspx?ID=2291).

## Inferences for Vestige

The following are design inferences from those examples, rather than Pathfinder rules or accepted Vestige mechanics:

- Separate a **reusable transformation** from its **eligibility conditions**. A range transformation can apply to casts with a meaningful range; an effect extension can require a relevant outcome or target.
- Describe conditional extensions as a trigger plus an added effect. Dumbfounding Doom suggests a fear outcome triggering a mental impairment; Contagious Frailty suggests an initial condition triggering a bounded secondary application; Shrouded Step suggests a movement action triggering concealment.
- Keep the base spell identity intact. Vestige can retain the full title in **Scroll of *Dumbfounding* Impending Doom**, even though Pathfinder's published specialty uses a shorter name. Modifier italics and generated names are Vestige presentation choices.
- Borrow the composition idea without copying Pathfinder's action counts, distances, daily wand use, condition magnitudes, prices, ranks, or save adjustments into Minecraft balance.

All four inferences come from the explicit eligibility and activation structure in the [GM Core rules](https://2e.aonprd.com/Rules.aspx?ID=3218), [Reaching](https://2e.aonprd.com/Equipment.aspx?ID=2287), [Dumbfounding Doom](https://2e.aonprd.com/Equipment.aspx?ID=2278), [Contagious Frailty](https://2e.aonprd.com/Equipment.aspx?ID=2275), and [Shrouded Step](https://2e.aonprd.com/Equipment.aspx?ID=2291). They do not identify Minecraft support blocks or Iron material meanings; those require separate material research.

## Source version and corroboration

The AoN pages inspected above identify **Treasure Vault (Remastered)**. Its [source page](https://2e.aonprd.com/Sources.aspx?ID=191) reports the June 4, 2025 revision and errata; each item page also offers its legacy version. This note uses the current remastered pages for spell naming, particularly **Enfeeble**. It does not assert that every legacy name or stat was identical.

Foundry's source-owned PF2e repository corroborates the spell/effect pairings at revision `4cbdaa37d6c33e9519561bae2c59a23e0288cbce`: [Reaching](https://github.com/foundryvtt/pf2e/blob/4cbdaa37d6c33e9519561bae2c59a23e0288cbce/packs/equipment/wand-of-reaching-1st-level.json), [Dumbfounding Doom](https://github.com/foundryvtt/pf2e/blob/4cbdaa37d6c33e9519561bae2c59a23e0288cbce/packs/equipment/wand-of-dumbfounding-doom-4th-level.json), [Contagious Frailty](https://github.com/foundryvtt/pf2e/blob/4cbdaa37d6c33e9519561bae2c59a23e0288cbce/packs/equipment/wand-of-contagious-frailty.json), [Shrouded Step](https://github.com/foundryvtt/pf2e/blob/4cbdaa37d6c33e9519561bae2c59a23e0288cbce/packs/equipment/wand-of-shrouded-step.json). Those records have legacy publication flags and are corroborating implementation data, not the authority for current remastered wording. No upstream rules text or assets are copied into native spell definitions by this research.
