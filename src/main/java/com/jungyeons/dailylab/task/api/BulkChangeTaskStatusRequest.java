package com.jungyeons.dailylab.task.api;

import java.util.List;

import com.jungyeons.dailylab.domain.TaskStatus;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BulkChangeTaskStatusRequest(
		@NotEmpty List<@NotNull @Positive Long> taskIds,
		@NotNull TaskStatus status
) {
}
