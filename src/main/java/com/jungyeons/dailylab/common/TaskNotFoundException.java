package com.jungyeons.dailylab.common;

public class TaskNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TaskNotFoundException(long id) {
		super("Task not found: " + id);
	}
}
