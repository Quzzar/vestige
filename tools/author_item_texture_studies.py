#!/usr/bin/env python3
"""Author new 16px item drafts from explicit pixel grids; compare vanilla unchanged."""
from __future__ import annotations

import argparse
import hashlib
import io
import json
from pathlib import Path
from zipfile import ZipFile

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs/art/native-item-textures"
VANILLA = ROOT / "build/moddev/artifacts/neoforge-21.1.72-minecraft-resources-aka-client-extra.jar"
REFERENCES = ("paper", "map", "book", "bone", "name_tag", "amethyst_shard", "echo_shard", "prismarine_shard", "lapis_lazuli", "diamond")

# Literal palettes from Minecraft 1.21.1, reused as color references. No source
# silhouettes or generated illustrations are resampled into the new sprites.
PAPER = dict(X="#878787", S="#aeaeae", M="#d6d6d6", H="#e9eaeb", I="#fcfcf2", P="#6f4fab", L="#b38ef3", D="#54398a")
AGED = dict(X="#5c4417", S="#ceac6d", M="#e6c78c", H="#e8e5d2", I="#fcfbed", P="#6f4fab", L="#b38ef3", D="#54398a")
CRYSTAL = dict(D="#54398a", P="#6f4fab", M="#8d6acc", L="#b38ef3", H="#cfa0f3", I="#fecbe6", E="#052a32", B="#034150", T="#0a5060", C="#009295")

SCROLL_ROLL = (
    "................",
    "..........XXXX..",
    ".........XIIIHX.",
    ".........XISXSX.",
    "........XIIHMSX.",
    ".......XIIHMSX..",
    "......XIIHMSX...",
    ".....XIIHMSX....",
    "....XIIHMSX.....",
    "...XIIHMSX......",
    "..XIIHMSX.......",
    ".XIIHMSX........",
    ".XISXSX.........",
    "..XMISX.........",
    "...XXX..........",
    "................",
)

def altered(rows: tuple[str, ...], edits: tuple[tuple[int, int, str], ...]) -> tuple[str, ...]:
    pixels = [list(row) for row in rows]
    for x, y, color in edits:
        if pixels[y][x] == ".":
            raise ValueError(f"An accent cannot extend the roll at {(x, y)}")
        pixels[y][x] = color
    return tuple("".join(row) for row in pixels)

STUDIES = {
    "scroll_clean": {
        "label": "R1 · Clean Roll", "kind": "scroll", "palette": PAPER,
        "rows": altered(SCROLL_ROLL, ((7, 7, "P"), (8, 7, "L"), (9, 7, "D"), (6, 8, "P"), (7, 8, "L"), (8, 8, "D"))),
    },
    "scroll_aged": {
        "label": "R2 · Parchment Roll", "kind": "scroll", "palette": AGED,
        "rows": altered(SCROLL_ROLL, ((8, 6, "P"), (9, 6, "D"), (7, 7, "L"), (8, 7, "P"), (7, 8, "D"), (8, 8, "P"))),
    },
    "scroll_inscribed": {
        "label": "R3 · Inscribed Roll", "kind": "scroll", "palette": PAPER,
        "rows": (
            "................",
            "............XX..",
            "..........XXIIX.",
            ".........XIIIHX.",
            "........XIIISIX.",
            "......XIIHMSX...",
            ".....XIIHMSX....",
            "....XIIPLMSX....",
            "...XIIIPMSX.....",
            "..XIIIPMSX......",
            ".XIIHMSXX.......",
            ".XIHMSX.........",
            ".XISXSX.........",
            "..XMISX.........",
            "...XXX..........",
            "................",
        ),
    },
    "shard_bound": {
        "label": "S1 · Bound Shard", "kind": "shard", "palette": CRYSTAL,
        "rows": (
            "................",
            "...........D....",
            "..........DID...",
            ".........DIHMD..",
            "........DIHMPD..",
            ".......DLHMPPD..",
            "......DLHMEPD...",
            ".....DLHMBEPD...",
            "....DLHMBEPD....",
            "...DLMBBEPD.....",
            "..DPMBTEPD......",
            "..DMBTEPD.......",
            "..DBTEPD........",
            "..DEEPD.........",
            "...DD...........",
            "................",
        ),
    },
    "shard_paired": {
        "label": "S2 · Paired Shard", "kind": "shard", "palette": CRYSTAL,
        "rows": (
            "................",
            "......D.........",
            ".....DID........",
            ".....DHLDD......",
            "....DLHMPD..D...",
            "....DLMMPD.DID..",
            "....DLMPPD.DHLD.",
            ".....DMPPD.LMPD.",
            ".....DMPPDLMPD..",
            ".....DMPDLMMPD..",
            ".....DBTBDMPD...",
            ".....EBBTBEPD...",
            "......EBTEE.....",
            ".......EEE......",
            "................",
            "................",
        ),
    },
    "shard_inscribed": {
        "label": "S3 · Etched Shard", "kind": "shard", "palette": CRYSTAL,
        "rows": (
            "................",
            ".........D......",
            "........DID.....",
            ".......DIHMD....",
            "......DLHMMPD...",
            ".....EDMMLPPD...",
            ".....EBMLMLPD...",
            "....EBTMMLPPD...",
            "....EBTMPLPD....",
            "....EBTMPLPD....",
            "....EBTMPPD.....",
            ".....EEMPD......",
            ".....EEPD.......",
            "......DD........",
            "................",
            "................",
        ),
    },
}

