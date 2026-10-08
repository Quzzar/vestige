#!/usr/bin/env python3
"""Inventory current names and author display aliases without changing magic rules."""
import argparse
import itertools
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / 'tools/magic-adjective-policy.json'
RESOURCE = ROOT / 'src/main/resources/data/vestige/magic_adjectives.json'
LEDGER = ROOT / 'docs/magic-adjectives.md'
WORD = re.compile(r'[A-Z][a-z]+\Z')
ID = re.compile(r'[a-z0-9_.-]+:[a-z0-9_./-]+\Z')


def key(requirements):
    return tuple(sorted(requirements.items()))


def compile_catalog():
    policy = json.loads(POLICY.read_text())
    if policy['version'] != 1:
        raise ValueError('Unsupported adjective policy')
    rules = json.loads((ROOT / 'src/main/resources/data/vestige/spellshaping_rules.json').read_text())['rules']
    terms = []
    for rule in rules:
        terms.append(dict(id='vestige:spellshaping/' + rule['id'], word=rule['name'],
                          meaning=rule['description'], kind='compound' if rule.get('compound') else 'individual',
                          status='Implemented spell adjustment', max_degree=rule.get('max_degree', 8)))
    tips = re.findall(r'^\s+([A-Z_]+)\(Items\.\w+,"([A-Za-z]+)",',
                      (ROOT / 'src/main/java/com/quzzar/vestige/apparatus/WandTips.java').read_text(), re.M)
    if set(policy['tip_meanings']) != {tip.lower() for tip, _ in tips}:
        raise ValueError('Tip meanings disagree with the executable palette')
    for tip, word in tips:
        terms.append(dict(id='vestige:wand_tip/' + tip.lower(), word=word,
                          meaning=policy['tip_meanings'][tip.lower()], kind='individual',
                          status='Implemented wand tip', max_degree=1))
    for term in policy['additional']:
        terms.append(dict(kind='individual', max_degree=1, **term))
    by_id = {term['id']: term for term in terms}
    if len(by_id) != len(terms):
        raise ValueError('Duplicate adjective identity')
    for term in terms:
        if not ID.fullmatch(term['id']) or not WORD.fullmatch(term['word']) or not 1 <= term['max_degree'] <= 8:
            raise ValueError('Invalid adjective: ' + term['id'])
    ordinary = {term['word'] for term in terms if term['kind'] == 'individual'}
    reserved = {term['word'] for term in terms if term['kind'] == 'compound'}
    components = policy['components']
    thread_source = (ROOT / 'src/main/java/com/quzzar/vestige/apparatus/MagicalThreadRecipe.java').read_text()
    thread_names = {name.title() for name in re.findall(r'^\s+([A-Z]+)\("[a-z_]+_thread",', thread_source, re.M)}
    if thread_names != {component['word'] for component in components}:
        raise ValueError('Thread names disagree with the executable palette')
    aliases = policy['combinations']
    mixed_word = policy['mixed_word']
    if not WORD.fullmatch(mixed_word) or mixed_word in ordinary | reserved | {c['word'] for c in components}:
        raise ValueError('Mixed adjective must be a reserved combination-only word')
    identities = set()
    alias_words = {}
    for alias in aliases:
        requirements = alias['requires']
        identity = (alias['family'], key(requirements))
        if not ID.fullmatch(alias['family']) or not WORD.fullmatch(alias['word']) or identity in identities:
            raise ValueError('Invalid or duplicate combined name: ' + alias['word'])
        if len(requirements) < 2 or sum(requirements.values()) > 16:
            raise ValueError('Combined adjectives require multiple contributions')
        for name, degree in requirements.items():
            if name not in by_id or type(degree) is not int or not 1 <= degree <= by_id[name]['max_degree']:
                raise ValueError('Invalid combined-name contribution: ' + name)
        meaning_key = tuple(sorted((by_id[name]['word'], degree) for name, degree in requirements.items()))
        if (alias['word'] in ordinary | reserved | {c['word'] for c in components} | {mixed_word}
                or alias['word'] in alias_words and alias_words[alias['word']] != meaning_key):
            raise ValueError('Combination-only adjective already has another meaning: ' + alias['word'])
        identities.add(identity)
        alias_words[alias['word']] = meaning_key
    for family in policy['complete_families']:
        # Each group supplies zero or one alternative; groups compose independently.
        choices = [[None, *group] for group in family['groups']]
        for selection in itertools.product(*choices):
            requirements = {name: 1 for name in selection if name is not None}
            if any(name not in by_id for name in requirements):
                raise ValueError('Unknown complete-family contribution')
            if len(requirements) > 1 and (family['id'], key(requirements)) not in identities:
                raise ValueError('Missing combined name: ' + str(requirements))
    return dict(version=1, mixed_word=mixed_word, terms=terms, combinations=aliases), components, rules, policy['complete_families']


