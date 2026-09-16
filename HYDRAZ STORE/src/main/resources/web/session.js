/**
 * session.js — Shared login/session/payment-history/cart logic for Hydraz Store
 */

const SESSION_KEY = 'hydraz-session-ign';
const AVATAR_BASE = 'https://mc-heads.net/avatar/';

/* ── Session helpers ─────────────────────────────────────────── */
function getSessionIgn() {
    return localStorage.getItem(SESSION_KEY) || '';
}

function setSessionIgn(ign) {
    localStorage.setItem(SESSION_KEY, ign);
}

function clearSessionIgn() {
    localStorage.removeItem(SESSION_KEY);
}

function avatarUrl(ign, size = 64) {
    return `${AVATAR_BASE}${encodeURIComponent(ign)}/${size}`;
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function toggleBodyScroll(disable) {
    if (disable) {
        document.body.classList.add('modal-open');
    } else {
        document.body.classList.remove('modal-open');
    }
}

/* ── Render header actions ───────────────────────────────────── */
function renderHeaderActions(isCartPage = false) {
    const container = document.getElementById('header-actions');
    if (!container) return;

    const ign = getSessionIgn();
    let cartCount = 0;
    try {
        const cart = JSON.parse(localStorage.getItem('cart')) || [];
        cartCount = cart.reduce((sum, item) => sum + (item.quantity || 1), 0);
    } catch(e) {}

    if (!ign) {
        container.innerHTML = `
            ${!isCartPage ? `<button class="cart-summary" id="cart-summary-btn" aria-label="Open cart" onclick="openCartDrawer()">
                <i class="fas fa-shopping-cart"></i>
                <span class="cart-label">Cart</span>
                <span id="cart-item-count">${cartCount}</span>
            </button>` : ''}
            <a href="index.html" class="login-trigger-btn" style="text-decoration:none;">
                <i class="fas fa-sign-in-alt"></i> Login on Home
            </a>
        `;
    } else {
        container.innerHTML = `
            ${!isCartPage ? `<button class="cart-summary" id="cart-summary-btn" aria-label="Open cart" onclick="openCartDrawer()">
                <i class="fas fa-shopping-cart"></i>
                <span class="cart-label">Cart</span>
                <span id="cart-item-count">${cartCount}</span>
            </button>` : ''}
            <button class="header-btn history-btn" id="open-ph-btn" onclick="openPaymentHistory('${escapeHtml(ign)}')">
                <i class="fas fa-receipt"></i> <span class="history-btn-text">Payment History</span>
            </button>
            <div class="user-session-chip">
                <img src="${avatarUrl(ign, 56)}" alt="${escapeHtml(ign)}'s avatar" onerror="this.src='https://mc-heads.net/avatar/MHF_Steve/56'">
                <span class="user-session-ign">${escapeHtml(ign)}</span>
                <button class="logout-mini-btn" onclick="doLogout()" title="Logout"><i class="fas fa-sign-out-alt"></i></button>
            </div>
        `;
    }
}

function doLogout() {
    clearSessionIgn();
    renderHeaderActions(document.body.classList.contains('cart-view'));
    if (document.body.classList.contains('cart-view')) {
        renderCheckoutIgnSection();
    }
    if (typeof showToast === 'function') {
        showToast('Logged out successfully.');
    }
}

/* ── Payment History Drawer ───────────────────────────────────── */
let phAllOrders = [];
let phActiveStatus = 'VERIFYING';

function openPaymentHistory(ign, initialStatus = 'VERIFYING') {
    const overlay = document.getElementById('ph-overlay');
    if (!overlay) return;

    const nameEl = document.getElementById('ph-ign-name');
    const avatarEl = document.getElementById('ph-avatar');
    if (nameEl) nameEl.textContent = ign;
    if (avatarEl) {
        avatarEl.src = avatarUrl(ign, 20);
        avatarEl.onerror = () => { avatarEl.src = 'https://mc-heads.net/avatar/MHF_Steve/20'; };
    }

    overlay.classList.add('open');
    if (typeof toggleBodyScroll === 'function') toggleBodyScroll(true);
    phActiveStatus = initialStatus || 'VERIFYING';

    document.querySelectorAll('.ph-tab').forEach(t => t.classList.remove('active'));
    const targetTab = document.querySelector(`.ph-tab[data-status="${phActiveStatus}"]`) || document.querySelector('.ph-tab');
    if (targetTab) targetTab.classList.add('active');

    document.querySelectorAll('.ph-tab').forEach(tab => {
        tab.onclick = () => {
            phActiveStatus = tab.dataset.status;
            document.querySelectorAll('.ph-tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            phRenderOrders();
        };
    });

    const closeBtn = document.getElementById('ph-close-btn');
    if (closeBtn) closeBtn.onclick = closePaymentHistory;
    overlay.onclick = (e) => { if (e.target === overlay) closePaymentHistory(); };

    phFetchOrders(ign);
}

function closePaymentHistory() {
    const overlay = document.getElementById('ph-overlay');
    if (overlay) {
        overlay.classList.remove('open');
        if (typeof toggleBodyScroll === 'function') toggleBodyScroll(false);
    }
}

async function phFetchOrders(ign) {
    const body = document.getElementById('ph-body');
    if (!body) return;
    body.innerHTML = '<div class="orders-loading"><i class="fas fa-spinner fa-spin"></i> Loading orders...</div>';

    try {
        const res = await fetch('/api/checkouts?ign=' + encodeURIComponent(ign));
        if (!res.ok) {
            body.innerHTML = '<div class="orders-empty">Could not load orders. Try again later.</div>';
            return;
        }
        phAllOrders = await res.json();
        phUpdateTabCounts();
        phRenderOrders();
    } catch {
        body.innerHTML = '<div class="orders-empty">Failed to fetch orders.</div>';
    }
}

function phUpdateTabCounts() {
    const counts = { VERIFYING: 0, COMPLETED: 0 };
    phAllOrders.forEach(o => {
        let s = (o.status || '').toUpperCase();
        if (s === 'PENDING') s = 'VERIFYING';
        if (s === 'ACCEPTED') s = 'COMPLETED';
        if (counts[s] !== undefined) counts[s]++;
    });
    const v = document.getElementById('ph-count-verifying');
    const c = document.getElementById('ph-count-completed');
    if (v) v.textContent = counts.VERIFYING;
    if (c) c.textContent = counts.COMPLETED;
}

function phRenderOrders() {
    const body = document.getElementById('ph-body');
    if (!body) return;

    const filtered = phAllOrders.filter(o => {
        let s = (o.status || '').toUpperCase();
        if (s === 'PENDING') s = 'VERIFYING';
        if (s === 'ACCEPTED') s = 'COMPLETED';
        return s === phActiveStatus;
    });

    if (filtered.length === 0) {
        const labels = { VERIFYING: 'verifying', COMPLETED: 'completed' };
        body.innerHTML = `<div class="orders-empty">No ${labels[phActiveStatus] || 'active'} orders found.</div>`;
        return;
    }

    body.innerHTML = filtered.map(order => {
        let rawStatus = (order.status || '').toUpperCase();
        if (rawStatus === 'PENDING') rawStatus = 'VERIFYING';
        if (rawStatus === 'ACCEPTED') rawStatus = 'COMPLETED';
        const isCompleted = rawStatus === 'COMPLETED';
        const statusClass = isCompleted ? 'completed' : 'verifying';
        const statusLabel = isCompleted ? 'Completed' : 'Verifying';
        const statusIcon = isCompleted ? 'fas fa-check-circle' : 'fas fa-spinner fa-spin';

        let formattedItemsHtml = '';
        try {
            const itemsArr = JSON.parse(order.items);
            if (Array.isArray(itemsArr) && itemsArr.length > 0) {
                formattedItemsHtml = itemsArr.map(i => {
                    const qty = i.quantity || 1;
                    const name = i.name || i.id;
                    return `<span class="ph-item-chip"><i class="fas fa-box-open"></i> ${qty}x ${escapeHtml(name)}</span>`;
                }).join('');
            } else {
                formattedItemsHtml = `<span class="ph-item-chip empty">No Items</span>`;
            }
        } catch (e) {
            formattedItemsHtml = `<span class="ph-item-chip text">${escapeHtml(order.items || 'None')}</span>`;
        }

        let amountBadge = '';
        if (order.amount && Number(order.amount) > 0) {
            amountBadge = `<span style="color: #4ADE80; font-weight: 700; font-size: 12px; margin-left: 8px;">₹${Number(order.amount).toFixed(2)}</span>`;
        }

        return `
            <div class="order-card">
                <div class="order-card-header">
                    <span class="order-card-id"><i class="fas fa-receipt"></i> Order #${escapeHtml(String(order.id))}${amountBadge}</span>
                    <span class="order-status-badge ${statusClass}"><i class="${statusIcon}" style="margin-right: 4px;"></i>${statusLabel}</span>
                </div>
                <div class="order-card-items-container">
                    ${formattedItemsHtml}
                </div>
            </div>
        `;
    }).join('');
}

/* ── Checkout IGN section (cart page only) ────────────────────── */
function renderCheckoutIgnSection() {
    const section = document.getElementById('checkout-ign-section');
    const ignInput = document.getElementById('ign');
    const submitBtn = document.getElementById('checkout-submit-btn');
    if (!section) return;

    const ign = getSessionIgn();

    if (ign) {
        section.innerHTML = `
            <label style="font-size:13px;font-weight:600;color:var(--text-secondary);display:block;margin-bottom:8px;">Minecraft Username</label>
            <div class="ign-locked-row">
                <img src="${avatarUrl(ign, 56)}" alt="${escapeHtml(ign)}" onerror="this.src='https://mc-heads.net/avatar/MHF_Steve/56'">
                <span class="ign-locked-name">${escapeHtml(ign)}</span>
                <span class="ign-locked-badge">Logged In</span>
            </div>
        `;
        if (ignInput) ignInput.value = ign;
        if (submitBtn) submitBtn.disabled = false;
    } else {
        section.innerHTML = `
            <div class="checkout-login-prompt">
                <i class="fas fa-user-lock"></i>
                <strong>Login required to checkout</strong><br>
                Please go to the Home page to set your Minecraft username before checking out.
                <br><br>
                <a href="index.html" class="login-btn-primary" style="max-width:220px;margin:0 auto;text-decoration:none;text-align:center;">
                    <i class="fas fa-home"></i> Go to Home
                </a>
            </div>
        `;
        if (ignInput) ignInput.value = '';
        if (submitBtn) submitBtn.disabled = true;
    }
}

/* ── Cart Drawer Overlay (Global) ─────────────────────────────────── */
function openCartDrawer() {
    let overlay = document.getElementById('cart-drawer-overlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.id = 'cart-drawer-overlay';
        overlay.className = 'cart-drawer-overlay';
        overlay.innerHTML = `
            <div class="cart-drawer" id="cart-drawer">
                <div class="cart-drawer-header">
                    <div style="display:flex;align-items:center;gap:8px;">
                        <h2><i class="fas fa-shopping-bag" style="color:#A855F7;"></i> Your Cart</h2>
                    </div>
                    <div style="display:flex;align-items:center;gap:10px;">
                        <button class="cart-clear-btn" onclick="clearCart()" title="Clear all items" style="background:transparent;border:1px solid rgba(248,113,113,0.3);color:#F87171;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;font-weight:600;"><i class="fas fa-trash-alt"></i> Clear</button>
                        <button class="cart-drawer-close" id="cart-drawer-close" aria-label="Close cart"><i class="fas fa-times"></i></button>
                    </div>
                </div>
                <div class="cart-drawer-body" id="cart-drawer-body">
                    <!-- Items injected here -->
                </div>
                <div class="cart-drawer-footer">
                    <div class="cart-drawer-total-row">
                        <span>Cart Total</span>
                        <span id="cart-drawer-total" style="color:#A855F7;font-weight:800;font-size:20px;">₹0.00</span>
                    </div>
                    <a href="cart.html" class="cart-drawer-checkout-btn" style="display:flex;align-items:center;justify-content:center;gap:10px;background:linear-gradient(135deg,#A855F7,#7C3AED);color:#fff;padding:14px;border-radius:12px;text-decoration:none;font-weight:800;letter-spacing:0.5px;box-shadow:0 8px 24px rgba(168,85,247,0.35);transition:all 0.25s;">
                        Proceed to Checkout <i class="fas fa-arrow-right"></i>
                    </a>
                </div>
            </div>
        `;
        document.body.appendChild(overlay);

        document.getElementById('cart-drawer-close').addEventListener('click', () => {
            overlay.classList.remove('open');
            toggleBodyScroll(false);
        });
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.remove('open');
                toggleBodyScroll(false);
            }
        });
    }

    renderCartDrawerItems();
    overlay.classList.add('open');
    toggleBodyScroll(true);
}

