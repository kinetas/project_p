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
public class StockPriceId implements Serializable {

    @Column(name = "basDt", length = 8, nullable = false)
    private String basDt;

    @Column(name = "srtnCd", length = 6, nullable = false)
    private String srtnCd;
}
