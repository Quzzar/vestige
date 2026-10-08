# Item rarity and Fluxed Flint transfer — October 8, 2026

This is the completed scope from the rarity/Dissentient Diamond/Fluxed Flint chat. The [machine-readable manifest](manifest.json) enumerates every changed file and evidence file, with SHA-256 hashes and shared-file merge boundaries.

Publication base: `4efe9f9de38ac3b14d90d18c93b6d9ab506355af` (`origin/main` when prepared). The approved Diamond registration/artwork and repair foundation are already merged in this base. No root index or branch was changed while preparing this handoff.

## Transfer

1. Apply or merge [the scoped patch](scoped-changes.patch): **24 files**, including the binary Flint texture. It contains only this chat’s source, tests, authoring and document changes. It was applied successfully to an isolated exact-base copy and every resulting file matched its scoped target hash.
2. Copy the **81 new archived files** listed individually in `transferEvidence` in the manifest. These hold the rarity receipt, Flint v2 source/prompt/export/native evidence, and Diamond v10 source provenance referenced by the already-merged v11 approval. Hash each copied file. The **32 v11 evidence files** already in base are unchanged.
3. Preserve the other chats’ current shared-file changes while merging. Entries with `copyWholeRootFile: false` are deliberately synthesized from the publication base plus only this chat’s owned hunks; their root reference hashes include other work and are not whole-file transfer instructions.
4. Include this README, manifest and scoped patch as the handoff record if retaining publication provenance. Rebuild the final combined package in the publication checkout; the earlier review JAR is a historical snapshot.

## Changed files

| File | Owned change |
| --- | --- |
| `BREAKING_CHANGES.md` | Only revised Fluxed Flint ingredient-chain entry. Excludes Hourglass API/payment entry. |
| `docs/art/fluxed-flint-v1/README.md` | Mark old hold and violet sprite as historical; link to v2. |
| `docs/design/item-imbuement-and-readiness.md` | Only accepted item-name rarities section and lifted Flint hold in its audit row. Excludes other-chat robe proposals/benchmarks. |
| `docs/design/magical-repair.md` | Current recipe chain, rarity, lifted hold, unchanged repair/volatility semantics and separate new-pixel review boundary. |
| `docs/design/recipe-viewers.md` | Only Dissentient Diamond added to the ordinary ordered-construction list. |
| `docs/design/ritual-crafting.md` | Only two revised construction rows and lifted hold paragraph. Excludes other-chat Hourglass ritual section. |
| `docs/design/robe-behavior-review.md` | Only approved native item-name rarity paragraph. Excludes other-chat art/model or robe proposal changes. |
| `docs/design/staff-crafting.md` | Only approved native item-name rarity paragraph. Excludes other-chat art/model or robe proposal changes. |
| `docs/design/wayfarer-imbuement-review.md` | Only approved Uncommon base rarity row. |
| `docs/development-status.md` | Only this chat’s Flint shimmer, recipe continuation and rarity verification entries. Excludes all other new development entries. |
| `src/main/java/com/quzzar/vestige/apparatus/DissentientDiamondRecipe.java` | New ordinary four-inner-seat Diamond/Gunpowder/Skull/Gunpowder matcher; one output; whole rotations. |
| `src/main/java/com/quzzar/vestige/apparatus/FluxedFlintRecipe.java` | Only ingredients(): Flint/Netherite/Dissentient Diamond/Netherite. Existing unordered repair unchanged. |
| `src/main/java/com/quzzar/vestige/apparatus/FluxedFlintTest.java` | One actual two-stage construction world case; atomic duplicate consumption, rotation, sockets and inactive outer inputs. |
| `src/main/java/com/quzzar/vestige/apparatus/RitualCrafting.java` | Only the Dissentient Diamond construction branch immediately before Flint construction. Excludes other-chat Hourglass branch. |
| `src/main/java/com/quzzar/vestige/apparatus/ScrollItems.java` | Six reviewed native rarity property additions plus owner-requested Fluxed Flint default enchantment shimmer. Excludes other-chat Hourglass registration and creative entry. Diamond registration/Uncommon rarity already in base. |
| `src/main/java/com/quzzar/vestige/apparatus/ShapelessRitualTest.java` | Repeated ingots give eight labeled arrangements of four distinct rotated patterns; repair audit unchanged. |
| `src/main/java/com/quzzar/vestige/apparatus/client/NativeFlintCapture.java` | Opt-in inventory comparison and actual server-tick completion wait; gameplay timings unchanged. |
| `src/main/java/com/quzzar/vestige/apparatus/recipeviewer/RitualDisplays.java` | Only ordinary Diamond output mapping and dissentientDiamond() live-pattern entry. Excludes other-chat Hourglass output fallback. |
| `src/main/java/com/quzzar/vestige/apparatus/recipeviewer/RitualViewerClient.java` | Only Diamond public viewer registration. Excludes other-chat Hourglass entries on this shared line. |
| `src/main/java/com/quzzar/vestige/equipment/MagicArmorItem.java` | Only Uncommon rarity on the default robe constructor. |
| `src/main/java/com/quzzar/vestige/equipment/WayfarerBootsItem.java` | Only Uncommon rarity on the boots constructor; all sixteen variants. |
| `src/main/resources/assets/vestige/textures/item/fluxed_flint.png` | 24 accent pixels recolored to pinned vanilla Diamond shades; alpha, stone, silhouette and model unchanged. Final owner pixel selection pending. |
| `src/test/java/com/quzzar/vestige/apparatus/OrderedConstructionTest.java` | Diamond viewer/matcher coverage, separate duplicate offerings, and rejection of retired Flint recipe. |
| `tools/author_fluxed_flint.py` | Constrained blue export from retained generated source and original logical cells. |

