package com.example.TagihanApp;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.netflix.discovery.EurekaClient;
import com.netflix.servo.monitor.TimedStopwatch;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    Logger logger = LoggerFactory.getLogger(TagihanController.class);

    @Autowired
    private TagihanService tagihanService;
    private EurekaClient eurekaClient;

    //	private String nasabahUrl = "NASABAHSERVICE";
//	private String tabunganUrl = "TABUNGANSERVICE";
    private String nasabahUrl = "TAGIHANSERVICE";
    private String tabunganUrl = "TAGIHANSERVICE";
    private String tagihanUrl = "TAGIHANSERVICE";

    public WebClient nasabahClientBuilder() {
        return WebClient.create(this.eurekaClient.getNextServerFromEureka(nasabahUrl, false).getHomePageUrl());
    }

    public WebClient tabunganClientBuilder() {
        return WebClient.create(this.eurekaClient.getNextServerFromEureka(tabunganUrl, false).getHomePageUrl());
    }

    @Bean
    public WebClient tagihanClientBuilder() {
        return WebClient.create(this.eurekaClient.getNextServerFromEureka(tagihanUrl, false).getHomePageUrl());
    }

    @GetMapping("")
    public @ResponseBody Restponse getTagihanByIdUsingBody(@RequestBody Long idTagihan) {
        logger.info("membaca tagihan dengan idTagihan=" + idTagihan.toString() + "melalui request body.");
        return tagihanService.getTagihan(idTagihan);
    }

    @GetMapping("/{idTagihan}")
    public @ResponseBody Restponse getTagihanByIdUsingVariable(@PathVariable("idTagihan") Long idTagihan) {
        logger.info("membaca tagihan dengan idTagihan=" + idTagihan.toString() + "melalui path variable.");
        return tagihanService.getTagihan(idTagihan);
    }

    @GetMapping("/param")
    public @ResponseBody Restponse getTagihanByIdUsingParam(@RequestParam("idTagihan") Long idTagihan) {
        logger.info("membaca tagihan dengan idTagihan=" + idTagihan.toString() + "melalui request param.");
        return tagihanService.getTagihan(idTagihan);
    }

    @GetMapping(path="/all")
    public @ResponseBody Restponse getAllTagihan() {
        return tagihanService.getAllTagihan();
    }

    @GetMapping(path="/lunasitagihan")
    public @ResponseBody Restponse lunasiTagihan(@RequestBody Long idTagihan) {
        return tagihanService.lunasiTagihan(idTagihan);
    }

    @PostMapping(path="")
    public @ResponseBody Restponse buatTagihan (@RequestBody Tagihan tagihan) {
        return tagihanService.addNewTagihan(tagihan);
    }

    @PatchMapping(path="")
    public @ResponseBody Restponse editTagihan (@RequestBody Tagihan oldTagihan) {
        return tagihanService.editTagihan(oldTagihan);
    }

    @PutMapping(path="")
    public @ResponseBody Restponse replaceTagihan (@RequestBody Tagihan newTagihan) {
        return tagihanService.replaceTagihan(newTagihan);
    }

    @DeleteMapping(path="")
    public @ResponseBody Restponse deleteTagihan (@RequestBody Long idTagihan) {
        return tagihanService.deleteTagihan(idTagihan);
    }

    @GetMapping(path="/tes1")
    public @ResponseBody Restponse tes1 () {
        return tagihanService.tes1();
    }
}
