package com.jungyeons.dailylab.task.api;

import java.util.List;

public record CursorPageResponse<T>(
		List<T> items,
		Long nextCursor,
		boolean hasNext
) {
}
