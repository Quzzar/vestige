#!/usr/bin/env python3
"""Audit resolved parameters and exact numerical boost responses without simulating combat.

Facts remain symbolic. Unknown target density, accuracy, AI, event frequency, costs,
and utility are not collapsed into a pretend gameplay power score.
"""
import argparse
import itertools
import json
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
POLICY_PATH = ROOT / 'tools/spell-balance-policy.json'
DEFINITIONS = ROOT / 'src/main/resources/data/vestige/runtime_spells'
REPORT = ROOT / 'docs/spell-balance-audit.md'
DETAILS = ROOT / 'docs/spell-balance-audit.json'
SCALING = ('vestige:amplify', 'vestige:range', 'vestige:area')


def nodes(value, path=''):
    if isinstance(value, dict):
        yield path, value
        for key, inner in value.items():
            yield from nodes(inner, f'{path}/{key}')
    elif isinstance(value, list):
        for index, inner in enumerate(value):
            yield from nodes(inner, f'{path}/{index}')


def clean(poly):
    return {key: value for key, value in poly.items() if value != 0}


def polynomial(value, traits):
    """Resolve traits while keeping world facts as independent symbolic variables."""
    if isinstance(value, (int, float)) and not isinstance(value, bool):
        if not math.isfinite(value):
            raise ValueError('Non-finite expression')
        return clean({(): value})
    if not isinstance(value, dict) or len(value) != 1:
        raise ValueError(f'Unsupported expression: {value}')
    kind, inner = next(iter(value.items()))
    if kind == 'trait':
        return clean({(): traits.get(inner, 0)})
    if kind == 'fact':
        return {(inner,): 1}
    if kind == 'sum':
        result = {}
        for term in inner:
            for key, coefficient in polynomial(term, traits).items():
                result[key] = result.get(key, 0) + coefficient
        return clean(result)
    if kind == 'product':
        result = {(): 1}
        for factor in inner:
            next_result = {}
            for (left, a), (right, b) in itertools.product(result.items(), polynomial(factor, traits).items()):
                key = tuple(sorted(left + right))
                next_result[key] = next_result.get(key, 0) + a * b
            result = clean(next_result)
        return result
    raise ValueError(f'Unsupported expression operator: {kind}')


def response(baseline, boosted):
    """Return a ratio only when it holds for every assignment of symbolic facts."""
    if not baseline:
        return 1 if not boosted else None
    if set(baseline) != set(boosted):
        return None
    ratios = [boosted[key] / coefficient for key, coefficient in baseline.items()]
    return ratios[0] if all(math.isclose(ratios[0], r, rel_tol=1e-9, abs_tol=1e-9) for r in ratios) else None


def parameters(effects):
    result = []
    for path, node in nodes(effects, 'effects'):
        for key, value in node.get('values', {}).items():
            result.append((f'{path}/values/{key}', value, False))
        if node.get('type') == 'capture_value':
            result.append((f'{path}/value', node['value'], False))
        if 'selection' in node:
            if 'distance' in node:
                spatial = node['selection'] in ('near_target', 'nearby_entities', 'cone', 'melee')
                result.append((f'{path}/distance', node['distance'], spatial))
            for key, value in node.get('options', {}).items():
                result.append((f'{path}/options/{key}', value, False))
    return result


