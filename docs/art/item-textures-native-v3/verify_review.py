"""Verify the completed native art review and packaged Echo Shard craft."""
from pathlib import Path
from collections import deque
from datetime import datetime, timezone
import hashlib
import json
import xml.etree.ElementTree as ET
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
PREVIEW_BUILD = PROJECT / 'run/item-textures-native-v3-build'
PRODUCTION_BUILD = PROJECT / 'run/echo-shard-production-build'

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    sources = json.loads((ROOT / 'texture-sources.json').read_text())
    details = json.loads((ROOT / 'texture-details.json').read_text())
    for variant, detail in zip(sources['variants'], details, strict=True):
        path = ROOT / 'textures' / (variant['id'] + '.png')
        assert sha(path) == detail['sha256']
        assert path.read_bytes() == (ROOT / 'preview-resources/assets/vestige/textures/item' / path.name).read_bytes()
        palette = {key: tuple(bytes.fromhex(color[1:])) + (255,) for key, color in sources['palettes'][variant['palette']].items()}
        with Image.open(path) as image:
            assert image.size == (16, 16) and image.mode == 'RGBA'
            pixels = [image.getpixel((x, y)) for y in range(16) for x in range(16)]
        assert pixels == [(0, 0, 0, 0) if key == '.' else palette[key] for row in variant['rows'] for key in row]
        assert {p[3] for p in pixels} == {0, 255}
        cells = {(x, y) for y in range(16) for x in range(16) if pixels[y * 16 + x][3]}
        assert all(0 < x < 15 and 0 < y < 15 for x, y in cells)
        reached = {next(iter(cells))}
        pending = deque(reached)
        while pending:
            x, y = pending.popleft()
            for neighbor in ((x-1, y), (x+1, y), (x, y-1), (x, y+1)):
                if neighbor in cells and neighbor not in reached:
                    reached.add(neighbor)
                    pending.append(neighbor)
        assert reached == cells

    capture = json.loads((ROOT / 'captures/capture.json').read_text())
    assets = capture['assetSha256']
    with zipfile.ZipFile(PREVIEW_BUILD / 'moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar') as archive:
        for name, expected in assets.items():
            namespace, name_in_pack = name.split(':', 1)
            if namespace == 'vestige' or name_in_pack == 'models/item/paper.json':
                data = (ROOT / 'preview-resources/assets' / namespace / name_in_pack).read_bytes()
            else:
                data = archive.read('assets/' + namespace + '/' + name_in_pack)
            assert hashlib.sha256(data).hexdigest() == expected, name
    assert len(assets) == 44
    models = capture['resolvedModels']
    assert len(models) == len(set(models.values())) == 18
    assert list(models.values()) == ['vestige:item/' + name for name in capture['variantOrder']]
    assert len(capture['screens']) == 6
    for name in capture['screens']:
        with Image.open(ROOT / 'captures' / name) as image:
            assert image.size == (1280, 960)
    assert len({sha(ROOT / 'captures' / name) for name in capture['screens']}) == 6

    originals = {
        'Eye': PROJECT / 'src/main/resources/assets/vestige/textures/item/homebound_eye.png',
        'Shell': PROJECT / 'docs/art/whispering-shell-native-v1/whispering-shell.png'
    }
    for name, source in originals.items():
        reference = ROOT / 'references' / ('current-homebound-eye.png' if name == 'Eye' else 'current-whispering-shell.png')
        assert reference.read_bytes() == source.read_bytes()

    for name in ('native-client.log', 'native-recipe-check.log', 'production-build.log'):
        assert 'BUILD SUCCESSFUL' in (ROOT / name).read_text(), name
    assert 'All 1 required tests passed' in (ROOT / 'native-recipe-check.log').read_text()
    results = list((PRODUCTION_BUILD / 'test-results/test').glob('TEST-*.xml'))
    assert results
    counts = {name: sum(int(ET.parse(path).getroot().get(name, '0')) for path in results) for name in ('tests', 'failures', 'errors', 'skipped')}
    assert counts['tests'] > 0 and counts['failures'] == counts['errors'] == counts['skipped'] == 0
    jar = PRODUCTION_BUILD / 'libs/vestige-0.1.0.jar'
    package_files = ['data/vestige/recipe/echo_shard.json', 'data/vestige/advancement/recipes/echo_shard.json',
                     'assets/vestige/textures/item/homebound_eye.png']
    with zipfile.ZipFile(jar) as archive:
        for name in package_files:
            assert archive.read(name) == (PROJECT / 'src/main/resources' / name).read_bytes(), name
        assert not any(name.endswith('/ItemTextureStudiesCapture.class') or name.endswith('/EchoShardCraftReview.class')
                       or name == 'assets/minecraft/models/item/paper.json' for name in archive.namelist())
        assert not any('assets/vestige/textures/item/' + v['id'] + '.png' in archive.namelist() for v in sources['variants'])

    files = [ROOT / 'texture-sources.json', ROOT / 'author_textures.py', ROOT / 'verify_review.py',
             ROOT / 'isolated-preview.gradle', ROOT / 'production-build.gradle', ROOT / 'captures/capture.json']
    files += list((ROOT / 'preview-source').rglob('*.java'))
    files += [ROOT / 'captures' / name for name in capture['screens']]
    files += [PROJECT / 'src/main/resources' / name for name in package_files]
    classes = list((PREVIEW_BUILD / 'classes/java/main/com/quzzar/vestige/apparatus/client').glob('ItemTextureStudiesCapture*.class'))
    classes += list((PREVIEW_BUILD / 'classes/java/main/com/quzzar/vestige/gametest').glob('EchoShardCraftReview.class'))
    assert len(classes) == 5
    record = {
        'verified_utc': datetime.now(timezone.utc).isoformat(),
        'engine': capture['engine'],
        'checks': {name: 'PASS' for name in ['source_grid_export', 'binary_alpha_full_border_connected_silhouettes',
                    '44_loaded_assets_match_sources', '18_native_models_match', 'six_native_views_visually_inspected',
                    'one_focused_minecraft_craft_check', 'production_build', 'original_textures_preserved', 'preview_assets_excluded_from_production']},
        'unit_tests': counts,
        'variants': details,
        'asset_sha256': assets,
        'source_capture_sha256': {str(path.relative_to(PROJECT)): sha(path) for path in files},
        'compiled_review_sha256': {str(path.relative_to(PROJECT)): sha(path) for path in classes},
        'production_jar': {'path': str(jar.relative_to(PROJECT)), 'sha256': sha(jar)},
        'recipe_verified': ['six unordered ingredient permutations', 'extra ingredient rejected', 'Pearl recipe rejected',
                            '2x2 InventoryMenu collection consumes one of each', '3x3 CraftingMenu collection consumes one of each',
                            'fresh player Ghast Tear advancement and recipe-book unlock before crafting'],
        'limits': ['Art options await selection; production Eye unchanged', 'Appearance-only carriers omit attunement marks and bound-item glint',
                   'Held/offhand effects and Shell gameplay unverified by this study', 'Full world suite and multiplayer not rerun',
                   'No installed game files replaced']
    }
    (ROOT / 'verification.json').write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps({'checks': record['checks'], 'unit_tests': counts, 'production_jar': record['production_jar']}, indent=2))

if __name__ == '__main__':
    main()
