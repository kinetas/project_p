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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    // 3. 다중회사 주요계정 — fnlttMultiAcnt.json
    // ──────────────────────────────────────────────────────────

    /**
     * GET /api/fnlttMultiAcnt.json — DART API 응답 list의 모든 row를
     * FinancialStatementEntity로 저장.
     * 기존 데이터(stockCode + bsnsYear)는 전부 삭제 후 재저장 (UPSERT).
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
            String bsnsYearStr = String.valueOf(year);

            // 기존 데이터 삭제 후 재저장
            List<FinancialStatementEntity> existing =
                    financialStatementRepository.findByStockCodeAndBsnsYear(stockCode, bsnsYearStr);
            if (!existing.isEmpty()) {
                financialStatementRepository.deleteAll(existing);
                log.info("[DART] 기존 재무제표 삭제 — stockCode: {}, year: {}, rows: {}",
                        stockCode, year, existing.size());
            }

            List<FinancialStatementEntity> entities = new ArrayList<>();
            for (Map<String, Object> row : items) {
                String rowStockCode = parseStr(row, "stock_code");
                // API 응답의 stock_code가 비어 있는 경우 파라미터로 전달받은 stockCode 사용
                String effectiveStockCode = (rowStockCode != null && !rowStockCode.isBlank())
                        ? rowStockCode.trim() : stockCode;

                FinancialStatementEntity entity = FinancialStatementEntity.builder()
                        .bsnsYear(parseStr(row, "bsns_year"))
                        .stockCode(effectiveStockCode)
                        .reprtCode(parseStr(row, "reprt_code"))
                        .accountNm(parseStr(row, "account_nm"))
                        .fsDiv(parseStr(row, "fs_div"))
                        .fsNm(parseStr(row, "fs_nm"))
                        .sjDiv(parseStr(row, "sj_div"))
                        .sjNm(parseStr(row, "sj_nm"))
                        .thstrmNm(parseStr(row, "thstrm_nm"))
                        .thstrmDt(parseStr(row, "thstrm_dt"))
                        .thstrmAmount(parseAmount(parseStr(row, "thstrm_amount")))
                        .thstrmAddAmount(parseAmount(parseStr(row, "thstrm_add_amount")))
                        .frmtrmNm(parseStr(row, "frmtrm_nm"))
                        .frmtrmDt(parseStr(row, "frmtrm_dt"))
                        .frmtrmAmount(parseAmount(parseStr(row, "frmtrm_amount")))
                        .frmtrmAddAmount(parseAmount(parseStr(row, "frmtrm_add_amount")))
                        .bfefrmtrmNm(parseStr(row, "bfefrmtrm_nm"))
                        .bfefrmtrmDt(parseStr(row, "bfefrmtrm_dt"))
                        .bfefrmtrmAmount(parseAmount(parseStr(row, "bfefrmtrm_amount")))
                        .ord(parseOrd(row, "ord"))
                        .currency(parseStr(row, "currency"))
                        .build();
                entities.add(entity);
            }

            financialStatementRepository.saveAll(entities);
            log.info("[DART] 재무제표 저장 완료 — stockCode: {}, year: {}, rows: {}",
                    stockCode, year, entities.size());

        } catch (Exception e) {
            log.error("[DART] fnlttMultiAcnt 실패 — stockCode: {}, year: {}, error: {}",
                    stockCode, year, e.getMessage());
        }
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

                        // stock_code 없으면 스킵 (상장사만)
                        if (stockCode == null || stockCode.isBlank()) continue;
                        if (corpCode == null || corpCode.isBlank()) continue;

                        stockCode = stockCode.trim();
                        corpCode  = corpCode.trim();
                        corpName  = corpName != null ? corpName.trim() : "";
                        corpCls   = corpCls  != null ? corpCls.trim()  : "";

                        Optional<CompanyEntity> existing = companyRepository.findByStockCode(stockCode);
                        if (existing.isPresent()) {
                            // UPDATE: corp_name, corp_cls
                            CompanyEntity updated = existing.get().toBuilder()
                                    .corpName(corpName)
                                    .corpCls(corpCls)
                                    .build();
                            companyRepository.save(updated);
                        } else {
                            // INSERT: isinCd는 null 허용
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

    /**
     * Map에서 String 값 추출 (null 안전)
     */
    private String parseStr(Map<?, ?> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        String s = val.toString().trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * 금액 문자열 → Long 변환 (쉼표 제거, 빈문자열→null)
     */
    private Long parseAmount(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * ord 필드 → Integer 변환 (null 안전)
     */
    private Integer parseOrd(Map<String, Object> row, String key) {
        Object val = row.get(key);
        if (val == null) return null;
        try {
            return Integer.parseInt(val.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
