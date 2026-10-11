/**
 * RohithMart Core JavaScript Client
 * Comprehensive utilities for cart updates, modals, password toggling, and notifications.
 */
(function() {
    'use strict';

    window.RohithMart = window.RohithMart || {};

    // Get WebApp Context Path
    RohithMart.getContextPath = function() {
        const meta = document.querySelector('meta[name="context-path"]');
        if (meta && meta.content) return meta.content;
        const path = window.location.pathname;
        const slashIdx = path.indexOf('/', 1);
        return slashIdx > 0 ? path.substring(0, slashIdx) : '';
    };

    // Standard JSON Fetch Wrapper with CSRF header injection
    RohithMart.fetchJson = async function(url, options = {}) {
        options.headers = options.headers || {};
        options.headers['Accept'] = 'application/json';

        // Retrieve CSRF token from meta or form if available
        const csrfToken = document.querySelector('meta[name="csrf-token"]')?.content ||
                          document.querySelector('input[name="_csrf"]')?.value;
        if (csrfToken) {
            options.headers['X-CSRF-TOKEN'] = csrfToken;
        }

        if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(options.body);
        }

        try {
            const response = await fetch(url, options);
            const data = await response.json().catch(() => null);
            return { ok: response.ok, status: response.status, data };
        } catch (err) {
            console.error('Network or parsing error:', err);
            return { ok: false, status: 0, error: { message: 'Network connection failed' } };
        }
    };

    // Live Cart Badge Management
    RohithMart.updateCartBadge = function(count) {
        const badge = document.getElementById('nav-cart-badge');
        if (!badge) return;
        if (count && count > 0) {
            badge.textContent = count;
            badge.style.display = 'inline-block';
        } else {
            badge.style.display = 'none';
        }
    };

    RohithMart.refreshCart = async function() {
        const contextPath = RohithMart.getContextPath();
        const res = await RohithMart.fetchJson(contextPath + '/api/cart');
        if (res.ok && res.data) {
            const count = res.data.itemCount || (res.data.items ? res.data.items.length : 0);
            RohithMart.updateCartBadge(count);
        }
    };

    // Quick Add to Cart from any card or detail page
    RohithMart.quickAddToCart = async function(productId, quantity = 1, btn = null) {
        if (!productId) return;
        const contextPath = RohithMart.getContextPath();

        if (btn) {
            btn.classList.add('loading');
            btn.disabled = true;
        }

        try {
            const res = await RohithMart.fetchJson(contextPath + '/api/cart/add', {
                method: 'POST',
                body: { productId: parseInt(productId, 10), quantity: parseInt(quantity, 10) }
            });

            if (res.ok) {
                RohithMart.showToast('Item successfully added to your cart.', 'success');
                if (res.data && res.data.itemCount !== undefined) {
                    RohithMart.updateCartBadge(res.data.itemCount);
                } else {
                    RohithMart.refreshCart();
                }
            } else if (res.status === 401) {
                RohithMart.showToast('Please sign in to add items to your cart.', 'info');
                setTimeout(() => {
                    window.location.href = contextPath + '/auth/login';
                }, 1000);
            } else {
                const errMsg = res.data?.message || res.data?.errorCode || 'Could not add item to cart.';
                RohithMart.showToast(errMsg, 'error');
            }
        } catch (err) {
            RohithMart.showToast('Network error while updating cart.', 'error');
        } finally {
            if (btn) {
                btn.classList.remove('loading');
                btn.disabled = false;
            }
        }
    };

    // Accessible Global Modal / Confirmation Dialog Controller
    RohithMart.Modal = {
        open: function(title, message, onConfirm, confirmText = 'Confirm', isDanger = false) {
            const modal = document.getElementById('global-modal');
            if (!modal) {
                if (window.confirm(message)) onConfirm();
                return;
            }

            const titleEl = document.getElementById('modal-title');
            const descEl = document.getElementById('modal-desc');
            const confirmBtn = document.getElementById('modal-confirm-btn');
            const cancelBtn = document.getElementById('modal-cancel-btn');
            const closeBtn = document.getElementById('modal-close-btn');

            if (titleEl) titleEl.textContent = title;
            if (descEl) descEl.textContent = message;

            if (confirmBtn) {
                confirmBtn.textContent = confirmText;
                confirmBtn.className = isDanger ? 'btn btn-danger btn-sm' : 'btn btn-primary btn-sm';
                confirmBtn.onclick = function() {
                    RohithMart.Modal.close();
                    if (typeof onConfirm === 'function') onConfirm();
                };
            }

            const dismiss = function() {
                RohithMart.Modal.close();
            };

            if (cancelBtn) cancelBtn.onclick = dismiss;
            if (closeBtn) closeBtn.onclick = dismiss;

            modal.classList.add('active');
            modal.setAttribute('aria-hidden', 'false');
            if (confirmBtn) confirmBtn.focus();

            // Keyboard Escape Listener
            const keyHandler = function(e) {
                if (e.key === 'Escape') {
                    dismiss();
                    document.removeEventListener('keydown', keyHandler);
                }
            };
            document.addEventListener('keydown', keyHandler);
        },
        close: function() {
            const modal = document.getElementById('global-modal');
            if (modal) {
                modal.classList.remove('active');
                modal.setAttribute('aria-hidden', 'true');
            }
        }
    };

    // DOM Ready Initialization
    document.addEventListener('DOMContentLoaded', function() {
        // 1. Initial cart badge sync
        RohithMart.refreshCart();

        // 2. Add-to-cart buttons binding
        document.querySelectorAll('.add-to-cart-btn').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const productId = this.dataset.productId;
                const qtyInput = document.getElementById('qty');
                const quantity = qtyInput ? qtyInput.value : 1;
                RohithMart.quickAddToCart(productId, quantity, this);
            });
        });

        // 3. Password visibility toggle
        document.querySelectorAll('.password-toggle-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const targetId = this.dataset.target;
                const input = targetId ? document.getElementById(targetId) : this.closest('.password-wrapper')?.querySelector('input');
                if (!input) return;
                if (input.type === 'password') {
                    input.type = 'text';
                    this.textContent = '👁️‍🗨️';
                    this.setAttribute('aria-label', 'Hide password');
                } else {
                    input.type = 'password';
                    this.textContent = '👁️';
                    this.setAttribute('aria-label', 'Show password');
                }
            });
        });

        // 4. Quantity Stepper (+ / -)
        document.querySelectorAll('.qty-stepper').forEach(stepper => {
            const minusBtn = stepper.querySelector('.qty-minus');
            const plusBtn = stepper.querySelector('.qty-plus');
            const input = stepper.querySelector('input[type="number"]');
            if (!input) return;

            if (minusBtn) {
                minusBtn.addEventListener('click', function() {
                    const min = parseInt(input.min, 10) || 1;
                    const val = parseInt(input.value, 10) || 1;
                    if (val > min) {
                        input.value = val - 1;
                        input.dispatchEvent(new Event('change'));
                    }
                });
            }

            if (plusBtn) {
                plusBtn.addEventListener('click', function() {
                    const max = parseInt(input.max, 10) || 999;
                    const val = parseInt(input.value, 10) || 1;
                    if (val < max) {
                        input.value = val + 1;
                        input.dispatchEvent(new Event('change'));
                    }
                });
            }
        });

        // 5. Product Gallery Thumbnail Switcher
        document.querySelectorAll('.gallery-thumb').forEach(thumb => {
            thumb.addEventListener('click', function() {
                const fullSrc = this.dataset.fullSrc;
                const mainImg = document.querySelector('.product-detail-image img');
                if (fullSrc && mainImg) {
                    mainImg.src = fullSrc;
                    document.querySelectorAll('.gallery-thumb').forEach(t => t.classList.remove('active'));
                    this.classList.add('active');
                }
            });
        });

        // 6. Form Loading Button State
        document.querySelectorAll('form[data-loading]').forEach(form => {
            form.addEventListener('submit', function() {
                const submitBtn = this.querySelector('button[type="submit"]');
                if (submitBtn) {
                    submitBtn.classList.add('loading');
                    submitBtn.disabled = true;
                }
            });
        });
    });
})();
