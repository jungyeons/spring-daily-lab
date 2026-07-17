package com.jungyeons.dailylab.notification;

public interface EmailNotificationPort {

	void send(EmailNotification notification);
}
