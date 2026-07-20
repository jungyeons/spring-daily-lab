package com.jungyeons.dailylab.common;

public class TaskDependencyCycleException extends RuntimeException {

	public TaskDependencyCycleException() {
		super("Task dependencies must not contain a cycle");
	}
}
