package com.example.demo.repository;

import com.example.demo.entity.FinancialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRepository extends JpaRepository<FinancialEntity, Long> {

    // 종목별 전체 연도 (최신 순)
    List<FinancialEntity> findByStockCodeOrderByYearDesc(String stockCode);

    // 최근 5년
    List<FinancialEntity> findTop5ByStockCodeOrderByYearDesc(String stockCode);

    // 특정 종목 + 특정 연도
    Optional<FinancialEntity> findByStockCodeAndYear(String stockCode, Integer year);
}
