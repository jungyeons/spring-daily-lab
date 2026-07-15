package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import com.jungyeons.dailylab.task.api.TaskStreakResponse;

@MockitoSettings
class TaskStreakServiceTest {

	@Mock
	private TaskRepository taskRepository;

	private TaskStreakService taskStreakService;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2026-01-03T00:30:00Z"), ZoneOffset.UTC);
		taskStreakService = new TaskStreakService(taskRepository, clock);
	}

	@Test
	void calculatesStreaksAcrossTimeZoneDateBoundaries() {
		when(taskRepository.findAllCompletionInstants()).thenReturn(List.of(
				Instant.parse("2025-12-31T16:00:00Z"),
				Instant.parse("2026-01-01T16:00:00Z"),
				Instant.parse("2026-01-02T15:30:00Z"),
				Instant.parse("2026-01-02T16:30:00Z")
		));

		TaskStreakResponse seoul = taskStreakService.calculate(ZoneId.of("Asia/Seoul"));
		TaskStreakResponse losAngeles = taskStreakService.calculate(ZoneId.of("America/Los_Angeles"));

		assertThat(seoul.currentStreak()).isEqualTo(3);
		assertThat(seoul.longestStreak()).isEqualTo(3);
		assertThat(seoul.lastCompletedDate()).hasToString("2026-01-03");
		assertThat(losAngeles.currentStreak()).isEqualTo(3);
		assertThat(losAngeles.lastCompletedDate()).hasToString("2026-01-02");
	}

	@Test
	void allowsTodayToBeMissingWithoutBreakingAnActiveStreak() {
		when(taskRepository.findAllCompletionInstants()).thenReturn(List.of(
				Instant.parse("2026-01-01T10:00:00Z"),
				Instant.parse("2026-01-02T10:00:00Z")
		));

		TaskStreakResponse response = taskStreakService.calculate(ZoneOffset.UTC);

		assertThat(response.currentStreak()).isEqualTo(2);
		assertThat(response.longestStreak()).isEqualTo(2);
	}
}
