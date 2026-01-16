package com.alura.churninsight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChurninsightApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChurninsightApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.web.client.RestTemplate restTemplate(
			org.springframework.boot.web.client.RestTemplateBuilder builder) {
		return builder
				.connectTimeout(java.time.Duration.ofSeconds(10))
				.readTimeout(java.time.Duration.ofSeconds(10))
				.build();
	}
}
