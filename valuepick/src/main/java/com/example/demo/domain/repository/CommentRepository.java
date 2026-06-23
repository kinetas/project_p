package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Comment;
import com.example.demo.domain.entity.InvestmentJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByJournalOrderByCreatedAtAsc(InvestmentJournal journal);
    List<Comment> findByJournalIdOrderByCreatedAtAsc(Long journalId);
    List<Comment> findByUserId(String userId);
}
