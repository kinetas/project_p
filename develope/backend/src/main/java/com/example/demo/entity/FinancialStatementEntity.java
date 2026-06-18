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

    @Column(name = "account_nm", length = 100, nullable = false)
    private String accountNm;

    @Column(name = "fs_div", length = 5, nullable = false)
    private String fsDiv;

    @Column(name = "fs_nm", length = 50)
    private String fsNm;

    @Column(name = "sj_div", length = 5, nullable = false)
    private String sjDiv;

    @Column(name = "sj_nm", length = 50)
    private String sjNm;

    @Column(name = "thstrm_nm", length = 50)
    private String thstrmNm;

    @Column(name = "thstrm_dt", length = 30)
    private String thstrmDt;

    @Column(name = "thstrm_amount")
    private Long thstrmAmount;

    @Column(name = "thstrm_add_amount")
    private Long thstrmAddAmount;

    @Column(name = "frmtrm_nm", length = 50)
    private String frmtrmNm;

    @Column(name = "frmtrm_dt", length = 30)
    private String frmtrmDt;

    @Column(name = "frmtrm_amount")
    private Long frmtrmAmount;

    @Column(name = "frmtrm_add_amount")
    private Long frmtrmAddAmount;

    @Column(name = "bfefrmtrm_nm", length = 50)
    private String bfefrmtrmNm;

    @Column(name = "bfefrmtrm_dt", length = 30)
    private String bfefrmtrmDt;

    @Column(name = "bfefrmtrm_amount")
    private Long bfefrmtrmAmount;

    @Column(name = "ord")
    private Integer ord;

    @Column(name = "currency", length = 10)
    private String currency;
}
