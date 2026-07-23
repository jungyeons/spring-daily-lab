package com.jungyeons.dailylab.dashboard.api;

import java.util.List;

public record DashboardWidgetsResponse(
		String dashboardKey,
		List<DashboardWidgetResponse> widgets
) {
	public DashboardWidgetsResponse {
		widgets = List.copyOf(widgets);
	}
}
