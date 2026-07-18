package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;

@SpringBootTest
@Transactional
class TaskServiceIntegrationTest {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

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
				LocalDate.now().plusDays(2),
				Set.of("api", "validation")
		));

		assertThat(created.id()).isNotNull();
		assertThat(created.status()).isEqualTo(TaskStatus.TODO);
		assertThat(created.tags()).containsExactlyInAnyOrder("api", "validation");
		assertThat(taskService.findAll(
				TaskStatus.TODO,
				TaskCategory.FEATURE,
				"API",
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
	void filtersTasksByTagAndReplacesTagsOnUpdate() {
		TaskResponse matching = taskService.create(new CreateTaskRequest(
				"Document API", null, TaskCategory.DOCUMENTATION, 2, null, Set.of("release", "api")
		));
		taskService.create(new CreateTaskRequest(
				"Prepare demo", null, TaskCategory.FEATURE, 3, null, Set.of("release")
		));

		assertThat(taskService.findAll(null, null, "api", PageRequest.of(0, 20)))
				.extracting(TaskResponse::id)
				.containsExactly(matching.id());

		TaskResponse updated = taskService.update(matching.id(), new com.jungyeons.dailylab.task.api.UpdateTaskRequest(
				"Document API", null, TaskCategory.DOCUMENTATION, 2, null, Set.of("guide")
		));
		assertThat(updated.tags()).containsExactly("guide");
		assertThat(taskService.findAll(null, null, "api", PageRequest.of(0, 20))).isEmpty();
	}
}
