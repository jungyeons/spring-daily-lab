package com.jungyeons.dailylab.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.http.max-request-body-size=256B")
@AutoConfigureMockMvc
class RequestBodyLimitIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsApiBodyOverConfiguredLimitWithProblemDetails() throws Exception {
		String oversizedDescription = "x".repeat(300);

		mockMvc.perform(post("/api/v1/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Oversized request",
								  "description": "%s",
								  "category": "TEST",
								  "priority": 3
								}
								""".formatted(oversizedDescription)))
				.andExpect(status().isContentTooLarge())
				.andExpect(jsonPath("$.title").value("Payload too large"))
				.andExpect(jsonPath("$.status").value(413))
				.andExpect(jsonPath("$.detail").value(
						"Request body exceeds the configured limit of 256 bytes"
				))
				.andExpect(jsonPath("$.instance").value("/api/v1/tasks"));
	}

	@Test
	void allowsApiBodyWithinConfiguredLimit() throws Exception {
		mockMvc.perform(post("/api/v1/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Bounded request",
								  "category": "TEST",
								  "priority": 3
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Bounded request"));
	}
}
