package com.jungyeons.dailylab.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.domain.GrowthTask;
import com.jungyeons.dailylab.domain.TaskCategory;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskBackupIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void clearDatabase() {
		taskRepository.deleteAll();
	}

	@Test
	void downloadsVersionedJsonBackup() throws Exception {
		taskRepository.save(GrowthTask.create("Preserve me", "Backup payload", TaskCategory.FEATURE, 5, null));

		mockMvc.perform(get("/api/v1/tasks/exports/backup.json"))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Disposition", "attachment; filename=tasks-backup-v1.json"))
				.andExpect(jsonPath("$.schemaVersion").value(1))
				.andExpect(jsonPath("$.exportedAt").isString())
				.andExpect(jsonPath("$.tasks[0].title").value("Preserve me"))
				.andExpect(jsonPath("$.tasks[0].version").value(0));
	}
}
