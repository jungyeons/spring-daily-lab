package com.jungyeons.dailylab.notification.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.jungyeons.dailylab.notification.EmailNotification;
import com.jungyeons.dailylab.notification.EmailNotificationPort;

@SpringBootTest
@ActiveProfiles("local")
class InMemoryEmailNotificationAdapterTest {

	@Autowired
	private EmailNotificationPort notificationPort;

	@Autowired
	private InMemoryEmailNotificationAdapter adapter;

	@BeforeEach
	void clearNotifications() {
		adapter.clear();
	}

	@Test
	void recordsNotificationsInDeliveryOrder() {
		EmailNotification first = new EmailNotification("first@example.com", "First", "First body");
		EmailNotification second = new EmailNotification("second@example.com", "Second", "Second body");

		notificationPort.send(first);
		notificationPort.send(second);

		assertThat(adapter.sentNotifications()).containsExactly(first, second);
	}

	@Test
	void rejectsIncompleteMessages() {
		assertThatThrownBy(() -> new EmailNotification(" ", "Subject", "Body"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("recipient must not be blank");
	}
}
