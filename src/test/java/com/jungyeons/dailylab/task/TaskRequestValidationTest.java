package com.jungyeons.dailylab.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskRequestValidationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@Test
	void reportsEveryInvalidCreationField() throws Exception {
		mockMvc.perform(post("/api/v1/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": " ",
								  "category": "FEATURE",
								  "priority": 0,
								  "dueDate": "%s"
								}
								""".formatted(LocalDate.now().minusDays(1))))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.detail").value("Request validation failed"))
				.andExpect(jsonPath("$.errors.title").exists())
				.andExpect(jsonPath("$.errors.priority").exists())
				.andExpect(jsonPath("$.errors.dueDate").exists());
	}

	@Test
	void returnsProblemDetailsForUnsupportedCategoryValue() throws Exception {
		mockMvc.perform(post("/api/v1/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Validate request",
								  "category": "UNKNOWN",
								  "priority": 3
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Unreadable request"))
				.andExpect(jsonPath("$.detail")
						.value("Malformed JSON or unsupported enum value"));
	}
}
