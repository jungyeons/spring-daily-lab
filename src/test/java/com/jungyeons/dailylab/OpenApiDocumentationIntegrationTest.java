package com.jungyeons.dailylab;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocumentationIntegrationTest {

	@LocalServerPort
	private int port;

	@Test
	void exposesOpenApiDocumentForTaskEndpoints() throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/v3/api-docs"))
				.GET()
				.build();

		HttpResponse<String> response = HttpClient.newHttpClient().send(
				request,
				HttpResponse.BodyHandlers.ofString()
		);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body())
				.contains("\"openapi\"")
				.contains("/api/v1/tasks")
				.contains("Spring Daily Lab API");
	}
}
