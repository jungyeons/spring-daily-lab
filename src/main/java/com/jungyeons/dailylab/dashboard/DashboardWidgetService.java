package com.jungyeons.dailylab.dashboard;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jungyeons.dailylab.dashboard.api.DashboardWidgetRequest;
import com.jungyeons.dailylab.dashboard.api.DashboardWidgetResponse;
import com.jungyeons.dailylab.dashboard.api.DashboardWidgetsResponse;
import com.jungyeons.dailylab.dashboard.api.UpdateDashboardWidgetsRequest;

@Service
@Transactional
public class DashboardWidgetService {

	private static final Pattern DASHBOARD_KEY_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,63}");

	private final DashboardWidgetPreferenceRepository repository;

	public DashboardWidgetService(DashboardWidgetPreferenceRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public DashboardWidgetsResponse find(String dashboardKey) {
		String normalizedKey = validateDashboardKey(dashboardKey);
		return toResponse(normalizedKey, repository.findByDashboardKeyOrderByDisplayOrderAsc(normalizedKey));
	}

	public DashboardWidgetsResponse replace(
			String dashboardKey,
			UpdateDashboardWidgetsRequest request
	) {
		String normalizedKey = validateDashboardKey(dashboardKey);
		validateUniqueTypes(request.widgets());

		repository.deleteByDashboardKey(normalizedKey);
		repository.flush();

		List<DashboardWidgetPreference> preferences = java.util.stream.IntStream
				.range(0, request.widgets().size())
				.mapToObj(position -> toPreference(normalizedKey, request.widgets().get(position), position))
				.toList();
		return toResponse(normalizedKey, repository.saveAll(preferences));
	}

	private static DashboardWidgetPreference toPreference(
			String dashboardKey,
			DashboardWidgetRequest request,
			int position
	) {
		return DashboardWidgetPreference.create(dashboardKey, request.type(), request.size(), position);
	}

	private static DashboardWidgetsResponse toResponse(
			String dashboardKey,
			List<DashboardWidgetPreference> preferences
	) {
		return new DashboardWidgetsResponse(
				dashboardKey,
				preferences.stream().map(DashboardWidgetResponse::from).toList()
		);
	}

	private static String validateDashboardKey(String dashboardKey) {
		if (dashboardKey == null || !DASHBOARD_KEY_PATTERN.matcher(dashboardKey).matches()) {
			throw new InvalidDashboardConfigurationException(
					"dashboardKey must contain 1-64 letters, numbers, underscores, or hyphens"
			);
		}
		return dashboardKey;
	}

	private static void validateUniqueTypes(List<DashboardWidgetRequest> widgets) {
		Set<DashboardWidgetType> types = new HashSet<>();
		for (DashboardWidgetRequest widget : widgets) {
			if (!types.add(widget.type())) {
				throw new InvalidDashboardConfigurationException(
						"Each widget type can appear only once"
				);
			}
		}
	}
}
