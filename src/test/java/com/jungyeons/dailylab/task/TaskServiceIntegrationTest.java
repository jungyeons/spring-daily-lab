package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CursorPageResponse;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskServiceIntegrationTest {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void clearDatabase() {
		taskRepository.deleteAll();
	}

	@Test
	void createsFiltersAndCompletesTask() {
		TaskResponse created = taskService.create(new CreateTaskRequest(
				"Add API validation",
				"Reject malformed requests",
				TaskCategory.FEATURE,
				4,
				LocalDate.now().plusDays(2)
		));

		assertThat(created.id()).isNotNull();
		assertThat(created.status()).isEqualTo(TaskStatus.TODO);
		assertThat(taskService.findAll(
				TaskStatus.TODO,
				TaskCategory.FEATURE,
				PageRequest.of(0, 20)
		)).hasSize(1);

		TaskResponse completed = taskService.changeStatus(
				created.id(),
				new ChangeTaskStatusRequest(TaskStatus.DONE)
		);
		assertThat(completed.completedAt()).isNotNull();
		assertThat(taskService.summary().done()).isEqualTo(1);
		assertThat(taskService.summary().todo()).isZero();
	}

	@Test
	void returnsStableCursorPagesAndPreservesFilters() {
		TaskResponse first = createTask("First", TaskCategory.FEATURE);
		TaskResponse second = createTask("Second", TaskCategory.TEST);
		TaskResponse third = createTask("Third", TaskCategory.FEATURE);

		CursorPageResponse<TaskResponse> firstPage = taskService.findNextPage(null, null, 0, 2);

		assertThat(firstPage.items()).extracting(TaskResponse::id).containsExactly(first.id(), second.id());
		assertThat(firstPage.hasNext()).isTrue();
		assertThat(firstPage.nextCursor()).isEqualTo(second.id());

		CursorPageResponse<TaskResponse> nextPage = taskService.findNextPage(null, null, firstPage.nextCursor(), 2);

		assertThat(nextPage.items()).extracting(TaskResponse::id).containsExactly(third.id());
		assertThat(nextPage.hasNext()).isFalse();
		assertThat(nextPage.nextCursor()).isNull();

		CursorPageResponse<TaskResponse> filtered = taskService.findNextPage(null, TaskCategory.FEATURE, 0, 10);

		assertThat(filtered.items()).extracting(TaskResponse::id).containsExactly(first.id(), third.id());
	}

	@Test
	void exposesCursorPaginationEndpoint() throws Exception {
		TaskResponse first = createTask("First", TaskCategory.FEATURE);
		TaskResponse second = createTask("Second", TaskCategory.FEATURE);
		createTask("Third", TaskCategory.TEST);

		mockMvc.perform(get("/api/v1/tasks/cursor")
					.param("category", "FEATURE")
					.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items[0].id").value(first.id()))
				.andExpect(jsonPath("$.hasNext").value(true))
				.andExpect(jsonPath("$.nextCursor").value(first.id()));

		mockMvc.perform(get("/api/v1/tasks/cursor")
					.param("category", "FEATURE")
					.param("cursor", second.id().toString())
					.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.items").isEmpty())
				.andExpect(jsonPath("$.hasNext").value(false))
				.andExpect(jsonPath("$.nextCursor").doesNotExist());
	}

	private TaskResponse createTask(String title, TaskCategory category) {
		return taskService.create(new CreateTaskRequest(title, null, category, 3, LocalDate.now().plusDays(1)));
	}
}
