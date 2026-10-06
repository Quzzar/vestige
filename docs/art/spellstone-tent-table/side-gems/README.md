# Four gemstones on the vertical rim

**Rejected interpretation, October 6:** the owner meant texture details wrapping the vertical edge corners, and rejected this side-center sprite's placement and style. See the [four corner alternatives](../../spellstone-corner-details/README.md). This page preserves the source and verification of the earlier implemented baseline.

The owner clarified that “diamond edge” meant **four separate gemstones on the vertical sides of the top stone**, with bare stone between them. The connected-line texture was a rejected interpretation. The rejected model put one cut gem at the center of each north/south/east/west rim face; the top stays clear for scrolls and the native Astral Seal.

The [native 32×32 RGBA sprite](native-gem.png) comes from the built-in image generation tool. Its [exact prompt](generation-prompt.txt), unchanged [generated source](generated-gem.png) and [asset evidence](asset-evidence.json) are retained. The only bitmap conversion is nearest-neighbor resizing to 32×32, with source alpha retained. Model UVs crop the clear margin around the isolated gem, without changing its artwork. Four flat cutout faces map it onto the cap's rim; it adds no collision. No connecting strips, top border or Diamond block tile is packaged in this iteration.

Reproduce the native size conversion:

```bash
ffmpeg -hide_banner -loglevel error -y -i docs/art/spellstone-tent-table/side-gems/generated-gem.png -vf scale=32:32:flags=neighbor -frames:v 1 src/main/resources/assets/vestige/textures/block/spellstone_diamond_gem.png
```

[Historical Minecraft screenshots and verification](../README.md) show the sprite on the actual registered Spellstone blocks, including Stone Brick, Tuff and Quartz finishes.
