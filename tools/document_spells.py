#!/usr/bin/env python3
"""Build the spell reference from checked-in definitions and authored recipe notes.

This does not regenerate or change spell content. Human guide text lives beside this
script in spell-reference-intro.md. Run with --check to detect reference drift.
"""
from spell_title import spell_title
import argparse
import importlib.util
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEFINITIONS = ROOT / 'src/main/resources/data/vestige/runtime_spells'
OUTPUT = ROOT / 'docs/spell-reference.md'
GROUPS = ['native', 'pathfinder', 'blood', 'ender', 'evocation', 'fire', 'holy', 'ice', 'lightning', 'nature', 'eldritch']
SCALING = {'vestige:amplify', 'vestige:range', 'vestige:area'}
NATIVE_NOTES = {
    'force_arrow': ('Launch a force projectile that damages its struck creature.',
                    'Native example of projectile values and explicit scaling; uses the default impact damage path.'),
    'summon_zombie': ('Summon one owned zombie for thirty seconds.',
                      'Vanilla zombie with a leather helmet and native ownership/follow/combat handling. This example has no recast dismissal continuation.'),
    'arcane_lock': ('Lock an aimed container for its owner; casting on an owned lock unlocks it.',
                    'Persistent container ownership blocks another player’s ordinary right-click access. It does not implement universal protection from breaking, automation, or other inventory access.'),
    'interposing_earth': ('A brief one-hit ward reduces the next incoming damage amount.',
                         'Native ward example: three-second lifetime, one reaction, reduction of 2 × amplify. It does not raise a terrain wall or implement PF2e saves.'),
}


def local(value):
    return value.removeprefix('vestige:')


def number(value):
    return f'{value:g}'


def expression(value):
    if isinstance(value, bool):
        return str(value).lower()
    if isinstance(value, (int, float)):
        return number(value)
    if isinstance(value, str):
        return value
    if not isinstance(value, dict) or len(value) != 1:
        raise ValueError(f'Unsupported expression: {value}')
    key, inner = next(iter(value.items()))
    if key == 'trait':
        return local(inner)
    if key == 'fact':
        return f'fact({local(inner)})'
    if key == 'id':
        return inner
    if key in ('sum', 'product'):
        operator = ' + ' if key == 'sum' else ' × '
        terms = []
        for term in inner:
            rendered = expression(term)
            if key == 'product' and isinstance(term, dict) and 'sum' in term:
                rendered = f'({rendered})'
            terms.append(rendered)
        return operator.join(terms)
    raise ValueError(f'Unsupported expression operator: {key}')


def duration(value):
    return 'persistent' if value == -1 else f'{value} ticks ({number(value / 20)} s)'


def walk(value):
    if isinstance(value, dict):
        yield value
        for inner in value.values():
            yield from walk(inner)
    elif isinstance(value, list):
        for inner in value:
            yield from walk(inner)


def condition(data):
    kind = local(data['type'])
    if kind == 'exists':
        return f'{local(data["path"])} exists'
    if kind == 'tagged':
        return f'{local(data["path"])} belongs to {data["tag"]}'
    if kind == 'compare':
        return f'{local(data["path"])} {data["operation"]} {expression(data["expected"])}'
    raise ValueError(f'Unsupported condition: {kind}')


def target(data):
    selections = {
        'self': 'caster', 'current': 'current subject', 'event_attacker': 'attacking creature from current damage event',
        'event_target': 'event victim or subject',
        'stored_target': 'captured target', 'entity_ray': 'aimed living creature',
        'any_entity_ray': 'aimed entity including projectiles', 'block_ray': 'aimed block',
        'aimed_position': 'aimed position', 'nearby_entities': 'creatures around caster',
        'near_target': 'creatures around current subject or impact', 'beam': 'creatures along aimed beam',
        'cone': 'creatures in aimed cone', 'chain': 'aimed creature and chained nearby creatures',
        'melee': 'creatures in aimed melee sweep',
    }
    selection = data['selection']
    text = selections[selection]
    if data.get('relationship', 'any') != 'any':
        text += f'; {data["relationship"]} only'
    if 'distance' in data:
        text += f'; reach/radius {expression(data["distance"])} blocks'
    options = dict(data.get('options', {}))
    if selection in ('cone', 'melee') and 'angle' not in options:
        options['angle'] = 45
    if selection == 'beam' and 'radius' not in options:
        options['radius'] = 0.75
    if options:
        text += '; ' + ', '.join(f'{key}={expression(value)}' for key, value in options.items())
    return text + ('; optional when empty' if not data.get('required', True) else '; required')


