"""Verify native UI frames and encode them with their observed elapsed times."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import math
import struct
import subprocess

ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).parent
ART = ROOT / 'docs/art/item-readiness-overlay-v2'
CAPTURES = ART / 'captures'
BUILD = ROOT / 'run/item-readiness-diagonal-review-build'
meta = json.loads((CAPTURES / 'capture.json').read_text())
assert meta['source'] == 'Minecraft main render target'
groups = {}
for frame in meta['frames']:
    assert (CAPTURES / frame['file']).is_file()
    assert 0 <= frame['shortage'] <= 1 and 0 <= frame['recovery'] <= 1
    groups.setdefault(Path(frame['file']).parts[0], []).append(frame)
assert set(groups) == {'diagonal-scale-3', 'diagonal-scale-2'}
rows = []
(ART / 'videos').mkdir(exist_ok=True)
for name, frames in groups.items():
    native_count = len(frames)
    # The first launch can capture before its initial private mana packet arrives.
    # Keep that native PNG/metadata, but begin playback at the first synchronized state.
    start = next(i for i, f in enumerate(frames) if f['shortage'] > .95 and f['recovery'] > .9)
    frames = frames[start:]
    dimensions = struct.unpack('>II', (CAPTURES / frames[0]['file']).read_bytes()[16:24])
    assert len(frames) >= 30
    assert frames[0]['shortage'] > .95 and frames[0]['recovery'] > .9
    assert any(f['shortage'] == 0 and f['recovery'] > 0 for f in frames)
    assert frames[-1]['shortage'] == frames[-1]['recovery'] == 0
    assert all(a['seconds'] < b['seconds'] for a, b in zip(frames, frames[1:]))
    durations = [b['seconds'] - a['seconds'] for a, b in zip(frames, frames[1:])] + [.1]
    lines = ['ffconcat version 1.0']
    for frame, duration in zip(frames, durations):
        lines += [f"file '{Path(frame['file']).name}'", f'duration {duration:.9f}']
    lines.append(f"file '{Path(frames[-1]['file']).name}'")
    concat = CAPTURES / name / 'frames.ffconcat'
    concat.write_text('\n'.join(lines) + '\n')
    video = ART / 'videos' / (name + '.mp4')
    subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-safe', '1', '-f', 'concat', '-i', str(concat),
                    '-vf', 'fps=30', '-an', '-c:v', 'libx264', '-threads', '2', '-preset', 'fast', '-crf', '18', '-pix_fmt', 'yuv420p',
                    '-movflags', '+faststart', str(video)], check=True)
    probe = json.loads(subprocess.check_output(['ffprobe', '-v', 'error', '-show_streams', '-show_format', '-of', 'json', str(video)]))
    assert len(probe['streams']) == 1 and probe['streams'][0]['codec_type'] == 'video'
    assert (probe['streams'][0]['width'], probe['streams'][0]['height']) == dimensions
    assert abs(float(probe['format']['duration']) - sum(durations)) < .15
    rows.append({'view': name, 'nativeFrames': native_count, 'encodedFrames': len(frames), 'warmupFramesOmitted': start,
                 'dimensions': list(dimensions), 'elapsedSeconds': sum(durations), 'video': str(video.relative_to(ART)),
                 'sha256': hashlib.sha256(video.read_bytes()).hexdigest(), 'audio': False})
    if name == 'diagonal-scale-3':
        scale = frames[0]['guiScale']
        panel_x = ((math.ceil(dimensions[0] / scale) - 304) // 2) * scale
        panel_y = ((math.ceil(dimensions[1] / scale) - 172) // 2) * scale
        crop = f'crop={304 * scale}:{172 * scale}:{panel_x}:{panel_y}'
        subprocess.run(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-i', str(video),
                        '-filter_complex_threads', '1',
                        '-filter_complex', f'fps=10,{crop},split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=none',
                        '-loop', '0', str(ART / 'crisscross-preview.gif')], check=True)
collision = 'com/quzzar/vestige/travel/StandingStoneCollision.class'
runtime = json.loads((HERE / 'runtime.json').read_text())
assert hashlib.sha256((BUILD / 'classes/java/main' / collision).read_bytes()).hexdigest() == runtime['originalCollisionSha256']
with ZipFile(BUILD / 'libs/vestige-readiness-ui-preview.jar') as jar:
    assert hashlib.sha256(jar.read(collision)).hexdigest() == runtime['originalCollisionSha256']
    for path in (BUILD / 'classes/java/main/com/quzzar/vestige/magic/world/client').glob('NativeReadinessPreview*.class'):
        assert jar.read(str(path.relative_to(BUILD / 'classes/java/main'))) == path.read_bytes()
approval = json.loads((ROOT / 'docs/art/magic-equipment-armor-v2/approval.json').read_text())
for texture in approval['approvedTextures']:
    relative = Path(texture['productionFile']).relative_to('src/main/resources')
    assert hashlib.sha256((BUILD / 'resources/main' / relative).read_bytes()).hexdigest() == texture['sha256']
receipt = {'engine': meta['engine'], 'scope': meta['scope'], 'nativeFrames': len(meta['frames']), 'clips': rows,
           'runtime': runtime, 'captureMetadataSha256': hashlib.sha256((CAPTURES / 'capture.json').read_bytes()).hexdigest(),
           'frameHashes': {f['file']: hashlib.sha256((CAPTURES / f['file']).read_bytes()).hexdigest() for f in meta['frames']},
           'sourceHashes': {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted((HERE / 'source').glob('*'))},
           'limits': ['Native comparison screen, not a functional new equipment ability or normal inventory interaction test.',
                      'Tints, opacity and sweep direction await owner review; production mana decoration unchanged.',
                      'No optional Kithkyn co-load, new unit/world suite, installation or publication in this UI study.']}
(HERE / 'verification.json').write_text(json.dumps(receipt, indent=2) + '\n')
print(f'Verified {len(meta["frames"])} native frames and two silent videos; restored collision and accepted robe assets agree.')
