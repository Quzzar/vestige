# Leyline structure response calculator

Native integration is implemented as of October 4, 2026; [playtest instructions](../leyline-playtesting.md) describe the two-block apparatus, stored scroll modifiers and verification. Numerical rules below remain the accepted v3 baseline.

**Accepted implementation baseline, October 4, 2026.** The owner locked in the current v3 calculator after reviewing beneficial specialization, minor offsets, the pyramid and Nature henge, and Amplify applicability. The current numerical cells, equal log-space trait blending, geometry measurements and bounds, response functions, cost tradeoff and final-amount rounding are accepted as the baseline for implementation. Each of the 46 configured descriptive traits has a **six-row, four-column response matrix**. The two signed height steps remain separate. Schools are ordinary traits, and a single Casting Cost multiplier scales each existing cost component. The calculator and native Minecraft evaluator execute the same saved rules. Crafted scrolls retain their resolved modifiers, and fresh worlds use only Spellstone and Plinth; old block IDs and migration hooks are removed.

**Application and optimizer clarification:** the owner confirmed that shaping emits all four modifiers. Effects use the axes they read; absent Range/Area/potency consumers simply have no corresponding outcome change. **Casting Cost still applies independently** and is not recalculated according to supported outcome axes. The owner's requested best-layout button now searches the permitted discrete layouts and applies a result for a chosen objective.

**Accepted balancing direction:** a structure suited to an output's traits should be largely beneficial for that type, with minor balancing offsets. Its specialization should feel rewarding. Current response strengths are locked in as the baseline; later tuning must retain school fairness, distinct optima and useful nearby layouts and regenerate the audit evidence.

Exact accepted matrices are in [v3 rules](leyline-calculator-v3.json). [V2](leyline-calculator-v2.md) and [its rules](leyline-calculator-v2.json) preserve the superseded single-response model; [v1](leyline-calculator-v1.md) retains the earlier rating/affinity proposal. [Plinth rituals](plinth-rituals.md) records the apparatus direction; [output shaping](leyline-output-shaping.md) records the owner clarification and compatible outcome requirements. The native implementation adds two-block recipes/art, bounded structure selection, ambiguity rejection and existing ritual timing. Embedded material mappings and free/passive equipment remain outside this numerical lock-in. Discovery probabilities and Volatile semantics retain their existing rules.

**School balance and monument pass, October 4:** the owner requires comparable shaping opportunities, no universally dominant layout and some genuinely large pyramid optima. The [exhaustive audit](../leyline-balance-audit.md) checks all 169/43,940 layouts across 82 examples per capacity, including Nature and pyramid focuses. Shared distance/shape biases are reduced, common height bias is removed, school cells retain equal opportunity envelopes, and Casting Cost has distinct efficiency preferences. No audited example has one optimum for all four objectives; no individual objective has a common school winner. These are verified properties of the accepted bounded numerical model, not gameplay balance.

## Reading and adjusting the calculator

**Beneficial specialization and rounding, October 4:** the owner rejected excessive cost premiums and requested nearest-whole final amounts. Before correction, Casting Cost exceeded every individual outcome gain at 75/138 four-slot and 88/138 eight-slot single-trait outcome-goal winners; across all 82 examples the counts were 120/246 and 142/246. The first correction moderated positive combined cost logs by 0.30. The current response uses **0.20** for cost increases and preserves full discounts. Bounded outcome factors below 1 use **a 0.30 exponent**, reducing penalties while retaining full positive gains. Outcome cells and exact goal maxima/winners remain unchanged; school Cost cells were refitted because smaller outcome penalties also change cost refunds. Guards reject excessive surcharges and collateral losses above 11% at audited outcome-goal winners. These are numerical tuning heuristics, not an overall gameplay value formula. Final applied amounts round once after all modifiers, with nonnegative exact halves rounding up; multipliers retain precision.

