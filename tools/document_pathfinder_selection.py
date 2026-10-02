#!/usr/bin/env python3
"""Build the 100-source selection and honest conversion/readiness breakdown.

Reviewed sources and native proposals are separate inputs. Runtime readiness is
inferred from exact source identities in real definitions, never from a proposal.
"""
import argparse
from collections import Counter
from copy import deepcopy
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
TOOLS = ROOT / 'tools'
sys.path.insert(0, str(TOOLS))
from convert_pathfinder_spells import NOTES, definitions

REPORT = ROOT / 'docs/pathfinder-spell-selection.md'
COLLECTION = TOOLS / 'pathfinder-spell-selection.json'
FAMILIES = ('Construction', 'Protection and reactions', 'Movement and positioning',
            'Illusion', 'Body and environment', 'Sensing and information',
            'Control and boundaries', 'Objects and companions')


def read(path):
    return json.loads(path.read_text())


def require(condition, message):
    if not condition:
        raise ValueError(message)


def collection():
    implemented = read(TOOLS / 'pathfinder-spells.json')['spells']
    additions = read(TOOLS / 'pathfinder-diverse-sources.json')['spells']
    plan_input = read(TOOLS / 'pathfinder-selection-plans.json')
    plans = plan_input['plans']
    all_sources = list({row["aon_id"]:row for row in implemented + additions}.values())
    require(len(all_sources) == 100, 'The curated selection must contain exactly 100 sources')
    require(len({row['id'] for row in all_sources}) == 100, 'Duplicate source slug')
    require(len({row['aon_id'] for row in all_sources}) == 100, 'Duplicate canonical AoN identity')
    require(len({row['display_name'].casefold() for row in all_sources}) == 100, 'Duplicate canonical name')
    require(set(plans) == {row['id'] for row in additions}, 'Every additional source needs one complete proposal')
    allowed_traits = set(re.findall(r'^- `([a-z_]+)`', (ROOT / 'docs/design/trait-catalog.md').read_text(), re.M))
    require(len(allowed_traits) == 50, 'Trait-catalog grammar or accepted repertoire changed; review plans')
    actual = definitions()
    runtime_dir = ROOT / 'src/main/resources/data/vestige/runtime_spells'
    all_native = {path.stem: read(path) for path in runtime_dir.glob('*.json')}
    policy = read(TOOLS / 'spell-balance-policy.json')['spells']
    output = []
    for source in all_sources:
        name = source['id']
        require(re.fullmatch('[a-z][a-z0-9_]*', name), f'Invalid slug: {name}')
        require(type(source['aon_id']) is int and source['aon_id'] > 0, f'Invalid AoN ID: {name}')
        require(source['url'] == f'https://2e.aonprd.com/Spells.aspx?ID={source["aon_id"]}', f'Wrong canonical URL: {name}')
        require(type(source['rank']) is int and 1 <= source['rank'] <= 10, f'Invalid source rank: {name}')
        require(type(source['cantrip']) is bool and source['traditions'] and set(source['traditions']) <= {'arcane','divine','occult','primal'}, f'Invalid source taxonomy: {name}')
        require(source['edition'] in ('remaster','legacy'), f'Invalid source edition: {name}')
        require(source['rarity'] in ('common','uncommon','rare','unique'), f'Invalid source rarity: {name}')
        errata = source['errata']
        require(set(errata) == {'version','date','source_url'} and all(errata.values()) and re.fullmatch(r'\d{4}-\d{2}-\d{2}', errata['date']), f'Missing errata snapshot: {name}')
        require(source['publication'] and type(source['page']) is int and source['page'] > 0 and source['revision'] and source['summary'], f'Missing provenance: {name}')
        native = 'pf2_' + name
        row = {'source': deepcopy(source)}
        if native in actual:
            spell = actual[native]
            require(all_native.get(native) == spell, f'Regenerate or repair native recipe: {native}')
            require(spell['source']['spell'] == f'pathfinder2e:aon/{source["aon_id"]}', f'Native provenance mismatch: {native}')
            require(native in policy, f'Inconsistent native readiness: {name}')
            row.update(status='native_recipe', native_id='vestige:' + native,
                       native={'behavior': NOTES[native][0], 'adaptation': NOTES[native][1],
                               'rarity': spell['rarity'], 'traits': spell['traits'], 'costs': spell['costs']})
        else:
            require(native not in all_native and native not in policy, f'Review conversion/readiness when promoting {native}')
            plan = plans[name]
            required = {'family','traits','plan','scaling_reads','closest_native','difference','visuals','needs','balance'}
            require(set(plan) == required and all(plan.values()), f'Incomplete proposal: {name}')
            require(plan['family'] in FAMILIES, f'Unknown family: {name}')
            require(len(set(plan['traits'])) == len(plan['traits']) and set(plan['traits']) <= allowed_traits, f'Unknown/duplicate planned trait: {name}')
            require(set(plan['closest_native']) <= set(all_native), f'Comparison names absent from catalog: {name}')
            row.update(status='rules_reviewed', proposal=deepcopy(plan))
        output.append(row)
    return {'system':'pathfinder_second_edition', 'updated':'2026-10-02',
            'scope':'100 curated canonical sources, all with explicit native recipes and balance entries. The 36 selection proposals are retained separately as historical design evidence. This is a total selection, not 100 additional spells.',
            'native_catalog_count':len(all_native),
            'counts':dict(Counter(row['status'] for row in output)),
            'historical_proposal_scaling':plan_input['scaling'], 'spells':output,
            'excluded_overlap':plan_input['excluded_overlap']}


