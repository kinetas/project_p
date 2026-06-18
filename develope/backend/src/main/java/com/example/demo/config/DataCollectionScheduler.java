package com.example.demo.config;

import com.example.demo.entity.CompanyEntity;
import com.example.demo.repository.CompanyRepository;
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
    private final CompanyRepository companyRepository;

    /**
     * 매일 오전 7시 — 전체 데이터 수집 파이프라인 실행
     * 1. DART corp_code.xml → company 테이블
     * 2. KRX 주식시세 → stock_price + company 업데이트
     * 3. DART 재무제표 → financial_statement
     * 4. 금융위원회 배당 → dividend_info
     * 5. 지표 계산 → stock_indicator
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void dailyCollection() {
        log.info("[Scheduler] 일간 데이터 수집 파이프라인 시작");

        try {
            // Step 1: DART corp_code.xml → company 테이블
            log.info("[Scheduler] Step 1 — DART corp_code 수집 시작");
            dartCollectorService.fetchCorpCodes();
            log.info("[Scheduler] Step 1 — DART corp_code 수집 완료");

            // Step 2: KRX 주식시세 → stock_price + company 업데이트
            log.info("[Scheduler] Step 2 — KRX 주식시세 수집 시작");
            // TODO: KrxCollectorService에 collectStockData() 메서드 없음 — collectStockList() 사용 (기존 메서드명 유지)
            krxCollectorService.collectStockList();
            log.info("[Scheduler] Step 2 — KRX 주식시세 수집 완료");

            // company 테이블 기반 전체 종목 조회
            List<CompanyEntity> allCompanies = companyRepository.findAll();
            log.info("[Scheduler] 전체 종목 {}건 조회 완료", allCompanies.size());

            // Step 3: DART 재무제표 → financial_statement (회사별)
            // 1회 호출당 당기·전기·전전기 3개 연도 저장 → 2회 호출로 6개 연도 커버
            int currentYear = LocalDate.now().getYear();
            int[] targetYears = {currentYear - 1, currentYear - 4};

            log.info("[Scheduler] Step 3 — DART 재무제표 수집 시작");
            for (CompanyEntity company : allCompanies) {
                String corpCode = company.getCorpCode();
                String stockCode = company.getStockCode();
                if (corpCode == null || corpCode.isBlank()) {
                    log.warn("[Scheduler] corp_code 없음 — skip: {}", stockCode);
                    continue;
                }
                for (int year : targetYears) {
                    dartCollectorService.collectFinancials(corpCode, stockCode, year);
                }
            }
            log.info("[Scheduler] Step 3 — DART 재무제표 수집 완료");

            // Step 4: 금융위원회 배당 → dividend_info (전체 페이지 — isinCd 기준 저장)
            log.info("[Scheduler] Step 4 — 배당정보 수집 시작");
            dividendCollectorService.collectDividendInfo();
            log.info("[Scheduler] Step 4 — 배당정보 수집 완료");

            // Step 5: 지표 계산 → stock_indicator (회사별 — 재무 + 시세 + 배당 모두 적재 완료 후)
            log.info("[Scheduler] Step 5 — 투자지표 계산 시작");
            for (CompanyEntity company : allCompanies) {
                if (company.getCorpCode() == null || company.getCorpCode().isBlank()) continue;
                indicatorCalculationService.calculateAndSave(company.getStockCode());
            }
            log.info("[Scheduler] Step 5 — 투자지표 계산 완료");

            log.info("[Scheduler] 일간 데이터 수집 파이프라인 완료 — 처리 종목: {}건", allCompanies.size());

        } catch (Exception e) {
            log.error("[Scheduler] 일간 수집 파이프라인 오류: {}", e.getMessage(), e);
        }
    }
}
