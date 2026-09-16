// index.js

document.addEventListener('DOMContentLoaded', () => {
    // Attempt to render header session state
    renderHomeSessionState();

    const playerInput = document.getElementById('player-ign-input');
    if (playerInput) {
        playerInput.addEventListener('input', onPlayerInput);
        playerInput.addEventListener('keydown', (e) => {
            if(e.key === 'Enter') doPlayerLogin();
        });
    }

    const adminInput = document.getElementById('adminPass');
    if (adminInput) {
        adminInput.addEventListener('keydown', (e) => {
            if(e.key === 'Enter') doAdminLogin();
        });
    }

    const overlay = document.getElementById('global-login-overlay');
    if (overlay) {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) closeGlobalLogin();
        });
    }
});

function renderHomeSessionState() {
    const container = document.getElementById('home-nav-actions');
    if (!container) return;

    const playerIgn = localStorage.getItem('hydraz-session-ign');
    const adminToken = localStorage.getItem('ai_admin_token');

    let html = `
        <a href="store.html" class="nav-link"><i class="fas fa-store"></i><span>Store</span></a>
        <a href="tiers.html" class="nav-link"><i class="fas fa-trophy"></i><span>Tiers</span></a>
        <a href="https://discord.gg/5uDZxAY4Dp" target="_blank" class="nav-link discord-btn" style="background:#5865F2;color:#fff;border:1px solid #5865F2;">
            <i class="fab fa-discord"></i><span>Discord</span>
        </a>
        <div class="nav-link ip-copy-btn" onclick="navigator.clipboard.writeText('play.hydraz.online').then(()=>showToast('IP Copied! ✓'))" title="Click to copy IP" style="cursor:pointer;font-family:monospace;font-size:12px;">
            <i class="fas fa-server" style="color:#A855F7;"></i><span>play.hydraz.online</span>
        </div>
    `;

    if (playerIgn || adminToken) {
        let badge = adminToken ? '<span class="admin-badge">Admin</span>' : '';
        let ign = playerIgn || 'Admin User';
        html += `
            <div class="home-user-chip">
                <img src="https://mc-heads.net/avatar/${playerIgn || 'MHF_Steve'}/32" alt="Avatar" width="32" height="32" loading="lazy" decoding="async">
                <span>${ign}</span>
                ${badge}
            </div>
            <button class="login-btn-outline logout-style" onclick="doGlobalLogout()"><i class="fas fa-sign-out-alt" style="margin-right:6px;"></i>Logout</button>
        `;
    } else {
        html += `<button class="login-btn-outline" onclick="openGlobalLogin()"><i class="fas fa-sign-in-alt" style="margin-right:6px;"></i>Login</button>`;
    }
    
    container.innerHTML = html;
}

function toggleBodyScroll(disable) {
    document.body.style.overflow = disable ? 'hidden' : '';
}

function openGlobalLogin() {
    const overlay = document.getElementById('global-login-overlay');
    if (!overlay) return;
    overlay.classList.add('open');
    toggleBodyScroll(true);
    switchLoginTab('player');
    
    const ign = localStorage.getItem('hydraz-session-ign');
    if(ign) {
        document.getElementById('player-ign-input').value = ign;
        onPlayerInput();
    }
}

function closeGlobalLogin() {
    const overlay = document.getElementById('global-login-overlay');
    if (!overlay) return;
    overlay.classList.remove('open');
    const err = document.getElementById('global-login-error');
    if (err) err.innerText = '';
    toggleBodyScroll(false);
}

function switchLoginTab(tab) {
    document.getElementById('tab-player').classList.toggle('active', tab === 'player');
    document.getElementById('tab-admin').classList.toggle('active', tab === 'admin');
    
    document.getElementById('form-player').style.display = tab === 'player' ? 'block' : 'none';
    document.getElementById('form-admin').style.display = tab === 'admin' ? 'block' : 'none';
    document.getElementById('global-login-error').innerText = '';

    if(tab === 'player') document.getElementById('player-ign-input').focus();
    else document.getElementById('adminUser').focus();
}

let debounceTimer;
function onPlayerInput() {
    const val = document.getElementById('player-ign-input').value.trim();
    const btn = document.getElementById('player-login-btn');
    const isValid = /^[a-zA-Z0-9_.]{2,20}$/.test(val);
    btn.disabled = !isValid;

    clearTimeout(debounceTimer);
    if(isValid) {
        debounceTimer = setTimeout(() => {
            const img = document.getElementById('modal-avatar-img');
            img.onerror = function() {
                this.src = 'https://mc-heads.net/avatar/MHF_Steve/64';
            };
            img.src = 'https://mc-heads.net/avatar/' + encodeURIComponent(val) + '/64';
            img.setAttribute('width', '64');
            img.setAttribute('height', '64');
            img.setAttribute('loading', 'lazy');
            img.setAttribute('decoding', 'async');
            document.getElementById('modal-avatar-label').innerText = val;
            document.getElementById('modal-avatar-label').className = 'avatar-ign-preview';
        }, 500);
    } else {
        document.getElementById('modal-avatar-img').src = 'https://mc-heads.net/avatar/MHF_Steve/64';
        document.getElementById('modal-avatar-label').innerText = 'Enter your username below';
        document.getElementById('modal-avatar-label').className = 'avatar-ign-placeholder';
    }
}

function doPlayerLogin() {
    const val = document.getElementById('player-ign-input').value.trim();
    if(!/^[a-zA-Z0-9_.]{2,20}$/.test(val)) return;
    
    localStorage.setItem('hydraz-session-ign', val);
    closeGlobalLogin();
    renderHomeSessionState();
    showToast('Welcome to the store, ' + val + '!');
}

async function doAdminLogin() {
    const user = document.getElementById('adminUser').value;
    const pass = document.getElementById('adminPass').value;
    const errorMsg = document.getElementById('global-login-error');
    
    if(!user || !pass) {
        errorMsg.innerText = "Please fill in all fields.";
        return;
    }

    const btn = document.getElementById('admin-login-btn');
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Authenticating...';

    try {
        const res = await fetch('/api/ai/admin/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: 'username=' + encodeURIComponent(user) + '&password=' + encodeURIComponent(pass)
        });

        if(res.ok) {
            const data = await res.json();
            localStorage.setItem('ai_admin_token', data.token);
            closeGlobalLogin();
            renderHomeSessionState();
            showToast("Admin authenticated successfully.");
        } else {
            errorMsg.innerText = "Invalid credentials!";
        }
    } catch(e) {
        errorMsg.innerText = "Connection error.";
    }

    btn.disabled = false;
    btn.innerHTML = '<i class="fas fa-lock"></i> Authenticate';
}

function doGlobalLogout() {
    localStorage.removeItem('hydraz-session-ign');
    localStorage.removeItem('ai_admin_token');
    renderHomeSessionState();
    showToast("Logged out successfully.");
}

function showToast(message) {
    const container = document.getElementById('toast-container');
    if(!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast show';
    toast.style.cssText = 'background:rgba(34,197,94,0.95);color:white;padding:12px 20px;border-radius:8px;font-weight:700;font-size:13px;margin-top:8px;';
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => { toast.remove(); }, 3000);
}

function copyIP() { navigator.clipboard.writeText('play.hydraz.online').then(() => showToast('Server IP Copied! ✓')); }
