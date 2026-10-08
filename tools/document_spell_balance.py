#!/usr/bin/env python3
"""Review every native spell's costs and ideal per-creature direct outcome ceiling.

This is a bounded plan calculation, not a simulation of accuracy, AI or encounters.
Traits are resolved through their formulas, never treated as a power budget.
"""
import argparse
from collections import defaultdict
import json
from pathlib import Path
import sys
sys.dont_write_bytecode = True
from audit_spell_balance import nodes, polynomial

ROOT = Path(__file__).resolve().parents[1]
DEFINITIONS = ROOT / 'src/main/resources/data/vestige/runtime_spells'
REPORT = ROOT / 'docs/spell-balance-review.md'


def direct_ceiling(spell, amplification=1):
    # A cast chooses one mode. Keep its costs/traits but compare complete plans;
    # never add incompatible forms of the same spell together.
    if spell.get('modes'):
        outcomes = [direct_ceiling(dict(spell, effects=plan, modes=[]), amplification)
                    for plan in [spell['effects']] + [mode['effects'] for mode in spell['modes']]]
        return {kind: max(outcome[kind] for outcome in outcomes) for kind in ('damage', 'healing')}
    traits = dict(spell['traits'])
    traits['vestige:amplify'] = traits.get('vestige:amplify', 0) * amplification
    facts = {'vestige:actor/weapon_damage': 6, 'vestige:target/health': 16}
    def number(value):
        result = 0
        for variables, coefficient in polynomial(value, traits).items():
            for variable in variables:
                coefficient *= facts[variable]
            result += coefficient
        return result
    for _, node in nodes(spell['effects']):
        if node.get('type') == 'capture_value': facts[node['key']] = number(node['value'])
    alternatives = [[]]
    def emit(entry):
        for entries in alternatives: entries.append(entry)
    def visit(effects, multiplicity=1):
        nonlocal alternatives
        previous = (0, 0, 'damage')
        for effect in effects:
            kind = effect['type']; values = effect.get('values', {})
            val = lambda key, default=0: number(values.get(key, default))
            if kind in ('sequence', 'for_each'): visit(effect['effects'], multiplicity)
            elif kind == 'repeat': visit(effect['effects'], multiplicity * effect['count'])
            elif kind == 'branch':
                base = [entries.copy() for entries in alternatives]
                visit(effect['then'], multiplicity)
                when_true = alternatives
                alternatives = base
                visit(effect.get('else', []), multiplicity)
                alternatives = when_true + alternatives
            elif kind == 'create_manifestation':
                m = effect['manifestation']; mv = m.get('values', {}); mk = m['kind'].split(':')[-1]
                if mk == 'projectile':
                    deliveries = multiplicity * number(mv.get('count', 1))
                    if m.get('on_hit'): visit(m['on_hit'], deliveries)
                    else: emit(('damage', number(mv.get('damage', 0)), deliveries, 0, 'damage'))
                else:
                    if mk == 'construct':
                        behavior = m.get('identifiers', {}).get('behavior', '').split(':')[-1]
                        if behavior in ('heal', 'pressure'):
                            # This budget is shared across all recipients/pulses. Its
                            # maximum on one creature cannot exceed the whole pool.
                            pool = min(32, max(0, number(mv.get('budget', 0))))
                            amount = min(8 if behavior == 'heal' else 6,
                                         max(0, number(mv.get('amount', 1 if behavior == 'heal' else 2))))
                            pulses = m['duration'] // 20
                            emit(('healing' if behavior == 'heal' else 'damage',
                                  min(pool, amount * pulses), multiplicity, 0, 'construct'))
                        if m.get('on_hit'): visit(m['on_hit'], multiplicity)
                    if m.get('on_tick'):
                        if m['duration'] < 0: raise ValueError('Unbounded numerical callback')
                        visit(m['on_tick'], multiplicity * (m['duration'] // m.get('interval', 1)))
                    if mk == 'wall': emit(('damage',number(mv['damage']),multiplicity * (m['duration']//10),0,'wall'))
                # Event bindings and on-end repayment are conditional, reviewed separately.
            elif kind in ('damage','weapon_damage','explode','fangs','grip'):
                amount = val('impact_damage',4) if kind == 'grip' else val('amount')
                if kind == 'weapon_damage': amount += val('weapon_fraction',1) * facts['vestige:actor/weapon_damage']
                cap = val('max_hits_per_target',3 if kind=='grip' else 0)
                group = effect.get('identifiers',{}).get('hit_group','grip' if kind=='grip' else 'damage')
                emit(('damage',amount,multiplicity,cap,group)); previous=(amount,cap,group)
            elif kind == 'heal': emit(('healing',val('amount'),multiplicity,0,'heal'))
            elif kind == 'dwell_heal': emit(('healing',val('amount'),multiplicity,1,'dwell_heal'))
            elif kind == 'leech': emit(('healing',previous[0]*val('fraction',.25),multiplicity,previous[1],previous[2]))
    visit(spell['effects'])
    result = {'damage':0,'healing':0}
    for entries in alternatives:
        totals = {'damage':0,'healing':0}; capped = defaultdict(list)
        for kind, amount, count, cap, group in entries:
            if cap: capped[kind,group].append((amount,count,cap))
            else: totals[kind] += amount * count
        for (kind,group), values in capped.items():
            # All existing capped deliveries in a group share their damage and limit.
            if len({(amount,cap) for amount,_,cap in values}) != 1: raise ValueError('Mixed capped hit group needs ordering analysis')
            amount,_,cap=values[0]; totals[kind] += amount * min(cap,sum(count for _,count,_ in values))
        for kind in result: result[kind] = max(result[kind], totals[kind])
    return result


def generate():
    policy=json.loads((ROOT/'tools/spell-balance-policy.json').read_text())
    rarities={name:rarity for rarity,data in policy['rarities'].items() for name in data['spells']}
    spells={p.stem:json.loads(p.read_text()) for p in DEFINITIONS.glob('*.json')}
    assert set(spells)==set(policy['spells'])==set(rarities)
    lines=['# Complete native spell balance review','',
           f'October 7, 2026: **all {len(spells)} spells have no ordinary cooldown**. Native outcomes, mana, charge times, control, protection, summons and utility retain their authored baselines. Trait profiles retain their relative units; no trait-point budget or rarity multiplier is applied. The [spell reference](spell-reference.md) documents every executable plan; the [boost audit](spell-balance-audit.md) resolves every numeric formula.', '',
           'The direct columns are ideal per-creature ceilings from the current effect graphs, including repeats, projectile counts, field pulses, shared hit caps, destructible-construct fracture callbacks and finite construct damage/healing budgets. Mutually exclusive modes and branches are compared rather than summed; damage and healing ceilings can belong to different eligible creatures or modes. A construct budget is shared across recipients, so its listed ceiling cannot be granted to each of them independently. These ceilings assume every relevant delivery connects, the victim remains eligible/in range, required destruction occurs, no mitigation, no overheal, and uninterrupted execution. Fields pulse every authored interval from creation and include a pulse at expiry. External example inputs are a six-HP weapon and a sixteen-HP sacrificed creature. Burning, Poison, Wither, vanilla freezing, death spread, incoming-damage reactions, enemy healing, vanilla mob attacks and utility are assessed in the role review instead of added to an artificial power score. A dash cannot multiply its capped hit by standing against one creature; meteor and volley caps apply across all deliveries in the same cast.', '',
           'Paid casting commits resources after initial preparation and permits recasts without paying again. Cancelled charges and failed payment spend nothing; ordinary spell cooldowns have been removed. Wands have no additional equipment recovery; composed resources, preparation/channel time and deterministic wear govern repeated use. One charge/channel per actor prevents stacking active channels. Same-spell summon replacement removes the previous cohort, including backing bodies created on a different subject. Reload/restart resets active sessions and any explicit data-pack spell cooldowns; persistent progression remains deferred.', '',
           'To test the costs, use `/vestige_magic mana 100` and `/vestige_magic cast_balanced vestige:<id>` in Survival. Players have 100 mana, full on first spawn and death/respawn, with recovery of 2 mana per second after a five-second expenditure delay; Gluttony can convert food. The ordinary `cast` development command bypasses resource costs. These are initial tested gameplay baselines, not a claim that AI, PvP, terrain, optional mods, or every multiplayer encounter have been exhaustively playtested.', '',
           '| Rarity | Spells |','|---|---:|']
    for rarity,data in policy['rarities'].items(): lines.append(f'| {rarity} | {len(data["spells"])} |')
    for rarity in policy['rarities']:
        lines += ['',f'## {rarity.title()}','', '| Spell | Role | Mana | Charge (s) | Direct HP / healing ceiling per creature | Outcome and tradeoff review |', '|---|---|---:|---|---|---|']
        for name in sorted(policy['rarities'][rarity]['spells']):
            spell=spells[name]; tuning=policy['spells'][name]
            assert spell['rarity']==rarity,name
            actual={c['type']:c.get('amount',c.get('ticks')) for c in spell['costs']}
            expected={'mana':tuning['mana']}
            if tuning['cooldown_ticks']:expected['cooldown']=tuning['cooldown_ticks']
            if tuning['charge_ticks']:expected['time']=tuning['charge_ticks']
            assert len(actual)==len(spell['costs']) and actual==expected,name
            outcome=direct_ceiling(spell)
            ceiling=' / '.join(f'{outcome[k]:g}' if outcome[k] else '—' for k in ('damage','healing'))
            path=f'../src/main/resources/data/vestige/runtime_spells/{name}.json'
            lines.append(f'| [{name}]({path}) | {tuning["role"]} | {tuning["mana"]} | {tuning["charge_ticks"]/20:g} | {ceiling} | {tuning["review"]} |')
    lines += ['', '## Verification', '',
              'The Minecraft encounter suite exercises actual health changes, amplification, crowd caps, paid mana and repeat casting, dash contact, summon health/replacement, wards, reactive damage/healing, destructible control, and utility lifecycles. Unit tests cover preparation snapshots, optional data-pack cooldown boundaries, cancellations, failed payment, paid failures, recasts, channel exclusivity, pulse phase and backing replacement. The current counts and executed commands are recorded in [development status](development-status.md).', '',
              'Regenerate with `python3 tools/document_spell_balance.py`; `--check` detects drift in every role/cost entry and the calculated ceilings. The formula audit remains separate from gameplay outcome review. Future playtesting may refine this baseline without reopening the deferred wand/discovery/progression work.']
    return '\n'.join(lines)+'\n'

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    content=generate()
    if args.check:
        if not REPORT.exists() or REPORT.read_text()!=content: raise SystemExit('Balance review is stale; regenerate it.')
        print('All spell balance reviews and native costs are current.')
    else: REPORT.write_text(content);print('Wrote complete native balance review for the full catalog.')
