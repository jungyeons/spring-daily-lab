package com.jungyeons.dailylab.task.api;

import com.jungyeons.dailylab.domain.RecurrenceFrequency;

import jakarta.validation.constraints.NotNull;

public record ConfigureRecurrenceRequest(@NotNull RecurrenceFrequency frequency) {
}
