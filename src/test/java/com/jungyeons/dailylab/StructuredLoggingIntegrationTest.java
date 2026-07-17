package com.jungyeons.dailylab;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ops")
@ExtendWith(OutputCaptureExtension.class)
class StructuredLoggingIntegrationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(StructuredLoggingIntegrationTest.class);

	@Test
	void emitsEcsJsonWithServiceAndEventFields(CapturedOutput output) {
		LOGGER.atInfo()
				.addKeyValue("event.action", "structured-log-test")
				.log("structured logging verification");

		assertThat(output.getOut())
				.contains("\"message\":\"structured logging verification\"")
				.contains("\"name\":\"spring-daily-lab\"")
				.contains("\"event\":{\"action\":\"structured-log-test\"}")
				.contains("\"ecs\":");
	}
}
