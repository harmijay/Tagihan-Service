package com.example.TagihanApp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Optional;

@Service
public class TagihanService {

    @Autowired
    private TagihanRepository tagihanRepository;
    private WebClient nasabahClient;
    private WebClient tabunganClient;

    @Autowired
    private WebClient tagihanClient;

    public Restponse getTagihan(Long idTagihan) {
        Optional<Tagihan> queryTagihan = tagihanRepository.findById(idTagihan);
        Restponse response = new Restponse();
        if (queryTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not find Tagihan with idTagihan=" + idTagihan.toString() + ".");
        } else {
            response.setStatus("200");
            response.setMessage("succesful");
        }
        response.setPayload(queryTagihan.get());
        return response;
    }

    public Restponse getAllTagihan() {
        Iterable<Tagihan> allTagihan = tagihanRepository.findAll();
        Restponse response = new Restponse();
        if (allTagihan.iterator().hasNext()) {
            response.setStatus("200");
            response.setMessage("succesful");
        } else {
            response.setStatus("401");
            response.setMessage("There is no tagihan data found.");
        }
        response.setPayload(allTagihan);
        return response;
    }

    public Restponse lunasiTagihan(Long idTagihan) {
        Restponse response = new Restponse();

        Optional<Tagihan> queryTagihan = tagihanRepository.findById(idTagihan);
        if (queryTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not Find Tagihan with idTagihan=" + idTagihan.toString() + ".");
            return response;
        }
        Tagihan tagihan = queryTagihan.get();

        if (isPenagihValid(tagihan)) {
            response.setStatus("471");
            response.setMessage("Failed to Find Nasabah Penagih Account Associated with idTagihan=" + idTagihan + ".");
            return response;
        }
        if (isDitagihValid(tagihan)) {
            response.setStatus("472");
            response.setMessage("Failed to Find Nasabah Yang Ditagih Account Associated with idTagihan=" + idTagihan + ".");
            return response;
        }
        if (transferPembayaranTagihan(tagihan)) {
            response.setStatus("473");
            response.setMessage("Transfer saldo pembayaran tagihan gagal dilakukan.");
            return response;
        }

        tagihan.setTagihanIsLunas(Boolean.TRUE);

        response.setStatus("270");
        response.setMessage("Tagihan Payment Succesful");
        response.setPayload(tagihanRepository.save(tagihan));
        return response;
    }

    public Restponse addNewTagihan(Tagihan newTagihan) {
        Restponse response = new Restponse();

        if (isPenagihValid(newTagihan)) {
            response.setStatus("471");
            response.setMessage("Failed to Find Nasabah Penagih Account Associated with idTagihan=" + newTagihan.getIdPenagih().toString() + ".");
            return response;
        }
        if (isDitagihValid(newTagihan)) {
            response.setStatus("472");
            response.setMessage("Failed to Find Nasabah Yang Ditagih Account Associated with idTagihan=" + newTagihan.getIdYangDitagih().toString() + ".");
            return response;
        }

        Optional<String> tagihanValidation = newTagihan.validateNewTagihan();
        if (tagihanValidation.isPresent()) {
            response.setStatus("401");
            response.setMessage(tagihanValidation.get());
            return response;
        }
        newTagihan.prepareNewTagihan();
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(tagihanRepository.save(newTagihan));
        return response;
    }

    public Restponse editTagihan(Tagihan newTagihan) {
        Restponse response = new Restponse();

        if (newTagihan.getIdTagihan() == null) {
            response.setStatus("401");
            response.setMessage("idTagihan is null.");
            return response;
        }
        Optional<Tagihan> queryTagihan = tagihanRepository.findById(newTagihan.getIdTagihan());
        if (queryTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not find Tagihan with idTagihan=" + newTagihan.getIdTagihan().toString() + ".");
            return response;
        }
        Tagihan editedTagihan = replaceTagihanValue(queryTagihan.get(), newTagihan);
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(tagihanRepository.save(editedTagihan));
        return response;
    }

