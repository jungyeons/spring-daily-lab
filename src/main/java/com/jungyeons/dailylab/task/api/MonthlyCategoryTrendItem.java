package com.jungyeons.dailylab.task.api;

import com.jungyeons.dailylab.domain.TaskCategory;

public record MonthlyCategoryTrendItem(
		String month,
		TaskCategory category,
		long created,
		long completed
) {
}