def action(data):
    kind = local(data['type'])
    values = data.get('values', {})
    ids = data.get('identifiers', {})
    val = lambda key, default=0: expression(values.get(key, default))
    if kind in ('damage', 'weapon_damage'):
        text = f'Deal {val("amount")} HP {ids.get("damage_type", "magic")} damage'
        if kind == 'weapon_damage':
            text += f' + {val("weapon_fraction", 1)} × caster attack damage'
        if values.get('ignore_invulnerability', 0):
            text += '; reset ordinary hit invulnerability frames'
        if values.get('max_hits_per_target', 0):
            text += f'; at most {val("max_hits_per_target")} hits per creature across this cast'
        return text
    if kind in ('transpose','create_water','shape_stone','gather_items','utterance'):
        return {'transpose':'Move up to three consenting allies after validating the complete formation', 'create_water':'Add one water layer to a permitted aimed cauldron', 'shape_stone':'Move one captured ordinary stone cell to a permitted empty destination, conserving volume', 'gather_items':'Pull up to eight loose eligible metal stacks without copying them', 'utterance':'Require an audible vocal delivery outside native silence'}[kind]
    if kind == 'heal':
        return f'Heal {val("amount")} HP'
    if kind == 'dwell_heal':
        return f'Heal {val("amount")} HP once per creature per cast after {duration(values.get("required_ticks", 60))} continuously sampled occupancy; checks every {values.get("interval", 5)} ticks, leaving resets progress'
    if kind == 'random_teleport':
        return f'Attempt safe random self-teleport within {val("radius", 4)} blocks; loaded, dry supported landing; an obstructed pulse is skipped'
    if kind == 'detect_magic':
        return f'Privately report native manifestations or equipped enchanted items within {val("radius", 16)} blocks; no discovery/progression'
    if kind == 'inspect_item':
        return 'Privately report whether the caster’s current held item has a vanilla enchantment; no item identification or discovery unlock'
    if kind == 'leech':
        return f'Heal caster by {val("fraction", 0.25)} × actual HP lost to preceding damage'
    if kind == 'status':
        return f'Apply {ids["effect"]} level {number(values.get("amplifier", 0) + 1)} for {duration(values.get("duration", 100))}'
    if kind == 'remove_status':
        return f'Remove {ids["effect"]}'
    if kind == 'ignite':
        return f'Ignite for {val("seconds", 3)} s'
    if kind == 'freeze':
        return f'Add {val("ticks", 140)} frozen ticks to vanilla freezing state, capped at 400'
    if kind == 'explode':
        return f'Area blast: {val("amount", 8)} HP magic damage; radius {val("radius", 3)} blocks; explosion particles'
    if kind == 'fangs':
        shape = 'forward line' if values.get('line', 0) else f'ring of radius {val("radius")} blocks'
        return f'Create {val("count", 1)} particle eruptions in a {shape}; {val("amount", 6)} HP once per creature, at most {val("max_targets", 128)} creatures'
    if kind in ('knockback', 'launch', 'pull', 'dash'):
        labels = {'knockback': 'Push target away', 'launch': 'Launch target', 'pull': 'Pull target toward effect origin', 'dash': 'Dash caster forward'}
        text = f'{labels[kind]}; strength {val("strength", 0.5)}'
        if kind != 'dash' or 'up' in values:
            text += f'; vertical impulse {val("up", 1 if kind == "launch" else 0.15)}'
        return text
    if kind in ('reduce_pending_damage', 'reduce_pending_heal', 'defer_pending_damage'):
        labels = {'reduce_pending_damage': 'Reduce pending damage by', 'reduce_pending_heal': 'Reduce pending healing by', 'defer_pending_damage': 'Accumulate deferred damage and reduce pending damage by'}
        return f'{labels[kind]} {val("amount")} HP before commit'
    if kind in ('break_block', 'break_blocks'):
        shape = 'aimed block' if kind == 'break_block' else f'aimed face; radius {val("radius", 1)}; depth {val("depth", 1)}'
        return f'Mine {shape}; maximum hardness {val("hardness", 5)}; held-tool drops and block-break cancellation'
    if kind == 'grip':
        return f'Pull captured target toward {val("distance", 6)} blocks in front of caster; horizontal collision damage {val("impact_damage", 4)} HP, at most {val("max_hits_per_target", 3)} collisions per cast'
    if kind == 'grant_max_health':
        return f'Grant temporary {val("amount", 2)} maximum HP, owned by this cast'
    if kind == 'food_mana':
        return f'Add {val("amount", 20)} native energy; restoration capped at 100 mana'
    if kind == 'redirect_projectiles':
        return f'Steer nearby projectiles within {val("radius", 6)} blocks toward current creature'
    labels = {
        'control': 'Temporarily control current mob; cast cleanup restores prior target and releases ownership',
        'teleport': 'Teleport caster to current position, or behind current creature; collision-checked',
        'recall': 'Return caster to valid respawn position or overworld spawn',
        'unlock': 'Remove this caster’s persistent container lock',
        'dispel': 'Interrupt target; dispel attached native effects; remove vanilla statuses; discard native caused entities or projectiles',
        'cleanse': 'Remove harmful vanilla status effects',
        'aggro_clear': 'Clear nearby mobs currently pursuing caster',
        'aggro_decoy': f'Draw hostile mobs within {val("radius", 12)} blocks toward the current living lure',
        'aggro_convert': 'Choose a random wool color for current sheep',
        'despawn': 'Remove current summon if owned by caster',
        'dismiss_manifestations': 'Dismiss manifestations owned by this cast',
        'ender_inventory': 'Open caster’s vanilla ender chest inventory',
        'pocket_dimension': 'Visit caster’s private room, or return to saved entry point when inside',
        'remove_attribute': 'Clean up temporary changes owned by this cast',
        'flight': 'Grant caster flight permission; owned cleanup restores prior permission',
    }
    if kind in labels:
        return labels[kind]
    raise ValueError(f'Unsupported action: {kind}')


