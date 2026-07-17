package com.jungyeons.dailylab.notification;

public record EmailNotification(String recipient, String subject, String body) {

	public EmailNotification {
		recipient = requireText(recipient, "recipient");
		subject = requireText(subject, "subject");
		body = requireText(body, "body");
	}

	private static String requireText(String value, String field) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(field + " must not be blank");
		}
		return value;
	}
}
