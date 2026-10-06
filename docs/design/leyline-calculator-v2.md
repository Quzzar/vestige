# Historical leyline calculator v2

**Superseded October 4, 2026 by the owner's factor-to-modifier matrix direction.** This version reduced all geometry to one response per trait before distributing it across output axes. The active [calculator design](leyline-calculator.md) and [v3 rules](leyline-calculator-v3.json) keep factor/axis cells independent, including a direct Casting Cost column. The v2 coefficients below remain historical proposals; their numerical outcomes are not preserved by v3.

# Leyline structure response calculator

**Version 2 design proposal, October 4, 2026.** The owner clarified the output model: schools are ordinary traits, and layout should change **Amplify, Range, Area and Casting Cost** according to the traits of the crafted output. School and element ratings are not separate mandatory inputs. A single Casting Cost multiplier scales each existing cost component. These meanings are accepted; the functions, coefficients, equal-weight combination and motif assignments below are proposals for review. Minecraft gameplay, spell definitions and the installed jar are unchanged.

**Application and optimizer clarification:** the owner confirmed that shaping emits all four modifiers. Effects use the axes they read; absent Range/Area/potency consumers simply have no corresponding outcome change. **Casting Cost still applies independently** and is not recalculated according to supported outcome axes. The owner's requested best-layout button now searches the permitted discrete layouts and applies a result for a chosen objective.

Exact candidate coefficients are in [v2 rules](leyline-calculator-v2.json). [V1](leyline-calculator-v1.md) and [its rules](leyline-calculator-v1.json) preserve the superseded rating/affinity model. [Plinth rituals](plinth-rituals.md) records the apparatus direction; [output shaping](leyline-output-shaping.md) records the owner clarification and compatible outcome requirements.

## Reading and adjusting the calculator

Use **Set layout** for four/eight active slots, Cross/Diagonal shapes, offsets and signed height steps. Both grid offsets locate a layer from the Spellstone. The outer stage gap used by the functions is measured from its nearest inner node, rather than from the Spellstone. Inner height is relative to the Spellstone; outer height is relative to the inner layer. Positive means higher. Dragging the diagram or using Rotate view changes only the camera.

Use **Trait on the spell or item → Include trait** in **Crafting impact** to describe an example containing any combination of the 46 descriptive traits with candidate responses. There is no required school, associated element or rating input. Each selected trait can be removed. Evocation, Fire, Earth, Illusion, Mind and the other entries use the same selection mechanism. Amplify, Range and Area are the output scaling axes; Volatile's failure semantics are not changed by geometry.

The four-row table updates immediately. A multiplier changes that output's existing scaling value or cost. Selecting Fire and Evocation asks how this layout shapes an output bearing both traits; it does not increase their descriptive ratings or add those traits to a recipe. The actual recipe remains responsible for determining which traits its output has.

Expand **Base casting costs → crafted costs** to enter mana, health, hunger, casting time, cooldown, material consumption or durability. Every entered component uses the same Casting Cost factor. Zero represents an absent component in this example and remains zero. Preview values are exact before integer-cost rounding. The table is an example cost bundle, not an imported item or spell recipe.

**Compare single-trait outputs** shows all 46 candidates for the same layout. Each row asks what the four modifiers would be if that trait alone were present. Rows do not add traits to the selected output.

## Finding an optimal layout

Choose **Optimize for**: Highest Amplify, Longest Range, Largest Area or Lowest Casting Cost. Then **Use best layout** searches the allowed placements, applies the winner, and updates the diagram, shapes, offsets and heights. There is no single layout that is best for all four objectives; the chosen goal supplies the meaning of optimal.

The recipe's four/eight-slot capacity stays fixed. Four-slot search evaluates 90 inner arrangements, omitting every outer input. Eight-slot search evaluates 11,745 valid arrangements across all four shape combinations, every allowed grid offset and the two independent −4…+4 height steps. Current trait selection is retained. Cost minimization compares the cost multiplier; absent components still remain absent.