Use **Set layout** for four/eight active slots, Cross/Diagonal shapes, offsets and signed height steps. Both grid offsets locate a layer from the Spellstone. The outer stage gap used by the functions is measured from its nearest inner node, rather than from the Spellstone. Inner height is relative to the Spellstone; outer height is relative to the inner layer. Positive means higher. Dragging the diagram or using Rotate view changes only the camera.

**Node footprint** below the diagram shows the occupied square block bounds and vertical span between node bases. It includes boundary node blocks, excludes inactive outer nodes and leaves out optional pillars, lintels, walls and access space. **Nature stone circle** applies the Life + Plant + Wood example and its level-circle placement while retaining the current four/eight-slot capacity, goal, costs and camera. It is a preset, not a promise that every chosen objective shares that optimum.

**Pyramid observatory** selects Divination + Light, sets the goal to Longest Range and applies their terraced pyramid optimum, retaining capacity, costs and camera. **Pyramid building guide · visual only** shows a translucent architectural envelope around descending tiers. Toggling it changes no modifier, candidate, ingredient or cost. The guide is remembered with camera settings and turns off when selecting the Nature circle. Decorative masonry is not another shaping factor or required material.

Use **Trait on the spell or item → Include trait** in **Crafting impact** to describe an example containing any combination of the 46 descriptive traits with accepted responses. There is no required school, associated element or rating input. Each selected trait can be removed. Evocation, Fire, Earth, Illusion, Mind and the other entries use the same selection mechanism. Amplify, Range and Area are the output scaling axes; Volatile's failure semantics are not changed by geometry.

The four-row table updates immediately. A multiplier changes that output's existing scaling value or cost. Selecting Fire and Evocation asks how this layout shapes an output bearing both traits; it does not increase their descriptive ratings or add those traits to a recipe. The actual recipe remains responsible for determining which traits its output has.

**What each layout factor contributes** expands a six-by-four table for the selected traits. Each row shows that factor's multiplier for Amplify, Range, Area and Casting Cost, including common geometry influences. Four-slot operations omit all three outer rows. **Outcome downside adjustment** shows the softer negative outcome response; **Output tradeoff** shows the shared casting-cost contribution from the three final outcome modifiers; **Cost increase adjustment** shows the softer positive cost response; **Output limits** appears when a bound changes a result. Multiplying each column's displayed factors, including those final rows, reconstructs the corresponding total before display rounding. The two height rows remain independently visible. No matrix parameter editor is required to change a layout or inspect its impact.

Expand **Base casting costs → crafted costs** to enter mana, health, hunger, casting time, cooldown, material consumption or durability. Every entered component uses the same Casting Cost factor and rounds its final amount to the nearest whole unit. Zero represents an absent component in this example and remains zero. The Change column labels higher costs as **more expensive** and lower costs as **cheaper**. The table is an example cost bundle, not an imported item or spell recipe.

**Compare single-trait outputs** shows all 46 candidates for the same layout. Each row asks what the four modifiers would be if that trait alone were present. Rows do not add traits to the selected output.

## Finding an optimal layout

Choose **Optimize for**: Highest Amplify, Longest Range, Largest Area or Lowest Casting Cost. Then **Use best layout** searches the allowed placements, applies the winner, and updates the diagram, shapes, offsets and heights. There is no single layout that is best for all four objectives; the chosen goal supplies the meaning of optimal.

The recipe's four/eight-slot capacity stays fixed. Four-slot search evaluates 169 inner arrangements, omitting every outer input. Eight-slot search evaluates 43,940 valid arrangements across all four shape combinations, every allowed grid offset and the two independent −6…+6 height steps. Current trait selection is retained. Cost minimization compares the cost multiplier; absent components still remain absent.

The search evaluates the same functions as the preview. It is exhaustive within the accepted bounds and regular-ring geometry, not a local guess or a claim about unrestricted world builds. Exact ties prefer a smaller active outer radius (inner radius with four slots), then a smaller vertical span, smaller inner radius and smaller absolute height steps, followed by deterministic shape/height ordering. Radius is distinct from the displayed axis-aligned footprint, especially for diagonal nodes. These tie preferences are accepted selection conventions, not extra shaping bonuses.

