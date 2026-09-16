/**
 * ai-chat.js — Hydraz AI AI Chat Widget
 * Handles session management, message sending, admin login, and confirmation flow.
 */

(function () {
    'use strict';

    // ─── State ───────────────────────────────────────────────────────────
    const state = {
        sessionId: null,
        isAdmin: false,
        adminToken: null,
        adminUser: null,
        isOpen: false,
        isSending: false,
        showAdminLogin: false,
        hasMessages: false,
    };

    const STORAGE_KEY_TOKEN = 'ai_admin_token';
    const STORAGE_KEY_USER = 'hydraz-ai-admin-user';

    // ─── DOM refs ────────────────────────────────────────────────────────
    let triggerBtn, panel, messagesEl, inputEl, sendBtn,
        adminLoginSection, adminUserInput, adminPassInput, adminLoginBtn,
        adminBadge, adminToggleBtn, statusDot;

    // ─── Init ────────────────────────────────────────────────────────────
    function init() {
        if (document.getElementById('ai-chat-root')) return;
        injectHTML();
        bindRefs();
        bindEvents();
        restoreAdminSession();
        createSession();
    }

    function injectHTML() {
        const container = document.createElement('div');
        container.id = 'ai-chat-root';
        container.innerHTML = `
            <!-- Trigger button -->
            <button class="ai-trigger" id="ai-trigger" title="Chat with Hydraz AI" aria-label="Open AI chat">
                <svg viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="10" rx="2"></rect>
                    <circle cx="12" cy="5" r="2"></circle>
                    <path d="M12 7v4"></path>
                    <line x1="8" y1="16" x2="8.01" y2="16" stroke-width="3"></line>
                    <line x1="16" y1="16" x2="16.01" y2="16" stroke-width="3"></line>
                </svg>
                <span class="ai-trigger-badge">AI</span>
            </button>

            <!-- Chat panel -->
            <div class="ai-panel" id="ai-panel" role="dialog" aria-label="Hydraz AI Chat">
                <div class="ai-panel-header">
                    <div class="ai-bot-avatar">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#A855F7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="11" width="18" height="10" rx="2"></rect>
                            <circle cx="12" cy="5" r="2"></circle>
                            <path d="M12 7v4"></path>
                            <line x1="8" y1="16" x2="8.01" y2="16" stroke-width="3"></line>
                            <line x1="16" y1="16" x2="16.01" y2="16" stroke-width="3"></line>
                        </svg>
                    </div>
                    <div class="ai-bot-info">
                        <div class="ai-bot-name">Hydraz AI <span class="ai-admin-badge" id="ai-admin-badge" style="display:none;">ADMIN</span></div>
                        <div class="ai-bot-status">
                            <div class="ai-status-dot" id="ai-status-dot"></div>
                            <span id="ai-status-label">Connecting...</span>
                        </div>
                    </div>
                    <div class="ai-header-actions">
                        <button class="ai-header-btn admin" id="ai-admin-toggle-btn" title="Admin login">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                            </svg>
                        </button>
                        <button class="ai-header-btn" id="ai-clear-btn" title="Clear chat">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="3 6 5 6 21 6"></polyline>
                                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            </svg>
                        </button>
                        <button class="ai-header-btn" id="ai-close-btn" title="Close">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="18" y1="6" x2="6" y2="18"></line>
                                <line x1="6" y1="6" x2="18" y2="18"></line>
                            </svg>
                        </button>
                    </div>
                </div>

                <!-- Messages -->
                <div class="ai-messages" id="ai-messages">
                    <div class="ai-empty-state" id="ai-empty-state">
                        <div class="ai-empty-icon">
                            <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#A855F7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="11" width="18" height="10" rx="2"></rect>
                                <circle cx="12" cy="5" r="2"></circle>
                                <path d="M12 7v4"></path>
                                <line x1="8" y1="16" x2="8.01" y2="16" stroke-width="3"></line>
                                <line x1="16" y1="16" x2="16.01" y2="16" stroke-width="3"></line>
                            </svg>
                        </div>
                        <div class="ai-empty-title">Hi! I'm Hydraz AI</div>
                        <div class="ai-empty-subtitle">Ask me anything about Hydraz Network — server IP, ranks, rules, or store purchases.</div>
                        <div class="ai-suggestions">
                            <button class="ai-suggestion" data-q="What is the server IP?">Server IP?</button>
                            <button class="ai-suggestion" data-q="How do I buy a rank?">How to buy a rank?</button>
                            <button class="ai-suggestion" data-q="What are the server rules?">Server Rules?</button>
                            <button class="ai-suggestion" data-q="Where is Discord?">Discord Link?</button>
                        </div>
                    </div>
                </div>

                <!-- Admin login panel -->
                <div class="ai-admin-login" id="ai-admin-login" style="display:none;">
                    <div class="ai-admin-login-title">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right:6px;">
                            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                        </svg>
                        Admin Login
                    </div>
                    <div class="ai-admin-inputs">
                        <input type="text" class="ai-admin-input" id="ai-admin-user" placeholder="Username" autocomplete="off">
                        <input type="password" class="ai-admin-input" id="ai-admin-pass" placeholder="Password">
                        <button class="ai-admin-login-btn" id="ai-admin-login-btn">
                            Login as Admin
                        </button>
                    </div>
                </div>

                <!-- Input area -->
                <div class="ai-input-area">
                    <textarea class="ai-input" id="ai-input" placeholder="Ask Hydraz AI anything..." rows="1" maxlength="2000"></textarea>
                    <button class="ai-send-btn" id="ai-send-btn" aria-label="Send message" disabled>
                        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="22" y1="2" x2="11" y2="13"></line>
                            <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
                        </svg>
                    </button>
                </div>
            </div>
        `;
        document.body.appendChild(container);
    }

    function bindRefs() {
        triggerBtn        = document.getElementById('ai-trigger');
        panel             = document.getElementById('ai-panel');
        messagesEl        = document.getElementById('ai-messages');
        inputEl           = document.getElementById('ai-input');
        sendBtn           = document.getElementById('ai-send-btn');
        adminLoginSection = document.getElementById('ai-admin-login');
        adminUserInput    = document.getElementById('ai-admin-user');
        adminPassInput    = document.getElementById('ai-admin-pass');
        adminLoginBtn     = document.getElementById('ai-admin-login-btn');
        adminBadge        = document.getElementById('ai-admin-badge');
        adminToggleBtn    = document.getElementById('ai-admin-toggle-btn');
        statusDot         = document.getElementById('ai-status-dot');
    }

    function bindEvents() {
        if (!triggerBtn) return;
        triggerBtn.addEventListener('click', togglePanel);
        document.getElementById('ai-close-btn').addEventListener('click', closePanel);
        document.getElementById('ai-clear-btn').addEventListener('click', clearChat);
        adminToggleBtn.addEventListener('click', toggleAdminLogin);
        adminLoginBtn.addEventListener('click', doAdminLogin);
        sendBtn.addEventListener('click', sendMessage);

        inputEl.addEventListener('input', () => {
            sendBtn.disabled = inputEl.value.trim().length === 0 || state.isSending;
            autoResizeInput();
        });
        inputEl.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                if (!sendBtn.disabled) sendMessage();
            }
        });
        adminPassInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') doAdminLogin();
        });

        // Suggestion buttons
        document.querySelectorAll('.ai-suggestion').forEach(btn => {
            btn.addEventListener('click', () => {
                inputEl.value = btn.dataset.q;
                sendBtn.disabled = false;
                sendMessage();
            });
        });
    }

    // ─── Session ─────────────────────────────────────────────────────────
    async function createSession() {
        try {
            const ign = localStorage.getItem('hydraz-session-ign') || 'Guest';
            const payload = { ign: ign };
            if (state.adminToken) payload.adminToken = state.adminToken;

            const res = await fetch('/api/ai/session', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!res.ok) throw new Error('HTTP ' + res.status);
            const data = await res.json();
            state.sessionId = data.sessionId;
            setStatus('online', state.isAdmin ? `Admin: ${state.adminUser}` : 'Online • Hydraz AI');
            if (inputEl) sendBtn.disabled = inputEl.value.trim().length === 0;

            if (state.adminToken) {
                await tryRestoreAdminSession();
            }
        } catch (e) {
            console.warn('AI session error:', e);
            setStatus('online', 'Online • Hydraz AI');
            if (sendBtn && inputEl) sendBtn.disabled = inputEl.value.trim().length === 0;
        }
    }

    function restoreAdminSession() {
        const token = localStorage.getItem(STORAGE_KEY_TOKEN);
        const user  = localStorage.getItem(STORAGE_KEY_USER);
        if (token && user) {
            state.adminToken = token;
            state.adminUser = user;
        }
    }

    async function tryRestoreAdminSession() {
        if (!state.adminToken || !state.sessionId) return;
        try {
            setAdminMode(true, state.adminUser);
        } catch (_) {}
    }

    // ─── UI state ────────────────────────────────────────────────────────
    function togglePanel() {
        state.isOpen ? closePanel() : openPanel();
    }

    function openPanel() {
        state.isOpen = true;
        triggerBtn.classList.add('open');
        panel.classList.add('open');
        inputEl.focus();
    }

    function closePanel() {
        state.isOpen = false;
        triggerBtn.classList.remove('open');
        panel.classList.remove('open');
    }

    function toggleAdminLogin() {
        if (state.isAdmin) {
            doAdminLogout();
            return;
        }
        state.showAdminLogin = !state.showAdminLogin;
        adminLoginSection.style.display = state.showAdminLogin ? 'block' : 'none';
        if (state.showAdminLogin) adminUserInput.focus();
    }

    function setStatus(type, label) {
        if (!statusDot) return;
        statusDot.className = 'ai-status-dot ' + type;
        const labelEl = document.getElementById('ai-status-label');
        if (labelEl) labelEl.textContent = label;
    }

    function setAdminMode(isAdmin, username) {
        state.isAdmin = isAdmin;
        adminBadge.style.display = isAdmin ? 'inline-block' : 'none';
        adminToggleBtn.classList.toggle('active', isAdmin);
        adminToggleBtn.title = isAdmin ? `Logged in as ${username} (click to logout)` : 'Admin login';
        adminLoginSection.style.display = 'none';
        state.showAdminLogin = false;
        setStatus('online', isAdmin ? `Admin: ${username}` : 'Online • Hydraz AI');
    }

    // ─── Admin auth ──────────────────────────────────────────────────────
    async function doAdminLogin() {
        const username = adminUserInput.value.trim();
        const password = adminPassInput.value;
        if (!username || !password) {
            appendBotMessage('⚠️ Please enter both username and password.');
            return;
        }
        if (!state.sessionId) {
            appendBotMessage('⚠️ Session not ready. Please wait a moment and try again.');
            return;
        }

        adminLoginBtn.disabled = true;
        adminLoginBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';

        try {
            const res = await fetch('/api/ai/admin/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `username=${encodeURIComponent(user)}&password=${encodeURIComponent(pass)}`
            });

            if (res.ok) {
                const data = await res.json();
                state.adminToken = data.token;
                localStorage.setItem(STORAGE_KEY_TOKEN, data.token);
                localStorage.setItem(STORAGE_KEY_USER, user);
                setAdminMode(true, user);
                adminPassInput.value = '';
                appendBotMessage(`✅ Welcome back, **${user}**! You now have admin access.`);
            } else {
                const err = await res.text();
                appendBotMessage('❌ Login failed: ' + err);
            }
        } catch (e) {
            appendBotMessage('❌ Login error. Please try again.');
        } finally {
            adminLoginBtn.disabled = false;
            adminLoginBtn.innerHTML = '<i class="fas fa-sign-in-alt"></i> Login as Admin';
        }
    }

    function doAdminLogout() {
        state.adminToken = null;
        state.adminUser = null;
        localStorage.removeItem(STORAGE_KEY_TOKEN);
        localStorage.removeItem(STORAGE_KEY_USER);
        setAdminMode(false, null);
        appendBotMessage('You have been logged out from admin mode.');
    }

    // ─── Messaging ───────────────────────────────────────────────────────
    async function sendMessage() {
        const text = inputEl.value.trim();
        if (!text || state.isSending || !state.sessionId) return;

        state.isSending = true;
        sendBtn.disabled = true;
        inputEl.value = '';
        inputEl.style.height = '';
        const emptyState = document.getElementById('ai-empty-state');
        if (emptyState) emptyState.style.display = 'none';
        state.hasMessages = true;

        appendUserMessage(text);
        const typingEl = appendTyping();
        setStatus('thinking', 'Thinking...');

        try {
            const body = { sessionId: state.sessionId, message: text };
            if (state.adminToken) body.adminToken = state.adminToken;

            const res = await fetch('/api/ai/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            typingEl.remove();

            if (!res.ok) {
                const err = await res.text();
                if (res.status === 404) {
                    await createSession();
                    appendBotMessage('⚠️ Session expired. Please resend your message.');
                } else {
                    appendBotMessage('⚠️ Error: ' + err);
                }
            } else {
                const data = await res.json();
                const replyText = data.text || data.reply;
                if (replyText) {
                    appendBotMessage(replyText);
                } else {
                    appendBotMessage('⚠️ Received an empty response.');
                }
            }
        } catch (e) {
            typingEl.remove();
            appendBotMessage('⚠️ Connection error. Please check your network and try again.');
        } finally {
            state.isSending = false;
            setStatus('online', state.isAdmin ? `Admin: ${state.adminUser}` : 'Online • Hydraz AI');
            sendBtn.disabled = inputEl.value.trim().length === 0;
        }
    }

    // ─── Message rendering ───────────────────────────────────────────────
    function appendUserMessage(text) {
        const msg = document.createElement('div');
        msg.className = 'ai-msg user';
        msg.innerHTML = `
            <div class="ai-msg-avatar"><i class="fas fa-user" style="font-size:11px;"></i></div>
            <div class="ai-msg-bubble">${escapeHtml(text)}</div>
        `;
        messagesEl.appendChild(msg);
        scrollToBottom();
    }

    function appendBotMessage(text) {
        const msg = document.createElement('div');
        msg.className = 'ai-msg bot';
        msg.innerHTML = `
            <div class="ai-msg-avatar"><i class="fas fa-robot" style="font-size:11px;color:#A855F7;"></i></div>
            <div class="ai-msg-bubble">${formatMarkdown(text)}</div>
        `;
        messagesEl.appendChild(msg);
        scrollToBottom();
    }

    function appendTyping() {
        const el = document.createElement('div');
        el.className = 'ai-msg bot ai-typing';
        el.innerHTML = `
            <div class="ai-msg-avatar"><i class="fas fa-robot" style="font-size:11px;color:#A855F7;"></i></div>
            <div class="ai-msg-bubble">
                <span class="ai-dot"></span><span class="ai-dot"></span><span class="ai-dot"></span>
            </div>
        `;
        messagesEl.appendChild(el);
        scrollToBottom();
        return el;
    }

    function clearChat() {
        messagesEl.innerHTML = `
            <div class="ai-empty-state" id="ai-empty-state">
                <div class="ai-empty-icon"><i class="fas fa-robot" style="font-size:36px;color:#A855F7;"></i></div>
                <div class="ai-empty-title">Hi! I'm Hydraz AI</div>
                <div class="ai-empty-subtitle">Ask me anything about Hydraz Network — server IP, ranks, rules, or store purchases.</div>
                <div class="ai-suggestions">
                    <button class="ai-suggestion" data-q="What is the server IP?">Server IP?</button>
                    <button class="ai-suggestion" data-q="How do I buy a rank?">How to buy a rank?</button>
                    <button class="ai-suggestion" data-q="What are the server rules?">Server Rules?</button>
                    <button class="ai-suggestion" data-q="Where is Discord?">Discord Link?</button>
                </div>
            </div>
        `;
        // Re-bind only suggestion buttons
        document.querySelectorAll('.ai-suggestion').forEach(btn => {
            btn.addEventListener('click', () => {
                inputEl.value = btn.dataset.q;
                sendMessage();
            });
        });
        state.hasMessages = false;
        messagesEl.classList.remove('has-messages');
        scrollToBottom();
    }

    function scrollToBottom() {
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    function autoResizeInput() {
        inputEl.style.height = 'auto';
        inputEl.style.height = Math.min(inputEl.scrollHeight, 100) + 'px';
    }

    function escapeHtml(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function formatMarkdown(text) {
        if (!text) return '';
        let html = escapeHtml(text);
        html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');
        html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
        html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
        html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
        html = html.replace(/\n/g, '<br>');
        return html;
    }

    // Boot when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
