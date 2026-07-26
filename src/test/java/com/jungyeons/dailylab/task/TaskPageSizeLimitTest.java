package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskPageSizeLimitTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TaskService taskService;

	@Test
	void capsOversizedPageRequests() throws Exception {
		when(taskService.findAll(isNull(), isNull(), any(Pageable.class)))
				.thenReturn(Page.empty());

		mockMvc.perform(get("/api/v1/tasks").param("size", "100000"))
				.andExpect(status().isOk());

		ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
		verify(taskService).findAll(isNull(), isNull(), pageable.capture());
		assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
	}
}
