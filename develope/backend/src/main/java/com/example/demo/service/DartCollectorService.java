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
import java.io.InputStream;
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

    private static final String DART_BASE_URL = "https://opendart.fss.or.kr/api";
    private static final String CORP_CODE_URL = DART_BASE_URL + "/corpCode.xml";
    private static final String FINANCIAL_URL = DART_BASE_URL + "/fnlttSinglAcntAll.json";

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;
    private final FinancialRepository financialRepository;

    @Value("${dart.api.key}")
    private String dartApiKey;

    /**
     * DART corp_code.zip 다운로드 및 파싱
     * - stockCode → dartCorpCode 매핑 후 StockEntity 업데이트
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
                    StockEntity updated = StockEntity.builder()
                            .id(stock.getId())
                            .stockCode(stock.getStockCode())
                            .stockName(stock.getStockName())
                            .market(stock.getMarket())
                            .sector(stock.getSector())
                            .listingDate(stock.getListingDate())
                            .ceoName(stock.getCeoName())
                            .currentPrice(stock.getCurrentPrice())
                            .marketCap(stock.getMarketCap())
                            .sharesOutstanding(stock.getSharesOutstanding())
                            .per(stock.getPer())
                            .pbr(stock.getPbr())
                            .roe(stock.getRoe())
                            .eps(stock.getEps())
                            .bps(stock.getBps())
                            .debtRatio(stock.getDebtRatio())
                            .operatingMargin(stock.getOperatingMargin())
                            .dartCorpCode(corpCode)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    stockRepository.save(updated);
                }
            }

            log.info("[DART] corp_code 매핑 완료");

        } catch (Exception e) {
            log.error("[DART] corp_code 수집 실패 — graceful fallback: {}", e.getMessage());
        }
    }

    /**
     * DART 단일회사 주요계정 API 호출 — 재무제표 수집
     * - 매출액, 영업이익, 순이익, 자산, 부채, 자본 파싱
     * - FinancialEntity 생성/업데이트 후 저장
     */
    public void collectFinancials(String dartCorpCode, String stockCode, int year) {
        try {
            String url = FINANCIAL_URL
                    + "?crtfc_key=" + dartApiKey
                    + "&corp_code=" + dartCorpCode
                    + "&bsns_year=" + year
                    + "&reprt_code=11011"   // 사업보고서
                    + "&fs_div=CFS";        // 연결재무제표 우선

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"000".equals(response.get("status"))) {
                log.warn("[DART] 재무제표 응답 오류 — stockCode: {}, year: {}, status: {}",
                        stockCode, year, response != null ? response.get("status") : "null");
                return;
            }

            Object listObj = response.get("list");
            if (!(listObj instanceof List)) {
                log.warn("[DART] 재무제표 list 파싱 실패 — stockCode: {}, year: {}", stockCode, year);
                return;
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) listObj;

            Long revenue = extractAmount(items, "ifrs-full_Revenue", "매출액");
            Long operatingProfit = extractAmount(items, "dart_OperatingIncomeLoss", "영업이익");
            Long netIncome = extractAmount(items, "ifrs-full_ProfitLoss", "당기순이익");
            Long totalAssets = extractAmount(items, "ifrs-full_Assets", "자산총계");
            Long totalLiabilities = extractAmount(items, "ifrs-full_Liabilities", "부채총계");
            Long totalEquity = extractAmount(items, "ifrs-full_Equity", "자본총계");

            Optional<FinancialEntity> existingOpt = financialRepository.findByStockCodeAndYear(stockCode, year);

            FinancialEntity entity;
            if (existingOpt.isPresent()) {
                FinancialEntity existing = existingOpt.get();
                entity = FinancialEntity.builder()
                        .id(existing.getId())
                        .stockCode(stockCode)
                        .year(year)
                        .revenue(revenue)
                        .operatingProfit(operatingProfit)
                        .netIncome(netIncome)
                        .totalAssets(totalAssets)
                        .totalLiabilities(totalLiabilities)
                        .totalEquity(totalEquity)
                        .operatingMargin(existing.getOperatingMargin())
                        .debtRatio(existing.getDebtRatio())
                        .roe(existing.getRoe())
                        .eps(existing.getEps())
                        .bps(existing.getBps())
                        .per(existing.getPer())
                        .pbr(existing.getPbr())
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
            log.info("[DART] 재무제표 저장 완료 — stockCode: {}, year: {}", stockCode, year);

        } catch (Exception e) {
            log.error("[DART] 재무제표 수집 실패 — stockCode: {}, year: {}, error: {}",
                    stockCode, year, e.getMessage());
        }
    }

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
                        String corpCode = getTagValue(el, "corp_code");
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
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private Long extractAmount(List<Map<String, Object>> items, String accountId, String accountName) {
        for (Map<String, Object> item : items) {
            String id = String.valueOf(item.getOrDefault("account_id", ""));
            String name = String.valueOf(item.getOrDefault("account_nm", ""));
            if (accountId.equals(id) || accountName.equals(name)) {
                Object val = item.get("thstrm_amount");
                if (val != null) {
                    try {
                        return Long.parseLong(val.toString().replace(",", "").trim());
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
