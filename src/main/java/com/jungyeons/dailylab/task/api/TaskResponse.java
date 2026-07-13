package com.jungyeons.dailylab.task.api;

import java.time.Instant;
import java.time.LocalDate;

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
				task.getVersion()
		);
	}
}
