package com.jungyeons.dailylab.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAuditLogRepository extends JpaRepository<TaskAuditLog, Long> {

	List<TaskAuditLog> findByTaskIdOrderByIdAsc(long taskId);
}
