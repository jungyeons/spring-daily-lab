package com.jungyeons.dailylab.task.time;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskTimeEntryRepository extends JpaRepository<TaskTimeEntry, Long> {

	List<TaskTimeEntry> findByTaskIdOrderByRecordedAtDescIdDesc(long taskId);

	@Query("select coalesce(sum(entry.durationMinutes), 0) from TaskTimeEntry entry where entry.taskId = :taskId")
	long sumDurationMinutesByTaskId(@Param("taskId") long taskId);
}
