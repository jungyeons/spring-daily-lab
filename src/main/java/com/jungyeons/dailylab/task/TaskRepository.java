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

public interface TaskRepository extends JpaRepository<GrowthTask, Long> {

	@Query("""
			select task from GrowthTask task
			where (:status is null or task.status = :status)
			  and (:category is null or task.category = :category)
			  and (:dueDateFrom is null or task.dueDate >= :dueDateFrom)
			  and (:dueDateTo is null or task.dueDate <= :dueDateTo)
			""")
	Page<GrowthTask> findAllFiltered(
			@Param("status") TaskStatus status,
			@Param("category") TaskCategory category,
			@Param("dueDateFrom") LocalDate dueDateFrom,
			@Param("dueDateTo") LocalDate dueDateTo,
			Pageable pageable
	);

	long countByStatus(TaskStatus status);

	long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);
}
