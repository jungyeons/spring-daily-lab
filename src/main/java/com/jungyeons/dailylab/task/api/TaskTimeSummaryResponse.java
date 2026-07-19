package com.jungyeons.dailylab.task.api;

import java.util.List;

public record TaskTimeSummaryResponse(
		long taskId,
		long totalMinutes,
		List<TaskTimeEntryResponse> entries
) {
}
