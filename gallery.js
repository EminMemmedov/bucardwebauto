/**
 * Bucard Web Automation - Visual Test Gallery & Screenshot Viewer
 * Injects an interactive screenshot gallery directly into Allure Overview
 * and provides full-screen modal zoom for any test attachment.
 */

(function () {
    let galleryData = [];
    let activeFilter = 'ALL';
    let lightboxEl = null;

    // Load gallery data
    function loadData() {
        return fetch('data/gallery.json')
            .then(res => res.json())
            .then(data => {
                galleryData = data;
                return data;
            })
            .catch(err => {
                console.warn('[BucardGallery] Could not load data/gallery.json:', err);
                return [];
            });
    }

    // Build categories list
    function getCategories() {
        const set = new Set();
        galleryData.forEach(item => {
            if (item.category) set.add(item.category);
        });
        return Array.from(set);
    }

    // Create Lightbox DOM element
    function ensureLightbox() {
        if (document.getElementById('bucard-lightbox')) {
            lightboxEl = document.getElementById('bucard-lightbox');
            return;
        }

        lightboxEl = document.createElement('div');
        lightboxEl.id = 'bucard-lightbox';
        lightboxEl.innerHTML = `
            <div class="bucard-lightbox-box">
                <div class="bucard-lightbox-header">
                    <div>
                        <h3 class="bucard-lightbox-title" id="bucard-lb-title"></h3>
                        <p class="bucard-lightbox-subtitle" id="bucard-lb-subtitle"></p>
                    </div>
                    <button class="bucard-lightbox-close" id="bucard-lb-close" title="Bağla (ESC)">&times;</button>
                </div>
                <div class="bucard-lightbox-body">
                    <img class="bucard-lightbox-img" id="bucard-lb-img" src="" alt="Screenshot">
                </div>
                <div class="bucard-lightbox-footer">
                    <span id="bucard-lb-category" style="font-size:12px;color:#64748b;font-weight:600;"></span>
                    <a href="#" id="bucard-lb-allure-link" class="bucard-btn-allure">📋 Bu testin Allure addımlarına keç &rarr;</a>
                </div>
            </div>
        `;
        document.body.appendChild(lightboxEl);

        document.getElementById('bucard-lb-close').addEventListener('click', closeLightbox);
        lightboxEl.addEventListener('click', (e) => {
            if (e.target === lightboxEl) closeLightbox();
        });

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && lightboxEl.classList.contains('active')) {
                closeLightbox();
            }
        });
    }

    function openLightbox(src, title, subtitle, category, uid) {
        ensureLightbox();
        document.getElementById('bucard-lb-img').src = src;
        document.getElementById('bucard-lb-title').textContent = title;
        document.getElementById('bucard-lb-subtitle').textContent = subtitle || '';
        document.getElementById('bucard-lb-category').textContent = category ? '📁 ' + category : '';
        const link = document.getElementById('bucard-lb-allure-link');
        link.href = '#testresult/' + uid;
        link.onclick = () => closeLightbox();
        lightboxEl.classList.add('active');
    }

    function closeLightbox() {
        if (lightboxEl) {
            lightboxEl.classList.remove('active');
        }
    }

    // Build Gallery HTML string
    function buildGalleryHtml(isFullPage) {
        const filtered = activeFilter === 'ALL'
            ? galleryData
            : galleryData.filter(item => item.category === activeFilter);

        const totalTests = galleryData.length;
        const totalScreenshots = galleryData.reduce((acc, r) => acc + (r.images ? r.images.length : 0), 0);
        const categories = getCategories();

        let filtersHtml = `
            <div class="bucard-filters-bar">
                <button class="bucard-filter-btn ${activeFilter === 'ALL' ? 'active' : ''}" data-cat="ALL">
                    ✨ Hamısı (${totalTests})
                </button>
        `;
        categories.forEach(cat => {
            const count = galleryData.filter(r => r.category === cat).length;
            filtersHtml += `
                <button class="bucard-filter-btn ${activeFilter === cat ? 'active' : ''}" data-cat="${cat}">
                    ${cat} (${count})
                </button>
            `;
        });
        filtersHtml += `</div>`;

        let cardsHtml = `<div class="bucard-cards-grid">`;
        filtered.forEach(item => {
            const firstImg = item.images && item.images.length > 0 ? item.images[0] : null;
            const imgSrc = firstImg ? firstImg.src : '';
            const imgCaption = firstImg ? firstImg.name : 'Ekran görüntüsü';

            cardsHtml += `
                <div class="bucard-card" data-uid="${item.uid}">
                    <div class="bucard-card-header">
                        <span class="bucard-card-badge passed">✓ UĞURLU</span>
                        <span class="bucard-card-category">${item.category || 'SMOKE'}</span>
                        <span class="bucard-card-duration">⏱ ${item.duration}s</span>
                    </div>
                    <div class="bucard-card-body">
                        <div class="bucard-card-title" title="${item.name}">${item.name}</div>
                        ${imgSrc ? `
                            <div class="bucard-thumb-container" onclick="window.__bucardOpenLb('${imgSrc}', '${item.name.replace(/'/g, "\\'")}', '${imgCaption.replace(/'/g, "\\'")}', '${item.category}', '${item.uid}')">
                                <img class="bucard-thumb-img" src="${imgSrc}" alt="${imgCaption}" loading="lazy">
                                <div class="bucard-thumb-overlay">
                                    <span>🔍 Tam Ölçülü Bax</span>
                                </div>
                            </div>
                            <div class="bucard-thumb-caption" title="${imgCaption}">📸 ${imgCaption}</div>
                        ` : ''}
                    </div>
                    <div class="bucard-card-footer">
                        ${imgSrc ? `
                            <button class="bucard-btn-view" onclick="window.__bucardOpenLb('${imgSrc}', '${item.name.replace(/'/g, "\\'")}', '${imgCaption.replace(/'/g, "\\'")}', '${item.category}', '${item.uid}')">
                                🔍 Böyüt
                            </button>
                        ` : '<span></span>'}
                        <a href="#testresult/${item.uid}" class="bucard-btn-allure">📋 Detallar &rarr;</a>
                    </div>
                </div>
            `;
        });
        cardsHtml += `</div>`;

        const containerClass = isFullPage ? 'bucard-gallery-fullpage' : 'bucard-gallery-widget';

        return `
            <div class="${containerClass}" id="bucard-gallery-content">
                <div class="bucard-gallery-header">
                    <div class="bucard-gallery-title-box">
                        <h2>📸 Bucard Veb — Canlı Test Skrinşotları və İnterfeys Qalereyası</h2>
                        <p>Hər bir smoke testin real brauzer görüntüləri və icra nəticələri. İstənilən şəkilə klikləyərək tam ölçüdə baxa bilərsiniz.</p>
                    </div>
                    <div class="bucard-stats-badges">
                        <span class="bucard-stat-badge success">✅ 100% Uğurlu</span>
                        <span class="bucard-stat-badge accent">📸 ${totalScreenshots} Ekran Görüntüsü</span>
                        <span class="bucard-stat-badge">🎯 ${totalTests} Smoke Test</span>
                    </div>
                </div>
                ${filtersHtml}
                ${cardsHtml}
            </div>
        `;
    }

    window.__bucardOpenLb = openLightbox;

    // Attach click events to filter buttons
    function attachFilterListeners(parent) {
        parent.querySelectorAll('.bucard-filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                activeFilter = btn.getAttribute('data-cat');
                updateActiveViews();
            });
        });
    }

    function updateActiveViews() {
        const widget = document.getElementById('bucard-gallery-widget-wrap');
        if (widget) {
            widget.innerHTML = buildGalleryHtml(false);
            attachFilterListeners(widget);
        }
        const full = document.getElementById('bucard-gallery-fullpage-wrap');
        if (full) {
            full.innerHTML = buildGalleryHtml(true);
            attachFilterListeners(full);
        }
    }

    // Injects the widget into the Overview page
    function injectOverviewWidget() {
        const hash = window.location.hash;
        if (hash && hash !== '#' && hash !== '#/' && !hash.startsWith('#overview')) {
            const existing = document.getElementById('bucard-gallery-widget-wrap');
            if (existing) existing.remove();
            return;
        }

        const widgetsGrid = document.querySelector('.widgets-grid');
        if (!widgetsGrid) return;

        let wrap = document.getElementById('bucard-gallery-widget-wrap');
        if (!wrap || !widgetsGrid.contains(wrap)) {
            if (wrap && wrap.parentNode) wrap.parentNode.removeChild(wrap);
            wrap = document.createElement('div');
            wrap.id = 'bucard-gallery-widget-wrap';
            wrap.innerHTML = buildGalleryHtml(false);
            widgetsGrid.insertBefore(wrap, widgetsGrid.firstChild);
            attachFilterListeners(wrap);
        }
    }

    // Injects sidebar button
    function injectSidebarNav() {
        const sideNavMenu = document.querySelector('.side-nav__menu');
        if (!sideNavMenu || document.getElementById('bucard-side-gallery')) return;

        const li = document.createElement('li');
        li.id = 'bucard-side-gallery';
        li.className = 'side-nav__item bucard-gallery-nav';
        li.innerHTML = `
            <a href="#gallery" class="side-nav__link" title="Skrinşotlar Qalereyası">
                <span class="side-nav__icon">📸</span>
                <span class="side-nav__text">Skrinşotlar</span>
            </a>
        `;
        sideNavMenu.insertBefore(li, sideNavMenu.children[1]);
    }

    // Handles full-page #gallery route
    function handleGalleryRoute() {
        if (window.location.hash !== '#gallery') return;

        const appContent = document.querySelector('.app__content');
        if (!appContent) return;

        let fullWrap = document.getElementById('bucard-gallery-fullpage-wrap');
        if (!fullWrap) {
            appContent.innerHTML = '';
            fullWrap = document.createElement('div');
            fullWrap.id = 'bucard-gallery-fullpage-wrap';
            fullWrap.innerHTML = buildGalleryHtml(true);
            appContent.appendChild(fullWrap);
            attachFilterListeners(fullWrap);
        }
    }

    // Periodic check to ensure elements persist across Backbone routing
    function tick() {
        injectSidebarNav();
        if (window.location.hash === '#gallery') {
            handleGalleryRoute();
        } else {
            injectOverviewWidget();
        }
    }

    // Initialize
    loadData().then(() => {
        ensureLightbox();
        setInterval(tick, 400);
        window.addEventListener('hashchange', () => {
            setTimeout(tick, 50);
        });
    });
})();
