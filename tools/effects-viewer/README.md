# Vestige spell casts

Browse the 214-spell catalog and watch actual casts recorded inside Minecraft. Every spell has a recorded primary cast. The full catalog is visible by default, including area boundaries, ordered chains, physical knockback, live entity copies, a combat companion and growing/retracting terrain. There are no browser sketches or effect-study substitutes.

```bash
bun install --frozen-lockfile
bun run dev
bun test
bun run build
```

Keep the server running while using [localhost:5175](http://127.0.0.1:5175/). Search/filter, choose a spell, use video controls, or download its MP4. Recorded subject health and movement come from the game. **Copy cast command** provides the native operator entrypoint.

From the repository root, `python3 tools/export_spell_effects.py` regenerates metadata; `--check` verifies it. [The guide](../../docs/effects-workshop.md) documents native capture and verification. The viewer plays saved footage; it does not execute Minecraft.
