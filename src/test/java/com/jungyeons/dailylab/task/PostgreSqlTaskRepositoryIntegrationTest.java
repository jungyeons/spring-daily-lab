package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.jungyeons.dailylab.domain.TaskCategory;
import com.jungyeons.dailylab.task.api.CreateTaskRequest;
import com.jungyeons.dailylab.task.api.TaskResponse;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlTaskRepositoryIntegrationTest {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
			DockerImageName.parse("postgres:17-alpine")
	);

	@DynamicPropertySource
	static void databaseProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskRepository taskRepository;

	@Test
	void migratesAndPersistsTasksInPostgreSql() {
		TaskResponse created = taskService.create(new CreateTaskRequest(
				"Verify PostgreSQL persistence",
				"Run the integration suite against a real database",
				TaskCategory.TEST,
				4,
				LocalDate.now().plusDays(1)
		));

		assertThat(taskRepository.findById(created.id()))
				.isPresent()
				.get()
				.satisfies(task -> {
					assertThat(task.getTitle()).isEqualTo("Verify PostgreSQL persistence");
					assertThat(task.getCategory()).isEqualTo(TaskCategory.TEST);
				});
	}
}
