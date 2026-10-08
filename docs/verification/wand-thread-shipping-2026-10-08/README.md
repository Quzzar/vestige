# Approved wand/thread shipping · October 8, 2026

All five magical threads now use Minecraft's native enchantment shimmer. This is a default cosmetic Glint Override component, with no real enchantments or gameplay bonus; stacks remain 64. The approved v6 thread pixels and retained v4 wand layers are unchanged. The combined build was installed in **Kithkyn Testing**, preserving the previous jar outside the mods directory. Restart Minecraft to load it.

## Verified production artifact

Installed SHA-256: `201c7974425df87e72c815c63b711b6700e4cb92318de3e48c27542321395b97`.

[Installation receipt](installation.json) records the actual target, timestamp, backup and hashes. [Source manifest](source-sha256.json) pins 3,337 source/build files from the frozen combined snapshot; later unrelated shared-checkout edits are excluded. [Machine-readable results](verification.json) record counts and scope. The reviewed client is Minecraft 1.21.1 / NeoForge 21.1.72 / Java 21.

The first combined full-suite jar and final installed jar differ only in `PlinthColumnTest.class`, after strengthening a flaky test observation. Every production gameplay/resource ZIP entry is identical. [Artifact comparison](package-lineage.json) pins both hashes. The final focused world run exercised that corrected test. Review-only inventory capture code is excluded from the installed jar; its source is retained with the artwork evidence.

## Build and gameplay evidence

- Combined production build and installed-Kithkyn compatibility passed, with **179 unit tests** and **316 required Minecraft world tests**, zero failures. [Complete build/world log](logs/combined-build-world.log.gz).
- Final focused run passed **17 required world tests** for the thread recipes, real crafting chain and plinth behavior. [Focused log](logs/focused-chain-plinth.log.gz).
- The scoped publication branch passed **137 unit tests** and **254 required world tests** locally. [Publication log](logs/publication-build-world.log.gz). Both build and cast-gallery checks passed on code commit `b7fdabd2f3e8f9666155fe9617f7202521ef5c44` in [GitHub run 37730088733](https://github.com/Quzzar/vestige/actions/runs/37730088733).
- `tools/author_wand_models.py --check` passed all 63 layered wand models and 20 actual 16×16 component sprites. The packaged, loaded and approved PNG hashes match exactly.

The two added chain cases craft Callous Thread from String, Amethyst and Honeycomb with an Iron socket, carry the actual dropped thread into rotated eight-node binding, collect the actual wand drop, then complete paid casting. One case is untipped; the other has a Netherite tip and an exact shaped source. They check real consumption, retained sockets, sixteen-scroll stacking, source shaping once, initial payment and deterministic wear. Their player rejects any gameplay chat/actionbar message. Existing world cases cover all five thread recipes, exact augmented-source matching and other base/core/tip/casting behavior.

The combined verifier substitutes the pack's **actual installed Kithkyn jar**, SHA-256 `182c68e44eb5a1fdf50a1529621ec24535e945a3b70720d79901413b84004d2c`, for the differing sibling development jar. The [review-only Gradle init](combined-verification.init.gradle) records this substitution. Tests use disposable worlds; existing player worlds/configuration are unchanged. The final thread-viewer client also loads the installed JEI and Nature's Compass jars, completing the testing pack's mod set.

## Actual client appearance

[Native screenshots and manifests](../../art/wands-native/thread-shimmer/README.md) retain four ordinary inventory frames, five real JEI wand cases and eleven JEI thread cases. Inspected frames show the unchanged sprites beside vanilla String, Lead and materials, the parchment recipes, socket frames and exact shaped-scroll lookup. Paired unchanged-layout frames demonstrate animation on all five threads while the vanilla String control has zero pixel change. [Animation measurements](../../art/wands-native/thread-shimmer/animation-check.json) distinguish moving shimmer from altered artwork.

Logs: [inventory](logs/native-inventory.log.gz), [wand viewer](logs/native-wand-viewer.log.gz), [thread viewer](logs/native-thread-viewer.log.gz). All three completed successfully. This pass did not recapture EMI; previous EMI evidence remains historical.

## Corrected verification failures

An initial CI run exposed that the manually linked art guide was missing from its document generator. The generator now preserves that link. [Original drift failure](logs/ci-generator-failure.log.gz).

A later CI run intermittently failed an existing delayed, bounded lookup for an ejected plinth item. A deterministic reproduction moved the actual drop three blocks: the local query found zero although the item remained intact elsewhere. The regression now uniquely marks the offered item, captures its actual entity immediately, deliberately moves it, and checks identity/components/count plus cancellation, preserved other ingredients and unlocked nodes after the delay. Temporary debug logging is removed. No production plinth behavior changed. [Original observation failure](logs/ci-plinth-observation-failure.log.gz), [deterministic reproduction](logs/ci-moved-drop-reproduction.log.gz), [final passing CI](https://github.com/Quzzar/vestige/actions/runs/37730088733).

## Publication and remaining limits

The owner-approved wand/thread changes and this evidence are published to [PR #2](https://github.com/Quzzar/vestige/pull/2). The scoped wand foundation and the installed combined snapshot are distinct: the latter includes approved concurrent features and the later removal of extra wand recovery. Unrelated shared-checkout source changes are not swept into the wand PR. This receipt establishes installation, not a merge.

Remote multiplayer, encounter tuning and long-session feel remain playtest work. No new gameplay balance decision or magical thread texture redesign is implied by this cosmetic effect.
