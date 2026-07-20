package com.jungyeons.dailylab.task;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.common.TaskNotFoundException;
import com.jungyeons.dailylab.common.TaskDependencyCycleException;
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

	public TaskResponse addPrerequisite(long id, long prerequisiteId) {
		GrowthTask task = getTask(id);
		GrowthTask prerequisite = getTask(prerequisiteId);
		if (reaches(prerequisite, task.getId(), new HashSet<>())) {
			throw new TaskDependencyCycleException();
		}
		task.addPrerequisite(prerequisite);
		return TaskResponse.from(task);
	}

	public void removePrerequisite(long id, long prerequisiteId) {
		getTask(id).removePrerequisite(getTask(prerequisiteId));
	}

	@Transactional(readOnly = true)
	public List<TaskResponse> findPrerequisites(long id) {
		return getTask(id).getPrerequisites().stream().map(TaskResponse::from).toList();
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

	private boolean reaches(GrowthTask current, Long targetId, Set<Long> visited) {
		if (current.getId().equals(targetId)) {
			return true;
		}
		if (!visited.add(current.getId())) {
			return false;
		}
		return current.getPrerequisites().stream().anyMatch(task -> reaches(task, targetId, visited));
	}
}
