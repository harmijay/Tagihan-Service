package com.example.TagihanApp;

import lombok.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity // This tells Hibernate to make a table out of this class
@Data
public class Tagihan {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private Long idPenagih;

    private Long idYangDitagih;

    private BigDecimal tagihanNominal;

    private Boolean tagihanLunas;

    private Timestamp tagihanCreated;

    private Long tagihanDurasi;

    private String tagihanKeterangan;

//    public Tagihan (Long idPenagih, Long idYangDitagih, BigDecimal tagihanNominal, Boolean tagihanLunas
//            , Timestamp tagihanCreated, Long tagihanDurasi, String tagihanKeterangan) {
//        this.idPenagih = idPenagih;
//        this.idYangDitagih = idYangDitagih;
//        this.tagihanNominal = tagihanNominal;
//        this.tagihanLunas = tagihanLunas;
//        this.tagihanCreated = tagihanCreated;
//        this.tagihanDurasi = tagihanDurasi;
//        this.tagihanKeterangan = tagihanKeterangan;
//    }
}