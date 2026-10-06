#!/usr/bin/env python3
"""Native 16px item refinements, including the selected R2a and S1a directions."""
from __future__ import annotations

import argparse
import hashlib
import io
import json
from zipfile import ZipFile

from PIL import Image, ImageDraw

from author_item_texture_studies import AGED, CRYSTAL, ROOT, VANILLA, authored, font

OUT = ROOT / "docs/art/native-item-textures-v2"
PARCHMENT = {key: color for key, color in AGED.items() if key in "XSMHI"}
STUDIES = {
    "scroll_closed": {
        "label": "R2a · Closed Roll", "kind": "scroll", "palette": PARCHMENT,
        "rows": (
            "................",
            ".........XXXX...",
            "........XHHIIX..",
            ".......XHISXMSX.",
            "......XHIIHSMHX.",
            ".....XHIIHMSSX..",
            "....XHIIHMSSX...",
            "...XHIIHMSSX....",
            "..XHIIHMSSX.....",
            ".XHIIHMSSX......",
            ".XIIHSMSSX......",
            ".XISXMSXX.......",
            "..XHSISX........",
            "...XXXX.........",
            "................",
            "................",
        ),
    },
    "scroll_loose": {
        "label": "R2b · Loose Fold", "kind": "scroll", "palette": PARCHMENT,
        "rows": (
            "........XXXX....",
            ".......XHIIHX...",
            "......XHISXMSX..",
            ".....XHIIHSMHX..",
            ".....XIIIHMSX...",
            "....XIIIHMSX....",
            "...XIIIHMSX.....",
            "..XIIIHMSX......",
            ".XIIIHMSX.......",
            ".XIIHMSX........",
            ".XIHMSXX........",
            "..XMSSMHX.......",
            "...XHIMSX.......",
            "....XXXX........",
            "................",
            "................",
        ),
    },
    "scroll_sideways": {
        "label": "R2c · Sideways Roll", "kind": "scroll", "palette": PARCHMENT,
        "rows": (
            "................",
            "................",
            "................",
            "........XXXXX...",
            "...XXXXXHIIHIX..",
            "..XHHIIIHIHMSSX.",
            ".XHIIHMMMMMSSSX.",
            ".XHISXSSSSSSSX..",
            ".XIMSHSSSSSSX...",
            "..XHMHXXXXXX....",
            "...XXX..........",
            "................",
            "................",
            "................",
            "................",
            "................",
        ),
    },
    "shard_splinter": {
        "label": "S1a · Splinter", "kind": "shard", "palette": CRYSTAL,
        "rows": (
            "................",
            "...........D....",
            "..........DID...",
            "..........DHMD..",
            ".........DLMPD..",
            ".......DDLHMPD..",
            "......DLMHMPD...",
            ".....DLMHTEPD...",
            ".....DMHTEPD....",
            "....DPMBEPD.....",
            "...DPMBEPD......",
            "...DMBEPD.......",
            "....DEPD........",
            "....DD..........",
            "................",
            "................",
        ),
    },
    "shard_cleaved": {
        "label": "S1b · Cleaved", "kind": "shard", "palette": CRYSTAL,
        "rows": (
            "................",
            "..........D.....",
            ".........DID....",
            "........DILMD...",
            "........DLMMPD..",
            ".....DDDLMPPDD..",
            "....DILMTEDPD...",
            "...DILMTEMPD....",
            "...DLMBEMMPD....",
            "...DMBEPMPD.....",
            "....DBEPMPD.....",
            "....DEMPPD......",
            ".....DMPD.......",
            ".....DDD........",
            "................",
            "................",
        ),
    },
    "shard_fractured": {
        "label": "S1c · Fractured", "kind": "shard", "palette": CRYSTAL,
        "rows": (
            "................",
            "...........D....",
            "..........DID...",
            ".........DHMD...",
            "........DLMPDD..",
            ".......DLMMPPD..",
            "......DLMTEPD...",
            ".....DILTEPD....",
            "....DLMTEPPD....",
            "...DLMTEPPD.....",
            "...DMTEPPD......",
            "....DEPPD.......",
            "....DDPD........",
            "......D.........",
            "................",
            "................",
        ),
    },
}

def refine(source: str, label: str, edits: tuple[tuple[int, int, str], ...] = ()) -> dict:
    base = STUDIES[source]
    rows = [list(row) for row in base["rows"]]
    for x, y, symbol in edits:
        assert 0 <= x < 16 and 0 <= y < 16
        assert symbol == "." or symbol in base["palette"]
        rows[y][x] = symbol
    return dict(base, label=label, rows=tuple("".join(row) for row in rows), base=source, edits=edits)

