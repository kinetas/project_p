package com.example.demo.service;

import com.example.demo.entity.FinancialEntity;
import com.example.demo.entity.StockEntity;
import com.example.demo.repository.FinancialRepository;
import com.example.demo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorCalculationService {

    private final StockRepository stockRepository;
    private final FinancialRepository financialRepository;

    /**
     * 특정 종목의 투자지표 계산 및 저장
     * - EPS, BPS, PER, PBR, ROE, 영업이익률, 부채비율 계산
     * - StockEntity 및 연도별 FinancialEntity 모두 업데이트
     */
    public void calculateAndSave(String stockCode) {
        Optional<StockEntity> stockOpt = stockRepository.findByStockCode(stockCode);
        if (stockOpt.isEmpty()) {
            log.warn("[Indicator] 종목 없음 — stockCode: {}", stockCode);
            return;
        }

        StockEntity stock = stockOpt.get();

        // 최근 재무 데이터로 StockEntity 지표 계산
        List<FinancialEntity> financials = financialRepository.findTop5ByStockCodeOrderByYearDesc(stockCode);
        if (financials.isEmpty()) {
            log.warn("[Indicator] 재무 데이터 없음 — stockCode: {}", stockCode);
        } else {
            FinancialEntity latest = financials.get(0);
            Long sharesOutstanding = stock.getSharesOutstanding();
            Long currentPrice = stock.getCurrentPrice();

            Long eps = calcEps(latest.getNetIncome(), sharesOutstanding);
            Long bps = calcBps(latest.getTotalEquity(), sharesOutstanding);
            Double per = calcPer(currentPrice, eps);
            Double pbr = calcPbr(currentPrice, bps);
            Double roe = calcRoe(latest.getNetIncome(), latest.getTotalEquity());
            Double operatingMargin = calcOperatingMargin(latest.getOperatingProfit(), latest.getRevenue());
            Double debtRatio = calcDebtRatio(latest.getTotalLiabilities(), latest.getTotalEquity());

            StockEntity updatedStock = StockEntity.builder()
                    .id(stock.getId())
                    .stockCode(stock.getStockCode())
                    .stockName(stock.getStockName())
                    .market(stock.getMarket())
                    .sector(stock.getSector())
                    .listingDate(stock.getListingDate())
                    .ceoName(stock.getCeoName())
                    .currentPrice(currentPrice)
                    .marketCap(stock.getMarketCap())
                    .sharesOutstanding(sharesOutstanding)
                    .per(per)
                    .pbr(pbr)
                    .roe(roe)
                    .eps(eps)
                    .bps(bps)
                    .debtRatio(debtRatio)
                    .operatingMargin(operatingMargin)
                    .dartCorpCode(stock.getDartCorpCode())
                    .updatedAt(LocalDateTime.now())
                    .build();

            stockRepository.save(updatedStock);
            log.info("[Indicator] StockEntity 지표 갱신 완료 — stockCode: {}", stockCode);
        }

        // 연도별 FinancialEntity 지표 계산
        for (FinancialEntity financial : financials) {
            Long sharesOutstanding = stock.getSharesOutstanding();
            Long currentPrice = stock.getCurrentPrice();

            Long eps = calcEps(financial.getNetIncome(), sharesOutstanding);
            Long bps = calcBps(financial.getTotalEquity(), sharesOutstanding);
            Double per = calcPer(currentPrice, eps);
            Double pbr = calcPbr(currentPrice, bps);
            Double roe = calcRoe(financial.getNetIncome(), financial.getTotalEquity());
            Double operatingMargin = calcOperatingMargin(financial.getOperatingProfit(), financial.getRevenue());
            Double debtRatio = calcDebtRatio(financial.getTotalLiabilities(), financial.getTotalEquity());

            FinancialEntity updatedFinancial = FinancialEntity.builder()
                    .id(financial.getId())
                    .stockCode(financial.getStockCode())
                    .year(financial.getYear())
                    .revenue(financial.getRevenue())
                    .operatingProfit(financial.getOperatingProfit())
                    .netIncome(financial.getNetIncome())
                    .totalAssets(financial.getTotalAssets())
                    .totalLiabilities(financial.getTotalLiabilities())
                    .totalEquity(financial.getTotalEquity())
                    .operatingMargin(operatingMargin)
                    .debtRatio(debtRatio)
                    .roe(roe)
                    .eps(eps)
                    .bps(bps)
                    .per(per)
                    .pbr(pbr)
                    .build();

            financialRepository.save(updatedFinancial);
        }

        log.info("[Indicator] 연도별 지표 갱신 완료 — stockCode: {}", stockCode);
    }

    // EPS = 순이익 ÷ 발행주식수
    private Long calcEps(Long netIncome, Long sharesOutstanding) {
        if (netIncome == null || sharesOutstanding == null || sharesOutstanding == 0) return null;
        return netIncome / sharesOutstanding;
    }

    // BPS = 자본 ÷ 발행주식수
    private Long calcBps(Long totalEquity, Long sharesOutstanding) {
        if (totalEquity == null || sharesOutstanding == null || sharesOutstanding == 0) return null;
        return totalEquity / sharesOutstanding;
    }

    // PER = 현재가 ÷ EPS
    private Double calcPer(Long currentPrice, Long eps) {
        if (currentPrice == null || eps == null || eps == 0) return null;
        return (double) currentPrice / eps;
    }

    // PBR = 현재가 ÷ BPS
    private Double calcPbr(Long currentPrice, Long bps) {
        if (currentPrice == null || bps == null || bps == 0) return null;
        return (double) currentPrice / bps;
    }

    // ROE = 순이익 ÷ 자본
    private Double calcRoe(Long netIncome, Long totalEquity) {
        if (netIncome == null || totalEquity == null || totalEquity == 0) return null;
        return (double) netIncome / totalEquity;
    }

    // 영업이익률 = 영업이익 ÷ 매출액
    private Double calcOperatingMargin(Long operatingProfit, Long revenue) {
        if (operatingProfit == null || revenue == null || revenue == 0) return null;
        return (double) operatingProfit / revenue;
    }

    // 부채비율 = 부채 ÷ 자본
    private Double calcDebtRatio(Long totalLiabilities, Long totalEquity) {
        if (totalLiabilities == null || totalEquity == null || totalEquity == 0) return null;
        return (double) totalLiabilities / totalEquity;
    }
}
