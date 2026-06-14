document.addEventListener('DOMContentLoaded', () => {
  initHeader('list');

  let stocks = [...MOCK_STOCKS];
  let sortKey = null;
  let sortDir = 'desc';
  let filters = {};

  const params = new URLSearchParams(window.location.search);
  const searchQuery = params.get('q')?.toLowerCase() || '';
  if (searchQuery) {
    document.getElementById('headerSearch').value = searchQuery;
    stocks = stocks.filter(
      (s) => s.name.toLowerCase().includes(searchQuery) || s.code.includes(searchQuery)
    );
  }

  const countEl = document.getElementById('stockCount');
  const tableBody = document.getElementById('tableBody');
  const cardList = document.getElementById('cardList');
  const filterBtn = document.getElementById('filterBtn');
  const filterPanel = document.getElementById('filterPanel');
  const resetFilterBtn = document.getElementById('resetFilterBtn');

  function applyFilters() {
    let result = [...MOCK_STOCKS];

    if (searchQuery) {
      result = result.filter(
        (s) => s.name.toLowerCase().includes(searchQuery) || s.code.includes(searchQuery)
      );
    }

    const ranges = [
      { key: 'per', min: 'perMin', max: 'perMax' },
      { key: 'pbr', min: 'pbrMin', max: 'pbrMax' },
      { key: 'roe', min: 'roeMin', max: 'roeMax' },
      { key: 'dividendYield', min: 'divMin', max: 'divMax' },
      { key: 'marketCap', min: 'capMin', max: 'capMax' },
    ];

    ranges.forEach(({ key, min, max }) => {
      if (filters[min] != null) result = result.filter((s) => s[key] >= filters[min]);
      if (filters[max] != null) result = result.filter((s) => s[key] <= filters[max]);
    });

    if (sortKey) {
      result = sortStocks(result, sortKey, sortDir);
    }

    return result;
  }

  function activeFilterCount() {
    return Object.values(filters).filter((v) => v != null && v !== '').length;
  }

  function updateFilterBtn() {
    const count = activeFilterCount();
    filterBtn.innerHTML = count > 0
      ? `필터 (${count}) <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>`
      : `필터 <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"/></svg>`;
    resetFilterBtn.style.display = count > 0 ? 'inline-block' : 'none';
  }

  function renderTable(data) {
    countEl.textContent = `총 ${data.length}개 종목`;

    if (data.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="8"><div class="empty-state">조건에 맞는 종목이 없습니다.</div></td></tr>`;
      cardList.innerHTML = `<div class="empty-state">조건에 맞는 종목이 없습니다.</div>`;
      return;
    }

    tableBody.innerHTML = data.map((s) => {
      const cls = changeClass(s.changeRate);
      return `
        <tr data-code="${s.code}">
          <td>
            <div class="td-name">${s.name}</div>
            <div class="td-code">${s.code}</div>
          </td>
          <td>${formatPrice(s.price)}</td>
          <td class="${cls}">${formatChange(s.changeRate)}</td>
          <td>${formatMarketCap(s.marketCap)}</td>
          <td>${s.per}</td>
          <td>${s.pbr}</td>
          <td>${s.roe}%</td>
          <td>${s.dividendYield}%</td>
        </tr>
      `;
    }).join('');

    cardList.innerHTML = data.map(renderStockCard).join('');

    tableBody.querySelectorAll('tr[data-code]').forEach((row) => {
      row.addEventListener('click', () => goToDetail(row.dataset.code));
    });
    bindStockCards(cardList);
  }

  function render() {
    stocks = applyFilters();
    renderTable(stocks);
    updateFilterBtn();
  }

  // 필터 토글
  filterBtn.addEventListener('click', () => {
    filterPanel.classList.toggle('open');
    filterBtn.classList.toggle('open');
  });

  document.getElementById('applyFilter').addEventListener('click', () => {
    filters = {};
    ['perMin', 'perMax', 'pbrMin', 'pbrMax', 'roeMin', 'roeMax', 'divMin', 'divMax', 'capMin', 'capMax'].forEach((id) => {
      const el = document.getElementById(id);
      const val = parseFloat(el.value);
      if (!isNaN(val)) filters[id] = val;
    });
    filterPanel.classList.remove('open');
    filterBtn.classList.remove('open');
    render();
  });

  document.getElementById('cancelFilter').addEventListener('click', () => {
    filterPanel.classList.remove('open');
    filterBtn.classList.remove('open');
  });

  resetFilterBtn.addEventListener('click', () => {
    filters = {};
    document.querySelectorAll('.filter-panel input').forEach((el) => { el.value = ''; });
    render();
  });

  // 정렬
  document.querySelectorAll('.stock-table th[data-sort]').forEach((th) => {
    th.addEventListener('click', () => {
      const key = th.dataset.sort;
      if (sortKey === key) {
        sortDir = sortDir === 'asc' ? 'desc' : 'asc';
      } else {
        sortKey = key;
        sortDir = 'desc';
      }
      document.querySelectorAll('.stock-table th').forEach((h) => h.classList.remove('sorted'));
      th.classList.add('sorted');
      render();
    });
  });

  render();
});
