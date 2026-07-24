package com.jungyeons.dailylab.task.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.jungyeons.dailylab.domain.TaskCategory;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

class TaskRequestValidationTest {

	@Test
	void allowsAnOverdueDateWhenUpdatingAnExistingTask() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			UpdateTaskRequest request = new UpdateTaskRequest(
					"Update overdue task",
					null,
					TaskCategory.FEATURE,
					3,
					LocalDate.now().minusDays(1)
			);

			assertThat(factory.getValidator().validate(request)).isEmpty();
		}
	}

	@Test
	void stillRejectsPastDueDatesForNewTasks() {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			CreateTaskRequest request = new CreateTaskRequest(
					"Create overdue task",
					null,
					TaskCategory.FEATURE,
					3,
					LocalDate.now().minusDays(1)
			);

			assertThat(factory.getValidator().validate(request))
					.extracting(violation -> violation.getPropertyPath().toString())
					.containsExactly("dueDate");
		}
	}
}
