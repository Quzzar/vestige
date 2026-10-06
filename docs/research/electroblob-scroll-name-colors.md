# Electroblob's Wizardry scroll-name colors

October 6, 2026. Source review and separate owner-approved Vestige presentation decision. The owner accepted the native Minecraft palette below and explicitly retained literal **Unknown Scroll**. No upstream code or assets were imported.

## Source scope

This examines **original Electroblob's Wizardry for Minecraft 1.12.2**, not Wizardry Redux: [Electroblob77/Wizardry commit `fe5d05a7a134836dd972e3680a900619c7e9c134`](https://github.com/Electroblob77/Wizardry/commit/fe5d05a7a134836dd972e3680a900619c7e9c134), dated September 3, 2026. This is also the revision already pinned for Vestige's original scroll-art reference. Forge's supporting 1.12.x implementation is pinned to [commit `3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1`](https://github.com/MinecraftForge/MinecraftForge/commit/3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1).

## Ordinary scroll names

**Ordinary original Wizardry scrolls have white item names before and after discovery, regardless of spell tier.** This is a source-derived conclusion, not a captured screenshot. The complete path is:

1. `WizardryItems` registers `scroll` as `new ItemScroll()`. Its registration helper sets registry/translation names and creative-tab ordering; it adds no rarity or color behavior. [Registration](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/registry/WizardryItems.java#L466-L497), [scroll construction](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/registry/WizardryItems.java#L575-L578).
2. `ItemScroll` extends vanilla `Item` directly. Neither the class nor its casting/workbench interfaces overrides rarity. Its `hasEffect` override always returns true, giving visual enchantment glint; its display-name override delegates to the sided proxy. Normal scroll binding creates a plain stack with spell metadata, without enchantment NBT. [ItemScroll](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/item/ItemScroll.java#L38-L101), [casting interface](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/item/ISpellCastingItem.java), [workbench interface](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/item/IWorkbenchItem.java), [binding](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/item/ItemBlankScroll.java#L64-L68).
3. Forge's default item rarity delegates to vanilla rarity: an actually enchanted stack is `RARE`; otherwise it is `COMMON`. `COMMON` is white. The GUI colors the first tooltip line from that rarity. Glint alone does not enter this rarity calculation. [Default rarity](https://github.com/MinecraftForge/MinecraftForge/blob/3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1/patches/minecraft/net/minecraft/item/Item.java.patch#L41-L48), [Forge delegation](https://github.com/MinecraftForge/MinecraftForge/blob/3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1/patches/minecraft/net/minecraft/item/Item.java.patch#L854-L857), [common color](https://github.com/MinecraftForge/MinecraftForge/blob/3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1/patches/minecraft/net/minecraft/item/EnumRarity.java.patch#L7-L11), [tooltip name coloring](https://github.com/MinecraftForge/MinecraftForge/blob/3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1/patches/minecraft/net/minecraft/client/gui/GuiScreen.java.patch#L22-L28).

Thus an unusually enchanted or externally modified scroll can differ through the inherited Minecraft rarity path. The ordinary scroll tier does not select its item-name color.

## What discovery changes

| State in normal survival discovery mode | English item-name shape | Color | Font |
| --- | --- | --- | --- |
| Undiscovered spell | `Scroll "<glyph name>"` | White | Ordinary letters for `Scroll` and quotation marks; glyph-name section uses Standard Galactic Alphabet |
| Discovered spell | `Scroll of <spell name>` | White | Ordinary Minecraft text |

The client inserts a world-specific glyph name surrounded by `#` markers for undiscovered spells. The English translations supply these two name forms. Glyph names are generated only when absent and stored per world. The mixed font renderer consumes the markers and draws that section with Minecraft's Standard Galactic Alphabet renderer. This is stable glyph lettering, **not** Minecraft's animated `§k` obfuscation, and the original does not use the literal name `Unknown Scroll`. [Client name selection](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/client/ClientProxy.java#L340-L358), [English names](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/resources/assets/ebwizardry/lang/en_us.lang#L194-L199), [persistent glyph names](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/data/SpellGlyphData.java#L21-L56), [font selection](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/client/ClientProxy.java#L321-L336), [mixed renderer](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/client/MixedFontRenderer.java#L9-L49).

The discovered name uses `Spell#getDisplayName`, which explicitly returns unformatted text. Creative players, disabled discovery mode, a missing client player, or missing `WizardData` bypass unknown naming. Server-side name lookup also returns the readable name without player discovery filtering. These are original behavior, not proposed Vestige exceptions. [Unformatted spell name](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/spell/Spell.java#L662-L684), [client conditions](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/client/ClientProxy.java#L344-L355), [server name lookup](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/CommonProxy.java#L95-L107).

## The tier colors the owner may remember

Original Wizardry defines the following **spell-tier text** colors:

| Wizardry tier | Formatting color |
| --- | --- |
| Novice | White (`WHITE`) |
| Apprentice | Aqua (`AQUA`) |
| Advanced | Dark blue (`DARK_BLUE`) |
| Master | Dark purple (`DARK_PURPLE`) |

[Tier definitions](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/constants/Tier.java#L11-L16).

`ItemSpellBook` displays this colored tier as a **separate tooltip line even for an undiscovered spell**. Its undiscovered spell-name line uses blue glyphs; its discovered spell-name line uses the spell's element color. This is a plausible explanation for remembering colors, but does not establish which item or mod version the owner remembers. Scrolls instead add an unformatted tier line only when discovered and advanced tooltips are enabled. [Spellbook tooltip](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/item/ItemSpellBook.java#L93-L120), [scroll advanced tooltip](https://github.com/Electroblob77/Wizardry/blob/fe5d05a7a134836dd972e3680a900619c7e9c134/src/main/java/electroblob/wizardry/item/ItemScroll.java#L104-L120).

## Accepted Vestige mapping, October 6

Minecraft/NeoForge 1.21.1 item rarity colors are **Common white, Uncommon yellow, Rare aqua, Epic light purple**. These differ from Wizardry's tier palette. [NeoForge 1.21.1 item documentation](https://docs.neoforged.net/docs/1.21.1/items/#creating-an-item).

The owner selected this native mapping:

| Native Vestige scroll state | Accepted name color |
| --- | --- |
| Any unidentified spell | White `Unknown Scroll` |
| Identified Common spell | White |
| Identified Uncommon spell | Yellow |
| Identified Rare spell | Aqua |
| Identified Mythic spell | Light purple, using the vanilla Epic color |

This preserves the owner's plain unknown name, conceals rarity until identification, and makes identification reveal both the readable name and its native rarity color. It uses **Vestige's own Common/Uncommon/Rare/Mythic rarity**; Wizardry tiers and inert Iron/Pathfinder source rarity do not select it. Italic augment names inherit the same color, and tooltips retain only the scroll name. Per-player server snapshots send actual rarity only for identified spells. The owner explicitly chose plain **Unknown Scroll** over Wizardry’s stable glyph names. Native implementation and verification are recorded separately in [development status](../development-status.md).
