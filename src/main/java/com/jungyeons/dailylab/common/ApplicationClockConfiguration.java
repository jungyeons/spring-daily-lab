package com.jungyeons.dailylab.common;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ApplicationClockConfiguration {

	@Bean
	Clock applicationClock(@Value("${daily-lab.time-zone}") String timeZone) {
		return Clock.system(ZoneId.of(timeZone));
	}
}
