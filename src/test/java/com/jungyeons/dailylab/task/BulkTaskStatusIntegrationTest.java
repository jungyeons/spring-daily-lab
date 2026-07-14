package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jungyeons.dailylab.common.TaskNotFoundException;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.BulkChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;

@SpringBootTest
class BulkTaskStatusIntegrationTest {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void clearDatabase() {
		taskRepository.deleteAll();
	}

	@Test
	void changesSeveralTaskStatusesInOneTransaction() {
		TaskResponse first = createTask("First task");
		TaskResponse second = createTask("Second task");

		List<TaskResponse> changed = taskService.changeStatuses(new BulkChangeTaskStatusRequest(
				List.of(first.id(), second.id()), TaskStatus.DONE
		));

		assertThat(changed).extracting(TaskResponse::status).containsOnly(TaskStatus.DONE);
		assertThat(taskService.findById(first.id()).status()).isEqualTo(TaskStatus.DONE);
		assertThat(taskService.findById(second.id()).status()).isEqualTo(TaskStatus.DONE);
	}

	@Test
	void rollsBackAllChangesWhenOneTaskDoesNotExist() {
		TaskResponse existing = createTask("Existing task");

		assertThatThrownBy(() -> taskService.changeStatuses(new BulkChangeTaskStatusRequest(
				List.of(existing.id(), 999_999L), TaskStatus.DONE
		))).isInstanceOf(TaskNotFoundException.class);

		assertThat(taskService.findById(existing.id()).status()).isEqualTo(TaskStatus.TODO);
	}

	private TaskResponse createTask(String title) {
		return taskService.create(new CreateTaskRequest(title, null, TaskCategory.FEATURE, 3, null));
	}
}