def analyze(name, spell, policy):
    rarity = spell['rarity']
    if rarity not in policy['rarities']:
        raise ValueError(f'Unknown rarity for {name}: {rarity}')
    traits = spell['traits']
    effects = spell['effects'] + [mode['effects'] for mode in spell.get('modes', [])]
    values = parameters(effects)
    baselines = {path: polynomial(value, traits) for path, value, _ in values}
    scenarios = {f'double:{key}': {key: 2} for key in sorted(set(traits) | set(SCALING) | {'vestige:evocation'})}
    scenarios['amplify_area_2x'] = {'vestige:amplify': 2, 'vestige:area': 2}
    scenarios['scaling_2x'] = dict.fromkeys(SCALING, 2)
    scenarios['amplify_stacked_2x'] = {'vestige:amplify': 4}
    results = {}
    for scenario, factors in scenarios.items():
        boosted_traits = {key: value * factors.get(key, 1) for key, value in traits.items()}
        changes = []
        for path, value, spatial in values:
            ratio = response(baselines[path], polynomial(value, boosted_traits))
            if ratio is None or not math.isclose(ratio, 1):
                changes.append({'path': path, 'expression': value, 'ratio': ratio,
                                'planar_coverage_proxy': ratio ** 2 if spatial and ratio is not None else None,
                                'volume_coverage_proxy': ratio ** 3 if spatial and ratio is not None else None})
        results[scenario] = {
            'max_exact_numeric_ratio': max([c['ratio'] for c in changes if c['ratio'] is not None], default=1),
            'context_dependent_parameters': sum(c['ratio'] is None for c in changes),
            'max_planar_coverage_proxy': max([c['planar_coverage_proxy'] for c in changes if c['planar_coverage_proxy'] is not None], default=1),
            'max_volume_coverage_proxy': max([c['volume_coverage_proxy'] for c in changes if c['volume_coverage_proxy'] is not None], default=1),
            'changes': changes,
        }
    flags = set()
    for _, node in nodes(effects):
        kind = node.get('type')
        if kind == 'repeat': flags.add('repeated plan needs cumulative outcome review')
        if kind == 'await_recast': flags.add('recast or staged execution')
        if kind == 'install_binding' or 'bindings' in node and node['bindings']: flags.add('event-driven value depends on event frequency')
        if node.get('on_tick'): flags.add('persistent pulses depend on lifetime and tick scheduling')
        if node.get('kind') == 'vestige:summon': flags.add('summon value depends on lifetime and AI')
        if kind in ('teleport', 'recall', 'pocket_dimension', 'ender_inventory', 'flight', 'break_blocks', 'break_block', 'unlock') or node.get('kind') in ('vestige:portal', 'vestige:block_lock'):
            flags.add('utility requires comparison beyond HP')
    if traits.get('vestige:volatile', 0) > 0:
        flags.add('volatile has inherent forfeit behavior outside these numerical effect parameters')
    baseline_parameters = [
        {'path': path, 'expression': value,
         'terms': [{'facts': list(facts), 'coefficient': coefficient} for facts, coefficient in sorted(baselines[path].items())]}
        for path, value, _ in values
    ]
    return {'id': f'vestige:{name}', 'rarity': rarity, 'authored_traits': traits,
            'baseline_parameters': baseline_parameters, 'scenarios': results, 'review_flags': sorted(flags)}


