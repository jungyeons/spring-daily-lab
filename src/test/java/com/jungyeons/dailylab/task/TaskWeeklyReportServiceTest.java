package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.web.server.ResponseStatusException;

import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.WeeklyTaskReportResponse;

@MockitoSettings
class TaskWeeklyReportServiceTest {

	@Mock
	private TaskRepository taskRepository;

	private TaskWeeklyReportService reportService;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2026-07-15T03:00:00Z"), ZoneOffset.UTC);
		reportService = new TaskWeeklyReportService(taskRepository, clock);
	}

	@Test
	void reportsCompletedIncompleteAndOverdueForAZoneAwareWeek() {
		ZoneId zoneId = ZoneId.of("Asia/Seoul");
		LocalDate weekStart = LocalDate.parse("2026-07-13");
		Instant start = Instant.parse("2026-07-12T15:00:00Z");
		Instant end = Instant.parse("2026-07-19T15:00:00Z");
		when(taskRepository.countByCompletedAtGreaterThanEqualAndCompletedAtLessThan(start, end)).thenReturn(4L);
		when(taskRepository.countByCreatedAtLessThanAndStatusNot(end, TaskStatus.DONE)).thenReturn(3L);
		when(taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.parse("2026-07-16"), TaskStatus.DONE))
				.thenReturn(2L);

		WeeklyTaskReportResponse response = reportService.report(weekStart, zoneId);

		assertThat(response.completed()).isEqualTo(4);
		assertThat(response.incomplete()).isEqualTo(3);
		assertThat(response.overdue()).isEqualTo(2);
		assertThat(response.weekEnd()).isEqualTo(LocalDate.parse("2026-07-19"));
		assertThat(response.zoneId()).isEqualTo("Asia/Seoul");
	}

	@Test
	void defaultsToTheCurrentMondayAndRejectsOtherStartDays() {
		WeeklyTaskReportResponse response = reportService.report(null, ZoneOffset.UTC);

		assertThat(response.weekStart()).isEqualTo(LocalDate.parse("2026-07-13"));
		verify(taskRepository).countByDueDateBeforeAndStatusNot(LocalDate.parse("2026-07-16"), TaskStatus.DONE);

		assertThatThrownBy(() -> reportService.report(LocalDate.parse("2026-07-14"), ZoneOffset.UTC))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("weekStart must be a Monday");
	}
}
