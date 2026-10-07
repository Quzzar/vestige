#!/usr/bin/env python3
"""Vanilla-resolution, compositional wand and thread item assets. Requires Pillow for export."""
import argparse
from collections import Counter
import colorsys
import json
import pathlib
from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'src/main/resources/assets/vestige'
ART = ROOT / 'docs/art/wands-native/source-art'
BASES = ['stick', 'bamboo', 'bone', 'blaze_rod', 'breeze_rod', 'end_rod', 'lightning_rod']
TIPS = ['amethyst', 'diamond', 'emerald', 'ender_pearl', 'copper', 'iron', 'ghast_tear', 'netherite']
THREADS = ['ensorcelled', 'callous', 'smoldering', 'laced', 'consecrated']
BODY_BOUNDS = [(46,91,285,381), (362,91,590,381), (661,91,901,381), (988,91,1208,381),
               (61,440,289,727), (362,441,590,727), (661,440,901,727)]
TIP_BOUNDS = [(115,829,204,939), (429,829,516,939), (736,829,826,939), (1048,829,1156,941),
              (113,1061,203,1161), (426,1061,515,1161), (737,1061,817,1161), (1059,1062,1149,1161)]
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


def gold_binding(rgb):
    hue, saturation, value = colorsys.rgb_to_hsv(*(channel/255 for channel in rgb[:3]))
    return 35 <= hue*360 <= 65 and saturation >= .60 and value >= .35


def binding_pixel(region, x, y):
    # Authored grip-band window in the reference crops. Blaze's golden shaft
    # must retain its own ramp rather than joining this small accent palette.
    return x < region.width*.55 and region.height*.60 <= y < region.height*.90 and gold_binding(region.getpixel((x,y)))


def indexed_colours(region, colours, preserve_binding):
    """Reserve the small gold accent's palette so frequent shaft colours cannot erase it."""
    groups = [[], []] if preserve_binding else [[]]
    for y in range(region.height):
        for x in range(region.width):
            rgb = region.getpixel((x,y))
            if rgb[3] >= 128:
                group = int(preserve_binding and binding_pixel(region,x,y))
                groups[group].append(rgb[:3])
    palettes = []
    for group, opaque in enumerate(groups):
        if not opaque:
            palettes.append(None)
            continue
        sample = Image.new('RGB', (len(opaque),1))
        sample.putdata(opaque)
        budget = (2 if group else colours-2) if preserve_binding else colours
        palette = sample.quantize(colors=budget, method=Image.Quantize.MAXCOVERAGE,
                                  dither=Image.Dither.NONE)
        palettes.append(region.convert('RGB').quantize(palette=palette, dither=Image.Dither.NONE).convert('RGB'))
    return palettes


def wand_sprite(source, bounds, size, position, colours, preserve_binding=False):
    """Indexed, nondithered export: one dominant flat colour per logical pixel.

    Generated source cells have small raster variations even inside their flat
    colour patches. Index the opaque source colours, then take each logical
    cell's majority. Point sampling those variations produced noisy shading.
    Geometry comes entirely from the generated artwork, without painting pixels.
    """
    region = source.crop(bounds).convert('RGBA')
    palettes = indexed_colours(region, colours, preserve_binding)
    canvas = Image.new('RGBA', (16, 16))
    for y in range(size[1]):
        for x in range(size[0]):
            box = (round(x*region.width/size[0]), round(y*region.height/size[1]),
                   round((x+1)*region.width/size[0]), round((y+1)*region.height/size[1]))
            values = [[], []] if preserve_binding else [[]]
            for py in range(box[1],box[3]):
                for px in range(box[0],box[2]):
                    source_pixel = region.getpixel((px,py))
                    if source_pixel[3] >= 128:
                        group = int(preserve_binding and binding_pixel(region,px,py))
                        values[group].append(palettes[group].getpixel((px,py)))
            area = (box[2]-box[0])*(box[3]-box[1])
            filled = sum(len(group) for group in values)
            # Preserve thin source branches as a whole native pixel; majority
            # alpha alone erased the wood crook while reducing its silhouette.
            if filled*(3 if preserve_binding else 2) >= area:
                # A short contrasting band must survive reduction even when its
                # two gold shades each lose individually to a shaft shade.
                group = 1 if preserve_binding and len(values[1])*4 >= filled else 0
                colour = Counter(values[group]).most_common(1)[0][0]
                canvas.putpixel((position[0]+x,position[1]+y), (*colour,255))
    return canvas


def verify_wands(result):
    """Check the rendered union, including an optional overlay, not just layers."""
    for relative, sprite in result.items():
        colour_limit = 8 if '/base_' in relative else 4
        pixels = list(sprite.getdata())
        if sprite.mode != 'RGBA' or sprite.size != (16,16):
            raise ValueError('Wrong wand texture format: '+relative)
        if any(pixel[3] not in (0,255) for pixel in pixels):
            raise ValueError('Soft alpha in wand texture: '+relative)
        if len({pixel[:3] for pixel in pixels if pixel[3]}) > colour_limit:
            raise ValueError('Noisy wand palette: '+relative)
        if '/base_' in relative and not any(gold_binding(pixel) for pixel in pixels if pixel[3]):
            raise ValueError('Missing reference gold binding: '+relative)
    for base in BASES:
        body = result['textures/item/wands/base_'+base+'.png']
        for tip in [None, *TIPS]:
            composed = body.copy()
            if tip:
                composed.alpha_composite(result['textures/item/wands/tip_'+tip+'.png'])
            name = base+('/'+tip if tip else '')
            occupied = {(x,y) for y in range(16) for x in range(16)
                        if composed.getpixel((x,y))[3]}
            if not occupied or any(x in (0,15) or y in (0,15) for x,y in occupied):
                raise ValueError('Wand must have a clear one-pixel border: '+name)
            pending = [next(iter(occupied))]
            connected = set(pending)
            while pending:
                x,y = pending.pop()
                for dx in (-1,0,1):
                    for dy in (-1,0,1):
                        neighbour = (x+dx,y+dy)
                        if neighbour in occupied and neighbour not in connected:
                            connected.add(neighbour)
                            pending.append(neighbour)
            if connected != occupied:
                raise ValueError('Detached wand pixels or disconnected tip: '+name)


def textures():
    result = {}
    with Image.open(ART / 'wand-components-v2.png') as atlas:
        for name, bounds in zip(BASES, BODY_BOUNDS):
            # Fit uniformly instead of squashing every crop into the same box.
            width = round(13*(bounds[2]-bounds[0])/(bounds[3]-bounds[1]))
            result['textures/item/wands/base_' + name + '.png'] = wand_sprite(atlas, bounds, (width,13), (13-width,2), 8, preserve_binding=True)
        for name, bounds in zip(TIPS, TIP_BOUNDS):
            result['textures/item/wands/tip_' + name + '.png'] = wand_sprite(atlas, bounds, (4,4), (10,1), 4)
    verify_wands(result)
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
