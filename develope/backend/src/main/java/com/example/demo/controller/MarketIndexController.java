package com.example.demo.controller;

import com.example.demo.dto.market.MarketIndexResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketIndexController {

    /**
     * GET /api/market/indices — 시장 지표 조회
     */
    @GetMapping("/indices")
    public ResponseEntity<List<MarketIndexResponse>> getMarketIndices() {
        List<MarketIndexResponse> indices = List.of(
            MarketIndexResponse.builder().name("KOSPI").value("2,678.22").changeRate(1.24).changeAmount(32.84).build(),
            MarketIndexResponse.builder().name("KOSDAQ").value("758.45").changeRate(-0.68).changeAmount(-5.19).build(),
            MarketIndexResponse.builder().name("USD/KRW").value("1,328.5").changeRate(0.15).changeAmount(2.00).build(),
            MarketIndexResponse.builder().name("JPY/KRW").value("8.95").changeRate(-0.22).changeAmount(-0.02).build()
        );
        return ResponseEntity.ok(indices);
    }
}
