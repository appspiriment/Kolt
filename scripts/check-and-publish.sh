#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# check-and-publish.sh — Selective publisher for Kolt artifacts
#
# Checks Maven Central to see if each library/plugin version has already been
# published. If yes, it skips publishing. If no, it publishes the artifact.
#
# USAGE:
#   ./scripts/check-and-publish.sh                  # Dry run (checks Central, does not publish)
#   ./scripts/check-and-publish.sh --release        # Publishes releases (-PisRelease -PsignRelease)
#   ./scripts/check-and-publish.sh --local          # Publishes locally (~/.m2)
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "$REPO_ROOT"

# Flags
DRY_RUN=true
IS_RELEASE=false
LOCAL_ONLY=false
EXTRA_GRADLE_ARGS=()

while [[ $# -gt 0 ]]; do
    case $1 in
        --release)  IS_RELEASE=true; DRY_RUN=false; shift ;;
        --local)    LOCAL_ONLY=true; DRY_RUN=false; shift ;;
        --dry-run)  DRY_RUN=true;                   shift ;;
        -P*|-D*)    EXTRA_GRADLE_ARGS+=("$1");       shift ;;  # forward Gradle project/system props
        *) echo "Unknown flag: $1"; exit 1 ;;
    esac
done

RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'; YELLOW='\033[1;33m'; NC='\033[0m'
info() { echo -e "${CYAN}▶ $*${NC}"; }
ok()   { echo -e "${GREEN}✓ $*${NC}"; }
warn() { echo -e "${YELLOW}⚠ $*${NC}"; }
fail() { echo -e "${RED}✗ $*${NC}" >&2; exit 1; }

# Helper to load version from version.properties
get_version() {
    local major_key="$1"
    local dev_key="$2"
    local major
    major=$(grep -E "^${major_key}=" version.properties | cut -d= -f2)
    local dev
    dev=$(grep -E "^${dev_key}=" version.properties | cut -d= -f2)
    if $IS_RELEASE; then
        echo "$major"
    else
        printf "%s.dev-%02d" "$major" "$dev"
    fi
}

get_bom_version() {
    grep -E "^BOM_VERSION=" version.properties | cut -d= -f2
}

# Helper to check if an artifact is published on Maven Central
is_published() {
    local artifact_id="$1"
    local version="$2"
    local group_path="io/github/appspiriment/kolt"
    local url="https://repo1.maven.org/maven2/${group_path}/${artifact_id}/${version}/${artifact_id}-${version}.pom"
    
    # If not release, always publish (snapshots/local over-writes are allowed/expected)
    if ! $IS_RELEASE; then
        return 1
    fi
    
    local status
    status=$(curl -s -o /dev/null -w "%{http_code}" "$url")
    if [ "$status" = "200" ]; then
        return 0 # is published
    else
        return 1 # not published
    fi
}

# Run a publish command
run_publish() {
    local label="$1"
    local project_dir="$2"
    local task="$3"
    local extra="${4:-}"
    
    info "Publishing $label..."
    if $DRY_RUN; then
        warn "[Dry-Run] Would execute: ./gradlew ${project_dir:+-p $project_dir} $task ${IS_RELEASE:+-PisRelease} ${EXTRA_GRADLE_ARGS[*]:-} $extra"
        return 0
    fi
    
    local flags=""
    if $IS_RELEASE; then
        flags="-PisRelease"
    fi
    
    # Run in separate Gradle invocation
    # shellcheck disable=SC2086
    ./gradlew ${project_dir:+-p $project_dir} "$task" $flags "${EXTRA_GRADLE_ARGS[@]:-}" $extra --no-parallel || fail "Failed to publish $label"
    ok "Successfully published $label"
}

# Artifact definitions
# name : artifactId : gradlePath : majorKey : devKey : isBom
# Note: compose-utils module coordinates are artifactId "compose", not "compose-utils"
ARTIFACTS=(
    "utils:utils::UTILS_MAJOR:UTILS_DEV:false"
    "logutils:logutils::LOGUTILS_MAJOR:LOGUTILS_DEV:false"
    "compose-utils:compose::COMPOSE_UTILS_MAJOR:COMPOSE_UTILS_DEV:false"
    "compose-kmp:compose-kmp::COMPOSE_KMP_MAJOR:COMPOSE_KMP_DEV:false"
    "update-utils:update-utils::UPDATE_UTILS_MAJOR:UPDATE_UTILS_DEV:false"
    "location:location::LOCATION_MAJOR:LOCATION_DEV:false"
    "bom:kolt-bom::BOM_VERSION::true"
    "conventions:koltlibs:build-logic:PLUGIN_MAJOR:PLUGIN_DEV:false"
)

info "Analyzing version.properties and Maven Central status..."
echo "--------------------------------------------------------"

for entry in "${ARTIFACTS[@]}"; do
    # Replace colons to avoid issues if parts have colons, but since none do it's fine
    IFS=":" read -r name artifact_id gradle_path major_key dev_key is_bom <<< "$entry"
    
    # Resolve version
    version=""
    if [ "$is_bom" = "true" ]; then
        version=$(get_bom_version)
    else
        version=$(get_version "$major_key" "$dev_key")
    fi
    
    # Check if already published
    if is_published "$artifact_id" "$version"; then
        ok "$name ($version) is already published. Skipping."
    else
        warn "$name ($version) is NOT published yet."
        
        # Determine task
        task=""
        if [ "$name" = "conventions" ]; then
            if $LOCAL_ONLY; then
                task=":conventions:publishToMavenLocal"
            else
                task="publishAllPublicationsToMavenCentralRepository"
            fi
        else
            if $LOCAL_ONLY; then
                task=":libs:${name}:publishToMavenLocal"
            else
                task=":libs:${name}:publishAllPublicationsToMavenCentralRepository"
            fi
        fi
        
        run_publish "$name" "$gradle_path" "$task"
    fi
done

echo "--------------------------------------------------------"
ok "Selective publication analysis completed."
