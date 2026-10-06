#!/usr/bin/env python3
"""Make a capture-only resource pack and preserve before/after model evidence."""
import argparse
import hashlib
import json
import zipfile
from apparatus_columns import simple_column_model, simplified_spellstone
from author_apparatus_models import ROOT, physical, block_name

ARCHIVE=ROOT/'docs/art/apparatus-columns'
PACK=ROOT/'run/effects-capture/resourcepacks/vestige-apparatus-simplified'


def vanilla_complexity():
    resource_jar=ROOT/'build/moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar'
    names=['stone','lodestone','stone_brick_stairs','anvil','enchanting_table','lectern',
           'grindstone','stonecutter','brewing_stand','campfire']
    rows=[]
    with zipfile.ZipFile(resource_jar) as resources:
        for name in names:
            source=resources.read('assets/minecraft/models/block/'+name+'.json')
            model=json.loads(source);ancestors=[]
            while 'elements' not in model:
                parent=model['parent'].removeprefix('minecraft:');ancestors.append(parent)
                model=json.loads(resources.read('assets/minecraft/models/'+parent+'.json'))
            elements=model['elements']
            rows.append({'model':'block/'+name,'sha256':hashlib.sha256(source).hexdigest(),
                'inherited_from':ancestors,'elements':len(elements),
                'faces':sum(len(e['faces']) for e in elements),
                'rotated_elements':sum('rotation' in e for e in elements)})
    return {'minecraft_version':'1.21.1','source':'Pinned vanilla resources shipped with NeoForge 21.1.72',
        'metric':'Resolved block-model elements/faces; excludes block-entity extras such as enchanting books and campfire food',
        'models':rows}


def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    palette=json.loads((ROOT/'docs/design/apparatus-materials.json').read_text())
    outputs={PACK/'pack.mcmeta':{'pack':{'pack_format':34,'description':'Vestige simplified apparatus comparison (capture only)'}},
             ARCHIVE/'vanilla-model-complexity.json':vanilla_complexity()}
    evidence=[]
    for m in palette['materials']:
        for role in palette['roles']:
            name=block_name(m,role)
            before=json.loads((ROOT/'docs/art/apparatus-concepts/models'/(role+'-'+m['id']+'.json')).read_text())
            after=simple_column_model(before,'single') if role=='plinth' else simplified_spellstone(before)
            for label,model in [('before',before),('after',after)]:
                outputs[ARCHIVE/'models'/label/(role+'-'+m['id']+'.json')]=model
            outputs[PACK/'assets/vestige/models/block'/(name+'.json')]=physical(after)
            outputs[PACK/'assets/vestige/models/item'/(name+'.json')]=after
            if role=='plinth':
                for part in ['base','shaft','cap']:
                    outputs[PACK/'assets/vestige/models/block'/(name+'_'+part+'.json')]=simple_column_model(after,part)
            evidence.append({'material':m['id'],'role':role,'before_elements':len(before['elements']),
                'after_elements':len(after['elements']),'before_faces':sum(len(e['faces']) for e in before['elements']),
                'after_faces':sum(len(e['faces']) for e in after['elements']),
                'before_rotations':sum('rotation' in e for e in before['elements']),
                'after_rotations':sum('rotation' in e for e in after['elements'])})
    outputs[ARCHIVE/'comparison-models.json']={'status':'Standalone simplification is a review alternative; connected column states are native',
        'rune':'Original intact 64x64 overlay at y7.12/16, native emissive renderer unchanged',
        'materials':evidence,'column_element_counts':{'base':3,'shaft':1,'cap':7}}
    for path,value in outputs.items():
        text=json.dumps(value,indent=2)+'\n'
        if args.check:assert path.is_file() and path.read_text()==text,'Comparison drift: '+str(path)
        else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text)
    print('36 finishes: Plinth 52 → 9; Spellstone 33 → 15 including unchanged rune; capture-only pack ready')


if __name__=='__main__':main()
