#!/usr/bin/env python3
"""Quiet stone edging and two Diamond top inlays, across four native preview finishes."""
import argparse
import copy
import hashlib
import json
import struct
import zipfile
from apparatus_columns import box
from author_apparatus_middle import validate
from author_gilded_spellstone import ROOT, VANILLA, gilding, models as material_models

ARCHIVE = ROOT / 'docs/art/spellstone-diamond-trim'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-diamond-trim'
TRIMS = {'stone': 'polished_deepslate', 'blackstone': 'smooth_stone',
         'quartz': 'smooth_stone', 'sandstone': 'cut_sandstone'}
TILES = ('smooth_stone', 'polished_blackstone', 'quartz_block_bottom', 'sandstone_top',
         'polished_deepslate', 'cut_sandstone', 'diamond_block')


def edging():
    pieces = gilding()
    for element in pieces:
        if element['from'][1] < 7:
            element['from'][1] = 6.566
        for face in element['faces'].values():
            face['texture'] = '#trim'
            face['uv'] = [4, 5, 12, 6]
    return pieces


def inlays():
    pieces = []
    # Two opposed, fitted Diamond shapes echo the construction recipe without corner clips.
    for z in (2.5, 13.5):
        for width, y, texture in ((2, 7.024, 'trim'), (1.5, 7.032, 'diamond')):
            element = box([8-width/2, y, z-width/2], [8+width/2, y, z+width/2], texture)
            element['rotation'] = {'origin': [8, y, z], 'axis': 'y', 'angle': 45, 'rescale': False}
            element['faces'] = {'up': {'texture': '#' + texture, 'uv': [3, 3, 13, 13]}}
            pieces.append(element)
    return pieces


def models():
    result = {}
    for key, (name, carrier, tile, source) in material_models().items():
        model = copy.deepcopy(source)
        model['textures'].pop('gold')
        model['textures']['trim'] = 'minecraft:block/' + TRIMS[key]
        model['textures']['diamond'] = 'minecraft:block/diamond_block'
        model['elements'] = model['elements'][:7] + edging() + inlays()
        validate(model)
        assert len(model['elements']) == 27
        assert sum(all(a < b for a, b in zip(e['from'], e['to'])) for e in model['elements']) == 7
        assert 'gold' not in json.dumps(model)
        result[key] = (name, carrier, tile, model)
    assert all(entry[3]['elements'] == next(iter(result.values()))[3]['elements'] for entry in result.values())
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    outputs = {PACK / 'pack.mcmeta': {'pack': {'pack_format': 34,
               'description': 'Vestige stone trim and Diamond inlays (capture only)'}}}
    ledger, tiles = [], []
    for key, (name, carrier, tile, model) in models().items():
        outputs[ARCHIVE / 'models' / (key + '.json')] = model
        for context in ('block', 'item'):
            outputs[PACK / 'assets/vestige/models' / context / (carrier + '.json')] = model
        ledger.append({'id': key, 'name': name, 'carrier': 'vestige:' + carrier,
                       'bodyTile': 'minecraft:block/' + tile, 'trimTile': 'minecraft:block/' + TRIMS[key],
                       'solidCuboids': 7, 'flatEdgingFaces': 16, 'flatInlayFaces': 4,
                       'bakedFaces': sum(len(e['faces']) for e in model['elements'])})
    with zipfile.ZipFile(VANILLA) as vanilla:
        for tile in TILES:
            path = 'assets/minecraft/textures/block/' + tile + '.png'
            data = vanilla.read(path)
            assert struct.unpack('>II', data[16:24]) == (16, 16)
            tiles.append({'path': path, 'size': [16, 16], 'sha256': hashlib.sha256(data).hexdigest()})
    outputs[ARCHIVE / 'designs.json'] = {
        'status': 'Owner clarified ordinary trim rather than gilding, with some Diamond on top',
        'shared': 'Same seven-solid low octagon and lowered Astral Seal. A narrow stone border and two opposed fitted Diamond top inlays replace the Gold edge.',
        'dimensions': {'footprint': '14/16', 'receivingHeight': '7/16', 'topTrimWidth': '.5 model unit', 'sideTrimHeight': '.45 model unit'},
        'scope': 'Native preview pack only; no replacement selected or installed, no gameplay or construction change.',
        'designs': ledger, 'vanillaTextures': tiles}
    for path, value in outputs.items():
        contents = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == contents, 'Stone trim drift: ' + str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
    print('Four stone-trim finishes: seven solids, two fitted Diamond top inlays, original 16x16 textures; no Gold.')


if __name__ == '__main__':
    main()
