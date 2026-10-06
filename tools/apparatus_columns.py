"""Connected column geometry and archived/simple/middle review alternatives, in model units."""
import copy
import math

TILE_BORDERS={
    'minecraft:block/polished_andesite','minecraft:block/polished_diorite',
    'minecraft:block/polished_granite','minecraft:block/polished_tuff',
    'minecraft:block/polished_blackstone','minecraft:block/cut_sandstone',
    'minecraft:block/cut_red_sandstone','minecraft:block/quartz_block_top',
    'minecraft:block/purpur_block',
}

# Shared block-space coordinates for a flat socket and slightly raised material inset.
PLINTH_SOCKET_CENTER_Y=8
PLINTH_SOCKET_FACE_Z=2.5
PLINTH_SOCKET_BACK_Z=2.98
PLINTH_SOCKET_SIZE=4
SPELLSTONE_CORNER_PALETTE={'d':[15,5,16,6],'m':[4,0,5,1],'c':[4,2,5,3],
    'p':[4,1,5,2],'w':[1,1,2,2]}
SPELLSTONE_CORNER_CHIP=('pm.','md.','d..')
SPELLSTONE_CORNER_PIXEL=.85*.65
SPELLSTONE_SUPPORT_SURFACE=10
SPELLSTONE_SURFACE=10.25


def box(a, b, texture='stone'):
    x,y,z=a;X,Y,Z=b
    uv={'up':[x,z,X,Z],'down':[x,z,X,Z],
        'north':[16-X,16-Y,16-x,16-y],'south':[x,16-Y,X,16-y],
        'west':[z,16-Y,Z,16-y],'east':[16-Z,16-Y,16-z,16-y]}
    return {'from':a,'to':b,'faces':{f:{'texture':'#'+texture,'uv':v} for f,v in uv.items()}}


def tier(width, bottom, top, texture='trim'):
    edge=(16-width)/2
    return box([edge,bottom,edge],[16-edge,top,16-edge],texture)


def shaft(bottom, top):
    element=tier(7.896,bottom,top,'carving')
    # Four coherent carvings use a whole vanilla tile, rather than many tiny frame cuboids.
    for face in ('north','south','east','west'):
        element['faces'][face]['uv']=[0,0,16,16]
    return element


def rim():
    return [box([1.89,14,1.89],[14.11,14.3,2.548],'carving'),
            box([1.89,14,13.452],[14.11,14.3,14.11],'carving'),
            box([1.89,14,2.548],[2.548,14.3,13.452],'carving'),
            box([13.452,14,2.548],[14.11,14.3,13.452],'carving')]


def plinth_parts(part):
    foot=[tier(10.34,0,.8),tier(8.836,.8,1.8)]
    crown=[tier(8.836,11.1,12.9),tier(12.22,12.9,14)]+rim()
    if part=='base': return foot+[shaft(1.8,16)]
    if part=='shaft': return [shaft(0,16)]
    if part=='cap': return [shaft(0,11.1)]+crown
    if part=='single': return foot+[shaft(1.8,11.1)]+crown
    raise ValueError(part)


def simple_column_model(original, part):
    result=copy.deepcopy(original)
    result['elements']=plinth_parts(part)
    return result


def corner_trim(bottom, top):
    # The same bounds and block-space UVs continue through every vertical join.
    return [box([x,bottom,z],[x+.72,top,z+.72],'trim')
            for x in (3.723,11.557) for z in (3.723,11.557)]


def column_uv(element, border):
    # Tile the interior rows, omitting decorative tile-edge bands at artificial block joins.
    for side in ('north','south','east','west'):
        uv=element['faces'][side]['uv']
        scale=(16-2*border)/16
        uv[1]=border+(16-element['to'][1])*scale
        uv[3]=border+(16-element['from'][1])*scale
    return element


def panel_bars():
    bars=[]
    for low,high in ((2.94,3.35),(10.25,10.65)):
        bars.extend([box([4.443,low,3.81],[11.557,high,4.12],'trim'),
                     box([4.443,low,11.88],[11.557,high,12.19],'trim'),
                     box([3.81,low,4.443],[4.12,high,11.557],'trim'),
                     box([11.88,low,4.443],[12.19,high,11.557],'trim')])
    return bars


def masonry_shaft(bottom, top):
    # Keep one texel scale and phase, instead of fitting a full carved tile into each part.
    return tier(7.896,bottom,top,'column')


