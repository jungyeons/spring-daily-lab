package com.jungyeons.dailylab.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.jungyeons.dailylab.common.InvalidTaskEtagException;

class TaskEtagTest {

	@Test
	void parsesStrongNumericTagsAndWildcard() {
		assertThat(TaskEtag.parseIfMatch("\"12\"")).isEqualTo(12L);
		assertThat(TaskEtag.parseIfMatch("*")).isNull();
		assertThat(TaskEtag.parseIfMatch(null)).isNull();
	}

	@Test
	void rejectsWeakMultipleAndMalformedTags() {
		assertThatThrownBy(() -> TaskEtag.parseIfMatch("W/\"1\""))
				.isInstanceOf(InvalidTaskEtagException.class);
		assertThatThrownBy(() -> TaskEtag.parseIfMatch("\"1\", \"2\""))
				.isInstanceOf(InvalidTaskEtagException.class);
		assertThatThrownBy(() -> TaskEtag.parseIfMatch("1"))
				.isInstanceOf(InvalidTaskEtagException.class);
	}
}
