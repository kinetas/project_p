package com.example.demo.service;

import com.example.demo.entity.CompanyEntity;
import com.example.demo.entity.StockPriceEntity;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    private final CompanyRepository companyRepository;
    private final StockPriceRepository stockPriceRepository;

    @Value("${fsc.api.key}")
    private String fscApiKey;

    /**
     * 금융위원회 주식시세 API — KOSPI/KOSDAQ 전체 종목 수집
     */
    public void collectStockList() {
        String basDt = getLastTradingDay();
        log.info("[FSC] 기준일자: {}", basDt);
        collectByMarket("KOSPI", basDt);
        collectByMarket("KOSDAQ", basDt);
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
                    saveStock(item, market, basDt);
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

    @Transactional
    private void saveStock(Map<String, Object> item, String market, String basDt) {
        try {
            String srtnCd  = parseString(item, "srtnCd");
            String itmsNm  = parseString(item, "itmsNm");

            if (srtnCd == null || itmsNm == null) return;

            String isinCd    = parseString(item, "isinCd");
            String mrktCtg   = parseString(item, "mrktCtg");
            if (mrktCtg == null) mrktCtg = market;

            Long   clpr      = parseLong(item, "clpr");
            Long   vs        = parseLong(item, "vs");
            BigDecimal fltRt = parseBigDecimal(item, "fltRt");
            Long   mkp       = parseLong(item, "mkp");
            Long   hipr      = parseLong(item, "hipr");
            Long   lopr      = parseLong(item, "lopr");
            Long   trqu      = parseLong(item, "trqu");
            Long   trPrc     = parseLong(item, "trPrc");
            Long   lstgStCnt = parseLong(item, "lstgStCnt");
            Long   mrktTotAmt = parseLong(item, "mrktTotAmt");

            // company 처리: stockCode로 조회 후 존재하면 UPDATE, 없으면 스킵
            Optional<CompanyEntity> companyOpt = companyRepository.findByStockCode(srtnCd);
            if (companyOpt.isPresent()) {
                CompanyEntity existing = companyOpt.get();
                String corpCls = convertCorpCls(mrktCtg);
                CompanyEntity updated = existing.toBuilder()
                        .isinCd(isinCd)
                        .corpName(itmsNm)
                        .corpCls(corpCls)
                        .build();
                companyRepository.save(updated);
            } else {
                log.debug("[FSC] company 미존재 — srtnCd={} 스킵 (DART 선행 필요)", srtnCd);
            }

            // stock_price 저장
            StockPriceEntity stockPriceEntity = StockPriceEntity.builder()
                    .basDt(basDt)
                    .srtnCd(srtnCd)
                    .isinCd(isinCd)
                    .itmsNm(itmsNm)
                    .mrktCtg(mrktCtg)
                    .clpr(clpr)
                    .vs(vs)
                    .fltRt(fltRt)
                    .mkp(mkp)
                    .hipr(hipr)
                    .lopr(lopr)
                    .trqu(trqu)
                    .trPrc(trPrc)
                    .lstgStCnt(lstgStCnt)
                    .mrktTotAmt(mrktTotAmt)
                    .build();

            stockPriceRepository.save(stockPriceEntity);

        } catch (Exception e) {
            log.error("[FSC] 개별 종목 처리 오류: {}", e.getMessage());
        }
    }

    /**
     * mrktCtg → corp_cls 변환
     * "KOSPI" → "Y", "KOSDAQ" → "K", "KONEX" → "N", 그 외 → "E"
     */
    private String convertCorpCls(String mrktCtg) {
        if (mrktCtg == null) return "E";
        return switch (mrktCtg.toUpperCase()) {
            case "KOSPI"  -> "Y";
            case "KOSDAQ" -> "K";
            case "KONEX"  -> "N";
            default       -> "E";
        };
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
        if (val == null) return null;
        String str = val.toString().trim();
        return str.isEmpty() ? null : str;
    }

    private Long parseLong(Map<String, Object> item, String key) {
        try {
            Object val = item.get(key);
            if (val == null) return null;
            String str = val.toString().replace(",", "").trim();
            if (str.isEmpty()) return null;
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Map<String, Object> item, String key) {
        try {
            Object val = item.get(key);
            if (val == null) return null;
            String str = val.toString().replace(",", "").trim();
            if (str.isEmpty()) return null;
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