def document(catalog, components, rules, complete_families):
    terms = catalog['terms']
    lines = ['# Magical adjective catalog', '',
             'Generated by `tools/author_magic_adjectives.py`; edit the executable source names or `tools/magic-adjective-policy.json`.', '',
             'The [naming convention](design/magic-adjectives.md) separates reusable individual words, combination-only names, and actual mechanical compounds. Names never grant effects, alter payments, or identify a spell. Planned entries below do not implement their items.', '',
             '## Reusable individual adjectives', '',
             '| Adjective | Used for | Current meaning |', '| --- | --- | --- |']
    individual = {}
    for term in terms:
        if term['kind'] == 'individual':
            individual.setdefault(term['word'], []).append(term)
    for word, uses in sorted(individual.items()):
        lines.append('| ' + word + ' | ' + '<br>'.join(f"`{use['id']}` — {use['status']}" for use in uses)
                     + ' | ' + '<br>'.join(dict.fromkeys(use['meaning'] for use in uses)) + ' |')
    lines += ['', '## Combination-only display adjectives', '',
              'These names describe the exact complete set below. The underlying adjustments still compose independently. Other degrees, additional adjustments and mixtures without a specific full-set match use *' + catalog['mixed_word'] + '*. This neutral combination-only word promises no additional mechanic. Distinct names are not required for distinct stored variants.', '',
              '| Combined adjective | Item | Required adjustments | Meaning / status |', '| --- | --- | --- | --- |']
    by_id = {term['id']: term for term in terms}
    for alias in catalog['combinations']:
        labels = [by_id[name]['word'] + (f' ×{degree}' if degree != 1 else '') for name, degree in alias['requires'].items()]
        lines.append(f"| {alias['word']} | `{alias['family']}` | {' + '.join(labels)} | {alias['meaning']} {alias['status']}. |")
    lines += ['', '## Implemented mechanical compounds', '',
              'These six names are also reserved for combinations, but select actual replacement mechanics. The raw ingredient/socket patterns are authoritative; several constituent pairs have no standalone adjective. They must not be reduced to invented adjective equations.', '',
              '| Compound adjective | Required offering / embedded material pairs | Authored outcome |', '| --- | --- | --- |']
    for rule in rules:
        if rule.get('compound'):
            pairs = '<br>'.join(pair['offering'].removeprefix('minecraft:').replace('_', ' ').title() + ' / '
                              + pair['material'].removeprefix('minecraft:').replace('_', ' ').title() for pair in rule['pairs'])
            lines.append(f"| {rule['name']} | {pairs} | {rule['description']} |")
    lines += ['', '## Thread component names', '',
              'These name five crafting components; they are not additional wand prefixes. Their authored behavior is preserved separately from the bound scroll and tip.', '',
              '| Component adjective | Current wand contribution |', '| --- | --- |']
    lines += [f"| {component['word']} | {component['meaning']} |" for component in components]
    lines += ['', '## Scope and coverage', '',
              f"Inventory: **{len(individual)} distinct individual words**, **{len(catalog['combinations'])} display combinations**, **six mechanical compounds**, **five thread component names**.", '',
              'Scrolls, bound wands and staff stored-scroll labels use one italic adjective for a mixture. Supported Homebound Eye payment routes use their individual adjective; its ordinary durability route keeps the plain title. Wayfarer uses its complete finite matrix. Hourglass names are reserved design entries, not implemented gameplay. Plain items, spell titles, rarity colors, affinity names, cosmetic finishes and attunement runes are not inferred into this catalog.', '',
              'Ordinary / Greater / Grand are existing degree labels, not three different adjustments. A degree-specific single-word alias must match that degree exactly. Existing name-only scroll tooltips remain name-only.', '']
    for family in complete_families:
        count = 1
        for group in family['groups']:
            count *= len(group) + 1
        lines += [f"`{family['id']}` has exhaustive naming coverage for its **{count}** declared combinations, including the unmodified item and individual adjustments. Gameplay reachability is checked by its own recipe tests.", '']
        aliases = {(alias['family'], key(alias['requires'])): alias['word'] for alias in catalog['combinations']}
        lines += ['| Full item name | Underlying adjustments |', '| --- | --- |']
        for selection in itertools.product(*[[None, *group] for group in family['groups']]):
            requirements = {name: 1 for name in selection if name is not None}
            labels = [by_id[name]['word'] for name in requirements]
            adjective = ('' if not labels else labels[0] if len(labels) == 1 else aliases[(family['id'], key(requirements))])
            item_name = family.get('label', family['id'])
            full_name = (f'*{adjective}* ' if adjective else '') + item_name
            lines.append(f"| {full_name} | {' + '.join(labels) or 'None'} |")
        lines.append('')
    return '\n'.join(lines)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    catalog, components, rules, families = compile_catalog()
    outputs = {RESOURCE: json.dumps(catalog, indent=2) + '\n', LEDGER: document(catalog, components, rules, families)}
    for path, content in outputs.items():
        if args.check:
            if not path.is_file() or path.read_text() != content:
                raise SystemExit('Adjective catalog drift: ' + str(path.relative_to(ROOT)))
        else:
            path.write_text(content)
    print(f"Adjective catalog: {len(catalog['terms'])} source entries, {len(catalog['combinations'])} combined names")


if __name__ == '__main__':
    main()
