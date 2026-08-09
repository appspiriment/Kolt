#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
# new-project.sh — Bootstrap a new Appspiriment Android or KMP project
#
# USAGE (interactive):
#   ./scripts/new-project.sh
#
# USAGE (non-interactive / AI agent):
#   ./scripts/new-project.sh --type android --name MyApp --package com.example.myapp
#   ./scripts/new-project.sh --type kmp     --name MyApp --package com.example.myapp [--output /path/to/dest]
#
# OPTIONS:
#   --type     android | kmp                    (required; prompted if omitted)
#   --name     Project name, e.g. "MyApp"       (required; prompted if omitted)
#   --package  Root package, e.g. "com.example.myapp"  (required; prompted if omitted)
#   --output   Destination directory            (default: ./<name>)
#   --help     Print this message
#
# WHAT IT DOES:
#   1. Copies the chosen template (templates/android-project/ or templates/kmp-project/)
#      from this repo's project-templates into the destination directory.
#   2. Substitutes <AppName>, <Company>.<AppName>, and namespace placeholders.
#   3. Copies the Gradle wrapper from this repo so the project is immediately buildable.
#   4. Prints next steps.
# ─────────────────────────────────────────────────────────────────────────────
set -euo pipefail

# ── Locate repo root (script lives in <repo>/scripts/) ─────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
TEMPLATES_DIR="${REPO_ROOT}/project-templates/templates"

# ── Colour helpers ───────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${CYAN}ℹ${NC}  $*"; }
success() { echo -e "${GREEN}✓${NC}  $*"; }
warn()    { echo -e "${YELLOW}⚠${NC}  $*"; }
err()     { echo -e "${RED}✗  $*${NC}" >&2; exit 1; }

# ── Argument parsing ─────────────────────────────────────────────────────────
PROJECT_TYPE=""
PROJECT_NAME=""
PACKAGE_NAME=""
OUTPUT_DIR=""

print_help() {
    sed -n '2,20p' "$0" | sed 's/^# //' | sed 's/^#//'
}

while [[ $# -gt 0 ]]; do
    case $1 in
        --type)    PROJECT_TYPE="$2";  shift 2 ;;
        --name)    PROJECT_NAME="$2";  shift 2 ;;
        --package) PACKAGE_NAME="$2";  shift 2 ;;
        --output)  OUTPUT_DIR="$2";    shift 2 ;;
        --help|-h) print_help; exit 0 ;;
        *) err "Unknown option: $1 (run with --help)" ;;
    esac
done

# ── Interactive prompts (only for missing values) ───────────────────────────
echo ""
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}  Appspiriment — New Project Bootstrap${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

if [[ -z "$PROJECT_TYPE" ]]; then
    echo "Project type:"
    echo "  1) android  — Android-only (Compose + Hilt, single-platform)"
    echo "  2) kmp      — Kotlin Multiplatform (Android + optional iOS/Desktop)"
    read -rp "Choice [1/2]: " TYPE_CHOICE
    case "$TYPE_CHOICE" in
        1|android) PROJECT_TYPE="android" ;;
        2|kmp)     PROJECT_TYPE="kmp" ;;
        *) err "Invalid choice. Run again and enter 1 or 2." ;;
    esac
fi

[[ "$PROJECT_TYPE" != "android" && "$PROJECT_TYPE" != "kmp" ]] && \
    err "Invalid --type '$PROJECT_TYPE'. Must be 'android' or 'kmp'."

if [[ -z "$PROJECT_NAME" ]]; then
    read -rp "Project name (e.g. MyApp): " PROJECT_NAME
fi
[[ -z "$PROJECT_NAME" ]] && err "Project name cannot be empty."

if [[ -z "$PACKAGE_NAME" ]]; then
    SUGGESTED=$(echo "com.example.$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cd 'a-z0-9')")
    read -rp "Package name [$SUGGESTED]: " PACKAGE_INPUT
    PACKAGE_NAME="${PACKAGE_INPUT:-$SUGGESTED}"
