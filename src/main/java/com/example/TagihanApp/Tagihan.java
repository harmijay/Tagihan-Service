package com.example.TagihanApp;

import lombok.*;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
public class Tagihan {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long idTagihan;

    private Long idPenagih;

    private Long idYangDitagih;

    private BigDecimal tagihanNominal;

    private Boolean tagihanIsLunas;

    private Timestamp tagihanCreationDatetime;

    private Long tagihanDurasi;

    private String tagihanKeterangan;

    public Optional<String> validateNewTagihan() {
        if (this.idPenagih == null) return Optional.of("ID penagih is null");
        if (this.idYangDitagih == null) return Optional.of("ID yang ditagih is null");
        if (this.tagihanNominal.compareTo(BigDecimal.valueOf(0.0)) == -1) return Optional.of("Nominal tagihan cannot be negative");
        if (this.tagihanNominal.compareTo(BigDecimal.valueOf(0.0)) == 0) return Optional.of("Nominal tagihan cannot be zero");
        if (this.tagihanDurasi == null) return Optional.of("Durasi tagihan is null");
        return null;
    }

    public Void prepareNewTagihan() {
        this.tagihanIsLunas = Boolean.FALSE;
        this.tagihanCreationDatetime = new Timestamp(System.currentTimeMillis());
        if (this.tagihanKeterangan == null) this.tagihanKeterangan = " - ";
        return null;
    }
}