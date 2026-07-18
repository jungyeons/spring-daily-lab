package com.jungyeons.dailylab.task.api;

import java.time.LocalDate;
import java.util.Set;

import com.jungyeons.dailylab.domain.TaskCategory;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
		@NotBlank @Size(max = 120) String title,
		@Size(max = 2000) String description,
		@NotNull TaskCategory category,
		@NotNull @Min(1) @Max(5) Integer priority,
		@FutureOrPresent LocalDate dueDate,
		@Size(max = 10) Set<@NotBlank @Size(max = 40) String> tags
) {
}
