package com.jungyeons.dailylab.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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

	@ManyToMany
	@JoinTable(
			name = "growth_task_tags",
			joinColumns = @JoinColumn(name = "task_id"),
			inverseJoinColumns = @JoinColumn(name = "tag_id")
	)
	private Set<TaskTag> tags = new LinkedHashSet<>();

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

	public boolean isOverdue(LocalDate today) {
		return dueDate != null && dueDate.isBefore(today) && status != TaskStatus.DONE;
	}

	public void setTags(Set<TaskTag> tags) {
		this.tags = new LinkedHashSet<>(Objects.requireNonNull(tags, "tags must not be null"));
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

	public Set<TaskTag> getTags() {
		return Set.copyOf(tags);
	}

	public long getVersion() {
		return version;
	}
}
