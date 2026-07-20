package com.jungyeons.dailylab.common;

public class IdempotencyKeyConflictException extends RuntimeException {

	public IdempotencyKeyConflictException() {
		super("Idempotency-Key was already used with a different request");
	}
}
