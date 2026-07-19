package com.jungyeons.dailylab.task;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.common.TaskNotFoundException;
import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskTimeEntryRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;
import com.jungyeons.dailylab.task.api.TaskSummaryResponse;
import com.jungyeons.dailylab.task.api.TaskTimeEntryResponse;
import com.jungyeons.dailylab.task.api.TaskTimeSummaryResponse;
import com.jungyeons.dailylab.task.api.UpdateTaskRequest;
import com.jungyeons.dailylab.task.time.TaskTimeEntry;
import com.jungyeons.dailylab.task.time.TaskTimeEntryRepository;

@Service
@Transactional
public class TaskService {

	private final TaskRepository taskRepository;
	private final TaskTimeEntryRepository taskTimeEntryRepository;

	public TaskService(TaskRepository taskRepository, TaskTimeEntryRepository taskTimeEntryRepository) {
		this.taskRepository = taskRepository;
		this.taskTimeEntryRepository = taskTimeEntryRepository;
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

	public TaskTimeEntryResponse recordTime(long taskId, CreateTaskTimeEntryRequest request) {
		getTask(taskId);
		TaskTimeEntry entry = TaskTimeEntry.create(taskId, request.durationMinutes(), request.note());
		return TaskTimeEntryResponse.from(taskTimeEntryRepository.save(entry));
	}

	@Transactional(readOnly = true)
	public TaskTimeSummaryResponse timeSummary(long taskId) {
		getTask(taskId);
		List<TaskTimeEntryResponse> entries = taskTimeEntryRepository
				.findByTaskIdOrderByRecordedAtDescIdDesc(taskId).stream()
				.map(TaskTimeEntryResponse::from)
				.toList();
		return new TaskTimeSummaryResponse(
				taskId,
				taskTimeEntryRepository.sumDurationMinutesByTaskId(taskId),
				entries
		);
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
}
