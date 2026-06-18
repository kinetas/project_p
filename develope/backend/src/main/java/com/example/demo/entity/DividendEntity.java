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

    @EmbeddedId
    private DividendId id;

    @Column(name = "crno")
    private String crno;

    @Column(name = "stck_issu_cmpy_nm")
    private String stckIssuCmpyNm;

    @Column(name = "dvdn_bas_dt")
    private String dvdnBasDt;

    @Column(name = "cash_dvdn_pay_dt")
    private String cashDvdnPayDt;

    @Column(name = "stck_hndv_dt")
    private String stckHndvDt;

    @Column(name = "isin_cd_nm")
    private String isinCdNm;

    @Column(name = "stck_dvdn_rcd")
    private String stckDvdnRcd;

    @Column(name = "stck_dvdn_rcd_nm")
    private String stckDvdnRcdNm;

    @Column(name = "trsnm_dpty_dcd")
    private String trsnmDptyDcd;

    @Column(name = "trsnm_dpty_dcd_nm")
    private String trsnmDptyDcdNm;

    @Column(name = "scrs_itms_kcd")
    private String scrsItmsKcd;

    @Column(name = "scrs_itms_kcd_nm")
    private String scrsItmsKcdNm;

    @Column(name = "stck_genr_dvdn_amt", precision = 19, scale = 4)
    private BigDecimal stckGenrDvdnAmt;

    @Column(name = "stck_grdn_dvdn_amt", precision = 19, scale = 4)
    private BigDecimal stckGrdnDvdnAmt;

    @Column(name = "stck_genr_cash_dvdn_rt", precision = 19, scale = 4)
    private BigDecimal stckGenrCashDvdnRt;

    @Column(name = "stck_genr_dvdn_rt", precision = 19, scale = 4)
    private BigDecimal stckGenrDvdnRt;

    @Column(name = "cash_grdn_dvdn_rt", precision = 19, scale = 4)
    private BigDecimal cashGrdnDvdnRt;

    @Column(name = "stck_grdn_dvdn_rt", precision = 19, scale = 4)
    private BigDecimal stckGrdnDvdnRt;

    @Column(name = "stck_par_prc", precision = 19, scale = 4)
    private BigDecimal stckParPrc;

    @Column(name = "stck_stac_md")
    private String stckStacMd;
}
