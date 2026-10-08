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
The sole trait with inherent engine semantics. Its rating expresses a spell's unavoidable forfeit risk, which remains even after the spell is identified; adding any other semantic trait requires an explicit design decision with the project owner.
_Avoid_: Wild, unstable

**Forfeit**:
A failed cast replaced by a randomly selected chaotic outcome. Unknown spells and volatile spells each carry risk; an unknown volatile spell is especially prone to forfeiting.
_Avoid_: Fizzle, ordinary cast failure

**Scroll**:
A single-use item containing one spell, available to cast whether or not the caster has identified it. It can instead be dismantled into trait fragments.

**Scroll Fragment**:
A piece of a dismantled scroll associated with one of its spell's traits. Placed fragments can be reconstructed into a scroll whose spell contains all their traits.

**Unknown Spell**:
A spell the player has not yet identified. Attempting to cast it carries additional chaos risk.

**Identified Spell**:
A spell the player has successfully cast from a scroll, removing its unknown-spell chaos risk while retaining inherent volatility. This is the sole known state; casting through wands or other sources does not identify it.
_Avoid_: Learned state, unlocked tier

**Scroll Reconstruction**:
The creation of a random spell scroll from trait fragments, requiring every supplied trait and favoring spells with a higher average rating across the placed fragments.

**Spell Discovery**:
The reconstruction of at least four placed trait fragments into a random spell scroll containing all their traits. Producing a scroll does not identify its spell for the player.

**Spell Crafting**:
The construction or augmentation of spells into scrolls. It is a distinct operation from random discovery, even if both use a shared station system.

**Reference Scroll**:
A scroll placed on the Spellstone to select a spell's crafting puzzle. It remains intact while the player investigates, reproduces or fails that recipe.

**Ritual Arrangement**:
The placement of ingredients in relative Plinth recipe slots. Turning the whole arrangement retains its relationships; reflection reverses its order.

**Ritual Failure**:
A completed incorrect arrangement that provides placement clues and may explode according to the reference spell's knowledge and volatility risk. An explosion destroys participating offerings and damages nearby creatures while preserving terrain and the reference scroll.

**Equipment Crafting**:
The construction of native magical gear through authored Spellstone/Plinth recipes. Wardweave and Cinderweave robes use eight offerings, including four matching-color wool blocks, and share spells' trait/variable resolution for their abilities. Other clothing packages remain proposals.

**Staff**:
A durable collection of spells that share one permanent trait. Its two/four/six menu slots accept only scrolls identified by the inserting player and matching that trait. Each removable scroll retains its own shaping; taking it off returns that stored variant. Casting pays the selected spell's ordinary costs plus 1/2/3/4 wear for native Common/Uncommon/Rare/Mythic rarity, including paid chaos. Default durability is 40/80/120 by slot capacity; selection never identifies spells.

**Mundane Staff**:
A physical weapon crafted from two matching vanilla shaft materials. It has the wooden-pickaxe combat profile, spends one durability on each successful left-click hit and uses a held 40% frontal guard after the normal five-tick raise delay on right-click. Every guarded hit spends one Staff durability; the guard has none of a shield's knockback, axe disabling or full-block behavior. It has no spell runtime or innate magical effect. A mundane Staff is intended to become the Shaft ingredient of a future magical Staff construction.

**Staff Affinity**:
The single descriptive trait a staff requires of its bound spells, excluding Amplify, Range and Area. Additional traits are allowed; the affinity is not a mutually exclusive school or a spell-power rank.

**Runic Circle**:
A structure for spell discovery, crafting and attunement, centered on a Spellstone and four inner Plinth slots, with up to four outer slots. Its active layout includes distances, height differences and connections between nodes.
_Usage_: Internal code and documentation only; never surface this term in UI, tooltips, messages or recipe viewers.
_Avoid_: Crafting Structure, Leyline Structure

**Spellstone**:
The central control and item surface of a Runic Circle, holding a retained reference scroll and collectible output. Its role is distinct from the surrounding ingredient slots.
_Avoid_: Center Stone

**Cosmetic Apparatus Finish**:
One of 36 stone/masonry stair materials used for Plinth or Spellstone construction and appearance. It never supplies an imbuement, shaping modifier or attunement identity component. All finishes have matching full blocks and slabs.

**Plinth**:
A leyline node with an offering surface and a separately embedded imbuement material. The same kind of Plinth can occupy either ritual layer and can form part of a structure without holding an offering.
Vertically stacked Plinths connect into a column. Only the exposed cap is an active ritual surface; supporting segments retain contents without contributing additional nodes or modifiers.
_Avoid_: Stone Pedestal, Runic Pedestal, advanced pedestal

