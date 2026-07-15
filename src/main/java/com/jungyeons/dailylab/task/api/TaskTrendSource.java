package com.jungyeons.dailylab.task.api;

import java.time.Instant;

import com.jungyeons.dailylab.domain.TaskCategory;

public record TaskTrendSource(
		TaskCategory category,
		Instant createdAt,
		Instant completedAt
) {
}
