package com.example.demo.dto.stock;

import com.example.demo.entity.StockEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StockDetailResponse {

    private String stockCode;
    private String stockName;
    private String market;
    private String sector;
    private LocalDate listingDate;
    private String ceoName;

    private Long currentPrice;
    private Long marketCap;
    private Double per;
    private Double pbr;
    private Double roe;

    private Long eps;
    private Long bps;
    private Double debtRatio;
    private Double operatingMargin;

    public static StockDetailResponse from(StockEntity entity) {
        return StockDetailResponse.builder()
                .stockCode(entity.getStockCode())
                .stockName(entity.getStockName())
                .market(entity.getMarket())
                .sector(entity.getSector())
                .listingDate(entity.getListingDate())
                .ceoName(entity.getCeoName())
                .currentPrice(entity.getCurrentPrice())
                .marketCap(entity.getMarketCap())
                .per(entity.getPer())
                .pbr(entity.getPbr())
                .roe(entity.getRoe())
                .eps(entity.getEps())
                .bps(entity.getBps())
                .debtRatio(entity.getDebtRatio())
                .operatingMargin(entity.getOperatingMargin())
                .build();
    }
}
