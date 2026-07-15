package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

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
class TaskCsvExportIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskRepository taskRepository;

	@BeforeEach
	void clearDatabase() {
		taskRepository.deleteAll();
	}

	@Test
	void downloadsUtf8CsvWithEscapedTaskFields() throws Exception {
		taskRepository.save(GrowthTask.create(
				"Plan, review",
				"Line \"one\"\nLine two",
				TaskCategory.FEATURE,
				4,
				null
		));

		byte[] body = mockMvc.perform(get("/api/v1/tasks/exports/tasks.csv"))
				.andExpect(status().isOk())
				.andExpect(header().string("Content-Disposition", "attachment; filename=tasks.csv"))
				.andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
				.andReturn()
				.getResponse()
				.getContentAsByteArray();

		String csv = new String(body, StandardCharsets.UTF_8);
		assertThat(csv).startsWith("id,title,description,category,status,priority,dueDate,createdAt,updatedAt,completedAt,version\r\n");
		assertThat(csv).contains("\"Plan, review\",\"Line \"\"one\"\"\nLine two\",FEATURE,TODO,4");
		assertThat(csv).endsWith("\r\n");
	}
}
