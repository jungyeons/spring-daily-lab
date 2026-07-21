package com.jungyeons.dailylab.domain;

import java.time.LocalDate;

public enum RecurrenceFrequency {

	DAILY {
		@Override
		public LocalDate nextDueDate(LocalDate dueDate) {
			return dueDate.plusDays(1);
		}
	},
	WEEKLY {
		@Override
		public LocalDate nextDueDate(LocalDate dueDate) {
			return dueDate.plusWeeks(1);
		}
	},
	MONTHLY {
		@Override
		public LocalDate nextDueDate(LocalDate dueDate) {
			return dueDate.plusMonths(1);
		}
	};

	public abstract LocalDate nextDueDate(LocalDate dueDate);
}
