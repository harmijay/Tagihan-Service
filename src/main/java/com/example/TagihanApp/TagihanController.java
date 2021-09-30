package com.example.TagihanApp;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.netflix.discovery.EurekaClient;
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

@Controller
@RequestMapping(path="/tagihan")
public class TagihanController {

    @Autowired
    private TagihanRepository tagihanRepository;

    @Autowired
    private EurekaClient eurekaClient;

    private String nasabahService = "NASABAHSERVICE";
    private String tabunganService = "TABUNGANSERVICE";

    @GetMapping("")
    public @ResponseBody Restponse getTagihan(@RequestBody Long idTagihan) {
        Optional<Tagihan> optionalTagihan = tagihanRepository.findById(idTagihan);
        Restponse response = new Restponse();
        if (optionalTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return response;
        }
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(optionalTagihan.get());
        return response;
    }

    @GetMapping("/{idTagihan}")
    public @ResponseBody Restponse getTagihan2(@PathVariable("idTagihan") Long idTagihan) {
        Optional<Tagihan> optionalTagihan = tagihanRepository.findById(idTagihan);
        Restponse response = new Restponse();
        if (optionalTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return response;
        }
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(optionalTagihan.get());
        return response;
    }

    @GetMapping(path="/all")
    public @ResponseBody Restponse getAllTagihan() {
        Iterable<Tagihan> tagihan = tagihanRepository.findAll();
        Restponse response = new Restponse();
        if (tagihan.iterator().hasNext()) {
            response.setStatus("401");
            response.setMessage("There is no tagihan data found.");
        }
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(tagihan);
        return response;
    }

    @GetMapping(path="/lunasitagihan")
    public @ResponseBody Restponse lunasiTagihan(@RequestBody Long idTagihan) throws JSONException {
        WebClient nasabahClient = WebClient.create(this.eurekaClient.getNextServerFromEureka(nasabahService, false).getHomePageUrl());
        WebClient tabunganClient = WebClient.create(this.eurekaClient.getNextServerFromEureka(tabunganService, false).getHomePageUrl());
        HashMap hash = new HashMap();
        Restponse response = new Restponse();

        Optional<Tagihan> tagihan = tagihanRepository.findById(idTagihan);
        if (tagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not Find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return response;
        }

//        hash.clear();
//        hash.put("", );

        ResponseSpec responseSpecNasabah = nasabahClient.put()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        Restponse responseNasabah = (Restponse) responseSpecNasabah.bodyToMono(Object.class).block();
        if ( responseNasabah.getStatus() == "441" ) {
            response.setStatus("472");
            response.setMessage("Failed to Find Nasabah Penagih Account Associated with idTagihan=" + idTagihan + ".");
            return response;
        }

//        hash.clear();
//        hash.put("", );

        responseSpecNasabah = nasabahClient.put()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        responseNasabah = (Restponse) responseSpecNasabah.bodyToMono(Object.class).block();
        if ( responseNasabah.getStatus() == "441" ) {
            response.setStatus("471");
            response.setMessage("Failed to Find Nasabah Yang Ditagih Account Associated with idTagihan=" + idTagihan + ".");
            return response;
        }

        tagihan.get().setTagihanIsLunas(Boolean.TRUE);

        hash.clear();
        hash.put("nomorRekening", tagihan.get().getIdPenagih());
        hash.put("jumlah", tagihan.get().getTagihanNominal());

        ResponseSpec responseSpecTabungan = tabunganClient.put()
                .uri("tabungan/tambah_saldo")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        Restponse responseTabungan = (Restponse) responseSpecTabungan.bodyToMono(Object.class).block();
        if ( responseTabungan.getStatus() == "431" ) {
            response.setStatus("474");
            response.setMessage("Failed to Increase Saldo Nasabah Penagih");
            return response;
        }

        hash.clear();
        hash.put("nomorRekening", tagihan.get().getIdYangDitagih());
        hash.put("jumlah", tagihan.get().getTagihanNominal());

        responseSpecTabungan = tabunganClient.put()
                .uri("tabungan/kurangi_saldo")
                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        responseTabungan = (Restponse) responseSpecTabungan.bodyToMono(Object.class).block();
        if ( responseTabungan.getStatus() == "431" ) {
            response.setStatus("473");
            response.setMessage("Saldo Nasabah Yang Ditagih Kurang");
            return response;
        }

        response.setStatus("270");
        response.setMessage("Tagihan Payment Succesful");
        response.setPayload(tagihanRepository.save(tagihan.get()));
        return response;
    }

