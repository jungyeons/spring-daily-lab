package com.jungyeons.dailylab.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHeadersFilterTest {

	private final SecurityHeadersFilter filter = new SecurityHeadersFilter();

	@Test
	void addsSecurityAndNoStoreHeadersToApiResponses() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tasks");
		request.setSecure(true);
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
		assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
		assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
		assertThat(response.getHeader("Permissions-Policy"))
				.isEqualTo("camera=(), geolocation=(), microphone=()");
		assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
		assertThat(response.getHeader("Pragma")).isEqualTo("no-cache");
		assertThat(response.getHeader("Strict-Transport-Security"))
				.isEqualTo("max-age=31536000; includeSubDomains");
	}

	@Test
	void omitsHstsForPlainHttp() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getHeader("Strict-Transport-Security")).isNull();
		assertThat(response.getHeader("Cache-Control")).isEqualTo("no-store");
	}

	@Test
	void leavesLocalConsoleAndStaticPathsUntouched() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/h2-console/");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertThat(response.getHeaderNames()).isEmpty();
	}
}
