package com.example.TagihanApp;

import lombok.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity // This tells Hibernate to make a table out of this class
@Data @NoArgsConstructor @AllArgsConstructor
public class Tagihan {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    public Long idPenagih;

    public Long idYangDitagih;

    public BigDecimal nominalTagihan;
}