# Vestige built-in trait catalog

Status: accepted initial catalog, 2026-09-14.

Vestige traits form an open, namespaced repertoire. The built-in catalog gives Vestige and its integrations a shared vocabulary; it is not an allowlist. Addons and data packs may introduce traits from other namespaces.

With exactly one exception, a trait name has no behavior in the core engine. A spell's trait profile assigns ratings to trait names, while effects, conditions, equipment, paths, and compatibility adapters choose how to interpret them. Missing traits have a rating of zero. The sole exception is `vestige:volatile`, documented below.

## Built-in traits

All identifiers below use the `vestige` namespace.

### Scaling (3)

- `amplify`
- `range`
- `area`

The conventional authored baseline for these traits is `1`. An effect must explicitly read a scaling trait for it to matter. A typical damage effect may multiply its base amount by `amplify`; a projectile may multiply its maximum travel distance by `range`; an explosion may multiply its radius by `area`. The engine does not apply any of those interpretations automatically.

### Special (1)

- `volatile`

`volatile` is the only trait with semantics baked into the spell engine. Its rating creates an inherent chance for the cast to forfeit, even after the caster has identified the spell. No second special trait may be introduced as an implementation convenience: adding one requires an explicit design conversation and decision with the project owner.

The current executable tuning uses a 20% forfeit baseline for an unknown spell, preserving Electroblob's default. Each `volatile` point represents another 5 percentage points of inherent risk. A cast uses the higher of its unknown-spell baseline and inherent volatile risk, capped at 100%. This makes `volatile: 4` a 20% risk whether identified or unknown, while an identified nonvolatile spell has no forfeit risk. These numbers are balance policy and may later become configuration; `volatile` being the sole semantic trait is the architectural rule.

