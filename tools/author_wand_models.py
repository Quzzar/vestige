#!/usr/bin/env python3
"""Vanilla-resolution, compositional wand and thread item assets. Requires Pillow for export."""
import argparse
import io
import json
import pathlib
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/vestige'
ART = ROOT / 'docs/art/wands-native/source-art'
BASES = ['stick', 'bamboo', 'bone', 'blaze_rod', 'breeze_rod', 'end_rod', 'lightning_rod']
TIPS = ['amethyst', 'diamond', 'emerald', 'ender_pearl', 'copper', 'iron', 'ghast_tear', 'netherite']
THREADS = ['ensorcelled', 'callous', 'smoldering', 'laced', 'consecrated']
BODY_BOUNDS = [(52,95,282,367), (359,95,578,367), (670,95,892,367), (991,95,1203,367),
               (66,443,282,718), (359,443,588,718), (668,442,901,718)]
TIP_BOUNDS = [(117,830,199,923), (427,829,509,923), (739,830,821,923), (1055,830,1149,923),
              (117,1066,199,1149), (429,1066,511,1149), (740,1066,808,1149), (1056,1066,1137,1149)]
THREAD_BOUNDS = [(46,189,391,580), (433,238,791,560), (842,198,1206,597), (1258,198,1650,601), (1689,182,1936,613)]


def sprite(source, bounds, size, position):
    """Production export: crop generated components, nearest-neighbor sampling, hard alpha."""
    region = source.crop(bounds).resize(size, Image.Resampling.NEAREST)
    region.putalpha(region.getchannel('A').point(lambda a: 255 if a >= 128 else 0))
    canvas = Image.new('RGBA', (16, 16))
    canvas.paste(region, position)
    # Transparent pixels carry no fringe color into resource-pack mipmaps.
    pixels = canvas.load()
    for y in range(16):
        for x in range(16):
            if pixels[x,y][3] == 0:
                pixels[x,y] = (0,0,0,0)
    return canvas


def textures():
    result = {}
    with Image.open(ART / 'wand-components.png') as atlas:
        for name, bounds in zip(BASES, BODY_BOUNDS):
            result['textures/item/wands/base_' + name + '.png'] = sprite(atlas, bounds, (12,13), (1,3))
        for name, bounds in zip(TIPS, TIP_BOUNDS):
            result['textures/item/wands/tip_' + name + '.png'] = sprite(atlas, bounds, (4,5), (11,0))
    with Image.open(ART / 'magical-threads.png') as atlas:
        for name, bounds in zip(THREADS, THREAD_BOUNDS):
            width, height = bounds[2]-bounds[0], bounds[3]-bounds[1]
            scale = 14/max(width,height)
            size = (round(width*scale), round(height*scale))
            result['textures/item/' + name + '_thread.png'] = sprite(atlas, bounds, size, ((16-size[0])//2,(16-size[1])//2))
    return result


def models():
    result = {}
    overrides = []
    for i, base in enumerate(BASES):
        for j, tip in enumerate([None, *TIPS]):
            name = 'wand_' + base + ('_' + tip if tip else '')
            layers = {'layer0': 'vestige:item/wands/base_' + base}
            if tip:
                layers['layer1'] = 'vestige:item/wands/tip_' + tip
            result['models/item/' + name + '.json'] = {'parent': 'minecraft:item/handheld', 'textures': layers}
            if i or j:
                overrides.append({'predicate': {'vestige:wand_appearance': i*(len(TIPS)+1)+j}, 'model': 'vestige:item/'+name})
    result['models/item/wand.json'] = {'parent':'vestige:item/wand_stick', 'overrides':overrides}
    for thread in THREADS:
        result['models/item/' + thread + '_thread.json'] = {
            'parent': 'minecraft:item/generated', 'textures': {'layer0':'vestige:item/' + thread + '_thread'}}
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    opts = parser.parse_args()
    for relative, value in models().items():
        path = ASSETS/relative
        text = json.dumps(value,indent=2)+'\n'
        if opts.check:
            if not path.exists() or path.read_text()!=text:
                raise SystemExit('Wand model drift: '+str(path))
        else:
            path.parent.mkdir(parents=True,exist_ok=True)
            path.write_text(text)
    for relative, value in textures().items():
        path = ASSETS/relative
        if opts.check:
            if not path.exists():
                raise SystemExit('Missing sprite: '+str(path))
            with Image.open(path) as existing:
                if existing.mode!='RGBA' or existing.size!=(16,16) or existing.tobytes()!=value.tobytes():
                    raise SystemExit('16x16 sprite drift: '+str(path))
        else:
            path.parent.mkdir(parents=True,exist_ok=True)
            value.save(path)
    print('Verified' if opts.check else 'Authored',len(BASES)*(len(TIPS)+1),'layered wand models and 20 actual 16x16 component/thread sprites')

if __name__ == '__main__':
    main()
