package com.jungyeons.dailylab.outbox;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "domain_event_outbox")
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64)
	private String aggregateType;

	@Column(nullable = false)
	private Long aggregateId;

	@Column(nullable = false, length = 64)
	private String eventType;

	@Column(nullable = false, columnDefinition = "text")
	private String payload;

	@Column(nullable = false, updatable = false)
	private Instant occurredAt;

	private Instant publishedAt;

	protected OutboxEvent() {
	}

	private OutboxEvent(String aggregateType, long aggregateId, String eventType, String payload) {
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.payload = payload;
	}

	public static OutboxEvent create(String aggregateType, long aggregateId, String eventType, String payload) {
		return new OutboxEvent(aggregateType, aggregateId, eventType, payload);
	}

	@PrePersist
	void onCreate() {
		occurredAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public Long getAggregateId() {
		return aggregateId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}
}
