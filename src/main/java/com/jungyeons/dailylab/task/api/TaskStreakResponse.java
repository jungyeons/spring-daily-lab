package com.jungyeons.dailylab.task.api;

import java.time.LocalDate;

public record TaskStreakResponse(
		int currentStreak,
		int longestStreak,
		LocalDate lastCompletedDate,
		String zoneId
) {
}
