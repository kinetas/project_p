package com.example.demo.domain.controller;

import com.example.demo.domain.dto.ExchangeDto;
import com.example.demo.domain.dto.MarketIndexDto;
import com.example.demo.domain.service.SimpleInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/info")
public class InfoController {

    @Autowired
    SimpleInfoService simpleInfoService;

    // 저PER 상위 5
    @GetMapping("/per")
    public ResponseEntity<List<Map<String, Object>>> getPER() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPER());
    }

    // 고ROE 상위 5
    @GetMapping("/roe")
    public ResponseEntity<List<Map<String, Object>>> getROE() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getROE());
    }

    // 저PBR 상위 5
    @GetMapping("/pbr")
    public ResponseEntity<List<Map<String, Object>>> getPBR() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getPBR());
    }

    // 고배당수익률 상위 5
    @GetMapping("/dividend-yield")
    public ResponseEntity<List<Map<String, Object>>> getDividendYield() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getDividendYield());
    }

    // TOP10
    @GetMapping("/top10")
    public ResponseEntity<List<Map<String, Object>>> getTOP10() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getTOP10());
    }

    // TOP100 전체
    @GetMapping("/top100")
    public ResponseEntity<List<Map<String, Object>>> getTOP100() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getTOP100());
    }

    // 전체 목록 (company + indicator + 최신 주가)
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getList() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getList());
    }

    // 필터 목록 (per, roe, pbr, dividendYield 최소/최대)
    @GetMapping("/list/filter")
    public ResponseEntity<Map<String, Object>> getListWithFilter(
            @RequestParam(required = false) Double perMin,
            @RequestParam(required = false) Double perMax,
            @RequestParam(required = false) Double roeMin,
            @RequestParam(required = false) Double roeMax,
            @RequestParam(required = false) Double pbrMin,
            @RequestParam(required = false) Double pbrMax,
            @RequestParam(required = false) Double dyMin,
            @RequestParam(required = false) Double dyMax) throws Exception {
        return ResponseEntity.ok(simpleInfoService.getListWithFilter(
                perMin, perMax, roeMin, roeMax, pbrMin, pbrMax, dyMin, dyMax));
    }

    // 기업명 검색
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String keyword) throws Exception {
        return ResponseEntity.ok(simpleInfoService.getSerachResult(keyword));
    }

    // 코스피
    @GetMapping("/kospi")
    public ResponseEntity<MarketIndexDto> getKOSPI() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getKOSPI());
    }

    // 환율
    @GetMapping("/exchange")
    public ResponseEntity<ExchangeDto> getExchange() throws Exception {
        return ResponseEntity.ok(simpleInfoService.getExchange());
    }
}
