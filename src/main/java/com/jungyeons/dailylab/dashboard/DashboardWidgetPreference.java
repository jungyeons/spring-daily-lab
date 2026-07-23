package com.jungyeons.dailylab.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "dashboard_widget_preferences",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_dashboard_widget_type", columnNames = {"dashboard_key", "widget_type"}),
				@UniqueConstraint(name = "uk_dashboard_widget_order", columnNames = {"dashboard_key", "display_order"})
		}
)
public class DashboardWidgetPreference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64)
	private String dashboardKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private DashboardWidgetType widgetType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private DashboardWidgetSize widgetSize;

	@Column(nullable = false)
	private int displayOrder;

	protected DashboardWidgetPreference() {
	}

	private DashboardWidgetPreference(
			String dashboardKey,
			DashboardWidgetType widgetType,
			DashboardWidgetSize widgetSize,
			int displayOrder
	) {
		this.dashboardKey = dashboardKey;
		this.widgetType = widgetType;
		this.widgetSize = widgetSize;
		this.displayOrder = displayOrder;
	}

	public static DashboardWidgetPreference create(
			String dashboardKey,
			DashboardWidgetType widgetType,
			DashboardWidgetSize widgetSize,
			int displayOrder
	) {
		return new DashboardWidgetPreference(dashboardKey, widgetType, widgetSize, displayOrder);
	}

	public DashboardWidgetType getWidgetType() {
		return widgetType;
	}

	public DashboardWidgetSize getWidgetSize() {
		return widgetSize;
	}

	public int getDisplayOrder() {
		return displayOrder;
	}
}
