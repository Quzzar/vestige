import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { enumerateLeylineLayouts, leylineLayoutGeometry, leylineLayoutSize, preferLeylineLayout, resolveLeylineModifiers } from './leyline_calculator.mjs';
import { auditLeylineBalance, LEYLINE_SCHOOLS } from './audit_leyline_balance.mjs';

const rules = JSON.parse(readFileSync(new URL('../docs/design/leyline-calculator-v3.json', import.meta.url)));

test('the monument search domain has 169/43940 unique valid block-grid arrangements', () => {
  for (const [slots, expected] of [[4, 169], [8, 43940]]) {
    const layouts = [...enumerateLeylineLayouts(slots, rules.layoutBounds)];
    assert.equal(layouts.length, expected);
    assert.equal(new Set(layouts.map(layout => JSON.stringify(layout))).size, expected);
    for (const layout of layouts) {
      const geometry = leylineLayoutGeometry(layout);
      assert.ok(geometry.d1 > 0 && geometry.d1 <= rules.layoutBounds.innerRadius);
      if (slots === 4) assert.deepEqual(Object.keys(geometry), ['active', 'innerShape', 'd1', 'dy1']);
      else {
        assert.ok(geometry.d2 > 0 && layout.outer * (layout.outerShape === 'diagonal' ? Math.SQRT2 : 1) > geometry.d1);
        assert.equal(geometry.dy2, layout.outerStep);
      }
    }
  }
  assert.throws(() => [...enumerateLeylineLayouts(6)], /four or eight/);
  assert.equal([...enumerateLeylineLayouts(4)].length,90, 'Legacy default domain remains compatible');
  assert.equal([...enumerateLeylineLayouts(8)].length,11745);
  assert.throws(() => [...enumerateLeylineLayouts(8,{innerRadius:8,outerRadius:8,heightStep:6})], /Outer radius/);
  assert.throws(() => [...enumerateLeylineLayouts(8,{innerRadius:8,outerRadius:16,heightStep:6.5})], /Invalid layout bound/);
  assert.ok(preferLeylineLayout({ slots: 4, innerShape: 'cross', inner: 2, innerHeight: 0 }, { slots: 4, innerShape: 'cross', inner: 2, innerHeight: 1 }));
});

test('the complete 82-example monument sweep has fair school opportunities and no universal optimum', () => {
  const report = auditLeylineBalance(rules);
  assert.deepEqual(report.failures, []);
  assert.equal(report.capacities[4].scenarios, 82);
  assert.equal(report.capacities[8].scenarios, 82);
  assert.equal(report.capacities[4].evaluations + report.capacities[8].evaluations, 3616938);
  for (const capacity of Object.values(report.capacities)) {
    for (const result of Object.values(capacity.results)) assert.equal(result.jointMaximizers, 0);
    for (const result of Object.values(capacity.diagnostics)) {
      assert.equal(result.universalMaximizers, 0);
      assert.ok(result.distinctSchoolWinners >= 4);
    }
    for(const [name, winners] of [['allExamples',246],['singleTraits',138]]) {
      assert.equal(capacity.affordability[name].outcomeGoalWinners,winners);
      assert.equal(capacity.affordability[name].costExceedsEveryGain,0);
      assert.ok(capacity.affordability[name].maxOutcomeReduction<=.11);
    }
  }
});

test('the minor-offset guard rejects large collateral losses at otherwise optimized layouts', () => {
  const changed = structuredClone(rules);
  changed.scaling.outcomeDownsideExponent = 1;
  assert.ok(auditLeylineBalance(changed, { full: false }).failures.some(failure => failure.includes('collateral outcome loss')));
});

test('the affordability guard rejects the old excessive cost-increase curve', () => {
  const changed = structuredClone(rules);
  changed.cost.increaseExponent = 1;
  assert.ok(auditLeylineBalance(changed, { full: false }).failures.some(failure => failure.includes('costs more than every individual outcome gain')));
});

test('occupied footprint sizes use block coordinates and ignore inactive outer geometry', () => {
  const layout = { slots: 8, innerShape: 'cross', outerShape: 'diagonal', inner: 5, outer: 4, innerHeight: -2, outerStep: 3 };
  assert.deepEqual(leylineLayoutSize(layout), { width: 11, depth: 11, heightSpan: 3 });
  assert.deepEqual(leylineLayoutSize({ ...layout, slots: 4, outer: 12, outerStep: 8 }), { width: 11, depth: 11, heightSpan: 2 });
  assert.deepEqual(leylineLayoutSize({ ...layout, inner: 1, outer: 12, innerHeight: 4, outerStep: 4 }), { width: 25, depth: 25, heightSpan: 8 });
});

