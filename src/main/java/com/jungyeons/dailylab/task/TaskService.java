package com.jungyeons.dailylab.task;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.common.TaskNotFoundException;
import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.domain.TaskTag;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;
import com.jungyeons.dailylab.task.api.TaskSummaryResponse;
import com.jungyeons.dailylab.task.api.UpdateTaskRequest;

@Service
@Transactional
public class TaskService {

	private final TaskRepository taskRepository;
	private final TaskTagRepository taskTagRepository;

	public TaskService(TaskRepository taskRepository, TaskTagRepository taskTagRepository) {
		this.taskRepository = taskRepository;
		this.taskTagRepository = taskTagRepository;
	}

	public TaskResponse create(CreateTaskRequest request) {
		GrowthTask task = GrowthTask.create(
				request.title(),
				request.description(),
				request.category(),
				request.priority(),
				request.dueDate()
		);
		setTags(task, request.tags());
		return TaskResponse.from(taskRepository.save(task));
	}

	@Transactional(readOnly = true)
	public Page<TaskResponse> findAll(TaskStatus status, TaskCategory category, String tag, Pageable pageable) {
		String tagName = tag == null || tag.isBlank() ? null : TaskTag.normalizeName(tag);
		return taskRepository.findByFilters(status, category, tagName, pageable).map(TaskResponse::from);
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
		setTags(task, request.tags());
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

	private void setTags(GrowthTask task, Collection<String> requestedTags) {
		Set<String> normalizedNames = normalizeTagNames(requestedTags);
		Map<String, TaskTag> tagsByName = new LinkedHashMap<>();
		taskTagRepository.findByNameIn(normalizedNames).forEach(tag -> tagsByName.put(tag.getName(), tag));
		for (String name : normalizedNames) {
			tagsByName.computeIfAbsent(name, ignored -> taskTagRepository.save(TaskTag.create(name)));
		}
		task.setTags(new LinkedHashSet<>(tagsByName.values()));
	}

	private Set<String> normalizeTagNames(Collection<String> tags) {
		if (tags == null || tags.isEmpty()) {
			return Set.of();
		}
		Set<String> normalizedNames = new LinkedHashSet<>();
		for (String tag : tags) {
			normalizedNames.add(TaskTag.normalizeName(tag));
		}
		return normalizedNames;
	}
}
