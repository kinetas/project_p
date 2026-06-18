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
import java.util.Arrays;
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

    private static final List<String> REVENUE_NAMES =
            Arrays.asList("매출액", "수익(매출액)", "영업수익");
    private static final List<String> OPERATING_PROFIT_NAMES =
            Arrays.asList("영업이익", "영업이익(손실)");
    private static final List<String> NET_INCOME_NAMES =
            Arrays.asList("당기순이익", "당기순이익(손실)");

    /**
     * 검색/필터 조건으로 종목 목록 조회
     */
    public List<StockListResponse> getStockList(StockSearchRequest request) {
        List<CompanyEntity> companies = companyRepository.findAll();

        return companies.stream()
                .map(company -> {
                    Optional<StockPriceEntity> stockPrice =
                            stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(company.getStockCode());
                    Optional<StockIndicatorEntity> indicator =
                            stockIndicatorRepository.findTopByIdStockCodeOrderByIdCalcYearDesc(company.getStockCode());
                    return StockListResponse.from(company, stockPrice.orElse(null), indicator.orElse(null));
                })
                .filter(r -> matchesFilter(r, request))
                .collect(Collectors.toList());
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
     * 최근 5년 재무제표 조회
     */
    public List<FinancialResponse> getFinancials(String stockCode) {
        List<String> years = financialStatementRepository
                .findDistinctBsnsYearsByStockCodeOrderByDesc(stockCode);

        List<String> recentYears = years.stream()
                .limit(5)
                .collect(Collectors.toList());

        return recentYears.stream()
                .map(year -> {
                    List<FinancialStatementEntity> rows =
                            financialStatementRepository.findByStockCodeAndBsnsYear(stockCode, year);

                    Long revenue = extractAmount(rows, REVENUE_NAMES);
                    Long operatingProfit = extractAmount(rows, OPERATING_PROFIT_NAMES);
                    Long netIncome = extractAmount(rows, NET_INCOME_NAMES);

                    return FinancialResponse.builder()
                            .year(year)
                            .revenue(revenue)
                            .operatingProfit(operatingProfit)
                            .netIncome(netIncome)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * account_nm 기준으로 해당 계정의 thstrmAmount 추출
     */
    private Long extractAmount(List<FinancialStatementEntity> rows, List<String> accountNames) {
        return rows.stream()
                .filter(r -> accountNames.contains(r.getAccountNm()))
                .map(FinancialStatementEntity::getThstrmAmount)
                .filter(amount -> amount != null)
                .findFirst()
                .orElse(null);
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
