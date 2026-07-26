package com.jungyeons.dailylab.task;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.TaskSummaryResponse;

public interface TaskRepository extends JpaRepository<GrowthTask, Long> {

	Page<GrowthTask> findByStatus(TaskStatus status, Pageable pageable);

	Page<GrowthTask> findByCategory(TaskCategory category, Pageable pageable);

	Page<GrowthTask> findByStatusAndCategory(TaskStatus status, TaskCategory category, Pageable pageable);

	@Query("""
			select new com.jungyeons.dailylab.task.api.TaskSummaryResponse(
				count(task),
				coalesce(sum(case when task.status = com.jungyeons.dailylab.domain.TaskStatus.TODO then 1 else 0 end), 0),
				coalesce(sum(case when task.status = com.jungyeons.dailylab.domain.TaskStatus.IN_PROGRESS then 1 else 0 end), 0),
				coalesce(sum(case when task.status = com.jungyeons.dailylab.domain.TaskStatus.DONE then 1 else 0 end), 0),
				coalesce(sum(case when task.dueDate < :today
					and task.status <> com.jungyeons.dailylab.domain.TaskStatus.DONE then 1 else 0 end), 0)
			)
			from GrowthTask task
			""")
	TaskSummaryResponse summarize(@Param("today") LocalDate today);
}