    public Restponse replaceTagihan(Tagihan newTagihan) {
        Restponse response = new Restponse();

        if (newTagihan.getIdTagihan() == null) {
            response.setStatus("401");
            response.setMessage("idTagihan is null.");
            return response;
        }
        Optional<Tagihan> queryTagihan = tagihanRepository.findById(newTagihan.getIdTagihan());
        if (queryTagihan.isEmpty()) {
            response.setStatus("401");
            response.setMessage("Could not find Tagihan with idTagihan=" + newTagihan.getIdTagihan().toString() + ".");
            return response;
        }
        Optional<String> validationError= newTagihan.validateNewTagihan();
        if (validationError.isPresent()) {
            response.setStatus("400");
            response.setPayload(validationError.get());
            return response;
        }
        response.setStatus("200");
        response.setMessage("succesful");
        response.setPayload(tagihanRepository.save(newTagihan));
        return response;
    }

    public Restponse deleteTagihan(Long idTagihan) {
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

    public Boolean isPenagihValid(Tagihan tagihan) {
        return isNasabahValid(tagihan.getIdPenagih());
    }

    public Boolean isDitagihValid(Tagihan tagihan) {
        return isNasabahValid(tagihan.getIdYangDitagih());
    }

    public Boolean isNasabahValid(Long idNasabah) {
        HashMap requestBody = new HashMap();
        requestBody.put("noRekening",idNasabah.toString());

        WebClient.ResponseSpec responseSpecNasabah = nasabahClient.put()
                .uri("/tesNasabah")
                .body(Mono.just(requestBody), HashMap.class)
                .retrieve();
        Restponse responseNasabah = (Restponse) responseSpecNasabah.bodyToMono(Object.class).block();
        if ( responseNasabah.getStatus() == "441" ) {
            return false;
        } else {
            return true;
        }
    }

    public Boolean transferPembayaranTagihan(Tagihan tagihan) {
        HashMap requestBody = new HashMap();
        requestBody.put("nomorRekening", tagihan.getIdYangDitagih());
        requestBody.put("jumlah", tagihan.getTagihanNominal());

        WebClient.ResponseSpec responseSpecTabungan = tabunganClient.put()
                .uri("tabungan/transfer")
                .body(Mono.just(requestBody), HashMap.class)
                .retrieve();
        Restponse responseTabungan = (Restponse) responseSpecTabungan.bodyToMono(Object.class).block();
        if ( responseTabungan.getStatus() == "431" ) {
            return false;
        } else {
            return true;
        }
    }

    public Tagihan replaceTagihanValue(Tagihan newTagihan, Tagihan oldTagihan) {
        if (newTagihan.getIdPenagih() == null) { newTagihan.setIdPenagih(oldTagihan.getIdPenagih()); }
        if (newTagihan.getIdYangDitagih() == null) { newTagihan.setIdYangDitagih(oldTagihan.getIdYangDitagih()); }
        if (newTagihan.getTagihanNominal() == null) { newTagihan.setTagihanNominal(oldTagihan.getTagihanNominal()); }
        if (newTagihan.getTagihanCreationDatetime() == null) { newTagihan.setTagihanCreationDatetime(oldTagihan.getTagihanCreationDatetime()); }
        if (newTagihan.getTagihanIsLunas() == null) { newTagihan.setTagihanIsLunas(oldTagihan.getTagihanIsLunas()); }
        if (newTagihan.getTagihanDurasi() == null) { newTagihan.setTagihanDurasi(oldTagihan.getTagihanDurasi()); }
        if (newTagihan.getTagihanKeterangan() == null) { newTagihan.setTagihanKeterangan(oldTagihan.getTagihanKeterangan()); }
        return newTagihan;
    }

    public Restponse tes1() {
        Restponse response = (Restponse) tagihanClient.get()
                .uri("/tagihan/1").retrieve().bodyToMono(Object.class).block();

        return response;
    }
}