**Ritual Layer**:
A group of four relative Plinth positions around a Spellstone. The inner layer provides four recipe slots; the outer layer supplies additional slots only when the operation uses it.
_Avoid_: Apparatus quality tier

**Ritual Shape**:
The Cross or Diagonal positioning of each active Plinth layer. An inner-only ritual has two shape choices; a ritual using both layers has four combinations.

**Ritual Distance**:
The spacing between successive stages: Spellstone to inner layer, then inner layer to outer layer when active. The two spacings are separate influences on Spellshaping.

**Height Ratio**:
The relative heights of Spellstone, inner layer and active outer layer, expressed through their two signed height steps. It describes a build's height profile independently of its absolute elevation in the world.

**Offering**:
The ingredient placed on a Plinth's top surface for a ritual. It is separate from the Plinth's installed material.

**Imbuement**:
A material embedded in a Plinth, intended to alter its offering through an authored material/ingredient interaction. Sockets persist and render; supported offering/material pairs select native Spellshaping.
_Avoid_: Slot Base, foundation bonus

**Spellshaping**:
The modification of a base spell through resolved traits, composed effects and typed costs, preserving its identity. Local offering–imbuement pairs select automatic rules; complete compounds replace their constituent contributions. Secondary plans have shared finite budgets. Only implemented rules appear in the executable ledger.

**Adjustment Adjective**:
A reusable italicized word describing an authored magical adjustment. Its meaning is shared vocabulary; each spell or item explicitly determines its compatible effects and trade-offs.

**Combined Adjective**:
One italicized word naming a particular combination of adjustments and their degrees. It describes their composed result without replacing their effects; an actual Spellshaping compound can instead replace its participating contributions.

**Leyline Shaping**:
The contribution of active ritual geometry to Amplify, Range, Area and Casting Cost, conditioned by the output's descriptive traits. Outcome modifiers affect only properties the output explicitly uses; Casting Cost scales its existing payment components independently.

**Nature Focus**:
A grouping of the existing Life, Plant and Wood traits used to describe grove or henge-oriented shaping. It is not an additional trait or a fixed bonus for every nature-themed spell.

**Crafting Pattern**:
A recipe blueprint describing ingredient roles and the properties or effects they contribute. Iron's patterns provide inspiration rather than a required assembly system.

**Crafting Part**:
An ingredient role in a crafting recipe that accepts particular materials and contributes selected properties or effects.

**Crafting Material**:
An ingredient identity associated with distinct properties, effects and crafting roles. The recipe determines which contributions are used.

**Material Quality**:
Iron's measure of material-based bonus scaling, retained as source reference. Vestige does not use it to rank materials or multiply crafted effects.

**Ingredient Alternative**:
An additional ingredient accepted for a recipe's construction role. Acceptance does not make its material properties identical to those of another accepted ingredient.

**Optional Integration**:
Support for another mod's items or behavior when that mod is present. Vestige's core spells, crafting and progression remain available without it.

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
Time, recovery or a resource required by a cast. Cost components can include casting time, cooldown, material consumption or durability, mana, health and hunger.

**Casting Cost Modifier**:
A shared multiplier applied separately to every existing component of a cast's cost. It preserves each component's units and leaves absent costs absent.

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


**Attunement Shard**:
A six-offering ritual component on eight active Plinths, retaining a versioned physical blueprint and its full reproducible key. Its fixed recipe is 2 Amethyst Shards, 1 Echo Shard, 1 Iron Ingot, 1 Diamond and 1 Lapis Lazuli. It records both active layers, the two empty offering positions and local offering–imbuement pairings, canonicalized under whole quarter-turns. Its short display prefix is not the identity. Standing Stones and planned attuned devices inherit this key.

**Crane Bag**:
A shared Bundle whose full attunement signature identifies one limited pool of mixed items, accessible through every matching Crane Bag. Matching bags share contents and the same capacity rather than each adding more storage.
_Avoid_: Linked Bundle, Kindred Scrip, Echo Pouch

**Whispering Shell**:
A planned communication item whose attunement signature selects the channel shared with matching Whispering Shells.
_Avoid_: Echo Shell, Macalla Shell, Rune Shell

**Homebound Eye**:
A reusable handheld item that teleports its user back to its crafting Spellstone when right-clicked. Each return wears its durability; the imbuement in the Spider Eye's Plinth selects a stored health, hunger, mana or experience payment with reduced wear. An empty selector gives durability-only payment.
Its accepted recipe is Attunement Shard + Spider Eye + Ender Pearl + Flint. The selected appearance is a chipped grey flint talisman with an Ender Pearl teal-green eye recess and a red center.
_Avoid_: Hearth Charm, Homeward Stone, Recall Stone
