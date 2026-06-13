package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "stock_code", unique = true, nullable = false)
    private String stockCode;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "market")
    private String market;

    @Column(name = "sector")
    private String sector;

    @Column(name = "listing_date")
    private LocalDate listingDate;

    @Column(name = "ceo_name")
    private String ceoName;

    @Column(name = "current_price")
    private Long currentPrice;

    @Column(name = "market_cap")
    private Long marketCap;

    @Column(name = "shares_outstanding")
    private Long sharesOutstanding;

    @Column(name = "per")
    private Double per;

    @Column(name = "pbr")
    private Double pbr;

    @Column(name = "roe")
    private Double roe;

    @Column(name = "eps")
    private Long eps;

    @Column(name = "bps")
    private Long bps;

    @Column(name = "debt_ratio")
    private Double debtRatio;

    @Column(name = "operating_margin")
    private Double operatingMargin;

    @Column(name = "dart_corp_code")
    private String dartCorpCode;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
