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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KrxCollectorService {

    private static final String FSC_API_URL =
            "https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo";
    private static final int PAGE_SIZE = 1000;

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;

    @Value("${fsc.api.key}")
    private String fscApiKey;

    /**
     * 금융위원회 주식시세 API — KOSPI/KOSDAQ 전체 종목 수집
     */
    public void collectStockList() {
        String basDt = getLastTradingDay();
        log.info("[FSC] 기준일자: {}", basDt);
        collectByMarket("KOSPI", basDt);
        // collectByMarket("KOSDAQ", basDt);
    }

    private void collectByMarket(String market, String basDt) {
        try {
            int pageNo = 1;
            int totalCount = Integer.MAX_VALUE;

            while ((pageNo - 1) * PAGE_SIZE < totalCount) {
                String url = UriComponentsBuilder.fromHttpUrl(FSC_API_URL)
                        .queryParam("serviceKey", fscApiKey)
                        .queryParam("numOfRows", PAGE_SIZE)
                        .queryParam("pageNo", pageNo)
                        .queryParam("resultType", "json")
                        .queryParam("mrktCls", market)
                        .queryParam("basDt", basDt)
                        .build(false)
                        .toUriString();

                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
                if (response.getBody() == null) break;

                Map<String, Object> body = extractBody(response.getBody());
                if (body == null) break;

                Object tc = body.get("totalCount");
                if (tc == null) break;
                totalCount = Integer.parseInt(tc.toString());

                Map<String, Object> items = (Map<String, Object>) body.get("items");
                if (items == null) break;

                Object itemObj = items.get("item");
                if (!(itemObj instanceof List)) break;

                List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemObj;
                if (itemList.isEmpty()) break;

                log.info("[FSC] {} page={} size={}", market, pageNo, itemList.size());
                for (Map<String, Object> item : itemList) {
                    saveStock(item, market);
                }

                pageNo++;
            }

            log.info("[FSC] {} 수집 완료", market);

        } catch (Exception e) {
            log.error("[FSC] {} 데이터 수집 실패 — graceful fallback: {}", market, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractBody(Map<?, ?> raw) {
        try {
            Map<String, Object> response = (Map<String, Object>) raw.get("response");
            if (response == null) return null;
            return (Map<String, Object>) response.get("body");
        } catch (ClassCastException e) {
            log.warn("[FSC] 응답 구조 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private void saveStock(Map<String, Object> item, String market) {
        try {
            String stockCode = parseString(item, "srtnCd");
            String stockName = parseString(item, "itmsNm");

            if (stockCode == null || stockName == null) return;

            String isinCd         = parseString(item, "isinCd");
            Long currentPrice     = parseLong(item, "clpr");
            Long changeAmount     = parseLong(item, "vs");
            Double changeRate     = parseDouble(item, "fltRt");
            Long marketCap        = parseLong(item, "mrktTotAmt");
            Long sharesOutstanding = parseLong(item, "lstgStCnt");

            Optional<StockEntity> existingOpt = stockRepository.findByStockCode(stockCode);

            StockEntity entity;
            if (existingOpt.isPresent()) {
                StockEntity e = existingOpt.get();
                entity = StockEntity.builder()
                        .id(e.getId())
                        .stockCode(e.getStockCode())
                        .stockName(stockName)
                        .market(market)
                        .sector(e.getSector())
                        .listingDate(e.getListingDate())
                        .ceoName(e.getCeoName())
                        .isinCd(isinCd)
                        .currentPrice(currentPrice)
                        .changeAmount(changeAmount)
                        .changeRate(changeRate)
                        .marketCap(marketCap)
                        .sharesOutstanding(sharesOutstanding)
                        .per(e.getPer())
                        .pbr(e.getPbr())
                        .roe(e.getRoe())
                        .eps(e.getEps())
                        .bps(e.getBps())
                        .debtRatio(e.getDebtRatio())
                        .operatingMargin(e.getOperatingMargin())
                        .dartCorpCode(e.getDartCorpCode())
                        .updatedAt(LocalDateTime.now())
                        .build();
            } else {
                entity = StockEntity.builder()
                        .stockCode(stockCode)
                        .stockName(stockName)
                        .market(market)
                        .isinCd(isinCd)
                        .currentPrice(currentPrice)
                        .changeAmount(changeAmount)
                        .changeRate(changeRate)
                        .marketCap(marketCap)
                        .sharesOutstanding(sharesOutstanding)
                        .updatedAt(LocalDateTime.now())
                        .build();
            }

            stockRepository.save(entity);

        } catch (Exception e) {
            log.error("[FSC] 개별 종목 처리 오류: {}", e.getMessage());
        }
    }

    /** 가장 최근 평일 (주말 제외, 공휴일 미처리) */
    private String getLastTradingDay() {
        LocalDate date = LocalDate.now().minusDays(1);
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.minusDays(1);
        }
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String parseString(Map<String, Object> item, String key) {
        Object val = item.get(key);
        return val != null ? val.toString().trim() : null;
    }

    private Long parseLong(Map<String, Object> item, String key) {
        try {
            Object val = item.get(key);
            if (val == null) return null;
            return Long.parseLong(val.toString().replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(Map<String, Object> item, String key) {
        try {
            Object val = item.get(key);
            if (val == null) return null;
            return Double.parseDouble(val.toString().replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
