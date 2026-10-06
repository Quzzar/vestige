# Leyline school balance audit

**October 4, 2026 · accepted numerical design baseline, not verified gameplay balance.**

The owner locked in the current v3 matrices, shared functions, geometry domain, equal log-space trait blend, minor balancing offsets and final-amount rounding. Numerical evidence below verifies that baseline. Native apparatus shaping and stored crafted modifiers remain to be implemented.

The sweep uses the same rules, geometry domain and tie policy as the calculator. Higher Amplify, Range and Area and lower Casting Cost define the four separate objectives. No overall utility score is assumed.

A layout dominating every alternative would have to achieve all four extrema at once. None does in any audited example. Schools also have no common maximum for any individual objective, including Casting Cost. That does not mean every layout is useful, or that a chosen weighted average cannot have a best compromise.

Near-universal diagnostics normalize each school’s worst-to-best span to 0–1 for one objective, then find the layout with the highest minimum fraction across the eight schools. This is an audit diagnostic, not an output stat or gameplay power score. A value above 0.90 fails the guard.

School opportunities share a sum of single-axis peak log multipliers of 0.60 with four slots and 0.96 with eight. These separately achievable peaks need not occur in the same layout. Specialty allocations differ: Evocation favors potency, Divination reach, Illusion coverage; other schools distribute the same envelope more evenly. This authoring guard compares access to modifiers, not damage, healing, utility or spell power.

## 4 slots

169 layouts × 82 examples = 13,858 evaluations.

| School | Peak Amplify | Peak Range | Peak Area | Minimum Casting Cost |
| --- | ---: | ---: | ---: | ---: |
| Abjuration | 1.162 | 1.123 | 1.162 | 0.900 |
| Conjuration | 1.152 | 1.152 | 1.142 | 0.879 |
| Divination | 1.119 | 1.206 | 1.123 | 0.854 |
| Enchantment | 1.162 | 1.133 | 1.152 | 0.866 |
| Evocation | 1.196 | 1.123 | 1.128 | 0.900 |
| Illusion | 1.128 | 1.128 | 1.191 | 0.900 |
| Necromancy | 1.171 | 1.133 | 1.142 | 0.900 |
| Transmutation | 1.152 | 1.147 | 1.147 | 0.900 |

**Each column is optimized separately.** The row is not a simultaneously available result.

| Objective | Common school maximizers | Distinct school winners | Best worst-school fraction |
| --- | ---: | ---: | ---: |
| Amplify | 0 | 8 | 20.2% |
| Range | 0 | 8 | 56.3% |
| Area | 0 | 8 | 30.6% |
| Casting Cost | 0 | 8 | 17.4% |

Affordability check: **0 of 246** outcome-goal winners have a Casting Cost factor greater than every individual outcome factor (0/138 single-trait winners). Largest collateral outcome reduction is **9.4%**. These are numerical tuning guards, not a claim that Range, Area and Amplify have interchangeable gameplay value.


Worst one-block neighbor retains 95.5% of its objective value for height changes and 86.8% for spacing changes. Shape switches are discrete, not smooth.

| Goal | School winner footprint sides · min / median / max | Single-trait winner footprint sides · min / median / max |
| --- | ---: | ---: |
| Amplify | 3 / 5 / 13 | 3 / 5 / 13 |
| Range | 5 / 9 / 17 | 5 / 17 / 17 |
| Area | 3 / 5 / 9 | 3 / 3 / 11 |
| Casting Cost | 3 / 7 / 11 | 3 / 5 / 11 |

Footprints are square occupied block bounds, including boundary node blocks and excluding optional masonry/access space. Diagonal offsets use their axis-aligned box, not twice their radial distance. Medians may fall between realizable sizes. These are selected goal winners, not all allowed builds.

## 8 slots

43,940 layouts × 82 examples = 3,603,080 evaluations.

| School | Peak Amplify | Peak Range | Peak Area | Minimum Casting Cost |
| --- | ---: | ---: | ---: | ---: |
| Abjuration | 1.271 | 1.205 | 1.271 | 0.813 |
| Conjuration | 1.254 | 1.254 | 1.237 | 0.726 |
| Divination | 1.197 | 1.349 | 1.205 | 0.719 |
| Enchantment | 1.271 | 1.221 | 1.254 | 0.801 |
| Evocation | 1.331 | 1.205 | 1.213 | 0.830 |
| Illusion | 1.213 | 1.213 | 1.322 | 0.830 |
| Necromancy | 1.288 | 1.221 | 1.237 | 0.820 |
| Transmutation | 1.254 | 1.246 | 1.246 | 0.830 |

**Each column is optimized separately.** The row is not a simultaneously available result.

| Objective | Common school maximizers | Distinct school winners | Best worst-school fraction |
| --- | ---: | ---: | ---: |
| Amplify | 0 | 8 | 19.2% |
| Range | 0 | 8 | 40.5% |
| Area | 0 | 8 | 23.2% |
| Casting Cost | 0 | 8 | 25.7% |

Affordability check: **0 of 246** outcome-goal winners have a Casting Cost factor greater than every individual outcome factor (0/138 single-trait winners). Largest collateral outcome reduction is **10.1%**. These are numerical tuning guards, not a claim that Range, Area and Amplify have interchangeable gameplay value.


Worst one-block neighbor retains 95.6% of its objective value for height changes and 86.7% for spacing changes. Shape switches are discrete, not smooth.

| Goal | School winner footprint sides · min / median / max | Single-trait winner footprint sides · min / median / max |
| --- | ---: | ---: |
| Amplify | 5 / 7 / 21 | 5 / 7 / 21 |
| Range | 7 / 12 / 33 | 7 / 18 / 33 |
| Area | 5 / 12 / 17 | 5 / 13 / 33 |
| Casting Cost | 5 / 12 / 33 | 3 / 5 / 33 |

