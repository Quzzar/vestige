import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import { LEYLINE_AXES, validateLeylineRules, leylineCellContribution, leylineTraitContributions, resolveLeylineModifiers, applyLeylineAmount } from './leyline_calculator.mjs';

const rules = JSON.parse(readFileSync(new URL('../docs/design/leyline-calculator-v3.json', import.meta.url)));
const names = Object.keys(rules.traits);
const reference = { active: true, innerShape: 'cross', outerShape: 'cross', d1: 2, d2: 2, dy1: 0, dy2: 0 };
const sample = { active: true, innerShape: 'cross', outerShape: 'diagonal', d1: 1, d2: 3, dy1: -3, dy2: 3 };
const near = (actual, expected) => assert.ok(Math.abs(actual - expected) < 1e-11, `${actual} != ${expected}`);

test('all 46 traits have complete six-by-four matrices with more than one response direction', () => {
  assert.equal(validateLeylineRules(rules), rules);
  assert.equal(names.length, 46);
  assert.deepEqual(LEYLINE_AXES, ['amplify', 'range', 'area', 'cost']);
  for (const matrix of Object.values(rules.traits)) {
    const a = matrix.d1, b = matrix.dy1;
    assert.ok(Math.abs(a[0][1] * b[1][1] - a[1][1] * b[0][1]) > 1e-6,
      'Matrix must not reduce to one score multiplied by axis weights');
    assert.notEqual(a[0][0], a[1][0], 'Potency and reach can prefer different distances');
  }
});

test('both neutral references give exactly one for every trait and combination', () => {
  for (const active of [false, true]) for (const traits of [[], names, ...names.map(name => [name])]) {
    assert.deepEqual(resolveLeylineModifiers(rules, { ...reference, active }, traits).modifiers,
      { amplify: 1, range: 1, area: 1, cost: 1 });
  }
});

test('shape, logarithmic distance and signed height cells agree with known arithmetic', () => {
  near(leylineCellContribution(rules.factors.innerShape, 'diagonal', ['cross', 0.1]), -0.2);
  near(leylineCellContribution({ ...rules.factors.d1, width: 0.8 }, 4, [4, 0.2]), 0.2 * (1 - Math.exp(-0.5 * (1 / 0.8) ** 2)));
  near(leylineCellContribution(rules.factors.dy1, 2, [2, 0.16]), 0.16 * (1 - Math.exp(-0.5)));
});

test('the two signed height steps stay independently observable even with equal net height', () => {
  const flat = resolveLeylineModifiers(rules, reference, ['fire']);
  const upDown = resolveLeylineModifiers(rules, { ...reference, dy1: 3, dy2: -3 }, ['fire']);
  assert.notDeepEqual(upDown.modifiers, flat.modifiers);
  const fixed = leylineTraitContributions(rules, sample, 'fire');
  for (let height = -4; height <= 4; height++) {
    const first = leylineTraitContributions(rules, { ...sample, dy1: height }, 'fire');
    assert.deepEqual(first.dy2, fixed.dy2);
    const second = leylineTraitContributions(rules, { ...sample, dy2: height }, 'fire');
    assert.deepEqual(second.dy1, fixed.dy1);
  }
});

test('editing a range cell changes range independently of amplify and area', () => {
  const changed = structuredClone(rules);
  changed.traits.fire.dy1[1][1] += 0.1;
  const before = resolveLeylineModifiers(rules, sample, ['fire']);
  const after = resolveLeylineModifiers(changed, sample, ['fire']);
  assert.notEqual(after.modifiers.range, before.modifiers.range);
  near(after.modifiers.amplify, before.modifiers.amplify);
  near(after.modifiers.area, before.modifiers.area);
  assert.notEqual(after.modifiers.cost, before.modifiers.cost, 'Shared output tradeoff still reacts to range');
});

