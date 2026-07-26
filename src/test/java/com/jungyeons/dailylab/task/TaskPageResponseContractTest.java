package com.jungyeons.dailylab.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskPageResponseContractTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void returnsStablePageMetadata() throws Exception {
		mockMvc.perform(get("/api/v1/tasks").param("page", "2").param("size", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.page.size").value(10))
				.andExpect(jsonPath("$.page.number").value(2))
				.andExpect(jsonPath("$.page.totalElements").value(0))
				.andExpect(jsonPath("$.page.totalPages").value(0))
				.andExpect(jsonPath("$.pageable").doesNotExist());
	}
}
