# Vestige trait vocabulary for mod interoperability

Research checked against first-party documentation and source on 2026-09-14. Iron's observations use the official `1.21` branch at commit [`e4056af`](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252), which targets Minecraft 1.21.1 and NeoForge 21.1.200. The dragon-mod section intentionally identifies candidates rather than assuming which mod is meant.

## Recommendation

Vestige should keep three distinct kinds of information:

1. **Semantic traits** say what a spell *is about* and can participate in weighted mechanics. The elemental/substance and essence/metaphysical vocabulary belongs here, along with the scaling traits `vestige:amplify`, `vestige:range`, and `vestige:area`, the classical school traits, and a small number of cross-mod concepts such as `vestige:draconic` and `vestige:motion`.
2. **Derived capabilities** say what the resolved spell graph can *do*: damage, heal, teleport, summon, alter blocks, apply a status, and so on. They are useful for validation and queries, but should be derived from effects rather than hand-authored as weighted traits.
3. **Adapter metadata** preserves exact foreign identity: upstream spell ID, school ID, cast type, cast source, damage type, rarity/level, entity breed/type, and Create machine state. These values should remain namespaced and lossless rather than being forced into Vestige's core trait vocabulary.

This gives a safe rule for interoperating with foreign casts:

```text
foreign identity is preserved exactly
    + exact foreign school remains adapter metadata
    + adapter projects the school directly into Vestige's trait vocabulary
    + lifecycle and outcome events enter Vestige's trigger context
```

This support does not require converting the foreign spell or maintaining a per-spell map. A native content port is a separate activity and may use explicit per-spell semantics and effects.

### Core trait additions justified by interoperability

| Trait(s) | Recommendation | Reason |
|---|---|---|
| `amplify`, `range`, `area` | Add as the three general scaling traits | Effects opt into their conventional potency, reach, and spatial-extent meanings. Iron does not offer universal range or area modification hooks, so those traits may be descriptive on foreign casts. |
| `abjuration`, `conjuration`, `divination`, `enchantment`, `evocation`, `illusion`, `necromancy`, `transmutation` | Add as the classical school traits | They are recognizable semantic classifications and may coexist with elements, substances, and metaphysical traits. They should not replace upstream school IDs. |
| `draconic` | Add | It gives spells, gear, creatures, paths, and integrations a shared cross-mod query surface without encoding a particular dragon mod's breed system into Vestige core. |
| `motion` | Consider adding | This is the only broadly useful semantic gap surfaced by Create: magic may govern motion independent of whether Create is installed. Exact rotation speed, direction, stress, and contraption state are adapter data, not traits. |

Do **not** add `instant`, `long`, `continuous`, `spellbook`, `scroll`, `sword`, `rpm`, `stress`, `contraption`, or a foreign entity breed as core traits. Those are execution or adapter facts.

## Iron's Spells 'n Spellbooks

### Exact upstream schools

Iron's registers nine schools in its own custom registry: `fire`, `ice`, `lightning`, `holy`, `ender`, `blood`, `evocation`, `nature`, and `eldritch`. Each school owns a focus item tag, a power attribute, a resistance attribute, a cast sound, and a school damage type. ([`SchoolRegistry`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/SchoolRegistry.java#L49-L148), [`AttributeRegistry`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/AttributeRegistry.java#L25-L56))

The official school guide describes their themes as follows: fire emphasizes damage and damage-over-time; ice damage and crowd control; lightning concentrated damage; holy support/healing/buffs; ender arcane utility and mobility; blood necromancy, wither, damage, and self-buffing; nature debuffs plus damage; and eldritch rare ancient magic. Iron's **Evocation** is explicitly a broad school of trickery and conjuration spanning offense, defense, and utility. ([official Schools guide](https://iron.wiki/schools/))

That last point prevents a one-to-one name mapping. For example, Iron's Evocation folder contains `InvisibilitySpell`, `ShieldSpell`, `SlowSpell`, `SummonHorseSpell`, and `SummonVexSpell`, which Vestige would ordinarily classify as illusion, abjuration, transmutation, and conjuration rather than all as classical evocation. ([official Evocation spell sources](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/evocation))

Therefore ordinary integration should retain `irons_spellbooks:evocation` as adapter metadata and project it to `vestige:evocation` as an intentional compatibility policy. The projection is not a claim of perfect taxonomic equivalence. More generally, each exact Iron school maps directly into the closest Vestige trait, and finer semantics remain optional datapack refinements. A future native content port may still assign classical schools per spell.

