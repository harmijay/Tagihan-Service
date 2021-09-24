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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller // This means that this class is a Controller
@RequestMapping(path="/tagihan") // This means URL's start with /demo (after Application path)
public class TagihanController {

    @Autowired
    private TagihanRepository tagihanRepository;

    @Autowired
    private WebClient.Builder builder;

//    private String nasabahService = "http://10.10.30.:700";
//    private String tabunganService = "http://10.10.30.32:7002";
    private String nasabahService = "http://localhost:7004/tagihan";
    private String tabunganService = "http://localhost:7004/tagihan";
    private String transaksiService = "http://10.10.30.49:7007";
    private String tagihanService = "http://localhost:7004";

    @GetMapping("")
    public @ResponseBody Restponse getTagihan(@RequestBody Long idTagihan) {
        Optional<Tagihan> n = tagihanRepository.findById(idTagihan);
        Restponse rez = new Restponse(null, null, n.get());
        if (n.isEmpty()) {
            rez.setStatus("401");
            rez.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return rez;
        }
        rez.setStatus("200");
        rez.setMessage("succesful");
        return rez;
    }

    @GetMapping("/{idTagihan}")
    public @ResponseBody Restponse getTagihan2(@PathVariable("idTagihan") Long idTagihan) {
        Optional<Tagihan> n = tagihanRepository.findById(idTagihan);
        Restponse rez = new Restponse(null, null, n.get());
        if (n.isEmpty()) {
            rez.setStatus("401");
            rez.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return rez;
        }
        rez.setStatus("200");
        rez.setMessage("succesful");
        return rez;
    }

    @GetMapping(path="/all")
    public @ResponseBody Restponse getAllTagihan() {
        Iterable<Tagihan> n = tagihanRepository.findAll();
        Restponse rez = new Restponse(null, null, n);
        rez.setStatus("200");
        rez.setMessage("succesful");
        return rez;
    }

    @GetMapping(path="/tes1")
    public @ResponseBody Object tes1() throws JSONException {
        WebClient tesClient = WebClient.create(tagihanService);
//        WebClient tesClient = builder.baseUrl(tagihanService).build();

        HashMap hash = new HashMap();
        hash.put("idPenagih", 2);
        hash.put("idYangDitagih", 1);
        hash.put("tagihanNominal", 20);
        hash.put("tagihanKeterangan", "tes jumat tes 1 - 1");

        ResponseSpec responseTes = tesClient.post()
                .uri("/tagihan/")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();

        return responseTes.bodyToMono(Object.class).block();
    }

    @GetMapping(path="/tes2")
    public @ResponseBody Object tes2() {
        WebClient tesClient = WebClient.create(tagihanService);
//        WebClient tesClient = builder.baseUrl(tagihanService).build();

        ResponseSpec response = tesClient.get()
                .uri("/tagihan/all").retrieve();

        return response.toEntity(Object.class).block();
    }

    @GetMapping(path="/tes3")
    public @ResponseBody Object tes3() {
//        WebClient tesClient = WebClient.create(tagihanService);
        WebClient tesClient = builder.baseUrl(tagihanService).build();

        ResponseSpec response = tesClient.get()
                .uri("/tagihan/all").retrieve();

        return response.bodyToMono(Object.class).block();
    }

    @GetMapping(path="/tesTabungan")
    public @ResponseBody Object tes4() throws JSONException {
        Restponse rez = new Restponse();
        rez.setStatus("441");
        return rez;
    }

    @GetMapping(path="/tesNasabah")
    public @ResponseBody Restponse tes5() throws JSONException {
        Restponse rez = new Restponse();
        rez.setStatus("441");
        return rez;
    }

