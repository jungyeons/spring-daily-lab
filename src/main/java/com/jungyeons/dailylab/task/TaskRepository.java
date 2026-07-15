package com.jungyeons.dailylab.task;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;

public interface TaskRepository extends JpaRepository<GrowthTask, Long>, JpaSpecificationExecutor<GrowthTask> {

	Page<GrowthTask> findByStatus(TaskStatus status, Pageable pageable);

	Page<GrowthTask> findByCategory(TaskCategory category, Pageable pageable);

	Page<GrowthTask> findByStatusAndCategory(TaskStatus status, TaskCategory category, Pageable pageable);

	long countByStatus(TaskStatus status);

	long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);

	long countByArchivedAtIsNull();

	long countByStatusAndArchivedAtIsNull(TaskStatus status);

	long countByDueDateBeforeAndStatusNotAndArchivedAtIsNull(LocalDate date, TaskStatus status);
}