def binding(data, depth):
    prefix = '  ' * depth
    charge_word = 'charge' if data['charges'] == 1 else 'charges'
    lines = [f'{prefix}Binding {local(data["id"])}: {data["charges"]} {charge_word}, {duration(data["duration"])}']
    for trigger in data['triggers']:
        text = f'{prefix}  React on {local(trigger["event"])}'
        if trigger.get('conditions'):
            text += ' when ' + ' and '.join(condition(c) for c in trigger['conditions'])
        lines.append(text)
    lines.extend(plan(data['effects'], depth + 1))
    return lines


def plan(effects, depth=0):
    lines = []
    prefix = '  ' * depth
    for data in effects:
        kind = local(data['type'])
        if data.get('conditions'):
            lines.append(prefix + 'Only when ' + ' and '.join(condition(c) for c in data['conditions']))
        if kind == 'for_each':
            lines.append(prefix + 'Select ' + target(data['target']))
            if data.get('visual'): lines.append(prefix + '  Cosmetic selection cue: ' + visual_description(data['visual']))
            lines.extend(plan(data['effects'], depth + 1))
        elif kind == 'sequence':
            lines.append(prefix + 'In order')
            lines.extend(plan(data['effects'], depth + 1))
        elif kind == 'branch':
            lines.append(prefix + 'If ' + condition(data['condition']))
            lines.extend(plan(data['then'], depth + 1))
            if data.get('else'):
                lines.append(prefix + 'Otherwise')
                lines.extend(plan(data['else'], depth + 1))
        elif kind == 'repeat':
            lines.append(prefix + f'Repeat {data["count"]} times, {data["interval"]} ticks between iterations; first immediately')
            lines.extend(plan(data['effects'], depth + 1))
        elif kind == 'delay':
            lines.append(prefix + 'Wait ' + duration(data['ticks']) + ' before remaining steps')
        elif kind == 'await_recast':
            lines.append(prefix + 'Await another input in this session, timeout ' + duration(data['timeout']))
        elif kind in ('set_value', 'capture_value'):
            verb = 'Set' if kind == 'set_value' else 'Capture numerical snapshot'
            lines.append(prefix + f'{verb} {local(data["key"])} = {expression(data["value"])}')
        elif kind == 'store_target':
            lines.append(prefix + f'Store current target as {local(data["key"])}')
        elif kind == 'end_manifestation':
            lines.append(prefix + 'End this manifestation')
        elif kind == 'install_binding':
            lines.append(prefix + 'Attach reaction to ' + target(data['target']))
            lines.extend(binding(data['binding'], depth + 1))
        elif kind == 'create_manifestation':
            manifestation = data['manifestation']
            mkind = local(manifestation['kind'])
            lines.append(prefix + f'Create {mkind}, {duration(manifestation["duration"])}; target: {target(data["target"])}')
            if manifestation.get('visual'): lines.append(prefix + '  Attached presentation: ' + visual_description(manifestation['visual']))
            for key, value in manifestation.get('values', {}).items():
                lines.append(prefix + f'  {key} = {expression(value)}')
            for key, value in manifestation.get('identifiers', {}).items():
                lines.append(prefix + f'  {key} = {value}')
            if mkind == 'projectile' and not manifestation.get('on_hit'):
                lines.append(prefix + '  On creature impact: default damage = ' + expression(manifestation.get('values', {}).get('damage', 0)) + ' HP')
            for reaction in manifestation.get('bindings', []):
                lines.extend(binding(reaction, depth + 1))
            for callback, label in [('on_hit', 'On impact'), ('on_tick', f'On tick callback every {manifestation.get("interval", 1)} ticks'), ('on_end', 'On normal end')]:
                if manifestation.get(callback):
                    lines.append(prefix + '  ' + label)
                    lines.extend(plan(manifestation[callback], depth + 2))
        elif kind == 'visual':
            lines.append(prefix + 'Cosmetic cue at current subject: ' + visual_description(data['visual']))
        else:
            lines.append(prefix + action(data))
    return lines


