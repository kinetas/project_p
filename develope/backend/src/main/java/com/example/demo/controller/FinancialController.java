package com.example.demo.controller;

import com.example.demo.dto.stock.FinancialResponse;
import com.example.demo.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class FinancialController {

    private final StockService stockService;

    /**
     * GET /api/stocks/{code}/financials — 최근 5년 재무제표 조회
     */
    @GetMapping("/{code}/financials")
    public ResponseEntity<List<FinancialResponse>> getFinancials(@PathVariable String code) {
        List<FinancialResponse> result = stockService.getFinancials(code);
        return ResponseEntity.ok(result);
    }
}
