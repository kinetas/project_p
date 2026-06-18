package com.example.demo.service;

import com.example.demo.entity.DividendEntity;
import com.example.demo.entity.DividendId;
import com.example.demo.repository.DividendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DividendCollectorService {

    private static final String DIVIDEND_API_URL =
            "http://apis.data.go.kr/1160100/GetStocDiviInfoService_V2/getDiviInfo_V2";
    private static final int PAGE_SIZE = 1000;

    private final RestTemplate restTemplate;
    private final DividendRepository dividendRepository;

    @Value("${fsc.api.key}")
    private String apiKey;

    /**
     * 금융위원회 주식 배당 정보 API — 전체 배당 데이터 수집
     */
    public void collectDividendInfo() {
        try {
            int pageNo = 1;
            int totalCount = Integer.MAX_VALUE;

            while ((pageNo - 1) * PAGE_SIZE < totalCount) {
                String url = UriComponentsBuilder.fromHttpUrl(DIVIDEND_API_URL)
                        .queryParam("serviceKey", apiKey)
                        .queryParam("resultType", "json")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", PAGE_SIZE)
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

                log.info("[DIVIDEND] page={} size={}", pageNo, itemList.size());
                for (Map<String, Object> item : itemList) {
                    saveDividend(item);
                }

                pageNo++;
            }

            log.info("[DIVIDEND] 배당 정보 수집 완료");

        } catch (Exception e) {
            log.error("[DIVIDEND] 배당 데이터 수집 실패 — graceful fallback: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractBody(Map<?, ?> raw) {
        try {
            Map<String, Object> response = (Map<String, Object>) raw.get("response");
            if (response == null) return null;
            return (Map<String, Object>) response.get("body");
        } catch (ClassCastException e) {
            log.warn("[DIVIDEND] 응답 구조 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private void saveDividend(Map<String, Object> item) {
        try {
            String isinCd = parseString(item, "isinCd");
            String basDt  = parseString(item, "basDt");

            if (isinCd == null || basDt == null) return;

            DividendId dividendId = new DividendId(isinCd, basDt);

            DividendEntity entity = DividendEntity.builder()
                    .id(dividendId)
                    .crno(parseString(item, "crno"))
                    .stckIssuCmpyNm(parseString(item, "stckIssuCmpyNm"))
                    .dvdnBasDt(parseString(item, "dvdnBasDt"))
                    .cashDvdnPayDt(parseString(item, "cashDvdnPayDt"))
                    .stckHndvDt(parseString(item, "stckHndvDt"))
                    .isinCdNm(parseString(item, "isinCdNm"))
                    .stckDvdnRcd(parseString(item, "stckDvdnRcd"))
                    .stckDvdnRcdNm(parseString(item, "stckDvdnRcdNm"))
                    .trsnmDptyDcd(parseString(item, "trsnmDptyDcd"))
                    .trsnmDptyDcdNm(parseString(item, "trsnmDptyDcdNm"))
                    .scrsItmsKcd(parseString(item, "scrsItmsKcd"))
                    .scrsItmsKcdNm(parseString(item, "scrsItmsKcdNm"))
                    .stckGenrDvdnAmt(parseBigDecimal(item, "stckGenrDvdnAmt"))
                    .stckGrdnDvdnAmt(parseBigDecimal(item, "stckGrdnDvdnAmt"))
                    .stckGenrCashDvdnRt(parseBigDecimal(item, "stckGenrCashDvdnRt"))
                    .stckGenrDvdnRt(parseBigDecimal(item, "stckGenrDvdnRt"))
                    .cashGrdnDvdnRt(parseBigDecimal(item, "cashGrdnDvdnRt"))
                    .stckGrdnDvdnRt(parseBigDecimal(item, "stckGrdnDvdnRt"))
                    .stckParPrc(parseBigDecimal(item, "stckParPrc"))
                    .stckStacMd(parseString(item, "stckStacMd"))
                    .build();

            dividendRepository.save(entity);

        } catch (Exception e) {
            log.error("[DIVIDEND] 개별 배당 정보 처리 오류: {}", e.getMessage());
        }
    }

    private String parseString(Map<String, Object> item, String key) {
        Object val = item.get(key);
        if (val == null) return null;
        String str = val.toString().trim();
        return str.isEmpty() ? null : str;
    }

    private BigDecimal parseBigDecimal(Map<String, Object> item, String key) {
        try {
            Object val = item.get(key);
            if (val == null) return null;
            String str = val.toString().replace(",", "").trim();
            return str.isEmpty() ? null : new BigDecimal(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
