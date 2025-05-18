package com.futurex.services.FutureXCourseCatalog;

import io.opentelemetry.api.OpenTelemetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class FutureXCourseCatalogApplication {

	public static void main(String[] args) {
		SpringApplication.run(FutureXCourseCatalogApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(OpenTelemetry openTelemetry) {
		return new RestTemplate();
	}

}
