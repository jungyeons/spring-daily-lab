package com.jungyeons.dailylab;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GracefulShutdownIntegrationTest {

	@Test
	void waitsForAnActiveRequestBeforeStoppingTheServer() throws Exception {
		SpringApplication application = new SpringApplication(
				SpringDailyLabApplication.class,
				SlowRequestConfiguration.class
		);
		application.setDefaultProperties(Map.of(
				"server.port", "0",
				"server.shutdown", "graceful",
				"spring.lifecycle.timeout-per-shutdown-phase", "5s"
		));
		application.setRegisterShutdownHook(false);

		ConfigurableApplicationContext context = application.run(
				"--spring.datasource.url=jdbc:h2:mem:graceful-shutdown;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_LOWER=TRUE"
		);
		SlowRequestController controller = context.getBean(SlowRequestController.class);
		ExecutorService shutdownExecutor = Executors.newSingleThreadExecutor();

		try {
			int port = context.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
			HttpRequest request = HttpRequest.newBuilder(
					URI.create("http://localhost:" + port + "/test/slow")
			).GET().build();
			CompletableFuture<HttpResponse<String>> response = HttpClient.newHttpClient()
					.sendAsync(request, HttpResponse.BodyHandlers.ofString());

			assertThat(controller.awaitStarted(Duration.ofSeconds(5))).isTrue();

			CompletableFuture<Void> shutdown = CompletableFuture.runAsync(context::close, shutdownExecutor);
			Thread.sleep(200);
			assertThat(shutdown.isDone()).isFalse();

			controller.complete();

			assertThat(response.get(5, TimeUnit.SECONDS).statusCode()).isEqualTo(200);
			assertThat(response.get(5, TimeUnit.SECONDS).body()).isEqualTo("completed");
			shutdown.get(5, TimeUnit.SECONDS);
		} finally {
			controller.complete();
			if (context.isActive()) {
				context.close();
			}
			shutdownExecutor.shutdownNow();
		}
	}

	@Configuration(proxyBeanMethods = false)
	static class SlowRequestConfiguration {

		@Bean
		SlowRequestController slowRequestController() {
			return new SlowRequestController();
		}
	}

	@RestController
	static class SlowRequestController {

		private final CountDownLatch started = new CountDownLatch(1);
		private final CountDownLatch complete = new CountDownLatch(1);

		@GetMapping("/test/slow")
		String slowRequest() throws InterruptedException {
			started.countDown();
			if (!complete.await(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("test request did not receive completion signal");
			}
			return "completed";
		}

		boolean awaitStarted(Duration timeout) throws InterruptedException {
			return started.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
		}

		void complete() {
			complete.countDown();
		}
	}
}
