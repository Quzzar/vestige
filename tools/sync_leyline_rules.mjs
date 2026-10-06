import fs from 'node:fs';
import {enumerateLeylineLayouts, leylineLayoutGeometry, resolveLeylineModifiers, validateLeylineRules} from './leyline_calculator.mjs';
const rulesText = fs.readFileSync('docs/design/leyline-calculator-v3.json', 'utf8');
const rules = validateLeylineRules(JSON.parse(rulesText));
const layouts = [...enumerateLeylineLayouts(4, rules.layoutBounds)];
// Cover every four-slot layout and deterministic samples of the larger eight-slot domain.
let index = 0;
for (const layout of enumerateLeylineLayouts(8, rules.layoutBounds)) if (index++ % 137 === 0) layouts.push(layout);
const traits = Object.keys(rules.traits);
const cases = layouts.map((layout, i) => {
  const selected = i % 3 === 0 ? [traits[i % traits.length]] : [traits[i % traits.length], traits[(i + 13) % traits.length], traits[(i + 29) % traits.length]];
  return {layout, traits: selected, modifiers: resolveLeylineModifiers(rules, leylineLayoutGeometry(layout), selected).modifiers};
});
for (const [file, text] of [
  ['src/main/resources/data/vestige/leyline_rules.json', rulesText],
  ['src/test/resources/leyline-parity.json', JSON.stringify(cases) + '\n']
]) {
  if (process.argv.includes('--check')) {
    if (fs.readFileSync(file, 'utf8') !== text) throw new Error('Leyline data drift: ' + file);
  } else {
    fs.mkdirSync(file.substring(0, file.lastIndexOf('/')), {recursive: true}); fs.writeFileSync(file, text);
  }
}
console.log(`${cases.length} native parity fixtures; accepted rules synchronized`);