fi
[[ -z "$PACKAGE_NAME" ]] && err "Package name cannot be empty."

# Validate package name format
if ! echo "$PACKAGE_NAME" | grep -qE '^[a-z][a-z0-9]*(\.[a-z][a-z0-9]*){1,}$'; then
    err "Invalid package name '$PACKAGE_NAME'. Use lowercase reverse-domain format, e.g. com.example.myapp"
fi

if [[ -z "$OUTPUT_DIR" ]]; then
    DEFAULT_OUTPUT="./${PROJECT_NAME}"
    read -rp "Output directory [$DEFAULT_OUTPUT]: " OUTPUT_INPUT
    OUTPUT_DIR="${OUTPUT_INPUT:-$DEFAULT_OUTPUT}"
fi

# ── Resolve source template ──────────────────────────────────────────────────
TEMPLATE_SRC="${TEMPLATES_DIR}/${PROJECT_TYPE}-project"
[[ ! -d "$TEMPLATE_SRC" ]] && err "Template not found at: $TEMPLATE_SRC"

# ── Check destination ────────────────────────────────────────────────────────
OUTPUT_DIR="${OUTPUT_DIR%/}"  # strip trailing slash
if [[ -e "$OUTPUT_DIR" ]]; then
    warn "Directory already exists: $OUTPUT_DIR"
    read -rp "Overwrite? [y/N]: " CONFIRM
    [[ "${CONFIRM,,}" != "y" ]] && err "Aborted."
fi

mkdir -p "$OUTPUT_DIR"

