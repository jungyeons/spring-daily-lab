package com.jungyeons.dailylab.task.api;

import java.time.Instant;
import java.util.List;

public record TaskBackupResponse(
		int schemaVersion,
		Instant exportedAt,
		List<TaskResponse> tasks
) {
}