    @GetMapping(path="/buattagihan")
    public @ResponseBody Restponse buatTagihan (@RequestBody Long idPenagih
            , @RequestBody Long idYangDitagih, @RequestBody BigDecimal tagihanNominal
            , @RequestBody Long tagihanDurasi, @RequestBody(required = false) String tagihanKeterangan) {

        Tagihan newTagihan = new Tagihan();
        Restponse response = new Restponse();
        WebClient nasabahClient = WebClient.create(this.eurekaClient.getNextServerFromEureka(nasabahService, false).getHomePageUrl());

//        hash.clear();
//        hash.put("", );

        ResponseSpec responseSpecNasabah = nasabahClient.get()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        Restponse responseNasabah = (Restponse) responseSpecNasabah.bodyToMono(Object.class).block();
        if ( responseNasabah.getStatus() == "441" ) {
            response.setStatus("473");
            response.setMessage("Failed to Find Nasabah Penagih Account");
            return response;
        }

//        hash.clear();
//        hash.put("", );

        responseSpecNasabah = nasabahClient.get()
                .uri("/tesNasabah")
//                .body(Mono.just(hash), HashMap.class)
                .retrieve();
        responseNasabah = (Restponse) responseSpecNasabah.bodyToMono(Object.class).block();
        if ( responseNasabah.getStatus() == "441" ) {
            response.setStatus("472");
            response.setMessage("Failed to Find Nasabah Yang Ditagih Account");
            return response;
        }

        if ( !(tagihanNominal.compareTo(BigDecimal.ZERO) > 0) ) {
            response.setStatus("471");
            response.setMessage("Tagihan nominal cannot be 0 or below");
            return response;
        }
        newTagihan.setTagihanNominal(tagihanNominal);
        newTagihan.setTagihanCreationDatetime(new Timestamp(System.currentTimeMillis()));
        newTagihan.setTagihanIsLunas(Boolean.FALSE);
        newTagihan.setTagihanDurasi(tagihanDurasi);
        if (tagihanKeterangan.isEmpty()) { tagihanKeterangan = "-"; }
        newTagihan.setTagihanKeterangan(tagihanKeterangan);

        tagihanRepository.save(newTagihan);
        response.setStatus("270");
        response.setMessage("Tagihan created succesfully");
        response.setPayload(newTagihan);
        return response;
    }

    @PatchMapping(path="")
    public @ResponseBody Restponse editTagihan (@RequestBody Tagihan oldTagihan) {
        Restponse response = new Restponse();
        if (oldTagihan.getIdTagihan() == null) {
            response.setStatus("401");
            response.setMessage("idTagihan is null.");
            return response;
        }
        Optional<Tagihan> optionalTagihan = tagihanRepository.findById(oldTagihan.getIdTagihan());
        if (optionalTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not find Tagihan with idTagihan=" + oldTagihan.getIdTagihan().toString() + ".");
            return response;
        }
        Tagihan newTagihan = optionalTagihan.get();
        if (newTagihan.getIdPenagih() == null) { newTagihan.setIdPenagih(oldTagihan.getIdPenagih()); }
        if (newTagihan.getIdYangDitagih() == null) { newTagihan.setIdYangDitagih(oldTagihan.getIdYangDitagih()); }
        if (newTagihan.getTagihanNominal() == null) { newTagihan.setTagihanNominal(oldTagihan.getTagihanNominal()); }
        if (newTagihan.getTagihanCreationDatetime() == null) { newTagihan.setTagihanCreationDatetime(oldTagihan.getTagihanCreationDatetime()); }
        if (newTagihan.getTagihanIsLunas() == null) { newTagihan.setTagihanIsLunas(oldTagihan.getTagihanIsLunas()); }
        if (newTagihan.getTagihanDurasi() == null) { newTagihan.setTagihanDurasi(oldTagihan.getTagihanDurasi()); }
        if (newTagihan.getTagihanKeterangan() == null) { newTagihan.setTagihanKeterangan(oldTagihan.getTagihanKeterangan()); }
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(tagihanRepository.save(newTagihan));
        return response;
    }

    @PostMapping(path="")
    public @ResponseBody Restponse postMapping (@RequestBody Tagihan tagihan) {
        return addNewTagihan(tagihan);
    }

    @PutMapping(path="")
    public @ResponseBody Restponse replaceTagihan (@RequestBody Tagihan newTagihan) {
        Optional<Tagihan> oldTagihan = tagihanRepository.findById(newTagihan.getIdTagihan());
        Restponse response = new Restponse();
        if (oldTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not find Tagihan with idTagihan=" + newTagihan.getIdTagihan().toString() + ".");
            return response;
        }
        response.setStatus("200");
        response.setMessage("succesful");
        tagihanRepository.save(newTagihan);
        response.setPayload(tagihanRepository.findById(newTagihan.getIdTagihan()));
        return response;
    }

    @DeleteMapping(path="")
    public @ResponseBody Restponse deleteTagihan(@RequestBody Long idTagihan) {

        Restponse response = new Restponse();
        if(tagihanRepository.existsById(idTagihan)) {
            response.setStatus("270");
            response.setMessage("Data deleted succesfully");
            response.setPayload(tagihanRepository.findById(idTagihan));
            tagihanRepository.deleteById(idTagihan);
            return response;
        } else {
            response.setStatus("440");
            response.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return response;
        }
    }

    public Restponse addNewTagihan (Tagihan tagihan) {
        Restponse response = new Restponse();
        Optional<String> tagihanValidation = tagihan.validateNewTagihan();
        if (tagihanValidation.isPresent()) {
            response.setStatus("401");
            response.setMessage(tagihanValidation.get());
            return response;
        }
        tagihan.prepareNewTagihan();
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(tagihanRepository.save(tagihan));
        return response;
    }

    @GetMapping(path="/tes1")
    public @ResponseBody Object tes1() throws JSONException {
        WebClient tesClient = WebClient.create(tabunganService);

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
}
