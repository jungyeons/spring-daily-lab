package com.jungyeons.dailylab.task;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.domain.TaskStatus;
import com.jungyeons.dailylab.task.api.ChangeTaskStatusRequest;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;
import com.jungyeons.dailylab.task.api.TaskSummaryResponse;
import com.jungyeons.dailylab.task.api.UpdateTaskRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

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
		return ResponseEntity.created(location).eTag(Long.toString(created.version())).body(created);
	}

	@GetMapping
	public Page<TaskResponse> findAll(
			@RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) TaskCategory category,
			@PageableDefault(size = 20, sort = "createdAt") Pageable pageable
	) {
		return taskService.findAll(status, category, pageable);
	}

	@GetMapping("/summary")
	public TaskSummaryResponse summary() {
		return taskService.summary();
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponse> findById(@PathVariable long id) {
		return withEtag(taskService.findById(id));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TaskResponse> update(
			@PathVariable long id,
			@Valid @RequestBody UpdateTaskRequest request,
			@RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
	) {
		return withEtag(taskService.update(id, request, TaskEtag.parseIfMatch(ifMatch)));
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<TaskResponse> changeStatus(
			@PathVariable long id,
			@Valid @RequestBody ChangeTaskStatusRequest request,
			@RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
	) {
		return withEtag(taskService.changeStatus(id, request, TaskEtag.parseIfMatch(ifMatch)));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
			@PathVariable long id,
			@RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch
	) {
		taskService.delete(id, TaskEtag.parseIfMatch(ifMatch));
		return ResponseEntity.noContent().build();
	}

	private ResponseEntity<TaskResponse> withEtag(TaskResponse response) {
		return ResponseEntity.ok().eTag(Long.toString(response.version())).body(response);
	}
}