def visual_description(visual):
    layers = ' + '.join(f'{layer["shape"]} (#{layer["color"]})' for layer in visual['layers'])
    result = f'{layers}; radius {expression(visual["radius"])} blocks; {duration(visual["duration"])}'
    if visual.get('ends_with_bindings'): result += '; ends when all owned reactions are spent/expired'
    if visual.get('sound'): result += '; sound ' + visual['sound']['id']
    return result


def presentation(effects):
    descriptions = []
    for data in walk(effects):
        if data.get('visual'): descriptions.append('Composed native cue: ' + visual_description(data['visual']) + '.')
        if data.get('type') == 'create_manifestation':
            m = data['manifestation']
            kind = local(m['kind'])
            ids, values = m.get('identifiers', {}), m.get('values', {})
            if kind == 'projectile':
                item = 'the held item' if values.get('held_item') else ids.get('item', 'minecraft:amethyst_shard')
                if m.get('visual'):
                    descriptions.append('Attached native projectile body/trail: ' + visual_description(m['visual']) + f'; {item} remains the visible fallback.')
                else: descriptions.append(f'Projectile uses {item} as a vanilla item model with an end-rod trail.')
                if not m.get('on_hit'):
                    descriptions.append('Default creature-impact damage adds enchanted-hit particles on victims.')
            elif kind in ('summon', 'decoy'):
                descriptions.append(f'Vanilla {ids.get("entity", "minecraft:zombie")} body' + (f', carrying {ids["weapon"]}' if 'weapon' in ids else '') + '.')
            elif kind in ('area', 'status', 'barrier', 'tether'):
                descriptions.append(f'{kind.capitalize()} uses {ids.get("particle", "minecraft:enchant")} particles; backing marker, where needed, is invisible.')
                if m.get('visual'): descriptions.append('Attached native presentation: ' + visual_description(m['visual']) + '.')
            elif kind == 'wall':
                descriptions.append('Flame particles draw the placed segment; backing marker is invisible.')
            elif kind == 'portal':
                descriptions.append('Portal particles mark both endpoints; backing markers are invisible.')
        if local(data.get('type', '')) in ('damage', 'weapon_damage'):
            descriptions.append('Damage actions add enchanted-hit particles on victims.')
        if data.get('type') == 'fangs':
            descriptions.append('Ground eruptions use crit particles rather than fang models.')
        if data.get('type') == 'explode':
            descriptions.append('The blast action adds vanilla explosion particles.')
        if data.get('type') == 'ignite':
            descriptions.append('Ignition uses vanilla burning visuals.')
        if data.get('type') == 'status':
            descriptions.append('Status applications use vanilla effect behavior and presentation.')
    return ' '.join(dict.fromkeys(descriptions)) or 'Vanilla world or inventory changes; no dedicated spell model, beam, animation, or sound is authored.'