Edits to geometry, traits, costs or goal invalidate the prior result. An in-progress search is discarded if those inputs change, preventing it from overwriting a newer choice. Camera motion leaves the search objective unchanged. Existing saved states default to Amplify; future interactions preserve the selected goal. Optimizer results remain local derived state and do not install a game feature.

## Accepted factors and geometry

Ingredients and their relative positions select a recipe. Embedded imbuements are reserved for future local contributions according to the offering and its role; current sockets have no numeric effect. Geometry supplies global shaping. This calculator isolates geometry; material interactions and recipe matching are not simulated.

Geometry has three factor families, each retaining two stages where active:

- **Shape:** Cross/Diagonal independently for inner and outer layers.
- **Distance:** Spellstone-to-inner and inner-to-outer spacing.
- **Height:** inner minus Spellstone elevation, and outer minus inner elevation.

Four-slot recipes ignore every outer value and contribution, including cost. Absolute world elevation, whole-build translation and quarter-turns do not change shaping. Adjacent-Plinth spacing and beam length are derived measurements, not additional independent power awards.

The accepted baseline uses horizontal block-center lengths. Diagonal grid offsets have radius `offset * sqrt(2)`. Distance and signed height remain independent, so raising a node does not also earn a distance bonus. Outer radius is strictly beyond inner radius. Accepted bounds are inner radius at most 8, outer radius at most 16, and each height step from −6 to +6, stored in `layoutBounds` in the canonical rules. The evaluator's optional default retains the preceding 6/12/4 domain for old callers; the current calculator and audit explicitly use the saved bounds. Native integration uses the accepted explicit bounds, matches 490 reference fixtures and reads only already-loaded chunks.

## Size variety and restructuring gains

Relative to the neutral reference, separately optimized school outcome modifiers peak around **12–21% above baseline with four slots** and **20–35% with eight**. School cost minima range from approximately 0.85–0.90 and 0.72–0.83 respectively. Outcome peaks and cost minima are separate goals; stronger output can cost more, with increases moderated by the shared affordability response. For example, the Divination + Light pyramid below gives Range 1.238 with Casting Cost 1.067, while their cheapest eight-slot layout gives Range 0.987 with Cost 0.856. These numerical exchanges are not measured spell-power gains.

The allowed footprint sides span 3–17 blocks with four active nodes and 3–33 with eight. Actual school potency winners range from **3×3 to 13×13** (median side 5) with four slots, and **5×5 to 21×21** (median side 7) with eight. Eight-slot school reach winners range from 7×7 to 33×33, coverage from 5×5 to 17×17, and cost-minimizing winners from 5×5 to 33×33. Thus a large site can serve a particular specialization without making every wizard-tower build inferior. The [audit](../leyline-balance-audit.md) also records all 46 single-trait size distributions and each winner's placement/size. Footprint size alone is not a reward or an optimizer goal.

Curves remain broad: a nearby arrangement can retain most of the advantage. Across both capacities, the school's worst valid one-block neighbor retains at least 95.4% of the objective multiplier for height changes and 86.6% for spacing changes. These percentages refer to the whole multiplier, not the extra bonus above 1. Shape changes are discrete. Integer coordinates and building-space preferences can make a slightly suboptimal design a worthwhile compromise.

## Large pyramid observatory

The owner requested large optimal builds suited to a pyramid. Divination and Light now have **Longest Range** preferences for aligned Cross/Cross layers, both horizontal stage gaps 8 and both outward height steps −6. Amplify and Area retain independent cells and distinct optima; Casting Cost uses independently authored efficiency cells. The shared evaluator does not recognize a pyramid, inspect masonry, or award a blanket size bonus. Offline coefficient fitting preserves every school's prior single-axis outcome peaks (and their 0.60/0.96 opportunity envelopes), while relocating Divination's useful sites. Light's prior Amplify/Range peaks are retained; its Area matrix and peak remain unchanged. Other descriptive-trait matrices, including Earth and the Nature focus, are unchanged, though expanding the search domain can expose new goal winners. The later minor-offset pass refits school Cost cells without changing those outcome cells.

