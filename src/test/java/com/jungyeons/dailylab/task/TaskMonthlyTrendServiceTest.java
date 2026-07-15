package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.web.server.ResponseStatusException;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.task.api.MonthlyCategoryTrendItem;
import com.jungyeons.dailylab.task.api.MonthlyCategoryTrendResponse;
import com.jungyeons.dailylab.task.api.TaskTrendSource;

@MockitoSettings
class TaskMonthlyTrendServiceTest {

	@Mock
	private TaskRepository taskRepository;

	private TaskMonthlyTrendService trendService;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC);
		trendService = new TaskMonthlyTrendService(taskRepository, clock);
	}

	@Test
	void groupsCreatedAndCompletedCountsByLocalMonthAndCategory() {
		ZoneId seoul = ZoneId.of("Asia/Seoul");
		Instant start = Instant.parse("2026-05-31T15:00:00Z");
		Instant end = Instant.parse("2026-07-31T15:00:00Z");
		when(taskRepository.findTrendSources(start, end)).thenReturn(List.of(
				new TaskTrendSource(TaskCategory.FEATURE, Instant.parse("2026-06-30T16:00:00Z"), Instant.parse("2026-07-02T00:00:00Z")),
				new TaskTrendSource(TaskCategory.FEATURE, Instant.parse("2026-07-05T00:00:00Z"), null),
				new TaskTrendSource(TaskCategory.TEST, Instant.parse("2026-06-15T00:00:00Z"), Instant.parse("2026-06-30T14:30:00Z"))
		));

		MonthlyCategoryTrendResponse response = trendService.report(2, seoul);

		assertThat(response.fromMonth()).isEqualTo("2026-06");
		assertThat(response.toMonth()).isEqualTo("2026-07");
		assertThat(response.trends()).hasSize(TaskCategory.values().length * 2);
		assertThat(find(response, "2026-07", TaskCategory.FEATURE))
				.extracting(MonthlyCategoryTrendItem::created, MonthlyCategoryTrendItem::completed)
				.containsExactly(2L, 1L);
		assertThat(find(response, "2026-06", TaskCategory.TEST))
				.extracting(MonthlyCategoryTrendItem::created, MonthlyCategoryTrendItem::completed)
				.containsExactly(1L, 1L);
	}

	@Test
	void rejectsAnUnboundedMonthRange() {
		assertThatThrownBy(() -> trendService.report(25, ZoneOffset.UTC))
				.isInstanceOf(ResponseStatusException.class)
				.hasMessageContaining("months must be between 1 and 24");
	}

	private MonthlyCategoryTrendItem find(
			MonthlyCategoryTrendResponse response,
			String month,
			TaskCategory category
	) {
		return response.trends().stream()
				.filter(item -> item.month().equals(month) && item.category() == category)
				.findFirst()
				.orElseThrow();
	}
}
