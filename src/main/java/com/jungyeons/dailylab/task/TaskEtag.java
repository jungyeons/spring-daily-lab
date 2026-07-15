package com.jungyeons.dailylab.task;

import com.jungyeons.dailylab.common.InvalidTaskEtagException;

final class TaskEtag {

	private TaskEtag() {
	}

	static Long parseIfMatch(String value) {
		if (value == null) {
			return null;
		}
		String tag = value.trim();
		if (tag.equals("*")) {
			return null;
		}
		if (tag.startsWith("W/") || tag.indexOf(',') >= 0 || tag.length() < 3
				|| tag.charAt(0) != '"' || tag.charAt(tag.length() - 1) != '"') {
			throw new InvalidTaskEtagException();
		}
		try {
			long version = Long.parseLong(tag.substring(1, tag.length() - 1));
			if (version < 0) {
				throw new InvalidTaskEtagException();
			}
			return version;
		} catch (NumberFormatException exception) {
			throw new InvalidTaskEtagException();
		}
	}
}
