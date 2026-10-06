# Repository and builds

The canonical repository is [Quzzar/vestige](https://github.com/Quzzar/vestige).
`main` holds the current development build. Use a short-lived branch and a pull request
for subsequent changes; `codex/` is the default prefix for agent branches.

## GitHub checks

The [Build workflow](../.github/workflows/build.yml) runs on pushes to `main`, pull requests
and manual requests. Its layout follows Kithkyn's repository, with checks specific to Vestige:

- Java 21 and Python 3.13, with Gradle dependency caching.
- Deterministic native spell conversion and generated reference, balance, source and art checks.
- Balance, presentation and recording verification, including hashes for all 214 cast videos.
- Gradle unit tests, packaging and Minecraft behavior tests, with Kithkyn co-loading disabled.
- A separate cast-gallery job using Bun 1.3.11, the frozen lockfile, catalog tests and the production build.

A successful build uploads `vestige-jars`. Download it from the workflow run's **Artifacts**
section; use `vestige-<version>.jar`, rather than the sources or Javadoc jars. Test reports
and server logs are uploaded as `vestige-test-reports`, including on failed builds.
These are development artifacts. This setup does not create release tags or publish a release.

## Local verification

The same standalone Minecraft check runs without a sibling checkout:

```bash
./gradlew check build runGameTestServer -Pwith_kithkyn=false
```

Ordinary development runs retain Kithkyn co-loading by default. They require a sibling
checkout with the same Minecraft and NeoForge versions; use
`-Pkithkyn_project_dir=/absolute/path/to/kithkyn` to select it. The published Kithkyn project
may advance independently, so standalone CI does not fetch or build its changing default branch.

For gallery changes:

```bash
cd tools/effects-viewer
bun install --frozen-lockfile
bun test
bun run build
```

The gallery's footage is actual Minecraft framebuffer output. The complete recording check
does not recapture footage: it verifies coverage and exact agreement with the committed
definitions and videos. See [the capture guide](effects-workshop.md) when changing appearance.

## Art review evidence

Keep authored assets, chosen review images, short review clips and verification records in Git. Repeated native `frame-*.png` / `frame-*.jpg` sequences under `docs/art/` remain local and are ignored; saved capture metadata may name those local originals. Final review exports remain available in the repository. Normal build output, test worlds and the local capture resource packs remain outside Git.

## Contributions and attribution

Use [Vestige Issues](https://github.com/Quzzar/vestige/issues) for bugs and proposals.
Include the spell ID, mod/platform versions, reproduction steps and relevant logs for bugs.
Read [the project instructions](../AGENTS.md) and the relevant design documents before changing
spell structure or balance. Keep verification results in [development status](development-status.md).

The inherited [license](../LICENSE.md) and [historical credits](../CREDITS.md) remain in the
repository and packaged jar. Repository presentation follows Kithkyn; its GPL license does
not replace Vestige's existing notices.
