package com.jungyeons.dailylab.task.api;

import java.time.Instant;

import com.jungyeons.dailylab.task.time.TaskTimeEntry;

public record TaskTimeEntryResponse(
		Long id,
		long taskId,
		int durationMinutes,
		String note,
		Instant recordedAt
) {
	public static TaskTimeEntryResponse from(TaskTimeEntry entry) {
		return new TaskTimeEntryResponse(
				entry.getId(),
				entry.getTaskId(),
				entry.getDurationMinutes(),
				entry.getNote(),
				entry.getRecordedAt()
		);
	}
}
