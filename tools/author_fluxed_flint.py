#!/usr/bin/env python3
"""Deterministic 16x16 export from the generated Fluxed Flint study. Requires Pillow."""
from pathlib import Path
from collections import Counter
from io import BytesIO
import argparse
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'docs/art/fluxed-flint-v1'
TARGET = ROOT / 'src/main/resources/assets/vestige/textures/item/fluxed_flint.png'


def sprite():
    source = Image.open(ART / 'source.png').convert('RGBA')
    bounds = source.getchannel('A').point(lambda a: 255 if a >= 128 else 0).getbbox()
    region = source.crop(bounds)
    # Reserve the source's purple fracture ramp during nondithered palette reduction.
    def accent(rgb):
        return rgb[2] > rgb[1]*1.15 and rgb[0] > rgb[1]*1.1
    groups = [[], []]
    for rgb in (region.getpixel((x,y)) for y in range(region.height) for x in range(region.width)):
        if rgb[3] >= 128:
            groups[int(accent(rgb))].append(rgb[:3])
    palettes = []
    for colors, budget in zip(groups, (5, 3)):
        sample = Image.new('RGB', (len(colors), 1)); sample.putdata(colors)
        palette = sample.quantize(colors=budget, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
        palettes.append(region.convert('RGB').quantize(palette=palette, dither=Image.Dither.NONE).convert('RGB'))
    out = Image.new('RGBA', (16, 16))
    for y in range(13):
        for x in range(11):
            box = (round(x*region.width/11), round(y*region.height/13), round((x+1)*region.width/11), round((y+1)*region.height/13))
            values = [[], []]
            for py in range(box[1], box[3]):
                for px in range(box[0], box[2]):
                    rgb = region.getpixel((px, py))
                    if rgb[3] >= 128:
                        group = int(accent(rgb)); values[group].append(palettes[group].getpixel((px, py)))
            count = sum(map(len, values))
            if count*2 >= (box[2]-box[0])*(box[3]-box[1]):
                group = int(len(values[1])*4 >= count)
                out.putpixel((x+2, y+1), (*Counter(values[group]).most_common(1)[0][0], 255))
    return out


if __name__ == '__main__':
    parser = argparse.ArgumentParser(); parser.add_argument('--check', action='store_true'); args = parser.parse_args()
    result = sprite()
    pixels = [result.getpixel((x,y)) for y in range(16) for x in range(16)]
    assert {p[3] for p in pixels} == {0, 255}, 'Sprite needs hard alpha'
    assert len({p for p in pixels if p[3]}) <= 8, 'Palette exceeds eight shades'
    occupied = {(x,y) for y in range(16) for x in range(16) if result.getpixel((x,y))[3]}
    assert occupied and all(0 < x < 15 and 0 < y < 15 for x,y in occupied), 'Missing transparent margin'
    connected = {next(iter(occupied))}; pending = list(connected)
    while pending:
        x,y = pending.pop()
        for dx in (-1,0,1):
            for dy in (-1,0,1):
                point = x+dx,y+dy
                if point in occupied and point not in connected:
                    connected.add(point); pending.append(point)
    assert occupied == connected, 'Disconnected sprite silhouette'
    data = BytesIO(); result.save(data, format='PNG')
    if args.check:
        if not TARGET.exists() or TARGET.read_bytes() != data.getvalue():
            raise SystemExit('Fluxed Flint sprite drift; regenerate with tools/author_fluxed_flint.py')
        print('Fluxed Flint 16x16 export matches')
    else:
        TARGET.write_bytes(data.getvalue())
        board = Image.new('RGBA', (512, 256), '#777777'); board.alpha_composite(result.resize((256, 256), Image.Resampling.NEAREST))
        dark = Image.new('RGBA', (256, 256), '#202028'); dark.alpha_composite(result.resize((256, 256), Image.Resampling.NEAREST)); board.alpha_composite(dark, (256, 0))
        board.save(ART / 'export-preview.png')