With the outer tier's base level at 0:

- **Spellstone:** `(0,12,0)` at the apex.
- **Inner Cross:** `(±8,6,0)` and `(0,6,±8)` on the middle terrace.
- **Outer Cross:** `(±16,0,0)` and `(0,0,±16)` around the base.

The occupied node box is **33×33**, with **12 blocks of elevation** from base to Spellstone. This is an actual maximum-Range layout for Divination alone, Light alone and their equal-weight combination within the expanded domain. The square architectural guide shows where a pyramid can envelop the nodes; terrace thickness, stairs, stone choice and decoration are free design choices.

For **Divination + Light**, the resulting modifiers are **Amplify 1.085, Range 1.238, Area 1.024, Casting Cost 1.067**: approximately +24% Range for +7% cost, with a 20-mana example becoming **21 mana** after rounding. A 29×29 alternative with offsets 7/14 and height steps −5/−5 retains over 90% of the additional Range above baseline. Their potency optimum instead occupies 21×21 at offsets 6/10 and heights −4/−4, with Amplify 1.148; their cheapest layout is a 9×9 Diagonal/Diagonal arrangement at offsets 3/4 and heights +3/+1, with Cost 0.856, Amplify 0.971 and Range 0.987. No goal silently changes when running the optimizer; the preset explicitly selects Range.

The maximum-Range layout lies on the current placement boundary. Trait curves peak at those preferred gaps/heights and taper beyond them, rather than awarding unbounded power for enlargement. The shared common distance terms also saturate. Even within this domain, compact Earth potency and the 11×11 Nature circle remain optimal for their goals. These are accepted geometry preferences; native selection recognizes node rings rather than architectural pyramid masonry, and numerical bonuses are not a measured gameplay balance score.

## Nature focus and a stone circle

The owner requested a Nature-focused layout suited to a Stonehenge-style build. **Nature is a focus label for the existing Life, Plant and Wood traits**, not a new native trait. Their accepted matrix preferences favor level bases and staggered Cross/Diagonal layers. The original Nature motif pass left schools, Earth, shared functions, bounds and cost tradeoffs unchanged, preserving prior individual outcome peaks except for a roughly 0.04% increase in Wood's eight-slot Amplify peak to match its four-slot opportunity. The subsequent monument pass above broadens bounds and refits school outcomes while leaving these Nature matrices intact; that wider domain exposes a new Nature reach winner below. The level potency circle remains optimal. Neither pass adds a reward for stone masonry.

With the Spellstone at `(0,0,0)`, the eight-slot potency optimum for **Life + Plant + Wood** places inner nodes at `(±5,0,0)` / `(0,0,±5)` and outer nodes at `(±4,0,±4)`. All bases are level. The eight points form an approximately circular arrangement inside **11×11 blocks**: four cardinal nodes at radius 5 and four corner nodes at radius √32. Pillars and lintels can surround or support them without creating mandatory decorative blocks or altering imbuement.

| Nature focus layout | Node footprint | Amplify | Range | Area | Casting Cost |
| --- | ---: | ---: | ---: | ---: | ---: |
| Potency optimum · inner Cross 5 / outer Diagonal 4 · level | 11×11 | 1.176 | 1.046 | 1.180 | 1.059 |
| Compact circle · inner Cross 4 / outer Diagonal 3 · level | 9×9 | 1.171 | 1.036 | 1.168 | 1.055 |
| Broader reach optimum · inner Cross 8 / outer Diagonal 6 · level | 17×17 | 1.104 | 1.065 | 1.122 | 1.042 |
| Coverage optimum · inner Cross 4 / outer Diagonal 7 · level | 15×15 | 1.163 | 1.035 | 1.207 | 1.060 |

