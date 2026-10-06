#!/usr/bin/env python3
"""Inspect exported native meshes for outward normals, atlas-safe UVs and reserved bounds."""
from standing_stone_geometry import PROFILES, SIGNATURE_BUCKETS, faces, obj, slices

assert len(PROFILES) == 16 and len(set(SIGNATURE_BUCKETS)) == 16 and set(SIGNATURE_BUCKETS) == set(PROFILES)
assert len({tuple(p['outline']) for p in PROFILES.values()}) == 16, 'Each form must have its own silhouette'

for name, profile in PROFILES.items():
    # All four native glyphs need a receiving face, including squat crowns and leaning bodies.
    rune_x, rune_y = profile['runes']
    for glyph in range(4):
        top = rune_y - glyph * 12 * .022 * 16
        for sample in range(9):
            y = top - sample * .022 * 16
            intersections = []
            outline = profile['outline']
            for (ax, ay), (bx, by) in zip(outline, outline[1:] + outline[:1]):
                if ay != by and min(ay, by) <= y < max(ay, by):
                    intersections.append(ax + (bx - ax) * (y - ay) / (by - ay))
            intersections.sort()
            assert any(lo <= rune_x - 4 * .022 * 16 and rune_x + 4 * .022 * 16 <= hi
                       for lo, hi in zip(intersections[::2], intersections[1::2])), f'{name}: glyph escapes its stone face'
    for material, points in faces(profile):
        if all(abs(p[2] - profile['front']) < 1e-8 for p in points):
            a, b, c = points[:3]
            normal_z = (b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0])
            assert normal_z < 0, f'{name}: inward receiving face'
    for half in (0, 1, None):
        vertices = []; normals = []; face_count = 0
        for line in obj(profile, half).splitlines():
            fields = line.split()
            if fields[0] == 'v':
                v = list(map(float, fields[1:])); vertices.append(v)
                assert 0 <= v[0] <= 1 and 0 <= v[1] <= (2 if half is None else 1) and 0 <= v[2] <= 1
            if fields[0] == 'vt': assert all(-1e-8 <= float(v) <= 1+1e-8 for v in fields[1:]), f'{name}: atlas spill'
            if fields[0] == 'vn':
                normal = list(map(float, fields[1:])); normals.append(normal)
                assert abs(sum(v*v for v in normal)-1) < 1e-6
            if fields[0] == 'f':
                face_count += 1
                assert 3 <= len(fields[1:]) <= 4, 'OBJ baker requires triangles/quads'
                assert all(1 <= int(v.split('/')[0]) <= len(vertices) for v in fields[1:])
        assert face_count <= 25, f'{name}: unnecessary face complexity {face_count}'
        assert vertices and normals
    # The two placed meshes meet without a vertical gap; whole inventory mesh retains their height.
    assert abs(max(float(line.split()[2]) for line in obj(profile, 0).splitlines() if line.startswith('v '))-1) < 1e-6
    assert min(float(line.split()[2]) for line in obj(profile, 1).splitlines() if line.startswith('v ')) == 0
    for half in (0,1):
        bands = slices(profile, half)
        assert bands and all(0 <= b[0] < b[3] <= 16 and 0 <= b[1] < b[4] <= 16 and 0 <= b[2] < b[5] <= 16 for b in bands)
print(f'{len(PROFILES)} native meshes: outward faces, bounded UVs, <=25 baked faces, joined halves, contained collisions and fitted rune anchors verified')
