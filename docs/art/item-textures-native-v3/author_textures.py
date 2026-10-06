"""Bake original literal 16x16 study grids; never resize or edit generated concept images."""
import argparse
import hashlib
import io
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    sources = json.loads((ROOT / 'texture-sources.json').read_text())
    results = []
    for entry in sources['variants']:
        rows = entry['rows']
        assert len(rows) == 16 and all(len(row) == 16 for row in rows), entry['id']
        assert rows[0] == rows[-1] == '.' * 16
        assert all(row[0] == row[-1] == '.' for row in rows)
        palette = sources['palettes'][entry['palette']]
        colors = {key: tuple(bytes.fromhex(value[1:])) + (255,) for key, value in palette.items()}
        pixels = [(0, 0, 0, 0) if key == '.' else colors[key] for row in rows for key in row]
        image = Image.new('RGBA', (16, 16))
        image.putdata(pixels)
        output = io.BytesIO()
        image.save(output, format='PNG')
        data = output.getvalue()
        for path in [ROOT / 'textures' / (entry['id'] + '.png'), ROOT / 'preview-resources/assets/vestige/textures/item' / (entry['id'] + '.png')]:
            if args.check:
                assert path.read_bytes() == data, f'Texture drift: {path}'
            else:
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(data)
        used = len({p[:3] for p in pixels if p[3]})
        assert {p[3] for p in pixels} == {0, 255}
        assert used <= 16
        results.append({'id': entry['id'], 'size': [16, 16], 'bounds': list(image.getbbox()), 'opaque_colors': used, 'opaque_pixels': sum(p[3] == 255 for p in pixels), 'sha256': hashlib.sha256(data).hexdigest()})
    if not args.check:
        (ROOT / 'texture-details.json').write_text(json.dumps(results, indent=2) + '\n')
    print(json.dumps(results))

if __name__ == '__main__':
    main()