The compact 9×9 circle retains **96.8% of the additional potency above baseline**, with a lower exact cost factor; both become 21 mana from a 20-mana base after rounding. Enlarging to inner Cross 6 / outer Diagonal 8 (17×17) reduces potency to 1.149; bigger is not automatically better. The four-slot Nature potency optimum keeps the inner cardinal nodes at 5, ignoring outer nodes completely: 11×11, Amplify 1.125, Range 1.041, Area 1.084, Cost 1.039. Adding a school or other trait can move any optimum because responses blend.

## Traits condition the four modifiers

Every descriptive trait has one matrix with these rows: `innerShape`, `outerShape`, `d1`, `d2`, `dy1`, `dy2`. Its columns are **Amplify, Range, Area, Casting Cost**. Each cell is `[preferredInput, coefficient]`; it can be edited independently of every other cell. The row declares the shared response function and width. A school matrix has the same structure as an element matrix. No cell increases a raw Fire, Earth or Evocation rating.

Shape cells evaluate Cross/Diagonal matches. Distance cells evaluate smooth peaks in logarithmic distance, and height cells evaluate smooth peaks around signed height steps. A coefficient can be positive, negative or zero. Preferences can differ across columns, so potency and reach can favor different distances. This permits distinct response directions rather than requiring all columns to be scaled copies of one trait score. Trait names select data; the evaluator has no Fire/Earth/school-specific branches.

The starting Amplify preferences retain these motifs; other columns have independently authored cells:

| Trait | Shapes, inner / outer | Preferred stage gaps | Height steps | Intended feel |
| --- | --- | --- | --- | --- |
| Earth | Cross / Cross | 1 / 1 | 0 / 0 | Compact, aligned, regular and level; potency/coverage over reach |
| Fire | Cross / Diagonal | 1 / 3 | −3 / +3 | Staggered layers, contrasting spacing and falling/rising heights; potency/coverage over reach |
| Evocation | Cross / Cross | 3 / 2 | −2 / −2 | Moderate outward spacing and descending release |
| Illusion | Diagonal / Diagonal | 3 / 3 | +2 / −2 | Broad anchored structure with a raised inner stage |
| Divination | Cross / Cross | 6 / 4 | −4 / −4 | Pyramid potency terraces; Range separately prefers 8 / 8 and −6 / −6 |
| Light | Cross / Cross | 6 / 4 | −4 / −4 | Pyramid potency terraces; Range shares observatory geometry; Area remains independent |
| Water | Diagonal / Diagonal | 3 / 3 | +2 / +2 | Broad ascending coverage |
| Life / Plant | Cross / Diagonal | 5 / √17 | 0 / 0 | Broad level sacred grove / stone circle |
| Wood | Cross / Diagonal | 4 / √10 | 0 / 0 | Slightly more compact level grove |

These assignments are independently authored cells in the accepted baseline. Most starting first-distance preferences for Range are at least 2 and one block beyond the trait's Amplify preference; Area's outer-distance preference starts one block beyond the Amplify preference, capped at 6. The balance pass moves Transmutation's first-distance preference to 3, with Range preferring 4, and Abjuration's Area outer-gap preference to 1. These are authoring choices in the saved data, not rules the evaluator imposes on a new trait. Signed height preferences retain the preceding motifs, while their coefficients differ by column. School Casting Cost cells now have their own efficiency motifs, which can differ from potency/coverage/reach preferences. Every cell remains independently editable.

The current regular rings cannot express arbitrary asymmetric or jagged placements. Fire's chaotic motif means sharper contrasts within the supported shape/spacing/height factors; it does not increase Volatile or unknown-cast failure risk. Some ideal physical gaps cannot be matched exactly on the block grid.

Mixed traits blend each factor/column response equally in log space, as accepted in the current baseline. Fire and Evocation have different preferences, so a layout favorable to Fire can be less favorable to Evocation. Distinct selected names are sorted before evaluation for deterministic results; input order and duplicates do not change the output. Adding traits does not multiply separate bonuses into an uncontrolled stacking ladder. Descriptive trait magnitude is not an input to geometry blending. This is separate from discovery's slot-counted rating weights. A descriptive trait without a configured matrix adds no trait-specific shaping response and is omitted from the blend.

## Exact accepted functions

