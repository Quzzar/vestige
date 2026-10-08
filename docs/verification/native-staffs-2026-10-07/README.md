# Native staff verification · October 7, 2026

The [staff design](../../design/staff-crafting.md) records accepted choices separately from initial recipe, wear and art tuning. This evidence uses Minecraft 1.21.1 / NeoForge 21.1.72 / Java 21 in the shared development checkout. It is not an isolated release and is not installed in Prism.

The final integration build passes **140 JUnit tests**, with no failures, errors or skips, plus **13 required native Minecraft staff tests**. The final negotiated menu fixture verifies a real clientbound payload and forged/stale/replayed selection rejection. Other tests cover construction, shaped binding/replacement, expansion, retained sockets, canceled rituals and output spawning, payment/recovery/wear, interrupted preparation, final offhand use, paid recasts and unavailable saved source identity. The [manifest](verification.json), staff-only JUnit XML and [native summary](world-summary.log) retain results and hashes.

The [initial gameplay snapshot](gameplay-verification.json) records 139 passing JUnit tests. Client/menu transforms and another chat's shared `RitualCrafting` integration changed afterward. A final `test build runGameTestServer` recheck passes with the current source, including the additional shared unit test. The manifest lists changed paths and the final package hash; this is not a claim that every source file stayed unchanged between runs.

The initial native run passed twelve gameplay tests but failed its menu test: Minecraft's stock mock player has no negotiated `staff_slots` channel. The corrected fixture registers that channel on an in-memory packet connection; production menu authorization was unchanged. The final thirteen-test run passes. A final-run attempt initially lacked write permission for the existing Gradle cache; its authorized rerun passes. Neither failed attempt is counted as a passing run.

The preceding Kithkyn platform check verifies matching project versions. These behavior tests and client captures run with `with_kithkyn=false`; they do not claim actual Kithkyn co-loading or remote human multiplayer. Optional JEI/EMI staff entries remain separate work. Initial recipes and 40/80/120 durability need normal encounter playtesting.

Reproduce with Java 21 and an unused fresh directory:

```sh
./gradlew -I docs/verification/native-staffs-2026-10-07/build.init.gradle test build runGameTestServer -Pwith_kithkyn=false -Pgametest_filter=staff_ -Pgametest_directory=/private/tmp/vestige-staff-fresh
./gradlew -I docs/verification/native-staffs-2026-10-07/build.init.gradle runEffectsCapture -Pwith_kithkyn=false -Pcapture_kind=staff_slots -Pcapture_directory=/private/tmp/vestige-staff-client-fresh -Pcapture_output=/private/tmp/vestige-staff-captures
python3 tools/audit_staff_traits.py --check
python3 tools/author_staff_texture.py --check
```

The capture creates a fresh flat world, seeds staff variants on the actual integrated server, receives native knowledge/menu payloads, clicks real slot buttons, verifies the selection on the server and casts through a normal use-item packet. It exports Minecraft's framebuffer rather than the desktop. The [initial captures](../../art/native-staffs-v1/verification.json) exposed double-blurred headers and oversized holding transforms. The [completed client review](../../art/native-staffs-complete/README.md) contains six visually inspected images and 17 passing checks after those corrections. Its loaded model/texture hashes match the package. A subsequent appearance attempt was interrupted and is not counted as complete; the final completed run includes normal casting.
