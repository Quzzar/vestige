# Standing Stone destination list

**Historical presentation, rejected in the subsequent owner review.** These native captures document the preceding installed draft. The owner subsequently selected a narrower centered menu, name-plus-pencil editing, Minecraft destination buttons with right-aligned costs, direct travel and an unlabelled centered bottom signature. The [current button-list implementation](../standing-stone-button-list/README.md) replaces the network title, count, current-stone label and Travel confirmation.

October 6, 2026. The owner selected one clean list, an editable current-stone name and the shard signature at the bottom. This replaces the [earlier schematic map](../standing-stone-network/README.md). The sixteen approved monolith forms and their 36 finishes retain their existing appearance and identity rules.

The screen uses Minecraft's own font, name field and buttons. All same-key destinations in the current dimension remain available through eight-row pages. The current endpoint is marked **Here**; **Travel** confirms another selected row. The bottom signature uses the same four colored runes as the shard. **Save**, or Enter while editing the name, updates the current endpoint without changing its key or model. Names are retained by saves, directory entries and mined items.

Presentation follows the full-width selectable rows in [Square](https://mobbin.com/screens/8b317877-df63-4b01-a60d-35c69b10d591), the direct label/edit/commit arrangement in [Calendly](https://mobbin.com/screens/b45f09d9-d909-4e2a-819a-39bd1d5684da), and the compact rename flow in [GetYourGuide](https://mobbin.com/screens/e313c6d4-7e68-4ea6-8401-848b2968ecf4). These are interaction references, not copied art or Minecraft API evidence.

## Native inspection

Build/compatibility, 105 unit tests, 15 required Standing Stone world tests and eight live native menu assertions pass. Six final screenshots were inspected at two GUI scales. The first run is retained in `diagnostic-first/` because its single-endpoint count said “1 stones”; the final label is corrected. Native row tooltips remain enabled, including focus/hover behavior visible in these captures.

The opt-in `stone_network` capture creates a fresh isolated Minecraft world with real Standing Stone block entities and network membership. It opens the production screen through the real server payload, selects a destination, sends an actual Save request, receives the refreshed name, and follows all three pages of 21 endpoints. A separate single-stone network verifies disabled Travel. These are native framebuffer exports, not browser renders or illustrative mockups. Travel itself is covered by the Minecraft behavior tests rather than a trip recording in this menu inspection.

![Initial list](/Users/quzzar/Projects/vestige/docs/art/standing-stone-list/list-wide.png)

![Name saved through the server](/Users/quzzar/Projects/vestige/docs/art/standing-stone-list/renamed-wide.png)

![Larger interface scale](/Users/quzzar/Projects/vestige/docs/art/standing-stone-list/list-narrow.png)

![Second page](/Users/quzzar/Projects/vestige/docs/art/standing-stone-list/second-page.png)

![Last page](/Users/quzzar/Projects/vestige/docs/art/standing-stone-list/last-page.png)

![Single-stone network](/Users/quzzar/Projects/vestige/docs/art/standing-stone-list/only-current-stone.png)

`capture.json` records runtime assertions, viewport sizes and loaded class hashes. `verification.json` records the inspected image and packaged class hashes, build/unit/world-test evidence and limits. `prism-install.json` records the verified backup and atomic installation in **Kithkyn Testing**. Restart Minecraft to load it. Final generated-language escaping is canonical; all translated values and tested classes retain their inspected behavior, and both apparatus/Standing Stone author checks pass. The installed jar also preserves the approved apparatus trim; its tint class and all 252 Plinth/Spellstone models match the other tested package byte for byte.

Antique Atlas/Surveyor and Waystones integration remains optional future work, potentially an addon. The base menu adds no map dependency. Payments were unfinished in this preceding capture. The current implementation charges the owner-approved ordinary XP-point distance fare; alternate payment routes remain deferred. See the [accepted gameplay design](../../design/standing-stones.md).
