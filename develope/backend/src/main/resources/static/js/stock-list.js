/**
 * stock-list.js — 종목 목록 페이지 전용 로직
 * fetch API 사용 (jQuery 사용 금지)
 */

'use strict';

/* ── 상수 ── */
const API_BASE = '/api';
const PAGE_SIZE = 20;

/* ── 상태 ── */
let currentPage = 0;
let totalPages = 0;
let totalElements = 0;
let sortField = '';
let sortDir = 'asc';
let currentFilters = {};

/* ── DOM ready ── */
document.addEventListener('DOMContentLoaded', () => {
  initSearchBar();
  initFilterBar();
  initTop10Tabs();
  initTableSort();
  loadStockList({ page: 0 });
  loadTop10('value');
  updateAuthHeader();
});

/* ── 숫자 포맷 유틸리티 ── */
function formatPrice(val) {
  if (val == null) return '-';
  return Number(val).toLocaleString('ko-KR');
}

function formatMarketCap(val) {
  if (val == null) return '-';
  const trillion = Math.floor(val / 1_000_000_000_000);
  const hundred = Math.floor((val % 1_000_000_000_000) / 100_000_000);
  if (trillion > 0) {
    return hundred > 0 ? `${trillion}조 ${hundred}억` : `${trillion}조`;
  }
  return `${hundred}억`;
}

function formatRatio(val, digits = 2) {
  if (val == null) return '-';
  return Number(val).toFixed(digits);
}

/* ── 인증 상태 헤더 반영 ── */
function updateAuthHeader() {
  const nav = document.getElementById('headerNav');
  if (!nav) return;
  const token = localStorage.getItem('token') || sessionStorage.getItem('token');
  if (token) {
    nav.innerHTML = `
      <span id="welcomeMsg" style="color:#6b7280;font-size:13px;"></span>
      <a href="#" id="logoutBtn">로그아웃</a>
    `;
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
  }
}

async function handleLogout(e) {
  e.preventDefault();
  try {
    await fetch(`${API_BASE}/users/logout`, { method: 'POST' });
  } catch (_) { /* ignore */ }
  localStorage.removeItem('token');
  sessionStorage.removeItem('token');
  window.location.reload();
}

/* ── 검색바 ── */
function initSearchBar() {
  const searchInput = document.getElementById('searchInput');
  const searchBtn = document.getElementById('searchBtn');
  if (!searchInput || !searchBtn) return;

  searchBtn.addEventListener('click', () => {
    currentFilters.keyword = searchInput.value.trim() || undefined;
    currentPage = 0;
    loadStockList({ page: 0 });
  });

  searchInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') searchBtn.click();
  });
}

/* ── 필터바 ── */
function initFilterBar() {
  const filterBtn = document.getElementById('filterBtn');
  const resetBtn = document.getElementById('resetBtn');
  if (!filterBtn) return;

  filterBtn.addEventListener('click', applyFilters);
  if (resetBtn) resetBtn.addEventListener('click', resetFilters);
}

function applyFilters() {
  const market = document.getElementById('filterMarket')?.value;
  const perMin = document.getElementById('filterPerMin')?.value;
  const perMax = document.getElementById('filterPerMax')?.value;
  const roeMin = document.getElementById('filterRoeMin')?.value;
  const debtMax = document.getElementById('filterDebtMax')?.value;

  currentFilters = {
    ...currentFilters,
    market: market && market !== 'ALL' ? market : undefined,
    minPer: perMin || undefined,
    maxPer: perMax || undefined,
    minRoe: roeMin || undefined,
    maxDebtRatio: debtMax || undefined,
  };

  currentPage = 0;
  loadStockList({ page: 0 });
}

function resetFilters() {
  currentFilters = {};
  currentPage = 0;
  sortField = '';
  sortDir = 'asc';

  const ids = ['filterMarket', 'filterPerMin', 'filterPerMax', 'filterRoeMin', 'filterDebtMax', 'searchInput'];
  ids.forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.value = el.tagName === 'SELECT' ? el.options[0]?.value : '';
  });

  document.querySelectorAll('thead th').forEach((th) => th.classList.remove('sort-asc', 'sort-desc'));
  loadStockList({ page: 0 });
}

