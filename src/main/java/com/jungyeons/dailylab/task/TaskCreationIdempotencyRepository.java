package com.jungyeons.dailylab.task;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCreationIdempotencyRepository extends JpaRepository<TaskCreationIdempotency, String> {
}