The neutral four-slot reference is an inner Cross at radius 2, height 0. The eight-slot reference is Cross/Cross at radii 2 and 4, both heights 0. All four modifiers equal 1 for every selected trait combination at its reference.

For trait `t`, factor `i` and modifier column `a`, evaluate the cell independently:

```text
shapeMatch(actual, preferred) = +1 if equal, otherwise -1
distancePeak(d, preferred) = exp(-0.5 * (log2(d / preferred) / 1.1)^2)
heightPeak(y, preferred) = exp(-0.5 * ((y - preferred) / 2)^2)

cell(t, i, a) = [preferred(t, i, a), coefficient(t, i, a)]
cellLog(t, i, a) = coefficient(t, i, a)
    * (response(actual_i, preferred(t, i, a))
       - response(reference_i, preferred(t, i, a)))

factorLog(i, a) = commonLog(i, a) + mean(selected traits' cellLog(t, i, a))
rawAxisLog(a) = sum(active factors' factorLog(i, a))
boundedOutcome(a) = clamp(2^rawAxisLog(a), bounds(a))
outcomeMultiplier(a) = boundedOutcome(a)^0.30 if boundedOutcome(a) < 1,
    otherwise boundedOutcome(a), for Amplify, Range and Area
```

All outer cells are absent in four-slot mode. Distance width is 1.1 and signed-height width is 2, shared by a row's cells. The wider distance curves preserve useful neighborhoods around the optimum. There is no intermediate scalar `traitResponse` or subsequent common three-axis weight vector. Output limits are applied after blending, then the generic negative-response adjustment softens losses without changing neutral or positive values. Bounded curves and final modifier bounds prevent indefinite improvement from increasing height or distance.

The starting coefficient directions are recorded in the saved cells. For review, the three outcome columns were initially authored from the preceding trait's potency/reach/coverage coordinates with these row sensitivities:

| Factor | Amplify sensitivity | Range sensitivity | Area sensitivity | Casting Cost coefficient |
| --- | ---: | ---: | ---: | ---: |
| Inner shape | 0.10 | 0.035 | 0.07 | −0.010 |
| Outer shape | 0.06 | 0.035 | 0.085 | −0.008 |
| First distance | 0.24 | 0.15 | 0.12 | −0.018 |
| Second distance | 0.10 | 0.10 | 0.20 | −0.014 |
| First height | 0.16 | 0.055 | 0.105 | −0.014 |
| Second height | 0.10 | 0.09 | 0.075 | −0.012 |

This table records the initial authored values, before the school balance pass; it is not a table of current school coefficients. The evaluator does not regenerate cells from it. School outcome coefficients were fitted offline to comparable peak envelopes, scaling factors with positive opportunity without inflating factors that only impose penalties from the reference. Cost preferences and coefficients were authored independently. All current cells are in the v3 rules. Different row sensitivities and per-column preferences ensure the matrices do not reduce to the old single-score model. All six factors can contribute differently to all four modifiers.

The common geometry contribution remains independent of descriptive trait identity:

```text
D1 = clamp(log2(d1 / 2), -1, 2)
D2 = clamp(log2(d2 / 2), -1, 2), only with active outer layer
diag1 / diag2 = 1 for Diagonal, otherwise 0

commonAmplifyLog = 0
commonRangeLog = 0.04 * D1 - 0.015 * diag1
commonAreaLog, four slots = -0.025 * D1 + 0.0125 * diag1
commonAreaLog, eight slots = -0.025 * D1 + 0.035 * D2 + 0.02 * diag2

Each common term is assigned to its corresponding factor/column cell before summing.
Common Casting Cost contributions are zero; direct cost responses come from trait matrices.
```

With no selected traits the mean is zero. Before the negative-response adjustment, Amplify bounds are 0.75–1.50; Range and Area bounds are 0.70–1.50. The resulting effective lower limits are approximately **0.917 Amplify** and **0.899 Range/Area**, retaining the upper limit 1.50. These bounds belong to this geometry producer, not the general trait resolver or all future item modifiers.

