package com.jungyeons.dailylab.task;

import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jungyeons.dailylab.domain.TaskStatus;

@ExtendWith(MockitoExtension.class)
class TaskSummaryTimeZoneTest {

	@Mock
	private TaskRepository taskRepository;

	@Test
	void calculatesTheOverdueCutoffInTheConfiguredTimeZone() {
		Clock seoulClock = Clock.fixed(
				Instant.parse("2026-07-26T15:30:00Z"),
				ZoneId.of("Asia/Seoul")
		);
		TaskService taskService = new TaskService(taskRepository, seoulClock);

		taskService.summary();

		verify(taskRepository).countByDueDateBeforeAndStatusNot(
				LocalDate.of(2026, 7, 27),
				TaskStatus.DONE
		);
	}
}
