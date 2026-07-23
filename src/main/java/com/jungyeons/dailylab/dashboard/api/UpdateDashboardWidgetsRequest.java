package com.jungyeons.dailylab.dashboard.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDashboardWidgetsRequest(
		@NotNull
		@Size(max = 12)
		List<@Valid DashboardWidgetRequest> widgets
) {
	public UpdateDashboardWidgetsRequest {
		widgets = widgets == null ? null : List.copyOf(widgets);
	}
}
