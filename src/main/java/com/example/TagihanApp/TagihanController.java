package com.example.TagihanApp;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.netflix.servo.monitor.TimedStopwatch;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller // This means that this class is a Controller
@RequestMapping(path="/tagihan") // This means URL's start with /demo (after Application path)
public class TagihanController {

    @Autowired
    private TagihanRepository tagihanRepository;
    private WebClient.Builder builder;

    private String nasabahService = "http://10.10.30.:700";
    private String transaksiService = "http://10.10.30.49:7007";
    private String tabunganService = "http://10.10.30.32:7002";

    @GetMapping("")
    public @ResponseBody Tagihan getTagihan(@RequestBody Long idTagihan) {
        return tagihanRepository.findById(idTagihan).get();
    }

    @GetMapping("/{idTagihan}")
    public @ResponseBody Tagihan getTagihan2(@PathVariable("idTagihan") Long idTagihan) {
        return tagihanRepository.findById(idTagihan).get();
    }

    @GetMapping(path="/all")
    public @ResponseBody Iterable<Tagihan> getAllTagihan() {
        // This returns a JSON or XML with the users
        return tagihanRepository.findAll();
    }

    @GetMapping(path="/tes")
    public @ResponseBody Object testingSpring() throws JSONException {
        WebClient tesClient = WebClient.create(transaksiService);

        HashMap hash = new HashMap();
        hash.put("nomorNasabah", 456);
        hash.put("jenisTransaksi", 2);
        hash.put("statusTransaksi", 1);
        hash.put("logTransaksi", "Transaksi Tio 2");

        ResponseSpec responseTes = tesClient.post()
                .uri("/api/transaksi")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();

        return responseTes.bodyToMono(Object.class).block();
    }

    @GetMapping(path="/bayartagihan/{idTagihan}")
    public @ResponseBody String bayarTagihan(@PathVariable("idTagihan") Long idTagihan) throws JSONException {
        WebClient nasabahClient = WebClient.create(nasabahService);
        WebClient tabunganClient = WebClient.create(tabunganService);
        HashMap hash = new HashMap();

        Optional<Tagihan> tagihan = tagihanRepository.findById(idTagihan);
        if (tagihan.isEmpty()) { return "Tidak ditemukan tagihan dengan id"; }

        //cek idPenagih
//        hash.clear();
//        hash.put("", );
//
//        ResponseSpec responseTes = nasabahClient.put()
//                .uri("")
//                .body(Mono.just(hash), HashMap.class)
//                .retrieve();
//        responseTes.bodyToMono(Object.class).block();

//        //cek idYangDitagih
//        hash.clear();
//        hash.put("", );
//
//        ResponseSpec responseTes = nasabahClient.put()
//                .uri("")
//                .body(Mono.just(hash), HashMap.class)
//                .retrieve();
//        responseTes.bodyToMono(Object.class).block();

        //tagihan lunas
        tagihan.get().setTagihanLunas(Boolean.TRUE);

        //nambah tabungan penagih
        hash.clear();
        hash.put("nomorRekening", tagihan.get().getIdPenagih());
        hash.put("jumlah", tagihan.get().getTagihanNominal());

        ResponseSpec responseTes = tabunganClient.put()
                .uri("/tabungan/tambah_saldo")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        responseTes.bodyToMono(Object.class).block();

        //kurangin tabungan yang ditagih
        hash.clear();
        hash.put("nomorRekening", tagihan.get().getIdPenagih());
        hash.put("jumlah", tagihan.get().getTagihanNominal());

        responseTes = tabunganClient.put()
                .uri("/tabungan/kurangi_saldo")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        responseTes.bodyToMono(Object.class).block();

        return "Pembayaran tagihan berhasil";
    }

