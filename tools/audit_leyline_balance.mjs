#!/usr/bin/env node
// Accepted design audit. It neither values gameplay outcomes nor changes runtime spells.
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { LEYLINE_AXES, enumerateLeylineLayouts, leylineLayoutGeometry, leylineLayoutSize, preferLeylineLayout, resolveLeylineModifiers, validateLeylineRules } from './leyline_calculator.mjs';

export const LEYLINE_SCHOOLS = ['abjuration', 'conjuration', 'divination', 'enchantment', 'evocation', 'illusion', 'necromancy', 'transmutation'];
const outcomeAxes = LEYLINE_AXES.filter(axis => axis !== 'cost');
const direction = axis => axis === 'cost' ? -1 : 1;
const intersection = sets => [...sets[0]].filter(i => sets.every(set => set.has(i)));
const round = value => Math.round(value * 1e9) / 1e9;

function analyzeScenario(rules, layouts, geometries, traits) {
  const outputs = geometries.map(geometry => resolveLeylineModifiers(rules, geometry, traits).modifiers);
  for (const output of outputs) for (const axis of LEYLINE_AXES) {
    if (!Number.isFinite(output[axis]) || output[axis] < rules.bounds[axis][0] || output[axis] > rules.bounds[axis][1]) throw new Error(`Invalid ${traits.join('+')}/${axis} output`);
  }
  const axes = {}, winningSets = [];
  for (const axis of LEYLINE_AXES) {
    const sign = direction(axis);
    let best = 0, min = Infinity, max = -Infinity;
    for (let i = 0; i < outputs.length; i++) {
      const value = outputs[i][axis];
      min = Math.min(min, value); max = Math.max(max, value);
      const delta = sign * (value - outputs[best][axis]);
      if (delta > 1e-12 || (Math.abs(delta) <= 1e-12 && preferLeylineLayout(layouts[i], layouts[best]))) best = i;
    }
    const winners = new Set(outputs.flatMap((output, i) => Math.abs(output[axis] - outputs[best][axis]) <= 1e-12 ? [i] : []));
    winningSets.push(winners);
    const winner = layouts[best];
    const retention = { height: [], distance: [] };
    for (const [field, type] of [['innerHeight', 'height'], ['outerStep', 'height'], ['inner', 'distance'], ['outer', 'distance']]) {
      if (winner[field] === undefined) continue;
      for (const step of [-1, 1]) {
        const neighbor = { ...winner, [field]: winner[field] + step };
        const index = layouts.findIndex(layout => Object.keys(layout).every(key => layout[key] === neighbor[key]));
        if (index >= 0) retention[type].push(axis === 'cost' ? outputs[best][axis] / outputs[index][axis] : outputs[index][axis] / outputs[best][axis]);
      }
    }
    axes[axis] = { min: round(min), max: round(max), winningLayouts: winners.size,
      bestLayout: winner, bestSize: leylineLayoutSize(winner), bestModifiers: Object.fromEntries(LEYLINE_AXES.map(a => [a, round(outputs[best][a])])),
      neighborRetention: Object.fromEntries(Object.entries(retention).map(([type, values]) => [type, values.length ? round(Math.min(...values)) : null])) };
  }
  const goalResults = outcomeAxes.map(axis => axes[axis].bestModifiers);
  const costExceedsEveryGain = goalResults.filter(m => m.cost > Math.max(m.amplify, m.range, m.area) + 1e-9).length;
  const maxOutcomeReduction = round(1 - Math.min(...goalResults.flatMap(m => outcomeAxes.map(axis => m[axis])), 1));
  return { outputs, winningSets, summary: { traits, axes, jointMaximizers: intersection(winningSets).length,
    affordability: { outcomeGoalWinners: goalResults.length, costExceedsEveryGain, maxOutcomeReduction } } };
}

