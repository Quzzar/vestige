# Spell preparation display

**Owner locked the reviewed result October 8, 2026 (implemented October 7):** initial spell preparation uses a subtle camera zoom in place of the expanding crosshair edge. The maximum FOV reduction is 3%, reduced from the reviewed 8% candidate. The held scroll, wand or staff retains its straight draw-back toward the player. The native crosshair keeps its ordinary shape, color and rendering. The separate bar and expanding edge are removed.

## Subtle charge zoom

The camera gradually narrows its current FOV by up to 3% over the final composed preparation time, with a cubic smoothstep matching the held-item movement. Minecraft's native FOV interpolation smooths the transition and return after release or cancellation. The modifier composes multiplicatively with ordinary movement/bow/mod FOV effects. The player's FOV Effects setting scales the cue; zero disables the zoom while retaining the held-item draw-back.

Zoom applies only to the local player's exact reserved held source in first person, while alive, non-spectating and without an open menu. Replacing the source, opening a menu, changing camera or ending preparation resets the target modifier; the camera returns through Minecraft's native interpolation. Instant casts, delayed effects, active channels, dormant recasts and operator casts without a held-source reservation add no preparation zoom. Third-person gestures and hold-to-release controls remain separate decisions.

The crosshair, resource-pack sprite and vanilla sword attack indicator remain managed by Minecraft. Preparation adds no ring, bar, label, percentage, custom color or sound.

Progress comes from the runtime's final composed `Time` costs after source/shaping changes. Private version-2 server snapshots contain a cast UUID, elapsed ticks and total ticks, plus the reserved hand and an opaque item/source fingerprint for held casts, without spell identity or undiscovered facts. While preparing, the server sends at most one snapshot per tick to the caster; one empty snapshot clears it at the end. The item transform interpolates within a tick; the camera target follows the same progress at native FOV updates. Server interruption, failed payment, actor loss, reload and disconnect clear progress.

## Straight draw-back

One scoped first-person transform serves scrolls, wands and staffs. The item keeps its ordinary horizontal/vertical rest position and orientation, smoothly translating up to 0.08 rendering units toward the camera along the depth axis over its final time cost. Perspective makes it approach slightly; it does not move toward the crosshair. Vanilla rest/equip/attack transforms return when preparation ends. The item model does not stretch, bend or change texture. Left arm and offhand retain their native mirrored rest positions and the same depth movement.

The server routes motion to the hand reserved by that exact cast. The opaque source key includes item kind because a staff and its source scroll share a mana price key. Replaced/invalid sources stop animating immediately on the client; canceled preparation, failed payment, release and disconnect clear both cues. Operator casts do not animate an unrelated held item or zoom the camera. The extension runs inside vanilla's scoped item pose and cannot transform the other hand. Native casts still release automatically when their authored time cost is paid.

## Gameplay and verification

Ordinary spell cooldowns remain removed from all 214 shipped definitions and their modes. Charge time and resource costs remain authored independently. Wands, staffs and scrolls have no additional equipment recovery; wand repetition remains limited by composed resources, preparation/channel time and durability. The mana pool regenerates invisibly, with each item showing its own affordability shade. The mana symbol PNG remains available for resource-cost artwork.

Payment remains atomic at release. Canceling before payment spends no mana, source scroll or equipment wear. Successful releases and paid failures retain existing payment rules. Another preparation can begin after the current charge/channel finishes, subject to available resources. Explicit data-pack `cooldown` costs remain supported independently of the shipped catalog, and original Iron cooldowns remain inert provenance.

The existing runtime/model/source tests cover final timing, payment, recasts, reservation, hand routing and cleanup. Opt-in `capture_kind=preparation` records actual native progress/release/cancellation for all three sources, offhand/left main arm, two display/GUI sizes, bright/dark ground, FOV Effects disabled and third person. The FOV measurement listener is confined to this opt-in inspection harness. Current source hashes, measured FOV, captures and limitations belong in development status and the subtle-zoom evidence.

## Superseded experiments

The owner first selected an expanding edge with Minecraft's native contrast-inverting color after reviewing violet and black crosshair fills. The later [8% zoom comparison](../verification/spell-preparation-zoom-trial-2026-10-07/README.md) established the preferred camera cue, then the owner requested a much smaller zoom. Those captures preserve historical review evidence; the accepted 3% zoom supersedes the edge and 8% candidate. The [first charging footage](../art/held-spell-preparation/README.md) retains the earlier bar and inward item motion.