test('Nature potency favors a level stone circle with a useful smaller neighbor', () => {
  assert.deepEqual(rules.natureFocus.traits, ['life', 'plant', 'wood']);
  assert.ok(!Object.hasOwn(rules.traits, 'nature'), 'Nature is a focus label, not a new native trait');
  const preset = rules.natureFocus.preset;
  const result = resolveLeylineModifiers(rules, leylineLayoutGeometry(preset), rules.natureFocus.traits).modifiers;
  for (const layout of enumerateLeylineLayouts(8,rules.layoutBounds)) {
    const m = resolveLeylineModifiers(rules, leylineLayoutGeometry(layout), rules.natureFocus.traits).modifiers;
    assert.ok(m.amplify <= result.amplify + 1e-12, 'Stone circle must remain a Nature potency optimum');
  }
  const compact = resolveLeylineModifiers(rules, leylineLayoutGeometry({ ...preset, inner: 4, outer: 3 }), rules.natureFocus.traits).modifiers;
  assert.ok((compact.amplify - 1) / (result.amplify - 1) > .95, 'Compact alternative retains at least 95% of the extra potency');
  assert.ok(compact.cost < result.cost, 'Compact compromise is cheaper');
  for (const trait of rules.natureFocus.traits) for (const factor of ['dy1', 'dy2']) {
    assert.ok(rules.traits[trait][factor].every(([preferred]) => preferred === 0), 'Nature favors level node bases');
  }
  const larger = resolveLeylineModifiers(rules, leylineLayoutGeometry({ ...preset, inner: 6, outer: 8 }), rules.natureFocus.traits).modifiers;
  assert.ok(larger.amplify < result.amplify, 'Expanding beyond the preferred circle cannot indefinitely award potency');
});

test('a large terraced pyramid is a Range optimum, with independent heights and useful smaller neighbors', () => {
  const {preset,traits,goal}=rules.pyramidFocus;
  assert.equal(goal,'range');
  assert.deepEqual(traits,['divination','light']);
  assert.deepEqual(leylineLayoutSize(preset),{width:33,depth:33,heightSpan:12});
  const calculate=layout=>resolveLeylineModifiers(rules,leylineLayoutGeometry(layout),traits).modifiers;
  const peak=calculate(preset);
  for(const layout of enumerateLeylineLayouts(8,rules.layoutBounds))assert.ok(calculate(layout).range<=peak.range+1e-12);
  const smaller=calculate({...preset,inner:7,outer:14,innerHeight:-5,outerStep:-5});
  assert.ok((smaller.range-1)/(peak.range-1)>.90, '29×29 neighbor retains over 90% of extra reach');
  // Evaluate the curve outside the placement domain to distinguish a true
  // preferred scale from a bonus that merely follows the maximum size limit.
  assert.ok(calculate({...preset,inner:9,outer:18,innerHeight:-7,outerStep:-7}).range<peak.range);
  assert.ok(calculate({...preset,innerHeight:0,outerStep:0}).range<peak.range, 'Distance alone does not give the full pyramid response');
  assert.notEqual(calculate({...preset,innerHeight:-5}).range,calculate({...preset,outerStep:-5}).range, 'Height steps are independent');
  const stone=resolveLeylineModifiers(rules,leylineLayoutGeometry(preset),['earth']).modifiers;
  const compact=resolveLeylineModifiers(rules,leylineLayoutGeometry({slots:8,innerShape:'cross',outerShape:'cross',inner:1,outer:2,innerHeight:0,outerStep:0}),['earth']).modifiers;
  assert.ok(compact.amplify>stone.amplify, 'The monument is not a universal size reward');
});

test('the audit rejects a common school response even when all values remain bounded', () => {
  const changed = structuredClone(rules);
  for (const school of LEYLINE_SCHOOLS) changed.traits[school] = structuredClone(rules.traits.evocation);
  const failures = auditLeylineBalance(changed, { full: false }).failures;
  assert.ok(failures.some(failure => failure.includes('universal school optimum')));
  assert.ok(failures.some(failure => failure.includes('near-universal school optimum')));
  assert.ok(failures.some(failure => failure.includes('fewer than four distinct')));
});

test('the audit rejects a school receiving a larger modifier opportunity budget', () => {
  const changed = structuredClone(rules);
  for (const cells of Object.values(changed.traits.evocation)) for (let axis = 0; axis < 3; axis++) cells[axis][1] *= 2;
  assert.ok(auditLeylineBalance(changed, { full: false }).failures.some(failure => failure.includes('school opportunity envelope drift')));
});

test('the audit catches narrow spikes that make one-block height neighbors poor', () => {
  const changed = structuredClone(rules);
  changed.factors.dy1.width = .15;
  changed.factors.dy2.width = .15;
  assert.ok(auditLeylineBalance(changed, { full: false }).failures.some(failure => failure.includes('height neighbor')));
});
