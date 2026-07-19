package com.jungyeons.dailylab.audit;

import java.time.Instant;
import java.time.LocalDate;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_audit_logs")
public class TaskAuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private long taskId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TaskAuditAction action;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(length = 2000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TaskCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TaskStatus status;

	@Column(nullable = false)
	private int priority;

	private LocalDate dueDate;

	@Column(nullable = false, updatable = false)
	private Instant recordedAt;

	protected TaskAuditLog() {
	}

	private TaskAuditLog(GrowthTask task, TaskAuditAction action) {
		this.taskId = task.getId();
		this.action = action;
		this.title = task.getTitle();
		this.description = task.getDescription();
		this.category = task.getCategory();
		this.status = task.getStatus();
		this.priority = task.getPriority();
		this.dueDate = task.getDueDate();
	}

	public static TaskAuditLog capture(GrowthTask task, TaskAuditAction action) {
		return new TaskAuditLog(task, action);
	}

	@PrePersist
	void onCreate() {
		recordedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public long getTaskId() {
		return taskId;
	}

	public TaskAuditAction getAction() {
		return action;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public TaskCategory getCategory() {
		return category;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public int getPriority() {
		return priority;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public Instant getRecordedAt() {
		return recordedAt;
	}
}
