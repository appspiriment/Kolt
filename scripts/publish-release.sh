#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# publish-release.sh — Publish all Appspiriment artifacts to Maven Central
#
# Runs each module in a SEPARATE Gradle invocation to avoid the known vanniktech
# SonatypeRepositoryBuildService classloader conflict in multi-module builds.
#
# USAGE:
#   ./scripts/publish-release.sh              # dry run (dev version, no signing)
#   ./scripts/publish-release.sh --release    # full release with signing
#   ./scripts/publish-release.sh --local      # publish to ~/.m2 only (fastest dev cycle)
#   ./scripts/publish-release.sh --module logutils  # single module only
#
# CREDENTIALS (in ~/.gradle/gradle.properties or exported as env vars):
#   ossrhUsername / OSSRH_USERNAME
#   ossrhPassword / OSSRH_PASSWORD
#   GPG key available via gpg --list-secret-keys (signing.gnupg.keyName if needed)
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$REPO_ROOT"

# ── Flags ────────────────────────────────────────────────────────────────────
IS_RELEASE=false
LOCAL_ONLY=false
SINGLE_MODULE=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --release)  IS_RELEASE=true;         shift ;;
        --local)    LOCAL_ONLY=true;          shift ;;
        --module)   SINGLE_MODULE="$2";       shift 2 ;;
        --help|-h)
            sed -n '2,22p' "$0" | sed 's/^# //' | sed 's/^#//'
            exit 0 ;;
        *) echo "Unknown flag: $1 (use --help)"; exit 1 ;;
    esac
done

RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'; YELLOW='\033[1;33m'; NC='\033[0m'
step()    { echo -e "\n${CYAN}▶ $*${NC}"; }
ok()      { echo -e "${GREEN}✓ $*${NC}"; }
warn()    { echo -e "${YELLOW}⚠ $*${NC}"; }
fail()    { echo -e "${RED}✗ $*${NC}" >&2; exit 1; }

RELEASE_FLAG=""
PUBLISH_TASK=""
if $IS_RELEASE; then
    RELEASE_FLAG="-PisRelease"
    PUBLISH_TASK="publishAllPublicationsToMavenCentralRepository"
    echo ""
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}  Appspiriment — Release Publish to Maven Central${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    warn "Publishing SIGNED release. Ensure your GPG key is loaded."
elif $LOCAL_ONLY; then
    PUBLISH_TASK="publishToMavenLocal"
    echo ""
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}  Appspiriment — Publish to ~/.m2 (local)${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
else
    PUBLISH_TASK="publishToMavenLocal"
    echo ""
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}  Appspiriment — Dev Publish (MavenLocal)${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    warn "Not a release build — publishing to ~/.m2 only. Use --release for Maven Central."
fi

# ── Helper: run a single Gradle publish ──────────────────────────────────────
run_publish() {
    local label="$1"
    local project_flag="$2"   # e.g. "" for root, "-p build-logic" for included build
    local task="$3"
    local extra="${4:-}"

    step "$label"
    # shellcheck disable=SC2086
    ./gradlew $project_flag "$task" $RELEASE_FLAG $extra --no-parallel || fail "$label failed"
    ok "$label"
}

# ── Determine which modules to publish ───────────────────────────────────────
MODULES=("logutils" "utils" "compose-utils" "update-utils")
if [[ -n "$SINGLE_MODULE" ]]; then
    MODULES=("$SINGLE_MODULE")
fi

# ── Step 1: bump version (dev builds only, release uses MAJOR from version.properties) ──
if ! $IS_RELEASE && ! $LOCAL_ONLY; then
    step "Bumping DEV version counter"
    ./gradlew bumpDevVersion --no-parallel
    ok "Version bumped"
fi

# Print current version
CURRENT_VERSION=$(./gradlew -q :build-logic:conventions:printVersion 2>/dev/null || \
    grep -E "^MAJOR=" version.properties | cut -d= -f2)
echo ""
echo "  Version: $CURRENT_VERSION"

# ── Step 2: Publish convention plugins + catalogs (build-logic) ───────────────
if [[ -z "$SINGLE_MODULE" ]]; then
    if $IS_RELEASE; then
        run_publish "Convention plugins + catalogs → Sonatype" \
            "-p build-logic" ":conventions:publishToSonatype"
    else
        run_publish "Convention plugins + catalogs → MavenLocal" \
            "-p build-logic" ":conventions:publishToMavenLocal"
    fi
fi

# ── Step 3: Publish each lib in a separate Gradle invocation ─────────────────
# IMPORTANT: each lib must be its own invocation to avoid the vanniktech
# SonatypeRepositoryBuildService classloader conflict (registered once per
# project, each with a different ClassLoader in a multi-module build).

for module in "${MODULES[@]}"; do
    run_publish "libs/$module → ${IS_RELEASE:+Maven Central}${IS_RELEASE:-~/.m2}" \
        "" ":libs:${module}:${PUBLISH_TASK}"
done

# ── Done ──────────────────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  All artifacts published successfully ✓${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
if $IS_RELEASE; then
    echo "  Next: close and release the staging repository at"
    echo "  https://s01.oss.sonatype.org/#stagingRepositories"
    echo "  (or wait ~10 min for auto-release if automaticRelease=true)"
fi
echo ""
