#!/usr/bin/env python3
"""Combine actual ritual clips with captions outside the unchanged game image."""
import argparse
import hashlib
import json
import shutil
import subprocess
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
STATES = (
    ('ritual_idle', 'Ready to craft', 'READY'),
    ('ritual_hints', 'Missing item silhouettes and misplaced items', 'HINTS'),
    ('ritual_wrong', 'Completed wrong recipe', 'WRONG'),
    ('ritual_reference_success', 'Successful crafting and ingredient lift', 'CRAFTING'),
    ('ritual_failure', 'Failed ritual and blast', 'EXPLOSION_PENDING'),
    ('ritual_discovery', 'Fragment discovery', 'DISCOVERING'),
)
BACKFIRES = (
    ('ritual_failure', 'Four slots: central blast and four Plinth bursts', 'EXPLOSION_PENDING'),
    ('ritual_failure_eight', 'Eight slots: central blast and eight Plinth bursts', 'EXPLOSION_PENDING'),
    ('ritual_failure_spread', 'Eight slots with distant, elevated Plinths', 'EXPLOSION_PENDING'),
)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('archive', nargs='?', type=Path, default=ROOT)
    parser.add_argument('--backfires', action='store_true')
    args = parser.parse_args()
    root = args.archive.resolve()
    states = BACKFIRES if args.backfires else STATES
    recordings = root / ('native' if args.backfires else 'rituals')
    reel_name = 'backfire-review' if args.backfires else 'ritual-review'
    ffmpeg = shutil.which('ffmpeg')
    assert ffmpeg
    inputs, filters, ledger = [], [], []
    captions = []
    caption_directory = root / 'captions'
    caption_directory.mkdir(exist_ok=True)
    font = ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial.ttf', 22)
    for i, (name, title, outcome) in enumerate(states):
        source = recordings / (name + '.mp4')
        meta = json.loads((recordings / (name + '.json')).read_text())
        assert meta['outcome'] == outcome, (name, meta['outcome'])
        assert meta['apparatusPresentation'] == 'current' and meta['kind'] == 'ritual'
        assert meta['audio'] is False and [meta['width'], meta['height']] == [960, 540]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == meta['videoSha256']
        inputs += ['-i', str(source)]
        caption = caption_directory / (name + '.png')
        strip = Image.new('RGB', (960, 40), '#141414')
        ImageDraw.Draw(strip).text((14, 7), title, font=font, fill='white')
        strip.save(caption)
        captions += ['-loop', '1', '-i', str(caption)]
        filters.append(f'[{i}:v]fps=30,setsar=1,pad=960:580:0:40:color=0x141414[base{i}];'
                       f'[base{i}][{i+len(states)}:v]overlay=shortest=1[v{i}]')
        ledger.append({'state': name, 'title': title, 'outcome': outcome,
                       'sourceSha256': meta['videoSha256'], 'nativeFrames': len(meta['times']),
                       'nativeSeconds': meta['seconds'], 'serverTicks': meta['elapsedTicks']})
    filters.append(''.join(f'[v{i}]' for i in range(len(states))) +
                   f'concat=n={len(states)}:v=1:a=0,format=yuv420p[out]')
    destination = root / (reel_name + '.mp4')
    subprocess.run([ffmpeg, '-hide_banner', '-loglevel', 'error', '-y', *inputs, *captions,
        '-filter_complex', ';'.join(filters), '-map', '[out]', '-an', '-c:v', 'libx264',
        '-crf', '18', '-movflags', '+faststart', str(destination)], check=True)
    subprocess.run([ffmpeg, '-hide_banner', '-loglevel', 'error', '-i', str(destination),
                    '-f', 'null', '-'], check=True)
    (root / (reel_name + '.json')).write_text(json.dumps({
        'source': 'Actual Minecraft framebuffer recordings; native game image retained at 960x540',
        'captionPlacement': 'Additional forty-pixel band above the game image',
        'size': [960, 580], 'audio': False, 'states': ledger,
        'sha256': hashlib.sha256(destination.read_bytes()).hexdigest(),
        'decodeVerification': 'Complete reel decoded successfully'}, indent=2) + '\n')
    print(f'{len(states)} native ritual states combined and decoded; silent captions above the game image.')


if __name__ == '__main__':
    main()
