// Accepted v3 reference evaluator, shared with the native leyline implementation.
export const LEYLINE_AXES = ['amplify', 'range', 'area', 'cost'];
const LEYLINE_OUTCOME_AXES = LEYLINE_AXES.filter(axis => axis !== 'cost');

const clampLeyline = (value, bounds) => Math.max(bounds[0], Math.min(bounds[1], value));
const emptyAxes = () => Object.fromEntries(LEYLINE_AXES.map(axis => [axis, 0]));

const leylineShapeRadius = shape => shape === 'diagonal' ? Math.SQRT2 : 1;
const legacyLeylineBounds = Object.freeze({ innerRadius: 6, outerRadius: 12, heightStep: 4 });

export function validateLeylineLayoutBounds(bounds) {
  for (const key of ['innerRadius', 'outerRadius', 'heightStep']) {
    if (!Number.isInteger(bounds[key]) || bounds[key] < 1) throw new Error('Invalid layout bound: ' + key);
  }
  if (bounds.outerRadius <= bounds.innerRadius) throw new Error('Outer radius limit must exceed inner radius limit');
  return bounds;
}

// This is the prototype optimizer's bounded block-grid domain, not world placement rules.
export function* enumerateLeylineLayouts(slots, bounds = legacyLeylineBounds) {
  if (![4, 8].includes(slots)) throw new Error('Expected four or eight slots');
  validateLeylineLayoutBounds(bounds);
  const { innerRadius, outerRadius, heightStep } = bounds;
  for (const innerShape of ['cross', 'diagonal']) for (let inner = 1; inner <= Math.floor(innerRadius / leylineShapeRadius(innerShape)); inner++) {
    if (slots === 4) {
      for (let innerHeight = -heightStep; innerHeight <= heightStep; innerHeight++) yield { slots, innerShape, inner, innerHeight };
    } else for (const outerShape of ['cross', 'diagonal']) {
      const minOuter = Math.floor(inner * leylineShapeRadius(innerShape) / leylineShapeRadius(outerShape)) + 1;
      for (let outer = minOuter; outer <= Math.floor(outerRadius / leylineShapeRadius(outerShape)); outer++) {
        for (let innerHeight = -heightStep; innerHeight <= heightStep; innerHeight++) for (let outerStep = -heightStep; outerStep <= heightStep; outerStep++) {
          yield { slots, innerShape, outerShape, inner, outer, innerHeight, outerStep };
        }
      }
    }
  }
}

export function leylineLayoutGeometry(layout) {
  const d1 = layout.inner * leylineShapeRadius(layout.innerShape);
  const geometry = { active: layout.slots === 8, innerShape: layout.innerShape, d1, dy1: layout.innerHeight };
  if (geometry.active) {
    const radius = layout.outer * leylineShapeRadius(layout.outerShape);
    geometry.outerShape = layout.outerShape;
    geometry.d2 = Math.sqrt(d1 ** 2 + radius ** 2 - 2 * d1 * radius * (layout.innerShape === layout.outerShape ? 1 : Math.SQRT1_2));
    geometry.dy2 = layout.outerStep;
  }
  return geometry;
}

// Occupied block bounds, including node blocks; optional masonry/access space is excluded.
export function leylineLayoutSize(layout) {
  const outer = layout.slots === 8;
  const side = 2 * Math.max(layout.inner, outer ? layout.outer : 0) + 1;
  const levels = outer ? [0, layout.innerHeight, layout.innerHeight + layout.outerStep] : [0, layout.innerHeight];
  return { width: side, depth: side, heightSpan: Math.max(...levels) - Math.min(...levels) };
}

export function leylineLayoutTieKey(layout) {
  const active = layout.slots === 8;
  const heights = active ? [0, layout.innerHeight, layout.innerHeight + layout.outerStep] : [0, layout.innerHeight];
  const radius = layout.inner * leylineShapeRadius(layout.innerShape);
  return [active ? layout.outer * leylineShapeRadius(layout.outerShape) : radius,
    Math.max(...heights) - Math.min(...heights), radius,
    Math.abs(layout.innerHeight) + (active ? Math.abs(layout.outerStep) : 0),
    layout.innerShape === 'cross' ? 0 : 1, active && layout.outerShape === 'diagonal' ? 1 : 0,
    layout.innerHeight, active ? layout.outerStep : 0];
}

