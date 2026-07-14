package com.jungyeons.dailylab.common;

public class InvalidSortPropertyException extends RuntimeException {

	public InvalidSortPropertyException(String property) {
		super("Sorting by '%s' is not supported".formatted(property));
	}
}
