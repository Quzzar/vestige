# Native mana item overlays · October 7, 2026

Actual Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures from a disposable Survival world, with Kithkyn co-loaded. The production mana HUD is absent. Native item decorators reproduce vanilla cooldown shading from private, server-compiled prices and the hidden pooled balance.

`mana-overlay-demo.gif` and `.mp4` enlarge the actual hotbar crops. Captions are outside game pixels. The five-frame-per-second source is played at 12.5 frames per second, so regeneration is a **2.5× time-lapse**. The labels describe affordability rather than displaying the pooled balance. `*-native.png` preserve unmodified framebuffer stills at 1920×1080 and 1280×720, including the real inventory menu. `mana-symbol-preview.png` enlarges the actual 9×9 transparent GUI asset with nearest-neighbor scaling.

The hotbar contains, left to right: Shield / 6 mana, Force Arrow / 14, Fireball / 28, Greater Heal / 42, Fireball with a 2× leyline cost / 56, an Ensorcelled Fireball wand / 24, a staff selected to Fireball / 28, a mana Homebound Eye / 30, and spare Shield scrolls / 6. These numbers document the fixture; they are not drawn on the HUD. At fourteen mana, Shield and Force Arrow are clear while the other sources show different shortages. Selecting the staff's Force Arrow binding clears its shade immediately.

`capture.json` records all 170 raw frames, real private mana amounts and item shortages. Ten real Shield casts spend sixty mana and consume ten scrolls, while another Shield remains affordable and clear. Six additional casts leave four mana and a partially shaded spare scroll. Real regeneration then clears the cheaper sources before Greater Heal and the double-cost Fireball, while total mana remains below full.

The inspections verified hotbar and inventory geometry, empty/partial/affordable states, staff selection, count independence, actual payment, regeneration, two window/GUI sizes, readable vanilla counts and the absence of a pooled mana meter/number. The shared violet rune is retained as the actual PNG at `src/main/resources/assets/vestige/textures/gui/mana.png`; enabled native pixels match the preceding procedural rune.

The separately accepted sixty-second wand source recovery is retained pending the owner's optional decision. This overlay communicates mana affordability, while other payment and cast conditions remain server checks. Remote multiplayer was not exercised. [Verification](../../verification/item-mana-overlay-2026-10-07/README.md) pins the tested source and staged artifact.
