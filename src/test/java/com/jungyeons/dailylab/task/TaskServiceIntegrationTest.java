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
import com.jungyeons.dailylab.common.TaskDependencyCycleException;
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
	void modelsPrerequisitesAndRejectsCircularDependencies() {
		TaskResponse design = createTask("Design API");
		TaskResponse implementation = createTask("Implement API");
		TaskResponse documentation = createTask("Document API");

		TaskResponse updated = taskService.addPrerequisite(implementation.id(), design.id());
		taskService.addPrerequisite(documentation.id(), implementation.id());

		assertThat(updated.prerequisiteIds()).containsExactly(design.id());
		assertThat(taskService.findPrerequisites(documentation.id()))
				.extracting(TaskResponse::id)
				.containsExactly(implementation.id());
		assertThatThrownBy(() -> taskService.addPrerequisite(design.id(), documentation.id()))
				.isInstanceOf(TaskDependencyCycleException.class);
	}

	private TaskResponse createTask(String title) {
		return taskService.create(new CreateTaskRequest(title, null, TaskCategory.FEATURE, 3, null));
	}
}
