// Shared sidebar/topbar chrome for every docs page.
// Each page includes this script and an empty <div id="docs-sidebar"></div> + <div id="docs-topbar"></div>.
// `docsBasePath` (a page-level global, e.g. "../" or "../../") tells this script how to reach docs/ root.

const DOCS_NAV = [
    {
        title: 'Overview & Basics',
        links: [
            { label: 'Documentation Home', href: 'index.html' },
            { label: 'Getting Started', href: 'getting-started/index.html' },
            { label: 'Which library do I need?', href: 'which-library/index.html' },
        ],
    },
    {
        title: 'Guides & Standards',
        links: [
            { label: 'Guides Overview', href: 'guides/index.html' },
            { label: 'Choose a Plugin', href: 'guides/choose-plugin.html' },
            { label: 'Build an MVI Screen', href: 'guides/mvi-screen.html' },
            { label: 'Offline-First Data Layer', href: 'guides/offline-data-layer.html' },
            { label: 'Starter Project Templates', href: 'guides/starter-projects.html' },
            { label: 'IntelliJ / Android Studio Plugin', href: 'guides/intellij-plugin.html' },
            { label: 'AI Agent Steering & Readiness', href: 'guides/ai-steering.html' },
            { label: 'Theme & Typography System', href: 'guides/theme-system.html' },
            { label: 'Coding Standards', href: 'architecture/coding-standards.html' },
            { label: 'Testing Standards', href: 'architecture/testing.html' },
        ],
    },
    {
        title: 'Gradle Plugins',
        links: [
            { label: 'Plugins Overview', href: 'plugins/index.html' },
            { label: 'android-application', href: 'plugins/detail.html?id=android-application' },
            { label: 'android-library', href: 'plugins/detail.html?id=android-library' },
            { label: 'android-library-hilt', href: 'plugins/detail.html?id=android-library-hilt' },
            { label: 'android-library-compose', href: 'plugins/detail.html?id=android-library-compose' },
            { label: 'android-library-hilt-compose', href: 'plugins/detail.html?id=android-library-hilt-compose' },
            { label: 'android-data-layer', href: 'plugins/detail.html?id=android-data-layer' },
            { label: 'kmp-library', href: 'plugins/detail.html?id=kmp-library' },
            { label: 'kmp-library-compose', href: 'plugins/detail.html?id=kmp-library-compose' },
            { label: 'kmp-library-koin', href: 'plugins/detail.html?id=kmp-library-koin' },
            { label: 'kmp-library-koin-compose', href: 'plugins/detail.html?id=kmp-library-koin-compose' },
            { label: 'kmp-data', href: 'plugins/detail.html?id=kmp-data' },
            { label: 'kmp-application', href: 'plugins/detail.html?id=kmp-application' },
        ],
    },
    {
        title: 'Core KMP Libraries',
        links: [
            { label: 'utils: Overview', href: 'utilities/detail.html?id=utils' },
            { label: 'utils: AsyncState & Flow', href: 'utilities/utils-flow-state.html' },
            { label: 'utils: String & Phone', href: 'utilities/utils-string-phone.html' },
            { label: 'utils: List & Formatting', href: 'utilities/utils-list-format.html' },
            { label: 'utils: Date, Time & Timing', href: 'utilities/utils-time-timing.html' },
            { label: 'logutils: Logging APIs', href: 'utilities/detail.html?id=logutils' },
            { label: 'location: Timezones & Places', href: 'utilities/detail.html?id=location' },
        ],
    },
    {
        title: 'Resource Wrappers (KMP)',
        links: [
            { label: 'UiText Reference', href: 'utilities/uitext.html' },
            { label: 'UiImage Reference', href: 'utilities/uiimage.html' },
            { label: 'UiColor & UiDimen Reference', href: 'utilities/uicolor-uidimen.html' },
        ],
    },
    {
        title: 'Compose KMP UI Components',
        links: [
            { label: 'Overview & Setup', href: 'utilities/detail.html?id=compose-kmp' },
            
            // Components grouping
            { label: 'Core Components', href: 'components/index.html#core', isHeader: true },
            { label: 'Buttons & Actions', href: 'components/detail.html?id=buttons-actions', isSubLink: true },
            { label: 'Text Helpers & Components', href: 'components/detail.html?id=text-helpers-components', isSubLink: true },
            { label: 'Highlight Search Text', href: 'components/detail.html?id=highlight-search-text', isSubLink: true },
            { label: 'Images & Avatars', href: 'components/detail.html?id=images-avatars', isSubLink: true },
            
            { label: 'Inputs & Controls', href: 'components/index.html#inputs', isHeader: true },
            { label: 'AppsSlider', href: 'components/detail.html?id=appsslider-value-controls', isSubLink: true },
            { label: 'AppsRatingBar', href: 'components/detail.html?id=appsratingbar-star-rating', isSubLink: true },
            
            { label: 'Indicators & Feedback', href: 'components/index.html#indicators', isHeader: true },
            { label: 'AppsStatusTag', href: 'components/detail.html?id=appsstatustag-semantic-tags', isSubLink: true },
            { label: 'AppsBadge & Tooltip', href: 'components/detail.html?id=appsbadge-appstooltip', isSubLink: true },
            { label: 'Progress Indicators', href: 'components/detail.html?id=progress-loaders', isSubLink: true },
            { label: 'Shimmer & Empty States', href: 'components/detail.html?id=shimmer-skeleton-empty-states', isSubLink: true },
            { label: 'Banners & Feedback', href: 'components/detail.html?id=banners-system-feedback', isSubLink: true },
            
            { label: 'Layouts & Containers', href: 'components/index.html#layouts', isHeader: true },
            { label: 'AppsDivider & Spacers', href: 'components/detail.html?id=appsdivider-spacers', isSubLink: true },
            { label: 'Smooth Corners Card', href: 'components/detail.html?id=texttitledcardview-smooth-corners', isSubLink: true },
            { label: 'Generic TitledCard', href: 'components/detail.html?id=generic-titledcardview', isSubLink: true },
            { label: 'AppsAccordion', href: 'components/detail.html?id=appsaccordion-collapsible-panels', isSubLink: true },
            
            { label: 'Complex Systems', href: 'components/index.html#complex', isHeader: true },
            { label: 'AsyncStateBox', href: 'components/detail.html?id=asyncstatebox-state-machine', isSubLink: true },
            { label: 'SmartPullToRefresh', href: 'components/detail.html?id=smartpulltorefreshbox', isSubLink: true },
            { label: 'AppsStepper', href: 'components/detail.html?id=appsstepper-wizard-flows', isSubLink: true },
            { label: 'Overlay Controllers', href: 'components/detail.html?id=native-overlay-controllers', isSubLink: true },
        ],
    },
    {
        title: 'Android-Specific Extensions',
        links: [
            { label: 'compose-utils Overview', href: 'utilities/detail.html?id=compose-utils' },
            { label: 'Permissions & Photo Pickers', href: 'utilities/compose-utils-permissions-pickers.html' },
            { label: 'Keyboard & System Actions', href: 'utilities/compose-utils-keyboard-actions.html' },
            { label: 'Themed Form Fields', href: 'components/detail.html?id=themed-form-fields' },
            { label: 'Dropdown Selection', href: 'components/detail.html?id=appsdropdowns-dropdown-selection' },
            { label: 'Page Scaffolds & Drawers', href: 'components/detail.html?id=scaffolds-page-structures' },
            { label: 'Swipeable List Items', href: 'components/detail.html?id=swipeableactionsbox-list-item-gestures' },
            { label: 'update-utils Overview', href: 'utilities/detail.html?id=update-utils' },
            { label: 'In-App Update System', href: 'components/detail.html?id=in-app-updates-update-utils' },
        ],
    },
];

