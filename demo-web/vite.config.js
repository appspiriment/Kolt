import { defineConfig } from 'vite';
import { fileURLToPath } from 'node:url';
import { readdirSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';

const root = fileURLToPath(new URL('.', import.meta.url));
const IGNORED_DIRS = new Set(['node_modules', 'dist', '.git', 'build']);

function findHtmlFiles(dir, results = []) {
    for (const entry of readdirSync(dir)) {
        if (IGNORED_DIRS.has(entry)) continue;
        const full = join(dir, entry);
        const stats = statSync(full);
        if (stats.isDirectory()) {
            findHtmlFiles(full, results);
        } else if (entry.endsWith('.html')) {
            results.push(full);
        }
    }
    return results;
}

const htmlEntries = findHtmlFiles(root).reduce((input, file) => {
    const name = relative(root, file).replace(/[\\/]/g, '_').replace(/\.html$/, '');
    input[name] = file;
    return input;
}, {});

export default defineConfig({
    build: {
        rollupOptions: {
            input: htmlEntries,
        },
    },
});
