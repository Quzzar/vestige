#!/usr/bin/env python3
"""Three restrained additions to the owner's preferred Sealstone silhouette."""
import argparse
import copy
import json
import math
from apparatus_columns import box
from author_apparatus_models import ROOT, physical
from author_apparatus_middle import validate
from author_spellstone_designs import designs

ARCHIVE=ROOT/'docs/art/sealstone-refinements'
PACK=ROOT/'run/effects-capture/resourcepacks/vestige-sealstone-refinements'


def layer(base,low,high,inset=0):
    elements=copy.deepcopy(base['elements'][:-1]);scale=(14-2*inset)/14
    for e in elements:
        for point in ('from','to'):
            for axis in (0,2):e[point][axis]=8+(e[point][axis]-8)*scale
        e['from'][1]=low;e['to'][1]=high-(.002 if 'rotation' in e else 0)
        if 'rotation' in e:
            for axis in (0,2):e['rotation']['origin'][axis]=8+(e['rotation']['origin'][axis]-8)*scale
            e['rotation']['origin'][1]=(low+e['to'][1])/2
        e['faces']=box(e['from'],e['to'],'stone')['faces']
        e['faces']['up']['texture']='#trim'
    return elements


def crown():
    pieces=[box([5,7,1],[11,8,2],'trim'),box([5,7,14],[11,8,15],'trim'),
            box([1,7,5],[2,8,11],'trim'),box([14,7,5],[15,8,11],'trim')]
    near=3+math.sqrt(2)/4;far=16-near;half=2*math.sqrt(2)
    for x,z,angle in ((near,near,45),(far,near,-45),(far,far,45),(near,far,-45)):
        e=box([x-half,7,z-.5],[x+half,7.998,z+.5],'trim')
        e['rotation']={'origin':[x,7.499,z],'axis':'y','angle':angle,'rescale':False}
        pieces.append(e)
    return pieces


def engravings():
    result=[]
    for side,a,b in (
        ('north',[5.5,1,.998],[10.5,6,.998]),
        ('south',[5.5,1,15.002],[10.5,6,15.002]),
        ('west',[.998,1,5.5],[.998,6,10.5]),
        ('east',[15.002,1,5.5],[15.002,6,10.5]),
    ):
        e=box(a,b,'carving');e['faces']={side:{'texture':'#carving','uv':[0,0,16,16]}}
        result.append(e)
    return result


def refinements():
    base=designs()['seal'][2];rune=base['elements'][-1]
    candidates={
        'rimmed':('Rimmed Sealstone','spellstone',layer(base,0,7)+crown()),
        'engraved':('Engraved Sealstone','tuff_spellstone',layer(base,0,7)+engravings()),
        'banded':('Banded Sealstone','quartz_spellstone',layer(base,0,2.75)+layer(base,2.75,4,.75)+layer(base,4,7)),
    }
    result={}
    for key,(name,carrier,elements) in candidates.items():
        model=copy.deepcopy(base);model['elements']=elements+[copy.deepcopy(rune)]
        validate(model);assert model['elements'][-1]==rune
        assert len(model['elements'])<=22
        result[key]=(name,carrier,model)
    return result


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    outputs={PACK/'pack.mcmeta':{'pack':{'pack_format':34,'description':'Vestige Sealstone refinements (capture only)'}}}
    ledger=[]
    for key,(name,carrier,model) in refinements().items():
        outputs[ARCHIVE/'models'/(key+'.json')]=model
        outputs[PACK/'assets/vestige/models/block'/(carrier+'.json')]=physical(model)
        outputs[PACK/'assets/vestige/models/item'/(carrier+'.json')]=model
        ledger.append({'design':key,'name':name,'capture_carrier':'vestige:'+carrier,
            'solid_cuboids':sum(all(a<b for a,b in zip(e['from'],e['to'])) for e in physical(model)['elements']),
            'flat_engraving_faces':sum(not all(a<b for a,b in zip(e['from'],e['to'])) for e in physical(model)['elements']),
            'inventory_elements':len(model['elements']),'scroll_surface':7,'rune_height':7.12})
    outputs[ARCHIVE/'designs.json']={'status':'Sealstone direction preferred; owner requested more character',
        'brief':'Retain the clean octagonal silhouette; explore one controlled addition per variant rather than restore the rejected moldings and gems.',
        'signature':{'rimmed':'A narrow eight-sided stone crown framing the rune.',
                     'engraved':'Four inset-looking carved medallions on the long sides; existing cube count unchanged.',
                     'banded':'One shallow recessed belt between two octagonal masses, with no separate footing.'},
        'shared':'Same vanilla Stone Brick finish, Polished Andesite top, intact purple rune, flat scroll bed at y7/16.',
        'scope':'Capture-only options; production models, collision, recipes and installed Prism jar unchanged.',
        'designs':ledger}
    for path,value in outputs.items():
        text=json.dumps(value,indent=2)+'\n'
        if args.check:assert path.is_file() and path.read_text()==text,'Sealstone refinement drift: '+str(path)
        else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text)
    print('Sealstone refinements: narrow rim, four engravings, one recessed belt; capture-only models ready.')


if __name__=='__main__':main()
