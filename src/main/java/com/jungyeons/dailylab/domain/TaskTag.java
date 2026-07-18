package com.jungyeons.dailylab.domain;

import java.util.Locale;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "task_tags", uniqueConstraints = @UniqueConstraint(name = "uk_task_tags_name", columnNames = "name"))
public class TaskTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 40)
	private String name;

	protected TaskTag() {
	}

	private TaskTag(String name) {
		this.name = normalizeName(name);
	}

	public static TaskTag create(String name) {
		return new TaskTag(name);
	}

	public static String normalizeName(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("tag name must not be blank");
		}
		return value.trim().toLowerCase(Locale.ROOT);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof TaskTag tag)) {
			return false;
		}
		return Objects.equals(name, tag.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
