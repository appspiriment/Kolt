// Math calculation for G2 continuous squircle path
// Using the exact cubic Bezier blend formula from SmoothCornerShape.kt
function getSquirclePath(radiusNorm, smoothness) {
    const s = smoothness;
    const r = radiusNorm;
    const kCircle = 0.5522847;
    const kBlend = kCircle + s * (1 - kCircle);
    
    const w = 1.0;
    const h = 1.0;
    
    // Limit radius to 0.5 to avoid overlapping corners
    const rTr = Math.min(r, 0.5);
    const rBr = Math.min(r, 0.5);
    const rBl = Math.min(r, 0.5);
    const rTl = Math.min(r, 0.5);
    
    let path = `M 0.5,0 `;
    
    // Top-Right corner
    if (rTr > 0) {
        const startOffset = rTr * (1 + s);
        const controlOffset = rTr * kBlend;
        path += `L ${w - startOffset},0 `;
        path += `C ${w - startOffset + controlOffset},0 ${w},${startOffset - controlOffset} ${w},${startOffset} `;
    } else {
        path += `L ${w},0 `;
    }
    
    // Bottom-Right corner
    if (rBr > 0) {
        const startOffset = rBr * (1 + s);
        const controlOffset = rBr * kBlend;
        path += `L ${w},${h - startOffset} `;
        path += `C ${w},${h - startOffset + controlOffset} ${w - startOffset + controlOffset},${h} ${w - startOffset},${h} `;
    } else {
        path += `L ${w},${h} `;
    }
    
    // Bottom-Left corner
    if (rBl > 0) {
        const startOffset = rBl * (1 + s);
        const controlOffset = rBl * kBlend;
        path += `L ${startOffset},${h} `;
        path += `C ${startOffset - controlOffset},${h} 0,${h - startOffset + controlOffset} 0,${h - startOffset} `;
    } else {
        path += `L 0,${h} `;
    }
    
    // Top-Left corner
    if (rTl > 0) {
        const startOffset = rTl * (1 + s);
        const controlOffset = rTl * kBlend;
        path += `L 0,${startOffset} `;
        path += `C 0,${startOffset - controlOffset} ${startOffset - controlOffset},0 ${startOffset},0 `;
    } else {
        path += `L 0,0 `;
    }
    
    path += "Z";
    return path;
}

