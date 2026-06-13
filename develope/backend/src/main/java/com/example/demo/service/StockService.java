package com.example.demo.service;

import com.example.demo.dto.stock.*;
import com.example.demo.entity.StockEntity;
import com.example.demo.repository.FinancialRepository;
import com.example.demo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final FinancialRepository financialRepository;

    /**
     * 검색/필터 조건으로 종목 목록 페이징 조회
     */
    public List<StockListResponse> getStockList(StockSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        String keyword = request.getKeyword();

        Page<StockEntity> page;
        if (keyword != null && !keyword.isBlank()) {
            page = stockRepository.findByStockNameContainingOrStockCodeContaining(
                    keyword, keyword, pageable);
        } else {
            page = stockRepository.findAll(pageable);
        }

        return page.stream()
                .filter(s -> matchesFilter(s, request))
                .map(StockListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 종목 상세 조회
     */
    public StockDetailResponse getStockDetail(String stockCode) {
        StockEntity stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다: " + stockCode));
        return StockDetailResponse.from(stock);
    }

    /**
     * TOP10 조회 — "value" / "lowPer" / "highRoe"
     */
    public Top10Response getTop10(String type) {
        List<StockEntity> stocks;

        switch (type) {
            case "lowPer" -> stocks = stockRepository.findTop10ByPerIsNotNullOrderByPerAsc();
            case "highRoe" -> stocks = stockRepository.findTop10ByRoeIsNotNullOrderByRoeDesc();
            default -> {
                // value: 가치주 TOP10 (PER/PBR 낮고, ROE 높고, 부채비율 낮은 순)
                Pageable top10 = PageRequest.of(0, 10);
                stocks = stockRepository.findTop10ValueStocks(top10);
            }
        }

        List<StockListResponse> responses = stocks.stream()
                .map(StockListResponse::from)
                .collect(Collectors.toList());

        return Top10Response.builder()
                .type(type)
                .stocks(responses)
                .build();
    }

    /**
     * 최근 5년 재무제표 조회
     */
    public List<FinancialResponse> getFinancials(String stockCode) {
        return financialRepository.findTop5ByStockCodeOrderByYearDesc(stockCode)
                .stream()
                .map(FinancialResponse::from)
                .collect(Collectors.toList());
    }

    private boolean matchesFilter(StockEntity s, StockSearchRequest req) {
        if (req.getMarket() != null && !req.getMarket().isBlank()) {
            if (!req.getMarket().equalsIgnoreCase(s.getMarket())) return false;
        }
        if (req.getMinPer() != null && (s.getPer() == null || s.getPer() < req.getMinPer())) return false;
        if (req.getMaxPer() != null && (s.getPer() == null || s.getPer() > req.getMaxPer())) return false;
        if (req.getMinRoe() != null && (s.getRoe() == null || s.getRoe() < req.getMinRoe())) return false;
        if (req.getMaxDebtRatio() != null && (s.getDebtRatio() == null || s.getDebtRatio() > req.getMaxDebtRatio())) return false;
        return true;
    }
}
