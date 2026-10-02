# Vestige Magic

Vestige models spells as magical definitions that are resolved through a caster, an item, and the surrounding world.

## Language

**Spell**:
An immutable magical definition composed of rarity, traditions, a trait profile, costs, a trigger, and effects.

**Spell Rarity**:
A spell's common, uncommon, rare, or mythic classification, expressing its expected overall magical impact and eventual availability. Spells of the same rarity should offer comparable value with their costs and constraints considered.
_Avoid_: Legendary, spell level, tier

**Trait Proportions**:
The relative ratings within a spell's trait profile. Their expression through effect formulas and modifiers determines outcomes; a larger raw rating or total does not itself indicate a stronger spell.

**Boost Leverage**:
The relative change in a spell's resolved outcome parameters when a trait modifier or combination of modifiers is applied.

**Tradition**:
A recognized way of accessing magic. The four traditions are arcane, primal, divine, and occult, and a spell can belong to more than one.

**Trait**:
A namespaced word in the shared magical repertoire, such as fire, evocation, or amplify. Except for volatile, the core engine assigns no behavior to a trait name; effects, conditions, equipment, and adapters decide how to interpret its rating.

**Volatile**:
The sole trait with inherent engine semantics. Its rating expresses a spell's unavoidable forfeit risk, which remains even after the spell is discovered; adding any other semantic trait requires an explicit design decision with the project owner.
_Avoid_: Wild, unstable

**Forfeit**:
A failed cast replaced by a randomly selected chaotic outcome. Undiscovered spells have a baseline forfeit risk, while a volatile spell carries its own inherent risk whether discovered or not.
_Avoid_: Fizzle, ordinary cast failure

**Amplify**:
The general potency scaling trait. Amplify represents how strongly a spell expresses its primary outcome. Its authored baseline is 1, and each effect explicitly defines which of its values respond to amplification. Amplify does not implicitly change range, duration, area, target count, cast time, cooldown, or cost.
_Avoid_: Magnitude, power level

**Range**:
The scaling trait conventionally used by effects to determine how far a spell can reach, travel, or select a target. Its authored baseline is 1. The core engine does not apply range automatically.
_Avoid_: Reach, distance

**Area**:
The scaling trait conventionally used by effects to determine the spatial extent of a spell's outcome. Its authored baseline is 1. The core engine does not apply area automatically.
_Avoid_: Radius, width

**School Trait**:
One of the eight iconic magical techniques: abjuration, conjuration, divination, enchantment, evocation, illusion, necromancy, or transmutation. School traits describe how a spell works and remain independent of its tradition and domain. They are composable; a spell can express more than one school.
_Avoid_: Tradition, domain

**Realm Trait**:
A trait associating magic with a particular plane or dimension. The initial realm traits are ender, nether, and aether. A realm trait is distinct from an element, metaphysical concept, or school even when their themes overlap.
_Avoid_: Dimension requirement

**Capability**:
A fact derived from a spell's resolved effects, such as being able to damage, heal, summon, teleport, alter blocks, or apply a status. Capabilities can be queried by conditions, but are not hand-authored weighted traits.

**Adapter Metadata**:
Exact namespaced information retained from another mod, such as an Iron's Spells school and cast type, a Kithkyn kind, or Create machine state. Adapters can project metadata into Vestige traits and condition values without replacing or losing the original identity.

**Foreign Cast**:
A cast executed and owned by another mod but described to Vestige so that triggers, conditions, traits, bonuses, and resistances can interact with it. A foreign cast is not converted into a Vestige spell.

**Trait Projection**:
An adapter's mapping from a foreign classification into Vestige traits. The exact foreign identifier remains adapter metadata for inspection and precise conditions, but is not added to the trait profile. A projection expresses intended interoperability, not perfect taxonomic equivalence.

**Trait Profile**:
The collection of trait ratings associated with a spell or cast. A spell has a base trait profile; a cast has a resolved trait profile after applicable influences are incorporated.
_Avoid_: Domain list, school list

**Cast**:
A single runtime attempt to activate and resolve a spell in a particular context. A cast does not change the spell definition from which it originates.

**Trigger**:
An event pattern that can begin a cast, such as interaction, attack, jumping, summoning, healing, equipping, or breaking a block.

**Condition**:
A predicate over the cast context that must be satisfied for a trigger or effect to proceed.

**Condition Value**:
A named fact exposed by the cast context for conditions to inspect, such as an actor's health, a source item's name, an event's damage, or a spell's resolved trait rating.

**Effect**:
A magical outcome produced during a cast. An effect can use values from the resolved trait profile and can be guarded by conditions.

**Cost**:
Time or a resource required to complete a cast. The initial cost kinds are time, material, mana, health, and hunger.

**Cast Context**:
The caster, triggering event, source item, targets, world state, and other situational facts available while a cast is evaluated.

**Causal Chain**:
The lineage beginning with one root event and containing every cast, effect, and resulting event caused by it, including indirect consequences.

**Causal Loop**:
A sequence of trigger activations that repeats within one branch of a causal chain, including loops that pass through multiple different spells or effects.

**Cast Mode**:
An alternate cost and effect plan belonging to the same spell, such as a quick activation or a longer charge.

**Cast Session**:
The temporary state of an unfinished cast, including its stage, selected subjects, and stored values. A recast can continue the same session rather than create a new spell identity.

**Binding**:
Temporary event-driven magical behavior attached to a subject, with its own lifetime, charges, and replacement rules.

**Manifestation**:
Persistent magical state created by a cast, such as a summon, barrier, lock, aura, or portal. It retains its source, ownership, and magical identity until it expires, is destroyed, or is dispelled.

**Target Specification**:
The selection and validation rules for the subjects of an effect, including what happens when no suitable subject exists.

**Resolution Phase**:
A point in an event's lifecycle at which magic can observe or alter its outcome. A calculating outcome is still changeable; a committed outcome has already taken effect.

**Source Provenance**:
The exact upstream spell and school identifiers, revision, and cast metadata retained by a native adaptation. Provenance identifies the behavior source without requiring its original mod to execute the native spell.

**Native Adaptation**:
An independently authored Vestige spell that preserves a source spell’s recognizable behavior through reusable native effects, while documenting intentional mechanic and presentation differences.

**Target Anchor**:
A subject captured in a cast session for a later effect or recast, such as a portal endpoint or telekinetically held creature.
