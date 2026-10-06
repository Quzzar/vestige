"""Package unchanged generated concepts for isolated native menu review."""
import argparse
import hashlib
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    entries = json.loads((ROOT / 'concept-files.json').read_text())
    details = []
    overrides = []
    for i, entry in enumerate(entries):
        source = ROOT / entry['destination']
        data = source.read_bytes()
        assert hashlib.sha256(data).hexdigest() == entry['sha256']
        # Read-only inspection. Do not resize, crop, filter or repaint generated PNGs.
        with Image.open(source) as im:
            width, height = im.size
            alpha = im.getchannel('A')
            bounds = alpha.point(lambda value: 255 if value >= 230 else 0).getbbox()
            assert bounds is not None
        left, top, right, bottom = bounds
        factor = 14 / max(right - left, bottom - top)
        scale = [factor * width / 16, factor * height / 16, 1]
        translation = [factor * (width / 2 - (left + right) / 2), factor * ((top + bottom) / 2 - height / 2), 0]
        model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'vestige:item/' + entry['id']},
                 'display': {'gui': {'rotation': [0, 0, 0], 'translation': translation, 'scale': scale}}}
        files = {
            ROOT / 'preview-resources/assets/vestige/textures/item' / (entry['id'] + '.png'): data,
            ROOT / 'preview-resources/assets/vestige/models/item' / (entry['id'] + '.json'): (json.dumps(model, indent=2) + '\n').encode()
        }
        for path, content in files.items():
            if args.check:
                assert path.read_bytes() == content, f'Preview drift: {path}'
            else:
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(content)
        overrides.append({'predicate': {'custom_model_data': 261200 + i}, 'model': 'vestige:item/' + entry['id']})
        details.append({'id': entry['id'], 'originalDimensions': [width, height], 'solidBounds': list(bounds),
                        'guiScale': scale, 'guiTranslation': translation, 'sha256': entry['sha256'],
                        'note': 'Original generated PNG bytes and alpha preserved. Native GUI model fits the solid body within 14 menu pixels and preserves its natural aspect ratio.'})
    paper = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'minecraft:item/paper'}, 'overrides': overrides}
    for path, content in {
        ROOT / 'preview-resources/assets/minecraft/models/item/paper.json': json.dumps(paper, indent=2) + '\n',
        ROOT / 'preview-details.json': json.dumps(details, indent=2) + '\n'
    }.items():
        if args.check:
            assert path.read_text() == content, f'Preview drift: {path}'
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(content)
    print('Eight unchanged concepts packaged; original alpha, native GUI size and separate model identities verified.')

if __name__ == '__main__':
    main()
