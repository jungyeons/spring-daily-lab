package com.jungyeons.dailylab.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

	private final CorrelationIdFilter filter = new CorrelationIdFilter();

	@Test
	void propagatesASafeClientCorrelationIdToTheResponseAndLogContext() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.HEADER_NAME, "request-42.web");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
				assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isEqualTo("request-42.web"));

		assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("request-42.web");
		assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
	}

	@Test
	void generatesASafeCorrelationIdWhenTheRequestHeaderIsMissingOrUnsafe() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.HEADER_NAME, "untrusted\r\nvalue");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
				assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).matches("[A-Za-z0-9._-]{1,128}"));

		assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).matches("[A-Za-z0-9._-]{1,128}");
	}

}
