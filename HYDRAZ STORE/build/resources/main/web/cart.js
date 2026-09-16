document.addEventListener('DOMContentLoaded', () => {
    const cartItemsContainer = document.getElementById('cart-items');
    const totalPriceEl = document.getElementById('total-price');
    const checkoutForm = document.getElementById('checkout-form');
    const clearCartBtn = document.getElementById('clear-cart-btn');

    let cart = loadCart();

    // Init session header + checkout IGN section
    renderHeaderActions(true);
    renderCheckoutIgnSection();

    if (clearCartBtn) {
        clearCartBtn.addEventListener('click', () => {
            if (cart.length === 0) { showToast('Your cart is already empty.', 'error'); return; }
            if (confirm('Are you sure you want to clear all items from your cart?')) {
                cart = [];
                saveCart();
                updateCart();
                showToast('Cart cleared successfully.');
            }
        });
    }

    // (Screenshot upload removed - Zero-Touch automated verification)

    function loadCart() {
        try {
            const parsed = JSON.parse(localStorage.getItem('cart') || '[]');
            if (!Array.isArray(parsed)) return [];
            return parsed
                .filter(item => item && typeof item.id === 'string' && typeof item.name === 'string')
                .map(item => ({
                    id: item.id,
                    name: item.name,
                    price: Number(item.price) || 0,
                    quantity: Math.min(99, Math.max(1, parseInt(item.quantity, 10) || 1))
                }))
                .filter(item => item.price > 0);
        } catch {
            localStorage.removeItem('cart');
            return [];
        }
    }

    function saveCart() {
        localStorage.setItem('cart', JSON.stringify(cart));
    }

    function getConvenienceFee(subtotal) {
        if (subtotal <= 0) return 0;
        let fee = sessionStorage.getItem('cart_fee');
        if (!fee) {
            // Stable 2-digit paise verification fee (e.g. 0.15 to 0.89)
            const paise = Math.floor(Math.random() * 75) + 15;
            fee = (paise / 100).toFixed(2);
            sessionStorage.setItem('cart_fee', fee);
        }
        return parseFloat(fee);
    }

    function updateCart() {
        cartItemsContainer.innerHTML = '';
        const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
        const fee = getConvenienceFee(subtotal);
        const finalTotal = subtotal > 0 ? (subtotal + fee) : 0;

        const submitBtn = document.getElementById('checkout-submit-btn');
        const qrCodeEl = document.getElementById('payment-qr-code');
        const qrBadgeEl = document.getElementById('qr-amount-badge');
        const payUpiBtn = document.getElementById('pay-upi-btn');
        const subtotalEl = document.getElementById('subtotal-price');
        const feeEl = document.getElementById('convenience-fee');

        if (cart.length === 0) {
            sessionStorage.removeItem('cart_fee');
            cartItemsContainer.innerHTML = '<div class="empty-state">Your cart is empty. <a href="store.html" style="color:#C084FC;font-weight:700;margin-left:6px;">Continue shopping</a></div>';
            totalPriceEl.textContent = '₹0.00';
            if (subtotalEl) subtotalEl.textContent = '₹0.00';
            if (feeEl) feeEl.textContent = '+₹0.00';
            if (qrBadgeEl) qrBadgeEl.textContent = '₹0.00';
            if (qrCodeEl) qrCodeEl.src = 'img/qrcode.png';
            if (payUpiBtn) payUpiBtn.href = 'upi://pay?pa=veervikram278@okaxis&pn=Veervikram%20Pratap%20singh&cu=INR';
            if (submitBtn) submitBtn.disabled = true;
            if (clearCartBtn) clearCartBtn.style.display = 'none';
            return;
        }

        // Only enable checkout submit if logged in
        if (submitBtn) submitBtn.disabled = !getSessionIgn();
        if (clearCartBtn) clearCartBtn.style.display = 'flex';

        cartItemsContainer.innerHTML = cart.map(item => `
            <div class="cart-item">
                <div class="cart-item-info">
                    <span class="cart-item-name">${escapeHtml(item.name)}</span>
                    <span class="cart-item-price">₹${item.price.toFixed(0)} each</span>
                </div>
                <div class="cart-item-quantity" aria-label="${escapeHtml(item.name)} quantity">
                    <button class="quantity-btn" type="button" data-id="${escapeHtml(item.id)}" data-change="-1" aria-label="Decrease quantity"><i class="fas fa-minus"></i></button>
                    <span>${item.quantity}</span>
                    <button class="quantity-btn" type="button" data-id="${escapeHtml(item.id)}" data-change="1" aria-label="Increase quantity"><i class="fas fa-plus"></i></button>
                </div>
                <div class="cart-item-subtotal">₹${(item.price * item.quantity).toFixed(0)}</div>
            </div>
        `).join('');

        if (subtotalEl) subtotalEl.textContent = `₹${subtotal.toFixed(0)}`;
        if (feeEl) feeEl.textContent = `+₹${fee.toFixed(2)}`;
        totalPriceEl.textContent = `₹${finalTotal.toFixed(2)}`;
        if (qrBadgeEl) qrBadgeEl.textContent = `₹${finalTotal.toFixed(2)}`;

        // Dynamic UPI URL with exact decimal amount for automated payment verification
        const upiUrl = `upi://pay?pa=veervikram278@okaxis&pn=Veervikram%20Pratap%20singh&am=${finalTotal.toFixed(2)}&cu=INR&tn=Hydraz%20Order%20Verification`;
        
        if (qrCodeEl) {
            qrCodeEl.src = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&margin=10&data=${encodeURIComponent(upiUrl)}`;
        }
        if (payUpiBtn) {
            payUpiBtn.href = upiUrl;
        }

        document.querySelectorAll('.quantity-btn').forEach(button => {
            button.addEventListener('click', () => {
                updateQuantity(button.dataset.id, parseInt(button.dataset.change, 10));
            });
        });
    }

    function updateQuantity(id, change) {
        const item = cart.find(cartItem => cartItem.id === id);
        if (!item) return;
        item.quantity += change;
        if (item.quantity <= 0) {
            cart = cart.filter(cartItem => cartItem.id !== id);
        } else {
            item.quantity = Math.min(99, item.quantity);
        }
        saveCart();
        updateCart();
    }

    checkoutForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const ign = getSessionIgn();
        if (!ign) {
            showToast('Please login first to complete your purchase.', 'error');
            setTimeout(() => { window.location.href = 'index.html'; }, 1500);
            return;
        }

        if (cart.length === 0) {
            showToast('Your cart is empty.', 'error');
            return;
        }

        const submitBtn = document.getElementById('checkout-submit-btn');
        submitBtn.disabled = true;
        const originalHtml = submitBtn.innerHTML;
        submitBtn.innerHTML = '<span>Verifying...</span><i class="fas fa-spinner fa-spin" aria-hidden="true"></i>';

        const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
        const fee = getConvenienceFee(subtotal);
        const finalTotal = subtotal > 0 ? (subtotal + fee) : 0;

        const formData = new FormData();
        formData.set('ign', ign);
        formData.append('cart', JSON.stringify(cart));
        formData.append('final_total', finalTotal.toFixed(2));
        formData.append('convenience_fee', fee.toFixed(2));

        try {
            const response = await fetch('/api/checkout', { method: 'POST', body: formData });
            let resData = null;
            try {
                resData = await response.json();
            } catch {
                resData = { success: false, message: await response.text() };
            }

            if (!response.ok || !resData || resData.success === false) {
                showToast(resData && resData.message ? resData.message : 'Checkout failed.', 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalHtml;
                return;
            }

            // If already auto-verified immediately
            if (resData.status === 'COMPLETED') {
                showToast('Payment verified! Your order has been delivered in-game.', 'success');
                localStorage.removeItem('cart');
                cart = [];
                updateCart();
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalHtml;
                setTimeout(() => openPaymentHistory(ign, 'COMPLETED'), 800);
                return;
            }

            // Order entered VERIFYING stage -> Open Verification Polling Modal
            const orderId = resData.orderId;
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHtml;
            startVerifyingFlow(ign, orderId, finalTotal);

        } catch (err) {
            showToast('Checkout failed. Please try again.', 'error');
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalHtml;
        }
    });

    function startVerifyingFlow(ign, orderId, amount) {
        const vmOverlay = document.getElementById('vm-overlay');
        const vmTitle = document.getElementById('vm-title');
        const vmSubtitle = document.getElementById('vm-subtitle');
        const vmAmount = document.getElementById('vm-amount');
        const vmProgressBar = document.getElementById('vm-progress-bar');
        const vmOrderText = document.getElementById('vm-order-text');
        const vmSpinnerWrap = document.getElementById('vm-spinner-wrap');
        const vmActions = document.getElementById('vm-actions');
        const vmDoneBtn = document.getElementById('vm-done-btn');

        if (!vmOverlay) return;

        // Reset modal state
        vmOverlay.style.display = 'flex';
        vmSpinnerWrap.innerHTML = '<div class="vm-spinner" id="vm-spinner"></div>';
        vmTitle.textContent = 'Verifying Payment...';
        vmSubtitle.innerHTML = `Checking UPI gateway for exact match of <span style="color: #4ADE80; font-weight: 800;">₹${amount.toFixed(2)}</span>`;
        if (vmAmount) vmAmount.textContent = `₹${amount.toFixed(2)}`;
        if (vmOrderText) vmOrderText.innerHTML = `Order #${orderId} &bull; Listening for UPI payment notification`;
        if (vmProgressBar) {
            vmProgressBar.style.width = '15%';
            vmProgressBar.style.background = 'linear-gradient(90deg, #A855F7, #EC4899, #10B981)';
        }
        if (vmActions) vmActions.style.display = 'none';

        let pollCount = 0;
        const maxPolls = 15; // 15 * 2s = 30 seconds
        let completed = false;

        const pollInterval = setInterval(async () => {
            pollCount++;
            const progress = Math.min(92, 15 + Math.floor((pollCount / maxPolls) * 75));
            if (vmProgressBar) vmProgressBar.style.width = `${progress}%`;

            try {
                const res = await fetch(`/api/order-status?id=${orderId}`);
                if (res.ok) {
                    const data = await res.json();
                    if (data.status === 'COMPLETED' || data.verified) {
                        completed = true;
                        clearInterval(pollInterval);

                        // Visual success transition
                        if (vmSpinnerWrap) {
                            vmSpinnerWrap.innerHTML = '<i class="fas fa-check-circle" style="font-size: 54px; color: #10B981; animation: scaleUp 0.3s ease;"></i>';
                        }
                        if (vmTitle) vmTitle.textContent = 'Payment Verified! ✓';
                        if (vmSubtitle) vmSubtitle.innerHTML = `Order #${orderId} verified. Your items have been delivered in-game!`;
                        if (vmProgressBar) {
                            vmProgressBar.style.width = '100%';
                            vmProgressBar.style.background = '#10B981';
                        }

                        // Clear cart
                        localStorage.removeItem('cart');
                        cart = [];
                        updateCart();

                        setTimeout(() => {
                            vmOverlay.style.display = 'none';
                            openPaymentHistory(ign, 'COMPLETED');
                        }, 2200);
                        return;
                    }
                }
            } catch (e) {}

            if (pollCount >= maxPolls && !completed) {
                clearInterval(pollInterval);

                // Polling timeout - notification delayed or pending bank clearance
                if (vmSpinnerWrap) {
                    vmSpinnerWrap.innerHTML = '<i class="fas fa-hourglass-half" style="font-size: 48px; color: #FBBF24;"></i>';
                }
                if (vmTitle) vmTitle.textContent = 'Order in Verifying Stage';
                if (vmSubtitle) {
                    vmSubtitle.innerHTML = `Your order <strong>#${orderId}</strong> is saved. As soon as your UPI payment confirms, your items will be automatically delivered in-game!`;
                }
                if (vmProgressBar) vmProgressBar.style.width = '100%';
                if (vmActions) vmActions.style.display = 'block';

                // Clear cart so cart is clean for next purchase
                localStorage.removeItem('cart');
                cart = [];
                updateCart();

                if (vmDoneBtn) {
                    vmDoneBtn.onclick = () => {
                        vmOverlay.style.display = 'none';
                        openPaymentHistory(ign, 'VERIFYING');
                    };
                }
            }
        }, 2000);
    }

    // Initial render
    updateCart();
});

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast show';
    toast.style.cssText = `background:${type === 'error' ? 'rgba(239,68,68,0.95)' : 'rgba(34,197,94,0.95)'};color:white;padding:12px 20px;border-radius:8px;font-weight:700;font-size:13px;margin-top:8px;box-shadow:0 8px 24px rgba(0,0,0,0.4);`;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => { toast.remove(); }, 3000);
}
