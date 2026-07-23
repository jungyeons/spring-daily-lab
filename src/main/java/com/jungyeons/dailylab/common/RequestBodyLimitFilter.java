package com.jungyeons.dailylab.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestBodyLimitFilter extends OncePerRequestFilter {

	private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH");

	private final int maxRequestBodyBytes;
	private final ObjectMapper objectMapper;

	public RequestBodyLimitFilter(
			@Value("${app.http.max-request-body-size:1MB}") DataSize maxRequestBodySize,
			ObjectMapper objectMapper
	) {
		long bytes = maxRequestBodySize.toBytes();
		if (bytes < 1 || bytes >= Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"app.http.max-request-body-size must be between 1 byte and 2 GB"
			);
		}
		this.maxRequestBodyBytes = (int) bytes;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith("/api/")
				|| !BODY_METHODS.contains(request.getMethod())
				|| isMultipart(request);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		if (request.getContentLengthLong() > maxRequestBodyBytes) {
			writePayloadTooLarge(response, request);
			return;
		}

		byte[] body = request.getInputStream().readNBytes(maxRequestBodyBytes + 1);
		if (body.length > maxRequestBodyBytes) {
			writePayloadTooLarge(response, request);
			return;
		}

		filterChain.doFilter(new CachedBodyRequest(request, body), response);
	}

	private void writePayloadTooLarge(HttpServletResponse response, HttpServletRequest request) throws IOException {
		response.setStatus(HttpStatus.CONTENT_TOO_LARGE.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

		Map<String, Object> problem = new LinkedHashMap<>();
		problem.put("type", "about:blank");
		problem.put("title", "Payload too large");
		problem.put("status", HttpStatus.CONTENT_TOO_LARGE.value());
		problem.put(
				"detail",
				"Request body exceeds the configured limit of " + maxRequestBodyBytes + " bytes"
		);
		problem.put("instance", request.getRequestURI());
		objectMapper.writeValue(response.getOutputStream(), problem);
	}

	private static boolean isMultipart(HttpServletRequest request) {
		String contentType = request.getContentType();
		return contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).startsWith("multipart/");
	}

	private static final class CachedBodyRequest extends HttpServletRequestWrapper {

		private final byte[] body;

		private CachedBodyRequest(HttpServletRequest request, byte[] body) {
			super(request);
			this.body = body;
		}

		@Override
		public ServletInputStream getInputStream() {
			return new ByteArrayServletInputStream(body);
		}

		@Override
		public BufferedReader getReader() {
			Charset charset = getCharacterEncoding() == null
					? StandardCharsets.UTF_8
					: Charset.forName(getCharacterEncoding());
			return new BufferedReader(new InputStreamReader(getInputStream(), charset));
		}

		@Override
		public int getContentLength() {
			return body.length;
		}

		@Override
		public long getContentLengthLong() {
			return body.length;
		}
	}

	private static final class ByteArrayServletInputStream extends ServletInputStream {

		private final ByteArrayInputStream inputStream;

		private ByteArrayServletInputStream(byte[] body) {
			this.inputStream = new ByteArrayInputStream(body);
		}

		@Override
		public boolean isFinished() {
			return inputStream.available() == 0;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener readListener) {
			// Spring MVC reads request bodies synchronously.
		}

		@Override
		public int read() {
			return inputStream.read();
		}

		@Override
		public int read(byte[] bytes, int offset, int length) {
			return inputStream.read(bytes, offset, length);
		}
	}
}
