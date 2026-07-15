package com.jungyeons.dailylab.common;

public class TaskVersionMismatchException extends RuntimeException {

	private final long expectedVersion;
	private final long actualVersion;

	public TaskVersionMismatchException(long expectedVersion, long actualVersion) {
		super("Task version does not match the If-Match precondition");
		this.expectedVersion = expectedVersion;
		this.actualVersion = actualVersion;
	}

	public long getExpectedVersion() {
		return expectedVersion;
	}

	public long getActualVersion() {
		return actualVersion;
	}
}
