package com.example.demo.dto.stock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FinancialResponse {

    private String year;
    private Long revenue;
    private Long operatingProfit;
    private Long netIncome;
    private Long totalAssets;
    private Long totalLiabilities;
    private Long totalEquity;
}
