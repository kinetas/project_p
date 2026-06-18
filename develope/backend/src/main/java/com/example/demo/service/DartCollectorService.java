package com.example.demo.service;

import com.example.demo.entity.FinancialEntity;
import com.example.demo.entity.StockEntity;
import com.example.demo.repository.FinancialRepository;
import com.example.demo.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DartCollectorService {

    private static final String DART_BASE_URL    = "https://opendart.fss.or.kr/api";
    private static final String CORP_CODE_URL    = DART_BASE_URL + "/corpCode.xml";
    private static final String COMPANY_URL      = DART_BASE_URL + "/company.json";
    private static final String MULTI_ACNT_URL   = DART_BASE_URL + "/fnlttMultiAcnt.json";

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;
    private final FinancialRepository financialRepository;

    @Value("${dart.api.key}")
    private String dartApiKey;

    // ──────────────────────────────────────────────────────────
    // 1. corp_code 매핑 (기존 유지)
    // ──────────────────────────────────────────────────────────

    /**
     * DART corp_code.zip 다운로드 및 파싱
     * stockCode → dartCorpCode 매핑 후 StockEntity 업데이트
     */
    public void fetchCorpCodes() {
        try {
            String url = CORP_CODE_URL + "?crtfc_key=" + dartApiKey;
            byte[] zipBytes = restTemplate.getForObject(url, byte[].class);

            if (zipBytes == null) {
                log.warn("[DART] corp_code.zip 응답이 null입니다.");
                return;
            }

            Map<String, String> stockToCorpMap = parseCorpCodeZip(zipBytes);
            log.info("[DART] corp_code 파싱 완료 — 매핑 수: {}", stockToCorpMap.size());

            List<StockEntity> stocks = stockRepository.findAll();
            for (StockEntity stock : stocks) {
                String corpCode = stockToCorpMap.get(stock.getStockCode());
                if (corpCode != null && !corpCode.equals(stock.getDartCorpCode())) {
                    stockRepository.save(stock.toBuilder()
                            .dartCorpCode(corpCode)
                            .updatedAt(LocalDateTime.now())
                            .build());
                }
            }

            log.info("[DART] corp_code 매핑 완료");

        } catch (Exception e) {
            log.error("[DART] corp_code 수집 실패 — graceful fallback: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────
    // 2. 기업개황 — company.json (기업.txt 기반)
    // ──────────────────────────────────────────────────────────

    /**
     * GET /api/company.json — 기업개황 조회
     * ceoName, sector(업종코드) 를 StockEntity에 반영
     */
    public void fetchCompanyInfo(String dartCorpCode, String stockCode) {
        try {
            String url = COMPANY_URL
                    + "?crtfc_key=" + dartApiKey
                    + "&corp_code=" + dartCorpCode;

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"000".equals(response.get("status"))) {
                log.warn("[DART] company.json 오류 — stockCode: {}, status: {}",
                        stockCode, response != null ? response.get("status") : "null");
                return;
            }

            String ceoName    = parseStr(response, "ceo_nm");
            String sector     = parseStr(response, "induty_code");

            Optional<StockEntity> opt = stockRepository.findByStockCode(stockCode);
            if (opt.isEmpty()) return;

            StockEntity stock = opt.get();
            stockRepository.save(stock.toBuilder()
                    .ceoName(ceoName)
                    .sector(sector)
                    .updatedAt(LocalDateTime.now())
                    .build());

            log.info("[DART] company 저장 완료 — stockCode: {}, ceo: {}, sector: {}",
                    stockCode, ceoName, sector);

        } catch (Exception e) {
            log.error("[DART] company.json 실패 — stockCode: {}, error: {}", stockCode, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────
    // 3. 다중회사 주요계정 — fnlttMultiAcnt.json (재무.txt 기반)
    // ──────────────────────────────────────────────────────────

    /**
     * GET /api/fnlttMultiAcnt.json — 다중회사 주요계정 조회
     * 연결재무제표(CFS) 우선, 없으면 개별재무제표(OFS) 사용
     * 매출액 / 영업이익 / 당기순이익 / 자산총계 / 부채총계 / 자본총계 파싱
     */
    /**
     * 사업보고서(11011) 1회 호출로 당기(year)·전기(year-1)·전전기(year-2) 3개 연도를 한번에 저장.
     * API 호출 횟수: 기존 5회 → 2회 (스케줄러에서 {currentYear-1, currentYear-4} 2개 연도만 호출)
     */
    public void collectFinancials(String dartCorpCode, String stockCode, int year) {
        try {
            String url = MULTI_ACNT_URL
                    + "?crtfc_key=" + dartApiKey
                    + "&corp_code=" + dartCorpCode
                    + "&bsns_year=" + year
                    + "&reprt_code=11011";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"000".equals(response.get("status"))) {
                log.warn("[DART] fnlttMultiAcnt 오류 — stockCode: {}, year: {}, status: {}",
                        stockCode, year, response != null ? response.get("status") : "null");
                return;
            }

            Object listObj = response.get("list");
            if (!(listObj instanceof List)) {
                log.warn("[DART] fnlttMultiAcnt list 파싱 실패 — stockCode: {}, year: {}", stockCode, year);
                return;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) listObj;

            // CFS(연결) 우선, 없으면 OFS(개별) 사용
            List<Map<String, Object>> cfsItems = items.stream()
                    .filter(i -> "CFS".equals(i.get("fs_div")))
                    .toList();
            List<Map<String, Object>> target = cfsItems.isEmpty() ? items : cfsItems;

            // 당기(year), 전기(year-1), 전전기(year-2) 각각 별도 행으로 저장
            saveFinancialYear(stockCode, year,     target, "thstrm_amount");
            saveFinancialYear(stockCode, year - 1, target, "frmtrm_amount");
            saveFinancialYear(stockCode, year - 2, target, "bfefrmtrm_amount");

            log.info("[DART] 재무제표 저장 완료 — stockCode: {}, years: {}/{}/{}",
                    stockCode, year, year - 1, year - 2);

        } catch (Exception e) {
            log.error("[DART] fnlttMultiAcnt 실패 — stockCode: {}, year: {}, error: {}",
                    stockCode, year, e.getMessage());
        }
    }

    private void saveFinancialYear(String stockCode, int year,
                                   List<Map<String, Object>> items, String amountField) {
        Long revenue          = extractByField(items, amountField, "매출액", "수익(매출액)", "영업수익");
        Long operatingProfit  = extractByField(items, amountField, "영업이익", "영업이익(손실)");
        Long netIncome        = extractByField(items, amountField, "당기순이익", "당기순이익(손실)");
        Long totalAssets      = extractByField(items, amountField, "자산총계");
        Long totalLiabilities = extractByField(items, amountField, "부채총계");
        Long totalEquity      = extractByField(items, amountField, "자본총계");

        // 모든 값이 null이면 해당 연도 데이터 없음 — 저장 스킵
        if (revenue == null && operatingProfit == null && netIncome == null
                && totalAssets == null && totalLiabilities == null && totalEquity == null) {
            return;
        }

        Optional<FinancialEntity> existingOpt = financialRepository.findByStockCodeAndYear(stockCode, year);
        FinancialEntity entity;
        if (existingOpt.isPresent()) {
            entity = existingOpt.get().toBuilder()
                    .revenue(revenue)
                    .operatingProfit(operatingProfit)
                    .netIncome(netIncome)
                    .totalAssets(totalAssets)
                    .totalLiabilities(totalLiabilities)
                    .totalEquity(totalEquity)
                    .build();
        } else {
            entity = FinancialEntity.builder()
                    .stockCode(stockCode)
                    .year(year)
                    .revenue(revenue)
                    .operatingProfit(operatingProfit)
                    .netIncome(netIncome)
                    .totalAssets(totalAssets)
                    .totalLiabilities(totalLiabilities)
                    .totalEquity(totalEquity)
                    .build();
        }
        financialRepository.save(entity);
    }

    // ──────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────

    private Map<String, String> parseCorpCodeZip(byte[] zipBytes) {
        Map<String, String> result = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    byte[] xmlBytes = zis.readAllBytes();
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new ByteArrayInputStream(xmlBytes));

                    NodeList list = doc.getElementsByTagName("list");
                    for (int i = 0; i < list.getLength(); i++) {
                        Element el = (Element) list.item(i);
                        String corpCode  = getTagValue(el, "corp_code");
                        String stockCode = getTagValue(el, "stock_code");
                        if (stockCode != null && !stockCode.isBlank()) {
                            result.put(stockCode.trim(), corpCode.trim());
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("[DART] corp_code.zip 파싱 오류: {}", e.getMessage());
        }
        return result;
    }

    private String getTagValue(Element el, String tagName) {
        NodeList nodes = el.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }

    /** account_nm 기준으로 지정 금액 필드(thstrm/frmtrm/bfefrmtrm_amount) 추출 */
    private Long extractByField(List<Map<String, Object>> items, String fieldName, String... candidates) {
        for (String candidate : candidates) {
            for (Map<String, Object> item : items) {
                if (candidate.equals(item.get("account_nm"))) {
                    Object val = item.get(fieldName);
                    if (val != null) {
                        try {
                            return Long.parseLong(val.toString().replace(",", "").trim());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String parseStr(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString().trim() : null;
    }
}