## One casting-cost factor

The owner-selected interpretation is componentwise multiplication, preserving units:

```text
craftedCostComponent = round(baseCostComponent * CastingCostMultiplier)
```

For ×1.20, 20 mana becomes 24 mana, 2 hearts become 2 hearts, 5 hunger points become 6, and 40 casting ticks become 48. A bundle containing all of those scales and rounds each separately; they are never summed into one unitless resource. No mana, health, hunger or time component is introduced when absent. There is no additive charging surcharge. Round once after composing geometry, imbuements and any other applicable modifiers; rounding each intermediate contribution changes the result.

Casting Cost has its own independent matrix column. It also retains the shared output tradeoff, with stronger outcomes increasing payment and weaker outcomes allowing a smaller reduction:

```text
Laxis = log2(final axis multiplier)
outputTradeoffLog = sum(axis cost weight * Laxis * (0.9 if Laxis >= 0, otherwise 0.18))
combinedCostLog = rawAxisLog(cost) + outputTradeoffLog
finalCostLog = combinedCostLog * (0.20 if combinedCostLog > 0, otherwise 1.0)
CastingCostMultiplier = clamp(2^finalCostLog, 0.65, 2.00)
axis cost weights: Amplify 1.0, Range 0.65, Area 0.90
```

This continuous, strictly increasing final cost response softens increases while preserving negative combined logs and their ordering. It is shared by every trait; no school or named structure receives an exception. School Cost cells were refitted offline after softer outcome losses reduced tradeoff refunds, activating dormant cost cells where required. Transmutation's Cost first-distance preference is 4, distinct from its Amplify preference 3. Other descriptive-trait matrices remain unchanged. Together these changes move the pyramid's preceding 1.386 cost factor to 1.067. The independent six-row cost column remains intact.

The owner-selected rule is nearest-whole **final applied amounts**, including costs and supported numeric outcomes. Exact nonnegative halves round up. Factors, percentages, geometry measurements and intermediate expressions retain precision. The calculator currently applies the rule to its cost bundle, in each field's declared unit (health is entered in hearts). The native implementation converts health cost HP to hearts before rounding and converts the result back to HP. Other cost components round in their declared native unit; damage/healing outcomes round in HP. Unshaped activations preserve existing runtime behavior. Small quantities can round to the same amount despite different exact factors; gameplay review must use actual rounded results.

## Balanced opportunities and competing optima

School outcome opportunities share a sum of separately attainable peak log multipliers of 0.60 with four slots and 0.96 with eight. This is a coefficient-authoring guard, not a combined gameplay score or a simultaneously available result. Each school distributes that envelope differently:

| School | Amplify share | Range share | Area share |
| --- | ---: | ---: | ---: |
| Abjuration | 36% | 28% | 36% |
| Conjuration | 34% | 34% | 32% |
| Divination | 27% | 45% | 28% |
| Enchantment | 36% | 30% | 34% |
| Evocation | 43% | 28% | 29% |
| Illusion | 29% | 29% | 42% |
| Necromancy | 38% | 30% | 32% |
| Transmutation | 34% | 33% | 33% |

Casting Cost remains its independent fourth column. School efficiency curves use their own preferences; coefficient authoring aimed at minimum factors around 0.90 for four slots and 0.83 for eight, without adding a discount where existing outcome tradeoffs already beat that target. Those are authoring aims, not runtime cost clamps; actual eight-slot minima range from about 0.719 to 0.830. The shared reduction exponent is lowered to 0.18 so uniformly weakening everything is not almost the best economy layout for every school.

The durable audit guards require no joint four-objective optimum in any checked example, no common school maximum for any one objective, at least four distinct school winners, no layout above 90% of every school's attainable span for the same objective, outcome peak spread at most 15% and minimum-cost spread at most 20%. At every checked outcome-goal winner, the Casting Cost factor must not exceed all three individual outcome factors and collateral outcome losses must stay at or below 11%. These numerical guards do not equate damage, reach and coverage or guarantee benefits for absent consumers. One-block height/spacing neighbors must retain at least 92%/85% of the corresponding school optimum's objective value. These numeric tolerances are baseline audit guards. Shape choices remain discrete.

