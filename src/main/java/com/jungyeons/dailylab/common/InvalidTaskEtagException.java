package com.jungyeons.dailylab.common;

public class InvalidTaskEtagException extends RuntimeException {

	public InvalidTaskEtagException() {
		super("If-Match must contain one strong numeric ETag or *");
	}
}
