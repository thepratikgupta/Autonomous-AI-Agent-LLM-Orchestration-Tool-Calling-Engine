/* =========================================================================
   AI AGENT CONSOLE — Frontend
   =========================================================================
   This file connects to your Spring Boot backend. The endpoint paths below
   are CONVENTIONAL — adjust them to match your actual @RestController routes.

   Look at the CONFIG block (right below) and the ENDPOINTS object.
   ========================================================================= */

// ============ CONFIG ============
const CONFIG = {
    // Your backend base URL. Change in Settings (gear icon) at runtime.
    defaultApiBase: 'http://localhost:8080/api',

    // localStorage keys
    tokenKey: 'aiagent_token',
    userKey: 'aiagent_user',
    historyKey: 'aiagent_history',
    apiBaseKey: 'aiagent_api_base',
};

// >>> ADJUST THESE PATHS to match YOUR Spring Boot controllers <<<
const ENDPOINTS = {
    login:    '/auth/login',       // POST {username, password} -> {token, user}
    register: '/auth/signup',    // POST {name, email, password} -> {token, user}
    me:       '/auth/me',          // GET  (with Bearer token) -> {name, email, ...}
    logout:   '/auth/logout',      // POST (optional)
    // OAuth2 — Spring Security's default Google flow:
    oauthGoogle: '/oauth2/authorization/google', // GET (browser redirect)

    // Your AI prompt endpoint — adjust to your controller:
    prompt:   '/agent/chat',     // POST {prompt} -> {response, command?, output?}
    history:  '/agent/history',    // GET  -> [{id, prompt, response, createdAt}]
};

// ============ STATE ============
let state = {
    apiBase: localStorage.getItem(CONFIG.apiBaseKey) || CONFIG.defaultApiBase,
    token: localStorage.getItem(CONFIG.tokenKey),
    user: JSON.parse(localStorage.getItem(CONFIG.userKey) || 'null'),
    history: JSON.parse(localStorage.getItem(CONFIG.historyKey) || '[]'),
    currentChat: [],
};

// ============ DOM ============
const $ = (sel) => document.querySelector(sel);
const authScreen = $('#auth-screen');
const appShell = $('#app-shell');
const chatArea = $('#chat-area');
const promptInput = $('#prompt-input');
const sendBtn = $('#send-btn');
const historyList = $('#history-list');

// ============ INIT ============
function init() {
    $('#api-url-input').value = state.apiBase;
    $('#api-url-display').textContent = state.apiBase;

    // OAuth2 callback: if URL has ?token=xxx (Spring redirects after Google login),
    // grab it, store it, and fetch user info.
    const url = new URL(window.location.href);
    const oauthToken = url.searchParams.get('token');
    if (oauthToken) {
        saveAuth(oauthToken, null);
        window.history.replaceState({}, '', window.location.pathname);
        fetchCurrentUser().then(showApp).catch(showAuth);
        return;
    }

    if (state.token) {
        showApp();
    } else {
        showAuth();
    }
    bindEvents();
}

// ============ AUTH ============
function showAuth() { authScreen.classList.remove('hidden'); appShell.classList.add('hidden'); }
function showApp() {
    authScreen.classList.add('hidden');
    appShell.classList.remove('hidden');
    renderUser();
    renderHistory();
}

function saveAuth(token, user) {
    state.token = token;
    localStorage.setItem(CONFIG.tokenKey, token);
    if (user) {
        state.user = user;
        localStorage.setItem(CONFIG.userKey, JSON.stringify(user));
    }
}

function clearAuth() {
    state.token = null;
    state.user = null;
    localStorage.removeItem(CONFIG.tokenKey);
    localStorage.removeItem(CONFIG.userKey);
}

async function fetchCurrentUser() {
    const user = await api('GET', ENDPOINTS.me);
    state.user = user;
    localStorage.setItem(CONFIG.userKey, JSON.stringify(user));
    return user;
}

