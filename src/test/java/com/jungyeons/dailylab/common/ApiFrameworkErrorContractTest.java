package com.jungyeons.dailylab.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiFrameworkErrorContractTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsProblemDetailsForUnsupportedMediaType() throws Exception {
		mockMvc.perform(post("/api/v1/tasks")
						.contentType(MediaType.TEXT_PLAIN)
						.content("not-json"))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unsupported Media Type"))
				.andExpect(jsonPath("$.status").value(415))
				.andExpect(jsonPath("$.instance").value("/api/v1/tasks"));
	}

	@Test
	void returnsProblemDetailsForUnsupportedMethod() throws Exception {
		mockMvc.perform(post("/api/v1/tasks/summary"))
				.andExpect(status().isMethodNotAllowed())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Method Not Allowed"))
				.andExpect(jsonPath("$.status").value(405))
				.andExpect(jsonPath("$.instance").value("/api/v1/tasks/summary"));
	}
}