# ── Derive substitution values ───────────────────────────────────────────────
# e.g. com.example.myapp  ->  company=example, appname=myapp
PACKAGE_PARTS=(${PACKAGE_NAME//./ })
COMPANY_PART="${PACKAGE_PARTS[${#PACKAGE_PARTS[@]}-2]:-example}"
APP_PART="${PACKAGE_PARTS[${#PACKAGE_PARTS[@]}-1]:-app}"
# Namespace path for src/main (com/example/myapp)
PACKAGE_PATH="${PACKAGE_NAME//./\/}"
# App name in lowercase (for use in file content)
APP_NAME_LOWER=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]')

info "Creating ${PROJECT_TYPE} project '${PROJECT_NAME}' (${PACKAGE_NAME}) in ${OUTPUT_DIR}"
echo ""

# ── Copy template files with substitution ───────────────────────────────────
# Copy directory structure, doing placeholder replacement on each file
copy_and_sub() {
    local src="$1"
    local dst="$2"

    if [[ -d "$src" ]]; then
        mkdir -p "$dst"
        for item in "$src"/{*,.[^.]*}; do
            [[ -e "$item" ]] || continue
            local basename
            basename="$(basename "$item")"
            copy_and_sub "$item" "$dst/$basename"
        done
    elif [[ -f "$src" ]]; then
        mkdir -p "$(dirname "$dst")"
        # Perform substitutions (order matters: specific before generic)
        sed \
            -e "s|com\.<company>\.<appname>|${PACKAGE_NAME}|g" \
            -e "s|com\.example\.myapp|${PACKAGE_NAME}|g" \
            -e "s|<company>|${COMPANY_PART}|g" \
            -e "s|<appname>|${APP_PART}|g" \
            -e "s|<AppName>|${PROJECT_NAME}|g" \
            -e "s|\"${PROJECT_NAME}\"[[:space:]]*\/\/.*← replace|\"${PROJECT_NAME}\"|g" \
            "$src" > "$dst"
        success "  $(realpath --relative-to="$OUTPUT_DIR" "$dst" 2>/dev/null || echo "$dst")"
    fi
}

copy_and_sub "$TEMPLATE_SRC" "$OUTPUT_DIR"

# ── Copy Gradle wrapper from this repo ──────────────────────────────────────
echo ""
info "Copying Gradle wrapper..."
if [[ -f "${REPO_ROOT}/gradlew" ]]; then
    cp "${REPO_ROOT}/gradlew"     "${OUTPUT_DIR}/gradlew"
    cp "${REPO_ROOT}/gradlew.bat" "${OUTPUT_DIR}/gradlew.bat" 2>/dev/null || true
    mkdir -p "${OUTPUT_DIR}/gradle/wrapper"
    cp -r "${REPO_ROOT}/gradle/wrapper/." "${OUTPUT_DIR}/gradle/wrapper/"
    chmod +x "${OUTPUT_DIR}/gradlew"
    success "  gradlew (from UtilsLibs)"
else
    warn "Gradle wrapper not found in repo root — skipping. Run 'gradle wrapper' in the new project to add it."
fi

# ── Symlink Standards steering files ─────────────────────────────────────────
echo ""
info "Linking AI-agent steering standards from Standards/..."
if [[ -f "${REPO_ROOT}/Standards/scripts/link-standards.sh" ]]; then
    "${REPO_ROOT}/Standards/scripts/link-standards.sh" "$PROJECT_TYPE" "$OUTPUT_DIR"
    success "  Symlinked .standards -> Standards/steering/${PROJECT_TYPE}"
    success "  Symlinked AGENTS.md, CLAUDE.md, GEMINI.md -> .standards/AGENTS.md"
else
    DOCS_SRC="${REPO_ROOT}/Standards"
    mkdir -p "${OUTPUT_DIR}/docs"
    for f in CODING_STANDARDS.md ARCHITECTURE.md TESTING.md KOLT.md APPSPIRIMENT.md; do
        if [[ -f "${DOCS_SRC}/${f}" ]]; then
            cp "${DOCS_SRC}/${f}" "${OUTPUT_DIR}/docs/${f}"
            success "  docs/${f}"
        fi
    done
fi

# ── Create initial source directory structure ────────────────────────────────
echo ""
info "Creating source directories..."

create_src() {
    local mod="$1"; local src_set="$2"
    mkdir -p "${OUTPUT_DIR}/${mod}/src/${src_set}/kotlin/${PACKAGE_PATH}"
    success "  ${mod}/src/${src_set}/kotlin/${PACKAGE_PATH}/"
}

create_src "app" "main"
create_src "app" "test"

if [[ "$PROJECT_TYPE" == "kmp" ]]; then
    create_src "shared" "commonMain"
    create_src "shared" "androidMain"
    create_src "shared" "commonTest"
    create_src "data"   "commonMain"
    create_src "data"   "androidMain"
else
    # Standard Android module source dirs
    mkdir -p "${OUTPUT_DIR}/app/src/main/res/"{values,values-night,layout,drawable}
    success "  app/src/main/res/{values,values-night,layout,drawable}/"
fi

# ── Print next steps ─────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  Project created: ${OUTPUT_DIR}${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "  Next steps:"
echo ""
echo "  1. Update the catalog version in settings.gradle.kts:"
echo "     from(\"io.github.appspiriment:appspiriment-catalog:<version>\")"
echo "     → check https://github.com/appspiriment/UtilsLibs/releases"
echo ""
echo "  2. Open in Android Studio:"
echo "     studio ${OUTPUT_DIR}"
echo ""
echo "  3. Build once to scaffold theme XMLs and AI-agent docs:"
echo "     cd ${OUTPUT_DIR} && ./gradlew :app:assembleDebug"
echo ""
echo "  4. Fill in project facts in CLAUDE.md / AGENTS.md"
echo ""
if [[ "$PROJECT_TYPE" == "kmp" ]]; then
echo "  5. To enable iOS targets, in shared/build.gradle.kts:"
echo "     kmp { enableIos.set(true) }"
echo ""
fi
echo "  Docs: ${OUTPUT_DIR}/docs/APPSPIRIMENT.md"
echo ""