The search evaluates the same functions as the preview. It is exhaustive within this prototype's bounds and regular-ring geometry, not a local guess or a claim about unrestricted world builds. Exact ties prefer a smaller horizontal footprint, then a smaller vertical span, smaller inner radius and smaller absolute height steps, followed by deterministic shape/height ordering. These tie preferences are proposal conveniences, not extra shaping bonuses.

Edits to geometry, traits, costs or goal invalidate the prior result. An in-progress search is discarded if those inputs change, preventing it from overwriting a newer choice. Camera motion leaves the search objective unchanged. Existing saved states default to Amplify; future interactions preserve the selected goal. Optimizer results remain local derived state and do not install a game feature.

## Accepted factors and proposal geometry

Ingredients and their relative positions select a recipe. Embedded imbuements supply local contributions according to the offering and its role. Geometry supplies global shaping. This calculator isolates geometry; material interactions and recipe matching are not simulated.

Geometry has three factor families, each retaining two stages where active:

- **Shape:** Cross/Diagonal independently for inner and outer layers.
- **Distance:** Spellstone-to-inner and inner-to-outer spacing.
- **Height:** inner minus Spellstone elevation, and outer minus inner elevation.

Four-slot recipes ignore every outer value and contribution, including cost. Absolute world elevation, whole-build translation and quarter-turns do not change shaping. Adjacent-Plinth spacing and beam length are derived measurements, not additional independent power awards.

The candidate uses horizontal block-center lengths. Diagonal grid offsets have radius `offset * sqrt(2)`. Distance and signed height remain independent, so raising a node does not also earn a distance bonus. Outer radius is strictly beyond inner radius. Prototype bounds are inner radius at most 6, outer radius at most 12, and each height step from −4 to +4. These conventions are proposals, not installed placement rules.

## Traits condition the four modifiers

Every descriptive trait has one candidate profile, with six preferred geometric inputs and three response weights for Amplify, Range and Area. A school profile has the same structure as an element profile. None increases a raw Fire, Earth or Evocation rating.

The starting motifs include:

| Trait | Shapes, inner / outer | Preferred stage gaps | Height steps | Intended feel |
| --- | --- | --- | --- | --- |
| Earth | Cross / Cross | 1 / 1 | 0 / 0 | Compact, aligned, regular and level; potency/coverage over reach |
| Fire | Cross / Diagonal | 1 / 3 | −3 / +3 | Staggered layers, contrasting spacing and falling/rising heights; potency/coverage over reach |
| Evocation | Cross / Cross | 3 / 2 | −2 / −2 | Moderate outward spacing and descending release |
| Illusion | Diagonal / Diagonal | 3 / 3 | +2 / −2 | Broad anchored structure with a raised inner stage |
| Divination | Cross / Diagonal | 5 / 3 | +2 / +2 | Spread, ascending observation, emphasizing reach |
| Water | Diagonal / Diagonal | 3 / 3 | +2 / +2 | Broad ascending coverage |

These assignments are independently authored proposals. The current regular rings cannot express arbitrary asymmetric or jagged placements. Fire's chaotic motif means sharper contrasts within the supported shape/spacing/height factors; it does not increase Volatile or unknown-cast failure risk. Some ideal physical gaps cannot be matched exactly on the block grid.

Mixed traits blend their responses equally in log space. This is a candidate compromise rule, not an owner-selected final algorithm. Fire and Evocation have different preferences, so a layout favorable to Fire can be less favorable to Evocation. Adding traits does not multiply separate bonuses into an uncontrolled stacking ladder. Descriptive trait magnitude is deliberately not an input to this prototype; the owner can review a relative-ratio interpretation separately if desired.

## Exact candidate functions

The neutral four-slot reference is an inner Cross at radius 2, height 0. The eight-slot reference is Cross/Cross at radii 2 and 4, both heights 0. All four modifiers equal 1 for every selected trait combination at its reference.

For each trait, calculate separate contributions for active shapes, distances and heights:

