package com.jungyeons.dailylab.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.TaskRepository;
import com.jungyeons.dailylab.task.TaskService;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;
import com.jungyeons.dailylab.task.api.UpdateTaskRequest;

@SpringBootTest
class OutboxEventIntegrationTest {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private OutboxEventRepository outboxEventRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void clearDatabase() {
		outboxEventRepository.deleteAll();
		taskRepository.deleteAll();
	}

	@Test
	void recordsTaskLifecycleEventsWithSnapshots() {
		TaskResponse created = taskService.create(new CreateTaskRequest(
				"Publish domain events",
				"Persist events before dispatch",
				TaskCategory.FEATURE,
				4,
				LocalDate.of(2030, 1, 15)
		));
		taskService.update(created.id(), new UpdateTaskRequest(
				"Publish reliable events",
				null,
				TaskCategory.OPERATIONS,
				5,
				null
		));
		taskService.changeStatus(created.id(), new ChangeTaskStatusRequest(TaskStatus.DONE));
		taskService.delete(created.id());

		List<OutboxEvent> events = outboxEventRepository.findAllByOrderByIdAsc();

		assertThat(events).extracting(OutboxEvent::getEventType).containsExactly(
				OutboxEventRecorder.TASK_CREATED,
				OutboxEventRecorder.TASK_UPDATED,
				OutboxEventRecorder.TASK_STATUS_CHANGED,
				OutboxEventRecorder.TASK_DELETED
		);
		assertThat(events).allSatisfy(event -> {
			assertThat(event.getAggregateType()).isEqualTo("GROWTH_TASK");
			assertThat(event.getAggregateId()).isEqualTo(created.id());
			assertThat(event.getOccurredAt()).isNotNull();
			assertThat(event.getPublishedAt()).isNull();
			assertThat(event.getPayload()).contains("\"taskId\":" + created.id());
		});
		assertThat(events.get(0).getPayload()).contains(
				"\"title\":\"Publish domain events\"",
				"\"status\":\"TODO\""
		);
		assertThat(events.get(2).getPayload()).contains(
				"\"title\":\"Publish reliable events\"",
				"\"status\":\"DONE\""
		);
	}

	@Test
	void rollsBackTaskAndOutboxEventTogether() {
		assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
			taskService.create(new CreateTaskRequest(
					"Rollback together",
					null,
					TaskCategory.TEST,
					3,
					null
			));
			throw new IllegalStateException("force rollback");
		})).isInstanceOf(IllegalStateException.class);

		assertThat(taskRepository.count()).isZero();
		assertThat(outboxEventRepository.count()).isZero();
	}
}
