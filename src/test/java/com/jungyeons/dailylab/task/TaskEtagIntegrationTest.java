package com.jungyeons.dailylab.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskEtagIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void clearDatabase() {
		taskRepository.deleteAll();
	}

	@Test
	void returnsEtagsAndRejectsStaleUpdates() throws Exception {
		TaskResponse created = taskService.create(new CreateTaskRequest(
				"Original", null, TaskCategory.FEATURE, 3, LocalDate.now().plusDays(2)
		));

		mockMvc.perform(get("/api/v1/tasks/{id}", created.id()))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ETAG, "\"0\""));

		String update = """
				{"title":"Updated","description":null,"category":"FEATURE","priority":4,"dueDate":"%s"}
				""".formatted(LocalDate.now().plusDays(3));

		mockMvc.perform(put("/api/v1/tasks/{id}", created.id())
					.header(HttpHeaders.IF_MATCH, "\"1\"")
					.contentType(MediaType.APPLICATION_JSON)
					.content(update))
				.andExpect(status().isPreconditionFailed())
				.andExpect(jsonPath("$.expectedVersion").value(1))
				.andExpect(jsonPath("$.actualVersion").value(0));

		mockMvc.perform(put("/api/v1/tasks/{id}", created.id())
					.header(HttpHeaders.IF_MATCH, "\"0\"")
					.contentType(MediaType.APPLICATION_JSON)
					.content(update))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
				.andExpect(jsonPath("$.title").value("Updated"))
				.andExpect(jsonPath("$.version").value(1));
	}
}
