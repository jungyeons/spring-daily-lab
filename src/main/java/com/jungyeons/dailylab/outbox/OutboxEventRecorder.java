package com.jungyeons.dailylab.outbox;

import org.springframework.stereotype.Component;

import com.jungyeons.dailylab.domain.GrowthTask;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxEventRecorder {

	public static final String TASK_CREATED = "TASK_CREATED";
	public static final String TASK_UPDATED = "TASK_UPDATED";
	public static final String TASK_STATUS_CHANGED = "TASK_STATUS_CHANGED";
	public static final String TASK_DELETED = "TASK_DELETED";

	private static final String TASK_AGGREGATE = "GROWTH_TASK";

	private final OutboxEventRepository outboxEventRepository;
	private final ObjectMapper objectMapper;

	public OutboxEventRecorder(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
		this.outboxEventRepository = outboxEventRepository;
		this.objectMapper = objectMapper;
	}

	public void recordTaskEvent(String eventType, GrowthTask task) {
		TaskEventPayload payload = new TaskEventPayload(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.getCategory().name(),
				task.getStatus().name(),
				task.getPriority(),
				task.getDueDate(),
				task.getCompletedAt()
		);
		try {
			outboxEventRepository.save(OutboxEvent.create(
					TASK_AGGREGATE,
					task.getId(),
					eventType,
					objectMapper.writeValueAsString(payload)
			));
		} catch (JacksonException exception) {
			throw new IllegalStateException("Failed to serialize task outbox event", exception);
		}
	}

	private record TaskEventPayload(
			Long taskId,
			String title,
			String description,
			String category,
			String status,
			int priority,
			java.time.LocalDate dueDate,
			java.time.Instant completedAt
	) {
	}
}
