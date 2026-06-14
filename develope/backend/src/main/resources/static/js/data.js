/** @type {import('./data.js')} mock stock data from Figma design */
const MOCK_STOCKS = [
  {
    code: '005930', name: '삼성전자', market: 'KOSPI',
    price: 71800, changeRate: 2.14, changeAmount: 1500,
    marketCap: 42850, per: 12.5, pbr: 1.2, roe: 9.8, dividendYield: 2.5,
    shares: '5,969,783천주', eps: 5744, bps: 59842, debtRatio: 45.2,
    operatingProfit: 35000, operatingMargin: 17.5,
    sector: '반도체', listedDate: '1975-06-11', ceo: '전영현',
    revenueHistory: [302231, 279600, 244000, 236800, 200000],
    operatingHistory: [43376, 35994, 27711, 29025, 35000],
    netIncomeHistory: [26408, 26410, 21739, 23891, 28500],
    assetHistory: [426901, 392290, 378686, 352255, 339000],
    debtHistory: [135032, 102083, 87668, 74048, 68000],
    equityHistory: [291869, 290207, 311018, 278207, 271000],
    roeHistory: [9.0, 9.1, 7.0, 8.6, 9.8],
    debtRatioHistory: [46.3, 35.2, 28.2, 26.6, 25.1],
    perHistory: [14.2, 13.8, 15.1, 13.0, 12.5],
    pbrHistory: [1.4, 1.3, 1.25, 1.22, 1.2],
    epsHistory: [4420, 4800, 5100, 5520, 5744],
    bpsHistory: [48900, 52100, 54800, 57200, 59842],
  },
  {
    code: '000660', name: 'SK하이닉스', market: 'KOSPI',
    price: 142500, changeRate: -1.38, changeAmount: -1990,
    marketCap: 10380, per: 8.3, pbr: 1.5, roe: 18.2, dividendYield: 1.2,
    shares: '728,000천주', eps: 17169, bps: 95000, debtRatio: 38.5,
    operatingProfit: 28000, operatingMargin: 32.1,
    sector: '반도체', listedDate: '1996-12-26', ceo: '곽노정',
    revenueHistory: [42998, 44622, 32776, 36600, 42000],
    operatingHistory: [12814, 6786, -7700, 15000, 28000],
    netIncomeHistory: [21737, 22000, -9100, 12000, 22000],
    assetHistory: [103000, 98000, 95000, 102000, 110000],
    debtHistory: [42000, 38000, 35000, 36000, 38000],
    equityHistory: [61000, 60000, 60000, 66000, 72000],
    roeHistory: [35.6, 36.7, -15.2, 18.2, 18.2],
    debtRatioHistory: [68.9, 63.3, 58.3, 54.5, 52.8],
    perHistory: [12.0, 10.5, 18.0, 9.5, 8.3],
    pbrHistory: [2.1, 1.9, 1.7, 1.6, 1.5],
    epsHistory: [8500, 12000, -4500, 14000, 17169],
    bpsHistory: [72000, 78000, 82000, 89000, 95000],
  },
  {
    code: '035420', name: 'NAVER', market: 'KOSPI',
    price: 185000, changeRate: 3.67, changeAmount: 6550,
    marketCap: 3030, per: 15.8, pbr: 1.8, roe: 11.4, dividendYield: 0.8,
    shares: '164,000천주', eps: 11709, bps: 102778, debtRatio: 28.3,
    operatingProfit: 8500, operatingMargin: 18.2,
    sector: '인터넷', listedDate: '2008-11-28', ceo: '최수연',
    revenueHistory: [6786, 8247, 9670, 10200, 9800],
    operatingHistory: [1200, 1500, 1800, 2100, 8500],
    netIncomeHistory: [800, 950, 1100, 1300, 5200],
    assetHistory: [28000, 30000, 32000, 34000, 36000],
    debtHistory: [8000, 7500, 7000, 6800, 6500],
    equityHistory: [20000, 22500, 25000, 27200, 29500],
    roeHistory: [4.0, 4.2, 4.4, 4.8, 11.4],
    debtRatioHistory: [40.0, 33.3, 28.0, 25.0, 22.0],
    perHistory: [45.0, 38.0, 32.0, 22.0, 15.8],
    pbrHistory: [3.5, 3.0, 2.5, 2.0, 1.8],
    epsHistory: [4900, 5200, 5500, 6800, 11709],
    bpsHistory: [85000, 90000, 95000, 98000, 102778],
  },
  {
    code: '005380', name: '현대차', market: 'KOSPI',
    price: 235000, changeRate: 1.08, changeAmount: 2510,
    marketCap: 5020, per: 5.2, pbr: 0.6, roe: 11.5, dividendYield: 3.8,
    shares: '214,000천주', eps: 45192, bps: 391667, debtRatio: 52.1,
    operatingProfit: 42000, operatingMargin: 8.5,
    sector: '자동차', listedDate: '1974-07-01', ceo: '장재훈',
    revenueHistory: [117610, 142150, 162660, 175000, 168000],
    operatingHistory: [5200, 8800, 12000, 35000, 42000],
    netIncomeHistory: [3100, 5600, 9800, 28000, 35000],
    assetHistory: [210000, 220000, 230000, 245000, 250000],
    debtHistory: [110000, 105000, 100000, 95000, 90000],
    equityHistory: [100000, 115000, 130000, 150000, 160000],
    roeHistory: [3.1, 4.9, 7.5, 18.7, 11.5],
    debtRatioHistory: [110.0, 91.3, 76.9, 63.3, 56.3],
    perHistory: [8.5, 7.2, 6.5, 5.8, 5.2],
    pbrHistory: [0.9, 0.8, 0.7, 0.65, 0.6],
    epsHistory: [14500, 22000, 32000, 40000, 45192],
    bpsHistory: [280000, 310000, 340000, 370000, 391667],
  },
  {
    code: '035720', name: '카카오', market: 'KOSPI',
    price: 41200, changeRate: -2.37, changeAmount: -1000,
    marketCap: 1790, per: 22.8, pbr: 1.4, roe: 6.1, dividendYield: 0.5,
    shares: '435,000천주', eps: 1807, bps: 29429, debtRatio: 35.8,
    operatingProfit: 3200, operatingMargin: 12.3,
    sector: '인터넷', listedDate: '2017-08-10', ceo: '정신아',
    revenueHistory: [5240, 7167, 7200, 6800, 6500],
    operatingHistory: [800, 1200, 900, 600, 3200],
    netIncomeHistory: [600, 900, 500, 300, 1800],
    assetHistory: [45000, 48000, 50000, 52000, 54000],
    debtHistory: [18000, 17000, 16000, 15000, 14000],
    equityHistory: [27000, 31000, 34000, 37000, 40000],
    roeHistory: [2.2, 2.9, 1.5, 0.8, 6.1],
    debtRatioHistory: [66.7, 54.8, 47.1, 40.5, 35.0],
    perHistory: [55.0, 45.0, 60.0, 35.0, 22.8],
    pbrHistory: [2.5, 2.2, 1.9, 1.6, 1.4],
    epsHistory: [750, 920, 680, 1200, 1807],
    bpsHistory: [22000, 24000, 26000, 28000, 29429],
  },
  {
    code: '051910', name: 'LG화학', market: 'KOSPI',
    price: 358000, changeRate: 0.56, changeAmount: 2000,
    marketCap: 2530, per: 18.5, pbr: 0.9, roe: 4.9, dividendYield: 1.4,
    shares: '71,000천주', eps: 19351, bps: 397778, debtRatio: 42.3,
    operatingProfit: 8500, operatingMargin: 6.8,
    sector: '화학', listedDate: '2001-09-05', ceo: '신학철',
    revenueHistory: [42665, 51000, 55000, 48000, 45000],
    operatingHistory: [3500, 4200, 3800, 3200, 8500],
    netIncomeHistory: [2800, 3200, 2500, 2000, 5500],
    assetHistory: [65000, 68000, 70000, 72000, 75000],
    debtHistory: [30000, 28000, 26000, 25000, 24000],
    equityHistory: [35000, 40000, 44000, 47000, 51000],
    roeHistory: [8.0, 8.0, 5.7, 4.3, 4.9],
    debtRatioHistory: [85.7, 70.0, 59.1, 53.2, 47.1],
    perHistory: [25.0, 22.0, 20.0, 19.0, 18.5],
    pbrHistory: [1.3, 1.2, 1.1, 1.0, 0.9],
    epsHistory: [14500, 16000, 17500, 18500, 19351],
    bpsHistory: [350000, 370000, 385000, 392000, 397778],
  },
  {
    code: '006400', name: '삼성SDI', market: 'KOSPI',
    price: 425000, changeRate: 4.18, changeAmount: 17050,
    marketCap: 2980, per: 35.4, pbr: 2.1, roe: 5.9, dividendYield: 0.7,
    shares: '70,000천주', eps: 12006, bps: 202381, debtRatio: 48.6,
    operatingProfit: 6200, operatingMargin: 9.2,
    sector: '2차전지', listedDate: '2000-07-05', ceo: '최주선',
    revenueHistory: [13400, 18000, 22000, 25000, 28000],
    operatingHistory: [800, 1200, 1800, 3500, 6200],
    netIncomeHistory: [500, 800, 1200, 2500, 4200],
    assetHistory: [35000, 38000, 42000, 48000, 52000],
    debtHistory: [18000, 17000, 16000, 18000, 20000],
    equityHistory: [17000, 21000, 26000, 30000, 32000],
    roeHistory: [2.9, 3.8, 4.6, 8.3, 5.9],
    debtRatioHistory: [105.9, 81.0, 61.5, 60.0, 62.5],
    perHistory: [80.0, 65.0, 50.0, 42.0, 35.4],
    pbrHistory: [3.5, 3.0, 2.6, 2.3, 2.1],
    epsHistory: [3500, 4800, 6200, 8500, 12006],
    bpsHistory: [120000, 145000, 168000, 185000, 202381],
  },
  {
    code: '207940', name: '삼성바이오로직스', market: 'KOSPI',
    price: 892000, changeRate: 1.25, changeAmount: 11000,
    marketCap: 6270, per: 42.5, pbr: 5.8, roe: 13.6, dividendYield: 0.3,
    shares: '70,000천주', eps: 20988, bps: 153793, debtRatio: 22.1,
    operatingProfit: 12000, operatingMargin: 28.5,
    sector: '바이오', listedDate: '2016-11-10', ceo: '임존',
    revenueHistory: [12000, 15000, 18000, 22000, 28000],
    operatingHistory: [2500, 3200, 4500, 8000, 12000],
    netIncomeHistory: [1800, 2400, 3500, 6500, 9800],
    assetHistory: [45000, 52000, 60000, 72000, 85000],
    debtHistory: [8000, 7500, 7000, 6500, 6000],
    equityHistory: [37000, 44500, 53000, 65500, 79000],
    roeHistory: [4.9, 5.4, 6.6, 9.9, 13.6],
    debtRatioHistory: [21.6, 16.9, 13.2, 9.9, 7.6],
    perHistory: [120.0, 95.0, 75.0, 55.0, 42.5],
    pbrHistory: [8.5, 7.5, 6.8, 6.2, 5.8],
    epsHistory: [7500, 9800, 12500, 16200, 20988],
    bpsHistory: [95000, 110000, 125000, 140000, 153793],
  },
];

const MARKET_INDICES = [
  { name: 'KOSPI', value: '2,678.22', changeRate: 1.24, changeAmount: 32.84 },
  { name: 'KOSDAQ', value: '758.45', changeRate: -0.68, changeAmount: -5.19 },
  { name: 'USD/KRW', value: '1,328.5', changeRate: 0.15, changeAmount: 2.00 },
  { name: 'JPY/KRW', value: '8.95', changeRate: -0.22, changeAmount: -0.02 },
];

const YEARS = [2021, 2022, 2023, 2024, 2025];

function getStockByCode(code) {
  return MOCK_STOCKS.find((s) => s.code === code) || null;
}

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

const API_BASE = "";

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
