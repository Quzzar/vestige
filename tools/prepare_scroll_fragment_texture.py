#!/usr/bin/env python3
"""Import the approved tiny paper scrap to native resolution; --check verifies exact output."""
import argparse
import io
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "docs/art/scroll-fragments"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    source = Image.open(ART / "options-v1/02-torn-corner-v2.png").convert("RGBA")
    # Remove nearly invisible generated fringe before measuring the transparent margin.
    source.putalpha(source.getchannel("A").point(lambda a: 255 if a >= 128 else 0))
    bounds = source.getbbox()
    cx, cy = (bounds[0] + bounds[2]) / 2, (bounds[1] + bounds[3]) / 2
    side = max(bounds[2] - bounds[0], bounds[3] - bounds[1]) * 16 / 8
    box = tuple(round(v) for v in (cx - side / 2, cy - side / 2, cx + side / 2, cy + side / 2))
    texture = source.crop(box).resize((16, 16), Image.Resampling.NEAREST)
    assert set(texture.getchannel("A").tobytes()) == {0, 255}
    images = {ROOT / "src/main/resources/assets/vestige/textures/item/scroll_fragment.png": texture,
              ART / "texture-preview.png": texture.resize((256, 256), Image.Resampling.NEAREST)}
    for path, image in images.items():
        output = io.BytesIO()
        image.save(output, format="PNG")
        if args.check:
            assert path.read_bytes() == output.getvalue(), f"Texture drift: {path}"
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(output.getvalue())
    print("Native 16x16 fragment texture verified")


if __name__ == "__main__":
    main()
