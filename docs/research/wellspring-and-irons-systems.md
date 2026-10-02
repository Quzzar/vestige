# Pathfinder wellspring terminology and Iron's spell-system architecture

Research checked against primary sources on 2026-09-14. Pathfinder claims use Paizo rules as reproduced by Archives of Nethys. Iron's claims use the mod's official documentation and official source repository; code observations were verified against both the current `1.21` branch and the latest available 1.20.1 release tag, `v1.20.1-3.16.3`.

## Short answer

- Pathfinder's canonical name for the uncontrolled event is **wellspring surge**, not **wild magic surge**.
- **Wellspring magic** is the unstable source/condition; **Wellspring Mage** is the class archetype that uses it; **wellspring surge** is the random event it produces.
- **Wild magic** is also official PF2e wording, but it describes a broader phenomenon of unstable environmental magic. The Mana Wastes' wild-magic rules cause failed spells to produce *wellspring surges*.
- Iron's Spells 'n Spellbooks does **not** build spells by assembling reusable trigger and effect nodes. Its spells are registered Java objects, normally one custom `AbstractSpell` subclass per spell, with shared casting lifecycle hooks and events.
- Iron's closest match to “when this happens, do this” is its event-driven integration and passive-item code. Numeric equipment upgrades modify attributes; individual passives subscribe to game events and run hard-coded conditions/effects. Neither is a player-facing spell-composition graph.

## Pathfinder 2e: exact terminology

### Wellspring magic

