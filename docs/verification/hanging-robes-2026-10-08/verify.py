"""Freeze the final native capture's resource and packaging evidence."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
BUILD = Path('/private/tmp/vestige-robes-hanging-review-build-2026-10-08')
ART = ROOT / 'docs/art/magic-equipment-hanging'


def sha(data):
    return hashlib.sha256(data).hexdigest()


capture = json.loads((ART / 'captures/capture.json').read_text())
assert len(capture['checks']) == 72
assert len(list((ART / 'captures').glob('*.png'))) == 72
jar = next(p for p in (BUILD / 'libs').glob('*.jar') if not p.name.endswith(('-sources.jar', '-javadoc.jar')))
assets = []
with ZipFile(jar) as packaged:
    for name in ('wardweave', 'cinderweave'):
        for suffix in ('', '_overlay'):
            rel = f'assets/vestige/textures/models/armor/{name}_layer_1{suffix}.png'
            source = (ROOT / 'src/main/resources' / rel).read_bytes()
            assert source == (BUILD / 'resources/main' / rel).read_bytes() == packaged.read(rel)
            with Image.open(ROOT / 'src/main/resources' / rel) as image:
                assert image.mode == 'RGBA' and image.size == (64, 64)
                assert set(image.getchannel('A').getdata()) <= {0, 255}
                assets.append({'path': rel, 'sha256': sha(source), 'size': list(image.size), 'hardAlpha': True})
    model = packaged.read('com/quzzar/vestige/equipment/client/RobeModel.class')
    assert b'edge' in model and b'rim' in model and b'copySign' in model and b'showHem' not in model
    assert model == (BUILD / 'classes/java/main/com/quzzar/vestige/equipment/client/RobeModel.class').read_bytes()
    assert b'countRenderedSections' in packaged.read('com/quzzar/vestige/equipment/client/MagicEquipmentCapture.class')
tests = [ET.parse(p).getroot() for p in (BUILD / 'test-results/test').glob('TEST-*.xml')]
counts = {k: sum(int(t.attrib[k]) for t in tests) for k in ('tests', 'failures', 'errors', 'skipped')}
assert counts['tests'] > 0 and counts['failures'] == counts['errors'] == counts['skipped'] == 0
evidence = {
    'date': '2026-10-08', 'artApproval': 'pending owner visual review',
    'engine': capture['engine'], 'unitTests': counts,
    'unitTestScope': 'Full Gradle pass before final client-only hip-fold/preview-label refinements; gameplay unchanged.',
    'finalClientCompilation': 'Java 21 javac and jar using the prepared pinned Gradle runtime; final_client.py',
    'nativeCaptures': {'total': 72, 'dyedInventoryAndPoses': 64, 'ironArmorLayering': 2, 'thirdPersonWorld': 6,
                       'bodyTypes': ['WIDE', 'SLIM'], 'poses': ['front', 'side', 'back', 'walking', 'crouching']},
    'wornAssets': assets, 'packagedJar': jar.name, 'packagedJarSha256': sha(jar.read_bytes()),
    'captureHashes': {p.name: sha(p.read_bytes()) for p in sorted((ART / 'captures').glob('*.png'))},
    'sourceHashes': {str(p.relative_to(ROOT)): sha(p.read_bytes()) for p in [
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/RobeModel.java',
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/RobeModels.java',
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/MagicEquipmentCapture.java',
        ROOT / 'tools/author_robe_armor.py']},
    'limits': ['Visual candidate; Cinderweave Coal Cuffs remains provisional.',
               'Simple articulated cloth, not a cloth physics simulation.',
               'No new gameplay changes; no world combat test rerun in this art pass.']
}
(Path(__file__).parent / 'verification.json').write_text(json.dumps(evidence, indent=2) + '\n')
print(f"Verified {counts['tests']} unit tests, 72 native captures and exact packaged worn textures.")
