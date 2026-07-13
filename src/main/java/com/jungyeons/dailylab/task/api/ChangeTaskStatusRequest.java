package com.jungyeons.dailylab.task.api;

import com.jungyeons.dailylab.domain.TaskStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(@NotNull TaskStatus status) {
}
