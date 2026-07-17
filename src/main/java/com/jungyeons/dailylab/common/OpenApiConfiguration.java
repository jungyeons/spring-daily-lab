package com.jungyeons.dailylab.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI springDailyLabOpenApi() {
		return new OpenAPI().info(new Info()
				.title("Spring Daily Lab API")
				.version("v1")
				.description("API for recording, organizing, and tracking daily growth tasks.")
				.license(new License().name("MIT").url("https://opensource.org/license/mit")));
	}
}