export function auditLeylineBalance(rules, { full = true } = {}) {
  validateLeylineRules(rules);
  const failures = [];
  const report = { version: 'leyline-balance-audit-v1', rulesVersion: rules.version,
    rulesStatus: rules.status, rulesAcceptedOn: rules.acceptedOn,
    layoutBounds: rules.layoutBounds,
    scope: 'Exhaustive bounded regular-ring geometry; descriptive trait examples, not Minecraft gameplay balance.',
    capacities: {} };
  for (const slots of [4, 8]) {
    const layouts = [...enumerateLeylineLayouts(slots, rules.layoutBounds)], geometries = layouts.map(leylineLayoutGeometry);
    const selections = full ? Object.keys(rules.traits).map(trait => [trait]) : LEYLINE_SCHOOLS.map(trait => [trait]);
    if (full) {
      for (let i = 0; i < LEYLINE_SCHOOLS.length; i++) for (let j = i + 1; j < LEYLINE_SCHOOLS.length; j++) selections.push([LEYLINE_SCHOOLS[i], LEYLINE_SCHOOLS[j]]);
      selections.push(['fire', 'evocation'], ['earth', 'abjuration'], ['water', 'conjuration'], ['mind', 'illusion'], ['fire', 'earth'], LEYLINE_SCHOOLS, rules.natureFocus.traits, rules.pyramidFocus.traits);
    }
    // Only school vectors are needed for cross-school comparisons. Release other
    // vectors after computing their summary so large domains do not retain every cast.
    const scenarios = new Map();
    for (const traits of selections) {
      const scenario = analyzeScenario(rules, layouts, geometries, traits);
      const school = traits.length === 1 && LEYLINE_SCHOOLS.includes(traits[0]);
      scenarios.set(traits.join('+'), school ? scenario : { summary: scenario.summary });
    }
    const schools = LEYLINE_SCHOOLS.map(trait => scenarios.get(trait));
    const diagnostics = {};
    for (let column = 0; column < LEYLINE_AXES.length; column++) {
      const axis = LEYLINE_AXES[column], sign = direction(axis);
      const allWinners = intersection(schools.map(school => school.winningSets[column]));
      let bestWorstFraction = -Infinity, compromise = 0;
      for (let i = 0; i < layouts.length; i++) {
        const worstFraction = Math.min(...schools.map(school => {
          const { min, max } = school.summary.axes[axis];
          return max - min < 1e-12 ? 1 : sign === 1 ? (school.outputs[i][axis] - min) / (max - min) : (max - school.outputs[i][axis]) / (max - min);
        }));
        if (worstFraction > bestWorstFraction + 1e-12 || (Math.abs(worstFraction - bestWorstFraction) <= 1e-12 && preferLeylineLayout(layouts[i], layouts[compromise]))) {
          bestWorstFraction = worstFraction; compromise = i;
        }
      }
      diagnostics[axis] = { universalMaximizers: allWinners.length,
        bestWorstAttainableFraction: round(bestWorstFraction), compromiseLayout: layouts[compromise],
        distinctSchoolWinners: new Set(schools.map(school => JSON.stringify(school.summary.axes[axis].bestLayout))).size };
      if (allWinners.length) failures.push(`${slots} slots: ${axis} has a universal school optimum`);
      if (bestWorstFraction > .90) failures.push(`${slots} slots: ${axis} has a near-universal school optimum (>90% of each school's attainable span)`);
      if (diagnostics[axis].distinctSchoolWinners < 4) failures.push(`${slots} slots: ${axis} has fewer than four distinct school winners`);
    }
    const envelopes = schools.map(school => outcomeAxes.reduce((sum, axis) => sum + Math.log2(school.summary.axes[axis].max), 0));
    const target = slots === 4 ? .60 : .96;
    if (envelopes.some(value => Math.abs(value - target) > 1e-6)) failures.push(`${slots} slots: school opportunity envelope drift`);
    const peakRatios = Object.fromEntries(outcomeAxes.map(axis => {
      const peaks = schools.map(school => school.summary.axes[axis].max);
      return [axis, round(Math.max(...peaks) / Math.min(...peaks))];
    }));
    if (Object.values(peakRatios).some(ratio => ratio > 1.15)) failures.push(`${slots} slots: school peak spread exceeds 15%`);
    const cheapest = schools.map(school => school.summary.axes.cost.min);
    const costMinimumRatio = Math.max(...cheapest) / Math.min(...cheapest);
    if (costMinimumRatio > 1.20) failures.push(`${slots} slots: school minimum-cost spread exceeds 20%`);
    for (const [key, scenario] of scenarios) {
      if (scenario.summary.jointMaximizers) failures.push(`${slots} slots: ${key} has an optimum for all four objectives`);
      if (scenario.summary.affordability.costExceedsEveryGain) failures.push(`${slots} slots: ${key} outcome optimum costs more than every individual outcome gain`);
      if (scenario.summary.affordability.maxOutcomeReduction > .11) failures.push(`${slots} slots: ${key} outcome optimum has more than 11% collateral outcome loss`);
    }
    const heightRetention = Math.min(...schools.flatMap(school => LEYLINE_AXES.map(axis => school.summary.axes[axis].neighborRetention.height)).filter(value => value !== null));
    const distanceRetention = Math.min(...schools.flatMap(school => LEYLINE_AXES.map(axis => school.summary.axes[axis].neighborRetention.distance)).filter(value => value !== null));
    if (heightRetention < .92) failures.push(`${slots} slots: one-block height neighbor loses more than 8% of a school optimum`);
    if (distanceRetention < .85) failures.push(`${slots} slots: one-block spacing neighbor loses more than 15% of a school optimum`);
    const sizeRange = group => Object.fromEntries(LEYLINE_AXES.map(axis => {
      const widths = group.map(scenario => scenario.summary.axes[axis].bestSize.width).sort((a, b) => a - b);
      return [axis, { min: widths[0], median: (widths[Math.floor((widths.length - 1) / 2)] + widths[Math.ceil((widths.length - 1) / 2)]) / 2, max: widths.at(-1) }];
    }));
    report.capacities[slots] = { layouts: layouts.length, scenarios: scenarios.size,
      evaluations: layouts.length * scenarios.size, diagnostics,
      affordability: Object.fromEntries([['allExamples', [...scenarios.values()]], ['singleTraits', [...scenarios.values()].filter(s => s.summary.traits.length === 1)]].map(([name, group]) => [name, {
        outcomeGoalWinners: group.reduce((sum, s) => sum + s.summary.affordability.outcomeGoalWinners, 0),
        costExceedsEveryGain: group.reduce((sum, s) => sum + s.summary.affordability.costExceedsEveryGain, 0),
        maxOutcomeReduction: Math.max(...group.map(s => s.summary.affordability.maxOutcomeReduction))
      }])),
      winningFootprints: { schools: sizeRange(schools), singleTraits: sizeRange(Object.keys(rules.traits).filter(trait => scenarios.has(trait)).map(trait => scenarios.get(trait))) },
      schoolOpportunity: { peakLogSum: { min: round(Math.min(...envelopes)), max: round(Math.max(...envelopes)) },
        peakRatios, costMinimumRatio: round(costMinimumRatio),
        neighborRetention: { height: round(heightRetention), distance: round(distanceRetention) } },
      results: Object.fromEntries([...scenarios].map(([key, scenario]) => [key, scenario.summary])) };
  }
  report.failures = failures;
  return report;
}