export function preferLeylineLayout(left, right) {
  const a = leylineLayoutTieKey(left), b = leylineLayoutTieKey(right);
  for (let i = 0; i < a.length; i++) if (Math.abs(a[i] - b[i]) > 1e-12) return a[i] < b[i];
  return false;
}

export function validateLeylineRules(rules) {
  if (rules.version !== 'leyline-calculator-v3') throw new Error('Unsupported leyline rules');
  if (JSON.stringify(rules.axes) !== JSON.stringify(LEYLINE_AXES)) throw new Error('Unexpected matrix columns');
  if (rules.layoutBounds) validateLeylineLayoutBounds(rules.layoutBounds);
  if (!Number.isFinite(rules.cost.increaseExponent) || rules.cost.increaseExponent <= 0 || rules.cost.increaseExponent > 1) throw new Error('Invalid casting-cost increase exponent');
  if (!Number.isFinite(rules.scaling.outcomeDownsideExponent) || rules.scaling.outcomeDownsideExponent <= 0 || rules.scaling.outcomeDownsideExponent > 1) throw new Error('Invalid outcome downside exponent');
  const factors = Object.entries(rules.factors);
  if (factors.length !== 6) throw new Error('Expected six independent factors');
  for (const [name, factor] of factors) {
    if (!['shape', 'distance', 'height'].includes(factor.curve)) throw new Error('Unknown curve: ' + name);
    if (factor.curve !== 'shape' && (!Number.isFinite(factor.width) || factor.width <= 0)) throw new Error('Invalid curve width: ' + name);
  }
  for (const [trait, matrix] of Object.entries(rules.traits)) {
    if (Object.keys(matrix).length !== factors.length) throw new Error('Incomplete matrix: ' + trait);
    for (const [name, factor] of factors) {
      const cells = matrix[name];
      if (!Array.isArray(cells) || cells.length !== LEYLINE_AXES.length) throw new Error('Missing row: ' + trait + '/' + name);
      for (const cell of cells) {
        if (!Array.isArray(cell) || cell.length !== 2 || !Number.isFinite(cell[1])) throw new Error('Invalid cell: ' + trait + '/' + name);
        if (factor.curve === 'shape' ? !['cross', 'diagonal'].includes(cell[0]) : !Number.isFinite(cell[0])) throw new Error('Invalid preference: ' + trait + '/' + name);
        if (factor.curve === 'distance' && cell[0] <= 0) throw new Error('Invalid preferred distance: ' + trait + '/' + name);
      }
    }
  }
  return rules;
}

export function leylineCellContribution(factor, actual, cell) {
  const [preferred, coefficient] = cell;
  const response = value => {
    if (factor.curve === 'shape') return value === preferred ? 1 : -1;
    const delta = factor.curve === 'distance' ? Math.log2(value / preferred) : value - preferred;
    return Math.exp(-0.5 * (delta / factor.width) ** 2);
  };
  return coefficient * (response(actual) - response(factor.reference));
}

export function leylineTraitContributions(rules, geometry, trait) {
  const matrix = rules.traits[trait];
  return Object.fromEntries(Object.entries(rules.factors).map(([name, factor]) => [name,
    Object.fromEntries(LEYLINE_AXES.map((axis, column) => [axis,
      !matrix || (factor.outer && !geometry.active) ? 0 : leylineCellContribution(factor, geometry[name], matrix[name][column])
    ]))
  ]));
}

