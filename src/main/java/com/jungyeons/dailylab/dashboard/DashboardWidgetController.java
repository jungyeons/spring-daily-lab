package com.jungyeons.dailylab.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jungyeons.dailylab.dashboard.api.DashboardWidgetsResponse;
import com.jungyeons.dailylab.dashboard.api.UpdateDashboardWidgetsRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/dashboards/{dashboardKey}/widgets")
public class DashboardWidgetController {

	private final DashboardWidgetService dashboardWidgetService;

	public DashboardWidgetController(DashboardWidgetService dashboardWidgetService) {
		this.dashboardWidgetService = dashboardWidgetService;
	}

	@GetMapping
	public DashboardWidgetsResponse find(@PathVariable String dashboardKey) {
		return dashboardWidgetService.find(dashboardKey);
	}

	@PutMapping
	public DashboardWidgetsResponse replace(
			@PathVariable String dashboardKey,
			@Valid @RequestBody UpdateDashboardWidgetsRequest request
	) {
		return dashboardWidgetService.replace(dashboardKey, request);
	}
}
