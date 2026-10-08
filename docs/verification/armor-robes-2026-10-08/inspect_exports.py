"""Diagnostic boards only: enlarge the exact production and sampled face pixels."""
from pathlib import Path
import importlib.util
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[3]
ART = ROOT / 'docs/art/magic-equipment-armor-v2'
spec = importlib.util.spec_from_file_location('robe_export', ROOT / 'tools/author_robe_armor.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
board = Image.new('RGB', (1056, 800), '#454545')
draw = ImageDraw.Draw(board)
for row, name in enumerate(module.REGIONS):
    draw.text((15, row*400+10), name+' / exact exported native face pixels', fill='white')
    for col, (key, face) in enumerate(module.faces(name).items()):
        draw.text((15+col*140, row*400+35), key, fill='white')
        board.paste(face.resize((face.width*14, face.height*14), Image.Resampling.NEAREST),
                    (15+col*140, row*400+55))
    base = ROOT / 'src/main/resources/assets/vestige/textures/models/armor'
    cloth = Image.open(base / (name+'_layer_1.png')).convert('RGBA')
    trim = Image.open(base / (name+'_layer_1_overlay.png')).convert('RGBA')
    atlas = Image.alpha_composite(cloth, trim)
    large = atlas.resize((256, 256), Image.Resampling.NEAREST)
    board.paste(large, (780, row*400+55), large)
    atlas_board = Image.new('RGB', (1080, 600), '#383838')
    labels = ImageDraw.Draw(atlas_board)
    for x, color in ((12, '#353535'), (548, '#dfdfdf')):
        atlas_board.paste(color, (x, 48, x+512, 560))
        large = atlas.resize((512, 512), Image.Resampling.NEAREST)
        atlas_board.paste(large, (x, 48), large)
    labels.text((12, 12), name+' / exact 64x64 production atlas / 8x nearest-neighbor', fill='white')
    labels.text((12, 575), 'Native texel density; diagnostic board, not a game screenshot', fill='white')
    atlas_board.save(ART / (name+'-atlas-8x.png'))
board.save(ART / 'exported-pixels.png')