The audit covers all 46 single traits, all 28 two-school pairs and eight selected mixtures, using the calculator's canonical geometry, evaluator and tie policy. Its [report](../leyline-balance-audit.md) retains exact scope and [JSON evidence](../leyline-balance-audit.json) retains each example's goal-specific winners, occupied footprint, vertical span and other modifiers. A chosen goal or weighted crafting workload can still have a best compromise. There is no meaningful universal "best across the board" without supplying those priorities, and none of the tested layouts dominates every alternative across all four outputs.

## Actual consumers and remaining decisions

Amplify changes only authored potency consumers; Range changes supported reach; Area changes the declared spatial extent. Area radius and covered area are not numerically interchangeable. A spell without an Area consumer does not gain coverage from this table. A future magic item uses only the channels it actually supports.

**Amplify clarification at lock-in:** the current seven Divination-tagged spells have no explicit Amplify read. The Divination + Light pyramid's 1.085 Amplify factor is therefore a potential modifier, not an existing Divination gameplay benefit. Additional sensing strength or observation duration is not automatically created by that number. Nature spells such as [Heal](../../src/main/resources/data/vestige/runtime_spells/heal.json), [Healing Circle](../../src/main/resources/data/vestige/runtime_spells/healing_circle.json) and [Firefly Swarm](../../src/main/resources/data/vestige/runtime_spells/firefly_swarm.json) already explicitly read Amplify for healing or damage. They can consume a crafted Amplify modifier in the native implementation. The henge's 1.176 Amplify factor belongs to the Life + Plant + Wood example; a real spell's complete descriptive trait selection determines its own response. Amplify is weighted most heavily in the shared cost tradeoff, with Area close behind and Range lower. Those weights are heuristics, not a guarantee that Amplify is always the strongest gameplay benefit.

The current Fireball already reads Amplify, Range and Area for damage, travel and radius. This design does not require adding a second Fire/Evocation potency read to Fireball: those traits select the shaping response, whose emitted Amplify modifier is then consumed once. The old v1 affinity expression is superseded, preventing double application.

The owner resolved unsupported axes with simple application: a spell without a Range or Area consumer gets no change to that property, while the Casting Cost factor still scales its existing costs. The cost function receives layout and traits, not a list of supported consumers. It is not reweighted per spell. This avoids a second hidden applicability calculation.

Native geometry, stored scroll modifiers and final-amount consumers are implemented and tested; local imbuement contributions remain to be authored. A zero-cost activation stays zero under multiplication; a cost-only downside therefore cannot balance a free beneficial modification. Passive or free items need a real supported outcome tradeoff or a restricted shaping choice, rather than an invented new payment. Exact handling remains open.

The canonical baseline evaluator is [leyline_calculator.mjs](../../tools/leyline_calculator.mjs). Its matrix tests run with `node --test tools/test_leyline_calculator.mjs`. Use `python3 tools/update_leyline_widget.py /absolute/path/to/plinth-geometry-3d.html` to embed the canonical rules and evaluator; `--check` detects drift. The fragment remains self-contained and needs no network or runtime module loader. V3 saves migrate v2/v1 examples and both earlier geometry states, preserving layouts, traits, goals, costs and camera settings where present. Recomputed numerical results intentionally use the new matrices.

Run `node tools/audit_leyline_balance.mjs --check` for report freshness and the balance guards; `node --test tools/test_leyline_balance.mjs` also proves that common school matrices, inflated school opportunities and sharp height spikes are rejected. The optimizer and audit share the same 169/43,940 candidate domain, geometry measurements and tie preferences. Domain validation, the Nature circle and the large pyramid/nearby compromise have regression coverage.

Verification covers calculator arithmetic, trait selection, saved-state migration, outer omission and browser appearance. It is not gameplay balance or Minecraft presentation verification. Results and limitations are recorded in [development status](../development-status.md).
