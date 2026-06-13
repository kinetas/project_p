package com.example.demo.dto.stock;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StockSearchRequest {

    private String keyword;
    private String market;
    private Double minPer;
    private Double maxPer;
    private Double minRoe;
    private Double maxDebtRatio;
    private int page = 0;
    private int size = 20;
}