### Effects and capabilities actually represented by Iron's spells

Iron's current registry contains 111 registered spells across the nine schools, of which 110 are default-enabled; Cloud of Regeneration is deprecated and default-disabled. It expresses behavior in custom spell classes rather than in a reusable effect-node catalog. Reviewing those first-party spell and supporting-effect classes reveals these integration-relevant capability families: ([spell registry](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/SpellRegistry.java#L89-L219), [Cloud of Regeneration](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/spells/holy/CloudOfRegenerationSpell.java#L28-L47), [mob-effect sources](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/effect), [spell-entity sources](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/entity/spells))

- Damage delivered directly or through projectiles, rays/beams, cones/breaths, chains, bursts, storms, persistent areas, and walls.
- Fire, freeze/chill, poison, wither/blight, lifesteal, delayed damage, and other post-hit behavior.
- Healing, regeneration, cleansing, shields, fortification, haste, invisibility, evasion, and flight.
- Slowing, rooting, shackling, throwing, telekinesis, counterspelling, and forced targeting or allegiance changes.
- Dashes, ascent, teleportation, recall, portals, and a pocket dimension.
- Summoned creatures, weapons, projectiles, barriers, and utility objects such as an Ender Chest.
- Block/world interaction including digging, earthquakes, ice containment, and persistent fields.

These are strong candidates for Vestige **effect types** and automatically derived capability flags. They are not evidence for adding `damage`, `heal`, `projectile`, `summon`, or `teleport` as authored weighted traits.

### Cast and event interoperability

Iron's cast types are exactly `NONE`, `INSTANT`, `LONG`, and `CONTINUOUS`. Its cast sources are `SPELLBOOK`, `SCROLL`, `SWORD`, `MOB`, `COMMAND`, and `NONE`; source also determines whether Iron consumes mana or respects cooldown. ([`CastType`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/spells/CastType.java), [`CastSource`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/spells/CastSource.java)) These belong in trigger context or adapter metadata, not traits.

The current public event surface supports concrete adapter triggers and interception points:

- `SpellPreCastEvent` is cancellable and exposes player, spell ID, spell level, school, and cast source; `SpellOnCastEvent` exposes the same cast identity and permits changing spell level and mana cost. ([pre-cast](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/events/SpellPreCastEvent.java), [on-cast](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/events/SpellOnCastEvent.java))
- `SpellDamageEvent`, `SpellHealEvent`, `SpellSummonEvent`, `SetSummonOwnerEvent`, and `SpellTeleportEvent` expose outcome-specific caster, target, spell, school, level, or amount data. Damage can be cancelled or changed, and the summon entity can be replaced. ([event directory](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/events))
- `CounterSpellEvent`, `ChangeManaEvent`, `SpellCooldownAddedEvent.Pre/Post`, `ModifySpellLevelEvent`, and `InscribeSpellEvent` cover counterspelling, resource changes, cooldowns, level modification, and spell inscription. ([event directory](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/events))

A Vestige adapter should translate those to namespaced triggers such as `irons_spellbooks:pre_cast`, `on_cast`, `spell_damage`, `spell_heal`, `spell_summon`, `summon_owner_set`, `spell_teleport`, `counterspell`, `mana_changed`, `cooldown_pre`, `cooldown_post`, `level_queried`, and `spell_inscribed`. Their payload fields become reusable condition inputs.

### Damage types and tags

