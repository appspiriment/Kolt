# Release Management

Steering doc for cutting a Kolt release — versioning, changelogs, publishing,
and GitHub releases. Read this **before running `scripts/release.sh`** or
hand-rolling any of its steps (a manual `gh release create`, a manual
`bump*Version`, editing a `CHANGELOG.md` by hand). Skip it if you're just
building/testing library code with no release involved.

## The moving parts, in one picture

```
libs/<name>/*                      (you change code)
        │
        ▼
version.properties                 (source of truth for every artifact's version)
        │  ./gradlew bump<Name>Version / bumpBomVersion / bumpPluginVersion
        ▼
libs/<name>/CHANGELOG.md           (source of truth for what changed, per artifact)
        │  git commit + push
        ▼
check-and-publish.sh --release     (→ Maven Central / Sonatype)
        │
        ▼
gh release create                  (→ GitHub Releases, notes from the CHANGELOG entries)
        │
        ▼
.github/workflows/publish.yml      (fires on release published — safety-net re-publish)
```

`scripts/release.sh` drives every step below in order, interactively. This
doc explains what it does and why; read `scripts/release.sh` itself for the
exact commands.

## 1. Versioning — `version.properties`

Every artifact (`utils`, `logutils`, `compose-utils`, `compose-kmp`,
`update-utils`, `location`, `location-picker`, the convention plugins, and
the BOM) has its **own independent version**, tracked as a `<NAME>_MAJOR` +
`<NAME>_DEV` pair in `version.properties` at the repo root. Dev builds render
as `<MAJOR>.dev-<DEV padded to 2 digits>` (e.g. `0.2.1.dev-03`); a release
build (`-PisRelease`) drops the suffix and publishes `<MAJOR>` as-is.

Only artifacts that actually changed get a version bump and get published —
this is why the release flow starts with **detecting which `libs/*`
directories changed**, not bumping everything.

Per-artifact bump tasks (`./gradlew bump<Name>Version`) and `bumpBomVersion` /
`bumpPluginVersion` are defined in `build-logic/conventions/build.gradle.kts`
and delegated from the root `build.gradle.kts`. They increment the `_DEV`
counter and rewrite the README version badges. A new **MAJOR** version is a
manual edit to `version.properties` (reset `_DEV` to `0`) — not something
`release.sh` does.

## 2. Changelogs — one `CHANGELOG.md` per artifact

Each publishable artifact has its own `CHANGELOG.md` next to its
`build.gradle.kts`:

```
libs/utils/CHANGELOG.md
libs/logutils/CHANGELOG.md
libs/compose-utils/CHANGELOG.md
libs/compose-kmp/CHANGELOG.md
libs/update-utils/CHANGELOG.md
libs/location/CHANGELOG.md
libs/location-picker/CHANGELOG.md
libs/bom/CHANGELOG.md              (auto-generated — lists which libs moved)
build-logic/CHANGELOG.md           (convention plugins + catalogs)
```

**Format** (a fixed, minimal dialect — see §5 for why it must stay fixed):

```markdown
# Changelog

All notable changes to `<artifact>` (`io.github.appspiriment.kolt:<artifact>`)
are documented here, newest first. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.2.1.dev-03] - 2026-08-08
- Added X.
- Fixed Y.

## [0.2.1.dev-02] - 2026-08-01
- ...
```