function renderCartDrawerItems() {
    const body = document.getElementById('cart-drawer-body');
    const totalEl = document.getElementById('cart-drawer-total');
    if (!body || !totalEl) return;

    let cart = [];
    try {
        cart = JSON.parse(localStorage.getItem('cart')) || [];
    } catch(e) {
        cart = [];
    }

    if (cart.length === 0) {
        body.innerHTML = '<div style="text-align:center; padding: 60px 20px; color: #64748B;"><i class="fas fa-shopping-cart" style="font-size: 42px; margin-bottom: 14px; opacity: 0.35; color:#A855F7;"></i><br><strong style="color:#CBD5E1;font-size:16px;">Your cart is empty</strong><br><span style="font-size:13px;">Browse the store and click "Add to Cart"!</span></div>';
        totalEl.textContent = '₹0.00';
        return;
    }

    let total = 0;
    body.innerHTML = cart.map((item, index) => {
        const itemPrice = Number(item.price) || 0;
        const itemQty = Number(item.quantity) || 1;
        total += itemPrice * itemQty;
        return `
            <div class="cart-drawer-item" style="display:flex;align-items:center;justify-content:space-between;padding:12px 14px;background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.07);border-radius:10px;margin-bottom:10px;">
                <div class="cart-drawer-item-info" style="display:flex;flex-direction:column;gap:4px;">
                    <span class="cart-drawer-item-name" style="font-weight:700;color:#F8FAFC;font-size:14px;">${escapeHtml(item.name)}</span>
                    <span class="cart-drawer-item-price" style="color:#A855F7;font-weight:700;font-size:13px;">₹${itemPrice.toFixed(0)} <span style="color:#64748B;font-weight:400;font-size:12px;">each</span></span>
                </div>
                <div class="cart-drawer-item-actions" style="display:flex;align-items:center;gap:6px;">
                    <button class="cart-drawer-qty-btn" onclick="updateDrawerCartQty(${index}, -1)"><i class="fas fa-minus"></i></button>
                    <span style="font-weight:800; min-width:24px; text-align:center; color:#FFF; font-size:14px;">${itemQty}</span>
                    <button class="cart-drawer-qty-btn" onclick="updateDrawerCartQty(${index}, 1)"><i class="fas fa-plus"></i></button>
                    <button class="cart-drawer-qty-btn" onclick="removeDrawerCartItem(${index})" style="background:rgba(239, 68, 68, 0.15); border:1px solid rgba(239, 68, 68, 0.3); color:#F87171; margin-left: 6px;"><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `;
    }).join('');

    totalEl.textContent = '₹' + total.toFixed(0);
}

