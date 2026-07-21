package com.jungyeons.dailylab.task;

import java.time.LocalDate;

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

	Page<GrowthTask> findByDeletedAtIsNull(Pageable pageable);

	Page<GrowthTask> findByDeletedAtIsNullAndStatus(TaskStatus status, Pageable pageable);

	Page<GrowthTask> findByDeletedAtIsNullAndCategory(TaskCategory category, Pageable pageable);

	Page<GrowthTask> findByDeletedAtIsNullAndStatusAndCategory(
			TaskStatus status,
			TaskCategory category,
			Pageable pageable
	);

	Page<GrowthTask> findByDeletedAtIsNotNull(Pageable pageable);

	Page<GrowthTask> findByDeletedAtIsNotNullAndStatus(TaskStatus status, Pageable pageable);

	Page<GrowthTask> findByDeletedAtIsNotNullAndCategory(TaskCategory category, Pageable pageable);

	Page<GrowthTask> findByDeletedAtIsNotNullAndStatusAndCategory(
			TaskStatus status,
			TaskCategory category,
			Pageable pageable
	);

	long countByDeletedAtIsNull();

	long countByDeletedAtIsNullAndStatus(TaskStatus status);

	long countByDeletedAtIsNullAndDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);
}
