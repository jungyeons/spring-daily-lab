package com.jungyeons.dailylab.notification.local;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.jungyeons.dailylab.notification.EmailNotification;
import com.jungyeons.dailylab.notification.EmailNotificationPort;

@Component
@Profile("local")
public class InMemoryEmailNotificationAdapter implements EmailNotificationPort {

	private final Queue<EmailNotification> sentNotifications = new ConcurrentLinkedQueue<>();

	@Override
	public void send(EmailNotification notification) {
		sentNotifications.add(Objects.requireNonNull(notification, "notification must not be null"));
	}

	public List<EmailNotification> sentNotifications() {
		return List.copyOf(sentNotifications);
	}

	public void clear() {
		sentNotifications.clear();
	}
}
