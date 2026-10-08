"""Package locked art without altering any approved pixels, alpha, shapes or colors."""
import argparse
import hashlib
import json
import math
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]

def rotate(vector, degrees):
    # ItemTransform uses Quaternionf.rotationXYZ: rotate Z, then Y, then X.
    x, y, z = vector
    rx, ry, rz = map(math.radians, degrees)
    x, y = math.cos(rz)*x - math.sin(rz)*y, math.sin(rz)*x + math.cos(rz)*y
    x, z = math.cos(ry)*x + math.sin(ry)*z, -math.sin(ry)*x + math.cos(ry)*z
    y, z = math.cos(rx)*y - math.sin(rx)*z, math.sin(rx)*y + math.cos(rx)*z
    return [x, y, z]

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    selection = json.loads((ROOT / 'selection.json').read_text())
    parent = json.loads((ROOT / 'pinned-generated-parent.json').read_text())
    pairs = []
    records = []
    for entry in selection['selections']:
        source = ROOT / entry['source']
        assert hashlib.sha256(source.read_bytes()).hexdigest() == entry['sha256']
        target = PROJECT / 'src/main/resources/assets/vestige/textures/item' / (entry['item'] + '.png')
        pairs += [str(source), str(target)]
        size = 1
        while size < max(entry['dimensions']):
            size *= 2
        left, top, right, bottom = entry['solidBounds']
        factor = 14 / max(right - left, bottom - top)
        normalized_scale = [factor * size / 16, factor * size / 16, 1]
        offset = [factor * (size / 2 - (left + right) / 2), factor * ((top + bottom) / 2 - size / 2), 0]
        display = {'gui': {'rotation': [0, 0, 0], 'translation': offset, 'scale': normalized_scale}}
        contexts = dict(parent['display'])
        contexts['firstperson_lefthand'] = contexts['firstperson_righthand']
        contexts['thirdperson_lefthand'] = contexts['thirdperson_righthand']
        for context, transform in contexts.items():
            rotation = transform.get('rotation', [0, 0, 0])
            scale = transform.get('scale', [1, 1, 1])
            translation = transform.get('translation', [0, 0, 0])
            # ItemTransform mirrors X translation and Y/Z rotation in the left hand.
            # Compose normalization in that effective frame, then store pre-mirror values.
            left_hand = context.endswith('_lefthand')
            effective_rotation = [rotation[0], -rotation[1], -rotation[2]] if left_hand else rotation
            effective_translation = [-translation[0], translation[1], translation[2]] if left_hand else translation
            delta = rotate([a*b for a, b in zip(offset, scale, strict=True)], effective_rotation)
            result_translation = [a+b for a, b in zip(effective_translation, delta, strict=True)]
            if left_hand:
                result_translation[0] *= -1
            display[context] = {'rotation': rotation, 'translation': result_translation,
                                'scale': [a*b for a, b in zip(scale, normalized_scale, strict=True)]}
        model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'vestige:item/' + entry['item']}, 'display': display}
        target_model = PROJECT / 'src/main/resources/assets/vestige/models/item' / (entry['item'] + '.json')
        content = json.dumps(model, indent=2) + '\n'
        if args.check:
            assert target_model.read_text() == content, 'Model drift: ' + str(target_model)
        else:
            target_model.parent.mkdir(parents=True, exist_ok=True)
            target_model.write_text(content)
        records.append({'item': entry['item'], 'variant': entry['variant'], 'approvedSha256': entry['sha256'],
                        'atlasDimensions': [size, size], 'gui': display['gui'], 'normalization': '14 native GUI pixels; parent ground/head/hand/frame transforms composed with the same artwork normalization'})
    java_home = os.environ.get('JAVA_HOME')
    java = str(Path(java_home) / 'bin/java') if java_home else 'java'
    subprocess.run([java, str(ROOT / 'LockedTextureImport.java'), '--check' if args.check else '--write', *pairs], check=True)
    for entry in records:
        entry['textureSha256'] = hashlib.sha256((PROJECT / 'src/main/resources/assets/vestige/textures/item' / (entry['item'] + '.png')).read_bytes()).hexdigest()
        entry['modelSha256'] = hashlib.sha256((PROJECT / 'src/main/resources/assets/vestige/models/item' / (entry['item'] + '.json')).read_bytes()).hexdigest()
    content = json.dumps(records, indent=2) + '\n'
    if args.check:
        assert (ROOT / 'asset-details.json').read_text() == content
    else:
        (ROOT / 'asset-details.json').write_text(content)
    print('Locked artwork packaged without resampling, recoloring, redrawing or changing original alpha.')

if __name__ == '__main__':
    main()
