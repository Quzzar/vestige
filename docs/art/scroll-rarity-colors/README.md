# Native scroll rarity colors · October 6, 2026

The owner retained literal **Unknown Scroll** in white. Identification reveals Vestige Common names in white, Uncommon in yellow, Rare in aqua and Mythic in light purple, using Minecraft’s Epic color. Augment adjectives retain italics and inherit the same color. The primary mod name remains Vestige, with Traditions of Lost Magic as subtitle; the viewer category is Spellstone.

| State | Color | Native example |
|---|---|---|
| Unknown, any rarity | White `#FFFFFF` | Unknown Scroll |
| Identified Common | White `#FFFFFF` | Scroll of Firebolt |
| Identified Uncommon | Yellow `#FFFF55` | Scroll of Burning Dash |
| Identified Rare | Aqua `#55FFFF` | Scroll of Ball Lightning |
| Identified Mythic | Light purple `#FF55FF` | Scroll of Black Hole |

[JEI palette](jei/8-scroll_names.png) · [EMI palette](emi/8-scroll_names.png)

These are actual Minecraft framebuffer captures. The palette screen uses native item rendering, native normal/advanced tooltip generation, NeoForge tooltip events and the game’s tooltip renderer. Both clients receive actual per-player server knowledge snapshots. The fixture calls the server’s identification/craft-memory methods to make the states reproducible; the full world tests separately verify successful native casts and ingredient commitment.

Each viewer passes twenty normal/advanced tooltip cases: seven displayed samples and three additional unknown stacks, including malformed data, a native Mythic spell and an explicit Epic item rarity override. Every tooltip retains only its name, unknown names remain white, and the identified Rare augment retains its italics. Identified native rarities come from the server snapshot, including remote-server data support; unknown entries carry only neutral Common. All 214 shipped spell displays round-trip their authored rarity after identification and conceal it before identification.

[JEI crafted Fireball](jei/6-crafted_fireball.png) and [EMI crafted Fireball](emi/6-crafted_fireball.png) show actual ingredients while the scroll remains unknown. [JEI identified Magnetic Attraction](jei/7-identified_magnetic_attraction.png) and [EMI identified Magnetic Attraction](emi/7-identified_magnetic_attraction.png) retain question marks because identification does not teach the recipe. Each runtime retains 216 rituals; Paper usage grows from zero to one after Fireball crafting, and JEI’s Magnetic Attraction name search updates from zero to one after identification.

**Verification:** Java 21 test, build, sibling compatibility and all 187 required world tests pass; 105 JUnit tests have zero failures, errors or skips. [Verification record](verification.json) includes hashes and exact results; completed build/world logs and both capture metadata files are retained alongside it. The tested jar is staged at `build/scroll-rarity-colors/vestige-0.1.0.jar`, with payload version 4 requiring matching client/server builds.

**Limits:** JEI and EMI were inspected individually using isolated integrated servers. No remote multiplayer capture or Prism installation occurred. The combined JEI/EMI bridge was not revalidated for this color change; its earlier warnings remain documented in [the prior knowledge pass](../scroll-knowledge-native/README.md). EMI’s optional mod-name footer is disabled only in the isolated inspection profile. Its screenshots retain development warnings for the sibling Kithkyn’s untranslated `mine_support_materials` tag; all native ritual, search, name and color assertions pass. Original Wizardry glyph naming and its white scroll names are separately documented in [the pinned source review](../../research/electroblob-scroll-name-colors.md).
