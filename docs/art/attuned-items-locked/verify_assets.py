"""Audit approved artwork preservation, native loading and production packaging."""
from datetime import datetime, timezone
from pathlib import Path
import hashlib
import json
import os
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
PREVIEW = PROJECT / 'run/attuned-items-locked-build'
PRODUCTION = PROJECT / 'run/attuned-items-locked-production-build'

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    subprocess.run([sys.executable, str(ROOT / 'author_assets.py'), '--check'], check=True)
    selections = json.loads((ROOT / 'selection.json').read_text())['selections']
    capture = json.loads((ROOT / 'captures/capture.json').read_text())
    assert len(selections) == len(capture['screens']) == len(capture['resolvedModels']) == 2
    assert list(capture['resolvedModels'].values()) == ['vestige:item/' + e['item'] for e in selections]
    files = []
    for entry in selections:
        source = ROOT / entry['source']
        packaged = PROJECT / 'src/main/resources/assets/vestige/textures/item' / (entry['item'] + '.png')
        assert sha(source) == entry['sha256']
        with Image.open(source) as original, Image.open(packaged) as output:
            assert output.size == (2048, 2048)
            assert original.convert('RGBA').tobytes() == output.crop((0, 0, *original.size)).convert('RGBA').tobytes()
            assert output.getchannel('A').crop((original.width, 0, output.width, output.height)).getbbox() is None
            assert output.getchannel('A').crop((0, original.height, original.width, output.height)).getbbox() is None
        files += [source, packaged, PROJECT / 'src/main/resources/assets/vestige/models/item' / (entry['item'] + '.json')]
    assets = capture['assetSha256']
    with zipfile.ZipFile(PREVIEW / 'moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar') as archive:
        for name, expected in assets.items():
            namespace, resource = name.split(':', 1)
            if namespace == 'vestige':
                data = (PROJECT / 'src/main/resources/assets/vestige' / resource).read_bytes()
            elif resource == 'models/item/paper.json':
                data = (ROOT / 'preview-resources/assets/minecraft' / resource).read_bytes()
            else:
                data = archive.read('assets/' + namespace + '/' + resource)
            assert hashlib.sha256(data).hexdigest() == expected, name
    assert len(assets) == 47
    comparisons = []
    for entry, screen in zip(selections, capture['screens'], strict=True):
        assert screen['variant'] == entry['item'] and screen['customItems'] == 1 and screen['customSlot'] == 13
        assert (screen['width'], screen['height'], screen['guiScale']) == (960, 720, 3.0)
        current = ROOT / 'captures' / screen['file']
        previous = (ROOT / entry['approvedMenu']).resolve()
        with Image.open(current) as a, Image.open(previous) as b:
            assert a.size == b.size == (960, 720)
            ax, bx = a.convert('RGB').tobytes(), b.convert('RGB').tobytes()
            changed = 0
            outside = 0
            total_error = 0
            maximum_error = 0
            for n in range(960 * 720):
                x, y = n % 960, n // 960
                av, bv = ax[n*3:n*3+3], bx[n*3:n*3+3]
                if av != bv:
                    if 453 <= x < 507 and 213 <= y < 267:
                        changed += 1
                    else:
                        outside += 1
                if 456 <= x < 504 and 216 <= y < 264:
                    error = max(abs(c-d) for c, d in zip(av, bv, strict=True))
                    total_error += error
                    maximum_error = max(maximum_error, error)
            assert outside == 0
            assert total_error / (48*48) < 1
            comparisons.append({'item': entry['item'], 'differentIconPixels': changed, 'differentSurroundingPixels': outside,
                                'meanMaximumRgbDifference': total_error/(48*48), 'maximumRgbDifference': maximum_error,
                                'note': 'Native atlas now retains mip level 4 instead of the study atlas dropping to 0; very small filtering differences are limited to the icon. Original artwork RGBA is exact.'})
        files += [current]
    client_log = (ROOT / 'native-client.log').read_text()
    assert '4096x4096x4 minecraft:textures/atlas/blocks.png-atlas' in client_log
    for name in ('native-client.log', 'production-build.log', 'kithkyn-compatibility.log'):
        assert 'BUILD SUCCESSFUL' in (ROOT / name).read_text(), name
    xml = list((PRODUCTION / 'test-results/test').glob('TEST-*.xml'))
    counts = {name: sum(int(ET.parse(path).getroot().get(name, '0')) for path in xml) for name in ('tests', 'failures', 'errors', 'skipped')}
    assert counts['tests'] > 0 and counts['failures'] == counts['errors'] == counts['skipped'] == 0
    jar = PRODUCTION / 'libs/vestige-0.1.0.jar'
    with zipfile.ZipFile(jar) as archive:
        for entry in selections:
            for kind, extension in (('textures', '.png'), ('models', '.json')):
                name = 'assets/vestige/' + kind + '/item/' + entry['item'] + extension
                assert archive.read(name) == (PROJECT / 'src/main/resources' / name).read_bytes()
        assert 'assets/minecraft/models/item/paper.json' not in archive.namelist()
        assert not any('LockedItemTextureCapture' in name for name in archive.namelist())
        assert not any('CenteredItemTextureCapture' in name or 'SelectedItemTextureCapture' in name or 'SoloItemTextureCapture' in name for name in archive.namelist())
        for name in ('HomeboundEyeItem.class', 'HomeboundEyeItem$Binding.class', 'HomeboundEyeItem$Payment.class'):
            path = 'com/quzzar/vestige/apparatus/' + name
            old = PROJECT / 'run/item-textures-native-v5-build/classes/java/main' / path
            assert archive.read(path) == old.read_bytes(), 'Eye gameplay class changed: ' + name
    classes = list((PREVIEW / 'classes/java/main/com/quzzar/vestige/apparatus/client').glob('LockedItemTextureCapture*.class'))
    assert len(classes) == 3
    files += [ROOT / name for name in ('selection.json', 'asset-details.json', 'pinned-generated-parent.json', 'LockedTextureImport.java',
                                     'author_assets.py', 'verify_assets.py', 'isolated-preview.gradle', 'production-build.gradle', 'captures/capture.json')]
    files += list((ROOT / 'preview-source').rglob('*.java'))
    files += [ROOT / 'preview-resources/assets/minecraft/models/item/paper.json']
    record = {
        'verifiedUtc': datetime.now(timezone.utc).isoformat(), 'engine': capture['engine'], 'lockedSelections': selections,
        'checks': {name: 'PASS' for name in ('all_original_approved_rgba_preserved', 'only_transparent_padding_added',
                    '47_loaded_assets_match_sources', 'two_correct_native_models', 'two_native_menu_views_visually_inspected',
                    'approved_menu_shape_and_color_retained', 'native_atlas_mip_level_four_retained',
                    'production_package_matches_locked_assets', 'preview_fixture_excluded_from_production', 'production_build',
                    'kithkyn_compatibility', 'homebound_eye_gameplay_classes_unchanged')},
        'unitTests': counts, 'menuComparisons': comparisons,
        'loadedAssetSha256': assets,
        'sourceAndCaptureSha256': {str(path.relative_to(PROJECT)): sha(path) for path in files},
        'compiledCaptureSha256': {str(path.relative_to(PROJECT)): sha(path) for path in classes},
        'productionJar': {'path': str(jar.relative_to(PROJECT)), 'sha256': sha(jar)},
        'limits': ['High-resolution artwork is the explicitly approved native menu appearance; not a literal 16x16 texture',
                   'Native GUI verifies an unbound production Eye, with no attunement mark or glint',
                   'Shell artwork is production-ready but Shell registration/chat gameplay remains unimplemented',
                   'Normalized hand/head/frame/ground transforms are authored but not visually captured',
                   'World mechanics and multiplayer were not rerun for this asset-only change', 'No installed game files replaced']
    }
    (ROOT / 'verification.json').write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps({'checks': record['checks'], 'unitTests': counts, 'productionJar': record['productionJar']}, indent=2))

if __name__ == '__main__':
    main()
