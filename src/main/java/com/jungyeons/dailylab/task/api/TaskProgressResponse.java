package com.jungyeons.dailylab.task.api;

import java.time.Instant;

import com.jungyeons.dailylab.task.progress.TaskProgressEntry;

public record TaskProgressResponse(
		Long id,
		long taskId,
		int percent,
		String note,
		Instant recordedAt
) {
	public static TaskProgressResponse from(TaskProgressEntry entry) {
		return new TaskProgressResponse(
				entry.getId(),
				entry.getTaskId(),
				entry.getPercent(),
				entry.getNote(),
				entry.getRecordedAt()
		);
	}
}
