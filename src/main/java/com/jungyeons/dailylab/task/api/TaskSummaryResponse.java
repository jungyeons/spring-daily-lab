package com.jungyeons.dailylab.task.api;

public record TaskSummaryResponse(
		long total,
		long todo,
		long inProgress,
		long done,
		long overdue
) {
}
