package com.jungyeons.dailylab.task;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.common.TaskNotFoundException;
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
		Pageable stablePageable = withStableSort(pageable);
		Page<GrowthTask> tasks;
		if (status != null && category != null) {
			tasks = taskRepository.findByStatusAndCategory(status, category, stablePageable);
		} else if (status != null) {
			tasks = taskRepository.findByStatus(status, stablePageable);
		} else if (category != null) {
			tasks = taskRepository.findByCategory(category, stablePageable);
		} else {
			tasks = taskRepository.findAll(stablePageable);
		}
		return tasks.map(TaskResponse::from);
	}

	@Transactional(readOnly = true)
	public TaskResponse findById(long id) {
		return TaskResponse.from(getTask(id));
	}

	public TaskResponse update(long id, UpdateTaskRequest request) {
		GrowthTask task = getTask(id);
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
		GrowthTask task = getTask(id);
		task.changeStatus(request.status());
		return TaskResponse.from(task);
	}

	public void delete(long id) {
		GrowthTask task = getTask(id);
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

	private Pageable withStableSort(Pageable pageable) {
		if (pageable.isUnpaged() || pageable.getSort().getOrderFor("id") != null) {
			return pageable;
		}
		Sort stableSort = pageable.getSort().and(Sort.by("id"));
		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), stableSort);
	}
}
