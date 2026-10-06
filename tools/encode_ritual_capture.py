#!/usr/bin/env python3
"""Archive and encode actual native ritual frames at their measured elapsed times."""
import argparse
import hashlib
import json
import shutil
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('captures',type=Path)
    parser.add_argument('destination',type=Path)
    parser.add_argument('--build-directory',type=Path,default=ROOT/'build')
    args=parser.parse_args(); ffmpeg=shutil.which('ffmpeg')
    if not ffmpeg: raise SystemExit('ffmpeg is required')
    args.destination.mkdir(parents=True,exist_ok=True)
    for folder in sorted(args.captures.iterdir()):
        source=folder/'capture.json'
        if not source.is_file(): continue
        meta=json.loads(source.read_text());times=meta['times']
        assert meta['kind']=='ritual' and meta['source']=='Minecraft main render target'
        assert len(times)>=2 and all(a<b for a,b in zip(times,times[1:]))
        if 'ritualCraftingSha256' in meta:
            compiled=args.build_directory/'classes/java/main/com/quzzar/vestige/apparatus/RitualCrafting.class'
            assert hashlib.sha256(compiled.read_bytes()).hexdigest()==meta['ritualCraftingSha256'], 'Loaded ritual gameplay differs from the verified build'
        for path,digest in meta['assetSha256'].items():
            assert hashlib.sha256((ROOT/'src/main/resources/assets/vestige'/path).read_bytes()).hexdigest()==digest
        lines=['ffconcat version 1.0']
        for i,time in enumerate(times):
            assert (folder/f'frame-{i:05d}.png').is_file()
            lines += [f"file 'frame-{i:05d}.png'",f"duration {(times[i+1]-time) if i+1<len(times) else 1/30:.9f}"]
        lines += [f"file 'frame-{len(times)-1:05d}.png'"]
        concat=folder/'frames.ffconcat';concat.write_text('\n'.join(lines)+'\n')
        name=meta['spell'].split(':')[1]; video=args.destination/f'{name}.mp4'
        subprocess.run([ffmpeg,'-hide_banner','-loglevel','error','-y','-safe','1','-f','concat','-i',str(concat),'-vf','fps=30','-an','-c:v','libx264','-crf','23','-pix_fmt','yuv420p','-movflags','+faststart',str(video)],check=True)
        shutil.copy2(folder/f'frame-{min(len(times)-1,max(1,len(times)//3)):05d}.png',args.destination/f'{name}.png')
        archived=meta|{'videoSha256':hashlib.sha256(video.read_bytes()).hexdigest(),'audio':False}
        (args.destination/f'{name}.json').write_text(json.dumps(archived,indent=2)+'\n')
        print(f'{name}: {len(times)} native frames, {meta["outcome"]}, {meta["elapsedTicks"]} server ticks')
if __name__=='__main__': main()
