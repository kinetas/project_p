package com.example.demo.domain.repository;

import com.example.demo.domain.entity.Company;
import com.example.demo.domain.entity.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {
    List<FinancialStatement> findByCompany(Company company);
    List<FinancialStatement> findByCompanyAndBsnsYear(Company company, String bsnsYear);
    List<FinancialStatement> findByCompanyAndFsDiv(Company company, String fsDiv);
}
