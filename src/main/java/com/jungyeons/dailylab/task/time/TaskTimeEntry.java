package com.jungyeons.dailylab.task.time;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_time_entries")
public class TaskTimeEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private long taskId;

	@Column(nullable = false)
	private int durationMinutes;

	@Column(length = 500)
	private String note;

	@Column(nullable = false, updatable = false)
	private Instant recordedAt;

	protected TaskTimeEntry() {
	}

	private TaskTimeEntry(long taskId, int durationMinutes, String note) {
		if (durationMinutes <= 0) {
			throw new IllegalArgumentException("durationMinutes must be positive");
		}
		this.taskId = taskId;
		this.durationMinutes = durationMinutes;
		this.note = note == null || note.isBlank() ? null : note.trim();
	}

	public static TaskTimeEntry create(long taskId, int durationMinutes, String note) {
		return new TaskTimeEntry(taskId, durationMinutes, note);
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

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public String getNote() {
		return note;
	}

	public Instant getRecordedAt() {
		return recordedAt;
	}
}
