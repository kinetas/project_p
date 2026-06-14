package com.example.demo.repository;

import com.example.demo.entity.StockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<StockEntity, Long> {

    Optional<StockEntity> findByStockCode(String stockCode);

    // 저PER TOP10
    List<StockEntity> findTop10ByPerIsNotNullOrderByPerAsc();

    // 고ROE TOP10
    List<StockEntity> findTop10ByRoeIsNotNullOrderByRoeDesc();

    // 종목명 또는 종목코드 검색
    Page<StockEntity> findByStockNameContainingOrStockCodeContaining(
            String stockName, String stockCode, Pageable pageable);

    // 저PBR TOP10
    List<StockEntity> findTop10ByPbrIsNotNullOrderByPbrAsc();

    // 가치주 TOP10: PER 낮고, PBR 낮고, ROE 높고, 부채비율 낮은 순 (복합 정렬)
    @Query("SELECT s FROM StockEntity s " +
           "WHERE s.per IS NOT NULL AND s.pbr IS NOT NULL " +
           "AND s.roe IS NOT NULL AND s.debtRatio IS NOT NULL " +
           "ORDER BY s.per ASC, s.pbr ASC, s.roe DESC, s.debtRatio ASC")
    List<StockEntity> findTop10ValueStocks(Pageable pageable);
}
