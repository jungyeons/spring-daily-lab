package com.jungyeons.dailylab.dashboard.api;

import com.jungyeons.dailylab.dashboard.DashboardWidgetSize;
import com.jungyeons.dailylab.dashboard.DashboardWidgetType;

import jakarta.validation.constraints.NotNull;

public record DashboardWidgetRequest(
		@NotNull DashboardWidgetType type,
		@NotNull DashboardWidgetSize size
) {
}