window.clearCart = function() {
    localStorage.removeItem('cart');
    const countEl = document.getElementById('cart-item-count');
    if (countEl) countEl.textContent = '0';
    renderCartDrawerItems();
    if (typeof showToast === 'function') {
        showToast('Cart cleared.');
    }
};

window.updateDrawerCartQty = function(index, delta) {
    let cart = [];
    try {
        cart = JSON.parse(localStorage.getItem('cart')) || [];
    } catch(e){}
    if (cart[index]) {
        cart[index].quantity = (Number(cart[index].quantity) || 1) + delta;
        if (cart[index].quantity < 1) cart[index].quantity = 1;
        if (cart[index].quantity > 99) cart[index].quantity = 99;
        localStorage.setItem('cart', JSON.stringify(cart));
        
        const countEl = document.getElementById('cart-item-count');
        if (countEl) {
            const count = cart.reduce((n, i) => n + (i.quantity || 1), 0);
            countEl.textContent = count;
        }
        renderCartDrawerItems();
    }
};

window.removeDrawerCartItem = function(index) {
    let cart = [];
    try {
        cart = JSON.parse(localStorage.getItem('cart')) || [];
    } catch(e){}
    cart.splice(index, 1);
    localStorage.setItem('cart', JSON.stringify(cart));
    
    const countEl = document.getElementById('cart-item-count');
    if (countEl) {
        const count = cart.reduce((n, i) => n + (i.quantity || 1), 0);
        countEl.textContent = count;
    }
    renderCartDrawerItems();
};

