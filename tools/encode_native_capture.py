#!/usr/bin/env python3
"""Encode Minecraft framebuffer captures with their real elapsed frame times.

The output is silent. This reads images exported by the optional development
client, not desktop screenshots or a browser reconstruction.
"""
import argparse
import hashlib
import json
import math
import shutil
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / 'tools/effects-viewer/public/native'
MANIFEST = ROOT / 'tools/effects-viewer/src/native-clips.json'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def without_spell_cooldowns(value):
    """Remove only explicit cooldown entries in definition/mode cost lists."""
    if isinstance(value, list):
        return [without_spell_cooldowns(entry) for entry in value]
    if isinstance(value, dict):
        return {key: without_spell_cooldowns([entry for entry in child if entry.get('type') != 'cooldown']
                    if key == 'costs' and isinstance(child, list) else child)
                for key, child in value.items()}
    return value


def matches_recorded_definition(source, recorded_hash):
    if digest(source) == recorded_hash:
        return True
    # Operator casts used for this gallery bypass cooldowns. Preserve the exact
    # recorded source bytes and accept only that one documented removal; effect,
    # presentation, preparation, mana and all other changes still need footage.
    recorded = ROOT / 'tools/recorded-spell-definitions' / (recorded_hash + '.json')
    if not recorded.is_file() or digest(recorded) != recorded_hash:
        return False
    original = json.loads(recorded.read_text())
    revised = without_spell_cooldowns(original)
    return revised != original and json.loads(source.read_text()) == revised


def encode(directory, ffmpeg):
    meta = json.loads((directory / 'capture.json').read_text())
    assert meta['source'] == 'Minecraft main render target'
    source = ROOT / 'src/main/resources/data/vestige/runtime_spells' / (meta['spell'].split(':')[1] + '.json')
    assert digest(source) == meta['definitionSha256'], f'Stale native capture: {directory}'
    times = meta['times']
    assert len(times) >= 2 and all(math.isfinite(t) for t in times)
    assert all(a < b for a, b in zip(times, times[1:])), 'Frame times must increase'
    filename = directory.name + '.mp4'
    OUTPUT.mkdir(parents=True, exist_ok=True)
    video = OUTPUT / filename
    concat = directory / 'frames.ffconcat'
    # ffconcat filenames are relative to the manifest; their generated names
    # contain no user-controlled path, quoting, shell expansion or traversal.
    lines = ['ffconcat version 1.0']
    for i, time in enumerate(times):
        image = directory / f'frame-{i:05d}.png'
        assert image.is_file(), image
        lines += [f"file '{image.name}'", f'duration {(times[i+1]-time) if i+1<len(times) else 1/30:.9f}']
    lines.append(f"file 'frame-{len(times)-1:05d}.png'")
    concat.write_text('\n'.join(lines) + '\n')
    subprocess.run([ffmpeg, '-hide_banner', '-loglevel', 'error', '-y', '-safe', '1', '-f', 'concat', '-i', str(concat),
                    '-vf', 'fps=30', '-an', '-c:v', 'libx264', '-preset', 'fast', '-crf', '23', '-pix_fmt', 'yuv420p',
                    '-movflags', '+faststart', str(video)], check=True)
    return {k: meta[k] for k in ('spell', 'phase', 'label', 'kind', 'engine', 'outcome', 'definitionSha256', 'width', 'height')} | {
        'url': '/native/' + filename, 'seconds': times[-1]-times[0]+1/30, 'frames': len(times), 'audio': False,
        'videoSha256': digest(video), 'captureFps': (len(times)-1)/(times[-1]-times[0]),
        'captureSha256': digest(directory / 'capture.json'),
    } | ({'castContext': meta['castContext']} if 'castContext' in meta else {})


def check(rows, complete):
    catalog = json.loads((ROOT / 'tools/effects-viewer/src/catalog.json').read_text())
    definitions = {s['id']: s for s in catalog}
    identities = set()
    for row in rows:
        key = (row['spell'], row['kind'], row['phase'])
        assert key not in identities, f'Duplicate native clip: {key}'
        identities.add(key)
        assert row['spell'] in definitions and row['kind'] == 'cast'
        assert row['phase']==0
        assert 'castContext' in row and any(s['role']=='Caster' for s in row['castContext']['subjects'])
        assert row['frames'] >= 2 and row['seconds'] > 0 and row['audio'] is False
        if row['kind']=='cast':
            assert row['outcome'] in ('COMPLETED','AWAITING_RECAST','RUNNING','CHARGING'), f'Cast did not execute successfully: {key}: {row["outcome"]}'
        assert row['url'].startswith('/native/') and Path(row['url']).name == row['url'].removeprefix('/native/')
        video = OUTPUT / Path(row['url']).name
        assert digest(video) == row['videoSha256'], f'Changed video: {video}'
        source = ROOT / 'src/main/resources/data/vestige/runtime_spells' / (row['spell'].split(':')[1] + '.json')
        assert matches_recorded_definition(source, row['definitionSha256']), f'Clip predates spell definition: {key}'
    if complete:
        missing = [(s['id'], 'cast', 0) for s in catalog if (s['id'], 'cast', 0) not in identities]
        assert not missing, f'Missing {len(missing)} actual cast clips: {missing[:5]}'
    return len(identities)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('captures', nargs='*', type=Path)
    parser.add_argument('--check', action='store_true')
    parser.add_argument('--complete', action='store_true', help='Require an actual cast recording for every spell')
    args = parser.parse_args()
    rows = json.loads(MANIFEST.read_text()) if MANIFEST.exists() else []
    if not args.check:
        ffmpeg = shutil.which('ffmpeg')
        assert ffmpeg, 'ffmpeg is required to encode footage'
        selected = {}
        for capture in args.captures:
            for metadata in sorted(capture.glob('*/capture.json')):
                meta = json.loads(metadata.read_text())
                if meta['kind']=='cast': selected[(meta['spell'], meta['kind'], meta['phase'])] = metadata
        # A later recording pass replaces that identity before encoding, so an
        # explicitly superseded failed/stale take never becomes published media.
        for metadata in selected.values():
            previous = next((row for row in rows if row.get('captureSha256') == digest(metadata)), None)
            if previous and (OUTPUT / Path(previous['url']).name).is_file() and digest(OUTPUT / Path(previous['url']).name) == previous['videoSha256']:
                continue
            row = encode(metadata.parent, ffmpeg)
            rows = [old for old in rows if (old['spell'], old['kind'], old['phase']) != (row['spell'], row['kind'], row['phase'])]
            rows.append(row)
            print(f"Encoded {row['spell']} {row['kind']} {row['phase']} ({row['seconds']:.2f}s)", flush=True)
        rows.sort(key=lambda r: (r['spell'], r['kind'], r['phase']))
        check(rows, args.complete)
        MANIFEST.write_text(json.dumps(rows, indent=2) + '\n')
    print(f'Verified {check(rows, args.complete)} native Minecraft clips')


if __name__ == '__main__':
    main()
