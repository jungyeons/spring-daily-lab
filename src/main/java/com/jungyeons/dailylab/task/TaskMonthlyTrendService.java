package com.jungyeons.dailylab.task;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.task.api.MonthlyCategoryTrendItem;
import com.jungyeons.dailylab.task.api.MonthlyCategoryTrendResponse;
import com.jungyeons.dailylab.task.api.TaskTrendSource;

@Service
public class TaskMonthlyTrendService {

	private final TaskRepository taskRepository;
	private final Clock clock;

	@Autowired
	public TaskMonthlyTrendService(TaskRepository taskRepository) {
		this(taskRepository, Clock.systemUTC());
	}

	TaskMonthlyTrendService(TaskRepository taskRepository, Clock clock) {
		this.taskRepository = taskRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public MonthlyCategoryTrendResponse report(int months, ZoneId zoneId) {
		if (months < 1 || months > 24) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "months must be between 1 and 24");
		}
		YearMonth toMonth = YearMonth.now(clock.withZone(zoneId));
		YearMonth fromMonth = toMonth.minusMonths(months - 1L);
		Instant start = fromMonth.atDay(1).atStartOfDay(zoneId).toInstant();
		Instant end = toMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant();
		Map<TrendKey, TrendCount> counts = initializeCounts(fromMonth, toMonth);

		for (TaskTrendSource source : taskRepository.findTrendSources(start, end)) {
			increment(counts, source.category(), source.createdAt(), zoneId, true);
			increment(counts, source.category(), source.completedAt(), zoneId, false);
		}

		List<MonthlyCategoryTrendItem> trends = counts.entrySet().stream()
				.map(entry -> new MonthlyCategoryTrendItem(
						entry.getKey().month().toString(),
						entry.getKey().category(),
						entry.getValue().created,
						entry.getValue().completed
				))
				.toList();
		return new MonthlyCategoryTrendResponse(fromMonth.toString(), toMonth.toString(), zoneId.getId(), trends);
	}

	private Map<TrendKey, TrendCount> initializeCounts(YearMonth fromMonth, YearMonth toMonth) {
		Map<TrendKey, TrendCount> counts = new LinkedHashMap<>();
		for (YearMonth month = fromMonth; !month.isAfter(toMonth); month = month.plusMonths(1)) {
			for (TaskCategory category : TaskCategory.values()) {
				counts.put(new TrendKey(month, category), new TrendCount());
			}
		}
		return counts;
	}

	private void increment(
			Map<TrendKey, TrendCount> counts,
			TaskCategory category,
			Instant instant,
			ZoneId zoneId,
			boolean created
	) {
		if (instant == null) {
			return;
		}
		TrendCount count = counts.get(new TrendKey(YearMonth.from(instant.atZone(zoneId)), category));
		if (count != null) {
			if (created) {
				count.created++;
			} else {
				count.completed++;
			}
		}
	}

	private record TrendKey(YearMonth month, TaskCategory category) {
	}

	private static final class TrendCount {
		private long created;
		private long completed;
	}
}
