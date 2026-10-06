"""Verify unchanged generated concepts and the eight completed native context views."""
from datetime import datetime, timezone
from pathlib import Path
import hashlib
import json
import subprocess
import sys
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
BUILD = PROJECT / 'run/item-textures-native-v4-build'

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    subprocess.run([sys.executable, str(ROOT / 'prepare_review.py'), '--check'], check=True)
    entries = json.loads((ROOT / 'concept-files.json').read_text())
    capture = json.loads((ROOT / 'captures/capture.json').read_text())
    assert len(entries) == len(capture['screens']) == 8
    assert len(capture['resolvedModels']) == len(set(capture['resolvedModels'].values())) == 8
    assert list(capture['resolvedModels'].values()) == ['vestige:item/' + e['id'] for e in entries]
    for entry in entries:
        original = ROOT / entry['destination']
        packaged = ROOT / 'preview-resources/assets/vestige/textures/item' / (entry['id'] + '.png')
        assert original.read_bytes() == packaged.read_bytes()
        assert sha(original) == entry['sha256']
    assets = capture['assetSha256']
    with zipfile.ZipFile(BUILD / 'moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar') as archive:
        for name, expected in assets.items():
            namespace, resource = name.split(':', 1)
            if namespace == 'vestige' or resource == 'models/item/paper.json':
                data = (ROOT / 'preview-resources/assets' / namespace / resource).read_bytes()
            else:
                data = archive.read('assets/' + namespace + '/' + resource)
            assert hashlib.sha256(data).hexdigest() == expected, name
    assert len(assets) == 59
    assert capture['worldOpened'] is False
    assert len(capture['neighbors']) == 27
    assert all(item.startswith('minecraft:') for item in capture['neighbors'] + capture['inventory'] + capture['hotbar'])
    assert capture['neighbors'][13] == 'minecraft:paper'  # Replaced by the sole review carrier.
    pixels = []
    for entry, screen in zip(entries, capture['screens'], strict=True):
        assert screen['variant'] == entry['id']
        assert screen['customItems'] == 1 and screen['customSlot'] == 13
        assert (screen['width'], screen['height'], screen['guiScale']) == (960, 720, 3.0)
        with Image.open(ROOT / 'captures' / screen['file']) as im:
            assert im.size == (960, 720)
            pixels.append(list(im.convert('RGB').getdata()))
    # All vanilla surroundings must remain byte-identical across all eight views.
    # Only the ordinary title and the central custom slot may vary.
    allowed = lambda x, y: (237 <= x < 729 and 123 <= y < 153) or (453 <= x < 507 and 213 <= y < 267)
    outside = [i for i in range(960 * 720) if not allowed(i % 960, i // 960)]
    for i in range(1, len(pixels)):
        assert all(pixels[i][j] == pixels[0][j] for j in outside), capture['screens'][i]['file']
    screenshots = [ROOT / 'captures' / s['file'] for s in capture['screens']]
    assert len({sha(path) for path in screenshots}) == 8
    assert 'BUILD SUCCESSFUL' in (ROOT / 'native-client.log').read_text()
    classes = list((BUILD / 'classes/java/main/com/quzzar/vestige/apparatus/client').glob('SoloItemTextureCapture*.class'))
    assert len(classes) == 3
    files = [ROOT / name for name in ('concept-files.json', 'imagegen-prompts.json', 'preview-details.json', 'prepare_review.py', 'verify_review.py', 'isolated-preview.gradle', 'captures/capture.json')]
    files += list((ROOT / 'preview-source').rglob('*.java')) + screenshots
    record = {
        'verifiedUtc': datetime.now(timezone.utc).isoformat(),
        'engine': capture['engine'],
        'checks': {name: 'PASS' for name in ('eight_original_generated_pngs_and_alpha_preserved', 'eight_distinct_native_model_resolutions',
                    '59_loaded_assets_match_originals_and_pinned_vanilla', 'one_custom_item_per_native_menu',
                    'vanilla_surroundings_pixel_identical_across_all_eight_views', 'eight_native_views_visually_inspected', 'isolated_native_client_compile_and_capture')},
        'screens': capture['screens'],
        'sourceAndCaptureSha256': {str(path.relative_to(PROJECT)): sha(path) for path in files},
        'compiledReviewSha256': {str(path.relative_to(PROJECT)): sha(path) for path in classes},
        'loadedAssetSha256': assets,
        'limits': ['Generated concepts are high-resolution studies, not final literal 16x16 textures',
                   'Final texture selection and refinement remain open', 'No gameplay changes or new unit/world test claims',
                   'Held/offhand, binding marks and Shell gameplay not reviewed', 'No installed game files replaced']
    }
    (ROOT / 'verification.json').write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps({'checks': record['checks'], 'screens': len(screenshots)}, indent=2))

if __name__ == '__main__':
    main()
