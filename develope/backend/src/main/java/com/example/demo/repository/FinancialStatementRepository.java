package com.example.demo.repository;

import com.example.demo.entity.FinancialStatementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatementEntity, Long> {

    Optional<FinancialStatementEntity> findByStockCodeAndBsnsYear(String stockCode, String bsnsYear);

    @Modifying
    @Query("DELETE FROM FinancialStatementEntity f WHERE f.stockCode = :stockCode AND f.bsnsYear = :bsnsYear")
    void deleteByStockCodeAndBsnsYear(@Param("stockCode") String stockCode, @Param("bsnsYear") String bsnsYear);

    @Query("SELECT MAX(f.bsnsYear) FROM FinancialStatementEntity f WHERE f.stockCode = :stockCode")
    Optional<String> findTopBsnsYearByStockCode(@Param("stockCode") String stockCode);

    @Query("SELECT DISTINCT f.bsnsYear FROM FinancialStatementEntity f WHERE f.stockCode = :stockCode ORDER BY f.bsnsYear DESC")
    List<String> findDistinctBsnsYearsByStockCodeOrderByDesc(@Param("stockCode") String stockCode);
}
