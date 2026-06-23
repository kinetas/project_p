package com.example.demo.domain.repository;

import com.example.demo.domain.entity.InvestmentJournal;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvestmentJournalRepository extends JpaRepository<InvestmentJournal, Long> {
    List<InvestmentJournal> findByUserOrderByCreatedAtDesc(User user);
    List<InvestmentJournal> findByUserIdOrderByCreatedAtDesc(String userId);
}
