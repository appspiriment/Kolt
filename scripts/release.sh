#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# release.sh — Interactive release: bump changed libs, publish to Sonatype,
# cut a GitHub release.
#
# 1. Detects which libs/* modules changed since the last version.properties
#    bump (i.e. the last release prep commit) via git diff.
# 2. Lets you confirm/adjust the list, then runs each lib's `bump*Version`
#    Gradle task (+ bumpBomVersion, since any lib change requires a new BOM).
# 3. Commits + pushes version.properties (and the README badges it updates).
# 4. Publishes to Maven Central via the existing check-and-publish.sh --release
#    (needs Sonatype creds + GPG key locally — see .claude/docs/PLUGIN_DEV.md §11).
# 5. Creates a GitHub release with `gh release create`.
#
# NOTE: .github/workflows/publish.yml ALSO runs check-and-publish.sh --release
# whenever a GitHub release is published. Since step 4 already publishes locally,
# that CI run should just skip (check-and-publish.sh checks Maven Central first)
# — but if Central hasn't finished indexing yet, it may hit a duplicate-upload
# error. That's expected with this flow; the CI run is a safety net, not harmful.
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

command -v gh >/dev/null || fail "GitHub CLI (gh) not found — install it: https://cli.github.com"
gh auth status >/dev/null 2>&1 || fail "gh is not authenticated — run 'gh auth login' first."

[[ -z "$(git status --porcelain)" ]] || fail "Working tree has uncommitted changes — commit or stash first."

# dir-name : bump-task : MAJOR-key
LIB_TABLE=(
    "utils:bumpUtilsVersion:UTILS_MAJOR"
    "logutils:bumpLogutilsVersion:LOGUTILS_MAJOR"
    "compose-utils:bumpComposeUtilsVersion:COMPOSE_UTILS_MAJOR"
    "compose-kmp:bumpComposeKmpVersion:COMPOSE_KMP_MAJOR"
    "update-utils:bumpUpdateUtilsVersion:UPDATE_UTILS_MAJOR"
    "location:bumpLocationVersion:LOCATION_MAJOR"
    "location-picker:bumpLocationPickerVersion:LOCATION_PICKER_MAJOR"
)

# ── Step 1: detect changed libs since the last version.properties bump ────────
step "Detecting changed libraries"
BASE_REF=$(git log -1 --format=%H -- version.properties)
[[ -n "$BASE_REF" ]] || fail "No commit history for version.properties — can't determine a baseline."
echo "  Baseline: $(git log -1 --format='%h %s' "$BASE_REF")"

CHANGED_FILES=$(git diff --name-only "$BASE_REF" -- libs build-logic/conventions/src)
PLUGIN_CHANGED=false
if grep -q '^build-logic/conventions/src' <<<"$CHANGED_FILES"; then
    PLUGIN_CHANGED=true
fi

TO_BUMP=()
for entry in "${LIB_TABLE[@]}"; do
    IFS=":" read -r dir task major_key <<<"$entry"
    if grep -q "^libs/${dir}/" <<<"$CHANGED_FILES"; then
        TO_BUMP+=("$entry")
    fi
done

if [[ ${#TO_BUMP[@]} -eq 0 && "$PLUGIN_CHANGED" == false ]]; then
    warn "No changes detected under libs/ or build-logic/conventions/src since the last version bump."
    confirm "Nothing to bump — exit?" && exit 0
fi

echo ""
echo "  Changed since baseline:"
for entry in "${TO_BUMP[@]}"; do
    IFS=":" read -r dir _ _ <<<"$entry"
    echo "    - $dir"
done
$PLUGIN_CHANGED && echo "    - convention-plugins (build-logic/conventions)"
echo ""

# ── Step 2: confirm each one individually ───────────────────────────────────
step "Confirm which to bump"
SELECTED=()
for entry in "${TO_BUMP[@]}"; do
    IFS=":" read -r dir _ _ <<<"$entry"
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
    IFS=":" read -r dir task _ <<<"$entry"
    ./gradlew "$task" --no-parallel
    ok "$dir bumped"
done
if [[ ${#TO_BUMP[@]} -gt 0 ]]; then
    ./gradlew bumpBomVersion --no-parallel
    ok "BOM bumped"
fi

git --no-pager diff -- version.properties README.md

# ── Step 4: commit + push ────────────────────────────────────────────────────
step "Commit + push version bump"
confirm "Commit version.properties + README.md and push?" || fail "Aborted before commit — bump is uncommitted, revert with 'git checkout -- version.properties README.md' if needed."

SUMMARY=$( { $PLUGIN_CHANGED && echo "convention-plugins"; for e in "${TO_BUMP[@]}"; do IFS=":" read -r dir _ _ <<<"$e"; echo "$dir"; done; } | paste -sd', ' - )
git add version.properties README.md
git commit -m "chore: bump ${SUMMARY} for release"
git push
ok "Pushed"

# ── Step 5: publish to Sonatype ──────────────────────────────────────────────
step "Publish to Sonatype (Maven Central)"
warn "Requires ~/.gradle/gradle.properties Sonatype creds + a loaded GPG key (see .claude/docs/PLUGIN_DEV.md §11)."
if confirm "Run ./scripts/check-and-publish.sh --release now?"; then
    ./scripts/check-and-publish.sh --release
    ok "Published to Sonatype"
else
    warn "Skipped local publish — the GitHub release below will still trigger CI's publish.yml."
fi

# ── Step 6: create GitHub release ────────────────────────────────────────────
step "Create GitHub release"
DEFAULT_TAG="release-$(date +%Y%m%d-%H%M%S)"
read -r -p "Release tag [${DEFAULT_TAG}]: " TAG
TAG="${TAG:-$DEFAULT_TAG}"
read -r -p "Release title [${SUMMARY}]: " TITLE
TITLE="${TITLE:-$SUMMARY}"

if confirm "Create GitHub release '${TAG}' (${TITLE}) from $(git branch --show-current)?"; then
    gh release create "$TAG" \
        --title "$TITLE" \
        --generate-notes \
        --target "$(git branch --show-current)"
    ok "GitHub release created — .github/workflows/publish.yml will run automatically."
else
    warn "Skipped — run manually later: gh release create <tag> --generate-notes"
fi

echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  Release flow complete ✓${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
