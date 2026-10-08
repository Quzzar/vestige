# Spell preparation and cooldown removal verification

The owner selected a short bar beneath the crosshair and removal of ordinary spell cooldowns. All 214 definitions/modes now omit cooldown costs; a semantic comparison against the pre-generation snapshot verified this is their only gameplay-data change. Mana, preparation, effects, traits, rarity and exact source provenance match. Explicit zero policy values drive both catalog generators. The separately accepted sixty-second wand source recovery and explicit optional data-pack cooldown support remain.

Verified Java 21 source snapshot: `/private/tmp/vestige-charge-20261007-source`. Its 235 owned source/catalog/build files match the workspace by SHA-256. The staged build is `build/spell-preparation-review/libs/vestige-0.1.0.jar`; its packaged 214 definitions/modes and progress classes were inspected. Artifact identity and source manifest are adjacent to this file.

Successful commands, with `-Pkithkyn_project_dir=/Users/quzzar/Projects/kithkyn` configured for the temporary checkout:

```text
./gradlew --no-configuration-cache --console=plain test build verifyKithkynCompatibility
./gradlew --no-configuration-cache --console=plain runGameTestServer \
  '-Pgametest_filter=cast_preparation|player_mana|balance.*|scroll_.*|staff_.*|wand_.*'
./gradlew --no-configuration-cache --console=plain runEffectsCapture -Pcapture_kind=preparation
```

All **152 unit tests** pass without failures/errors/skips. All **71 required native world tests** pass, covering initial preparation and clearing, cancellation/failed payment, immediate repeats, scroll-stack consumption, Creative zero-mana repeats, mana recovery, shared wand source recovery, staff wear and affinities, recasts, interruption, channels and numerical spell behavior. Wire round trips include active progress, empty clearing and long durations; malformed timing snapshots reject. `build.log`, `world.log`, `client.log` and unit totals retain the executed evidence.

Catalog/reference/balance generation and checks passed for Iron, Pathfinder, spell reference, balance audit/review, candidate inventory and effects-viewer catalog. All **17 Python balance tests**, **56 Spellshaping rule checks** and **490 leyline parity fixtures** passed. The 72-frame native capture exits successfully; original and encoded appearance was inspected at two viewport sizes. Footage and server payment assertions are in `docs/art/spell-preparation/`.

The first unit run correctly found stale wand/catalog cooldown expectations; these were updated to the accepted rule, and the viewer catalog was regenerated. Early temporary-checkout launch attempts omitted the sibling-project override. Concurrent build launches also produced a transient missing-mixin startup failure; the unchanged compiled source passed when runs were sequential. Final build, native capture and world evidence above are successful. Javadoc/temporary-water/authentication/startup warnings are unrelated existing development diagnostics.

This verifies the mechanic and focused casting regressions, not multiplayer appearance or encounter balance for all 214 spells after removing recovery. Unknown/volatile forfeit, channel exclusivity and payment rules still apply. GUI/menu/dead/spectator suppression and disconnect cleanup are implemented; exhaustive multi-client/reload visual scenarios remain release playtests. Matching client/server builds are required for the new version-1 progress payload. No installed game profile was changed.