def generate():
    # Loading authoring notes must not create files beside the content tools.
    sys.dont_write_bytecode = True
    module_spec = importlib.util.spec_from_file_location('conversion_notes', ROOT / 'tools/convert_irons_spells.py')
    notes_module = importlib.util.module_from_spec(module_spec)
    module_spec.loader.exec_module(notes_module)
    pf_spec = importlib.util.spec_from_file_location('pathfinder_notes', ROOT / 'tools/convert_pathfinder_spells.py')
    pf_module = importlib.util.module_from_spec(pf_spec)
    pf_spec.loader.exec_module(pf_module)
    entries = []
    for path in sorted(DEFINITIONS.glob('*.json')):
        definition = json.loads(path.read_text())
        source = definition.get('source', {})
        group = 'pathfinder' if source.get('reference', {}).get('system') == 'pathfinder_second_edition' else local(source.get('school', 'native')).removeprefix('irons_spellbooks:')
        notes = pf_module.NOTES[path.stem] if group == 'pathfinder' else notes_module.NOTES[path.stem] if source else NATIVE_NOTES[path.stem]
        display = spell_title(source.get('name', path.stem.replace('_', ' ').title()))
        if group == 'pathfinder': display = 'Pathfinder ' + display
        entries.append((path.stem, definition, display, group, notes))
    assert {entry[0] for entry in entries if entry[3] not in ('native', 'pathfinder')} == set(notes_module.ROWS)
    assert {entry[0] for entry in entries if entry[3] == 'pathfinder'} == set(pf_module.NOTES)
    assert {entry[0] for entry in entries if entry[3] == 'native'} == set(NATIVE_NOTES)
    assert {entry[3] for entry in entries} == set(GROUPS)
    lines = [(ROOT / 'tools/spell-reference-intro.md').read_text().rstrip(), '', '| Catalog group | Spells |', '|---|---|']
    for group in GROUPS:
        label = 'Native examples' if group == 'native' else 'Pathfinder adaptations' if group == 'pathfinder' else group.title() + ' source spells'
        lines.append(f'| [{label}](#{label.lower().replace(" ", "-")}) | {sum(e[3] == group for e in entries)} |')
    for group in GROUPS:
        label = 'Native examples' if group == 'native' else 'Pathfinder adaptations' if group == 'pathfinder' else group.title() + ' source spells'
        grouped = sorted((e for e in entries if e[3] == group), key=lambda e: e[2])
        lines.extend(['', f'## {label}', '', ' · '.join(f'[{e[2]}](#{e[2].lower().replace(" ", "-")})' for e in grouped)])
        for name, definition, display, _, notes in grouped:
            lines.extend(['', f'### {display}', '', notes[0], ''])
            source = definition.get('source')
            reads = sorted({d['trait'] for d in walk([definition.get(k, []) for k in ('effects', 'costs', 'triggers', 'modes')]) if 'trait' in d})
            unused = sorted((set(definition['traits']) & SCALING) - set(reads))
            costs = []
            for cost in definition['costs']:
                if cost['type'] == 'time':
                    costs.append('initial charge ' + duration(cost['ticks']))
                elif cost['type'] == 'mana':
                    costs.append(number(cost['amount']) + ' mana')
                elif cost['type'] == 'cooldown':
                    costs.append('native recovery ' + duration(cost['ticks']))
                else:
                    raise ValueError(f'Unsupported cost: {cost}')
            rows = [
                ('Native ID', f'`vestige:{name}`'),
                ('Rarity', definition['rarity']),
                ('Traditions', ', '.join(definition['traditions'])),
                ('Trait ratings', ', '.join(f'`{local(k)}` {number(v)}' for k, v in definition['traits'].items())),
                ('Authored costs', '; '.join(costs)),
                ('Start triggers', '; '.join(f'{local(t["id"])} on {local(t["event"])}' for t in definition['triggers'])),
                ('Traits actually read', ', '.join(f'`{local(t)}`' for t in reads) or 'None'),
                ('Listed scaling traits unused by plan', ', '.join(f'`{local(t)}`' for t in unused) or 'None'),
            ]
            if source and 'reference' in source:
                ref = source['reference']
                rows.append(('Source metadata', f'[{source["name"]}]({ref["url"]}); {ref["publication"]}; {ref["edition"]}; ' + ('cantrip ' if ref['cantrip'] else 'rank ') + str(ref['rank']) + f'; source rarity {ref["rarity"]}; `{source["revision"]}`'))
            elif source:
                rows.append(('Source metadata', f'`{source["spell"]}`; {source["cast_type"]} cast; cooldown {duration(source["cooldown_ticks"])} in provenance only'))
            rows.append(('Definition', f'[{name}.json](../src/main/resources/data/vestige/runtime_spells/{name}.json)'))
            lines.extend(['| Property | Current definition |', '|---|---|', *(f'| {key} | {value} |' for key, value in rows), '', '**Effect plan**', '', '```text', *plan(definition['effects']), '```', '', '**Current appearance:** ' + presentation(definition['effects']), '', '**Adaptation notes:** ' + (notes[1] or 'Shared native delivery and presentation with an authored initial balance baseline; custom source visuals remain deferred.')])
            for mode in definition.get('modes', []):
                lines.extend(["", "**Alternate mode: " + mode["id"] + "**", "", "Same authored mana, charge and recovery as the primary plan.", "", "```text", *plan(mode["effects"]), "```", "", "**Appearance:** " + presentation(mode["effects"])])
    lines.extend(['', '## Definition files and maintenance', '',
                  'The property tables and effect plans above come from the checked-in JSON, including nested callbacks and reactions. The opening guide and worked examples are authored in [spell-reference-intro.md](../tools/spell-reference-intro.md); review those examples when changing the corresponding spells. Behavior summaries and adaptation notes reuse the explicit conversion recipes, while the four native examples have their own notes in the reference generator.', '',
                  'After editing definitions or conversion notes, refresh this document:', '',
                  '```bash', 'python3 tools/document_spells.py', 'python3 tools/document_spells.py --check', '```', '',
                  'The reference generator does not alter spell definitions. See the [runtime design](design/spell-runtime.md) for lifecycle and data grammar, the [trait catalog](design/trait-catalog.md) for classification rules, the [Iron ledger](design/iron-spell-conversions.md) and [Pathfinder ledger](design/pathfinder-spell-conversions.md) for pinned source links and differences, and [development status](development-status.md) for verification and remaining milestones.', ''])
    return '\n'.join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='fail if the checked-in reference differs from current definitions')
    args = parser.parse_args()
    content = generate()
    if args.check:
        if not OUTPUT.exists() or OUTPUT.read_text() != content:
            raise SystemExit('Spell reference is stale; run python3 tools/document_spells.py')
        print('Spell reference matches the complete checked-in catalog.')
    else:
        OUTPUT.write_text(content)
        print(f'Wrote {OUTPUT.relative_to(ROOT)} for the complete catalog.')


if __name__ == '__main__':
    main()
