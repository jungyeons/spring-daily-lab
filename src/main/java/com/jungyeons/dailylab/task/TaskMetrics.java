package com.jungyeons.dailylab.task;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class TaskMetrics {

	private final Map<TaskCategory, Counter> createdCounters = new EnumMap<>(TaskCategory.class);
	private final Map<TaskCategory, Counter> completedCounters = new EnumMap<>(TaskCategory.class);

	public TaskMetrics(MeterRegistry meterRegistry, TaskRepository taskRepository) {
		for (TaskCategory category : TaskCategory.values()) {
			createdCounters.put(category, Counter.builder("dailylab.tasks.created")
					.description("Number of tasks created")
					.tag("category", category.name())
					.register(meterRegistry));
			completedCounters.put(category, Counter.builder("dailylab.tasks.completed")
					.description("Number of tasks completed")
					.tag("category", category.name())
					.register(meterRegistry));
		}
		Gauge.builder(
				"dailylab.tasks.overdue",
				taskRepository,
				repository -> repository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE)
		)
				.description("Current number of overdue incomplete tasks")
				.register(meterRegistry);
	}

	public void recordCreated(TaskCategory category) {
		createdCounters.get(category).increment();
	}

	public void recordCompleted(TaskCategory category) {
		completedCounters.get(category).increment();
	}
}