SELECTED_REFINEMENTS = {
    "scroll_r2a_base": refine("scroll_closed", "R2a · Selected base"),
    "scroll_clear_curls": refine("scroll_closed", "R2a1 · Clear Curls", (
        (9, 2, "I"), (10, 2, "I"), (8, 3, "I"), (10, 3, "X"),
        (7, 4, "I"), (11, 4, "M"), (12, 4, "S"), (13, 4, "S"),
        (6, 5, "I"), (5, 6, "I"), (4, 7, "I"), (3, 8, "I"),
        (2, 9, "I"), (5, 10, "M"), (6, 10, "S"), (5, 11, "X"),
        (3, 12, "I"), (4, 12, "H"),
    )),
    "scroll_layered_edge": refine("scroll_closed", "R2a2 · Layered Edge", (
        (12, 4, "X"), (13, 4, "M"), (11, 5, "H"),
        (10, 6, "H"), (9, 7, "H"), (8, 8, "H"), (7, 9, "H"),
        (6, 10, "H"), (5, 11, "H"), (5, 12, "H"),
        (10, 2, "I"), (3, 12, "I"),
    )),
    "shard_s1a_base": refine("shard_splinter", "S1a · Selected base"),
    "shard_sharp_tip": refine("shard_splinter", "S1a1 · Sharp Tip", (
        (12, 0, "D"), (11, 1, "H"), (12, 1, "D"),
        (11, 2, "H"), (11, 3, "L"),
        (8, 5, "L"), (9, 5, "H"), (10, 5, "L"),
        (7, 7, "H"), (8, 7, "M"), (6, 8, "L"), (7, 8, "M"),
        (5, 10, "L"), (4, 11, "P"),
    )),
    "shard_chipped_edge": refine("shard_splinter", "S1a2 · Chipped Edge", (
        (13, 5, "."), (12, 5, "D"), (5, 6, "D"), (6, 6, "M"),
        (5, 8, "."), (6, 8, "D"), (4, 12, "."), (5, 12, "D"),
        (6, 7, "I"), (8, 7, "M"), (9, 7, "B"), (8, 8, "E"),
    )),
}

# Fresh silhouettes after the owner rejected the narrow R2a scrolls. The red
# binding is a visual material choice from their reference, not a spell trait.
BOUND_PAPER = dict(PARCHMENT, D="#791c27", R="#d2443f", F="#ef6d62", K="#a82c47")
REBUILT_SCROLLS = {
    "scroll_ribbon": {
        "label": "R3a · Ribbon Roll", "kind": "scroll", "palette": BOUND_PAPER,
        "rows": (
            "................",
            "...XSSX.........",
            "..XMIHMS........",
            ".XMIHHMSX.......",
            ".XHMIHHMSX......",
            ".XHSXMHMSDX.....",
            "..XSMXMSDRRX....",
            "...XXSSDRFRX....",
            "....XSDRFRDSX...",
            ".....DRFRDHMMS..",
            "....DRFRDMIHMSX.",
            "....RFRDSMHXMSX.",
            "....RRD.XSXMHMX.",
            ".....D...XSXMSX.",
            "..........XSSX..",
            "................",
        ),
    },
    "scroll_cord": {
        "label": "R3b · Bound Parchment", "kind": "scroll", "palette": BOUND_PAPER,
        "rows": (
            "................",
            "...XSSX.........",
            "..XMIHMS........",
            ".XMIHHMSX.......",
            ".XHMIHHMSX......",
            ".XHSXMHMSX......",
            "..XSMXMSKDX.....",
            "...XXSSKFRSX....",
            "....XSKFRSHSX...",
            ".....KFRSHHMMS..",
            ".....DDXSMIHMSX.",
            ".....RX.SMHXMSX.",
            ".....D..XSXMHMX.",
            ".........XSXMSX.",
            "..........XSSX..",
            "................",
        ),
    },
    "scroll_wide_roll": {
        "label": "R3c · Compact Roll", "kind": "scroll", "palette": BOUND_PAPER,
        "rows": (
            "................",
            "................",
            ".........XXXXX..",
            "........XHMIHMSX",
            "......XXHMIHMSMX",
            "....XXHMIRDSXMX.",
            "...XHMIHRRDMSXX.",
            "..XHMIHRFDSSXX..",
            "..XHSXMRDDSX....",
            "..XMSHMDSSX.....",
            "...XSSRDXX......",
            "....XXDX........",
            "................",
            "................",
            "................",
            "................",
        ),
    },
}

