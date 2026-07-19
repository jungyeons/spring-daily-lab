package com.jungyeons.dailylab.task.progress;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskProgressEntryRepository extends JpaRepository<TaskProgressEntry, Long> {

	List<TaskProgressEntry> findByTaskIdOrderByRecordedAtDescIdDesc(long taskId);
}
