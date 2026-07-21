package com.jungyeons.dailylab.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.jungyeons.dailylab.common.TaskRecurrenceConflictException;

@Entity
@Table(name = "growth_tasks")
public class GrowthTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

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
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	private Instant completedAt;

	@Enumerated(EnumType.STRING)
	@Column(length = 16)
	private RecurrenceFrequency recurrenceFrequency;

	private Instant recurrenceGeneratedAt;

	@Version
	@Column(nullable = false)
	private long version;

	protected GrowthTask() {
	}

	private GrowthTask(String title, String description, TaskCategory category, int priority, LocalDate dueDate) {
		this.title = normalizeTitle(title);
		this.description = normalizeDescription(description);
		this.category = Objects.requireNonNull(category, "category must not be null");
		this.status = TaskStatus.TODO;
		this.priority = validatePriority(priority);
		this.dueDate = dueDate;
	}

	public static GrowthTask create(
			String title,
			String description,
			TaskCategory category,
			int priority,
			LocalDate dueDate
	) {
		return new GrowthTask(title, description, category, priority, dueDate);
	}

	public void update(
			String title,
			String description,
			TaskCategory category,
			int priority,
			LocalDate dueDate
	) {
		this.title = normalizeTitle(title);
		this.description = normalizeDescription(description);
		this.category = Objects.requireNonNull(category, "category must not be null");
		this.priority = validatePriority(priority);
		this.dueDate = dueDate;
	}

	public void changeStatus(TaskStatus newStatus) {
		this.status = Objects.requireNonNull(newStatus, "status must not be null");
		this.completedAt = newStatus == TaskStatus.DONE ? Instant.now() : null;
	}

	public void configureRecurrence(RecurrenceFrequency frequency) {
		if (dueDate == null) {
			throw new TaskRecurrenceConflictException("A recurring task must have a due date");
		}
		if (recurrenceGeneratedAt != null) {
			throw new TaskRecurrenceConflictException("The next recurring task has already been generated");
		}
		this.recurrenceFrequency = Objects.requireNonNull(frequency, "frequency must not be null");
	}

	public GrowthTask generateNextRecurringTask() {
		if (recurrenceFrequency == null) {
			throw new TaskRecurrenceConflictException("The task does not have a recurrence rule");
		}
		if (recurrenceGeneratedAt != null) {
			throw new TaskRecurrenceConflictException("The next recurring task has already been generated");
		}
		GrowthTask nextTask = GrowthTask.create(
				title,
				description,
				category,
				priority,
				recurrenceFrequency.nextDueDate(dueDate)
		);
		nextTask.recurrenceFrequency = recurrenceFrequency;
		recurrenceGeneratedAt = Instant.now();
		return nextTask;
	}

	public boolean isOverdue(LocalDate today) {
		return dueDate != null && dueDate.isBefore(today) && status != TaskStatus.DONE;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	private static String normalizeTitle(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("title must not be blank");
		}
		return value.trim();
	}

	private static String normalizeDescription(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private static int validatePriority(int value) {
		if (value < 1 || value > 5) {
			throw new IllegalArgumentException("priority must be between 1 and 5");
		}
		return value;
	}

	public Long getId() {
		return id;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public RecurrenceFrequency getRecurrenceFrequency() {
		return recurrenceFrequency;
	}

	public Instant getRecurrenceGeneratedAt() {
		return recurrenceGeneratedAt;
	}

	public long getVersion() {
		return version;
	}
}