document.addEventListener('DOMContentLoaded', () => {
    // --- 0. Setup Static Squircles for Buttons and Dialogs ---
    const svgDefs = document.querySelector('svg defs');
    if (svgDefs) {
        // Button squircle (r = 8px, size = 44px height -> norm = 8/44 = 0.18)
        const btnClip = document.createElementNS('http://www.w3.org/2000/svg', 'clipPath');
        btnClip.setAttribute('id', 'squircle-clip-button');
        btnClip.setAttribute('clipPathUnits', 'objectBoundingBox');
        const btnPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        btnPath.setAttribute('d', getSquirclePath(0.18, 0.55));
        btnClip.appendChild(btnPath);
        svgDefs.appendChild(btnClip);

        // Dialog squircle (r = 12px, size = 300px height -> norm = 12/300 = 0.04)
        const dialogClip = document.createElementNS('http://www.w3.org/2000/svg', 'clipPath');
        dialogClip.setAttribute('id', 'squircle-clip-dialog');
        dialogClip.setAttribute('clipPathUnits', 'objectBoundingBox');
        const dialogPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        dialogPath.setAttribute('id', 'squircle-path-dialog');
        dialogPath.setAttribute('d', getSquirclePath(0.04, 0.55));
        dialogClip.appendChild(dialogPath);
        svgDefs.appendChild(dialogClip);
    }

    // --- 1. Tab Navigation & ScrollSpy ---
    const navButtons = document.querySelectorAll('.nav-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    
    // Smooth scroll on button click
    navButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const tabId = btn.getAttribute('data-tab');
            const targetSection = document.getElementById(tabId);
            if (targetSection) {
                const headerOffset = 80;
                const elementPosition = targetSection.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                
                window.scrollTo({
                    top: offsetPosition,
                    behavior: 'smooth'
                });
            }
        });
    });

    // Intersection Observer to highlight sidebar buttons and update header title
    const observerOptions = {
        root: null,
        rootMargin: '-100px 0px -60% 0px', // Trigger when section occupies top-mid screen
        threshold: 0
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const id = entry.target.getAttribute('id');
                
                navButtons.forEach(btn => {
                    if (btn.getAttribute('data-tab') === id) {
                        btn.classList.add('active');
                        
                        // Smoothly scroll sidebar to keep active button in view
                        btn.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

                        // Update page header title dynamically
                        const pageTitle = document.getElementById('page-title');
                        if (pageTitle) {
                            const iconSpan = btn.querySelector('.icon');
                            if (iconSpan) {
                                pageTitle.textContent = btn.textContent.replace(iconSpan.textContent, '').trim();
                            } else {
                                pageTitle.textContent = btn.textContent.trim();
                            }
                        }
                    } else {
                        btn.classList.remove('active');
                    }
                });
            }
        });
    }, observerOptions);

    tabContents.forEach(section => observer.observe(section));

    // Scroll-driven Parallax Background Interactions
    window.addEventListener('scroll', () => {
        const scrolled = window.pageYOffset;
        const banners = document.querySelectorAll('.parallax-banner');
        banners.forEach(banner => {
            const rect = banner.getBoundingClientRect();
            if (rect.top < window.innerHeight && rect.bottom > 0) {
                const speed = 0.15;
                const yPos = -(rect.top * speed);
                const overlay = banner.querySelector('.banner-overlay');
                if (overlay) {
                    overlay.style.transform = `translateY(${yPos}px)`;
                }
            }
        });
    });

    // --- 2. Theme Toggle ---
    const themeToggle = document.getElementById('theme-toggle');
    const body = document.body;
    
    themeToggle.addEventListener('click', () => {
        const isDark = body.classList.toggle('dark-mode');
        body.classList.toggle('light-mode', !isDark);
        
        const icon = themeToggle.querySelector('.theme-icon');
        const text = themeToggle.querySelector('.theme-text');
        
        if (isDark) {
            icon.textContent = '☀️';
            text.textContent = 'Light Mode';
        } else {
            icon.textContent = '🌙';
            text.textContent = 'Dark Mode';
        }

        const fieldBorderSelect = document.getElementById('field-border-select');
        if (fieldBorderSelect) {
            fieldBorderSelect.dispatchEvent(new Event('change'));
        }
    });

    // --- 3. Corner Shape Playground ---
    const smoothnessSlider = document.getElementById('smoothness-slider');
    const radiusSlider = document.getElementById('radius-slider');
    
    const smoothnessVal = document.getElementById('smoothness-val');
    const radiusVal = document.getElementById('radius-val');
    
    const roundedPreview = document.getElementById('rounded-preview');
    const squirclePath = document.getElementById('squircle-path');
    
    function updateCorners() {
        const smoothness = smoothnessSlider.value / 100;
        const radius = parseInt(radiusSlider.value);
        
        smoothnessVal.textContent = `${Math.round(smoothness * 100)}%`;
        radiusVal.textContent = `${radius}px`;
        
        // Update standard rounded border radius
        roundedPreview.style.borderRadius = `${radius}px`;
        
        // Calculate normalized radius for squircle (140px box size)
        const radiusNorm = radius / 140;
        const newD = getSquirclePath(radiusNorm, smoothness);
        
        // Apply path to SVG clip
        squirclePath.setAttribute('d', newD);
    }
    
    smoothnessSlider.addEventListener('input', updateCorners);
    radiusSlider.addEventListener('input', updateCorners);
    updateCorners(); // initial run

    // --- 4. Accordion Actions ---
    const accordions = document.querySelectorAll('.accordion-item');
    
    accordions.forEach(item => {
        const header = item.querySelector('.accordion-header');
        const body = item.querySelector('.accordion-body');
        
        header.addEventListener('click', () => {
            const isExpanded = item.classList.toggle('expanded');
            if (isExpanded) {
                body.style.maxHeight = `${body.scrollHeight}px`;
            } else {
                body.style.maxHeight = '0px';
            }
        });
    });
    
    // Open panel B by default
    const accB = document.getElementById('accordion-b');
    if (accB) {
        accB.classList.add('expanded');
        const bodyB = accB.querySelector('.accordion-body');
        bodyB.style.maxHeight = `${bodyB.scrollHeight}px`;
    }

    // --- 5. Stepper Logic ---
    const stepperContainer = document.getElementById('horizontal-stepper');
    const stepperCountSelect = document.getElementById('stepper-count-select');
    const stepperLayoutSelect = document.getElementById('stepper-layout-select');
    
    const stepLabels = ['Verification', 'Payment', 'Confirm', 'Delivery', 'Success'];
    const stepTasks = [
        'Verify identity details',
        'Add payment details',
        'Confirm order contents',
        'Choose delivery preferences',
        'Order placed successfully!'
    ];
    
    let stepperCount = 3;
    let stepperCurrentStep = 1;

    function rebuildStepper() {
        if (!stepperContainer) return;
        const count = parseInt(stepperCountSelect ? stepperCountSelect.value : 3);
        const layout = stepperLayoutSelect ? stepperLayoutSelect.value : 'horizontal';
        stepperCount = count;
        
        if (stepperCurrentStep >= count) {
            stepperCurrentStep = count - 1;
        }
        
        if (layout === 'vertical') {
            stepperContainer.className = 'stepper-container vertical';
            stepperContainer.style.flexDirection = 'column';
            stepperContainer.style.alignItems = 'flex-start';
            stepperContainer.style.gap = '24px';
            stepperContainer.style.padding = '20px 40px';
        } else {
            stepperContainer.className = 'stepper-container';
            stepperContainer.style.flexDirection = '';
            stepperContainer.style.alignItems = '';
            stepperContainer.style.gap = '';
            stepperContainer.style.padding = '';
        }
        
        stepperContainer.innerHTML = `
            <div class="stepper-progress-bg">
                <div class="stepper-progress-fill" id="step-progress-bar"></div>
            </div>
        `;
        
        for (let i = 0; i < count; i++) {
            const node = document.createElement('div');
            node.className = 'step-node';
            node.setAttribute('data-step', i);
            
            const circle = document.createElement('div');
            circle.className = 'node-circle';
            circle.textContent = i + 1;
            
            const label = document.createElement('span');
            label.className = 'node-label';
            label.textContent = stepLabels[i] || `Step ${i + 1}`;
            
            node.appendChild(circle);
            node.appendChild(label);
            stepperContainer.appendChild(node);
        }
        
        updateStepperNew();
    }

    function updateStepperNew() {
        const layout = stepperLayoutSelect ? stepperLayoutSelect.value : 'horizontal';
        const fillBar = document.getElementById('step-progress-bar');
        const indicatorText = document.getElementById('step-indicator-text');
        const activeStepDesc = document.getElementById('active-step-desc');
        const prevBtn = document.getElementById('prev-step-btn');
        const nextBtn = document.getElementById('next-step-btn');
        
        const nodes = stepperContainer.querySelectorAll('.step-node');
        nodes.forEach((node, idx) => {
            const circle = node.querySelector('.node-circle');
            node.classList.remove('completed', 'active');
            
            if (layout === 'vertical') {
                node.style.flexDirection = 'row';
                node.style.alignItems = 'center';
                node.style.textAlign = 'left';
                node.style.width = 'auto';
                node.style.gap = '16px';
            } else {
                node.style.flexDirection = '';
                node.style.alignItems = '';
                node.style.textAlign = '';
                node.style.width = '';
                node.style.gap = '';
            }
            
            if (idx < stepperCurrentStep) {
                node.classList.add('completed');
                circle.innerHTML = '✓';
            } else if (idx === stepperCurrentStep) {
                node.classList.add('active');
                circle.innerHTML = idx + 1;
            } else {
                circle.innerHTML = idx + 1;
            }
        });
        
        const progressPercentage = (stepperCurrentStep / (stepperCount - 1)) * 100;
        const progressBg = stepperContainer.querySelector('.stepper-progress-bg');
        
        if (progressBg) {
            if (layout === 'vertical') {
                progressBg.style.left = '65px';
                progressBg.style.top = '40px';
                progressBg.style.bottom = '40px';
                progressBg.style.width = '4px';
                progressBg.style.height = 'auto';
                progressBg.style.right = 'auto';
                
                if (fillBar) {
                    fillBar.style.width = '100%';
                    fillBar.style.height = `${progressPercentage}%`;
                }
            } else {
                progressBg.style.left = '';
                progressBg.style.top = '';
                progressBg.style.bottom = '';
                progressBg.style.width = '';
                progressBg.style.height = '';
                progressBg.style.right = '';
                
                if (fillBar) {
                    fillBar.style.height = '100%';
                    fillBar.style.width = `${progressPercentage}%`;
                }
            }
        }
        
        if (indicatorText) {
            indicatorText.textContent = `Step ${stepperCurrentStep + 1} of ${stepperCount}`;
        }
        if (activeStepDesc) {
            activeStepDesc.textContent = `Current step task: ${stepTasks[stepperCurrentStep] || 'In progress'}`;
        }
        if (prevBtn) prevBtn.disabled = stepperCurrentStep === 0;
        if (nextBtn) nextBtn.disabled = stepperCurrentStep === stepperCount - 1;
    }

    const prevBtn = document.getElementById('prev-step-btn');
    const nextBtn = document.getElementById('next-step-btn');
    
    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (stepperCurrentStep > 0) {
                stepperCurrentStep--;
                updateStepperNew();
            }
        });
    }
    
    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (stepperCurrentStep < stepperCount - 1) {
                stepperCurrentStep++;
                updateStepperNew();
            }
        });
    }

    if (stepperCountSelect) stepperCountSelect.addEventListener('change', rebuildStepper);
    if (stepperLayoutSelect) stepperLayoutSelect.addEventListener('change', rebuildStepper);
    
    rebuildStepper(); // initial setup

    // --- 6. Slider & Progress Bar Sync ---
    const realSlider = document.getElementById('real-slider');
    const sliderContainer = document.querySelector('.apps-slider-container');
    const sliderFill = document.getElementById('custom-slider-fill');
    const progressFill = document.getElementById('custom-progress-fill');
    const sliderValDisplay = document.getElementById('slider-current-val');
    const tooltip = document.getElementById('slider-tooltip');
    const thumbGlow = document.getElementById('slider-thumb-glow');
    
    const sliderMinInput = document.getElementById('slider-min-input');
    const sliderMaxInput = document.getElementById('slider-max-input');
    const sliderStepSelect = document.getElementById('slider-step-select');
    const sliderDisableToggle = document.getElementById('slider-disable-toggle');

    function updateSlider(val) {
        if (!realSlider) return;
        const min = parseFloat(realSlider.getAttribute('min')) || 0;
        const max = parseFloat(realSlider.getAttribute('max')) || 100;
        const pct = Math.max(0, Math.min(100, ((val - min) / (max - min)) * 100));
        
        if (sliderFill) sliderFill.style.width = `${pct}%`;
        if (progressFill) progressFill.style.width = `${pct}%`;
        if (sliderValDisplay) sliderValDisplay.textContent = val;
        
        if (tooltip) {
            tooltip.textContent = val;
            tooltip.style.left = `calc(${pct}% - 24px)`;
        }
        if (thumbGlow) {
            thumbGlow.style.left = `calc(${pct}% - 16px)`;
        }
    }
    
    if (realSlider) {
        realSlider.addEventListener('input', (e) => {
            updateSlider(e.target.value);
        });
        realSlider.addEventListener('mousedown', () => {
            sliderContainer?.classList.add('active');
        });
        realSlider.addEventListener('mouseup', () => {
            sliderContainer?.classList.remove('active');
        });
        realSlider.addEventListener('touchstart', () => {
            sliderContainer?.classList.add('active');
        });
        realSlider.addEventListener('touchend', () => {
            sliderContainer?.classList.remove('active');
        });
    }

    function syncSliderSettings() {
        if (!realSlider) return;
        const min = parseInt(sliderMinInput?.value || 0);
        const max = parseInt(sliderMaxInput?.value || 100);
        const step = parseInt(sliderStepSelect?.value || 1);
        
        realSlider.setAttribute('min', min);
        realSlider.setAttribute('max', max);
        realSlider.setAttribute('step', step);
        
        // Clamp current value
        let val = parseInt(realSlider.value);
        if (val < min) val = min;
        if (val > max) val = max;
        realSlider.value = val;
        
        updateSlider(val);
    }

    if (sliderMinInput) sliderMinInput.addEventListener('input', syncSliderSettings);
    if (sliderMaxInput) sliderMaxInput.addEventListener('input', syncSliderSettings);
    if (sliderStepSelect) sliderStepSelect.addEventListener('change', syncSliderSettings);
    if (sliderDisableToggle) {
        sliderDisableToggle.addEventListener('change', (e) => {
            const disabled = e.target.checked;
            if (realSlider) realSlider.disabled = disabled;
            if (sliderContainer) {
                sliderContainer.style.opacity = disabled ? '0.5' : '1';
                sliderContainer.style.pointerEvents = disabled ? 'none' : 'auto';
            }
        });
    }
    
    syncSliderSettings(); // initial run

    // --- 7. Button Enable & Custom Theme Colors ---
    const btnEnableToggle = document.getElementById('btn-enable-toggle');
    const btnIosStyleSelect = document.getElementById('btn-ios-style-select');
    const btnColorSelect = document.getElementById('btn-color-select');
    const demoButtons = document.querySelectorAll('.btn-demo');
    
    if (btnEnableToggle) {
        btnEnableToggle.addEventListener('change', () => {
            const enabled = btnEnableToggle.checked;
            demoButtons.forEach(btn => {
                btn.disabled = !enabled;
            });
        });
    }

    if (btnIosStyleSelect) {
        btnIosStyleSelect.addEventListener('change', (e) => {
            const val = e.target.value;
            const iosButtons = document.querySelectorAll('.btn-ios');
            iosButtons.forEach(btn => {
                if (val === 'all') {
                    btn.style.display = '';
                } else {
                    if (btn.classList.contains(val)) {
                        btn.style.display = '';
                    } else {
                        btn.style.display = 'none';
                    }
                }
            });
        });
    }

    if (btnColorSelect) {
        btnColorSelect.addEventListener('change', (e) => {
            const val = e.target.value;
            const btnCardFront = btnEnableToggle?.closest('.playground-container')?.querySelector('.demo-card-front');
            if (btnCardFront) {
                if (val === 'success') {
                    btnCardFront.style.setProperty('--primary-color', '#10b981');
                    btnCardFront.style.setProperty('--primary-gradient', 'linear-gradient(135deg, #10b981 0%, #059669 100%)');
                    btnCardFront.style.setProperty('--primary-glow', 'rgba(16, 185, 129, 0.3)');
                } else if (val === 'warning') {
                    btnCardFront.style.setProperty('--primary-color', '#f59e0b');
                    btnCardFront.style.setProperty('--primary-gradient', 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)');
                    btnCardFront.style.setProperty('--primary-glow', 'rgba(245, 158, 11, 0.3)');
                } else if (val === 'error') {
                    btnCardFront.style.setProperty('--primary-color', '#ef4444');
                    btnCardFront.style.setProperty('--primary-gradient', 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)');
                    btnCardFront.style.setProperty('--primary-glow', 'rgba(239, 68, 68, 0.3)');
                } else {
                    btnCardFront.style.removeProperty('--primary-color');
                    btnCardFront.style.removeProperty('--primary-gradient');
                    btnCardFront.style.removeProperty('--primary-glow');
                }
            }
        });
    }

    const btnShapeSelect = document.getElementById('btn-shape-select');
    if (btnShapeSelect) {
        btnShapeSelect.addEventListener('change', (e) => {
            const shape = e.target.value;
            const targetButtons = document.querySelectorAll('.btn-demo');
            targetButtons.forEach(btn => {
                // Skip circular, link, text, and ios buttons to keep their native look
                if (btn.classList.contains('btn-circular') || btn.classList.contains('btn-link') || btn.classList.contains('btn-text') || btn.classList.contains('btn-ios')) {
                    return;
                }
                if (shape === 'btn-smooth') {
                    btn.style.clipPath = 'url(#squircle-clip-button)';
                    btn.style.borderRadius = '8px';
                } else if (shape === 'pill') {
                    btn.style.clipPath = 'none';
                    btn.style.borderRadius = '9999px';
                } else if (shape === 'rounded') {
                    btn.style.clipPath = 'none';
                    btn.style.borderRadius = '8px';
                } else if (shape === 'square') {
                    btn.style.clipPath = 'none';
                    btn.style.borderRadius = '0px';
                }
            });
        });
    }

    // --- 8. Interactive Rating Bar Star Click / Hover ---
    const interactiveRating = document.getElementById('interactive-rating');
    const ratingVal = document.getElementById('rating-val');
    
    function updateStars(container, rating) {
        const stars = container.querySelectorAll('.star');
        stars.forEach((star, idx) => {
            const starVal = rating - idx;
            if (starVal >= 1) {
                star.classList.add('filled');
                star.classList.remove('half');
            } else if (starVal >= 0.5) {
                star.classList.remove('filled');
                star.classList.add('half');
            } else {
                star.classList.remove('filled');
                star.classList.remove('half');
            }
        });
    }

    function rebuildRatingStars() {
        const countSelect = document.getElementById('rating-star-count-select');
        const charSelect = document.getElementById('rating-char-select');
        const count = parseInt(countSelect ? countSelect.value : 5);
        const char = charSelect ? charSelect.value : '★';
        
        const ratingMaxVal = document.getElementById('rating-max-val');
        if (ratingMaxVal) {
            ratingMaxVal.textContent = `${count}.0`;
        }
        
        const interactive = document.getElementById('interactive-rating');
        if (interactive) {
            interactive.innerHTML = '';
            for (let i = 1; i <= count; i++) {
                const star = document.createElement('span');
                star.className = 'star';
                star.setAttribute('data-value', i);
                star.textContent = char;
                interactive.appendChild(star);
            }
        }
        
        const readonlyRating = document.getElementById('readonly-rating');
        if (readonlyRating) {
            readonlyRating.innerHTML = '';
            const halfVal = Math.round(count * 0.7 * 2) / 2;
            for (let i = 1; i <= count; i++) {
                const star = document.createElement('span');
                star.className = 'star';
                star.textContent = char;
                const starVal = halfVal - (i - 1);
                if (starVal >= 1) {
                    star.classList.add('filled');
                } else if (starVal >= 0.5) {
                    star.classList.add('half');
                }
                readonlyRating.appendChild(star);
            }
        }
        
        const midVal = (count / 2).toFixed(1);
        if (ratingVal) ratingVal.textContent = midVal;
        if (interactive) updateStars(interactive, parseFloat(midVal));
    }

    const starCountSelect = document.getElementById('rating-star-count-select');
    const charSelect = document.getElementById('rating-char-select');
    if (starCountSelect) starCountSelect.addEventListener('change', rebuildRatingStars);
    if (charSelect) charSelect.addEventListener('change', rebuildRatingStars);

    if (interactiveRating && ratingVal) {
        let isDragging = false;
        
        function calculateRatingFromEvent(e) {
            const rect = interactiveRating.getBoundingClientRect();
            const pointerX = e.clientX - rect.left;
            const starsCount = starCountSelect ? parseInt(starCountSelect.value) : 5;
            const starWidth = rect.width / starsCount;
            
            let val = pointerX / starWidth;
            val = Math.max(0.5, Math.min(starsCount, val));
            
            const halfToggle = document.getElementById('rating-half-toggle');
            const allowHalf = halfToggle ? halfToggle.checked : true;
            
            if (allowHalf) {
                val = Math.ceil(val * 2) / 2;
            } else {
                val = Math.ceil(val);
            }
            return val;
        }
        
        interactiveRating.addEventListener('pointerdown', (e) => {
            isDragging = true;
            try {
                interactiveRating.setPointerCapture(e.pointerId);
            } catch (err) {}
            const val = calculateRatingFromEvent(e);
            ratingVal.textContent = val.toFixed(1);
            updateStars(interactiveRating, val);
        });
        
        interactiveRating.addEventListener('pointermove', (e) => {
            if (!isDragging) return;
            const val = calculateRatingFromEvent(e);
            ratingVal.textContent = val.toFixed(1);
            updateStars(interactiveRating, val);
        });
        
        interactiveRating.addEventListener('pointerup', (e) => {
            if (isDragging) {
                try {
                    interactiveRating.releasePointerCapture(e.pointerId);
                } catch (err) {}
                isDragging = false;
            }
        });
        
        interactiveRating.addEventListener('pointercancel', (e) => {
            if (isDragging) {
                try {
                    interactiveRating.releasePointerCapture(e.pointerId);
                } catch (err) {}
                isDragging = false;
            }
        });
    }

    rebuildRatingStars(); // initial setup

    // --- 9. Smart Pull To Refresh Mock Dragging ---
    const pullRefreshBox = document.getElementById('pull-refresh-demo-box');
    const pullIndicator = document.getElementById('pull-refresh-indicator');
    const refreshFeed = document.getElementById('refresh-feed-content');
    const pullIndicatorText = pullIndicator?.querySelector('.indicator-text');
    const spinner = pullIndicator?.querySelector('.refresh-spinner');
    
    const pullResistanceSlider = document.getElementById('pull-resistance-slider');
    const pullResistanceVal = document.getElementById('pull-resistance-val');
    const pullDelaySlider = document.getElementById('pull-delay-slider');
    const pullDelayVal = document.getElementById('pull-delay-val');
    const pullOutcomeSelect = document.getElementById('pull-outcome-select');

    let pullResistance = 5;
    let pullDelay = 1500;

    if (pullResistanceSlider && pullResistanceVal) {
        pullResistanceSlider.addEventListener('input', (e) => {
            pullResistance = parseInt(e.target.value) || 5;
            pullResistanceVal.textContent = `Damping: ${pullResistance}`;
        });
    }
    if (pullDelaySlider && pullDelayVal) {
        pullDelaySlider.addEventListener('input', (e) => {
            pullDelay = parseInt(e.target.value) || 1500;
            pullDelayVal.textContent = `${(pullDelay / 1000).toFixed(1)}s`;
        });
    }

    if (pullRefreshBox && pullIndicator && refreshFeed) {
        let startY = 0;
        let diffY = 0;
        let isDragging = false;
        let isRefreshing = false;
        
        const onDragStart = (e) => {
            if (isRefreshing) return;
            startY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
            isDragging = true;
            pullIndicator.classList.add('pulling');
        };
        
        const onDragMove = (e) => {
            if (!isDragging || isRefreshing) return;
            const currentY = e.type.includes('touch') ? e.touches[0].clientY : e.clientY;
            diffY = currentY - startY;
            
            if (diffY > 0) {
                const pullDistance = Math.min(diffY * (1 / pullResistance), 70);
                refreshFeed.style.transform = `translateY(${pullDistance}px)`;
                pullIndicator.style.top = `${-48 + pullDistance}px`;
                
                if (pullDistance >= 50) {
                    if (pullIndicatorText) pullIndicatorText.textContent = 'Release to refresh';
                } else {
                    if (pullIndicatorText) pullIndicatorText.textContent = 'Pull to refresh';
                }
            }
        };
        
        const onDragEnd = () => {
            if (!isDragging || isRefreshing) return;
            isDragging = false;
            pullIndicator.classList.remove('pulling');
            
            const pullDistance = parseFloat(refreshFeed.style.transform.replace(/[^\d.]/g, '')) || 0;
            
            if (pullDistance >= 50) {
                isRefreshing = true;
                pullIndicator.classList.add('refreshing');
                if (pullIndicatorText) pullIndicatorText.textContent = 'Refreshing...';
                if (spinner) spinner.style.display = 'inline-block';
                refreshFeed.style.transform = 'translateY(40px)';
                
                setTimeout(() => {
                    isRefreshing = false;
                    pullIndicator.classList.remove('refreshing');
                    pullIndicator.style.top = '-48px';
                    refreshFeed.style.transform = 'none';
                    
                    const outcome = pullOutcomeSelect ? pullOutcomeSelect.value : 'success';
                    if (outcome === 'success') {
                        const timeString = new Date().toLocaleTimeString();
                        const newItem = document.createElement('div');
                        newItem.className = 'feed-item';
                        newItem.style.borderColor = 'var(--primary-color)';
                        newItem.textContent = `⚡ Feed updated at ${timeString}`;
                        refreshFeed.insertBefore(newItem, refreshFeed.firstChild);
                        showToast('Feed refreshed successfully');
                    } else {
                        showToast('❌ Refresh error: Connection timed out');
                    }
                }, pullDelay);
            } else {
                refreshFeed.style.transform = 'none';
                pullIndicator.style.top = '-48px';
            }
        };
        
        pullRefreshBox.addEventListener('mousedown', onDragStart);
        window.addEventListener('mousemove', onDragMove);
        window.addEventListener('mouseup', onDragEnd);
        
        pullRefreshBox.addEventListener('touchstart', onDragStart);
        window.addEventListener('touchmove', onDragMove);
        window.addEventListener('touchend', onDragEnd);
    }

    // --- 10. AsyncStateBox Mock Switcher ---
    const asyncBtns = document.querySelectorAll('.async-state-btn');
    const asyncContents = document.querySelectorAll('.async-state-content');
    const asyncSuccessInput = document.getElementById('async-success-input');
    const asyncErrorInput = document.getElementById('async-error-input');
    const asyncSuccessDesc = document.getElementById('async-success-desc');
    const asyncErrorDesc = document.getElementById('async-error-desc');
    
    asyncBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const state = btn.getAttribute('data-state');
            asyncBtns.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            
            asyncContents.forEach(c => {
                if (c.id === `state-${state}`) {
                    c.classList.add('active');
                } else {
                    c.classList.remove('active');
                }
            });
        });
    });

    if (asyncSuccessInput && asyncSuccessDesc) {
        asyncSuccessInput.addEventListener('input', (e) => {
            asyncSuccessDesc.textContent = e.target.value;
        });
    }
    if (asyncErrorInput && asyncErrorDesc) {
        asyncErrorInput.addEventListener('input', (e) => {
            asyncErrorDesc.textContent = e.target.value;
        });
    }

    // --- 11. Android Overlays (MessageDialog & BottomSheet) ---
    const triggerDialogBtn = document.getElementById('trigger-dialog-btn');
    const dialogModal = document.getElementById('dialog-modal');
    const dismissBtn = document.getElementById('dialog-dismiss-btn');
    const acceptBtn = document.getElementById('dialog-accept-btn');
    
    const triggerSheetBtn = document.getElementById('trigger-sheet-btn');
    const sheetModal = document.getElementById('sheet-modal');
    const closeSheetCross = document.getElementById('close-sheet-cross');
    const closeSheetBtn = document.getElementById('close-sheet-btn');
    
    const triggerToastBtn = document.getElementById('trigger-toast-btn');
    const bannerToastBtn = document.getElementById('banner-toast-btn');
    const toastHost = document.getElementById('toast-host');
    
    const triggerSnackbarBtn = document.getElementById('trigger-snackbar-btn');
    const snackbarHost = document.getElementById('snackbar-host');
    const snackbarUndo = document.getElementById('snackbar-undo-btn');

    const overlayDialogTitle = document.getElementById('overlay-dialog-title');
    const overlayDialogMsg = document.getElementById('overlay-dialog-msg');
    const overlaySheetCloseToggle = document.getElementById('overlay-sheet-close-toggle');

    if (triggerDialogBtn && dialogModal) {
        triggerDialogBtn.addEventListener('click', () => {
            const titleEl = dialogModal.querySelector('.dialog-card h4');
            const descEl = dialogModal.querySelector('.dialog-card p');
            if (titleEl && overlayDialogTitle) titleEl.textContent = overlayDialogTitle.value;
            if (descEl && overlayDialogMsg) descEl.textContent = overlayDialogMsg.value;
            dialogModal.classList.add('open');
        });
        
        const closeDialog = () => dialogModal.classList.remove('open');
        dismissBtn?.addEventListener('click', () => {
            closeDialog();
            showToast('Dismissed');
        });
        acceptBtn?.addEventListener('click', () => {
            closeDialog();
            showToast('Accepted');
        });
        dialogModal.addEventListener('click', (e) => {
            if (e.target === dialogModal) closeDialog();
        });
    }

    if (triggerSheetBtn && sheetModal) {
        triggerSheetBtn.addEventListener('click', () => sheetModal.classList.add('open'));
        const closeSheet = () => sheetModal.classList.remove('open');
        closeSheetCross?.addEventListener('click', closeSheet);
        closeSheetBtn?.addEventListener('click', closeSheet);
        sheetModal.addEventListener('click', (e) => {
            if (e.target === sheetModal) closeSheet();
        });
    }

    if (overlaySheetCloseToggle && closeSheetCross) {
        overlaySheetCloseToggle.addEventListener('change', (e) => {
            closeSheetCross.style.display = e.target.checked ? 'block' : 'none';
        });
    }

    // --- Overlay Dialog SVG Squircle Corner Radius & Smoothness ---
    const overlayRadiusSlider = document.getElementById('overlay-radius-slider');
    const overlaySmoothnessSlider = document.getElementById('overlay-smoothness-slider');
    const overlayRadiusVal = document.getElementById('overlay-radius-val');
    const overlaySmoothnessVal = document.getElementById('overlay-smoothness-val');
    
    function updateOverlaySquircle() {
        if (!overlayRadiusSlider || !overlaySmoothnessSlider) return;
        const radius = parseInt(overlayRadiusSlider.value);
        const smoothness = overlaySmoothnessSlider.value / 100;
        
        if (overlayRadiusVal) overlayRadiusVal.textContent = `${radius}px`;
        if (overlaySmoothnessVal) overlaySmoothnessVal.textContent = `${Math.round(smoothness * 100)}%`;
        
        const dialogPathEl = document.getElementById('squircle-path-dialog');
        if (dialogPathEl) {
            // Calculate normalized radius for squircle (height ~300px)
            const radiusNorm = radius / 300;
            const newD = getSquirclePath(radiusNorm, smoothness);
            dialogPathEl.setAttribute('d', newD);
        }
    }
    
    if (overlayRadiusSlider) overlayRadiusSlider.addEventListener('input', updateOverlaySquircle);
    if (overlaySmoothnessSlider) overlaySmoothnessSlider.addEventListener('input', updateOverlaySquircle);
    updateOverlaySquircle(); // initial run

    function showToast(message) {
        if (!toastHost) return;
        const toastElement = toastHost.querySelector('.apps-toast');
        if (toastElement) {
            toastElement.textContent = message;
            toastHost.classList.add('show');
            setTimeout(() => {
                toastHost.classList.remove('show');
            }, 2500);
        }
    }
    
    if (triggerToastBtn) {
        triggerToastBtn.addEventListener('click', () => {
            showToast('This is a native Android toast!');
        });
    }
    
    if (bannerToastBtn) {
        bannerToastBtn.addEventListener('click', () => {
            showToast('Subscription renewed!');
        });
    }

    if (triggerSnackbarBtn && snackbarHost) {
        triggerSnackbarBtn.addEventListener('click', () => {
            const textEl = snackbarHost.querySelector('.snackbar-text');
            const actionEl = snackbarHost.querySelector('.snackbar-action');
            const msgInput = document.getElementById('snackbar-msg-input');
            const actionInput = document.getElementById('snackbar-action-input');
            
            if (textEl && msgInput) textEl.textContent = msgInput.value;
            if (actionEl && actionInput) actionEl.textContent = actionInput.value;
            
            snackbarHost.classList.add('show');
            setTimeout(() => {
                snackbarHost.classList.remove('show');
            }, 4000);
        });
        
        snackbarUndo?.addEventListener('click', () => {
            snackbarHost.classList.remove('show');
            showToast('Action undone');
        });
    }

    // --- 12. AppsDivider & Spacers Interactive Sync ---
    const dividerThicknessSlider = document.getElementById('divider-thickness-slider');
    const dividerThicknessVal = document.getElementById('divider-thickness-val');
    const dividerColorSelect = document.getElementById('divider-color-select');
    const spacerSizeSlider = document.getElementById('spacer-size-slider');
    const spacerSizeVal = document.getElementById('spacer-size-val');
    
    const demoHorizontalDivider = document.getElementById('demo-horizontal-divider');
    const demoVerticalDivider = document.getElementById('demo-vertical-divider');
    const verticalSpacers = document.querySelectorAll('.divider-container .vertical-spacer');
    const horizontalSpacers = document.querySelectorAll('.divider-container .horizontal-spacer');
    
    function updateDividerThickness(val) {
        if (dividerThicknessVal) dividerThicknessVal.textContent = `${val}px`;
        if (demoHorizontalDivider) demoHorizontalDivider.style.setProperty('--divider-thickness', `${val}px`);
        if (demoVerticalDivider) demoVerticalDivider.style.setProperty('--divider-thickness', `${val}px`);
    }
    
    function updateDividerColor(color) {
        if (demoHorizontalDivider) demoHorizontalDivider.style.setProperty('--divider-color-val', color);
        if (demoVerticalDivider) demoVerticalDivider.style.setProperty('--divider-color-val', color);
    }
    
    function updateSpacerSize(val) {
        if (spacerSizeVal) spacerSizeVal.textContent = `${val}px`;
        
        verticalSpacers.forEach(s => {
            s.style.setProperty('--spacer-size-val', `${val}px`);
            s.setAttribute('data-height-label', `Vertical Spacer (${val}px)`);
        });
        
        horizontalSpacers.forEach(s => {
            s.style.setProperty('--spacer-size-val', `${val}px`);
            s.setAttribute('data-width-label', `${val}px`);
        });
    }
    
    if (dividerThicknessSlider) {
        dividerThicknessSlider.addEventListener('input', (e) => updateDividerThickness(e.target.value));
        updateDividerThickness(dividerThicknessSlider.value);
    }
    
    if (dividerColorSelect) {
        dividerColorSelect.addEventListener('change', (e) => updateDividerColor(e.target.value));
        updateDividerColor(dividerColorSelect.value);
    }
    
    if (spacerSizeSlider) {
        spacerSizeSlider.addEventListener('input', (e) => updateSpacerSize(e.target.value));
        updateSpacerSize(spacerSizeSlider.value);
    }
    
    // --- 13. AppsStatusTag Interactivity ---
    const statusTagContainer = document.getElementById('status-tag-container');
    const customTagText = document.getElementById('custom-tag-text');
    const customTagIntent = document.getElementById('custom-tag-intent');
    const addTagBtn = document.getElementById('add-tag-btn');
    const clearTagsBtn = document.getElementById('clear-tags-btn');
    const tagStyleSelect = document.getElementById('tag-style-select');
    const tagIconToggle = document.getElementById('tag-icon-toggle');
    
    function wireStatusTagClick(tag) {
        tag.addEventListener('click', () => {
            const label = tag.textContent.trim();
            const type = tag.getAttribute('data-type') || 'Custom';
            showToast(`Status tag clicked: ${label} (${type})`);
        });
    }

    function refreshTags() {
        if (!statusTagContainer) return;
        const shape = tagStyleSelect ? tagStyleSelect.value : 'tag-pill';
        const showIcon = tagIconToggle ? tagIconToggle.checked : false;
        const borderSelect = document.getElementById('tag-border-select');
        const borderFormat = borderSelect ? borderSelect.value : 'tint';
        
        const tags = statusTagContainer.querySelectorAll('.status-tag');
        tags.forEach(tag => {
            if (shape === 'tag-square') {
                tag.style.borderRadius = '6px';
            } else {
                tag.style.borderRadius = '100px';
            }
            
            if (borderFormat === 'solid') {
                tag.style.borderStyle = 'solid';
                tag.style.borderWidth = '1.5px';
                tag.style.borderColor = 'currentColor';
            } else if (borderFormat === 'dashed') {
                tag.style.borderStyle = 'dashed';
                tag.style.borderWidth = '1.5px';
                tag.style.borderColor = 'currentColor';
            } else if (borderFormat === 'none') {
                tag.style.borderStyle = 'none';
                tag.style.borderWidth = '0px';
                tag.style.borderColor = 'transparent';
            } else {
                // tint / default
                tag.style.borderStyle = '';
                tag.style.borderWidth = '';
                tag.style.borderColor = '';
            }
            
            const iconSpan = tag.querySelector('.tag-icon');
            if (iconSpan) iconSpan.remove();
            
            if (showIcon) {
                let iconChar = '•';
                if (tag.classList.contains('tag-success')) iconChar = '✓';
                else if (tag.classList.contains('tag-warning')) iconChar = '⚠️';
                else if (tag.classList.contains('tag-error')) iconChar = '🚨';
                else if (tag.classList.contains('tag-info')) iconChar = 'ℹ️';
                else if (tag.classList.contains('tag-primary')) iconChar = '⚡';
                
                const iconEl = document.createElement('span');
                iconEl.className = 'tag-icon';
                iconEl.style.marginRight = '4px';
                iconEl.textContent = iconChar;
                tag.insertBefore(iconEl, tag.firstChild);
            }
        });
    }
    
    if (statusTagContainer) {
        const initialTags = statusTagContainer.querySelectorAll('.status-tag');
        initialTags.forEach(tag => wireStatusTagClick(tag));
    }
    
    if (addTagBtn && statusTagContainer && customTagText && customTagIntent) {
        addTagBtn.addEventListener('click', () => {
            const textVal = customTagText.value.trim();
            if (!textVal) return;
            
            const intentVal = customTagIntent.value;
            const intentText = customTagIntent.options[customTagIntent.selectedIndex].text;
            
            const newTag = document.createElement('span');
            newTag.className = `status-tag ${intentVal}`;
            newTag.setAttribute('data-type', intentText);
            newTag.textContent = textVal;
            
            statusTagContainer.appendChild(newTag);
            wireStatusTagClick(newTag);
            refreshTags();
            showToast(`Created custom tag: "${textVal}"`);
        });
    }
    
    if (clearTagsBtn && statusTagContainer) {
        clearTagsBtn.addEventListener('click', () => {
            const tags = statusTagContainer.querySelectorAll('.status-tag');
            tags.forEach((tag, idx) => {
                if (idx >= 6) {
                    tag.remove();
                }
            });
            refreshTags();
            showToast('Reset custom status tags');
        });
    }

    if (tagStyleSelect) tagStyleSelect.addEventListener('change', refreshTags);
    if (tagIconToggle) tagIconToggle.addEventListener('change', refreshTags);
    const tagBorderSelect = document.getElementById('tag-border-select');
    if (tagBorderSelect) tagBorderSelect.addEventListener('change', refreshTags);
    refreshTags();

    // --- 14. AppsBadge & AppsTooltip Interactivity ---
    const demoBadgePill = document.getElementById('demo-badge-pill');
    const badgeCountInput = document.getElementById('badge-count-input');
    const badgeDecBtn = document.getElementById('badge-dec-btn');
    const badgeIncBtn = document.getElementById('badge-inc-btn');
    
    const demoTooltipTrigger = document.getElementById('demo-tooltip-trigger');
    const demoTooltipBubble = document.getElementById('demo-tooltip-bubble');
    const tooltipPositionSelect = document.getElementById('tooltip-position-select');
    const tooltipTextInput = document.getElementById('tooltip-text-input');
    const tooltipDelaySlider = document.getElementById('tooltip-delay-slider');
    const tooltipDelayVal = document.getElementById('tooltip-delay-val');
    const badgeAlignSelect = document.getElementById('badge-align-select');
    
    let tooltipDelay = 500;
    
    function updateBadgeCount(count) {
        if (!demoBadgePill) return;
        count = Math.max(0, parseInt(count) || 0);
        if (badgeCountInput) badgeCountInput.value = count;
        
        demoBadgePill.textContent = count;
        if (count === 0) {
            demoBadgePill.classList.add('hidden');
        } else {
            demoBadgePill.classList.remove('hidden');
        }
    }
    
    if (badgeCountInput) {
        badgeCountInput.addEventListener('input', (e) => updateBadgeCount(e.target.value));
    }
    
    if (badgeDecBtn) {
        badgeDecBtn.addEventListener('click', () => {
            const current = parseInt(badgeCountInput.value) || 0;
            updateBadgeCount(current - 1);
        });
    }
    
    if (badgeIncBtn) {
        badgeIncBtn.addEventListener('click', () => {
            const current = parseInt(badgeCountInput.value) || 0;
            updateBadgeCount(current + 1);
        });
    }

    if (badgeAlignSelect && demoBadgePill) {
        badgeAlignSelect.addEventListener('change', (e) => {
            const align = e.target.value;
            demoBadgePill.style.top = 'auto';
            demoBadgePill.style.bottom = 'auto';
            demoBadgePill.style.left = 'auto';
            demoBadgePill.style.right = 'auto';
            
            if (align === 'top-right') {
                demoBadgePill.style.top = '-8px';
                demoBadgePill.style.right = '-8px';
            } else if (align === 'top-left') {
                demoBadgePill.style.top = '-8px';
                demoBadgePill.style.left = '-8px';
            } else if (align === 'bottom-right') {
                demoBadgePill.style.bottom = '-8px';
                demoBadgePill.style.right = '-8px';
            } else if (align === 'bottom-left') {
                demoBadgePill.style.bottom = '-8px';
                demoBadgePill.style.left = '-8px';
            }
        });
    }
    
    function updateTooltipText(text) {
        if (demoTooltipBubble) demoTooltipBubble.textContent = text;
    }
    
    function updateTooltipPosition(posClass) {
        if (!demoTooltipTrigger) return;
        demoTooltipTrigger.classList.remove('tooltip-top', 'tooltip-bottom', 'tooltip-left', 'tooltip-right');
        demoTooltipTrigger.classList.add(posClass);
    }
    
    if (tooltipTextInput) {
        tooltipTextInput.addEventListener('input', (e) => updateTooltipText(e.target.value));
        updateTooltipText(tooltipTextInput.value);
    }
    
    if (tooltipPositionSelect) {
        tooltipPositionSelect.addEventListener('change', (e) => updateTooltipPosition(e.target.value));
        updateTooltipPosition(tooltipPositionSelect.value);
    }

    if (tooltipDelaySlider && tooltipDelayVal) {
        tooltipDelaySlider.addEventListener('input', (e) => {
            tooltipDelay = parseInt(e.target.value) || 500;
            tooltipDelayVal.textContent = `${tooltipDelay}ms`;
        });
    }
    
    if (demoTooltipTrigger) {
        let pressTimer;
        demoTooltipTrigger.addEventListener('mousedown', () => {
            pressTimer = window.setTimeout(() => {
                demoTooltipTrigger.classList.add('active');
            }, tooltipDelay);
        });
        demoTooltipTrigger.addEventListener('mouseup', () => {
            clearTimeout(pressTimer);
            window.setTimeout(() => {
                demoTooltipTrigger.classList.remove('active');
            }, 2000);
        });
        demoTooltipTrigger.addEventListener('touchstart', () => {
            pressTimer = window.setTimeout(() => {
                demoTooltipTrigger.classList.add('active');
            }, tooltipDelay);
        });
        demoTooltipTrigger.addEventListener('touchend', () => {
            clearTimeout(pressTimer);
            window.setTimeout(() => {
                demoTooltipTrigger.classList.remove('active');
            }, 2000);
        });
    }

    // --- 15. Dropdowns Setup ---
    function setupDropdown(triggerId, menuId, onSelected) {
        const trigger = document.getElementById(triggerId);
        const menu = document.getElementById(menuId);
        if (!trigger || !menu) return;
        
        trigger.addEventListener('click', (e) => {
            if (trigger.disabled) return;
            e.stopPropagation();
            menu.classList.toggle('open');
        });
        
        const items = menu.querySelectorAll('.dropdown-item');
        items.forEach(item => {
            item.addEventListener('click', (e) => {
                const val = item.getAttribute('data-val');
                if (onSelected) onSelected(val, trigger);
                menu.classList.remove('open');
            });
        });
        
        document.addEventListener('click', () => {
            menu.classList.remove('open');
        });
    }

    function updateDropdownItems(menuId, itemsList, onSelected) {
        const menu = document.getElementById(menuId);
        if (!menu) return;
        menu.innerHTML = '';
        itemsList.forEach(itemText => {
            const item = document.createElement('div');
            item.className = 'dropdown-item';
            item.setAttribute('data-val', itemText.trim());
            item.textContent = itemText.trim();
            menu.appendChild(item);
        });
        
        const items = menu.querySelectorAll('.dropdown-item');
        items.forEach(item => {
            item.addEventListener('click', (e) => {
                e.stopPropagation();
                const val = item.getAttribute('data-val');
                let trigger = null;
                if (menuId === 'web-text-dropdown-menu') trigger = document.getElementById('web-text-dropdown');
                else if (menuId === 'web-floating-dropdown-menu') {
                    const wrapper = document.getElementById('web-floating-dropdown');
                    const input = wrapper?.querySelector('.floating-input');
                    if (input) {
                        input.value = val;
                        wrapper.classList.remove('focused');
                        wrapper.classList.add('has-value');
                        showToast(`Selected State: ${val}`);
                    }
                }
                
                if (onSelected && trigger) onSelected(val, trigger);
                menu.classList.remove('open');
            });
        });
    }

    setupDropdown('web-text-dropdown', 'web-text-dropdown-menu', (val, trigger) => {
        trigger.querySelector('.dropdown-val').textContent = val;
        showToast(`Selected State: ${val}`);
    });

    setupDropdown('web-icon-dropdown', 'web-icon-dropdown-menu', (val) => {
        showToast(`Icon Dropdown: ${val}`);
    });

    const floatingDropdown = document.getElementById('web-floating-dropdown');
    if (floatingDropdown) {
        const input = floatingDropdown.querySelector('.floating-input');
        const menu = document.getElementById('web-floating-dropdown-menu');
        const wrapper = floatingDropdown.querySelector('.floating-input-wrapper');
        
        wrapper.addEventListener('click', (e) => {
            if (floatingDropdown.classList.contains('disabled')) return;
            e.stopPropagation();
            menu.classList.toggle('open');
            floatingDropdown.classList.add('focused');
        });
        
        const items = menu.querySelectorAll('.dropdown-item');
        items.forEach(item => {
            item.addEventListener('click', (e) => {
                const val = item.getAttribute('data-val');
                input.value = val;
                floatingDropdown.classList.remove('focused');
                floatingDropdown.classList.add('has-value');
                menu.classList.remove('open');
                showToast(`Selected State: ${val}`);
            });
        });
        
        document.addEventListener('click', () => {
            menu.classList.remove('open');
            floatingDropdown.classList.remove('focused');
        });
    }

    const dropdownsDisableToggle = document.getElementById('dropdowns-disable-toggle');
    const dropdownsItemsInput = document.getElementById('dropdowns-items-input');
    
    if (dropdownsDisableToggle) {
        dropdownsDisableToggle.addEventListener('change', (e) => {
            const disabled = e.target.checked;
            const triggers = ['web-text-dropdown', 'web-icon-dropdown', 'web-chip-dropdown'];
            triggers.forEach(id => {
                const el = document.getElementById(id);
                if (el) el.disabled = disabled;
            });
            const floating = document.getElementById('web-floating-dropdown');
            if (floating) {
                const input = floating.querySelector('.floating-input');
                if (input) input.disabled = disabled;
                if (disabled) floating.classList.add('disabled');
                else floating.classList.remove('disabled');
            }
        });
    }
    
    if (dropdownsItemsInput) {
        dropdownsItemsInput.addEventListener('input', (e) => {
            const items = e.target.value.split(',').map(s => s.trim()).filter(s => s.length > 0);
            if (items.length === 0) return;
            
            updateDropdownItems('web-text-dropdown-menu', items, (val, trigger) => {
                trigger.querySelector('.dropdown-val').textContent = val;
                showToast(`Selected State: ${val}`);
            });
            updateDropdownItems('web-floating-dropdown-menu', items);
        });
    }

    // --- 16. Text Helpers Setup ---
    const copyableText = document.getElementById('web-copyable-text');
    if (copyableText) {
        copyableText.addEventListener('click', () => {
            const text = copyableText.getAttribute('data-text');
            navigator.clipboard.writeText(text).then(() => {
                showToast("Copied to clipboard!");
                copyableText.classList.add('copied');
                setTimeout(() => {
                    copyableText.classList.remove('copied');
                }, 1500);
            });
        });
    }

    const expandableText = document.getElementById('web-expandable-text');
    const expandableToggle = document.getElementById('web-expandable-toggle');
    const expandableBody = document.getElementById('web-expandable-body');
    const expandableTextLines = document.getElementById('expandable-text-lines');
    const copyableTextInput = document.getElementById('copyable-text-input');
    const imageTextGap = document.getElementById('image-text-gap');
    const imageTextGapVal = document.getElementById('image-text-gap-val');
    const webImageTextContainer = document.getElementById('web-image-text-container');
    
    let lineLimit = 2;
    if (expandableTextLines) {
        lineLimit = parseInt(expandableTextLines.value) || 2;
        expandableTextLines.addEventListener('change', (e) => {
            lineLimit = parseInt(e.target.value) || 2;
            if (expandableBody && expandableBody.classList.contains('collapsed')) {
                expandableBody.style.webkitLineClamp = lineLimit;
            }
        });
    }

    if (expandableText && expandableToggle && expandableBody) {
        expandableToggle.addEventListener('click', () => {
            const isCollapsed = expandableBody.classList.toggle('collapsed');
            expandableToggle.textContent = isCollapsed ? 'Show more' : 'Show less';
            if (isCollapsed) {
                expandableBody.style.webkitLineClamp = lineLimit;
            } else {
                expandableBody.style.webkitLineClamp = 'unset';
            }
        });
    }

    if (copyableTextInput && copyableText) {
        copyableTextInput.addEventListener('input', (e) => {
            const val = e.target.value;
            copyableText.setAttribute('data-text', val);
            const textContentSpan = copyableText.querySelector('.text-content');
            if (textContentSpan) textContentSpan.textContent = val;
        });
    }

    if (imageTextGap && imageTextGapVal && webImageTextContainer) {
        imageTextGap.addEventListener('input', (e) => {
            const val = e.target.value;
            imageTextGapVal.textContent = `${val}px`;
            webImageTextContainer.style.gap = `${val}px`;
        });
    }

    // --- 17. Chip Dropdown Setup ---
    setupDropdown('web-chip-dropdown', 'web-chip-dropdown-menu', (val, trigger) => {
        trigger.querySelector('.dropdown-val').textContent = val;
        showToast(`Applied Filter: ${val}`);
    });

    // --- 18. Scaffold Sidebar Toggle ---
    const toggleDrawerBtn = document.getElementById('toggle-preview-drawer');
    const previewSidebar = document.getElementById('preview-sidebar');
    if (toggleDrawerBtn && previewSidebar) {
        toggleDrawerBtn.addEventListener('click', () => {
            const currentWidth = previewSidebar.style.width;
            if (currentWidth === '0px') {
                previewSidebar.style.width = '140px';
                previewSidebar.style.padding = '';
            } else {
                previewSidebar.style.width = '0px';
                previewSidebar.style.padding = '0';
            }
        });
    }

    // --- 19. Password Field Visibility Toggle ---
    const passwordField = document.getElementById('web-password-field');
    const passwordToggle = document.getElementById('web-password-toggle');
    if (passwordField && passwordToggle) {
        passwordToggle.addEventListener('click', () => {
            const isPassword = passwordField.type === 'password';
            passwordField.type = isPassword ? 'text' : 'password';
            passwordToggle.textContent = isPassword ? '🙈' : '👁️';
        });
    }

    // --- 20. Search Input Clear Action ---
    const searchField = document.getElementById('web-search-field');
    const searchClear = document.getElementById('web-search-clear');
    if (searchField && searchClear) {
        searchField.addEventListener('input', () => {
            if (searchField.value.length > 0) {
                searchClear.classList.remove('hidden');
            } else {
                searchClear.classList.add('hidden');
            }
        });
        searchClear.addEventListener('click', () => {
            searchField.value = '';
            searchClear.classList.add('hidden');
            searchField.focus();
            showToast('Search query cleared');
        });
    }

    // --- 21. Email Validation Form Input ---
    const validatedField = document.getElementById('web-validated-field');
    const validatedFeedback = document.getElementById('web-validated-feedback');
    const fieldPlaceholderInput = document.getElementById('field-placeholder-input');
    const webStandardField = document.getElementById('web-standard-field');
    const webFloatingField = document.getElementById('web-floating-field');
    const fieldStateSelect = document.getElementById('field-state-select');
    
    if (fieldPlaceholderInput) {
        fieldPlaceholderInput.addEventListener('input', (e) => {
            const val = e.target.value;
            if (webStandardField) webStandardField.placeholder = val;
            if (webFloatingField) {
                const label = webFloatingField.nextElementSibling;
                if (label && label.classList.contains('floating-label')) {
                    label.textContent = val;
                }
            }
        });
    }
    
    if (validatedField && validatedFeedback) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        
        function runValidation() {
            const state = fieldStateSelect ? fieldStateSelect.value : 'none';
            if (state === 'valid') {
                validatedField.classList.remove('invalid');
                validatedField.classList.add('valid');
                validatedFeedback.textContent = '✓ Forced validation success';
                validatedFeedback.className = 'validation-feedback valid-feedback';
            } else if (state === 'invalid') {
                validatedField.classList.remove('valid');
                validatedField.classList.add('invalid');
                validatedFeedback.textContent = '✗ Forced validation error';
                validatedFeedback.className = 'validation-feedback invalid-feedback';
            } else {
                const val = validatedField.value.trim();
                if (val.length === 0) {
                    validatedField.classList.remove('valid', 'invalid');
                    validatedFeedback.textContent = '';
                    validatedFeedback.className = 'validation-feedback';
                } else if (emailRegex.test(val)) {
                    validatedField.classList.remove('invalid');
                    validatedField.classList.add('valid');
                    validatedFeedback.textContent = '✓ Valid email address';
                    validatedFeedback.className = 'validation-feedback valid-feedback';
                } else {
                    validatedField.classList.remove('valid');
                    validatedField.classList.add('invalid');
                    validatedFeedback.textContent = '✗ Invalid email format';
                    validatedFeedback.className = 'validation-feedback invalid-feedback';
                }
            }
        }
        
        validatedField.addEventListener('input', runValidation);
        if (fieldStateSelect) {
            fieldStateSelect.addEventListener('change', runValidation);
        }
    }

    const fieldShapeSelect = document.getElementById('field-shape-select');
    const fieldBorderSelect = document.getElementById('field-border-select');
    
    function updateFieldShapes() {
        if (!fieldShapeSelect) return;
        const shape = fieldShapeSelect.value;
        const container = fieldPlaceholderInput?.closest('.playground-container')?.querySelector('.demo-card-front');
        if (!container) return;
        
        const inputs = container.querySelectorAll('.themed-input, .floating-input-wrapper');
        inputs.forEach(input => {
            if (shape === 'square') {
                input.style.borderRadius = '0px';
            } else if (shape === 'pill') {
                input.style.borderRadius = '24px';
            } else {
                input.style.borderRadius = ''; // Default
            }
        });
    }

    function updateFieldBorderColors() {
        if (!fieldBorderSelect) return;
        const val = fieldBorderSelect.value;
        const container = fieldPlaceholderInput?.closest('.playground-container')?.querySelector('.demo-card-front');
        if (!container) return;
        
        const isLight = document.body.classList.contains('light-mode');
        
        if (val === 'purple') {
            container.style.setProperty('--primary-color', isLight ? '#7c3aed' : '#a78bfa');
            container.style.setProperty('--primary-glow', isLight ? 'rgba(124, 58, 237, 0.2)' : 'rgba(167, 139, 250, 0.35)');
        } else if (val === 'rose') {
            container.style.setProperty('--primary-color', isLight ? '#e11d48' : '#fb7185');
            container.style.setProperty('--primary-glow', isLight ? 'rgba(225, 29, 72, 0.2)' : 'rgba(251, 113, 133, 0.35)');
        } else if (val === 'green') {
            container.style.setProperty('--primary-color', isLight ? '#16a34a' : '#4ade80');
            container.style.setProperty('--primary-glow', isLight ? 'rgba(22, 163, 74, 0.2)' : 'rgba(74, 222, 128, 0.35)');
        } else if (val === 'blue') {
            container.style.setProperty('--primary-color', isLight ? '#1a73e8' : '#8ab4f8');
            container.style.setProperty('--primary-glow', isLight ? 'rgba(26, 115, 232, 0.15)' : 'rgba(138, 180, 248, 0.25)');
        } else {
            // Default
            container.style.removeProperty('--primary-color');
            container.style.removeProperty('--primary-glow');
        }
    }
    
    if (fieldShapeSelect) fieldShapeSelect.addEventListener('change', updateFieldShapes);
    if (fieldBorderSelect) fieldBorderSelect.addEventListener('change', updateFieldBorderColors);

    // --- 22. Swipeable Actions Box Gestures ---
    const swipeItems = document.querySelectorAll('.swipe-item-wrapper');
    const swipeLeftLabelInput = document.getElementById('swipe-left-label');
    const swipeLeftIconSelect = document.getElementById('swipe-left-icon');
    const swipeRightLabelInput = document.getElementById('swipe-right-label');
    const swipeRightIconSelect = document.getElementById('swipe-right-icon');
    const swipeThresholdSlider = document.getElementById('swipe-threshold-slider');
    const swipeThresholdVal = document.getElementById('swipe-threshold-val');

    let swipeThreshold = 70;

    function updateSwipeActionContents() {
        const leftLabel = swipeLeftLabelInput?.value || 'Archive';
        const leftIcon = swipeLeftIconSelect?.value || '📥';
        const rightLabel = swipeRightLabelInput?.value || 'Delete';
        const rightIcon = swipeRightIconSelect?.value || '🗑️';
        
        document.querySelectorAll('.swipe-left-content').forEach(el => {
            el.textContent = `${leftIcon} ${leftLabel}`;
        });
        document.querySelectorAll('.swipe-right-content').forEach(el => {
            el.textContent = `${rightIcon} ${rightLabel}`;
        });
    }

    if (swipeLeftLabelInput) swipeLeftLabelInput.addEventListener('input', updateSwipeActionContents);
    if (swipeLeftIconSelect) swipeLeftIconSelect.addEventListener('change', updateSwipeActionContents);
    if (swipeRightLabelInput) swipeRightLabelInput.addEventListener('input', updateSwipeActionContents);
    if (swipeRightIconSelect) swipeRightIconSelect.addEventListener('change', updateSwipeActionContents);

    if (swipeThresholdSlider && swipeThresholdVal) {
        swipeThresholdSlider.addEventListener('input', (e) => {
            swipeThreshold = parseInt(e.target.value) || 70;
            swipeThresholdVal.textContent = `${swipeThreshold}px`;
        });
    }

    swipeItems.forEach(wrapper => {
        const foreground = wrapper.querySelector('.swipe-foreground');
        const leftBg = wrapper.querySelector('.swipe-left-bg');
        const rightBg = wrapper.querySelector('.swipe-right-bg');
        if (!foreground) return;

        let startX = 0;
        let isSwiping = false;
        const maxOffset = 100;

        const onStart = (e) => {
            startX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX;
            isSwiping = true;
            foreground.style.transition = 'none';
            if (leftBg) leftBg.style.transition = 'none';
            if (rightBg) rightBg.style.transition = 'none';
        };

        const onMove = (e) => {
            if (!isSwiping) return;
            const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX;
            let diffX = clientX - startX;

            if (Math.abs(diffX) > maxOffset) {
                const sign = Math.sign(diffX);
                diffX = sign * (maxOffset + Math.log(1 + Math.abs(diffX) - maxOffset) * 5);
            }

            foreground.style.transform = `translateX(${diffX}px)`;

            if (diffX > 0) {
                if (leftBg) {
                    leftBg.style.opacity = Math.min(diffX / swipeThreshold, 1);
                }
                if (rightBg) {
                    rightBg.style.opacity = '0';
                }
            } else {
                if (rightBg) {
                    rightBg.style.opacity = Math.min(Math.abs(diffX) / swipeThreshold, 1);
                }
                if (leftBg) {
                    leftBg.style.opacity = '0';
                }
            }
        };

        const onEnd = () => {
            if (!isSwiping) return;
            isSwiping = false;
            
            const transformMatrix = window.getComputedStyle(foreground).transform;
            let currentOffset = 0;
            if (transformMatrix && transformMatrix !== 'none') {
                const values = transformMatrix.split('(')[1].split(')')[0].split(',');
                currentOffset = parseFloat(values[4]);
            }

            foreground.style.transition = 'transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1)';
            if (leftBg) leftBg.style.transition = 'opacity 0.3s';
            if (rightBg) rightBg.style.transition = 'opacity 0.3s';

            const leftLabel = swipeLeftLabelInput?.value || 'Archive';
            const rightLabel = swipeRightLabelInput?.value || 'Delete';

            if (currentOffset > swipeThreshold) {
                showToast(`Item ${leftLabel}d!`);
                if (leftBg) leftBg.style.opacity = '1';
            } else if (currentOffset < -swipeThreshold) {
                showToast(`Item ${rightLabel}d!`);
                if (rightBg) rightBg.style.opacity = '1';
            }

            foreground.style.transform = 'translateX(0px)';
            setTimeout(() => {
                if (leftBg) leftBg.style.opacity = '0';
                if (rightBg) rightBg.style.opacity = '0';
            }, 300);
        };

        foreground.addEventListener('mousedown', onStart);
        foreground.addEventListener('touchstart', onStart, { passive: true });

        window.addEventListener('mousemove', onMove);
        window.addEventListener('touchmove', onMove, { passive: true });

        window.addEventListener('mouseup', onEnd);
        window.addEventListener('touchend', onEnd);
    });

    updateSwipeActionContents(); // initial setup

    // --- 23. Highlight Text Search (AppsHighlightText) ---
    const highlightSearchInput = document.getElementById('highlight-search-input');
    const highlightSourceText = document.getElementById('highlight-source-text');
    const highlightCaseToggle = document.getElementById('highlight-case-toggle');
    const highlightStyleSelect = document.getElementById('highlight-style-select');

    if (highlightSearchInput && highlightSourceText) {
        const cleanOriginalText = highlightSourceText.textContent.trim();
        
        function performHighlight() {
            const query = highlightSearchInput.value;
            if (!query) {
                highlightSourceText.textContent = cleanOriginalText;
                return;
            }
            
            const caseSensitive = highlightCaseToggle ? highlightCaseToggle.checked : false;
            const styleVal = highlightStyleSelect ? highlightStyleSelect.value : 'yellow';
            
            let inlineStyle = 'background-color: rgba(255, 235, 59, 0.4); color: inherit; padding: 2px 4px; border-radius: 4px;';
            if (styleVal === 'green') {
                inlineStyle = 'background-color: rgba(16, 185, 129, 0.2); color: #10b981; padding: 2px 4px; border-radius: 4px; box-shadow: 0 0 8px rgba(16, 185, 129, 0.3); font-weight: 600;';
            } else if (styleVal === 'orange-underline') {
                inlineStyle = 'background-color: transparent; text-decoration: underline; text-decoration-color: #f59e0b; text-decoration-thickness: 2px; color: inherit; font-weight: 600;';
            } else if (styleVal === 'red-strike') {
                inlineStyle = 'background-color: transparent; text-decoration: line-through; text-decoration-color: #ef4444; text-decoration-thickness: 2px; color: inherit; opacity: 0.8;';
            }
            
            const escapedQuery = query.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
            const flags = caseSensitive ? 'g' : 'gi';
            const regex = new RegExp(`(${escapedQuery})`, flags);
            
            const highlightedText = cleanOriginalText.replace(regex, `<span style="${inlineStyle}">$1</span>`);
            highlightSourceText.innerHTML = highlightedText;
        }
        
        highlightSearchInput.addEventListener('input', performHighlight);
        if (highlightCaseToggle) highlightCaseToggle.addEventListener('change', performHighlight);
        if (highlightStyleSelect) highlightStyleSelect.addEventListener('change', performHighlight);
    }

    // --- 24. In-App Updates Dialog Simulation (update-utils) ---
    const btnFlexibleUpdate = document.getElementById('btn-trigger-update-flexible');
    const btnImmediateUpdate = document.getElementById('btn-trigger-update-immediate');
    const updateModal = document.getElementById('update-modal');
    const updateTitle = document.getElementById('update-title');
    const updateDesc = document.getElementById('update-desc');
    const updateCancelBtn = document.getElementById('update-cancel-btn');
    const updateActionBtn = document.getElementById('update-action-btn');
    
    const updateVersionInput = document.getElementById('update-version-input');
    const updateFailToggle = document.getElementById('update-fail-toggle');

    if (updateModal && updateCancelBtn && updateActionBtn) {
        let activeUpdateType = 'flexible';
        
        const openUpdateModal = (type) => {
            activeUpdateType = type;
            updateModal.classList.add('open');
            const version = updateVersionInput ? updateVersionInput.value.trim() : '2.4.0';
            
            if (type === 'flexible') {
                updateTitle.textContent = `Update Available (v${version})`;
                updateDesc.textContent = 'A new version of the app is available. You can continue using the app while the update downloads in the background.';
                updateCancelBtn.style.display = 'block';
                updateCancelBtn.textContent = 'Later';
                updateActionBtn.textContent = 'Download';
            } else {
                updateTitle.textContent = `Critical Update Required (v${version})`;
                updateDesc.textContent = 'An immediate update is required to continue using the app. Please update now to access system features.';
                updateCancelBtn.style.display = 'none';
                updateActionBtn.textContent = 'Update Now';
            }
        };
        
        btnFlexibleUpdate?.addEventListener('click', () => openUpdateModal('flexible'));
        btnImmediateUpdate?.addEventListener('click', () => openUpdateModal('immediate'));
        
        updateCancelBtn.addEventListener('click', () => {
            updateModal.classList.remove('open');
            showToast('Update postponed');
        });
        
        updateActionBtn.addEventListener('click', () => {
            updateModal.classList.remove('open');
            const isFailure = updateFailToggle ? updateFailToggle.checked : false;
            
            if (isFailure) {
                showToast('❌ Update installation failed due to simulation error');
                return;
            }
            
            if (activeUpdateType === 'flexible') {
                showToast('Downloading update in background...');
                
                setTimeout(() => {
                    const snackbar = document.getElementById('snackbar-host');
                    if (snackbar) {
                        const snackbarText = snackbar.querySelector('.snackbar-text');
                        const snackbarAction = snackbar.querySelector('.snackbar-action');
                        if (snackbarText && snackbarAction) {
                            snackbarText.textContent = 'Update downloaded and ready to install.';
                            snackbarAction.textContent = 'Restart';
                            
                            snackbarAction.onclick = () => {
                                showToast('Restarting app & installing update...');
                                snackbar.classList.remove('show');
                            };
                            
                            snackbar.classList.add('show');
                            setTimeout(() => {
                                snackbar.classList.remove('show');
                            }, 6000);
                        }
                    }
                }, 3000);
            } else {
                showToast('Installing critical update...');
                const installOverlay = document.createElement('div');
                installOverlay.className = 'modal-overlay open';
                installOverlay.style.zIndex = '9999';
                installOverlay.innerHTML = `
                    <div class="dialog-card" style="display: flex; flex-direction: column; align-items: center; gap: 16px; text-align: center;">
                        <div class="apps-circular-progress" style="width: 48px; height: 48px; border-width: 4px;"></div>
                        <h4 style="font-size: 18px; font-weight: 600;">Installing Update</h4>
                        <p style="font-size: 13px; color: var(--text-muted);">Please do not close the app. Optimizing packages...</p>
                    </div>
                `;
                document.body.appendChild(installOverlay);
                
                setTimeout(() => {
                    installOverlay.remove();
                    showToast('System updated successfully!');
                }, 3500);
            }
        });
    }

    // --- 25. Color Scheme Dropdown Setup ---
    const schemeDropdown = document.getElementById('color-scheme-dropdown');
    const schemeMenu = document.getElementById('color-scheme-menu');
    
    if (schemeDropdown && schemeMenu) {
        schemeDropdown.addEventListener('click', (e) => {
            e.stopPropagation();
            schemeMenu.classList.toggle('open');
        });
        
        const schemeItems = schemeMenu.querySelectorAll('.dropdown-item');
        schemeItems.forEach(item => {
            item.addEventListener('click', () => {
                const scheme = item.getAttribute('data-scheme');
                const label = item.getAttribute('data-label');
                const activeName = document.getElementById('active-scheme-name');
                const activeDot = document.getElementById('active-scheme-dot');
                
                if (activeName) activeName.textContent = label;
                document.body.classList.remove('theme-blue', 'theme-green', 'theme-purple', 'theme-orange', 'theme-rose', 'theme-graphite');
                document.body.classList.add(`theme-${scheme}`);
                
                if (activeDot) {
                    const dotColors = {
                        blue: '#1a73e8',
                        green: '#16a34a',
                        purple: '#7c3aed',
                        orange: '#ea580c',
                        rose: '#e11d48',
                        graphite: '#52525b'
                    };
                    activeDot.style.backgroundColor = dotColors[scheme] || '#1a73e8';
                }
                
                schemeMenu.classList.remove('open');
                showToast(`Switched color scheme to ${label}`);
            });
        });
        
        document.addEventListener('click', () => {
            schemeMenu.classList.remove('open');
        });
    }

    // --- 26. Shimmer Loading Toggle & Speed ---
    const shimmerToggle = document.getElementById('shimmer-toggle');
    const shimmerView = document.getElementById('shimmer-view');
    const actualContentView = document.getElementById('actual-content-view');
    
    if (shimmerToggle && shimmerView && actualContentView) {
        shimmerToggle.addEventListener('change', () => {
            if (shimmerToggle.checked) {
                shimmerView.style.display = 'block';
                actualContentView.style.display = 'none';
            } else {
                shimmerView.style.display = 'none';
                actualContentView.style.display = 'block';
            }
        });
    }

    const shimmerDurationSlider = document.getElementById('shimmer-duration-slider');
    const shimmerDurationVal = document.getElementById('shimmer-duration-val');
    if (shimmerDurationSlider && shimmerDurationVal) {
        shimmerDurationSlider.addEventListener('input', (e) => {
            const val = e.target.value;
            shimmerDurationVal.textContent = `${val}s`;
            const animElements = document.querySelectorAll('.shimmer-anim');
            animElements.forEach(el => {
                el.style.animationDuration = `${val}s`;
            });
        });
    }

    // --- 27. Images & Avatars Settings ---
    const avatarInitialsInput = document.getElementById('avatar-initials-input');
    const avatarPreview = document.getElementById('web-avatar-preview');
    const avatarColorSelect = document.getElementById('avatar-color-select');
    const circleIconBox = document.getElementById('web-circle-icon-box');
    const avatarIcon = document.getElementById('web-avatar-icon');
    const avatarIconSize = document.getElementById('avatar-icon-size');
    const avatarIconSizeVal = document.getElementById('avatar-icon-size-val');

    if (avatarInitialsInput && avatarPreview) {
        avatarInitialsInput.addEventListener('input', (e) => {
            avatarPreview.textContent = e.target.value.toUpperCase();
        });
    }

    if (avatarColorSelect) {
        avatarColorSelect.addEventListener('change', (e) => {
            const val = e.target.value;
            const mapping = {
                'var(--primary-color)': { text: 'var(--primary-color)', bg: 'rgba(93, 95, 239, 0.15)' },
                '#10b981': { text: '#10b981', bg: 'rgba(16, 185, 129, 0.15)' },
                '#3b82f6': { text: '#3b82f6', bg: 'rgba(59, 130, 246, 0.15)' },
                '#f59e0b': { text: '#f59e0b', bg: 'rgba(245, 158, 11, 0.15)' },
                '#ef4444': { text: '#ef4444', bg: 'rgba(239, 68, 68, 0.15)' }
            };
            const map = mapping[val] || mapping['var(--primary-color)'];
            
            if (avatarPreview) {
                avatarPreview.style.backgroundColor = val === 'var(--primary-color)' ? 'var(--primary-color)' : val;
            }
            if (circleIconBox) {
                circleIconBox.style.color = map.text;
                circleIconBox.style.backgroundColor = map.bg;
            }
            if (avatarIcon) {
                avatarIcon.style.color = val;
            }
        });
    }

    if (avatarIconSize && circleIconBox && avatarIconSizeVal) {
        avatarIconSize.addEventListener('input', (e) => {
            const val = e.target.value;
            avatarIconSizeVal.textContent = `${val}px`;
            circleIconBox.style.width = `${val}px`;
            circleIconBox.style.height = `${val}px`;
            const iconEmoji = circleIconBox.querySelector('span');
            if (iconEmoji) {
                iconEmoji.style.fontSize = `${val * 0.45}px`;
            }
        });
    }

    // --- 28. Banners, Spacers & Empty Settings ---
    const bannerTitleInput = document.getElementById('banner-title-input');
    const bannerMsgInput = document.getElementById('banner-msg-input');
    const bannerIntentSelect = document.getElementById('banner-intent-select');
    const demoBanner = document.getElementById('demo-banner');
    const demoBannerIcon = document.getElementById('demo-banner-icon');
    const demoBannerTitle = document.getElementById('demo-banner-title');
    const demoBannerDesc = document.getElementById('demo-banner-desc');

    if (bannerTitleInput && demoBannerTitle) {
        bannerTitleInput.addEventListener('input', (e) => {
            demoBannerTitle.textContent = e.target.value;
        });
    }
    if (bannerMsgInput && demoBannerDesc) {
        bannerMsgInput.addEventListener('input', (e) => {
            demoBannerDesc.textContent = e.target.value;
        });
    }
    if (bannerIntentSelect && demoBanner && demoBannerIcon) {
        bannerIntentSelect.addEventListener('change', (e) => {
            const intent = e.target.value;
            demoBanner.className = `apps-banner ${intent}`;
            const iconMap = { info: 'ℹ️', success: '✓', warning: '⚠️', error: '🚨' };
            demoBannerIcon.textContent = iconMap[intent] || 'ℹ️';
        });
    }

    const emptyIconSelect = document.getElementById('empty-icon-select');
    const emptyTitleInput = document.getElementById('empty-title-input');
    const emptyDescInput = document.getElementById('empty-desc-input');
    const emptyActionInput = document.getElementById('empty-action-input');
    
    const webEmptyIcon = document.getElementById('web-empty-icon');
    const webEmptyTitle = document.getElementById('web-empty-title');
    const webEmptyDesc = document.getElementById('web-empty-desc');
    const webEmptyAction = document.getElementById('web-empty-action');
    
    if (emptyIconSelect && webEmptyIcon) {
        emptyIconSelect.addEventListener('change', (e) => {
            webEmptyIcon.textContent = e.target.value;
        });
    }
    if (emptyTitleInput && webEmptyTitle) {
        emptyTitleInput.addEventListener('input', (e) => {
            webEmptyTitle.textContent = e.target.value;
        });
    }
    if (emptyDescInput && webEmptyDesc) {
        emptyDescInput.addEventListener('input', (e) => {
            webEmptyDesc.textContent = e.target.value;
        });
    }
    if (emptyActionInput && webEmptyAction) {
        emptyActionInput.addEventListener('input', (e) => {
            const val = e.target.value.trim();
            webEmptyAction.textContent = val;
            if (val === '') {
                webEmptyAction.style.display = 'none';
            } else {
                webEmptyAction.style.display = '';
            }
        });
    }

    const progressValSlider = document.getElementById('progress-val-slider');
    const progressValText = document.getElementById('progress-val-text');
    const customProgressFill = document.getElementById('custom-progress-fill');
    
    const progressSpeedSelect = document.getElementById('progress-speed-select');
    const webCircularProgress = document.getElementById('web-circular-progress');
    const webLinearProgressBar = document.getElementById('web-linear-progress-bar');
    
    const progressThicknessSlider = document.getElementById('progress-thickness-slider');
    const progressThicknessVal = document.getElementById('progress-thickness-val');
    const webProgressBarContainer = document.getElementById('web-progress-bar-container');
    const webLinearProgressContainer = document.getElementById('web-linear-progress-container');

    if (progressValSlider && progressValText && customProgressFill) {
        progressValSlider.addEventListener('input', (e) => {
            const val = e.target.value;
            progressValText.textContent = `${val}%`;
            customProgressFill.style.width = `${val}%`;
        });
    }

    if (progressSpeedSelect) {
        progressSpeedSelect.addEventListener('change', (e) => {
            const speed = e.target.value;
            if (webCircularProgress) {
                const circDur = speed === 'slow' ? '2s' : (speed === 'fast' ? '0.5s' : '1s');
                webCircularProgress.style.animationDuration = circDur;
            }
            if (webLinearProgressBar) {
                const linDur = speed === 'slow' ? '3s' : (speed === 'fast' ? '0.75s' : '1.5s');
                webLinearProgressBar.style.animationDuration = linDur;
            }
        });
    }

    if (progressThicknessSlider && progressThicknessVal) {
        progressThicknessSlider.addEventListener('input', (e) => {
            const val = e.target.value;
            progressThicknessVal.textContent = `${val}px`;
            if (webProgressBarContainer) webProgressBarContainer.style.height = `${val}px`;
            if (webLinearProgressContainer) webLinearProgressContainer.style.height = `${val}px`;
            if (webCircularProgress) {
                webCircularProgress.style.borderWidth = `${Math.max(2, Math.round(val / 1.5))}px`;
            }
        });
    }

    // --- 29. Config Tab Toggle System ---
    const configTabs = document.querySelectorAll('.config-tab-btn');
    configTabs.forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.stopPropagation();
            const parent = tab.closest('.playground-container');
            if (parent) {
                parent.classList.toggle('config-expanded');
            }
        });
    });

    // --- 30. Docs embed support: ?embed=1 strips chrome, #demo-* scrolls to + highlights a card ---
    const params = new URLSearchParams(window.location.search);
    if (params.get('embed') === '1') {
        document.body.classList.add('embed-mode');
    }

    const scrollToDemoTarget = () => {
        const hash = window.location.hash;
        if (!hash.startsWith('#demo-')) return;
        const target = document.querySelector(hash);
        if (!target) return;
        // Switch to the tab containing the target so it's actually visible before scrolling.
        const tabSection = target.closest('.tab-content');
        if (tabSection && !tabSection.classList.contains('active')) {
            document.querySelectorAll('.tab-content').forEach(s => s.classList.remove('active'));
            tabSection.classList.add('active');
            const navBtn = document.querySelector(`.nav-btn[data-tab="${tabSection.id}"]`);
            if (navBtn) {
                document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
                navBtn.classList.add('active');
            }
        }
        target.scrollIntoView({ block: 'center', behavior: document.body.classList.contains('embed-mode') ? 'auto' : 'smooth' });
        target.classList.add('highlight-target');
        setTimeout(() => target.classList.remove('highlight-target'), 2200);
    };
    scrollToDemoTarget();
    window.addEventListener('hashchange', scrollToDemoTarget);
});
