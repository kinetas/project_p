package com.example.demo.repository;

import com.example.demo.entity.DividendEntity;
import com.example.demo.entity.DividendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, DividendId> {
}
