#!/usr/bin/env python3
"""Author the middle-ground review pack and verify continuous column texture mapping."""
import argparse
import json
from apparatus_columns import middle_column_model as column_model, middle_spellstone, simple_column_model
from author_apparatus_models import ROOT, physical, block_name, bounds

ARCHIVE=ROOT/'docs/art/apparatus-middle-ground'
PACK=ROOT/'run/effects-capture/resourcepacks/vestige-apparatus-middle-ground'


def validate(model):
    for e in model['elements']:
        assert all(a<=b for a,b in zip(e['from'],e['to']))
        assert all(-1e-6<=v<=16+1e-6 for v in bounds(e)),e
        if 'rotation' in e:assert e['rotation']['angle'] in (-45,-22.5,0,22.5,45)
        for f in e['faces'].values():
            assert f['texture'][1:] in model['textures']
            assert len(f['uv'])==4 and all(0<=v<=16 for v in f['uv'])


def seam_faces(model, y):
    faces={}
    for e in model['elements']:
        if y not in (e['from'][1],e['to'][1]):continue
        for side in ('north','south','east','west'):
            f=e['faces'][side];uv=f['uv']
            scale=(uv[3]-uv[1])/(e['to'][1]-e['from'][1])
            border=uv[1]-(16-e['to'][1])*scale
            assert abs(border-round(border))<1e-9 and round(border) in (0,1)
            assert abs(scale-(16-2*border)/16)<1e-9
            key=(side,e['from'][0],e['from'][2],e['to'][0],e['to'][2])
            faces[key]=(f['texture'],uv[0],uv[2],round(border),round(scale,10))
    assert len(faces)==20,'Core and four continuous corner trims must reach the seam'
    return faces


def validate_column_seams(original):
    parts={part:column_model(original,part) for part in ('base','shaft','cap')}
    for model in parts.values():validate(model)
    base_top=seam_faces(parts['base'],16)
    shaft_bottom=seam_faces(parts['shaft'],0)
    shaft_top=seam_faces(parts['shaft'],16)
    cap_bottom=seam_faces(parts['cap'],0)
    assert base_top==shaft_bottom==shaft_top==cap_bottom
    return {'vertical_scale':'fixed V phase; sixteen rows for masonry/pillars, fourteen interior rows for bordered polished tiles',
            'matched_face_sections_per_join':len(base_top),
            'joins':['base → shaft','shaft → shaft','shaft → cap','base → cap'],
            'shaft_texture':parts['shaft']['textures']['column'],
            'corner_texture':parts['shaft']['textures']['trim'],
            'column_elements':{part:len(model['elements']) for part,model in parts.items()}}


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    palette=json.loads((ROOT/'docs/design/apparatus-materials.json').read_text())
    outputs={PACK/'pack.mcmeta':{'pack':{'pack_format':34,'description':'Vestige middle-ground apparatus comparison (capture only)'}}}
    evidence=[];seams=[]
    for m in palette['materials']:
        for role in palette['roles']:
            name=block_name(m,role)
            before=json.loads((ROOT/'docs/art/apparatus-concepts/models'/(role+'-'+m['id']+'.json')).read_text())
            after=column_model(before,'single') if role=='plinth' else middle_spellstone(before)
            validate(after)
            assert len(after['elements'])=={'plinth':24,'spellstone':25}[role]
            for label,model in [('before',before),('middle',after)]:
                outputs[ARCHIVE/'models'/label/(role+'-'+m['id']+'.json')]=model
            outputs[PACK/'assets/vestige/models/block'/(name+'.json')]=physical(after)
            outputs[PACK/'assets/vestige/models/item'/(name+'.json')]=after
            if role=='plinth':
                seams.append({'material':m['id'],**validate_column_seams(before)})
                for part in ('base','shaft','cap'):
                    model=column_model(after,part)
                    outputs[PACK/'assets/vestige/models/block'/(name+'_'+part+'.json')]=model
                    outputs[ARCHIVE/'models/column-middle'/(name+'_'+part+'.json')]=model
                    outputs[ARCHIVE/'models/column-before'/(name+'_'+part+'.json')]=simple_column_model(before,part)
            else:
                assert before['elements'][-1]==after['elements'][-1],'Preserve the intact purple rune'
            evidence.append({'material':m['id'],'role':role,'original_elements':len(before['elements']),
                'middle_elements':len(after['elements']),'middle_faces':sum(len(e['faces']) for e in after['elements']),
                'middle_rotated_elements':sum('rotation' in e for e in after['elements'])})
    outputs[ARCHIVE/'model-and-seam-audit.json']={'status':'Historical middle-ground candidate; superseded by detailed models with foot-only simplification',
        'models':evidence,'continuous_mapping':seams,
        'textures':'Unchanged vanilla 16×16 tiles; fixed-scale UVs, bordered polished tiles use interior rows; Quartz/Purpur use native pillar shafts',
        'spellstone':'Octagonal silhouette, original rune, four near-flush Diamond decals; no raster asset changes'}
    for path,value in outputs.items():
        text=json.dumps(value,indent=2)+'\n'
        if args.check:assert path.is_file() and path.read_text()==text,'Middle model drift: '+str(path)
        else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text)
    print('36 finishes: middle Plinth 24, Spellstone 25; base/shaft/cap 7/5/22; all column UV joins agree')


if __name__=='__main__':main()
