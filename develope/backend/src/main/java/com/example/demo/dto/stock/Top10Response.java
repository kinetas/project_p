package com.example.demo.dto.stock;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Top10Response {

    private String type;
    private List<StockListResponse> stocks;
}
