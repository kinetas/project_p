package com.example.demo.service;

import com.example.demo.dto.stock.*;
import com.example.demo.entity.CompanyEntity;
import com.example.demo.entity.FinancialStatementEntity;
import com.example.demo.entity.StockIndicatorEntity;
import com.example.demo.entity.StockPriceEntity;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.FinancialStatementRepository;
import com.example.demo.repository.StockIndicatorRepository;
import com.example.demo.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final CompanyRepository companyRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockIndicatorRepository stockIndicatorRepository;
    private final FinancialStatementRepository financialStatementRepository;


    /**
     * 검색/필터 조건으로 종목 목록 조회
     */
    public PageResponse<StockListResponse> getStockList(StockSearchRequest request) {
        List<CompanyEntity> companies = companyRepository.findAll();

        List<StockListResponse> allFiltered = companies.stream()
            .map(company -> {
                Optional<StockPriceEntity> sp = stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(company.getStockCode());
                Optional<StockIndicatorEntity> ind = stockIndicatorRepository.findTopByIdStockCodeOrderByIdCalcYearDesc(company.getStockCode());
                return StockListResponse.from(company, sp.orElse(null), ind.orElse(null));
            })
            .filter(r -> matchesFilter(r, request))
            .collect(Collectors.toList());

        int total = allFiltered.size();
        int page = request.getPage();
        int size = request.getSize() > 0 ? request.getSize() : 20;
        int totalPages = (int) Math.ceil((double) total / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<StockListResponse> paged = fromIndex >= total
            ? Collections.emptyList()
            : allFiltered.subList(fromIndex, toIndex);

        return PageResponse.<StockListResponse>builder()
            .data(paged)
            .totalCount(total)
            .page(page)
            .size(size)
            .totalPages(totalPages)
            .build();
    }

    /**
     * 종목 상세 조회
     */
    public StockDetailResponse getStockDetail(String stockCode) {
        CompanyEntity company = companyRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new IllegalArgumentException("종목을 찾을 수 없습니다: " + stockCode));

        Optional<StockPriceEntity> stockPrice =
                stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(stockCode);
        Optional<StockIndicatorEntity> indicator =
                stockIndicatorRepository.findTopByIdStockCodeOrderByIdCalcYearDesc(stockCode);

        return StockDetailResponse.from(company, stockPrice.orElse(null), indicator.orElse(null));
    }

    /**
     * TOP10 조회 — type: "lowPer" / "highRoe" / "lowPbr" / "value"
     */
    public Top10Response getTop10(String type) {
        List<StockIndicatorEntity> allIndicators = stockIndicatorRepository.findAll();

        List<StockIndicatorEntity> top10Indicators;

        switch (type) {
            case "highRoe" -> top10Indicators = allIndicators.stream()
                    .filter(i -> i.getRoe() != null)
                    .sorted(Comparator.comparing(StockIndicatorEntity::getRoe).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
            case "lowPbr" -> top10Indicators = allIndicators.stream()
                    .filter(i -> i.getPbr() != null)
                    .sorted(Comparator.comparing(StockIndicatorEntity::getPbr))
                    .limit(10)
                    .collect(Collectors.toList());
            case "value" -> top10Indicators = allIndicators.stream()
                    .filter(i -> i.getPer() != null && i.getPbr() != null && i.getRoe() != null && i.getDebtRatio() != null)
                    .sorted(Comparator.comparing(StockIndicatorEntity::getPer)
                            .thenComparing(StockIndicatorEntity::getPbr)
                            .thenComparing(Comparator.comparing(StockIndicatorEntity::getRoe).reversed())
                            .thenComparing(StockIndicatorEntity::getDebtRatio))
                    .limit(10)
                    .collect(Collectors.toList());
            default -> // lowPer
                top10Indicators = allIndicators.stream()
                        .filter(i -> i.getPer() != null)
                        .sorted(Comparator.comparing(StockIndicatorEntity::getPer))
                        .limit(10)
                        .collect(Collectors.toList());
        }

        List<StockListResponse> responses = top10Indicators.stream()
                .map(indicator -> {
                    String stockCode = indicator.getId().getStockCode();
                    Optional<CompanyEntity> company = companyRepository.findByStockCode(stockCode);
                    Optional<StockPriceEntity> stockPrice =
                            stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(stockCode);
                    return company.map(c -> StockListResponse.from(c, stockPrice.orElse(null), indicator))
                            .orElse(null);
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        return Top10Response.builder()
                .type(type)
                .stocks(responses)
                .build();
    }

    /**
     * 시가총액 TOP10 종목 조회
     */
    public List<StockListResponse> getFeaturedStocks() {
        return companyRepository.findAll().stream()
                .map(company -> {
                    Optional<StockPriceEntity> sp = stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(company.getStockCode());
                    Optional<StockIndicatorEntity> ind = stockIndicatorRepository.findTopByIdStockCodeOrderByIdCalcYearDesc(company.getStockCode());
                    return StockListResponse.from(company, sp.orElse(null), ind.orElse(null));
                })
                .filter(r -> r.getMrktTotAmt() != null && r.getMrktTotAmt() > 0)
                .sorted(Comparator.comparingLong(StockListResponse::getMrktTotAmt).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 최근 5년 재무제표 조회
     */
    public List<FinancialResponse> getFinancials(String stockCode) {
        return financialStatementRepository
                .findDistinctBsnsYearsByStockCodeOrderByDesc(stockCode)
                .stream()
                .limit(5)
                .map(year -> financialStatementRepository.findByStockCodeAndBsnsYear(stockCode, year)
                        .map(fs -> FinancialResponse.builder()
                                .year(year)
                                .revenue(fs.getRevenue())
                                .operatingProfit(fs.getOperatingIncome())
                                .netIncome(fs.getNetIncome())
                                .totalAssets(fs.getTotalAssets())
                                .totalLiabilities(fs.getTotalLiabilities())
                                .totalEquity(fs.getTotalEquity())
                                .build())
                        .orElse(null))
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    /**
     * StockListResponse 기준 필터 처리
     */
    private boolean matchesFilter(StockListResponse response, StockSearchRequest req) {
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String keyword = req.getKeyword().toLowerCase();
            boolean nameMatch = response.getCorpName() != null && response.getCorpName().toLowerCase().contains(keyword);
            boolean codeMatch = response.getStockCode() != null && response.getStockCode().contains(keyword);
            if (!nameMatch && !codeMatch) return false;
        }
        if (req.getMarket() != null && !req.getMarket().isBlank()) {
            if (response.getMrktCtg() == null || !req.getMarket().equalsIgnoreCase(response.getMrktCtg())) return false;
        }
        if (req.getMinPer() != null) {
            if (response.getPer() == null || response.getPer().doubleValue() < req.getMinPer()) return false;
        }
        if (req.getMaxPer() != null) {
            if (response.getPer() == null || response.getPer().doubleValue() > req.getMaxPer()) return false;
        }
        if (req.getMinRoe() != null) {
            if (response.getRoe() == null || response.getRoe().doubleValue() < req.getMinRoe()) return false;
        }
        if (req.getMaxDebtRatio() != null) {
            if (response.getDebtRatio() == null || response.getDebtRatio().doubleValue() > req.getMaxDebtRatio()) return false;
        }
        return true;
    }
}
