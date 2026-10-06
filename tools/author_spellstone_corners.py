#!/usr/bin/env python3
"""Four native pixel corner studies and three finish comparisons; capture only."""
import argparse
import copy
import hashlib
import json
import struct
import zipfile
from apparatus_columns import spellstone_corner_pixels, SPELLSTONE_CORNER_PALETTE
from author_apparatus_models import ROOT
from author_apparatus_middle import validate
from author_gilded_spellstone import VANILLA

ARCHIVE = ROOT / 'docs/art/spellstone-corner-details/smaller-corners'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-corner-details'
CORNER_SCALE = .65
PALETTE = SPELLSTONE_CORNER_PALETTE
PATTERNS = {
    'chip': ('Corner chip', ('pm.', 'md.', 'd..'), .85),
    'notch': ('Stepped inset', ('dp.', 'mcw', 'dm.'), .85),
    'vein': ('Broken vein', ('pm..', 'dcp.', '.dm.', '..d.'), .72),
    'crust': ('Crystal flecks', ('cp.d', 'mdm.', 'd.pm', '.d..'), .72),
}
STUDIES = (
    ('chip', 'spellstone', 'smooth_stone', 'chip'),
    ('notch', 'tuff_spellstone', 'smooth_stone', 'notch'),
    ('vein', 'quartz_spellstone', 'smooth_stone', 'vein'),
    ('crust', 'sandstone_spellstone', 'smooth_stone', 'crust'),
    ('blackstone', 'polished_blackstone_spellstone', 'polished_blackstone', 'notch'),
    ('quartz', 'smooth_quartz_spellstone', 'quartz_block_bottom', 'notch'),
    ('sandstone', 'smooth_sandstone_spellstone', 'sandstone_top', 'notch'),
)
TILES = ('smooth_stone', 'polished_blackstone', 'quartz_block_bottom', 'sandstone_top', 'diamond_block')


def corner_pixels(pattern):
    """Each of four vertical corners wraps onto its two adjacent side faces."""
    _, rows, size = PATTERNS[pattern]
    return spellstone_corner_pixels(rows, size * CORNER_SCALE)


def models():
    source = json.loads((ARCHIVE / 'models/chip.json').read_text())
    body = copy.deepcopy(source['elements'][:3])
    assert len(body) == 3 and body[2]['from'] == [2, 4.75, 2] and body[2]['to'] == [14, 8, 14]
    result = {}
    for key, carrier, tile, pattern in STUDIES:
        model = copy.deepcopy(source)
        model['textures'] = {'stone': 'minecraft:block/' + tile, 'particle': 'minecraft:block/' + tile,
                             'diamond': 'minecraft:block/diamond_block'}
        model['elements'] = copy.deepcopy(body) + corner_pixels(pattern)
        validate(model)
        assert sum(all(a < b for a, b in zip(e['from'], e['to'])) for e in model['elements']) == 3
        assert all(set(e['faces']).issubset({'north', 'south', 'east', 'west'}) for e in model['elements'][3:])
        result[key] = model
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    outputs = {PACK / 'pack.mcmeta': {'pack': {'pack_format': 34,
               'description': 'Vestige four vertical corner details and finish studies (capture only)'}}}
    ledger = []
    authored = models()
    for key, carrier, tile, pattern in STUDIES:
        model = authored[key]
        outputs[ARCHIVE / 'models' / (key + '.json')] = model
        for context in ('block', 'item'):
            outputs[PACK / 'assets/vestige/models' / context / (carrier + '.json')] = model
        name, rows, size = PATTERNS[pattern]
        ledger.append({'id': key, 'name': name, 'carrier': 'vestige:' + carrier,
                       'bodyTile': 'minecraft:block/' + tile, 'pattern': list(rows), 'pixelWidth': size * CORNER_SCALE,
                       'solidCuboids': 3, 'cornerFaces': len(model['elements']) - 3,
                       'bakedFaces': sum(len(e['faces']) for e in model['elements'])})
    tiles = []
    with zipfile.ZipFile(VANILLA) as vanilla:
        for tile in TILES:
            path = 'assets/minecraft/textures/block/' + tile + '.png'
            data = vanilla.read(path)
            assert struct.unpack('>II', data[16:24]) == (16, 16)
            tiles.append({'path': path, 'size': [16, 16], 'sha256': hashlib.sha256(data).hexdigest()})
    outputs[ARCHIVE / 'designs.json'] = {
        'status': 'Owner liked the corner studies and requested smaller Diamond patches; no individual pattern or finish selected',
        'cornerScale': CORNER_SCALE,
        'revision': 'Same pixel patterns and upper-corner anchors; patch width and height reduced by 35 percent, area by 57.75 percent.',
        'shared': 'Same compact three-solid tent body and Astral Seal; four details wrap the vertical slab corners onto both adjacent faces. Top and side centers are bare.',
        'scope': 'Isolated native capture resource pack. Registered carrier names are preview aliases; production artwork, recipes and collision are unchanged.',
        'palette': PALETTE, 'designs': ledger, 'vanillaTextures': tiles}
    for path, value in outputs.items():
        contents = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == contents, 'Corner study drift: ' + str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
    print('Four smaller corner designs plus three finishes: 65 percent patch dimensions; native 16x16 palette; three shared solids.')


if __name__ == '__main__':
    main()
