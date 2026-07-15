package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskCsvExportServiceTest {

	@Test
	void escapesCommasQuotesAndLineBreaks() {
		String row = TaskCsvExportService.row(
				1,
				"Plan, review",
				"Line \"one\"\nLine two",
				null
		);

		assertThat(row).isEqualTo("1,\"Plan, review\",\"Line \"\"one\"\"\nLine two\",");
	}

	@Test
	void leavesPlainValuesUnquoted() {
		assertThat(TaskCsvExportService.escape("FEATURE")).isEqualTo("FEATURE");
		assertThat(TaskCsvExportService.escape(4)).isEqualTo("4");
	}
}
