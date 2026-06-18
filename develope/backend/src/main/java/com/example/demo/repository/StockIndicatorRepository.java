package com.example.demo.repository;

import com.example.demo.entity.StockIndicatorEntity;
import com.example.demo.entity.StockIndicatorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockIndicatorRepository extends JpaRepository<StockIndicatorEntity, StockIndicatorId> {
    List<StockIndicatorEntity> findByIdStockCode(String stockCode);
    Optional<StockIndicatorEntity> findTopByIdStockCodeOrderByIdCalcYearDesc(String stockCode);
}
