package com.jungyeons.dailylab.common;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiCorsConfiguration implements WebMvcConfigurer {

	private final String[] allowedOrigins;

	ApiCorsConfiguration(@Value("${app.cors.allowed-origins:}") String configuredOrigins) {
		this.allowedOrigins = Arrays.stream(configuredOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toArray(String[]::new);
		if (Arrays.asList(allowedOrigins).contains("*")) {
			throw new IllegalArgumentException("CORS allowed origins must not contain a wildcard");
		}
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		if (allowedOrigins.length == 0) {
			return;
		}
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigins)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
				.allowedHeaders("Content-Type", "If-Match", "Idempotency-Key", "X-Correlation-Id")
				.exposedHeaders("Location", "ETag", "X-Correlation-Id")
				.allowCredentials(false)
				.maxAge(3600);
	}
}
