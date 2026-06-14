document.addEventListener('DOMContentLoaded', () => {
  initHeader('home');

  // 시장 지표
  const marketGrid = document.getElementById('marketGrid');
  marketGrid.innerHTML = MARKET_INDICES.map((idx) => {
    const cls = changeClass(idx.changeRate);
    const sign = idx.changeRate >= 0 ? '+' : '';
    return `
      <div class="market-card">
        <div class="market-name">${idx.name}</div>
        <div class="market-value">${idx.value}</div>
        <div class="market-change ${cls}">${sign}${idx.changeRate}% (${sign}${idx.changeAmount})</div>
      </div>
    `;
  }).join('');

  // 주요 종목
  const stocksGrid = document.getElementById('stocksGrid');
  stocksGrid.innerHTML = MOCK_STOCKS.map(renderStockCard).join('');
  bindStockCards(stocksGrid);

  // 랭킹
  const rankings = [
    { id: 'rankPer', key: 'per', title: '📈 저PER 순위' },
    { id: 'rankPbr', key: 'pbr', title: '📊 저PBR 순위' },
    { id: 'rankRoe', key: 'roe', title: '💰 고ROE 순위' },
    { id: 'rankDiv', key: 'dividendYield', title: '💵 배당수익률 순위' },
  ];

  rankings.forEach(({ id, key, title }) => {
    const el = document.getElementById(id);
    el.innerHTML = `
      <div class="ranking-card">
        <h3>${title}</h3>
        ${renderRankingList(MOCK_STOCKS, key)}
      </div>
    `;
    bindStockCards(el);
  });
});
