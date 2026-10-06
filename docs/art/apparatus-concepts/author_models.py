#!/usr/bin/env python3
"""Author review-only Minecraft apparatus models and embed their preview inputs."""
from __future__ import annotations

import argparse
import base64
import json
import math
from pathlib import Path
import zipfile

HERE = Path(__file__).resolve().parent
PALETTE = HERE.parents[1]/'design/apparatus-materials.json'
FRAGMENT = HERE/'review-source.html'
CLIENT = Path('/Users/quzzar/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
VARIANTS = {entry['id']:entry for entry in json.loads(PALETTE.read_text())['materials']}
RUNE_PRESENTATION = {
    'hover_y': 7.12, 'core_brightness': 1.2,
    'halo_radius_texels': 1.0, 'halo_opacity': .5,
    'halo_color': [0.66, 0.24, 1.0],
}


def box(a, b, texture='stone', *, rotation=None, faces=None):
    """Use block-space UVs so small moldings do not stretch a whole stone tile."""
    x, y, z = a
    X, Y, Z = b
    uv = {'up': [x,z,X,Z], 'down': [x,z,X,Z],
          'north': [16-X,16-Y,16-x,16-y], 'south': [x,16-Y,X,16-y],
          'west': [z,16-Y,Z,16-y], 'east': [16-Z,16-Y,16-z,16-y]}
    value = {'from': a, 'to': b,
             'faces': {f: {'texture': '#'+texture, 'uv': uv[f]} for f in (faces or uv)}}
    if rotation:
        value['rotation'] = rotation
    return value


def tier(width, y0, y1, texture='trim'):
    edge = (16-width)/2
    return box([edge,y0,edge],[16-edge,y1,16-edge],texture)


def side(piece, turn):
    """Turn complete side ornaments around the block center, using supported angles."""
    value = json.loads(json.dumps(piece))
    if turn == 0:
        return value
    # Bake quarter-turns into bounds instead of using unsupported 90-degree element rotations.
    pts = []
    for x in (value['from'][0], value['to'][0]):
        for z in (value['from'][2], value['to'][2]):
            px, pz = x, z
            for _ in range(turn):
                px, pz = 16-pz, px
            pts.append((px,pz))
    return box([min(p[0] for p in pts), value['from'][1], min(p[1] for p in pts)],
               [max(p[0] for p in pts), value['to'][1], max(p[1] for p in pts)],
               next(iter(value['faces'].values()))['texture'][1:])


def models():
    # Plinth: distinct foot, recessed framed shaft, widening neck and thin overhanging cap.
    plinth = [tier(11,0,.8),tier(10.2,.8,1.25),tier(9.4,1.25,1.8),
              tier(8.4,1.8,9.1,'stone')]
    for x in (3.45,11.55):
        for z in (3.45,11.55):
            plinth.append(box([x,1.8,z],[x+1,9.15,z+1],'trim'))
    panel = [box([4.6,2.7,3.55],[11.4,3.12,3.9],'trim'),
             box([4.6,8.15,3.55],[11.4,8.6,3.9],'trim'),
             box([4.6,3.12,3.55],[5.02,8.15,3.9],'trim'),
             box([10.98,3.12,3.55],[11.4,8.15,3.9],'trim'),
             box([6.55,4.25,3.59],[9.45,4.58,3.93],'carving'),
             box([6.55,6.82,3.59],[9.45,7.15,3.93],'carving'),
             box([6.55,4.58,3.59],[6.88,6.82,3.93],'carving'),
             box([9.12,4.58,3.59],[9.45,6.82,3.93],'carving')]
    for turn in range(4):
        plinth.extend(side(p,turn) for p in panel)
    # Give the shaft breathing room before the flared cornice, as in the owner's reference.
    for element in plinth:
        for corner in ('from','to'):
            if element[corner][1] > 1.8:
                element[corner][1] = 1.8+(element[corner][1]-1.8)*(9.3/7.3)
    plinth.extend([tier(9,11.1,11.9),tier(10.1,11.9,12.9),
                   tier(12,12.9,13.15),tier(13,13.15,14,'trim')])
    for axis,angle,origin,a,b in [
        ('x',-22.5,[8,12.15,2.95],[2.75,10.9,2.8],[13.25,13.4,3.1]),
        ('x',22.5,[8,12.15,13.05],[2.75,10.9,12.9],[13.25,13.4,13.2]),
        ('z',22.5,[2.95,12.15,8],[2.8,10.9,2.75],[3.1,13.4,13.25]),
        ('z',-22.5,[13.05,12.15,8],[12.9,10.9,2.75],[13.2,13.4,13.25]),
    ]:
        plinth.append(box(a,b,'trim',rotation={'origin':origin,'axis':axis,'angle':angle,'rescale':False}))
    # Top receiving surface is visibly inset within the overhanging rim.
    plinth.extend([box([2.35,14,2.35],[13.65,14.3,3.05],'carving'),
                   box([2.35,14,12.95],[13.65,14.3,13.65],'carving'),
                   box([2.35,14,3.05],[3.05,14.3,12.95],'carving'),
                   box([12.95,14,3.05],[13.65,14.3,12.95],'carving')])
    # Narrow the entire footprint without changing the cap's deliberate overhang.
    for element in plinth:
        for key in ('from','to'):
            for coordinate in (0,2):
                element[key][coordinate] = 8+(element[key][coordinate]-8)*.94
        if 'rotation' in element:
            for coordinate in (0,2):
                element['rotation']['origin'][coordinate] = 8+(element['rotation']['origin'][coordinate]-8)*.94

    # Spellstone: squat ancient altar with a separate eight-sided crown and a scroll well.
    spellstone = [tier(14,0,.75),tier(13,.75,1.3),
                  box([3.6,1.3,3.6],[12.4,6.98,12.4],'stone')]
    # Broad eight-sided body: long diagonal stone faces distinguish it from the Plinth.
    for a,b in [([5.25,1.3,1.25],[10.75,6.98,4.7]),
                ([5.25,1.3,11.3],[10.75,6.98,14.75]),
                ([1.25,1.3,5.25],[4.7,6.98,10.75]),
                ([11.3,1.3,5.25],[14.75,6.98,10.75])]:
        spellstone.append(box(a,b,'stone'))
    half_length=math.sqrt(8)
    offset=1.2/math.sqrt(2)
    for x,z,angle,sx,sz in [(3.25,3.25,45,1,1),(12.75,3.25,-45,-1,1),
                           (12.75,12.75,45,-1,-1),(3.25,12.75,-45,1,-1)]:
        cx,cz=x+sx*offset,z+sz*offset
        spellstone.append(box([cx-half_length,1.3,cz-1.2],[cx+half_length,6.98,cz+1.2],'stone',
            rotation={'origin':[cx,4,cz],'axis':'y','angle':angle,'rescale':False}))
    spellstone.append(box([3.25,6.99,3.5],[12.75,7,12.5],'trim'))
    rim = [box([4,6.8,1],[12,8,2.55],'carving'),
           box([4,6.8,13.45],[12,8,15],'carving'),
           box([1,6.8,4],[2.55,8,12],'carving'),
           box([13.45,6.8,4],[15,8,12],'carving')]
    for x,z,angle in [(2.8,2.8,45),(13.2,2.8,-45),(13.2,13.2,45),(2.8,13.2,-45)]:
        rotation={'origin':[x,7.4,z],'axis':'y','angle':angle,'rescale':False}
        rim.extend([box([x-2.1,6.8,z-.75],[x-.58,8,z+.75],'carving',rotation=rotation),
                    box([x+.58,6.8,z-.75],[x+2.1,8,z+.75],'carving',rotation=rotation),
                    box([x-.58,6.8,z-.75],[x+.58,7.8,z+.75],'carving',rotation=rotation)])
        # A small flush diamond cap sits inside each diagonal stone section.
        diamond=box([x-.58,7.8,z-.5],[x+.58,8,z+.5],'diamond',rotation=rotation)
        diamond['faces']['up']['uv']=[4,4,12,12]
        rim.append(diamond)
    spellstone.extend(rim)
    # The entire original sigil is one plane, a tiny gap above the y=7 stone backdrop.
    height = RUNE_PRESENTATION['hover_y']
    spellstone.append({'from':[2.75,height,2.75],'to':[13.25,height,13.25],
                       'shade':False,
                       'faces':{'up':{'texture':'#runes','uv':[0,0,16,16]}}})
    for name,elements in [('plinth',plinth),('spellstone',spellstone)]:
        yield name, {'parent':'minecraft:block/block','ambientocclusion':True,
                     'render_type':'minecraft:cutout','textures':{},'elements':elements}


def validate(model):
    """Check native JSON bounds and supported element rotations before exposing a preview."""
    for e in model['elements']:
        assert all(a<=b for a,b in zip(e['from'],e['to']))
        if 'rotation' in e:
            assert e['rotation']['angle'] in (-45,-22.5,0,22.5,45)
        for x in (e['from'][0],e['to'][0]):
            for y in (e['from'][1],e['to'][1]):
                for z in (e['from'][2],e['to'][2]):
                    point=[x,y,z]
                    rotation=e.get('rotation')
                    if rotation:
                        origin=rotation['origin']
                        v=[point[i]-origin[i] for i in range(3)]
                        angle=math.radians(rotation['angle'])
                        c,s=math.cos(angle),math.sin(angle)
                        if rotation['axis']=='x':
                            v=[v[0],c*v[1]-s*v[2],s*v[1]+c*v[2]]
                        elif rotation['axis']=='y':
                            v=[c*v[0]+s*v[2],v[1],-s*v[0]+c*v[2]]
                        else:
                            v=[c*v[0]-s*v[1],s*v[0]+c*v[1],v[2]]
                        point=[v[i]+origin[i] for i in range(3)]
                    assert all(0<=v<=16 for v in point),point
        for face in e['faces'].values():
            assert len(face['uv'])==4
            assert all(0<=v<=16 for v in face['uv']),face


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check',action='store_true')
    parser.add_argument('--fragment',type=Path,default=FRAGMENT)
    parser.add_argument('--client-jar',type=Path,default=CLIENT)
    args = parser.parse_args()
    base_models = dict(models())
    presentation_path = HERE/'rune-presentation.json'
    presentation_content = json.dumps(RUNE_PRESENTATION,indent=2)+'\n'
    if args.check:
        assert presentation_path.read_text()==presentation_content,presentation_path
    else:
        presentation_path.write_text(presentation_content)
    textures = {'amethyst':'minecraft:block/amethyst_block','diamond':'minecraft:block/diamond_block',
                'gold':'minecraft:block/gold_block','runes':'vestige:block/spellstone_runes_purple',
                'paper':'minecraft:item/paper'}
    for variant,spec in VARIANTS.items():
        for name,base in base_models.items():
            value = json.loads(json.dumps(base))
            value['textures']={**spec['textures'],'particle':spec['textures']['stone'],
                               'amethyst':textures['amethyst'],'diamond':textures['diamond'],'runes':textures['runes']}
            validate(value)
            path = HERE/'models'/f'{name}-{variant}.json'
            content = json.dumps(value,indent=2)+'\n'
            if args.check:
                assert path.read_text()==content,path
            else:
                path.parent.mkdir(parents=True,exist_ok=True)
                path.write_text(content)
            textures.update({f'{variant}-{key}':val for key,val in value['textures'].items() if key in ('stone','carving','trim')})
    atlas = {}
    texture_frames = {}
    with zipfile.ZipFile(args.client_jar) as jar:
        stairs = json.loads(jar.read('data/minecraft/tags/block/stairs.json'))['values']
        expected_stairs = {value for value in stairs if not value.startswith('#')
                           and 'bamboo' not in value and 'copper' not in value}
        selected_stairs = [entry['source_stairs'] for entry in VARIANTS.values()]
        assert len(selected_stairs)==len(set(selected_stairs)), 'Duplicate stair material'
        assert set(selected_stairs)==expected_stairs, 'Stone stair palette drift'
        for key,location in textures.items():
            namespace,asset = location.split(':',1)
            if namespace=='minecraft':
                data = jar.read(f'assets/minecraft/textures/{asset}.png')
                metadata_path = f'assets/minecraft/textures/{asset}.png.mcmeta'
                if metadata_path in jar.namelist():
                    animation = json.loads(jar.read(metadata_path)).get('animation')
                    if animation is not None:
                        # Preserve the vanilla PNG; the review samples its first animation frame.
                        width = int.from_bytes(data[16:20], 'big')
                        height = int.from_bytes(data[20:24], 'big')
                        if 'width' in animation:
                            frame_width = animation['width']
                            frame_height = animation.get('height', height)
                        elif 'height' in animation:
                            frame_width, frame_height = width, animation['height']
                        else:
                            frame_width = frame_height = min(width, height)
                        assert frame_width > 0 and frame_height > 0, location
                        assert width % frame_width == height % frame_height == 0, location
                        columns, rows = width // frame_width, height // frame_height
                        first = animation.get('frames', [0])[0]
                        index = first['index'] if isinstance(first, dict) else first
                        assert isinstance(index, int) and 0 <= index < columns * rows, location
                        texture_frames[key] = {
                            'repeat': [1 / columns, 1 / rows],
                            'offset': [(index % columns) / columns, 1 - (index // columns + 1) / rows],
                        }
            else:
                data = (HERE/'textures'/(Path(asset).name+'.png')).read_bytes()
            atlas[key]='data:image/png;base64,'+base64.b64encode(data).decode()
    manifest={'models':base_models,'variants':{k:v['name'] for k,v in VARIANTS.items()},'textures':atlas,
              'textureFrames':texture_frames,
              'runePresentation':RUNE_PRESENTATION,
              'surfaces':{'plinth':14,'spellstone':7},
              'socket':{'center':[8,1.8+(5.7-1.8)*(9.3/7.3),8+(3.84-8)*.94],
                        'width':2.22*.94,'height':2.22*(9.3/7.3),'depth':.10}}
    encoded=json.dumps(manifest,separators=(',',':'))
    fragment=args.fragment.read_text()
    start=fragment.index('/* APPARATUS_DATA_BEGIN */')+len('/* APPARATUS_DATA_BEGIN */')
    end=fragment.index('/* APPARATUS_DATA_END */')
    expected='\nconst DATA = '+encoded+';\n'
    if args.check:
        assert fragment[start:end]==expected,'Preview input drift'
    else:
        args.fragment.write_text(fragment[:start]+expected+fragment[end:])
    expected_paths = {HERE/'models'/f'{role}-{variant}.json'
                      for role in base_models for variant in VARIANTS}
    stale = set((HERE/'models').glob('*.json'))-expected_paths
    if args.check:
        assert not stale, f'Superseded model exports: {stale}'
    else:
        for path in stale:
            path.unlink()
    print(f'{len(base_models)} silhouettes, {len(VARIANTS)} material treatments; native model checks pass.')


if __name__=='__main__':
    main()
