package com.jungyeons.dailylab.task.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record CreateTaskProgressRequest(
		@Min(0) @Max(100) int percent,
		@Size(max = 500) String note
) {
}
