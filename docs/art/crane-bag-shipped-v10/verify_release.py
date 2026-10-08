"""Verify the approved source, production package and real-item Minecraft capture."""
from datetime import datetime, timezone
from pathlib import Path
import argparse
import hashlib
import json
import xml.etree.ElementTree as ET
import zipfile
from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
BUILD = PROJECT / 'run/crane-bag-shipped-build'
PREVIEW = PROJECT / 'run/crane-bag-shipped-preview-build'
ASSETS = ('assets/vestige/models/item/crane_bag.json', 'assets/vestige/textures/item/crane_bag.png')


def sha(data):
    return hashlib.sha256(data).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--visually-inspected', action='store_true')
    parser.add_argument('--installed', action='store_true')
    args = parser.parse_args()
    assert args.visually_inspected, 'Inspect the real-item native screenshot first'
    selection = json.loads((ROOT / 'selection.json').read_text())
    approved = PROJECT / selection['approvedSource']
    production = PROJECT / selection['productionTexture']
    assert approved.read_bytes() == production.read_bytes()
    assert sha(production.read_bytes()) == selection['sha256']
    sprite = Image.open(production).convert('RGBA')
    assert sprite.size == (16, 16)
    pixels = list(sprite.get_flattened_data())
    assert {pixel[3] for pixel in pixels} == {0, 255}
    assert len({pixel for pixel in pixels if pixel[3]}) == 10
    assert sprite.getbbox() == (2, 2, 15, 15)
    assert sum(bool(pixel[3]) for pixel in pixels) == 105
    model = json.loads((PROJECT / 'src/main/resources' / ASSETS[0]).read_text())
    assert model == {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'vestige:item/crane_bag'}}
    frozen = json.loads((ROOT / 'frozen-input.json').read_text())
    snapshot = Path(frozen['directory'])
    for name, expected in frozen['files'].items():
        assert sha((snapshot / name).read_bytes()) == expected, name
        if name.startswith('src/main/templates/'):
            assert sha((PROJECT / name).read_bytes()) == expected, name
    for name in ASSETS:
        assert (snapshot / 'src/main/resources' / name).read_bytes() == (PROJECT / 'src/main/resources' / name).read_bytes()
    build_log = (ROOT / 'production-build.log').read_text()
    assert 'BUILD SUCCESSFUL' in build_log
    assert ':verifyKithkynCompatibility' in build_log
    assert 'BUILD SUCCESSFUL' in (ROOT / 'native-client.log').read_text()
    totals = {name: 0 for name in ('tests', 'failures', 'errors', 'skipped')}
    results = list((BUILD / 'test-results/test').glob('TEST-*.xml'))
    assert results
    for file in results:
        suite = ET.parse(file).getroot()
        for name in totals:
            totals[name] += int(suite.get(name, '0'))
    assert totals['tests'] > 0 and all(totals[name] == 0 for name in ('failures', 'errors', 'skipped'))
    jar = BUILD / 'libs/vestige-0.1.0.jar'
    with zipfile.ZipFile(jar) as packaged:
        assert packaged.testzip() is None
        for name in ASSETS:
            assert packaged.read(name) == (PROJECT / 'src/main/resources' / name).read_bytes()
        assert not any('NativeCraneBagFinalCapture' in name for name in packaged.namelist())
        assert 'assets/minecraft/models/item/paper.json' not in packaged.namelist()
    capture = json.loads((ROOT / 'captures/capture.json').read_text())
    assert capture['worldOpened'] is False
    assert capture['nativeSpriteDimensions'] == [16, 16]
    assert capture['resolvedModels'] == {'Crane Bag': 'vestige:item/crane_bag'}
    assert len(capture['screens']) == 1
    screen = capture['screens'][0]
    assert (screen['customItems'], screen['customSlot'], screen['width'], screen['height'], screen['guiScale']) == (1, 13, 960, 720, 3.0)
    assert capture['neighbors'][13] == 'vestige:crane_bag'
    assert sum(name.startswith('vestige:') for name in capture['neighbors'] + capture['inventory'] + capture['hotbar']) == 1
    assets = capture['assetSha256']
    assert len(assets) == 44
    with zipfile.ZipFile(PREVIEW / 'moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar') as vanilla:
        for name, expected in assets.items():
            namespace, resource = name.split(':', 1)
            path = 'assets/' + namespace + '/' + resource
            data = (snapshot / 'src/main/resources' / path).read_bytes() if namespace == 'vestige' else vanilla.read(path)
            assert sha(data) == expected, name
    screenshot_path = ROOT / 'captures' / screen['file']
    screenshot = Image.open(screenshot_path).convert('RGB')
    assert screenshot.size == (960, 720)
    verified_cells = 0
    for y in range(16):
        for x in range(16):
            color = sprite.getpixel((x, y))
            if color[3]:
                block = screenshot.crop((456 + x*3, 216 + y*3, 459 + x*3, 219 + y*3))
                assert set(block.get_flattened_data()) == {color[:3]}, (x, y)
                verified_cells += 1
    assert verified_cells == 105
    old = Image.open(PROJECT / 'docs/art/crane-bag-native-variants-v9/captures/bag-a.png').convert('RGB')
    assert screenshot.crop((453, 213, 507, 267)).tobytes() == old.crop((453, 213, 507, 267)).tobytes()
    now_bytes, old_bytes = screenshot.tobytes(), old.tobytes()
    for index in range(960*720):
        x, y = index % 960, index // 960
        if not (237 <= x < 729 and 123 <= y < 153):
            assert now_bytes[index*3:index*3+3] == old_bytes[index*3:index*3+3], (x, y)
    class_path = 'com/quzzar/vestige/storage/CraneBagItem.class'
    assert (BUILD / 'classes/java/main' / class_path).read_bytes() == (PREVIEW / 'classes/java/main' / class_path).read_bytes()
    checks = {name: 'PASS' for name in (
        'approved_A_source_identical_to_production_16x16_png',
        'ten_colors_binary_alpha_standard_generated_model',
        'frozen_inputs_and_metadata_templates_verified',
        'production_build_and_Kithkyn_compatibility', 'all_snapshot_unit_tests',
        'production_jar_contains_approved_assets_without_review_overrides',
        'real_registered_Crane_Bag_resolves_production_texture',
        '44_loaded_asset_hashes_match_production_or_pinned_vanilla',
        '105_opaque_cells_match_exact_3x3_framebuffer_blocks',
        'real_item_pixels_equal_approved_A_review_pixels',
        'unchanged_vanilla_inventory_surroundings', 'native_framebuffer_visually_inspected')}
    installation = None
    if args.installed:
        installation = json.loads((ROOT / 'prism-install.json').read_text())
        assert installation['installed']
        target = Path(installation['installedJar'])
        assert sha(target.read_bytes()) == installation['installedSha256']
        assert sha(Path(installation['backupJar']).read_bytes()) == installation['baseSha256']
        with zipfile.ZipFile(installation['baseJar']) as base, zipfile.ZipFile(target) as installed:
            assert installed.testzip() is None
            assert set(installed.namelist()) == set(base.namelist()) | set(ASSETS)
            for name in base.namelist():
                if name not in ASSETS:
                    assert base.read(name) == installed.read(name), name
            for name in ASSETS:
                assert installed.read(name) == (PROJECT / 'src/main/resources' / name).read_bytes()
        checks['installed_two_approved_assets_and_preserved_all_other_members'] = 'PASS'
        checks['backup_and_installed_hash_verified'] = 'PASS'
    files = [approved, production, screenshot_path, ROOT / 'captures/capture.json', ROOT / 'selection.json', ROOT / 'frozen-input.json', ROOT / 'production-build.gradle', ROOT / 'preview.gradle', ROOT / 'verify_release.py', ROOT / 'install_approved_art.py', ROOT / 'preview-source/com/quzzar/vestige/apparatus/client/NativeCraneBagFinalCapture.java']
    record = {
        'verifiedUtc': datetime.now(timezone.utc).isoformat(), 'engine': capture['engine'],
        'selected': selection['selected'], 'checks': checks,
        'unitTests': totals, 'nativeScreens': capture['screens'],
        'opaqueCellsVerified': verified_cells, 'loadedAssetSha256': assets,
        'sourceAndCaptureSha256': {str(file.relative_to(PROJECT)): sha(file.read_bytes()) for file in files},
        'productionJar': str(jar), 'productionJarSha256': sha(jar.read_bytes()),
        'installation': installation,
        'limits': ['Only model and texture shipped; recipe remains pending.',
                   'Full shared-checkout build is verification evidence, not the installed release.',
                   'Unbound real item used for inventory appearance; live storage and held/offhand not rechecked.',
                   'No new gameplay changes or world-test claim. Existing game instance was not restarted.'],
        'resolvedFailures': ['Invalid metadata CopySpec API in isolated scripts corrected; original setup logs retained.',
                             'Snapshot frozen during another chat edit needed the already corrected offeringSeats lambda fix; frozen repair provenance retained. No runtime classes installed.'],
    }
    (ROOT / 'verification.json').write_text(json.dumps(record, indent=2) + '\n')
    print(json.dumps({'checks': checks, 'unitTests': totals, 'productionJarSha256': record['productionJarSha256']}, indent=2))


if __name__ == '__main__':
    main()