def connected_plinth_parts(part):
    foot=[tier(10.34,0,.8),tier(8.836,.8,1.8)]
    crown=[tier(8.836,11.1,11.9),tier(9.494,11.9,12.9),tier(12.22,12.9,14)]+rim()
    if part=='base':return foot+[masonry_shaft(1.8,16)]+corner_trim(1.8,16)
    if part=='shaft':return [masonry_shaft(0,16)]+corner_trim(0,16)
    if part not in ('single','cap'):raise ValueError(part)
    bottom=1.8 if part=='single' else 0
    panel=tier(7.896,3.2,10.4,'stone')
    for face in ('north','south','east','west'):
        panel['faces'][face]={'texture':'#carving','uv':[3,3,13,13]}
    head=[masonry_shaft(bottom,3.2),panel,masonry_shaft(10.4,11.1)]
    return (foot if part=='single' else [])+head+corner_trim(bottom,11.1)+panel_bars()+crown


def middle_column_model(original, part):
    """Frozen middle-ground alternative, retained for historical comparison captures."""
    result=copy.deepcopy(original)
    source=result['textures']['stone']
    result['textures']['column']={
        'minecraft:block/quartz_block_side':'minecraft:block/quartz_pillar',
        'minecraft:block/purpur_block':'minecraft:block/purpur_pillar',
    }.get(source,source)
    result['elements']=connected_plinth_parts(part)
    for element in result['elements']:
        texture=element['faces']['north']['texture']
        is_corner=element['from'][0] in (3.723,11.557) and element['from'][2] in (3.723,11.557)
        if texture=='#column' or is_corner:
            column_uv(element,1 if result['textures'][texture[1:]] in TILE_BORDERS else 0)
    return result


def column_model(original, part):
    """Plain stone columns; installed materials are rendered separately by the client."""
    if part not in ('single','base','shaft','cap'):raise ValueError(part)
    result=copy.deepcopy(original)
    result['textures']={key:result['textures'][key] for key in ('stone','particle')}
    texture=result['textures']['stone']
    result['textures']['column']={
        'minecraft:block/quartz_block_side':'minecraft:block/quartz_pillar',
        'minecraft:block/purpur_block':'minecraft:block/purpur_pillar',
    }.get(texture,texture)
    foot=part in ('single','base');cap=part in ('single','cap')
    body=box([3,2 if foot else 0,3],[13,12 if cap else 16,13],'column')
    # Both ends are covered by the foot/cap or the adjacent column segment.
    body['faces'].pop('up');body['faces'].pop('down')
    column_uv(body,1 if result['textures']['column'] in TILE_BORDERS else 0)
    result['elements']=([tier(12,0,2,'stone')] if foot else [])+[body]
    if cap:result['elements'].append(tier(12,12,14,'stone'))
    return result


def detailed_spellstone(original):
    """Keep the crown/body; clip the single-height foundation to the same octagonal outline."""
    result=copy.deepcopy(original);source=original['elements']
    assert len(source)==33
    height=source[1]['to'][1]
    foot=[box([1,0,5],[15,height,11],'trim'),
          box([5,0,1],[11,height,5],'trim'),
          box([5,0,11],[11,height,15],'trim')]
    # Four standard 45-degree cuboids bridge the cut corners. Their overlap lies
    # inside the foundation; the visible perimeter follows x+z=6 at the NW corner.
    half_length=2*math.sqrt(2);half_depth=math.sqrt(2)
    for x,z,angle in ((4,4,45),(12,4,-45),(12,12,45),(4,12,-45)):
        corner=box([x-half_length,0,z-half_depth],[x+half_length,height,z+half_depth],'trim')
        corner['rotation']={'origin':[x,height/2,z],'axis':'y','angle':angle,'rescale':False}
        foot.append(corner)
    result['elements']=foot+copy.deepcopy(source[2:])
    return result