The October 2 [scroll discovery decision](spell-discovery.md#chaos-and-identification) requires unknown volatile spells to be especially likely to produce chaos. The current maximum-of-two-risks formula does not provide that combined danger and must be revised when discovery is implemented; its replacement formula and probabilities remain undecided. Identification requires a successful scroll cast and does not remove volatility. Unknown and identified are the only knowledge states; there is no separate learned state.

### Essence and metaphysical (11)

- `life`
- `mind`
- `spirit`
- `death`
- `dream`
- `memory`
- `emotion`
- `time`
- `space`
- `fate`
- `void`

The former `fundamental` category has been removed. `life`, `mind`, and `spirit` remain useful umbrella vocabulary and now live here; `matter` was removed as too broad to add useful classification.

### Element and substance (17)

- `acid`
- `air`
- `blood`
- `earth`
- `fire`
- `force`
- `ice`
- `light`
- `lightning`
- `metal`
- `plant`
- `poison`
- `shadow`
- `sonic`
- `stone`
- `water`
- `wood`

`cold` was consolidated into `ice`, and `electricity` into `lightning`. The remaining adjacent terms are deliberate: `earth` can describe dirt, sand, and elemental terrain without specifically describing stone, while `plant` can describe living flora without describing wood as a material.

### Realm (3)

- `ender`
- `nether`
- `aether`

Realm traits describe magical association, not a requirement that a cast occur in that dimension. `ender` remains distinct from `space` and `void`; `nether` from `fire` and `unholy`; and `aether` from `air`, `light`, and `holy`.

### Classical school (8)

- `abjuration`
- `conjuration`
- `divination`
- `enchantment`
- `evocation`
- `illusion`
- `necromancy`
- `transmutation`

Schools are composable traits, not mutually exclusive containers. They describe magical technique and are independent of tradition.

### Other semantic traits (7)

- `curse`
- `holy`
- `unholy`
- `draconic`
- `motion`
- `polymorph`
- `teleportation`

`draconic` is the common bridge for dragon-related magic without committing Vestige to one dragon mod's species model. `motion` describes magic concerned with movement and gives Create integration a general semantic hook without turning RPM, stress, machinery, or contraptions into traits. `polymorph` and `teleportation` remain explicit because equipment, paths, resistances, or integrations may reasonably care about those techniques independently of their effects or school.

`fortune` and `misfortune` were consolidated into the broader `fate` trait.

The revised built-in catalog therefore contains **50 traits**.

## Rarity and relative ratings

Spell rarity is separate metadata: common, uncommon, rare, or mythic. Ratings and their proportions provide inputs to authored formulas; their raw magnitudes and totals do not measure spell power. The [balance guide](spell-balance.md) explains equivalent scales and compensated formulas. The [catalog audit](../spell-balance-audit.md) records resolved parameters, spatial coverage, and individual or combined boost responses. Outcomes, costs, timing, and constraints determine gameplay balance within rarity. Profiles are not automatically normalized; fixed additions, thresholds, and volatile's inherent risk have unit-sensitive interpretations. The planned [scroll reconstruction pool](spell-discovery.md#reconstruction-ratings-across-spells) requires every fragment-supplied trait to be present and weights eligible spells by their average base rating across the placed fragments, counting duplicates. Dismantling uses proportions within one spell.

## Deliberate exclusions

Delivery forms and outcomes such as `damage`, `shield`, `projectile`, `beam`, `aura`, `heal`, and `summon` are derived capabilities of the effect graph rather than authored traits. Execution descriptors such as `auditory`, `visual`, `subtle`, `concentrate`, `instant`, and `continuous` belong in cast or effect data when needed.

This does not prevent conditions from asking whether a spell can damage, heal, summon, teleport, or alter blocks. It prevents authors from having to keep a separate hand-written capability list synchronized with the actual effects.

## Native Iron conversions and foreign projection

The September 30 scope decision adds 110 explicit native Iron adaptations, executed by Vestige without Iron installed. They retain exact source IDs as provenance and choose per-spell traits/effects; see [the conversion ledger](iron-spell-conversions.md). The school projection below describes a future foreign-cast adapter, not how native content must be authored.

When Iron itself executes a foreign cast, Iron owns that execution. A future Vestige adapter observes that foreign cast and exposes its provider, exact spell ID, exact school ID, level, cast type, and cast source to the shared trigger and condition systems. Ordinary interoperability does not convert an Iron spell into a Vestige spell and does not require a per-spell catalog.

The exact Iron school remains metadata, not a second trait vocabulary. The adapter maps each school directly into the closest Vestige trait:

| Iron school | Vestige trait |
|---|---|
| Fire | `vestige:fire` |
| Ice | `vestige:ice` |
| Lightning | `vestige:lightning` |
| Holy | `vestige:holy` |
| Blood | `vestige:blood` |
| Ender | `vestige:ender` |
| Evocation | `vestige:evocation` |
| Nature | `vestige:life` |
| Eldritch | `vestige:void` |

These mappings are compatibility policy rather than perfect taxonomic equivalence. In particular, Iron uses Evocation more broadly than the classical definition, Nature contains earth and poison magic as well as living magic, and Eldritch is not universally void magic. The mapping intentionally gives each foreign school one stable Vestige classification that new and addon spells inherit automatically.

Iron's public events make observing and reacting to foreign spell casts relatively direct. The integration can therefore support unknown and addon spells automatically when they use Iron's standard schools and events. The exact Iron school remains queryable as `spell/school` metadata when a rule truly needs it, but ordinary trait rules see only the mapped Vestige trait.

`amplify` can participate in outcome hooks that Iron exposes, such as damage or healing. Iron does not provide one generic hook for changing every spell's reach or spatial extent, so `range` and `area` are descriptive on a foreign cast unless a particular integration feature has a suitable public hook.

Sources: [Iron's school registry](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/SchoolRegistry.java), [official school guide](https://iron.wiki/schools/), [spell registry](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/registry/SpellRegistry.java), and [public spell events](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/java/io/redspace/ironsspellbooks/api/events).
