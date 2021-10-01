package com.example.TagihanApp;

import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableEurekaClient
public class TagihanAppApplication {

	private static final Logger log = LoggerFactory.getLogger(TagihanAppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TagihanAppApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public WebClient.Builder webclientBuilder() {
		return WebClient.builder();
	}
}
