package com.jungyeons.dailylab.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GrowthTaskDatabaseConstraintTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private long taskId;

	@BeforeEach
	void insertValidTask() {
		jdbcTemplate.update("""
				INSERT INTO growth_tasks (
				    title, description, category, status, priority, due_date, created_at, updated_at
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""",
				"Database invariant",
				null,
				"TEST",
				"TODO",
				3,
				null,
				Instant.parse("2026-07-15T00:00:00Z"),
				Instant.parse("2026-07-15T00:00:00Z")
		);
		taskId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM growth_tasks", Long.class);
	}

	@ParameterizedTest
	@CsvSource({
			"category, UNKNOWN",
			"status, BLOCKED",
			"priority, 0",
			"priority, 6"
	})
	void rejectsValuesOutsideDomainChecks(String column, String value) {
		Object databaseValue = column.equals("priority") ? Integer.valueOf(value) : value;

		assertThatThrownBy(() -> jdbcTemplate.update(
				"UPDATE growth_tasks SET " + column + " = ? WHERE id = ?",
				databaseValue,
				taskId
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@ParameterizedTest
	@ValueSource(strings = {"title", "category", "status", "priority", "created_at", "updated_at", "version"})
	void rejectsNullForRequiredColumns(String column) {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"UPDATE growth_tasks SET " + column + " = NULL WHERE id = ?",
				taskId
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsTextBeyondSchemaLimits() {
		assertThatThrownBy(() -> jdbcTemplate.update(
				"UPDATE growth_tasks SET title = ? WHERE id = ?",
				"x".repeat(121),
				taskId
		)).isInstanceOf(DataIntegrityViolationException.class);

		assertThatThrownBy(() -> jdbcTemplate.update(
				"UPDATE growth_tasks SET description = ? WHERE id = ?",
				"x".repeat(2001),
				taskId
		)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void suppliesTheInitialOptimisticLockVersion() {
		Long version = jdbcTemplate.queryForObject(
				"SELECT version FROM growth_tasks WHERE id = ?",
				Long.class,
				taskId
		);

		assertThat(version).isZero();
	}
}
