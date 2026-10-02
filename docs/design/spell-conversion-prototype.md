# Spell conversion and Iron integration prototype

> September 30 scope update: the project owner requested native conversion of the entire pinned Iron catalog and removal of inherited gameplay. The earlier integration-only scope below is historical. See [the conversion ledger](iron-spell-conversions.md) and the current runtime/status documents.

Status: historical design experiment, 2026-09-14. The current implementation and its limits are recorded in [spell-runtime.md](spell-runtime.md); verification is recorded in [development-status.md](../development-status.md).

The verdict below describes the skeleton as it existed on September 14. The September 30 runtime milestone adds executable JSON plans, cast sessions/modes, bindings, manifestations, and the four native canaries recommended here. It does not imply that the full conversion sample or foreign integrations are implemented.

This document tests two different relationships:

- Pathfinder 2e and Electroblob are native conversion sources. Their spells should become Vestige `SpellDefinition` content.
- Iron's Spells 'n Spellbooks is an integration peer. Iron remains authoritative for defining, casting, and resolving its spells while a Vestige adapter exposes their identity and events to Vestige systems.

The upstream behavior survey and primary-source links are recorded in [the compatibility sample](../research/spell-framework-compatibility-sample.md). The Iron native-recreation sketches below are diagnostic pressure tests for Vestige's vocabulary. They are not a recommendation to port or replace Iron's spell implementations.

The numbers below are illustrative translations, not balance recommendations. Trait names come from the current built-in catalog. A trait still has no automatic behavior: every effect shown below explicitly chooses whether to read `amplify`, `range`, or `area`.

## Verdict

The model has the right top-level bones for native Pathfinder and Electroblob conversion, but it is not yet a complete execution grammar.

- Pathfinder identity converts cleanly: traditions, traits, typed costs, triggers, and conditions describe all seven samples without distortion.
- Immediate Pathfinder and Electroblob-style spells convert cleanly: projectiles, direct healing, ordinary teleportation, status application, and simple summons fit naturally.
- Stateful spells expose the missing center of the model: channels, auras, reactions, recasts, sustained summons, destructible constructs, and delayed consequences need a persistent runtime object owned by a cast.
- Pathfinder's degrees of success and Iron's event interception require resolution phases. A trigger that runs only after damage has been committed cannot faithfully implement Interposing Earth, Counterspell, or Oakskin.
- Any spell could be hidden inside a bespoke `SpellEffect` Java class, but that is not counted as a clean conversion. It would reproduce the class-per-spell architecture we are trying to move beyond.

The current skeleton can describe the identity of every sampled spell, but it does not execute concrete effect types yet. Approximately one third of the native-conversion sample maps cleanly to obvious reusable leaf effects without needing an additional state model. With the small set of reusable runtime concepts proposed below, most Pathfinder and Electroblob spell families should retain their important behavior without spell-specific orchestration. Synesthesia and Wall of Stone still require intentional Minecraft-specific interaction design.

Iron's is a different result: its identity, cast lifecycle, and major outcomes project cleanly across an adapter seam, while arbitrary internal geometry and scaling do not. Vestige should observe or deliberately modify the public Iron event surface, never duplicate Iron's projectiles, portals, summon manager, mana spending, cooldowns, or cleanup.

## Iron integration seam

The adapter should present a small interface to Vestige:

```text
Iron cast or outcome event
  -> exact foreign spell ID, school ID, level, cast type, and cast source
  -> optional school-level Vestige trait projection
  -> Vestige trigger plus condition context
  -> optional supported mutation of the Iron event
```

Iron remains the sole owner of execution. Vestige gets four forms of tandem behavior:

1. Vestige equipment, bindings, or spells can react to Iron casts, damage, healing, summons, teleportation, mana changes, and counterspells.
2. Iron spell identity can participate in Vestige conditions through provider, exact upstream school, level, cast type, cast source, and optional broad traits. The spell ID remains available but ordinary integration rules do not need to enumerate it.
3. Vestige can modify outcomes only where Iron exposes a supported mutable or cancellable event. This includes several cast, damage, healing, summon, teleport, mana, cooldown, and level seams.
4. A small bridge can make Iron counterspelling cancel an active Vestige cast and make Vestige dispelling recognize supported Iron-owned manifestations, without either engine taking ownership of the other's state.

