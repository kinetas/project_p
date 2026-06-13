package com.example.demo.controller;

import com.example.demo.dto.stock.StockDetailResponse;
import com.example.demo.dto.stock.StockListResponse;
import com.example.demo.dto.stock.StockSearchRequest;
import com.example.demo.dto.stock.Top10Response;
import com.example.demo.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * GET /api/stocks — 종목 목록 조회 (검색/필터/페이징)
     */
    @GetMapping
    public ResponseEntity<List<StockListResponse>> getStockList(
            @ModelAttribute StockSearchRequest request) {
        List<StockListResponse> result = stockService.getStockList(request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/stocks/{code} — 종목 상세 조회
     */
    @GetMapping("/{code}")
    public ResponseEntity<StockDetailResponse> getStockDetail(@PathVariable String code) {
        StockDetailResponse result = stockService.getStockDetail(code);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/stocks/top10?type=value|lowPer|highRoe — TOP10 조회
     */
    @GetMapping("/top10")
    public ResponseEntity<Top10Response> getTop10(
            @RequestParam(defaultValue = "value") String type) {
        Top10Response result = stockService.getTop10(type);
        return ResponseEntity.ok(result);
    }
}
