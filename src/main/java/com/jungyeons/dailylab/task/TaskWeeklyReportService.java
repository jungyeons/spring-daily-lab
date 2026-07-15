package com.jungyeons.dailylab.task;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.WeeklyTaskReportResponse;

@Service
public class TaskWeeklyReportService {

	private final TaskRepository taskRepository;
	private final Clock clock;

	@Autowired
	public TaskWeeklyReportService(TaskRepository taskRepository) {
		this(taskRepository, Clock.systemUTC());
	}

	TaskWeeklyReportService(TaskRepository taskRepository, Clock clock) {
		this.taskRepository = taskRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public WeeklyTaskReportResponse report(LocalDate requestedWeekStart, ZoneId zoneId) {
		LocalDate today = LocalDate.now(clock.withZone(zoneId));
		LocalDate weekStart = requestedWeekStart == null
				? today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
				: requestedWeekStart;
		if (weekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weekStart must be a Monday");
		}

		LocalDate weekEnd = weekStart.plusDays(6);
		Instant start = weekStart.atStartOfDay(zoneId).toInstant();
		Instant endExclusive = weekEnd.plusDays(1).atStartOfDay(zoneId).toInstant();
		LocalDate overdueCutoff = today.isBefore(weekEnd) ? today.plusDays(1) : weekEnd.plusDays(1);

		return new WeeklyTaskReportResponse(
				weekStart,
				weekEnd,
				zoneId.getId(),
				taskRepository.countByCompletedAtGreaterThanEqualAndCompletedAtLessThan(start, endExclusive),
				taskRepository.countByCreatedAtLessThanAndStatusNot(endExclusive, TaskStatus.DONE),
				taskRepository.countByDueDateBeforeAndStatusNot(overdueCutoff, TaskStatus.DONE)
		);
	}
}
