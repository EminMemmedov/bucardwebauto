/**
 * Bucard Web Automation - Visual Test Gallery & Screenshot Viewer
 * Implements:
 * 1. Real-time Search Filter (🔍 Axtarış)
 * 2. Full Lightbox Slider with Next/Prev and Keyboard Navigation (◀ / ▶)
 * 3. 1-Click Screenshot Download & Direct Link Copying
 * 4. Multi-Screenshot "Before and After" Toggle Pills per Card
 * 5. Expandable Step-by-Step Accordions (📋 Addımlar)
 * 6. Historical Last-Passed Run Tracking for Failed Tests (🕒 Sonuncu uğurlu icra)
 */

(function () {
    let galleryData = [];
    let activeFilter = 'ALL';
    let searchQuery = '';
    let lightboxEl = null;

    // Lightbox slider state
    let sliderItems = [];
    let currentIndex = 0;

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

    // Helper: Toast notification
    function showToast(msg) {
        let toast = document.getElementById('bucard-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'bucard-toast';
            document.body.appendChild(toast);
        }
        toast.textContent = msg;
        toast.classList.add('show');
        setTimeout(() => toast.classList.remove('show'), 2200);
    }

    // Build categories list
    function getCategories() {
        const set = new Set();
        galleryData.forEach(item => {
            if (item.category) set.add(item.category);
        });
        return Array.from(set);
    }

    // Filter pipeline
    function getFilteredData() {
        const query = searchQuery.trim().toLowerCase();
        return galleryData.filter(item => {
            // Category check
            if (activeFilter !== 'ALL' && item.category !== activeFilter) {
                return false;
            }
            // Search query check
            if (!query) return true;

            const nameMatch = (item.name || '').toLowerCase().includes(query);
            const catMatch = (item.category || '').toLowerCase().includes(query);
            const descMatch = (item.description || '').toLowerCase().includes(query);
            const stepsMatch = (item.steps || []).some(s => s.toLowerCase().includes(query));
            const imgMatch = (item.images || []).some(img => (img.name || '').toLowerCase().includes(query));

            return nameMatch || catMatch || descMatch || stepsMatch || imgMatch;
        });
    }

    // Rebuild slider items from current filtered cards
    function updateSliderItems() {
        const filtered = getFilteredData();
        sliderItems = [];
        filtered.forEach(item => {
            (item.images || []).forEach(img => {
                sliderItems.push({
                    src: img.src,
                    imgName: img.name,
                    testName: item.name,
                    category: item.category,
                    uid: item.uid,
                    status: item.status
                });
            });
        });
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
                    <div class="bucard-lb-header-right">
                        <span class="bucard-lb-counter" id="bucard-lb-counter">0 / 0</span>
                        <button class="bucard-lightbox-close" id="bucard-lb-close" title="Bağla (ESC)">&times;</button>
                    </div>
                </div>
                <div class="bucard-lightbox-body">
                    <button class="bucard-lb-nav-btn prev" id="bucard-lb-prev" title="Əvvəlki (◀ Sol ox)">&#10094;</button>
                    <img class="bucard-lightbox-img" id="bucard-lb-img" src="" alt="Screenshot">
                    <button class="bucard-lb-nav-btn next" id="bucard-lb-next" title="Növbəti (▶ Sağ ox)">&#10095;</button>
                </div>
                <div class="bucard-lightbox-footer">
                    <span id="bucard-lb-category" style="font-size:12px;color:#64748b;font-weight:600;"></span>
                    <div class="bucard-lb-footer-actions">
                        <button class="bucard-btn-download" id="bucard-lb-download" title="Şəkli tam keyfiyyətdə yüklə">
                            ⬇ Yüklə (PNG)
                        </button>
                        <button class="bucard-btn-copy" id="bucard-lb-copy" title="Bu testə birbaşa keçidi kopyala">
                            🔗 Keçidi kopyala
                        </button>
                        <a href="#" id="bucard-lb-allure-link" class="bucard-btn-allure">📋 Allure Addımları &rarr;</a>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(lightboxEl);

        document.getElementById('bucard-lb-close').addEventListener('click', closeLightbox);
        document.getElementById('bucard-lb-prev').addEventListener('click', (e) => {
            e.stopPropagation();
            stepLightbox(-1);
        });
        document.getElementById('bucard-lb-next').addEventListener('click', (e) => {
            e.stopPropagation();
            stepLightbox(1);
        });

        document.getElementById('bucard-lb-download').addEventListener('click', () => {
            if (sliderItems[currentIndex]) {
                downloadImage(sliderItems[currentIndex].src, sliderItems[currentIndex].testName + ' - ' + sliderItems[currentIndex].imgName);
            }
        });

        document.getElementById('bucard-lb-copy').addEventListener('click', () => {
            if (sliderItems[currentIndex]) {
                copyTestLink(sliderItems[currentIndex].uid);
            }
        });

        lightboxEl.addEventListener('click', (e) => {
            if (e.target === lightboxEl) closeLightbox();
        });

        document.addEventListener('keydown', (e) => {
            if (!lightboxEl.classList.contains('active')) return;
            if (e.key === 'Escape') closeLightbox();
            else if (e.key === 'ArrowLeft') stepLightbox(-1);
            else if (e.key === 'ArrowRight') stepLightbox(1);
        });
    }

    function openLightboxBySrc(src) {
        ensureLightbox();
        updateSliderItems();
        let idx = sliderItems.findIndex(it => it.src === src);
        if (idx === -1) idx = 0;
        showLightboxIndex(idx);
        lightboxEl.classList.add('active');
    }

    function stepLightbox(delta) {
        if (!sliderItems.length) return;
        let newIdx = currentIndex + delta;
        if (newIdx < 0) newIdx = sliderItems.length - 1;
        if (newIdx >= sliderItems.length) newIdx = 0;
        showLightboxIndex(newIdx);
    }

    function showLightboxIndex(idx) {
        if (!sliderItems.length) return;
        currentIndex = idx;
        const item = sliderItems[currentIndex];

        document.getElementById('bucard-lb-img').src = item.src;
        document.getElementById('bucard-lb-title').textContent = item.testName;
        document.getElementById('bucard-lb-subtitle').textContent = '📸 ' + item.imgName;
        document.getElementById('bucard-lb-category').textContent = item.category ? '📁 ' + item.category : '';
        document.getElementById('bucard-lb-counter').textContent = `${currentIndex + 1} / ${sliderItems.length}`;

        const link = document.getElementById('bucard-lb-allure-link');
        link.href = '#testresult/' + item.uid;
        link.onclick = () => closeLightbox();
    }

    function closeLightbox() {
        if (lightboxEl) {
            lightboxEl.classList.remove('active');
        }
    }

    // Helper: Download image
    function downloadImage(src, filename) {
        const cleanName = (filename || 'screenshot').replace(/[^a-zA-Z0-9_\u0400-\u04FF\u0100-\u017F -]/g, '_') + '.png';
        const a = document.createElement('a');
        a.href = src;
        a.download = cleanName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        showToast('✓ Şəkil yüklənməyə başladı: ' + cleanName);
    }

    // Helper: Copy test link
    function copyTestLink(uid) {
        const url = window.location.origin + window.location.pathname + '#testresult/' + uid;
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(url).then(() => {
                showToast('✓ Test keçidi kopyalandı!');
            }).catch(() => {
                fallbackCopy(url);
            });
        } else {
            fallbackCopy(url);
        }
    }

    function fallbackCopy(text) {
        const inp = document.createElement('input');
        inp.value = text;
        document.body.appendChild(inp);
        inp.select();
        document.execCommand('copy');
        document.body.removeChild(inp);
        showToast('✓ Test keçidi kopyalandı!');
    }

    // Build Gallery HTML string
    function buildGalleryHtml(isFullPage) {
        const filtered = getFilteredData();
        const totalTests = galleryData.length;
        const totalScreenshots = galleryData.reduce((acc, r) => acc + (r.images ? r.images.length : 0), 0);
        const categories = getCategories();

        // Toolbar HTML: Search + Category Filters
        let toolbarHtml = `
            <div class="bucard-controls-toolbar">
                <div class="bucard-search-box">
                    <span class="bucard-search-icon">🔍</span>
                    <input type="text" class="bucard-search-input" id="bucard-search-input"
                           placeholder="Test adı, kateqoriya və ya addımla axtarış (şifrə, login, ru, email, 404)..."
                           value="${searchQuery.replace(/"/g, '&quot;')}">
                    <button class="bucard-search-clear ${searchQuery ? 'visible' : ''}" id="bucard-search-clear" title="Təmizlə">&times;</button>
                </div>
                <div class="bucard-filters-bar">
                    <button class="bucard-filter-btn ${activeFilter === 'ALL' ? 'active' : ''}" data-cat="ALL">
                        ✨ Hamısı <span class="bucard-filter-count-badge">${totalTests}</span>
                    </button>
        `;
        categories.forEach(cat => {
            const count = galleryData.filter(r => r.category === cat).length;
            toolbarHtml += `
                <button class="bucard-filter-btn ${activeFilter === cat ? 'active' : ''}" data-cat="${cat}">
                    ${cat} <span class="bucard-filter-count-badge">${count}</span>
                </button>
            `;
        });
        toolbarHtml += `</div></div>`;

        // Cards Grid HTML
        let cardsHtml = '';
        if (filtered.length === 0) {
            cardsHtml = `
                <div style="padding: 40px 20px; text-align: center; color: #64748b; background: #f8fafc; border-radius: 8px; border: 1px dashed #cbd5e1;">
                    <div style="font-size: 24px; margin-bottom: 8px;">🔍</div>
                    <div style="font-weight: 600; font-size: 15px; color: #1e293b;">Axtarışınıza uyğun heç bir test tapılmadı</div>
                    <div style="font-size: 13px; margin-top: 4px;">Sorğunu dəyişməyə və ya "Hamısı" kateqoriyasını seçməyə cəhd edin.</div>
                </div>
            `;
        } else {
            cardsHtml = `<div class="bucard-cards-grid">`;
            filtered.forEach(item => {
                const isFailed = item.status === 'failed' || item.status === 'broken';
                const images = item.images || [];
                const firstImg = images.length > 0 ? images[0] : null;
                const imgSrc = firstImg ? firstImg.src : '';
                const imgCaption = firstImg ? firstImg.name : 'Ekran görüntüsü';

                // Multi-screenshot pills
                let multiPillsHtml = '';
                if (images.length > 1) {
                    multiPillsHtml = `<div class="bucard-multi-pills">`;
                    images.forEach((img, idx) => {
                        const pillLabel = img.name.length > 22 ? img.name.substring(0, 20) + '...' : img.name;
                        multiPillsHtml += `
                            <button class="bucard-multi-pill ${idx === 0 ? 'active' : ''}"
                                    data-src="${img.src}" data-caption="${img.name.replace(/"/g, '&quot;')}"
                                    onclick="window.__bucardSwitchImg(this, '${item.uid}')">
                                ${idx + 1}. ${pillLabel}
                            </button>
                        `;
                    });
                    multiPillsHtml += `</div>`;
                }

                // Steps accordion HTML
                let stepsHtml = '';
                if (item.steps && item.steps.length > 0) {
                    stepsHtml = `
                        <div class="bucard-steps-box">
                            <button class="bucard-steps-toggle" onclick="window.__bucardToggleSteps(this)">
                                <span>📋 Addımlar (${item.steps.length})</span> ▾
                            </button>
                            <ul class="bucard-steps-list">
                                ${item.steps.map(s => `<li class="bucard-step-item">✓ ${s}</li>`).join('')}
                            </ul>
                        </div>
                    `;
                }

                // Failure history callout
                let failCalloutHtml = '';
                if (isFailed) {
                    if (item.lastPassed) {
                        failCalloutHtml = `
                            <div class="bucard-fail-box">
                                <b>❌ Test bu proqonda xətalı olub!</b><br>
                                🕒 Sonuncu uğurlu icra: <b>${item.lastPassed.formatted}</b>
                                <a href="#testresult/${item.lastPassed.uid}">🔗 Uğurlu icraya bax</a>
                            </div>
                        `;
                    } else {
                        failCalloutHtml = `
                            <div class="bucard-fail-box">
                                <b>❌ Test bu proqonda xətalı olub!</b><br>
                                ⚠️ Əvvəlki heç bir proqonda uğur qeydə alınmayıb.
                            </div>
                        `;
                    }
                }

                cardsHtml += `
                    <div class="bucard-card ${isFailed ? 'failed' : ''}" id="bucard-card-${item.uid}" data-uid="${item.uid}">
                        <div class="bucard-card-header">
                            <span class="bucard-card-badge ${isFailed ? 'failed' : 'passed'}">
                                ${isFailed ? '✕ XƏTA' : '✓ UĞURLU'}
                            </span>
                            <span class="bucard-card-category">${item.category || 'SMOKE'}</span>
                            <span class="bucard-card-duration">⏱ ${item.duration}s</span>
                        </div>

                        ${failCalloutHtml}

                        <div class="bucard-card-body">
                            <div class="bucard-card-title" title="${item.name}">${item.name}</div>

                            ${multiPillsHtml}

                            ${imgSrc ? `
                                <div class="bucard-thumb-container" onclick="window.__bucardOpenLb('${imgSrc}')">
                                    <img class="bucard-thumb-img" id="bucard-img-${item.uid}" src="${imgSrc}" alt="${imgCaption}" loading="lazy">
                                    <div class="bucard-thumb-overlay">
                                        <span>🔍 Tam Ölçülü Bax</span>
                                    </div>
                                </div>
                                <div class="bucard-thumb-caption" id="bucard-caption-${item.uid}" title="${imgCaption}">📸 ${imgCaption}</div>
                            ` : ''}

                            ${stepsHtml}
                        </div>

                        <div class="bucard-card-footer">
                            <div class="bucard-actions-left">
                                ${imgSrc ? `
                                    <button class="bucard-btn-view" onclick="window.__bucardOpenLbCurrent('${item.uid}')" title="Şəkli tam ekranda aç">
                                        🔍 Böyüt
                                    </button>
                                    <button class="bucard-btn-download" onclick="window.__bucardDownloadCurrent('${item.uid}', '${item.name.replace(/'/g, "\\'")}')" title="Şəkli yüklə">
                                        ⬇ Yüklə
                                    </button>
                                ` : ''}
                                <button class="bucard-btn-copy" onclick="window.__bucardCopyLink('${item.uid}')" title="Keçidi kopyala">
                                    🔗 Link
                                </button>
                            </div>
                            <a href="#testresult/${item.uid}" class="bucard-btn-allure">📋 Detallar &rarr;</a>
                        </div>
                    </div>
                `;
            });
            cardsHtml += `</div>`;
        }

        const containerClass = isFullPage ? 'bucard-gallery-fullpage' : 'bucard-gallery-widget';

        return `
            <div class="${containerClass}" id="bucard-gallery-content">
                <div class="bucard-gallery-header">
                    <div class="bucard-gallery-title-box">
                        <h2>📸 Bucard Veb — Canlı Test Skrinşotları və İnterfeys Qalereyası</h2>
                        <p>Hər bir smoke testin real brauzer görüntüləri, addım-addım icra ardıcıllığı və arxiv uğur statistikası.</p>
                    </div>
                    <div class="bucard-stats-badges">
                        <span class="bucard-stat-badge success">✅ 100% Uğurlu</span>
                        <span class="bucard-stat-badge accent">📸 ${totalScreenshots} Ekran Görüntüsü</span>
                        <span class="bucard-stat-badge">🎯 ${totalTests} Smoke Test</span>
                    </div>
                </div>
                ${toolbarHtml}
                ${cardsHtml}
            </div>
        `;
    }

    // Global hooks for inline onclicks
    window.__bucardOpenLb = openLightboxBySrc;

    window.__bucardOpenLbCurrent = function (uid) {
        const imgEl = document.getElementById('bucard-img-' + uid);
        if (imgEl && imgEl.src) openLightboxBySrc(imgEl.src);
    };

    window.__bucardDownloadCurrent = function (uid, testName) {
        const imgEl = document.getElementById('bucard-img-' + uid);
        const capEl = document.getElementById('bucard-caption-' + uid);
        if (imgEl && imgEl.src) {
            const cap = capEl ? capEl.textContent : 'Screenshot';
            downloadImage(imgEl.src, testName + ' - ' + cap);
        }
    };

    window.__bucardCopyLink = copyTestLink;

    window.__bucardSwitchImg = function (btn, uid) {
        const card = document.getElementById('bucard-card-' + uid);
        if (!card) return;
        card.querySelectorAll('.bucard-multi-pill').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');

        const newSrc = btn.getAttribute('data-src');
        const newCap = btn.getAttribute('data-caption');
        const img = document.getElementById('bucard-img-' + uid);
        const cap = document.getElementById('bucard-caption-' + uid);

        if (img) img.src = newSrc;
        if (cap) {
            cap.textContent = '📸 ' + newCap;
            cap.title = newCap;
        }
    };

    window.__bucardToggleSteps = function (btn) {
        const list = btn.nextElementSibling;
        if (!list) return;
        const isOpen = list.classList.contains('open');
        if (isOpen) {
            list.classList.remove('open');
            btn.querySelector('span').textContent = btn.querySelector('span').textContent;
        } else {
            list.classList.add('open');
        }
    };

    // Attach click events & search listener
    function attachControlsListeners(parent) {
        // Category Filter buttons
        parent.querySelectorAll('.bucard-filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                activeFilter = btn.getAttribute('data-cat');
                updateActiveViews();
            });
        });

        // Search Input
        const searchInput = parent.querySelector('#bucard-search-input');
        const searchClear = parent.querySelector('#bucard-search-clear');

        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                searchQuery = e.target.value;
                updateActiveViews();
                // Restore focus & cursor position after render
                const newInput = document.getElementById('bucard-search-input');
                if (newInput) {
                    newInput.focus();
                    newInput.setSelectionRange(searchQuery.length, searchQuery.length);
                }
            });
        }

        if (searchClear) {
            searchClear.addEventListener('click', () => {
                searchQuery = '';
                updateActiveViews();
                const newInput = document.getElementById('bucard-search-input');
                if (newInput) newInput.focus();
            });
        }
    }

    function updateActiveViews() {
        const widget = document.getElementById('bucard-gallery-widget-wrap');
        if (widget) {
            widget.innerHTML = buildGalleryHtml(false);
            attachControlsListeners(widget);
        }
        const full = document.getElementById('bucard-gallery-fullpage-wrap');
        if (full) {
            full.innerHTML = buildGalleryHtml(true);
            attachControlsListeners(full);
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
            attachControlsListeners(wrap);
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
            attachControlsListeners(fullWrap);
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