    @GetMapping(path="/bayartagihan")
    public @ResponseBody Restponse bayarTagihan(@RequestBody Long idTagihan) throws JSONException {
        WebClient nasabahClient = WebClient.create(nasabahService);
        WebClient tabunganClient = WebClient.create(tabunganService);
        HashMap hash = new HashMap();
        Restponse rez = new Restponse();

        Optional<Tagihan> tagihan = tagihanRepository.findById(idTagihan);
        if (tagihan.isEmpty()) {
            rez.setStatus("240");
            rez.setMessage("Could not Find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return rez;
        }

        //cek idPenagih
//        hash.clear();
//        hash.put("", );

        ResponseSpec responseNasabah = nasabahClient.put()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        Restponse rezNasabah = (Restponse) responseNasabah.bodyToMono(Object.class).block();
        if ( rezNasabah.getStatus() == "441" ) {
            rez.setStatus("472");
            rez.setMessage("Failed to Find Nasabah Penagih Account Associated with idTagihan=" + idTagihan + ".");
            return rez;
        }

        //cek idYangDitagih
//        hash.clear();
//        hash.put("", );

        responseNasabah = nasabahClient.put()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        rezNasabah = (Restponse) responseNasabah.bodyToMono(Object.class).block();
        if ( rezNasabah.getStatus() == "441" ) {
            rez.setStatus("471");
            rez.setMessage("Failed to Find Nasabah Yang Ditagih Account Associated with idTagihan=" + idTagihan + ".");
            return rez;
        }

        //tagihan lunas
        tagihan.get().setTagihanLunas(Boolean.TRUE);

        //nambah tabungan penagih
        hash.clear();
        hash.put("nomorRekening", tagihan.get().getIdPenagih());
        hash.put("jumlah", tagihan.get().getTagihanNominal());

        ResponseSpec responseTabungan = tabunganClient.put()
                .uri("tabungan/tambah_saldo")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        Restponse rezTabungan = (Restponse) responseTabungan.bodyToMono(Object.class).block();
        if ( rezTabungan.getStatus() == "431" ) {
            rez.setStatus("474");
            rez.setMessage("Failed to Increase Saldo Nasabah Penagih");
            return rez;
        }

        //kurangin tabungan yang ditagih
        hash.clear();
        hash.put("nomorRekening", tagihan.get().getIdYangDitagih());
        hash.put("jumlah", tagihan.get().getTagihanNominal());

        responseTabungan = tabunganClient.put()
                .uri("tabungan/kurangi_saldo")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        rezTabungan = (Restponse) responseTabungan.bodyToMono(Object.class).block();
        if ( rezTabungan.getStatus() == "431" ) {
            rez.setStatus("473");
            rez.setMessage("Saldo Nasabah Yang Ditagih Kurang");
            return rez;
        }

        tagihanRepository.save(tagihan.get());
        rez.setStatus("270");
        rez.setMessage("Tagihan Payment Succesful");
        rez.setData(tagihan.get());
        return rez;
    }

    @GetMapping(path="/buattagihan")
    public @ResponseBody Restponse buatTagihan (@RequestBody Long idPenagih
            , @RequestBody Long idYangDitagih, @RequestBody BigDecimal tagihanNominal
            , @RequestBody Long tagihanDurasi, @RequestBody(required = false) String tagihanKeterangan) {

        Tagihan n = new Tagihan();
        Restponse rez = new Restponse();
        WebClient nasabahClient = WebClient.create(nasabahService);

        //cek idPenagih
//        hash.clear();
//        hash.put("", );

        ResponseSpec responseNasabah = nasabahClient.get()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        Restponse rezNasabah = (Restponse) responseNasabah.bodyToMono(Object.class).block();
        if ( rezNasabah.getStatus() == "441" ) {
            rez.setStatus("473");
            rez.setMessage("Failed to Find Nasabah Penagih Account");
            return rez;
        }

        //cek idYangDitagih
//        hash.clear();
//        hash.put("", );

        responseNasabah = nasabahClient.get()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        rezNasabah = (Restponse) responseNasabah.bodyToMono(Object.class).block();
        if ( rezNasabah.getStatus() == "441" ) {
            rez.setStatus("472");
            rez.setMessage("Failed to Find Nasabah Yang Ditagih Account");
            return rez;
        }

        if ( !(tagihanNominal.compareTo(BigDecimal.ZERO) > 0) ) {
            rez.setStatus("471");
            rez.setMessage("Tagihan nominal cannot be 0 or below");
            return rez;
        }
        n.setTagihanNominal(tagihanNominal);

        n.setTagihanCreated(new Timestamp(System.currentTimeMillis()));
        n.setTagihanLunas(Boolean.FALSE);

        n.setTagihanDurasi(tagihanDurasi);

        if (tagihanKeterangan.isEmpty()) { tagihanKeterangan = "-"; }
        n.setTagihanKeterangan(tagihanKeterangan);

        tagihanRepository.save(n);
        rez.setStatus("270");
        rez.setMessage("Tagihan created succesfully");
        rez.setData(n);
        return rez;
    }

    @PatchMapping(path="")
    public @ResponseBody String editTagihan (@RequestBody Tagihan tagihan) {
//        Restponse rez = new Restponse();
//        if (n.isEmpty()) {
//            rez.setStatus("401");
//            rez.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
//            return rez;
//        }
//        rez.setStatus("200");
//        rez.setMessage("succesful");

        if (tagihan.getId() == null) { return "I need idTagihan"; }
        Optional<Tagihan> on = tagihanRepository.findById(tagihan.getId());
        if (on.isEmpty()) { return "Failed to Find Tagihan by Id"; }
        Tagihan n = on.get();
        if (tagihan.getIdPenagih() == null) { n.setIdPenagih(tagihan.getIdPenagih()); }
        if (tagihan.getIdYangDitagih() == null) { n.setIdYangDitagih(tagihan.getIdYangDitagih()); }
        if (tagihan.getTagihanNominal() == null) { n.setTagihanNominal(tagihan.getTagihanNominal()); }
        if (tagihan.getTagihanCreated() == null) { n.setTagihanCreated(tagihan.getTagihanCreated()); }
        if (tagihan.getTagihanLunas() == null) { n.setTagihanLunas(tagihan.getTagihanLunas()); }
        if (tagihan.getTagihanDurasi() == null) { n.setTagihanDurasi(tagihan.getTagihanDurasi()); }
        if (tagihan.getTagihanKeterangan() == null) { n.setTagihanKeterangan(tagihan.getTagihanKeterangan()); }
        tagihanRepository.save(n);
        return "Tagihan Modified Succesfully";
    }

    @PostMapping(path="") // Map ONLY POST Requests
    public @ResponseBody String addNewTagihan (@RequestBody Tagihan tagihan) {
//        Restponse rez = new Restponse();
//        if (n.isEmpty()) {
//            rez.setStatus("401");
//            rez.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
//            return rez;
//        }
//        rez.setStatus("200");
//        rez.setMessage("succesful");
//        System.out.println(tagihan.toString());
        tagihanRepository.save(tagihan);
        return "Saved";
    }

    @PutMapping(path="")
    public @ResponseBody String replaceTagihan (@RequestBody Tagihan tagihan) {
//        Restponse rez = new Restponse();
//        if (n.isEmpty()) {
//            rez.setStatus("401");
//            rez.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
//            return rez;
//        }
//        rez.setStatus("200");
//        rez.setMessage("succesful");
        tagihanRepository.save(tagihan);
        return "Tagihan Replaced Succesfully";
    }

    @DeleteMapping(path="")
    public @ResponseBody Restponse deleteTagihan(@RequestBody Long idTagihan) {

        Restponse rez = new Restponse();
        if(tagihanRepository.existsById(idTagihan)) {
            rez.setStatus("270");
            rez.setMessage("Data deleted succesfully");
            rez.setData(tagihanRepository.findById(idTagihan));
            tagihanRepository.deleteById(idTagihan);
            return rez;
        } else {
            rez.setStatus("440");
            rez.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return rez;
        }
    }

    @DeleteMapping(path="/{idTagihan}")
    public @ResponseBody Restponse deleteTagihan2(@PathVariable("idTagihan") Long idTagihan) {

        Restponse rez = new Restponse();
        if(tagihanRepository.existsById(idTagihan)) {
            rez.setStatus("270");
            rez.setMessage("Data deleted succesfully");
            rez.setData(tagihanRepository.findById(idTagihan));
            tagihanRepository.deleteById(idTagihan);
            return rez;
        } else {
            rez.setStatus("440");
            rez.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return rez;
        }
    }

//    @DeleteMapping(path="/all")
//    public @ResponseBody String deleteAllTagihan() {
//        tagihanRepository.deleteAll();
//        return "All Tagihan Deleted Succesfully";
//    }
}