# Owner-selected reuse of Electroblob's source art, with only its blue band
# recolored. Literal RGBA source cells retain even invisible source RGB values.
RED_SCROLL_SOURCE = {
    "palette": {
        "A": "#00000000", "B": "#791c27ff", "C": "#a82c47ff",
        "D": "#17050500", "E": "#d2443fff", "F": "#26080900",
        "G": "#380c0d00", "H": "#55131300", "I": "#5b5b5b00",
        "J": "#65171700", "K": "#99999900", "L": "#a06d0000",
        "M": "#a58747ff", "N": "#b1790c00", "O": "#b7b7b700",
        "P": "#c9bb8900", "Q": "#cab374ff", "R": "#d6d6d600",
        "S": "#eaddae00", "T": "#edeeb6ff",
    },
    "rows": (
        "AAAAAAAAAAAAAAAA", "AAAAAAAAFFFAAAAA", "AAAAAAFFJJJFAPPA",
        "AAAAFFJJJGJPPPSP", "AAFFJJJGJPPMMMPP", "FFJJJGPJJMMTTTMP",
        "FHPPPPMEEETTTQMP", "FPMMMMTQEECQQQMI", "FMQTMTTTQCCMMMKI",
        "DMMMQMTQQBBKOKGD", "AMTMQMQMMKKOGGDD", "NNMTTMMOKOGGDDAA",
        "ALAMMRKKGGDDAAAA", "AAAADHGGDDAAAAAA", "AAAAADDDAAAAAAAA",
        "AAAAAAAAAAAAAAAA",
    ),
}

def red_scroll_review(check: bool) -> None:
    out = ROOT / "docs/art/scroll-red-binding-v5"
    original_data = (ROOT / "docs/art/scroll-rebuild-v4/references/electroblob-scroll.png").read_bytes()
    blob = hashlib.sha1(f"blob {len(original_data)}\0".encode() + original_data).hexdigest()
    assert blob == "13d56d1bad50f42b6283d9041ad4eacd01c3a572"
    original = Image.open(io.BytesIO(original_data)).convert("RGBA")
    assert original.size == (16, 16)
    native = Image.new("RGBA", (16, 16))
    mapping = {
        (23, 62, 101, 255): (210, 68, 63, 255),
        (19, 52, 85, 255): (168, 44, 71, 255),
        (12, 35, 56, 255): (121, 28, 39, 255),
    }
    changed = []
    assert len(RED_SCROLL_SOURCE["rows"]) == 16
    for y, row in enumerate(RED_SCROLL_SOURCE["rows"]):
        assert len(row) == 16
        for x, cell in enumerate(row):
            rgba = tuple(bytes.fromhex(RED_SCROLL_SOURCE["palette"][cell][1:]))
            before = original.getpixel((x, y))
            assert rgba == mapping.get(before, before), (x, y)
            native.putpixel((x, y), rgba)
            if rgba != before:
                changed.append([x, y])
    assert len(changed) == 10
    assert set(native.getchannel("A").get_flattened_data()) == {0, 255}
    assert len({p for p in native.get_flattened_data() if p[3]}) == 6
    stream = io.BytesIO()
    native.save(stream, format="PNG", optimize=False)
    data = stream.getvalue()
    texture = out / "textures/spell_scroll.png"
    metadata = dict(
        RED_SCROLL_SOURCE, status="owner-selected native texture; not installed",
        dimensions=[16, 16], changedPixels=changed, changedPixelCount=10,
        sourceGitBlob=blob, sourceSha256=hashlib.sha256(original_data).hexdigest(),
        sha256=hashlib.sha256(data).hexdigest(),
        attribution="Electroblob; binding recolor for Vestige by Quzzar/Codex",
        license="../../../LICENSE.md",
        generation="Literal native RGBA grid; generated enlarged preview excluded",
    )
    content = json.dumps(metadata, indent=2) + "\n"
    if check:
        assert texture.read_bytes() == data, "Selected red scroll drift"
        assert (out / "pixel-source.json").read_text() == content
    else:
        texture.parent.mkdir(parents=True, exist_ok=True)
        texture.write_bytes(data)
        (out / "pixel-source.json").write_text(content)
        for dark in (True, False):
            bg, fg = ("#292929", "#ededed") if dark else ("#dddddd", "#303030")
            board = Image.new("RGB", (560, 352), bg)
            draw = ImageDraw.Draw(board)
            draw.text((24, 18), "Electroblob's scroll · red binding", font=font(24), fill=fg)
            draw.text((24, 52), "Actual 16×16 · only ten binding pixels changed", font=font(15), fill=fg)
            for x, icon, label in ((80, original, "Original · Blue"), (350, native, "Selected · Red")):
                large = icon.resize((128, 128), Image.Resampling.NEAREST)
                board.paste(large, (x, 94), large)
                draw.text((x, 230), label, font=font(16), fill=fg)
                board.paste(slot(icon), (x, 265))
            draw.text((24, 326), "Electroblob source art · texture review · not installed", font=font(13), fill=fg)
            board.save(out / f"comparison-{'dark' if dark else 'light'}.png")
    print(f"{'Checked' if check else 'Authored'} selected native 16x16 scroll: exactly 10 blue pixels recolored; all other RGBA cells identical.")

