#!/usr/bin/env python3
"""Author the normal 16px Staff icons and their separate anchored guard geometry."""
from __future__ import annotations

import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DEST = ROOT / "src/main/resources/assets/vestige/models/item"
STAFFS = ("stick", "bamboo", "bone", "blaze_rod", "breeze_rod", "end_rod", "lightning_rod")


def face(uv: list[int]) -> dict[str, object]:
    return {side: {"uv": uv, "texture": "#layer0"} for side in ("north", "south", "east", "west", "up", "down")}


def cube(start: list[int], end: list[int], uv: list[int]) -> dict[str, object]:
    if any(value < -16 or value > 32 for value in (*start, *end)):
        raise ValueError("Minecraft item-model elements must stay within -16..32")
    return {"from": start, "to": end, "faces": face(uv)}


REGULAR = {
    "parent": "minecraft:item/handheld",
    "display": {
        "thirdperson_righthand": {"rotation": [0, -90, -35], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
        "thirdperson_lefthand": {"rotation": [0, 90, 35], "translation": [0, 4, 0.5], "scale": [0.85, 0.85, 0.85]},
        "firstperson_righthand": {"rotation": [0, -90, 0], "translation": [-3, 2, 0], "scale": [0.5, 0.5, 0.5]},
        "firstperson_lefthand": {"rotation": [0, 90, 0], "translation": [-3, 2, 0], "scale": [0.5, 0.5, 0.5]},
    },
}

# The center of the grip is Minecraft's normal generated-item hand point (8, 8, 8).  A Staff is not a walking cane:
# it extends well above the hand and down to the ground.  The deliberately long geometry is
# only used for the held guard pose; inventories retain the compact 16px icon.
GUARD = {
    "textures": {"particle": "#layer0"},
    "gui_light": "front",
    "elements": [
        cube([6, 6, 6], [10, 10, 10], [2, 13, 3, 14]),
        cube([7, -12, 7], [9, 32, 9], [8, 7, 9, 8]),
        cube([6, -16, 6], [10, -12, 10], [11, 3, 12, 4]),
    ],
    "display": {
        "thirdperson_righthand": {"rotation": [0, -90, 140], "translation": [0, 0, 0.5], "scale": [0.75, 0.75, 0.75]},
        "thirdperson_lefthand": {"rotation": [0, 90, -140], "translation": [0, 0, 0.5], "scale": [0.75, 0.75, 0.75]},
        "firstperson_righthand": {"rotation": [0, -90, 0], "translation": [-3, 2, 0], "scale": [0.5, 0.5, 0.5]},
        "firstperson_lefthand": {"rotation": [0, 90, 0], "translation": [-3, 2, 0], "scale": [0.5, 0.5, 0.5]},
    },
}


def encoded(data: dict[str, object]) -> bytes:
    return (json.dumps(data, indent=2) + "\n").encode()


def output() -> dict[Path, bytes]:
    files = {
        DEST / "mundane_staff.json": encoded(REGULAR),
        DEST / "mundane_staff_guard.json": encoded(GUARD),
    }
    for staff in STAFFS:
        files[DEST / f"{staff}_staff.json"] = encoded({
            "parent": "vestige:item/mundane_staff",
            "textures": {"layer0": f"vestige:item/{staff}_staff"},
            "overrides": [{"predicate": {"vestige:guarding": 1}, "model": f"vestige:item/{staff}_staff_guard"}],
        })
        files[DEST / f"{staff}_staff_guard.json"] = encoded({
            "parent": "vestige:item/mundane_staff_guard",
            "textures": {"layer0": f"vestige:item/{staff}_staff"},
        })
    return files


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    for path, expected in output().items():
        if args.check:
            if not path.exists() or path.read_bytes() != expected:
                raise SystemExit(f"Mundane Staff model differs from authored model: {path.name}")
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(expected)


if __name__ == "__main__":
    main()