/* ── 테이블 정렬 ── */
function initTableSort() {
  document.querySelectorAll('thead th[data-sort]').forEach((th) => {
    th.addEventListener('click', () => {
      const field = th.dataset.sort;
      if (sortField === field) {
        sortDir = sortDir === 'asc' ? 'desc' : 'asc';
      } else {
        sortField = field;
        sortDir = 'asc';
      }
      document.querySelectorAll('thead th').forEach((t) => t.classList.remove('sort-asc', 'sort-desc'));
      th.classList.add(sortDir === 'asc' ? 'sort-asc' : 'sort-desc');
      currentPage = 0;
      loadStockList({ page: 0 });
    });
  });
}

/* ── 종목 목록 API 호출 ── */
async function loadStockList({ page = 0 } = {}) {
  const tbody = document.getElementById('stockTableBody');
  const countEl = document.getElementById('totalCount');
  if (!tbody) return;

  showTableState(tbody, 'loading');

  const params = new URLSearchParams();
  if (currentFilters.keyword) params.set('keyword', currentFilters.keyword);
  if (currentFilters.market) params.set('market', currentFilters.market);
  if (currentFilters.minPer) params.set('minPer', currentFilters.minPer);
  if (currentFilters.maxPer) params.set('maxPer', currentFilters.maxPer);
  if (currentFilters.minRoe) params.set('minRoe', currentFilters.minRoe);
  if (currentFilters.maxDebtRatio) params.set('maxDebtRatio', currentFilters.maxDebtRatio);
  params.set('page', page);
  params.set('size', PAGE_SIZE);
  if (sortField) params.set('sort', `${sortField},${sortDir}`);

  try {
    const res = await fetch(`${API_BASE}/stocks?${params.toString()}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();

    /* 응답: { content: [...], totalElements, totalPages, number } */
    const items = data.content ?? data;
    totalElements = data.totalElements ?? items.length;
    totalPages = data.totalPages ?? 1;
    currentPage = data.number ?? page;

    if (countEl) countEl.textContent = `총 ${totalElements.toLocaleString()}개 종목`;

    if (items.length === 0) {
      showTableState(tbody, 'empty');
      renderPagination(0, 0);
      return;
    }

    tbody.innerHTML = items.map((s) => renderStockRow(s)).join('');
    renderPagination(currentPage, totalPages);
  } catch (err) {
    console.error('[stock-list] loadStockList error:', err);
    showTableState(tbody, 'error', err.message);
  }
}

function renderStockRow(s) {
  const marketBadge = s.market === 'KOSPI'
    ? '<span class="badge-market badge-kospi">KOSPI</span>'
    : '<span class="badge-market badge-kosdaq">KOSDAQ</span>';

  return `
    <tr>
      <td>
        <a class="stock-name-link" href="detail.html?code=${encodeURIComponent(s.stockCode)}">${escapeHtml(s.stockName)}</a>
        <span class="stock-code">${escapeHtml(s.stockCode)}</span>
      </td>
      <td>${marketBadge}</td>
      <td>${formatPrice(s.currentPrice)}</td>
      <td>${formatMarketCap(s.marketCap)}</td>
      <td class="${getRatioClass(s.per, 0, 15)}">${formatRatio(s.per)}</td>
      <td class="${getRatioClass(s.pbr, 0, 1)}">${formatRatio(s.pbr)}</td>
      <td class="${getPositiveClass(s.roe)}">${formatRatio(s.roe)}</td>
      <td class="${getRatioClass(s.debtRatio, 0, 100)}">${formatRatio(s.debtRatio)}</td>
    </tr>
  `;
}

function getRatioClass(val, low, high) {
  if (val == null) return 'val-na';
  return val <= high ? 'val-positive' : '';
}

function getPositiveClass(val) {
  if (val == null) return 'val-na';
  return val >= 10 ? 'val-positive' : '';
}

function showTableState(tbody, state, msg = '') {
  const colSpan = 8;
  if (state === 'loading') {
    tbody.innerHTML = `<tr><td colspan="${colSpan}"><div class="state-loading">데이터를 불러오는 중...</div></td></tr>`;
  } else if (state === 'empty') {
    tbody.innerHTML = `<tr><td colspan="${colSpan}"><div class="state-empty">조건에 맞는 종목이 없습니다.</div></td></tr>`;
  } else if (state === 'error') {
    tbody.innerHTML = `<tr><td colspan="${colSpan}"><div class="state-error">데이터를 불러오지 못했습니다. (${escapeHtml(msg)})</div></td></tr>`;
  }
}

/* ── 페이지네이션 ── */
function renderPagination(page, total) {
  const container = document.getElementById('pagination');
  if (!container) return;
  if (total <= 1) { container.innerHTML = ''; return; }

  const maxPages = 5;
  let start = Math.max(0, page - Math.floor(maxPages / 2));
  let end = Math.min(total - 1, start + maxPages - 1);
  if (end - start < maxPages - 1) start = Math.max(0, end - maxPages + 1);

  const buttons = [];

  // 이전
  buttons.push(
    `<button class="page-btn" ${page === 0 ? 'disabled' : ''} data-page="${page - 1}">&lsaquo;</button>`
  );

  for (let i = start; i <= end; i++) {
    buttons.push(
      `<button class="page-btn ${i === page ? 'active' : ''}" data-page="${i}">${i + 1}</button>`
    );
  }

  // 다음
  buttons.push(
    `<button class="page-btn" ${page >= total - 1 ? 'disabled' : ''} data-page="${page + 1}">&rsaquo;</button>`
  );

  container.innerHTML = buttons.join('');
  container.querySelectorAll('.page-btn:not(:disabled)').forEach((btn) => {
    btn.addEventListener('click', () => {
      const p = parseInt(btn.dataset.page, 10);
      loadStockList({ page: p });
      window.scrollTo({ top: 0, behavior: 'smooth' });
    });
  });
}

/* ── TOP10 탭 ── */
function initTop10Tabs() {
  document.querySelectorAll('.tab-btn[data-top10]').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.tab-btn[data-top10]').forEach((b) => b.classList.remove('active'));
      document.querySelectorAll('.tab-content[data-top10]').forEach((c) => c.classList.remove('active'));
      btn.classList.add('active');
      const type = btn.dataset.top10;
      const content = document.querySelector(`.tab-content[data-top10="${type}"]`);
      if (content) content.classList.add('active');
      loadTop10(type);
    });
  });
}

async function loadTop10(type) {
  const container = document.querySelector(`.tab-content[data-top10="${type}"]`);
  if (!container) return;

  container.innerHTML = '<div class="state-loading" style="padding:24px 0">로딩 중...</div>';

  try {
    const res = await fetch(`${API_BASE}/stocks/top10?type=${encodeURIComponent(type)}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const items = await res.json();

    if (!items || items.length === 0) {
      container.innerHTML = '<div class="state-empty" style="padding:24px 0">데이터가 없습니다.</div>';
      return;
    }

    const columns = getTop10Columns(type);
    container.innerHTML = `
      <table class="mini-table">
        <thead>
          <tr>
            <th style="text-align:left">순위</th>
            <th style="text-align:left">종목명</th>
            <th>현재가</th>
            ${columns.map((c) => `<th>${c.label}</th>`).join('')}
          </tr>
        </thead>
        <tbody>
          ${items.map((s, idx) => `
            <tr>
              <td style="text-align:left">
                <span class="rank-badge ${idx < 3 ? 'top3' : ''}">${idx + 1}</span>
              </td>
              <td style="text-align:left">
                <a class="stock-name-link" href="detail.html?code=${encodeURIComponent(s.stockCode)}">${escapeHtml(s.stockName)}</a>
              </td>
              <td>${formatPrice(s.currentPrice)}</td>
              ${columns.map((c) => `<td>${formatRatio(s[c.field])}</td>`).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  } catch (err) {
    console.error(`[stock-list] loadTop10(${type}) error:`, err);
    container.innerHTML = `<div class="state-error" style="padding:24px 0">데이터를 불러오지 못했습니다.</div>`;
  }
}

function getTop10Columns(type) {
  switch (type) {
    case 'value':
      return [
        { label: 'PER', field: 'per' },
        { label: 'PBR', field: 'pbr' },
        { label: 'ROE(%)', field: 'roe' },
      ];
    case 'lowPer':
      return [
        { label: 'PER', field: 'per' },
        { label: 'PBR', field: 'pbr' },
      ];
    case 'highRoe':
      return [
        { label: 'ROE(%)', field: 'roe' },
        { label: 'PER', field: 'per' },
      ];
    default:
      return [{ label: 'PER', field: 'per' }];
  }
}

/* ── XSS 방어 ── */
function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
