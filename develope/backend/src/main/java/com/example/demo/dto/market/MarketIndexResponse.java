package com.example.demo.dto.market;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketIndexResponse {
    private String name;
    private String value;
    private Double changeRate;
    private Double changeAmount;
}
