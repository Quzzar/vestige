# Mapping Iron's Spells 'n Spellbooks into Vestige

> September 30 scope update: the project owner requested native conversion of the entire pinned Iron catalog and removal of inherited gameplay. The earlier integration-only scope below is historical. See [the conversion ledger](../design/iron-spell-conversions.md) and the current runtime/status documents.

Research checked against Iron's official documentation and official `1.21` source on 2026-09-14. Source links are pinned to commit [`e4056af`](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252), whose [`gradle.properties`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/gradle.properties#L6-L21) targets Minecraft 1.21.1 and mod version 3.16.3.

## Verdict

Iron's maps **cleanly as an interoperating spell provider**, but not as an automatically converted Vestige effect graph.

Vestige's chosen integration scope is the first case. Iron remains authoritative for execution, and ordinary support operates at the provider, school, and event-family level. Each Iron school is projected directly into Vestige's vocabulary; the foreign school ID remains metadata rather than becoming a second trait. Projections may be broader than exact taxonomic equivalence. The per-spell semantic examples later in this report are useful only for optional datapack refinements or future native recreations; they are not a required compatibility catalog.

- **Identity and classification: clean.** Vestige can preserve the exact Iron spell ID and school ID, then attach its own namespaced trait words. Every registered Iron spell fits the proposed vocabulary.
- **Casting lifecycle and outcome events: clean.** Iron exposes pre-cast, cast, damage, healing, summon, teleport, counterspell, mana, cooldown, and spell-level events. These can become namespaced Vestige triggers and context values without replacing Iron's implementation. ([official event package](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/events))
- **Mana and ordinary cast metadata: clean.** Spell level, mana cost, cast type, cast source, cooldown, and rarity have direct typed representations. They should remain metadata or costs, not traits. [`AbstractSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/spells/AbstractSpell.java#L57-L160) owns these values.
- **Numerical scaling: requires an explicit per-spell map.** Iron has one general/per-school power product, but each spell decides what that number means. The same value can become damage, distance, duration, radius, count, healing, or several at once.
- **Full behavioral conversion: partially clean.** Most spells reduce to reusable targeting, damage, healing, status, movement, summoning, persistent-area, and world-interaction operations. A meaningful long tail contains recasts, delayed consequences, event-driven follow-ups, custom entities, portals, inventory access, aggro manipulation, weapon scaling, or special counterspell behavior. Those need composite definitions or small purpose-built effect types.

The selected compatibility level is therefore:

```text
Iron remains authoritative for executing an Iron spell
  -> Vestige preserves its exact upstream identity
  -> exact school remains adapter metadata
  -> adapter projects the school into one Vestige trait
  -> Iron lifecycle/outcome events enter Vestige as triggers
  -> cross-mod bonuses modify supported outcomes
```

Recreating an Iron spell as a native Vestige spell is possible, but it is content-porting work: it cannot be inferred losslessly from `AbstractSpell` at runtime.

## Three layers of the map

| Iron datum | Vestige representation | Cleanliness |
|---|---|---|
| Spell resource ID | Lossless adapter metadata | Exact |
| Iron school resource ID | Lossless adapter metadata | Exact |
| School-to-semantic-trait projection | Small automatic table below | Complete and intentionally coarse |
| Classical school traits | Iron Evocation projects to `evocation`; other classical mappings are optional | Policy rather than exact taxonomy |
| Spell level and rarity | Typed adapter metadata | Exact |
| Mana cost | `ManaCost` or observed Iron mana event | Exact |
| Cast type and source | Trigger context / cast metadata | Exact |
| Cooldown and cast time | Typed cast properties / event context | Exact |
| General + Iron-school spell power | Input to a per-spell scaling projection | Partial |
| Reach, radius, wall length, chain distance | `range` and `area` ratings plus effect-local bases/coefficients | Per-spell |
| Concrete behavior | Vestige targeting/shape/effect graph or Iron-owned execution | Per-spell |

Iron registers concrete `AbstractSpell` instances, usually one custom Java subclass per spell; it does not expose a data-driven effect graph to inspect. The registry and base class make this boundary explicit. ([`SpellRegistry`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/SpellRegistry.java#L27-L64), [`AbstractSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/spells/AbstractSpell.java#L57-L160))

## Automatic school mappings

The exact upstream school remains available as metadata such as `foreign_school = irons_spellbooks:fire`. The adapter projects each school into one stable Vestige trait:

| Iron school | Vestige trait |
|---|---|
| Fire | `fire` |
| Ice | `ice` |
| Lightning | `lightning` |
| Holy | `holy` |
| Blood | `blood` |
| Ender | `ender` |
| Evocation | `evocation` |
| Nature | `life` |
| Eldritch | `void` |

The official school guide itself describes Iron's Evocation as trickery and conjuration with offensive, defensive, and utility spells. ([official Schools guide](https://iron.wiki/schools/)) In source it contains Invisibility, Shield, Slow, Spectral Hammer, Summon Horse, Summon Vex, Throw, and Wololo alongside direct attacks. ([official Evocation sources](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/evocation)) The projection deliberately favors interoperability: an `evocation` bonus recognizes the whole Iron school even though the label is taxonomically broader than Vestige's classical definition.

### Consolidated elemental vocabulary

The audit originally considered separate `ice`/`cold` and `lightning`/`electricity` concepts. Vestige subsequently chose the smaller vocabulary:

- Iron Ice projects to `ice`, covering both frozen matter and cold effects.
- Iron Lightning projects to `lightning`, covering both bolts and electrical effects.

This loses a small amount of descriptive precision but prevents two near-synonymous trait pairs from fragmenting compatibility rules.

## Representative optional native mappings from all nine schools

These are examples for optional datapack refinement or native spell recreation, not requirements of the Iron integration. `amplify`, `range`, and `area` are omitted from the trait column because their ratings must be derived from each spell's actual formula and selected effect nodes.

| Iron spell | Iron school | Suggested Vestige traits | Classical school trait(s) | Main effect composition |
|---|---|---|---|---|
| Blood Step | Blood | `blood`, `space`, `shadow`, `teleportation` | `conjuration`, `illusion` | Short teleport, then true invisibility |
| Raise Dead | Blood | `blood`, `death`, `spirit` | `necromancy`, `conjuration` | Summon several equipped undead followers |
| Heartstop | Blood | `blood`, `life`, `death` | `necromancy`, `abjuration` | Become invulnerable, accumulate prevented damage, repay half on expiry |
| Teleport | Ender | `space`, `teleportation` | `conjuration` | Collision-safe aimed teleport |
| Counterspell | Ender | `space` | `abjuration` | Interrupt casting; dispel or banish supported effects, barriers, and summons |
| Black Hole | Ender | `void`, `space`, `motion` | `conjuration`, `evocation` | Persistent area pull plus repeated damage |
| Echoing Strikes | Ender | `force` | `conjuration`, `evocation` | Temporary listener: qualifying mundane attacks create follow-up sword or arrow attacks |
| Invisibility | Evocation | — | `illusion` | Timed true invisibility; breaks on damage dealt and clears mob aggro |
| Shield | Evocation | `force` | `abjuration` | Summon a stationary collision/projectile barrier with health |
| Summon Vex | Evocation | `spirit` | `conjuration` | Summon owned combat followers |
| Slow | Evocation | `motion` | `transmutation` | Timed movement, attack, mining, and casting-speed penalty |
| Fireball | Fire | `fire` | `evocation` | Projectile, impact damage/explosion, ignition |
| Burning Dash | Fire | `fire`, `motion` | `evocation` | Caster dash plus contact damage/burning |
| Wall of Fire | Fire | `fire` | `evocation`, `conjuration` | Recast anchor placement, then persistent wall that burns and blocks projectiles |
| Heal | Holy | `holy`, `life` | `evocation` | Immediate self-healing |
| Cleanse | Holy | `holy`, `life` | `abjuration` | Remove harmful statuses from self and nearby allies |
| Angel Wings | Holy | `holy`, `light`, `motion` | `transmutation` | Timed elytra-like flight form |
| Guiding Bolt | Holy | `holy`, `light` | `evocation`, `divination` | Projectile damage plus status that redirects nearby projectiles toward the target |
| Frost Step | Ice | `ice`, `space`, `teleportation` | `conjuration`, `illusion` | Teleport, leave an aggro decoy, shatter decoy into damaging icicles |
| Ice Tomb | Ice | `ice`, `life` | `abjuration`, `conjuration` | Mount caster in a healing barrier that absorbs one hit/counterspell |
| Blizzard | Ice | `ice`, `air`, `motion` | `evocation`, `conjuration` | Moving persistent zone, freezing, inward pull |
| Ascension | Lightning | `lightning`, `air`, `motion` | `evocation` | Lightning strike, knockback, launch caster, temporary reduced gravity |
| Charge | Lightning | `lightning`, `motion` | `transmutation` | Timed speed, attack-damage, and school-power buffs |
| Thunderstorm | Lightning | `lightning`, `air` | `conjuration`, `evocation` | Timed caster-centered area that periodically strikes visible targets |
| Aspect of the Spider | Nature | `life`, `poison` | `transmutation` | Timed conditional bonus damage against poisoned targets |
| Earthquake | Nature | `earth`, `stone`, `motion` | `evocation`, `transmutation` | Ground area damage and slow |
| Root | Nature | `plant`, `earth`, `motion` | `conjuration`, `transmutation` | Summon damageable roots that tether a target |
| Touch Dig | Nature | `earth`, `stone` | `transmutation` | Validate and break one block, retaining held-tool drop behavior |
| Planar Sight | Eldritch | `space`, `mind` | `divination` | Timed perception of creatures through solid matter |
| Pocket Dimension | Eldritch | `space`, `void`, `teleportation` | `conjuration` | Enter or leave a private dimension with access rules |
| Telekinesis | Eldritch | `mind`, `force`, `motion` | `evocation` | Continuous target grip and forced movement, allowing kinetic impact damage |
| Sonic Boom | Eldritch | `sonic`, `force` | `evocation` | Piercing beam through solid matter with damage along its path |

Descriptions above were checked against the official [complete spell guide](https://iron.wiki/spells/) and the corresponding first-party spell classes under the nine [official spell source folders](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells).

The classical-school assignments are Vestige design decisions, not claims about Iron's own taxonomy. They should be stored in a reviewed data map so a pack can override them without changing either mod.

## Complete default-enabled built-in catalog

The `1.21` registry contains **111 registered built-in spells**, but Cloud of Regeneration is deprecated and default-disabled. The default-enabled catalog therefore contains **110 spells**: 10 Blood, 16 Ender, 17 Evocation, 13 Fire, 12 Holy, 12 Ice, 10 Lightning, 13 Nature, and 7 Eldritch. Iron's runtime `enabled` configuration can change that set further. ([registry and filter](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/SpellRegistry.java#L48-L64), [Cloud of Regeneration configuration](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/holy/CloudOfRegenerationSpell.java#L28-L47), [`setDeprecated`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/config/DefaultConfig.java#L8-L31))

| Iron school | Registered spells |
|---|---|
| Blood (10) | Acupuncture; Blood Needles; Blood Slash; Blood Step; Devour; Heartstop; Raise Dead; Ray of Siphoning; Sacrifice; Wither Skull |
| Ender (16) | Arcane Shackle; Black Hole; Counterspell; Dragon's Breath; Echoing Strikes; Evasion; Gravity Fissure; Magic Arrow; Magic Missile; Portal; Recall; Shadow Slash; Starfall; Summon Ender Chest; Summon Swords; Teleport |
| Evocation (17) | Arrow Volley; Chain Creeper; Fang Strike; Fang Swirl; Fang Ward; Firecracker; Gust; Invisibility; Lob Creeper; Scapegoat; Shield; Slow; Spectral Hammer; Summon Horse; Summon Vex; Throw; Wololo |
| Fire (13) | Blaze Storm; Burning Dash; Fire Arrow; Fire Breath; Fireball; Firebolt; Flaming Barrage; Flaming Strike; Heat Surge; Magma Bomb; Raise Hell; Scorch; Wall of Fire |
| Holy (12) | Angel Wings; Blessing of Life; Cleanse; Divine Smite; Fortify; Greater Heal; Guiding Bolt; Haste; Heal; Healing Circle; Sunbeam; Wisp |
| Ice (12) | Blizzard; Cone of Cold; Frost Step; Frostbite; Frostwave; Ice Block; Ice Spikes; Ice Tomb; Icicle; Ray of Frost; Snowball; Summon Polar Bear |
| Lightning (10) | Ascension; Ball Lightning; Chain Lightning; Charge; Electrocute; Lightning Bolt; Lightning Lance; Shockwave; Thunderstorm; Volt Strike |
| Nature (13) | Acid Spit (`AcidOrbSpell` in source); Aspect of the Spider; Blight; Earthquake; Firefly Swarm; Gluttony; Oakskin; Poison Arrow; Poison Splash; Poison Spray (`PoisonBreathSpell` in source); Root; Stomp; Touch Dig |
| Eldritch (7) | Abyssal Shroud; Eldritch Blast; Planar Sight; Pocket Dimension; Sculk Tentacles; Sonic Boom; Telekinesis |

The remaining source-only cases are:

- Cloud of Regeneration is registered but default-disabled and absent from the official current spell page.
- Soulfire Ray and Thunderstep have implementations, but their registry declarations are commented out.
- A stale commented Frostbite declaration remains, but a later active declaration registers Frostbite exactly once.
- `NoneSpell` is an unregistered internal fallback sentinel.

([complete official registry](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/SpellRegistry.java#L78-L203), [Soulfire Ray](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/fire/SoulfireRaySpell.java), [Thunderstep](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/lightning/ThunderStepSpell.java))

## Effect and targeting families Vestige needs

The 111 spells do not require 111 engine primitives. They require a compact set of ordinary operations plus composition and state.

### Targeting and delivery

1. Self, aimed entity, aimed block/position, and caster-centered targeting.
2. Projectile with collision, homing, piercing, gravity, bounce, and on-impact child effects.
3. Ray/beam, cone, ground line, radial burst/ring, chain to nearby targets, and melee arc.
4. Persistent area, moving area, storm, wall/polyline between recast anchors, and attached/following area.
5. Relationship filters: self, ally, hostile, owner, owned summon, and line-of-sight.

These should be delivery/shape components rather than authored semantic traits. Iron's official spell descriptions show all of these forms across its catalog. ([official Spells guide](https://iron.wiki/spells/))

### Core effects

1. Deal damage, heal, grant temporary health, and siphon a fraction of damage as healing.
2. Apply, remove, and query status effects; freeze/chill and ignite are specialized status accumulators.
3. Add velocity, pull, knock back, launch, dash, reduce gravity, and tether.
4. Teleport safely, teleport to spawn, maintain linked portals, and transfer between dimensions.
5. Summon/despawn an owned creature or spell entity; configure count, health, damage, equipment, and allegiance.
6. Create a barrier or collision field with health and projectile behavior.
7. Break, inspect, or temporarily replace blocks while retaining tool/drop semantics.
8. Modify entity attributes and mana; convert food consumption into mana.
9. Interrupt a cast, dispel a status/field, banish a summon, and absorb a counterspell.
10. Change aggro/targeting, create a decoy, or redirect projectiles.
11. Open or expose inventory and private-space behavior.

### Composition/state primitives

1. Delay or schedule an effect; repeat every interval while an area or cast remains active.
2. Accumulate prevented damage and release a formula on expiry.
3. Install a temporary listener for on-attack, on-damaged, on-kill, on-target-death, or on-food-consumed.
4. Recast with stored anchors or stored targets.
5. Spawn child effects on impact, death, shatter, expiry, or a target's death.
6. Scale from held-weapon damage/enchantments or target/summon state.

These state primitives are what separate faithful conversion from a superficial `damage + projectile` translation. Heartstop, Echoing Strikes, Evasion, Frostbite, Guiding Bolt, Wall of Fire, Chain Creeper, Firefly Swarm, Gluttony, Portal, and Sacrifice are representative examples from the official catalog. ([official Spells guide](https://iron.wiki/spells/))

## Scaling audit: `amplify`, `range`, and `area`

The three axes are sufficient as Vestige's general scaling vocabulary, but they cannot be populated from Iron's school or level with one universal formula.

Iron calculates base spell power as:

```text
(baseSpellPower + spellPowerPerLevel * (level - 1))
  * general spell-power attribute
  * current Iron-school power attribute
  * per-spell config multiplier
```

([`AbstractSpell.getSpellPower`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/spells/AbstractSpell.java#L196-L222))

Individual classes then reuse that one scalar differently:

- Fireball uses it for **both damage and explosion radius**. ([`FireballSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/fire/FireballSpell.java#L76-L92))
- Teleport uses it for **maximum distance**, with an additional soft-capped entity-power multiplier. ([`TeleportSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/ender/TeleportSpell.java#L166-L168))
- Invisibility uses it for **duration**. ([`InvisibilitySpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/evocation/InvisibilitySpell.java#L82-L84))
- Healing Circle uses it for healing, but keeps its radius and duration constant. ([`HealingCircleSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/holy/HealingCircleSpell.java#L121-L133))

Consequences:

- Do not set `amplify = Iron spell power` and assume fidelity. That would incorrectly lose Fireball's area growth, Teleport's range growth, and Invisibility's duration growth.
- For a **native Vestige recreation**, author effect-local formulas that read the appropriate subset of `amplify`, `range`, and `area`. A spell may use `amplify` for damage, `area` for explosion radius, and fixed or node-local constants for duration.
- For a **live Iron cast**, let Iron compute its behavior. Vestige can observe and modify damage/heal/summon outcomes through public events, but there is no universal public event for changing every spell's reach or radius. `range` and `area` bonuses therefore require an explicit supported-spell adapter, an upstream extension point, or acceptance that those bonuses do not alter Iron-owned casts.
- A single spell can have multiple distance concepts: placement reach, projectile lifetime, explosion radius, chain-jump distance, wall length, and persistent-zone radius. `range` and `area` should be general inputs with per-node coefficients, not the sole storage location for exact block measurements.

## Mapping risks

| Risk | Severity | Mitigation |
|---|---|---|
| Mistaking the Evocation projection for exact taxonomy | Medium | Preserve the upstream school as metadata and document that bonuses intentionally recognize the whole Iron school |
| Treating Iron spell power as only `amplify` | High for fidelity | Per-spell scaling projection into `amplify`, `range`, `area`, and fixed/local parameters |
| Trying to introspect an effect graph that Iron does not have | High | Execute Iron spells in Iron; hand-author native recreations |
| Treating Ender/Nature/Eldritch projections as exact descriptions of every spell | Medium | Document them as deliberately coarse compatibility policy and retain exact school metadata |
| Expecting `range`/`area` to alter arbitrary live Iron spells | Medium/high | Start read-only; add explicit supported hooks only where the public API permits |
| Losing reactive/recast behavior in conversion | Medium/high | Include temporary listeners, stored cast state, child effects, and scheduling as first-class composition tools |
| Configuration drift | Medium | Build the projection by resource ID, tolerate unknown spells, and query the runtime registry/enabled flag |
| Addon spells using the same Iron schools | Medium | Apply the documented school projection automatically; allow addon-provided or datapack trait maps only as optional refinements |

## Trait gaps revealed by Iron's catalog

### Strong additions

- **`teleportation`** is strongly justified. Blood Step, Frost Step, Teleport, Recall, Portal, Evasion, and Pocket Dimension need a shared semantic query independent of school or element.
- **`motion`** is strongly justified. Gust, Throw, Telekinesis, Black Hole, Gravity Fissure, Burning Dash, Ascension, Haste, Slow, Root, Stomp, and Volt Strike all manipulate motion without sharing one element or school.

### Useful, but not required by Iron alone

- **`polymorph`** remains valuable for Vestige and dragon integration, but Iron's registered catalog does not contain a clear full creature-form transformation. Aspect of the Spider and Oakskin are attribute/status changes; Wololo changes sheep color. None requires `polymorph` for a faithful Iron map.
- **`draconic`** is reasonable for Dragon's Breath and external dragon integrations, though Fire Breath should not receive it merely because its delivery is breath-shaped.

### No additional required core trait

Iron does not force Vestige to add `damage`, `healing`, `shield`, `projectile`, `summon`, `aura`, `visual`, `auditory`, or `subtle`. Those facts are available from the resolved effect/delivery graph or upstream event. Its other apparent vocabulary gaps are already covered compositionally:

- gravity = `space` + `motion` and sometimes `force`;
- undead magic = `death` + `spirit` + `necromancy`;
- arcane force = `force` plus the relevant classical school;
- defense = `abjuration` plus the spell's substance/essence traits;
- storms = air plus the relevant fire, ice, or lightning traits.

If Vestige later needs equipment or conditions specifically keyed to undead creatures, `undead` can be added as an ordinary namespaced trait then. It is not necessary to classify Raise Dead itself.

## Recommended implementation order

1. Create an optional Iron adapter that exposes exact spell ID, exact school ID, level, cast type/source, and relevant event context.
2. Retain the exact Iron school ID as metadata and project it through the nine-entry Vestige map. Unknown and addon spells then inherit ordinary compatibility without a spell catalog.
3. Make Vestige react to Iron lifecycle/outcome events; do not attempt native conversion yet.
4. Apply Vestige bonuses only through supported generic outcomes or Iron attributes. Treat `range` and `area` as descriptive unless Iron exposes a suitable public hook for the feature.
5. Keep per-spell aliases and native recreations optional and data-driven when a pack deliberately wants finer semantics.

This produces meaningful compatibility immediately without making Vestige depend on Iron's internal spell entities or promising a lossless automatic conversion that its API cannot supply.