export function leylineCommonContributions(rules, geometry) {
  const rows = Object.fromEntries(Object.keys(rules.factors).map(name => [name, emptyAxes()]));
  const k = rules.scaling;
  const d1 = clampLeyline(Math.log2(geometry.d1 / rules.factors.d1.reference), k.distanceLogBounds);
  const d2 = geometry.active ? clampLeyline(Math.log2(geometry.d2 / rules.factors.d2.reference), k.distanceLogBounds) : 0;
  rows.dy1.amplify = k.amplifyInnerHeight * geometry.dy1;
  rows.d1.range = k.rangeDistance * d1;
  rows.d1.area = k.areaInnerDistance * d1;
  rows.innerShape.range = geometry.innerShape === 'diagonal' ? k.rangeDiagonal : 0;
  if (geometry.active) {
    rows.dy2.amplify = k.amplifyOuterHeight * geometry.dy2;
    rows.d2.area = k.areaOuterDistance * d2;
    rows.outerShape.area = geometry.outerShape === 'diagonal' ? k.areaOuterDiagonal : 0;
  } else {
    rows.innerShape.area = geometry.innerShape === 'diagonal' ? k.areaInnerDiagonal : 0;
  }
  return rows;
}

export function resolveLeylineModifiers(rules, geometry, selectedTraits) {
  const traits = [...new Set(selectedTraits)].filter(trait => Object.hasOwn(rules.traits, trait)).sort();
  const common = leylineCommonContributions(rules, geometry);
  const responses = traits.map(trait => leylineTraitContributions(rules, geometry, trait));
  const factorLogs = Object.fromEntries(Object.keys(rules.factors).map(name => [name,
    Object.fromEntries(LEYLINE_AXES.map(axis => [axis, common[name][axis] +
      (traits.length ? responses.reduce((sum, response) => sum + response[name][axis], 0) / traits.length : 0)
    ]))
  ]));
  const rawLogs = Object.fromEntries(LEYLINE_AXES.map(axis => [axis,
    Object.values(factorLogs).reduce((sum, row) => sum + row[axis], 0)
  ]));
  const limitLogs = emptyAxes();
  const outcomeResponseLogs = emptyAxes();
  const modifiers = Object.fromEntries(LEYLINE_OUTCOME_AXES.map(axis => {
    const boundedLog = Math.log2(clampLeyline(2 ** rawLogs[axis], rules.bounds[axis]));
    limitLogs[axis] = boundedLog - rawLogs[axis];
    // Keep full positive specialization and soften bounded negative responses.
    // Applying this after the bound limits collateral losses to about 10%.
    outcomeResponseLogs[axis] = boundedLog < 0 ? boundedLog * (rules.scaling.outcomeDownsideExponent - 1) : 0;
    return [axis, 2 ** (boundedLog + outcomeResponseLogs[axis])];
  }));
  const tradeoffLogs = emptyAxes();
  tradeoffLogs.cost = LEYLINE_OUTCOME_AXES.reduce((sum, axis) => {
    const log = Math.log2(modifiers[axis]);
    return sum + rules.cost[axis + 'Weight'] * log * (log >= 0 ? rules.cost.benefitExponent : rules.cost.reductionExponent);
  }, 0);
  // Moderate only the combined cost increase. This continuous, strictly
  // increasing curve preserves efficiency preferences and every cost discount.
  const costLog = rawLogs.cost + tradeoffLogs.cost;
  const costResponseLogs = emptyAxes();
  costResponseLogs.cost = costLog > 0 ? costLog * (rules.cost.increaseExponent - 1) : 0;
  modifiers.cost = clampLeyline(2 ** (costLog + costResponseLogs.cost), rules.bounds.cost);
  limitLogs.cost = Math.log2(modifiers.cost) - rawLogs.cost - tradeoffLogs.cost - costResponseLogs.cost;
  return { modifiers, factorLogs, tradeoffLogs, outcomeResponseLogs, costResponseLogs, limitLogs };
}

// Apply after composing all modifiers, once, in the amount's declared unit.
// Fractional multipliers remain exact; absent amounts remain absent.
export function applyLeylineAmount(baseAmount, multiplier) {
  if (!Number.isFinite(baseAmount) || baseAmount < 0 || !Number.isFinite(multiplier) || multiplier < 0) throw new Error('Invalid final amount');
  const amount = baseAmount * multiplier;
  if (!Number.isFinite(amount)) throw new Error('Invalid final amount');
  return Math.round(amount);
}