**Wellspring magic** is magic that wells up inside or through a caster too strongly to remain under full control. It can replenish expended spell slots, but the same influx can escape as an uncontrolled surge. It can be intrinsic, inherited, granted by another being, or acquired through exposure to a destroyed artifact or powerful magical death. ([*Secrets of Magic*, “Wellspring Magic,” p. 245](https://2e.aonprd.com/Rules.aspx?ID=1576))

This is not a fifth magical tradition. A resulting surge inherits the caster's arcane, divine, occult, or primal tradition trait. ([“Wellspring Surges”](https://2e.aonprd.com/Rules.aspx?ID=1580))

### Wellspring Mage

**Wellspring Mage** is the rare class archetype that formalizes wellspring magic. Archives of Nethys currently labels the archetype and its dedication **Legacy Content**, sourced from *Secrets of Magic*; it is not presented as a renamed Remaster subsystem. ([Wellspring Mage](https://2e.aonprd.com/Archetypes.aspx?ID=104))

Its basic exchange is:

- the character must cast spontaneously from a repertoire;
- they lose one normal spell slot of every rank and one cantrip;
- at initiative for a non-trivial combat, or another GM-selected high-stress moment, they attempt a DC 6 flat check;
- critical success temporarily restores a chosen expended slot; success restores a randomly ranked slot from among the top three available ranks; failure generates a wellspring surge;
- the base feature permits at most two recovered temporary slots per day, while **Wellspring Mage Dedication** removes that daily cap.

The exact durations and forced-failure rule matter: the critical-success slot lasts 1 minute, the success slot lasts 3 rounds, and rolling again while a temporary slot is already present automatically fails. ([Wellspring Magic and Wellspring Mage Dedication](https://2e.aonprd.com/Archetypes.aspx?ID=104))

### Wellspring surge

**Wellspring surge** is the canonical mechanical term for the uncontrolled event. The caster rolls on a d20 table. The surge uses the affected caster's spell DC, originates from that caster, includes the caster in its area, and gains the caster's tradition trait. The results are not uniformly punishments: the table contains damage, hostile arrival, battlefield disruption, conditions, protection, healing, and an immediate extra spell cast. ([*Secrets of Magic*, “Wellspring Surges,” p. 250](https://2e.aonprd.com/Rules.aspx?ID=1580))

### Wild magic

**Wild Magic** is the heading of an environmental rules section in *Impossible Lands*. In the Mana Wastes, local magic is unstable; a caster makes a location-dependent flat check and, on failure, loses the intended spell and creates a **wellspring surge** instead. The rules consistently use *wellspring surge* for the event rather than introducing a separate “wild magic surge” mechanic. ([*Impossible Lands*, “Wild Magic,” p. 232](https://2e.aonprd.com/Rules.aspx?ID=1879))

The text makes the noun distinction explicit elsewhere: spellscar fexts are described as victims of “wild magic or wellspring surges,” and their Unstable Feedback reaction makes a caster undergo a **wellspring surge**. ([Spellscar Fext](https://2e.aonprd.com/Monsters.aspx?ID=2415))

**Terminology conclusion:** use *wellspring* for the unstable source or a route built around it; use *wellspring surge* (or simply *surge*) for the event; use *wild magic* as a descriptive umbrella for unstable ambient magic. “Wild Magic Surge” is not the formal PF2e compound term found in these rules.

## Iron's Spells 'n Spellbooks: actual architecture

### Spells are custom classes, not trigger/effect recipes

Iron's common framework is [`AbstractSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/api/spells/AbstractSpell.java#L60-L173). It supplies shared fields and calculations for mana cost, power, cast time, cooldown, and school, and exposes lifecycle methods for pre-cast, cast ticks, completion, client effects, and the server-side spell effect. The casting pipeline checks conditions, posts pre-cast and on-cast events, deducts mana, invokes the spell's `onCast`, and applies cooldown. ([casting pipeline](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/api/spells/AbstractSpell.java#L270-L400))

The individual effect is authored directly in a subclass. For example, `FireballSpell` declares its school, levels, costs, cast type, and then implements `onCast` by constructing, configuring, and spawning a fireball entity. ([`FireballSpell`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/spells/fire/FireballSpell.java#L25-L93)) The central registry likewise registers concrete objects such as `new FireballSpell()`, `new ShieldSpell()`, and `new SummonVexSpell()`, rather than loading graphs of generic triggers and effects. ([`SpellRegistry`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/api/registry/SpellRegistry.java#L53-L150))

The accurate model is therefore:

```text
registered spell class
  + standardized cast lifecycle/configuration
  + custom imperative effect code
  + event hooks for other systems to inspect or modify the cast
```

### Where the trigger/effect impression does fit

Iron exposes events around casting and outcomes. `SpellOnCastEvent`, for example, carries the spell, school, source, level, and mana cost, and permits listeners to change the level and mana cost before the spell effect executes. ([`SpellOnCastEvent`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/api/events/SpellOnCastEvent.java#L10-L64)) Other first-party event types cover pre-cast cancellation, damage, healing, summoning, teleportation, cooldowns, and spell-level modification.

Its equipment system also exposes numeric attributes such as max mana, mana regeneration, cooldown reduction, cast-time reduction, general spell power/resistance, and per-school power/resistance. ([`AttributeRegistry`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/api/registry/AttributeRegistry.java#L25-L64)) Upgrade-orb definitions map crafted upgrades to those attributes; for example, school-power and cooldown upgrades add percentage modifiers, while mana adds a flat 50. ([`UpgradeOrbTypeRegistry`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/registries/UpgradeOrbTypeRegistry.java#L30-L74))

Triggered passives are implemented with ordinary event listeners. The Lurker Ring subscribes to a living-damage event and, when the attacker is invisible, the ring is equipped, and its cooldown is ready, multiplies that attack's damage. ([`LurkerRing`](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/item/curios/LurkerRing.java#L12-L41)) This is structurally “on event, if conditions, apply effect,” but it remains hard-coded item behavior—not a general, craftable trigger/effect language.

### Player progression

Iron's official progression is an exploration-and-crafting ladder:

1. **Early:** find scrolls and equipment in structures; find or craft a limited beginner spellbook; use the Inscription Table to place scroll spells into it.
2. **Mid:** use the Scroll Forge to make scrolls from ink, paper, and a school focus. Ink sets the rarity and therefore spell level; the focus selects the school. Craft better books, armor, and gather arcane essence/runestones.
3. **Late:** use the Arcane Anvil to level scrolls, apply attribute upgrades to armor and books, and imbue weapons with spells; school armor supports specialization.

Sources: Iron's official [Progression guide](https://iron.wiki/progression/), [Getting Started and workstation guide](https://iron.wiki/), and [Schools guide](https://iron.wiki/schools/).

A found scroll already contains a spell. It can be consumed to cast without mana or cooldown, or inserted into a compatible spellbook; book capacity/rarity constrains what can be installed. ([official Getting Started guide](https://iron.wiki/)) Iron therefore offers a useful **content-acquisition and gear-progression loop**, but not a compositional spell grammar.

### Mana behavior relevant to comparison

Iron uses a player mana pool with built-in regeneration. In the latest 1.20.1 release source, max mana defaults to 100 and the mana-regeneration attribute defaults to 1. Every 10 ticks, the manager adds `max mana × mana-regen attribute × 1% × server multiplier`, clamped at the maximum. ([attributes](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/api/registry/AttributeRegistry.java#L25-L32), [regeneration loop](https://github.com/iron431/irons-spells-n-spellbooks/blob/v1.20.1-3.16.3/src/main/java/io/redspace/ironsspellbooks/capabilities/magic/MagicManager.java#L27-L45)) At untouched defaults, that is 1 mana every half-second, or roughly 50 seconds from empty to full; equipment, upgrades, and server configuration can change it.

## Design takeaway for Vestige

Iron is evidence for separating three concerns:

- a stable cast lifecycle and event vocabulary;
- authored spell behavior;
- orthogonal player/equipment attributes and triggered passives.

It is not evidence that trigger/effect composition already solves player-authored spells. If Vestige wants spells assembled from trigger, targeting, effect, and modifier parts, that would be a new explicit grammar inspired by event systems—not a direct adoption of Iron's spell model.
