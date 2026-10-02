# Spell-framework compatibility sample

Research checked against primary sources on 2026-09-14. Pathfinder mechanics come from Paizo rules as reproduced by Archives of Nethys. Iron's Spells 'n Spellbooks mechanics come from the official source repository's latest Minecraft 1.20.1 release tag, [`v1.20.1-3.16.3`](https://github.com/iron431/irons-spells-n-spellbooks/tree/v1.20.1-3.16.3); the tag's [`gradle.properties`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/gradle.properties#L6-L29) identifies Minecraft 1.20.1 and mod version 3.16.3.

This is a deliberately adversarial sample for evaluating Vestige's new spell-definition framework. It records source behavior and the mechanics that put pressure on a general representation. **It does not propose Vestige conversions.** “Representation pressure” is an inference from the cited source rules or code, not a claim made by either upstream project.

## Coverage at a glance

| Mechanic family | Pathfinder samples | Iron's samples |
|---|---|---|
| Projectile or ranged attack | Horizon Thunder Sphere | Fireball |
| Area and relation filtering | Horizon Thunder Sphere; Bless | Fireball |
| Summon and ownership | Summon Animal | Summon Vex |
| Sustained, channeled, or per-tick behavior | Summon Animal; Bless | Telekinesis |
| Reaction or event-driven passive behavior | Interposing Earth; Horizon Thunder Sphere; Synesthesia | Oakskin |
| Teleportation | Translocate | Portal |
| Buff or debuff | Bless; Synesthesia | Oakskin |
| Terrain, barrier, or construct | Interposing Earth; Wall of Stone | Portal |
| Unusual costs, casting, or targeting | Horizon Thunder Sphere; Translocate; Wall of Stone | Portal; Counterspell |

## Pathfinder 2e sample

### 1. Horizon Thunder Sphere

**Source:** [Archives of Nethys — Horizon Thunder Sphere](https://2e.aonprd.com/Spells.aspx?ID=2732)

- **Identity:** rank 1; arcane or primal; attack, concentrate, electricity, and manipulate traits.
- **Casting:** the caster chooses two actions, three actions, or a two-round cast consisting of three actions followed by two actions on the next turn.
- **Targeting and resolution:** one creature; the initial effect is a ranged spell attack against AC. The two-action version has 30-foot range. The three-action version has 60-foot range and deals half damage on an ordinary miss. A hit deals 3d6 electricity; a critical hit doubles the damage and dazzles the target for 1 round.
- **Two-round version:** actions can be taken between the two casting installments, but casting another spell before completion loses Horizon Thunder Sphere. After the attack, whether it hits or misses, creatures other than the primary target in a 10-foot emanation around that target take 2d6 electricity with a basic Reflex save. For 1 minute afterward, a creature that Grapples the caster or hits the caster with a qualifying close attack takes 1 electricity damage.
- **Heightening:** every additional rank adds 2d6 to both the initial and burst damage and adds 1 to the retaliatory damage.
- **Representation pressure:** action-count variants alter range and miss behavior; a cast can span turns and be invalidated by an intervening event; one cast combines an attack roll with secondary saves; the primary target is excluded from a target-centered area; the completed cast installs a retaliatory trigger; one heightening rule changes several independent quantities.

### 2. Summon Animal

**Sources:** [Archives of Nethys — Summon Animal](https://2e.aonprd.com/Spells.aspx?ID=1694), [summon trait](https://2e.aonprd.com/Traits.aspx?ID=520), [summoned trait](https://2e.aonprd.com/Traits.aspx?ID=706), [minion trait](https://2e.aonprd.com/Traits.aspx?ID=653), and [Sustain action](https://2e.aonprd.com/Actions.aspx?ID=2317)

- **Identity:** rank 1; arcane or primal; concentrate, manipulate, and summon traits.
- **Casting and duration:** three actions; range 30 feet; sustained for up to 1 minute.
- **Effect:** creates a common animal-trait creature of level –1 in an unoccupied space large enough for it. The creature has the summoned and minion rules, receives 2 actions immediately, disappears at 0 HP or when the spell ends, and is subject to the shared restrictions on summoning, value creation, and cost-bearing abilities.
- **Ongoing control:** on later turns, Sustaining the spell also commands the minion and gives it its 2 actions.
- **Heightening:** ranks 2 through 10 summon maximum creature levels 1, 2, 3, 5, 7, 9, 11, 13, and 15 respectively.
- **Representation pressure:** the spell delegates most behavior to an external creature definition and several shared trait rules; placement depends on the selected creature's footprint; maintenance both prolongs the spell and grants subordinate actions; lifecycle, ownership, and command restrictions must remain coupled; heightening uses a discrete lookup table rather than a formula.

### 3. Interposing Earth

**Source:** [Archives of Nethys — Interposing Earth](https://2e.aonprd.com/Spells.aspx?ID=1337)

- **Identity:** rank 1; arcane or primal; earth and manipulate traits.
- **Casting trigger:** a reaction when the caster is targeted by a Strike **or** would attempt a Reflex save against a damaging area effect.
- **Effect:** creates a 1-inch-thick, 5-by-5-foot earth barrier on a square border between the caster and the triggering source. It gives the caster standard cover against the triggering effect.
- **Damage interception and persistence:** if that effect still damages the caster, the barrier is destroyed and reduces the damage by 2. Otherwise it remains for up to 3 rounds or until destroyed, with AC 5, Hardness 2, and 5 HP.
- **Heightening:** at rank 4, damage reduction and Hardness become 8 and HP becomes 20.
- **Representation pressure:** the two trigger alternatives occur before different kinds of resolution; the barrier's placement is relative to the triggering source; it must be inserted into an already-started attack or save calculation; damage interception conditionally destroys the interceptor; the remaining effect is a world object with combat statistics; heightening changes at a fixed threshold rather than every rank.

### 4. Translocate

**Source:** [Archives of Nethys — Translocate](https://2e.aonprd.com/Spells.aspx?ID=1724)

- **Identity:** rank 4; arcane or occult; concentrate, manipulate, and teleportation traits.
- **Casting and destination:** two actions; the caster moves up to 120 feet to a visible, unoccupied space, bringing worn and held items.
- **Passenger restriction:** if the teleport would carry another creature, including a creature inside an extradimensional container, the spell is lost.
- **Heightening:** at rank 5 the range becomes 1 mile. Line of sight is no longer required if the caster has visited the destination and knows its relative location and distance; after using this version, the caster is temporarily immune to it for 1 hour.
- **Representation pressure:** this validates a destination rather than targeting an entity; transported inventory and nested-container occupants affect validity; invalid passenger state loses the spell rather than merely failing movement; heightening changes validation rules, not only numbers, and installs a long-lived reuse immunity.

### 5. Bless

**Source:** [Archives of Nethys — Bless](https://2e.aonprd.com/Spells.aspx?ID=1451)

- **Identity:** rank 1; divine or occult; aura, concentrate, manipulate, and mental traits.
- **Casting and duration:** two actions; a 15-foot emanation centered on the caster; duration 1 minute.
- **Effect:** the caster and allies currently in the emanation gain a +1 status bonus to attack rolls. The effect follows the caster and membership depends on current containment.
- **Optional Sustain:** once per round, the caster can Sustain Bless to enlarge the emanation radius by 10 feet. Sustain changes its area rather than maintaining its already fixed duration. Bless can counteract *bane*.
- **Representation pressure:** a moving aura needs relation-filtered, continuously changing membership; optional Sustain performs a side effect without owning the duration; the geometry accumulates changes during the cast; the bonus uses PF2e's typed stacking rules; a named interaction with another spell uses counteract rules.

### 6. Synesthesia

**Source:** [Archives of Nethys — Synesthesia](https://2e.aonprd.com/Spells.aspx?ID=2035)

- **Identity:** rank 5; occult; concentrate, manipulate, and mental traits.
- **Casting and defense:** two actions; range 30 feet; one creature; Will save.
- **Effect package:** the target must pass a DC 5 flat check each time it uses a concentrate action or waste that action; all creatures and objects are concealed from it; and it is clumsy 3 with a –10-foot status penalty to Speed.
- **Degrees of success:** critical success has no effect; success lasts 1 round; failure lasts 1 minute; critical failure lasts 1 minute and also inflicts stunned 2.
- **Heightening:** at rank 9, it can target up to five creatures.
- **Representation pressure:** one save produces several heterogeneous ongoing effects; one component intercepts arbitrary future actions; concealment is observer-relative perception state; condition and numeric penalties coexist; degree of success controls duration and adds a further condition; fixed-rank heightening changes target cardinality.

### 7. Wall of Stone

**Source:** [Archives of Nethys — Wall of Stone](https://2e.aonprd.com/Spells.aspx?ID=1751)

- **Identity:** rank 5; arcane or primal; concentrate, earth, and manipulate traits.
- **Casting and geometry:** three actions; range 120 feet; creates a 1-inch-thick wall up to 120 feet long and 20 feet high. Each 5-foot length follows a square border. The wall need not be vertical and can serve as a bridge or stairs.
- **Placement:** its path must be one unbroken open space and cannot intersect creatures or objects; otherwise the spell is lost.
- **Object rules:** each 10-by-10-foot section has AC 10, Hardness 14, 50 HP, and immunity to critical hits and precision damage. A destroyed section becomes difficult-terrain rubble.
- **Heightening:** every 2 ranks gives each section 15 additional HP.
- **Representation pressure:** placement is freeform connected geometry with functional orientation; any invalid segment loses the whole spell; the result is a sectioned world object with per-section defenses and immunities; destruction transforms individual sections into a different terrain state; object lifecycle, rather than a spell duration, controls persistence.

## Iron's Spells 'n Spellbooks sample

### 1. Fireball

**Sources:** official [`FireballSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/fire/FireballSpell.java#L25-L93) and [`MagicFireball`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/entity/spells/fireball/MagicFireball.java#L31-L101)

- **Configuration:** Fire school; Rare minimum rarity; levels 1–5; 25-second cooldown; Long cast lasting 40 ticks; base mana 60 plus 15 per level after the first.
- **Launch:** completing the cast spawns a no-gravity projectile from the caster's eye/forward position. Damage is `5 + 5 × spell power`; radius is `2 + floor(spell power)`.
- **Impact:** the projectile finds entities inside the radius, requires line of sight, and scales damage down by squared distance from impact. When spell griefing is enabled, it also creates a terrain-destroying, fire-starting Minecraft explosion at half the damage radius; the explosion can be cancelled by Forge's explosion-start event.
- **Representation pressure:** the delivered projectile and impact outcome have different spatial rules; damage falloff is nonlinear and visibility-gated; world damage is configuration-dependent and externally cancellable; one radius is transformed before being passed to the terrain explosion.

### 2. Telekinesis

**Sources:** official [`TelekinesisSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/eldritch/TelekinesisSpell.java#L35-L166) and [`TelekinesisData`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/capabilities/magic/TelekinesisData.java#L5-L21)

- **Configuration:** Eldritch school; Legendary minimum rarity; levels 1–5; 35-second cooldown; Continuous cast; base mana 25 with no level surcharge. Cast duration is `140 + 20 × (level – 1)` ticks, and target range is `12 + 2 × (level – 1)` blocks.
- **Acquisition:** a pre-cast entity ray target is mandatory. The cast stores the target plus its initial distance, with a minimum held distance of 6.
- **Channel behavior:** every 2 server ticks, the spell computes a moving point in front of the caster, smooths the stored hold distance, and changes the target's velocity toward that point. Upward movement resets fall distance. Every 10 ticks it refreshes Airborne and Antigravity effects. A removed or dying target cancels a player caster's cast.
- **Representation pressure:** effect execution is live caster-relative vector control rather than a one-time outcome; target identity and mutable hold state persist across ticks; several clocks coexist; movement, fall state, and temporary effects are coupled; target lifecycle can terminate the channel asynchronously.

### 3. Summon Vex

**Sources:** official [`SummonVexSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/evocation/SummonVexSpell.java#L30-L120), [`SummonManager` ownership and recast handling](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/capabilities/magic/SummonManager.java#L33-L160), and [`SummonManager` disconnect persistence](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/capabilities/magic/SummonManager.java#L227-L260)

- **Configuration:** Evocation school; Rare minimum rarity; levels 1–5; 150-second cooldown; Long cast lasting 20 ticks; base mana 50 plus 10 per level after the first.
- **Effect:** the first cast spawns `level + 2` Summoned Vex entities around the caster. Each is finalized as a summoned mob, assigned to the caster, recorded in cast data, and given a 10-minute lifetime.
- **Recast and lifecycle:** the spell creates a recast instance holding the summon UUIDs. Finishing a non-timeout recast normally unsummons or discards the recorded entities. The shared summon manager tracks owner-to-summon relationships, expiration, removal, and recast cleanup; its later code also serializes active summons when an owner disconnects.
- **Representation pressure:** count and spawn placement are dynamic; the outcome delegates combat behavior to a custom mob; ownership, expiration, recast state, and entity UUIDs must stay consistent; manual dismissal and natural expiry take different cleanup paths; summon persistence crosses player disconnects.

### 4. Portal

**Sources:** official [`PortalSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/ender/PortalSpell.java#L40-L249), [`PortalData`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/entity/spells/portal/PortalData.java#L13-L148), and [`PortalEntity`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/entity/spells/portal/PortalEntity.java#L81-L171)

- **Configuration:** Ender school; Uncommon minimum rarity; levels 1–3; 180-second cooldown; Instant cast; base mana 200 plus 10 per level after the first; fixed placement reach of 48 blocks.
- **Two-stage creation:** the first cast raycasts a block location and stores the first endpoint in a recast lasting 120 seconds. The second cast supplies the other endpoint. An endpoint can be a spawned portal entity or a compatible, unconnected portal-frame block entity, and block and entity paths have separate validation and cleanup. Casting is forbidden inside Iron's pocket dimension.
- **Persistence:** paired endpoint data includes dimension, position, rotation, UUID, remaining lifetime, and whether the portal uses blocks. Before external spell-power multipliers, spawned portal duration is 5 minutes at level 1 and increases by 2 minutes per level.
- **Transit:** a portal scans its volume for eligible entities and projectiles. Same-dimension travel preserves vertical velocity and rotates horizontal travel to the exit; cross-dimension travel transfers the entity. Per-entity cooldown and loop tracking prevent immediate cycling, with special handling for pathological loops.
- **Representation pressure:** one logical spell is a state machine across separate casts; typed world references and UUIDs must serialize and synchronize; endpoints can have multiple backing implementations; teleportation applies to arbitrary entities and projectiles and can cross dimensions; motion transform, cooldown, loop safety, expiry, and partial-placement cleanup are part of the spell's behavior.

### 5. Counterspell

**Source:** official [`CounterspellSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/ender/CounterspellSpell.java#L32-L114)

- **Configuration:** Ender school; Rare minimum rarity; one level; 10-second cooldown; Instant cast; 50 mana.
- **Targeting:** raycast up to 80 blocks, with blocks stopping the ray, a widened entity hitbox, and Iron's anti-magic target filter.
- **Resolution:** first posts a cancellable CounterSpellEvent. An `AntiMagicSusceptible` target handles its own anti-magic response, subject to special ownership and idle-state rules for summons. A player target has its current cast cancelled and all active recasts removed with a Counterspell result; a magic mob has its cast cancelled. A living target also loses every active mob effect that implements Iron's `MagicMobEffect` marker.
- **Representation pressure:** applicability is protocol- and runtime-type-based rather than a single tag; summon ownership and current AI target affect legality; outside listeners can veto the effect; interruption must unwind in-progress casts and persistent recast state; dispelling uses an extensible effect class boundary; different target kinds have materially different resolution.

### 6. Oakskin

**Sources:** official [`OakskinSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/nature/OakskinSpell.java#L32-L106), [`OakskinEffect`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/effect/OakskinEffect.java#L22-L74), and [`OakskinData`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/effect/OakskinData.java#L7-L23)

- **Configuration:** Nature school; Common minimum rarity; levels 1–8; 90-second cooldown; Instant cast; base mana 25 plus 10 per level after the first.
- **Application:** self-targeted. It removes an existing Oakskin effect and the marker that distinguishes the elixir version, then applies the effect for a base duration of `(20 + 3 × (level – 1))` seconds before external spell-power multipliers.
- **Mitigation:** an event subscriber intercepts every `LivingHurtEvent` while the effect is active and multiplies incoming damage by one minus the computed reduction. The spell version uses a fixed amplifier of 2, producing a base 20% reduction before entity spell-power scaling, with a 75% cap. The elixir marker deliberately disables that scaling.
- **Tradeoff and cleanup:** while active, an attribute hook imposes a fixed 25% movement-speed penalty at every amplifier. Removing the effect also removes its auxiliary source marker.
- **Representation pressure:** the visible status installs an incoming-damage interceptor and an attribute modifier rather than resolving at cast time; mitigation depends on effect amplifier, caster/entity scaling, cap, and source provenance; reapplication has replacement semantics; cleanup must synchronize the status and auxiliary data.

## What this sample establishes

Across thirteen spells, the compatibility boundary is broader than static cast metadata plus immediate effects. A faithful general representation must at least be able to describe or deliberately delegate:

- multi-stage and multi-turn casts, optional Sustain actions, recasts, channels, and independent clocks;
- attacks, saves, save degrees, event interception, and cancellation at several points in resolution;
- changing target sets, relation filters, observer-relative state, destination validation, and arbitrary-entity transit;
- persistent state attached to casters, targets, summons, world objects, and paired endpoints;
- custom creature behavior, freeform/sectioned geometry, destructible constructs, and terrain transformation;
- lifecycle behavior for completion, invalidation, expiry, destruction, dismissal, disconnect, and cleanup;
- fixed-threshold, table-driven, and multi-output scaling in addition to ordinary numeric formulas.

Those are observations about the source mechanic families exposed by the sample. Deciding which belong in Vestige's core schema, reusable runtime capabilities, adapters, or purpose-built effects is a separate design step.