Footprints are square occupied block bounds, including boundary node blocks and excluding optional masonry/access space. Diagonal offsets use their axis-aligned box, not twice their radial distance. Medians may fall between realizable sizes. These are selected goal winners, not all allowed builds.

## Scope and repeatability

Full coverage includes all 46 configured single traits, all 28 two-school combinations, Fire + Evocation, Earth + Abjuration, Water + Conjuration, Mind + Illusion, Fire + Earth, the eight-school mixture, Life + Plant + Wood (the Nature focus) and Divination + Light (the pyramid focus): 82 examples per capacity. Every example is checked for finite bounds and absence of a joint four-objective optimum. Numerical parity across every possible trait subset is not claimed.

School-only guards also cover equal opportunity envelopes, at most 15% peak spread per outcome axis, at most 20% minimum-cost spread, at least four distinct school winners per objective, absence of a near-universal winner and neighboring-layout retention. Cost minima differ because reducing different outcomes supplies different payment reductions. Cheaper does not imply stronger.

The October 4 affordability review found excessive surcharges across many traits: before correction, 120/246 four-slot and 142/246 eight-slot outcome-goal winners cost more than every individual outcome gain (75/138 and 88/138 single-trait winners). The first correction moderated positive combined cost logs by 0.30 and left negative logs unchanged. It changed neither the outcome matrices nor the independent efficiency preferences. That continuous monotone curve preserved cost-optimum ordering and existing discounts. The later minor-offset response below is the accepted baseline. The affordability guard rejects a return to excessive goal-winner surcharges.

Final applied amounts round once to the nearest whole unit after composing all modifiers; nonnegative exact halves round up. The calculator applies this to each cost component separately and keeps absent costs zero. Modifiers and intermediate expressions retain full precision. The optimizer compares exact factors; rounded costs can tie for a particular base amount. Native application to costs and numerical outcomes remains future work.

The owner clarified that a matching structure should be largely beneficial for its type, with only minor balancing offsets. The shared outcome response keeps full positive gains and raises bounded sub-neutral outcome factors to the power 0.30. Neutral remains 1; response ordering and smooth neighborhoods remain. This limits Amplify losses to about 8.3% and Range/Area losses to about 10.1%, rather than the earlier 25%/30% floors. A new guard rejects outcome-goal winners with collateral losses above 11%. Positive combined cost logs now use 0.20 for smaller payment offsets; negative logs retain full value. Reduced outcome losses also change cost refunds, so school Cost cells were refitted offline toward minimum factors of 0.90/0.83 where required, preserving better results. Previously dormant independent cost cells are activated; Transmutation cost favors first distance 4 for a broader efficiency neighborhood. Outcome cells and their individual peak opportunities remain unchanged. The complete audit is regenerated rather than assuming efficiency results stay identical.

The old common Range distance bonus strongly favored maximum spread for almost every school. Common distance/shape terms are now one quarter as strong; the common Amplify height bias is zero. Distance width increases from 0.8 to 1.1. The six independent matrix rows still produce all four modifiers. Transmutation’s first-distance motif changes from 2 to 3, with Range preferring 4, and Abjuration’s Area outer-gap preference changes from 2 to 1, so their beneficial responses do not sit entirely on the neutral reference.

School Casting Cost columns have independently authored efficiency preferences. Coefficient fitting aims for minimum factors around 0.90 with four slots and 0.83 with eight; where existing outcome tradeoffs already provide a lower minimum, no extra discount is added. These aims are not runtime clamps. The shared weaker-outcome reduction exponent decreases from 0.35 to 0.18, reducing the universal appeal of weakening everything for a refund. Cost remains uniform across each existing payment component and is not reweighted by outcome applicability.

Schools use the same evaluator as every other descriptive trait. No school-specific runtime branch, raw trait-point boost, capability-dependent cost reweighting or net-height shortcut is added. School coefficient fitting was an offline authoring step; the stored cells remain independently editable data.

The monument pass expands the proposal domain to inner radius 8, outer radius 16 and independent height steps ±6. Divination and Light favor square descending pyramid terraces for reach, with distinct potency/coverage/cost preferences. Every school’s prior single-axis outcome peak and opportunity envelope is preserved by offline coefficient fitting; Light retains its prior outcome peaks. Casting Cost cells and shared modifier limits are unchanged. Larger bounds can expose new minima and other descriptive-trait winners. See the [pyramid design](design/leyline-calculator.md#large-pyramid-observatory) for coordinates and tradeoffs.

Run `node tools/audit_leyline_balance.mjs` to regenerate this report and its [JSON evidence](leyline-balance-audit.json); `--check` verifies freshness and every guard. Run `node --test tools/test_leyline_calculator.mjs tools/test_leyline_balance.mjs` for arithmetic and regression checks.

An optimal layout still exists once traits, capacity and a particular goal are specified. All-round compromise layouts can exist after someone supplies weights or a crafting workload. The mathematical requirement here is the absence of a universal dominant choice, not the absence of any optimizer result.

This accepted design is not installed into the game. Real costs, unused outcome consumers, integer rounding, Area radius versus covered area, local imbuements, arbitrary non-ring structures, optional mods and multiplayer encounters still need native implementation and gameplay review. Free/passive outputs cannot pay a tradeoff through absent costs. In particular, current Divination spells do not explicitly read Amplify; a displayed factor does not create sensing strength. Nature spells with authored Amplify reads can consume that modifier once integration exists.
