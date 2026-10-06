"""Authored low-poly monolith silhouettes in 1/16-block model units.

Fronts are quiet flat receiving faces for native rune glyphs. Irregular outlines
supply volume without inset edge bands, added caps or foot geometry.
"""
import math

PROFILES = {
    'blade': {'outline': [(3, 0), (11, 0), (12, 7), (10, 21), (7, 30), (4, 32), (3, 13)], 'front': 6, 'back': 10, 'runes': (7, 23.52)},
    'shoulder': {'outline': [(1, 0), (15, 0), (14, 9), (11, 22), (8, 29), (4, 25), (2, 15)], 'front': 4, 'back': 12, 'runes': (8, 23.52)},
    'leaning': {'outline': [(1, 0), (9, 0), (12, 9), (14, 26), (12, 31), (10, 28), (5, 14)], 'front': 5, 'back': 10, 'runes': (10, 23.52)},
    'spire': {'outline': [(4, 0), (10, 0), (11, 13), (9, 27), (7, 32), (5, 25), (4, 9)], 'front': 6, 'back': 11, 'runes': (7.5, 23)},
    'wedge': {'outline': [(1, 0), (15, 0), (13, 8), (9, 20), (3, 29), (2, 16)], 'front': 4, 'back': 12, 'runes': (6, 20)},
    'crown': {'outline': [(4, 0), (12, 0), (15, 19), (13, 27), (5, 29), (1, 22)], 'front': 5, 'back': 11, 'runes': (8, 24)},
    'fang': {'outline': [(2, 0), (12, 0), (11, 11), (15, 29), (10, 26), (6, 16), (3, 8)], 'front': 5, 'back': 10, 'runes': (8.7, 19)},
    'cleft': {'outline': [(1, 0), (15, 0), (14, 27), (11, 31), (8, 26), (3, 30)], 'front': 4, 'back': 11, 'runes': (8, 24)},
    'hunched': {'outline': [(1, 0), (15, 0), (14, 15), (10, 23), (5, 25), (2, 20), (1, 9)], 'front': 3, 'back': 13, 'runes': (7.5, 21)},
    'taper': {'outline': [(2, 0), (13, 0), (12, 14), (8, 30), (6, 28), (3, 19)], 'front': 5, 'back': 12, 'runes': (7, 23)},
    'slant': {'outline': [(5, 0), (15, 0), (13, 18), (9, 29), (2, 27), (1, 19)], 'front': 4, 'back': 10, 'runes': (7.5, 24)},
    'tablet': {'outline': [(2, 0), (14, 0), (13, 22), (11, 25), (3, 25), (2, 20)], 'front': 6, 'back': 10, 'runes': (8, 22)},
    'chisel': {'outline': [(4, 0), (12, 0), (11, 19), (10, 32), (5, 28), (3, 12)], 'front': 7, 'back': 10, 'runes': (7.5, 24)},
    'saddle': {'outline': [(2, 0), (14, 0), (13, 26), (10, 24), (7, 21), (3, 26)], 'front': 4, 'back': 13, 'runes': (7.5, 19)},
    'obelisk': {'outline': [(3, 0), (13, 0), (11, 25), (8, 31), (5, 25)], 'front': 5, 'back': 11, 'runes': (8, 23.5)},
    'keel': {'outline': [(2, 0), (14, 0), (15, 9), (12, 24), (5, 30), (2, 20), (1, 9)], 'front': 6, 'back': 10, 'runes': (8, 23)},
}

# Frozen signature buckets; enum order and future family size cannot silently remap keys.
SIGNATURE_BUCKETS = ('blade', 'leaning', 'spire', 'wedge', 'shoulder', 'crown', 'fang', 'cleft',
                     'hunched', 'taper', 'slant', 'tablet', 'chisel', 'saddle', 'obelisk', 'keel')


def area(points):
    return sum(a[0]*b[1]-b[0]*a[1] for a, b in zip(points, points[1:]+points[:1])) / 2