Iron's defines one damage type for each of its nine schools plus `blood_cauldron`, `heartstop`, `dragon_breath_pool`, `fire_field`, and `poison_cloud`. Its data generator makes school-specific damage tags and nests all nine school tags into NeoForge's common `Tags.DamageTypes.IS_MAGIC`; it also defines `bypass_evasion` and `long_cast_ignore` behavior tags. ([damage types](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/damage/ISSDamageTypes.java#L13-L43), [damage-tag generator](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/datagen/DamageTypeTagGenerator.java#L29-L94))

Vestige conditions should query the common magic tag, Iron's school tags, and exact damage-type key. If the source is Iron's `SpellDamageSource`, the adapter can also recover the concrete spell and its configured lifesteal, fire ticks, freeze ticks, and invulnerability-frame override. ([`SpellDamageSource`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/damage/SpellDamageSource.java#L13-L100)) Those values are effect/damage context, not traits.

### `amplify` bridge

Iron's exposes max mana, mana regeneration, cooldown reduction, spell power, spell resistance, cast-time reduction, summon damage, casting movement speed, and power/resistance for each registered school. ([`AttributeRegistry`](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/AttributeRegistry.java#L25-L56))

Only **spell power** and **summon damage** naturally project into `amplify`. School power should affect `amplify` only for a cast carrying that exact upstream school ID. Spell level should remain adapter metadata because it also governs tooltips and mana cost in Iron's; cooldown and cast time remain typed cast properties.

## Create

The official 1.21.1 line's latest tagged source reviewed here is Create 6.0.10. Create does not justify a family of Create-branded traits. Existing `fire`/heat, `water`, `air`, and spirit/death-style semantic traits already cover its blasting, splashing, airflow/boiler, and haunting themes. `motion` is the only plausible new general-purpose trait; `amplify` can scale the amount, duration, radius, or strength of a Create-facing effect. Rotation, RPM, stress, contraption, machine, automation, recipe type, blasting, and haunting are adapter or execution facts rather than weighted traits. ([official releases](https://github.com/Creators-of-Create/Create/releases), [official 1.21.1 source](https://github.com/Creators-of-Create/Create/tree/mc1.21.1-6.0.10))

### High-value integration seams

| Create concept | First-party seam | Vestige representation |
|---|---|---|
| Kinetic machinery | `KineticBlockEntity.getSpeed()`, `getTheoreticalSpeed()`, `isOverStressed()`, `hasNetwork()` ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/content/kinetics/base/KineticBlockEntity.java)) | Read-only adapter conditions such as `create:rpm`, `create:overstressed`, and `create:has_kinetic_network`; optionally project the semantic `motion` trait. The class is internal, so isolate it behind a version-specific adapter. |
| A Vestige block consumes or generates rotation | Public `BlockStressValues.IMPACTS`, `CAPACITIES`, and `RPM` ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/stress/BlockStressValues.java)) | Adapter registration/metadata, not traits. `RPM` is tooltip-oriented; active generation still involves internal kinetic classes. |
| A Vestige block moves on a contraption | `MovementBehaviour` callbacks including `startMoving`, `tick`, `visitNewPosition`, `onSpeedChanged`, and `stopMoving` ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/behaviour/movement/MovementBehaviour.java)) | Namespaced triggers attached through the adapter. |
| Moving-block interaction and movement safety | `MovingInteractionBehaviour`, `BlockMovementChecks`, `ContraptionMovementSetting`, and moved-block transformers ([interaction](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/behaviour/interaction/MovingInteractionBehaviour.java), [movement checks](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/contraption/BlockMovementChecks.java)) | Triggers/effects plus block adapter metadata. |
| Inventory and fluid automation | NeoForge item/fluid block and item capabilities, which Create exposes across belts, basins, vaults, tanks, and spouts ([NeoForge capability docs](https://docs.neoforged.net/docs/1.21.1/inventories/capabilities/), [Create basin exposure](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/content/processing/basin/BasinBlockEntity.java#L134-L145)) | Capability-presence/contents conditions and transfer effects. This is the lowest-coupling initial integration. |
| Magical storage mounted on contraptions | `MountedItemStorageType` and `MountedFluidStorageType` ([item](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/contraption/storage/item/MountedItemStorageType.java), [fluid](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/contraption/storage/fluid/MountedFluidStorageType.java)) | Adapter registration/metadata. |
| Magical fluids leaving open pipes | `OpenPipeEffectHandler.REGISTRY` ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/effect/OpenPipeEffectHandler.java)) | Area-effect adapter. |
| Spouting onto ritual blocks or cauldrons | `BlockSpoutingBehaviour.BY_BLOCK` and `BY_BLOCK_ENTITY` ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/behaviour/spouting/BlockSpoutingBehaviour.java)) | Fluid-transfer/reaction effect adapter. |
| Fluids colliding | `PipeCollisionEvent.Flow` and `.Spill`, posted on `NeoForge.EVENT_BUS`, with a mutable product state ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/event/PipeCollisionEvent.java)) | Strong trigger plus reaction effect. |
| Magical boiler heat | `BoilerHeater.REGISTRY` ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/boiler/BoilerHeater.java)) | Adapter registration driven by heat-producing behavior; an existing `fire` trait may inform eligibility but should not be the heat implementation. |
| Automated magical processing | `AllRecipeTypes`: crushing, cutting, milling, mixing, compacting, pressing, splashing, haunting, deploying, filling, emptying, mechanical crafting, and sequenced assembly ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/AllRecipeTypes.java)) | Recipes and effect metadata, preferably data-driven. |
| Create filters query Vestige magic | `CreateBuiltInRegistries.ITEM_ATTRIBUTE_TYPE` and `ItemAttribute` ([registry](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/registry/CreateBuiltInRegistries.java), [attribute contract](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/content/logistics/item/filter/attribute/ItemAttribute.java)) | Excellent adapter bridge: expose “has Vestige trait X” for scrolls, implements, and ingredients. |
| Create displays show magical state | `DisplaySource` and `DisplayTarget` ([source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/api/behaviour/display/DisplaySource.java)) | Display adapter for mana, ritual progress, selected spell, and similar values. |