test('casting cost has an independent matrix response, alongside the shared tradeoff', () => {
  const changed = structuredClone(rules);
  changed.traits.fire.dy1[3][1] -= 0.1;
  const before = resolveLeylineModifiers(rules, sample, ['fire']);
  const after = resolveLeylineModifiers(changed, sample, ['fire']);
  assert.notEqual(after.modifiers.cost, before.modifiers.cost);
  for (const axis of ['amplify', 'range', 'area']) near(after.modifiers[axis], before.modifiers[axis]);
  assert.deepEqual(after.tradeoffLogs, before.tradeoffLogs);
});

test('four-slot output and cost ignore every outer input, including absent values', () => {
  const basic = resolveLeylineModifiers(rules, { ...sample, active: false }, names);
  const other = resolveLeylineModifiers(rules,
    { ...sample, active: false, outerShape: 'anything', d2: NaN, dy2: undefined }, names);
  assert.deepEqual(other, basic);
  for (const key of ['outerShape', 'd2', 'dy2']) assert.ok(Object.values(basic.factorLogs[key]).every(value => value === 0));
});

test('all breakdown factors, tradeoff and output limits reconstruct all four final modifiers', () => {
  for (const geometry of [sample, { ...sample, dy1: -4, dy2: -4, d1: 6, d2: 8 }]) {
    const result = resolveLeylineModifiers(rules, geometry, ['fire', 'evocation', 'earth']);
    for (const axis of LEYLINE_AXES) {
      const total = Object.values(result.factorLogs).reduce((sum, row) => sum + row[axis], 0)
        + result.tradeoffLogs[axis] + result.outcomeResponseLogs[axis] + result.costResponseLogs[axis] + result.limitLogs[axis];
      near(2 ** total, result.modifiers[axis]);
    }
  }
});

test('outcome downsides are continuous, monotone and modest while positive gains stay whole', () => {
  for (const axis of ['amplify', 'range', 'area']) {
    const column = LEYLINE_AXES.indexOf(axis);
    let previous = 0;
    for (const log of [-2, -.2, -1e-8, 0, 1e-8, .2]) {
      const custom = structuredClone(rules);
      custom.traits = { example: Object.fromEntries(Object.entries(rules.traits.fire).map(([factor, cells]) => [factor, cells.map(([preferred]) => [preferred, 0])])) };
      custom.traits.example.innerShape[column] = ['cross', -log / 2];
      custom.scaling.rangeDiagonal = 0;
      const result = resolveLeylineModifiers(custom, { ...reference, innerShape: 'diagonal' }, ['example']);
      const bounded = Math.max(rules.bounds[axis][0], Math.min(rules.bounds[axis][1], 2 ** log));
      const expected = bounded < 1 ? bounded ** rules.scaling.outcomeDownsideExponent : bounded;
      near(result.modifiers[axis], expected);
      assert.ok(result.modifiers[axis] >= .898, 'No geometry downside exceeds about 10%');
      assert.ok(result.modifiers[axis] >= previous, 'Nearby layouts retain monotone response');
      previous = result.modifiers[axis];
    }
  }
  const invalid = structuredClone(rules);
  invalid.scaling.outcomeDownsideExponent = 0;
  assert.throws(() => validateLeylineRules(invalid), /downside exponent/);
});

test('combined cost increases are softened continuously while discounts retain their full value', () => {
  for (const value of [-.2, -1e-8, 0, 1e-8, .2]) {
    const custom = structuredClone(rules);
    custom.traits = { example: Object.fromEntries(Object.entries(rules.traits.fire).map(([factor, cells]) => [factor, cells.map(([preferred]) => [preferred, 0])])) };
    // Isolate one known cost response without any outcome tradeoff or common term.
    custom.traits.example.innerShape[3] = ['cross', -value / 2];
    custom.scaling.rangeDiagonal = 0;
    custom.scaling.areaInnerDiagonal = 0;
    const result = resolveLeylineModifiers(custom, { ...reference, innerShape: 'diagonal' }, ['example']);
    const finalLog = value > 0 ? value * rules.cost.increaseExponent : value;
    near(result.modifiers.cost, 2 ** finalLog);
    near(result.costResponseLogs.cost, finalLog - value);
  }
  const invalid = structuredClone(rules);
  invalid.cost.increaseExponent = 0;
  assert.throws(() => validateLeylineRules(invalid), /increase exponent/);
});

