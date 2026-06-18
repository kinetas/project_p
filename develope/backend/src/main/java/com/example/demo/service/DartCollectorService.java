package com.example.demo.service;

import com.example.demo.entity.CompanyEntity;
import com.example.demo.entity.FinancialStatementEntity;
import com.example.demo.repository.CompanyRepository;
import com.example.demo.repository.FinancialStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.transaction.annotation.Propagation;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DartCollectorService {

    private static final String DART_BASE_URL  = "https://opendart.fss.or.kr/api";
    private static final String CORP_CODE_URL  = DART_BASE_URL + "/corpCode.xml";
    private static final String MULTI_ACNT_URL = DART_BASE_URL + "/fnlttMultiAcnt.json";

    private final RestTemplate restTemplate;
    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository financialStatementRepository;

    @Value("${dart.api.key}")
    private String dartApiKey;

    // ──────────────────────────────────────────────────────────
    // 1. corp_code 매핑
    // ──────────────────────────────────────────────────────────

    /**
     * DART corp_code.zip 다운로드 및 파싱
     * corp_code, stock_code, corp_name, corp_cls → CompanyEntity UPSERT
     * stock_code가 비어 있으면 스킵 (상장사만)
     */
    @Transactional
    public void fetchCorpCodes() {
        try {
            String url = CORP_CODE_URL + "?crtfc_key=" + dartApiKey;
            byte[] zipBytes = restTemplate.getForObject(url, byte[].class);

            if (zipBytes == null) {
                log.warn("[DART] corp_code.zip 응답이 null입니다.");
                return;
            }

            int upsertCount = parseAndUpsertCorpCodes(zipBytes);
            log.info("[DART] corp_code UPSERT 완료 — 처리 수: {}", upsertCount);

        } catch (Exception e) {
            log.error("[DART] corp_code 수집 실패 — graceful fallback: {}", e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────
    // 2. 기업개황 — no-op (company 테이블에 ceoName/sector 컬럼 없음)
    // ──────────────────────────────────────────────────────────

    // fetchCompanyInfo() 제거됨 — company 테이블 스키마에 ceoName/sector 없음

    // ──────────────────────────────────────────────────────────
    // 3. 다중회사 주요계정 — fnlttMultiAcnt.json (피벗 저장)
    // ──────────────────────────────────────────────────────────

    /**
     * GET /api/fnlttMultiAcnt.json
     * - CFS 우선, 없으면 OFS 사용
     * - account_nm별 중복 제거 (첫 번째 occurrence 유지)
     * - (stock_code, bsns_year) 1행으로 피벗 저장
     * - API year 기준으로 당기(year)/전기(year-1)/전전기(year-2) 3행 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

            // year, year-1, year-2 기존 데이터 삭제
            for (int offset = 0; offset <= 2; offset++) {
                financialStatementRepository.deleteByStockCodeAndBsnsYear(
                        stockCode, String.valueOf(year - offset));
            }

            // stock_code별 그룹화
            Map<String, List<Map<String, Object>>> byStock = new LinkedHashMap<>();
            for (Map<String, Object> row : items) {
                String sc = parseStr(row, "stock_code");
                if (sc == null || sc.isBlank()) sc = stockCode;
                else sc = sc.trim();
                byStock.computeIfAbsent(sc, k -> new ArrayList<>()).add(row);
            }

            List<FinancialStatementEntity> entities = new ArrayList<>();

            for (Map.Entry<String, List<Map<String, Object>>> entry : byStock.entrySet()) {
                String sc = entry.getKey();
                List<Map<String, Object>> allRows = entry.getValue();

                // CFS 우선, 없으면 OFS
                List<Map<String, Object>> cfsRows = allRows.stream()
                        .filter(r -> "CFS".equals(parseStr(r, "fs_div")))
                        .collect(Collectors.toList());
                List<Map<String, Object>> selected = cfsRows.isEmpty()
                        ? allRows.stream().filter(r -> "OFS".equals(parseStr(r, "fs_div"))).collect(Collectors.toList())
                        : cfsRows;

                if (selected.isEmpty()) continue;

                // account_nm 중복 제거 (첫 번째 occurrence만 유지)
                Map<String, Map<String, Object>> deduped = new LinkedHashMap<>();
                for (Map<String, Object> row : selected) {
                    String accountNm = parseStr(row, "account_nm");
                    if (accountNm != null && !deduped.containsKey(accountNm)) {
                        deduped.put(accountNm, row);
                    }
                }

                Map<String, Object> first = selected.get(0);
                String reprtCode = parseStr(first, "reprt_code");
                String fsDiv     = parseStr(first, "fs_div");
                String currency  = parseStr(first, "currency");

                // 당기 행 (bsns_year = year)
                entities.add(buildPivotEntity(sc, String.valueOf(year), reprtCode, fsDiv, currency,
                        parseStr(first, "thstrm_nm"), parseStr(first, "thstrm_dt"),
                        deduped, "thstrm_amount"));

                // 전기 행 (bsns_year = year-1) — frmtrm 데이터가 있으면 생성
                if (getFirstNonNull(deduped, "frmtrm_amount") != null) {
                    entities.add(buildPivotEntity(sc, String.valueOf(year - 1), reprtCode, fsDiv, currency,
                            parseStr(first, "frmtrm_nm"), parseStr(first, "frmtrm_dt"),
                            deduped, "frmtrm_amount"));
                }

                // 전전기 행 (bsns_year = year-2) — bfefrmtrm 데이터가 있으면 생성
                if (getFirstNonNull(deduped, "bfefrmtrm_amount") != null) {
                    entities.add(buildPivotEntity(sc, String.valueOf(year - 2), reprtCode, fsDiv, currency,
                            parseStr(first, "bfefrmtrm_nm"), parseStr(first, "bfefrmtrm_dt"),
                            deduped, "bfefrmtrm_amount"));
                }
            }

            financialStatementRepository.saveAll(entities);
            log.info("[DART] 재무제표 저장 완료 — stockCode: {}, year: {}, rows: {}",
                    stockCode, year, entities.size());

        } catch (Exception e) {
            log.error("[DART] fnlttMultiAcnt 실패 — stockCode: {}, year: {}, error: {}",
                    stockCode, year, e.getMessage());
        }
    }

    private FinancialStatementEntity buildPivotEntity(
            String stockCode, String bsnsYear, String reprtCode, String fsDiv, String currency,
            String periodNm, String periodDt,
            Map<String, Map<String, Object>> deduped, String amtField) {
        return FinancialStatementEntity.builder()
                .stockCode(stockCode)
                .bsnsYear(bsnsYear)
                .reprtCode(reprtCode)
                .fsDiv(fsDiv)
                .periodNm(periodNm)
                .periodDt(periodDt)
                .currentAssets(getAmt(deduped, amtField, "유동자산"))
                .nonCurrentAssets(getAmt(deduped, amtField, "비유동자산"))
                .totalAssets(getAmt(deduped, amtField, "자산총계"))
                .currentLiabilities(getAmt(deduped, amtField, "유동부채"))
                .nonCurrentLiabilities(getAmt(deduped, amtField, "비유동부채"))
                .totalLiabilities(getAmt(deduped, amtField, "부채총계"))
                .capitalStock(getAmt(deduped, amtField, "자본금"))
                .retainedEarnings(getAmt(deduped, amtField, "이익잉여금"))
                .totalEquity(getAmt(deduped, amtField, "자본총계"))
                .revenue(getAmt(deduped, amtField, "매출액", "수익(매출액)", "영업수익"))
                .operatingIncome(getAmt(deduped, amtField, "영업이익", "영업이익(손실)"))
                .incomeBeforeTax(getAmt(deduped, amtField, "법인세차감전 순이익", "법인세비용차감전순이익(손실)"))
                .netIncome(getAmt(deduped, amtField, "당기순이익(손실)", "당기순이익"))
                .currency(currency)
                .build();
    }

    /** deduped 맵에서 accountNames 순서로 첫 번째 non-null amount 반환 */
    private Long getAmt(Map<String, Map<String, Object>> deduped, String amtField, String... accountNames) {
        for (String name : accountNames) {
            Map<String, Object> row = deduped.get(name);
            if (row != null) {
                Long val = parseAmount(parseStr(row, amtField));
                if (val != null) return val;
            }
        }
        return null;
    }

    /** deduped 맵 전체에서 해당 amtField의 non-null 값이 하나라도 있으면 반환 */
    private Long getFirstNonNull(Map<String, Map<String, Object>> deduped, String amtField) {
        for (Map<String, Object> row : deduped.values()) {
            Long val = parseAmount(parseStr(row, amtField));
            if (val != null) return val;
        }
        return null;
    }

    // ──────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────

    private int parseAndUpsertCorpCodes(byte[] zipBytes) {
        int count = 0;
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
                        String corpName  = getTagValue(el, "corp_name");
                        String corpCls   = getTagValue(el, "corp_cls");

                        if (stockCode == null || stockCode.isBlank()) continue;
                        if (corpCode == null || corpCode.isBlank()) continue;

                        stockCode = stockCode.trim();
                        corpCode  = corpCode.trim();
                        corpName  = corpName != null ? corpName.trim() : "";
                        corpCls   = corpCls  != null ? corpCls.trim()  : "";

                        Optional<CompanyEntity> existing = companyRepository.findByStockCode(stockCode);
                        if (existing.isPresent()) {
                            CompanyEntity updated = existing.get().toBuilder()
                                    .corpName(corpName)
                                    .corpCls(corpCls)
                                    .build();
                            companyRepository.save(updated);
                        } else {
                            CompanyEntity newEntity = CompanyEntity.builder()
                                    .corpCode(corpCode)
                                    .stockCode(stockCode)
                                    .corpName(corpName)
                                    .corpCls(corpCls)
                                    .isinCd(null)
                                    .build();
                            companyRepository.save(newEntity);
                        }
                        count++;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("[DART] corp_code.zip 파싱 오류: {}", e.getMessage());
        }
        return count;
    }

    private String getTagValue(Element el, String tagName) {
        NodeList nodes = el.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : null;
    }

    private String parseStr(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        String s = val.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private Long parseAmount(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
