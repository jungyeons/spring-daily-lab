package com.jungyeons.dailylab.task.api;

import java.time.LocalDate;

public record WeeklyTaskReportResponse(
		LocalDate weekStart,
		LocalDate weekEnd,
		String zoneId,
		long completed,
		long incomplete,
		long overdue
) {
}
