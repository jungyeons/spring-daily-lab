package com.jungyeons.dailylab.task;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.task.api.TaskStreakResponse;

@Service
public class TaskStreakService {

	private final TaskRepository taskRepository;
	private final Clock clock;

	public TaskStreakService(TaskRepository taskRepository, Clock clock) {
		this.taskRepository = taskRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public TaskStreakResponse calculate(ZoneId zoneId) {
		NavigableSet<LocalDate> completedDates = toLocalDates(taskRepository.findAllCompletionInstants(), zoneId);
		LocalDate today = LocalDate.now(clock.withZone(zoneId));
		return new TaskStreakResponse(
				calculateCurrent(completedDates, today),
				calculateLongest(completedDates),
				completedDates.isEmpty() ? null : completedDates.last(),
				zoneId.getId()
		);
	}

	private NavigableSet<LocalDate> toLocalDates(List<Instant> instants, ZoneId zoneId) {
		NavigableSet<LocalDate> dates = new TreeSet<>();
		instants.forEach(instant -> dates.add(instant.atZone(zoneId).toLocalDate()));
		return dates;
	}

	private int calculateCurrent(NavigableSet<LocalDate> dates, LocalDate today) {
		LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);
		int streak = 0;
		while (dates.contains(cursor)) {
			streak++;
			cursor = cursor.minusDays(1);
		}
		return streak;
	}

	private int calculateLongest(NavigableSet<LocalDate> dates) {
		LocalDate previous = null;
		int running = 0;
		int longest = 0;
		for (LocalDate date : dates) {
			running = previous != null && date.equals(previous.plusDays(1)) ? running + 1 : 1;
			longest = Math.max(longest, running);
			previous = date;
		}
		return longest;
	}
}
