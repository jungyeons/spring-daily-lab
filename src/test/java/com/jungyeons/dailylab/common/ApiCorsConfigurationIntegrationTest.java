package com.jungyeons.dailylab.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "app.cors.allowed-origins=https://allowed.example,https://second.example"
)
class ApiCorsConfigurationIntegrationTest {

	@LocalServerPort
	private int port;

	@Test
	void acceptsListedOriginPreflight() throws Exception {
		HttpResponse<String> response = preflight("https://allowed.example");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
				.hasValue("https://allowed.example");
		assertThat(response.headers().firstValue("Access-Control-Allow-Credentials")).isEmpty();
	}

	@Test
	void rejectsUnlistedOriginPreflight() throws Exception {
		HttpResponse<String> response = preflight("https://untrusted.example");

		assertThat(response.statusCode()).isEqualTo(403);
		assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).isEmpty();
	}

	@Test
	void rejectsWildcardConfiguration() {
		assertThatThrownBy(() -> new ApiCorsConfiguration("*"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("CORS allowed origins must not contain a wildcard");
	}

	private HttpResponse<String> preflight(String origin) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(
				URI.create("http://localhost:" + port + "/api/v1/tasks")
		)
				.method("OPTIONS", HttpRequest.BodyPublishers.noBody())
				.header("Origin", origin)
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "Content-Type")
				.build();
		return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
	}
}
