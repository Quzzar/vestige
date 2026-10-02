#!/usr/bin/env python3
"""Export spell metadata to the actual-cast video library."""
import argparse,json,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(ROOT/'tools'))
from spell_title import spell_title
from convert_pathfinder_spells import NOTES as PF_NOTES
from convert_irons_spells import NOTES as IRON_NOTES
OUT=ROOT/'tools/effects-viewer/src/catalog.json'
def build():
    rows=[]
    for p in sorted((ROOT/'src/main/resources/data/vestige/runtime_spells').glob('*.json')):
        s=json.loads(p.read_text())
        notes=PF_NOTES.get(p.stem,IRON_NOTES.get(p.stem,('Native composed example.','')))
        rows.append({'id':'vestige:'+p.stem,'name':spell_title(s.get('source',{}).get('name',p.stem.replace('_',' ').title())),'rarity':s['rarity'],
                     'origin':'Pathfinder' if p.stem.startswith('pf2_') else 'Iron' if 'source' in s else 'Native',
                     'description':notes[0],'notes':notes[1],'traits':list(s['traits']),'costs':s['costs']})
    assert len(rows)==214
    return rows
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
    text=json.dumps(build(),indent=2,ensure_ascii=False)+'\n'
    if args.check:assert OUT.read_text()==text,'Stale effects viewer catalog'
    else:OUT.write_text(text)
    print('Verified' if args.check else 'Wrote','214-spell cast library catalog')
