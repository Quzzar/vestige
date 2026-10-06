# Tiny scroll-fragment concepts

Generated October 6, 2026 with the built-in image generation tool. Four independent generation calls used `transparent_background: true`, with no reference images. The fourth concept received the targeted cleanup edit below, but retained a visible haze, so its final version was generated afresh with the shorter prompt recorded at the end. These are concept previews; no game texture or consuming code changed in this selection round.

## Exact generation prompts

Final saved images:

- [1. Thin strip](01-thin-strip.png)
- [2. Torn corner](02-torn-corner.png)
- [3. Narrow sliver](03-narrow-sliver.png)
- [4. Ragged scrap](04-ragged-shred.png)

The PNGs are unchanged generated previews with transparency; they have not been reduced or imported as 16×16 game textures. `files.json` records their dimensions and hashes. The two earlier fourth-option attempts remain alongside them for provenance.

October 6 follow-up: the owner preferred the torn-corner option and requested a less geometric silhouette that avoids an arrowhead appearance. [Torn corner v2](02-torn-corner-v2.png) blunts the pointed edge and introduces uneven torn notches while retaining the small scale, parchment colors and broken ink mark. It was edited with the built-in image generator using the original option as its reference; the [exact prompt and mode](02-torn-corner-v2-prompt.txt) are archived alongside it. This remains a concept preview.

Each call used the common prompt followed by its corresponding variant paragraph.

### Common prompt

```text
Use case: stylized-concept
Asset type: Minecraft scroll fragment inventory sprite concept.
Primary request: a very tiny torn shred of old paper, much smaller than a page or a full scroll. It should look like a little leftover scrap, not a whole sheet.
Style: authentic low resolution Minecraft pixel art, hard square pixels, no smooth edges, no antialiasing. Design as a 16x16 source-pixel sprite and enlarge uniformly for presentation. Very simple, readable silhouette and a restrained 4-color palette of pale cream, warm ivory, muted tan, and soft brown. Keep the outline subtle, not thick or black. A little incomplete faded ink is optional.
Composition: one single tiny isolated paper shred centered in a square transparent canvas. The shred itself occupies only about 6 to 8 source pixels across and 3 to 6 pixels tall, leaving abundant transparent space. Flat inventory view.
Constraints: actual transparent background; no background scene, no shadow, no glow, no ribbon, no rolled ends, no crystals, no skull or bones, no text or labels, no full sheet of paper. The shape must feel broken and torn rather than neatly cut.
```

### 01-thin-strip

```text
Variant 1: a short, thin horizontal strip of paper with frayed, uneven ends and a small triangular tear missing from its lower edge. Almost blank, with just one tiny partial dark ink dash. It is a little narrow shred, roughly 8 pixels wide by 3 pixels tall.
```

### 02-torn-corner

```text
Variant 2: a tiny asymmetrical triangular corner of parchment, pointed at one end and with a ragged zigzag torn edge opposite it. Light pale paper, with one tiny broken ink mark near the broad end. Roughly 6 pixels wide by 5 pixels tall.
```

### 03-narrow-sliver

```text
Variant 3: a very narrow upright paper sliver leaning diagonally, torn unevenly at both ends. One subtle crease down part of its length and one isolated faded ink speck. Mostly pale cream, delicate and slender, roughly 3 pixels wide by 7 pixels tall.
```

### 04-ragged-shred

```text
Variant 4: a very small irregular squat shred of parchment with several missing edge pixels, a deep little notch on one side and a slight fold catching pale light. A single broken ink stroke, plenty of blank paper. Roughly 6 pixels wide by 4 pixels tall. Avoid an oval or complete rectangular sheet.
```

## Exact fourth-option cleanup prompt

```text
Use case: precise-object-edit
Asset type: Minecraft scroll fragment inventory sprite concept.
Edit target: the attached fourth option, a tiny ragged paper shred with a folded upper-right corner and broken diagonal ink.
Primary request: remove ALL glow, haze, shadow, colored edge fringes, and background from this image. Preserve the tiny torn paper silhouette, the pale cream and tan paper, folded corner, broken ink stroke, and the existing placement and scale.
Style: crisp low resolution pixel art, flat inventory sprite with hard square pixel edges. Paper pixels are fully opaque. Every pixel outside the paper silhouette is fully transparent, including between torn notches. No antialiasing, no translucent outline, no light spilling outside the paper, no gradient aura. Do not enlarge the paper, add detail, or change the torn shape. One isolated tiny paper shred, no text, no additional objects.
```

## Final fourth-option generation prompt

```text
Use case: stylized-concept
Asset type: Minecraft inventory icon, scroll fragment option 4.
Subject: a tiny torn-off scrap of pale parchment. Make an irregular squat L-shaped shred with a deep torn notch on its left edge, one folded pale corner, and one short incomplete brown ink mark.
Style: extremely simple FLAT pixel art, a handful of sharp square solid-color blocks in cream and tan, like a classic 16x16 Minecraft item sprite. Scale up the square pixels uniformly for this preview. The paper scrap should occupy about 6 pixels wide and 4 pixels tall of the 16x16 grid. Center it with generous empty space.
Background: entirely transparent alpha outside the paper. Draw only fully opaque paper blocks and ink blocks. Hard clean square edges, no translucent pixels around the paper.
Avoid: glow, aura, bloom, blurry edge, halo, shadow, gradients, texture noise, 3D lighting, black outline, full sheet of paper, rolled scroll, crystals, skulls, labels or text. One tiny isolated paper shred.
```
