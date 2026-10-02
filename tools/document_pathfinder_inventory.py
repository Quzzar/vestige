#!/usr/bin/env python3
"""Freeze PF2e candidate metadata and generate a browsable, offline inventory.

Import reads a local reference checkout. It never copies rules, mechanics, images,
or source code into the project, and does not author native spell definitions.
"""
import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import subprocess
from urllib.parse import quote

ROOT = Path(__file__).resolve().parents[1]
INDEX = ROOT / 'tools/pathfinder-spell-inventory.json'
REPORT = ROOT / 'docs/pathfinder-spell-inventory.md'
SOURCE_ROOT = 'packs/pf2e/spells'
REPOSITORY = 'https://github.com/foundryvtt/pf2e'
KINDS = ('cantrip', 'spell', 'focus', 'ritual', 'impossible')
LABELS = ('Ordinary cantrips', 'Other ordinary spells', 'Focus spells (including focus cantrips)', 'Rituals', 'Impossible spells (upstream category)')


def git(checkout, *args):
    return subprocess.check_output(['git', '-C', str(checkout), *args]).decode().strip()


def source_url(revision, path):
    return f'{REPOSITORY}/blob/{revision}/{quote(path, safe="/")}'


def import_metadata(checkout, retrieved):
    revision = git(checkout, 'rev-parse', 'HEAD')
    try:
        branch = git(checkout, 'symbolic-ref', '--quiet', '--short', 'HEAD')
    except subprocess.CalledProcessError:
        branch = None
    records = []
    tree = git(checkout, 'ls-tree', '-r', revision, '--', SOURCE_ROOT)
    for row in tree.splitlines():
        details, path = row.split('\t', 1)
        _, kind, blob = details.split()
        if kind != 'blob' or not path.endswith('.json') or path.endswith('/_folders.json'):
            continue
        raw = (checkout / path).read_bytes()
        digest = hashlib.sha1(b'blob ' + str(len(raw)).encode() + b'\0' + raw).hexdigest()
        if digest != blob:
            raise ValueError(f'Reference file differs from pinned Git blob: {path}')
        record = json.loads(raw)
        if record['type'] != 'spell':
            raise ValueError(f'Unexpected source record type: {path}')
        system = record['system']
        traits = system['traits']
        publication = system['publication']
        relative = Path(path).relative_to(SOURCE_ROOT)
        if relative.parts[0] == 'focus':
            category = 'focus'
        elif relative.parts[0] == 'rituals':
            category = 'ritual'
        elif relative.parts[0] == 'impossible-spells':
            category = 'impossible'
        elif 'cantrip' in traits['value']:
            category = 'cantrip'
        else:
            category = 'spell'
        records.append({
            'source_id': record['_id'],
            'name': record['name'],
            'kind': category,
            'rank': system['level']['value'],
            'cantrip': 'cantrip' in traits['value'],
            'traditions': sorted(traits['traditions']),
            'source_traits': sorted(traits['value']),
            'source_rarity': traits['rarity'],
            'publication': publication['title'],
            'publication_license': publication['license'],
            'publication_remaster': publication['remaster'],
            'path': path,
            'blob_sha': blob,
            'url': source_url(revision, path),
        })
    if not records:
        raise ValueError('No source spells found; check the upstream pack layout')
    return {
        'system': 'pathfinder_second_edition',
        'retrieved': retrieved,
        'source': {
            'repository': REPOSITORY,
            'commit': revision,
            'branch': branch,
            'committed_at': git(checkout, 'show', '-s', '--format=%cI', revision),
            'directory': SOURCE_ROOT,
        },
        'scope': 'PF2e spell records in this pinned Foundry development compendium, including focus spells, rituals, impossible spells, and retained legacy material; not a canonical deduplicated census or converted content.',
        'note': 'Metadata only. Source descriptions, numerical rules, implementation code, and artwork are omitted. Native rarity and balance remain independent. Verify canonical AoN rules before adapting a candidate.',
        'spells': sorted(records, key=lambda entry: (KINDS.index(entry['kind']), entry['rank'], entry['name'].casefold(), entry['path'])),
    }