test('final applied amounts round once, preserve absent costs and do not round multipliers', () => {
  assert.equal(applyLeylineAmount(20, 1.386), 28);
  assert.equal(applyLeylineAmount(20, .8), 16);
  assert.equal(applyLeylineAmount(20, 1.5 * .75), 23);
  assert.equal(applyLeylineAmount(3, 1.15 * 1.15), 4, 'Round only after composing modifiers');
  assert.equal(applyLeylineAmount(1, .65), 1, 'Minimum allowed discount does not erase a whole-unit cost');
  for (const multiplier of [.65, 1, 1.386, 2]) assert.equal(applyLeylineAmount(0, multiplier), 0);
  for (const [base, multiplier] of [[NaN, 1], [-1, 1], [1, Infinity], [1, -1]]) assert.throws(() => applyLeylineAmount(base, multiplier), /final amount/);
});

test('trait names select data only; new names work without a runtime branch', () => {
  const changed = structuredClone(rules);
  changed.traits['example:new_trait'] = changed.traits.fire;
  assert.deepEqual(resolveLeylineModifiers(changed, sample, ['example:new_trait']), resolveLeylineModifiers(rules, sample, ['fire']));
  assert.deepEqual(resolveLeylineModifiers(rules, sample, ['missing']), resolveLeylineModifiers(rules, sample, []));
});

test('mixed responses are order/duplicate invariant and do not compound tags', () => {
  const a = resolveLeylineModifiers(rules, sample, ['fire', 'evocation', 'earth']);
  const b = resolveLeylineModifiers(rules, sample, ['earth', 'evocation', 'fire', 'fire']);
  assert.deepEqual(a, b);
  for (const factor of Object.keys(rules.factors)) for (const axis of LEYLINE_AXES) {
    const individuals = ['fire', 'evocation', 'earth'].map(trait => resolveLeylineModifiers(rules, sample, [trait]).factorLogs[factor][axis]);
    near(a.factorLogs[factor][axis], individuals.reduce((sum, value) => sum + value, 0) / 3);
  }
});

test('consumer/applicability metadata does not reweight casting cost', () => {
  // Consumer/applicability metadata must not reweight the shared cost calculation.
  assert.deepEqual(resolveLeylineModifiers(rules, { ...sample, uses: { area: false, range: false } }, ['fire', 'evocation']),
    resolveLeylineModifiers(rules, sample, ['fire', 'evocation']));
});

test('all traits remain finite and bounded over signed heights, shapes and stage gaps', () => {
  for (let i = 0; i < 180; i++) {
    const geometry = { active: i % 3 !== 0, innerShape: i % 2 ? 'cross' : 'diagonal', outerShape: i % 4 < 2 ? 'cross' : 'diagonal', d1: 1 + i % 6, d2: 1 + i % 10, dy1: i % 9 - 4, dy2: (i * 5) % 9 - 4 };
    for (const trait of names) for (const [axis, value] of Object.entries(resolveLeylineModifiers(rules, geometry, [trait]).modifiers)) {
      assert.ok(Number.isFinite(value) && value >= rules.bounds[axis][0] && value <= rules.bounds[axis][1]);
    }
  }
});

test('incomplete matrices and invalid cell preferences are rejected', () => {
  const changed = structuredClone(rules);
  changed.traits.fire.dy1.pop();
  assert.throws(() => validateLeylineRules(changed), /Missing row/);
  changed.traits.fire.dy1 = structuredClone(rules.traits.fire.dy1);
  changed.traits.fire.d1[0][0] = 0;
  assert.throws(() => validateLeylineRules(changed), /preferred distance/);
});
