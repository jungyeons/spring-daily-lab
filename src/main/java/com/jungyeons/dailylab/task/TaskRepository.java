package com.jungyeons.dailylab.task;

import java.time.LocalDate;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;

public interface TaskRepository extends JpaRepository<GrowthTask, Long> {

	Page<GrowthTask> findByStatus(TaskStatus status, Pageable pageable);

	Page<GrowthTask> findByCategory(TaskCategory category, Pageable pageable);

	Page<GrowthTask> findByStatusAndCategory(TaskStatus status, TaskCategory category, Pageable pageable);

	List<GrowthTask> findByIdGreaterThanOrderByIdAsc(long cursor, Pageable pageable);

	List<GrowthTask> findByStatusAndIdGreaterThanOrderByIdAsc(TaskStatus status, long cursor, Pageable pageable);

	List<GrowthTask> findByCategoryAndIdGreaterThanOrderByIdAsc(TaskCategory category, long cursor, Pageable pageable);

	List<GrowthTask> findByStatusAndCategoryAndIdGreaterThanOrderByIdAsc(
			TaskStatus status,
			TaskCategory category,
			long cursor,
			Pageable pageable
	);

	long countByStatus(TaskStatus status);

	long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);
}
