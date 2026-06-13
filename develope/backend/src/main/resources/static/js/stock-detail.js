/**
 * stock-detail.js — 종목 상세 페이지 전용 로직
 * fetch API 사용 (jQuery 사용 금지)
 * Chart.js는 CDN으로 로드 (detail.html에서 script 태그로 삽입)
 */

'use strict';

const API_BASE = '/api';

let stockCode = null;
let chartRevenue = null;
let chartRoe = null;
let chartDebt = null;

/* ── DOM ready ── */
document.addEventListener('DOMContentLoaded', () => {
  stockCode = getStockCodeFromUrl();
  if (!stockCode) {
    showGlobalError('종목코드가 없습니다. 목록 페이지로 돌아가주세요.');
    return;
  }
  loadStockDetail(stockCode);
  loadFinancials(stockCode);
  initFinancialTabs();
});

/* ── URL 파라미터 추출 ── */
function getStockCodeFromUrl() {
  const params = new URLSearchParams(window.location.search);
  return params.get('code') || null;
}

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

function formatAmount(val) {
  if (val == null) return '-';
  const n = Number(val);
  if (Math.abs(n) >= 1_000_000_000_000) {
    return (n / 1_000_000_000_000).toFixed(1) + '조';
  }
  if (Math.abs(n) >= 100_000_000) {
    return (n / 100_000_000).toFixed(0) + '억';
  }
  return n.toLocaleString('ko-KR');
}

function formatRatio(val, digits = 2) {
  if (val == null) return '-';
  return Number(val).toFixed(digits);
}

function formatDate(val) {
  if (!val) return '-';
  if (typeof val === 'string' && val.length === 8) {
    return `${val.slice(0, 4)}.${val.slice(4, 6)}.${val.slice(6, 8)}`;
  }
  return val;
}

