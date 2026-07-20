package com.jungyeons.dailylab.task;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "task_creation_idempotency")
public class TaskCreationIdempotency {

	@Id
	@Column(name = "idempotency_key", nullable = false, length = 128)
	private String key;

	@Column(name = "request_fingerprint", nullable = false, length = 64)
	private String requestFingerprint;

	@Column(name = "task_id", nullable = false)
	private Long taskId;

	protected TaskCreationIdempotency() {
	}

	public TaskCreationIdempotency(String key, String requestFingerprint, Long taskId) {
		this.key = Objects.requireNonNull(key, "key must not be null");
		this.requestFingerprint = Objects.requireNonNull(requestFingerprint, "requestFingerprint must not be null");
		this.taskId = Objects.requireNonNull(taskId, "taskId must not be null");
	}

	public boolean matches(String fingerprint) {
		return requestFingerprint.equals(fingerprint);
	}

	public Long getTaskId() {
		return taskId;
	}
}