def native_link(name):
    return f'[{name}](../src/main/resources/data/vestige/runtime_spells/{name}.json)'


def report(data):
    rows = data['spells']
    require(all(row['status'] == 'native_recipe' for row in rows), 'Every selected source needs its native recipe')
    history = read(TOOLS / 'pathfinder-selection-plans.json')['plans']
    lines = ['# Pathfinder: 100 native adaptations', '', 'Updated October 2, 2026.', '',
             f'October 1, 2026: **all 100 selected canonical Pathfinder sources have explicit native definitions and matching balance entries**. The complete Vestige catalog contains {data["native_catalog_count"]} spells. This is 100 Pathfinder adaptations in total, including the original 64 and the latest 36.', '',
             'The latest batch adds construction, ally formations, shared protective budgets, physical size, private sensing, acoustic/perception boundaries and object/companion utility. Shared mechanics interpret explicit plans; there is no spell-name fallback attack. Native forms are bounded Minecraft adaptations, with deliberate source differences in the [conversion ledger](design/pathfinder-spell-conversions.md). The earlier [36 design proposals](../tools/pathfinder-selection-plans.json) remain historical evidence; the executable recipes supersede their proposed mechanics and limits.', '',
             'The [combined collection](../tools/pathfinder-spell-selection.json) retains exact canonical IDs, editions, traditions, book/pages and errata. Primary-source research covers the [first 48](research/pathfinder-spell-batch.md), [next sixteen](research/pathfinder-expansion-batch.md) and [latest 36](research/pathfinder-diverse-batch.md). Source rank and rarity do not set native power; descriptive trait totals do not measure balance. Wands, discovery and progression remain deferred.', '',
             'Every spell has layered presentation and an actual primary-cast video in the [effects workshop](effects-workshop.md). The [art ledger](spell-art-direction.md) records authored silhouettes and movement; the [spell reference](spell-reference.md) lists traits, targeting, formulas, modes, callbacks and presentation. Server behavior has targeted suite coverage; representative actual appearance, including Wall of Ice\'s real block formation and collapse, is inspected. Exhaustive encounters and multiplayer observer transitions remain further review, as recorded in [development status](development-status.md).', '',
             '## Latest batch by gameplay family', '',
             '| Family | Implemented sources |', '|---|---|']
    for family in FAMILIES:
        chosen=[row['source']['display_name'] for row in rows if row['source']['id'] in history and history[row['source']['id']]['family']==family]
        lines.append(f'| {family} | {", ".join(chosen)} |')
    lines += ['', '## All 100 current recipes', '',
              'These rows describe generated definitions and current native costs. The linked reference and ledger provide complete traits, scaling, presentation and source differences.', '',
              '| Spell / canonical source | Source rank | Native rarity | Mana; charge/recovery (s) | Current native behavior |', '|---|---|---|---|---|']
    for row in rows:
        source,native=row['source'],row['native']
        costs={c['type']:c.get('amount',c.get('ticks')) for c in native['costs']}
        rank=('cantrip ' if source['cantrip'] else '')+str(source['rank'])
        slug=row['native_id'].removeprefix('vestige:')
        lines.append(f'| [{source["display_name"]}]({source["url"]}) / {native_link(slug)} | {rank} | {native["rarity"]} | {costs["mana"]}; {costs.get("time",0)/20:g}/{costs["cooldown"]/20:g} | {native["behavior"]} |')
    lines += ['', '## Deliberately excluded overlap', '']
    for excluded in data['excluded_overlap']:
        lines.append(f'- **{excluded["candidate"]}:** {excluded["reason"]}')
    lines += ['', 'The broader [1,992-record inventory](pathfinder-spell-inventory.md) remains available for later selections. Legacy aliases and reprints are provenance rather than automatically separate native identities.', '',
              '## Keeping the collection current', '',
              'Author source metadata in `tools/pathfinder-spells.json`, explicit recipes in `tools/convert_pathfinder_spells.py` / `tools/pathfinder_diverse_recipes.py`, and costs/rarities in `tools/spell-balance-policy.json`. Regenerate the converter, then run `python3 tools/document_pathfinder_selection.py` and its `--check`. It validates 100 unique canonical identities, provenance and exact agreement between current definitions and policy before checking both collection outputs. This generator reads native definitions; it does not create gameplay.']
    return '\n'.join(lines)+'\n'


def write(check=False):
    data = collection()
    outputs = {COLLECTION:json.dumps(data,indent=2,ensure_ascii=False)+'\n', REPORT:report(data)}
    for path, content in outputs.items():
        if check:
            require(path.exists() and path.read_text() == content, f'Stale selection output: {path}')
        else:
            path.write_text(content)
    counts = data['counts']
    print(('Verified' if check else 'Wrote') + f" 100-source selection: {counts.get('native_recipe',0)} native recipes, {counts.get('rules_reviewed',0)} reviewed proposals.")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    write(parser.parse_args().check)
