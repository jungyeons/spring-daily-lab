package com.jungyeons.dailylab.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class GrowthTaskTest {

	@Test
	void completingAndReopeningTaskUpdatesCompletionTimestamp() {
		GrowthTask task = GrowthTask.create("Write tests", null, TaskCategory.TEST, 3, LocalDate.now().plusDays(1));

		task.changeStatus(TaskStatus.DONE);
		assertThat(task.getCompletedAt()).isNotNull();

		task.changeStatus(TaskStatus.IN_PROGRESS);
		assertThat(task.getCompletedAt()).isNull();
	}

	@Test
	void overdueOnlyAppliesToIncompleteTasks() {
		GrowthTask task = GrowthTask.create("Review backlog", null, TaskCategory.LEARNING, 2, LocalDate.now().minusDays(1));

		assertThat(task.isOverdue(LocalDate.now())).isTrue();
		task.changeStatus(TaskStatus.DONE);
		assertThat(task.isOverdue(LocalDate.now())).isFalse();
	}

	@Test
	void priorityMustStayWithinSupportedRange() {
		assertThatThrownBy(() -> GrowthTask.create("Invalid", null, TaskCategory.FEATURE, 0, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("priority");
	}

	@Test
	void textFieldsMustStayWithinDatabaseColumnLimits() {
		assertThatThrownBy(() -> GrowthTask.create(
				"x".repeat(121),
				null,
				TaskCategory.FEATURE,
				3,
				null
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("title");

		GrowthTask task = GrowthTask.create("Valid", null, TaskCategory.FEATURE, 3, null);
		assertThatThrownBy(() -> task.update(
				"Valid",
				"x".repeat(2001),
				TaskCategory.FEATURE,
				3,
				null
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("description");
	}
}
