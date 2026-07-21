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

import com.jungyeons.dailylab.common.TaskRecurrenceConflictException;
import com.jungyeons.dailylab.domain.RecurrenceFrequency;
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
	void configuresRecurringTaskAndGeneratesOneNextOccurrence() {
		TaskResponse created = taskService.create(new CreateTaskRequest(
				"Review weekly progress",
				"Keep the planning cadence",
				TaskCategory.LEARNING,
				3,
				LocalDate.of(2030, 1, 31)
		));

		TaskResponse configured = taskService.configureRecurrence(created.id(), RecurrenceFrequency.MONTHLY);
		TaskResponse next = taskService.generateNextRecurringTask(created.id());

		assertThat(configured.recurrenceFrequency()).isEqualTo(RecurrenceFrequency.MONTHLY);
		assertThat(next.id()).isNotEqualTo(created.id());
		assertThat(next.title()).isEqualTo(created.title());
		assertThat(next.status()).isEqualTo(TaskStatus.TODO);
		assertThat(next.dueDate()).isEqualTo(LocalDate.of(2030, 2, 28));
		assertThat(next.recurrenceFrequency()).isEqualTo(RecurrenceFrequency.MONTHLY);
		assertThat(taskService.findById(created.id()).recurrenceGeneratedAt()).isNotNull();
		assertThatThrownBy(() -> taskService.generateNextRecurringTask(created.id()))
				.isInstanceOf(TaskRecurrenceConflictException.class)
				.hasMessage("The next recurring task has already been generated");
	}

	@Test
	void rejectsRecurrenceForTaskWithoutDueDate() {
		TaskResponse created = taskService.create(new CreateTaskRequest(
				"Plan without a deadline",
				null,
				TaskCategory.LEARNING,
				3,
				null
		));

		assertThatThrownBy(() -> taskService.configureRecurrence(created.id(), RecurrenceFrequency.DAILY))
				.isInstanceOf(TaskRecurrenceConflictException.class)
				.hasMessage("A recurring task must have a due date");
	}
}