// Define globally for accordion title clicks
window.toggleNavSection = function(idx) {
    const sectionEl = document.getElementById(`nav-section-${idx}`);
    if (sectionEl) {
        const isCollapsed = sectionEl.classList.toggle('collapsed');
        if (window.docsSectionStates) {
            window.docsSectionStates[idx] = isCollapsed;
        }
    }
};

function initInPageToc() {
    if (!window.docsEnableToc) return;
    const article = document.querySelector('.docs-article');
    if (!article) return;
    const headers = article.querySelectorAll('h2, h3');
    if (headers.length === 0) return;

    const main = document.querySelector('.docs-main');
    if (!main) return;

    const tocRail = document.createElement('div');
    tocRail.className = 'docs-toc-rail';

    let html = '<div class="docs-toc-title">On this page</div>';
    html += '<ul class="docs-toc-list">';
    headers.forEach((header, idx) => {
        if (!header.id) {
            header.id = 'toc-header-' + idx;
        }
        const isH3 = header.tagName.toLowerCase() === 'h3';
        const classes = isH3 ? 'docs-toc-link docs-toc-h3' : 'docs-toc-link';
        html += `<li><a href="#${header.id}" class="${classes}">${header.textContent}</a></li>`;
    });
    html += '</ul>';
    tocRail.innerHTML = html;

    const wrapper = document.createElement('div');
    wrapper.className = 'docs-content-wrapper';
    article.parentNode.insertBefore(wrapper, article);
    wrapper.appendChild(article);
    wrapper.appendChild(tocRail);
}

