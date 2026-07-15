package com.jungyeons.dailylab.task;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.common.TaskNotFoundException;
import com.jungyeons.dailylab.common.TaskVersionMismatchException;
import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;
import com.jungyeons.dailylab.task.api.TaskSummaryResponse;
import com.jungyeons.dailylab.task.api.UpdateTaskRequest;

@Service
@Transactional
public class TaskService {

	private final TaskRepository taskRepository;

	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public TaskResponse create(CreateTaskRequest request) {
		GrowthTask task = GrowthTask.create(
				request.title(),
				request.description(),
				request.category(),
				request.priority(),
				request.dueDate()
		);
		return TaskResponse.from(taskRepository.save(task));
	}

	@Transactional(readOnly = true)
	public Page<TaskResponse> findAll(TaskStatus status, TaskCategory category, Pageable pageable) {
		Page<GrowthTask> tasks;
		if (status != null && category != null) {
			tasks = taskRepository.findByStatusAndCategory(status, category, pageable);
		} else if (status != null) {
			tasks = taskRepository.findByStatus(status, pageable);
		} else if (category != null) {
			tasks = taskRepository.findByCategory(category, pageable);
		} else {
			tasks = taskRepository.findAll(pageable);
		}
		return tasks.map(TaskResponse::from);
	}

	@Transactional(readOnly = true)
	public TaskResponse findById(long id) {
		return TaskResponse.from(getTask(id));
	}

	public TaskResponse update(long id, UpdateTaskRequest request) {
		return update(id, request, null);
	}

	public TaskResponse update(long id, UpdateTaskRequest request, Long expectedVersion) {
		GrowthTask task = getTask(id);
		checkVersion(task, expectedVersion);
		task.update(
				request.title(),
				request.description(),
				request.category(),
				request.priority(),
				request.dueDate()
		);
		taskRepository.flush();
		return TaskResponse.from(task);
	}

	public TaskResponse changeStatus(long id, ChangeTaskStatusRequest request) {
		return changeStatus(id, request, null);
	}

	public TaskResponse changeStatus(long id, ChangeTaskStatusRequest request, Long expectedVersion) {
		GrowthTask task = getTask(id);
		checkVersion(task, expectedVersion);
		task.changeStatus(request.status());
		taskRepository.flush();
		return TaskResponse.from(task);
	}

	public void delete(long id) {
		delete(id, null);
	}

	public void delete(long id, Long expectedVersion) {
		GrowthTask task = getTask(id);
		checkVersion(task, expectedVersion);
		taskRepository.delete(task);
	}

	@Transactional(readOnly = true)
	public TaskSummaryResponse summary() {
		return new TaskSummaryResponse(
				taskRepository.count(),
				taskRepository.countByStatus(TaskStatus.TODO),
				taskRepository.countByStatus(TaskStatus.IN_PROGRESS),
				taskRepository.countByStatus(TaskStatus.DONE),
				taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE)
		);
	}

	private GrowthTask getTask(long id) {
		return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}

	private void checkVersion(GrowthTask task, Long expectedVersion) {
		if (expectedVersion != null && task.getVersion() != expectedVersion) {
			throw new TaskVersionMismatchException(expectedVersion, task.getVersion());
		}
	}
}