Create's `AllTags` also defines ready-made datapack contracts for fan-processing catalysts (`blasting`, `smoking`, `splashing`, `haunting`), movable/non-movable and brittle blocks, mounted storage, passive boiler heaters, windmill sails, bottomless fluids, and belt orientation. Vestige content should participate in those tags; their names should not become traits. ([`AllTags`](https://github.com/Creators-of-Create/Create/blob/mc1.21.1-6.0.10/src/main/java/com/simibubi/create/AllTags.java))

Two limitations matter. Create has no general NeoForge kinetic-change or contraption-assembly event, and its rotation system is not NeoForge Energy. Read-only kinetic conditions through a thin adapter are reasonable, but directly mutating `KineticNetwork`, `TorquePropagator`, or rotation propagation would bind Vestige to internals. Start with standard item/fluid capabilities, recipes and tags, trait-aware item filters, pipe/spout/open-pipe reactions, displays, and contraption-safety metadata; postpone arbitrary spell-driven kinetic-network mutation.

## Dragon mod is not identified yet

At least three first-party projects plausibly match the phrase, but they imply very different integration work:

- **Ice and Fire: Community Edition** is currently available for Minecraft 1.21.1 on NeoForge. Its official repository describes it as an unofficial Ice and Fire fork and explicitly lists an Iron's Spells compatibility addon. ([official repository](https://github.com/IAFEnvoy/IceAndFire-CE), [official project files](https://www.curseforge.com/minecraft/mc-mods/iceandfire-ce/files/all?page=1&version=1.21.1))
- **Dragon Survival** has an active `1.21.1` branch whose build targets Minecraft 1.21.1 and NeoForge 21.1; it is about the player transforming into a dragon, so its integration surface would emphasize player form/type, size/stage, breath, flight, and body restrictions rather than tame-dragon ownership. ([official repository](https://github.com/DragonSurvivalTeam/DragonSurvival/tree/1.21.1), [`gradle.properties`](https://github.com/DragonSurvivalTeam/DragonSurvival/blob/1.21.1/gradle.properties))
- **Dragon Mounts: Legacy** has official releases whose Forge jar covers Minecraft 1.21 and 1.21.1, but the release source uses Minecraft Forge 51 rather than NeoForge. It is a plausible name match but not a clean fit for the requested NeoForge baseline. ([official release](https://github.com/MWall541/Dragon-Mounts-Legacy/releases/tag/1.21-1.2.5-beta), [`gradle.properties` at that release](https://github.com/MWall541/Dragon-Mounts-Legacy/blob/e267604de32a61989510fa69a7918af9e0972557/gradle.properties))

Do not design a concrete dragon adapter until the mod is named. `draconic` is still a safe core semantic trait: the adapter can project it from a dragon entity, player form, breath spell, item, or lineage while preserving each mod's exact type/breed/stage as namespaced metadata. Fire, ice, lightning, poison, flight, breath shape, taming, riding, transformation, and growth remain ordinary semantic traits, effects, conditions, or event payloads according to what the selected mod actually exposes.

## Proposed interoperability contract

- Keep traits flat and namespaced; the families above are documentation, not a runtime category hierarchy.
- Store exact external IDs before projection. Never make integration depend on localized display names.
- Keep exact foreign school IDs as adapter metadata and project them directly into Vestige traits. Optional datapacks may contribute finer mappings, but ordinary compatibility must not depend on them.
- Derive capability flags from native Vestige effects. Foreign casts expose the capability information available from their outcome events without pretending Vestige owns their effect graph.
- Use `amplify` for generic potency only. Specific numerical properties remain typed values in effect/cast definitions.
- Prefer NeoForge/common tags and public events over mixins. Use direct class integration only behind optional, versioned compatibility modules.