def validate(index):
    spells = index['spells']
    if not spells or len({entry['source_id'] for entry in spells}) != len(spells):
        raise ValueError('Empty inventory or duplicate compendium identities')
    if len({entry['path'] for entry in spells}) != len(spells):
        raise ValueError('Duplicate source paths')
    revision = index['source']['commit']
    if not re.fullmatch('[a-f0-9]{40}', revision):
        raise ValueError('Source revision must be a pinned commit')
    for entry in spells:
        if entry['kind'] not in KINDS or type(entry['rank']) is not int or not 1 <= entry['rank'] <= 10:
            raise ValueError(f'Invalid category/rank: {entry["name"]}')
        if entry['source_rarity'] not in ('common', 'uncommon', 'rare', 'unique'):
            raise ValueError(f'Invalid source rarity: {entry["name"]}')
        if not entry['name'] or not entry['publication'] or not re.fullmatch('[a-f0-9]{40}', entry['blob_sha']):
            raise ValueError('Missing source identity or publication')
        if not entry['path'].startswith(SOURCE_ROOT + '/') or entry['url'] != source_url(revision, entry['path']):
            raise ValueError(f'Inconsistent source URL: {entry["name"]}')
        if entry['cantrip'] != ('cantrip' in entry['source_traits']):
            raise ValueError(f'Inconsistent cantrip metadata: {entry["name"]}')


def text(value):
    return str(value).replace('\\', '\\\\').replace('|', '\\|').replace('[', '\\[').replace(']', '\\]').replace('\n', ' ')


