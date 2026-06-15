package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "financials",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stock_code", "year"})
    }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FinancialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "revenue")
    private Long revenue;

    @Column(name = "operating_profit")
    private Long operatingProfit;

    @Column(name = "net_income")
    private Long netIncome;

    @Column(name = "total_assets")
    private Long totalAssets;

    @Column(name = "total_liabilities")
    private Long totalLiabilities;

    @Column(name = "total_equity")
    private Long totalEquity;

    @Column(name = "operating_margin")
    private Double operatingMargin;

    @Column(name = "debt_ratio")
    private Double debtRatio;

    @Column(name = "roe")
    private Double roe;

    @Column(name = "eps")
    private Long eps;

    @Column(name = "bps")
    private Long bps;

    @Column(name = "per")
    private Double per;

    @Column(name = "pbr")
    private Double pbr;
}