```text
shapeMatch(actual, preferred) = +1 if equal, otherwise -1
distancePeak(d, preferred) = exp(-0.5 * (log2(d / preferred) / 0.8)^2)
heightPeak(y, preferred) = exp(-0.5 * ((y - preferred) / 2)^2)

each term = weight * (response at actual input - response at neutral reference)
traitResponse = clamp(sum of active terms, -0.5, +0.5)
traitAxisLog = traitResponse * that trait's axis weight
```

Term weights are inner shape 0.10, outer shape 0.06, inner distance 0.24, outer distance 0.14, inner height 0.16 and outer height 0.10. All outer terms are absent in four-slot mode. Response curves weaken beyond preferred values; building farther or higher does not yield unlimited gains.

The common geometry contribution remains independent of descriptive trait identity:

```text
D1 = clamp(log2(d1 / 2), -1, 2)
D2 = clamp(log2(d2 / 2), -1, 2), only with active outer layer
diag1 / diag2 = 1 for Diagonal, otherwise 0

commonAmplifyLog = -0.0175 * dy1 + activeOuter * (-0.00875 * dy2)
commonRangeLog = 0.16 * D1 - 0.06 * diag1
commonAreaLog, four slots = -0.10 * D1 + 0.05 * diag1
commonAreaLog, eight slots = -0.10 * D1 + 0.14 * D2 + 0.08 * diag2

axisMultiplier = clamp(2^(commonAxisLog + mean(selected traitAxisLogs)), axis bounds)
```

With no selected traits the mean is zero. Amplify bounds are 0.75–1.50; Range and Area bounds are 0.70–1.50. These bounds belong to this geometry producer, not the general trait resolver or all future item modifiers.

## One casting-cost factor

The owner-selected interpretation is componentwise multiplication, preserving units:

```text
craftedCostComponent = baseCostComponent * CastingCostMultiplier
```

For ×1.20, 20 mana becomes 24 mana, 2 hearts become 2.4 hearts, 5 hunger points become 6, and 40 casting ticks become 48. A bundle containing all of those scales all of them; they are never summed into one unitless resource. No mana, health, hunger or time component is introduced when absent. There is no additive charging surcharge.

The candidate cost factor uses the three final scaling factors, with stronger output increasing payment and weaker output allowing a smaller reduction:

```text
Laxis = log2(final axis multiplier)
costLog = sum(axis cost weight * Laxis * (0.9 if Laxis >= 0, otherwise 0.35))
CastingCostMultiplier = clamp(2^costLog, 0.65, 2.00)
axis cost weights: Amplify 1.0, Range 0.65, Area 0.90
```

This heuristic is proposed tuning, not proven gameplay balance. The current native model has continuous mana/health, integer hunger/ticks/material amounts and material CONSUME/DAMAGE payment kinds. The preview preserves exact values; minimums and rounding for integer costs remain undecided. Health is entered in hearts for readability; native health points are a different display unit, with the same dimensionless factor.

## Actual consumers and remaining decisions

Amplify changes only authored potency consumers; Range changes supported reach; Area changes the declared spatial extent. Area radius and covered area are not numerically interchangeable. A spell without an Area consumer does not gain coverage from this table. A future magic item uses only the channels it actually supports.

The current Fireball already reads Amplify, Range and Area for damage, travel and radius. This v2 design does not require adding a second Fire/Evocation potency read to Fireball: those traits select the shaping response, whose emitted Amplify modifier is then consumed once. The old v1 affinity expression is superseded, preventing double application.

The owner resolved unsupported axes with simple application: a spell without a Range or Area consumer gets no change to that property, while the Casting Cost factor still scales its existing costs. The cost function receives layout and traits, not a list of supported consumers. It is not reweighted per spell. This avoids a second hidden applicability calculation.

Before game implementation, combine geometry with local imbuements, choose integer rounding and verify actual effects/costs. A zero-cost activation stays zero under multiplication; a cost-only downside therefore cannot balance a free beneficial modification. Passive or free items need a real supported outcome tradeoff or a restricted shaping choice, rather than an invented new payment. Exact handling remains open.

Verification covers calculator arithmetic, trait selection, saved-state migration, outer omission and browser appearance. It is not gameplay balance or Minecraft presentation verification. Results and limitations are recorded in [development status](../development-status.md).
