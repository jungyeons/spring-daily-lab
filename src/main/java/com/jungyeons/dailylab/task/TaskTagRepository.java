package com.jungyeons.dailylab.task;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jungyeons.dailylab.domain.TaskTag;

public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {

	List<TaskTag> findByNameIn(Collection<String> names);
}
