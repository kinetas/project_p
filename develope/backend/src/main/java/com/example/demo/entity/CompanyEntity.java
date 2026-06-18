package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CompanyEntity {

    @Id
    @Column(name = "corp_code", length = 8, nullable = false)
    private String corpCode;

    @Column(name = "stock_code", length = 6, nullable = false, unique = true)
    private String stockCode;

    @Column(name = "isinCd", length = 12, unique = true)
    private String isinCd;

    @Column(name = "corp_name", length = 100, nullable = false)
    private String corpName;

    @Column(name = "corp_cls", length = 1, nullable = false)
    private String corpCls;
}
