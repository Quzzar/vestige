#!/usr/bin/env python3
"""Reproduce/check the native hourglass export from its retained generated source."""
import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image
from author_wand_models import indexed_sprite, verify_connected

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'docs/art/kairotic-hourglass-v1'
TARGET=ROOT/'src/main/resources/assets/vestige/textures/item/kairotic_hourglass.png'

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    source=Image.open(ART/'generated-source.png').convert('RGBA')
    bounds=source.getchannel('A').point(lambda alpha:255 if alpha>=128 else 0).getbbox()
    sprite=indexed_sprite(source,bounds,(8,14),(4,1),12,coverage=(2,5))
    verify_connected(sprite,'Kairotic Hourglass')
    if args.check:
        with Image.open(TARGET) as actual:
            if actual.mode!='RGBA' or actual.size!=(16,16) or actual.tobytes()!=sprite.tobytes():
                raise SystemExit('Kairotic Hourglass export drift')
        if set(sprite.getchannel('A').getdata())!={0,255}:raise SystemExit('Hourglass alpha must be hard')
        print('Kairotic Hourglass: exact 16×16 RGBA export, padding, connectivity and palette checked')
        return
    sprite.save(TARGET);sprite.save(ART/'kairotic_hourglass.png')
    board=Image.new('RGBA',(768,384),(210,210,210,255));large=sprite.resize((384,384),Image.Resampling.NEAREST)
    board.alpha_composite(large);board.paste((35,35,35,255),(384,0,768,384));board.alpha_composite(large,(384,0));board.save(ART/'native-pixels-enlarged.png')
    (ART/'export.json').write_text(json.dumps({'source':'generated-source.png','source_crop':bounds,'grid':[16,16],'occupied_grid':[8,14],
        'position':[4,1],'palette_limit':12,'alpha':'hard','method':'opaque-source median-cut palette; dominant indexed color per cell; no dithering; 40% coverage',
        'production_sha256':hashlib.sha256(TARGET.read_bytes()).hexdigest(),'art_status':'Native review pending; owner approval separate'},indent=2)+'\n')

if __name__=='__main__':main()