/* ── 종목 상세 API 호출 ── */
async function loadStockDetail(code) {
  try {
    const res = await fetch(`${API_BASE}/stocks/${encodeURIComponent(code)}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    renderSummaryCard(data);
    renderCompanyInfo(data);
    renderValueMetrics(data);
  } catch (err) {
    console.error('[stock-detail] loadStockDetail error:', err);
    showGlobalError(`종목 정보를 불러오지 못했습니다. (${err.message})`);
  }
}

/* ── 요약카드 렌더링 ── */
function renderSummaryCard(s) {
  setInnerHTML('stockName', escapeHtml(s.stockName ?? '-'));
  setInnerHTML('stockCode', escapeHtml(s.stockCode ?? '-'));
  setInnerHTML('stockMarket', buildMarketBadge(s.market));
  setInnerHTML('currentPrice', formatPrice(s.currentPrice) + '<span>원</span>');
  setInnerHTML('marketCap', '시가총액 ' + formatMarketCap(s.marketCap));
  document.title = `${s.stockName ?? s.stockCode} — 가치투자 종목 발굴 서비스`;

  setMetric('metricPer', s.per);
  setMetric('metricPbr', s.pbr);
  setMetric('metricRoe', s.roe, '%');
  setMetric('metricDividend', s.dividendYield, '%');
}

function setMetric(id, val, suffix = '') {
  const el = document.getElementById(id);
  if (!el) return;
  if (val == null) {
    el.textContent = '-';
    el.className = 'metric-value na';
  } else {
    el.textContent = Number(val).toFixed(2) + suffix;
    el.className = 'metric-value';
  }
}

function buildMarketBadge(market) {
  if (market === 'KOSPI') return '<span class="badge-market badge-kospi">KOSPI</span>';
  if (market === 'KOSDAQ') return '<span class="badge-market badge-kosdaq">KOSDAQ</span>';
  return market ? escapeHtml(market) : '-';
}

/* ── 기업정보 렌더링 ── */
function renderCompanyInfo(s) {
  setText('infoSector', s.sector);
  setText('infoMarket', s.market);
  setText('infoListDate', formatDate(s.listDate));
  setText('infoCeo', s.ceo);
}

/* ── 가치지표 렌더링 ── */
function renderValueMetrics(s) {
  setText('valueEps', s.eps != null ? formatPrice(s.eps) + '원' : null);
  setText('valueBps', s.bps != null ? formatPrice(s.bps) + '원' : null);
  setText('valueRoe', s.roe != null ? formatRatio(s.roe) + '%' : null);
  setText('valueDebtRatio', s.debtRatio != null ? formatRatio(s.debtRatio) + '%' : null);
  setText('valueOpMargin', s.operatingMargin != null ? formatRatio(s.operatingMargin) + '%' : null);
}

/* ── 재무제표 API 호출 ── */
async function loadFinancials(code) {
  const chartsMsgEl = document.getElementById('chartsSection');

  try {
    const res = await fetch(`${API_BASE}/stocks/${encodeURIComponent(code)}/financials`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const items = await res.json(); // 배열, 연도 오름차순

    if (!Array.isArray(items) || items.length === 0) {
      if (chartsMsgEl) chartsMsgEl.innerHTML = '<div class="state-empty" style="padding:16px 0">재무 데이터가 없습니다.</div>';
      return;
    }

    if (chartsMsgEl) chartsMsgEl.innerHTML = '';
    renderCharts(items);
    renderFinancialTables(items);
  } catch (err) {
    console.error('[stock-detail] loadFinancials error:', err);
    if (chartsMsgEl) chartsMsgEl.innerHTML = `<div class="state-error" style="padding:16px 0">재무 데이터를 불러오지 못했습니다. (${escapeHtml(err.message)})</div>`;
  }
}

/* ── 차트 렌더링 ── */
function renderCharts(items) {
  const chartsSection = document.getElementById('chartsSection');
  if (chartsSection) chartsSection.innerHTML = ''; // 기존 로딩 상태 제거

  const years = items.map((d) => String(d.year));
  const revenues = items.map((d) => d.revenue ?? 0);
  const operatingProfits = items.map((d) => d.operatingProfit ?? 0);
  const roes = items.map((d) => d.roe ?? null);
  const debtRatios = items.map((d) => d.debtRatio ?? null);

  // 차트1: 매출액 + 영업이익 묶음 막대
  const ctx1 = document.getElementById('chartRevenue');
  if (ctx1) {
    if (chartRevenue) chartRevenue.destroy();
    chartRevenue = new Chart(ctx1, {
      type: 'bar',
      data: {
        labels: years,
        datasets: [
          {
            label: '매출액',
            data: revenues,
            backgroundColor: 'rgba(26, 86, 219, 0.7)',
            borderRadius: 4,
          },
          {
            label: '영업이익',
            data: operatingProfits,
            backgroundColor: 'rgba(16, 185, 129, 0.7)',
            borderRadius: 4,
          },
        ],
      },
      options: buildBarOptions('금액 (원)'),
    });
  }

  // 차트2: ROE 추이 꺾은선
  const ctx2 = document.getElementById('chartRoe');
  if (ctx2) {
    if (chartRoe) chartRoe.destroy();
    chartRoe = new Chart(ctx2, {
      type: 'line',
      data: {
        labels: years,
        datasets: [
          {
            label: 'ROE (%)',
            data: roes,
            borderColor: '#ef4444',
            backgroundColor: 'rgba(239, 68, 68, 0.1)',
            tension: 0.3,
            fill: true,
            pointRadius: 4,
          },
        ],
      },
      options: buildLineOptions('ROE (%)'),
    });
  }

  // 차트3: 부채비율 추이 꺾은선
  const ctx3 = document.getElementById('chartDebt');
  if (ctx3) {
    if (chartDebt) chartDebt.destroy();
    chartDebt = new Chart(ctx3, {
      type: 'line',
      data: {
        labels: years,
        datasets: [
          {
            label: '부채비율 (%)',
            data: debtRatios,
            borderColor: '#f59e0b',
            backgroundColor: 'rgba(245, 158, 11, 0.1)',
            tension: 0.3,
            fill: true,
            pointRadius: 4,
          },
        ],
      },
      options: buildLineOptions('부채비율 (%)'),
    });
  }
}

function buildBarOptions(yLabel) {
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { font: { size: 11 } } },
      tooltip: {
        callbacks: {
          label: (ctx) => `${ctx.dataset.label}: ${Number(ctx.raw).toLocaleString('ko-KR')}원`,
        },
      },
    },
    scales: {
      y: {
        ticks: {
          callback: (val) => {
            if (Math.abs(val) >= 1_000_000_000_000) return (val / 1_000_000_000_000).toFixed(1) + '조';
            if (Math.abs(val) >= 100_000_000) return (val / 100_000_000).toFixed(0) + '억';
            return val.toLocaleString('ko-KR');
          },
          font: { size: 10 },
        },
        grid: { color: '#f3f4f6' },
      },
      x: { ticks: { font: { size: 11 } }, grid: { display: false } },
    },
  };
}

function buildLineOptions(yLabel) {
  return {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'bottom', labels: { font: { size: 11 } } },
      tooltip: {
        callbacks: {
          label: (ctx) => `${ctx.dataset.label}: ${Number(ctx.raw).toFixed(2)}%`,
        },
      },
    },
    scales: {
      y: {
        ticks: {
          callback: (val) => val.toFixed(1) + '%',
          font: { size: 10 },
        },
        grid: { color: '#f3f4f6' },
      },
      x: { ticks: { font: { size: 11 } }, grid: { display: false } },
    },
  };
}

/* ── 재무 테이블 렌더링 ── */
function renderFinancialTables(items) {
  renderIncomeTable(items);
  renderBalanceTable(items);
  renderInvestTable(items);
}

function renderIncomeTable(items) {
  const tbody = document.getElementById('incomeTableBody');
  if (!tbody) return;
  tbody.innerHTML = items.map((d) => `
    <tr>
      <td>${d.year}</td>
      <td>${formatAmount(d.revenue)}</td>
      <td>${formatAmount(d.operatingProfit)}</td>
      <td>${formatRatio(d.operatingMargin)}%</td>
      <td>${formatAmount(d.netIncome)}</td>
    </tr>
  `).join('');
}

function renderBalanceTable(items) {
  const tbody = document.getElementById('balanceTableBody');
  if (!tbody) return;
  tbody.innerHTML = items.map((d) => `
    <tr>
      <td>${d.year}</td>
      <td>${formatAmount(d.totalAssets)}</td>
      <td>${formatAmount(d.totalLiabilities)}</td>
      <td>${formatAmount(d.totalEquity)}</td>
      <td>${formatRatio(d.debtRatio)}%</td>
    </tr>
  `).join('');
}

function renderInvestTable(items) {
  const tbody = document.getElementById('investTableBody');
  if (!tbody) return;
  tbody.innerHTML = items.map((d) => `
    <tr>
      <td>${d.year}</td>
      <td>${formatPrice(d.eps)}</td>
      <td>${formatPrice(d.bps)}</td>
      <td>${formatRatio(d.per)}</td>
      <td>${formatRatio(d.pbr)}</td>
      <td>${formatRatio(d.roe)}%</td>
    </tr>
  `).join('');
}

/* ── 재무 탭 전환 ── */
function initFinancialTabs() {
  document.querySelectorAll('.fin-tab-btn').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.fin-tab-btn').forEach((b) => b.classList.remove('active'));
      document.querySelectorAll('.fin-tab-content').forEach((c) => c.classList.remove('active'));
      btn.classList.add('active');
      const target = document.getElementById(btn.dataset.tab);
      if (target) target.classList.add('active');
    });
  });
}

/* ── 헬퍼 ── */
function setInnerHTML(id, html) {
  const el = document.getElementById(id);
  if (el) el.innerHTML = html;
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val != null ? val : '-';
}

function showGlobalError(msg) {
  const main = document.querySelector('.main');
  if (main) {
    main.innerHTML = `<div class="state-error" style="padding:64px 0; text-align:center;">${escapeHtml(msg)}</div>`;
  }
}

function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
