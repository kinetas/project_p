package com.example.demo.dto.stock;

import com.example.demo.entity.FinancialEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FinancialResponse {

    // 연도
    private Integer year;

    // 손익계산서
    private Long revenue;
    private Long operatingProfit;
    private Long netIncome;

    // 재무상태표
    private Long totalAssets;
    private Long totalLiabilities;
    private Long totalEquity;

    // 비율 지표
    private Double operatingMargin;
    private Double debtRatio;
    private Double roe;

    // 투자지표
    private Long eps;
    private Long bps;
    private Double per;
    private Double pbr;

    public static FinancialResponse from(FinancialEntity entity) {
        return FinancialResponse.builder()
                .year(entity.getYear())
                .revenue(entity.getRevenue())
                .operatingProfit(entity.getOperatingProfit())
                .netIncome(entity.getNetIncome())
                .totalAssets(entity.getTotalAssets())
                .totalLiabilities(entity.getTotalLiabilities())
                .totalEquity(entity.getTotalEquity())
                .operatingMargin(entity.getOperatingMargin())
                .debtRatio(entity.getDebtRatio())
                .roe(entity.getRoe())
                .eps(entity.getEps())
                .bps(entity.getBps())
                .per(entity.getPer())
                .pbr(entity.getPbr())
                .build();
    }
}
