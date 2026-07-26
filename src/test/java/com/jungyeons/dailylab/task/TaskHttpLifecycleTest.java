package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskHttpLifecycleTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void clearDatabase() {
		taskRepository.deleteAll();
	}

	@Test
	void exercisesTheCompleteTaskHttpLifecycle() throws Exception {
		LocalDate dueDate = LocalDate.now().plusDays(2);
		MvcResult creation = mockMvc.perform(post("/api/v1/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Exercise HTTP API",
								  "description": "Cover the task lifecycle",
								  "category": "TEST",
								  "priority": 4,
								  "dueDate": "%s"
								}
								""".formatted(dueDate)))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value("TODO"))
				.andReturn();

		long taskId = taskRepository.findAll().getFirst().getId();
		assertThat(creation.getResponse().getHeader(HttpHeaders.LOCATION))
				.endsWith("/api/v1/tasks/" + taskId);

		mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Exercise HTTP API"))
				.andExpect(jsonPath("$.version").value(0));

		mockMvc.perform(put("/api/v1/tasks/{id}", taskId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Updated HTTP API",
								  "description": "Verify replacement",
								  "category": "DOCUMENTATION",
								  "priority": 2,
								  "dueDate": "%s"
								}
								""".formatted(dueDate.plusDays(1))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Updated HTTP API"))
				.andExpect(jsonPath("$.category").value("DOCUMENTATION"));

		mockMvc.perform(patch("/api/v1/tasks/{id}/status", taskId)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"DONE\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("DONE"))
				.andExpect(jsonPath("$.completedAt").isNotEmpty());

		mockMvc.perform(get("/api/v1/tasks/summary"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.todo").value(0))
				.andExpect(jsonPath("$.done").value(1));

		mockMvc.perform(delete("/api/v1/tasks/{id}", taskId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/tasks/{id}", taskId))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.title").value("Task not found"));
	}
}