function renderUser() {
    const name = state.user?.name || state.user?.username || state.user?.email || 'User';
    const email = state.user?.email || '';
    $('#user-name').textContent = name;
    $('#user-email').textContent = email;
    $('#user-avatar').textContent = name.charAt(0).toUpperCase();
}

// ============ API HELPER ============
async function api(method, path, body) {
    const headers = { 'Content-Type': 'application/json' };
    if (state.token) headers['Authorization'] = `Bearer ${state.token}`;

    const res = await fetch(state.apiBase + path, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined,
        credentials: 'include', // sends session cookies (for OAuth2 session-based auth)
    });

    if (res.status === 401) {
        clearAuth();
        showAuth();
        throw new Error('Session expired. Please sign in again.');
    }

    const text = await res.text();
    const data = text ? safeJson(text) : null;

    if (!res.ok) {
        throw new Error(data?.message || data?.error || `Request failed (${res.status})`);
    }
    return data;
}

function safeJson(text) {
    try { return JSON.parse(text); } catch { return { message: text }; }
}

// ============ EVENT BINDINGS ============
function bindEvents() {
    // Tabs
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
            tab.classList.add('active');
            document.getElementById(`${tab.dataset.tab}-form`).classList.add('active');
            $('#auth-error').classList.remove('show');
        });
    });

    // Login
    $('#login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        try {
            const data = await api('POST', ENDPOINTS.login, {
                username: fd.get('username'),
                email: fd.get('username'), // sent both keys — backend uses one
                password: fd.get('password'),
            });
            saveAuth(data.token || data.accessToken || data.jwt, data.user || data);
            if (!state.user || !state.user.name) await fetchCurrentUser().catch(() => {});
            showApp();
        } catch (err) {
            authError(err.message);
        }
    });

    // Register
    $('#register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const fd = new FormData(e.target);
        try {
            const data = await api('POST', ENDPOINTS.register, {
                name: fd.get('name'),
                email: fd.get('email'),
                password: fd.get('password'),
            });
            saveAuth(data.token || data.accessToken || data.jwt, data.user || data);
            showApp();
        } catch (err) {
            authError(err.message);
        }
    });

    // OAuth Google — redirect the browser to Spring Security's endpoint
    $('#oauth-google').addEventListener('click', () => {
        window.location.href = state.apiBase + ENDPOINTS.oauthGoogle;
    });

    // Logout
    $('#logout-btn').addEventListener('click', async () => {
        try { await api('POST', ENDPOINTS.logout); } catch {}
        clearAuth();
        showAuth();
    });

    // Settings modal
    $('#settings-btn').addEventListener('click', () => $('#settings-modal').classList.remove('hidden'));
    $('#settings-cancel').addEventListener('click', () => $('#settings-modal').classList.add('hidden'));
    $('#settings-save').addEventListener('click', () => {
        const val = $('#api-url-input').value.trim().replace(/\/$/, '');
        if (val) {
            state.apiBase = val;
            localStorage.setItem(CONFIG.apiBaseKey, val);
            $('#api-url-display').textContent = val;
            toast('Settings saved', 'success');
        }
        $('#settings-modal').classList.add('hidden');
    });

    // New chat
    $('#new-chat').addEventListener('click', () => {
        state.currentChat = [];
        renderChat();
        $('#chat-title').textContent = 'New conversation';
    });

    // Prompt textarea auto-grow + Enter to send
    promptInput.addEventListener('input', () => {
        promptInput.style.height = 'auto';
        promptInput.style.height = Math.min(promptInput.scrollHeight, 200) + 'px';
    });
    promptInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            $('#prompt-form').requestSubmit();
        }
    });

    // Send prompt
    $('#prompt-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const text = promptInput.value.trim();
        if (!text) return;
        await sendPrompt(text);
    });

    // Suggestions
    document.querySelectorAll('.suggestion').forEach(s => {
        s.addEventListener('click', () => sendPrompt(s.textContent));
    });
}

