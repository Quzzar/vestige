#!/usr/bin/env python3
"""Three heavy, interlocking relic fragments beneath the accepted native Astral Seal."""
import argparse
import hashlib
import json
import struct
from zipfile import ZipFile
from apparatus_columns import box
from author_apparatus_models import ROOT
from author_apparatus_middle import validate

ARCHIVE = ROOT / 'docs/art/fractured-spellstone'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-fractured-spellstone'
VANILLA = ROOT / 'build/moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar'
TILE = 'stone'


def model():
    # Six broad cuboids form three pieces, with staggered breaks rather than parallel rails.
    # The center's two adjoining cuboids share one flat y7 surface for real scrolls.
    elements = [
        box([5.5, 0, 5.8], [8.5, 7, 14.5]),
        box([8.5, 0, 5.5], [12, 7, 14.5]),
        box([1.5, 0, 1.5], [4.5, 6.7, 14.5]),
        box([4.5, 0, 1.5], [8.5, 6.7, 4.8]),
        box([9.5, 0, 1.8], [14.5, 6.85, 4.5]),
        box([13, 0, 4.5], [14.5, 6.85, 14.5]),
    ]
    # Keep one uninterrupted native texture phase across the reassembled fragments.
    # Thin side faces crop the tile at their actual model dimensions.
    result = {'parent': 'minecraft:block/block', 'ambientocclusion': True, 'render_type': 'minecraft:cutout',
              'textures': {'stone': 'minecraft:block/' + TILE, 'particle': 'minecraft:block/' + TILE},
              'elements': elements}
    validate(result)
    assert len(elements) == 6 and all(all(a < b for a, b in zip(e['from'], e['to'])) for e in elements)
    assert all('rotation' not in e for e in elements)
    # The centered flat scroll footprint stays on the central piece.
    assert elements[0]['from'][0] <= 6 and elements[1]['to'][0] >= 10
    assert max(elements[0]['from'][2], elements[1]['from'][2]) < 6
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    body = model()
    path = 'assets/minecraft/textures/block/' + TILE + '.png'
    with ZipFile(VANILLA) as jar:
        tile = jar.read(path)
        assert struct.unpack('>II', tile[16:24]) == (16, 16)
    outputs = {
        PACK/'pack.mcmeta': {'pack': {'pack_format': 34, 'description': 'Vestige fractured relic (native art preview)'}},
        ARCHIVE/'model.json': body,
        PACK/'assets/vestige/models/block/spellstone.json': body,
        PACK/'assets/vestige/models/item/spellstone.json': body,
        ARCHIVE/'designs.json': {
            'status': 'Owner authorized the fractured-relic direction after resetting everything except the top runes',
            'signature': 'Three heavy stone fragments with staggered narrow breaks; the Astral Seal bridges their flat upper surfaces.',
            'rejected': 'Literal Amethyst/Diamond edge decoration, continuous trim, gem collars and the repeated octagonal body.',
            'dimensions': {'centralSurface': '7/16', 'otherSurfaces': ['6.7/16', '6.85/16'],
                           'footprint': '13/16', 'fractureGaps': '1 model unit'},
            'fragments': 3, 'solidCuboids': 6, 'bakedFaces': sum(len(e['faces']) for e in body['elements']),
            'vanillaTextures': [{'path': path, 'size': [16, 16], 'sha256': hashlib.sha256(tile).hexdigest()}],
            'scope': 'One native art preview. Accepted Astral renderer, production collision/scroll placement, recipes, Plinths and installed Prism jar unchanged.'}}
    for path, value in outputs.items():
        contents = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == contents, 'Fractured relic drift: ' + str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
    print('Fractured Spellstone: three fragments, six solid cuboids, 36 baked faces; original 16x16 Stone.')


if __name__ == '__main__':
    main()
