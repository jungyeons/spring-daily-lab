package com.jungyeons.dailylab.task;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

@WebMvcTest(TaskController.class)
class TaskIdValidationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@ParameterizedTest
	@MethodSource("requestsWithInvalidTaskIds")
	void rejectsNonPositiveTaskIds(RequestBuilder request) throws Exception {
		mockMvc.perform(request)
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Invalid request"))
				.andExpect(jsonPath("$.errors.id").value("must be a positive integer"));
		verifyNoInteractions(taskService);
	}

	private static Stream<RequestBuilder> requestsWithInvalidTaskIds() {
		String updateBody = """
				{
				  "title": "Updated task",
				  "description": null,
				  "category": "FEATURE",
				  "priority": 3,
				  "dueDate": "%s"
				}
				""".formatted(LocalDate.now().plusDays(1));

		return Stream.of(
				get("/api/v1/tasks/0"),
				put("/api/v1/tasks/-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateBody),
				patch("/api/v1/tasks/0/status")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DONE\"}"),
				delete("/api/v1/tasks/-1")
		);
	}
}
