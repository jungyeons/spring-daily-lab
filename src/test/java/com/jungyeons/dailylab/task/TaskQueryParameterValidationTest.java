package com.jungyeons.dailylab.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskQueryParameterValidationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@Test
	void returnsProblemDetailsForAnUnsupportedStatus() throws Exception {
		mockMvc.perform(get("/api/v1/tasks").param("status", "UNKNOWN"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Invalid parameter"))
				.andExpect(jsonPath("$.detail")
						.value("Parameter 'status' has an unsupported value"))
				.andExpect(jsonPath("$.parameter").value("status"));
	}

	@Test
	void returnsProblemDetailsForAnUnsupportedCategory() throws Exception {
		mockMvc.perform(get("/api/v1/tasks").param("category", "UNKNOWN"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Invalid parameter"))
				.andExpect(jsonPath("$.parameter").value("category"));
	}
}
