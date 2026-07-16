package com.jungyeons.dailylab;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(properties = "management.endpoint.health.show-details=always")
class HealthProbeIntegrationTest {

	@Autowired
	private WebApplicationContext applicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = webAppContextSetup(applicationContext).build();
	}

	@Test
	void exposesLivenessWithoutDatabaseDependency() throws Exception {
		mockMvc.perform(get("/actuator/health/liveness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components.livenessState.status").value("UP"));
	}

	@Test
	void exposesReadinessWithDatabaseHealth() throws Exception {
		mockMvc.perform(get("/actuator/health/readiness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components.readinessState.status").value("UP"))
				.andExpect(jsonPath("$.components.db.status").value("UP"));
	}
}
