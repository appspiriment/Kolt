// Maps each independently-versioned artifact to its CHANGELOG.md source path
// (relative to the repo root) and Maven coordinate. One entry per file created
// in scripts/release.sh's LIB_TABLE / PLUGIN_CHANGELOG / BOM_CHANGELOG.
export const CHANGELOGS = [
    { id: 'utils', title: 'utils', coordinate: 'io.github.appspiriment.kolt:utils', path: 'libs/utils/CHANGELOG.md' },
    { id: 'logutils', title: 'logutils', coordinate: 'io.github.appspiriment.kolt:logutils', path: 'libs/logutils/CHANGELOG.md' },
    { id: 'compose-utils', title: 'compose-utils', coordinate: 'io.github.appspiriment.kolt:compose', path: 'libs/compose-utils/CHANGELOG.md' },
    { id: 'compose-kmp', title: 'compose-kmp', coordinate: 'io.github.appspiriment.kolt:compose-kmp', path: 'libs/compose-kmp/CHANGELOG.md' },
    { id: 'update-utils', title: 'update-utils', coordinate: 'io.github.appspiriment.kolt:update-utils', path: 'libs/update-utils/CHANGELOG.md' },
    { id: 'location', title: 'location', coordinate: 'io.github.appspiriment.kolt:location', path: 'libs/location/CHANGELOG.md' },
    { id: 'location-picker', title: 'location-picker', coordinate: 'io.github.appspiriment.kolt:location-picker', path: 'libs/location-picker/CHANGELOG.md' },
    { id: 'bom', title: 'Kolt BOM', coordinate: 'io.github.appspiriment.kolt:kolt-bom', path: 'libs/bom/CHANGELOG.md' },
    { id: 'convention-plugins', title: 'Convention Plugins', coordinate: 'kolt-catalog / kmp-catalog', path: 'build-logic/CHANGELOG.md' },
];

export function getChangelogById(id) {
    return CHANGELOGS.find(c => c.id === id);
}

// Raw GitHub content — always reflects what's on `main`, so this page never
// goes stale relative to the source-of-truth files under libs/*/CHANGELOG.md.
export const RAW_BASE = 'https://raw.githubusercontent.com/appspiriment/Kolt/main/';