def generate(index):
    validate(index)
    spells = index['spells']
    source = index['source']
    publications = Counter(entry['publication'] for entry in spells)
    counts = Counter(entry['kind'] for entry in spells)
    native_sources = json.loads((ROOT / 'tools/pathfinder-spells.json').read_text())['spells']
    native_names = {entry['display_name'].casefold(): entry['id'] for entry in native_sources}
    reviewed_sources = json.loads((ROOT / 'tools/pathfinder-diverse-sources.json').read_text())['spells']
    reviewed_names = {entry['display_name'].casefold(): entry['id'] for entry in reviewed_sources}
    matched = sum(entry['name'].casefold() in native_names for entry in spells)
    reviewed_matches = sum(entry['name'].casefold() in reviewed_names for entry in spells)
    branch_note = f', collected from upstream branch `{source["branch"]}`' if source.get('branch') else ''
    lines = [
        '# Pathfinder Second Edition spell candidate inventory', '',
        f'Metadata collected **{index["retrieved"]}**: **{len(spells):,} compendium entries** across **{len(publications)} publication labels**. This is a broad review queue; the native Pathfinder catalog still has **{len(native_sources)} implemented adaptations**.', '',
        f'Source: [Foundry PF2e spell compendium]({REPOSITORY}/tree/{source["commit"]}/{SOURCE_ROOT}), pinned commit `{source["commit"]}` (committed {source["committed_at"]}){branch_note}. This community-maintained structured index includes retained legacy material, focus spells, rituals and its separate impossible-spell category. Development records may precede a stable compendium release. Counts describe records in this snapshot, not every distinct official spell or a guarantee of completeness.', '',
        'Names, source ranks, rarity, traditions, traits and publication metadata are frozen in [the JSON inventory](../tools/pathfinder-spell-inventory.json). Rules text, formulas, artwork and implementation code are omitted. Review the linked source metadata, verify the current canonical [Archives of Nethys rules](https://2e.aonprd.com/Spells.aspx), then author an explicit native recipe. Focus spells, rituals and impossible spells need deliberate native interaction rules before conversion. Publication labels are taken verbatim from the compendium; they can identify individual adventure issues or supplements rather than separate rulebooks.', '',
        f'**{matched} entries have an exact display-name match to a source in our existing batch.** The Match column is only a navigation aid; it does not reconcile source editions, errata or aliases. Existing verified provenance remains in [the frozen source ledger](../tools/pathfinder-spells.json) and [conversion ledger](design/pathfinder-spell-conversions.md). Other entries are candidates; their individual review/implementation status is tracked in the research and conversion ledgers. Source rarity is independent of Vestige common/uncommon/rare/mythic tuning.', '',
        f'The [100-source curated selection](pathfinder-spell-selection.md) combines {len(native_sources)} native sources with {len(reviewed_sources)} new rules-reviewed proposals. **{reviewed_matches} inventory records exactly match those reviewed names**; aliases and editions are not reconciled by this hint. Readiness is tracked by canonical AoN identity in the selection, rather than inferred from a compendium name.', '',
        'For the gameplay/presentation approach and recommended next implementation, see [spell presentation and catalog expansion](design/spell-presentation-and-expansion.md).', '',
        'Regenerate offline with `python3 tools/document_pathfinder_inventory.py`; validate with `--check`. To deliberately refresh metadata from a clean local upstream checkout, use `--source-checkout /absolute/path/to/pf2e --retrieved YYYY-MM-DD`. Import verifies each source file against its pinned Git blob and does not modify the native catalog.', '',
        '## Record counts', '',
        '| Category | Entries |', '|---|---:|',
    ]
    for kind, label in zip(KINDS, LABELS):
        lines.append(f'| {label} | {counts[kind]} |')
    lines.extend(['', '## Publication coverage', '', '| Source publication label | Entries |', '|---|---:|'])
    for publication, count in sorted(publications.items()):
        lines.append(f'| {text(publication)} | {count} |')
    for kind, label in zip(KINDS, LABELS):
        lines.extend(['', f'## {label}', '', '| Source entry | Rank | Source rarity | Traditions | Publication | Match |', '|---|---:|---|---|---|---|'])
        for entry in spells:
            if entry['kind'] != kind:
                continue
            match = native_names.get(entry['name'].casefold())
            reviewed = reviewed_names.get(entry['name'].casefold())
            hint = f'`pf2_{match}` (native name)' if match else f'Reviewed proposal `{reviewed}` (name)' if reviewed else 'Not matched to selection'
            rank = f'{entry["rank"]} (cantrip)' if entry['cantrip'] else str(entry['rank'])
            lines.append(f'| [{text(entry["name"])}]({entry["url"]}) | {rank} | {entry["source_rarity"]} | {", ".join(entry["traditions"]) or "—"} | {text(entry["publication"])} | {hint} |')
    return '\n'.join(lines) + '\n'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--source-checkout', type=Path)
    parser.add_argument('--retrieved')
    args = parser.parse_args()
    if args.source_checkout:
        if args.check or not args.retrieved or not re.fullmatch(r'\d{4}-\d{2}-\d{2}', args.retrieved):
            parser.error('Import requires --retrieved YYYY-MM-DD and cannot be combined with --check')
        index = import_metadata(args.source_checkout, args.retrieved)
        validate(index)
        INDEX.write_text(json.dumps(index, indent=2, ensure_ascii=False) + '\n')
    else:
        if args.retrieved:
            parser.error('--retrieved requires --source-checkout')
        index = json.loads(INDEX.read_text())
    report = generate(index)
    if args.check:
        if not REPORT.exists() or REPORT.read_text() != report:
            raise SystemExit('Pathfinder candidate inventory is stale; regenerate it')
        print(f'Pathfinder candidate inventory agrees with all {len(index["spells"]):,} frozen entries.')
    else:
        REPORT.write_text(report)
        print(f'Wrote {len(index["spells"]):,} Pathfinder candidate entries to {REPORT.relative_to(ROOT)}.')


if __name__ == '__main__':
    main()