Range, area, duration, projectile behavior, and bespoke spell state cannot be changed generically across every Iron spell because Iron does not expose one universal effect graph. Per-spell compatibility may add deeper support, but the default adapter should remain event-based and lossless about foreign identity.

The default adapter therefore has no maintained list of Iron spells. New spells in a known school are detectable immediately. Addon schools remain detectable by their exact namespaced school ID even when Vestige has no semantic trait projection for them.

## Cleanliness rubric

| Grade | Meaning |
|---|---|
| A | Composed from ordinary data and reusable leaf effects |
| B | Needs one reusable state or targeting primitive, but no spell-specific runtime code |
| C | Representable, but several important behaviors collapse into a large custom effect |
| D | Identity survives while the characteristic play pattern is lost |

## Three representation pressure tests

These examples use a readable pseudo-data syntax. They describe the intended `SpellDefinition` shape, not a codec that already exists.

### Iron's Fireball: diagnostic recreation, not the integration path

Source: [official Fireball implementation](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/fire/FireballSpell.java#L25-L93)

```yaml
id: vestige:irons_fireball
traditions: [arcane, primal]
traits:
  vestige:fire: 5
  vestige:evocation: 4
  vestige:amplify: 1
  vestige:range: 1
  vestige:area: 1
costs:
  - time: { ticks: 40 }
  - mana: { amount: 60 }
triggers:
  - id: primary
    event: vestige:interact
effects:
  - type: vestige:projectile
    projectile: vestige:fireball
    maximum_distance: { product: [32, { trait: vestige:range }] }
    on_impact:
      - type: vestige:damage_area
        damage_type: minecraft:in_fire
        shape:
          sphere_radius: { product: [3, { trait: vestige:area }] }
        amount: { product: [10, { trait: vestige:amplify }] }
        falloff: squared_distance
        conditions:
          - matches: [target, vestige:line_of_sight_from_impact]
      - type: vestige:ignite
        ticks: { product: [80, { trait: vestige:amplify }] }
      - type: vestige:explode_blocks
        conditions:
          - matches: [world, vestige:spell_griefing_enabled]
```

This shows that Vestige's vocabulary could describe the same behavior. The tandem integration does not instantiate this definition: Iron executes its own projectile and explosion, while Vestige observes cast and damage events and associates the reviewed traits with that foreign spell ID. `amplify`, `range`, and `area` are ordinary ratings until a Vestige-owned effect or adapter rule explicitly reads them. Native-recreation grade: **A**, but recreation is not the integration goal.

### Pathfinder Interposing Earth: reaction plus manifestation

Source: [Interposing Earth](https://2e.aonprd.com/Spells.aspx?ID=1337)

```yaml
id: vestige:interposing_earth
traditions: [arcane, primal]
traits:
  vestige:earth: 5
  vestige:stone: 3
  vestige:abjuration: 4
  vestige:conjuration: 2
  vestige:amplify: 1
costs:
  - mana: { amount: 18 }
triggers:
  - id: intercept_strike
    event: vestige:attack_targeted
    conditions:
      - compare: [target, equal, actor]
  - id: intercept_area
    event: vestige:save_requested
    conditions:
      - tagged: [event/damage_type, c:damage_types/damaging]
      - compare: [event/save_kind, equal, vestige:reflex]
effects:
  - type: vestige:create_manifestation
    manifestation: vestige:earth_barrier
    placement: between_actor_and_event_source
    duration_ticks: 60
    state:
      health: { product: [5, { trait: vestige:amplify }] }
      hardness: { product: [2, { trait: vestige:amplify }] }
    bindings:
      - event: vestige:damage_calculating
        effects:
          - type: vestige:reduce_pending_damage
            amount: { product: [2, { trait: vestige:amplify }] }
          - type: vestige:destroy_this_manifestation
```

The spell's identity fits the existing definition. Its behavior requires two pre-resolution trigger points and one persistent, destructible manifestation. Running it from `damage_taken` would be too late and would lose the reaction's defensive timing. Current grade: **C**. Grade with the reusable primitives below: **A**.

### Iron's Portal: diagnostic state test, not the integration path

Sources: [Portal spell](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/ender/PortalSpell.java#L40-L249) and [portal entity](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/entity/spells/portal/PortalEntity.java#L81-L171)

```yaml
id: vestige:irons_portal
traditions: [arcane, occult]
traits:
  vestige:space: 5
  vestige:void: 2
  vestige:teleportation: 5
  vestige:conjuration: 4
  vestige:range: 1
costs:
  - mana: { amount: 200 }
triggers:
  - id: place_endpoint
    event: vestige:interact
effects:
  - type: vestige:branch
    if:
      not:
        exists: cast_session/first_endpoint
    then:
      - type: vestige:store_cast_value
        path: first_endpoint
        value: selected_block_face
      - type: vestige:await_recast
        timeout_ticks: 2400
    else:
      - type: vestige:create_linked_manifestations
        manifestation: vestige:portal_endpoint
        endpoints: [cast_session/first_endpoint, selected_block_face]
        duration_ticks: { product: [6000, { trait: vestige:amplify }] }
      - type: vestige:complete_cast_session
```

This shows the state that a native portal effect would require. In the actual tandem integration, Iron continues to own both endpoints, dimension transfer, velocity rotation, cooldown, loop protection, and cleanup. Vestige observes the cast and teleport outcomes and can react through the adapter. Native-recreation grade: **D** without generic sessions and manifestations, or **A** with them, but recreation is not the integration goal.

## Pathfinder conversions

### Horizon Thunder Sphere

Source: [Horizon Thunder Sphere](https://2e.aonprd.com/Spells.aspx?ID=2732)

- **Traditions:** arcane, primal.
- **Traits:** lightning 5, evocation 4, amplify 1, range 1, area 1.
- **Costs and trigger:** an interact trigger selects one of three cast modes. Each mode supplies its own time cost.
- **Effects:** a ranged projectile or ray attack; an on-critical dazzle; the three-action mode changes range and miss resolution; the two-round mode stores a pending cast, creates a target-centered burst that excludes the primary target, and installs a one-minute retaliatory damage binding on the caster.
- **Vibe risk:** flattening the three modes into three separate spells loses the wonderful decision between speed, reach, reliability, and a dangerous full charge. The modes should remain one spell with one identity.
- **Fit:** current grade **D** for the two-round version, proposed grade **A** with cast modes, staged sessions, outcome branches, and bindings.

### Summon Animal

Sources: [Summon Animal](https://2e.aonprd.com/Spells.aspx?ID=1694) and the [summon trait rules](https://2e.aonprd.com/Traits.aspx?ID=520)

- **Traditions:** arcane, primal.
- **Traits:** life 4, spirit 2, conjuration 5, amplify 1, range 1.
- **Costs and trigger:** a long interact cast with mana and time costs.
- **Effects:** choose an entity from an allowed registry tag and power band, validate a destination against its footprint, spawn it as an owned manifestation, and install a Sustain binding. Each Sustain refreshes expiry and grants or refreshes its command budget.
- **Vibe risk:** choosing the creature is the spell. Replacing it with a fixed wolf summon produces a functional summon but not Summon Animal.
- **Fit:** current grade **C**, proposed grade **A** with an entity-choice target, owned manifestations, and Sustain.

### Interposing Earth

The full encoding appears above. The key lesson is that reaction timing is part of the effect. Grade: **C** now, **A** with pre-resolution hooks and manifestations.

### Translocate

Source: [Translocate](https://2e.aonprd.com/Spells.aspx?ID=1724)

- **Traditions:** arcane, occult.
- **Traits:** space 5, teleportation 5, conjuration 4, range 1.
- **Costs and trigger:** interact with a moderate time and mana cost.
- **Effects:** select a visible empty destination, validate collision and passenger rules, then safely move the caster and allowed inventory. A more powerful authored variant can replace the destination policy and install a reuse lockout binding.
- **Vibe risk:** low. Minecraft does not normally put creatures inside carried extradimensional containers, so that particular restriction becomes adapter-specific validation rather than a core rule.
- **Fit:** current grade **B**, proposed grade **A** with a reusable destination selector and failure policy.

### Bless

Source: [Bless](https://2e.aonprd.com/Spells.aspx?ID=1451)

- **Traditions:** divine, occult.
- **Traits:** holy 4, spirit 4, emotion 2, enchantment 3, amplify 1, area 1.
- **Costs and trigger:** interact with time and mana costs.
- **Effects:** create a caster-attached aura manifestation. Every tick it derives membership from current distance and ally relation and maintains an attack bonus. An optional Sustain trigger increases stored radius once per interval. A dispel interaction can explicitly counter its opposed manifestation.
- **Vibe risk:** snapshotting allies at cast time loses the feeling of rallying around a moving source. The area must follow the caster and recalculate membership.
- **Fit:** current grade **C**, proposed grade **A** with attached manifestations and mutable state.

### Synesthesia

Source: [Synesthesia](https://2e.aonprd.com/Spells.aspx?ID=2035)

- **Tradition:** occult.
- **Traits:** mind 5, emotion 3, illusion 4, enchantment 3, curse 2, amplify 1.
- **Costs and trigger:** interact against one selected entity.
- **Effects:** resolve a mental defense outcome, then branch by degree. Apply movement and coordination penalties, install a binding that can waste future concentration actions, and alter what the affected player can perceive. Critical failure also stuns.
- **Vibe risk:** high. Replacing the spell with Slowness and Blindness keeps its combat strength but loses the sensory-confusion identity. Faithful conversion needs observer-relative rendering or targeting changes.
- **Fit:** current grade **D**, proposed grade **B** because client perception remains a specialized effect even after generic outcome and binding support.

### Wall of Stone

Source: [Wall of Stone](https://2e.aonprd.com/Spells.aspx?ID=1751)

- **Traditions:** arcane, primal.
- **Traits:** earth 5, stone 5, conjuration 4, transmutation 2, amplify 1, range 1, area 1.
- **Costs and trigger:** long interact cast with mana and a selected path.
- **Effects:** validate an entire connected placement before paying the final cost, create section manifestations with health and hardness, and replace destroyed sections with rubble.
- **Vibe risk:** high if reduced to a straight prefab wall. The Pathfinder spell is memorable because it can become a wall, bridge, ramp, or staircase. Minecraft is unusually well suited to preserving this, but it needs a good path-selection interaction.
- **Fit:** current grade **D**, proposed grade **B**. The runtime can be generic, but freeform placement UX is purpose-built.

## Iron's tandem-integration pressure tests

The entries in this section describe what Vestige needs to understand or observe. Their native-recreation grades are diagnostic only. None proposes replacing Iron's authoritative execution.

### Fireball

The adapter preserves `irons_spellbooks:fireball`, its Fire school, level, cast type, source, and relevant configuration-derived metadata. Its Fire school projects directly to the Vestige `fire` trait. Iron owns charge-up, mana, cooldown, projectile motion, collision, damage falloff, ignition, explosion, and cleanup.

Vestige can react to Iron's pre-cast, cast, and spell-damage events. A Vestige item could reduce a supported mana cost, add a conditional effect after Fireball damage, or progress a fire-oriented path. Vestige cannot generically enlarge Fireball's explosion merely because the caster has more `area`; that requires an explicit Fireball integration because Iron owns the radius calculation.

### Telekinesis

Source: [official Telekinesis implementation](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/eldritch/TelekinesisSpell.java#L35-L166)

- **Projection:** arcane and occult compatibility; mind, force, motion, and classical evocation traits.
- **Observed behavior:** cast identity and lifecycle enter Vestige as foreign-cast triggers. Vestige may react when the channel begins or completes.
- **Iron ownership:** target acquisition, hold distance, live gaze-relative velocity, refresh clocks, cancellation, mana, and cooldown remain entirely inside Iron.
- **Integration limit:** there is no need for Vestige to model the channel's internal state unless a particular cross-mod feature must inspect it. General `range` or `amplify` ratings do not alter it automatically.

### Summon Vex

Source: [official Summon Vex implementation](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/evocation/SummonVexSpell.java#L30-L120)

- **Projection:** arcane and occult compatibility; spirit and classical conjuration traits.
- **Observed behavior:** Iron's summon and owner-assignment events become Vestige trigger contexts containing the foreign spell ID, summoned entity, and owner.
- **Tandem behavior:** Vestige conditions can recognize the Vex as an Iron-owned summon, and Vestige equipment can respond to its creation or actions.
- **Iron ownership:** entity count, placement, AI, recast dismissal, UUID tracking, expiry, disconnect persistence, mana, and cooldown remain inside Iron's summon manager.

### Portal

Iron retains the two-cast setup, recast state, endpoint entities or blocks, serialization, cross-dimension transfer, motion transformation, loop protection, and cleanup. Vestige observes the foreign cast and `SpellTeleportEvent`, projects space, void, teleportation, and classical conjuration traits, and can react to portal transit. The native state sketch above remains useful for validating Vestige's own future portal spells, not for replacing Iron's.

### Counterspell

Source: [official Counterspell implementation](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/ender/CounterspellSpell.java#L32-L114)

- **Projection:** arcane and occult compatibility; space and classical abjuration traits.
- **Observed behavior:** Iron's `CounterSpellEvent` becomes a Vestige trigger with the exact caster and target.
- **Two-way bridge:** when the target owns an active Vestige cast, the adapter can interrupt that cast. Conversely, a Vestige dispel effect can recognize explicitly supported Iron targets through the adapter rather than depending directly on Iron implementation classes throughout the spell engine.
- **Iron ownership:** ray selection, native target protocols, summon exceptions, Iron cast cancellation, recast removal, native status removal, mana, and cooldown remain inside Iron.

### Oakskin

Sources: [official Oakskin spell](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/nature/OakskinSpell.java#L32-L106) and [Oakskin effect](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/effect/OakskinEffect.java#L22-L74)

- **Projection:** primal compatibility; life, plant, wood, classical abjuration, and transmutation traits.
- **Observed behavior:** Vestige can see the foreign cast and ordinary NeoForge damage events involving an Oakskin-bearing entity.
- **Tandem behavior:** a Vestige condition may recognize the active Iron effect, but Vestige should not reproduce or stack its reduction formula unless a specific compatibility rule calls for it.
- **Iron ownership:** replacement behavior, duration, movement penalty, damage reduction, cap, source provenance, mana, cooldown, and cleanup remain inside Iron.

## Minimal additions that unlock the sample

These are runtime concepts for native Pathfinder and Electroblob conversion, not new trait behavior. The basic Iron adapter does not depend on recreating them, though bindings and resolution phases help with deeper two-way compatibility.

### 1. Target specification

A target specification selects and validates subjects independently of effects:

- origins: caster, source, impact, stored point, or manifestation;
- selections: self, entity ray, block face, visible point, area, chain, or authored path;
- filters: ally, hostile, owner, entity tag, line of sight, empty placement, or named predicate;
- failure policy: do nothing, refund, consume, interrupt, or lose the spell.

This removes duplicated target logic from every effect without turning delivery shapes into traits.

### 2. Resolution phases and outcomes

Gameplay events need explicit phases such as `targeting`, `calculating`, `committed`, and `completed`. A calculating event carries a mutable pending result that defensive effects may reduce, replace, or cancel. Optional attack/save resolution can produce named outcomes such as critical success, success, failure, and critical failure.

This unlocks Interposing Earth, Synesthesia, Counterspell, Oakskin, shields, resistance, reflected damage, and many equipment passives.

### 3. Cast sessions and cast modes

A cast session is temporary state belonging to one unfinished or repeatable activation. It stores selected subjects and authored values, supports stages and recasts, and owns completion, interruption, timeout, refund, and cleanup behavior. Cast modes let one spell expose several cost and effect plans without pretending they are unrelated spells.

This unlocks Horizon Thunder Sphere, Telekinesis, Portal, recast dismissal, charged spells, and any multi-click ritual.

### 4. Bindings

A binding installs triggers and modifiers on an entity or manifestation for a duration or number of charges. It owns replacement rules and cleanup. Bindings reuse the existing trigger, condition, and effect language rather than creating a parallel status-effect system.

This unlocks retaliation, Oakskin, Bless membership, concentration disruption, Evasion, Echoing Strikes, delayed detonations, and conditional weapon enchantments.

### 5. Manifestations

A manifestation is persistent magical state created by a cast. It can be backed by an entity, block entity, transient field, or attached logical object. It carries owner, source spell, resolved traits, causal lineage, state values, duration, ticking behavior, child triggers, and a dispelling protocol.

This unlocks summons, barriers, auras, storms, black holes, walls, portals, traps, zones, and temporary constructs without forcing them into the definition of a one-time cast.

### 6. Small effect composition algebra

The engine needs generic composition nodes:

- sequence;
- branch;
- delay;
- repeat while a session, binding, or manifestation is active;
- for-each selected target;
- install binding;
- create manifestation;
- invoke a registered leaf effect.

The leaf catalog can remain pragmatic: damage, heal, apply or remove status, move, teleport, summon, place or break block, modify a pending event, interrupt, and dispel. Exotic visuals and creature AI remain purpose-built executors behind those stable boundaries.

## What would actually lose the vibe

For Pathfinder and Electroblob conversion, the danger is not changing exact numbers. Minecraft needs different balance. The danger is collapsing the decision or state that makes a spell recognizable. For Iron integration, the corresponding danger is duplicating behavior and allowing Vestige's approximation to drift from Iron's authoritative result.

| Spell | Characteristic state or decision that must survive |
|---|---|
| Horizon Thunder Sphere | Choosing how long to charge and accepting interruption risk |
| Summon Animal | Choosing a creature and actively sustaining command |
| Interposing Earth | Interrupting an incoming effect before it resolves |
| Translocate | Selecting and validating a destination |
| Bless | Allies moving through an aura that grows with continued attention |
| Synesthesia | The target's future actions and perception becoming unreliable |
| Wall of Stone | Drawing useful freeform geometry |
| Fireball | Projectile impact, radial falloff, and fiery world interaction |
| Telekinesis | Steering a held victim continuously with the caster's gaze |
| Summon Vex | Owned followers with a dismissible lifecycle |
| Portal | Establishing two endpoints, then using a persistent connection |
| Counterspell | One anti-magic protocol applying across casts, summons, and effects |
| Oakskin | Trading movement for durable damage reduction |

## Recommendation

Keep the existing immutable `SpellDefinition` and flat trait profile. Do not put any of the missing behavior into traits.

For Iron, build one deep adapter module with a small event-oriented interface:

1. preserve exact foreign spell and school identity;
2. expose provider, school, level, cast type, cast source, and any safe school-level trait projection;
3. translate supported Iron lifecycle and outcome events into Vestige triggers and condition values;
4. correlate events with Vestige causal chains where possible;
5. expose narrow interruption and dispelling hooks for explicit two-way support.

Iron remains authoritative for mana, cooldowns, cast state, projectiles, effects, summons, portals, and cleanup.

For native Pathfinder and Electroblob conversion, the next architectural prototype should add only three runtime seams first:

1. a cast session with mode/stage and local state;
2. bindings for temporary event-driven behavior;
3. manifestations for persistent world or entity state.

Target specifications and effect composition can then be introduced as the narrow language used inside those three owners. Four useful native canaries already exist in scope:

- Electroblob Force Arrow for ordinary projectile delivery;
- Electroblob Summon Zombie for ownership, lifetime, count, and equipment-dependent variants;
- Electroblob Arcane Lock for persistent block state and interaction prevention;
- Pathfinder Interposing Earth for a pre-resolution reaction and destructible manifestation.

Horizon Thunder Sphere should follow as the staged-cast test. If these remain data-readable and do not need spell-specific orchestration classes, the framework is deep enough to begin bulk Pathfinder and Electroblob conversion.
