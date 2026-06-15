const YEARS = [2021, 2022, 2023, 2024, 2025];

function formatPrice(price) {
  return price.toLocaleString('ko-KR') + '원';
}

function formatMarketCap(cap) {
  return cap.toLocaleString('ko-KR') + '억';
}

function formatChange(rate) {
  const sign = rate >= 0 ? '+' : '';
  return sign + rate.toFixed(2) + '%';
}

function changeClass(rate) {
  return rate >= 0 ? 'up' : 'down';
}

function sortStocks(stocks, key, dir) {
  return [...stocks].sort((a, b) => {
    const av = a[key];
    const bv = b[key];
    return dir === 'asc' ? av - bv : bv - av;
  });
}

function getRanking(stocks, key, limit = 8) {
  const valid = stocks.filter((s) => s[key] > 0);
  const asc = key === 'per' || key === 'pbr';
  return sortStocks(valid, key, asc ? 'asc' : 'desc').slice(0, limit);
}

// ──────────────────────────────────────────────
// API Layer
// ──────────────────────────────────────────────

const API_BASE = "http://localhost:8080";

/**
 * 백엔드 StockListResponse → 프론트 stock 객체로 변환
 * @param {Object} s - StockListResponse
 * @returns {Object}
 */
function normalizeStock(s) {
  return {
    code: s.stockCode,
    name: s.stockName,
    market: s.market || "",
    price: s.currentPrice || 0,
    changeRate: s.changeRate || 0,
    changeAmount: s.changeAmount || 0,
    marketCap: s.marketCap || 0,
    per: s.per,
    pbr: s.pbr,
    roe: s.roe,
    dividendYield: s.dividendYield || 0,
  };
}

/**
 * 백엔드 StockDetailResponse + FinancialResponse[] → 프론트 상세 stock 객체로 변환
 * @param {Object} s - StockDetailResponse
 * @param {Array}  financials - FinancialResponse[] (백엔드 응답 그대로)
 * @returns {Object}
 */
function normalizeDetail(s, financials) {
  const sorted = [...(financials || [])].sort((a, b) => a.year - b.year);
  return {
    code: s.stockCode,
    name: s.stockName,
    market: s.market || "",
    price: s.currentPrice || 0,
    changeRate: s.changeRate || 0,
    changeAmount: s.changeAmount || 0,
    marketCap: s.marketCap || 0,
    per: s.per,
    pbr: s.pbr,
    roe: s.roe,
    dividendYield: s.dividendYield || 0,
    shares: s.sharesOutstanding ? s.sharesOutstanding.toLocaleString("ko-KR") + "천주" : "-",
    eps: s.eps || 0,
    bps: s.bps || 0,
    debtRatio: s.debtRatio,
    operatingMargin: s.operatingMargin,
    sector: s.sector || "-",
    listedDate: s.listingDate || "-",
    ceo: s.ceoName || "-",
    operatingProfit: sorted.length > 0 ? sorted[sorted.length - 1].operatingProfit || 0 : 0,
    revenueHistory: sorted.map(f => f.revenue || 0),
    operatingHistory: sorted.map(f => f.operatingProfit || 0),
    netIncomeHistory: sorted.map(f => f.netIncome || 0),
    assetHistory: sorted.map(f => f.totalAssets || 0),
    debtHistory: sorted.map(f => f.totalLiabilities || 0),
    equityHistory: sorted.map(f => f.totalEquity || 0),
    roeHistory: sorted.map(f => f.roe || 0),
    debtRatioHistory: sorted.map(f => f.debtRatio || 0),
    perHistory: sorted.map(f => f.per || 0),
    pbrHistory: sorted.map(f => f.pbr || 0),
    epsHistory: sorted.map(f => f.eps || 0),
    bpsHistory: sorted.map(f => f.bps || 0),
    years: sorted.map(f => f.year),
  };
}

/**
 * GET /api/stocks — 종목 목록 조회
 * @param {Object} params - { keyword, market, minPer, maxPer, minRoe, maxDebtRatio, page, size }
 * @returns {Promise<Object[]>}
 */
async function fetchStocks(params = {}) {
  const query = new URLSearchParams(params).toString();
  const url = `${API_BASE}/api/stocks${query ? "?" + query : ""}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error(`fetchStocks failed: ${res.status}`);
  const data = await res.json();
  return data.map(normalizeStock);
}

/**
 * GET /api/stocks/top10?type={type} — TOP10 조회
 * @param {"value"|"lowPer"|"highRoe"|"lowPbr"} type
 * @returns {Promise<Object[]>}
 */
async function fetchTop10(type = "value") {
  const res = await fetch(`${API_BASE}/api/stocks/top10?type=${encodeURIComponent(type)}`);
  if (!res.ok) throw new Error(`fetchTop10 failed: ${res.status}`);
  const data = await res.json();
  return (data.stocks || []).map(normalizeStock);
}

/**
 * GET /api/stocks/{code} — 종목 상세 원본 조회 (정규화 전)
 * @param {string} code
 * @returns {Promise<Object>} StockDetailResponse
 */
async function fetchStockDetail(code) {
  const res = await fetch(`${API_BASE}/api/stocks/${encodeURIComponent(code)}`);
  if (!res.ok) throw new Error(`fetchStockDetail failed: ${res.status}`);
  return res.json();
}

/**
 * GET /api/stocks/{code}/financials — 재무제표 원본 조회 (정규화 전)
 * @param {string} code
 * @returns {Promise<Object[]>} FinancialResponse[]
 */
async function fetchFinancials(code) {
  const res = await fetch(`${API_BASE}/api/stocks/${encodeURIComponent(code)}/financials`);
  if (!res.ok) throw new Error(`fetchFinancials failed: ${res.status}`);
  return res.json();
}

/**
 * 종목 상세 + 재무제표 병렬 조회 후 정규화된 상세 객체 반환
 * @param {string} code
 * @returns {Promise<Object>}
 */
async function fetchStockFull(code) {
  const [detail, financials] = await Promise.all([
    fetchStockDetail(code),
    fetchFinancials(code),
  ]);
  return normalizeDetail(detail, financials);
}

/**
 * GET /api/market/indices — 시장 지표 조회
 * @returns {Promise<Array<{name: string, value: string, changeRate: number, changeAmount: number}>>}
 */
async function fetchMarketIndices() {
  const res = await fetch(`${API_BASE}/api/market/indices`);
  if (!res.ok) throw new Error(`fetchMarketIndices failed: ${res.status}`);
  return res.json();
}