## Evidence directories

- `docs/verification/item-rarity-2026-10-08/` — 12 new files.
- `docs/verification/fluxed-flint-v2-2026-10-08/` — 7 new files.
- `docs/art/fluxed-flint-v2/` — 21 new files, including the retained failed timing attempt as historical diagnostic evidence, not final verification.
- `docs/art/dissentient-diamond-v10/` — 28 new source/provenance files referenced by v11. V10’s texture is byte-identical to approved v11; the GUI alignment lives in the v11 model.
- `docs/art/dissentient-diamond-v11/` — all 32 files already merged and unchanged; retains explicit owner acceptance and exact pins.
- `docs/verification/fluxed-flint-glint-2026-10-08/` — owner-requested shimmer follow-up, native captures, source/package hashes and build log.

Java files inside these evidence folders are archived fixtures. They are not production replacements. In particular, never transfer a preview collision bypass into `src/`.

## Acceptance and verification

The item rarities and exact Diamond v11 pixels/alignment are explicitly approved. The human authorized resuming Flint with the discussed recipe and blue accents, then explicitly requested its native magical shimmer. The owner subsequently requested closeout and explicitly authorized the completed package for final release with its native shimmer; `docs/art/fluxed-flint-v2/approval.json` retains that context and pins the current exact artwork. Earlier verification receipts retain their original review status. Existing 128-point budget, 25% cap and Volatile 2 remain unchanged playtest tuning; no stabilization choice was invented.

The rarity pass recorded **202 unit tests and 84 native assertions**. The later combined snapshot recorded **211 unit tests**, **26 focused required Minecraft tests**, and **five final native assertions**, with exact component preservation and both stages of the real construction chain covered. Current scoped Flint sources/resources still match that frozen verification snapshot. The manifests link full logs, screenshots, hashes and limitations. This chat did not install or publish a build.

Hourglass changes, all other new development entries, robe proposals/benchmarks and unrelated root changes remain with their owners. The Hourglass test parser-call correction should stay with the Hourglass package. Shared hunks were explicitly excluded from this patch; consult `scope` and `exclusions` in the manifest.
