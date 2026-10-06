# Leyline shaping for spells and magic items

**Native scroll integration implemented October 4, 2026.** The accepted matrix now shapes crafted scrolls and their actual casts. See [playtesting](../leyline-playtesting.md). Material contributions are implemented through [Spellshaping](spellshaping.md); magic gear remains deferred.

**Accepted geometry-shaping baseline, October 4, 2026.** The owner locked in the current [v3 calculator design](leyline-calculator.md) and [numerical rules](leyline-calculator-v3.json). Schools belong to the same flat trait repertoire as elements and other traits. The traits of a crafted output condition geometry response functions that emit **Amplify, Range, Area and Casting Cost** modifiers. Geometry does not require a separate school field or school/element rating increases. Different preferred layouts can conflict, making a mixed output reward a compromise. Current functions, coefficients, equal log-space blending and minor-offset tuning are the accepted implementation baseline. Native scroll application and gameplay verification are implemented; earlier alternatives below retain historical status.

An earlier alternative below gives geometry consistent Directed/Anchored and Unified/Linked meanings across schools. It remains a behavioral-transformation idea for comparison, rather than the selected meaning of each shape. The current discussion can begin with numerical trait shaping without requiring those transformations.

The accepted apparatus inputs and two/four shape cases remain in [Plinth rituals](plinth-rituals.md). The [executable Spellshaping ledger](../spellshaping-recipes.md) supplies the supported material interactions. Native wands/equipment remain deferred.

The accepted [leyline calculator baseline](leyline-calculator.md) and [v3 rules](leyline-calculator-v3.json) implement the owner's factor-to-modifier matrix direction: six independent geometry rows and four columns, including Casting Cost. Each trait supplies per-cell curve parameters; shared functions evaluate them without collapsing all geometry into one trait score. Cost has direct matrix responses plus the shared output tradeoff, and its final factor scales every existing component uniformly. [V2](leyline-calculator-v2.md) retains the superseded single-response model; [v1](leyline-calculator-v1.md) retains the earlier rating/affinity interpretation. The calculator verifies the accepted design, and native scroll application is implemented.

The owner also requires comparable shaping opportunities, especially across the eight schools, and no universally dominant layout. The [October 4 audit](../leyline-balance-audit.md) verifies that bounded baseline domain across all 46 single traits and selected mixtures. School opportunity envelopes and explicit regression guards constrain tuning while preserving distinct preferred layouts; choosing a particular goal can still produce an optimum. Coefficient comparisons do not establish equal gameplay value across different outcomes or spells.

**Accepted balancing intent:** a structure matching the output's type should be largely beneficial, with minor balancing offsets. Specializing the build should give a clear reward. The current numerical responses are the implementation baseline; later tuning must retain school fairness and distinct optima and update the audit evidence.

## Current output model

The inputs remain ingredients/relative recipe order, geometry and slot-local imbuements. Geometry comprises the active layers' **shape**, two independent stage **distances** and two signed **height steps**. Ingredients select the output's base recipe and traits; imbuement acts on the ingredient above it. Trait magnitudes do not form a quality budget.

For an output with Fire and Evocation, apply both traits' shaping functions to the same layout and combine the resulting scaling responses. Fire and Evocation remain traits, with neither made a separate required category. The accepted baseline uses an equal log-space blend of distinct configured descriptive traits, avoiding compounding a bonus merely by adding more tags. Their descriptive ratings are not weights in this geometry blend; discovery probabilities retain their separate slot-counted rating rule.

The accepted Earth function favors compact, aligned, level geometry. Fire favors staggered layers, contrasting spacing and sharper falling/rising height changes. These motifs use the supported regular-ring inputs; arbitrary asymmetric layouts are not currently modeled. Geometry does not modify Volatile failure semantics.

Amplify affects compatible potency consumers, Range affects compatible reach, and Area affects authored spatial extent. A response emits those scaling modifiers directly, so there is no additional Fire/Evocation affinity factor to apply again to the same damage. Future gear uses the channels its behavior actually supports.

