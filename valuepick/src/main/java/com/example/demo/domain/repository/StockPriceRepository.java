package com.example.demo.domain.repository;

import com.example.demo.domain.entity.StockPrice;
import com.example.demo.domain.entity.StockPriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, StockPriceId> {
    List<StockPrice> findBySrtnCdOrderByBasDtDesc(String srtnCd);
    List<StockPrice> findBySrtnCdAndBasDtBetween(String srtnCd, LocalDate start, LocalDate end);
    Optional<StockPrice> findTopBySrtnCdOrderByBasDtDesc(String srtnCd);
}
