#!/usr/bin/env python3
"""Author the original fragment font from explicit pixel glyphs; --check detects drift."""
import argparse
import io
import json
import math
import re
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/vestige"


def outputs():
    symbols = json.loads((ROOT / "tools/fragment-symbols.json").read_text())
    # The catalog is open. This only checks the complete built-in presentation set.
    catalog = (ROOT / "docs/design/trait-catalog.md").read_text().split("## Built-in traits")[1].split("## Rarity")[0]
    expected = {"vestige:" + name for name in re.findall(r"^- `([^`]+)`", catalog, re.M)}
    shipped = set()
    for path in (ROOT / "src/main/resources/data/vestige/runtime_spells").glob("*.json"):
        shipped.update(json.loads(path.read_text()).get("traits", {}))
    assert set(symbols) == expected | shipped, "Missing or surplus catalog/shipped trait glyphs"
    assert len({entry["glyph"] for entry in symbols.values()}) == len(symbols)
    assert len({tuple(entry["pixels"]) for entry in symbols.values()}) == len(symbols), "Repeated glyph silhouette"
    atlas = Image.new("RGBA", (80, math.ceil(len(symbols) / 10) * 8))
    chars = []
    for index, entry in enumerate(symbols.values()):
        assert len(entry["glyph"]) == 1 and 0xE100 <= ord(entry["glyph"]) <= 0xF8FF
        rows = entry["pixels"]
        assert len(rows) == 7 and all(len(row) == 7 and set(row) <= {".", "#"} for row in rows)
        assert any("#" in row for row in rows)
        x0, y0 = index % 10 * 8, index // 10 * 8
        for y, row in enumerate(rows):
            for x, pixel in enumerate(row):
                if pixel == "#":
                    atlas.putpixel((x0 + x, y0 + y), (255, 255, 255, 255))
        chars.append(entry["glyph"])
    png = io.BytesIO()
    atlas.save(png, format="PNG")
    font = {"providers": [{"type": "bitmap", "file": "vestige:font/fragment_symbols.png", "height": 8,
                           "ascent": 7, "chars": ["".join(chars[i:i + 10]).ljust(10, "\0") for i in range(0, len(chars), 10)]}]}
    mapping = {trait: entry["glyph"] for trait, entry in symbols.items()}
    return {ASSETS / "textures/font/fragment_symbols.png": png.getvalue(),
            ASSETS / "font/fragment_symbols.json": (json.dumps(font, indent=2) + "\n").encode(),
            ASSETS / "fragment_symbols.json": (json.dumps(mapping, indent=2) + "\n").encode()}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    for path, content in outputs().items():
        if args.check:
            assert path.is_file() and path.read_bytes() == content, f"Generated asset drift: {path}"
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(content)
    print("Fragment glyph coverage, unique silhouettes and packaged assets verified" if args.check else "Authored fragment trait glyphs")


if __name__ == "__main__":
    main()