function authError(msg) {
    const el = $('#auth-error');
    el.textContent = msg;
    el.classList.add('show');
}

// ============ CHAT ============
async function sendPrompt(text) {
    promptInput.value = '';
    promptInput.style.height = 'auto';
    sendBtn.disabled = true;

    pushMessage('user', text);
    const typingEl = pushTyping();

    try {
        const data = await api('POST', ENDPOINTS.prompt, { prompt: text });

        // Tolerant to different backend response shapes:
        const reply = data?.response || data?.reply || data?.message || data?.result || JSON.stringify(data);
        const command = data?.command || data?.executedCommand;
        const output = data?.output || data?.commandOutput;

        typingEl.remove();
        pushMessage('assistant', reply, { command, output });

        // Save to history
        const entry = {
            id: Date.now(),
            title: text.slice(0, 40),
            messages: [...state.currentChat],
        };
        state.history.unshift(entry);
        state.history = state.history.slice(0, 30);
        localStorage.setItem(CONFIG.historyKey, JSON.stringify(state.history));
        renderHistory();
        $('#chat-title').textContent = entry.title;
    } catch (err) {
        typingEl.remove();
        pushMessage('assistant', `⚠ ${err.message}`);
        toast(err.message, 'error');
    } finally {
        sendBtn.disabled = false;
        promptInput.focus();
    }
}

function pushMessage(role, text, extras = {}) {
    state.currentChat.push({ role, text, ...extras });

    // Clear empty state
    const empty = chatArea.querySelector('.empty-state');
    if (empty) empty.remove();

    const el = document.createElement('div');
    el.className = `message ${role}`;
    const tag = role === 'user' ? 'You' : 'Agent';
    let html = `<div class="bubble"><div class="role-tag">${tag}</div><div>${escapeHtml(text)}</div>`;
    if (extras.command) {
        html += `<div class="cmd-block"><div class="cmd-label">Executed command</div>$ ${escapeHtml(extras.command)}</div>`;
    }
    if (extras.output) {
        html += `<div class="cmd-block"><div class="cmd-label">Output</div>${escapeHtml(extras.output)}</div>`;
    }
    html += `</div>`;
    el.innerHTML = html;
    chatArea.appendChild(el);
    chatArea.scrollTop = chatArea.scrollHeight;
}

function pushTyping() {
    const el = document.createElement('div');
    el.className = 'message assistant';
    el.innerHTML = `<div class="bubble"><div class="role-tag">Agent</div><div class="typing"><span></span><span></span><span></span></div></div>`;
    chatArea.appendChild(el);
    chatArea.scrollTop = chatArea.scrollHeight;
    return el;
}

function renderChat() {
    chatArea.innerHTML = '';
    if (state.currentChat.length === 0) {
        chatArea.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">✦</div>
        <h3>Ask the agent anything</h3>
        <p class="muted">It can answer questions and execute commands on your system.</p>
      </div>`;
        return;
    }
    state.currentChat.forEach(m => pushMessage(m.role, m.text, m));
}

function renderHistory() {
    historyList.innerHTML = '';
    state.history.forEach(h => {
        const li = document.createElement('li');
        li.textContent = h.title;
        li.addEventListener('click', () => {
            state.currentChat = [...h.messages];
            renderChat();
            $('#chat-title').textContent = h.title;
            document.querySelectorAll('#history-list li').forEach(x => x.classList.remove('active'));
            li.classList.add('active');
        });
        historyList.appendChild(li);
    });
}

// ============ UTILS ============
function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
}

function toast(msg, type = '') {
    const el = $('#toast');
    el.textContent = msg;
    el.className = 'toast ' + type;
    setTimeout(() => el.classList.add('hidden'), 3500);
    el.classList.remove('hidden');
}

// ============ GO ============
init();
