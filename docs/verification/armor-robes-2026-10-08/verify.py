"""Freeze the actual native robe inspection and exact packaged resources."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parents[3]
BUILD = ROOT / 'run/robes-mantles-review-build'
ART = ROOT / 'docs/art/magic-equipment-armor-v2'


def sha(data):
    return hashlib.sha256(data).hexdigest()


capture = json.loads((ART / 'captures/capture.json').read_text())
assert len(capture['checks']) == 66
assert len(list((ART / 'captures').glob('*.png'))) == 66
assert {c['view'] for c in capture['checks']} == {p.stem for p in (ART / 'captures').glob('*.png')}
assert len([c for c in capture['checks'] if '-world-' in c['view']]) == 0
assert all(c.get('trimUntinted') for c in capture['checks'] if '-gui-' in c['view'])
assert all(c['models'] == ['WIDE', 'SLIM'] for c in capture['checks'] if 'models' in c)
jar = next(p for p in (BUILD / 'libs').glob('*.jar') if not p.name.endswith(('-sources.jar', '-javadoc.jar')))
assets = []
with ZipFile(jar) as packaged:
    for name in ('wardweave', 'cinderweave'):
        layers = []
        for suffix in ('', '_overlay'):
            rel = f'assets/vestige/textures/models/armor/{name}_layer_1{suffix}.png'
            source = (ROOT / 'src/main/resources' / rel).read_bytes()
            assert source == (BUILD / 'resources/main' / rel).read_bytes() == packaged.read(rel)
            assert source == (ART / 'export' / Path(rel).name).read_bytes()
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
    assert b'mantle_back' not in packaged.read('com/quzzar/vestige/equipment/client/RobeModel.class')
    assert b'right_coat' in packaged.read('com/quzzar/vestige/equipment/client/RobeModel.class')
    assert b'prepare' in packaged.read('com/quzzar/vestige/equipment/client/RobeModels$1.class')
    collision = 'com/quzzar/vestige/travel/StandingStoneCollision.class'
    assert packaged.read(collision) == (BUILD / 'classes/java/main' / collision).read_bytes()
fixture = json.loads((Path(__file__).parent / 'fixture.json').read_text())
assert fixture['originalClassSha256'] == sha((BUILD / 'classes/java/main' / collision).read_bytes())
assert 'ROBE ART FIXTURE:' in (Path(__file__).parent / 'client.log').read_text()
tests = [ET.parse(p).getroot() for p in (BUILD / 'test-results/test').glob('TEST-*.xml')]
counts = {k: sum(int(t.attrib[k]) for t in tests) for k in ('tests', 'failures', 'errors', 'skipped')}
assert counts['tests'] > 0 and counts['failures'] == counts['errors'] == counts['skipped'] == 0
evidence = {
    'date': '2026-10-08', 'artApprovalRecord': '../../art/magic-equipment-armor-v2/approval.json',
    'engine': capture['engine'], 'unitTests': counts,
    'unitTestScope': '181-test gameplay baseline from the prior isolated full build, not rerun for this appearance-only revision. Current robe classes compiled and packaged by capture_client.py.',
    'nativeCaptures': {'total': 66, 'dyedInventoryAndPoses': 64, 'ironArmorLayering': 2, 'thirdPersonWorld': 0,
                       'bodyTypes': ['WIDE', 'SLIM'], 'poses': ['front', 'side', 'back', 'walking', 'crouching']},
    'artOnlyFixture': fixture,
    'wornAssets': assets, 'packagedJar': str(jar.relative_to(ROOT)), 'packagedJarSha256': sha(jar.read_bytes()),
    'captureHashes': {p.name: sha(p.read_bytes()) for p in sorted((ART / 'captures').glob('*.png'))},
    'referenceHashes': {p.name: sha(p.read_bytes()) for p in sorted((ART / 'references').glob('*.png'))},
    'sourceHashes': {str(p.relative_to(ROOT)): sha(p.read_bytes()) for p in [
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/RobeModel.java',
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/RobeModels.java',
        ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/MagicEquipmentCapture.java',
        ROOT / 'tools/author_robe_armor.py', ART / 'source/wardweave-generated.png', ART / 'source/cinderweave-generated.png', ART / 'prompts.json']},
    'limits': ['Owner acceptance is recorded separately in the linked approval record.', 'Split lower robe follows native leg poses; no cape or back panel.',
               'Final art client excludes optional Kithkyn and ordinary world captures; no integration/world presentation claim.',
               'Art-only classpath fixture simplifies non-displayed standing-stone collision; production class restored afterward.',
               'Riding, swimming, flight and other mods\' armor rendering remain additional visual playtests.',
               'No gameplay changes; no combat world-test rerun or installed pack changes in this art pass.']
}
(Path(__file__).parent / 'verification.json').write_text(json.dumps(evidence, indent=2) + '\n')
print(f"Verified 66 native captures and exact packaged worn textures; prior {counts['tests']}-test baseline recorded separately.")
