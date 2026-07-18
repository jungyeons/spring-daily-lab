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

	@Query(
			value = """
					select distinct task from GrowthTask task
					left join task.tags tag
					where (:status is null or task.status = :status)
					  and (:category is null or task.category = :category)
					  and (:tagName is null or tag.name = :tagName)
					""",
			countQuery = """
					select count(distinct task) from GrowthTask task
					left join task.tags tag
					where (:status is null or task.status = :status)
					  and (:category is null or task.category = :category)
					  and (:tagName is null or tag.name = :tagName)
					"""
	)
	Page<GrowthTask> findByFilters(
			@Param("status") TaskStatus status,
			@Param("category") TaskCategory category,
			@Param("tagName") String tagName,
			Pageable pageable
	);

	long countByStatus(TaskStatus status);

	long countByDueDateBeforeAndStatusNot(LocalDate date, TaskStatus status);
}
