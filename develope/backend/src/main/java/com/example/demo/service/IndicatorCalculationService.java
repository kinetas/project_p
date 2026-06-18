package com.example.demo.service;

import com.example.demo.entity.CompanyEntity;
import com.example.demo.entity.DividendEntity;
import com.example.demo.entity.FinancialStatementEntity;
import com.example.demo.entity.StockIndicatorEntity;
import com.example.demo.entity.StockIndicatorId;
import com.example.demo.entity.StockPriceEntity;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.DividendRepository;
import com.example.demo.repository.FinancialStatementRepository;
import com.example.demo.repository.StockIndicatorRepository;
import com.example.demo.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorCalculationService {

    private final CompanyRepository companyRepository;
    private final StockPriceRepository stockPriceRepository;
    private final FinancialStatementRepository financialStatementRepository;
    private final StockIndicatorRepository stockIndicatorRepository;
    private final DividendRepository dividendRepository;

    /**
     * 특정 종목의 투자지표 계산 및 저장
     * - EPS, BPS, PER, PBR, ROE, 영업이익률, 부채비율, 배당수익률 계산
     * - StockIndicatorEntity UPSERT
     */
    @Transactional
    public void calculateAndSave(String stockCode) {

        // 1. CompanyEntity 조회
        Optional<CompanyEntity> companyOpt = companyRepository.findByStockCode(stockCode);
        if (companyOpt.isEmpty()) {
            log.warn("[Indicator] 종목 없음 — stockCode: {}", stockCode);
            return;
        }
        CompanyEntity company = companyOpt.get();

        // 2. 최신 주가 조회
        Optional<StockPriceEntity> priceOpt = stockPriceRepository.findTopBySrtnCdOrderByBasDtDesc(stockCode);
        if (priceOpt.isEmpty()) {
            log.warn("[Indicator] 주가 데이터 없음 — stockCode: {}", stockCode);
            return;
        }
        StockPriceEntity stockPrice = priceOpt.get();
        Long clpr = stockPrice.getClpr();
        Long lstgStCnt = stockPrice.getLstgStCnt();
        String baseDt = stockPrice.getBasDt();

        // 3. 최신 bsns_year 조회
        Optional<String> bsnsYearOpt = financialStatementRepository.findTopBsnsYearByStockCode(stockCode);
        if (bsnsYearOpt.isEmpty()) {
            log.warn("[Indicator] 재무제표 연도 없음 — stockCode: {}", stockCode);
            return;
        }
        String bsnsYear = bsnsYearOpt.get();

        // 4. CFS 우선 조회, 없으면 OFS 조회
        List<FinancialStatementEntity> rows =
                financialStatementRepository.findByStockCodeAndBsnsYearAndFsDivOrderByOrdAsc(stockCode, bsnsYear, "CFS");
        if (rows.isEmpty()) {
            rows = financialStatementRepository.findByStockCodeAndBsnsYearAndFsDivOrderByOrdAsc(stockCode, bsnsYear, "OFS");
        }
        if (rows.isEmpty()) {
            log.warn("[Indicator] 재무제표 데이터 없음 — stockCode: {}, bsnsYear: {}", stockCode, bsnsYear);
            return;
        }

        // 5. account_nm 기준 금액 추출
        Long revenue          = extractAmount(rows, "매출액", "수익(매출액)", "영업수익");
        Long operatingProfit  = extractAmount(rows, "영업이익", "영업이익(손실)");
        Long netIncome        = extractAmount(rows, "당기순이익", "당기순이익(손실)");
        Long totalAssets      = extractAmount(rows, "자산총계");
        Long totalLiabilities = extractAmount(rows, "부채총계");
        Long totalEquity      = extractAmount(rows, "자본총계");

        // 6. 배당 조회
        BigDecimal dividendAmt = null;
        String isinCd = company.getIsinCd();
        if (isinCd != null) {
            Optional<DividendEntity> dividendOpt = dividendRepository.findTopByIsinCdOrderByBasDtDesc(isinCd);
            if (dividendOpt.isPresent()) {
                dividendAmt = dividendOpt.get().getStckGenrDvdnAmt();
            }
        }

        // 7. 지표 계산 (null 안전, BigDecimal 사용)
        BigDecimal shareCountBd = toLong2Bd(lstgStCnt);
        BigDecimal priceBd      = toLong2Bd(clpr);

        BigDecimal eps             = divideOrNull(toLong2Bd(netIncome), shareCountBd);
        BigDecimal bps             = divideOrNull(toLong2Bd(totalEquity), shareCountBd);
        BigDecimal per             = divideOrNull(priceBd, eps);
        BigDecimal pbr             = divideOrNull(priceBd, bps);
        BigDecimal roe             = percentOrNull(toLong2Bd(netIncome), toLong2Bd(totalEquity));
        BigDecimal operatingMargin = percentOrNull(toLong2Bd(operatingProfit), toLong2Bd(revenue));
        BigDecimal debtRatio       = percentOrNull(toLong2Bd(totalLiabilities), toLong2Bd(totalEquity));
        BigDecimal dividendYield   = calcDividendYield(dividendAmt, priceBd);

        // 8. StockIndicatorEntity UPSERT
        String calcYear = String.valueOf(java.time.LocalDate.now().getYear());
        StockIndicatorId indicatorId = new StockIndicatorId(stockCode, calcYear);

        StockIndicatorEntity indicator = StockIndicatorEntity.builder()
                .id(indicatorId)
                .bsnsYear(bsnsYear)
                .reprtCode("11011")
                .baseDt(baseDt)
                .eps(eps)
                .bps(bps)
                .per(per)
                .pbr(pbr)
                .roe(roe)
                .operatingMargin(operatingMargin)
                .debtRatio(debtRatio)
                .dividendYield(dividendYield)
                .calculatedAt(LocalDateTime.now())
                .build();

        stockIndicatorRepository.save(indicator);
        log.info("[Indicator] 지표 저장 완료 — stockCode: {}, calcYear: {}, bsnsYear: {}", stockCode, calcYear, bsnsYear);
    }

    /**
     * rows 순서대로 accountNm이 names 배열 중 하나와 equals이면 thstrmAmount 반환
     */
    private Long extractAmount(List<FinancialStatementEntity> rows, String... names) {
        for (FinancialStatementEntity row : rows) {
            if (row.getAccountNm() == null) continue;
            for (String name : names) {
                if (row.getAccountNm().equals(name)) {
                    return row.getThstrmAmount();
                }
            }
        }
        return null;
    }

    /** Long → BigDecimal 변환, null이면 null 반환 */
    private BigDecimal toLong2Bd(Long value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    /** numerator / denominator, denominator가 null이거나 0이면 null */
    private BigDecimal divideOrNull(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /** (numerator / denominator) * 100, denominator가 null이거나 0이면 null */
    private BigDecimal percentOrNull(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.multiply(BigDecimal.valueOf(100))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    /** 배당수익률 = (dividendAmt / price) * 100 */
    private BigDecimal calcDividendYield(BigDecimal dividendAmt, BigDecimal price) {
        if (dividendAmt == null || price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return dividendAmt.multiply(BigDecimal.valueOf(100))
                .divide(price, 2, RoundingMode.HALF_UP);
    }
}
