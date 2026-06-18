package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "dividend_info")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DividendEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "isinCd", length = 12, nullable = false)
    private String isinCd;

    @Column(name = "basDt", length = 8, nullable = false)
    private String basDt;

    @Column(name = "crno")
    private String crno;

    @Column(name = "stckIssuCmpyNm")
    private String stckIssuCmpyNm;

    @Column(name = "dvdnBasDt")
    private String dvdnBasDt;

    @Column(name = "cashDvdnPayDt")
    private String cashDvdnPayDt;

    @Column(name = "stckHndvDt")
    private String stckHndvDt;

    @Column(name = "isinCdNm")
    private String isinCdNm;

    @Column(name = "stckDvdnRcd")
    private String stckDvdnRcd;

    @Column(name = "stckDvdnRcdNm")
    private String stckDvdnRcdNm;

    @Column(name = "trsnmDptyDcd")
    private String trsnmDptyDcd;

    @Column(name = "trsnmDptyDcdNm")
    private String trsnmDptyDcdNm;

    @Column(name = "scrsItmsKcd")
    private String scrsItmsKcd;

    @Column(name = "scrsItmsKcdNm")
    private String scrsItmsKcdNm;

    @Column(name = "stckGenrDvdnAmt", precision = 22, scale = 3)
    private BigDecimal stckGenrDvdnAmt;

    @Column(name = "stckGrdnDvdnAmt", precision = 22, scale = 3)
    private BigDecimal stckGrdnDvdnAmt;

    @Column(name = "stckGenrCashDvdnRt", precision = 26, scale = 10)
    private BigDecimal stckGenrCashDvdnRt;

    @Column(name = "stckGenrDvdnRt", precision = 26, scale = 10)
    private BigDecimal stckGenrDvdnRt;

    @Column(name = "cashGrdnDvdnRt", precision = 26, scale = 10)
    private BigDecimal cashGrdnDvdnRt;

    @Column(name = "stckGrdnDvdnRt", precision = 26, scale = 10)
    private BigDecimal stckGrdnDvdnRt;

    @Column(name = "stckParPrc", precision = 22, scale = 3)
    private BigDecimal stckParPrc;

    @Column(name = "stckStacMd")
    private String stckStacMd;
}
