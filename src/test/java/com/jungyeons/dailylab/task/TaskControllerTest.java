package com.jungyeons.dailylab.task;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@Test
	void rejectsSortPropertiesOutsideTheAllowlist() throws Exception {
		mockMvc.perform(get("/api/v1/tasks").param("sort", "owner,asc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Unsupported sort property"))
				.andExpect(jsonPath("$.detail").value("Sorting by 'owner' is not supported"));
	}
}
