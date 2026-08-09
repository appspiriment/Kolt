#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# release.sh — Interactive release: bump changed libs, record release notes,
# publish to Sonatype, cut a GitHub release.
#
# See docs/RELEASE_MANAGEMENT.md for the full design this script implements.
#
# 1. Detects which libs/* modules (+ build-logic/conventions) changed since the
#    last version.properties bump (i.e. the last release prep commit).
# 2. Lets you confirm each one individually, then runs its `bump*Version`
#    Gradle task (+ bumpBomVersion, since any lib change requires a new BOM).
# 3. For each bumped artifact, prompts for release-note bullets and prepends a
#    "## [version] - date" entry to its CHANGELOG.md. The BOM's CHANGELOG.md
#    gets an auto-generated entry listing which libs moved.
# 4. Commits + pushes version.properties, README.md, and the CHANGELOG.md files.
# 5. Publishes to Maven Central via check-and-publish.sh --release (needs
#    Sonatype creds + a loaded GPG key locally — see docs/RELEASE_MANAGEMENT.md).
# 6. Creates a GitHub release with `gh release create`, using the collected
#    notes as the release body (plus --generate-notes for the auto PR list).
#
# NOTE: .github/workflows/publish.yml ALSO runs check-and-publish.sh --release
# whenever a GitHub release is published. Since step 5 already publishes
# locally, that CI run is currently expected to fail (duplicate-upload /
# already-published) — known and accepted for now; it's a no-op safety net,
# not the source of truth. See docs/RELEASE_MANAGEMENT.md.
#
# USAGE: ./scripts/release.sh
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$REPO_ROOT"

RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'; YELLOW='\033[1;33m'; NC='\033[0m'
step() { echo -e "\n${CYAN}▶ $*${NC}"; }
ok()   { echo -e "${GREEN}✓ $*${NC}"; }
warn() { echo -e "${YELLOW}⚠ $*${NC}"; }
fail() { echo -e "${RED}✗ $*${NC}" >&2; exit 1; }
confirm() {
    read -r -p "$1 [y/N] " reply
    [[ "$reply" =~ ^[Yy]$ ]]
}

# Reads bullet lines from stdin until a blank line; prints them prefixed "- "
# (lines already starting with "-" are passed through as-is). Falls back to a
# generic bullet if nothing was entered.
read_notes() {
    local label="$1" line notes=""
    echo "  Release notes for $label (one bullet per line, blank line to finish):"
    while IFS= read -r -p "    > " line; do
        [[ -z "$line" ]] && break
        [[ "$line" == -* ]] || line="- $line"
        notes+="$line"$'\n'
    done
    [[ -n "$notes" ]] || notes="- Version bump."
    printf '%s' "$notes"
}

# Prepends "## [version] - date\n<notes>\n" to a CHANGELOG.md, right before its
# first existing "## [" entry (or at the end if it has none yet).
prepend_changelog_entry() {
    local file="$1" version="$2" notes="$3"
    local entry_file
    entry_file=$(mktemp)
    printf '## [%s] - %s\n%s\n' "$version" "$(date +%Y-%m-%d)" "$notes" > "$entry_file"
    if [[ -f "$file" ]] && grep -q '^## \[' "$file"; then
        # -v can't carry embedded newlines portably across awk implementations
        # (breaks on macOS/BWK awk) — feed the entry via getline from a temp file.
        awk -v entryfile="$entry_file" '
            !done && /^## \[/ {
                while ((getline line < entryfile) > 0) print line
                close(entryfile)
                print ""
                done = 1
            }
            { print }
        ' "$file" > "${file}.tmp" && mv "${file}.tmp" "$file"
    else
        cat "$entry_file" >> "$file"
        echo "" >> "$file"
    fi
    rm -f "$entry_file"
}

get_version() {
    local major dev
    major=$(grep -E "^${1}=" version.properties | cut -d= -f2)
    dev=$(grep -E "^${2}=" version.properties | cut -d= -f2)
    printf "%s.dev-%02d" "$major" "$dev"
}

command -v gh >/dev/null || fail "GitHub CLI (gh) not found — install it: https://cli.github.com"
gh auth status >/dev/null 2>&1 || fail "gh is not authenticated — run 'gh auth login' first."

[[ -z "$(git status --porcelain)" ]] || fail "Working tree has uncommitted changes — commit or stash first."

# dir-name : bump-task : MAJOR-key : DEV-key : changelog-path
LIB_TABLE=(
    "utils:bumpUtilsVersion:UTILS_MAJOR:UTILS_DEV:libs/utils/CHANGELOG.md"
    "logutils:bumpLogutilsVersion:LOGUTILS_MAJOR:LOGUTILS_DEV:libs/logutils/CHANGELOG.md"
    "compose-utils:bumpComposeUtilsVersion:COMPOSE_UTILS_MAJOR:COMPOSE_UTILS_DEV:libs/compose-utils/CHANGELOG.md"
    "compose-kmp:bumpComposeKmpVersion:COMPOSE_KMP_MAJOR:COMPOSE_KMP_DEV:libs/compose-kmp/CHANGELOG.md"
    "update-utils:bumpUpdateUtilsVersion:UPDATE_UTILS_MAJOR:UPDATE_UTILS_DEV:libs/update-utils/CHANGELOG.md"
    "location:bumpLocationVersion:LOCATION_MAJOR:LOCATION_DEV:libs/location/CHANGELOG.md"
    "location-picker:bumpLocationPickerVersion:LOCATION_PICKER_MAJOR:LOCATION_PICKER_DEV:libs/location-picker/CHANGELOG.md"
)
PLUGIN_CHANGELOG="build-logic/CHANGELOG.md"
BOM_CHANGELOG="libs/bom/CHANGELOG.md"

# ── Step 1: detect changed libs since the last version.properties bump ────────
step "Detecting changed libraries"
BASE_REF=$(git log -1 --format=%H -- version.properties)
[[ -n "$BASE_REF" ]] || fail "No commit history for version.properties — can't determine a baseline."
echo "  Baseline: $(git log -1 --format='%h %s' "$BASE_REF")"

CHANGED_FILES=$(git diff --name-only "$BASE_REF" -- libs build-logic/conventions/src)
PLUGIN_CHANGED=false
grep -q '^build-logic/conventions/src' <<<"$CHANGED_FILES" && PLUGIN_CHANGED=true

TO_BUMP=()
for entry in "${LIB_TABLE[@]}"; do
    IFS=":" read -r dir _ _ _ _ <<<"$entry"
    grep -q "^libs/${dir}/" <<<"$CHANGED_FILES" && TO_BUMP+=("$entry")
done

if [[ ${#TO_BUMP[@]} -eq 0 && "$PLUGIN_CHANGED" == false ]]; then
    warn "No changes detected under libs/ or build-logic/conventions/src since the last version bump."
    confirm "Nothing to bump — exit?" && exit 0
fi

echo ""
echo "  Changed since baseline:"
for entry in "${TO_BUMP[@]}"; do
    IFS=":" read -r dir _ _ _ _ <<<"$entry"
    echo "    - $dir"
done
$PLUGIN_CHANGED && echo "    - convention-plugins (build-logic/conventions)"
echo ""

# ── Step 2: confirm each one individually ───────────────────────────────────
step "Confirm which to bump"
SELECTED=()
for entry in "${TO_BUMP[@]}"; do
    IFS=":" read -r dir _ _ _ _ <<<"$entry"
    confirm "  Bump $dir?" && SELECTED+=("$entry")
done
TO_BUMP=("${SELECTED[@]}")
if $PLUGIN_CHANGED; then
    confirm "  Bump convention-plugins?" || PLUGIN_CHANGED=false
fi
if [[ ${#TO_BUMP[@]} -eq 0 && "$PLUGIN_CHANGED" == false ]]; then
    fail "Nothing selected — aborted."
fi

# ── Step 3: run bump tasks ───────────────────────────────────────────────────
step "Bumping versions"
$PLUGIN_CHANGED && { ./gradlew bumpPluginVersion --no-parallel; ok "convention-plugins bumped"; }
for entry in "${TO_BUMP[@]}"; do
    IFS=":" read -r dir task _ _ _ <<<"$entry"
    ./gradlew "$task" --no-parallel
    ok "$dir bumped"
done
[[ ${#TO_BUMP[@]} -gt 0 ]] && { ./gradlew bumpBomVersion --no-parallel; ok "BOM bumped"; }

# ── Step 4: collect release notes → CHANGELOG.md files ───────────────────────
step "Release notes"
RELEASE_NOTES_FILE=$(mktemp)
BUMPED_SUMMARY=()

if $PLUGIN_CHANGED; then
    ver=$(get_version PLUGIN_MAJOR PLUGIN_DEV)
    notes=$(read_notes "convention-plugins ($ver)")
    prepend_changelog_entry "$PLUGIN_CHANGELOG" "$ver" "$notes"
    { echo "### convention-plugins $ver"; echo "$notes"; echo; } >> "$RELEASE_NOTES_FILE"
    BUMPED_SUMMARY+=("convention-plugins $ver")
fi
for entry in "${TO_BUMP[@]}"; do
    IFS=":" read -r dir _ major_key dev_key changelog <<<"$entry"
    ver=$(get_version "$major_key" "$dev_key")
    notes=$(read_notes "$dir ($ver)")
    prepend_changelog_entry "$changelog" "$ver" "$notes"
    { echo "### $dir $ver"; echo "$notes"; echo; } >> "$RELEASE_NOTES_FILE"
    BUMPED_SUMMARY+=("$dir $ver")
done
if [[ ${#TO_BUMP[@]} -gt 0 ]]; then
    bom_ver=$(grep -E "^BOM_VERSION=" version.properties | cut -d= -f2)
    bom_notes=$(printf '%s\n' "${BUMPED_SUMMARY[@]}" | sed 's/^/- /')
    prepend_changelog_entry "$BOM_CHANGELOG" "$bom_ver" "$bom_notes"
fi
ok "Release notes recorded in CHANGELOG.md files"

git --no-pager diff -- version.properties README.md '**/CHANGELOG.md'

# ── Step 5: commit + push ────────────────────────────────────────────────────
step "Commit + push version bump"
confirm "Commit version.properties + README.md + CHANGELOG.md files and push?" || \
    fail "Aborted before commit — revert with 'git checkout -- version.properties README.md **/CHANGELOG.md' if needed."

SUMMARY=$(printf '%s\n' "${BUMPED_SUMMARY[@]}" | sed 's/ [^ ]*$//' | paste -sd', ' -)
git add version.properties README.md '**/CHANGELOG.md'
git commit -m "chore: bump ${SUMMARY} for release"
git push
ok "Pushed"

# ── Step 6: publish to Sonatype ──────────────────────────────────────────────
step "Publish to Sonatype (Maven Central)"
warn "Requires ~/.gradle/gradle.properties Sonatype creds + a loaded GPG key (see docs/RELEASE_MANAGEMENT.md)."
if confirm "Run ./scripts/check-and-publish.sh --release now?"; then
    ./scripts/check-and-publish.sh --release
    ok "Published to Sonatype"
else
    warn "Skipped local publish — the GitHub release below will still trigger CI's publish.yml."
fi

# ── Step 7: create GitHub release ────────────────────────────────────────────
step "Create GitHub release"
DEFAULT_TAG="release-$(date +%Y%m%d-%H%M%S)"
read -r -p "Release tag [${DEFAULT_TAG}]: " TAG
TAG="${TAG:-$DEFAULT_TAG}"
read -r -p "Release title [${SUMMARY}]: " TITLE
TITLE="${TITLE:-$SUMMARY}"

if confirm "Create GitHub release '${TAG}' (${TITLE}) from $(git branch --show-current)?"; then
    gh release create "$TAG" \
        --title "$TITLE" \
        --notes-file "$RELEASE_NOTES_FILE" \
        --generate-notes \
        --target "$(git branch --show-current)"
    ok "GitHub release created — .github/workflows/publish.yml will run automatically."
else
    warn "Skipped — run manually later: gh release create <tag> --notes-file $RELEASE_NOTES_FILE --generate-notes"
fi

rm -f "$RELEASE_NOTES_FILE"

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  Release flow complete ✓${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