export function renderLeylineBalanceReport(report) {
  const lines = ['# Leyline school balance audit', '', '**October 4, 2026 · accepted numerical design baseline, not verified gameplay balance.**', '',
    'The owner locked in the current v3 matrices, shared functions, geometry domain, equal log-space trait blend, minor balancing offsets and final-amount rounding. Numerical evidence below verifies that baseline. Native apparatus shaping and stored crafted modifiers remain to be implemented.', '',
    'The sweep uses the same rules, geometry domain and tie policy as the calculator. Higher Amplify, Range and Area and lower Casting Cost define the four separate objectives. No overall utility score is assumed.', '',
    'A layout dominating every alternative would have to achieve all four extrema at once. None does in any audited example. Schools also have no common maximum for any individual objective, including Casting Cost. That does not mean every layout is useful, or that a chosen weighted average cannot have a best compromise.', '',
    'Near-universal diagnostics normalize each school’s worst-to-best span to 0–1 for one objective, then find the layout with the highest minimum fraction across the eight schools. This is an audit diagnostic, not an output stat or gameplay power score. A value above 0.90 fails the guard.', '',
    'School opportunities share a sum of single-axis peak log multipliers of 0.60 with four slots and 0.96 with eight. These separately achievable peaks need not occur in the same layout. Specialty allocations differ: Evocation favors potency, Divination reach, Illusion coverage; other schools distribute the same envelope more evenly. This authoring guard compares access to modifiers, not damage, healing, utility or spell power.', ''];
  for (const [slots, data] of Object.entries(report.capacities)) {
    lines.push(`## ${slots} slots`, '', `${data.layouts.toLocaleString('en-US')} layouts × ${data.scenarios} examples = ${data.evaluations.toLocaleString('en-US')} evaluations.`, '',
      '| School | Peak Amplify | Peak Range | Peak Area | Minimum Casting Cost |', '| --- | ---: | ---: | ---: | ---: |');
    for (const school of LEYLINE_SCHOOLS) {
      const axes = data.results[school].axes;
      lines.push(`| ${school[0].toUpperCase() + school.slice(1)} | ${axes.amplify.max.toFixed(3)} | ${axes.range.max.toFixed(3)} | ${axes.area.max.toFixed(3)} | ${axes.cost.min.toFixed(3)} |`);
    }
    lines.push('', '**Each column is optimized separately.** The row is not a simultaneously available result.', '',
      '| Objective | Common school maximizers | Distinct school winners | Best worst-school fraction |', '| --- | ---: | ---: | ---: |');
    for (const axis of LEYLINE_AXES) {
      const d = data.diagnostics[axis];
      lines.push(`| ${axis === 'cost' ? 'Casting Cost' : axis[0].toUpperCase() + axis.slice(1)} | ${d.universalMaximizers} | ${d.distinctSchoolWinners} | ${(100 * d.bestWorstAttainableFraction).toFixed(1)}% |`);
    }
    lines.push('', `Affordability check: **${data.affordability.allExamples.costExceedsEveryGain} of ${data.affordability.allExamples.outcomeGoalWinners}** outcome-goal winners have a Casting Cost factor greater than every individual outcome factor (${data.affordability.singleTraits.costExceedsEveryGain}/${data.affordability.singleTraits.outcomeGoalWinners} single-trait winners). Largest collateral outcome reduction is **${(100 * data.affordability.allExamples.maxOutcomeReduction).toFixed(1)}%**. These are numerical tuning guards, not a claim that Range, Area and Amplify have interchangeable gameplay value.`, '');
    lines.push('', `Worst one-block neighbor retains ${(100 * data.schoolOpportunity.neighborRetention.height).toFixed(1)}% of its objective value for height changes and ${(100 * data.schoolOpportunity.neighborRetention.distance).toFixed(1)}% for spacing changes. Shape switches are discrete, not smooth.`, '');
    lines.push('| Goal | School winner footprint sides · min / median / max | Single-trait winner footprint sides · min / median / max |', '| --- | ---: | ---: |');
    for (const axis of LEYLINE_AXES) {
      const values = group => { const s = data.winningFootprints[group][axis]; return `${s.min} / ${s.median} / ${s.max}`; };
      lines.push(`| ${axis === 'cost' ? 'Casting Cost' : axis[0].toUpperCase() + axis.slice(1)} | ${values('schools')} | ${values('singleTraits')} |`);
    }
    lines.push('', 'Footprints are square occupied block bounds, including boundary node blocks and excluding optional masonry/access space. Diagonal offsets use their axis-aligned box, not twice their radial distance. Medians may fall between realizable sizes. These are selected goal winners, not all allowed builds.', '');
  }
  lines.push('## Scope and repeatability', '',
    'Full coverage includes all 46 configured single traits, all 28 two-school combinations, Fire + Evocation, Earth + Abjuration, Water + Conjuration, Mind + Illusion, Fire + Earth, the eight-school mixture, Life + Plant + Wood (the Nature focus) and Divination + Light (the pyramid focus): 82 examples per capacity. Every example is checked for finite bounds and absence of a joint four-objective optimum. Numerical parity across every possible trait subset is not claimed.', '',
    'School-only guards also cover equal opportunity envelopes, at most 15% peak spread per outcome axis, at most 20% minimum-cost spread, at least four distinct school winners per objective, absence of a near-universal winner and neighboring-layout retention. Cost minima differ because reducing different outcomes supplies different payment reductions. Cheaper does not imply stronger.', '',
    'The October 4 affordability review found excessive surcharges across many traits: before correction, 120/246 four-slot and 142/246 eight-slot outcome-goal winners cost more than every individual outcome gain (75/138 and 88/138 single-trait winners). The first correction moderated positive combined cost logs by 0.30 and left negative logs unchanged. It changed neither the outcome matrices nor the independent efficiency preferences. That continuous monotone curve preserved cost-optimum ordering and existing discounts. The later minor-offset response below is the accepted baseline. The affordability guard rejects a return to excessive goal-winner surcharges.', '',
    'Final applied amounts round once to the nearest whole unit after composing all modifiers; nonnegative exact halves round up. The calculator applies this to each cost component separately and keeps absent costs zero. Modifiers and intermediate expressions retain full precision. The optimizer compares exact factors; rounded costs can tie for a particular base amount. Native application to costs and numerical outcomes remains future work.', '',
    'The owner clarified that a matching structure should be largely beneficial for its type, with only minor balancing offsets. The shared outcome response keeps full positive gains and raises bounded sub-neutral outcome factors to the power 0.30. Neutral remains 1; response ordering and smooth neighborhoods remain. This limits Amplify losses to about 8.3% and Range/Area losses to about 10.1%, rather than the earlier 25%/30% floors. A new guard rejects outcome-goal winners with collateral losses above 11%. Positive combined cost logs now use 0.20 for smaller payment offsets; negative logs retain full value. Reduced outcome losses also change cost refunds, so school Cost cells were refitted offline toward minimum factors of 0.90/0.83 where required, preserving better results. Previously dormant independent cost cells are activated; Transmutation cost favors first distance 4 for a broader efficiency neighborhood. Outcome cells and their individual peak opportunities remain unchanged. The complete audit is regenerated rather than assuming efficiency results stay identical.', '',
    'The old common Range distance bonus strongly favored maximum spread for almost every school. Common distance/shape terms are now one quarter as strong; the common Amplify height bias is zero. Distance width increases from 0.8 to 1.1. The six independent matrix rows still produce all four modifiers. Transmutation’s first-distance motif changes from 2 to 3, with Range preferring 4, and Abjuration’s Area outer-gap preference changes from 2 to 1, so their beneficial responses do not sit entirely on the neutral reference.', '',
    'School Casting Cost columns have independently authored efficiency preferences. Coefficient fitting aims for minimum factors around 0.90 with four slots and 0.83 with eight; where existing outcome tradeoffs already provide a lower minimum, no extra discount is added. These aims are not runtime clamps. The shared weaker-outcome reduction exponent decreases from 0.35 to 0.18, reducing the universal appeal of weakening everything for a refund. Cost remains uniform across each existing payment component and is not reweighted by outcome applicability.', '',
    'Schools use the same evaluator as every other descriptive trait. No school-specific runtime branch, raw trait-point boost, capability-dependent cost reweighting or net-height shortcut is added. School coefficient fitting was an offline authoring step; the stored cells remain independently editable data.', '',
    'The monument pass expands the proposal domain to inner radius 8, outer radius 16 and independent height steps ±6. Divination and Light favor square descending pyramid terraces for reach, with distinct potency/coverage/cost preferences. Every school’s prior single-axis outcome peak and opportunity envelope is preserved by offline coefficient fitting; Light retains its prior outcome peaks. Casting Cost cells and shared modifier limits are unchanged. Larger bounds can expose new minima and other descriptive-trait winners. See the [pyramid design](design/leyline-calculator.md#large-pyramid-observatory) for coordinates and tradeoffs.', '',
    'Run `node tools/audit_leyline_balance.mjs` to regenerate this report and its [JSON evidence](leyline-balance-audit.json); `--check` verifies freshness and every guard. Run `node --test tools/test_leyline_calculator.mjs tools/test_leyline_balance.mjs` for arithmetic and regression checks.', '',
    'An optimal layout still exists once traits, capacity and a particular goal are specified. All-round compromise layouts can exist after someone supplies weights or a crafting workload. The mathematical requirement here is the absence of a universal dominant choice, not the absence of any optimizer result.', '',
    'This accepted design is not installed into the game. Real costs, unused outcome consumers, integer rounding, Area radius versus covered area, local imbuements, arbitrary non-ring structures, optional mods and multiplayer encounters still need native implementation and gameplay review. Free/passive outputs cannot pay a tradeoff through absent costs. In particular, current Divination spells do not explicitly read Amplify; a displayed factor does not create sensing strength. Nature spells with authored Amplify reads can consume that modifier once integration exists.', '');
  return lines.join('\n');
}

if (process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1]) {
  const rules = JSON.parse(readFileSync(new URL('../docs/design/leyline-calculator-v3.json', import.meta.url)));
  const report = auditLeylineBalance(rules);
  if (report.failures.length) throw new Error(report.failures.join('\n'));
  for (const [name, content] of [['leyline-balance-audit.json', JSON.stringify(report, null, 2) + '\n'], ['leyline-balance-audit.md', renderLeylineBalanceReport(report)]]) {
    const path = new URL('../docs/' + name, import.meta.url);
    if (process.argv.includes('--check')) {
      if (readFileSync(path, 'utf8') !== content) throw new Error('Stale report: ' + name);
    } else writeFileSync(path, content);
  }
  console.log(`Leyline balance ${process.argv.includes('--check') ? 'verified' : 'recorded'}: ${Object.values(report.capacities).reduce((sum, capacity) => sum + capacity.evaluations, 0).toLocaleString('en-US')} evaluations, all guards pass`);
}
