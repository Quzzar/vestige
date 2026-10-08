#!/usr/bin/env python3
"""Export the imagegen blue fissures while retaining all unaffected v9 texels."""
from collections import Counter
from pathlib import Path
import hashlib
import json
from PIL import Image

ART = Path(__file__).resolve().parent
PRIOR = ART.parent / 'dissentient-diamond-v9/dissentient-diamond-16.png'
prior = Image.open(PRIOR).convert('RGBA')
source = Image.open(ART / 'source.png').convert('RGBA')
cracks = {(x,y) for y in range(16) for x in range(16)
          if (p:=prior.getpixel((x,y)))[3] and max(p[:3]) < 40}
assert len(cracks) == 13
palette = ((21,83,101), (16,53,72))
sample = source.resize((16*32,16*32),Image.Resampling.NEAREST)
sprite = prior.copy()
sampled = []
for x,y in sorted(cracks):
    values = [sample.getpixel((px,py))[:3]
              for py in range(y*32+8,y*32+24)
              for px in range(x*32+8,x*32+24)
              if sample.getpixel((px,py))[3] >= 128]
    assert values, (x,y)
    color = Counter(values).most_common(1)[0][0]
    aligned = min(palette,key=lambda p: sum((p[i]-color[i])**2 for i in range(3)))
    assert color[2] > color[0] and color[1] > color[0], (x,y,color)
    sprite.putpixel((x,y),(*aligned,255))
    sampled.append({'cell':[x,y],'generatedColor':list(color),
                    'paletteColor':'#%02x%02x%02x'%aligned})
assert {sprite.getpixel(p)[:3] for p in cracks} == set(palette)
changed = {(x,y) for y in range(16) for x in range(16)
           if sprite.getpixel((x,y)) != prior.getpixel((x,y))}
assert changed == cracks
assert sprite.getchannel('A').tobytes() == prior.getchannel('A').tobytes()
sprite.save(ART / 'dissentient-diamond-16.png')
sprite.resize((32,32),Image.Resampling.NEAREST).save(ART / 'dissentient-diamond-32.png')
records = {}
for size in (16,32):
    path = ART / f'dissentient-diamond-{size}.png'
    image = Image.open(path).convert('RGBA')
    records[str(size)] = {'file':path.name,'dimensions':list(image.size),
        'bounds':list(image.getbbox()),'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),
        'opaquePixels':sum(p[3]>0 for p in image.get_flattened_data()),
        'palette':sorted('#%02x%02x%02x'%p[:3] for p in set(image.get_flattened_data()) if p[3])}
(ART/'export.json').write_text(json.dumps({
    'generator':'Built-in image_gen localized edit','source':'source.png','prompt':'prompt.txt',
    'sourceSha256':hashlib.sha256((ART/'source.png').read_bytes()).hexdigest(),
    'basis':str(PRIOR.relative_to(ART.parent)),'editTarget':'edit-target-64x.png',
    'nativeGrid':[16,16],'changedCells':[list(p) for p in sorted(changed)],
    'changedPixelCount':len(changed),'alphaUnchanged':True,'allOtherPixelsUnchanged':True,
    'crackPalette':['#155365','#103548'],
    'export':'Sample requested imagegen crack cells; align to two blue shades; retain all unaffected v9 texels',
    'generatedSamples':sampled,'32pxArtifact':'Exact 2x nearest-neighbor derivative of the native 16px sprite, not a new 32px study',
    'artifacts':records,'ownerAccepted':False,'nativeReviewed':False
},indent=2)+'\n')
print('Exported thirteen generated crack pixels in two blue shades; all other v9 pixels and alpha unchanged.')
