package com.example.demo.dto.stock;

import com.example.demo.entity.StockEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockListResponse {

    private String stockCode;
    private String stockName;
    private String market;
    private Long currentPrice;
    private Long marketCap;
    private Double per;
    private Double pbr;
    private Double roe;
    private Double debtRatio;
    private Double changeRate;
    private Long changeAmount;
    private Double dividendYield;

    public static StockListResponse from(StockEntity entity) {
        return StockListResponse.builder()
                .stockCode(entity.getStockCode())
                .stockName(entity.getStockName())
                .market(entity.getMarket())
                .currentPrice(entity.getCurrentPrice())
                .marketCap(entity.getMarketCap())
                .per(entity.getPer())
                .pbr(entity.getPbr())
                .roe(entity.getRoe())
                .debtRatio(entity.getDebtRatio())
                .changeRate(entity.getChangeRate() != null ? entity.getChangeRate() : 0.0)
                .changeAmount(entity.getChangeAmount() != null ? entity.getChangeAmount() : 0L)
                .dividendYield(0.0)
                .build();
    }
}
