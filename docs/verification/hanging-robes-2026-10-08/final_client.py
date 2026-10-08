"""Repeat the native preview using the already verified pinned Gradle runtime."""
from pathlib import Path
import os
import subprocess

ROOT = Path(__file__).resolve().parents[3]
BUILD = Path('/private/tmp/vestige-robes-hanging-review-build-2026-10-08')
JAVA = Path('/Users/quzzar/.gradle/jdks/eclipse_adoptium-21-aarch64-os_x.2/jdk-21.0.12.1+1/Contents/Home/bin')
CLASSES = BUILD / 'classes/java/main'
LEGACY = (BUILD / 'moddev/effectsCaptureLegacyClasspath.txt').read_text().splitlines()
MINECRAFT = BUILD / 'moddev/artifacts/neoforge-21.1.72-minecraft.jar'
CP = os.pathsep.join([str(CLASSES), str(MINECRAFT), *LEGACY])
subprocess.run([str(JAVA / 'javac'), '-proc:none', '--release', '21', '-cp', CP, '-d', str(CLASSES),
                str(ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/RobeModel.java'),
                str(ROOT / 'src/main/java/com/quzzar/vestige/equipment/client/MagicEquipmentCapture.java')], check=True)
jar = next(p for p in (BUILD / 'libs').glob('*.jar') if not p.name.endswith(('-sources.jar', '-javadoc.jar')))
subprocess.run([str(JAVA / 'jar'), '--update', '--file', str(jar), '-C', str(CLASSES),
                'com/quzzar/vestige/equipment/client/RobeModel.class', '-C', str(CLASSES),
                'com/quzzar/vestige/equipment/client/MagicEquipmentCapture.class', '-C', str(CLASSES),
                'com/quzzar/vestige/equipment/client/MagicEquipmentCapture$Review.class'], check=True)
kithkyn = ROOT.parent / 'kithkyn'
version = next(line.split('=', 1)[1].strip() for line in (kithkyn / 'gradle.properties').read_text().splitlines()
               if line.startswith('mod_version='))
runtime = os.pathsep.join([CP, str(kithkyn / f'build/libs/kithkyn-{version}.jar')])
folders = os.pathsep.join([f'vestige%%{CLASSES}', f'vestige%%{BUILD / "resources/main"}'])
subprocess.run([str(JAVA / 'java'), '-Xmx2G', '-cp', runtime,
                '@' + str(BUILD / 'moddev/effectsCaptureRunVmArgs.txt'), '-Dfml.modFolders=' + folders,
                '@' + str(BUILD / 'moddev/effectsCaptureRunProgramArgs.txt')],
               cwd=ROOT / 'run/robes-hanging-v3-2026-10-08', check=True)
