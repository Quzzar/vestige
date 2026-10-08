#!/usr/bin/env python3
"""Export the mage armor studies at one texel per model unit, with dye masks."""
from pathlib import Path
from collections import Counter
from PIL import Image
import argparse
import io

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'docs/art/magic-equipment-armor-v2/source'
TARGET = ROOT / 'src/main/resources/assets/vestige/textures/models/armor'
# Sample upper and lower studies separately so the belt lands at the waist.
REGIONS = {
    'wardweave': {
        'front': (123, 85, 552, 488), 'back': (656, 85, 1085, 488),
        'lower_front': (123, 488, 552, 983), 'lower_back': (656, 488, 1085, 1004),
        'arm': (1232, 193, 1415, 826),
    },
    'cinderweave': {
        'front': (136, 73, 565, 530), 'back': (681, 73, 1095, 550),
        'lower_front': (136, 530, 565, 999), 'lower_back': (681, 550, 1095, 999),
        'arm': (1218, 76, 1408, 780),
    },
}


def palette(pixel):
    r, g, b, _ = pixel
    if max(r, g, b) - min(r, g, b) >= 25:
        return tuple(min(240, round(c / 24) * 24) for c in (r, g, b)) + (255,)
    value = (r + g + b) / 3
    shades = (40, 64, 88) if value < 105 else (130, 160, 190, 220)
    shade = min(shades, key=lambda n: abs(n - value))
    return shade, shade, shade, 255


def crop(master, region, size):
    source = master.crop(region)
    w, h = source.size
    face = Image.new('RGBA', size)
    for y in range(size[1]):
        for x in range(size[0]):
            cell = source.crop((round(x*w/size[0]), round(y*h/size[1]),
                                round((x+1)*w/size[0]), round((y+1)*h/size[1])))
            shades = Counter(palette(p) for p in cell.get_flattened_data())
            accents = Counter({p: n for p, n in shades.items() if max(p[:3])-min(p[:3]) >= 25})
            selected = accents if sum(accents.values()) >= sum(shades.values()) * .2 else shades
            face.putpixel((x, y), selected.most_common(1)[0][0])
    return face


def cube(canvas, offset, front, back, side, top):
    """Exact vanilla cuboid UV net, preserving physical dimensions on every face."""
    x, y = offset
    width, height = front.size
    depth = side.width
    assert back.size == front.size and side.height == height
    assert top.size == (width, depth)
    canvas.paste(side, (x, y+depth))
    canvas.paste(front, (x+depth, y+depth))
    canvas.paste(side.transpose(Image.Transpose.FLIP_LEFT_RIGHT), (x+depth+width, y+depth))
    canvas.paste(back, (x+2*depth+width, y+depth))
    canvas.paste(top, (x+depth, y))
    canvas.paste(top, (x+depth+width, y))


def repeat_edge(face, column, width):
    return face.crop((column, 0, column+1, face.height)).resize((width, face.height), Image.Resampling.NEAREST)


def faces(name):
    master = Image.open(SOURCE / f'{name}-generated.png').convert('RGBA')
    return {key: crop(master, region, (4, 12) if key == 'arm' else (8, 8) if key.startswith('lower') else (8, 12))
            for key, region in REGIONS[name].items()}


def export(name):
    f = faces(name)
    front, back, arm = f['front'], f['back'], f['arm']
    canvas = Image.new('RGBA', (64, 64))
    cube(canvas, (16, 16), front, back, repeat_edge(front, 0, 4), front.crop((0, 0, 8, 4)))
    cube(canvas, (40, 16), arm, arm, arm, arm.crop((0, 0, 4, 4)))
    # Narrow sleeves need their own complete UV net, not a cropped wide net.
    narrow = arm.crop((0, 0, 3, 12))
    cube(canvas, (40, 48), narrow, narrow, arm, narrow.crop((0, 0, 3, 4)))
    for start, offset in ((0, (0, 32)), (4, (18, 32))):
        lower_front = f['lower_front'].crop((start, 0, start+4, 8))
        lower_back = f['lower_back'].crop((4-start, 0, 8-start, 8))
        side = repeat_edge(f['lower_front'], 0 if start == 0 else 7, 5)
        top = Image.new('RGBA', (4, 5), lower_front.getpixel((0, 0)))
        cube(canvas, offset, lower_front, lower_back, side, top)
    for width, shoulder_u, cuff_u in ((4, 0, 0), (3, 16, 16)):
        shoulder = arm.crop((0, 0, width, 3))
        cuff = arm.crop((0, 9, width, 12))
        side_top = arm.crop((0, 0, 4, 3))
        side_cuff = arm.crop((0, 9, 4, 12))
        top = arm.crop((0, 0, width, 4))
        cube(canvas, (shoulder_u, 0), shoulder, shoulder, side_top, top)
        cube(canvas, (cuff_u, 48), cuff, cuff, side_cuff, top)
    cube(canvas, (32, 0), front.crop((0, 0, 8, 2)), back.crop((0, 0, 8, 2)),
         repeat_edge(front.crop((0, 0, 8, 2)), 0, 4), front.crop((0, 0, 8, 4)))
    fabric, trim = Image.new('RGBA', canvas.size), Image.new('RGBA', canvas.size)
    for y in range(64):
        for x in range(64):
            r, g, b, a = canvas.getpixel((x, y))
            if a:
                fixed = len({r, g, b}) > 1 or max(r, g, b) < 105
                (trim if fixed else fabric).putpixel((x, y), (r, g, b, 255))
    return fabric, trim


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for name in REGIONS:
        for suffix, image in zip(('', '_overlay'), export(name)):
            path = TARGET / f'{name}_layer_1{suffix}.png'
            data = io.BytesIO()
            image.save(data, format='PNG')
            if args.check:
                if not path.exists() or path.read_bytes() != data.getvalue():
                    raise SystemExit(f'Drift: {path}')
            else:
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(data.getvalue())


if __name__ == '__main__':
    main()
