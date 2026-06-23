package com.example.demo.domain.repository;

import com.example.demo.domain.entity.MarketIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarketIndexRepository extends JpaRepository<MarketIndex, Long> {
    List<MarketIndex> findByBasDd(LocalDate basDd);
    List<MarketIndex> findByIdxNmOrderByBasDdDesc(String idxNm);
    Optional<MarketIndex> findTop1ByIdxNmOrderByBasDdDesc(String idxNm);
}
