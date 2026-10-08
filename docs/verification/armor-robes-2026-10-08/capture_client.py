"""Run the current robe classes/resources through the prepared native NeoForge client."""
from pathlib import Path
import os
import subprocess
import shutil
import hashlib
import json

ROOT = Path(__file__).resolve().parents[3]
BUILD = ROOT / 'run/robes-mantles-review-build'
PREPARED = Path('/private/tmp/vestige-robes-hanging-review-build-2026-10-08/moddev')
JAVA = Path('/Users/quzzar/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.12.1+1/Contents/Home/bin')
CLASSES = BUILD / 'classes/java/main'
LEGACY = (PREPARED / 'effectsCaptureLegacyClasspath.txt').read_text().splitlines()
CP = os.pathsep.join([str(CLASSES), str(BUILD / 'moddev/artifacts/neoforge-21.1.72-minecraft.jar'), *LEGACY])
SOURCES = ['RobeModel', 'RobeModels', 'MagicEquipmentCapture']
if not os.environ.get('VESTIGE_CAPTURE_SKIP_COMPILE'):
    subprocess.run([str(JAVA / 'javac'), '-J-Xmx512m', '-J-XX:ActiveProcessorCount=2', '-proc:none', '--release', '21', '-cp', CP, '-d', str(CLASSES),
                    *[str(ROOT / f'src/main/java/com/quzzar/vestige/equipment/client/{name}.java') for name in SOURCES]], check=True)
jar = next(p for p in (BUILD / 'libs').glob('*.jar') if not p.name.endswith(('-sources.jar', '-javadoc.jar')))
updates = []
for source in (ROOT / 'src/main/resources/assets/vestige/textures/models/armor').glob('*_layer_1*.png'):
    rel = source.relative_to(ROOT / 'src/main/resources')
    target = BUILD / 'resources/main' / rel
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
    updates.extend(['-C', str(BUILD / 'resources/main'), str(rel)])
for name in SOURCES:
    for path in (CLASSES / 'com/quzzar/vestige/equipment/client').glob(name + '*.class'):
        updates.extend(['-C', str(CLASSES), str(path.relative_to(CLASSES))])
subprocess.run([str(JAVA / 'jar'), '--update', '--file', str(jar), *updates], check=True)
runtime = CP  # Native art review excludes the optional Kithkyn runtime.
folders = os.pathsep.join([f'vestige%%{CLASSES}', f'vestige%%{BUILD / "resources/main"}'])
game = ROOT / 'run/robes-armor-2026-10-08'
game.mkdir(parents=True, exist_ok=True)
(game / 'options.txt').write_text('onboardAccessibility:false\npauseOnLostFocus:false\nrenderDistance:2\nautoJump:false\nmaxFps:60\n')
config = game / 'config/kithkyn-common.toml'
config.parent.mkdir(parents=True, exist_ok=True)
config.write_text('[llm]\n"Enable LLM?" = false\n')
# The overloaded host spent minutes constructing unrelated stone collision.
# Only this isolated art classpath uses the fixture; no stones are in the scene.
# Do not package it, and restore the original class even after launch failure.
fixture = Path(__file__).parent / 'fixture/StandingStoneCollision.java'
collision = CLASSES / 'com/quzzar/vestige/travel/StandingStoneCollision.class'
original = collision.read_bytes()
try:
    subprocess.run([str(JAVA / 'javac'), '-J-Xmx256m', '-J-XX:ActiveProcessorCount=2', '-proc:none', '--release', '21', '-cp', CP,
                    '-d', str(CLASSES), str(fixture)], check=True)
    (Path(__file__).parent / 'fixture.json').write_text(json.dumps({
        'scope': 'Art review only: standing stones absent; their collision uses a unit cube.',
        'originalClassSha256': hashlib.sha256(original).hexdigest(),
        'fixtureClassSha256': hashlib.sha256(collision.read_bytes()).hexdigest(),
        'robeClassesAndAssets': 'Current production files, unchanged by fixture.',
    }, indent=2) + '\n')
    subprocess.run([str(JAVA / 'java'), '-Xmx2G', '-XX:ActiveProcessorCount=2', '-cp', runtime,
                    '@' + str(PREPARED / 'effectsCaptureRunVmArgs.txt'), '-Dfml.modFolders=' + folders,
                    '-Dvestige.capture.output=' + str(ROOT / 'docs/art/magic-equipment-armor-v2/captures'),
                    '-Dvestige.capture.gui_only=true',
                    '@' + str(PREPARED / 'effectsCaptureRunProgramArgs.txt')], cwd=game, check=True, timeout=1200)
    manifest = ROOT / 'docs/art/magic-equipment-armor-v2/captures/capture.json'
    assert manifest.exists() and len(json.loads(manifest.read_text())['checks']) == 66, 'Preview closed before completing its 66 captures.'
finally:
    collision.write_bytes(original)
