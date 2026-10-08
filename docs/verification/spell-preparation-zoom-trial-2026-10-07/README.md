# Spell charging zoom review · October 7, 2026

**Later owner decision:** use zoom in place of the edge, reduced to 3%. This 8% comparison remains historical trial evidence; see the [accepted preparation design](../../design/spell-preparation.md).

The owner suggested trying a slight camera zoom instead of the expanding crosshair edge. This was an isolated review candidate. At capture time the shared checkout retained the previously accepted edge; the testing pack was not changed.

The candidate gradually narrows FOV by up to 8% over the final composed preparation time. Minecraft's normal camera interpolation smooths the transition and return. The held source keeps its straight draw-back; the native crosshair is unchanged. Exact held-source matching, first-person camera, alive/non-spectator state and no open menu gate the camera cue. The FOV Effects setting scales the zoom and zero disables it. Existing movement/bow/mod modifiers compose multiplicatively. Payment, durability, resources and automatic release are unchanged.

**Actually verified:** client compilation, 271 native Minecraft framebuffer captures and eleven integrated-server cast/cancel checks. Scroll, offhand, wand, left-hand staff, cancellation, two display/GUI sizes, snow/night, zero FOV Effects and third person are captured. Every view returns to 70 degrees; ordinary charges reach approximately 64.4–65 degrees, and cancellation returns without payment. FOV Effects zero and third person stay at 70 throughout. [Recorded values](verification.json) and [all frame metadata](capture.json) retain the measured values. No model/runtime tests or broad world suite were rerun for this isolated presentation experiment.

The local pinned Minecraft 1.21.1 `AbstractClientPlayer.getFieldOfViewModifier` narrows the fully drawn bow's modifier by 15%. `GameRenderer.tickFov` eases halfway toward the target each tick. The pinned NeoForge 21.1.72 `ComputeFovModifierEvent` applies the player's FOV Effects setting before additional modifiers. Those local primary sources establish the native reference; the candidate uses a smaller maximum and the actual spell time rather than a bow's fixed twenty ticks.

[Native comparison footage](../../art/spell-preparation-zoom-trial/charging-comparison.mp4) plays the existing edge first and the isolated zoom trial second at recorded frame times. Titles are outside gameplay. Frames are downscaled by exactly one half with nearest-neighbor sampling; no game UI or camera movement is synthesized. Raw frames remain at `build/zoom-charge-trial-native`. Representative unmodified native stills are beside the clip. Source files are retained in `source/`; the inspection-only FOV recorder should be removed before any production adoption.

This captures first-person appearance and measured cleanup. It does not establish which cue feels better during moving combat, aiming or long play sessions. The owner subsequently selected zoom and requested the accepted 3% maximum.
