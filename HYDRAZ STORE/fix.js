const fs = require('fs');
let content = fs.readFileSync('src/main/resources/web/session.js', 'utf8');

const targetStr = const avatarEl = document.getElementById('ph-avatar');\r\n        const s = (o.status || '').toUpperCase();;
const newStr = const avatarEl = document.getElementById('ph-avatar');
    if (nameEl) nameEl.textContent = ign;
    if (avatarEl) {
        avatarEl.src = avatarUrl(ign, 20);
        avatarEl.onerror = () => { avatarEl.src = 'https://mc-heads.net/avatar/MHF_Steve/20'; };
    }

    overlay.classList.add('open');
    if (typeof toggleBodyScroll === 'function') toggleBodyScroll(true);
    phActiveStatus = 'PENDING';

    // Reset tabs
    document.querySelectorAll('.ph-tab').forEach(t => t.classList.remove('active'));
    const firstTab = document.querySelector('.ph-tab[data-status="PENDING"]');
    if (firstTab) firstTab.classList.add('active');

    // Bind tabs
    document.querySelectorAll('.ph-tab').forEach(tab => {
        tab.onclick = () => {
            phActiveStatus = tab.dataset.status;
            document.querySelectorAll('.ph-tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            phRenderOrders();
        };
    });

    // Close button
    const closeBtn = document.getElementById('ph-close-btn');
    if (closeBtn) closeBtn.onclick = closePaymentHistory;
    overlay.onclick = (e) => { if (e.target === overlay) closePaymentHistory(); };

    // Fetch orders
    phFetchOrders(ign);
}

function closePaymentHistory() {
    const overlay = document.getElementById('ph-overlay');
    if (overlay) overlay.classList.remove('open');
    if (typeof toggleBodyScroll === 'function') toggleBodyScroll(false);
}

async function phFetchOrders(ign) {
    const body = document.getElementById('ph-body');
    if (!body) return;
    body.innerHTML = '<div class=\"orders-loading\"><i class=\"fas fa-spinner fa-spin\"></i> Loading orders...</div>';

    try {
        const res = await fetch(\/api/checkouts?ign=\\);
        if (!res.ok) {
            body.innerHTML = '<div class=\"orders-empty\">Could not load orders. Try again later.</div>';
            return;
        }
        phAllOrders = await res.json();
        phUpdateTabCounts();
        phRenderOrders();
    } catch {
        body.innerHTML = '<div class=\"orders-empty\">Failed to fetch orders.</div>';
    }
}

function phUpdateTabCounts() {
    const counts = { PENDING: 0, ACCEPTED: 0, REJECTED: 0 };
    phAllOrders.forEach(o => {
        const s = (o.status || '').toUpperCase();;

content = content.replace(targetStr, newStr);
// Wait, the file might have just \n instead of \r\n.
// Let's use a regex replacement to be safe.
const regex = /const avatarEl = document.getElementById\('ph-avatar'\);[\s\S]*?const s = \(o\.status \|\| ''\)\.toUpperCase\(\);/;
content = content.replace(regex, newStr);
fs.writeFileSync('src/main/resources/web/session.js', content);
