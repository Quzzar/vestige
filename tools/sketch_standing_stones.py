#!/usr/bin/env python3
"""Low-fidelity vector silhouette study, not a Minecraft rendering or texture asset."""
from pathlib import Path
from html import escape

ROOT = Path(__file__).resolve().parents[1]
DESTINATION = ROOT / 'docs/art/standing-stone-concepts'

# Every design can fit a two-block upright without complicated modeled decoration.
DESIGNS = [
    ('A', 'Straight monolith', '10 wide · 30 high · 1 solid', [(3, 0, 5, 13, 30, 11)]),
    ('B', 'Broad shoulders', '12 wide · 28 high · 2 solids', [(2, 0, 5, 14, 24, 11), (3, 24, 5, 13, 28, 11)]),
    ('C', 'Deep crown notch', '10 wide · 31 high · 3 solids', [(3, 0, 5, 13, 25, 11), (3, 25, 5, 6, 31, 11), (10, 25, 5, 13, 31, 11)]),
    ('D', 'Worn asymmetric crown', '10 wide · 31 high · 2 solids', [(3, 0, 5, 13, 27, 11), (3, 27, 5, 10, 31, 11)]),
    ('E', 'Stepped crown', '10 wide · 31 high · 3 solids', [(3, 0, 5, 13, 25, 11), (4, 25, 5, 12, 28, 11), (5, 28, 5, 11, 31, 11)]),
    ('F', 'Broad shallow notch', '12 wide · 28 high · 3 solids', [(2, 0, 5, 14, 25, 11), (2, 25, 5, 6, 28, 11), (10, 25, 5, 14, 28, 11)]),
]


def project(p):
    x, y, z = p
    return ((x - z) * 6.0, (x + z) * 2.65 - y * 6.0)


def polygon(points, fill, stroke='#595a54'):
    points = ' '.join(f'{x:.2f},{y:.2f}' for x, y in map(project, points))
    return f'<polygon points="{points}" fill="{fill}" stroke="{stroke}" stroke-width="1.3" stroke-linejoin="round"/>'


def solid(box, shades):
    x, y, z, X, Y, Z = box
    front, side, top = shades
    return ''.join([
        polygon([(X, y, z), (X, y, Z), (X, Y, Z), (X, Y, z)], side),
        polygon([(x, y, Z), (X, y, Z), (X, Y, Z), (x, Y, Z)], front),
        polygon([(x, Y, z), (X, Y, z), (X, Y, Z), (x, Y, Z)], top),
    ])


def rune_mark():
    # Representative carved strokes only; shipped glyphs must reuse AttunementMark.
    paths = [
        [(7, 22), (8, 23), (9, 22), (8, 21), (7, 22), (8, 22), (8, 20)],
        [(7, 16), (8, 17), (8, 19), (8, 17), (9, 18)],
        [(7, 13), (8, 15), (9, 13), (8, 14), (8, 12)],
        [(7, 10), (8, 9), (9, 10), (8, 11), (7, 10)],
    ]
    result = []
    for path in paths:
        points = ' '.join(f'{x:.2f},{y:.2f}' for x, y in (project((x, y, 11.03)) for x, y in path))
        result.append(f'<polyline points="{points}" fill="none" stroke="#414c4b" stroke-width="4" stroke-linecap="square" stroke-linejoin="miter"/>')
        result.append(f'<polyline points="{points}" fill="none" stroke="#9ee1dd" stroke-width="2.4" stroke-linecap="square" stroke-linejoin="miter"/>')
    return ''.join(result)


def main():
    result = ['<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="1010" viewBox="0 0 1200 1010">',
              '<rect width="1200" height="1010" fill="#efeee8"/>',
              '<style>text{font-family:Arial,sans-serif;fill:#353a36}.small{font-size:15px;fill:#656b64}</style>',
              '<text x="48" y="54" font-size="30" font-weight="bold">Standing Stone · upright silhouette studies</text>',
              '<text x="48" y="83" class="small">Six possible members of one signature-selected family. These are sketches, not in-game renders.</text>']
    for index, (letter, name, detail, boxes) in enumerate(DESIGNS):
        x, y = 48 + (index % 3) * 374, 120 + (index // 3) * 362
        result.append(f'<g transform="translate({x},{y})">')
        result.append('<path d="M0,0H350" stroke="#cbcfc4"/>')
        result.append(f'<text x="0" y="28" font-size="20" font-weight="bold">{letter} · {escape(name)}</text>')
        result.append(f'<text x="0" y="52" class="small">{escape(detail)}</text>')
        result.append('<g transform="translate(187,295)">')
        # A unit guide conveys scale and keeps proportions comparable between panels.
        result.append('<path d="M-110,38V-155M-115,38H-105M-115,-58H-105M-115,-154H-105" fill="none" stroke="#c3c7bd"/>')
        result.append('<text x="-128" y="-53" font-size="12" fill="#818879">1</text>')
        result.append('<text x="-128" y="-148" font-size="12" fill="#818879">2</text>')
        for box in boxes:
            result.append(solid(box, ('#929b8a', '#70796b', '#abb2a2')))
        result.append(rune_mark())
        result.append('</g></g>')
    result += ['<path d="M48,856H1152" stroke="#bfc6b9"/>',
               '<text x="48" y="891" font-size="19" font-weight="bold">Same signature, different masonry</text>',
               '<text x="48" y="920" class="small">Shape + rune identity stay the same across Tuff, Sandstone, Quartz and the other supported finishes.</text>',
               '<text x="48" y="951" class="small">Recipe: attuned shard + Ender Pearl + two matching full masonry blocks. Dimensions use 1/16-block model units.</text>',
               '<text x="48" y="980" class="small">Rune strokes shown here are illustrative; native stones will display the shard’s actual key-derived mark.</text>', '</svg>']
    DESTINATION.mkdir(parents=True, exist_ok=True)
    (DESTINATION / 'upright-silhouettes-v1.svg').write_text('\n'.join(result))
    print(DESTINATION / 'upright-silhouettes-v1.svg')


if __name__ == '__main__':
    main()
