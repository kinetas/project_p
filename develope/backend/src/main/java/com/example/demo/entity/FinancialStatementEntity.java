package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "financial_statement")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FinancialStatementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bsns_year", length = 4, nullable = false)
    private String bsnsYear;

    @Column(name = "stock_code", length = 6, nullable = false)
    private String stockCode;

    @Column(name = "reprt_code", length = 5, nullable = false)
    private String reprtCode;

    @Column(name = "fs_div", length = 5, nullable = false)
    private String fsDiv;

    @Column(name = "period_nm", length = 50)
    private String periodNm;

    @Column(name = "period_dt", length = 50)
    private String periodDt;

    @Column(name = "current_assets")
    private Long currentAssets;

    @Column(name = "non_current_assets")
    private Long nonCurrentAssets;

    @Column(name = "total_assets")
    private Long totalAssets;

    @Column(name = "current_liabilities")
    private Long currentLiabilities;

    @Column(name = "non_current_liabilities")
    private Long nonCurrentLiabilities;

    @Column(name = "total_liabilities")
    private Long totalLiabilities;

    @Column(name = "capital_stock")
    private Long capitalStock;

    @Column(name = "retained_earnings")
    private Long retainedEarnings;

    @Column(name = "total_equity")
    private Long totalEquity;

    @Column(name = "revenue")
    private Long revenue;

    @Column(name = "operating_income")
    private Long operatingIncome;

    @Column(name = "income_before_tax")
    private Long incomeBeforeTax;

    @Column(name = "net_income")
    private Long netIncome;

    @Column(name = "currency", length = 10)
    private String currency;
}
