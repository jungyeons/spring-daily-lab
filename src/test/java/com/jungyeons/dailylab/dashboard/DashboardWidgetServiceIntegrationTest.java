package com.jungyeons.dailylab.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jungyeons.dailylab.dashboard.api.DashboardWidgetRequest;
import com.jungyeons.dailylab.dashboard.api.DashboardWidgetsResponse;
import com.jungyeons.dailylab.dashboard.api.UpdateDashboardWidgetsRequest;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardWidgetServiceIntegrationTest {

	@Autowired
	private DashboardWidgetService service;

	@Autowired
	private DashboardWidgetPreferenceRepository repository;

	@Autowired
	private MockMvc mockMvc;

	@BeforeEach
	void clearDatabase() {
		repository.deleteAll();
	}

	@Test
	void storesWidgetOrderAndSizePerDashboard() {
		DashboardWidgetsResponse saved = service.replace("primary", request(
				widget(DashboardWidgetType.TASK_SUMMARY, DashboardWidgetSize.SMALL),
				widget(DashboardWidgetType.OVERDUE_TASKS, DashboardWidgetSize.LARGE)
		));

		assertThat(saved.widgets()).extracting(widget -> widget.type()).containsExactly(
				DashboardWidgetType.TASK_SUMMARY,
				DashboardWidgetType.OVERDUE_TASKS
		);
		assertThat(saved.widgets()).extracting(widget -> widget.position()).containsExactly(0, 1);
		assertThat(service.find("primary")).isEqualTo(saved);
		assertThat(service.find("secondary").widgets()).isEmpty();
	}

	@Test
	void replacingPreferencesRemovesWidgetsNoLongerRequested() {
		service.replace("primary", request(
				widget(DashboardWidgetType.TASK_SUMMARY, DashboardWidgetSize.SMALL),
				widget(DashboardWidgetType.TODAY_TASKS, DashboardWidgetSize.MEDIUM)
		));

		DashboardWidgetsResponse replaced = service.replace("primary", request(
				widget(DashboardWidgetType.CATEGORY_BREAKDOWN, DashboardWidgetSize.LARGE)
		));

		assertThat(replaced.widgets()).singleElement().satisfies(widget -> {
			assertThat(widget.type()).isEqualTo(DashboardWidgetType.CATEGORY_BREAKDOWN);
			assertThat(widget.position()).isZero();
		});
		assertThat(repository.count()).isEqualTo(1);
	}

	@Test
	void rejectsDuplicateWidgetTypes() {
		assertThatThrownBy(() -> service.replace("primary", request(
				widget(DashboardWidgetType.TASK_SUMMARY, DashboardWidgetSize.SMALL),
				widget(DashboardWidgetType.TASK_SUMMARY, DashboardWidgetSize.LARGE)
		))).isInstanceOf(InvalidDashboardConfigurationException.class)
				.hasMessage("Each widget type can appear only once");

		assertThat(repository.count()).isZero();
	}

	@Test
	void replacesAndReadsPreferencesThroughHttp() throws Exception {
		mockMvc.perform(put("/api/v1/dashboards/team_alpha/widgets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "widgets": [
								    {"type": "TODAY_TASKS", "size": "MEDIUM"},
								    {"type": "TASK_SUMMARY", "size": "SMALL"}
								  ]
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dashboardKey").value("team_alpha"))
				.andExpect(jsonPath("$.widgets[0].type").value("TODAY_TASKS"))
				.andExpect(jsonPath("$.widgets[1].position").value(1));

		mockMvc.perform(get("/api/v1/dashboards/team_alpha/widgets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.widgets.length()").value(2))
				.andExpect(jsonPath("$.widgets[1].type").value("TASK_SUMMARY"));
	}

	private static UpdateDashboardWidgetsRequest request(DashboardWidgetRequest... widgets) {
		return new UpdateDashboardWidgetsRequest(List.of(widgets));
	}

	private static DashboardWidgetRequest widget(DashboardWidgetType type, DashboardWidgetSize size) {
		return new DashboardWidgetRequest(type, size);
	}
}
