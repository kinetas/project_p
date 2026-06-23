package com.example.demo.domain.repository;

import com.example.demo.domain.entity.DividendInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DividendInfoRepository extends JpaRepository<DividendInfo, String> {
    Optional<DividendInfo> findByCorpCode(String corpCode);
}
