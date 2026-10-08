"""Run the current robe classes/resources through the prepared native NeoForge client."""
from pathlib import Path
import os
import subprocess

ROOT = Path(__file__).resolve().parents[3]
BUILD = ROOT / 'run/robes-mantles-review-build'
PREPARED = Path('/private/tmp/vestige-robes-hanging-review-build-2026-10-08/moddev')
JAVA = Path('/Users/quzzar/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.12.1+1/Contents/Home/bin')
CLASSES = BUILD / 'classes/java/main'
LEGACY = (PREPARED / 'effectsCaptureLegacyClasspath.txt').read_text().splitlines()
CP = os.pathsep.join([str(CLASSES), str(BUILD / 'moddev/artifacts/neoforge-21.1.72-minecraft.jar'), *LEGACY])
SOURCES = ['RobeModel', 'RobeModels', 'MagicEquipmentCapture']
subprocess.run([str(JAVA / 'javac'), '-proc:none', '--release', '21', '-cp', CP, '-d', str(CLASSES),
                *[str(ROOT / f'src/main/java/com/quzzar/vestige/equipment/client/{name}.java') for name in SOURCES]], check=True)
jar = next(p for p in (BUILD / 'libs').glob('*.jar') if not p.name.endswith(('-sources.jar', '-javadoc.jar')))
updates = []
for name in SOURCES:
    for path in (CLASSES / 'com/quzzar/vestige/equipment/client').glob(name + '*.class'):
        updates.extend(['-C', str(CLASSES), str(path.relative_to(CLASSES))])
subprocess.run([str(JAVA / 'jar'), '--update', '--file', str(jar), *updates], check=True)
kithkyn = ROOT.parent / 'kithkyn'
version = next(line.split('=', 1)[1].strip() for line in (kithkyn / 'gradle.properties').read_text().splitlines()
               if line.startswith('mod_version='))
runtime = os.pathsep.join([CP, str(kithkyn / f'build/libs/kithkyn-{version}.jar')])
folders = os.pathsep.join([f'vestige%%{CLASSES}', f'vestige%%{BUILD / "resources/main"}'])
game = ROOT / 'run/robes-mantles-2026-10-08'
game.mkdir(parents=True, exist_ok=True)
(game / 'options.txt').write_text('onboardAccessibility:false\npauseOnLostFocus:false\nrenderDistance:2\nautoJump:false\nmaxFps:60\n')
config = game / 'config/kithkyn-common.toml'
config.parent.mkdir(parents=True, exist_ok=True)
config.write_text('[llm]\n"Enable LLM?" = false\n')
subprocess.run([str(JAVA / 'java'), '-Xmx2G', '-cp', runtime,
                '@' + str(PREPARED / 'effectsCaptureRunVmArgs.txt'), '-Dfml.modFolders=' + folders,
                '-Dvestige.capture.output=' + str(ROOT / 'docs/art/magic-equipment-mantles/captures'),
                '@' + str(PREPARED / 'effectsCaptureRunProgramArgs.txt')], cwd=game, check=True, timeout=360)
