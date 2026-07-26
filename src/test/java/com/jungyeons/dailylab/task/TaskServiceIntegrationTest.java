package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;
import com.jungyeons.dailylab.task.api.TaskSummaryResponse;

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
	void summarizesAllStatusesAndOverdueTasksInOneSnapshot() {
		GrowthTask overdueTodo = GrowthTask.create(
				"Overdue todo", null, TaskCategory.FEATURE, 3, LocalDate.now().minusDays(1)
		);
		GrowthTask inProgress = GrowthTask.create(
				"Current work", null, TaskCategory.FEATURE, 3, LocalDate.now()
		);
		inProgress.changeStatus(TaskStatus.IN_PROGRESS);
		GrowthTask completed = GrowthTask.create(
				"Completed work", null, TaskCategory.FEATURE, 3, LocalDate.now().minusDays(2)
		);
		completed.changeStatus(TaskStatus.DONE);
		taskRepository.saveAll(java.util.List.of(overdueTodo, inProgress, completed));

		TaskSummaryResponse summary = taskService.summary();

		assertThat(summary).isEqualTo(new TaskSummaryResponse(3, 1, 1, 1, 1));
	}

	@Test
	void returnsZeroesWhenThereAreNoTasks() {
		assertThat(taskService.summary()).isEqualTo(new TaskSummaryResponse(0, 0, 0, 0, 0));
	}
}
