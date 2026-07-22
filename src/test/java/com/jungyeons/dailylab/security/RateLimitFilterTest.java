package com.jungyeons.dailylab.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

	@Test
	void rejectsTaskWritesAfterTheConfiguredLimit() throws Exception {
		RateLimitFilter filter = filterWithLimits(5, 2);

		MockHttpServletResponse first = filterTaskWrite(filter);
		MockHttpServletResponse second = filterTaskWrite(filter);
		MockHttpServletResponse rejected = filterTaskWrite(filter);

		assertThat(first.getStatus()).isEqualTo(200);
		assertThat(second.getStatus()).isEqualTo(200);
		assertThat(rejected.getStatus()).isEqualTo(429);
		assertThat(rejected.getHeader("Retry-After")).isEqualTo("60");
		assertThat(rejected.getContentAsString()).contains("Rate limit exceeded");
	}

	@Test
	void appliesSeparateLimitsToLoginAttempts() throws Exception {
		RateLimitFilter filter = filterWithLimits(1, 1);

		MockHttpServletResponse loginFirst = filterRequest(filter, "POST", "/api/v1/auth/login");
		MockHttpServletResponse loginRejected = filterRequest(filter, "POST", "/api/v1/auth/login");
		MockHttpServletResponse writeFirst = filterTaskWrite(filter);

		assertThat(loginFirst.getStatus()).isEqualTo(200);
		assertThat(loginRejected.getStatus()).isEqualTo(429);
		assertThat(writeFirst.getStatus()).isEqualTo(200);
	}

	@Test
	void doesNotRateLimitTaskReads() throws Exception {
		RateLimitFilter filter = filterWithLimits(1, 1);

		MockHttpServletResponse response = filterRequest(filter, "GET", "/api/v1/tasks");

		assertThat(response.getStatus()).isEqualTo(200);
	}

	private static RateLimitFilter filterWithLimits(int loginLimit, int writeLimit) {
		RateLimitProperties properties = new RateLimitProperties();
		properties.setLoginMaxRequests(loginLimit);
		properties.setWriteMaxRequests(writeLimit);
		properties.setWindow(Duration.ofMinutes(1));
		return new RateLimitFilter(
				properties,
				Clock.fixed(Instant.parse("2026-07-22T00:00:00Z"), ZoneOffset.UTC)
		);
	}

	private static MockHttpServletResponse filterTaskWrite(RateLimitFilter filter) throws Exception {
		return filterRequest(filter, "POST", "/api/v1/tasks");
	}

	private static MockHttpServletResponse filterRequest(
			RateLimitFilter filter,
			String method,
			String path
	) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(method, path);
		request.setRemoteAddr("192.0.2.1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		filter.doFilter(request, response, new MockFilterChain());
		return response;
	}
}
