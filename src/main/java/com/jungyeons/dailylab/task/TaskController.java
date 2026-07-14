package com.jungyeons.dailylab.task;

import java.net.URI;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.common.InvalidSortPropertyException;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;
import com.jungyeons.dailylab.task.api.TaskSummaryResponse;
import com.jungyeons.dailylab.task.api.UpdateTaskRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
			"id", "title", "category", "status", "priority", "dueDate", "createdAt", "updatedAt", "completedAt"
	);

	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest request) {
		TaskResponse created = taskService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.id())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@GetMapping
	public Page<TaskResponse> findAll(
			@RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) TaskCategory category,
			@PageableDefault(size = 20, sort = "createdAt") Pageable pageable
	) {
		validateSortProperties(pageable.getSort());
		return taskService.findAll(status, category, pageable);
	}

	private void validateSortProperties(Sort sort) {
		for (Sort.Order order : sort) {
			if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
				throw new InvalidSortPropertyException(order.getProperty());
			}
		}
	}

	@GetMapping("/summary")
	public TaskSummaryResponse summary() {
		return taskService.summary();
	}

	@GetMapping("/{id}")
	public TaskResponse findById(@PathVariable long id) {
		return taskService.findById(id);
	}

	@PutMapping("/{id}")
	public TaskResponse update(@PathVariable long id, @Valid @RequestBody UpdateTaskRequest request) {
		return taskService.update(id, request);
	}

	@PatchMapping("/{id}/status")
	public TaskResponse changeStatus(
			@PathVariable long id,
			@Valid @RequestBody ChangeTaskStatusRequest request
	) {
		return taskService.changeStatus(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		taskService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
