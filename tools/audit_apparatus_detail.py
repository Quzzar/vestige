#!/usr/bin/env python3
"""Verify approved apparatus edging, body preservation, columns and collision."""
import argparse
import json
import math
from apparatus_columns import column_model, tent_spellstone, PLINTH_SOCKET_CENTER_Y, PLINTH_SOCKET_SIZE, PLINTH_SOCKET_FACE_Z, PLINTH_SOCKET_BACK_Z, SPELLSTONE_SURFACE
from author_apparatus_models import ROOT, review_model, physical, bounds, collision_boxes
from author_apparatus_middle import validate
from apparatus_framing import plinth_framing, spellstone_framing, TRIM_TINT_INDEX, TRIM_TINT_COLOR, TRIM_BRIGHTNESS


def solids(model):
    return [e for e in model['elements'] if all(a<b for a,b in zip(e['from'],e['to']))]


def envelope(model):
    boxes=[bounds(e) for e in solids(physical(model))]
    return [min(b[i] for b in boxes) for i in range(3)]+[max(b[i] for b in boxes) for i in range(3,6)]


def seam(model,y):
    faces={}
    for e in solids(model):
        if y not in (e['from'][1],e['to'][1]):continue
        for side in ('north','south','east','west'):
            f=e['faces'][side];uv=f['uv']
            scale=(uv[3]-uv[1])/(e['to'][1]-e['from'][1])
            border=uv[1]-(16-e['to'][1])*scale
            key=(side,e['from'][0],e['from'][2],e['to'][0],e['to'][2])
            faces[key]=(f['texture'],uv[0],uv[2],round(border,9),round(scale,9))
    assert len(faces)==4,'One square shaft must reach each join'
    return faces


def frame_audit(original, framed, expected):
    assert framed['elements'][:len(original['elements'])] == original['elements'], 'Original body changed'
    extras = framed['elements'][len(original['elements']):]
    assert len(extras) == expected
    for element in extras:
        assert len(element['faces']) == 1 and any(a == b for a, b in zip(element['from'], element['to']))
        face = next(iter(element['faces'].values()))
        assert face['tintindex'] == TRIM_TINT_INDEX
        assert framed['textures'][face['texture'][1:]] in (original['textures']['stone'], original['textures'].get('column'))
        assert not collision_boxes(element), 'Decorative edging changed collision'
    assert solids(framed) == solids(original)
    return extras


