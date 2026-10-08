"""Build and run an isolated native UI study; production gameplay/UI stay unchanged."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import os
import shutil
import subprocess

ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).parent
BASE = ROOT / 'run/robes-mantles-review-build'
BUILD = ROOT / 'run/item-readiness-overlay-review-build'
PREPARED = Path('/private/tmp/vestige-robes-hanging-review-build-2026-10-08/moddev')
JAVA = Path('/Users/quzzar/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.12.1+1/Contents/Home/bin')
CLASSES = BUILD / 'classes/java/main'
RESOURCES = BUILD / 'resources/main'
OUT = ROOT / 'docs/art/item-readiness-overlay-v1/captures'
GAME = ROOT / 'run/item-readiness-overlay-2026-10-08'

for source, target in ((BASE / 'classes/java/main', CLASSES), (BASE / 'resources/main', RESOURCES)):
    shutil.copytree(source, target, dirs_exist_ok=True)
legacy = (PREPARED / 'effectsCaptureLegacyClasspath.txt').read_text().splitlines()
neo = BASE / 'moddev/artifacts/neoforge-21.1.72-minecraft.jar'
cp = os.pathsep.join([str(CLASSES), str(neo), *legacy])
router = ROOT / 'src/main/java/com/quzzar/vestige/magic/presentation/client/NativeEffectsCapture.java'
frozen = HERE / 'source/NativeEffectsCapture.java'
frozen.write_text(router.read_text().replace('"item_mana",', '"item_mana","item_readiness",'))
fixture = HERE / 'source/StandingStoneCollision.java'
fixture.write_text((ROOT / 'docs/verification/armor-robes-2026-10-08/fixture/StandingStoneCollision.java').read_text().replace('ROBE ART FIXTURE:', 'READINESS UI FIXTURE:').replace('robe rendering is unchanged', 'item rendering is unchanged'))
sources = [HERE / 'source/NativeReadinessPreview.java', frozen]
subprocess.run([str(JAVA / 'javac'), '-J-Xmx512m', '-J-XX:ActiveProcessorCount=2', '-proc:none', '--release', '21',
                '-cp', cp, '-d', str(CLASSES), *map(str, sources)], check=True)
jar = BUILD / 'libs/vestige-readiness-ui-preview.jar'
jar.parent.mkdir(parents=True, exist_ok=True)
base_jar = BASE / 'libs/vestige-0.1.0.jar'
shutil.copyfile(base_jar, jar)
updates = []
for folder, prefix in (('magic/world/client', 'NativeReadinessPreview'), ('magic/presentation/client', 'NativeEffectsCapture')):
    for path in (CLASSES / 'com/quzzar/vestige' / folder).glob(prefix + '*.class'):
        updates.extend(['-C', str(CLASSES), str(path.relative_to(CLASSES))])
subprocess.run([str(JAVA / 'jar'), '--update', '--file', str(jar), *updates], check=True)
with ZipFile(BASE / 'moddev/artifacts/neoforge-21.1.72-minecraft-sources.jar') as source_jar:
    native = source_jar.read('net/minecraft/client/gui/GuiGraphics.java')
text = native.decode()
start = text.index('            LocalPlayer localplayer = this.minecraft.player;')
end = text.index('\n\n            this.pose.popPose();', start)
(HERE / 'source/vanilla-cooldown-snippet.txt').write_text(text[start:end] + '\n')
GAME.mkdir(parents=True, exist_ok=True)
(GAME / 'options.txt').write_text('onboardAccessibility:false\npauseOnLostFocus:false\nrenderDistance:2\nautoJump:false\nmaxFps:60\n')
collision = CLASSES / 'com/quzzar/vestige/travel/StandingStoneCollision.class'
original = collision.read_bytes()
try:
    subprocess.run([str(JAVA / 'javac'), '-J-Xmx256m', '-J-XX:ActiveProcessorCount=2', '-proc:none', '--release', '21',
                    '-cp', cp, '-d', str(CLASSES), str(fixture)], check=True)
    (HERE / 'runtime.json').write_text(json.dumps({
        'engine': 'Minecraft 1.21.1 / NeoForge 21.1.72',
        'baseBuild': str(BASE.relative_to(ROOT)), 'baseJarSha256': hashlib.sha256(base_jar.read_bytes()).hexdigest(),
        'scope': 'Isolated native UI study, no optional Kithkyn. Existing server mana regeneration and vanilla client cooldown timer; prototype price 12 mana, no new item ability.',
        'fixture': 'Non-displayed standing-stone collision simplified only on the isolated classpath, never packaged; restored in finally.',
        'originalCollisionSha256': hashlib.sha256(original).hexdigest(),
        'fixtureCollisionSha256': hashlib.sha256(collision.read_bytes()).hexdigest(),
        'vanillaGuiSourceSha256': hashlib.sha256(native).hexdigest(),
        'prototypeJarSha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
    }, indent=2) + '\n')
    folders = os.pathsep.join([f'vestige%%{CLASSES}', f'vestige%%{RESOURCES}'])
    subprocess.run([str(JAVA / 'java'), '-Xmx2G', '-XX:ActiveProcessorCount=2', '-cp', cp,
                    '@' + str(PREPARED / 'effectsCaptureRunVmArgs.txt'), '-Dfml.modFolders=' + folders,
                    '-Dvestige.capture.kind=item_readiness', '-Dvestige.capture.output=' + str(OUT),
                    '@' + str(PREPARED / 'effectsCaptureRunProgramArgs.txt')], cwd=GAME, check=True, timeout=1200)
    meta = json.loads((OUT / 'capture.json').read_text())
    assert {Path(f['file']).parts[0] for f in meta['frames']} == {'opposed-scale-3', 'matched-scale-3', 'opposed-scale-2', 'matched-scale-2'}
    print(f"Completed native readiness UI study: {len(meta['frames'])} unedited frames.")
finally:
    collision.write_bytes(original)
