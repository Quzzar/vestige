#!/usr/bin/env python3
"""Original 16px staff icon from an explicit native pixel grid; no foreign assets."""
from pathlib import Path
import argparse
import io
from PIL import Image, ImageColor

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "src/main/resources/assets/vestige/textures/item/staff.png"
PALETTE = dict(D="#3d294f", P="#79549d", L="#af82d1", H="#e0b7ec",
               X="#332b25", S="#60462f", M="#94704a", W="#b58a5c",
               I="#a3a9a7", B="#626866", T="#615078")
ROWS = (
    "................",
    "...........DD...",
    "..........DHLD..",
    "..........DLPD..",
    "..........DPPD..",
    ".........XBBD...",
    ".........IIX....",
    "........WMX.....",
    ".......WTX......",
    "......WMX.......",
    ".....WMX........",
    "....WMX.........",
    "...WMX..........",
    "..WMX...........",
    ".XSX............",
    "..X.............",
)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    assert len(ROWS) == 16 and all(len(row) == 16 for row in ROWS)
    image = Image.new("RGBA", (16, 16))
    for y, row in enumerate(ROWS):
        for x, key in enumerate(row):
            if key != ".": image.putpixel((x, y), ImageColor.getrgb(PALETTE[key]) + (255,))
    output = io.BytesIO(); image.save(output, format="PNG")
    if args.check:
        if not DEST.exists() or DEST.read_bytes() != output.getvalue():
            raise SystemExit("Staff texture differs from its authored grid")
    else:
        DEST.parent.mkdir(parents=True, exist_ok=True); DEST.write_bytes(output.getvalue())

if __name__ == "__main__": main()
