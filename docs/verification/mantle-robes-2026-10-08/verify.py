"""Freeze the actual native robe inspection and exact packaged resources."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
BUILD = ROOT / 'run/robes-mantles-review-build'
ART = ROOT / 'docs/art/magic-equipment-mantles'


def sha(data):
    return hashlib.sha256(data).hexdigest()


capture = json.loads((ART / 'captures/capture.json').read_text())
assert len(capture['checks']) == 84
assert len(list((ART / 'captures').glob('*.png'))) == 84
assert len([c for c in capture['checks'] if '-world-' in c['view']]) == 18
jar = next(p for p in (BUILD / 'libs').glob('*.jar') if not p.name.endswith(('-sources.jar', '-javadoc.jar')))
assets = []
with ZipFile(jar) as packaged:
    for name in ('wardweave', 'cinderweave'):
        layers = []
        for suffix in ('', '_overlay'):
            rel = f'assets/vestige/textures/models/armor/{name}_layer_1{suffix}.png'
            source = (ROOT / 'src/main/resources' / rel).read_bytes()
            assert source == (BUILD / 'resources/main' / rel).read_bytes() == packaged.read(rel)
            image = Image.open(ROOT / 'src/main/resources' / rel).convert('RGBA')
            assert image.size == (64, 64)
            assert set(image.getchannel('A').get_flattened_data()) <= {0, 255}
            layers.append(image)
            assets.append({'path': rel, 'sha256': sha(source), 'size': list(image.size), 'hardAlpha': True})
        fabric, trim = layers
        assert all(p[0] == p[1] == p[2] for p in fabric.get_flattened_data() if p[3])
        assert not any(a[3] and b[3] for a, b in zip(fabric.get_flattened_data(), trim.get_flattened_data()))
    for name in ('RobeModel', 'RobeModels', 'RobeModels$1', 'MagicEquipmentCapture', 'MagicEquipmentCapture$Review'):
        path = f'com/quzzar/vestige/equipment/client/{name}.class'
        assert packaged.read(path) == (BUILD / 'classes/java/main' / path).read_bytes()
    assert b'mantle_back' in packaged.read('com/quzzar/vestige/equipment/client/RobeModel.class')
    assert b'prepare' in packaged.read('com/quzzar/vestige/equipment/client/RobeModels$1.class')
tests = [ET.parse(p).getroot() for p in (BUILD / 'test-results/test').glob('TEST-*.xml')]
counts = {k: sum(int(t.attrib[k]) for t in tests) for k in ('tests', 'failures', 'errors', 'skipped')}
assert counts['tests'] > 0 and counts['failures'] == counts['errors'] == counts['skipped'] == 0
evidence = {
    'date': '2026-10-08', 'artApproval': 'pending owner visual review',
    'engine': capture['engine'], 'unitTests': counts,
    'unitTestScope': 'Full isolated Gradle build; final capture-angle refinements compiled/packaged by capture_client.py.',
    'nativeCaptures': {'total': 84, 'dyedInventoryAndPoses': 64, 'ironArmorLayering': 2, 'thirdPersonWorld': 18,
                       'bodyTypes': ['WIDE', 'SLIM'], 'poses': ['front', 'side', 'back', 'walking', 'crouching']},
    'wornAssets': assets, 'packagedJar': str(jar.relative_to(ROOT)), 'packagedJarSha256': sha(jar.read_bytes()),
    'captureHashes': {p.name: sha(p.read_bytes()) for p in sorted((ART / 'captures').glob('*.png'))},
    'sourceHashes': {str(p.relative_to(ROOT)): sha(p.read_bytes()) for p in [
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/RobeModel.java',
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/RobeModels.java',
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/MagicEquipmentCapture.java',
        ROOT / 'tools/author_robe_armor.py', ART / 'wardweave-generated.png', ART / 'cinderweave-generated.png', ART / 'prompts.json']},
    'limits': ['Visual candidate awaiting owner review.', 'Cape uses bounded pose/stride movement, not simulated cloth physics.',
               'Riding, swimming, flight and other mods\' back-slot equipment remain additional visual playtests.',
               'No gameplay changes; no combat world-test rerun or installed pack changes in this art pass.']
}
(Path(__file__).parent / 'verification.json').write_text(json.dumps(evidence, indent=2) + '\n')
print(f"Verified {counts['tests']} unit tests, 84 native captures and exact packaged worn textures.")
