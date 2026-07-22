package com.jungyeons.dailylab.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.rate-limit")
public class RateLimitProperties {

	private int loginMaxRequests = 5;
	private int writeMaxRequests = 60;
	private Duration window = Duration.ofMinutes(1);

	public int getLoginMaxRequests() {
		return loginMaxRequests;
	}

	public void setLoginMaxRequests(int loginMaxRequests) {
		this.loginMaxRequests = loginMaxRequests;
	}

	public int getWriteMaxRequests() {
		return writeMaxRequests;
	}

	public void setWriteMaxRequests(int writeMaxRequests) {
		this.writeMaxRequests = writeMaxRequests;
	}

	public Duration getWindow() {
		return window;
	}

	public void setWindow(Duration window) {
		this.window = window;
	}
}