The lock-in retains the [Amplify applicability clarification](leyline-calculator.md#actual-consumers-and-remaining-decisions): current Divination spells do not explicitly read Amplify, so the pyramid's displayed Amplify factor gives them no extra sensing behavior. Several Nature spells already use Amplify for healing or damage; the henge modifier still depends on the actual output's full trait combination. New Divination potency consumers remain separate authoring work.

**Casting Cost** is one factor applied separately to every existing component. The owner selected nearest-whole final amounts on October 4: compose all modifiers, then round once in the amount's declared unit, with nonnegative exact halves rounding up. At ×1.2, 20 mana becomes 24, 2 hearts become 2 and 40 casting ticks become 48. A mixed cost scales and rounds each component without summing unlike units. Zero or absent costs remain absent; there is no additive time charge. Fractional multipliers and intermediate expressions retain precision. This final-amount rule also applies to supported numerical outcomes when implemented; native runtime consumers are unchanged by the calculator update.

The calculator's proposed payment heuristic increases cost for stronger scaling and allows modest reductions for weaker scaling. The owner subsequently clarified that the emitted cost factor still applies when a spell does not use Range, Area or Amplify. Unsupported scaling simply has no consumer; cost is not dynamically reweighted per spell. Balance still requires checking actual outcomes and payment, including free/passive items and geometry/imbuement composition.

The current affordability response moderates **positive combined cost logs by 0.20** and preserves negative combined logs. Bounded sub-neutral Amplify, Range and Area factors are raised to **0.30**, keeping full positive gains while limiting losses to approximately 8.3% Amplify / 10.1% Range and Area. All traits use these shared continuous monotone responses. School Cost cells were refitted offline because smaller outcome penalties also reduce payment refunds; outcome cells and their exact maxima remain unchanged. The [audit](../leyline-balance-audit.md) rejects outcome-goal winners whose cost factor exceeds every individual outcome factor or whose collateral losses exceed 11%. These numerical guards do not establish actual spell value or guarantee that unused channels supply a benefit.

The requested **Use best layout** button searches the current four/eight-slot arrangement space for Highest Amplify, Longest Range, Largest Area or Lowest Casting Cost. It applies the winning shape, offsets and heights while retaining the selected example traits and recipe capacity. This is an exhaustive optimization of the accepted functions within the baseline bounds, not a gameplay guarantee.

## All input factors

| Factor | Information retained | Role |
| --- | --- | --- |
| Ingredients | The offered item in every used seat | Select the base recipe and output |
| Relative ingredient positions | Circular sequence and authored relationships, preserving quarter-turn equivalence | Match the recipe and identify ingredient roles |
| Shape | Cross or Diagonal independently for each active layer | Input to trait-conditioned scaling functions |
| Distance | Center-to-inner spacing and inner-to-outer spacing separately | Independent inputs to those functions |
| Height ratio | The two signed height steps between stages | Independent signed inputs to those functions |
| Imbuement | Installed material paired with that seat's offering and recipe role | Supply local effect/trait/cost contributions |

Capacity determines which seats are available, rather than output quality. Four-slot recipes ignore all outer information. Beam lengths and neighboring-node spacing are derived geometry, not additional independent power awards. Absolute world elevation has no effect on the height profile. The accepted v3 convention measures horizontal block-center distance, preserving signed height separately.

## Historical v1 discussion: schools and raw-trait shaping

**Superseded interpretation.** The following school/element rating adjustments, affinity expressions and separate payment/time suggestions predate the October 4 correction above. They remain historical proposals, not the current output model or a requirement to change spell classifications. The current calculator supports any descriptive trait combination without requiring a school.

Vestige has eight classical school traits: **Abjuration, Conjuration, Divination, Enchantment, Evocation, Illusion, Necromancy and Transmutation**. They are composable, rather than one compulsory school field. A direct catalog check found 18 of 214 definitions without any of these eight traits and 19 with two. Examples without a classical school include Heal, Teleport and Hydraulic Push; Wall of Ice combines Abjuration and Conjuration. This is current authoring data, not a decision to assign or remove schools.

The owner now intends crafted spells and future magical items to have a school. Requiring at least one classical school is compatible with retaining hybrid school traits; it does not require a new mandatory single-school field. The currently unclassified spells need an authoring review before that requirement is enforced. Classifications have not been changed in this discussion. A school trait must not automatically add a behavior to the core engine; [the trait catalog](trait-catalog.md) reserves inherent semantics for Volatile. Authored shaping functions and explicit effect expressions can interpret school traits without changing that rule.

Each distinct school or element can receive its own trait modifier. An Evocation/Fire spell may therefore benefit from both functions, exactly as the owner describes. The outcome expression determines how those changes combine. It must not accidentally apply the same contribution twice through both a direct trait read and an additional derived Amplify adjustment.

## School and element response functions

The proposed geometry input is:

```text
layout = (inner shape, active outer shape, d1, active d2, dy1, active dy2)
trait multiplier = response for that trait(layout)
```

The outer values are absent for a four-slot operation, not neutral numeric placeholders that an outer response can still inspect. `d1` is Spellstone-to-inner distance and `d2` is inner-to-outer distance. `dy1 = innerY - centerY` and `dy2 = outerY - innerY`; diagonal distance counting is still undecided. Orientation and absolute world elevation remain irrelevant.

Evocation and Fire have separate functions. For example, Evocation could favor a moderately spread, raised layout while Fire favors a compact layout. Their favored distances need not coincide. A Fire/Evocation spell then offers a choice of emphasizing one trait or building between their preferred layouts. These preferences are illustrative, not selected mappings. Illusion, Divination, Water and other traits can have their own authored preferences without introducing one bespoke runtime implementation per spell.

Use a few understandable influences per function: a shape preference, a smooth response around a preferred spacing or height, and only a small number of interactions where needed. A bounded response can reward an intermediate distance without making endlessly expanding the apparatus optimal. Clamp the function's emitted multiplier to its declared finite bounds, rather than changing the core trait-resolution mathematics. Exact bounds and distance units remain open.

Geometry should usually multiply traits already present. Multiplying an absent trait leaves it zero; adding a new element, effect or school is a separate explicitly authored operation. Several school/element functions can coexist, and local material interactions can contribute through the same existing `ADD`/`MULTIPLY` operations. This introduces no independent quality score or raw-trait power ladder.

## Current scaling and actual outcome consumers

The built-in scaling traits are **Amplify**, **Range** and **Area**. Amplify conventionally scales primary potency, including authored damage or healing. Range scales supported travel or target-selection distance. Area scales supported spatial extent. Each must be read by an effect expression; none acts automatically. Mana is a typed cost, not a scaling trait. Other current costs are charge time, cooldown, health, hunger and material consumption or durability. Lifetime, target count, pulse frequency and similar values are parameters of individual plans, with no universal Duration trait.

At the time of this earlier proposal, [Fireball](../../src/main/resources/data/vestige/runtime_spells/fireball.json) already had Fire 4, Evocation 4, Amplify 1, Range 1 and Area 1. Its damage is `8 * amplify`, travel is `32 * range`, blast radius is `3 * area`, and mana cost is 28. Increasing descriptive Fire/Evocation ratings alone would not strengthen its blast. The accepted v3 implementation resolves that gap by leaving descriptive ratings intact and emitting explicit scaling-axis multipliers plus cost; crafted casts apply those stored multipliers at existing consumers.

A candidate expression for this equal Fire/Evocation profile is:

```text
damage = 8 * amplify * ((fire + evocation) / 8)
```

Here the denominator is the authored unmodified Fire/Evocation total, not the current resolved total. It is a constant coefficient and can be expressed with the existing sum/product primitives. At the original values it preserves damage 8. Fire ×1.4 alone gives damage 9.6; Fire ×1.4 and Evocation ×1.4 give 11.2. A separate Amplify modifier still scales the primary output as explicitly authored.

This weighted combination is a recommendation for discussion, not a required formula for every school or spell. It lets distinct trait bonuses both contribute without automatically multiplying them together into 1.96× output. Explicitly multiplying school and element factors is also possible, but must be balanced as that stronger compound response. Do not apply both interpretations to the same contribution.

Authored coefficients preserve relative trait units. Rescaling the base profile and compensating its expression should preserve both the base outcome and the relative response to multiplicative shaping. A raw Fire rating of 100 is not stronger merely because its number is larger. Missing traits, hybrid schools and outcomes that use different domains need explicit weights and valid consumers; do not divide by an absent trait or invent a second school solely to complete a formula.

Reusable effect families can supply these expressions where their behavior fits. Damage, healing, protection, information, summons and illusions need suitable potency consumers and limits. Raising Illusion could strengthen a declared decoy lifetime or another supported property; it cannot silently grant unspecified copies or new abilities. The same requirement applies to a future passive or reactive item.

## Tradeoffs and player choices

Functions may produce positive and negative trait adjustments and separately declared cost changes. A stronger outcome could require more mana, longer charging, longer recovery, reduced reach or a smaller area. Each selected downside must affect a parameter the actual output uses. Reducing Water on a spell with no Water, or adding a mana multiplier to a zero-mana activation, does not pay for a benefit.

Do not assume +40% to a trait requires exactly +40% mana. The expression determines the real leverage: in the candidate Fireball expression, Fire ×1.4 alone increases direct per-recipient damage by 20%. Area can change potential contacts; duration can change pulse totals. Evaluate those actual outcomes together with costs, timing and constraints, including combined geometry and imbuement contributions. Balance remains outcome-based rather than a sum of trait points.

The intended choice is among useful specializations. A particular spell may have a best layout for damage, but that layout should not necessarily also minimize payment and maximize reach, coverage and responsiveness. Different schools, elements, output uses and local imbuements should support different choices. Curves and limits should be legible enough to learn; arbitrary hidden interactions would turn the apparatus into guesswork.

Before consuming offerings, a proposed inspection should show the matched base recipe, affected traits, supported resolved outcome changes and actual cost changes, explaining which layout or imbuement supplied each. An unsupported trait read must not be sold as a damage or healing improvement. Neutral baseline behavior and what information is initially hidden remain design choices.

## Earlier alternative using common geometry meanings

These mappings were proposed before the owner's school/element response-function direction. They remain an alternative for possible behavioral transformations, rather than selected geometry rules or approval of the numerical examples in the geometry document.

| Input | Candidate meaning | Required boundary |
| --- | --- | --- |
| Inner Cross | **Directed**: project the effect toward a chosen recipient or destination | Requires compatible targeting and delivery; preserves consent, hostility and world permissions |
| Inner Diagonal | **Anchored**: bind the effect to a creature, object or location | Requires an owned field, binding or manifestation; finite lifetime and cleanup |
| Outer Cross | **Unified**: reinforce one coherent expression | Does not duplicate all existing effects or collapse intrinsic base targeting without an authored transformation |
| Outer Diagonal | **Linked**: distribute an expression across several compatible recipients or points | Shares a calibrated outcome budget; extra contacts and reaction opportunities are reviewed |
| First distance | **Reach**: how far from the source an eligible expression can act | Extends a real selection/delivery consumer; unsupported self-only effects remain unchanged or need another compatible operation |
| Second distance | **Breadth**: eligible coverage or separation between linked recipients | Only with an active outer layer; area and recipient count remain separate consumers |
| Center raised relative to inner | Favor **intensity** over **persistence** | Increased immediate output needs a real reduction or cost; a nonexistent duration is not payment |
| Center lowered relative to inner | Favor **persistence** over **intensity** | Longer effects remain finite, with pulse totals and control uptime accounted for |
| Inner raised relative to outer | Favor **efficiency** with slower activation/recovery | Name the real resource cost and time/cooldown being exchanged |
| Inner lowered relative to outer | Favor **responsiveness** with greater payment | Resource-free or passive outputs need a real supported cost rather than a fictional mana penalty |

The four shapes then read as **Directed Unified**, **Directed Linked**, **Anchored Unified** and **Anchored Linked**. An inner-only recipe selects Directed or Anchored; it has no outer Unified/Linked contribution. These are explanatory candidate names, not registered traits or finalized item adjectives.

The meanings stay recognizable across schools. Creating a fire zone from a projectile, or making a single-target buff support an aura, is a behavioral transformation that needs a compatible shared plan. It cannot be promised through a range or area multiplier alone. Some outputs may support only a subset of forms. Preview that compatibility before consuming ingredients; distinguish a neutral baseline from an expensive named shaping that does nothing.

## Earlier school expression examples

The following are examples of the same proposed forms expressed through different magic. They do not establish automatic conversion rules for every spell in a school.

| School | Directed or Anchored expression | Unified or Linked expression |
| --- | --- | --- |
| Abjuration | A ward on a selected ally, or a ward bound to a bearer/place | One concentrated protection, or protection shared among eligible allies |
| Conjuration | A manifestation at a destination, or one maintained around an anchor | One sturdier summon, or a bounded group with reduced individual budgets |
| Divination | Inspect a selected subject, or maintain a local sensor | A narrow detailed reading, or a broader reading with less information per subject |
| Enchantment | Influence an eligible subject, or sustain an influence around an anchor | One stronger bounded influence, or milder influence across a limited group |
| Evocation | A traveling blast, or an anchored hazardous field | One concentrated release, or several smaller releases with a reviewed total budget |
| Illusion | A decoy at a selected point, or a maintained scene around an anchor | One longer-lived decoy, or several briefer copies |
| Necromancy | A targeted drain, or a bound curse/spirit | One drain connection, or weaker connections with a capped total transfer |
| Transmutation | Alter a selected creature/object, or maintain an enhancement on a bearer/place | One pronounced change, or modest changes across several eligible subjects |

Information depth, hard control, apparent copies, physical transformations and summons require their own bounds. They are not interchangeable numerical damage amounts. “Anchored” describes ownership/location; it does not automatically make a hostile effect permanent or make an item always active.

## Shared output model

Crafting resolves the base recipe plus geometry and material contributions into **stored shaping choices**. A later cast or item activation uses those choices, rather than rescanning the crafting installation. The owner can dismantle or move the apparatus without changing already crafted objects.

The future recipe system can select an output kind as well as a base definition. A scroll stores one shaped spell; a wand could store an activation using that spell; a ring could hold a reactive ward; another item could hold a passive attribute or trigger. The shaping rule names the part it changes: activation, trigger condition, effect, lifetime or payment. A passive attribute item has no implied projectile range or casting time.

For example, an Abjuration ward could appear on a scroll or a future ring. Directed/Anchored shapes choose supported application forms. First distance could extend a selected ally's application distance or a ring's declared protective trigger reach. Persistence could extend a finite ward lifetime. A quick-recovery variation must pay through a declared mana, health, hunger, material/durability, charge-time or cooldown change. The item-specific activation/payment rules remain to be designed; the earlier wand idea's zero mana must not be silently overwritten by a generic mana exchange.

This does not require copying a spell definition for every item or introducing one class for every school. Reuse the existing trait operations and composed effects. New shared consumers, persistent item representation and gear lifecycle support are implementation work, not claims of current capability.

## Balance and initial implementation

Resolve `(base + sum of additions) * product of multipliers` through the existing trait model, preserving relative units. Explicit effects consume those traits. Lifetimes, timing, information and targeting changes require supported expressions or transformations; there is no universal duration trait. Cost changes use the existing full typed vocabulary, including material consumption/durability.

Distribution splits or reprices real outcomes. Four projectiles should not grant four copies of the full original damage; four summons must not inherit four complete summon budgets. Longer-lived damage pulses and reactive items need total contact/charge ceilings. Protection, divination and control need limits suited to their behavior. Apply material and geometry contributions once, then review their combined leverage.

Start by selecting a small set of school/element response functions and explicit consumers on representative spells, with supported trait and payment exchanges. Review the school classifications, then test a damaging spell, a ward, a summon, a sensor, a utility spell and a hybrid. Inspect their resolved outcomes across permitted geometry, including combined imbuements, before expanding the mappings. Finite anchored/linked transformations from the earlier alternative can be explored separately. Spellshaping should give useful choices; it need not turn every geometry factor into a numerical change on every output.

The local 3D schematic shows node positions, stage heights and measured distances only. It does not simulate these proposed cast outcomes or use actual Minecraft apparatus assets. No gameplay, spell classifications, output recipes or installed jar changed with this proposal.