def authored(study: dict) -> Image.Image:
    rows = study["rows"]
    if len(rows) != 16 or any(len(row) != 16 for row in rows):
        raise ValueError(f"Not a 16x16 grid: {study['label']}")
    result = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, symbol in enumerate(row):
            if symbol != ".":
                result.putpixel((x, y), tuple(bytes.fromhex(study["palette"][symbol][1:])) + (255,))
    return result

def font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in ("/System/Library/Fonts/Supplemental/Arial.ttf", "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"):
        if Path(path).exists():
            return ImageFont.truetype(path, size)
    return ImageFont.load_default(size=size)

def comparison(kind: str, references: dict[str, Image.Image], dark: bool = True) -> Image.Image:
    bg = "#292929" if dark else "#dddddd"
    fg = "#ededed" if dark else "#303030"
    board = Image.new("RGB", (660, 596), bg)
    draw = ImageDraw.Draw(board)
    draw.text((24, 16), "Rolled spell scrolls" if kind == "scroll" else "Attunement Shards", font=font(24), fill=fg)
    draw.text((24, 49), "Actual 16×16 PNGs · all enlarged 8× equally", font=font(15), fill=fg)
    vanilla_names = ("paper", "name_tag", "book") if kind == "scroll" else ("amethyst_shard", "echo_shard", "prismarine_shard")
    draw.text((24, 82), "Vanilla Minecraft 1.21.1", font=font(17), fill=fg)
    for index, name in enumerate(vanilla_names):
        x = 44 + index * 211
        board.paste(references[name].resize((128, 128), Image.Resampling.NEAREST), (x, 110), references[name].resize((128, 128), Image.Resampling.NEAREST))
        draw.text((x, 244), name.replace("_", " ").title(), font=font(15), fill=fg)
    draw.text((24, 280), "Vestige texture drafts", font=font(17), fill=fg)
    for index, (name, study) in enumerate((pair for pair in STUDIES.items() if pair[1]["kind"] == kind)):
        x = 44 + index * 211
        icon = authored(study)
        enlarged = icon.resize((128, 128), Image.Resampling.NEAREST)
        board.paste(enlarged, (x, 310), enlarged)
        draw.text((x, 444), study["label"], font=font(15), fill=fg)
        # A familiar 16px item in an 18px Minecraft-style slot, displayed at
        # 2× UI scale. The art itself remains an unchanged transparent sprite.
        slot = Image.new("RGB", (18, 18), "#8b8b8b")
        slot_draw = ImageDraw.Draw(slot)
        slot_draw.line((0, 0, 17, 0), fill="#373737")
        slot_draw.line((0, 0, 0, 17), fill="#373737")
        slot_draw.line((1, 17, 17, 17), fill="#ffffff")
        slot_draw.line((17, 1, 17, 17), fill="#ffffff")
        slot.paste(icon, (1, 1), icon)
        board.paste(slot.resize((36, 36), Image.Resampling.NEAREST), (x, 493))
    draw.text((24, 546), "Small slots: 2× UI scale. No glint or emission applied.", font=font(15), fill=fg)
    draw.text((24, 570), "Drafts for review · not installed", font=font(13), fill=fg)
    return board

def run(check: bool = False) -> None:
    references = {}
    hashes = {}
    with ZipFile(VANILLA) as jar:
        for name in REFERENCES:
            path = f"assets/minecraft/textures/item/{name}.png"
            data = jar.read(path)
            references[name] = Image.open(io.BytesIO(data)).convert("RGBA")
            hashes[name] = hashlib.sha256(data).hexdigest()
            destination = OUT / "vanilla" / f"{name}.png"
            if check:
                assert destination.read_bytes() == data, f"Vanilla reference changed: {name}"
            else:
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes(data)
    allowed_colors = {pixel for ref in references.values() for pixel in ref.get_flattened_data() if pixel[3]}
    ledger = {}
    for name, study in STUDIES.items():
        texture = authored(study)
        palette = {pixel for pixel in texture.get_flattened_data() if pixel[3]}
        assert palette <= allowed_colors, f"Palette outside vanilla reference set: {name}"
        assert len(palette) <= 10, f"Excess color count: {name}"
        assert set(texture.getchannel("A").get_flattened_data()) == {0, 255}, name
        stream = io.BytesIO()
        texture.save(stream, format="PNG", optimize=False)
        data = stream.getvalue()
        destination = OUT / "textures" / f"{name}.png"
        if check:
            assert destination.read_bytes() == data, f"Authored texture drift: {name}"
        else:
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(data)
        ledger[name] = dict(study, colors=len(palette), sha256=hashlib.sha256(data).hexdigest(), dimensions=[16, 16])
    metadata = {"status": "review drafts; no production item texture selected", "minecraft": "1.21.1", "authorship": "independently authored explicit pixel grids; no generated image downsampling", "vanillaHashes": hashes, "studies": ledger}
    metadata_path = OUT / "pixel-source.json"
    metadata_text = json.dumps(metadata, indent=2) + "\n"
    if check:
        assert metadata_path.read_text() == metadata_text, "Pixel-source drift"
    else:
        metadata_path.write_text(metadata_text)
        for kind in ("scroll", "shard"):
            for dark in (True, False):
                comparison(kind, references, dark).save(OUT / f"{kind}-{'dark' if dark else 'light'}.png")
    print(f"{'Checked' if check else 'Authored'} {len(ledger)} native 16x16 RGBA sprites; unchanged vanilla references; palettes <=10 colors.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    run(parser.parse_args().check)
