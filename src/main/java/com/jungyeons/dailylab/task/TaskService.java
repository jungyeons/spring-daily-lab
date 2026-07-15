package com.jungyeons.dailylab.task;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
		return findAll(status, category, false, pageable);
	}

	@Transactional(readOnly = true)
	public Page<TaskResponse> findAll(TaskStatus status, TaskCategory category, boolean archived, Pageable pageable) {
		Specification<GrowthTask> specification = (root, query, builder) -> {
			var predicate = archived ? builder.isNotNull(root.get("archivedAt")) : builder.isNull(root.get("archivedAt"));
			if (status != null) {
				predicate = builder.and(predicate, builder.equal(root.get("status"), status));
			}
			if (category != null) {
				predicate = builder.and(predicate, builder.equal(root.get("category"), category));
			}
			return predicate;
		};
		Page<GrowthTask> tasks = taskRepository.findAll(specification, pageable);
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

	public TaskResponse archive(long id) {
		GrowthTask task = getTask(id);
		task.archive();
		return TaskResponse.from(task);
	}

	public TaskResponse unarchive(long id) {
		GrowthTask task = getTask(id);
		task.unarchive();
		return TaskResponse.from(task);
	}

	public void delete(long id) {
		GrowthTask task = getTask(id);
		taskRepository.delete(task);
	}

	@Transactional(readOnly = true)
	public TaskSummaryResponse summary() {
		return new TaskSummaryResponse(
				taskRepository.countByArchivedAtIsNull(),
				taskRepository.countByStatusAndArchivedAtIsNull(TaskStatus.TODO),
				taskRepository.countByStatusAndArchivedAtIsNull(TaskStatus.IN_PROGRESS),
				taskRepository.countByStatusAndArchivedAtIsNull(TaskStatus.DONE),
				taskRepository.countByDueDateBeforeAndStatusNotAndArchivedAtIsNull(LocalDate.now(), TaskStatus.DONE)
		);
	}

	private GrowthTask getTask(long id) {
		return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
	}
}