def trim_seam(model, y):
    faces = {}
    for element in model['elements']:
        if all(a < b for a, b in zip(element['from'], element['to'])) or y not in (element['from'][1], element['to'][1]):
            continue
        side, face = next(iter(element['faces'].items()))
        uv = face['uv']
        scale = (uv[3] - uv[1]) / (element['to'][1] - element['from'][1])
        border = uv[1] - (16 - element['to'][1]) * scale
        key = (side, element['from'][0], element['from'][2], element['to'][0], element['to'][2])
        faces[key] = (face['texture'], uv[0], uv[2], round(border, 9), round(scale, 9))
    assert len(faces) == 8, 'Eight corner strips must continue through each column join'
    return faces


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check',action='store_true');args=parser.parse_args()
    palette=json.loads((ROOT/'docs/design/apparatus-materials.json').read_text())
    approved=json.loads((ROOT/'docs/art/spellstone-corner-details/smaller-corners/models/chip.json').read_text())
    models=[];seams=[]
    expected_solids={'single':3,'base':2,'shaft':1,'cap':2}
    expected_faces={'single':32,'base':22,'shaft':12,'cap':22}
    trim_counts={'single':16,'base':12,'shaft':8,'cap':12}
    for m in palette['materials']:
        original=review_model(m['id'],'plinth')
        bodies={part:column_model(original,part) for part in expected_solids}
        parts={part:plinth_framing(body,part) for part,body in bodies.items()}
        for part,current in parts.items():
            validate(current)
            assert len(solids(current))==expected_solids[part]
            frame_audit(bodies[part],current,trim_counts[part])
            assert len(current['elements'])==expected_solids[part]+trim_counts[part]
            assert sum(len(e['faces']) for e in current['elements'])==expected_faces[part]
            assert all('rotation' not in e for e in current['elements'])
            assert 'carving' not in current['textures'], 'An empty column retains a carved socket tile'
            models.append({'material':m['id'],'role':'plinth','part':part,
                'solid_cuboids':len(solids(current)),'flat_socket_faces':0,
                'trim_faces':trim_counts[part],'trim_color':TRIM_TINT_COLOR,
                'total_elements':len(current['elements']),'baked_faces':expected_faces[part],
                'physical_envelope':envelope(current)})
        joins=[seam(parts['base'],16),seam(parts['shaft'],0),seam(parts['shaft'],16),seam(parts['cap'],0)]
        assert all(j==joins[0] for j in joins),'Column UV scale/phase/bounds differ at a join'
        trim_joins=[trim_seam(parts['base'],16),trim_seam(parts['shaft'],0),trim_seam(parts['shaft'],16),trim_seam(parts['cap'],0)]
        assert all(j==trim_joins[0] for j in trim_joins),'Corner trim UV scale/phase/bounds differ at a join'
        assert envelope(parts['single'])==[2,0,2,14,14,14]
        assert envelope(parts['shaft'])==[3,0,3,13,16,13]
        assert PLINTH_SOCKET_FACE_Z<PLINTH_SOCKET_BACK_Z<3
        assert 3-PLINTH_SOCKET_FACE_Z==0.5, 'Material should protrude half a model unit'
        seams.append({'material':m['id'],'matched_side_faces':4,
            'shaft_texture':parts['shaft']['textures']['column'],'matched_trim_faces':8,'empty_socket_texture_faces':0})
        original_stone=review_model(m['id'],'spellstone');body=tent_spellstone(original_stone);stone=spellstone_framing(body)
        validate(stone)
        frame_audit(body,stone,32)
        assert len(solids(stone))==3 and len(stone['elements'])==75
        assert sum(len(e['faces']) for e in stone['elements'])==90
        assert set(stone['textures'])=={'stone','diamond','particle','frame'}
        assert all(stone['textures'][key].startswith('minecraft:') for key in ('stone','particle'))
        assert stone['textures']['diamond']=='minecraft:block/diamond_block'
        for current,previous in zip(stone['elements'][3:43],approved['elements'][3:]):
            assert current['faces']==previous['faces'], 'Approved A corner pattern or palette drift'
            for key in ('from','to'):
                assert all(abs(current[key][i]-previous[key][i]-(2.25 if i==1 else 0))<1e-9 for i in range(3))
        supports=stone['elements'][:2];tabletop=stone['elements'][2];gems=stone['elements'][3:43]
        assert [e['rotation']['angle'] for e in supports]==[-22.5,22.5]
        assert all(e['rotation']['axis']=='z' and not e['rotation']['rescale'] for e in supports)
        assert all(abs(e['to'][0]-e['from'][0]-3.5)<1e-8 for e in supports)
        assert all(abs(bounds(e)[4]-9.99)<1e-8 for e in supports), 'Cap repair altered support coordinates'
        assert tabletop['to'][1]-max(bounds(e)[4] for e in supports)>=.25, 'Support still nearly flush with cap'
        assert tabletop['from']==[2,7,2] and tabletop['to']==[14,10.25,14]
        assert all(abs(a-b)<1e-8 for a,b in zip(envelope(stone),[2,0,2,14,10.25,14]))
        assert SPELLSTONE_SURFACE/16>.6, 'Top permits normal automatic stepping'
        assert len(gems)==40 and {next(iter(e['faces'])) for e in gems}=={'north','south','east','west'}
        for e in gems:
            side=next(iter(e['faces']))
            assert len(e['faces'])==1 and e['faces'][side]['texture']=='#diamond'
            assert e['faces'][side]['uv'] in ([15,5,16,6],[4,0,5,1],[4,1,5,2])
            assert 8.4125-1e-9<=e['from'][1]<e['to'][1]<=10.07+1e-9
            assert not collision_boxes(e), 'Corner pixel changes collision'
            axis=2 if side in ('north','south') else 0
            plane=1.988 if side in ('north','west') else 14.012
            assert e['from'][axis]==e['to'][axis]==plane, 'Chip is not on the vertical corner'
            across=0 if axis==2 else 2
            assert e['to'][across]<=3.105+1e-9 or e['from'][across]>=12.895-1e-9, 'Chip reaches the side center'
            assert abs(e['to'][across]-e['from'][across]-.5525)<1e-9
            assert abs(e['to'][1]-e['from'][1]-.5525)<1e-9
        boxes=[b for e in stone['elements'] for b in collision_boxes(e)]
        def occupied(x,y):
            return any(b[0]-1e-8<=x<=b[3]+1e-8 and b[1]-1e-8<=y<=b[4]+1e-8 for b in boxes)
        # Inverse-rotate independent sample points: every actual support point needs collision.
        for xi in range(57):
            for yi in range(41):
                x=1+xi/4;y=yi/4
                for e in supports:
                    r=e['rotation'];ox,oy,_=r['origin'];a=math.radians(r['angle'])
                    px=ox+(x-ox)*math.cos(a)+(y-oy)*math.sin(a)
                    py=oy-(x-ox)*math.sin(a)+(y-oy)*math.cos(a)
                    actual=e['from'][0]-1e-8<=px<=e['to'][0]+1e-8 and e['from'][1]-1e-8<=py<=e['to'][1]+1e-8
                    if actual:assert occupied(x,y),(m['id'],x,y)
        assert all(not occupied(8,y) for y in (0.5,1.5,2.5,4.5)), 'Tilted collision fills the triangular opening'
        assert occupied(5,1.5) and occupied(11,1.5) and occupied(8,9), 'Missing support/table collision'
        models.append({'material':m['id'],'role':'spellstone','solid_cuboids':3,'vertical_corner_chips':4,'diamond_top_faces':0,
            'diamond_side_faces':40,'corner_pixel_width':.5525,'trim_faces':32,'trim_color':TRIM_TINT_COLOR,'total_elements':75,'baked_faces':90,
            'physical_height':10.25,'tabletop_width':12,'tabletop_bottom':7,'tabletop_thickness':3.25,'support_thickness':3.5,'support_angles':[-22.5,22.5],
            'collision_slice_height':.25,'physical_envelope':envelope(stone)})
    result={'status':'Locked narrow corner/rim/support edging on all 36 finishes; thick three-piece Spellstone and smaller A corners'
        ,'trim':{'tint_index':TRIM_TINT_INDEX,'color':TRIM_TINT_COLOR,'brightness':TRIM_BRIGHTNESS,'texture':'Variant-native stone; pillars/interior tile rows follow the shaft'},
        'models':models,'column_seams':seams,
        'geometry':'Standalone: three stone cuboids and sixteen trim faces; base/cap: two cuboids and twelve trim faces; shaft: one cuboid and eight trim faces. No empty socket plaques.',
        'socket':{'center_y':PLINTH_SOCKET_CENTER_Y,'material_face_z':PLINTH_SOCKET_FACE_Z,
            'material_back_z':PLINTH_SOCKET_BACK_Z,'protrusion':3-PLINTH_SOCKET_FACE_Z,
            'material_size':PLINTH_SOCKET_SIZE,'quad_count_per_imbued_segment':20},
        'spellstone':'Two inward-leaning supports at 22.5 degrees, 3.5 model units thick, and one smaller 12-square cap 3.25 units thick at y10.25/16. Forty decorative quads retain the smaller A pattern and native palette. Accurate collision/picking uses quarter-unit slices; visible highlighting uses only the three authored cuboids. The native Astral Seal sits beneath flat offerings.',
        'preservation':'Plinth solid geometry, offerings, independent stored sockets, recipes, waterlogging and shaping are unchanged. Plinth surface stays y14/16; foot/cap width 12/16 and shaft width 10/16.',
        'textures':'Existing native 16x16 Minecraft masonry and Diamond tiles retain their bytes. Trim uses the matching variant tile at 217/255 brightness. Fixed UV phase across Plinth joins; native Quartz/Purpur pillar shaft. No custom Diamond bitmap is packaged.'}
    path=ROOT/'docs/art/plinth-framing/locked/model-and-seam-audit.json'
    text=json.dumps(result,indent=2)+'\n'
    if args.check:assert path.is_file() and path.read_text()==text,'Apparatus model audit drift'
    else:path.parent.mkdir(parents=True,exist_ok=True);path.write_text(text)
    print('36 finishes: approved edging, continuous body/trim joins, unchanged solid collision and smaller A Diamond chips')


if __name__=='__main__':main()
