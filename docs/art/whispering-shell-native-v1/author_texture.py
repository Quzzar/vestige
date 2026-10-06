"""Bake an independently authored literal native pixel grid; never resample concept art."""
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
    source = json.loads((ROOT / 'texture-source.json').read_text())
    rows, palette = source['rows'], source['palette']
    assert len(rows) == 16 and all(len(row) == 16 for row in rows)
    colors = {key: tuple(bytes.fromhex(value[1:])) + (255,) for key, value in palette.items()}
    pixels = [(0, 0, 0, 0) if key == '.' else colors[key] for row in rows for key in row]
    image = Image.new('RGBA', (16, 16))
    image.putdata(pixels)
    stream = io.BytesIO()
    image.save(stream, format='PNG')
    output = stream.getvalue()
    destinations = [ROOT / 'whispering-shell.png', ROOT / 'preview-resources/assets/vestige/textures/item/whispering_shell_preview.png']
    for destination in destinations:
        if args.check:
            assert destination.read_bytes() == output, f'Texture drift: {destination}'
        else:
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(output)
    assert {p[3] for p in pixels} == {0, 255}
    assert len({p[:3] for p in pixels if p[3]}) <= 8
    print(json.dumps({'size': [16, 16], 'opaque_pixels': sum(p[3] == 255 for p in pixels), 'opaque_colors': len({p[:3] for p in pixels if p[3]}), 'sha256': hashlib.sha256(output).hexdigest()}))

if __name__ == '__main__':
    main()
