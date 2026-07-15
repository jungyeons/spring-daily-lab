package com.jungyeons.dailylab.task;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.TaskTrendSource;

public interface TaskRepository extends JpaRepository<GrowthTask, Long> {

	Page<GrowthTask> findByStatus(TaskStatus status, Pageable pageable);

	Page<GrowthTask> findByCategory(TaskCategory category, Pageable pageable);

	Page<GrowthTask> findByStatusAndCategory(TaskStatus status, TaskCategory category, Pageable pageable);

	long countByStatus(TaskStatus status);

	long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

	@Query("""
			select new com.jungyeons.dailylab.task.api.TaskTrendSource(task.category, task.createdAt, task.completedAt)
			from GrowthTask task
			where (task.createdAt >= :start and task.createdAt < :end)
			   or (task.completedAt >= :start and task.completedAt < :end)
			""")
	List<TaskTrendSource> findTrendSources(Instant start, Instant end);
}
