package com.jungyeons.dailylab.task;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.common.TaskNotFoundException;
import com.jungyeons.dailylab.common.TaskDeletionConflictException;
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
		return findAll(status, category, false, pageable);
	}

	@Transactional(readOnly = true)
	public Page<TaskResponse> findAll(TaskStatus status, TaskCategory category, boolean deleted, Pageable pageable) {
		Page<GrowthTask> tasks;
		if (deleted) {
			tasks = findDeleted(status, category, pageable);
		} else {
			tasks = findActive(status, category, pageable);
		}
		return tasks.map(TaskResponse::from);
	}

	@Transactional(readOnly = true)
	public TaskResponse findById(long id) {
		return TaskResponse.from(getTask(id));
	}

	public TaskResponse update(long id, UpdateTaskRequest request) {
		GrowthTask task = getActiveTask(id);
		task.update(
				request.title(),
				request.description(),
				request.category(),
				request.priority(),
				request.dueDate()
		);
		return TaskResponse.from(task);
	}

	public TaskResponse changeStatus(long id, ChangeTaskStatusRequest request) {
		GrowthTask task = getActiveTask(id);
		task.changeStatus(request.status());
		return TaskResponse.from(task);
	}

	public void delete(long id) {
		GrowthTask task = getTask(id);
		task.softDelete();
	}

	public TaskResponse restore(long id) {
		GrowthTask task = getTask(id);
		task.restore();
		return TaskResponse.from(task);
	}

	@Transactional(readOnly = true)
	public TaskSummaryResponse summary() {
		return new TaskSummaryResponse(
			taskRepository.countByDeletedAtIsNull(),
			taskRepository.countByDeletedAtIsNullAndStatus(TaskStatus.TODO),
			taskRepository.countByDeletedAtIsNullAndStatus(TaskStatus.IN_PROGRESS),
			taskRepository.countByDeletedAtIsNullAndStatus(TaskStatus.DONE),
			taskRepository.countByDeletedAtIsNullAndDueDateBeforeAndStatusNot(LocalDate.now(), TaskStatus.DONE)
		);
	}

	private Page<GrowthTask> findActive(TaskStatus status, TaskCategory category, Pageable pageable) {
		if (status != null && category != null) {
			return taskRepository.findByDeletedAtIsNullAndStatusAndCategory(status, category, pageable);
		}
		if (status != null) {
			return taskRepository.findByDeletedAtIsNullAndStatus(status, pageable);
		}
		if (category != null) {
			return taskRepository.findByDeletedAtIsNullAndCategory(category, pageable);
		}
		return taskRepository.findByDeletedAtIsNull(pageable);
	}

	private Page<GrowthTask> findDeleted(TaskStatus status, TaskCategory category, Pageable pageable) {
		if (status != null && category != null) {
			return taskRepository.findByDeletedAtIsNotNullAndStatusAndCategory(status, category, pageable);
		}
		if (status != null) {
			return taskRepository.findByDeletedAtIsNotNullAndStatus(status, pageable);
		}
		if (category != null) {
			return taskRepository.findByDeletedAtIsNotNullAndCategory(category, pageable);
		}
		return taskRepository.findByDeletedAtIsNotNull(pageable);
	}

	private GrowthTask getActiveTask(long id) {
		GrowthTask task = getTask(id);
		if (task.isDeleted()) {
			throw new TaskDeletionConflictException("The task is deleted and must be restored first");
		}
		return task;
	}

	private GrowthTask getTask(long id) {
		return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}
}
