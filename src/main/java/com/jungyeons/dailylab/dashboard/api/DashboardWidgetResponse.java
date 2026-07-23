package com.jungyeons.dailylab.dashboard.api;

import com.jungyeons.dailylab.dashboard.DashboardWidgetPreference;
import com.jungyeons.dailylab.dashboard.DashboardWidgetSize;
import com.jungyeons.dailylab.dashboard.DashboardWidgetType;

public record DashboardWidgetResponse(
		DashboardWidgetType type,
		DashboardWidgetSize size,
		int position
) {
	public static DashboardWidgetResponse from(DashboardWidgetPreference preference) {
		return new DashboardWidgetResponse(
				preference.getWidgetType(),
				preference.getWidgetSize(),
				preference.getDisplayOrder()
		);
	}
}
