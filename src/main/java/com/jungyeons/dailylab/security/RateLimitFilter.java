package com.jungyeons.dailylab.security;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private static final String LOGIN_PATH = "/api/v1/auth/login";
	private static final String TASKS_PATH = "/api/v1/tasks";

	private final RateLimitProperties properties;
	private final Clock clock;
	private final Map<String, RateLimitWindow> windows = new ConcurrentHashMap<>();

	@Autowired
	public RateLimitFilter(RateLimitProperties properties) {
		this(properties, Clock.systemUTC());
	}

	RateLimitFilter(RateLimitProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		RateLimitRule rule = ruleFor(request);
		if (rule == null || consume(rule, request.getRemoteAddr())) {
			filterChain.doFilter(request, response);
			return;
		}

		Duration window = properties.getWindow();
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setHeader("Retry-After", String.valueOf(Math.max(1, window.toSeconds())));
		response.setContentType("application/problem+json");
		response.getWriter().write("""
				{"type":"about:blank","title":"Rate limit exceeded","status":429,"detail":"Too many requests. Try again after the rate limit window expires."}
				""");
	}

	private RateLimitRule ruleFor(HttpServletRequest request) {
		String path = request.getRequestURI();
		if (LOGIN_PATH.equals(path) && "POST".equals(request.getMethod())) {
			return new RateLimitRule("login", properties.getLoginMaxRequests());
		}
		if (path.startsWith(TASKS_PATH) && isWriteMethod(request.getMethod())) {
			return new RateLimitRule("write", properties.getWriteMaxRequests());
		}
		return null;
	}

	private boolean consume(RateLimitRule rule, String remoteAddress) {
		Duration windowDuration = properties.getWindow();
		if (rule.maxRequests() < 1 || windowDuration.isNegative() || windowDuration.isZero()) {
			return false;
		}

		long now = clock.millis();
		long expiresAt = now + windowDuration.toMillis();
		String key = rule.name() + ':' + remoteAddress;
		RateLimitWindow current = windows.compute(key, (ignored, existing) -> {
			if (existing == null || existing.expiresAt() <= now) {
				return new RateLimitWindow(expiresAt, 1);
			}
			return new RateLimitWindow(existing.expiresAt(), existing.requestCount() + 1);
		});
		return current.requestCount() <= rule.maxRequests();
	}

	private static boolean isWriteMethod(String method) {
		return "POST".equals(method)
				|| "PUT".equals(method)
				|| "PATCH".equals(method)
				|| "DELETE".equals(method);
	}

	private record RateLimitRule(String name, int maxRequests) {
	}

	private record RateLimitWindow(long expiresAt, int requestCount) {
	}
}