**Who writes these:** only `scripts/release.sh`, at release time. It never
accumulates an "Unreleased" section during day-to-day dev commits — when you
bump an artifact's version as part of a release, the script prompts you for
that artifact's release notes right then and prepends the entry. This keeps
the mechanism to one step instead of two (no separate "remember to add to
Unreleased" discipline to enforce on every commit).

**Linking:** every lib's `README.md` has a `Changelog` badge next to its
Maven Central / License badges, pointing at `CHANGELOG.md` in the same
directory. The root `README.md`'s Library Versions table has a `Changelog`
column doing the same, plus a link to this doc.

## 3. `scripts/release.sh` — the interactive driver

```bash
./scripts/release.sh
```

Requires `gh` authenticated (`gh auth login`) and a clean working tree. Steps:

1. **Detect changed libs** — diffs `libs/*` and `build-logic/conventions/src`
   against the last commit that touched `version.properties` (that commit is
   the reliable "last release" baseline; git tags in this repo are not
   consistently named, so they aren't used).
2. **Confirm per-artifact** — asks y/n for each detected artifact
   individually, so a stray touched file doesn't drag in a version bump you
   didn't intend.
3. **Bump** — runs each selected artifact's `bump<Name>Version` task, then
   `bumpBomVersion` if anything was bumped.
4. **Release notes** — prompts for bullet points per bumped artifact,
   prepends a `## [version] - date` entry to that artifact's `CHANGELOG.md`.
   Auto-writes the BOM's changelog entry (just lists which artifacts moved —
   the detail lives in each artifact's own file).
5. **Commit + push** `version.properties`, `README.md`, and the touched
   `CHANGELOG.md` files, in one commit.
6. **Publish to Sonatype** — offers to run
   `./scripts/check-and-publish.sh --release` right there (needs your local
   Sonatype credentials + a loaded GPG key, see §4).
7. **Create the GitHub release** — `gh release create` with the collected
   notes as the release body (`--notes-file`) plus `--generate-notes`
   appended (the auto PR-list Central adds after your notes).

## 4. Publishing to Sonatype / Maven Central

`./scripts/check-and-publish.sh --release` checks Maven Central for each
artifact's version and only uploads what's missing — safe to re-run.
Credentials come from `~/.gradle/gradle.properties` or env vars
(`ossrhUsername`/`ossrhPassword` or `ORG_GRADLE_PROJECT_*`), and signing
needs a GPG key loaded (`gpg --list-secret-keys`; run from an interactive
terminal so GPG can prompt for the passphrase).

**Known current state:** `.github/workflows/publish.yml` also runs
`check-and-publish.sh --release` automatically whenever a GitHub release is
published — this is intended as the automated path, but right now that CI
run reliably fails (see the workflow's Actions history). Since `release.sh`
already publishes locally in step 6 before creating the release, this is
**accepted as-is for now**: the CI run is a no-op safety net that happens to
error instead of skipping cleanly, not the thing actually publishing your
release. Don't block a release on it going green. Revisit CI once someone
has time to fix the credentials/config gap — the fix belongs in
`.github/workflows/publish.yml`'s secrets, not in `release.sh`.

## 5. Surfacing changelogs on the demo site

`demo-web/docs/changelog/index.html` is a docs-site page listing every
artifact and, per artifact, rendering its `CHANGELOG.md`. It fetches the file
straight from `raw.githubusercontent.com/appspiriment/Kolt/main/...` at
runtime — **not** a copy baked into the site — so it always reflects what's
on `main` with zero extra sync step, at the cost of only working once a
change is pushed (fine: by the time anyone views the site, the release
commit is already pushed).

`demo-web/docs/changelog/data.js` maps each artifact id to its
`CHANGELOG.md` path and Maven coordinate — add an entry there (and to
`scripts/release.sh`'s `LIB_TABLE`) when a new publishable library is added.
The nav entry lives in `demo-web/docs/assets/docs.js`'s `DOCS_NAV`
("Release Notes").

The page's markdown renderer (`renderChangelogMarkdown` in
`changelog/index.html`) is deliberately **not** a general markdown parser —
it only understands `# title` / `## [version] - date` / `- bullet` /
`[text](url)`, because `scripts/release.sh` is the only writer of these
files and that's the entire format it produces. If `CHANGELOG.md` content
ever needs richer markdown, upgrade the renderer then — don't pre-build for
formats that don't exist yet.

## 6. Adding a new publishable library

When a new `libs/<name>` module gets its own Maven coordinate:

1. Add `<NAME>_MAJOR` / `<NAME>_DEV` to `version.properties`.
2. Add a `bump<Name>Version` task in
   `build-logic/conventions/build.gradle.kts` (`registerBumpTask(...)`,
   mirror an existing one) and wire it into `bumpAllVersions`.
3. Seed `libs/<name>/CHANGELOG.md` (copy the format in §2).
4. Add the `Changelog` badge to `libs/<name>/README.md` and a row to the
   root `README.md`'s Library Versions table.
5. Add an entry to `scripts/release.sh`'s `LIB_TABLE`.
6. Add an entry to `demo-web/docs/changelog/data.js`'s `CHANGELOGS`.
7. Add the artifact to `scripts/check-and-publish.sh`'s `ARTIFACTS` list.
