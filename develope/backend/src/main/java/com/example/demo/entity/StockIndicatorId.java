package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StockIndicatorId implements Serializable {

    @Column(name = "stock_code", length = 6, nullable = false)
    private String stockCode;

    @Column(name = "calc_year", length = 4, nullable = false)
    private String calcYear;
}
