#!/usr/bin/env python3
"""Same native Spellstone and gilded edge, four vanilla material studies; capture only."""
import argparse
import copy
import hashlib
import json
import math
import struct
import zipfile
from apparatus_columns import box
from author_apparatus_models import ROOT
from author_apparatus_middle import validate
from author_astral_bodies import octagon

ARCHIVE = ROOT / 'docs/art/gilded-spellstone-finishes'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-gilded-finishes'
VANILLA = ROOT / 'build/moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar'
FINISHES = (
    ('stone', 'Smooth Stone', 'spellstone', 'smooth_stone'),
    ('blackstone', 'Polished Blackstone', 'tuff_spellstone', 'polished_blackstone'),
    ('quartz', 'Smooth Quartz', 'quartz_spellstone', 'quartz_block_bottom'),
    ('sandstone', 'Smooth Sandstone', 'sandstone_spellstone', 'sandstone_top'),
)


def gilding():
    """A shallow folded edge; flat decals add no solids or stepped geometry."""
    pieces = []
    width = .5
    for a, b in (([5, 7.012, 1], [11, 7.012, 1 + width]),
                 ([5, 7.012, 15 - width], [11, 7.012, 15]),
                 ([1, 7.012, 5], [1 + width, 7.012, 11]),
                 ([15 - width, 7.012, 5], [15, 7.012, 11])):
        element = box(a, b, 'gold')
        element['faces'] = {'up': {'texture': '#gold', 'uv': [4, 4, 12, 5]}}
        pieces.append(element)
    near = 3 + width / (2 * math.sqrt(2))
    half = (4 * math.sqrt(2) - width) / 2
    for x, z, angle in ((near, near, 45), (16-near, near, -45),
                        (16-near, 16-near, 45), (near, 16-near, -45)):
        element = box([x-half, 7.012, z-width/2], [x+half, 7.012, z+width/2], 'gold')
        element['rotation'] = {'origin': [x, 7.012, z], 'axis': 'y', 'angle': angle, 'rescale': False}
        element['faces'] = {'up': {'texture': '#gold', 'uv': [4, 4, 12, 5]}}
        pieces.append(element)
    for side, a, b in (
        ('north', [5, 6.25, .992], [11, 7.016, .992]),
        ('south', [5, 6.25, 15.008], [11, 7.016, 15.008]),
        ('west', [.992, 6.25, 5], [.992, 7.016, 11]),
        ('east', [15.008, 6.25, 5], [15.008, 7.016, 11]),
    ):
        element = box(a, b, 'gold')
        element['faces'] = {side: {'texture': '#gold', 'uv': [4, 5, 12, 6]}}
        pieces.append(element)
    for x, z, angle, side in ((4, 4, 45, 'north'), (12, 4, -45, 'north'),
                              (12, 12, 45, 'south'), (4, 12, -45, 'south')):
        outside = z + (-1 if side == 'north' else 1) * (math.sqrt(2) + .008)
        element = box([x-2*math.sqrt(2), 6.25, outside], [x+2*math.sqrt(2), 7.016, outside], 'gold')
        element['rotation'] = {'origin': [x, 3.5, z], 'axis': 'y', 'angle': angle, 'rescale': False}
        element['faces'] = {side: {'texture': '#gold', 'uv': [4, 5, 12, 6]}}
        pieces.append(element)
    return pieces


def models():
    body = octagon(0, 7)
    for element in body:
        for face, data in element['faces'].items():
            data['texture'] = '#stone'
            # A consistent native texel scale on the short sides, without tile-edge seams.
            if face not in ('up', 'down'):
                length = element['to'][0 if face in ('north', 'south') else 2] - element['from'][0 if face in ('north', 'south') else 2]
                data['uv'] = [(16-length)/2, 2, (16+length)/2, 2 + element['to'][1]]
    result = {}
    for key, name, carrier, tile in FINISHES:
        model = {'parent': 'minecraft:block/block', 'ambientocclusion': True, 'render_type': 'minecraft:cutout',
                 'textures': {'stone': 'minecraft:block/' + tile, 'particle': 'minecraft:block/' + tile,
                              'gold': 'minecraft:block/gold_block'},
                 'elements': copy.deepcopy(body) + gilding()}
        validate(model)
        assert len(model['elements']) == 23
        assert sum(all(a < b for a, b in zip(e['from'], e['to'])) for e in model['elements']) == 7
        result[key] = (name, carrier, tile, model)
    assert all(entry[3]['elements'] == next(iter(result.values()))[3]['elements'] for entry in result.values())
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    outputs = {PACK / 'pack.mcmeta': {'pack': {'pack_format': 34,
               'description': 'Vestige gilded material studies (capture only)'}}}
    ledger, tiles = [], []
    for key, (name, carrier, tile, model) in models().items():
        outputs[ARCHIVE / 'models' / (key + '.json')] = model
        for context in ('block', 'item'):
            outputs[PACK / 'assets/vestige/models' / context / (carrier + '.json')] = model
        ledger.append({'id': key, 'name': name, 'carrier': 'vestige:' + carrier,
                       'tile': 'minecraft:block/' + tile, 'solidCuboids': 7, 'flatGildingFaces': 16,
                       'bakedFaces': sum(len(e['faces']) for e in model['elements'])})
    # Read the installed baseline's original assets, without generating or altering raster textures.
    with zipfile.ZipFile(VANILLA) as vanilla:
        for tile in ('smooth_stone', 'polished_blackstone', 'quartz_block_bottom', 'sandstone_top', 'gold_block'):
            path = 'assets/minecraft/textures/block/' + tile + '.png'
            data = vanilla.read(path)
            assert struct.unpack('>II', data[16:24]) == (16, 16)
            tiles.append({'path': path, 'size': [16, 16], 'sha256': hashlib.sha256(data).hexdigest()})
    outputs[ARCHIVE / 'designs.json'] = {
        'status': 'Material study requested after owner disliked all three preceding stonework candidates; gilded trim requested',
        'controlled': 'Identical seven-solid low octagon, continuous folded gold edge and accepted lowered native Astral Seal. Only the body texture differs.',
        'dimensions': {'footprint': '14/16', 'receivingHeight': '7/16', 'topGildingWidth': '.5 model unit', 'sideBandHeight': '.766 model unit'},
        'scope': 'Native preview pack only. No new body, construction ingredient, collision or Prism installation is selected.',
        'designs': ledger, 'vanillaTextures': tiles}
    for path, value in outputs.items():
        contents = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == contents, 'Gilded finish drift: ' + str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
    print('Four gilded finishes: identical geometry/seal, seven solids, verified native 16x16 material assets.')


if __name__ == '__main__':
    main()
