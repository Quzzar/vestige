#!/usr/bin/env python3
"""Export the selected cyan diamond study to Minecraft's 16px item grid."""
from io import BytesIO
from pathlib import Path
import argparse
import hashlib
import json

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / 'docs/art/dissentient-diamond-v10'
REFERENCE = ROOT / 'docs/art/native-item-textures/vanilla/diamond.png'
SOURCE = ART / 'dissentient-diamond-16.png'
TARGET = ROOT / 'src/main/resources/assets/vestige/textures/item/dissentient_diamond.png'
MODEL = ROOT / 'src/main/resources/assets/vestige/models/item/dissentient_diamond.json'
MODEL_TEXT = '{\n  "parent": "minecraft:item/generated",\n  "textures": {\n    "layer0": "vestige:item/dissentient_diamond"\n  }\n}\n'


def export_sprite():
    source = Image.open(SOURCE).convert('RGBA')
    assert source.size == (16, 16)
    return source


def validate(sprite):
    colors = {rgba for rgba in sprite.get_flattened_data() if rgba[3]}
    assert sprite.size == (16, 16) and sprite.mode == 'RGBA'
    assert {rgba[3] for rgba in sprite.get_flattened_data()} == {0, 255}
    assert len(colors) <= 12
    occupied = {(x, y) for y in range(16) for x in range(16) if sprite.getpixel((x, y))[3]}
    assert occupied and all(0 < x < 15 and 0 < y < 15 for x, y in occupied)
    # Fracture gaps are authored in the selected study; retain them unchanged.
    return colors, occupied


def png_bytes(sprite):
    buffer = BytesIO()
    sprite.save(buffer, format='PNG')
    return buffer.getvalue()


def preview(sprite):
    board = Image.new('RGBA', (512, 256), '#a0a0a0')
    board.alpha_composite(sprite.resize((256, 256), Image.Resampling.NEAREST))
    dark = Image.new('RGBA', (256, 256), '#202028')
    dark.alpha_composite(sprite.resize((256, 256), Image.Resampling.NEAREST))
    board.alpha_composite(dark, (256, 0))
    board.save(ART / 'production-preview.png')
    comparison = Image.new('RGBA', (448, 188), '#8b8b8b')
    draw = ImageDraw.Draw(comparison)
    references = [ROOT / 'docs/art/native-item-textures/vanilla/diamond.png', None,
                  ROOT / 'docs/art/native-item-textures/vanilla/prismarine_shard.png']
    for i, (path, label) in enumerate(zip(references, ('Diamond', 'Dissentient Diamond', 'Prismarine Shard'))):
        item = Image.open(path).convert('RGBA') if path else sprite
        x = 16+i*144
        comparison.alpha_composite(item.resize((128, 128), Image.Resampling.NEAREST), (x, 18))
        draw.text((x, 151), label, fill='white')
        comparison.alpha_composite(item, (x+56, 168))
    comparison.save(ART / 'inventory-reference-board.png')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    sprite = export_sprite()
    colors, occupied = validate(sprite)
    data = png_bytes(sprite)
    if args.check:
        assert TARGET.read_bytes() == data, 'Dissentient Diamond texture export drift'
        assert MODEL.read_text() == MODEL_TEXT, 'Dissentient Diamond item model drift'
        print('Dissentient Diamond 16x16 texture and ordinary generated-item model match')
    else:
        TARGET.write_bytes(data)
        MODEL.write_text(MODEL_TEXT)
        preview(sprite)
        (ART / 'production-export.json').write_text(json.dumps({
            'source': SOURCE.name, 'generator': 'Built-in image_gen blue-fissure edit; native pixel export with unchanged v9 texels retained',
            'reference': str(REFERENCE.relative_to(ROOT)),
            'referenceSha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(),
            'sourceSha256': hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
            'texture': str(TARGET.relative_to(ROOT)), 'model': str(MODEL.relative_to(ROOT)),
            'size': [16, 16], 'bounds': list(sprite.getbbox()),
            'palette': sorted('#%02x%02x%02x' % color[:3] for color in colors),
            'opaquePixels': len(occupied), 'hardAlpha': True, 'transparentMargin': True,
            'exactNativeExport': True,
            'textureSha256': hashlib.sha256(data).hexdigest(),
            'modelSha256': hashlib.sha256(MODEL_TEXT.encode()).hexdigest(),
            'selectedForInventoryReview': True,
            'ownerAccepted': False, 'nativeReviewed': False,
            'note': 'V10 changes only thirteen near-black crack pixels to dark petrol/navy blues; all other v9 pixels and alpha are unchanged.'
        }, indent=2)+'\n')
