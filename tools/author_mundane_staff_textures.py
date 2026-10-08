#!/usr/bin/env python3
"""Original 16px mundane-staff sprites from small authored pixel programs; no foreign assets."""
from pathlib import Path
import argparse
import io
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "src/main/resources/assets/vestige/textures/item"

PALETTES = {
    "stick": ("#2f1c12", "#604027", "#96643a", "#bd8750"),
    "bamboo": ("#31451d", "#5f7930", "#96ad4a", "#cad56c"),
    "bone": ("#5f5747", "#a79b7d", "#d7caa0", "#f1e4b8"),
    "blaze_rod": ("#7d2516", "#c84a19", "#ef8d24", "#ffd85a"),
    "breeze_rod": ("#24515b", "#438b92", "#72c7cc", "#c0f0ea"),
    "end_rod": ("#35313e", "#756886", "#c2a55d", "#f5e49a"),
    "lightning_rod": ("#5d2f1d", "#9a4d2a", "#d9813c", "#f2bd65"),
}

LEATHER = {
    "dark": (62, 37, 25),
    "mid": (111, 66, 37),
    "light": (161, 101, 55),
}

def pixel(image, x, y, color):
    if 0 <= x < 16 and 0 <= y < 16:
        image.putpixel((x, y), color + (255,))

def pixels(image, points, color):
    for x, y in points:
        pixel(image, x, y, color)

def paint_shaft(image, axis, dark, mid, light):
    """A two-pixel diagonal: a narrow material face with one dark trailing edge."""
    for index, (x, y) in enumerate(axis):
        pixel(image, x, y + 1, dark)
        pixel(image, x, y, light if index in {2, 6, len(axis) - 2} else mid)
    # All mundane staffs have a small wrapped handhold, but it no longer turns the entire item into a club.
    for index, (x, y) in enumerate(axis[:4]):
        pixel(image, x, y, LEATHER["light"] if index % 2 == 0 else LEATHER["mid"])
        pixel(image, x, y + 1, LEATHER["dark"])


def sprite(kind, colors):
    dark, mid, light, highlight = tuple(tuple(int(c[i:i + 2], 16) for i in (1, 3, 5)) for c in colors)
    image = Image.new("RGBA", (16, 16))
    axis = [(2 + i, 13 - i) for i in range(10)]
    if kind == "stick":
        # A crooked branch with a forked tip.
        axis = [(2, 13), (3, 12), (4, 11), (5, 10), (6, 9), (7, 8), (8, 7), (9, 6), (10, 5), (10, 4), (11, 3)]
    elif kind == "lightning_rod":
        # The copper body keeps a deliberate angular jog before its lightning-prong cap.
        axis = [(2, 13), (3, 12), (4, 11), (5, 10), (6, 9), (7, 8), (7, 7), (9, 7), (8, 5), (10, 5), (9, 3)]
    paint_shaft(image, axis, dark, mid, light)
    pixels(image, ((1, 14), (2, 15), (3, 15)), dark)
    pixel(image, 2, 14, highlight)

    # Each head has a distinct mundane silhouette and keeps the shaft visibly narrow in inventory and in hand.
    if kind == "stick":
        pixels(image, ((11, 3), (12, 2), (13, 1), (12, 1), (12, 0), (13, 2)), dark)
        pixels(image, ((12, 2), (13, 1)), mid)
    elif kind == "bamboo":
        for x, y in (axis[3], axis[6], axis[8]):
            pixels(image, ((x, y), (x, y + 1), (x + 1, y)), dark)
        pixels(image, ((11, 4), (12, 3), (13, 2), (14, 2)), dark)
        pixels(image, ((12, 3), (13, 2)), highlight)
    elif kind == "bone":
        pixels(image, ((11, 4), (12, 3), (12, 2), (13, 1), (14, 1), (14, 2), (13, 3), (12, 4)), dark)
        pixels(image, ((12, 3), (13, 2), (13, 1)), highlight)
        pixel(image, 14, 2, light)
    elif kind == "blaze_rod":
        for x, y in (axis[3], axis[6], axis[8]):
            pixels(image, ((x, y), (x, y + 1)), highlight)
        pixels(image, ((11, 4), (12, 3), (12, 2), (13, 1), (14, 1), (14, 2), (13, 3)), dark)
        pixels(image, ((12, 3), (13, 2), (14, 2)), light)
        pixel(image, 13, 1, highlight)
    elif kind == "breeze_rod":
        pixels(image, ((11, 4), (12, 3), (12, 2), (13, 1), (14, 2), (14, 3), (13, 4), (12, 4)), dark)
        pixels(image, ((12, 3), (13, 2), (14, 3), (13, 4)), highlight)
        pixel(image, 13, 3, mid)
    elif kind == "end_rod":
        pixels(image, ((11, 4), (12, 3), (13, 2), (14, 2), (14, 3), (13, 4), (12, 4)), dark)
        pixels(image, ((12, 3), (13, 2), (14, 3)), light)
        pixel(image, 13, 1, highlight)
    elif kind == "lightning_rod":
        pixels(image, ((10, 3), (11, 2), (12, 2), (12, 1), (13, 1), (13, 0), (14, 0)), dark)
        pixels(image, ((11, 2), (12, 1), (13, 1)), highlight)
    return image

def encoded(kind, colors):
    output = io.BytesIO(); sprite(kind, colors).save(output, format="PNG"); return output.getvalue()

def main():
    parser = argparse.ArgumentParser(); parser.add_argument("--check", action="store_true"); args = parser.parse_args()
    for kind, colors in PALETTES.items():
        path = DEST / f"{kind}_staff.png"; expected = encoded(kind, colors)
        if args.check:
            if not path.exists() or path.read_bytes() != expected: raise SystemExit(f"Mundane Staff texture differs from authored sprite: {path.name}")
        else:
            path.parent.mkdir(parents=True, exist_ok=True); path.write_bytes(expected)

if __name__ == "__main__": main()
