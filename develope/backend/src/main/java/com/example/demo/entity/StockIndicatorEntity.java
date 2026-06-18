package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_indicator")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class StockIndicatorEntity {

    @EmbeddedId
    private StockIndicatorId id;

    @Column(name = "bsns_year", length = 4, nullable = false)
    private String bsnsYear;

    @Column(name = "reprt_code", length = 5, nullable = false)
    private String reprtCode;

    @Column(name = "base_dt", length = 8, nullable = false)
    private String baseDt;

    @Column(name = "eps", precision = 15, scale = 2)
    private BigDecimal eps;

    @Column(name = "bps", precision = 15, scale = 2)
    private BigDecimal bps;

    @Column(name = "per", precision = 10, scale = 2)
    private BigDecimal per;

    @Column(name = "pbr", precision = 10, scale = 2)
    private BigDecimal pbr;

    @Column(name = "roe", precision = 10, scale = 2)
    private BigDecimal roe;

    @Column(name = "operating_margin", precision = 10, scale = 2)
    private BigDecimal operatingMargin;

    @Column(name = "debt_ratio", precision = 10, scale = 2)
    private BigDecimal debtRatio;

    @Column(name = "dividend_yield", precision = 10, scale = 2)
    private BigDecimal dividendYield;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
