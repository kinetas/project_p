package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_price")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class StockPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "basDt", length = 8, nullable = false)
    private String basDt;

    @Column(name = "srtnCd", length = 6, nullable = false)
    private String srtnCd;

    @Column(name = "isinCd", length = 12)
    private String isinCd;

    @Column(name = "itmsNm", length = 100)
    private String itmsNm;

    @Column(name = "mrktCtg", length = 10)
    private String mrktCtg;

    @Column(name = "clpr")
    private Long clpr;

    @Column(name = "vs")
    private Long vs;

    @Column(name = "fltRt", precision = 8, scale = 2)
    private java.math.BigDecimal fltRt;

    @Column(name = "mkp")
    private Long mkp;

    @Column(name = "hipr")
    private Long hipr;

    @Column(name = "lopr")
    private Long lopr;

    @Column(name = "trqu")
    private Long trqu;

    @Column(name = "trPrc")
    private Long trPrc;

    @Column(name = "lstgStCnt")
    private Long lstgStCnt;

    @Column(name = "mrktTotAmt")
    private Long mrktTotAmt;
}
