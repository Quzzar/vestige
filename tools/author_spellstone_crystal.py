#!/usr/bin/env python3
"""One native Spellstone study: faceted crystal embedded in the stone's outer edges."""
import argparse
import copy
import hashlib
import json
import struct
from zipfile import ZipFile
from apparatus_columns import box
from author_apparatus_models import ROOT
from author_apparatus_middle import validate
from author_astral_bodies import octagon

ARCHIVE = ROOT / 'docs/art/spellstone-crystal-core'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-crystal-core'
VANILLA = ROOT / 'build/moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar'
TILES = ('smooth_stone', 'amethyst_block', 'diamond_block')


def native_uv(element):
    """Keep a consistent texel scale on dressed stone, avoiding compressed full tiles."""
    for side, face in element['faces'].items():
        if side in ('up', 'down'):
            continue
        axis = 0 if side in ('north', 'south') else 2
        width = element['to'][axis] - element['from'][axis]
        height = element['to'][1] - element['from'][1]
        face['uv'] = [(16-width)/2, 5, (16+width)/2, 5+height]
    return element


def model():
    body = octagon(0, 7)
    for i, element in enumerate(body):
        native_uv(element)
        for face in element['faces'].values():
            face['texture'] = '#amethyst' if i >= 3 else '#top'

    # Amethyst replaces the solid diagonal edge sections of the body.
    # Diamond facets are cut into two opposing upper crystal edges, not attached jewels.
    elements = []
    for i, original in enumerate(body):
        if i not in (3, 5):
            elements.append(original)
            continue
        low, high = original['from'], original['to']
        outer_north = i == 3
        boundary = low[2]+.45 if outer_north else high[2]-.45
        pieces = [
            (low[:], [high[0], 5.5, high[2]], 'amethyst', ('down', 'north' if outer_north else 'south')),
            ([low[0], 5.5, boundary if outer_north else low[2]],
             [high[0], high[1], high[2] if outer_north else boundary], 'amethyst', ('up',)),
            ([low[0], 5.5, low[2] if outer_north else boundary],
             [high[0], high[1], boundary if outer_north else high[2]], 'diamond', ('up', 'north' if outer_north else 'south')),
        ]
        for a, b, texture, faces in pieces:
            piece = native_uv(box(a, b, texture))
            piece['rotation'] = copy.deepcopy(original['rotation'])
            piece['faces'] = {side: face for side, face in piece['faces'].items() if side in faces}
            elements.append(piece)
    result = {'parent': 'minecraft:block/block', 'ambientocclusion': True, 'render_type': 'minecraft:cutout',
              'textures': {'top': 'minecraft:block/smooth_stone',
                           'amethyst': 'minecraft:block/amethyst_block', 'diamond': 'minecraft:block/diamond_block',
                           'particle': 'minecraft:block/smooth_stone'}, 'elements': elements}
    validate(result)
    assert len(elements) == 11 and all(all(a < b for a, b in zip(e['from'], e['to'])) for e in elements)
    assert len([e for e in elements if e['faces'].get('up', {}).get('texture') == '#diamond']) == 2
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    body = model()
    tiles = []
    with ZipFile(VANILLA) as jar:
        for tile in TILES:
            path = 'assets/minecraft/textures/block/' + tile + '.png'
            data = jar.read(path)
            assert struct.unpack('>II', data[16:24]) == (16, 16)
            tiles.append({'path': path, 'size': [16, 16], 'sha256': hashlib.sha256(data).hexdigest()})
    outputs = {
        PACK/'pack.mcmeta': {'pack': {'pack_format': 34, 'description': 'Vestige crystal edges (native preview only)'}},
        ARCHIVE/'model.json': body,
        PACK/'assets/vestige/models/block/spellstone.json': body,
        PACK/'assets/vestige/models/item/spellstone.json': body,
        ARCHIVE/'designs.json': {
            'status': 'One fresh study after the owner selected crystal worked into the stone edges',
            'rejected': 'Continuous trim and separate Diamond top settings were disliked across all four finishes.',
            'signature': 'One uninterrupted stone body with solid Amethyst edge facets; Diamond is cut into two opposing upper crystal edges.',
            'dimensions': {'receivingHeight': '7/16', 'bodyFootprint': '14/16', 'diamondFaceHeight': '1.5/16', 'diamondTopWidth': '.45/16'},
            'solidCuboids': len(body['elements']), 'bakedFaces': sum(len(e['faces']) for e in body['elements']),
            'vanillaTextures': tiles,
            'scope': 'Native preview only; accepted Astral Seal, construction recipe, production collision and Prism installation unchanged.'}}
    for path, value in outputs.items():
        contents = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == contents, 'Crystal-core preview drift: ' + str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
    print(f"Crystal-edge Spellstone: 11 solid sections, {sum(len(e['faces']) for e in body['elements'])} baked faces; original vanilla 16x16 tiles.")


if __name__ == '__main__':
    main()
