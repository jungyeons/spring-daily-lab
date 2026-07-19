package com.jungyeons.dailylab.task.progress;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_progress_entries")
public class TaskProgressEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private long taskId;

	@Column(nullable = false)
	private int percent;

	@Column(length = 500)
	private String note;

	@Column(nullable = false, updatable = false)
	private Instant recordedAt;

	protected TaskProgressEntry() {
	}

	private TaskProgressEntry(long taskId, int percent, String note) {
		this.taskId = taskId;
		this.percent = percent;
		this.note = note == null || note.isBlank() ? null : note.trim();
	}

	public static TaskProgressEntry create(long taskId, int percent, String note) {
		return new TaskProgressEntry(taskId, percent, note);
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

	public int getPercent() {
		return percent;
	}

	public String getNote() {
		return note;
	}

	public Instant getRecordedAt() {
		return recordedAt;
	}
}
