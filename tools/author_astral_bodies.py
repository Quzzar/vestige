#!/usr/bin/env python3
"""Three native stonework studies beneath the accepted Astral Seal; capture only."""
import argparse
import copy
import json
from apparatus_columns import box
from author_apparatus_models import ROOT, review_model
from author_apparatus_middle import validate
from author_spellstone_designs import designs

ARCHIVE = ROOT / 'docs/art/astral-body-options'
PACK = ROOT / 'run/effects-capture/resourcepacks/vestige-astral-bodies'


def octagon(low, high):
    elements = copy.deepcopy(designs()['seal'][2]['elements'][:7])
    for i, element in enumerate(elements):
        element['from'][1] = low
        element['to'][1] = high - (.002 if 'rotation' in element else 0)
        if 'rotation' in element:
            element['rotation']['origin'][1] = (low + element['to'][1]) / 2
        element['faces'] = box(element['from'], element['to'], 'stone')['faces']
        element['faces']['up']['texture'] = '#trim'
        # The diagonal pieces overlap internally; bake only their exposed faces.
        internal = {0: ('north', 'south'), 1: ('south', 'east', 'west'),
                    2: ('north', 'east', 'west'), 3: ('south', 'east', 'west'),
                    4: ('south', 'east', 'west'), 5: ('north', 'east', 'west'),
                    6: ('north', 'east', 'west')}[i]
        for side in internal:
            element['faces'].pop(side, None)
    return elements


def plaque(side, a, b, uv, texture='carving'):
    element = box(a, b, texture)
    element['faces'] = {side: {'texture': '#' + texture, 'uv': uv}}
    return element


def diamond(side, z):
    element = plaque(side, [7.2, 3.45, z], [8.8, 5.05, z], [3, 3, 13, 13], 'diamond')
    element['rotation'] = {'origin': [8, 4.25, z], 'axis': 'z', 'angle': 45, 'rescale': False}
    return element


def options():
    # One shared palette and one shared native animation make the stonework comparable.
    original = review_model('stone_bricks', 'spellstone')
    textures = {key: original['textures'][key] for key in ('stone', 'carving', 'trim', 'diamond', 'particle')}

    relic = octagon(1.25, 7) + [box([3, 0, 3], [13, 1.25, 13], 'trim')]
    for side, z in (('north', .992), ('south', 15.008)):
        relic += [plaque(side, [5.5, 1.8, z], [10.5, 6.6, z], [0, 0, 16, 16]),
                  diamond(side, z + (-.008 if side == 'north' else .008))]

    tablet = [box([3, 1.25, 1], [13, 6.75, 15], 'stone'),
              box([1, 1.25, 3], [3, 6.75, 13], 'stone'),
              box([13, 1.25, 3], [15, 6.75, 13], 'stone'),
              box([3, 0, 3], [13, 1.25, 13], 'trim'),
              box([3, 6.75, 3], [13, 7, 13], 'trim'),
              box([3, 6.75, 1], [13, 7.5, 3], 'trim'),
              box([3, 6.75, 13], [13, 7.5, 15], 'trim')]
    # The two broad carved bindings are structural masses, not a perimeter lip.
    for i in (0, 1, 2):
        tablet[i]['faces']['up']['texture'] = '#trim'
        # The raw brick crop produced a distracting vertical mortar bar on the front.
        # Use dressed stone faces here, leaving the carved bindings as the texture accent.
        for side in ('north', 'south', 'east', 'west'):
            width = tablet[i]['to'][0 if side in ('north', 'south') else 2] - tablet[i]['from'][0 if side in ('north', 'south') else 2]
            height = tablet[i]['to'][1] - tablet[i]['from'][1]
            # One texel per model unit; fitting a whole tile into a short face makes noisy stripes.
            tablet[i]['faces'][side] = {'texture': '#trim', 'uv': [(16-width)/2, 4, (16+width)/2, 4+height]}
    for i in (5, 6):
        tablet[i]['faces']['up'] = {'texture': '#carving', 'uv': [2, 5, 14, 8]}
    for z in (2.0, 13.65):
        tablet.append(plaque('up', [5.25, 7.508, z], [10.75, 7.508, z + .35], [2, 6, 14, 9], 'diamond'))

    vault = octagon(3, 7) + [box([3, 0, 4], [6, 3, 12], 'stone'),
                            box([10, 0, 4], [13, 3, 12], 'stone')]
    # A single large carved tablet on each end gives the low altar a readable face.
    # No gems need to be shown literally just because the recipe contains them.
    for side, z in (('north', .992), ('south', 15.008)):
        vault.append(plaque(side, [5, 3.5, z], [11, 6.5, z], [0, 4, 16, 12]))

    candidates = {
        'relic': ('Inscribed Relic', 'spellstone', relic,
                  'A substantial octagonal mass on a low inset foot, with two carved faces and a single diamond-shaped focus in each.'),
        'tablet': ('Bound Tablet', 'tuff_spellstone', tablet,
                   'Square-cut corners, an inset scroll bed and two broad carved stone bindings crossed by fine cyan seams.'),
        'vault': ('Vaulted Seal', 'quartz_spellstone', vault,
                  'The octagonal seal rests on two squat piers, leaving a real opening beneath; broad front/back inscriptions replace jewelry trim.')}
    result = {}
    for key, (name, carrier, elements, signature) in candidates.items():
        model = {'parent': 'minecraft:block/block', 'ambientocclusion': True,
                 'render_type': 'minecraft:cutout', 'textures': copy.deepcopy(textures), 'elements': elements}
        validate(model)
        solids = sum(all(a < b for a, b in zip(e['from'], e['to'])) for e in elements)
        assert solids <= 9
        assert 'runes' not in json.dumps(model)
        result[key] = (name, carrier, model, signature)
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    outputs = {PACK / 'pack.mcmeta': {'pack': {'pack_format': 34,
               'description': 'Vestige Astral stonework alternatives (capture only)'}}}
    ledger = []
    for key, (name, carrier, model, signature) in options().items():
        outputs[ARCHIVE / 'models' / (key + '.json')] = model
        for context in ('block', 'item'):
            outputs[PACK / 'assets/vestige/models' / context / (carrier + '.json')] = model
        ledger.append({'id': key, 'name': name, 'carrier': 'vestige:' + carrier, 'signature': signature,
                       'solidCuboids': sum(all(a < b for a, b in zip(e['from'], e['to'])) for e in model['elements']),
                       'flatFaces': sum(not all(a < b for a, b in zip(e['from'], e['to'])) for e in model['elements']),
                       'bakedFaces': sum(len(e['faces']) for e in model['elements'])})
    outputs[ARCHIVE / 'designs.json'] = {
        'status': 'Three body studies requested after owner rejected the four corner ticks and thin perimeter trim',
        'shared': 'The accepted, lowered native Astral Seal; same Stone Brick/Polished Andesite palette; vanilla 16x16 texture assets; flat scroll bed at y7/16.',
        'scope': 'Opt-in native Minecraft preview pack only. Production models, shapes, interactions, recipes and Prism installation remain unchanged.',
        'designs': ledger}
    for path, value in outputs.items():
        contents = json.dumps(value, indent=2) + '\n'
        if args.check:
            assert path.is_file() and path.read_text() == contents, 'Astral body drift: ' + str(path)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(contents)
    print('Astral body options: Inscribed Relic 8 solids, Bound Tablet 7, Vaulted Seal 9; capture only.')


if __name__ == '__main__':
    main()