def slot(icon: Image.Image) -> Image.Image:
    cell = Image.new("RGB", (18, 18), "#8b8b8b")
    draw = ImageDraw.Draw(cell)
    draw.line((0, 0, 17, 0), fill="#373737")
    draw.line((0, 0, 0, 17), fill="#373737")
    draw.line((1, 17, 17, 17), fill="#ffffff")
    draw.line((17, 1, 17, 17), fill="#ffffff")
    cell.paste(icon, (1, 1), icon)
    return cell.resize((36, 36), Image.Resampling.NEAREST)

def comparison(kind: str, refs: dict[str, Image.Image], dark: bool, pass_number: int = 2) -> Image.Image:
    background, text = ("#292929", "#ededed") if dark else ("#dddddd", "#303030")
    result = Image.new("RGB", (660, 596), background)
    draw = ImageDraw.Draw(result)
    title = "Plain parchment scrolls" if kind == "scroll" else "Bound shard refinements"
    if pass_number == 3:
        title = "R2a parchment refinements" if kind == "scroll" else "S1a splinter refinements"
    elif pass_number == 4:
        title = "Rebuilt spell scrolls"
    draw.text((24, 16), title, font=font(24), fill=text)
    draw.text((24, 49), "Actual 16×16 PNGs · equal 8× enlargement", font=font(15), fill=text)
    names = ("paper", "bone", "previous_r2") if kind == "scroll" else ("amethyst_shard", "echo_shard", "previous_s1")
    reference_title = "Vanilla references and previous R2" if kind == "scroll" else "Vanilla references and previous S1"
    if pass_number == 3:
        names = ("paper", "name_tag", "bone") if kind == "scroll" else ("amethyst_shard", "echo_shard", "prismarine_shard")
        reference_title = "Vanilla Minecraft 1.21.1"
    elif pass_number == 4:
        names = ("electroblob", "paper", "rejected_r2a")
        reference_title = "References · Electroblob / vanilla / rejected draft"
    draw.text((24, 82), reference_title, font=font(17), fill=text)
    for column, name in enumerate(names):
        x = 44 + 211 * column
        large = refs[name].resize((128, 128), Image.Resampling.NEAREST)
        result.paste(large, (x, 110), large)
        label = {"previous_s1": "Previous S1", "previous_r2": "Previous R2", "electroblob": "Electroblob's Scroll", "rejected_r2a": "Rejected R2a1"}.get(name, name.replace("_", " ").title())
        draw.text((x, 244), label, font=font(15), fill=text)
    draw.text((24, 280), "Selected base and two refinements" if pass_number == 3 else "Vestige texture drafts", font=font(17), fill=text)
    selected = REBUILT_SCROLLS if pass_number == 4 else SELECTED_REFINEMENTS if pass_number == 3 else STUDIES
    studies = [study for study in selected.values() if study["kind"] == kind]
    for column, study in enumerate(studies):
        x = 44 + 211 * column
        icon = authored(study)
        large = icon.resize((128, 128), Image.Resampling.NEAREST)
        result.paste(large, (x, 310), large)
        draw.text((x, 444), study["label"], font=font(15), fill=text)
        result.paste(slot(icon), (x, 493))
    draw.text((24, 546), "Small slots: 2× UI scale. No glint or emission applied.", font=font(15), fill=text)
    draw.text((24, 570), "Drafts for review · not installed", font=font(13), fill=text)
    return result

