package com.jungyeons.dailylab.task;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.domain.GrowthTask;

@Service
public class TaskCsvExportService {

	private static final String HEADER = "id,title,description,category,status,priority,dueDate,createdAt,updatedAt,completedAt,version\r\n";

	private final TaskRepository taskRepository;

	public TaskCsvExportService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Transactional(readOnly = true)
	public byte[] export() {
		StringBuilder csv = new StringBuilder(HEADER);
		taskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
				.forEach(task -> csv.append(toRow(task)).append("\r\n"));
		return csv.toString().getBytes(StandardCharsets.UTF_8);
	}

	private String toRow(GrowthTask task) {
		return row(
				task.getId(),
				task.getTitle(),
				task.getDescription(),
				task.getCategory(),
				task.getStatus(),
				task.getPriority(),
				task.getDueDate(),
				task.getCreatedAt(),
				task.getUpdatedAt(),
				task.getCompletedAt(),
				task.getVersion()
		);
	}

	static String row(Object... values) {
		return Arrays.stream(values)
				.map(TaskCsvExportService::escape)
				.collect(Collectors.joining(","));
	}

	static String escape(Object value) {
		if (value == null) {
			return "";
		}
		String text = value.toString();
		if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\r') >= 0 || text.indexOf('\n') >= 0) {
			return '"' + text.replace("\"", "\"\"") + '"';
		}
		return text;
	}
}
