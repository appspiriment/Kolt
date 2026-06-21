# Coding Standards (Android / KMP)

Canonical, agent-readable coding standards for all my Kotlin projects — Android-only and KMP.
Strict **Clean Architecture + MVI + SOLID + stateless Compose**. Works with **Claude** (`CLAUDE.md`)
and **Gemini / Antigravity** (`AGENTS.md`).

## Files

Layered for low token cost: the agent entry files are tiny and always loaded; the rest is **read on demand** for the task at hand.

| File | Purpose | When loaded |
|---|---|---|
| [`templates/CLAUDE.md`](templates/CLAUDE.md) · [`templates/AGENTS.md`](templates/AGENTS.md) | Thin per-project entry (Claude / Gemini-Antigravity). Index + project facts. | Always |
| [`CODING_STANDARDS.md`](CODING_STANDARDS.md) | **The laws.** Dense rule index, layer by layer (Clean + MVI + SOLID + stateless Compose). | Per task |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | **The shape.** Layering, DI strategy, offline-first data layer, layer wiring, Android-only vs KMP deltas, abstractions catalog, error handling, performance. | When designing/structuring |
| [`TESTING.md`](TESTING.md) | What to test, patterns (reducer/use-case/Flow), fakes, coverage targets. | When writing tests |

Rules live in exactly one place each, referenced (not duplicated) by the agent entry files — low token cost, no drift. Canonical base classes (MVI base, `AppResult`, `UseCase`, dispatchers, offline-sync contracts) live in your shared **core library**, not here — see `ARCHITECTURE.md` §6.

## File index

| File | Purpose | When agent loads it |
|---|---|---|
| `CLAUDE.md` · `AGENTS.md` | Thin per-project entry — index + project facts. Scaffolded to consumer project root. | **Always** |
| `CODING_STANDARDS.md` | **The laws.** Dense rule index (Clean + MVI + SOLID + stateless Compose). | Per coding task |
| `ARCHITECTURE.md` | **The shape.** Layering, DI, offline-first, layer wiring, Android/KMP deltas, abstractions catalog. | When designing |
| `TESTING.md` | Test patterns, fakes, coverage targets. | When writing tests |
| `KOLT.md` | Plugin catalog, DSL options, library APIs (logutils/utils/compose-utils/update-utils), catalog aliases, scaffolding. | When using Kolt suite |

## Adopt in a new project

**Automatic (recommended):** use any `io.github.appspiriment.kolt.*` convention plugin — it runs `scaffoldKoltDocs` on the first `preBuild` and writes all six files into your project root automatically. Then fill in the `<placeholder>` sections in `CLAUDE.md` / `AGENTS.md`.

**Manual copy:**
```bash
cp templates/android-project/CLAUDE.md <project>/CLAUDE.md
cp templates/android-project/AGENTS.md <project>/AGENTS.md
mkdir -p <project>/docs
cp CODING_STANDARDS.md ARCHITECTURE.md TESTING.md KOLT.md <project>/docs/
# Fill in <placeholder> values in CLAUDE.md and AGENTS.md
```

**For a new project from scratch:** copy a full Gradle template:
- `templates/android-project/` — Android-only project
- `templates/kmp-project/` — Kotlin Multiplatform project

See `KOLT.md` §1 for the quick-start guide.

## Notes

- Keep `CODING_STANDARDS.md` / `ARCHITECTURE.md` / `TESTING.md` / `KOLT.md` generic (no project-specific content). Project-specific facts (module graph, chosen libs, build commands) belong in each project's `CLAUDE.md` / `AGENTS.md` only.
- All docs are bundled inside the plugin JAR and scaffolded automatically. Edit them in `UtilsLibs/build-logic/conventions/src/steering-templates/` — changes ship with the next plugin release.
