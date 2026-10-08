#!/usr/bin/env python3
"""Export the selected Feather Tabs study and generated native-UV worn faces."""
from pathlib import Path
from collections import Counter
from PIL import Image
import argparse
import io
import json

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'docs/art/wayfarer-native'
ASSETS = ROOT / 'src/main/resources/assets/vestige'


def shade(pixel):
    if pixel[3] < 230:
        return (0, 0, 0, 0)
    return tuple(min(255, round(c / 16) * 16) for c in pixel[:3]) + (255,)


def cells(source, size):
    target = Image.new('RGBA', size)
    w, h = source.size
    for y in range(size[1]):
        for x in range(size[0]):
            cell = source.crop((round(x*w/size[0]), round(y*h/size[1]),
                                round((x+1)*w/size[0]), round((y+1)*h/size[1])))
            colors = Counter(shade(p) for p in cell.get_flattened_data())
            opaque = Counter({p: n for p, n in colors.items() if p[3]})
            if sum(opaque.values()) >= cell.width * cell.height * .4:
                target.putpixel((x, y), opaque.most_common(1)[0][0])
    return target


def exports():
    source = Image.open(ROOT / 'docs/art/magic-equipment-v1/preview-resources/assets/vestige/textures/item/wayfarer_c.png').convert('RGBA')
    source = source.crop(source.getchannel('A').point(lambda a: 255 if a >= 230 else 0).getbbox())
    size = (14, round(source.height / source.width * 14))
    sprite = Image.new('RGBA', (16, 16))
    sprite.paste(cells(source, size), ((16-size[0])//2, (16-size[1])//2))
    master = Image.open(ART / 'source/worn-generated.png').convert('RGBA')
    atlas = Image.new('RGBA', (64, 32))
    # Native armor legs: four 4-wide faces, 12 high; only bottom seven rows are boots.
    for u, region in ((0, (849, 642, 1034, 868)), (4, (370, 642, 520, 868)),
                      (8, (668, 642, 814, 868)), (12, (520, 642, 668, 868))):
        atlas.paste(cells(master.crop(region), (4, 7)), (u, 25))
    atlas.paste(cells(master.crop((590, 444, 740, 592)), (4, 4)), (8, 16))
    model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'vestige:item/wayfarer_boots'}}
    return {
        ASSETS / 'textures/item/wayfarer_boots.png': sprite,
        ASSETS / 'textures/models/armor/wayfarer_layer_1.png': atlas,
        ASSETS / 'models/item/wayfarer_boots.json': json.dumps(model, indent=2) + '\n',
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    for path, value in exports().items():
        if isinstance(value, Image.Image):
            assert set(value.getchannel('A').get_flattened_data()) <= {0, 255}
            stream = io.BytesIO(); value.save(stream, format='PNG'); data = stream.getvalue()
        else:
            data = value.encode()
        if args.check:
            if not path.exists() or path.read_bytes() != data:
                raise SystemExit(f'Drift: {path}')
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_bytes(data)


if __name__ == '__main__':
    main()