window.showCartToast = function(itemName) {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'bottom-cart-toast';
    toast.innerHTML = `
        <i class="fas fa-check-circle toast-check-icon"></i>
        <div class="toast-text-group">
            <span class="toast-title">${escapeHtml(itemName)} added to cart</span>
            <span class="toast-sub">Ready for checkout</span>
        </div>
        <button class="toast-view-cart-btn" onclick="openCartDrawer(); this.closest('.bottom-cart-toast').remove();">
            <i class="fas fa-shopping-bag"></i> View Cart
        </button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => toast.remove(), 300);
    }, 2800);
};

window.showToast = function(msg) {
    if (typeof msg === 'string' && msg.includes('added to cart')) {
        const cleanName = msg.replace(/added to cart.*$/i, '').trim();
        window.showCartToast(cleanName || msg);
    } else {
        window.showCartToast(msg);
    }
};

/* ── Real-Time Order Verification Notification System ────────────── */
window.showOrderNotification = function(order, status) {
    let container = document.getElementById('order-notification-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'order-notification-container';
        container.style.cssText = 'position:fixed;top:24px;right:24px;z-index:99999;display:flex;flex-direction:column;gap:12px;max-width:380px;width:calc(100% - 48px);pointer-events:none;';
        document.body.appendChild(container);
    }

    const card = document.createElement('div');
    card.style.cssText = `
        pointer-events:auto;
        background: ${status === 'ACCEPTED' ? 'linear-gradient(135deg, rgba(16, 185, 129, 0.95), rgba(5, 150, 105, 0.95))' : 'linear-gradient(135deg, rgba(239, 68, 68, 0.95), rgba(185, 28, 28, 0.95))'};
        color: #FFF;
        padding: 16px 18px;
        border-radius: 16px;
        box-shadow: 0 12px 35px rgba(0,0,0,0.5), 0 0 25px ${status === 'ACCEPTED' ? 'rgba(16,185,129,0.5)' : 'rgba(239,68,68,0.5)'};
        backdrop-filter: blur(12px);
        border: 1px solid rgba(255,255,255,0.25);
        display: flex;
        align-items: flex-start;
        gap: 14px;
        transition: all 0.3s ease;
    `;

    const icon = status === 'ACCEPTED' ? 'fa-check-circle' : 'fa-times-circle';
    const title = status === 'ACCEPTED' ? '🎉 Order Approved & Delivered!' : '❌ Order Rejected';
    const msg = status === 'ACCEPTED' 
        ? `Order #${order.id} was accepted by staff on Discord! Your items have been credited in-game.`
        : `Order #${order.id} was rejected. Please review payment proof or contact staff on Discord.`;

    card.innerHTML = `
        <div style="font-size: 24px; line-height: 1; margin-top: 2px;"><i class="fas ${icon}"></i></div>
        <div style="flex:1;">
            <div style="font-weight: 800; font-size: 14.5px; margin-bottom: 4px; letter-spacing: 0.3px;">${title}</div>
            <div style="font-size: 12.5px; opacity: 0.94; line-height: 1.4; margin-bottom: 10px;">${msg}</div>
            <button onclick="openPaymentHistory('${escapeHtml(getSessionIgn())}'); this.closest('div').parentElement.remove();" style="background: rgba(255,255,255,0.25); border: 1px solid rgba(255,255,255,0.4); color: white; padding: 5px 12px; border-radius: 8px; font-weight: 700; font-size: 11.5px; cursor: pointer;">
                View Order Details
            </button>
        </div>
        <button onclick="this.parentElement.remove()" style="background:none;border:none;color:white;opacity:0.8;cursor:pointer;font-size:16px;padding:2px;" aria-label="Close notification">
            <i class="fas fa-times"></i>
        </button>
    `;

    container.appendChild(card);

    // Auto-refresh payment history if currently open
    if (document.getElementById('ph-overlay')?.classList.contains('open')) {
        phFetchOrders(getSessionIgn());
    }

    setTimeout(() => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(-10px)';
        setTimeout(() => card.remove(), 350);
    }, 9000);
};

