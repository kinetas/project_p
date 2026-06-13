/**
 * auth.js — 인증 페이지 공통 로직
 * fetch API 사용 (jQuery 사용 금지)
 * login.html 및 register.html에서 공통 사용
 */

'use strict';

const API_BASE = '/api';

/* ── DOM ready ── */
document.addEventListener('DOMContentLoaded', () => {
  const page = document.body.dataset.page;
  if (page === 'login') initLoginPage();
  else if (page === 'register') initRegisterPage();
});

/* ══════════════════════════════
   로그인 페이지
══════════════════════════════ */
function initLoginPage() {
  const form = document.getElementById('loginForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert();

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const submitBtn = document.getElementById('submitBtn');

    if (!email || !password) {
      showAlert('error', '이메일과 비밀번호를 입력해주세요.');
      return;
    }

    setSubmitLoading(submitBtn, true);

    try {
      const res = await fetch(`${API_BASE}/users/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
        credentials: 'include',
      });

      if (res.ok) {
        const data = await res.json().catch(() => ({}));
        if (data.token) {
          sessionStorage.setItem('token', data.token);
        }
        showAlert('success', '로그인 성공! 이동 중...');
        setTimeout(() => { window.location.href = 'index.html'; }, 500);
      } else {
        const err = await res.json().catch(() => ({}));
        showAlert('error', err.message || '이메일 또는 비밀번호가 올바르지 않습니다.');
      }
    } catch (err) {
      console.error('[auth] login error:', err);
      showAlert('error', '서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setSubmitLoading(submitBtn, false);
    }
  });
}

/* ══════════════════════════════
   회원가입 페이지
══════════════════════════════ */
function initRegisterPage() {
  const form = document.getElementById('registerForm');
  if (!form) return;

  const passwordInput = document.getElementById('password');
  const passwordConfirmInput = document.getElementById('passwordConfirm');

  /* 실시간 비밀번호 유효성 검사 */
  if (passwordInput) {
    passwordInput.addEventListener('input', () => validatePasswordField(passwordInput));
  }

  if (passwordConfirmInput) {
    passwordConfirmInput.addEventListener('input', () =>
      validatePasswordConfirmField(passwordInput, passwordConfirmInput)
    );
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert();

    const email = document.getElementById('email').value.trim();
    const nickname = document.getElementById('nickname').value.trim();
    const password = passwordInput ? passwordInput.value : '';
    const passwordConfirm = passwordConfirmInput ? passwordConfirmInput.value : '';
    const submitBtn = document.getElementById('submitBtn');

    /* 클라이언트 유효성 검사 */
    let valid = true;
    if (!email) { showFieldError('emailError', '이메일을 입력해주세요.'); valid = false; }
    else if (!isValidEmail(email)) { showFieldError('emailError', '올바른 이메일 형식이 아닙니다.'); valid = false; }
    else { clearFieldError('emailError', document.getElementById('email')); }

    if (!nickname) { showFieldError('nicknameError', '닉네임을 입력해주세요.'); valid = false; }
    else { clearFieldError('nicknameError', document.getElementById('nickname')); }

    if (!validatePasswordField(passwordInput)) valid = false;
    if (!validatePasswordConfirmField(passwordInput, passwordConfirmInput)) valid = false;

    if (!valid) return;

    setSubmitLoading(submitBtn, true);

    try {
      const res = await fetch(`${API_BASE}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, nickname, password }),
        credentials: 'include',
      });

      if (res.ok || res.status === 201) {
        showAlert('success', '회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
        setTimeout(() => { window.location.href = 'login.html'; }, 1000);
      } else {
        const err = await res.json().catch(() => ({}));
        const msg = err.message || '회원가입에 실패했습니다.';
        if (msg.toLowerCase().includes('email') || msg.includes('이메일')) {
          showFieldError('emailError', msg);
          document.getElementById('email')?.classList.add('error');
        } else {
          showAlert('error', msg);
        }
      }
    } catch (err) {
      console.error('[auth] register error:', err);
      showAlert('error', '서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
    } finally {
      setSubmitLoading(submitBtn, false);
    }
  });
}

/* ── 유효성 검사 ── */
function validatePasswordField(input) {
  if (!input) return true;
  if (input.value.length < 8) {
    showFieldError('passwordError', '비밀번호는 8자 이상이어야 합니다.');
    input.classList.add('error');
    return false;
  }
  clearFieldError('passwordError', input);
  return true;
}

function validatePasswordConfirmField(passwordInput, confirmInput) {
  if (!confirmInput) return true;
  if (!passwordInput || confirmInput.value !== passwordInput.value) {
    showFieldError('passwordConfirmError', '비밀번호가 일치하지 않습니다.');
    confirmInput.classList.add('error');
    return false;
  }
  clearFieldError('passwordConfirmError', confirmInput);
  return true;
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/* ── UI 헬퍼 ── */
function showAlert(type, message) {
  const alert = document.getElementById('alertBox');
  if (!alert) return;
  alert.textContent = message;
  alert.className = `alert alert-${type} visible`;
}

function clearAlert() {
  const alert = document.getElementById('alertBox');
  if (alert) { alert.textContent = ''; alert.className = 'alert'; }
}

function showFieldError(errorId, message) {
  const el = document.getElementById(errorId);
  if (el) { el.textContent = message; el.classList.add('visible'); }
}

function clearFieldError(errorId, input) {
  const el = document.getElementById(errorId);
  if (el) { el.textContent = ''; el.classList.remove('visible'); }
  if (input) input.classList.remove('error');
}

function setSubmitLoading(btn, loading) {
  if (!btn) return;
  btn.disabled = loading;
  btn.textContent = loading ? '처리 중...' : btn.dataset.label || '확인';
}
