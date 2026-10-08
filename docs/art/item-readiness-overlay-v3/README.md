# Vanilla mana and cooldown layers

October 8, 2026. **Owner-approved appearance:** “Yeah, this is fine. This is sufficient.” Both readiness layers use vanilla's translucent white and normal top-to-bottom clearing. [Open the animation](index.html). [Acceptance](approval.json) pins the reviewed preview, videos, source and verification. Production integration remains follow-up work.

Both fills use Minecraft's `0x7FFFFFFF` shade and its ordinary 16×16 cooldown rectangle. Mana and cooldown each advance independently. The overlapping portion receives both fills, making it more opaque; an area covered by only one condition receives one fill. No diagonal geometry, custom tint or new texture is used.

The five columns show vanilla cooldown, mana alone, cooldown alone, both and ready, using native inventory slots and item rendering. The example uses 12 mana and a 300-tick native client cooldown, with real server mana regeneration. These are display-study inputs rather than new Homebound Eye or robe abilities.

- [GUI scale 3 video](videos/vanilla-scale-3.mp4)
- [GUI scale 2 video](videos/vanilla-scale-2.mp4)
- [Close-up loop](vanilla-preview.gif)
- [Native source and verification](../../verification/item-readiness-vanilla-2026-10-08/README.md)

Silent videos retain the Minecraft framebuffer's native dimensions and observed elapsed timing. The GIF crops the comparison panel without resizing and reduces its palette. [Capture metadata](captures/capture.json) retains the original frames. Production UI and item mechanics are unchanged. The earlier [tint study](../item-readiness-overlay-v1/README.md) and [diagonal study](../item-readiness-overlay-v2/README.md) remain historical evidence; this requested direction supersedes those candidates.
