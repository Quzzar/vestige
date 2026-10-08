#!/usr/bin/env python3
"""Deterministic constrained 16x16 export of the generated diamond-blue recolor. Requires Pillow."""
from pathlib import Path
from collections import Counter
from statistics import median
from io import BytesIO
import argparse
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'docs/art/fluxed-flint-v2'
TARGET = ROOT / 'src/main/resources/assets/vestige/textures/item/fluxed_flint.png'


def sprite():
    source = Image.open(ART / 'source.png').convert('RGBA')
    bounds = source.getchannel('A').point(lambda a: 255 if a >= 128 else 0).getbbox()
    region = source.crop(bounds)
    original = Image.open(ART / 'previous-16.png').convert('RGBA')
    # The requested edit preserves the original logical cells and five stone shades.
    # Collapse generated shading/edge noise into one diamond shade per accent role.
    roles = ((100, 39, 159), (193, 118, 239), (244, 194, 251))
    diamond = Image.open(ART / 'vanilla-diamond.png').convert('RGBA')
    palette = sorted({p[:3] for p in diamond.getdata() if p[3] and p[1] > p[0] + 20 and p[2] > p[0] + 20})
    samples = {role: [] for role in roles}
    for y in range(13):
        for x in range(11):
            role = original.getpixel((x+2, y+1))[:3]
            if role in samples:
                pixel = region.getpixel((int((x+.5)*region.width/11), int((y+.5)*region.height/13)))
                samples[role].append(pixel[:3])
    exported = {}
    for role, pixels in samples.items():
        center = tuple(median(p[channel] for p in pixels) for channel in range(3))
        exported[role] = min(palette, key=lambda shade: sum((shade[i]-center[i])**2 for i in range(3)))
    assert len(set(exported.values())) == 3, 'Generated accent ramp collapsed'
    out = original.copy()
    for y in range(16):
        for x in range(16):
            pixel = original.getpixel((x,y))
            if pixel[3] and pixel[:3] in exported:
                out.putpixel((x,y), (*exported[pixel[:3]], 255))
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