def triangles(points):
    """Ear clipping keeps authored mildly concave shoulders intact."""
    indices = list(range(len(points)))
    result = []
    def cross(a, b, c): return (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
    while len(indices) > 3:
        for at, current in enumerate(indices):
            prev, nxt = indices[at-1], indices[(at+1) % len(indices)]
            a, b, c = points[prev], points[current], points[nxt]
            if cross(a, b, c) <= 1e-9: continue
            if any(cross(a, b, points[i]) >= -1e-9 and cross(b, c, points[i]) >= -1e-9 and cross(c, a, points[i]) >= -1e-9
                   for i in indices if i not in (prev, current, nxt)): continue
            result.append((prev, current, nxt)); indices.pop(at); break
        else: raise ValueError('Invalid polygon')
    result.append(tuple(indices)); return result


def faces(profile):
    outline = profile['outline']
    assert area(outline) > 0
    front = [(x, y, profile['front']) for x, y in outline]
    back = [(x, y, profile['back']) for x, y in outline]
    result = [('stone', [front[i] for i in face]) for face in triangles(outline)]
    result += [('stone', [back[i] for i in reversed(face)]) for face in triangles(outline)]
    for i in range(len(outline)):
        nxt = (i+1) % len(outline)
        result.append(('stone', [front[i], back[i], back[nxt], front[nxt]]))
    return [(material, list(reversed(points))) for material, points in result]


def clip_y(points, y, above):
    result = []
    for a, b in zip(points, points[1:]+points[:1]):
        ina, inb = (a[1] >= y-1e-9, b[1] >= y-1e-9) if above else (a[1] <= y+1e-9, b[1] <= y+1e-9)
        if ina: result.append(a)
        if ina != inb:
            t = (y-a[1])/(b[1]-a[1]); result.append(tuple(a[i]+t*(b[i]-a[i]) for i in range(3)))
    cleaned = []
    for p in result:
        if not cleaned or max(abs(p[i]-cleaned[-1][i]) for i in range(3)) > 1e-8: cleaned.append(p)
    if len(cleaned)>1 and max(abs(cleaned[0][i]-cleaned[-1][i]) for i in range(3)) < 1e-8: cleaned.pop()
    return cleaned


def slices(profile, half):
    """Quarter-unit conservative collision bands follow the whole stone outline."""
    outline=profile['outline']; result=[]
    for index in range(64):
        low=half*16+index*.25; high=low+.25
        xs=[x for x,y in outline if low <= y <= high]
        for (ax,ay),(bx,by) in zip(outline,outline[1:]+outline[:1]):
            if abs(by-ay)<1e-9: continue
            for cut in (low,high):
                if min(ay,by)<=cut<=max(ay,by): xs.append(ax+(bx-ax)*(cut-ay)/(by-ay))
        if xs and max(xs)-min(xs)>1e-8:
            result.append([min(xs),low-half*16,profile['front'],max(xs),high-half*16,profile['back']])
    return result


def obj(profile, half=None):
    text=['# Authored Vestige native monolith; units are blocks.', 'mtllib standing_stones.mtl', 'o monolith']
    count=0
    for layer in ([half] if half is not None else [0, 1]):
      for material, original in faces(profile):
        points=clip_y(clip_y(original,layer*16,True),(layer+1)*16,False)
        if len(points)<3: continue
        # A clipped triangle may become a pentagon, which OBJ's quad baker cannot accept directly.
        polygons=[points] if len(points)<=4 else [[points[0],points[i],points[i+1]] for i in range(1,len(points)-1)]
        for points in polygons:
            normal=[sum((points[i][(axis+1)%3]-points[(i+1)%len(points)][(axis+1)%3]) *
                        (points[i][(axis+2)%3]+points[(i+1)%len(points)][(axis+2)%3]) for i in range(len(points))) for axis in range(3)]
            length=math.sqrt(sum(n*n for n in normal))
            if length<1e-9: continue
            normal=[n/length for n in normal]
            # UVs repeat each block of height; part clipping keeps samples inside their atlas tile.
            for x,y,z in points:
                text.append(f'v {x/16:.8f} {(y-(layer*16 if half is not None else 0))/16:.8f} {z/16:.8f}')
            for x,y,z in points:
                axis=max(range(3),key=lambda i:abs(normal[i]))
                u=(z if axis==0 else x)/16
                v=z/16 if axis==1 else 1-(y-layer*16)/16
                text.append(f'vt {u:.8f} {v:.8f}')
            for _ in points: text.append('vn '+' '.join(f'{n:.8f}' for n in normal))
            text.append('usemtl '+material)
            text.append('f '+' '.join(f'{count+i}/{count+i}/{count+i}' for i in range(1,len(points)+1)))
            count+=len(points)
    return '\n'.join(text)+'\n'
