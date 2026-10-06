#!/usr/bin/env python3
"""Encode unaltered Minecraft rune frames at measured times and pin the preview evidence."""
import argparse
import hashlib
import json
import shutil
import subprocess
from pathlib import Path
from author_apparatus_models import ROOT


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('captures', type=Path)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument('--astral', action='store_true', help='Archive the selected production Astral Spellstone')
    mode.add_argument('--table', action='store_true', help='Archive the approved production tent-table Spellstone')
    mode.add_argument('--bodies', action='store_true', help='Archive the three capture-only Astral body designs')
    mode.add_argument('--finishes', action='store_true', help='Archive four gilded material finishes on one Spellstone')
    mode.add_argument('--trim', action='store_true', help='Archive stone borders with Diamond top inlays')
    mode.add_argument('--crystal', action='store_true', help='Archive the exposed crystal-core Spellstone')
    mode.add_argument('--fractured', action='store_true', help='Archive the three-fragment relic Spellstone')
    mode.add_argument('--corners', action='store_true', help='Archive four vertical-corner designs and three alternative finishes')
    mode.add_argument('--small-corners', dest='small_corners', action='store_true', help='Archive the smaller vertical-corner revision')
    mode.add_argument('--locked-a', dest='locked_a', action='store_true', help='Archive the selected production A corner chips')
    mode.add_argument('--refined-a', dest='refined_a', action='store_true', help='Archive taller A, clean selection edges and empty/imbued Plinths')
    parser.add_argument('--build-directory', type=Path, default=ROOT/'build')
    args = parser.parse_args()
    args.captures = args.captures.resolve()
    ffmpeg = shutil.which('ffmpeg')
    assert ffmpeg, 'ffmpeg is required to encode the native recordings'
    selected = next((name for name in ('astral', 'table', 'bodies', 'finishes', 'trim', 'crystal', 'fractured', 'corners', 'small_corners', 'locked_a', 'refined_a') if getattr(args, name)), 'runes')
    profiles = {
        'runes': ('spellstone-rune-options', 'vestige-spellstone-rune-options', 'rune-options', 'SpellstoneRuneOptions', 'runeRendererSha256', 8),
        'astral': ('astral-spellstone', None, 'current', 'AstralSealRenderer', 'astralRendererSha256', 5),
        'table': ('spellstone-tent-table', None, 'current', 'AstralSealRenderer', 'astralRendererSha256', 6),
        'bodies': ('astral-body-options', 'vestige-astral-bodies', 'astral-bodies', 'AstralSealRenderer', 'astralRendererSha256', 4),
        'finishes': ('gilded-spellstone-finishes', 'vestige-gilded-finishes', 'gilded-finishes', 'AstralSealRenderer', 'astralRendererSha256', 5),
        'trim': ('spellstone-diamond-trim', 'vestige-diamond-trim', 'diamond-trim', 'AstralSealRenderer', 'astralRendererSha256', 5),
        'crystal': ('spellstone-crystal-core', 'vestige-crystal-core', 'crystal-core', 'AstralSealRenderer', 'astralRendererSha256', 3),
        'fractured': ('fractured-spellstone', 'vestige-fractured-spellstone', 'fractured', 'AstralSealRenderer', 'astralRendererSha256', 4),
        'corners': ('spellstone-corner-details', 'vestige-corner-details', 'corner-details', 'AstralSealRenderer', 'astralRendererSha256', 7),
        'small_corners': ('spellstone-corner-details/smaller-corners', 'vestige-corner-details', 'corner-details', 'AstralSealRenderer', 'astralRendererSha256', 7),
        'locked_a': ('spellstone-corner-details/locked-a', None, 'current', 'AstralSealRenderer', 'astralRendererSha256', 6),
        'refined_a': ('spellstone-corner-details/refined-a', None, 'current', 'AstralSealRenderer', 'astralRendererSha256', 10),
    }
    archive_name, pack_name, presentation, renderer_name, renderer_key, expected_views = profiles[selected]
    archive = ROOT / 'docs/art' / archive_name
    preview_pack = ROOT / 'run/effects-capture/resourcepacks' / pack_name if pack_name else None
    material_study = selected in ('finishes', 'trim', 'crystal', 'fractured', 'corners', 'small_corners')
    expected_textures = {t['path'].removeprefix('assets/minecraft/'): t['sha256']
                         for t in json.loads((archive / 'designs.json').read_text())['vanillaTextures']} if material_study else {}
    destination = archive / 'native'
    destination.mkdir(parents=True, exist_ok=True)
    renderer = ROOT / f'src/main/java/com/quzzar/vestige/apparatus/client/{renderer_name}.java'
    compiled = args.build_directory / f'classes/java/main/com/quzzar/vestige/apparatus/client/{renderer_name}.class'
    shots = []
    for folder in sorted(args.captures.iterdir()):
        capture = folder / 'capture.json'
        if not capture.is_file():
            continue
        meta = json.loads(capture.read_text())
        # Apparatus snapshots replace the generic source label with their registered-block provenance.
        assert meta['kind'] == 'apparatus' and meta['source'] == 'Registered blocks and synchronized offering block entities'
        assert meta['renderStage'] == 'after_level_before_gui' and meta['outcome'] == 'PLACED'
        assert meta['apparatusPresentation'] == presentation
        if selected == 'bodies' or material_study:
            expected_eye = ([3.0, 67.0, 6.5] if material_study else [2.5, 66.5, 5.0]) if meta['spell'].endswith('comparison') else [1.35, 66.1, 1.7]
            if selected == 'crystal' and meta['spell'].endswith('side'):
                expected_eye = [1.5, 65.65, 1.9]
            if selected == 'fractured' and meta['spell'].endswith('top'):
                expected_eye = [1.25, 66.8, 1.5]
            if selected in ('corners', 'small_corners'):
                expected_eye = [1.22, 65.92, 1.46]
            assert meta['apparatusCamera']['fixed'] and meta['apparatusCamera']['eye'] == expected_eye
        if material_study:
            assert meta['materialTextureSha256'] == expected_textures, 'Loaded vanilla material textures differ from the native source assets'
        assert meta[renderer_key] == digest(compiled)
        times = meta['times']
        assert len(times) >= 2 and all(a < b for a, b in zip(times, times[1:]))
        for asset, asset_hash in meta['assetSha256'].items():
            actual = ROOT / 'src/main/resources/assets/vestige' / asset
            if preview_pack:
                preview = preview_pack / 'assets/vestige' / asset
                if preview.is_file():
                    actual = preview
            assert digest(actual) == asset_hash, 'Loaded asset mismatch: ' + asset
        lines = ['ffconcat version 1.0']
        for i, time in enumerate(times):
            assert (folder / f'frame-{i:05d}.png').is_file()
            duration = times[i + 1] - time if i + 1 < len(times) else max(1 / 30, meta['seconds'] - time)
            lines += [f"file 'frame-{i:05d}.png'", f'duration {duration:.9f}']
        lines += [f"file 'frame-{len(times)-1:05d}.png'"]
        concat = folder / 'frames.ffconcat'
        concat.write_text('\n'.join(lines) + '\n')
        name = meta['spell'].split(':')[1]
        video = destination / (name + '.mp4')
        subprocess.run([ffmpeg, '-hide_banner', '-loglevel', 'error', '-y', '-safe', '1', '-f', 'concat',
            '-i', str(concat), '-vf', 'fps=30', '-an', '-c:v', 'libx264', '-crf', '18',
            '-pix_fmt', 'yuv420p', '-movflags', '+faststart', str(video)], check=True)
        subprocess.run([ffmpeg, '-hide_banner', '-loglevel', 'error', '-i', str(video), '-f', 'null', '-'], check=True)
        frame_index = min(range(len(times)), key=lambda i: abs(times[i] - 2.4))
        frame = folder / f'frame-{frame_index:05d}.png'
        poster = destination / (name + '.png')
        shutil.copy2(frame, poster)
        shutil.copy2(capture, destination / (name + '.json'))
        shots.append({'name': name, 'capture': str(capture.relative_to(ROOT)), 'posterFrame': frame_index,
            'posterSha256': digest(poster), 'videoSha256': digest(video), 'nativeFrames': len(times),
            'framebuffer': [meta['width'], meta['height']],
            'lighting': meta.get('runeLighting', 'midnight' if name.endswith('night') else 'noon'),
            'camera': meta.get('apparatusCamera'),
            'materialTextureSha256': meta.get('materialTextureSha256', {}),
            renderer_key: meta[renderer_key], 'assetSha256': meta['assetSha256']})
        print(f'{name}: {len(times)} native frames; encoded and decoded at original 960x540')
    assert len(shots) == expected_views, 'Incomplete set of native views'
    installed = Path('/Users/quzzar/Library/Application Support/PrismLauncher/instances/Kithkyn Testing/minecraft/mods/vestige-0.1.0.jar')
    installed_hash = digest(installed) if installed.is_file() else None
    # Current production evidence is independent of a separate launcher installation.
    if selected not in ('table', 'corners', 'small_corners', 'locked_a', 'refined_a'):
        if selected == 'bodies' or material_study:
            assert installed_hash == 'efbda56c98f9dd8dbe1d39af4d1a137780dd8da7e0f29644e9a56a3834683fa8', 'Unexpected Prism version while archiving body alternatives'
        elif args.astral:
            assert installed_hash in ('dfeedc862c28603643569cba9c7d351d21219c633c6c0aa47fd1b323de8a22c8',
                digest(ROOT / 'build/libs/vestige-0.1.0.jar')), 'Unexpected Prism version while archiving'
        else:
            assert installed_hash == 'dfeedc862c28603643569cba9c7d351d21219c633c6c0aa47fd1b323de8a22c8'
    (archive / 'capture-evidence.json').write_text(json.dumps({
        'source': 'Actual Minecraft framebuffer; unchanged PNGs, videos encoded at measured frame times',
        'rendererSourceSha256': digest(renderer),
        ('installedJarSha256' if selected == 'refined_a' else 'productionInstallationUnchanged'): installed_hash,
        'noShaderPack': True, 'noDynamicWorldLight': True, 'audio': False, 'shots': shots}, indent=2) + '\n')


if __name__ == '__main__':
    main()
