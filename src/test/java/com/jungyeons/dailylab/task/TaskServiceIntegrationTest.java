package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.common.TaskArchiveException;
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
	void archivesOnlyCompletedTasksAndHidesThemFromDefaultViews() {
		TaskResponse created = taskService.create(new CreateTaskRequest(
				"Archive me", null, TaskCategory.LEARNING, 2, LocalDate.now().plusDays(1)
		));

		assertThatThrownBy(() -> taskService.archive(created.id()))
				.isInstanceOf(TaskArchiveException.class)
				.hasMessage("Only completed tasks can be archived");

		taskService.changeStatus(created.id(), new ChangeTaskStatusRequest(TaskStatus.DONE));
		TaskResponse archived = taskService.archive(created.id());

		assertThat(archived.archivedAt()).isNotNull();
		assertThat(taskService.findAll(null, null, PageRequest.of(0, 20))).isEmpty();
		assertThat(taskService.findAll(null, null, true, PageRequest.of(0, 20))).hasSize(1);
		assertThat(taskService.summary().total()).isZero();

		TaskResponse restored = taskService.unarchive(created.id());

		assertThat(restored.archivedAt()).isNull();
		assertThat(taskService.findAll(null, null, PageRequest.of(0, 20))).hasSize(1);
	}
}
