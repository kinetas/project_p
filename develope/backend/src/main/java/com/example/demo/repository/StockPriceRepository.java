package com.example.demo.repository;

import com.example.demo.entity.StockPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StockPriceRepository extends JpaRepository<StockPriceEntity, Long> {
    Optional<StockPriceEntity> findTopBySrtnCdOrderByBasDtDesc(String srtnCd);
}
