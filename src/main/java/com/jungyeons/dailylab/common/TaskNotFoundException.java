package com.jungyeons.dailylab.common;

public class TaskNotFoundException extends RuntimeException {

	public TaskNotFoundException(long id) {
		super("Task not found: " + id);
	}
}
