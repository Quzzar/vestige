# Native untipped wand publication · October 7, 2026

This isolated branch starts from main `87f7d1b5`, retaining merged Standing Stone/mana and optional pedestal work. It adds only exact scroll stacking, the five thread components, native untipped binding/casting, initial seven-base/five-core profiles and their design/verification documents. The shared checkout's Shell, thread-viewer and equipment artwork work is excluded.

Java 21 `check build runGameTestServer -Pwith_kithkyn=false` passed: **124 unit tests** with zero failures/errors/skips and **all 237 required Minecraft tests**. The latter includes 13 new wand, 12 thread and six scroll-stacking tests. The separate Kithkyn platform-version check passed with the real sibling project. The complete native suite is additional publication verification; the October 6 evidence retains its original shared-checkout/focused scope.

The eight tip effect directions are locked, while their executable implementation, actual spell-coverage audit and final tuning remain future work. Quartz, Redstone, Nautilus and Magma remain deferred. Native final art, one reusable recipe-derived wand display adapter, Kithkyn co-loading and testing-pack installation are not claimed by these checks.

[Verification](verification.json) records source/package hashes and exact limits. [Minecraft result summary](minecraft-results.txt) records the complete successful suite. The local publication package is `build/libs/vestige-0.1.0.jar` in the managed `native-wand-foundation` worktree; publishing the source does not install that package.