def generate():
    policy = json.loads(POLICY_PATH.read_text())
    assignments = {}
    for rarity, entry in policy['rarities'].items():
        for name in entry['spells']:
            if name in assignments: raise ValueError(f'Duplicate assignment: {name}')
            assignments[name] = rarity
    files = {path.stem: path for path in DEFINITIONS.glob('*.json')}
    if set(files) != set(assignments):
        raise ValueError(f'Rarity coverage mismatch: missing {set(files)-set(assignments)}, extra {set(assignments)-set(files)}')
    entries = []
    names = {}
    for name, path in sorted(files.items()):
        spell = json.loads(path.read_text())
        if spell.get('rarity') != assignments[name]: raise ValueError(f'Rarity policy disagrees with definition: {name}')
        names[name] = spell.get('source', {}).get('name', name.replace('_', ' ').title())
        entries.append(analyze(name, spell, policy))
    lines = ['# Spell balance audit', '',
             f'Current catalog: **{len(entries)} spells** with explicit rarity assignments. Gameplay balance is assessed through resolved outcomes, costs, timing, and constraints. Trait magnitudes and totals are authoring choices, not power scores or rarity limits.', '',
             'This report evaluates every numerical expression in effects, modes, targets, impact/tick/end callbacks, and bindings. World facts remain symbolic: a ratio is exact only when it holds for every assignment of those facts. Mixed expressions with a context-dependent response are marked separately. Guards, hit chance, target count, event frequency, and state changes can alter final outcomes and are not simulated.', '',
             'The detailed report includes baseline parameters after trait substitution, retaining world facts symbolically. Equivalent trait scales with compensated formulas preserve baseline outcomes and multiplicative boost responses. Fixed additions and thresholds require corresponding unit changes if ratings are rescaled; the runtime does not automatically normalize profiles.', '',
             'Area coverage is a geometric proxy: doubling a queried radius gives 4× planar footprint or 8× volume. Neither figure promises that many targets. Amplify and area boosts together can compound to 8× aggregate damage under uniform planar target density, or 16× under uniform volumetric density, before target limits and world constraints.', '',
             'Current attack formulas generally read amplify, while semantic school traits such as evocation classify the spell. Doubling an unused evocation rating leaves numerical parameters unchanged. Original Iron cooldowns remain provenance. Native cooldown costs are enforced by paid casting; the development `cast` command bypasses payment and recovery. See the complete balance review for role/outcome tuning.', '',
             'Policy and formulas: [Spell rarity and balance](design/spell-balance.md). Detailed expression responses: [JSON audit](spell-balance-audit.json).', '',
             '## Rarity groups', '', '| Rarity | Spells |', '|---|---|']
    for rarity, config in policy['rarities'].items():
        lines.append(f'| {rarity} | {sum(e["rarity"] == rarity for e in entries)} |')
    lines.extend(['', '## Rarity and evocation response', '',
                  'Rarity is independent of raw trait magnitudes. The response column records the largest exact change to a numerical parameter when only evocation doubles. A response of 1× means those parameters are unchanged; mixed responses depend on symbolic facts. This is not a whole-spell power score.', '',
                  '| Spell | Rarity | Double evocation parameter response | Mixed expressions |', '|---|---|---|---|'])
    link = lambda entry: f'[{names[entry["id"].split(":",1)[1]]}](spell-reference.md#{names[entry["id"].split(":",1)[1]].lower().replace(" ", "-")})'
    for entry in entries:
        evoc = entry['scenarios']['double:vestige:evocation']
        lines.append(f'| {link(entry)} | {entry["rarity"]} | {evoc["max_exact_numeric_ratio"]:g}× | {evoc["context_dependent_parameters"]} |')
    lines.extend(['', '## Numerical responses to boosts', '',
                  'These columns show the largest exact change to an individual numerical parameter, which can be damage, healing, reach, or another value. They are not whole-spell power multipliers. “Mixed” counts expressions whose response depends on world or stored facts. Coverage counts only queried radii; it does not infer hit count or combine unrelated outcomes.', '',
                  '| Spell | Double amplify | Double range | Double area | Area planar proxy | Area volume proxy | Mixed expressions under amplify |', '|---|---|---|---|---|---|---|'])
    for entry in entries:
        a, r, area = [entry['scenarios'][f'double:{trait}'] for trait in SCALING]
        lines.append(f'| {link(entry)} | {a["max_exact_numeric_ratio"]:g}× | {r["max_exact_numeric_ratio"]:g}× | {area["max_exact_numeric_ratio"]:g}× | {area["max_planar_coverage_proxy"]:g}× | {area["max_volume_coverage_proxy"]:g}× | {a["context_dependent_parameters"]} |')
    lines.extend(['', '## Gameplay review flags', '', '| Spell | Review required |', '|---|---|'])
    for entry in entries:
        if entry['review_flags']: lines.append(f'| {link(entry)} | {"; ".join(entry["review_flags"])} |')
    lines.extend(['', '## Refreshing the audit', '', '```bash', 'python3 tools/audit_spell_balance.py', 'python3 tools/audit_spell_balance.py --check', '```', '',
                  'Check mode verifies rarity coverage, assignment agreement, and report freshness. Passing means baseline parameters and boost responses match the definitions. Comparable gameplay value within a rarity still requires outcome and encounter testing.', ''])
    detail = {'policy': policy, 'spells': entries}
    return '\n'.join(lines), json.dumps(detail, indent=2, ensure_ascii=False) + '\n'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    markdown, details = generate()
    count = len(json.loads(details)['spells'])
    if args.check:
        if not REPORT.exists() or REPORT.read_text() != markdown or not DETAILS.exists() or DETAILS.read_text() != details:
            raise SystemExit('Balance audit is stale; run python3 tools/audit_spell_balance.py')
        print(f'Balance audit is current; resolved parameters and boost responses recorded for all {count} spells.')
    else:
        REPORT.write_text(markdown)
        DETAILS.write_text(details)
        print(f'Wrote resolved-parameter and boost-response reports for {count} spells.')


if __name__ == '__main__':
    main()