def spellstone_corner_pixels(rows,size,top=7.82):
    """Wrap native Diamond pixel patches onto both faces of four vertical cap corners."""
    pieces=[]
    for x,z in ((2,2),(14,2),(14,14),(2,14)):
        for side in (('north' if z==2 else 'south'),('west' if x==2 else 'east')):
            for row,cells in enumerate(rows):
                y=top-(row+1)*size
                for col,colour in enumerate(cells):
                    if colour=='.':continue
                    if side in ('north','south'):
                        a,b=(x+col*size,x+(col+1)*size) if x==2 else (x-(col+1)*size,x-col*size)
                        plane=z+(-.012 if z==2 else .012)
                        element=box([a,y,plane],[b,y+size,plane],'diamond')
                    else:
                        a,b=(z+col*size,z+(col+1)*size) if z==2 else (z-(col+1)*size,z-col*size)
                        plane=x+(-.012 if x==2 else .012)
                        element=box([plane,y,a],[plane,y+size,b],'diamond')
                    element['faces']={side:{'texture':'#diamond','uv':SPELLSTONE_CORNER_PALETTE[colour]}}
                    pieces.append(element)
    return pieces


def tent_spellstone(original):
    """Approved compact three-piece stonework with smaller A chips at the vertical corners."""
    result=copy.deepcopy(original)
    result['textures']={key:original['textures'][key] for key in ('stone','particle')}
    result['textures']['diamond']='minecraft:block/diamond_block'
    # A gentler native 22.5-degree lean keeps the thick supports from crossing below the cap.
    # A pivot at ground level keeps the authored cuboids and their UVs within one block.
    thickness=3.5
    angle=math.radians(22.5)
    bottom=thickness/2*math.tan(angle)
    pivot=2+thickness/2*math.cos(2*angle)/math.cos(angle)
    length=(SPELLSTONE_SUPPORT_SURFACE-.01-thickness*math.sin(angle))/math.cos(angle)
    supports=[]
    for center,lean in ((pivot,-22.5),(16-pivot,22.5)):
        e=box([center-thickness/2,bottom,3],[center+thickness/2,bottom+length,13])
        e['rotation']={'origin':[center,0,8],'axis':'z','angle':lean,'rescale':False}
        supports.append(e)
    tabletop=box([2,SPELLSTONE_SURFACE-3.25,2],[14,SPELLSTONE_SURFACE,14])
    result['elements']=supports+[tabletop]+spellstone_corner_pixels(SPELLSTONE_CORNER_CHIP,SPELLSTONE_CORNER_PIXEL,SPELLSTONE_SURFACE-.18)
    return result


def middle_spellstone(original):
    """Keep the octagon; merge each three-piece corner molding into one stone piece."""
    result=copy.deepcopy(original);source=original['elements']
    assert len(source)==33 and '#runes' in {f['texture'] for f in source[-1]['faces'].values()}
    elements=copy.deepcopy(source[:16])
    inlays=[]
    for i in range(4):
        left,right,center,diamond=copy.deepcopy(source[16+i*4:20+i*4])
        center['from'][0]=left['from'][0];center['to'][0]=right['to'][0];center['to'][1]=8
        merged=box(center['from'],center['to'],'carving');merged['rotation']=center['rotation']
        elements.append(merged)
        # A flush-looking inlay decal avoids coplanar stone/diamond faces without a tiny inset box.
        diamond['from'][1]=diamond['to'][1]=8.004
        diamond['faces']={'up':diamond['faces']['up']}
        inlays.append(diamond)
    result['elements']=elements+inlays+[copy.deepcopy(source[-1])]
    return result


def simplified_spellstone(original):
    result=copy.deepcopy(original)
    # Square-cut corners match the vanilla voxel vocabulary, retaining a broad scroll well.
    elements=[box([1,0,3],[15,1,13],'trim'),box([3,0,1],[13,1,3],'trim'),box([3,0,13],[13,1,15],'trim'),
              box([2,1,4],[14,7,12]),box([4,1,2],[12,7,4]),box([4,1,12],[12,7,14]),
              box([3,7,1],[13,8,3],'carving'),box([3,7,13],[13,8,15],'carving'),
              box([1,7,3],[3,8,13],'carving'),box([13,7,3],[15,8,13],'carving')]
    for x,z in [(4,1.5),(11,1.5),(4,13.5),(11,13.5)]:
        diamond=box([x,7.8,z],[x+1,8,z+1],'diamond')
        diamond['faces']['up']['uv']=[4,4,12,12]
        elements.append(diamond)
    elements.extend(copy.deepcopy(e) for e in original['elements'] if any(f['texture']=='#runes' for f in e['faces'].values()))
    result['elements']=elements
    return result
