package com.example.demo.dto.stock;

import com.example.demo.entity.CompanyEntity;
import com.example.demo.entity.StockIndicatorEntity;
import com.example.demo.entity.StockPriceEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockDetailResponse {

    private String stockCode;
    private String corpName;

    private String mrktCtg;
    private Long clpr;
    private Long vs;
    private java.math.BigDecimal fltRt;
    private Long lstgStCnt;
    private Long mrktTotAmt;

    private java.math.BigDecimal eps;
    private java.math.BigDecimal bps;
    private java.math.BigDecimal per;
    private java.math.BigDecimal pbr;
    private java.math.BigDecimal roe;
    private java.math.BigDecimal operatingMargin;
    private java.math.BigDecimal debtRatio;
    private java.math.BigDecimal dividendYield;

    public static StockDetailResponse from(CompanyEntity company, StockPriceEntity stockPrice, StockIndicatorEntity indicator) {
        return StockDetailResponse.builder()
                .stockCode(company.getStockCode())
                .corpName(company.getCorpName())
                .mrktCtg(stockPrice != null ? stockPrice.getMrktCtg() : null)
                .clpr(stockPrice != null ? stockPrice.getClpr() : null)
                .vs(stockPrice != null ? stockPrice.getVs() : null)
                .fltRt(stockPrice != null ? stockPrice.getFltRt() : null)
                .lstgStCnt(stockPrice != null ? stockPrice.getLstgStCnt() : null)
                .mrktTotAmt(stockPrice != null ? stockPrice.getMrktTotAmt() : null)
                .eps(indicator != null ? indicator.getEps() : null)
                .bps(indicator != null ? indicator.getBps() : null)
                .per(indicator != null ? indicator.getPer() : null)
                .pbr(indicator != null ? indicator.getPbr() : null)
                .roe(indicator != null ? indicator.getRoe() : null)
                .operatingMargin(indicator != null ? indicator.getOperatingMargin() : null)
                .debtRatio(indicator != null ? indicator.getDebtRatio() : null)
                .dividendYield(indicator != null ? indicator.getDividendYield() : null)
                .build();
    }
}
