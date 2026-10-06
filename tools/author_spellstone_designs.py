#!/usr/bin/env python3
"""Author three capture-only Spellstone options; production assets stay untouched."""
import argparse
import copy
import json
from apparatus_columns import box, detailed_spellstone
from author_apparatus_models import ROOT, review_model, physical
from author_apparatus_middle import validate

ARCHIVE=ROOT/'docs/art/spellstone-redesign-options'
PACK=ROOT/'run/effects-capture/resourcepacks/vestige-spellstone-designs'


def rim():
    return [box([1,7,1],[15,8,2],'stone'),
            box([1,7,14],[15,8,15],'stone'),
            box([1,7,2],[2,8,14],'stone'),
            box([14,7,2],[15,8,14],'stone')]


def designs():
    original=review_model('stone_bricks','spellstone')
    rune=copy.deepcopy(original['elements'][-1])
    tablet=box([1,0,1],[15,7,15],'stone')
    for face in ('north','south','east','west'):
        tablet['faces'][face]={'texture':'#carving','uv':[0,0,16,16]}
    altar=[box([2,0,3],[5,5,13],'stone'),box([11,0,3],[14,5,13],'stone'),
           box([1,5,1],[15,7,15],'stone')]
    # A single-height octagonal mass, without the old foot, shoulders or crown settings.
    seal=copy.deepcopy(detailed_spellstone(original)['elements'][:7])
    for e in seal:
        # Hide coplanar overlaps beneath the unrotated top; the 0.002-unit
        # offset is imperceptible, while preventing texture flicker at joins.
        e['to'][1]=6.998 if 'rotation' in e else 7
        if 'rotation' in e:e['rotation']['origin'][1]=e['to'][1]/2
        e['faces']=box(e['from'],e['to'],'stone')['faces']
    options={
        'tablet':('Runic Tablet','spellstone',[tablet]+rim()),
        'altar':('Twin-Pier Altar','tuff_spellstone',altar+rim()),
        'seal':('Sealstone','quartz_spellstone',seal),
    }
    result={}
    for key,(name,carrier,elements) in options.items():
        model=copy.deepcopy(original)
        model['textures']={k:model['textures'][k] for k in ('stone','carving','trim','particle','runes')}
        for e in elements:e['faces']['up']['texture']='#trim'
        model['elements']=elements+[copy.deepcopy(rune)]
        validate(model)
        assert len(model['elements'])<=8
        assert model['elements'][-1]==rune
        result[key]=(name,carrier,model)
    return result


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    outputs={PACK/'pack.mcmeta':{'pack':{'pack_format':34,'description':'Vestige Spellstone design options (capture only)'}}}
    ledger=[]
    for key,(name,carrier,model) in designs().items():
        outputs[ARCHIVE/'models'/(key+'.json')]=model
        outputs[PACK/'assets/vestige/models/block'/(carrier+'.json')]=physical(model)
        outputs[PACK/'assets/vestige/models/item'/(carrier+'.json')]=model
        ledger.append({'design':key,'name':name,'capture_carrier':'vestige:'+carrier,
            'finish':'Stone Bricks for all options','solid_cuboids':len(physical(model)['elements']),
            'inventory_elements':len(model['elements']),'rune_height':7.12,'scroll_surface':7,
            'role':'Capture-only art candidate; collision/production integration awaits owner choice.'})
    outputs[ARCHIVE/'designs.json']={'status':'Three alternatives requested after owner rejected the detailed Spellstone',
        'rejected':'The many-piece altar, square footing and subsequent clipped-foot edit still looked bad in game. These start from new silhouettes.',
        'constraints':'Half-block scale, broad flat scroll bed at y7/16, original intact rune overlay, vanilla Stone Brick/carved sides and a quiet Polished Andesite top. No tiers, bevels, miniature gems or extra animation.',
        'scope':'Three existing registered variants act as carriers in one isolated resource pack; all preview textures use Stone Bricks. Production resources and Prism jar are untouched.',
        'designs':ledger}
    for path,value in outputs.items():
        text=json.dumps(value,indent=2)+'\n'
        if args.check:assert path.is_file() and path.read_text()==text,'Spellstone candidate drift: '+str(path)
        else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text)
    print('Three capture-only designs: Tablet 5 cuboids, Altar 7, Sealstone 7; intact rune, same Stone Brick finish.')


if __name__=='__main__':main()
