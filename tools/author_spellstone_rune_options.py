#!/usr/bin/env python3
"""Same plain Sealstone for three opt-in native rune treatments; no raster glyph."""
import argparse
import copy
import json
from author_apparatus_models import ROOT, physical
from author_apparatus_middle import validate
from author_spellstone_designs import designs

ARCHIVE = ROOT / 'docs/art/spellstone-rune-options'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-spellstone-rune-options'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    model = physical(designs()['seal'][2])
    model['textures'].pop('runes')
    validate(model)
    assert len(model['elements']) == 7
    outputs = {PACK / 'pack.mcmeta': {'pack': {'pack_format': 34,
        'description': 'Vestige native rune options (capture only)'}}}
    for carrier in ('spellstone', 'tuff_spellstone', 'quartz_spellstone'):
        for context in ('block', 'item'):
            outputs[PACK / 'assets/vestige/models' / context / (carrier + '.json')] = copy.deepcopy(model)
    outputs[ARCHIVE / 'model.json'] = model
    outputs[ARCHIVE / 'designs.json'] = {
        'status': 'Three native rune treatments requested after owner rejected the pixelated purple overlay',
        'shared': 'Same seven-cuboid Stone Brick Sealstone with Polished Andesite top; no baked rune texture.',
        'scope': 'Opt-in capture renderer and resource pack; production art and installed Prism jar unchanged.',
        'designs': [
            {'id': 'engraving', 'name': 'Illuminated Engraving', 'carrier': 'vestige:spellstone',
             'signature': 'A stationary eight-armed seal traced with fine pale-violet strokes in dark carved channels.'},
            {'id': 'astral', 'name': 'Astral Seal', 'carrier': 'vestige:tuff_spellstone',
             'signature': 'Two counter-rotating broken rings and an interlocked central glyph float above the stone.'},
            {'id': 'living', 'name': 'Living Script', 'carrier': 'vestige:quartz_spellstone',
             'signature': 'A moving point of light writes an angular glyph, leaving a fading trail and rising motes.'}
        ]}
    for path, value in outputs.items():
        contents = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == contents, 'Rune option drift: ' + str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
    print('Three rune treatments share one seven-cuboid Sealstone; native procedural renderer supplies the light.')


if __name__ == '__main__':
    main()
