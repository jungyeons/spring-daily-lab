package com.jungyeons.dailylab.task.api;

import java.util.List;

public record MonthlyCategoryTrendResponse(
		String fromMonth,
		String toMonth,
		String zoneId,
		List<MonthlyCategoryTrendItem> trends
) {
}
