#!/usr/bin/env python3
"""Reproduce/check the native hourglass export from its retained generated source."""
import argparse
import hashlib
import json
from io import BytesIO
from pathlib import Path
from PIL import Image
from collections import Counter
from author_wand_models import verify_connected

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'docs/art/kairotic-hourglass-v3'
TARGET=ROOT/'src/main/resources/assets/vestige/textures/item/kairotic_hourglass.png'

def material(pixel):
    r,g,b,_=pixel
    if r>b*1.25 and r>g*1.1:return 0  # brass
    if max(r,g,b)<170 and g>r*1.25 and b>r*1.2:return 2  # echo sand
    return 1  # glass


def export(source,bounds):
    """Preserve three material ramps while flattening the generated coarse cells.

    Every occupied pixel comes from its source cell's dominant material/color;
    there are no painted corrections or geometric decorations in the exporter.
    """
    region=source.crop(bounds).convert('RGBA')
    palettes=[]
    for group,budget in enumerate((3,3,2)):
        values=[p[:3] for p in region.getdata() if p[3]>=128 and material(p)==group]
        sample=Image.new('RGB',(len(values),1));sample.putdata(values)
        palette=sample.quantize(colors=budget,method=Image.Quantize.MEDIANCUT,dither=Image.Dither.NONE)
        palettes.append(region.convert('RGB').quantize(palette=palette,dither=Image.Dither.NONE).convert('RGB'))
    sprite=Image.new('RGBA',(16,16))
    for y in range(14):
        for x in range(8):
            box=(round(x*region.width/8),round(y*region.height/14),round((x+1)*region.width/8),round((y+1)*region.height/14))
            groups=[[] for _ in palettes]
            for py in range(box[1],box[3]):
                for px in range(box[0],box[2]):
                    value=region.getpixel((px,py))
                    if value[3]>=128:
                        group=material(value);groups[group].append(palettes[group].getpixel((px,py)))
            if sum(map(len,groups))*5 >= (box[2]-box[0])*(box[3]-box[1])*2:
                group=max(range(3),key=lambda index:len(groups[index]))
                sprite.putpixel((4+x,1+y),(*Counter(groups[group]).most_common(1)[0][0],255))
    return sprite


def main():
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    source=Image.open(ART/'generated-source.png').convert('RGBA')
    bounds=source.getchannel('A').point(lambda alpha:255 if alpha>=128 else 0).getbbox()
    sprite=export(source,bounds)
    verify_connected(sprite,'Kairotic Hourglass')
    encoded=BytesIO();sprite.save(encoded,format='PNG')
    approval_path=ART/'approval.json'
    if approval_path.exists():
        approval=json.loads(approval_path.read_text())
        model=ROOT/'src/main/resources/assets/vestige/models/item/kairotic_hourglass.json'
        for expected,actual in (
            (approval['textureSha256'],hashlib.sha256(encoded.getvalue()).hexdigest()),
            (approval['modelSha256'],hashlib.sha256(model.read_bytes()).hexdigest()),
            (approval['sourceSha256'],hashlib.sha256((ART/'generated-source.png').read_bytes()).hexdigest())):
            if expected!=actual:raise SystemExit('Approved Hourglass artwork drift; author a new revision')
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
        'position':[4,1],'palette_limit':8,'alpha':'hard','method':'separate brass/glass/sand 3/3/2 shade ramps; dominant material and shade per logical cell; no dithering; 40% coverage',
        'production_sha256':hashlib.sha256(TARGET.read_bytes()).hexdigest(),
        'art_status':'Owner-approved; see approval.json' if approval_path.exists() else 'Native review pending; owner approval separate'},indent=2)+'\n')

if __name__=='__main__':main()
