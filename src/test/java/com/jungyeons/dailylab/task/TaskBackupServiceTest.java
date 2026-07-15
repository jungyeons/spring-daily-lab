package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.Sort;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.task.api.TaskBackupResponse;

@MockitoSettings
class TaskBackupServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Test
	void exportsVersionedSnapshotWithStableTaskOrdering() {
		Clock clock = Clock.fixed(Instant.parse("2026-07-15T05:00:00Z"), ZoneOffset.UTC);
		TaskBackupService backupService = new TaskBackupService(taskRepository, clock);
		GrowthTask task = GrowthTask.create("Backup data", null, TaskCategory.LEARNING, 3, null);
		when(taskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))).thenReturn(List.of(task));

		TaskBackupResponse response = backupService.export();

		assertThat(response.schemaVersion()).isEqualTo(1);
		assertThat(response.exportedAt()).isEqualTo(Instant.parse("2026-07-15T05:00:00Z"));
		assertThat(response.tasks()).hasSize(1);
		assertThat(response.tasks().getFirst().title()).isEqualTo("Backup data");
	}
}
