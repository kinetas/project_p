package com.example.demo.config;

import com.example.demo.entity.StockEntity;
import com.example.demo.repository.StockRepository;
import com.example.demo.service.DartCollectorService;
import com.example.demo.service.DividendCollectorService;
import com.example.demo.service.IndicatorCalculationService;
import com.example.demo.service.KrxCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

// 주의: DemoApplication.java 클래스에 @EnableScheduling 어노테이션을 추가해야 합니다.
// @SpringBootApplication
// @EnableScheduling  <-- 이 어노테이션 추가 필요
// public class DemoApplication { ... }

@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectionScheduler {

    private final KrxCollectorService krxCollectorService;
    private final DividendCollectorService dividendCollectorService;
    private final DartCollectorService dartCollectorService;
    private final IndicatorCalculationService indicatorCalculationService;
    private final StockRepository stockRepository;

    /**
     * 매일 오전 7시 — 전체 데이터 수집 파이프라인 실행
     * 1. KRX 현재가/시총 갱신
     * 2. 배당정보 수집 (금융위원회)
     * 3. 전체 종목 조회
     * 4. DART corp_code 매핑 (최초 1회 또는 미매핑 종목 대상)
     * 5. 최근 5년 재무 수집
     * 6. 투자지표 계산 및 저장
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void dailyCollection() {
        log.info("[Scheduler] 일간 데이터 수집 파이프라인 시작");

        try {
            // Step 1: KRX 현재가/시총 갱신
            log.info("[Scheduler] Step 1 — KRX 현재가/시총 수집 시작");
            krxCollectorService.collectStockList();
            log.info("[Scheduler] Step 1 — KRX 수집 완료");

            // Step 2: 배당정보 수집 (금융위원회) — isinCd 기준으로 저장
            log.info("[Scheduler] Step 2 — 배당정보 수집 시작");
            dividendCollectorService.collectDividendInfo();
            log.info("[Scheduler] Step 2 — 배당정보 수집 완료");

            // Step 3: 전체 종목 조회
            List<StockEntity> allStocks = stockRepository.findAll();
            log.info("[Scheduler] Step 3 — 전체 종목 조회 완료: {}건", allStocks.size());

            // Step 4: DART corp_code 매핑 (dartCorpCode가 없는 종목이 있을 경우 일괄 수행)
            boolean hasMissingCorpCode = allStocks.stream()
                    .anyMatch(s -> s.getDartCorpCode() == null || s.getDartCorpCode().isBlank());

            if (hasMissingCorpCode) {
                log.info("[Scheduler] Step 4 — DART corp_code 매핑 시작");
                dartCollectorService.fetchCorpCodes();
                log.info("[Scheduler] Step 4 — DART corp_code 매핑 완료");
                // 매핑 후 최신 종목 목록 재조회
                allStocks = stockRepository.findAll();
            } else {
                log.info("[Scheduler] Step 4 — corp_code 매핑 전체 완료 상태, skip");
            }

            // Step 5, 6 & 7: 종목별 기업개황 + 재무 수집 + 지표 계산
            int currentYear = LocalDate.now().getYear();
            // 1회 호출당 당기·전기·전전기 3개 연도 저장 → 2회 호출로 6개 연도 커버
            // {Y-1} → Y-1, Y-2, Y-3 / {Y-4} → Y-4, Y-5, Y-6
            int[] targetYears = {currentYear - 1, currentYear - 4};

            for (StockEntity stock : allStocks) {
                String stockCode = stock.getStockCode();
                String dartCorpCode = stock.getDartCorpCode();

                if (dartCorpCode == null || dartCorpCode.isBlank()) {
                    log.warn("[Scheduler] dart_corp_code 없음 — skip: {}", stockCode);
                    continue;
                }

                // Step 5: 기업개황 수집 (CEO, 업종코드)
                dartCollectorService.fetchCompanyInfo(dartCorpCode, stockCode);

                // Step 6: 최근 5년 재무 수집
                for (int year : targetYears) {
                    dartCollectorService.collectFinancials(dartCorpCode, stockCode, year);
                }

                // Step 7: 투자지표 계산 및 저장
                indicatorCalculationService.calculateAndSave(stockCode);
            }

            log.info("[Scheduler] 일간 데이터 수집 파이프라인 완료 — 처리 종목: {}건", allStocks.size());

        } catch (Exception e) {
            log.error("[Scheduler] 일간 수집 파이프라인 오류: {}", e.getMessage(), e);
        }
    }
}
