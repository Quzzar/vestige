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
BODY_BOUNDS = [(45,90,287,381), (360,90,592,381), (661,91,902,381), (986,90,1209,381),
               (61,439,288,727), (360,439,592,727), (661,439,902,727)]
TIP_BOUNDS = [(114,828,206,941), (427,828,519,941), (736,828,828,941), (1047,828,1157,942),
              (114,1061,205,1161), (426,1061,516,1161), (736,1061,817,1161), (1060,1061,1151,1161)]
THREAD_BOUNDS = [(79,190,357,528), (485,190,795,528), (913,190,1247,528),
                 (1365,190,1681,516), (1795,214,2126,512)]


def gold_binding(rgb):
    hue, saturation, value = colorsys.rgb_to_hsv(*(channel/255 for channel in rgb[:3]))
    return 35 <= hue*360 <= 65 and saturation >= .45 and value >= .35


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
        palette = sample.quantize(colors=budget, method=Image.Quantize.MEDIANCUT,
                                  dither=Image.Dither.NONE)
        palettes.append(region.convert('RGB').quantize(palette=palette, dither=Image.Dither.NONE).convert('RGB'))
    return palettes


def indexed_sprite(source, bounds, size, position, colours, preserve_binding=False, coverage=(1,2)):
    """Indexed, nondithered export: one dominant flat colour per logical pixel.

    Generated source cells have small raster variations even inside their flat
    colour patches. Index the opaque source colours, then take each logical
    cell's majority. Point sampling those variations produced noisy shading.
    Geometry comes entirely from the generated artwork, without painting pixels.
    """
    region = source.crop(bounds).convert('RGBA')
    palettes = indexed_colours(region, colours, preserve_binding)
    canvas = Image.new('RGBA', (16, 16))
    numerator, denominator = (2,5) if preserve_binding else coverage
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
            # Body coverage preserves the short branch; other source cells
            # use their configured coverage without expanding the contour.
            output_position = (position[0]+x,position[1]+y)
            if filled*denominator >= area*numerator:
                # A short contrasting band must survive reduction even when its
                # two gold shades each lose individually to a shaft shade.
                group = 1 if preserve_binding and len(values[1])*4 >= filled else 0
                colour = Counter(values[group]).most_common(1)[0][0]
                canvas.putpixel(output_position, (*colour,255))
    return canvas


def verify_connected(sprite, name):
    occupied = {(x,y) for y in range(16) for x in range(16) if sprite.getpixel((x,y))[3]}
    if not occupied or any(x in (0,15) or y in (0,15) for x,y in occupied):
        raise ValueError('Item must have a clear one-pixel border: '+name)
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
        raise ValueError('Detached item pixels or disconnected component: '+name)


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
            verify_connected(composed, name)


def verify_threads(result):
    for thread in THREADS:
        relative = 'textures/item/'+thread+'_thread.png'
        sprite = result[relative]
        if sprite.mode != 'RGBA' or sprite.size != (16,16):
            raise ValueError('Wrong thread texture format: '+relative)
        pixels = list(sprite.getdata())
        if any(pixel[3] not in (0,255) for pixel in pixels):
            raise ValueError('Soft alpha in thread texture: '+relative)
        if len({pixel[:3] for pixel in pixels if pixel[3]}) > 4:
            raise ValueError('Noisy thread palette: '+relative)
        tones = [.2126*p[0]+.7152*p[1]+.0722*p[2] for p in pixels if p[3]]
        if not tones or min(tones)>85 or max(tones)<220:
            raise ValueError('Thread must retain a dark shadow and pale strands: '+relative)
        verify_connected(sprite, thread)


def textures():
    result = {}
    with Image.open(ART / 'wand-components-v4.png') as atlas:
        for name, bounds in zip(BASES, BODY_BOUNDS):
            # Fit uniformly instead of squashing every crop into the same box.
            width = round(13*(bounds[2]-bounds[0])/(bounds[3]-bounds[1]))
            result['textures/item/wands/base_' + name + '.png'] = indexed_sprite(atlas, bounds, (width,13), (13-width,2), 8, preserve_binding=True)
        for name, bounds in zip(TIPS, TIP_BOUNDS):
            result['textures/item/wands/tip_' + name + '.png'] = indexed_sprite(atlas, bounds, (4,4), (10,1), 4)
    verify_wands(result)
    with Image.open(ART / 'magical-threads-v6.png') as atlas:
        for name, bounds in zip(THREADS, THREAD_BOUNDS):
            width, height = bounds[2]-bounds[0], bounds[3]-bounds[1]
            scale = 14/max(width,height)
            size = (round(width*scale), round(height*scale))
            result['textures/item/' + name + '_thread.png'] = indexed_sprite(atlas, bounds, size, ((16-size[0])//2,(16-size[1])//2), 4)
    verify_threads(result)
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
