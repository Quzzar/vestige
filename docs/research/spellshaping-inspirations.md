# Spellshaping inspirations from Pathfinder and D&D

**Status:** source research and native design proposals, checked October 3, 2026. The 25 examples below are inspiration, not accepted Vestige recipes, numerical tuning, added runtime semantics, or implemented modifiers. Published mechanics and proposed adaptations occupy separate columns. Native wand/equipment progression remains deferred.

The source mix is Pathfinder 1e metamagic; Pathfinder 2e **GM Core** property runes and specialty wands; **Treasure Vault (Remastered)** specialty wands; and D&D's **2024 Basic Rules** metamagic. Current remastered PF2 pages were used where available. Material meanings and support-block recipes require the separate pinned Iron material research; none of these tabletop examples establishes a Minecraft material mapping.

## Geometry, persistence and control: Pathfinder 1e

| # | Verified source | Published mechanic and cost | Proposed Vestige inspiration |
| --- | --- | --- | --- |
| 1 | [Selective Spell](https://aonprd.com/FeatDisplay.aspx?ItemName=Selective%20Spell), Advanced Player's Guide, p. 168 | Excludes a bounded selection of targets from an instantaneous area effect. Requires sufficient Spellcraft; uses a slot one level higher. | **Selective:** filter allies or specifically chosen entities out of an area plan. Broader area and selection are separate choices; an area boost must not silently grant ally protection. |
| 2 | [Lingering Spell](https://aonprd.com/FeatDisplay.aspx?ItemName=Lingering%20Spell), Advanced Player's Guide, p. 164 | An instantaneous area briefly remains and can affect new entrants; original occupants take no additional harm from this extension. Visible manifestations can obscure sight. Slot increase: one level. | **Lingering:** an explosion leaves a short-lived residue zone. Author its entry trigger, repeat policy and lifetime. Fire, frost, poison, healing and illumination could each leave a different residue. |
| 3 | [Persistent Spell](https://aonprd.com/FeatDisplay.aspx?ItemName=Persistent%20Spell), Advanced Player's Guide, p. 167 | A creature that successfully saves must save again; failure on the second test applies the spell. Only relevant to spells with saves. Slot increase: two levels. | **Tenacious:** eligible resisted effects make a bounded second attempt. This is pressure against resistance, not a duration increase. Minecraft has no universal tabletop saving-throw procedure, so eligibility requires an explicit native resistance mechanism. |
| 4 | [Delayed Spell](https://aonprd.com/FeatDisplay.aspx?ItemName=Delayed%20Spell), Pathfinder #116: Fangs of War, p. 67 | Stores a square-targeted effect for later manual activation. Location and casting choices are fixed; the effect remains identifiable. Slot increase: one level. | **Primed:** establish a fixed anchor, then release the spell through a recast or explicit trigger before expiry. **Timed** would be a separate native proposal using automatic release; the source feat is not simply a timer. |
| 5 | [Rime Spell](https://aonprd.com/FeatDisplay.aspx?ItemName=Rime%20Spell), Ultimate Magic, p. 155 | Cold damage causes temporary entanglement. Applies only to cold spells. Slot increase: one level. | **Riming:** actual ice damage adds an authored slowing, rooting or ice-binding plan. A recipe can require an ice-contributing ingredient and a compatible support block rather than modifying every damaging spell. |
| 6 | [Dazing Spell](https://aonprd.com/FeatDisplay.aspx?ItemName=Dazing%20Spell), Advanced Player's Guide, p. 157 | Damage can add dazing, with a save to avoid it. Damage is required; slot increase: three levels. | **Dazing:** a successful damaging outcome adds brief control. Its substantially larger source cost illustrates why disabling effects need deliberate native limits, not the same cost as a modest numerical boost. |

**Terminology warning:** PF1 Persistent Spell above means resistance pressure. PF2 persistent damage below means damage continuing after the hit. Neither automatically means that a Vestige spell's entire duration increases. [PF1 Persistent Spell](https://aonprd.com/FeatDisplay.aspx?ItemName=Persistent%20Spell), [PF2 Wounding](https://2e.aonprd.com/Equipment.aspx?ID=2854).

## Conditional behavior: Pathfinder 2e specialty wands

| # | Verified source | Published mechanic and restriction | Proposed Vestige inspiration |
| --- | --- | --- | --- |
| 7 | [Wand of Widening](https://2e.aonprd.com/Equipment.aspx?ID=3053), GM Core, p. 283, remaster | Expands qualifying instantaneous bursts, cones or lines and adds a casting action. Not applicable to every area or duration. | **Widening:** modify `vestige:area` where the eligible geometry explicitly consumes it. A longer casting time or greater mana payment can pay for coverage. Range and area remain independent. |
| 8 | [Wand of Contagious Frailty](https://2e.aonprd.com/Equipment.aspx?ID=2275), Treasure Vault (Remastered), p. 139 | Specifically casts Enfeeble. An enfeebled initial target spreads a weaker opportunity for the effect to nearby creatures. | **Contagious:** a successful condition application causes one bounded secondary spread. Debuffs, poison, fear or healing could use different explicit follow-up plans; uncontrolled recursive spreading is not implied. |
| 9 | [Wand of Dumbfounding Doom](https://2e.aonprd.com/Equipment.aspx?ID=2278), Treasure Vault (Remastered), p. 139 | Specifically casts Impending Doom. If it frightens the target, it also inflicts stupefaction for the spell's duration. | **Dumbfounding:** a qualifying fear or mental outcome also disrupts concentration, interrupts an action, or adds another authored mental effect. Match executable outcomes; the adjective does not make every spell a fear spell. |
| 10 | [Wand of Shrouded Step](https://2e.aonprd.com/Equipment.aspx?ID=2291), Treasure Vault (Remastered), p. 142 | Specifically casts Fleet Step and adds concealment while the user Strides during its duration. | **Shrouded:** a movement or teleportation result grants temporary concealment, leaves a decoy, or suppresses a visible trail. The condition “while moving” demonstrates a trigger-based utility extension. |

These wands have a daily activation allowance and risky overcharge, and their craft requirements determine compatible spells. This is evidence for explicit eligibility and conditional extensions, not a universal adjective attachment rule. Vestige's scroll format **Scroll of *[modifier]* [full spell title]** is a native presentation choice. [PF2 wand rules](https://2e.aonprd.com/Rules.aspx?ID=3218).

## Damage, status, defense and utility: Pathfinder 2e property runes

All nine entries below use current **GM Core** remastered pages. Their etched-item usage and prices are source restrictions and acquisition costs; a rune's restriction must not be mistaken for a spell's eligibility rule. Vestige would author that eligibility independently.

| # | Verified source | Published mechanic and restriction | Proposed Vestige inspiration |
| --- | --- | --- | --- |
| 11 | [Wounding](https://2e.aonprd.com/Equipment.aspx?ID=2854), p. 239 | Adds persistent bleeding when a piercing or slashing melee weapon hits. | **Bleeding:** a qualifying hit applies an explicitly timed bleed effect to suitable living targets. Add an authored outcome plan; increasing `vestige:blood` alone does not create bleeding. |
| 12 | [Frost](https://2e.aonprd.com/Equipment.aspx?ID=2839), p. 237 | Adds cold damage; critical hits can also slow the target, subject to a save. | **Chilling:** adds an ice contribution plus a conditional slowing effect. PF2's slowed condition reduces actions, so Minecraft movement slowness would be a native adaptation. |
| 13 | [Shock](https://2e.aonprd.com/Equipment.aspx?ID=2847), p. 238 | Adds electricity damage; on a critical hit, it arcs to selected nearby creatures. | **Arcing:** a qualified impact jumps to a bounded set of additional targets. This can shape an elemental projectile or a suitable damaging beam without requiring a separate spell definition per base spell. |
| 14 | [Corrosive](https://2e.aonprd.com/Equipment.aspx?ID=2834), p. 237 | Adds acid damage; critical hits damage the target's armor or raised shield. | **Corroding:** eligible acid or metal-affecting outcomes temporarily weaken armor or a barrier. Item durability destruction and a temporary combat debuff are distinct designs and need explicit selection. |
| 15 | [Vitalizing](https://2e.aonprd.com/Equipment.aspx?ID=2852), p. 239 | Adds ongoing vitality damage specifically against undead; critical hits also enfeeble them. | **Consecrating:** a qualifying life or holy contribution adds an undead-specific outcome. Target predicates make it a specialization rather than an unconditional increase against every creature. |
| 16 | [Ghost Touch](https://2e.aonprd.com/Equipment.aspx?ID=2840), p. 238 | Enables physical interaction with incorporeal creatures and exploits their relevant vulnerability. | **Ghost-touching:** an eligible manifestation can affect a specified spirit or incorporeal entity capability. Do not reinterpret this as permission to pass through every wall or bypass every resistance. |
| 17 | [Spell Reservoir](https://2e.aonprd.com/Equipment.aspx?ID=2849), p. 239 | Stores one eligible low-rank spell in a melee weapon. An activated Strike expends it; it affects the target if that Strike hits. Charging takes time, and safe release is available. | **Imprinted:** temporarily bind a composed spell outcome to one qualifying future hit, contact or event. Consumption, missed-trigger behavior, expiry and cleanup belong to the binding plan. A protective spell could instead trigger once when its recipient is struck. |
| 18 | [Fortification](https://2e.aonprd.com/Equipment.aspx?ID=2789), p. 226 | Medium or heavy armor can sometimes convert a critical hit to a normal hit. The rune also makes the armor heavier and harder to handle. | **Fortifying:** an eligible shield or ward gains a bounded reaction against a large hit. A longer cast, shorter duration, lower mobility or additional resource payment can accompany the benefit. |
| 19 | [Shadow](https://2e.aonprd.com/Equipment.aspx?ID=2793), p. 227 | Light or medium armor grants a Stealth bonus. It does not independently grant invisibility. | **Veiled:** a utility, illusion or movement spell can gain an authored stealth aid. Concealment, invisibility, muffled sound and detection reduction should be separate explicit effects, not inferred from `vestige:shadow`. |

**Edition detail:** [legacy Spell-Storing](https://2e.aonprd.com/Equipment.aspx?ID=305&NoRedirect=1) releases its spell after a previous hit; current Spell Reservoir expends it as part of an activated Strike. [Legacy Disrupting](https://2e.aonprd.com/Equipment.aspx?ID=294&NoRedirect=1) adds immediate positive damage, whereas current Vitalizing adds persistent vitality damage. This note uses the current entries and preserves those behavioral differences.

## Six concise D&D metamagic examples

Source: Wizards' [2024 Basic Rules: Metamagic Options](https://www.dndbeyond.com/sources/dnd/br-2024/character-classes#MetamagicOptions). SP means Sorcery Points. Native candidates are proposals.

| # | Option | Published mechanic and cost | Native candidate |
| --- | --- | --- | --- |
| 20 | Distant Spell | Extends ranged or touch casting; 1 SP. | **Reaching:** trait-scaled travel or selection. |
| 21 | Extended Spell | Extends qualifying duration; supports concentration; 1 SP. | **Enduring:** duration-consuming expression. |
| 22 | Quickened Spell | Changes action casting to a Bonus Action; restricts other leveled casts that turn; 2 SP. | **Quickened:** shorter windup, higher payment. |
| 23 | Subtle Spell | Removes casting components except consumed or specifically priced materials; 1 SP. | **Subtle:** explicit sound/visual suppression. |
| 24 | Transmuted Spell | Changes damage among a specified elemental/poison set; 1 SP. | **Transmuted:** explicit damage-type replacement. |
| 25 | Twinned Spell | Advances eligible spells whose upcasting grants another creature target; 1 SP. | **Forked:** bounded target-count expression. |

2024 Twinned is not arbitrary duplication. Subtle preserves costly and consumed materials. [2024 Metamagic](https://www.dndbeyond.com/sources/dnd/br-2024/character-classes#MetamagicOptions).

## Native composition constraints

These are constraints supplied for this Vestige design task, not claims about tabletop rules:

1. **Every numerical augmentation uses existing `TraitModifier` operations, `ADD` or `MULTIPLY`, and an effect expression that explicitly consumes the affected trait.** Reaching uses `vestige:range`; Widening uses `vestige:area`. Enduring needs an eligible duration expression that explicitly reads an appropriate trait. A descriptor, modifier name or theme rating must not silently change unrelated values. There is no separate Spellshaping strength, rank, quality or intensity system.
2. **Added behavior is explicit composition.** Bleeding appends or binds an authored status plan; Arcing composes bounded retargeting; Lingering composes a temporary zone; Contagious composes a conditioned secondary application. Their numerical expressions still consume the resolved traits. Capability checks inspect executable plans, including continuations and bindings.
3. **Costs stay fully typed.** Health, hunger, mana, time, material consumption and cooldown/recovery are distinct possible payments or constraints. A recipe declares the exact types it uses. A health payment is not a smaller mana pool; longer windup is not the same as a delayed effect after release. All proposed magnitudes remain untuned.
4. **A compound material pairing can satisfy one augment recipe and produce one modifier name.** Several pedestal-local item/support-block matches can jointly establish Arcing or Lingering without awarding several unrelated adjectives. Those requirements form one explicit conjunction. The consumed ingredients, reusable support blocks, trait changes, appended plans and costs remain visible parts of that one recipe.
5. **Do not add implicit semantic traits.** `vestige:volatile` retains its exceptional engine semantics. Words such as bleeding, projectile, shield, contagious, subtle and triggered describe authored transformations or derived capabilities. They are not new globally interpreted traits.

## Strongest proposals to explore first

These are design inferences, with the inspirations linked above; they do not claim existing implementation.

- **Reaching / Widening / Enduring** teach three independent numerical choices: where a spell can act, how much space it covers and how long a compatible effect lasts. Preserve that distinction in the apparatus clues and item tooltip.
- **Bleeding / Chilling / Dazing** teach a hit followed by a bounded condition. Each needs a compatible target and outcome, and the disabling variants merit a more substantial payment or shorter application.
- **Lingering** creates tactical terrain without replacing the original impact. A player can choose an explosion for immediate use or residue to discourage movement through a doorway.
- **Arcing / Contagious** teach two different propagation triggers: an impact jumps, whereas a successfully applied condition spreads. Both need maximum targets, a generation limit and clear handling of repeat hits.
- **Primed / Imprinted** reward preparation. One stores an anchored cast for release; the other binds an outcome to a later qualifying event. Both require expiry, cancellation and cleanup so the world does not accumulate abandoned magic.
- **Shrouded / Veiled / Fortifying** make movement, utility and defensive spells worth shaping. They widen the system beyond additional damage.

A worthwhile native tradeoff could pair a larger area with longer casting, a quicker cast with greater mana or health payment, a lingering zone with shorter direct-control duration, or a specialized undead effect with narrower eligible targets. These are proposed combinations for playtesting, not inherited tabletop numbers or approved balance policy.