def run(check: bool, pass_number: int = 2) -> None:
    if pass_number == 5:
        red_scroll_review(check)
        return
    out = ROOT / "docs/art/scroll-rebuild-v4" if pass_number == 4 else ROOT / "docs/art/native-item-textures-v3" if pass_number == 3 else OUT
    selected = REBUILT_SCROLLS if pass_number == 4 else SELECTED_REFINEMENTS if pass_number == 3 else STUDIES
    if not check:
        out.mkdir(parents=True, exist_ok=True)
    refs = {}
    reference_hashes = {}
    with ZipFile(VANILLA) as jar:
        names = ("paper", "name_tag", "bone", "amethyst_shard", "echo_shard")
        if pass_number == 3:
            names += ("prismarine_shard",)
        elif pass_number == 4:
            names += ("red_dye",)
        for name in names:
            data = jar.read(f"assets/minecraft/textures/item/{name}.png")
            refs[name] = Image.open(io.BytesIO(data)).convert("RGBA")
            reference_hashes[name] = hashlib.sha256(data).hexdigest()
    previous_hashes = {}
    for name, filename in (("previous_s1", "shard_bound.png"), ("previous_r2", "scroll_aged.png")):
        data = (ROOT / "docs/art/native-item-textures/textures" / filename).read_bytes()
        refs[name] = Image.open(io.BytesIO(data)).convert("RGBA")
        previous_hashes[name] = hashlib.sha256(data).hexdigest()
    selected_bases = {}
    if pass_number == 3:
        for name, filename in (("scroll_r2a_base", "scroll_closed.png"), ("shard_s1a_base", "shard_splinter.png")):
            selected_bases[name] = (OUT / "textures" / filename).read_bytes()
    elif pass_number == 4:
        for name, path in (("electroblob", out / "references/electroblob-scroll.png"), ("rejected_r2a", ROOT / "docs/art/native-item-textures-v3/textures/scroll_clear_curls.png")):
            data = path.read_bytes()
            if name == "electroblob":
                blob_hash = hashlib.sha1(f"blob {len(data)}\0".encode() + data).hexdigest()
                assert blob_hash == "13d56d1bad50f42b6283d9041ad4eacd01c3a572", "Electroblob reference changed"
            refs[name] = Image.open(io.BytesIO(data)).convert("RGBA")
            previous_hashes[name] = hashlib.sha256(data).hexdigest()
    allowed = {p for ref in refs.values() for p in ref.get_flattened_data() if p[3]}
    ledger = {}
    for name, study in selected.items():
        image = authored(study)
        colors = {p for p in image.get_flattened_data() if p[3]}
        assert colors <= allowed and len(colors) <= 10, name
        assert set(image.getchannel("A").get_flattened_data()) == {0, 255}, name
        if study["kind"] == "scroll":
            palette = BOUND_PAPER if pass_number == 4 else PARCHMENT
            assert colors <= {tuple(bytes.fromhex(c[1:])) + (255,) for c in palette.values()}, name
        buffer = io.BytesIO()
        image.save(buffer, format="PNG", optimize=False)
        data = buffer.getvalue()
        if name in selected_bases:
            assert data == selected_bases[name], f"Selected baseline changed: {name}"
        path = out / "textures" / f"{name}.png"
        if check:
            assert path.read_bytes() == data, f"Texture drift: {name}"
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
        ledger[name] = dict(study, colors=len(colors), dimensions=[16, 16], sha256=hashlib.sha256(data).hexdigest())
    metadata = {"minecraft": "1.21.1", "status": "review drafts; not installed", "vanillaHashes": reference_hashes, "previousDraftHashes": previous_hashes, "studies": ledger}
    if selected_bases:
        metadata["selectedBaseHashes"] = {name: hashlib.sha256(data).hexdigest() for name, data in selected_bases.items()}
    if pass_number == 4:
        metadata["referenceProvenance"] = {
            "electroblob": {
                "url": "https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/resources/assets/ebwizardry/textures/items/scroll.png",
                "gitBlob": "13d56d1bad50f42b6283d9041ad4eacd01c3a572",
                "dimensions": [16, 16],
                "use": "unchanged visual reference only; outside packaged resources",
            },
        }
    content = json.dumps(metadata, indent=2) + "\n"
    path = out / "pixel-source.json"
    if check:
        assert path.read_text() == content, "Pixel-source drift"
    else:
        path.write_text(content)
        for kind in (("scroll",) if pass_number == 4 else ("scroll", "shard")):
            for dark in (True, False):
                comparison(kind, refs, dark, pass_number).save(out / f"{kind}-{'dark' if dark else 'light'}.png")
    print(f"{'Checked' if check else 'Authored'} {len(selected)} exact 16x16 RGBA sprites for pass {pass_number}.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--pass-number", choices=(2, 3, 4, 5), type=int, default=2)
    args = parser.parse_args()
    run(args.check, args.pass_number)
