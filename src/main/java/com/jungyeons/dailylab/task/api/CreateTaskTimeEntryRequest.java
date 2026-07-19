package com.jungyeons.dailylab.task.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CreateTaskTimeEntryRequest(
		@Min(1) int durationMinutes,
		@Size(max = 500) String note
) {
}