    @GetMapping(path="/buattagihan")
    public @ResponseBody String buatTagihan (@RequestBody Long idPenagih
            , @RequestBody Long idYangDitagih, @RequestBody BigDecimal tagihanNominal
            , @RequestBody Long tagihanDurasi, @RequestBody(required = false) String tagihanKeterangan) {

        Tagihan n = new Tagihan();

        //cek idPenagih
        n.setIdPenagih(idPenagih);

        //cek idYangDitagih
        n.setIdYangDitagih(idYangDitagih);

        if ( !(tagihanNominal.compareTo(BigDecimal.ZERO) > 0) ) { return "Tagihan cannot be zero"; }
        n.setTagihanNominal(tagihanNominal);

        n.setTagihanCreated(new Timestamp(System.currentTimeMillis()));
        n.setTagihanLunas(Boolean.FALSE);

        if (tagihanDurasi <= 0) { return "tagihan durasion is zero"; }
        n.setTagihanDurasi(tagihanDurasi);

        if (tagihanKeterangan.isEmpty()) { tagihanKeterangan = "-"; }
        n.setTagihanKeterangan(tagihanKeterangan);
        tagihanRepository.save(n);
        return "Saved";
    }

    @PatchMapping(path="")
    public @ResponseBody String editTagihan (@RequestBody Long idTagihan,
        @RequestBody(required = false) Optional<Long> idPenagih,
        @RequestBody(required = false) Optional<Long> idYangDitagih,
        @RequestBody(required = false) Optional<BigDecimal> tagihanNominal,
        @RequestBody(required = false) Optional<Boolean> tagihanLunas,
        @RequestBody(required = false) Optional<Timestamp> tagihanCreated,
        @RequestBody(required = false) Optional<Long> tagihanDurasi,
        @RequestBody(required = false) Optional<String> tagihanKeterangan) {

        Optional<Tagihan> on = tagihanRepository.findById(idTagihan);
        if (on.isEmpty()) { return "Failed to Find Tagihan by Id"; }
        Tagihan n = on.get();
        if (idPenagih.isPresent()) { n.setIdPenagih(idPenagih.get()); }
        if (idYangDitagih.isPresent()) { n.setIdYangDitagih(idYangDitagih.get()); }
        if (tagihanNominal.isPresent()) { n.setTagihanNominal(tagihanNominal.get()); }
        if (tagihanCreated.isPresent()) { n.setTagihanCreated(tagihanCreated.get()); }
        if (tagihanLunas.isPresent()) { n.setTagihanLunas(tagihanLunas.get()); }
        if (tagihanDurasi.isPresent()) { n.setTagihanDurasi(tagihanDurasi.get()); }
        if (tagihanKeterangan.isPresent()) { n.setTagihanKeterangan(tagihanKeterangan.get()); }
        tagihanRepository.save(n);
        return "Tagihan Modified Succesfully";
    }

    @PostMapping(path="") // Map ONLY POST Requests
    public @ResponseBody String addNewTagihan (@RequestBody Tagihan tagihan) {
        tagihanRepository.save(tagihan);
        return "Saved";
    }

    @PutMapping(path="")
    public @ResponseBody String replaceTagihan (@RequestBody Tagihan tagihan) {
        tagihanRepository.save(tagihan);
        return "Tagihan Replaced Succesfully";
    }

    @DeleteMapping(path="")
    public @ResponseBody String deleteTagihan(@RequestBody Long idTagihan) {
        // This returns a JSON or XML with the users
        if(tagihanRepository.existsById(idTagihan)) {
            tagihanRepository.deleteById(idTagihan);
            return "Tagihan Deleted Succesfully";
        } else {
            return "Failed to Find Tagihan by Id";
        }
    }

    @DeleteMapping(path="/{idTagihan}")
    public @ResponseBody String deleteTagihan2(@PathVariable("idTagihan") Long idTagihan) {
        if(tagihanRepository.existsById(idTagihan)) {
            tagihanRepository.deleteById(idTagihan);
            return "Tagihan Deleted Succesfully";
        } else {
            return "Failed to Find Tagihan by Id";
        }
    }

//    @DeleteMapping(path="/all")
//    public @ResponseBody String deleteAllTagihan() {
//        tagihanRepository.deleteAll();
//        return "All Tagihan Deleted Succesfully";
//    }
}
