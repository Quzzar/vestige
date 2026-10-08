#!/usr/bin/env python3
"""Faithful native-resolution exports of imagegen robe masters; split dyeable fabric from fixed trim."""
from pathlib import Path
import argparse, io
from PIL import Image
ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'docs/art/magic-equipment-native/sources'
TARGET = ROOT / 'src/main/resources/assets/vestige/textures/item'

def export(name):
    master = Image.open(SOURCE / f'{name}-inventory-generated.png').convert('RGBA')
    mask = master.getchannel('A').point(lambda a: 255 if a >= 230 else 0)
    bounds = mask.getbbox()
    sprite = master.crop(bounds)
    sprite.thumbnail((14, 14), Image.Resampling.NEAREST)
    native = Image.new('RGBA', (16, 16))
    native.paste(sprite, ((16-sprite.width)//2, (16-sprite.height)//2))
    fabric, trim = Image.new('RGBA',(16,16)), Image.new('RGBA',(16,16))
    for y in range(16):
        for x in range(16):
            r,g,b,a = native.getpixel((x,y))
            if a < 230: continue
            colored = max(r,g,b) - min(r,g,b) >= 25
            # Neutral dark belts in the original Ward master remain iron/dark-gray, independent of dye.
            fixed = colored or name == 'wardweave' and 4 <= x <= 11 and 8 <= y <= 10 and max(r,g,b) < 105
            if fixed: trim.putpixel((x,y),(r,g,b,255))
            else:
                shade = min((55,95,135,175,215,245), key=lambda n: abs(n-(r+g+b)/3))
                fabric.putpixel((x,y),(shade,shade,shade,255))
    return fabric,trim

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    for name in ('wardweave','cinderweave'):
        for suffix,image in zip(('', '_overlay'),export(name)):
            target=TARGET/f'{name}_robes{suffix}.png';data=io.BytesIO();image.save(data,format='PNG')
            if args.check:
                if not target.exists() or target.read_bytes()!=data.getvalue(): raise SystemExit(f'Drift: {target}')
            else: target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data.getvalue())
if __name__=='__main__':main()
