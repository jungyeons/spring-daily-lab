package com.jungyeons.dailylab.dashboard;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardWidgetPreferenceRepository extends JpaRepository<DashboardWidgetPreference, Long> {

	List<DashboardWidgetPreference> findByDashboardKeyOrderByDisplayOrderAsc(String dashboardKey);

	long deleteByDashboardKey(String dashboardKey);
}
