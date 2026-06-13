package com.example.demo.service;

import com.example.demo.entity.StockEntity;
import com.example.demo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KrxCollectorService {

    private static final String KRX_BASE_URL = "https://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;

    @Value("${krx.api.key}")
    private String krxApiKey;

    /**
     * KRX 주식시세 데이터 수집
     * - 코스피(STK) / 코스닥(KSQ) 전체 종목의 종목코드, 종목명, 현재가, 시가총액, 발행주식수 수집
     * - StockEntity 생성 또는 업데이트 후 저장
     */
    public void collectStockList() {
        collectByMarket("STK", "KOSPI");
        collectByMarket("KSQ", "KOSDAQ");
    }

    private void collectByMarket(String marketCode, String marketName) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(KRX_BASE_URL)
                    .queryParam("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
                    .queryParam("mktId", marketCode)
                    .queryParam("trdDd", getCurrentDateString())
                    .queryParam("auth", krxApiKey)
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getBody() == null) {
                log.warn("[KRX] {} 응답 body가 null입니다.", marketName);
                return;
            }

            Object outBlockObj = response.getBody().get("OutBlock_1");
            if (!(outBlockObj instanceof List)) {
                log.warn("[KRX] {} 데이터 파싱 실패 — OutBlock_1 없음", marketName);
                return;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) outBlockObj;
            log.info("[KRX] {} 종목 수집 시작 — 총 {}건", marketName, items.size());

            for (Map<String, Object> item : items) {
                try {
                    String stockCode = parseString(item, "ISU_SRT_CD");
                    String stockName = parseString(item, "ISU_ABBRV");
                    Long currentPrice = parseLong(item, "TDD_CLSPRC");
                    Long marketCap = parseLong(item, "MKTCAP");
                    Long sharesOutstanding = parseLong(item, "LIST_SHRS");

                    if (stockCode == null || stockName == null) {
                        continue;
                    }

                    Optional<StockEntity> existingOpt = stockRepository.findByStockCode(stockCode);

                    StockEntity entity;
                    if (existingOpt.isPresent()) {
                        StockEntity existing = existingOpt.get();
                        entity = StockEntity.builder()
                                .id(existing.getId())
                                .stockCode(existing.getStockCode())
                                .stockName(stockName)
                                .market(marketName)
                                .sector(existing.getSector())
                                .listingDate(existing.getListingDate())
                                .ceoName(existing.getCeoName())
                                .currentPrice(currentPrice)
                                .marketCap(marketCap)
                                .sharesOutstanding(sharesOutstanding)
                                .per(existing.getPer())
                                .pbr(existing.getPbr())
                                .roe(existing.getRoe())
                                .eps(existing.getEps())
                                .bps(existing.getBps())
                                .debtRatio(existing.getDebtRatio())
                                .operatingMargin(existing.getOperatingMargin())
                                .dartCorpCode(existing.getDartCorpCode())
                                .updatedAt(LocalDateTime.now())
                                .build();
                    } else {
                        entity = StockEntity.builder()
                                .stockCode(stockCode)
                                .stockName(stockName)
                                .market(marketName)
                                .currentPrice(currentPrice)
                                .marketCap(marketCap)
                                .sharesOutstanding(sharesOutstanding)
                                .updatedAt(LocalDateTime.now())
                                .build();
                    }

                    stockRepository.save(entity);

                } catch (Exception e) {
                    log.error("[KRX] 개별 종목 처리 중 오류: {}", e.getMessage());
                }
            }

            log.info("[KRX] {} 수집 완료", marketName);

        } catch (Exception e) {
            log.error("[KRX] {} 데이터 수집 실패 — graceful fallback: {}", marketName, e.getMessage());
        }
    }

    private String getCurrentDateString() {
        java.time.LocalDate today = java.time.LocalDate.now();
        return today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String parseString(Map<String, Object> item, String key) {
        Object val = item.get(key);
        return val != null ? val.toString().trim() : null;
    }

    private Long parseLong(Map<String, Object> item, String key) {
        try {
            Object val = item.get(key);
            if (val == null) return null;
            String str = val.toString().replace(",", "").trim();
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
