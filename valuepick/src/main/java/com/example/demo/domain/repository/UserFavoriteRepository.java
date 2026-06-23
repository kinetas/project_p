package com.example.demo.domain.repository;

import com.example.demo.domain.entity.UserFavorite;
import com.example.demo.domain.entity.UserFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, UserFavoriteId> {
    List<UserFavorite> findByUserId(String userId);
    boolean existsByUserIdAndStockCode(String userId, String stockCode);
    void deleteByUserIdAndStockCode(String userId, String stockCode);
}
