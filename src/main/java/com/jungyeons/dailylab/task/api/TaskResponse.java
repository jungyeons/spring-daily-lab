package com.jungyeons.dailylab.task.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;

public record TaskResponse(
		Long id,
		String title,
		String description,
		TaskCategory category,
		TaskStatus status,
		int priority,
		LocalDate dueDate,
		Instant createdAt,
		Instant updatedAt,
		Instant completedAt,
		Set<Long> prerequisiteIds,
		long version
) {
	public static TaskResponse from(GrowthTask task) {
		return new TaskResponse(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.getCategory(),
				task.getStatus(),
				task.getPriority(),
				task.getDueDate(),
				task.getCreatedAt(),
				task.getUpdatedAt(),
				task.getCompletedAt(),
				task.getPrerequisites().stream()
					.map(GrowthTask::getId)
					.sorted()
					.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
				task.getVersion()
		);
	}
}