function initDocsChrome() {
    const base = window.docsBasePath || '';
    const sidebarEl = document.getElementById('docs-sidebar');
    const topbarEl = document.getElementById('docs-topbar');

    // Inject favicon dynamically if not already present
    if (!document.querySelector('link[rel="icon"]')) {
        const link = document.createElement('link');
        link.rel = 'icon';
        link.type = 'image/png';
        link.href = base + '../img/logo.png';
        document.head.appendChild(link);
    }


    // Restore desktop sidebar collapse preference on load
    const isSidebarCollapsed = localStorage.getItem('sidebarCollapsed') === 'true';
    if (isSidebarCollapsed && window.innerWidth > 860 && !document.body.classList.contains('docs-landing-page')) {
        document.body.classList.add('sidebar-collapsed');
    }

    if (sidebarEl) {
        // Initialize collapse states cache in memory
        window.docsSectionStates = window.docsSectionStates || {};
        const here = (window.location.pathname + window.location.search).replace(/.*\/(docs\/.*)$/, '$1');

        let html = `
            <div class="docs-brand">
                <a href="${base}index.html" style="display: flex; align-items: center; gap: 10px; text-decoration: none; color: var(--text-main); font-weight: 700; font-size: 22px;">
                    <img src="${base}../img/logo.png" alt="Kolt Logo" style="height: 30px; width: auto; object-fit: contain;">
                    <span>Kolt</span>
                </a>
            </div>
            <input type="text" class="docs-search" id="docs-search-input" placeholder="Filter pages...">
        `;

        DOCS_NAV.forEach((section, idx) => {
            // Find if any link inside this section is active
            let hasActiveLink = false;
            section.links.forEach(link => {
                const target = link.href;
                if (here.endsWith(target.split('#')[0])) {
                    hasActiveLink = true;
                }
            });

            // Initialize state: expanded (false) if contains active link, otherwise collapsed (true)
            if (window.docsSectionStates[idx] === undefined) {
                window.docsSectionStates[idx] = !hasActiveLink;
            }

            const isCollapsed = window.docsSectionStates[idx];
            const sectionClass = isCollapsed ? 'docs-nav-section collapsed' : 'docs-nav-section';

            html += `
                <div class="${sectionClass}" id="nav-section-${idx}">
                    <div class="docs-nav-section-title" onclick="toggleNavSection(${idx})">
                        <span>${section.title}</span>
                        <span class="docs-nav-chevron">
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="6 9 12 15 18 9"></polyline>
                            </svg>
                        </span>
                    </div>
                    <div class="docs-nav-section-links">
            `;

            section.links.forEach(link => {
                const resolvedHref = base + link.href;
                let classes = ['docs-nav-link'];
                if (link.isHeader) classes.push('docs-nav-sub-header');
                if (link.isSubLink) classes.push('docs-nav-sub-link');

                if (here.endsWith(link.href.split('#')[0])) {
                    classes.push('active');
                }
                html += `<a class="${classes.join(' ')}" href="${resolvedHref}" data-label="${link.label.toLowerCase()}">${link.label}</a>`;
            });

            html += `
                    </div>
                </div>
            `;
        });
        sidebarEl.innerHTML = html;

        const searchInput = document.getElementById('docs-search-input');
        if (searchInput) {
            searchInput.addEventListener('input', () => {
                const q = searchInput.value.trim().toLowerCase();
                if (!q) {
                    // Restore original collapsed states when search is cleared
                    DOCS_NAV.forEach((section, idx) => {
                        const sectionEl = document.getElementById(`nav-section-${idx}`);
                        if (sectionEl) {
                            const isCollapsed = window.docsSectionStates[idx];
                            sectionEl.classList.toggle('collapsed', isCollapsed);
                        }
                    });
                    sidebarEl.querySelectorAll('.docs-nav-link').forEach(a => {
                        a.classList.remove('hidden-by-search');
                    });
                    return;
                }

                // Search is active: show matches, expand containing groups
                DOCS_NAV.forEach((section, idx) => {
                    const sectionEl = document.getElementById(`nav-section-${idx}`);
                    if (!sectionEl) return;

                    let sectionHasMatches = false;
                    const linkEls = sectionEl.querySelectorAll('.docs-nav-link');
                    
                    linkEls.forEach(a => {
                        const matches = a.getAttribute('data-label').includes(q);
                        a.classList.toggle('hidden-by-search', !matches);
                        if (matches) {
                            sectionHasMatches = true;
                        }
                    });

                    // Expand section if it has matches, otherwise collapse it
                    sectionEl.classList.toggle('collapsed', !sectionHasMatches);
                });
            });
        }
    }

    if (topbarEl) {
        let breadcrumbHtml = `<div class="docs-breadcrumb"><a href="${base}../index.html">Kolt</a> / <a href="${base}index.html">Docs</a></div>`;
        if (document.body.classList.contains('docs-landing-page')) {
            breadcrumbHtml = `
                <div style="display: flex; align-items: center; gap: 24px;">
                    <a id="topbar-brand" href="${base}../index.html" style="display: flex; align-items: center; gap: 10px; text-decoration: none; color: var(--text-main); font-weight: 700; font-size: 21px; opacity: 0; transform: translateY(-8px); visibility: hidden; transition: opacity 0.3s ease, transform 0.3s ease, visibility 0.3s;">
                        <img src="${base}../img/logo.png" alt="Kolt Logo" style="height: 28px; width: auto; object-fit: contain;">
                        <span>Kolt</span>
                    </a>
                    <nav style="display: flex; gap: 18px; align-items: center;" class="desktop-nav">
                        <a href="${base}getting-started/index.html" style="text-decoration: none; color: var(--text-muted); font-size: 13.5px; font-weight: 500; transition: color 0.15s ease;">Docs</a>
                        <a href="${base}../showcase/index.html" style="text-decoration: none; color: var(--text-muted); font-size: 13.5px; font-weight: 500; transition: color 0.15s ease;">Showcase</a>
                        <a href="${base}api/index.html" style="text-decoration: none; color: var(--text-muted); font-size: 13.5px; font-weight: 500; transition: color 0.15s ease;">KDoc API</a>
                        <a href="${base}guides/ai-steering.html" style="text-decoration: none; color: var(--primary-color); font-size: 13px; font-weight: 600; background: var(--primary-glow); padding: 5px 12px; border-radius: 14px; display: flex; align-items: center; gap: 5px; transition: background 0.2s ease;">
                            <span>🤖</span> AI Ready
                        </a>
                    </nav>
                </div>
            `;
        }

        const toggleButtonHtml = `
            <button id="docs-sidebar-toggle" class="sidebar-toggle-btn" aria-label="Toggle Sidebar" style="margin-right: 12px;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="3" y1="12" x2="21" y2="12"></line>
                    <line x1="3" y1="6" x2="21" y2="6"></line>
                    <line x1="3" y1="18" x2="21" y2="18"></line>
                </svg>
            </button>
        `;

        const searchWrapperHtml = `
            <div class="topbar-search-wrapper" style="position: relative; display: flex; align-items: center;">
                <svg class="search-icon" width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="position: absolute; left: 10px; color: var(--text-muted); pointer-events: none;">
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
                <input type="text" id="topbar-search-input" class="topbar-search-input" placeholder="Search... (⌘K)">
                <div id="topbar-search-results" class="topbar-search-results hidden"></div>
            </div>
        `;

        topbarEl.innerHTML = `
            <div style="display: flex; align-items: center; width: 100%; justify-content: space-between; position: relative;">
                <div style="display: flex; align-items: center;">
                    ${document.body.classList.contains('docs-landing-page') ? '' : toggleButtonHtml}
                    ${breadcrumbHtml}
                </div>
                <div class="docs-topbar-actions" style="display: flex; align-items: center; gap: 10px;">
                    ${searchWrapperHtml}
                    <a href="${base}../showcase/index.html" class="btn btn-outline desktop-only" style="padding:6px 14px; font-size:12.5px; border-radius:16px; text-decoration: none;">Open Showcase</a>
                    <button id="docs-theme-toggle" class="theme-toggle-btn header-theme-toggle desktop-only" style="padding:6px 12px; font-size:12.5px; border-radius:16px;">🌙 Dark</button>
                    
                    <!-- Mobile Hamburger Menu Button -->
                    <button id="mobile-menu-toggle" class="mobile-menu-toggle-btn" aria-label="Toggle Menu" style="display: none; border: none; background: transparent; color: var(--text-main); cursor: pointer; padding: 8px; align-items: center; justify-content: center;">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="3" y1="12" x2="21" y2="12"></line>
                            <line x1="3" y1="6" x2="21" y2="6"></line>
                            <line x1="3" y1="18" x2="21" y2="18"></line>
                        </svg>
                    </button>
                </div>
                
                <!-- Mobile Navigation Dropdown Menu -->
                <div id="mobile-menu-panel" class="mobile-menu-panel" style="display: none; position: absolute; top: 100%; right: 0; background-color: var(--surface-color); border: 1px solid var(--border-color); border-radius: 12px; padding: 12px; min-width: 200px; box-shadow: 0 4px 24px rgba(0,0,0,0.15); z-index: 1000; flex-direction: column; gap: 8px; margin-top: 8px;">
                    ${document.body.classList.contains('docs-landing-page') ? `
                        <a href="${base}getting-started/index.html" style="text-decoration: none; color: var(--text-muted); font-size: 14px; padding: 8px 12px; border-radius: 8px; display: block; font-weight: 500;">Docs</a>
                        <a href="${base}../showcase/index.html" style="text-decoration: none; color: var(--text-muted); font-size: 14px; padding: 8px 12px; border-radius: 8px; display: block; font-weight: 500;">Showcase</a>
                        <a href="${base}api/index.html" style="text-decoration: none; color: var(--text-muted); font-size: 14px; padding: 8px 12px; border-radius: 8px; display: block; font-weight: 500;">KDoc API</a>
                        <a href="${base}guides/ai-steering.html" style="text-decoration: none; color: var(--primary-color); font-size: 14px; padding: 8px 12px; border-radius: 8px; display: block; font-weight: 600; background: var(--primary-glow);">🤖 AI Ready</a>
                    ` : ''}
                    <a href="${base}../showcase/index.html" style="text-decoration: none; color: var(--text-main); font-size: 14px; padding: 8px 12px; border-radius: 8px; display: block; font-weight: 500; border-top: ${document.body.classList.contains('docs-landing-page') ? '1px solid var(--border-color)' : 'none'}; padding-top: 12px;">Open Showcase</a>
                    <button id="mobile-theme-toggle" style="background: transparent; border: none; text-align: left; width: 100%; color: var(--text-main); font-size: 14px; padding: 8px 12px; border-radius: 8px; cursor: pointer; display: block; font-weight: 500;">🌙 Dark Mode</button>
                </div>
            </div>
        `;

        const topbarSearchInput = document.getElementById('topbar-search-input');
        const topbarSearchResults = document.getElementById('topbar-search-results');

        if (topbarSearchInput && topbarSearchResults) {
            // Global keydown listener for Command+K or Control+K focus
            window.addEventListener('keydown', (e) => {
                if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
                    e.preventDefault();
                    topbarSearchInput.focus();
                }
            });

            topbarSearchInput.addEventListener('input', () => {
                const q = topbarSearchInput.value.trim().toLowerCase();
                
                // Keep sidebar search input in sync if it exists
                const sidebarSearchInput = document.getElementById('docs-search-input');
                if (sidebarSearchInput) {
                    sidebarSearchInput.value = topbarSearchInput.value;
                    // Trigger the input event programmatically to filter the sidebar
                    const event = new Event('input', { bubbles: true });
                    sidebarSearchInput.dispatchEvent(event);
                }

                if (!q) {
                    topbarSearchResults.classList.add('hidden');
                    topbarSearchResults.innerHTML = '';
                    return;
                }

                // Filter link items inside DOCS_NAV
                const matches = [];
                DOCS_NAV.forEach(section => {
                    section.links.forEach(link => {
                        // Skip sub-headers that are just section headers in categories
                        if (link.isHeader) return;
                        
                        const labelMatch = link.label.toLowerCase().includes(q);
                        const sectionMatch = section.title.toLowerCase().includes(q);
                        
                        if (labelMatch || sectionMatch) {
                            matches.push({
                                section: section.title,
                                label: link.label,
                                href: base + link.href
                            });
                        }
                    });
                });

                if (matches.length === 0) {
                    topbarSearchResults.innerHTML = `<div style="padding: 14px 16px; font-size: 13px; color: var(--text-muted); text-align: center;">No matches found for "${q}"</div>`;
                } else {
                    topbarSearchResults.innerHTML = matches.slice(0, 8).map(m => `
                        <a class="search-result-item" href="${m.href}">
                            <span class="search-result-section">${m.section}</span>
                            <span class="search-result-title">${m.label}</span>
                        </a>
                    `).join('');
                }
                topbarSearchResults.classList.remove('hidden');
            });

            // Hide results on click away
            document.addEventListener('click', (e) => {
                const wrapper = document.querySelector('.topbar-search-wrapper');
                if (wrapper && !wrapper.contains(e.target)) {
                    topbarSearchResults.classList.add('hidden');
                }
            });
        }

        // Register backdrop overlay for mobile responsive menus
        if (!document.querySelector('.docs-backdrop')) {
            const backdrop = document.createElement('div');
            backdrop.className = 'docs-backdrop';
            document.body.appendChild(backdrop);
            backdrop.addEventListener('click', () => {
                document.body.classList.remove('sidebar-open');
            });
        }

        const toggle = document.getElementById('docs-sidebar-toggle');
        if (toggle) {
            toggle.addEventListener('click', () => {
                if (window.innerWidth <= 860) {
                    document.body.classList.toggle('sidebar-open');
                } else {
                    const isCollapsed = document.body.classList.toggle('sidebar-collapsed');
                    localStorage.setItem('sidebarCollapsed', isCollapsed);
                }
            });
        }

        const themeToggle = document.getElementById('docs-theme-toggle');
        if (themeToggle) {
            themeToggle.addEventListener('click', () => {
                const isDark = document.body.classList.toggle('dark-mode');
                document.body.classList.toggle('light-mode', !isDark);
                themeToggle.textContent = isDark ? '☀️ Light' : '🌙 Dark';

                // Swap homepage logo if present
                const landingLogo = document.getElementById('landing-logo');
                if (landingLogo) {
                    const prefix = base ? '' : '../';
                    landingLogo.src = isDark ? prefix + 'img/logo_name_dark.png' : prefix + 'img/logo_name_light.png';
                }
                
                // Sync mobile theme toggle text if exists
                const mobileThemeToggle = document.getElementById('mobile-theme-toggle');
                if (mobileThemeToggle) {
                    mobileThemeToggle.textContent = isDark ? '☀️ Light Mode' : '🌙 Dark Mode';
                }
            });
        }

        // Mobile Hamburger Navigation Menu Toggle
        const mobileMenuToggle = document.getElementById('mobile-menu-toggle');
        const mobileMenuPanel = document.getElementById('mobile-menu-panel');
        
        if (mobileMenuToggle && mobileMenuPanel) {
            mobileMenuToggle.addEventListener('click', (e) => {
                e.stopPropagation();
                const isHidden = mobileMenuPanel.style.display === 'none' || mobileMenuPanel.style.display === '';
                mobileMenuPanel.style.display = isHidden ? 'flex' : 'none';
            });
            
            document.addEventListener('click', (e) => {
                if (!mobileMenuPanel.contains(e.target) && e.target !== mobileMenuToggle && !mobileMenuToggle.contains(e.target)) {
                    mobileMenuPanel.style.display = 'none';
                }
            });
        }
        
        const mobileThemeToggle = document.getElementById('mobile-theme-toggle');
        if (mobileThemeToggle) {
            mobileThemeToggle.addEventListener('click', () => {
                if (themeToggle) {
                    themeToggle.click(); // Trigger the main theme toggle logic
                }
            });
            // Initial sync on load
            const isDark = document.body.classList.contains('dark-mode');
            mobileThemeToggle.textContent = isDark ? '☀️ Light Mode' : '🌙 Dark Mode';
        }
    }

    // Set correct initial logo for homepage on load
    const landingLogo = document.getElementById('landing-logo');
    if (landingLogo) {
        const prefix = base ? '' : '../';
        const isDark = document.body.classList.contains('dark-mode');
        landingLogo.src = isDark ? prefix + 'img/logo_name_dark.png' : prefix + 'img/logo_name_light.png';
    }

    // Scroll parallax reveal logic for topbar brand on landing page
    if (document.body.classList.contains('docs-landing-page')) {
        const topbarBrand = document.getElementById('topbar-brand');
        const heroLogo = document.getElementById('landing-logo');
        
        if (topbarBrand) {
            const handleScroll = () => {
                const scrollY = window.scrollY || window.pageYOffset;
                let startFade = 50;
                let endFade = 140;
                
                if (heroLogo) {
                    const rect = heroLogo.getBoundingClientRect();
                    if (rect.bottom > 0 || rect.top > 0) {
                        const logoBottomAbsolute = rect.bottom + scrollY;
                        startFade = logoBottomAbsolute - 150;
                        endFade = logoBottomAbsolute - 60;
                    }
                }
                
                let progress = (scrollY - startFade) / (endFade - startFade);
                progress = Math.max(0, Math.min(1, progress));
                
                topbarBrand.style.opacity = progress;
                topbarBrand.style.transform = `translateY(${(1 - progress) * -8}px)`;
                topbarBrand.style.visibility = progress > 0 ? 'visible' : 'hidden';
            };
            
            window.addEventListener('scroll', handleScroll, { passive: true });
            handleScroll();
        }
    }

    initInPageToc();
}

document.addEventListener('DOMContentLoaded', initDocsChrome);

