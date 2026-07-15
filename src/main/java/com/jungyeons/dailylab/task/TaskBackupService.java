package com.jungyeons.dailylab.task;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.task.api.TaskBackupResponse;
import com.jungyeons.dailylab.task.api.TaskResponse;

@Service
public class TaskBackupService {

	public static final int SCHEMA_VERSION = 1;

	private final TaskRepository taskRepository;
	private final Clock clock;

	@Autowired
	public TaskBackupService(TaskRepository taskRepository) {
		this(taskRepository, Clock.systemUTC());
	}

	TaskBackupService(TaskRepository taskRepository, Clock clock) {
		this.taskRepository = taskRepository;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public TaskBackupResponse export() {
		List<TaskResponse> tasks = taskRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
				.map(TaskResponse::from)
				.toList();
		return new TaskBackupResponse(SCHEMA_VERSION, Instant.now(clock), tasks);
	}
}