let lastKnownStatuses = {};
function initOrderStatusPoller() {
    const ign = getSessionIgn();
    if (!ign) return;

    try {
        lastKnownStatuses = JSON.parse(sessionStorage.getItem('order_cache_' + ign) || '{}');
    } catch(e) {
        lastKnownStatuses = {};
    }

    async function pollOrderUpdates() {
        const currentIgn = getSessionIgn();
        if (!currentIgn) return;

        try {
            const res = await fetch('/api/checkouts?ign=' + encodeURIComponent(currentIgn));
            if (!res.ok) return;
            const orders = await res.json();
            if (!Array.isArray(orders)) return;

            let cacheUpdated = false;
            orders.forEach(order => {
                const id = String(order.id);
                const currentStatus = (order.status || 'PENDING').toUpperCase();
                const prevStatus = lastKnownStatuses[id];

                // If status transitioned from PENDING to ACCEPTED or REJECTED
                if (prevStatus && prevStatus === 'PENDING' && currentStatus !== 'PENDING') {
                    showOrderNotification(order, currentStatus);
                }
                if (lastKnownStatuses[id] !== currentStatus) {
                    lastKnownStatuses[id] = currentStatus;
                    cacheUpdated = true;
                }
            });

            if (cacheUpdated) {
                sessionStorage.setItem('order_cache_' + currentIgn, JSON.stringify(lastKnownStatuses));
                phUpdateTabCounts();
            }
        } catch(e) {}
    }

    pollOrderUpdates();
    setInterval(pollOrderUpdates, 6000);
}

document.addEventListener('DOMContentLoaded', () => {
    renderHeaderActions(document.body.classList.contains('cart-view'));
    initOrderStatusPoller();
});
