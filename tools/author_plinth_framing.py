#!/usr/bin/env python3
"""Historical Stone edge studies; approved framing shares the production geometry helper."""
import argparse
import copy
import json
from apparatus_columns import box, column_model, tent_spellstone
from author_apparatus_models import ROOT, review_model
from apparatus_framing import strip, plinth_framing, spellstone_framing, TRIM_TINT_INDEX, TRIM_TINT_COLOR, TRIM_BRIGHTNESS
from author_apparatus_middle import validate

ARCHIVE = ROOT / 'docs/art/plinth-framing'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-plinth-framing'
STUDIES = {'bands': ('Rim bands', 'stone_plinth'),
           'corners': ('Corner lines', 'andesite_plinth'),
           'panels': ('Framed sides', 'diorite_plinth'),
           'corner_ribs': ('Corner strips + ribs', 'granite_plinth'),
           'corner_edges': ('Corner strips + rim edges', 'cobblestone_plinth')}
SOFT_FRAME = 'minecraft:block/stone'
FRAME_TINT_INDEX = TRIM_TINT_INDEX
FRAME_TINT_COLOR = TRIM_TINT_COLOR


def rib(side, bottom, top):
    """A shallow ring segment with only its three exposed faces; corners meet cleanly."""
    depth = .18
    bounds = {
        'north': ([3 - depth, bottom, 3 - depth], [13 + depth, top, 3]),
        'south': ([3 - depth, bottom, 13], [13 + depth, top, 13 + depth]),
        'west': ([3 - depth, bottom, 3], [3, top, 13]),
        'east': ([13, bottom, 3], [13 + depth, top, 13]),
    }
    element = box(*bounds[side], 'frame')
    element['faces'] = {face: element['faces'][face] for face in (side, 'up', 'down')}
    return element


def model(kind, part):
    assert kind in STUDIES
    if kind == 'corner_edges':
        result = plinth_framing(column_model(review_model('stone', 'plinth'), part), part)
        validate(result)
        return result
    result = column_model(review_model('stone', 'plinth'), part)
    original = copy.deepcopy(result['elements'])
    result['textures']['frame'] = 'minecraft:block/polished_deepslate'
    foot, cap = part in ('single', 'base'), part in ('single', 'cap')
    bottom, top = 2 if foot else 0, 12 if cap else 16
    for side, edge in (('north', 3), ('south', 13), ('west', 3), ('east', 13)):
        if kind == 'bands':
            for low, high in ((0, .65),) if foot else ():
                result['elements'].append(strip(side, 2, 14, low, high, 2 if edge == 3 else 14))
            for low, high in ((13.35, 14),) if cap else ():
                result['elements'].append(strip(side, 2, 14, low, high, 2 if edge == 3 else 14))
        if kind in ('corners', 'corner_ribs'):
            for left, right in ((3, 3.75), (12.25, 13)):
                result['elements'].append(strip(side, left, right, bottom, top, edge))
        elif kind == 'panels':
            low, high = bottom + 1, top - 1
            for left, right, a, b in ((4, 4.5, low, high), (11.5, 12, low, high),
                                       (4.5, 11.5, low, low + .5), (4.5, 11.5, high - .5, high)):
                result['elements'].append(strip(side, left, right, a, b, edge))
        if kind == 'corner_ribs':
            heights = ([(2.65, 3), (3.5, 3.85)] if foot else []) + \
                      ([(10.15, 10.5), (11, 11.35)] if cap else [])
            result['elements'].extend(rib(side, low, high) for low, high in heights)
    validate(result)
    assert result['elements'][:len(original)] == original
    for element in result['elements'][len(original):]:
        if any(a == b for a, b in zip(element['from'], element['to'])):
            assert len(element['faces']) == 1
        else:
            assert kind == 'corner_ribs' and len(element['faces']) == 3
            assert max(element['to'][0], element['to'][2]) <= 13.18
            assert min(element['from'][0], element['from'][2]) >= 2.82
            assert element['to'][1] < 6 or element['from'][1] > 10
    # Column joins carry the same corner strips without repeating cap/foot collars.
    if kind == 'corner_ribs' and part == 'shaft':
        assert all(e['from'][1] == 0 and e['to'][1] == 16
                   for e in result['elements'][len(original):])
    return result


def spellstone_model(framed):
    original = tent_spellstone(review_model('stone', 'spellstone'))
    result = spellstone_framing(original) if framed else original
    validate(result)
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    outputs = {PACK / 'pack.mcmeta': {'pack': {'pack_format': 34,
               'description': 'Stone apparatus framing studies; capture only'}}}
    ledger = []
    for kind, (name, carrier) in STUDIES.items():
        for part in ('single', 'base', 'shaft', 'cap'):
            current = model(kind, part)
            suffix = '' if part == 'single' else '_' + part
            outputs[PACK / 'assets/vestige/models/block' / (carrier + suffix + '.json')] = current
            outputs[ARCHIVE / 'models' / (kind + suffix + '.json')] = current
            if part == 'single':
                outputs[PACK / 'assets/vestige/models/item' / (carrier + '.json')] = current
                ledger.append({'id': kind, 'name': name, 'carrier': 'vestige:' + carrier,
                               'body': 'minecraft:block/stone', 'frame': current['textures']['frame'],
                               'solidCuboids': sum(all(a != b for a, b in zip(e['from'], e['to']))
                                                  for e in current['elements']),
                               'bakedFaces': sum(len(e['faces']) for e in current['elements'])})
    stones = []
    for framed, carrier in ((False, 'andesite_spellstone'), (True, 'stone_spellstone')):
        current = spellstone_model(framed)
        name = 'spellstone_edges' if framed else 'spellstone_plain'
        outputs[PACK / 'assets/vestige/models/block' / (carrier + '.json')] = current
        outputs[PACK / 'assets/vestige/models/item' / (carrier + '.json')] = current
        outputs[ARCHIVE / 'models' / (name + '.json')] = current
        stones.append({'id': name, 'carrier': 'vestige:' + carrier,
                       'frame': current['textures'].get('frame'),
                       'solidCuboids': 3, 'bakedFaces': sum(len(e['faces']) for e in current['elements'])})
    outputs[ARCHIVE / 'designs.json'] = {'scope': 'Preview-only alternatives; no production apparatus art or collision changes',
                                       'studies': ledger, 'spellstones': stones,
                                       'currentTrim': {'texture': SOFT_FRAME, 'tintIndex': FRAME_TINT_INDEX,
                                                       'tintColor': FRAME_TINT_COLOR,
                                                       'brightness': TRIM_BRIGHTNESS}}
    for path, value in outputs.items():
        text = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == text, str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text)
    print(f'{len(STUDIES)} Plinth studies and 2 Spellstone views: ' + ('checked' if args.check else 'generated'))


if __name__ == '__main__':
    main()
